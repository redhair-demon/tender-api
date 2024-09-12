package io.codefresh.gradleexample.repository;

import io.codefresh.gradleexample.entity.Tender;
import io.codefresh.gradleexample.entity.TenderServiceType;
import io.codefresh.gradleexample.entity.TenderStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface TenderRepository extends JpaRepository<Tender, UUID> {
    public Page<Tender> findAll(Pageable pageable);
    public Page<Tender> findAllByServiceTypeIn(Collection<TenderServiceType> serviceType, Pageable pageable);
    public Page<Tender> findAllByCreatorUsername(String creator_username, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Tender u SET u.status = :status WHERE u.id = :id")
    public void setStatus(@Param("id") UUID id, @Param("status") TenderStatus status);
    @Transactional
    @Modifying
    @Query(value = "UPDATE Tender u SET u.version = u.version + 1 WHERE u.id = :id")
    public void increaseVersion(@Param("id") UUID id);
}
