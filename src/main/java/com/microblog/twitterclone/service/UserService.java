package com.microblog.twitterclone.service;

import com.microblog.twitterclone.dto.UserRegistrationDTO;
import com.microblog.twitterclone.dto.UserResponseDTO;
import com.microblog.twitterclone.entity.User;
import com.microblog.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public UserResponseDTO createUser(UserRegistrationDTO dto) {
        // Check if username already exists
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create user
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(hashPassword(dto.getPassword())) // Simple hash for now
                .bio(dto.getBio())
                .followersCount(0)
                .followingCount(0)
                .build();

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    // Update followUser method
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User to follow not found"));

        if (follower.getFollowing().add(following)) {
            follower.setFollowingCount(follower.getFollowingCount() + 1);
            following.setFollowersCount(following.getFollowersCount() + 1);

            userRepository.save(follower);
            userRepository.save(following);

            // ADDED: Send real-time notification
            notificationService.createFollowNotification(
                    followingId,
                    follower.getUsername()
            );
        }
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User to unfollow not found"));

        // Remove from following set
        if (follower.getFollowing().remove(following)) {
            follower.setFollowingCount(follower.getFollowingCount() - 1);
            following.setFollowersCount(following.getFollowersCount() - 1);

            userRepository.save(follower);
            userRepository.save(following);
        }
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }

    // Simple password hashing (use BCrypt in production)
    private String hashPassword(String password) {
        return "HASHED_" + password; // TODO: Use BCrypt
    }

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
