package com.hefebo.invasion_v2.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.model.Leader;
import com.hefebo.invasion_v2.model.CelestialBodies.Planet;
import com.hefebo.invasion_v2.model.structures.Structure;
import com.hefebo.invasion_v2.model.structures.TypeStructure;
import com.hefebo.invasion_v2.repository.LeaderRepository;
import com.hefebo.invasion_v2.repository.PlanetRepository;
import com.hefebo.invasion_v2.repository.StructureRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceOne {
    private final LeaderRepository leaderRepository;
    private final PlanetRepository planetRepository;
    private final StructureRepository structureRepository;
    private final TaskServiceTwo taskServiceTwo;
    private final AuxiliaryService auxiliaryService;

    private final TaskScheduler taskScheduler;

    private final Map<Long, ScheduledFuture<?>> structureTasks = new ConcurrentHashMap<>();

    @Transactional
    public Planet _colonizePlanet(double galaxy, double solarSystem, double position, long leaderId) {
        Leader leader = leaderRepository.findById(leaderId)
            .orElseThrow(() -> new RuntimeException("Giocatore non trovato."));

        Planet planet = _createPlanet(galaxy, solarSystem, position);

        leader.getPlanets().add(planet);
        planet.setLeader(leader);

        planetRepository.save(planet);

        return planet;
    }

    public Planet _createPlanet(double galaxy, double solarSystem, double position) {
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

        return planet;
    }

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
        if(firstLevel) auxiliaryService._validateTechnicalRequirements(structure, structureMap);

        double metalRequirement = auxiliaryService._getValueOrZero(structure.getMetalRequiredNextLevel());
        double diamondRequirement = auxiliaryService._getValueOrZero(structure.getDiamondRequiredNextLevel());
        double deuteriumRequirement = auxiliaryService._getValueOrZero(structure.getDeuteriumRequiredNextLevel());

        if(
            metalRequirement > auxiliaryService._getValueOrZero(metalDeposit.getSupply()) ||
            diamondRequirement > auxiliaryService._getValueOrZero(diamondDeposit.getSupply()) ||
            deuteriumRequirement > auxiliaryService._getValueOrZero(deuteriumDeposit.getSupply())){
            throw new RuntimeException("Resorse insufficienti per migliorare la struttura");
        }

        metalDeposit.setSupply(auxiliaryService._getValueOrZero(metalDeposit.getSupply()) - metalRequirement);
        diamondDeposit.setSupply(auxiliaryService._getValueOrZero(diamondDeposit.getSupply()) - diamondRequirement);
        deuteriumDeposit.setSupply(auxiliaryService._getValueOrZero(deuteriumDeposit.getSupply()) - deuteriumRequirement);

        structure.setMetalRequiredNextLevel(auxiliaryService._increaseRequirement(structure.getMetalRequiredNextLevel(), 1.15));
        structure.setDiamondRequiredNextLevel(auxiliaryService._increaseRequirement(structure.getDiamondRequiredNextLevel(), 1.25));
        structure.setDeuteriumRequiredNextLevel(auxiliaryService._increaseRequirement(structure.getDeuteriumRequiredNextLevel(), 1.35));

        structure.setLevel(structure.getLevel()+1);

        if(firstLevel)structure.setStatus(true);

        planetRepository.save(planet);

        if(firstLevel && auxiliaryService._isProductionStructure(structureId))
        _startStructureProduction(structureId);

        return structure;
    }

    public void _startStructureProduction(long structureId){
        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(() -> {
            taskServiceTwo.taskRegisteredForStructure(structureId);
        },Duration.ofSeconds(1));
        structureTasks.put(structureId, task);
    }

}
