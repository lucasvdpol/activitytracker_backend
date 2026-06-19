package org.acme.groups.dto;

import java.util.List;

import org.acme.auth.dto.UserResponse;
import org.acme.groups.model.Group;

public record GroupResponse(
        Long id,
        String name,
        String joinCode,
        UserResponse host,
        List<UserResponse> members) {

    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.id,
                group.name,
                group.joinCode,
                UserResponse.from(group.host),
                group.members == null
                        ? List.of()
                        : group.members.stream().map(m -> UserResponse.from(m.user)).toList());
    }
}
