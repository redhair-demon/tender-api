package io.codefresh.gradleexample.repository;

import io.codefresh.gradleexample.entity.BidRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BidRevisionRepository extends JpaRepository<BidRevision, UUID> {
    public BidRevision findFirstByBidIdAndVersion(UUID tenderId, Integer version);
}
