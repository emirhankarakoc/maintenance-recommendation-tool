package com.karakoc.sofra.ro;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoRepository extends JpaRepository<Ro, String> {

    List<Ro> findAllByUsername(
            String username,
            Sort sort
    );

    Optional<Ro> findByNumber(String number);

    boolean existsByNumber(String number);
}