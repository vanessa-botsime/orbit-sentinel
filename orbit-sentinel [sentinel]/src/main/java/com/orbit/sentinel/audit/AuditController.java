package com.orbit.sentinel.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-events")
public class AuditController {

    private final AuditEventRepository repository;
    private final AuditService auditService;

    public AuditController(AuditEventRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @GetMapping
    public PageResponse<AuditEvent> list(@RequestParam(required = false) String type,
                                         @RequestParam(required = false) String actor,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<AuditEvent> result;
        if (type != null && actor != null) {
            result = repository.findByTypeAndActor(type, actor, pageable);
        } else if (type != null) {
            result = repository.findByType(type, pageable);
        } else if (actor != null) {
            result = repository.findByActor(actor, pageable);
        } else {
            result = repository.findAll(pageable);
        }
        return new PageResponse<>(result.getContent(), safePage, safeSize, result.getTotalElements());
    }

    // ADMIN only (see SecurityConfig)
    @GetMapping("/verify")
    public VerificationResult verify() {
        return auditService.verifyChain();
    }
}
