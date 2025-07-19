package law.counsel.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SignUpRequest {

    @NotNull
    private String name;
    @NotNull
    private String email;
    @NotNull
    private String password;
}
