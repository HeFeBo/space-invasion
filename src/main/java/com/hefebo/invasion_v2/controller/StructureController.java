package com.hefebo.invasion_v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hefebo.invasion_v2.service.impl.StructureServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invasion2/struttura")
@RequiredArgsConstructor
public class StructureController {
    private final StructureServiceImpl structureServiceImpl;

    @PostMapping("/upgrade/{structureId}")
    public ResponseEntity<Void> upgradeStructure(@PathVariable long structureId){
        structureServiceImpl.upgradeStructureDelay(structureId);
        return ResponseEntity.accepted().build();

    }
}
