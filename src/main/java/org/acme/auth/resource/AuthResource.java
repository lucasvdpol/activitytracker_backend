package org.acme.auth.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.auth.dto.LoginRequest;
import org.acme.auth.dto.RefreshTokenRequest;
import org.acme.auth.dto.RegisterRequest;
import org.acme.auth.service.AuthService;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        return Response.status(Response.Status.CREATED).entity(authService.register(request)).build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        return Response.ok(authService.login(request)).build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(@Valid RefreshTokenRequest request) {
        return Response.ok(authService.refresh(request)).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Valid RefreshTokenRequest request) {
        authService.logout(request);
        return Response.noContent().build();
    }
}
