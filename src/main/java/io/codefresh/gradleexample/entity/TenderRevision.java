package io.codefresh.gradleexample.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tenders_rev")
@Getter
@Setter
public class TenderRevision implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tender_id", nullable = false)
    private UUID tenderId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private TenderServiceType serviceType;

    public TenderRevision(Tender tender) {
        this.tenderId = tender.getId();
        this.version = tender.getVersion();
        this.name = tender.getName();
        this.description = tender.getDescription();
        this.serviceType = tender.getServiceType();
    }

    public TenderRevision() {}

    public Tender rollback() {
        Tender t = new Tender();
        t.setId(this.getTenderId());
        t.setName(this.getName());
        t.setDescription(this.getDescription());
        t.setServiceType(this.getServiceType());
        return t;
    }
}

