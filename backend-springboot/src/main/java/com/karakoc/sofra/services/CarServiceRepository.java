package com.karakoc.sofra.services;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarServiceRepository
        extends JpaRepository<CarService, String> {

    List<CarService> findAllByCreatorUserId(String creatorUserId);
}