package io.codefresh.gradleexample.service;

import io.codefresh.gradleexample.entity.*;
import io.codefresh.gradleexample.repository.TenderRepository;
import io.codefresh.gradleexample.repository.TenderRevisionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TenderService {
    @Autowired
    private TenderRepository tenderRepository;
    @Autowired
    private TenderRevisionRepository tenderRevisionRepository;

    public Tender findById(UUID id) {
        return this.tenderRepository.findById(id).orElseThrow();
    }
    public List<Tender> findAll(Pageable pageable) {
        return this.tenderRepository.findAll(pageable).getContent();
    }
    public List<Tender> findAll(List<TenderServiceType> type, Pageable pageable) {
        return this.tenderRepository.findAllByServiceTypeIn(type, pageable).getContent();
    }

    public List<Tender> findByUser(String username, Pageable pageable) {
        return this.tenderRepository.findAllByCreatorUsername(username, pageable).getContent();
    }

    public Tender update(Tender tender) {
        Tender old = this.tenderRepository.findById(tender.getId()).orElseThrow();
        boolean changed = false;
        if (tender.getName() != null && !tender.getName().equals(old.getName())) {
            old.setName(tender.getName());
            changed = true;
        }
        if (tender.getDescription() != null && !tender.getDescription().equals(old.getDescription())) {
            old.setDescription(tender.getDescription());
            changed = true;
        }
        if (tender.getServiceType() != null && tender.getServiceType() != old.getServiceType()) {
            old.setServiceType(tender.getServiceType());
            changed = true;
        }

        if (changed) {
            this.tenderRevisionRepository.save(new TenderRevision(findById(tender.getId())));
            increaseVersion(tender.getId());
        }
        return this.store(old);
    }

    public void increaseVersion(UUID id) {
        this.tenderRepository.increaseVersion(id);
    }

    public void setStatus(UUID id, TenderStatus status) {
        this.tenderRepository.setStatus(id, status);
    }

    public Tender rollback(UUID id, Integer version) {
        TenderRevision backup = this.tenderRevisionRepository.findFirstByTenderIdAndVersion(id, version);
        if (backup == null) throw new NoSuchElementException();
        return update(backup.rollback());
    }

    public Tender store(Tender tender) {
        return this.tenderRepository.save(tender);
    }
}
