 package org.triple_brain.service.resources;
 
 import com.google.inject.Injector;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.module.model.User;
 import org.triple_brain.module.model.graph.GraphFactory;
 import org.triple_brain.module.model.graph.UserGraph;
 import org.triple_brain.module.repository.user.UserRepository;
 import org.triple_brain.module.search.GraphIndexer;
 
 import javax.annotation.security.PermitAll;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.net.URI;
 import java.util.Map;
 
 import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
 import static org.triple_brain.module.model.json.UserJsonFields.*;
 import static org.triple_brain.module.model.validator.UserValidator.*;
 import static org.triple_brain.service.resources.GraphManipulatorResourceUtils.isUserInSession;
 import static org.triple_brain.service.resources.GraphManipulatorResourceUtils.userFromSession;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 
 @Path("/")
 @PermitAll
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 @Singleton
 public class UserResource{
 
     @Inject
     UserRepository userRepository;
 
     @Inject
     GraphIndexer graphIndexer;
 
     @Inject
     private GraphFactory graphFactory;
 
     @Inject
     GraphResourceFactory graphResourceFactory;
 
     @Inject
     DrawnGraphResourceFactory drawnGraphResourceFactory;
 
     @Inject
     SearchResourceFactory searchResourceFactory;
 
     @Inject
     private Injector injector;
 
     @Context
     HttpServletRequest request;
 
     @Path("{username}/graph")
     public GraphResource graphResource(
             @PathParam("username") String username
     ) {
         if (isUserNameTheOneInSession(username)) {
             return graphResourceFactory.withUser(
                     userFromSession(request.getSession())
             );
         }
         throw new WebApplicationException(Response.Status.FORBIDDEN);
     }
 
     @Path("{username}/search")
     public SearchResource searchResource(
             @PathParam("username") String username
     ) {
         if (isUserNameTheOneInSession(username)) {
             return searchResourceFactory.withUser(
                     userFromSession(request.getSession())
             );
         }
         throw new WebApplicationException(Response.Status.FORBIDDEN);
     }
 
     @Path("{username}/admin")
    public AdminResource adminResource(
             @PathParam("username") String username
     ) {
         if(isUserNameTheOneInSession(username) && username.equals("vince")){
             return injector.getInstance(
                    AdminResource.class
             );
         }
         throw new WebApplicationException(
                 Response.Status.FORBIDDEN
         );
     }
     @Path("session")
     public UserSessionResource sessionResource(){
         return injector.getInstance(
                 UserSessionResource.class
         );
     }
 
     @POST
     @Produces(MediaType.WILDCARD)
     @Path("/")
     public Response createUser(JSONObject jsonUser){
         User user = User.withUsernameEmailAndLocales(
                 jsonUser.optString(USER_NAME, ""),
                 jsonUser.optString(EMAIL, ""),
                 jsonUser.optJSONArray(PREFERRED_LOCALES).toString()
        )
                 .password(jsonUser.optString(PASSWORD, ""));
 
         JSONArray jsonMessages = new JSONArray();
         Map<String, String> errors = validate(jsonUser);
 
         if (userRepository.emailExists(jsonUser.optString(EMAIL, "")))
             errors.put(EMAIL, ALREADY_REGISTERED_EMAIL);
 
         if (userRepository.usernameExists(jsonUser.optString(USER_NAME, "")))
             errors.put(USER_NAME, USER_NAME_ALREADY_REGISTERED);
 
         if (!errors.isEmpty()) {
             for (Map.Entry<String, String> entry : errors.entrySet()) {
                 try{
                     jsonMessages.put(new JSONObject().put(
                             "field", entry.getKey()
                     ).put(
                             "reason", entry.getValue()
                     ));
                 }catch(JSONException e){
                     throw new RuntimeException(e);
                 }
             }
 
             throw new WebApplicationException(Response
                     .status(BAD_REQUEST)
                     .entity(jsonMessages)
                     .build()
             );
         }
         userRepository.save(user);
         graphFactory.createForUser(user);
         UserGraph userGraph = graphFactory.loadForUser(user);
         graphIndexer.indexVertex(
                 userGraph.defaultVertex()
         );
         UserSessionResource.authenticateUserInSession(
                 user, request.getSession()
         );
         return Response.created(URI.create(
                 user.username()
         )).build();
     }
 
     @GET
     @Path("/is_authenticated")
     public Response isAuthenticated(@Context HttpServletRequest request) throws JSONException {
         return Response.ok(new JSONObject()
                 .put("is_authenticated", isUserInSession(request.getSession()))
         ).build();
     }
 
     private Boolean isUserNameTheOneInSession(String userName) {
         if(!isUserInSession(request.getSession())){
             return false;
         }
         User authenticatedUser = userFromSession(request.getSession());
         if (!authenticatedUser.username().equals(userName)) {
             return false;
         }
         return true;
     }
 
 }
