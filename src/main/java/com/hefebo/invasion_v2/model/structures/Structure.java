package com.hefebo.invasion_v2.model.structures;

import com.hefebo.invasion_v2.model.CelestialBodies.Moon;
import com.hefebo.invasion_v2.model.CelestialBodies.Planet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Structures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Structure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "typeStructure", nullable = false)
    private TypeStructure typeStructure;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "metalReq", nullable = true)
    private Double metalRequiredNextLevel;

    @Column(name = "diamReq", nullable = true)
    private Double diamondRequiredNextLevel;

    @Column(name = "deutReq", nullable = true)
    private Double DeuteriumRequiredNextLevel;

    @Column(name = "energyReq", nullable = true)
    private Double energyRequiredNextLevel;

    @Column(name = "prodPerHour", nullable = true)
    private Double productionPerHour;

    @Column(name = "supply", nullable = true)
    private Double supply;

    @Column(name = "capacity", nullable = true)
    private Double capacity;

    @Column(name = "InfStatus", nullable = true)
    private Boolean status;

    @ManyToOne(optional = true)
    @JoinColumn(name = "planet_id", nullable = true)
    private Planet planet;

    @ManyToOne(optional = true)
    @JoinColumn(name = "moon_id", nullable = true)
    private Moon moon;

    @PrePersist
    public void prePersist(){
        this.level = 0;
        int type = 0;
        
        if(typeStructure == TypeStructure.MetalMine) type=1;
        else if(typeStructure == TypeStructure.DiamondMine) type=2;
        else if(typeStructure == TypeStructure.DeuteriumSynthesizer) type=3;
        else if(typeStructure == TypeStructure.SolarPowerPlant) type=4;
        else if(typeStructure == TypeStructure.MetalDeposit) type=5;
        else if(typeStructure == TypeStructure.DiamondDeposit) type=6;
        else if(typeStructure == TypeStructure.DeuteriumDeposit) type=7;
        else if(typeStructure == TypeStructure.Hangar) type=8;
        else if(typeStructure == TypeStructure.RobotFactory) type=9;
        else if(typeStructure == TypeStructure.NanobotFactory) type=10;
        else if(typeStructure == TypeStructure.ResearchLaboratory) type=11;
        else if(typeStructure == TypeStructure.ParticleScanner) type=12;
        else type=13;

        switch(type){
            case 1:
                this.metalRequiredNextLevel = 100D;
                this.energyRequiredNextLevel = 50D;
                this.productionPerHour = 100D;
                this.status = false;
                break;
            case 2:
                this.metalRequiredNextLevel = 50D;
                this.diamondRequiredNextLevel = 100D;
                this.energyRequiredNextLevel = 60D;
                this.productionPerHour = 70D;
                this.status = false;
                break;
            case 3:
                this.metalRequiredNextLevel = 80D;
                this.diamondRequiredNextLevel = 150D;
                this.energyRequiredNextLevel = 70D;
                this.productionPerHour = 40D;
                this.status = false;
                break;
            case 4:
                this.metalRequiredNextLevel = 60D;
                this.diamondRequiredNextLevel = 120D;
                this.productionPerHour = 270D;
                this.status = false;
                break;
            case 5:
                this.metalRequiredNextLevel = 20D;
                this.supply = 5000D;
                this.capacity = 5000D;
                this.status = false;
                break;
            case 6:
                this.metalRequiredNextLevel = 40D;
                this.diamondRequiredNextLevel = 80D;
                this.supply = 5000D;
                this.capacity = 5000D;
                this.status = false;
                break;
            case 7:
                this.metalRequiredNextLevel = 60D;
                this.diamondRequiredNextLevel = 100D;
                this.supply = 500D;
                this.capacity = 500D;
                this.status = false;
                break;
            case 8:
                this.metalRequiredNextLevel = 80D;
                this.diamondRequiredNextLevel = 50D;
                this.status = false;
                break;
            case 9:
                this.metalRequiredNextLevel = 50D;
                this.diamondRequiredNextLevel = 100D;
                this.status = false;
                break;
            case 10:
                this.metalRequiredNextLevel = 100D;
                this.diamondRequiredNextLevel = 300D;
                this.status = false;
                break;
            case 11:
                this.metalRequiredNextLevel = 40D;
                this.diamondRequiredNextLevel = 80D;
                this.status = false;
                break;
            case 12:
                this.metalRequiredNextLevel = 150D;
                this.diamondRequiredNextLevel = 400D;
                this.status = false;
                break;
            case 13:
                this.metalRequiredNextLevel = 200D;
                this.diamondRequiredNextLevel = 500D;
                this.status = false;
                break;
            default:
                break;
        }

    }

}
