package com.karakoc.sofra.services;

import com.karakoc.sofra.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@AllArgsConstructor
public class CarServiceController {

    private final CarServiceService carServiceService;

    private final ServiceRunManager serviceRunManager;


    // =========================================================
    // REQUEST DTO
    // =========================================================

    public record CreateCarServiceReq(

            String name,

            int intervalMiles,

            Integer intervalMonths,

            ServiceType serviceType

    ) {}


    // =========================================================
    // CREATE SERVICE
    // =========================================================

    @PostMapping
    public CarService create(
            @RequestBody CreateCarServiceReq request,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return carServiceService.create(
                request.name(),
                request.intervalMiles(),
                request.intervalMonths(),
                request.serviceType(),
                user.getUserId()
        );
    }


    // =========================================================
    // GET MY SERVICES
    // =========================================================

    @GetMapping
    public List<CarService> getMyServices(
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return carServiceService
                .getAllByUserId(
                        user.getUserId()
                );
    }


    // =========================================================
    // DELETE SERVICE
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteService(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        carServiceService.delete(
                id,
                user.getUserId()
        );
    }


    // =========================================================
    // RUN
    // EXTENSION CALLS THIS
    // =========================================================

    @PostMapping("/run")
    public List<RunResult> run(
            @RequestBody RunRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return serviceRunManager.run(
                request,
                user
        );
    }


    // =========================================================
    // GET SAVED RO RESULTS
    // =========================================================

    @GetMapping("/{number}/results")
    public List<RunResult> getResults(
            @PathVariable String number,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return serviceRunManager.getResults(
                number,
                user
        ).results();
    }
}