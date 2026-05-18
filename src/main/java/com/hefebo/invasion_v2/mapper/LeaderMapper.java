package com.hefebo.invasion_v2.mapper;

import org.springframework.stereotype.Component;

import com.hefebo.invasion_v2.dto.LeaderResponse;
import com.hefebo.invasion_v2.model.Leader;

@Component
public class LeaderMapper {
    public LeaderResponse toDTO(Leader leader){
        LeaderResponse dto = new LeaderResponse(leader.getId(), leader.getName(), leader.getStatus());
        return dto;
    }
}
