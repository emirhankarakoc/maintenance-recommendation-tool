package com.karakoc.sofra.ro;

import java.util.List;

public interface RoService {

    void create(
            String roNumber,
            String username,
            Integer currentMileage,
            Integer vehicleYear,
            String vehicleMake,
            String vehicleModel,
            String vin
    );

    List<Ro> getAllRepairOrdersByUserEmail(
            String userEmail
    );

    Ro getByNumberForUser(
            String number,
            String userEmail
    );
}