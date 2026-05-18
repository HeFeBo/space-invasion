package com.hefebo.invasion_v2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hefebo.invasion_v2.model.CelestialBodies.Planet;

public interface PlanetRepository extends JpaRepository<Planet, Long> {
    Optional<Planet>findByGalaxyAndSolarSystemAndPosition(
        Double galaxy, 
        Double solarSystem, 
        Double position
    );

}
