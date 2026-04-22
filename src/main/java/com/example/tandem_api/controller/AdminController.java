package com.example.tandem_api.controller;

import com.example.tandem_api.domain.user.Status;
import com.example.tandem_api.dto.user.AdminUserListResponse;
import com.example.tandem_api.dto.user.DeactivateUserResponse;
import com.example.tandem_api.dto.user.UserProfileResponse;
import com.example.tandem_api.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<AdminUserListResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String timezone
    ) {
       AdminUserListResponse users = adminService.listUsers(page, size, status, timezone);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<DeactivateUserResponse> deactivateUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.deactivateUser(userId));
    }

}
