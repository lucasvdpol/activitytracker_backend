package org.acme.groups.resource;

import java.util.List;

import org.acme.groups.dto.GroupRequest;
import org.acme.groups.dto.GroupResponse;
import org.acme.groups.dto.JoinGroupRequest;
import org.acme.groups.service.GroupService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/groups")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class GroupResource {

    @Inject
    GroupService groupService;

    @POST
    public Response create(@Valid GroupRequest request) {
        return Response.status(Response.Status.CREATED).entity(groupService.create(request)).build();
    }

    @GET
    public List<GroupResponse> list() {
        return groupService.listMine();
    }

    @GET
    @Path("/{id}")
    public GroupResponse getById(@PathParam("id") Long id) {
        return groupService.getById(id);
    }

    @PUT
    @Path("/{id}")
    public GroupResponse update(@PathParam("id") Long id, @Valid GroupRequest request) {
        return groupService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        groupService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/join")
    public GroupResponse join(@Valid JoinGroupRequest request) {
        return groupService.join(request);
    }

    @DELETE
    @Path("/{id}/members/me")
    public Response leave(@PathParam("id") Long id) {
        groupService.leave(id);
        return Response.noContent().build();
    }
}
