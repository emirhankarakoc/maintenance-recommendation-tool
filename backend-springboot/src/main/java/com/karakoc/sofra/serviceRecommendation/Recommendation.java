package com.karakoc.sofra.serviceRecommendation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Recommendation {

    @Id
    private String id;

    private String name;

    private String carServiceId;

    private String text;

    // Example: "1.5"
    private String laborCost;

    // Example: "BF123"
    private String opCode;
}