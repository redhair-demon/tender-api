package io.codefresh.gradleexample.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "bid_rev")
@Getter
@Setter
public class BidRevision implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "bid_id", nullable = false)
    private UUID bidId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    public BidRevision(Bid bid) {
        this.bidId = bid.getId();
        this.version = bid.getVersion();
        this.name = bid.getName();
        this.description = bid.getDescription();
    }

    public BidRevision() {}

    public Bid rollback() {
        Bid b = new Bid();
        b.setId(this.getBidId());
        b.setName(this.getName());
        b.setDescription(this.getDescription());
        return b;
    }
}

