package com.auth.users_service.service;

import java.time.Instant;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * Writes each successful login to the administrators' activity log ("activity_log", shared with DriveManager, which
 * reads it and keeps it 180 days). A failure there never stops the login.
 */
@Service
public class LoginActivity {

    static final String COLLECTION = "activity_log";

    private final MongoTemplate mongo;

    public LoginActivity(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public void record(String userId) {
        if (userId == null) return;
        try {
            mongo.insert(new Document("userId", userId).append("action", "LOGIN").append("at", Instant.now()), COLLECTION);
        } catch (RuntimeException e) {
            // The log is for the administrators: the player still signs in
        }
    }
}
