package com.hefebo.invasion_v2.mapper;

import org.springframework.stereotype.Component;

import com.hefebo.invasion_v2.dto.ResearchResponse;
import com.hefebo.invasion_v2.model.researchs.Research;

@Component
public class ResearchMapper {
    public ResearchResponse toDTO(Research research){
        ResearchResponse dto = new ResearchResponse(research.getId(), research.getTypeResearch(), research.getLevel());
        return dto;
    }

}
