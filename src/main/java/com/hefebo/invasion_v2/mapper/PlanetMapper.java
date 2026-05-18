package com.hefebo.invasion_v2.mapper;

import org.springframework.stereotype.Component;

import com.hefebo.invasion_v2.dto.PlanetResponse;
import com.hefebo.invasion_v2.model.CelestialBodies.Planet;

@Component
public class PlanetMapper {
    public PlanetResponse toDTO(Planet planet){
        PlanetResponse dto = new PlanetResponse(planet.getId(), planet.getGalaxy(), planet.getSolarSystem(), planet.getPosition(), planet.getLeader().getId());
        return dto;
    }
}
