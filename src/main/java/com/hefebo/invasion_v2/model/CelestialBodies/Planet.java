package com.hefebo.invasion_v2.model.CelestialBodies;

import java.util.List;

import com.hefebo.invasion_v2.model.Leader;
import com.hefebo.invasion_v2.model.structures.Structure;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name="Planets",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"galaxy", "solSys", "posit"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Planet {
    //// ---- Dati planetari
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "planName")
    private String planetName;

    @Column(name = "galaxy", nullable = false)
    private Double galaxy;

    @Column(name = "solSys", nullable = false)
    private Double solarSystem;

    @Column(name = "posit", nullable = false)
    private Double position;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "moon_id", nullable = true)
    private Moon moon;

    @ManyToOne(optional = true)
    @JoinColumn(name = "leader_id", nullable = true)
    private Leader leader;

    //// ---- Struttures
    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Structure> structures;

    @PrePersist
    public void prePersist(){
        this.planetName = "New_Planet";
    }
}
