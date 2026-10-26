package com.learn2code.jobportal.services;

import com.learn2code.jobportal.entity.JobSeekerProfile;
import com.learn2code.jobportal.repository.JobSeekerProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class JobSeekerProfileService {
    private final JobSeekerProfileRepository jobSeekerProfileRepository;

    public Optional<JobSeekerProfile> getJobSeekerProfileById(Integer id) {
        return jobSeekerProfileRepository.findById(id);
    }

    public JobSeekerProfile addNew(JobSeekerProfile jobSeekerProfile) {
        return jobSeekerProfileRepository.save(jobSeekerProfile);
    }
}
