package com.karakoc.sofra.serviceRecommendation;

import java.util.List;

public interface RecommendationService {

    Recommendation createRecommendation(
            String text,
            String name,
            String carServiceId,
            String laborCost,
            String opCode,
            String userId
    );

    List<Recommendation> getAllRecommendationsByServiceId(
            String carServiceId,
            String userId
    );

    void deleteRecommendation(
            String recommendationId,
            String userId
    );

    void deleteAllByCarServiceId(
            String carServiceId,
            String userId
    );
}