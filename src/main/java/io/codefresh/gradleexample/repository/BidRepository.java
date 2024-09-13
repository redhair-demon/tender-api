package io.codefresh.gradleexample.repository;

import io.codefresh.gradleexample.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {
    public Page<Bid> findAll(Pageable pageable);
    public Page<Bid> findFirstByTender_Id(UUID tender_id, Pageable pageable);

    @Query(value = "SELECT b FROM Bid b " +
            "JOIN Employee e1    ON b.authorId = e1.id " +
            "JOIN Organization o ON b.authorId = o.id " +
            "JOIN Employee e2    ON e2.username = :username " +
            "WHERE (b.tender.creator.responsible.organization.id = e2.responsible.organization.id AND b.status <> 'Created' AND b.status <> 'Canceled') " +
            "OR (e1.id = e2.id AND b.authorType = 'User') " +
            "OR (o.id = e2.responsible.organization.id AND b.authorType = 'Organization')")
    public Page<Bid> findAllForUser(@Param("username") String username, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Bid u SET u.status = :status WHERE u.id = :id")
    public void setStatus(@Param("id") UUID id, @Param("status") BidStatus status);
    @Transactional
    @Modifying
    @Query(value = "UPDATE Bid u SET u.version = u.version + 1 WHERE u.id = :id")
    public void increaseVersion(@Param("id") UUID id);
}
