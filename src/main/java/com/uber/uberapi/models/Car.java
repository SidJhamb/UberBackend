package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="car")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Car extends Auditable{
    @ManyToOne
    private Color color;

    private String plateNumber;

    private String brandAndModel;

    @Enumerated(value = EnumType.STRING)
    private CarType carType;

    @OneToOne
    private Driver driver;
}
