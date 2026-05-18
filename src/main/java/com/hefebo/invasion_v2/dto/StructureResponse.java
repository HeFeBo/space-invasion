package com.hefebo.invasion_v2.dto;

import com.hefebo.invasion_v2.model.structures.TypeStructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StructureResponse {
    long id;
    TypeStructure typeStructure;
    int level;

}
