package com.learn2code.jobportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "job_seeker_profile")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobSeekerProfile {
    @Id
    private Integer userAccountId;

    @OneToOne
    @JoinColumn(name = "user_account_id")
    @MapsId
    private Users userId;

    private String city;
    private String country;
    private String employmentType;
    private String firstName;
    private String lastName;

    @Column(nullable = true, length = 64)
    private String profilePhoto;

    private String resume;
    private String state;
    private String workAuthorization;

    @OneToMany(targetEntity = Skills.class, cascade = CascadeType.ALL, mappedBy = "jobSeekerProfile")
    private List<Skills> skills;

    public JobSeekerProfile(Users users) {
        this.userId = users;
    }

    @Transient
    public String getPhotosImagePath(){
        if(profilePhoto == null || userAccountId==null){return null;}
        return "/photos/jobseeker/"+userAccountId +"/"+profilePhoto;
    }
}
