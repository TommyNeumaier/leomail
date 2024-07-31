package at.htlleonding.leomail.contracts;

import at.htlleonding.leomail.model.dto.KeycloakTokenIntrospectionResponse;
import at.htlleonding.leomail.model.dto.template.KeycloakTokenResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.FormParam;

@Path("/realms/2425-5bhitm/protocol/openid-connect")
@RegisterRestClient(configKey = "keycloak-api")
public interface IKeycloak {

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    KeycloakTokenResponse login(@FormParam("client_id") String clientId,
                                @FormParam("client_secret") String clientSecret,
                                @FormParam("grant_type") String grantType,
                                @FormParam("username") String username,
                                @FormParam("password") String password,
                                @FormParam("scope") String scope);

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Object refreshToken(@FormParam("client_id") String clientId,
                                       @FormParam("client_secret") String clientSecret,
                                       @FormParam("grant_type") String grantType,
                                       @FormParam("refresh_token") String refreshToken);

    @POST
    @Path("/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    KeycloakTokenIntrospectionResponse introspectToken(@FormParam("client_id") String clientId,
                                                       @FormParam("client_secret") String clientSecret,
                                                       @FormParam("token") String token);

    @Path("/token")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    KeycloakTokenResponse serviceAccountLogin(@FormParam("client_id") String clientId,
                                              @FormParam("client_secret") String clientSecret,
                                              @FormParam("grant_type") String grantType);
}
