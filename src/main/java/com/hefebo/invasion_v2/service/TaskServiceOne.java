package com.hefebo.invasion_v2.service;

import java.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.model.CelestialBodies.Planet;
import com.hefebo.invasion_v2.model.structures.Structure;
import com.hefebo.invasion_v2.model.structures.TypeStructure;
import com.hefebo.invasion_v2.repository.PlanetRepository;
import com.hefebo.invasion_v2.repository.StructureRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceOne {
    private static final Map<TypeStructure, Map<TypeStructure, Integer>> TECHNICAL_REQUIREMENTS = _createTechnicalRequirements();
    private final PlanetRepository planetRepository;
    private final StructureRepository structureRepository;
    private final TaskServiceTwo taskService;

    private final TaskScheduler taskScheduler;

    private final Map<Long, ScheduledFuture<?>> structureTasks = new ConcurrentHashMap<>();

    @Transactional
    public Structure _upgradeStructure(long structureId){
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
        if(firstLevel) _validateTechnicalRequirements(structure, structureMap);

        double metalRequirement = _getValueOrZero(structure.getMetalRequiredNextLevel());
        double diamondRequirement = _getValueOrZero(structure.getDiamondRequiredNextLevel());
        double deuteriumRequirement = _getValueOrZero(structure.getDeuteriumRequiredNextLevel());

        if(
            metalRequirement > _getValueOrZero(metalDeposit.getSupply()) ||
            diamondRequirement > _getValueOrZero(diamondDeposit.getSupply()) ||
            deuteriumRequirement > _getValueOrZero(deuteriumDeposit.getSupply())){
            throw new RuntimeException("Resorse insufficienti per migliorare la struttura");
        }

        metalDeposit.setSupply(_getValueOrZero(metalDeposit.getSupply()) - metalRequirement);
        diamondDeposit.setSupply(_getValueOrZero(diamondDeposit.getSupply()) - diamondRequirement);
        deuteriumDeposit.setSupply(_getValueOrZero(deuteriumDeposit.getSupply()) - deuteriumRequirement);

        structure.setMetalRequiredNextLevel(_increaseRequirement(structure.getMetalRequiredNextLevel(), 1.15));
        structure.setDiamondRequiredNextLevel(_increaseRequirement(structure.getDiamondRequiredNextLevel(), 1.25));
        structure.setDeuteriumRequiredNextLevel(_increaseRequirement(structure.getDeuteriumRequiredNextLevel(), 1.35));

        structure.setLevel(structure.getLevel()+1);

        if(firstLevel)structure.setStatus(true);

        planetRepository.save(planet);

        if(firstLevel && _isProductionStructure(structureId))
        _startStructureProduction(structureId);

        return structure;
    }

    @Transactional
    public void _startStructureProduction(long structureId){
        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(() -> {
            taskService.taskRegisteredForStructure(structureId);
        },Duration.ofSeconds(1));
        structureTasks.put(structureId, task);
    }


    public boolean _isProductionStructure(long structureId){
        Structure structure = structureRepository.findById(structureId).orElseThrow(() -> new RuntimeException("Struttura non trovata"));

        if(
            structure.getTypeStructure() == TypeStructure.MetalMine || 
            structure.getTypeStructure() == TypeStructure.DiamondMine || 
            structure.getTypeStructure() == TypeStructure.DeuteriumSynthesizer
        )return true;

        return false;
    }

    public double _getValueOrZero(Double value) {
        return value != null ? value : 0;
    }

    public Double _increaseRequirement(Double currentRequirement, double multiplier) {
        return currentRequirement != null ? currentRequirement * multiplier : null;
    }

    public void _validateTechnicalRequirements(Structure structure, Map<TypeStructure, Structure> structureMap) {
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

    public static Map<TypeStructure, Map<TypeStructure, Integer>> _createTechnicalRequirements() {
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
