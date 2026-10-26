package com.learn2code.jobportal.services;

import com.learn2code.jobportal.entity.*;
import com.learn2code.jobportal.repository.JobPostActivityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class JobPostActivityService {
    private final JobPostActivityRepository jobPostActivityRepository;

    public JobPostActivity addNew(JobPostActivity jobPostActivity) {
        return jobPostActivityRepository.save(jobPostActivity);
    }
    public List<RecruiterJobsDto> getRecruiterJobs(int recruiter){
        List<IRecruiterJobs> recruiterJobsDto = jobPostActivityRepository.getRecruiterJobs(recruiter);
        List<RecruiterJobsDto> recruiterJobsDtoList = new ArrayList<>();
        for(IRecruiterJobs recruiterJobs: recruiterJobsDto){
            JobLocation jobLocation = new JobLocation(recruiterJobs.getLocationId(), recruiterJobs.getCity(), recruiterJobs.getState(), recruiterJobs.getCountry());
            JobCompany jobCompany = new JobCompany(recruiterJobs.getCompanyId(), recruiterJobs.getName(), "");
            recruiterJobsDtoList.add(new RecruiterJobsDto(recruiterJobs.getTotalCandidates(), recruiterJobs.getJob_post_id(), recruiterJobs.getJob_title(), jobLocation, jobCompany));
        }
        return recruiterJobsDtoList;
    }

    public JobPostActivity getOne(int id) {
        return jobPostActivityRepository.findById(id).orElseThrow(()-> new RuntimeException("Job post activity not found!"));
    }
}
