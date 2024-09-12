package io.codefresh.gradleexample.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Audited
@Entity
@Table(name = "bids")
@Getter
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
    private BidStatus status;

    @Column(name = "tender_id")
    private UUID tenderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type")
    private BidAuthorType authorType;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "version")
    private Integer version;

    @Column(name = "created_at")
    private Date createdAt;
}
