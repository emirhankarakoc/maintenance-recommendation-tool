package com.karakoc.sofra.serviceresult;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoServiceResultRepository
        extends JpaRepository<RoServiceResult, String> {

    List<RoServiceResult> findAllByRoId(
            String roId
    );
}