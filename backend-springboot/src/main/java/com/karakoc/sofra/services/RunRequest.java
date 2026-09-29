package com.karakoc.sofra.services;


import java.time.LocalDate;

public record RunRequest(

        String roNumber,

        int currentMileage,

        LocalDate vehicleInServiceDate,

        String serviceHistory

) {}
