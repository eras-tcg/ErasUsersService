package com.auth.users_service.controller;

import com.auth.users_service.model.User;
import com.auth.users_service.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** The signed-in user's own profile: what any user can see and change of their own account. */
@RestController
@RequestMapping("/api/users/me")
public class ProfileController {

    /** "icon:color": an icon name of the site's set and a color identity. */
    private static final Pattern AVATAR = Pattern.compile("[a-z][a-z-]{0,23}:[BGPRW]");

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profile(user));
    }

    /** Sets the player's icon; an empty one goes back to the default. */
    @PutMapping("/avatar")
    public ResponseEntity<Object> setAvatar(@AuthenticationPrincipal User user, @RequestBody Map<String, String> body) {
        String avatar = body.get("avatar");
        if (avatar != null && avatar.isBlank()) avatar = null;
        if (avatar != null && !AVATAR.matcher(avatar).matches()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ícono no válido"));
        }
        user.setAvatar(avatar);
        userRepository.save(user);
        return ResponseEntity.ok(profile(user));
    }

    private static Map<String, Object> profile(User user) {
        Map<String, Object> out = new HashMap<>();
        out.put("id", user.getId());
        out.put("username", user.getUsername());
        out.put("avatar", user.getAvatar());
        return out;
    }
}
