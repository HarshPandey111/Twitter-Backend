package com.microblog.twitterclone.service;

import com.microblog.twitterclone.dto.TweetRequestDTO;
import com.microblog.twitterclone.dto.TweetResponseDTO;
import com.microblog.twitterclone.entity.Tweet;
import com.microblog.twitterclone.entity.User;
import com.microblog.twitterclone.repository.TweetRepository;
import com.microblog.twitterclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.microblog.twitterclone.event.LikeEvent;
import com.microblog.twitterclone.event.TweetCreatedEvent;
import com.microblog.twitterclone.kafka.KafkaProducerService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TweetService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducer;
    private final FeedService feedService;

    @Transactional
    public TweetResponseDTO createTweet(Long userId, TweetRequestDTO dto) {

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Tweet tweet = Tweet.builder()
                .author(author)
                .content(dto.getContent())
                .likeCount(0)
                .retweetCount(0)
                .build();

        Tweet savedTweet = tweetRepository.save(tweet);

        // --------------------------------------------
        // NEW: Safe Kafka publish (non-critical)
        // --------------------------------------------
        try {
            kafkaProducer.publishTweetCreated(
                    TweetCreatedEvent.builder()
                            .tweetId(savedTweet.getId())
                            .authorId(author.getId())
                            .authorUsername(author.getUsername())
                            .content(savedTweet.getContent())
                            .createdAt(savedTweet.getCreatedAt())
                            .build()
            );
        } catch (Exception e) {
            log.warn("Kafka publish failed (non-critical): {}", e.getMessage());
        }

        return mapToDTO(savedTweet);
    }


    public TweetResponseDTO getTweetById(Long tweetId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new RuntimeException("Tweet not found with id: " + tweetId));
        return mapToDTO(tweet);
    }

    public List<TweetResponseDTO> getUserTweets(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Tweet> tweets = tweetRepository.findByAuthor(user, pageable);

        return tweets.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TweetResponseDTO> getAllTweets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Tweet> tweets = tweetRepository.findAll(pageable);

        return tweets.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TweetResponseDTO updateTweet(Long tweetId, Long userId, TweetRequestDTO dto) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new RuntimeException("Tweet not found with id: " + tweetId));

        if (!tweet.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You can only edit your own tweets");
        }

        tweet.setContent(dto.getContent());
        Tweet updatedTweet = tweetRepository.save(tweet);

        return mapToDTO(updatedTweet);
    }

    @Transactional
    public void deleteTweet(Long tweetId, Long userId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new RuntimeException("Tweet not found with id: " + tweetId));

        if (!tweet.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own tweets");
        }

        feedService.removeTweetFromFeeds(tweet);

        tweetRepository.delete(tweet);
    }

    @Transactional
    public TweetResponseDTO likeTweet(Long tweetId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new RuntimeException("Tweet not found with id: " + tweetId));

        tweet.setLikeCount(tweet.getLikeCount() + 1);
        Tweet updatedTweet = tweetRepository.save(tweet);

        LikeEvent event = LikeEvent.builder()
                .tweetId(tweetId)
                .userId(tweet.getAuthor().getId())
                .username(tweet.getAuthor().getUsername())
                .timestamp(java.time.LocalDateTime.now())
                .eventType("LIKE")
                .build();

        try {
            kafkaProducer.publishLikeEvent(event);
        } catch (Exception e) {
            log.warn("Kafka LikeEvent publish failed: {}", e.getMessage());
        }

        return mapToDTO(updatedTweet);
    }

    @Transactional
    public TweetResponseDTO unlikeTweet(Long tweetId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new RuntimeException("Tweet not found with id: " + tweetId));

        if (tweet.getLikeCount() > 0) {
            tweet.setLikeCount(tweet.getLikeCount() - 1);
        }

        Tweet updatedTweet = tweetRepository.save(tweet);
        return mapToDTO(updatedTweet);
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
