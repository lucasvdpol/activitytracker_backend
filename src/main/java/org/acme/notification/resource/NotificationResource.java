package org.acme.notification.resource;

import org.acme.notification.dto.SendNotificationRequest;
import org.acme.notification.dto.SubscribeRequest;
import org.acme.notification.service.NotificationService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/notifications")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class NotificationResource {

    @Inject
    NotificationService notificationService;

    @POST
    @Path("/subscribe")
    public Response subscribe(@Valid SubscribeRequest request) {
        notificationService.subscribe(request);
        return Response.noContent().build();
    }

    @POST
    @Path("/send")
    public Response send(@Valid SendNotificationRequest request) {
        notificationService.sendNotification(request);
        return Response.noContent().build();
    }
}
