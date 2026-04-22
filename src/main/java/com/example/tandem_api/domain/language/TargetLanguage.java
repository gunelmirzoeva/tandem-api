package com.example.tandem_api.domain.language;

import com.example.tandem_api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "target_languages", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "language_code"})
})
public class TargetLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "language_code", nullable = false)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false)
    private TargetProficiencyLevel currentLevel;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
