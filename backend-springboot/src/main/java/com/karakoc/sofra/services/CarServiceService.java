package com.karakoc.sofra.services;

import com.karakoc.sofra.security.UserPrincipal;

import java.util.List;

public interface CarServiceService {

    CarService create(
            String name,
            int intervalMiles,
            Integer intervalMonths,
            ServiceType serviceType,
            String userId
    );

    List<CarService> getAllByUserId(
            String userId
    );

    void delete(
            String serviceId,
            String userId
    );
}