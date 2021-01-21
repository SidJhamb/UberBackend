package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name= "namedlocation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NamedLocation extends Auditable{
    @OneToOne
    private ExactLocation exactLocation;

    private String name;
    private String zipCode;
    private String city;
    private String country;
    private String state;

}
