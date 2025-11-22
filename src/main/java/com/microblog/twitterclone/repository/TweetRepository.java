package com.microblog.twitterclone.repository;

import com.microblog.twitterclone.entity.Tweet;
import com.microblog.twitterclone.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {
    Page<Tweet> findByAuthor(User author, Pageable pageable);
    List<Tweet> findByAuthorOrderByCreatedAtDesc(User author);
    List<Tweet> findByAuthorInOrderByCreatedAtDesc(List<User> authors);

}
