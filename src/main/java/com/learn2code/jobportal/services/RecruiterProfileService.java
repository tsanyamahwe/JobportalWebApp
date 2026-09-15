package com.learn2code.jobportal.services;

import com.learn2code.jobportal.entity.RecruiterProfile;
import com.learn2code.jobportal.repository.RecruiterProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class RecruiterProfileService {
    private final RecruiterProfileRepository recruiterProfileRepository;

    public Optional<RecruiterProfile> getRecruiterProfile(Integer id) {
        return recruiterProfileRepository.findById(id);
    }

    public RecruiterProfile addNew(RecruiterProfile recruiterProfile) {
        return recruiterProfileRepository.save(recruiterProfile);
    }
}
