package com.auth.users_service.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth.users_service.config.RolesProperties;
import com.auth.users_service.config.UserProperties;
import com.auth.users_service.dto.ChangePasswordRequest;
import com.auth.users_service.dto.EditUserRequest;
import com.auth.users_service.model.Role;
import com.auth.users_service.model.User;
import com.auth.users_service.repository.RoleRepository;
import com.auth.users_service.repository.UserRepository;
import com.auth.users_service.utils.JwtUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Account management cannot be used to take over other accounts or to raise privileges. */
class UsersManagerServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtUtils jwt = mock(JwtUtils.class);
    private final RolesProperties rolesProperties = new RolesProperties();
    private UsersManagerService service;

    private final Role userRole = role("r-user", "USER");
    private final Role adminRole = role("r-admin", "ADMIN");

    @BeforeEach
    void setUp() {
        rolesProperties.setBaseUsername("root");
        service = new UsersManagerService(users, roles, encoder, jwt, new UserProperties(), rolesProperties, mock(EmailService.class));
        when(roles.findById("r-admin")).thenReturn(Optional.of(adminRole));
        when(encoder.encode(anyString())).thenAnswer(i -> "enc:" + i.getArgument(0));
        when(jwt.generateToken(any())).thenReturn("new-token");
    }

    @AfterEach
    void clearSession() {
        SecurityContextHolder.clearContext();
    }

    // --- change-password ---

    @Test
    void changePasswordAlwaysActsOnTheSignedInUser() {
        signIn("ana");
        User ana = user("1", "ana", userRole);
        when(users.findByUsername("ana")).thenReturn(ana);
        when(encoder.matches("old-pass", "hash")).thenReturn(true);

        service.changePassword(new ChangePasswordRequest("old-pass", "new-password"));

        verify(users).findByUsername("ana");
        assertThat(ana.getPassword()).isEqualTo("enc:new-password");
    }

    // --- updateUser: tokens ---

    @Test
    void anAdminEditingSomeoneElseGetsNoToken() {
        signIn("admin", "manage_users");
        User bob = user("2", "bob", userRole);
        when(users.findById("2")).thenReturn(Optional.of(bob));

        assertThat(service.updateUser("2", edit("bob", null))).isNull();
        verify(jwt, never()).generateToken(any());
    }

    @Test
    void editingYourOwnAccountReturnsANewToken() {
        signIn("admin", "manage_users");
        User admin = user("1", "admin", adminRole);
        when(users.findById("1")).thenReturn(Optional.of(admin));

        assertThat(service.updateUser("1", edit("admin2", null))).isEqualTo("new-token");
        assertThat(admin.getUsername()).isEqualTo("admin2");
    }

    // --- updateUser: roles ---

    @Test
    void noRoleIdKeepsTheCurrentRole() {
        signIn("admin", "manage_users");
        User admin = user("1", "admin", adminRole);
        when(users.findById("1")).thenReturn(Optional.of(admin));

        service.updateUser("1", edit("admin", null));

        assertThat(admin.getRole()).isSameAs(adminRole);
        assertThat(admin.getTokenVersion()).isZero();
    }

    @Test
    void changingARoleNeedsManageRoles() {
        signIn("admin", "manage_users");
        User bob = user("2", "bob", userRole);
        when(users.findById("2")).thenReturn(Optional.of(bob));

        assertThatThrownBy(() -> service.updateUser("2", edit("bob", "r-admin")))
            .isInstanceOf(AccessDeniedException.class);
        assertThat(bob.getRole()).isSameAs(userRole);
        verify(users, never()).save(any());
    }

    @Test
    void sendingTheSameRoleDoesNotNeedManageRoles() {
        signIn("admin", "manage_users");
        User bob = user("2", "bob", userRole);
        when(users.findById("2")).thenReturn(Optional.of(bob));

        service.updateUser("2", edit("bob", "r-user"));

        assertThat(bob.getRole()).isSameAs(userRole);
        assertThat(bob.getTokenVersion()).isZero();
    }

    @Test
    void aRoleChangeEndsTheUsersOldSessions() {
        signIn("admin", "manage_users", "manage_roles");
        User bob = user("2", "bob", userRole);
        when(users.findById("2")).thenReturn(Optional.of(bob));

        service.updateUser("2", edit("bob", "r-admin"));

        assertThat(bob.getRole()).isSameAs(adminRole);
        assertThat(bob.getTokenVersion()).isEqualTo(1);
    }

    @Test
    void theBaseUsersRoleCannotBeChanged() {
        signIn("admin", "manage_users", "manage_roles");
        when(users.findById("0")).thenReturn(Optional.of(user("0", "root", adminRole)));

        assertThatThrownBy(() -> service.updateUser("0", edit("root", "r-user")))
            .hasMessage("The base user cannot be edited");
    }

    // --- setPassword (admin reset) ---

    @Test
    void theBaseUsersPasswordCannotBeReset() {
        when(users.findById("0")).thenReturn(Optional.of(user("0", "root", adminRole)));

        assertThatThrownBy(() -> service.setPassword("0", "new-password"))
            .hasMessage("The base user cannot be edited");
        verify(users, never()).save(any());
    }

    @Test
    void aResetFollowsThePasswordPolicy() {
        when(users.findById("2")).thenReturn(Optional.of(user("2", "bob", userRole)));

        assertThatThrownBy(() -> service.setPassword("2", "short"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password must be at least 8 characters");
        verify(users, never()).save(any());
    }

    @Test
    void aResetEndsTheUsersSessions() {
        User bob = user("2", "bob", userRole);
        when(users.findById("2")).thenReturn(Optional.of(bob));

        service.setPassword("2", "new-password");

        assertThat(bob.getPassword()).isEqualTo("enc:new-password");
        assertThat(bob.getTokenVersion()).isEqualTo(1);
    }

    // --- verification links ---

    @Test
    void aFreshLinkVerifiesTheAccount() {
        User bob = new User("bob", "b@x.cl", "hash");
        when(users.findByVerificationToken("tok")).thenReturn(bob);

        service.verifyUser("tok");

        assertThat(bob.isVerified()).isTrue();
        assertThat(bob.getVerificationToken()).isNull();
        assertThat(bob.getVerificationTokenExpiresAt()).isNull();
    }

    @Test
    void newLinksLastFortyEightHours() {
        User bob = new User("bob", "b@x.cl", "hash");
        assertThat(bob.getVerificationTokenExpiresAt())
            .isBetween(Instant.now().plus(User.VERIFICATION_TOKEN_TTL).minusSeconds(60), Instant.now().plus(User.VERIFICATION_TOKEN_TTL));
    }

    @Test
    void anExpiredLinkIsRefused() {
        User bob = new User("bob", "b@x.cl", "hash");
        bob.setVerificationTokenExpiresAt(Instant.now().minusSeconds(1));
        when(users.findByVerificationToken("tok")).thenReturn(bob);

        assertThatThrownBy(() -> service.verifyUser("tok")).hasMessage("Verification link has expired");
        assertThat(bob.isVerified()).isFalse();
    }

    @Test
    void aLinkWithoutExpiryFromBeforeTheChangeStillWorksOnce() {
        User bob = new User("bob", "b@x.cl", "hash");
        bob.setVerificationTokenExpiresAt(null);
        when(users.findByVerificationToken("tok")).thenReturn(bob);

        service.verifyUser("tok");

        assertThat(bob.isVerified()).isTrue();
        assertThat(bob.getVerificationToken()).isNull();
    }

    private static void signIn(String username, String... authorities) {
        List<SimpleGrantedAuthority> granted = java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, null, granted));
    }

    private static Role role(String id, String name) {
        Role role = new Role(name);
        role.setId(id);
        return role;
    }

    private static User user(String id, String username, Role role) {
        User user = new User(username, username + "@x.cl", "hash");
        user.setId(id);
        user.setRole(role);
        user.setVerified(true);
        return user;
    }

    private static EditUserRequest edit(String username, String roleId) {
        EditUserRequest request = new EditUserRequest();
        request.setUsername(username);
        request.setEmail(username + "@x.cl");
        request.setRoleId(roleId);
        return request;
    }
}
