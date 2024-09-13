package io.codefresh.gradleexample.service;

import io.codefresh.gradleexample.entity.*;
import io.codefresh.gradleexample.repository.BidRepository;
import io.codefresh.gradleexample.repository.BidRevisionRepository;
import io.codefresh.gradleexample.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BidService {
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private BidRevisionRepository bidRevisionRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    public Bid findById(UUID id) {
        return this.bidRepository.findById(id).orElseThrow();
    }
    public List<Bid> findAll(Pageable pageable) {
        return this.bidRepository.findAll(pageable).getContent();
    }
    public List<Bid> findForUser(String username, Pageable pageable) {
        return this.bidRepository.findAllForUser(username, pageable).getContent();
    }
    public List<Bid> findByTender(UUID tenderId, Pageable pageable) {
        return this.bidRepository.findFirstByTender_Id(tenderId, pageable).getContent();
    }

    public Bid update(Bid bid) {
        Bid old = this.bidRepository.findById(bid.getId()).orElseThrow();
        boolean changed = false;
        if (bid.getName() != null && !bid.getName().equals(old.getName())) {
            old.setName(bid.getName());
            changed = true;
        }
        if (bid.getDescription() != null && !bid.getDescription().equals(old.getDescription())) {
            old.setDescription(bid.getDescription());
            changed = true;
        }

        if (changed) {
            this.bidRevisionRepository.save(new BidRevision(findById(bid.getId())));
            increaseVersion(bid.getId());
        }
        return this.store(old);
    }

    public void increaseVersion(UUID id) {
        this.bidRepository.increaseVersion(id);
    }

    public void setStatus(UUID id, BidStatus status) {
        this.bidRepository.setStatus(id, status);
    }

    public Bid rollback(UUID id, Integer version) {
        BidRevision backup = this.bidRevisionRepository.findFirstByBidIdAndVersion(id, version);
        if (backup == null) throw new NoSuchElementException();
        return update(backup.rollback());
    }

    public Bid submitDecision(UUID id, BidStatus decision, String username) {
        Bid bid = findById(id);
        if (!bid.getStatus().equals(BidStatus.Rejected)) {
            Employee user = this.employeeRepository.findByUsername(username);
            switch (decision) {
                case Approved -> {
                    bid.addApprover(user.getResponsible());
                    if (bid.getApprovers().size() >= Math.min(3, user.getResponsible().getOrganization().getResponsibles().size())) {
                        setStatus(id, BidStatus.Approved);
                    }
                }
                case Rejected -> setStatus(id, BidStatus.Rejected);
            }
        }
        return findById(id);
    }

    public Bid store(Bid bid) {
        return this.bidRepository.save(bid);
    }
}
