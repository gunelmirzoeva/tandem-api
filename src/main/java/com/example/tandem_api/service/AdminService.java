package com.example.tandem_api.service;

import com.example.tandem_api.domain.user.Status;
import com.example.tandem_api.domain.user.User;
import com.example.tandem_api.dto.user.AdminUserListResponse;
import com.example.tandem_api.dto.user.DeactivateUserResponse;
import com.example.tandem_api.dto.user.UserProfileResponse;
import com.example.tandem_api.exception.UserAlreadyDeactivatedException;
import com.example.tandem_api.exception.UserNotFoundException;
import com.example.tandem_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

    public AdminUserListResponse listUsers(int page, int size, Status status, String timezone) {

        Specification<User> spec = ((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());

        if(status != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status));
        }
        if(timezone != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("timezone"), timezone));
        }

        Page<User> userPage =  userRepository.findAll(spec, PageRequest.of(page, size));


        List<UserProfileResponse> content = userPage.getContent().stream()
                .map(user -> UserProfileResponse.builder()
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .timezone(user.getTimezone())
                        .status(user.getStatus())
                        .matchReady(false)
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();

        return AdminUserListResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();

    }

    public UserProfileResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserProfileResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .timezone(user.getTimezone())
                .status(user.getStatus())
                .matchReady(false) // change later
                .createdAt(user.getCreatedAt())
                .build();
    }

    public DeactivateUserResponse deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getStatus().equals(Status.DEACTIVATED)) {
            throw new UserAlreadyDeactivatedException("Deactivating already deactivated user");
        }

        user.setStatus(Status.DEACTIVATED);
        userRepository.save(user);

        return DeactivateUserResponse.builder()
                .message("User deactivated successfully")
                .build();
    }
}
