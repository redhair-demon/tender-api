package io.codefresh.gradleexample.service;

import io.codefresh.gradleexample.entity.Employee;
import io.codefresh.gradleexample.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee findById(UUID id) {
        return this.employeeRepository.findById(id).orElseThrow();
    }

    public Employee findByUsername(String username) {
        return this.employeeRepository.findByUsername(username);
    }
}
