package com.karakoc.sofra.ro;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Ro {

    @Id
    private String id;

    private String number;

    private String username;

    private boolean processed;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    // =========================================================
    // VEHICLE SNAPSHOT
    // =========================================================

    private Integer currentMileage;

    private Integer vehicleYear;

    private String vehicleMake;

    private String vehicleModel;

    private String vin;
}