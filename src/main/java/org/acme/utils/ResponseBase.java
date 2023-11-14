package org.acme.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.exceptions.StandardMsg;

@ApplicationScoped
public class ResponseBase {

    public Response toResponse(Integer status, String msg) {

        StandardMsg err = new StandardMsg(System.currentTimeMillis(), status,
                msg);

        return Response.
                status(status)
                .entity(err)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
