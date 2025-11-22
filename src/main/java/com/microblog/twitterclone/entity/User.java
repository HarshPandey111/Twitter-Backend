package com.microblog.twitterclone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 160)
    private String bio;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Users this user is following
    @ManyToMany
    @JoinTable(
            name = "user_follows",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default  // ← ADD THIS
    private Set<User> following = new HashSet<>();

    // Users following this user
    @ManyToMany(mappedBy = "following")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default  // ← ADD THIS
    private Set<User> followers = new HashSet<>();

    @Builder.Default  // ← ADD THIS
    private Integer followersCount = 0;

    @Builder.Default  // ← ADD THIS
    private Integer followingCount = 0;
}
