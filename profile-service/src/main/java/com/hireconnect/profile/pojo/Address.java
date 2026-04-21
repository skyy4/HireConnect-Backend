package com.hireconnect.profile.pojo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {

    private String houseNo;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String country;
}
