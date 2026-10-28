package org.acme.users;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/whoami")
public class WhoAmiResource {

    @Inject
    Template whoami;

    @Inject
    SecurityContext sc;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        var userPrincipal = sc.getUserPrincipal();
        var userId = (userPrincipal != null) ? userPrincipal.getName() : null;
        return whoami.data("name", userId);
    }

}
