package com.karakoc.sofra.services;

import com.karakoc.sofra.exceptions.general.BadRequestException;
import com.karakoc.sofra.exceptions.general.NotfoundException;
import com.karakoc.sofra.serviceRecommendation.RecommendationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CarServiceManager implements CarServiceService {

    private final CarServiceRepository carServiceRepository;

    private final RecommendationService recommendationService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public CarService create(
            String name,
            int intervalMiles,
            Integer intervalMonths,
            ServiceType serviceType,
            String userId
    ) {

        CarService carService =
                new CarService();


        carService.setId(
                UUID.randomUUID().toString()
        );


        carService.setName(
                name
        );


        carService.setIntervalMiles(
                intervalMiles
        );


        carService.setIntervalMonths(
                intervalMonths
        );


        carService.setCreatorUserId(
                userId
        );


        carService.setServiceType(
                serviceType
        );


        carService.setRecommendationCount(
                0
        );


        return carServiceRepository.save(
                carService
        );
    }


    // =========================================================
    // GET ALL MY SERVICES
    // =========================================================

    @Override
    public List<CarService> getAllByUserId(
            String userId
    ) {

        return carServiceRepository
                .findAllByCreatorUserId(
                        userId
                );
    }


    // =========================================================
    // DELETE SERVICE
    // =========================================================

    @Override
    @Transactional
    public void delete(
            String serviceId,
            String userId
    ) {

        CarService service =
                carServiceRepository
                        .findById(
                                serviceId
                        )
                        .orElseThrow(() ->
                                new NotfoundException(
                                        "Service not found."
                                )
                        );


        if (
                !service
                        .getCreatorUserId()
                        .equals(userId)
        ) {

            throw new BadRequestException(
                    "This service does not belong to you."
            );
        }


        /*
         * First delete all recommendations
         * belonging to this service.
         */
        recommendationService
                .deleteAllByCarServiceId(
                        serviceId,
                        userId
                );


        /*
         * Then delete the service.
         */
        carServiceRepository.delete(
                service
        );
    }
}