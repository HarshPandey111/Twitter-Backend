package com.microblog.twitterclone.service;

import com.microblog.twitterclone.dto.TweetResponseDTO;
import com.microblog.twitterclone.entity.Tweet;
import com.microblog.twitterclone.entity.User;
import com.microblog.twitterclone.repository.TweetRepository;
import com.microblog.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    // REMOVED: private final ObjectMapper objectMapper;

    private static final String FEED_KEY_PREFIX = "feed:user:";
    private static final int MAX_FEED_SIZE = 1000;

    @Transactional
    public void fanOutTweet(Tweet tweet) {
        User author = tweet.getAuthor();
        Set<User> followers = author.getFollowers();

        log.info("Fanning out tweet {} to {} followers",
                tweet.getId(), followers.size());

        TweetResponseDTO tweetDTO = mapToDTO(tweet);
        double score = tweet.getCreatedAt()
                .toEpochSecond(ZoneOffset.UTC);

        // Push to each follower's feed
        followers.forEach(follower -> {
            String feedKey = FEED_KEY_PREFIX + follower.getId();
            redisTemplate.opsForZSet().add(feedKey, tweetDTO, score);

            Long feedSize = redisTemplate.opsForZSet().size(feedKey);
            if (feedSize != null && feedSize > MAX_FEED_SIZE) {
                redisTemplate.opsForZSet()
                        .removeRange(feedKey, 0, feedSize - MAX_FEED_SIZE - 1);
            }
        });

        String authorFeedKey = FEED_KEY_PREFIX + author.getId();
        redisTemplate.opsForZSet().add(authorFeedKey, tweetDTO, score);

        log.info("Tweet {} successfully fanned out", tweet.getId());
    }

    public List<TweetResponseDTO> getUserFeed(Long userId, int page, int size) {
        String feedKey = FEED_KEY_PREFIX + userId;

        long start = (long) page * size;
        long end = start + size - 1;

        Set<Object> results = redisTemplate.opsForZSet()
                .reverseRange(feedKey, start, end);

        if (results == null || results.isEmpty()) {
            log.info("Feed is empty for user {}, loading from database", userId);
            return loadFeedFromDatabase(userId, page, size);
        }

        List<TweetResponseDTO> feed = new ArrayList<>();

        for (Object obj : results) {
            try {
                // Handle LinkedHashMap conversion manually
                if (obj instanceof LinkedHashMap) {
                    LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) obj;
                    TweetResponseDTO tweet = TweetResponseDTO.builder()
                            .id(((Number) map.get("id")).longValue())
                            .authorId(((Number) map.get("authorId")).longValue())
                            .authorUsername((String) map.get("authorUsername"))
                            .content((String) map.get("content"))
                            .likeCount((Integer) map.get("likeCount"))
                            .retweetCount((Integer) map.get("retweetCount"))
                            .createdAt(java.time.LocalDateTime.parse((String) map.get("createdAt")))
                            .build();
                    feed.add(tweet);
                } else if (obj instanceof TweetResponseDTO) {
                    feed.add((TweetResponseDTO) obj);
                }
            } catch (Exception e) {
                log.error("Error converting tweet: {}", e.getMessage(), e);
            }
        }

        log.info("Retrieved {} tweets from Redis feed for user {}",
                feed.size(), userId);

        return feed;
    }

    private List<TweetResponseDTO> loadFeedFromDatabase(
            Long userId, int page, int size
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<User> following = user.getFollowing();
        List<User> feedSources = new ArrayList<>(following);
        feedSources.add(user);

        List<Tweet> tweets = tweetRepository
                .findByAuthorInOrderByCreatedAtDesc(feedSources);

        List<TweetResponseDTO> feed = tweets.stream()
                .map(this::mapToDTO)
                .limit(size)
                .collect(Collectors.toList());

        String feedKey = FEED_KEY_PREFIX + userId;
        feed.forEach(tweetDTO -> {
            double score = tweetDTO.getCreatedAt()
                    .toEpochSecond(ZoneOffset.UTC);
            redisTemplate.opsForZSet().add(feedKey, tweetDTO, score);
        });

        return feed;
    }

    @Transactional
    public void removeTweetFromFeeds(Tweet tweet) {
        User author = tweet.getAuthor();
        Set<User> followers = author.getFollowers();

        TweetResponseDTO tweetDTO = mapToDTO(tweet);

        followers.forEach(follower -> {
            String feedKey = FEED_KEY_PREFIX + follower.getId();
            redisTemplate.opsForZSet().remove(feedKey, tweetDTO);
        });

        String authorFeedKey = FEED_KEY_PREFIX + author.getId();
        redisTemplate.opsForZSet().remove(authorFeedKey, tweetDTO);

        log.info("Removed tweet {} from all feeds", tweet.getId());
    }

    public void clearUserFeed(Long userId) {
        String feedKey = FEED_KEY_PREFIX + userId;
        redisTemplate.delete(feedKey);
        log.info("Cleared feed for user {}", userId);
    }

    private TweetResponseDTO mapToDTO(Tweet tweet) {
        return TweetResponseDTO.builder()
                .id(tweet.getId())
                .authorId(tweet.getAuthor().getId())
                .authorUsername(tweet.getAuthor().getUsername())
                .content(tweet.getContent())
                .likeCount(tweet.getLikeCount())
                .retweetCount(tweet.getRetweetCount())
                .createdAt(tweet.getCreatedAt())
                .build();
    }
}
