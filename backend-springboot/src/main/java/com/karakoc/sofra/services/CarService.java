package com.karakoc.sofra.services;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class CarService {

    @Id
    private String id;

    private String name;

    /*
     * Example:
     * 30000
     */
    private int intervalMiles;

    /*
     * Example:
     * 24 months
     *
     * null = no time requirement
     */
    private Integer intervalMonths;

    private String creatorUserId;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private int recommendationCount;
}