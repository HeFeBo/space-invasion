package com.hefebo.invasion_v2.service.impl;

import org.springframework.stereotype.Service;

import com.hefebo.invasion_v2.dto.LeaderResponse;
import com.hefebo.invasion_v2.mapper.LeaderMapper;
import com.hefebo.invasion_v2.model.Leader;
import com.hefebo.invasion_v2.repository.LeaderRepository;
import com.hefebo.invasion_v2.service.LeaderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaderServiceImpl implements LeaderService{
    private final LeaderRepository leaderRepository;
    private final LeaderMapper leaderMapper;

    @Override
    public LeaderResponse createLeader(){
        Leader leader = new Leader();
        leaderRepository.save(leader);

        return leaderMapper.toDTO(leader);
    }
}
