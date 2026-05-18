package com.hefebo.invasion_v2.service;

import com.hefebo.invasion_v2.dto.StructureResponse;

public interface StructureService {
    boolean isProductionStructure(long structureId);
    void upgradeStructureDelay(long planetId);
    StructureResponse upgradeStructure(long structureId);
    void startStructureProduction(long structureId);
    void stopStructureProduction(Long structureId);
    void resumeStructureProduction(Long structureId);
}
