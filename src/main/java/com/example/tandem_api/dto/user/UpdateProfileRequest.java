package com.example.tandem_api.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "FullName cannot be blank")
    private String fullName;

    @NotBlank(message = "Timezone cannot be blank")
    @Size(max = 100)
    private String timezone;
}
