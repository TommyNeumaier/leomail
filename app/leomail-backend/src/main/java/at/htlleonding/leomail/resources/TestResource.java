package at.htlleonding.leomail.resources;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.util.LinkedList;
import java.util.List;

@Path("test")
public class TestResource {
    private List<String> list = new LinkedList<>();

    public TestResource() {
        list.add("Hello");
        list.add("World");
    }

    @GET
    @Path("get")
    public List<String> get() {
        return list;
    }

    @POST
    @Path("add")
    public String add(String s) {
        list.add(s);
        return s;
    }

    @GET
    @Path("authtest")
    @RolesAllowed("user")
    public String authtest() {
        return "You are authenticated!";
    }
}
