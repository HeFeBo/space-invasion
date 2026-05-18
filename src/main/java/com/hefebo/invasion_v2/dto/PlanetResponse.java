package com.hefebo.invasion_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanetResponse {
    long id;
    double galaxy;
    double solarSystem;
    double position;
    long leaderId;
}
