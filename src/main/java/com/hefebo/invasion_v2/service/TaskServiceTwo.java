package com.hefebo.invasion_v2.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
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
public class TaskServiceTwo {
    private final PlanetRepository planetRepository;
    private final StructureRepository structureRepository;
    private final AuxiliaryService auxiliaryService;

    private final SimpMessagingTemplate messagingTemplate;

    private static boolean fullDepositSensor = true;

    @Transactional
    public void taskRegisteredForStructure(long structureId){
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

        double productionPerSecond = auxiliaryService._getValueOrZero(structure.getProductionPerHour()) / 3600;

        Structure metalDeposit = structureMap.get(TypeStructure.MetalDeposit);
        Structure diamondDeposit = structureMap.get(TypeStructure.DiamondDeposit);
        Structure deuteriumDeposit = structureMap.get(TypeStructure.DeuteriumDeposit);

        if(structure.getTypeStructure() == TypeStructure.MetalMine){
            Structure metalMine = structure;
            if (metalDeposit.getSupply() >= metalDeposit.getCapacity()) {
                if(fullDepositSensor){
                    log.warn("Il serbatoio metallico è pieno.");
                    messagingTemplate.convertAndSend("/topic/struttura/produzione", // ---- Si ha aggiunto questo. ----
                            "ATENZIONE: Il serbatoio metallico è pieno. ");
                    fullDepositSensor = false;
                }
                return;
            }

            if(!fullDepositSensor) fullDepositSensor = true;

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
                if(fullDepositSensor){
                    log.warn("Il serbatoio di diamante è pieno.");
                    messagingTemplate.convertAndSend("/topic/struttura/produzione", // ---- Si ha aggiunto questo. ----
                            "ATENZIONE: Il serbatoio di diamante è pieno.");
                    fullDepositSensor = false;
                }
                return;
            }

            if(!fullDepositSensor) fullDepositSensor = true;

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
                if(fullDepositSensor){
                    log.warn("Il serbatoio di deuterio è pieno.");
                    messagingTemplate.convertAndSend("/topic/struttura/produzione", // ---- Si ha aggiunto questo. ----
                        "ATENZIONE: Il serbatoio di deuterio è pieno.");
                    fullDepositSensor = false;
                }
                
                return;
            }

            if(!fullDepositSensor) fullDepositSensor = true;
                        
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
    }   
    
}
