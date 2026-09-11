package com.learn2code.jobportal.services;

import com.learn2code.jobportal.entity.UsersType;
import com.learn2code.jobportal.repository.UsersTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UsersTypeService {
    private final UsersTypeRepository usersTypeRepository;

    public List<UsersType> getAll() {
        return usersTypeRepository.findAll();
    }
}
