package com.hefebo.invasion_v2.service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.model.Leader;
import com.hefebo.invasion_v2.model.researchs.Research;
import com.hefebo.invasion_v2.model.researchs.TypeResearch;
import com.hefebo.invasion_v2.model.structures.Structure;
import com.hefebo.invasion_v2.model.structures.TypeStructure;
import com.hefebo.invasion_v2.repository.ResearchRepository;
import com.hefebo.invasion_v2.repository.StructureRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuxiliaryService {
    private final StructureRepository structureRepository;
    private final ResearchRepository researchRepository;

    private static final Map<TypeStructure, Map<TypeStructure, Integer>> TECHNICAL_REQUIREMENTS = _createTechnicalRequirements();
    private static final Map<TypeResearch, Map<Enum<?>, Integer>> RESERCH_REQUIREMENTS  = createResearchRequirements();

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

    @Transactional
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
