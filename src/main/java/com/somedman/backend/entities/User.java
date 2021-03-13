package com.somedman.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="users")
@Data
public class User {
    @Id
    private int id;
    private String userName;
    private String email;           //Will change data type
    private String photoUrl;        // Will change data type to uri
    private String userId;          // Provided by Social platform. Same if logging in with email id.
    private String provider;
    private String accessToken;     //Only Applicable for Facebook. Stores the Short Lived Access Token
}


