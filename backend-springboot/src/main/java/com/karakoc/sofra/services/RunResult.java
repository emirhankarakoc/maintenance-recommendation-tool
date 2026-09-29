package com.karakoc.sofra.services;

import com.karakoc.sofra.serviceRecommendation.Recommendation;

import java.time.LocalDate;
import java.util.List;

public record RunResult(

        String serviceId,

        String serviceName,

        ServiceType serviceType,

        Integer lastMileage,

        LocalDate lastDate,

        Integer milesSinceLastService,

        Long monthsSinceLastService,

        List<Recommendation> recommendations

) {}