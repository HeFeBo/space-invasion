package com.hefebo.invasion_v2.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.dto.StructureResponse;
import com.hefebo.invasion_v2.mapper.StructureMapper;
import com.hefebo.invasion_v2.model.CelestialBodies.Planet;
import com.hefebo.invasion_v2.model.structures.Structure;
import com.hefebo.invasion_v2.model.structures.TypeStructure;
import com.hefebo.invasion_v2.repository.PlanetRepository;
import com.hefebo.invasion_v2.repository.StructureRepository;
import com.hefebo.invasion_v2.service.StructureService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StructureServiceImpl implements StructureService{
    private static final Map<TypeStructure, Map<TypeStructure, Integer>> TECHNICAL_REQUIREMENTS = createTechnicalRequirements();

    private final PlanetRepository planetRepository;
    private final StructureRepository structureRepository;
    private final StructureMapper structureMapper;

    private final TaskScheduler taskScheduler;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, ScheduledFuture<?>> structureTasks = new ConcurrentHashMap<>();

    @Override
    public boolean isProductionStructure(long structureId){
        Structure structure = structureRepository.findById(structureId).orElseThrow(() -> new RuntimeException("Struttura non trovata"));

        if(
            structure.getTypeStructure() == TypeStructure.MetalMine || 
            structure.getTypeStructure() == TypeStructure.DiamondMine || 
            structure.getTypeStructure() == TypeStructure.DeuteriumSynthesizer
        )return true;

        return false;
    }

    @Override
    public void upgradeStructureDelay(long structureId){
        Instant executedAt = Objects.requireNonNull(Instant.now().plusSeconds(10));
        if(!structureRepository.findById(structureId).isPresent()){
            throw new RuntimeException("struttura non trovata");
        }
        taskScheduler.schedule(() -> {
            StructureResponse structureResponseDTO = upgradeStructure(structureId);
            int newLevel = structureResponseDTO.getLevel();
            log.info("Inviando messaggio WebSocket a /topic/minieraMetallo/produzione");
            messagingTemplate.convertAndSend("/topic/minieraMetallo/produzione",
                "SUCCESO: Miniera de metallo aggiornata al livello " + newLevel);
        }, executedAt);

    }

    @Override
    public StructureResponse upgradeStructure(long structureId){
        Structure structure = structureRepository.findById(structureId).orElseThrow(() -> new RuntimeException("Struttura non trovata"));
        Planet planet = structure.getPlanet();

        List<Structure> structures = planet.getStructures();
        
        Map<TypeStructure, Structure> structureMap = structures
            .stream()
            .collect(Collectors.toMap(
                Structure::getTypeStructure,
                s -> s
            ));
        
        Structure metalDeposit = Optional.ofNullable(structureMap.get(TypeStructure.MetalDeposit)).orElseThrow(() ->
            new RuntimeException("Deposito di metallo non trovato"));

        Structure diamondDeposit = Optional.ofNullable(structureMap.get(TypeStructure.DiamondDeposit)).orElseThrow(() ->
            new RuntimeException("Deposito di diamante non trovato"));

        Structure deuteriumDeposit = Optional.ofNullable(structureMap.get(TypeStructure.DeuteriumDeposit)).orElseThrow(() ->
            new RuntimeException("Deposito di deuterio non trovato"));

        boolean firstLevel = structure.getLevel() == 0;
        if(firstLevel) validateTechnicalRequirements(structure, structureMap);

        double metalRequirement = getValueOrZero(structure.getMetalRequiredNextLevel());
        double diamondRequirement = getValueOrZero(structure.getDiamondRequiredNextLevel());
        double deuteriumRequirement = getValueOrZero(structure.getDeuteriumRequiredNextLevel());

        if(
            metalRequirement > getValueOrZero(metalDeposit.getSupply()) ||
            diamondRequirement > getValueOrZero(diamondDeposit.getSupply()) ||
            deuteriumRequirement > getValueOrZero(deuteriumDeposit.getSupply())){
            throw new RuntimeException("Resorse insufficienti per migliorare la struttura");
        }

        metalDeposit.setSupply(getValueOrZero(metalDeposit.getSupply()) - metalRequirement);
        diamondDeposit.setSupply(getValueOrZero(diamondDeposit.getSupply()) - diamondRequirement);
        deuteriumDeposit.setSupply(getValueOrZero(deuteriumDeposit.getSupply()) - deuteriumRequirement);

        structure.setMetalRequiredNextLevel(increaseRequirement(structure.getMetalRequiredNextLevel(), 1.15));
        structure.setDiamondRequiredNextLevel(increaseRequirement(structure.getDiamondRequiredNextLevel(), 1.25));
        structure.setDeuteriumRequiredNextLevel(increaseRequirement(structure.getDeuteriumRequiredNextLevel(), 1.35));

        structure.setLevel(structure.getLevel()+1);

        if(firstLevel)structure.setStatus(true);
        planetRepository.save(planet);

        if(firstLevel && isProductionStructure(structureId)) 
        startStructureProduction(structureId);

        return structureMapper.toDTO(structure);
    }
    
    @Override
    public void startStructureProduction(long structureId){
        Structure structure = structureRepository.findById(structureId).orElseThrow(()-> new RuntimeException("Struttura non trovata"));
        Planet planet = structure.getPlanet();
        List<Structure> structures = planet.getStructures();

        Map<TypeStructure, Structure> structureMap = structures
            .stream()
            .collect(Collectors.toMap(
                Structure::getTypeStructure,
                s -> s
            ));
        
        if(
            structure.getTypeStructure() != TypeStructure.MetalMine &&
            structure.getTypeStructure() != TypeStructure.DiamondMine &&
            structure.getTypeStructure() != TypeStructure.DeuteriumSynthesizer
        )
        
        throw new RuntimeException("La struttura non é una struttura produttiva");

        double productionPerSecond = getValueOrZero(structure.getProductionPerHour()) / 3600;

        Structure metalDeposit = structureMap.get(TypeStructure.MetalDeposit);
        Structure diamondDeposit = structureMap.get(TypeStructure.DiamondDeposit);
        Structure deuteriumDeposit = structureMap.get(TypeStructure.DeuteriumDeposit);
        

        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(() -> {
            if(structure.getTypeStructure() == TypeStructure.MetalMine){
                Structure metalMine = structure;
                if (metalDeposit.getSupply() >= metalDeposit.getCapacity()) {
                log.info("Il serbatoio metallico è pieno.");
                return;
                }
                if(!metalMine.getStatus()){
                    metalMine.setStatus(true);
                }
                double newSupply = Math.min(metalDeposit.getSupply() + productionPerSecond, metalDeposit.getCapacity());

                if(newSupply >= metalDeposit.getCapacity()){
                    metalMine.setStatus(false);
                } 
                metalDeposit.setSupply(newSupply);
                planetRepository.save(planet); 
            }else if(structure.getTypeStructure() == TypeStructure.DiamondMine){
                Structure diamondMine = structure;
                if (diamondDeposit.getSupply() >= diamondDeposit.getCapacity()) {
                log.info("Il serbatoio di diamante è pieno.");
                return;
                }
                if(!diamondMine.getStatus()){
                    diamondMine.setStatus(true);
                }
                double newSupply = Math.min(diamondDeposit.getSupply() + productionPerSecond, diamondDeposit.getCapacity());

                if(newSupply >= diamondDeposit.getCapacity()){
                    diamondMine.setStatus(false);
                } 
                diamondDeposit.setSupply(newSupply);
                planetRepository.save(planet); 
            }else{
                Structure deuteriumSynthesizer = structure;
                if (deuteriumDeposit.getSupply() >= deuteriumDeposit.getCapacity()) {
                log.info("Il serbatoio di deuterio è pieno.");
                return;
                }
                if(!deuteriumSynthesizer.getStatus()){
                    deuteriumSynthesizer.setStatus(true);
                }
                double newSupply = Math.min(deuteriumDeposit.getSupply() + productionPerSecond, deuteriumDeposit.getCapacity());

                if(newSupply >= deuteriumDeposit.getCapacity()){
                    deuteriumSynthesizer.setStatus(false);
                } 
                deuteriumDeposit.setSupply(newSupply);
                planetRepository.save(planet); 
            }       
        },Duration.ofSeconds(1));
        structureTasks.put(structure.getId(), task);
    }

    @Override
    public void stopStructureProduction(Long structureId) {
        if (structureId == null) throw new IllegalArgumentException("L'id non può essere null");

        Structure structure = structureRepository.findById(structureId).orElseThrow(() -> new RuntimeException("Struttura non trovata"));
        
        ScheduledFuture<?> task = structureTasks.get(structureId);

        if (task == null) throw new RuntimeException("Non esiste alcun compito per questa struttura.");

        task.cancel(false);
        structureTasks.remove(structureId);
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
        startStructureProduction(structureId);
    }

    private double getValueOrZero(Double value) {
        return value != null ? value : 0;
    }

    private Double increaseRequirement(Double currentRequirement, double multiplier) {
        return currentRequirement != null ? currentRequirement * multiplier : null;
    }

    private void validateTechnicalRequirements(Structure structure, Map<TypeStructure, Structure> structureMap) {
        Map<TypeStructure, Integer> requirements = TECHNICAL_REQUIREMENTS.getOrDefault(
            structure.getTypeStructure(),
            Collections.emptyMap()
        );

        for(Map.Entry<TypeStructure, Integer> requirement : requirements.entrySet()) {
            Structure requiredStructure = Optional.ofNullable(structureMap.get(requirement.getKey())).orElseThrow(() ->
                new RuntimeException("Requisito tecnico non trovato: " + requirement.getKey()));

            if(requiredStructure.getLevel() < requirement.getValue()) {
                throw new RuntimeException(
                    "Requisito tecnico non soddisfatto: " +
                    requirement.getKey() +
                    " livello " +
                    requirement.getValue()
                );
            }
        }
    }

    private static Map<TypeStructure, Map<TypeStructure, Integer>> createTechnicalRequirements() {
        Map<TypeStructure, Map<TypeStructure, Integer>> requirements = new EnumMap<>(TypeStructure.class);

        requirements.put(TypeStructure.Hangar, Map.of(
            TypeStructure.MetalMine, 2,
            TypeStructure.DeuteriumSynthesizer, 3
        ));
        requirements.put(TypeStructure.RobotFactory, Map.of(
            TypeStructure.MetalMine, 2,
            TypeStructure.DiamondMine, 1
        ));
        requirements.put(TypeStructure.NanobotFactory, Map.of(
            TypeStructure.RobotFactory, 5,
            TypeStructure.DiamondMine, 3
        ));
        requirements.put(TypeStructure.ResearchLaboratory, Map.of(
            TypeStructure.DiamondMine, 2,
            TypeStructure.SolarPowerPlant, 2
        ));
        requirements.put(TypeStructure.ParticleScanner, Map.of(
            TypeStructure.ResearchLaboratory, 3,
            TypeStructure.DeuteriumSynthesizer, 4
        ));
        requirements.put(TypeStructure.QuantumJumpPortal, Map.of(
            TypeStructure.Hangar, 5,
            TypeStructure.NanobotFactory, 3,
            TypeStructure.ResearchLaboratory, 5
        ));

        return Collections.unmodifiableMap(requirements);
    }

}
