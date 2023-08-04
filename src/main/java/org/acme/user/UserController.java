package org.acme.user;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.net.URI;
import java.util.Objects;
import java.util.stream.Stream;


@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id){
        UserModel user = userService.getUserById(id);
        if(Objects.nonNull(user)) {
            return Response.ok(user).build();
        }
        return Response.noContent().build();

    }
    @GET
    @Path("/userpaginated")
    public Response getAllUsersPaginated(
            @QueryParam("size")@DefaultValue("5") int size,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("sort")@DefaultValue("id") String sort,
            @QueryParam("order")@DefaultValue("Ascending") String order,
            @QueryParam("firstname") String firstName,
            @QueryParam("lastname") String lastName){

        if(Stream.of(firstName,lastName).allMatch(Objects::nonNull)){
            return  Response.ok( userService.findByfirstNameAndlastName(sort,order,page,size,firstName,lastName)
            ).build();
        }
        if(Objects.nonNull(firstName)){
            return  Response.ok( userService.findByFirstName(sort,order,page,size,firstName)
       ).build();
        }
        if(Objects.nonNull(lastName)){
            return  Response.ok( userService.findBylastName(sort,order,page,size,lastName)
        ).build();
        }
        //Order can be Ascending or Descending
        return  Response.ok(
                userService.findAllUsersSorted(sort,order,page,size)
        ).build();
    }

    @POST
    @Transactional
    public Response saveUser(@RequestBody UserDto user){
       String response = userService.save2User(user);
        if(response.contains("saved")){
            return Response.created(URI.create("/user/" + user.id())).build();
        }
        else{
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Transactional
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Long id, @RequestBody UserDto userDto){
            String response = userService.update2User(userDto,id);
             if(response.contains("updated")){
                 return  Response.ok().build();
             }
          return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id){
       if(userService.deleteUser(id)){
           return Response.noContent().build();
       }
       else{
           return Response.status(Response.Status.BAD_REQUEST).build();
       }
    }

}
