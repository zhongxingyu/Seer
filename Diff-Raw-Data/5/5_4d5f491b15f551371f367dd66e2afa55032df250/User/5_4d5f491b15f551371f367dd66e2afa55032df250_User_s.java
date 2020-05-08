 package models;
 
 import be.objectify.deadbolt.core.models.Permission;
 import be.objectify.deadbolt.core.models.Role;
 import be.objectify.deadbolt.core.models.Subject;
 import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
 import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
 import com.feth.play.module.pa.user.AuthUser;
 import com.feth.play.module.pa.user.AuthUserIdentity;
 import com.feth.play.module.pa.user.EmailIdentity;
 import com.feth.play.module.pa.user.NameIdentity;
 import com.feth.play.module.pa.user.FirstLastNameIdentity;
 import models.TokenAction.Type;
 
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 import play.data.format.Formats;
 import java.util.*;
 
 import com.google.code.morphia.annotations.Entity;
 import controllers.MorphiaObject;
 import controllers.routes;
 import play.libs.Json;
 
 @JsonIgnoreProperties(ignoreUnknown = true)
 @Entity
 public class User extends Item implements Subject {
 
 	private static final long serialVersionUID = 1L;
 
 	/** ------------------------ Attributes ------------------------- **/
 
 	public String email;
 	public String name;
 	public String firstName;
 	public String lastName;
     public String phone;
     public String address;
     public ObjectId profilePicture;
     public java.util.Map<String, List<ObjectId>> maps;
     public String identifier;
 
 	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
 	public Date lastLogin;
 	public boolean active;
 	public boolean emailValidated;
 	@JsonIgnore
 	public List<Permission> permissions;
 	//@ManyToMany
 	public List<SecurityRole> roles;
 	//@OneToMany(cascade = CascadeType.ALL)
 	public List<LinkedAccount> linkedAccounts;
 
 
 	/** ------------------------ Getters / Setters ------------------------- **/
 
 	@Override
 	public String getIdentifier() {
 		return id.toString();
 	}
 
 	@Override
 	public List<? extends Role> getRoles() {
 		return roles;
 	}
 
 	@Override
 	public List<? extends Permission> getPermissions() {
 		return null;
 	}
 
 
 	/** ------------------------ Authentication methods -------------------------- **/
 
 	public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity) {
 		return findByAuthUserIdentity(identity) != null;
 	}
 
 	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
 		if (identity == null) {
 			return null;
 		}
 		if (identity instanceof UsernamePasswordAuthUser) {
 			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
 		} else {
 			return MorphiaObject.datastore.find(User.class)
 					.field("linkedAccounts.providerUserId").equal(identity.getId())
 					.field("active").equal(true)
 					.field("linkedAccounts.providerKey").equal(identity.getProvider()).get();
 		}
 	}
 
 	public static User findByUsernamePasswordIdentity(final UsernamePasswordAuthUser identity) {
 		return MorphiaObject.datastore.find(User.class)
 				.field("email").equal(identity.getEmail())
 				.field("active").equal(true)
 				.field("linkedAccounts.providerKey").equal(identity.getProvider()).get();
 	}
 
 	public void merge(final User otherUser) {
 		for (final LinkedAccount acc : otherUser.linkedAccounts) {
 			this.linkedAccounts.add(LinkedAccount.create(acc));
 		}
 		// do all other merging stuff here - like resources, etc.
 		// deactivate the merged user that got added to this one
 		otherUser.active = false;
 		MorphiaObject.datastore.save(Arrays.asList(new User[]{otherUser, this}));
 	}
 
 	public static User create(final AuthUser authUser) {
 		final User user = new User();
 		user.roles = Collections.singletonList(
 				SecurityRole.findByRoleName(controllers.Application.USER_ROLE));
 		user.active = true;
 		user.lastLogin = new Date();
 		LinkedAccount la = LinkedAccount.create(authUser);
 		user.linkedAccounts = Collections.singletonList(la);
 
 		if (authUser instanceof EmailIdentity) {
 			final EmailIdentity identity = (EmailIdentity) authUser;
 			// Remember, even when getting them from FB & Co., emails should be verified within
 			// the application as a security breach there might break your security as well!
 			user.email = identity.getEmail();
 			user.emailValidated = false;
 		}
 
 		if (authUser instanceof NameIdentity) {
 			final NameIdentity identity = (NameIdentity) authUser;
 			final String name = identity.getName();
 			if (name != null)
 				user.name = name;
 		}
 
 		if (authUser instanceof FirstLastNameIdentity) {
 			final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
 			final String firstName = identity.getFirstName();
 			final String lastName = identity.getLastName();
 			if (firstName != null) {
 				user.firstName = firstName;
 			}
 			if (lastName != null) {
 				user.lastName = lastName;
 			}
 		}
 		user.save();
 		// Fix - adding the User to the LinkedAccount
 		la.setUserId(user.id.toString());
 		la.save();
 
 		return user;
 	}
 
 	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
 		User.findByAuthUserIdentity(oldUser).merge(
 				User.findByAuthUserIdentity(newUser));
 	}
 
 	public Set<String> getProviders() {
 		final Set<String> providerKeys = new HashSet<String>(
 				linkedAccounts.size());
 		for (final LinkedAccount acc : linkedAccounts) {
 			providerKeys.add(acc.providerKey);
 		}
 		return providerKeys;
 	}
 
 	public static void addLinkedAccount(final AuthUser oldUser,
 			final AuthUser newUser) {
 		final User u = User.findByAuthUserIdentity(oldUser);
 		u.linkedAccounts.add(LinkedAccount.create(newUser));
 		u.save();
 	}
 
 	public static void setLastLoginDate(final AuthUser knownUser) {
 		final User u = User.findByAuthUserIdentity(knownUser);
 		u.lastLogin = new Date();
 		u.save();
 	}
 
 	public static User findByEmail(final String email) {
 		return MorphiaObject.datastore.find(User.class).field("email").equal(email)
 				.field("active").equal(true).get();
 	}
 
 	public LinkedAccount getAccountByProvider(final String providerKey) {
 		return LinkedAccount.findByProviderKey(this, providerKey);
 	}
 
 	public static void verify(final User unverified) {
 		// You might want to wrap this into a transaction
 		unverified.emailValidated = true;
 		unverified.save();
 		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
 	}
 
 	public void changePassword(final UsernamePasswordAuthUser authUser,
 			final boolean create) {
 		LinkedAccount a = this.getAccountByProvider(authUser.getProvider());
 		if (a == null) {
 			if (create) {
 				a = LinkedAccount.create(authUser);
 				a.setUserId(this.id.toString());
 			} else {
 				throw new RuntimeException(
 						"Account not enabled for password usage");
 			}
 		}
 		a.providerUserId = authUser.getHashedPassword();
 		a.save();
 		for (final LinkedAccount acc : this.linkedAccounts)
 			if (UsernamePasswordAuthProvider.PROVIDER_KEY.equals(acc.providerKey))
 				acc.providerUserId = authUser.getHashedPassword();
 		this.save();
 	}
 
 	public void resetPassword(final UsernamePasswordAuthUser authUser,
 			final boolean create) {
 		// You might want to wrap this into a transaction
 		this.changePassword(authUser, create);
 		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
 	}
 
 	/** Parses a message list and prepares it for exporting to JSON
 	 * @param msgs Message list
 	 * @return List of ObjectNodes ready for use in toJson
 	 */
 	public static List<ObjectNode> usersToObjectNodes (List<User> usrs){
 		List<ObjectNode> users = new ArrayList<ObjectNode>();
 		for(User user : usrs){
 			users.add(userToObjectNode(user));
 		}
 		return users;
 	}
 
 	/** Parses a message and prepares it for exporting to JSON
 	 * @param message A message
 	 * @return ObjectNode ready for use in toJson
 	 */
 	public static ObjectNode userToObjectNode (User user){
 		ObjectNode userNode = Json.newObject();
 		// TODO implement to have a XToObjectNode in each model
 		return userNode;
 	}
 
 
 
 
     /** ------------ User model needs special ObjectIds handling ------------- **/
 
     @Override
     public JsonNode toJson() {
         JsonNode json = super.toJson();
         ArrayNode aux = new ArrayNode(JsonNodeFactory.instance);
         for (LinkedAccount la : this.linkedAccounts)
             aux.add(la.toJson());
         ((ObjectNode)json).put("linkedAccounts", aux);
         aux = new ArrayNode(JsonNodeFactory.instance);
         for (SecurityRole sr : this.roles)
             aux.add(sr.toJson());
         ((ObjectNode)json).put("roles", aux);
         ((ObjectNode)json).put("profilePicture", profilePicture != null ? profilePicture.toString() : null);
         ObjectNode mapsNode = Json.newObject();
         if (this.maps != null) {
             for (String key : this.maps.keySet()) {
                 ArrayNode mapsAux = new ArrayNode(JsonNodeFactory.instance);
                 for (ObjectId oid : this.maps.get(key))
                     mapsAux.add(oid.toString());
                 mapsNode.put(key, mapsAux);
             }
         }
         ((ObjectNode)json).put("maps", mapsNode);
         return json;
     }
 
     public static User userFromJson(JsonNode srcJson) {
         JsonNode json = User.fromJson(srcJson);
         JsonNode jtemp = json.findValue("profilePicture");
        if (!jtemp.isNull() && !jtemp.asText().equalsIgnoreCase(""))
             ((ObjectNode)json).putPOJO("profilePicture", new ObjectId(jtemp.asText()));
         else
             ((ObjectNode)json).putNull("profilePicture");
         jtemp = json.findValue("maps");
        if (!jtemp.isNull())  {
             Iterator<String> it = jtemp.getFieldNames();
             ObjectNode mapsNodes = Json.newObject();
             while (it.hasNext()) {
                 String key = it.next();
                 JsonNode mapNode = jtemp.findPath(key);
                 Iterator<JsonNode> itnode = mapNode.getElements();
                 ArrayNode aux = new ArrayNode(JsonNodeFactory.instance);
                 while (itnode.hasNext())
                     aux.addPOJO(new ObjectId(itnode.next().asText()));
 
                 mapsNodes.put(key, aux);
             }
             ((ObjectNode)json).put("maps", mapsNodes);
         } else {
             ((ObjectNode)json).putNull("maps");
         }
         return Json.fromJson(json, User.class);
     }
 
 
 	public static ObjectNode userToShortObjectNode(ObjectId oid){
 		User user = User.findById(oid, User.class);
 		return userToShortObjectNode(user);
 	}
 
 	
 	public static ObjectNode userToShortObjectNode(User user){
 		if (user != null){
 			ObjectNode userNode = Json.newObject();
 			userNode.put("id", user.id.toString());
 			userNode.put("name", user.name);
 			userNode.put("email", user.email);
 			userNode.put("profilePicture", user.profilePicture != null ? routes.PhotosREST.getPhoto(user.profilePicture.toString()).toString() +"/content" : null);
 			return userNode;
 		}
 		return null;
 	}
 
 
 	public static List<ObjectNode> usersToShortObjectNode(List<User> users){
 		List<ObjectNode> userNodes = new ArrayList<ObjectNode>();
 		for(User user : users){
 			ObjectNode userNode = userToShortObjectNode(user);
 			if (userNode != null)
 				userNodes.add(userNode);
 		}
 		return userNodes;
 	}
 
 	
 	public static List<ObjectNode> userIdsToShortObjectNode(List<ObjectId> userIds) {
 		List<ObjectNode> userNodes = new ArrayList<ObjectNode>();
 		for(ObjectId oid : userIds){
 			User user = User.findById(oid, User.class);
 			if (user != null){
 				ObjectNode userNode = userToShortObjectNode(user);
 				if (userNode != null){
 					userNodes.add(userNode);
 				}
 			}
 		}
 		return userNodes;
 	}
 
 }
