package org.acme.notification.service;

import java.util.List;

import org.acme.notification.dto.SendNotificationRequest;
import org.acme.notification.dto.SubscribeRequest;
import org.acme.notification.model.PushSubscription;
import org.acme.notification.service.FcmService.SendResult;
import org.acme.user.User;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class NotificationService {

    @Inject
    JsonWebToken jwt;

    @Inject
    FcmService fcmService;

    @Transactional
    public void subscribe(SubscribeRequest request) {
        PushSubscription subscription = PushSubscription.findByToken(request.token());
        if (subscription == null) {
            subscription = new PushSubscription();
            subscription.fcmToken = request.token();
            subscription.persist();
        }
        subscription.user = currentUser();
    }

    @Transactional
    public void sendNotification(SendNotificationRequest request) {
        List<PushSubscription> subscriptions = request.userId() != null
                ? PushSubscription.list("user.id", request.userId())
                : PushSubscription.listAll();

        for (PushSubscription subscription : subscriptions) {
            SendResult result = fcmService.send(subscription.fcmToken, request.title(), request.body());
            if (result == SendResult.INVALID_TOKEN) {
                subscription.delete();
            }
        }
    }

    private User currentUser() {
        User user = User.findByEmail(jwt.getName());
        if (user == null) {
            throw new WebApplicationException("User not found", Response.Status.UNAUTHORIZED);
        }
        return user;
    }
}
