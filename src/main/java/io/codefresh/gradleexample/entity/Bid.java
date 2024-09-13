package io.codefresh.gradleexample.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bids")
@Getter
@Setter
public class Bid implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BidStatus status = BidStatus.Created;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "tender_id")
    private Tender tender;

    @Transient
    private UUID tenderId;

    public UUID getTenderId() {
        return (tender != null) ? tender.getId() : tenderId;
    }
    @JsonIgnore
    public UUID getWritedTenderId() {
        return tenderId;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type")
    private BidAuthorType authorType;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "created_at")
    private Date createdAt = new Date();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "bid_id"),
            inverseJoinColumns = @JoinColumn(name = "responsible_id")
    )
    private List<OrganizationResponsible> approvers;

    public void addApprover(OrganizationResponsible approver) {
        this.approvers.add(approver);
    }

    @JsonIgnore
    @OneToOne
    private BidReview bidReview;
}
