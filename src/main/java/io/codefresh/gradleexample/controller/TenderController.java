package io.codefresh.gradleexample.controller;

import io.codefresh.gradleexample.config.OffsetBasedPageRequest;
import io.codefresh.gradleexample.entity.*;
import io.codefresh.gradleexample.service.EmployeeService;
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
    @Autowired
    private EmployeeService employeeService;

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
    ) throws IllegalAccessException {
        UUID organizationId = getUserOrganizationId(tender.getCreatorUsername());
        System.out.printf("%s %s %s\n", organizationId, tender.getOrganizationId(), organizationId == tender.getOrganizationId());
        if (organizationId != tender.getOrganizationId()) throw new IllegalAccessException();
        return this.tenderService.store(tender);
    }

    @GetMapping("/my")
    public List<Tender> getMy(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset
    ) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by("name"));
        UUID organizationId = getUserOrganizationId(username);
        return this.tenderService.findForUser(organizationId, TenderStatus.Published, pageable);
    }

    @GetMapping("/{id}/status")
    public TenderStatus get(
            @PathVariable UUID id,
            @RequestParam(name = "username", required = false) String username
    ) throws IllegalAccessException {
        return checkUser(id, username).getStatus();
    }

    @PutMapping("/{id}/status")
    public Tender get(
            @PathVariable UUID id,
            @RequestParam(name = "status") TenderStatus status,
            @RequestParam(name = "username") String username
    ) throws IllegalAccessException {
        checkUser(id, username);
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
    ) throws IllegalAccessException {
        checkUser(id, username);
        tender.setId(id);
        return this.tenderService.update(tender);
    }

    @PutMapping("/{id}/rollback/{version}")
    public Tender rollback(
            @PathVariable UUID id,
            @PathVariable Integer version,
            @RequestParam(name = "username") String username
    ) throws IllegalAccessException {
        checkUser(id, username);
        return this.tenderService.rollback(id, version);
    }

    private Tender checkUser(UUID id, String username) throws IllegalAccessException {
        Tender tender = this.tenderService.findById(id);
        UUID organizationId = getUserOrganizationId(username);
        if (organizationId != tender.getOrganizationId()) throw new IllegalAccessException();
        return tender;
    }

    private UUID getUserOrganizationId(String username) {
        if (username == null) return null;
        Employee e = this.employeeService.findByUsername(username);
        OrganizationResponsible responsible = e.getResponsible();
        System.out.printf("%s %s\n", e, responsible.getId());
        return (responsible == null) ? null : responsible.getOrganization().getId();
    }
}
