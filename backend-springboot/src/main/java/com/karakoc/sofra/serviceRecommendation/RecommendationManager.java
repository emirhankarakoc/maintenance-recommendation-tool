package com.karakoc.sofra.serviceRecommendation;

import com.karakoc.sofra.exceptions.general.BadRequestException;
import com.karakoc.sofra.exceptions.general.NotfoundException;
import com.karakoc.sofra.services.CarService;
import com.karakoc.sofra.services.CarServiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RecommendationManager implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final CarServiceRepository carServiceRepository;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    @Transactional
    public Recommendation createRecommendation(
            String text,
            String name,
            String carServiceId,
            String laborCost,
            String opCode,
            String userId
    ) {

        CarService carService =
                carServiceRepository
                        .findById(carServiceId)
                        .orElseThrow(() ->
                                new NotfoundException(
                                        "Car service not found."
                                )
                        );


        // make sure service belongs to user
        if (!carService.getCreatorUserId().equals(userId)) {
            throw new BadRequestException(
                    "This service does not belong to you."
            );
        }


        Recommendation recommendation =
                new Recommendation();

        recommendation.setId(
                UUID.randomUUID().toString()
        );

        recommendation.setText(text);
        recommendation.setName(name);
        recommendation.setCarServiceId(carServiceId);

        recommendation.setLaborCost(laborCost);
        recommendation.setOpCode(opCode);


        recommendationRepository.save(
                recommendation
        );


        carService.setRecommendationCount(
                carService.getRecommendationCount() + 1
        );

        carServiceRepository.save(
                carService
        );


        return recommendation;
    }


    // =========================================================
    // GET ALL FOR SERVICE
    // =========================================================

    @Override
    public List<Recommendation> getAllRecommendationsByServiceId(
            String carServiceId,
            String userId
    ) {

        CarService carService =
                carServiceRepository
                        .findById(carServiceId)
                        .orElseThrow(() ->
                                new NotfoundException(
                                        "Car service not found."
                                )
                        );


        if (!carService.getCreatorUserId().equals(userId)) {
            throw new BadRequestException(
                    "This service does not belong to you."
            );
        }


        return recommendationRepository
                .findAllByCarServiceId(
                        carServiceId
                );
    }


    // =========================================================
    // DELETE ONE
    // =========================================================

    @Override
    @Transactional
    public void deleteRecommendation(
            String recommendationId,
            String userId
    ) {

        Recommendation recommendation =
                recommendationRepository
                        .findById(recommendationId)
                        .orElseThrow(() ->
                                new NotfoundException(
                                        "Recommendation not found."
                                )
                        );


        CarService carService =
                carServiceRepository
                        .findById(
                                recommendation.getCarServiceId()
                        )
                        .orElseThrow(() ->
                                new NotfoundException(
                                        "Car service not found."
                                )
                        );


        if (!carService.getCreatorUserId().equals(userId)) {
            throw new BadRequestException(
                    "This recommendation does not belong to you."
            );
        }


        recommendationRepository.delete(
                recommendation
        );


        carService.setRecommendationCount(
                Math.max(
                        0,
                        carService.getRecommendationCount() - 1
                )
        );


        carServiceRepository.save(
                carService
        );
    }


    // =========================================================
    // DELETE ALL RECS FOR SERVICE
    // =========================================================

    @Override
    @Transactional
    public void deleteAllByCarServiceId(
            String carServiceId,
            String userId
    ) {

        CarService carService =
                carServiceRepository
                        .findById(carServiceId)
                        .orElseThrow(() ->
                                new NotfoundException(
                                        "Car service not found."
                                )
                        );


        if (!carService.getCreatorUserId().equals(userId)) {
            throw new BadRequestException(
                    "This service does not belong to you."
            );
        }


        List<Recommendation> recommendations =
                recommendationRepository
                        .findAllByCarServiceId(
                                carServiceId
                        );


        recommendationRepository.deleteAll(
                recommendations
        );


        carService.setRecommendationCount(0);

        carServiceRepository.save(
                carService
        );
    }
}