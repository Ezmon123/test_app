package com.project.psedataconverter.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

//@Setter
//@Getter
@NoArgsConstructor
@Data
@Entity
public class DemandForPower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    @Column(updatable = true, unique = true)
    private Date dateOfMeasurement;
    private Double forecastOfPowerDemand;
    private Double actualPowerDemand;

    private Date createdAt;
    private Date updatedAt;

    public DemandForPower(Date dateOfMeasurement, Double forecastOfPowerDemand, Double actualPowerDemand) {
        this.dateOfMeasurement = dateOfMeasurement;
        this.forecastOfPowerDemand = forecastOfPowerDemand;
        this.actualPowerDemand = actualPowerDemand;
    }

    @PrePersist
    protected void setCreatedAtDate() {
        this.createdAt = new Date();
    }

    @PreUpdate
    protected void setUpdatedAtDate() {
        this.updatedAt = new Date();
    }
}