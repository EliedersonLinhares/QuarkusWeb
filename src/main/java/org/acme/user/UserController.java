package org.acme.user;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
        return Response.ok(user).build();
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
    public Response saveUser(@Valid @RequestBody UserDto user){
      userService.save2User(user);
     return Response.created(URI.create("/user/" + user.id())).build();

    }

    @PUT
    @Transactional
    @Path("/{id}")
    public Response updateUser(@Valid @PathParam("id") Long id, @RequestBody UserDto userDto){
            userService.update2User(userDto,id);
            return  Response.ok().build();

    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id){
       userService.deleteUser(id);
       return Response.noContent().build();

    }

}
