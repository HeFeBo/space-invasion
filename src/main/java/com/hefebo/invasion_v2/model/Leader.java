package com.hefebo.invasion_v2.model;

import java.util.ArrayList;
import java.util.List;

import com.hefebo.invasion_v2.model.CelestialBodies.Planet;
import com.hefebo.invasion_v2.model.researchs.Research;
import com.hefebo.invasion_v2.model.researchs.TypeResearch;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Leaders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "leader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Planet> planets;

    @OneToMany(mappedBy = "leader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Research> researchs;

    @Enumerated(EnumType.STRING)
    @Column(name = "LeaderStatus", nullable = false)
    private LeaderStatus status;

    @PrePersist
    public void prePersist(){
        name = "New_Leader";
        status = LeaderStatus.ACTIVE;
        planets = new ArrayList<>();
        researchs = new ArrayList<>();

        for(TypeResearch tr : TypeResearch.values()){
            Research new_Research = new Research();
            new_Research.setTypeResearch(tr);
            new_Research.setLeader(this);
            researchs.add(new_Research);
        }
    }
    
}
