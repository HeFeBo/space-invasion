package com.hefebo.invasion_v2.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.dto.PlanetResponse;
import com.hefebo.invasion_v2.mapper.PlanetMapper;
import com.hefebo.invasion_v2.model.Leader;
import com.hefebo.invasion_v2.model.CelestialBodies.Planet;
import com.hefebo.invasion_v2.model.structures.Structure;
import com.hefebo.invasion_v2.model.structures.TypeStructure;
import com.hefebo.invasion_v2.repository.LeaderRepository;
import com.hefebo.invasion_v2.repository.PlanetRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanetColonizationTxService {
    private final LeaderRepository leaderRepository;
      private final PlanetRepository planetRepository;
      private final PlanetMapper planetMapper;

      @Transactional
      public PlanetResponse _colonizePlanet(double galaxy, double solarSystem, double position, long leaderId) {
          Leader leader = leaderRepository.findById(leaderId)
              .orElseThrow(() -> new RuntimeException("Giocatore non trovato."));

          PlanetResponse planetResponse = _createPlanet(galaxy, solarSystem, position);
          Planet planet = planetRepository.findById(planetResponse.getId())
              .orElseThrow(() -> new RuntimeException("Pianeta non trovato."));

          leader.getPlanets().add(planet);
          planet.setLeader(leader);

          planetRepository.save(planet);

          return planetMapper.toDTO(planet);
      }

      public PlanetResponse _createPlanet(double galaxy, double solarSystem, double position) {
          Planet planet = new Planet();
          planet.setGalaxy(galaxy);
          planet.setSolarSystem(solarSystem);
          planet.setPosition(position);

          List<Structure> structures = new ArrayList<>();
          for (TypeStructure typeStructure : TypeStructure.values()) {
              Structure structure = new Structure();
              structure.setTypeStructure(typeStructure);
              structure.setPlanet(planet);
              structures.add(structure);
          }

          planet.setStructures(structures);
          planet = planetRepository.save(planet);

          return planetMapper.toDTO(planet);
      }


}
