package com.hefebo.invasion_v2.service.impl;

import com.hefebo.invasion_v2.repository.LeaderRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.dto.LeaderResponse;
import com.hefebo.invasion_v2.dto.PlanetResponse;
import com.hefebo.invasion_v2.mapper.PlanetMapper;
import com.hefebo.invasion_v2.model.Leader;
import com.hefebo.invasion_v2.model.CelestialBodies.Planet;
import com.hefebo.invasion_v2.repository.PlanetRepository;
import com.hefebo.invasion_v2.service.LeaderService;
import com.hefebo.invasion_v2.service.PlanetService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanetServiceImpl implements PlanetService {
    private final LeaderRepository leaderRepository;
    private final PlanetRepository planetRepository;
    private final PlanetMapper planetMapper;
    private final LeaderService leaderService;
    private final PlanetColonizationTxService planetColonizationTxService;

    private final TaskScheduler taskScheduler;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public PlanetResponse initialPlanet() {
        LeaderResponse leaderResponse = leaderService.createLeader();
        Leader leader = leaderRepository.findById(leaderResponse.getId()).orElseThrow(() -> new RuntimeException("Giocatore non trovato."));

        boolean present = true;
        while(present){
            double galaxy = ThreadLocalRandom.current().nextInt(1, 3);
            double solarSystem = ThreadLocalRandom.current().nextInt(1, 3);
            double position = ThreadLocalRandom.current().nextInt(1, 4);

            Optional<Planet> optional = planetRepository.findByGalaxyAndSolarSystemAndPosition(galaxy, solarSystem, position);

            if(!optional.isPresent()){
                present = false;

                PlanetResponse planetResponse = planetColonizationTxService._createPlanet(galaxy, solarSystem, position);
                Planet planet = planetRepository.findById(planetResponse.getId()).orElseThrow(() -> new RuntimeException("Pianeta non trovato."));

                planet.setLeader(leader);

                leader.getPlanets().add(planet);
                planetRepository.save(planet);

                planet = planetRepository.findById(planet.getId()).orElseThrow(() -> new RuntimeException("Pianeta non trovato."));

                return planetMapper.toDTO(planet);
            }
    
        }

        return null;
        
    }

    @Override
    public void colonizePlanetDelayed(double galaxy, double solarSystem, double position, long leaderId){
        Instant executedAt = Objects.requireNonNull(Instant.now().plusSeconds(10));

        taskScheduler.schedule(() -> {

            boolean filledCoordinate = planetRepository
            .findByGalaxyAndSolarSystemAndPosition(galaxy, solarSystem, position)
            .isPresent();

            if(filledCoordinate){
                log.warn("Coordinata {}/{}/{} già occupata. Colonizzazione annullata.", galaxy, solarSystem, position);

                messagingTemplate.convertAndSend("/topic/colonization", 
                    "FALLO: La coordinata " + galaxy + "/" + solarSystem + "/" + position + " é giá occupatta.");
                return;
            }

            try{
                planetColonizationTxService._colonizePlanet(galaxy, solarSystem, position, leaderId);

                log.info("Inviando messaggio WebSocket a /topic/colonization");
                
                messagingTemplate.convertAndSend("/topic/colonization", 
                    "SUCCESSO: Pianeta colonizzato in " + galaxy + "/" + solarSystem + "/" + position);
            }catch(DataIntegrityViolationException e){
                log.warn("Conflitto di concorrenza in {}/{}/{}. Colonizzazione annullata.", galaxy, solarSystem, position);

                messagingTemplate.convertAndSend("/topic/colonization",
                    "ERRORE: Conflitto di concorrenza in " + galaxy + "/" + solarSystem + "/" + position);
            }

        }, executedAt);

    }

}
