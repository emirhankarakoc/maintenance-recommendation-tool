package com.karakoc.sofra.ro;

import com.karakoc.sofra.security.UserPrincipal;
import com.karakoc.sofra.services.RunResult;
import com.karakoc.sofra.services.ServiceRunManager;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repair-orders")
@AllArgsConstructor
public class RoController {

    private final RoService roService;

    private final ServiceRunManager serviceRunManager;

    // =========================================================
    // REQUEST DTO
    // =========================================================

    public record CreateRoRequest(
            String number,
            Integer currentMileage,
            Integer vehicleYear,
            String vehicleMake,
            String vehicleModel,
            String vin
    ) {}

    // =========================================================
    // GET MY REPAIR ORDERS
    // =========================================================

    @GetMapping
    public List<Ro> getMyRepairOrders(
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return roService
                .getAllRepairOrdersByUserEmail(
                        user.getEmail()
                );
    }

    // =========================================================
    // CREATE REPAIR ORDER
    // =========================================================

    @PostMapping
    public void createRepairOrder(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CreateRoRequest request
    ) {

        roService.create(
                request.number(),
                user.getEmail(),
                request.currentMileage(),
                request.vehicleYear(),
                request.vehicleMake(),
                request.vehicleModel(),
                request.vin()
        );
    }

    // =========================================================
    // GET REPAIR ORDER
    // =========================================================

    @GetMapping("/{number}")
    public Ro getRepairOrder(
            @PathVariable String number,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return roService.getByNumberForUser(
                number,
                user.getEmail()
        );
    }

    // =========================================================
    // GET SAVED RESULTS
    // =========================================================

    @GetMapping("/{number}/results")
    public ServiceRunManager.RoResultsResponse getResults(
            @PathVariable String number,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return serviceRunManager.getResults(
                number,
                user
        );
    }
}