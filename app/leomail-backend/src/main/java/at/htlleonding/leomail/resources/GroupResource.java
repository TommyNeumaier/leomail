package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.model.dto.groups.GroupDetailDTO;
import at.htlleonding.leomail.repositories.GroupRepository;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("groups")
public class GroupResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    GroupRepository groupRepository;

    @GET
    @Produces("application/json")
    @Path("/get/personal")
    @Authenticated
    // URL?pid={ProjektID}
    public Response getPersonalGroups(@QueryParam("pid") String projectId) {
        try {
            return Response.ok(groupRepository.getPersonalGroups(projectId, jwt.getClaim("sub"))).build();
        } catch (Exception e) {
            return Response.status(409).entity("E-Group-01").build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/get/details")
    @Authenticated
    // URL?pid={ProjektID}&gid={GruppenID}
    public Response getGroupDetails(@QueryParam("pid") String projectId, @QueryParam("gid") String groupId) {
        try {
            return Response.ok(groupRepository.getGroupDetails(projectId, jwt.getClaim("sub"), groupId)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(409).entity("E-Group-01").build();
        }
    }

    @DELETE
    @Path("/delete")
    @Transactional
    @Authenticated
    // URL?pid={ProjektID}&gid={GruppenID}
    public Response deleteGroup(@QueryParam("pid") String projectId, @QueryParam("gid") String groupId) {
        try {
            groupRepository.deleteGroup(projectId, jwt.getClaim("sub"), groupId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(409).entity("E-Group-01").build();
        }
    }

    @POST
    @Path("/add")
    @Transactional
    @Authenticated
    @Consumes("application/json")
    // URL?pid={ProjektID} und im POST-Body die Gruppeninformationen (Attribute wie in GroupDetailDTO)
    public Response addGroup(@QueryParam("pid") String projectId, GroupDetailDTO dto) {
        try {
            groupRepository.createGroup(projectId, jwt.getClaim("sub"), dto.description(), dto.name(), dto.members());
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(409).entity("E-Group-01").build();
        }
    }

    @POST
    @Path("/update")
    @Transactional
    @Authenticated
    @Consumes("application/json")
    // URL?pid={ProjektID} und im POST-Body die Gruppeninformationen (Attribute wie in GroupDetailDTO)
    public Response updateGroup(@QueryParam("pid") String projectId, GroupDetailDTO dto) {
        try {
            groupRepository.updateGroup(projectId, jwt.getClaim("sub"), dto.description(), dto.id(), dto.name(), dto.members());
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(409).entity("E-Group-01").build();
        }
    }
}
