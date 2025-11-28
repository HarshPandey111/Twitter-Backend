package com.microblog.twitterclone.kafka;

import com.microblog.twitterclone.event.LikeEvent;
import com.microblog.twitterclone.event.TweetCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TWEET_TOPIC = "tweet-events";
    private static final String LIKE_TOPIC = "like-events";

    public void publishTweetCreated(TweetCreatedEvent event) {
        log.info("📤 Publishing tweet created event: tweetId={}", event.getTweetId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TWEET_TOPIC, event.getTweetId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Tweet event published successfully: tweetId={}", event.getTweetId());
            } else {
                log.error("❌ Failed to publish tweet event: {}", ex.getMessage());
            }
        });
    }

    public void publishLikeEvent(LikeEvent event) {
        log.info("📤 Publishing like event for tweet: {}", event.getTweetId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(LIKE_TOPIC, event.getTweetId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Like event published successfully");
            } else {
                log.error("❌ Failed to publish like event: {}", ex.getMessage());
            }
        });
    }
}
