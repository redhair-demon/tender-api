package io.codefresh.gradleexample.service;

import io.codefresh.gradleexample.entity.Organization;
import io.codefresh.gradleexample.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrganizationService {
    @Autowired
    private OrganizationRepository organizationRepository;

    public Organization findById(UUID id) {
        return this.organizationRepository.findById(id).orElseThrow();
    }
}
