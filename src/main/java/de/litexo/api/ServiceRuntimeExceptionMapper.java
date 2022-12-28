package de.litexo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.litexo.model.external.ServiceError;
import de.litexo.repository.DefaultRepository;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static de.litexo.model.external.ServiceErrorType.RUNTIME_EXCEPTION;
import static javax.ws.rs.core.Response.status;

@Provider
public class ServiceRuntimeExceptionMapper implements ExceptionMapper<ServiceRuntimeException> {
    private static final Logger LOG = Logger.getLogger(ServiceRuntimeExceptionMapper.class);

    private static String toJson(final Object result) throws JsonProcessingException {

        return new ObjectMapper().writeValueAsString(result);
    }

    @Override
    public Response toResponse(final ServiceRuntimeException ex) {

        try {
            final ServiceError se = new ServiceError();
            se.setType(RUNTIME_EXCEPTION);
            se.setMessage(ex.getMessage());
            se.setStackTrace(ExceptionUtils.getStackTrace(ex));
            return status(500).type(MediaType.APPLICATION_JSON).entity(toJson(se)).build();
        } catch (final JsonProcessingException e) {
            LOG.error("Error while exception mapping", e);
            return status(500).build();
        }

    }
}
