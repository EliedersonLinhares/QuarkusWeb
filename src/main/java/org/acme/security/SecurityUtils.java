package org.acme.security;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.build.Jwt;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.exceptions.ObjectNotFoundException;
import org.acme.user.UserModel;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
This class show two ways to get user details, from SecurityIdentity and from cookie
, the data dependends on the whats has been include in Json.
 */
@ApplicationScoped
public class SecurityUtils {
    @Inject
    SecurityIdentity identity;


  public void getInformationFromIdentity(){
     String principal = identity.getPrincipal().getName();
     String firstName = principal.substring(principal.indexOf("firstName",principal.indexOf("=")) +12,principal.indexOf(",",principal.indexOf(",")));
       System.out.println(" ----- "+ firstName);
     //  String userid = principal.substring(principal.indexOf("id",principal.indexOf("=")) +3,principal.indexOf(",",principal.indexOf(",")+1));
      String userid = principal.substring(principal.indexOf("id",principal.indexOf("=")) +3,principal.indexOf("}",principal.indexOf("}")));
       System.out.println(" ----- "+ userid);
    //   String email = principal.substring(principal.indexOf("email",principal.indexOf("=")) +6,principal.indexOf("}",principal.indexOf("}")));
      // System.out.println(" ----- "+ email);

      System.out.println(" -----" + principal);
    }

    public Map<String, Object> userfromIdentity() {
        String principal = identity.getPrincipal().getName();
        String firstName = principal.substring(principal.indexOf("firstName",principal.indexOf("=")) +12,principal.indexOf(",",principal.indexOf(",")));

        String userid = principal.substring(principal.indexOf("id",principal.indexOf("=")) +3,principal.indexOf("}",principal.indexOf("}")));

        Map<String, Object> response = new HashMap<>();
        response.put("id", userid);
        response.put("username", firstName);
        return response;
    }

    public String getIdfromDecodedCookie(){
      try {
          HttpServerRequest request = ResteasyProviderFactory.getInstance().getContextData(HttpServerRequest.class);
          String[] chunks = request.getCookie("jwt").getValue().split("\\.");
          Base64.Decoder decoder = Base64.getUrlDecoder();
          //System.out.println("-------" + decoder);
          String payload = new String(decoder.decode(chunks[1]));

          return payload.substring(payload.indexOf("id", payload.indexOf("=")) + 3, payload.indexOf("}", payload.indexOf("}")));
      }catch (Exception e){
          throw new ObjectNotFoundException("Cookie inválido ou inexistente");
      }
      }

    public void decodeJwtDataUserFromCookie(long id) {
        HttpServerRequest request = ResteasyProviderFactory.getInstance().getContextData(HttpServerRequest.class);
        // System.out.println(" ----- "+request.getCookie("jwt").getValue());
        // String jwt =request.getCookie("jwt").getValue();
        String[] chunks = request.getCookie("jwt").getValue().split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        // String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));

       // String firstName = payload.substring(payload.indexOf("firstName")+10,payload.indexOf(","));
      String userId = payload.substring(payload.indexOf("id",payload.indexOf("=")) +3,payload.indexOf("}",payload.indexOf("}")));
      //  String email = payload.substring(payload.indexOf("email",payload.indexOf("="))+6 ,payload.indexOf("}",payload.indexOf("}")));
       String roles = payload.substring(payload.indexOf("[",payload.indexOf("["))+1 ,payload.indexOf("]",payload.indexOf("]")));

        if( Integer.parseInt(userId) != id && !roles.contains("admin")){
           throw new ObjectNotFoundException("User can return only your data");
        }

        //Prints to confirm data
        //System.out.println("payload----" + payload);
        //System.out.println("firstName-----" + firstName);
       // System.out.println("id-----" + userId);
        //System.out.println("roles-----" + roles);
    }

    public Map<String, Object> encryptJwt(UserModel userByEmail, Set<String> roles2) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", userByEmail.getId());
        user.put("firstName", userByEmail.getUsername());

        String token = Jwt.upn(user.toString())
                .groups(roles2)
                .expiresIn(Duration.ofMinutes(30))
                .sign();

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("token", token);
        return response;
    }

    /**
    public String getDateTimeInCookieFormat() {
        OffsetDateTime oneHourFromNow
                = OffsetDateTime.now(ZoneOffset.UTC)
                .plus(Duration.ofDays(365));
        return DateTimeFormatter.RFC_1123_DATE_TIME
                .format(oneHourFromNow);
    }
     **/
}
