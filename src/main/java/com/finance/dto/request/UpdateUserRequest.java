package com.finance.dto.request;

import com.finance.model.enums.Role;
import com.finance.model.enums.UserStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private Role role;

    private UserStatus status;
}
