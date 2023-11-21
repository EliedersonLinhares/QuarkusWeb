package org.acme.user;

import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acme.security.SecurityUtils;
import org.acme.security.refreshtoken.RefreshTokenService;
import org.acme.security.verificationtoken.VerificationTokenModel;
import org.acme.utils.RegistrationCompleteEvent;
import org.acme.utils.ResponseBase;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;


@Path("/api/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;
    private final RefreshTokenService refreshTokenService;
    private final RegistrationCompleteEvent event;
    private final HttpServerRequest serverRequest;
    private final UserMapper userMapper;
    private final ResponseBase responseBase;


    @GET
    @RolesAllowed({"admin","user"})
    @Path("/{id}")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully retrieved"),
            @APIResponse(responseCode = "404", description = "User not found")
    })
    @Operation( description = "Returns a user as per the id,role user only can return your own data, admin can return anyone")
    public Response getUserById(@PathParam("id") Long id){
        securityUtils.decodeJwtDataUserFromCookie(id);
        UserModel user = userService.getUserById(id);
        return Response.ok(user).build();
    }


    @POST
    @Path("/login")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully login"),
            @APIResponse(responseCode = "404", description = "User not found or Credentials invalid"),
    })
    @Operation(description = "Login user and generate a JWT token and store on cookie")
    public Response loginByEmail(@RequestBody LoginDto loginDto){

      Map<String,Object> response =  userService.authenticate( loginDto);


        var cookie = Cookie.cookie("jwt", response.get("token").toString())
                .setSameSite(CookieSameSite.STRICT)
                .setPath("/api")
                .setMaxAge(45000000000L)
                .setHttpOnly(true)
                .setSecure(true)
                ;

        return Response.ok()
                .header(HttpHeaders.SET_COOKIE.toString(), cookie.encode())
                .build();
    }


    @GET
    @Path("/logout")
    @Operation(description = "Logout invalidating the cookie")
    public Response logout(){

        refreshTokenService.deleteToken();
        var cookie = Cookie.cookie("jwt", "")
                .setPath("/api")
                ;

        return Response.ok()
                .header(HttpHeaders.SET_COOKIE.toString(), cookie.encode())
                .build();
    }


    @GET
    @RolesAllowed("admin")
    @Path("/userpaginated")
    @Operation(description = "Return a list of users paginated, and with filters")
    public Response getAllUsersPaginated(
            @QueryParam("size")@DefaultValue("5") int size,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("sort")@DefaultValue("id") String sort,
            @QueryParam("order")@DefaultValue("Ascending") String order,
            @QueryParam("username") String username,
            @QueryParam("email") String email){

        if(Stream.of(username,email).allMatch(Objects::nonNull)){
            return  Response.ok( userService.findByUsernameAndEmail(sort,order,page,size,username,email)
            ).build();
        }
        if(Objects.nonNull(email)){
            return  Response.ok( userService.findByEmail(sort,order,page,size,email)
       ).build();
        }
        if(Objects.nonNull(username)){
            return  Response.ok( userService.findByUsername(sort,order,page,size,username)
        ).build();
        }
        //Order can be Ascending or Descending
        return  Response.ok(
                userService.findAllUsersSorted(sort,order,page,size)
        ).build();
    }


    @POST
    @Path("/register")
    @PermitAll
    @Transactional
    @Operation(description = "Register a new user")
    public Response saveUser(@Valid @RequestBody UserDto user, @Context HttpServerRequest request){
        userService.save2User(user);
        event.sendVerificationToken(user.email(),applicationURL(request));

     return Response.ok().build();

    }

    private String applicationURL(@Context HttpServerRequest request) {
       return "http://" + request.host();
    }


    @GET
    @Path("/verifyEmail")
    @PermitAll
    public Response verifyEmail( @QueryParam("token") String token){
        String url = applicationURL(serverRequest) + "/user/resend-verificationtoken?token=" + token;
        String verificationResult = userService.validateToken(token);

        return switch (verificationResult) {
            case "abuse" ->
                    responseBase.toResponse(Response.Status.OK.getStatusCode(), "Exceed number of verification, wait before try again!", "");
            case "timeout" ->
                    responseBase.toResponse(Response.Status.OK.getStatusCode(), "Time of new verification no reached, wait before try login!", "");
            case "invalid" ->
                    responseBase.toResponse(Response.Status.OK.getStatusCode(), "Invalid token ou checked account!", "");
            case "expired" ->
                    responseBase.toResponse(Response.Status.OK.getStatusCode(), "Confirmation time expired", url);
            default ->
                    responseBase.toResponse(Response.Status.OK.getStatusCode(), "verification success, now you can log on system!", "");
        };

    }

    @GET
    @Path("/resend-verificationtoken")
    @PermitAll
    public Response resendVerificationToken(@QueryParam("token") String oldToken,@Context HttpServerRequest request) {
        VerificationTokenModel verificationToken = userService.generateNewVerificationToken(oldToken);
        resendVerificationTokenEmail(applicationURL(request), verificationToken);

        return responseBase.toResponse(Response.Status.OK.getStatusCode(),"New link send to your email, please ,confirm for activate your account","");
    }

    private void resendVerificationTokenEmail(String applicationUrl, VerificationTokenModel verificationToken) {
        String url = applicationUrl + "/user/verifyEmail?token=" + verificationToken.getToken();
        log.info("Url example: {}", url);
    }

    @GET
    @Path("/userinformation")
    @PermitAll
    public Response getUserInformation(){
        return Response.ok(securityUtils.userfromIdentity()).build();

    }


    @PUT
    @RolesAllowed({"user","admin"})
    @Transactional
    @Path("/{id}")
    @Operation(description = "Update basic data of the user, role user only can update your own data, admin can update any user data")
    public Response updateUser(@Valid @PathParam("id") Long id, @RequestBody UpdateUserDto userDto){
        securityUtils.decodeJwtDataUserFromCookie(id);
        userService.update2User(userDto,id);
            return  Response.ok().build();

    }


    @PUT
    @RolesAllowed("admin")
    @Transactional
    @Path("/roles/{id}")
    @Operation(description = "Update user status role, can be made only by admin")
    public Response updateUserRole(@PathParam("id") Long id, @RequestBody UpdateUserRole userDto){
        securityUtils.decodeJwtDataUserFromCookie(id);
        userService.updateUserRole(userDto,id);
        return  Response.ok().build();
    }

    @PUT
    @RolesAllowed({"user","admin"})
    @Transactional
    @Path("/password/{id}")
    @Operation(description = "Update user password, role user can update your own password")
    public Response updateUserPassword(@PathParam("id") Long id, @RequestBody UpdatePassword userDto){
        securityUtils.decodeJwtDataUserFromCookie(id);
        userService.updateUserPassword(userDto,id);
        return  Response.ok().build();
    }


    @DELETE
   @RolesAllowed("admin")
    @Path("/{id}")
    @Operation(description = "Delete a user from system")
    public Response deleteUser(@PathParam("id") Long id){
        securityUtils.decodeJwtDataUserFromCookie(id);
        userService.deleteUser(id);
       return Response.noContent().build();

    }


    @POST
    @PermitAll
    @Transactional
    @Path("/refreshtoken")
    public Response refreshToken(){
        String id = securityUtils.getIdfromDecodedCookie();
        UserModel userModel = userService.getUserById(Long.parseLong(id));
        Map<String,Object> response =  userService.newJWT(userModel);

        var cookie = Cookie.cookie("jwt", response.get("token").toString())
                .setSameSite(CookieSameSite.STRICT)
                .setPath("/api")
                .setMaxAge(45000000000L)
                .setHttpOnly(true)
                .setSecure(true)
                ;

        return Response.ok()
                .header(HttpHeaders.SET_COOKIE.toString(), cookie.encode())
                .build();

    }

}
