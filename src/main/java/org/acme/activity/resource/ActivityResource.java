package org.acme.activity.resource;

import java.util.List;

import org.acme.activity.dto.ActivityRequest;
import org.acme.activity.dto.ActivityResponse;
import org.acme.activity.dto.ParticipationRequest;
import org.acme.activity.service.ActivityService;

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

@Path("/activities")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ActivityResource {

    @Inject
    ActivityService activityService;

    @POST
    public Response create(@Valid ActivityRequest request) {
        return Response.status(Response.Status.CREATED).entity(activityService.create(request)).build();
    }

    @GET
    public List<ActivityResponse> list() {
        return activityService.listMine();
    }

    @GET
    @Path("/{id}")
    public ActivityResponse getById(@PathParam("id") Long id) {
        return activityService.getById(id);
    }

    @PUT
    @Path("/{id}")
    public ActivityResponse update(@PathParam("id") Long id, @Valid ActivityRequest request) {
        return activityService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        activityService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/participation")
    public ActivityResponse respond(@PathParam("id") Long id, @Valid ParticipationRequest request) {
        return activityService.respond(id, request);
    }
}
