package com.hefebo.invasion_v2.model.researchs;

import com.hefebo.invasion_v2.model.Leader;

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
@Table(name = "Researchs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Research {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "typeResearch", nullable = true)
    private TypeResearch typeResearch;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "metalReq", nullable = true)
    private Double metalRequiredNextLevel;

    @Column(name = "diamReq", nullable = true)
    private Double diamondRequiredNextLevel;

    @Column(name = "deutReq", nullable = true)
    private Double deuteriumRequiredNextLevel;

    @ManyToOne(optional = true)
    @JoinColumn(name = "leader_id", nullable = true)
    private Leader leader;

    @PrePersist
    public void prePersist(){
        this.level = 0;
        int type = 0;

        if(TypeResearch.CombustionDrive == typeResearch) type =1;
        else if (TypeResearch.ComputerTechnology == typeResearch) type =2;
        else if (TypeResearch.ShieldingTechnology == typeResearch) type =3;
        else if (TypeResearch.EnergyTechnology == typeResearch) type =4;
        else if (TypeResearch.EspionageTechnology == typeResearch) type =5;
        else if (TypeResearch.ImpulseDrive == typeResearch) type =6;
        else if (TypeResearch.LaserTechnology == typeResearch) type =7;
        else if (TypeResearch.MilitarTechnology == typeResearch) type =8;
        else if (TypeResearch.MissileSystemsTechnology == typeResearch) type =9;
        else type =10;

        switch(type){
            case 1:
                metalRequiredNextLevel = 100D;
                break;
            case 2:
                diamondRequiredNextLevel = 200D;
                break;
            case 3:
                metalRequiredNextLevel = 200D;
                break;
            case 4:
                diamondRequiredNextLevel = 150D;
                deuteriumRequiredNextLevel = 100D;
                break;
            case 5:
                diamondRequiredNextLevel = 120D;
                break;
            case 6:
                metalRequiredNextLevel = 200D;
                diamondRequiredNextLevel =300D;
                deuteriumRequiredNextLevel =150D;
                break;
            case 7:
                metalRequiredNextLevel = 300D;
                diamondRequiredNextLevel =250D;
                break;
            case 8:
                metalRequiredNextLevel = 100D;
                diamondRequiredNextLevel = 230D;
                deuteriumRequiredNextLevel = 300D;
                break;
            case 9:
                metalRequiredNextLevel = 50D;
                diamondRequiredNextLevel = 500D;
                break;
            case 10:
                diamondRequiredNextLevel = 300D;
                deuteriumRequiredNextLevel = 200D;
                break;
            default:
                break;
        }
    }

}
