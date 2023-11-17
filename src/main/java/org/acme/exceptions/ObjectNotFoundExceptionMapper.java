package org.acme.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ObjectNotFoundExceptionMapper implements ExceptionMapper<ObjectNotFoundException> {
    @Override
    public Response toResponse(ObjectNotFoundException e) {

        StandardMsg err = new StandardMsg(System.currentTimeMillis(), Response.Status.BAD_REQUEST.getStatusCode(),
                e.getMessage(),"");

        return Response.
                 status(Response.Status.BAD_REQUEST)
                .entity(err)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
