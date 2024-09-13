package io.codefresh.gradleexample.controller;

import io.codefresh.gradleexample.config.InvalidRightsException;
import io.codefresh.gradleexample.config.InvalidUserException;
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
    ) {
        UUID organizationId = getUserOrganizationId(tender.getCreatorUsername());
        if (!organizationId.equals(tender.getOrganizationId())) throw new InvalidRightsException("Not enough rights to perform this operation");
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
    ) {
        return checkUserRead(id, username).getStatus();
    }

    @PutMapping("/{id}/status")
    public Tender get(
            @PathVariable UUID id,
            @RequestParam(name = "status") TenderStatus status,
            @RequestParam(name = "username") String username
    ) {
        checkUserWrite(id, username);
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
        checkUserWrite(id, username);
        tender.setId(id);
        return this.tenderService.update(tender);
    }

    @PutMapping("/{id}/rollback/{version}")
    public Tender rollback(
            @PathVariable UUID id,
            @PathVariable Integer version,
            @RequestParam(name = "username") String username
    ) {
        checkUserWrite(id, username);
        return this.tenderService.rollback(id, version);
    }

    private Tender checkUserRead(UUID id, String username) {
        Tender tender = this.tenderService.findById(id);
        UUID organizationId = getUserOrganizationId(username);
        if (!tender.getOrganizationId().equals(organizationId) && !tender.getStatus().equals(TenderStatus.Published))
            throw new InvalidRightsException("Not enough rights to perform this operation");
        return tender;
    }

    private void checkUserWrite(UUID id, String username) {
        Tender tender = this.tenderService.findById(id);
        UUID organizationId = getUserOrganizationId(username);
        if (!organizationId.equals(tender.getOrganizationId())) throw new InvalidRightsException("Not enough rights to perform this operation");
    }

    private UUID getUserOrganizationId(String username) {
        if (username == null) return null;
        Employee e = this.employeeService.findByUsername(username);
        if (e == null) throw new InvalidUserException("User not exists or invalid");
        OrganizationResponsible responsible = e.getResponsible();
        return (responsible == null) ? null : responsible.getOrganization().getId();
    }
}
