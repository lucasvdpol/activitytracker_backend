package org.acme.activity.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.activity.model.Activity;
import org.acme.activity.model.ParticipantStatus;
import org.acme.auth.dto.UserResponse;

public record ActivityResponse(
        Long id,
        String name,
        String location,
        String description,
        Integer maxParticipants,
        long currentParticipants,
        LocalDateTime startTime,
        LocalDateTime endTime,
        UserResponse host,
        List<ParticipantResponse> participants) {

    public static ActivityResponse from(Activity activity) {
        long currentParticipants = activity.participants == null
                ? 0
                : activity.participants.stream().filter(p -> p.status == ParticipantStatus.ACCEPTED || p.status == ParticipantStatus.HOST).count();
        return new ActivityResponse(
                activity.id,
                activity.name,
                activity.location,
                activity.description,
                activity.maxParticipants,
                currentParticipants,
                activity.startTime,
                activity.endTime,
                UserResponse.from(activity.host),
                activity.participants == null
                        ? List.of()
                        : activity.participants.stream().map(ParticipantResponse::from).toList());
    }
}


