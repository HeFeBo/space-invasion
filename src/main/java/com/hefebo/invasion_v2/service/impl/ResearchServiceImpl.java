package com.hefebo.invasion_v2.service.impl;

import java.time.Instant;
import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.model.researchs.Research;
import com.hefebo.invasion_v2.repository.PlanetRepository;
import com.hefebo.invasion_v2.repository.ResearchRepository;
import com.hefebo.invasion_v2.service.ResearchService;
import com.hefebo.invasion_v2.service.TaskServiceOne;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResearchServiceImpl implements ResearchService{
    private final ResearchRepository researchRepository;
    private final PlanetRepository planetRepository;
    private final TaskServiceOne taskServiceOne;

    private final TaskScheduler taskScheduler;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void upgradeResearchDelay(Long researchId, long planetId){
        Instant executedAt = Objects.requireNonNull(Instant.now().plusSeconds(10));
        if(!researchRepository.findById(researchId).isPresent()){
            throw new RuntimeException("ricerca non trovata");
        }
        if(!planetRepository.findById(planetId).isPresent()){
            throw new RuntimeException("pianeta non trovato");
        }

        taskScheduler.schedule(() -> {
            Research research = taskServiceOne._upgradeResearch(researchId, planetId);
            int newLevel = research.getLevel();
            log.info("Inviando messaggio WebSocket a /topic/planet/research");
            messagingTemplate.convertAndSend("/topic/planet/research",
                "SUCCESO: Ricerca aggiornata al livello " + newLevel);
        }, executedAt);

    }

}
