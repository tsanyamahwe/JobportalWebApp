package com.learn2code.jobportal.controller;

import com.learn2code.jobportal.entity.JobSeekerProfile;
import com.learn2code.jobportal.entity.Skills;
import com.learn2code.jobportal.entity.Users;
import com.learn2code.jobportal.repository.UsersRepository;
import com.learn2code.jobportal.services.JobSeekerProfileService;
import com.learn2code.jobportal.util.FileUploadUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/job-seeker-profile")
public class JobSeekerProfileController {

    private JobSeekerProfileService jobSeekerProfileService;
    private UsersRepository usersRepository;

    @GetMapping({"", "/"})
    public String jobSeekerProfile(Model model) {
        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            assert authentication != null;
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Optional<JobSeekerProfile> seekerProfile =jobSeekerProfileService.getJobSeekerProfileById(user.getUserId());
            if (seekerProfile.isPresent()) {
                jobSeekerProfile = seekerProfile.get();
            }

            // Always make sure there is at least one skill row to show/clone
            if (jobSeekerProfile.getSkills() == null || jobSeekerProfile.getSkills().isEmpty()) {
                List<Skills> skills = new ArrayList<>();
                skills.add(new Skills());
                jobSeekerProfile.setSkills(skills);
            }
        }

        model.addAttribute("jobSeekerProfile", jobSeekerProfile);
        return "job-seeker-profile";
    }

    @PostMapping("/addNew")
    public String addNew(JobSeekerProfile jobSeekerProfile, @RequestParam("image") MultipartFile image, @RequestParam("pdf") MultipartFile pdf, Model model) {

        // 1. Link the profile to the logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            assert authentication != null;
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            jobSeekerProfile.setUserId(user);
            jobSeekerProfile.setUserAccountId(user.getUserId());
        }

        List<Skills> skills = new ArrayList<>();
        model.addAttribute("skills", skills);

        // 2. Link each skill back to its profile
        if (jobSeekerProfile.getSkills() != null) {
            for (Skills skill : jobSeekerProfile.getSkills()) {
                skill.setJobSeekerProfile(jobSeekerProfile);
            }
        }

        model.addAttribute("jobSeekerProfile", jobSeekerProfile);

        // 3. Work out the file names (only when a new file was chosen)
        String imageName = "";
        String resumeName = "";

        if (!Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            jobSeekerProfile.setProfilePhoto(imageName);
        }
        if (!Objects.equals(pdf.getOriginalFilename(), "")) {
            resumeName = StringUtils.cleanPath(Objects.requireNonNull(pdf.getOriginalFilename()));
            jobSeekerProfile.setResume(resumeName);
        }

        // 4. Save the profile (this gives us the user account id for the folder name)
        JobSeekerProfile savedUser = jobSeekerProfileService.addNew(jobSeekerProfile);

        // 5. Save the files to disk
        try {
            String uploadDir = "photos/jobseeker/" + savedUser.getUserAccountId();
            if (!Objects.equals(image.getOriginalFilename(), "")) {
                FileUploadUtil.saveFile(uploadDir, imageName, image);
            }
            if (!Objects.equals(pdf.getOriginalFilename(), "")) {
                FileUploadUtil.saveFile(uploadDir, resumeName, pdf);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return "redirect:/dashboard";
    }
}