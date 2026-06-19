package org.acme.activity.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.acme.activity.dto.ActivityRequest;
import org.acme.activity.dto.ActivityResponse;
import org.acme.activity.dto.ParticipationRequest;
import org.acme.activity.model.Activity;
import org.acme.activity.model.ActivityParticipant;
import org.acme.activity.model.ParticipantStatus;
import org.acme.user.User;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ActivityService {

    @Inject
    JsonWebToken jwt;

    @Transactional
    public ActivityResponse create(ActivityRequest request) {
        Activity activity = new Activity();
        activity.name = request.name();
        activity.location = request.location();
        activity.description = request.description();
        activity.startTime = request.startTime();
        activity.endTime = request.endTime();
        activity.host = currentUser();
        activity.persist();

        activity.participants = inviteUsers(activity, request.invitedUserIds());

        return ActivityResponse.from(activity);
    }

    public List<ActivityResponse> listMine() {
        User user = currentUser();
        List<Activity> hosted = Activity.list("host", user);
        List<Activity> invited = ActivityParticipant.<ActivityParticipant>list("user", user)
                .stream().map(p -> p.activity).toList();

        List<Activity> all = new ArrayList<>(hosted);
        for (Activity activity : invited) {
            if (all.stream().noneMatch(a -> a.id.equals(activity.id))) {
                all.add(activity);
            }
        }
        return all.stream().map(ActivityResponse::from).toList();
    }

    public ActivityResponse getById(Long id) {
        return ActivityResponse.from(findAccessible(id));
    }

    @Transactional
    public ActivityResponse update(Long id, ActivityRequest request) {
        Activity activity = findOwned(id);
        activity.name = request.name();
        activity.location = request.location();
        activity.description = request.description();
        activity.startTime = request.startTime();
        activity.endTime = request.endTime();
        if (request.invitedUserIds() != null) {
            syncInvitedUsers(activity, request.invitedUserIds());
        }
        return ActivityResponse.from(activity);
    }

    @Transactional
    public void delete(Long id) {
        findOwned(id).delete();
    }

    @Transactional
    public ActivityResponse respond(Long id, ParticipationRequest request) {
        User user = currentUser();
        Activity activity = findOrThrow(id);
        ActivityParticipant participant = activity.participants.stream()
                .filter(p -> p.user.id.equals(user.id))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("No invite found for this activity", Response.Status.FORBIDDEN));

        participant.status = request.accept() ? ParticipantStatus.ACCEPTED : ParticipantStatus.DECLINED;
        participant.respondedAt = LocalDateTime.now();

        return ActivityResponse.from(activity);
    }

    private User currentUser() {
        User user = User.findByEmail(jwt.getName());
        if (user == null) {
            throw new WebApplicationException("User not found", Response.Status.UNAUTHORIZED);
        }
        return user;
    }

    private Activity findOwned(Long id) {
        Activity activity = findOrThrow(id);
        if (!activity.host.id.equals(currentUser().id)) {
            throw new WebApplicationException("Only the host can do this", Response.Status.FORBIDDEN);
        }
        return activity;
    }

    private Activity findAccessible(Long id) {
        Activity activity = findOrThrow(id);
        User user = currentUser();
        boolean isHost = activity.host.id.equals(user.id);
        boolean isInvited = activity.participants.stream().anyMatch(p -> p.user.id.equals(user.id));
        if (!isHost && !isInvited) {
            throw new WebApplicationException("Not allowed to view this activity", Response.Status.FORBIDDEN);
        }
        return activity;
    }

    private Activity findOrThrow(Long id) {
        Activity activity = Activity.findById(id);
        if (activity == null) {
            throw new WebApplicationException("Activity not found", Response.Status.NOT_FOUND);
        }
        return activity;
    }

    private List<ActivityParticipant> inviteUsers(Activity activity, List<Long> userIds) {
        List<ActivityParticipant> participants = new ArrayList<>();
        if (userIds == null || userIds.isEmpty()) {
            return participants;
        }
        for (User user : User.<User>list("id in ?1", userIds)) {
            ActivityParticipant participant = new ActivityParticipant();
            participant.activity = activity;
            participant.user = user;
            participant.status = ParticipantStatus.INVITED;
            participant.persist();
            participants.add(participant);
        }
        return participants;
    }

    private void syncInvitedUsers(Activity activity, List<Long> userIds) {
        Set<Long> requestedIds = Set.copyOf(userIds);
        Set<Long> existingIds = activity.participants.stream().map(p -> p.user.id).collect(Collectors.toSet());

        activity.participants.removeIf(p -> !requestedIds.contains(p.user.id));

        List<Long> newIds = requestedIds.stream().filter(id -> !existingIds.contains(id)).toList();
        activity.participants.addAll(inviteUsers(activity, newIds));
    }
}
