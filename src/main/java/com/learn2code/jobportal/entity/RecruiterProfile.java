package com.learn2code.jobportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recruiter_profile")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecruiterProfile {
    @Id
    private int userAccountId;

    @OneToOne
    @JoinColumn(name = "user_account_id")
    @MapsId
    private Users userId;

    private String city;
    private String company;
    private String country;
    private String state;
    private String firstName;
    private String lastName;

    @Column(nullable = true, length = 64)
    private String profilePhoto;

    public RecruiterProfile(Users users) {
        this.userId = users;
    }
}
