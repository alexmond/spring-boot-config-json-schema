package org.alexmond.sample.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;

@Data
@Schema(description = "Configuration class containing nested configuration properties")
@Validated
public class ConfigSampleNested {
    /* Nested Config sample */
    @Schema(description = "First nested configuration property", example = "nestedConfig1")
    @NotEmpty
    private String nestedConfig1 = "nestedConfig1";

    @Schema(description = "Second nested configuration property", example = "123")
    @NotNull
    private Integer nestedConfig2 = 123;

    @Email
    private String emailValidTest = "abc@abc.com";
    
    @URL
    private String urlTest = "http://localhost:8080";

}
