package io.codefresh.gradleexample.controller;

import io.codefresh.gradleexample.config.InvalidRightsException;
import io.codefresh.gradleexample.config.InvalidUserException;
import io.codefresh.gradleexample.config.OffsetBasedPageRequest;
import io.codefresh.gradleexample.entity.*;
import io.codefresh.gradleexample.service.BidService;
import io.codefresh.gradleexample.service.EmployeeService;
import io.codefresh.gradleexample.service.OrganizationService;
import jakarta.servlet.ServletException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/bids")
public class BidController {
    @Autowired
    private BidService bidService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private OrganizationService organizationService;

    @GetMapping
    public List<Bid> getAll(
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset
    ) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by("name"));
        return this.bidService.findAll(pageable);
    }

    @PostMapping("/new")
    public Bid post(
            @RequestBody Bid bid
    ) {
        return this.bidService.store(bid);
    }

    @GetMapping("/my")
    public List<Bid> getMy(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset
    ) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by("name"));
        return this.bidService.findForUser(username, pageable);
    }

    @GetMapping("/{tenderId}/list")
    public List<Bid> getBidsByTender(
            @PathVariable(name = "tenderId") UUID tenderId,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset
    ) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by("name"));
        return this.bidService.findByTender(tenderId, pageable);
    }

    @GetMapping("/{id}/status")
    public BidStatus getStatus(
            @PathVariable(name = "id") UUID id,
            @RequestParam(name = "username", required = false) String username
    ) {
        return checkUserRead(id, username).getStatus();
    }

    @PutMapping("/{id}/status")
    public Bid setStatus(
            @PathVariable(name = "id") UUID id,
            @RequestParam(name = "status") BidStatus status,
            @RequestParam(name = "username") String username
    ) throws ServletException {
        if (!Set.of(BidStatus.Created, BidStatus.Published, BidStatus.Canceled).contains(status)) throw new ServletException("Available statuses : Created, Published, Canceled");
        checkUserWrite(id, username);
        this.bidService.setStatus(id, status);
        return this.bidService.findById(id);
    }

    @PatchMapping(
            value = "/{id}/edit",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Bid edit(
            @PathVariable UUID id,
            @RequestBody Bid bid,
            @RequestParam(name = "username") String username
    ) {
        checkUserWrite(id, username);
        bid.setId(id);
        return this.bidService.update(bid);
    }



    private Bid checkUserRead(UUID id, String username) {
        Bid bid = this.bidService.findById(id);
        UUID userOrganizationId = getUserOrganizationId(username);
        if (!isAbleToRead(username, bid, userOrganizationId))
            throw new InvalidRightsException("Not enough rights to perform this operation");
        return bid;
    }

    private boolean isAbleToRead(String username, Bid bid, UUID userOrganizationId) {
        return isBidAuthor(username, bid, userOrganizationId) || isTenderCreator(bid, userOrganizationId);
    }

    private boolean isTenderCreator(Bid bid, UUID userOrganizationId) {
        return bid.getTender().getId().equals(userOrganizationId);
    }

    private boolean isBidAuthor(String username, Bid bid, UUID userOrganizationId) {
        if (bid.getAuthorType().equals(BidAuthorType.User)) {
            return this.employeeService.findById(bid.getAuthorId()).getUsername().equals(username);
        }
        if (bid.getAuthorType().equals(BidAuthorType.Organization)) {
            return bid.getAuthorId().equals(userOrganizationId);
        }
        return false;
    }

    private void checkUserWrite(UUID id, String username) {
        Bid bid = this.bidService.findById(id);
        UUID organizationId = getUserOrganizationId(username);
        if (!isBidAuthor(username, bid, organizationId)) throw new InvalidRightsException("Not enough rights to perform this operation");
    }

    private UUID getUserOrganizationId(String username) {
        if (username == null) return null;
        Employee e = this.employeeService.findByUsername(username);
        if (e == null) throw new InvalidUserException("User not exists or invalid");
        OrganizationResponsible responsible = e.getResponsible();
        return (responsible == null) ? null : responsible.getOrganization().getId();
    }
}
