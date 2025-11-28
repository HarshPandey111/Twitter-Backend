package com.microblog.twitterclone;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class TwitterCloneApplication {
	public static void main(String[] args)  {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(TwitterCloneApplication.class, args);
	}

}
