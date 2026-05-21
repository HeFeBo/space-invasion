package com.hefebo.invasion_v2.service;

public interface StructureService {
    void upgradeStructureDelay(Long structuretId);
    void stopStructureProduction(Long structureId);
    void resumeStructureProduction(Long structureId);
}
