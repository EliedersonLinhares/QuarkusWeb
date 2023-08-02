package org.acme.user;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

@Path("/user")


public class UserController {

    @Inject
    UserRepository userRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(){
        List<UserModel> users = userRepository.listAll();
        return Response.ok(users).build();
    }
    @GET
    @Path("/userpaginated")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getAllUsersPaginated(
            @QueryParam("size")@DefaultValue("5") int size,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("sort")@DefaultValue("id") String sort,
            @QueryParam("order")@DefaultValue("Ascending") String order,
            @QueryParam("firstname") String firstName,
            @QueryParam("lastname") String lastName){

        if(Stream.of(firstName,lastName).allMatch(Objects::nonNull)){
            return getPaginatedResponse(userRepository.find(
                    "lower(firstName) like :firstName and lower(lastName) like :lastName",
                    Sort.by(sort).direction(Sort.Direction.valueOf(order)),
                    Parameters.with("firstName", firstName.toLowerCase() + "%")
                            .and("lastName", lastName.toLowerCase() + "%")
            ), page, size);
        }

        if(Objects.nonNull(firstName)){
            return getPaginatedResponse(userRepository.find(
                    "lower(firstName) like :firstName",
                    Sort.by(sort).direction(Sort.Direction.valueOf(order)),
                    Parameters.with("firstName", firstName.toLowerCase() + "%")), page, size);
        }
        if(Objects.nonNull(lastName)){
            return getPaginatedResponse(userRepository.find(
                    "lower(lastName) like :lastName",
                    Sort.by(sort).direction(Sort.Direction.valueOf(order)),
                    Parameters.with("lastName", lastName.toLowerCase() + "%")), page, size);
        }

        //Order can be Ascending or Descending
        return getPaginatedResponse(userRepository.findAll(Sort.by(sort).direction(Sort.Direction.valueOf(order))), page, size);

    }

    private Response getPaginatedResponse(PanacheQuery<UserModel> userRepository, int page, int size) {
        userRepository.page(Page.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("users", userRepository.stream().toList());
        response.put("currentPage", page);
        response.put("totalItems", userRepository.count());
        response.put("totalPages", userRepository.pageCount());

        return Response.ok(response).build();
    }

    @GET
    @Path("/gender/{gender}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByGender(@PathParam("gender") String gender){

        List<UserModel> users = userRepository.list("gender",gender);
        if(users.isEmpty()){
            return Response.ok("nenhum usuário encontrado").build();
        }
        return Response.ok(users).build();
    }


    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveUser(UserModel user){
        userRepository.persist(user);
        if(userRepository.isPersistent(user)){
            return Response.created(URI.create("/user/" + user.id)).build();
        }
        else{
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("id") Long id){
        UserModel user = userRepository.findById(id);
        return Response.ok(user).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("id") Long id, UserModel user){
        Optional<UserModel> optionalUserModel = userRepository.findByIdOptional(id);
        if(optionalUserModel.isPresent()){
            UserModel updateUser = optionalUserModel.get();
            updateUser.setFirstName(user.getFirstName());
            updateUser.setLastName(user.getLastName());
            updateUser.setEmail(user.getEmail());
            updateUser.setGender(user.getGender());
            userRepository.persist(updateUser);
            if(userRepository.isPersistent(updateUser)){
                return  Response.ok().build();
            }else{
                return  Response.status((Response.Status.BAD_REQUEST)).build();
            }
        }
       return  Response.status((Response.Status.BAD_REQUEST)).build();
    }

    @DELETE
    @Path("/{id}/")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") Long id){
       boolean isDeleted = userRepository.deleteById(id);
       if(isDeleted){
           return Response.noContent().build();
       }
       else{
           return Response.status(Response.Status.BAD_REQUEST).build();
       }
    }

}
