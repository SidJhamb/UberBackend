package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="account")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends Auditable{
    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    // account_roles -> account_id, role_id
    // primary key -> (account_id, role_id)

    // if we add a unique constraint to the role_id column
    // ,it becomes a 'one to one relation' (#roles entries)
    // if we add a unique constraint on the account_id column
    // ,it becomes a 'one to one relation' (#accounts entries)

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles = new ArrayList<>();
    // will be used in auth based auth
}


// Requirement analysis
// what the basic requirements are
// what the flow looks like for our actors
// like
// passenger will request a ride, driver will get notified, driver will accept

// things like adding a drivermatchingservice, locationtrackingservice
// come along the way as we start implementing
// we cannot figure out everything initially

// during the course of implementation,
// it is very rare, to remove a column from a model, remove a model
// it is very common, add certain columns to your tables, also add new relationships