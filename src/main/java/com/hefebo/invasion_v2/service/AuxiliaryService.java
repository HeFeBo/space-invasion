package com.hefebo.invasion_v2.service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.model.structures.Structure;
import com.hefebo.invasion_v2.model.structures.TypeStructure;
import com.hefebo.invasion_v2.repository.StructureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuxiliaryService {
    private final StructureRepository structureRepository;

    private static final Map<TypeStructure, Map<TypeStructure, Integer>> TECHNICAL_REQUIREMENTS = _createTechnicalRequirements();

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
