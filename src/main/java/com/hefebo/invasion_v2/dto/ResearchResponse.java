package com.hefebo.invasion_v2.dto;

import com.hefebo.invasion_v2.model.researchs.TypeResearch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResearchResponse {
    private Long id;
    private TypeResearch typeResearch;
    private Integer level;

}
