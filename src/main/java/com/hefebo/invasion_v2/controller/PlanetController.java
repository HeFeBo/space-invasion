package com.hefebo.invasion_v2.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hefebo.invasion_v2.dto.PlanetResponse;
import com.hefebo.invasion_v2.service.PlanetService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/invasion2")
@RequiredArgsConstructor
public class PlanetController {
    private final PlanetService planetService;

    @PostMapping()   
    public ResponseEntity<PlanetResponse> initialPlanet() {
        PlanetResponse planetResponse = planetService.initialPlanet();
        return ResponseEntity.status(201).body(planetResponse);
    }

    @PostMapping("/coordinate/{galaxy}/{solarSystem}/{position}/{leaderId}")
    public ResponseEntity<Void> colonizePlanet(
        @PathVariable double galaxy, 
        @PathVariable double solarSystem, 
        @PathVariable double position,
        @PathVariable long leaderId) {
        
        planetService.colonizePlanetDelayed(galaxy, solarSystem, position, leaderId);
        
        return ResponseEntity.accepted().build();
    }

}
