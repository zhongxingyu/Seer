 package si.setcce.societies.crowdtasking.api.RESTful.impl;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.gson.Gson;
 import com.googlecode.objectify.Ref;
 import si.setcce.societies.crowdtasking.api.RESTful.IUsersAPI;
 import si.setcce.societies.crowdtasking.api.RESTful.json.UserJS;
 import si.setcce.societies.crowdtasking.model.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import static si.setcce.societies.crowdtasking.model.dao.OfyService.ofy;
 
 @Path("/users/{querytype}")
 public class UsersAPI implements IUsersAPI {
     public static CTUser getUserById(Long userId) {
         return ofy().load().type(CTUser.class).id(userId).get();
     }
 
     public static CTUser getUser(Ref<CTUser> userRef) {
         return ofy().load().ref(userRef).get();
     }
 
     public static CTUser getUserByFederatedId(AuthenticatedUser authenticatedUser) {
         //AuthenticatedUser authenticatedUser = getAuthenticatedUser();
         if (authenticatedUser == null) {
             return null;
         }
         String federatedIdentity = authenticatedUser.getFederatedIdentity() == null ? "gugl" : authenticatedUser.getFederatedIdentity();
         System.out.println("getting user from db; provider:" + AuthenticationPrvider.getAuthenticationPrvider(federatedIdentity) +
                 ", userId:" + authenticatedUser.getUserId());
         CTUser user = ofy()
                 .load()
                 .type(CTUser.class)
                 .filter("connectedAccounts.provider", AuthenticationPrvider.getAuthenticationPrvider(federatedIdentity))
                 .filter("connectedAccounts.userId", authenticatedUser.getUserId())
                 .first().get();
         if (user != null) {
             System.out.println("Got:" + user.getUserName());
         } else {
             System.out.println("Got: nothing. Register new user.");
         }
         return user;
     }
 
     public static AuthenticatedUser getAuthenticatedUser(HttpSession session) {
         AuthenticatedUser authenticatedUser = (AuthenticatedUser) session.getAttribute("authenticatedUser");
         if (authenticatedUser != null) {
             return authenticatedUser;
         }
 
         UserService userService = UserServiceFactory.getUserService();
         if (userService.getCurrentUser() != null) {
             return new AuthenticatedUser(userService.getCurrentUser());
         }
         return null;
     }
 
     public static CTUser getLoggedInUser(HttpSession session) {
         Long userId = (Long) session.getAttribute("CTUserId");
         if (userId != null) {
 /*
             if (userId.longValue() == 5066549580791808L) {
                 CTUser user = getUserById(userId);
                 user.setAdmin(true);
                 UsersAPI.saveUser(user);
             }
 */
             return getUserById(userId);
         }
 
         AuthenticatedUser authenticatedUser = getAuthenticatedUser(session);
         if (authenticatedUser == null) {
             return null;
         }
         // register
         return new CTUser(authenticatedUser);
     }
 
     public static Map<Long, CTUser> getUsersMap(Long[] ids) {
         return ofy().load().type(CTUser.class).ids(ids);
     }
 
     @Override
     @GET
     @Produces({MediaType.APPLICATION_JSON})
     public String getUser(@PathParam("querytype") String querytype,
                           @DefaultValue("0") @QueryParam("limit") int limit,
                           @Context HttpServletRequest request) {
 
         Gson gson = new Gson();
         CTUser user = getLoggedInUser(request.getSession());
         if (user == null) {
             return null;
         }
         if ("me".equalsIgnoreCase(querytype)) {
             user.getNotifications();
             if (user.isAdmin()) {
                 ApplicationSettings appSettings = getApplicationSettings();
                 user.setApplicationSettings(appSettings);
             }
             System.out.println(user.getUserName());
             return gson.toJson(user);
         }
         if ("top".equalsIgnoreCase(querytype)) {
             return gson.toJson(getTopUsers(limit));
         }
         if ("all".equalsIgnoreCase(querytype)) {
             List<UserJS> usersJS = new ArrayList<>();
             for (CTUser userCT : getUsers()) {
                 usersJS.add(new UserJS(userCT, user.getId()));
             }
             return gson.toJson(usersJS);
         }
         return null;
     }
 
     public static ApplicationSettings getApplicationSettings() {
         ApplicationSettings appSettings = ofy().load().type(ApplicationSettings.class).first().get();
         if (appSettings == null) {
             appSettings = new ApplicationSettings();
         }
         return appSettings;
     }
 
     public static List<CTUser> getTopUsers(int limit) {
         return ofy().load().type(CTUser.class).order("-karma").limit(limit).list();
     }
 
     public static List<CTUser> getUsers() {
         return ofy().load().type(CTUser.class).list();
     }
 
     @Override
     @POST
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     public Response setUser(@PathParam("querytype") String querytype,
                             @FormParam("action") String action,
                             @FormParam("fname") String fname,
                             @FormParam("lname") String lname,
                             @FormParam("email") String email,
                             @FormParam("interests") String interests,
                             @FormParam("executeTask") String executeTask,
                             @FormParam("finalizeTask") String finalizeTask,
                             @FormParam("likeTask") String likeTask,
                             @FormParam("likeComment") String likeComment,
                             @FormParam("newTaskInCommunity") String newTaskInCommunity,
                             @FormParam("joinCommunityRequest") String joinCommunityRequest,
                             @FormParam("newComment") String newComment,
                             @FormParam("picUrl") String picUrl,
                             @FormParam("timeout") long timeout,
                             @FormParam("trustRelationships") String trustRelationshipsJSON,
                             @FormParam("symbolicLocation") String symbolicLocation,
                             @FormParam("userId") String userId,
                             @Context HttpServletRequest request) {
 
         CTUser user = getLoggedInUser(request.getSession());
         if (user == null) {
             /*AuthenticatedUser authenticatedUser = getAuthenticatedUser();
             if (authenticatedUser == null) {*/
             return Response.status(Status.UNAUTHORIZED).entity("Not authorized.").type("text/plain").build();
             /*}
             // register
 			user = new CTUser(authenticatedUser);*/
         }
         if ("profile".equalsIgnoreCase(querytype)) {
             if ("".equalsIgnoreCase(fname)) {
                 return Response.status(Status.BAD_REQUEST).entity("Name is required.").type("text/plain").build();
             }
             user.setFirstName(fname);
             user.setLastName(lname);
             user.setEmail(email);
             Gson gson = new Gson();
             user.setInterests((String[]) gson.fromJson(interests, String[].class));
             user.setPicUrl(picUrl);
             user.setLastLogin(new Date());
             saveUser(user);
             HttpSession session = request.getSession();
             // registered user
             if (session.getAttribute("CTUserId") == null) {
                 session.setAttribute("CTUserId", user.getId());
                 EventAPI.logNewAccount(user);
             }
         }
         if ("settings".equalsIgnoreCase(querytype)) {
             NotificationSettings notifications = user.getNotifications();
             notifications.setExecuteTask(stringToBoolean(executeTask));
             notifications.setFinalizeTask(stringToBoolean(finalizeTask));
             notifications.setLikeTask(stringToBoolean(likeTask));
             notifications.setLikeComment(stringToBoolean(likeComment));
             notifications.setNewTaskInCommunity(stringToBoolean(newTaskInCommunity));
             notifications.setNewComment(stringToBoolean(newComment));
             notifications.setJoinCommunityRequest(stringToBoolean(joinCommunityRequest));
             user.setNotifications(notifications);
             saveUser(user);
             if (user.isAdmin()) {
                 // minutes to miliseconds
                 Long newTimeout = timeout * 60000;
                 ApplicationSettings appSettings = getApplicationSettings();
                 appSettings.setChekInTimeOut(newTimeout);
                 ofy().save().entity(appSettings).now();
             }
         }
         if ("trust".equalsIgnoreCase(querytype)) {
             user.setTrustRelationships(trustRelationshipsJSON);
             saveUser(user);
         }
         if ("location".equalsIgnoreCase(querytype)) {
             user.setCheckIn(new Date());
             user.setSymbolicLocation(symbolicLocation);
             CollaborativeSpace space = SpaceAPI.getCollaborativeSpace(symbolicLocation);
             Long spaceId = null;
             if (space != null) {
                 spaceId = space.getId();
             }
             user.setSpaceId(spaceId);
             saveUser(user);
             return Response.ok().entity("Got location symbolic: " + symbolicLocation).build();
         }
         return Response.ok().build();
     }
 
     private boolean stringToBoolean(String value) {
        if (value == null) {
            return false;
        }
        return "on".equalsIgnoreCase(value);
     }
 
     public static void saveUser(CTUser user) {
         ofy().save().entity(user).now();
     }
 
     public static Ref<NotificationSettings> saveNotifications(NotificationSettings notifications) {
         return Ref.create(ofy().save().entity(notifications).now());
     }
 }
