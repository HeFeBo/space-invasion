package com.hefebo.invasion_v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hefebo.invasion_v2.service.ResearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invasion2/research")
@RequiredArgsConstructor
public class ResearchController {
    public final ResearchService researchService;

    @PostMapping("/upgrade/{researchId}/{planetId}")
    public ResponseEntity<Void> upgradeResearch(@PathVariable Long researchId, @PathVariable Long planetId) {
        researchService.upgradeResearchDelay(researchId, planetId);
        return ResponseEntity.accepted().build();
    }

}
