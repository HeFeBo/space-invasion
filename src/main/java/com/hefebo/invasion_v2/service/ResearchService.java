package com.hefebo.invasion_v2.service;

import com.hefebo.invasion_v2.dto.ResearchResponse;

public interface ResearchService {
    ResearchResponse upgradeResearch(long researchId, long planetId);
}
