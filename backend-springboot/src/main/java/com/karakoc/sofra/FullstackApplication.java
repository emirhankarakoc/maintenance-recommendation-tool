package com.karakoc.sofra;

import com.karakoc.sofra.account.AuthManager;
import com.karakoc.sofra.account.requests.LoginResponse;
import com.karakoc.sofra.security.UserPrincipal;
import com.karakoc.sofra.serviceRecommendation.Recommendation;
import com.karakoc.sofra.serviceRecommendation.RecommendationRepository;
import com.karakoc.sofra.services.CarService;
import com.karakoc.sofra.services.CarServiceRepository;
import com.karakoc.sofra.services.ServiceType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
public class FullstackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackApplication.class, args);
    }


    @Bean
    CommandLineRunner demoUser(
            AuthManager authManager,
            CarServiceRepository carServiceRepository,
            RecommendationRepository recommendationRepository
    ) {

        return args -> {

            // =========================
            // DEMO USER
            // =========================

            try {
                authManager.attemptRegister("patron", "1234");
            } catch (Exception ignored) {
            }

            LoginResponse login =
                    authManager.attemptLogin("patron", "1234");

            System.out.println("DEMO LOGIN TOKEN:");
            System.out.println(login.getAccessToken());


            UserPrincipal principal =
                    (UserPrincipal) SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getPrincipal();

            String userId = principal.getUserId();


            // =========================
            // SERVICES
            // =========================

            createService(
                    carServiceRepository,
                    userId,
                    "cvt-fluid",
                    "CVT Transmission Fluid",
                    30000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "automatic-trans-fluid",
                    "Automatic Transmission Fluid",
                    50000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "rear-differential",
                    "Rear Differential Fluid",
                    30000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "alignment",
                    "4-Wheel Alignment",
                    15000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "fuel-induction",
                    "Fuel Induction Service",
                    50000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "spark-plugs",
                    "Spark Plugs",
                    104000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "valve-adjustment",
                    "Valve Adjustment",
                    104000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "timing-belt",
                    "Timing Belt",
                    104000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "serpentine-belt",
                    "Serpentine Belt",
                    104000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "coolant",
                    "Engine Coolant",
                    104000,
                    ServiceType.DIRECT_ADD
            );

            createService(
                    carServiceRepository,
                    userId,
                    "brake-fluid",
                    "Brake Fluid",
                    30000,
                    ServiceType.INSPECTION
            );

            createService(
                    carServiceRepository,
                    userId,
                    "engine-air-filter",
                    "Engine Air Filter",
                    10000,
                    ServiceType.INSPECTION
            );

            createService(
                    carServiceRepository,
                    userId,
                    "cabin-air-filter",
                    "Cabin Air Filter",
                    10000,
                    ServiceType.INSPECTION
            );




            // =========================
            // INITIAL RECOMMENDATIONS
            // =========================

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "cvt-fluid-maintenance-cost",
                    "cvt-fluid",
                    "MAINTENANCE COST",
                    "Regular CVT fluid service can help reduce long-term transmission repair costs."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "automatic-trans-fluid-maintenance-cost",
                    "automatic-trans-fluid",
                    "MAINTENANCE COST",
                    "Regular transmission fluid service helps protect transmission components and reduce future repair costs."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "rear-differential-maintenance-cost",
                    "rear-differential",
                    "MAINTENANCE COST",
                    "Fresh differential fluid helps protect internal components and reduce expensive drivetrain repairs."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "alignment-maintenance-cost",
                    "alignment",
                    "MAINTENANCE COST",
                    "Proper alignment helps prevent premature tire wear and can extend the life of your tires."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "fuel-induction-maintenance-cost",
                    "fuel-induction",
                    "MAINTENANCE COST",
                    "Periodic fuel system cleaning can help maintain performance and reduce buildup over time."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "spark-plugs-maintenance-cost",
                    "spark-plugs",
                    "MAINTENANCE COST",
                    "Replacing worn spark plugs helps prevent poor performance and potential future repair costs."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "valve-adjustment-maintenance-cost",
                    "valve-adjustment",
                    "MAINTENANCE COST",
                    "Proper valve clearance helps maintain engine performance and prevent unnecessary engine wear."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "timing-belt-maintenance-cost",
                    "timing-belt",
                    "MAINTENANCE COST",
                    "Replacing the timing belt at the proper interval can help prevent costly engine damage."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "serpentine-belt-maintenance-cost",
                    "serpentine-belt",
                    "MAINTENANCE COST",
                    "Replacing a worn belt before failure can help avoid breakdowns and additional repair costs."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "coolant-maintenance-cost",
                    "coolant",
                    "MAINTENANCE COST",
                    "Fresh coolant helps protect the cooling system and reduce the risk of expensive overheating repairs."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "brake-fluid-maintenance-cost",
                    "brake-fluid",
                    "MAINTENANCE COST",
                    "Keeping brake fluid in good condition helps protect brake system components from moisture and corrosion."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "engine-air-filter-maintenance-cost",
                    "engine-air-filter",
                    "MAINTENANCE COST",
                    "Replacing a dirty engine air filter can help maintain airflow and avoid unnecessary performance loss."
            );

            createRecommendation(
                    recommendationRepository,
                    carServiceRepository,
                    "cabin-air-filter-maintenance-cost",
                    "cabin-air-filter",
                    "MAINTENANCE COST",
                    "Replacing a dirty cabin filter helps maintain HVAC airflow and reduces strain caused by restricted airflow."
            );



            System.out.println("DEMO DATA CREATED");
        };
    }


    private void createService(
            CarServiceRepository carServiceRepository,
            String userId,
            String id,
            String name,
            int interval,
            ServiceType serviceType
    ) {

        if (carServiceRepository.existsById(id)) {
            return;
        }

        CarService service = new CarService();

        service.setId(id);
        service.setName(name);
        service.setCreatorUserId(userId);
        service.setIntervalMiles(interval);
        service.setServiceType(serviceType);
        service.setRecommendationCount(0);

        carServiceRepository.save(service);
    }


    private void createRecommendation(
            RecommendationRepository recommendationRepository,
            CarServiceRepository carServiceRepository,
            String id,
            String carServiceId,
            String name,
            String text
    ) {

        if (recommendationRepository.existsById(id)) {
            return;
        }

        CarService carService =
                carServiceRepository.findById(carServiceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Car service not found: " + carServiceId
                                )
                        );


        Recommendation recommendation =
                new Recommendation();

        recommendation.setId(id);
        recommendation.setCarServiceId(carServiceId);
        recommendation.setName(name);
        recommendation.setText(text);

        recommendationRepository.save(recommendation);


        carService.setRecommendationCount(
                carService.getRecommendationCount() + 1
        );

        carServiceRepository.save(carService);
    }
}