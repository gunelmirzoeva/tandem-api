package com.example.tandem_api.domain.language;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "supported_languages")
public class SupportedLanguage {

    @Id
    @Column(name = "language_code")
    private String languageCode;

    @Column(nullable = false)
    private String languageName;
}
