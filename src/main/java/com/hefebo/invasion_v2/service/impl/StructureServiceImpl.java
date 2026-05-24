package com.hefebo.invasion_v2.service.impl;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.model.structures.Structure;
import com.hefebo.invasion_v2.repository.StructureRepository;
import com.hefebo.invasion_v2.service.StructureService;
import com.hefebo.invasion_v2.service.TaskServiceOne;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StructureServiceImpl implements StructureService{
    private final StructureRepository structureRepository;
    private final TaskServiceOne taskServiceOne;

    private final TaskScheduler taskScheduler;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, ScheduledFuture<?>> structureTasks = new ConcurrentHashMap<>();

    @Override
    public void upgradeStructureDelay(Long structureId){
        if(!structureRepository.findById(structureId).isPresent()){
            throw new RuntimeException("struttura non trovata");
        }
        Instant executedAt = Objects.requireNonNull(Instant.now().plusSeconds(10));
        taskScheduler.schedule(() -> {
            Structure structure = taskServiceOne._upgradeStructure(structureId);
            int newLevel = structure.getLevel();
            log.info("Inviando messaggio WebSocket a /topic/struttura/produzione");
            messagingTemplate.convertAndSend("/topic/struttura/produzione", //Si ha aggiunto structure.getTypeStructure()
                "SUCCESO: " + structure.getTypeStructure() + " aggiornata al livello " + newLevel
            ); 
        }, executedAt);

    }
    
    @Override
    public void stopStructureProduction(Long structureId) {
        if (structureId == null) throw new IllegalArgumentException("L'id non può essere null");

        Structure structure = structureRepository.findById(structureId).orElseThrow(() -> new RuntimeException("Struttura non trovata"));
        
        ScheduledFuture<?> task = structureTasks.get(structureId);

        if (task == null) throw new RuntimeException("Non esiste alcun compito per questa struttura.");

        task.cancel(false);
        structureTasks.remove(structureId);//Revisar esto porque structureTask esta tambien en TaskServiceOne!!!!
        log.info("Produzione di struttura {} é in arresto.", structureId);

        structure.setStatus(false);
        structureRepository.save(structure);
 
    }

    @Override
    public void resumeStructureProduction(Long structureId) {
        if (structureId == null) throw new IllegalArgumentException("L'id non può essere null");

        Structure structure = structureRepository.findById(structureId).orElseThrow(() -> new RuntimeException("Struttura non trovata"));

        structure.setStatus(true);
        structureRepository.save(structure);
        taskServiceOne._startStructureProduction(structureId);
    }

}
