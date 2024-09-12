package io.codefresh.gradleexample.controller;

import io.codefresh.gradleexample.config.OffsetBasedPageRequest;
import io.codefresh.gradleexample.entity.Tender;
import io.codefresh.gradleexample.entity.TenderServiceType;
import io.codefresh.gradleexample.entity.TenderStatus;
import io.codefresh.gradleexample.service.TenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tenders")
public class TenderController {
    @Autowired
    private TenderService tenderService;

    @GetMapping
    public List<Tender> get(
            @RequestParam(name = "service_type", required = false) List<TenderServiceType> type,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset
    ) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by("name"));
        return (type == null || type.isEmpty()) ? this.tenderService.findAll(pageable) : this.tenderService.findAll(type, pageable);
    }

    @PostMapping(
            value = "/new",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Tender post(
            @RequestBody Tender tender
    ) {
        return this.tenderService.store(tender);
    }

    @GetMapping("/my")
    public List<Tender> getMy(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset
    ) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by("name"));
        return this.tenderService.findByUser(username, pageable);
    }

    @GetMapping("/{id}/status")
    public TenderStatus get(
            @PathVariable UUID id,
            @RequestParam(name = "username", required = false) String username
    ) {
        return this.tenderService.findById(id).getStatus();
    }

    @PutMapping("/{id}/status")
    public Tender get(
            @PathVariable UUID id,
            @RequestParam(name = "status") TenderStatus status,
            @RequestParam(name = "username") String username
    ) {
        this.tenderService.setStatus(id, status);
        return this.tenderService.findById(id);
    }

    @PatchMapping(
            value = "/{id}/edit",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Tender edit(
            @PathVariable UUID id,
            @RequestBody Tender tender,
            @RequestParam(name = "username") String username
    ) {
        tender.setId(id);
        return this.tenderService.update(tender);
    }

    @PutMapping("/{id}/rollback/{version}")
    public Tender rollback(
            @PathVariable UUID id,
            @PathVariable Integer version,
            @RequestParam(name = "username") String username
    ) {
        return this.tenderService.rollback(id, version);
    }
}
