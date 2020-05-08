 package org.fiteagle.delivery.rest.fiteagle;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.security.NoSuchAlgorithmException;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.fiteagle.core.userdatabase.User;
 import org.fiteagle.core.userdatabase.UserPersistable.InValidAttributeException;
 import org.fiteagle.core.userdatabase.UserPersistable.DatabaseException;
 import org.fiteagle.core.userdatabase.UserPersistable.DuplicateUsernameException;
 import org.fiteagle.core.userdatabase.UserPersistable.NotEnoughAttributesException;
 import org.fiteagle.core.userdatabase.UserPersistable.RecordNotFoundException;
 import org.fiteagle.interactors.api.UserManagerBoundary;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Inject;
 
 @Path("v1/user")
 public class UserPresenter{
   
   private static Logger log = LoggerFactory.getLogger(UserPresenter.class);
   
   private final UserManagerBoundary manager;
   
   @Inject
   public UserPresenter(final UserManagerBoundary manager){
     this.manager = manager;
   }
   
   @GET
   @Path("{username}")
   @Produces(MediaType.APPLICATION_JSON)
   public User getUser(@PathParam("username") String username) {
     User user = null;
     try {
       user = manager.get(username);
     } catch (RecordNotFoundException e) {
       throw new WebApplicationException(Response.Status.NOT_FOUND);
     } catch (DatabaseException e) {
       log.error(e.getMessage());
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
     }
     return user;
   }
   
   @PUT
   @Path("{username}")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response addUser(@PathParam("username") String username, NewUser user) {
     user.setUsername(username);
     try {
       manager.add(createUser(user));
     } catch (DuplicateUsernameException e) {
       throw new WebApplicationException(Response.Status.CONFLICT);
     } catch (DatabaseException e) {
       log.error(e.getMessage());
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    } catch (NotEnoughAttributesException | InValidAttributeException e) {
      throw new WebApplicationException(Response.status(422).build());
     }
     return Response.status(201).build();
   }
 
   @POST
   @Path("{username}")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response updateUser(@PathParam("username") String username, NewUser user) {
     user.setUsername(username);
     try {
       manager.update(createUser(user));    
     } catch (DatabaseException e) {
       log.error(e.getMessage());
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);  
     } catch (RecordNotFoundException e) {
       throw new WebApplicationException(Response.Status.NOT_FOUND);      
    } catch (NotEnoughAttributesException | InValidAttributeException e) {
      throw new WebApplicationException(Response.status(422).build());
     }
     return Response.status(200).build();
   }
 
   private User createUser(NewUser newUser){
     User user = null;
     try {
       user = new User(newUser.getUsername(), newUser.getFirstName(), newUser.getLastName(),
           newUser.getEmail(), newUser.getPassword(), newUser.getPublicKeys());
     } catch (NoSuchAlgorithmException e) {
       log.error(e.getMessage());
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
     }
     return user;
   }
   
   @POST
   @Path("{username}/pubkey/")
   @Consumes("text/plain")
   public Response addPublicKey(@PathParam("username") String username, String pubkey) {    
     try {
       manager.addKey(username, pubkey);
     } catch (InValidAttributeException e){
       throw new WebApplicationException(Response.status(422).build());
     } catch (RecordNotFoundException e) {
       throw new WebApplicationException(Response.Status.NOT_FOUND);
     } catch (DatabaseException e) {
       log.error(e.getMessage());
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
     }
     return Response.status(200).build();
   }
   
   @DELETE
   @Path("{username}/pubkey/{pubkey}")
   public Response deletePublicKey(@PathParam("username") String username, @PathParam("pubkey") String pubkey) {    
     try {
       String decodedPublicKey = URLDecoder.decode(pubkey, "UTF-8");
       manager.deleteKey(username, decodedPublicKey);
     } catch (InValidAttributeException e){
       throw new WebApplicationException(Response.status(422).build());
     } catch (RecordNotFoundException e) {
       throw new WebApplicationException(Response.Status.NOT_FOUND);
     } catch (UnsupportedEncodingException | DatabaseException e) {
       log.error(e.getMessage());
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
     }
     return Response.status(200).build();
   }
   
   @DELETE
   @Path("{username}")
   public Response deleteUser(@PathParam("username") String username) {
     try {
       manager.delete(username);
     } catch (DatabaseException e) {
       log.error(e.getMessage());
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
     }
     return Response.status(200).build();
   }
   
   @POST
   @Path("{username}/certificate")
   @Consumes("text/plain")
   public String getUserCertAndPrivateKey(@PathParam("username") String username, String passphrase) {  
     if(passphrase == null || passphrase.length() == 0){
       throw new WebApplicationException(Response.status(422).build());
     }
     try {      
       return manager.createUserPrivateKeyAndCertAsString(username, passphrase);
     } catch (Exception e) {
       log.error(e.getMessage());
       throw new RuntimeException(e.getCause());
     }    
   }
   
   @POST
   @Path("{username}/certificate")
   public String getUserCertificate(@PathParam("username") String uid, String publicKeyEncoded) {
     try {
       return manager.createUserCertificate(uid, publicKeyEncoded);
     } catch (Exception e) {
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
     }
   } 
   
   @DELETE
   @Path("{username}/cookie")
   public Response deleteCookie(@PathParam("username") String username){
     AuthenticationFilter.getInstance().deleteCookie(username);
     return Response.status(200).build();
   }
 
 }
