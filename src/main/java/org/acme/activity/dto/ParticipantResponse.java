package org.acme.activity.dto;

import org.acme.activity.model.ActivityParticipant;
import org.acme.activity.model.ParticipantStatus;

public record ParticipantResponse(Long userId, String name, String email, ParticipantStatus status) {

    public static ParticipantResponse from(ActivityParticipant participant) {
        return new ParticipantResponse(
                participant.user.id,
                participant.user.name,
                participant.user.email,
                participant.status);
    }
}
