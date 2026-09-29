package com.karakoc.sofra.serviceresult;

import com.karakoc.sofra.services.ServiceType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class RoServiceResult {

    @Id
    private String id;

    /*
     * Links result to RO.
     */
    private String roId;

    private String serviceId;

    private String serviceName;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private Integer lastMileage;

    private LocalDate lastDate;

    private Integer milesSinceLastService;

    private Long monthsSinceLastService;
}