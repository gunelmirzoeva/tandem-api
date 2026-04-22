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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "spoken_languages", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "language_code"})
})
public class SpokenLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "language_code", nullable = false)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpokenProficiencyLevel proficiencyLevel;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
