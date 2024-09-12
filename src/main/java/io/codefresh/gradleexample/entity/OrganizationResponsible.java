package io.codefresh.gradleexample.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "organization_responsible")
@Getter
public class OrganizationResponsible implements Serializable {
    @Id
    private UUID id;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "user_id")
    private UUID userId;
}
