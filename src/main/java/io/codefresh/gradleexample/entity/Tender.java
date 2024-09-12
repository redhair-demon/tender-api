package io.codefresh.gradleexample.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenders")
@Getter
@Setter
@ToString
public class Tender implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private TenderServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TenderStatus status;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "creator_username", nullable = false, updatable = false)
    private String creatorUsername;

//    @JsonIgnore
//    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "organization_id")
//    private Organization organization;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "creator_id")
    private Employee creator;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @JsonIgnore
    @OneToMany(mappedBy = "tenderId")
    private List<TenderRevision> versions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt = new Date();

//    public static class DTO {
//        public UUID id;
//        public String name;
//        public String description;
//        public TenderServiceType serviceType;
//        public TenderStatus status;
//        public UUID organizationId;
//        public String creatorUsername;
//        public Integer version;
//        public Date createdAt;
//
//        DTO (Tender t) {
//            this.id = t.getId();
//            this.name = t.getName();
//            this.description = t.getDescription();
//            this.serviceType = t.getServiceType();
//            this.status = t.getStatus();
////            this.organizationId = t.getOrganization().getId();
//            this.creatorUsername = t.getCreatorUsername();
//            this.version = t.getVersion();
//            this.createdAt = t.getCreatedAt();
//        }
//        DTO() {}
//
//        public Tender cast() {
//            Tender t = new Tender();
//            t.setName(name);
//            t.setDescription(description);
//            t.setServiceType(serviceType);
//            t.setStatus(status);
//            t.setCreatorUsername(creatorUsername);
//            t.setVersion(version);
//            return t;
//        }
//    }
}
