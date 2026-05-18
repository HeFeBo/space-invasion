package com.hefebo.invasion_v2.service.impl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.dto.ResearchResponse;
import com.hefebo.invasion_v2.mapper.ResearchMapper;
import com.hefebo.invasion_v2.model.Leader;
import com.hefebo.invasion_v2.model.CelestialBodies.Planet;
import com.hefebo.invasion_v2.model.researchs.Research;
import com.hefebo.invasion_v2.model.researchs.TypeResearch;
import com.hefebo.invasion_v2.model.structures.Structure;
import com.hefebo.invasion_v2.model.structures.TypeStructure;
import com.hefebo.invasion_v2.repository.PlanetRepository;
import com.hefebo.invasion_v2.repository.ResearchRepository;
import com.hefebo.invasion_v2.service.ResearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResearchServiceImpl implements ResearchService{
    private final ResearchRepository researchRepository;
    private final ResearchMapper researchMapper;
    private final PlanetRepository planetRepository;

    private static final Map<TypeResearch, Map<Enum<?>, Integer>> RESERCH_REQUIREMENTS  = createResearchRequirements();

    

    @Override
    public ResearchResponse upgradeResearch(long researchId, long planetId) {
        Research research = researchRepository.findById(researchId)
                            .orElseThrow(()-> new RuntimeException("Ricerca non trovata."));
        
        Planet planet = planetRepository.findById(planetId)
                            .orElseThrow(()-> new RuntimeException("Pianeta non trovato."));
        
        List<Structure> structures = planet.getStructures();

        Map<TypeStructure, Structure> mapStructure = structures.stream()
                            .collect(Collectors.toMap(Structure::getTypeStructure, s -> s));
        
        Structure metalDeposit = mapStructure.get(TypeStructure.MetalDeposit);
        Structure diamondDeposit = mapStructure.get(TypeStructure.DiamondDeposit);
        Structure deuteriumDeposit = mapStructure.get(TypeStructure.DeuteriumDeposit);

        boolean firstLevel = research.getLevel() == 0;
        if(firstLevel)validateResearchRequirements(researchId, mapStructure);

        double metalRequired = research.getMetalRequiredNextLevel() != null 
                                ? research.getMetalRequiredNextLevel() : 0;
        double diamondRequired = research.getDiamondRequiredNextLevel() != null 
                                ? research.getDiamondRequiredNextLevel() : 0;
        double deuteriumRequired = research.getDeuteriumRequiredNextLevel() != null 
                                ? research.getDeuteriumRequiredNextLevel() : 0;

        if(
            metalRequired > metalDeposit.getSupply() || 
            diamondRequired > diamondDeposit.getSupply() ||
            deuteriumRequired > deuteriumDeposit.getSupply()
        ) throw new RuntimeException("Risorce insufficenti per mi migliorare la ricerca.");

        metalDeposit.setSupply(metalDeposit.getSupply() - metalRequired);
        diamondDeposit.setSupply(diamondDeposit.getSupply() - diamondRequired);
        deuteriumDeposit.setSupply(deuteriumDeposit.getSupply() - deuteriumRequired);

        research.setLevel(research.getLevel() + 1);

        planetRepository.save(planet);
        researchRepository.save(research);
        
        return researchMapper.toDTO(research);

    }

    public void validateResearchRequirements(long researchId, Map<TypeStructure, Structure> mapStructure){
        Research research = researchRepository.findById(researchId).orElseThrow(() -> new RuntimeException("Ricerca non trovata"));
        Leader leader = research.getLeader();

        TypeResearch typeResearch = research.getTypeResearch();
        List<Research> researchs = leader.getResearchs();

        Map<TypeResearch, Research> mapResearch = researchs.stream()
                            .collect(Collectors.toMap(Research::getTypeResearch,s -> s));

        Map<Enum<?>, Integer> requirements = RESERCH_REQUIREMENTS.get(typeResearch);

        if(
            mapStructure.get(TypeStructure.ResearchLaboratory).getLevel() < 
            requirements.get(TypeStructure.ResearchLaboratory)
        ) throw new RuntimeException("livello insufficiente del laboratorio di ricerca ");
        
        for(Map.Entry<Enum<?>, Integer> requirement : requirements.entrySet()){
            if(mapResearch.get(requirement.getKey()) != null){
                Research requiredResearch = mapResearch.get((requirement.getKey()));

                if(requiredResearch.getLevel() < requirement.getValue()) 
                throw new RuntimeException("Requisito tecnico non soddisfatto");
            }

        }
    }

    /*public void validateResearchRequirements(long researchId, long planetId){
        Research research = researchRepository.findById(researchId).orElseThrow(() -> new RuntimeException("Ricerca non trovata"));
        Planet planet = planetRepository.findById(planetId).orElseThrow(() -> new RuntimeException("Pianeta non trovato"));
        Leader leader = research.getLeader();

        TypeResearch typeResearch = research.getTypeResearch();
        List<Structure> structures = planet.getStructures();
        List<Research> researchs = leader.getResearchs();

        Map<TypeStructure, Structure> mapStructure = structures.stream()
                            .collect(Collectors.toMap(Structure::getTypeStructure, s -> s));

        Map<TypeResearch, Research> mapResearch = researchs.stream()
                            .collect(Collectors.toMap(Research::getTypeResearch,s -> s));

        Map<Enum<?>, Integer> requirements = RESERCH_REQUIREMENTS.get(typeResearch);

        if(
            mapStructure.get(TypeStructure.ResearchLaboratory).getLevel() < 
            requirements.get(TypeStructure.ResearchLaboratory)
        ) throw new RuntimeException("livello insufficiente del laboratorio di ricerca ");
        
        for(Map.Entry<Enum<?>, Integer> requirement : requirements.entrySet()){
            Research requiredResearch = mapResearch.get((requirement.getKey()));

            if(requiredResearch.getLevel() < requirement.getValue()) 
                throw new RuntimeException("Requisito tecnico non soddisfatto");

        }
    }*/

    public static Map<TypeResearch, Map<Enum<?>, Integer>> createResearchRequirements(){
        Map<TypeResearch, Map<Enum<?>, Integer>> requirements = new EnumMap<>(TypeResearch.class);
        
        requirements.put(TypeResearch.EnergyTechnology, Map.of(
            TypeStructure.ResearchLaboratory, 1
        ));

        requirements.put(TypeResearch.EspionageTechnology, Map.of(
            TypeResearch.ComputerTechnology, 02,
            TypeStructure.ResearchLaboratory, 5
        ));

        requirements.put(TypeResearch.ComputerTechnology, Map.of(
            TypeStructure.ResearchLaboratory, 3
        ));

        requirements.put(TypeResearch.LaserTechnology, Map.of(
            TypeResearch.EnergyTechnology, 03,
            TypeStructure.ResearchLaboratory, 2
        ));

        requirements.put(TypeResearch.MilitarTechnology, Map.of(
            TypeResearch.ComputerTechnology, 05,
            TypeStructure.ResearchLaboratory, 3
        ));

        requirements.put(TypeResearch.ShieldingTechnology, Map.of(
            TypeStructure.ResearchLaboratory, 3
        ));

        requirements.put(TypeResearch.PlasmaTechnology, Map.of(
            TypeResearch.EnergyTechnology, 8,
            TypeResearch.LaserTechnology, 6,
            TypeStructure.ResearchLaboratory, 8
        ));

        requirements.put(TypeResearch.CombustionDrive, Map.of(
            TypeResearch.EnergyTechnology, 02,
            TypeStructure.ResearchLaboratory, 1
        ));

        requirements.put(TypeResearch.ImpulseDrive, Map.of(
            TypeResearch.EnergyTechnology, 04,
            TypeResearch.LaserTechnology, 3,
            TypeResearch.ComputerTechnology, 5,
            TypeStructure.ResearchLaboratory, 6
        ));

        requirements.put(TypeResearch.MissileSystemsTechnology, Map.of(
            TypeResearch.MilitarTechnology, 06,
            TypeResearch.LaserTechnology, 5,
            TypeResearch.PlasmaTechnology, 2,
            TypeStructure.ResearchLaboratory, 6
        ));

        return requirements;

    }



    


}
