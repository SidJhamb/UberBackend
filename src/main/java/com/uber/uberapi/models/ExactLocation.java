package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "exactlocation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExactLocation extends Auditable{
    private Double latitude;
    private Double longitude;

    // some business logic inside the model
    public double distanceKm(ExactLocation otherLocation) {
        //calculate distance between 2 location
        return 1;
    }
}


