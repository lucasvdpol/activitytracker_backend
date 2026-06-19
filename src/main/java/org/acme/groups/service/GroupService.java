package org.acme.groups.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.acme.groups.dto.GroupRequest;
import org.acme.groups.dto.GroupResponse;
import org.acme.groups.dto.JoinGroupRequest;
import org.acme.groups.model.Group;
import org.acme.groups.model.GroupMember;
import org.acme.user.User;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class GroupService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Inject
    JsonWebToken jwt;

    @Transactional
    public GroupResponse create(GroupRequest request) {
        Group group = new Group();
        group.name = request.name();
        group.host = currentUser();
        group.joinCode = generateUniqueJoinCode();
        group.persist();
        return GroupResponse.from(group);
    }

    public List<GroupResponse> listMine() {
        User user = currentUser();
        List<Group> hosted = Group.list("host", user);
        List<Group> joined = GroupMember.<GroupMember>list("user", user)
                .stream().map(m -> m.group).toList();

        List<Group> all = new ArrayList<>(hosted);
        for (Group group : joined) {
            if (all.stream().noneMatch(g -> g.id.equals(group.id))) {
                all.add(group);
            }
        }
        return all.stream().map(GroupResponse::from).toList();
    }

    public GroupResponse getById(Long id) {
        return GroupResponse.from(findAccessible(id));
    }

    @Transactional
    public GroupResponse update(Long id, GroupRequest request) {
        Group group = findOwned(id);
        group.name = request.name();
        return GroupResponse.from(group);
    }

    @Transactional
    public void delete(Long id) {
        findOwned(id).delete();
    }

    @Transactional
    public GroupResponse join(JoinGroupRequest request) {
        User user = currentUser();
        Group group = Group.<Group>find("joinCode", request.joinCode()).firstResultOptional()
                .orElseThrow(() -> new WebApplicationException("Group not found", Response.Status.NOT_FOUND));

        if (group.host.id.equals(user.id)) {
            throw new WebApplicationException("Host is already part of the group", Response.Status.CONFLICT);
        }
        boolean alreadyMember = group.members.stream().anyMatch(m -> m.user.id.equals(user.id));
        if (alreadyMember) {
            throw new WebApplicationException("Already a member of this group", Response.Status.CONFLICT);
        }

        GroupMember member = new GroupMember();
        member.group = group;
        member.user = user;
        member.joinedAt = LocalDateTime.now();
        member.persist();
        group.members.add(member);

        return GroupResponse.from(group);
    }

    @Transactional
    public void leave(Long id) {
        User user = currentUser();
        Group group = findAccessible(id);
        group.members.removeIf(m -> m.user.id.equals(user.id));
    }

    /**
     * Ids of users that share at least one group with the given user (as host or member),
     * excluding the user itself. Used to restrict who can be invited to an activity.
     */
    public Set<Long> sharedGroupMemberIds(User user) {
        List<Group> hosted = Group.list("host", user);
        List<Group> joined = GroupMember.<GroupMember>list("user", user)
                .stream().map(m -> m.group).toList();

        Set<Long> ids = new HashSet<>();
        for (Group group : hosted) {
            ids.add(group.host.id);
            group.members.forEach(m -> ids.add(m.user.id));
        }
        for (Group group : joined) {
            ids.add(group.host.id);
            group.members.forEach(m -> ids.add(m.user.id));
        }
        ids.remove(user.id);
        return ids;
    }

    private String generateUniqueJoinCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (Group.count("joinCode", code) > 0);
        return code;
    }

    private Group findOwned(Long id) {
        Group group = findOrThrow(id);
        if (!group.host.id.equals(currentUser().id)) {
            throw new WebApplicationException("Only the host can do this", Response.Status.FORBIDDEN);
        }
        return group;
    }

    private Group findAccessible(Long id) {
        Group group = findOrThrow(id);
        User user = currentUser();
        boolean isHost = group.host.id.equals(user.id);
        boolean isMember = group.members.stream().anyMatch(m -> m.user.id.equals(user.id));
        if (!isHost && !isMember) {
            throw new WebApplicationException("Not allowed to view this group", Response.Status.FORBIDDEN);
        }
        return group;
    }

    private Group findOrThrow(Long id) {
        Group group = Group.findById(id);
        if (group == null) {
            throw new WebApplicationException("Group not found", Response.Status.NOT_FOUND);
        }
        return group;
    }

    private User currentUser() {
        User user = User.findByEmail(jwt.getName());
        if (user == null) {
            throw new WebApplicationException("User not found", Response.Status.UNAUTHORIZED);
        }
        return user;
    }
}
