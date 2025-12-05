package com.microblog.twitterclone.kafka;

import com.microblog.twitterclone.entity.Tweet;
import com.microblog.twitterclone.event.LikeEvent;
import com.microblog.twitterclone.event.TweetCreatedEvent;
import com.microblog.twitterclone.repository.TweetRepository;
import com.microblog.twitterclone.service.FeedService;
import com.microblog.twitterclone.service.NotificationService; // ADD THIS
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final FeedService feedService;
    private final TweetRepository tweetRepository;
    private final NotificationService notificationService; // ADD THIS

    @KafkaListener(topics = "tweet-events", groupId = "microblog-group")
    public void consumeTweetCreated(TweetCreatedEvent event) {
        log.info("📥 Consumed tweet created event: tweetId={}", event.getTweetId());

        try {
            Tweet tweet = tweetRepository.findById(event.getTweetId())
                    .orElseThrow(() -> new RuntimeException("Tweet not found"));

            feedService.fanOutTweet(tweet);

            log.info("✅ Successfully processed tweet event: tweetId={}", event.getTweetId());
        } catch (Exception e) {
            log.error("❌ Error processing tweet event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "like-events", groupId = "microblog-group")
    public void consumeLikeEvent(LikeEvent event) {
        log.info("📥 Consumed like event for tweet: {}", event.getTweetId());

        try {
            log.info("👍 User {} {} tweet {}",
                    event.getUsername(),
                    event.getEventType(),
                    event.getTweetId());

            // UPDATED: Send real-time notification
            if ("LIKE".equals(event.getEventType())) {
                Tweet tweet = tweetRepository.findById(event.getTweetId())
                        .orElseThrow(() -> new RuntimeException("Tweet not found"));

                // Send notification to tweet author
                notificationService.createLikeNotification(
                        tweet.getAuthor().getId(),
                        event.getUsername(),
                        event.getTweetId()
                );
            }

            log.info("✅ Successfully processed like event");
        } catch (Exception e) {
            log.error("❌ Error processing like event: {}", e.getMessage(), e);
        }
    }
}
