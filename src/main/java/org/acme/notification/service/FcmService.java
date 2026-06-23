package org.acme.notification.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FcmService {

    private static final Logger LOG = Logger.getLogger(FcmService.class);

    @ConfigProperty(name = "firebase.service-account")
    String serviceAccountJson;

    private FirebaseApp firebaseApp;

    @PostConstruct
    void init() {
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            LOG.warn("firebase.service-account is not configured; push notifications are disabled");
            return;
        }
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)));
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            firebaseApp = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(options)
                    : FirebaseApp.getInstance();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize Firebase Admin SDK", e);
        }
    }

    public SendResult send(String token, String title, String body) {
        if (firebaseApp == null) {
            LOG.warn("Skipping push notification: Firebase Admin SDK is not initialized");
            return SendResult.FAILED;
        }

        Message message = Message.builder()
                .setToken(token)
                .putData("title", title)
                .putData("body", body)
                .putData("url", "/activities")
                .build();

        try {
            FirebaseMessaging.getInstance(firebaseApp).send(message);
            return SendResult.SENT;
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                LOG.infof("FCM token is no longer registered: %s", token);
                return SendResult.INVALID_TOKEN;
            }
            LOG.errorf(e, "Failed to send push notification to token %s", token);
            return SendResult.FAILED;
        }
    }

    public enum SendResult {
        SENT,
        INVALID_TOKEN,
        FAILED
    }
}
