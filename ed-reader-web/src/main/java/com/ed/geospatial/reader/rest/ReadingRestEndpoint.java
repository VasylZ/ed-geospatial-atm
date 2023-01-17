package com.ed.geospatial.reader.rest;

import com.ed.geospatial.core.persistence.AtmQuery;
import com.ed.geospatial.core.persistence.model.Atm;
import com.ed.geospatial.reader.presentation.AtmPresentationService;
import com.ed.geospatial.reader.shared.ResponseMessage;
import com.ed.geospatial.reader.web.ReadWebJettyApp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("rest/v1/read")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReadingRestEndpoint {

    @Inject
    private AtmPresentationService presentation;

    public ReadingRestEndpoint() {
        presentation = ReadWebJettyApp.GUICE.getInstance(AtmPresentationService.class);
    }

    @GET
    @Path("get/{id}")
    public Response get(@PathParam("id") final String id) {
        Atm atm = presentation.get(id);
        if (atm == null) {
            return Response.ok(
                    new ResponseMessage<>(null, Response.Status.NOT_FOUND.getStatusCode())
            ).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.ok(
                new ResponseMessage<>(atm, Response.Status.OK.getStatusCode())
        ).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Path("find")
    public Response find(final AtmQuery query) {
        return Response.ok(
                presentation.find(query)
        ).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
