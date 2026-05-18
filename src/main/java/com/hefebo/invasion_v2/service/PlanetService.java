package com.hefebo.invasion_v2.service;

import com.hefebo.invasion_v2.dto.PlanetResponse;

public interface PlanetService {
    PlanetResponse initialPlanet();
    void colonizePlanetDelayed(double galaxy, double solarSystem, double position, long leaderId);
    PlanetResponse _colonizePlanet(double galaxy, double solarSystem, double position, long leaderId);
    PlanetResponse _createPlanet(double galaxy, double solarSystem, double position);
}
