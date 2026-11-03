package com.auth.users_service.dto;

import org.junit.jupiter.api.Test;

import com.auth.users_service.validation.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The password policy applies to new passwords only, and change-password has no username to point elsewhere. */
class AuthRequestsTest {

    @Test
    void loginOnlyRequiresAPasswordNotThePolicy() throws Exception {
        var password = LoginRequest.class.getDeclaredField("password");
        assertThat(password.getAnnotation(ValidPassword.class)).isNull();
        assertThat(password.getAnnotation(NotBlank.class)).isNotNull();
    }

    @Test
    void changePasswordHasNoUsernameAndIgnoresOneSentByAnOldClient() {
        assertThatThrownBy(() -> ChangePasswordRequest.class.getDeclaredField("username"))
            .isInstanceOf(NoSuchFieldException.class);

        ChangePasswordRequest request = JsonMapper.builder().build().readValue(
            "{\"username\":\"victim\",\"oldPassword\":\"a\",\"newPassword\":\"b\"}", ChangePasswordRequest.class);
        assertThat(request.getOldPassword()).isEqualTo("a");
        assertThat(request.getNewPassword()).isEqualTo("b");
    }
}
