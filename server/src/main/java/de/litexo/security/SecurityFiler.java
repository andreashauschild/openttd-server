package de.litexo.security;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class SecurityFiler implements ContainerRequestFilter {
    @Inject
    SecurityService securityService;


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        securityService.validatedLoginSession(requestContext);
    }




}
