package com.microblog.twitterclone.controller;

import com.microblog.twitterclone.dto.UserRegistrationDTO;
import com.microblog.twitterclone.dto.UserResponseDTO;
import com.microblog.twitterclone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRegistrationDTO dto
    ) {
        UserResponseDTO user = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{followerId}/follow/{followingId}")
    public ResponseEntity<String> followUser(
            @PathVariable Long followerId,
            @PathVariable Long followingId
    ) {
        userService.followUser(followerId, followingId);
        return ResponseEntity.ok("Successfully followed user");
    }

    @DeleteMapping("/{followerId}/unfollow/{followingId}")
    public ResponseEntity<String> unfollowUser(
            @PathVariable Long followerId,
            @PathVariable Long followingId
    ) {
        userService.unfollowUser(followerId, followingId);
        return ResponseEntity.ok("Successfully unfollowed user");
    }
}
