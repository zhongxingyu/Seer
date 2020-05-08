 package controllers;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import models.Discussion;
 import models.Message;
 import models.User;
 import models.User2Discussion;
 import models.ApiAppUser;
 
 import controllers.ApiUserREST;
 
 
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.JsonNode;
 
 import play.libs.Json;
 import play.mvc.Result;
 
 public class User2DiscussionREST extends ItemREST {
 
 	public static Result all() {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		List<Discussion> discussions = user2disc.all();
 		if (discussions.size() == 0) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		} else {
 			// Return the response
 			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
 		}
 	}
 	
 	public static Result getDiscussions() {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		List<Discussion> discussions = user2disc.getDiscussions(0, 10);
 		if (discussions.size() == 0) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		} else {
 			// Return the response
 			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
 		}
 	}
 	
 	
 	public static Result getRangeDiscussions(int from, int to) {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		List<Discussion> discussions = user2disc.getDiscussions(from, to);
 		if (discussions.size() == 0) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		} else {
 			// Return the response
 			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
 		}
 	}
 
 
 	public static Result getUnreadDiscussions(){
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		List<Discussion> discussions = user2disc.unread();
 		if (discussions.size() == 0) {
			return ok(Constants.DISCUSSIONS_EMPTY.toString());
 		} else {
 			// Return the response
 			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
 		}
 	}
 
 
 	public static Result getDiscussion(String discussionId) {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		Discussion discussion = user2disc.findDiscussionById(discussionId);
 		if (discussion == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		// Return the response
 		return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
 	}
 
 
 	public static Result getMessage(String messageId) {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		String discussionId = json.findPath("discussion_id").getTextValue(); 
 		Discussion discussion = user2disc.findDiscussionById(discussionId);
 		if (discussion == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		Message message = discussion.findMessageById(messageId);
 		if (message == null){
 			return notFound(Constants.MESSAGES_EMPTY.toString());
 		}
 
 		// Return a copy of the message
 		return ok(Json.toJson(Message.messageToObjectNode(message)));
 	}
 
 	
 	public static Result updateMessage(String messageId) {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		String discussionId = json.findPath("discussion_id").getTextValue(); 
 		Discussion discussion = user2disc.findDiscussionById(discussionId);
 		if (discussion == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		Message message = discussion.findMessageById(messageId);
 		if (message == null){
 			return notFound(Constants.MESSAGES_EMPTY.toString());
 		}
 
 		message.message = json.findPath("message").getTextValue();
 		message.save();
 
 		// Create a list of all the users except me
 		List <ObjectId> otherUsers = discussion.userIds;
 		otherUsers.remove(user.id);
 
 		// Set the other users discusion to UNREAD
 		setUsersDiscussionToUnread(discussion, otherUsers);
 
 		// Return a copy of the message
 		return ok(Json.toJson(Message.messageToObjectNode(message)));
 	}
 
 	public static Result addDiscussion() {
 		final User user = Application.getLocalUser(session());
 
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 
 		Discussion discussion = null;
 		Message message = null;
 		// List to save them if everything went ok
 		List<User2Discussion> user2discList = new ArrayList<User2Discussion>(); 
 
 		discussion = new Discussion();		// Create discussion
 		discussion.userIds = new ArrayList<ObjectId>();
 		discussion.subject = json.findPath("subject").getTextValue();
 		discussion.save();
 
 		try {
 
 			message = new Message();		// Create message
 			message.message = json.findPath("message").getTextValue();
 			message.writerId = user.id;
 			message.save();
 
 			discussion.firstMessage = message.id; // This is the discussions first message
 			discussion.save();
 
 			// Add discussion to all readers
 			Iterator<JsonNode> userIds = json.findPath("users").getElements();
 
 			while(userIds.hasNext()){
 				String userId = userIds.next().toString().replace("\"", "");
 
 				User receiver = User.findById(userId, User.class);
 
 				if (receiver != null){
 					User2Discussion user2disc = User2Discussion.findById(receiver.id, User2Discussion.class);
 
 					// If is users first discussion, create new User2Discussion
 					if (user2disc == null){
 						user2disc = new User2Discussion();
 						/****************************************/
 						user2disc.id = new ObjectId(receiver.id.toString()); // IMPORTANT
 						/****************************************/
 						user2disc.save();
 					}
 
 					if (user2disc.discussionIds == null){
 						user2disc.discussionIds = new ArrayList<ObjectId>();
 					}
 
 					if (user2disc.unread == null){
 						user2disc.unread = new ArrayList<ObjectId>();
 					}
 
 					discussion.addUser(receiver); //  Add user to discussion
 					user2disc.addDiscussion(discussion); // Add discussion to this user
 					user2disc.setRead(discussion, false);
 					user2discList.add(user2disc);
 				}
 			}
 
 			// Add discussion to sender
 			User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 
 			// If is users first discussion, create new User2Discussion
 			if (user2disc == null){
 				user2disc = new User2Discussion();
 				/****************************************/
 				user2disc.id = new ObjectId(user.id.toString()); // IMPORTANT
 				/****************************************/
 				user2disc.save();
 			}
 
 			if (user2disc.discussionIds == null){
 				user2disc.discussionIds = new ArrayList<ObjectId>();
 			}
 
 			if (user2disc.unread == null){
 				user2disc.unread = new ArrayList<ObjectId>();
 			}
 
 			discussion.addUser(user); // Add user to discussion
 			user2disc.addDiscussion(discussion); // Add discussion to user
 			user2disc.setRead(discussion, true);
 			user2discList.add(user2disc);
 
 		} catch (Exception e) {
 
 			discussion.delete();
 
 			// TODO REMOVE DISCUSSION, MESSAGE AND USER2DISCUSSIONS
 
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		message.save(); // Save Message
 		discussion.save(); // Save discussion
 
 		for(final User2Discussion user2disc : user2discList)
 			user2disc.save(); // Save all user2discussions
 
 		//Return a copy of the discussion
 		return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
 	}
 
 
 	public static Result deleteDiscussion(String id){
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null || !user2disc.discussionIds.contains(new ObjectId(id))){
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		Discussion discussion = user2disc.findDiscussionById(id);
 		if (discussion == null || !discussion.userIds.contains(user.id)){
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 
 		user2disc.removeDiscussion(discussion);
 		user2disc.save();
 
 		discussion.removeUser(user);
 		discussion.save();
 
 		if (discussion.userIds.isEmpty()){
 
 			for(ObjectId oid : discussion.repliesIds){
 				Message message = discussion.findMessageById(oid);
 				message.delete();
 			}
 
 			discussion.delete();
 		}
 		return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
 	}
 
 
 	public static Result reply() {
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		String discussionId = json.findPath("discussion_id").getTextValue(); 
 		Discussion discussion = user2disc.findDiscussionById(discussionId);
 		if (discussion == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 
 		Message message = new Message();
 		message.message = json.findPath("message").getTextValue();
 		message.writerId = user.id;
 		message.save();
 
 		discussion.addMessage(message);
 		discussion.save(); // Save discussion
 
 		// Create a list of all the users except me
 		List <ObjectId> otherUsers = discussion.userIds;
 		otherUsers.remove(user.id);
 
 		setUsersDiscussionToUnread(discussion, otherUsers);
 
 		// Return a copy of the discussion
 		return ok(Json.toJson(Message.messageToObjectNode(message)));
 	}
 
 
 	public static Result replyToMessage(String id){
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		String discussionId = json.findPath("discussion_id").getTextValue(); 
 		Discussion discussion = user2disc.findDiscussionById(discussionId);
 		if (discussion == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 
 		// If the message replying to its really from this discussion
 		Message msg = discussion.findMessageById(id);
 		if(msg != null){
 			Message message = new Message();
 			message.message = json.findPath("message").getTextValue();
 			message.replyToMsg = msg.id;
 			message.writerId = user.id;
 			message.save(); // Save Message
 
 			discussion.addMessage(message); // Add message to its discussion
 			discussion.save(); // Save Discussion
 
 			// Create a list of all the users except me
 			List <ObjectId> otherUsers = discussion.userIds;
 			otherUsers.remove(user.id);
 
 			setUsersDiscussionToUnread(discussion, otherUsers);
 
 			// Return a copy of the discussion
 			return ok(Json.toJson(Message.messageToObjectNode(message)));
 		}
 		return notFound(Constants.MESSAGES_EMPTY.toString());
 	}
 
 
 	public static Result deleteMessage(String id){
 		final User user = Application.getLocalUser(session());
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 		JsonNode json = request().body().asJson();
 		if(json == null) {
 			return badRequest(Constants.JSON_EMPTY.toString());
 		}
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		String discussionId = json.findPath("discussion_id").getTextValue(); 
 		Discussion discussion = user2disc.findDiscussionById(discussionId);
 		if (discussion == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		Message message = discussion.findMessageById(id);
 		if (message == null){
 			return notFound(Constants.MESSAGES_EMPTY.toString());
 		}
 		
 		// If someone is trying to delete the first message from a discussion
 		// That cant be done
 		if (message.id.equals(discussion.firstMessage)){
 			return forbidden(Constants.UNAUTHORIZED.toString());
 		}
 		
 		discussion.deleteMessage(message);
 		discussion.save();
 		message.delete();
 
 		return ok(Json.toJson(Message.messageToObjectNode(message)));
 	}
 
 
 	/** Change discussion to unread for this users
 	 * @param discussionId
 	 * @param userIds
 	 */
 	public static void setUsersDiscussionToUnread(Discussion discussion, List<ObjectId> userIds){
 
 		for (ObjectId oid : userIds){
 			User2Discussion user2disc = User2Discussion.findById(oid, User2Discussion.class);
 
 			// If is users first discussion, create new User2Discussion
 			if (user2disc == null){
 				user2disc = new User2Discussion();
 				user2disc.id = oid;
 				user2disc.discussionIds = new ArrayList<ObjectId>();
 				user2disc.unread = new ArrayList<ObjectId>();
 			}
 			user2disc.setRead(discussion, false);
 		}
 	}
 
 	/*  REST API methods */
 
 	public static Result getApiDiscussions(String sessionToken) {
 		ApiAppUser user = ApiUserREST.findByToken(sessionToken);
 		if (user == null){
 			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
 		}
 
 		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
 		if (user2disc == null) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		}
 		List<Discussion> discussions = user2disc.all();
 		if (discussions.size() == 0) {
 			return notFound(Constants.DISCUSSIONS_EMPTY.toString());
 		} else {
 			// Return the response
 			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
 		}
 	}
 }
