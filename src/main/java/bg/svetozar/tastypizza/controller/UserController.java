package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.user.*;
import bg.svetozar.tastypizza.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe() {
        return ResponseEntity.ok(userService.getProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }
    @PatchMapping("/me/fullname")
    public ResponseEntity<UserDto> updateFullName(@Valid @RequestBody UpdateFullNameRequest req) {
        return ResponseEntity.ok(userService.updateFullName(req));
    }

    @PatchMapping("/me/username")
    public ResponseEntity<Void> updateUsername(@Valid @RequestBody UpdateUsernameRequest req) {
        userService.updateUsername(req);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(req);
        return ResponseEntity.noContent().build();
    }
}
