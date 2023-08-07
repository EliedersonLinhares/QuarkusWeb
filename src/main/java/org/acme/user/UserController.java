package org.acme.user;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;


@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class UserController {


  private final UserService userService;

    @GET
    @RolesAllowed({"admin","user"})
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id){

        UserModel user = userService.getUserById(id);
        return Response.ok(user).build();
    }
    @GET
    @Path("/login")
    public Response loginByEmail(@RequestBody LoginDto loginDto){
      Map<String,Object> response =  userService.authenticate(loginDto);

        String cookieExpires = userService.getDateTimeInCookieFormat();
        return Response.ok(response.get("user")).header("Set-Cookie", "jwt="+response.get("token")+ ";Expires="+cookieExpires+"; HttpOnly=true ").build();
    }



    @GET
    @Path("/logout")
    public Response logout(){
        return Response.ok().header("Set-Cookie", "jwt=;Expires=;").build();
    }

    @GET
   @RolesAllowed("admin")
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
    @PermitAll
    @Transactional
    public Response saveUser(@Valid @RequestBody UserDto user){
      userService.save2User(user);
     return Response.created(URI.create("/user/" + user.id())).build();

    }

    @PUT
    @RolesAllowed("user")
    @Transactional
    @Path("/{id}")
    public Response updateUser(@Valid @PathParam("id") Long id, @RequestBody UpdateUserDto userDto){
            userService.update2User(userDto,id);
            return  Response.ok().build();

    }
    @PUT
    @RolesAllowed("admin")
    @Transactional
    @Path("/roles/{id}")
    public Response updateUserRole(@PathParam("id") Long id, @RequestBody UpdateUserRole userDto){

        userService.updateUserRole(userDto,id);
        return  Response.ok().build();
    }
    @PUT
    @RolesAllowed({"user","admin"})
    @Transactional
    @Path("/password/{id}")
    public Response updateUserPassword(@PathParam("id") Long id, @RequestBody UpdatePassword userDto){

        userService.updateUserPassword(userDto,id);
        return  Response.ok().build();
    }

    @DELETE
   @RolesAllowed("admin")
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id){
       userService.deleteUser(id);
       return Response.noContent().build();

    }

}
