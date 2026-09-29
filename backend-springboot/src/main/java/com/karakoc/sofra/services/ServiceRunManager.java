package com.karakoc.sofra.services;

import com.karakoc.sofra.chatgptapi.ChatgptApi;
import com.karakoc.sofra.exceptions.general.BadRequestException;
import com.karakoc.sofra.exceptions.general.NotfoundException;
import com.karakoc.sofra.ro.Ro;
import com.karakoc.sofra.ro.RoRepository;
import com.karakoc.sofra.security.UserPrincipal;
import com.karakoc.sofra.serviceRecommendation.Recommendation;
import com.karakoc.sofra.serviceRecommendation.RecommendationRepository;
import com.karakoc.sofra.serviceresult.RoServiceResult;
import com.karakoc.sofra.serviceresult.RoServiceResultRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ServiceRunManager {

    private final RoRepository roRepository;

    private final RoServiceResultRepository roServiceResultRepository;

    private final CarServiceRepository carServiceRepository;

    private final RecommendationRepository recommendationRepository;

    private final ChatgptApi chatgptApi;


    // ============================================================
    // RUN
    // ============================================================

    @Transactional
    public List<RunResult> run(
            RunRequest request,
            UserPrincipal user
    ) {

        // ========================================================
        // 1. FIND REPAIR ORDER
        // ========================================================

        Ro ro = roRepository
                .findByNumber(request.roNumber())
                .orElseThrow(() ->
                        new NotfoundException(
                                "Repair order not found: "
                                        + request.roNumber()
                        )
                );


        // ========================================================
        // 2. SECURITY
        // ========================================================

        if (!Objects.equals(
                ro.getUsername(),
                user.getEmail()
        )) {

            throw new BadRequestException(
                    "This repair order does not belong to you."
            );
        }


        // ========================================================
        // 3. ONE RO = ONE RUN
        // ========================================================

        if (ro.isProcessed()) {

            throw new BadRequestException(
                    "This repair order has already been processed."
            );
        }


        // ========================================================
        // 4. GET USER SERVICES
        // ========================================================

        List<CarService> allServices =
                carServiceRepository
                        .findAllByCreatorUserId(
                                user.getUserId()
                        );


        // ========================================================
        // 5. PREPARE SERVICES FOR AI PARSER
        // ========================================================

        List<ChatgptApi.TrackedService> parserServices =
                allServices
                        .stream()
                        .map(service ->
                                new ChatgptApi.TrackedService(
                                        service.getId(),
                                        service.getName()
                                )
                        )
                        .toList();


        // ========================================================
        // 6. PARSE SERVICE HISTORY
        // ========================================================

        ChatgptApi.ParserResult parserResult =
                chatgptApi.parse(
                        new ChatgptApi.ParserQuery(
                                request.serviceHistory(),
                                parserServices
                        )
                );


        List<ChatgptApi.ParsedService> parsedServices =
                parserResult.services == null
                        ? List.of()
                        : parserResult.services;


        // ========================================================
        // 7. RESULT LISTS
        // ========================================================

        List<RunResult> results =
                new ArrayList<>();

        List<RoServiceResult> databaseResults =
                new ArrayList<>();


        LocalDate today =
                LocalDate.now();


        // ========================================================
        // 8. CHECK EVERY SERVICE
        // ========================================================

        for (CarService service : allServices) {

            // ====================================================
            // FIND PARSED HISTORY FOR THIS SERVICE
            // ====================================================

            ChatgptApi.ParsedService parsed =
                    parsedServices
                            .stream()
                            .filter(parsedService ->
                                    Objects.equals(
                                            parsedService.carServiceId,
                                            service.getId()
                                    )
                            )
                            .findFirst()
                            .orElse(null);


            // ====================================================
            // LAST SERVICE INFORMATION
            // ====================================================

            Integer lastMileage = null;
            LocalDate lastDate = null;


            if (parsed != null) {

                lastMileage =
                        parsed.lastMileage;

                lastDate =
                        parseDate(
                                parsed.lastDate
                        );
            }


            // ====================================================
            // NEVER PERFORMED
            // ====================================================

            boolean neverPerformed =
                    lastMileage == null
                            && lastDate == null;


            // ====================================================
            // MILES SINCE LAST SERVICE
            // ====================================================

            int milesSinceLastService;


            if (neverPerformed) {

                /*
                 * No history exists.
                 *
                 * Example:
                 *
                 * Current mileage = 50,000
                 * Spark plugs interval = 104,000
                 *
                 * milesSinceLastService = 50,000
                 *
                 * isDue() will return false.
                 */

                milesSinceLastService =
                        Math.max(
                                0,
                                request.currentMileage()
                        );

            } else if (lastMileage != null) {

                /*
                 * Service was performed before.
                 *
                 * Example:
                 *
                 * Current mileage = 80,000
                 * Last service = 50,000
                 *
                 * Miles since = 30,000
                 */

                milesSinceLastService =
                        Math.max(
                                0,
                                request.currentMileage()
                                        - lastMileage
                        );

            } else {

                /*
                 * We know service date,
                 * but parser did not find mileage.
                 *
                 * Do not guess mileage.
                 */

                milesSinceLastService = 0;
            }


            // ====================================================
            // MONTHS SINCE LAST SERVICE
            // ====================================================

            LocalDate dateBaseline;


            if (lastDate != null) {

                /*
                 * Previously performed service:
                 *
                 * use last service date.
                 */

                dateBaseline =
                        lastDate;

            } else {

                /*
                 * Never performed:
                 *
                 * use vehicle in-service date.
                 *
                 * This allows time-based maintenance to work
                 * correctly on newer vehicles.
                 */

                dateBaseline =
                        request.vehicleInServiceDate();
            }


            Long monthsSinceLastService = null;


            if (dateBaseline != null) {

                monthsSinceLastService =
                        Math.max(
                                0,
                                ChronoUnit.MONTHS.between(
                                        dateBaseline,
                                        today
                                )
                        );
            }


            // ====================================================
            // 9. CHECK INTERVAL
            // ====================================================

            /*
             * IMPORTANT:
             *
             * Every service goes through isDue().
             *
             * There is NO:
             *
             * DIRECT_ADD + never performed = automatically due
             *
             * rule anymore.
             *
             *
             * Example:
             *
             * Current mileage:
             * 50,000
             *
             * Service interval:
             * 104,000
             *
             * Result:
             * NOT DUE
             */

            System.out.println(
                    "\n================ DUE CHECK ================" +
                            "\nSERVICE: " + service.getName() +
                            "\nSERVICE ID: " + service.getId() +
                            "\nTYPE: " + service.getServiceType() +
                            "\nCURRENT MILEAGE: " + request.currentMileage() +
                            "\nLAST MILEAGE: " + lastMileage +
                            "\nMILES SINCE: " + milesSinceLastService +
                            "\nINTERVAL MILES: " + service.getIntervalMiles() +
                            "\nMONTHS SINCE: " + monthsSinceLastService +
                            "\nINTERVAL MONTHS: " + service.getIntervalMonths() +
                            "\n===========================================\n"
            );
            boolean due =
                    isDue(
                            service,
                            milesSinceLastService,
                            monthsSinceLastService
                    );


            // ====================================================
            // 10. SKIP IF NOT DUE
            // ====================================================

            if (!due) {
                continue;
            }


            // ====================================================
            // 11. GET RECOMMENDATIONS
            // ====================================================

            List<Recommendation> recommendations =
                    recommendationRepository
                            .findAllByCarServiceId(
                                    service.getId()
                            );


            // ====================================================
            // 12. CREATE API RESULT
            // ====================================================

            RunResult result =
                    new RunResult(
                            service.getId(),
                            service.getName(),
                            service.getServiceType(),
                            lastMileage,
                            lastDate,
                            milesSinceLastService,
                            monthsSinceLastService,
                            recommendations
                    );


            results.add(
                    result
            );


            // ====================================================
            // 13. CREATE DATABASE RESULT
            // ====================================================

            RoServiceResult databaseResult =
                    new RoServiceResult();


            databaseResult.setId(
                    UUID.randomUUID().toString()
            );


            databaseResult.setRoId(
                    ro.getId()
            );


            databaseResult.setServiceId(
                    service.getId()
            );


            databaseResult.setServiceName(
                    service.getName()
            );


            databaseResult.setServiceType(
                    service.getServiceType()
            );


            databaseResult.setLastMileage(
                    lastMileage
            );


            databaseResult.setLastDate(
                    lastDate
            );


            databaseResult.setMilesSinceLastService(
                    milesSinceLastService
            );


            databaseResult.setMonthsSinceLastService(
                    monthsSinceLastService
            );


            databaseResults.add(
                    databaseResult
            );
        }


        // ========================================================
        // 14. SAVE RESULTS
        // ========================================================

        roServiceResultRepository.saveAll(
                databaseResults
        );


        // ========================================================
        // 15. MARK RO PROCESSED
        // ========================================================

        ro.setProcessed(
                true
        );


        ro.setProcessedAt(
                LocalDateTime.now()
        );


        roRepository.save(
                ro
        );


        // ========================================================
        // 16. RETURN
        // ========================================================

        return results;
    }


    // ============================================================
    // DUE CALCULATION
    // ============================================================

    private boolean isDue(
            CarService service,
            int milesSinceLastService,
            Long monthsSinceLastService
    ) {



        int intervalMiles =
                service.getIntervalMiles();


        Integer intervalMonths =
                service.getIntervalMonths();


        boolean hasMileageInterval =
                intervalMiles > 0;


        boolean hasTimeInterval =
                intervalMonths != null
                        && intervalMonths > 0;


        // ========================================================
        // NO INTERVAL
        // ========================================================

        if (
                !hasMileageInterval
                        && !hasTimeInterval
        ) {

            return false;
        }


        // ========================================================
        // MILEAGE REQUIREMENT
        // ========================================================

        /*
         * If mileage interval exists,
         * mileage MUST reach that interval.
         *
         *
         * interval = 104,000
         *
         * 50,000
         * => false
         *
         * 103,999
         * => false
         *
         * 104,000
         * => continue
         */

        if (
                hasMileageInterval
                        && milesSinceLastService
                        < intervalMiles
        ) {

            return false;
        }


        // ========================================================
        // TIME REQUIREMENT
        // ========================================================

        /*
         * If time interval exists,
         * time MUST also reach that interval.
         *
         * If date information is missing,
         * we cannot prove the time requirement,
         * therefore it is NOT due.
         */

        if (hasTimeInterval) {

            if (monthsSinceLastService == null) {
                return false;
            }


            if (
                    monthsSinceLastService
                            < intervalMonths
            ) {

                return false;
            }
        }


        // ========================================================
        // ALL CONFIGURED REQUIREMENTS PASSED
        // ========================================================

        return true;
    }


    // ============================================================
    // GET SAVED RESULTS
    // ============================================================

    public RoResultsResponse getResults(
            String roNumber,
            UserPrincipal user
    ) {

        // ========================================================
        // FIND RO
        // ========================================================

        Ro ro =
                roRepository
                        .findByNumber(
                                roNumber
                        )
                        .orElseThrow(() ->
                                new NotfoundException(
                                        "Repair order not found: "
                                                + roNumber
                                )
                        );


        // ========================================================
        // SECURITY
        // ========================================================

        if (!Objects.equals(
                ro.getUsername(),
                user.getEmail()
        )) {

            throw new BadRequestException(
                    "This repair order does not belong to you."
            );
        }


        // ========================================================
        // MUST BE PROCESSED
        // ========================================================

        if (!ro.isProcessed()) {

            throw new BadRequestException(
                    "This repair order has not been processed yet."
            );
        }


        // ========================================================
        // LOAD RESULTS
        // ========================================================

        List<RunResult> results =
                roServiceResultRepository
                        .findAllByRoId(
                                ro.getId()
                        )
                        .stream()
                        .map(savedResult -> {

                            List<Recommendation> recommendations =
                                    recommendationRepository
                                            .findAllByCarServiceId(
                                                    savedResult.getServiceId()
                                            );


                            return new RunResult(
                                    savedResult.getServiceId(),
                                    savedResult.getServiceName(),
                                    savedResult.getServiceType(),
                                    savedResult.getLastMileage(),
                                    savedResult.getLastDate(),
                                    savedResult.getMilesSinceLastService(),
                                    savedResult.getMonthsSinceLastService(),
                                    recommendations
                            );
                        })
                        .toList();


        // ========================================================
        // RETURN RO + RESULTS
        // ========================================================

        return new RoResultsResponse(
                ro,
                results
        );
    }


    // ============================================================
    // DATE PARSER
    // ============================================================

    private LocalDate parseDate(
            String date
    ) {

        if (
                date == null
                        || date.isBlank()
        ) {

            return null;
        }


        // ========================================================
        // ISO
        //
        // 2026-08-12
        // ========================================================

        try {

            return LocalDate.parse(
                    date
            );

        } catch (DateTimeParseException ignored) {
        }


        // ========================================================
        // US
        //
        // 08/12/2026
        // ========================================================

        try {

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "MM/dd/yyyy"
                    );


            return LocalDate.parse(
                    date,
                    formatter
            );

        } catch (DateTimeParseException ignored) {
        }


        return null;
    }


    // ============================================================
    // RESPONSE
    // ============================================================

    public record RoResultsResponse(
            Ro ro,
            List<RunResult> results
    ) {
    }
}