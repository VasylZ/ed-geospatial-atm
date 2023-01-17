package com.ed.geospatial.writer.rest;

import com.ed.geospatial.core.persistence.model.Atm;
import com.ed.geospatial.writer.modification.AtmModificationService;
import com.ed.geospatial.writer.shared.ResponseMessage;
import com.ed.geospatial.writer.web.WriteWebJettyApp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("rest/v1/write")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WritingRestEndpoint {

    @Inject
    private AtmModificationService modificationService;

    public WritingRestEndpoint() {
        modificationService = WriteWebJettyApp.GUICE.getInstance(AtmModificationService.class);
    }

    @POST
    @Path("create")
    public Response create(final Atm mo) {
        modificationService.create(mo);
        return Response.ok(
                new ResponseMessage<>(mo, Response.Status.OK.getStatusCode())
        ).build();
    }

    @PUT
    @Path("update")
    public Response update(final Atm mo) {
        modificationService.update(mo);
        return Response.ok(
                new ResponseMessage<>(mo, Response.Status.OK.getStatusCode())
        ).build();
    }
}
