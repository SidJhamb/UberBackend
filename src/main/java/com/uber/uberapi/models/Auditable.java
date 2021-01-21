package com.uber.uberapi.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

// types of inheritance are available for databases
// OOP doesnt translate well and completely for databases
// Because databases are truth tables
// single table inheritance - all the columns from all the subclasses are present in 1 table
//                          - disadvantage : sparse table
// per table inheritance - each subclass has its own table with a copy of the parent classes columns
//                          - lose our OOP - references cannot be made to the superclass
// composition bases inheritance - each subclass has a foreign key to the superclass object
//                              - one of the best ways
//                              - disadvantage - we have to use table joins (expensive)
// mapped super class - the super class is abstract - no table for it
@MappedSuperclass // dont create table for Auditable
@EntityListeners(AuditingEntityListener.class) // so that @Temporal can work
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Auditable implements Serializable {
    // database to provide the id for us
    // autoincrement
    // UUID - 128 bits (complex to create indexes and query)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP) // from jpa
    @CreatedDate // from hibernate
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP) // from jpa
    @LastModifiedDate // from hibernate
    private Date updatedAt;

    // Passenger() <-> row in the database
    // Hibernate will cache things properly
    // that entity comparison is based on the table and id instead of just the memory address
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // same object
        // check the class of both
        if (o == null || getClass() != o.getClass()) return false;
        // cast to Auditable, coz I want to get the id
        Auditable auditable = (Auditable) o;
        // compare ids
        if(id == null || auditable.id == null){
            return false;
        }
        return id.equals(auditable.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : Objects.hash(id);
    }
}
