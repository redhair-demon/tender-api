package io.codefresh.gradleexample.repository;

import io.codefresh.gradleexample.entity.TenderRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenderRevisionRepository extends JpaRepository<TenderRevision, UUID> {
    public TenderRevision findFirstByTenderIdAndVersion(UUID tenderId, Integer version);
}
