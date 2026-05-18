package com.hefebo.invasion_v2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hefebo.invasion_v2.model.structures.Structure;

public interface StructureRepository extends JpaRepository<Structure, Long> {

}
