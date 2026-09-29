package com.karakoc.sofra.ro;

import com.karakoc.sofra.exceptions.general.BadRequestException;
import com.karakoc.sofra.exceptions.general.NotfoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoManager implements RoService {

    private final RoRepository roRepository;

    @Override
    public void create(
            String roNumber,
            String username,
            Integer currentMileage,
            Integer vehicleYear,
            String vehicleMake,
            String vehicleModel,
            String vin
    ) {

        // =====================================================
        // VALIDATE RO NUMBER
        // =====================================================

        if (roNumber == null || roNumber.isBlank()) {

            throw new BadRequestException(
                    "RO number is required."
            );
        }

        // =====================================================
        // DUPLICATE CHECK
        // =====================================================

        if (roRepository.existsByNumber(roNumber)) {

            throw new BadRequestException(
                    "This RO number is already in use."
            );
        }

        // =====================================================
        // CREATE RO
        // =====================================================

        Ro ro = new Ro();

        ro.setId(
                UUID.randomUUID().toString()
        );

        ro.setNumber(
                roNumber.trim()
        );

        ro.setUsername(
                username
        );

        // =====================================================
        // VEHICLE SNAPSHOT
        // =====================================================

        ro.setCurrentMileage(
                currentMileage
        );

        ro.setVehicleYear(
                vehicleYear
        );

        ro.setVehicleMake(
                cleanString(vehicleMake)
        );

        ro.setVehicleModel(
                cleanString(vehicleModel)
        );

        ro.setVin(
                cleanString(vin)
        );

        // =====================================================
        // PROCESSING STATE
        // =====================================================

        ro.setProcessed(
                false
        );

        ro.setProcessedAt(
                null
        );

        ro.setCreatedAt(
                LocalDateTime.now()
        );

        // =====================================================
        // SAVE
        // =====================================================

        roRepository.save(ro);
    }

    // =========================================================
    // GET ALL REPAIR ORDERS FOR USER
    // =========================================================

    @Override
    public List<Ro> getAllRepairOrdersByUserEmail(
            String userEmail
    ) {

        return roRepository.findAllByUsername(
                userEmail,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );
    }

    // =========================================================
    // GET ONE REPAIR ORDER
    // =========================================================

    @Override
    public Ro getByNumberForUser(
            String number,
            String userEmail
    ) {

        Ro ro = roRepository
                .findByNumber(number)
                .orElseThrow(() ->
                        new NotfoundException(
                                "Repair order not found."
                        )
                );

        if (!ro.getUsername().equals(userEmail)) {

            throw new BadRequestException(
                    "This repair order does not belong to you."
            );
        }

        return ro;
    }

    // =========================================================
    // STRING CLEANUP
    // =========================================================

    private String cleanString(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value.trim();

        if (cleaned.isEmpty()) {
            return null;
        }

        return cleaned;
    }
}