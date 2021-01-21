package com.uber.uberapi.designpatterns;

import lombok.Getter;
import lombok.Setter;

// There is also a Lombok annotation for the builder pattern
@Getter
@Setter
public class Person {
    private String name;
    private Integer phone;
    private String address;
    private String gender;

    public static class Builder {
        String name_;
        Integer phone_;
        String address_;
        String gender_;

        public Builder name(String name){
            this.name_ = name;
            return this;
        }

        public Builder phone(Integer phone){
            this.phone_ = phone;
            return this;
        }

        public Builder address(String address){
            this.address_ = address;
            return this;
        }

        public Builder gender(String gender){
            this.gender_ = gender;
            return this;
        }

        public Person build(){
            if(name_.startsWith("p")) {
                gender_ = "male";
            }
            else{
                gender_ = "female";
            }
            return new Person(this);
        }
    }

    private Person(Builder params){
        this.name = params.name_;
        this.phone = params.phone_;
        this.address = params.address_;
        this.gender = params.gender_;
    }

    public static Builder Builder() {
        return new Builder();
    }

    public static void main(){
        // (1) needed : named parameters
        // (2) needed : optional parameters (brute force for this is to generate 2 pow 4 constructors)
        // python supports optional args
        // (3) I also want default args

        // (1) (2) (3) are not supported by Java out of the box
        // Workaround : Builder pattern (in Java)
        // 1. Allows you to have named params
        // 2. Allows you to have optional params
        // 3. Allows ypu to have default values
        // 4. Allows for changing the order of params
        // 5. Allows to enforce custom constraints

        // Python provides these functionality out of the box

        Person person = Person.Builder()
                .name("Kush")
                .phone(123)
                .build();

        System.out.println(person.toString());
    }


}
