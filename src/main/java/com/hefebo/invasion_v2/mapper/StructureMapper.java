package com.hefebo.invasion_v2.mapper;

import org.springframework.stereotype.Component;

import com.hefebo.invasion_v2.dto.StructureResponse;
import com.hefebo.invasion_v2.model.structures.Structure;

@Component
public class StructureMapper {
    public StructureResponse toDTO(Structure structure){
        StructureResponse dto = new StructureResponse(structure.getId(), structure.getTypeStructure(), structure.getLevel());
        return dto;
    }
}
