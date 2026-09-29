package com.karakoc.sofra.serviceRecommendation;

import com.karakoc.sofra.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/recs")
public class RecommendationController {

    private final RecommendationService recommendationService;


    public record CreateRecBody(
            String text,
            String name,
            String carServiceId,
            String laborCost,
            String opCode
    ) {}


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public Recommendation createRecommendation(
            @RequestBody CreateRecBody request,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return recommendationService.createRecommendation(
                request.text(),
                request.name(),
                request.carServiceId(),
                request.laborCost(),
                request.opCode(),
                user.getUserId()
        );
    }


    // =========================================================
    // GET
    // =========================================================

    @GetMapping("/{carServiceId}")
    public List<Recommendation> getAllRecommendationsByServiceId(
            @PathVariable String carServiceId,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return recommendationService
                .getAllRecommendationsByServiceId(
                        carServiceId,
                        user.getUserId()
                );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    public void deleteRecommendation(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        recommendationService
                .deleteRecommendation(
                        id,
                        user.getUserId()
                );
    }
}