package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.user.UpdateUserRequest;
import bg.svetozar.tastypizza.model.dto.user.UserDto;
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
}
