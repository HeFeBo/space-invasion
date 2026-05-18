package com.hefebo.invasion_v2.model.CelestialBodies;

import java.util.List;

import com.hefebo.invasion_v2.model.structures.Structure;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Moons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Moon {
    //// ---- Dati
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moonName")
    private String moonName;

    @OneToOne(mappedBy = "moon")
    @JoinColumn(name = "planet_id", nullable = false)
    private Planet planet;

    //// ---- Struttures
    @OneToMany(mappedBy = "moon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Structure> structures;

    @PrePersist
    public void prePersist(){
        this.moonName = "New_Moon";
    }

}
