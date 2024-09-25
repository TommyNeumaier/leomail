package at.htlleonding.leomail.model.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error("Unhandled exception: ", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Ein unerwarteter Fehler ist aufgetreten.")
                .build();
    }
}
