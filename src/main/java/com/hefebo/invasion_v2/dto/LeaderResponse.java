package com.hefebo.invasion_v2.dto;

import com.hefebo.invasion_v2.model.LeaderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderResponse {
    private Long id;
    private String name;
    private LeaderStatus status;
}
