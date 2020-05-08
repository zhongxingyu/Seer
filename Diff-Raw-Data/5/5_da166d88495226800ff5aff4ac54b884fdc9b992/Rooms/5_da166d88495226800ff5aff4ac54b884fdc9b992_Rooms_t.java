 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import models.ChatRoom;
 import models.Message;
 import models.OrganizationGroup;
 import models.User;
 import play.data.validation.Required;
 import play.mvc.*;
 
 public class Rooms extends Controller {
     
     public static final String WELCOME_ROOM = "Home";
     
     public static final String PRIVATE_KEY = "Private-";
     
     public static final String CHOOSE_VALID_USER = "Please choose a valid user.";
     
     private static void errorValidUser() {
         if (!session.contains(GroupController.USER_KEY)) {
             flash.error(CHOOSE_VALID_USER);
             Application.index();
         }
     }
     
     public static void room(@Required String groupId, @Required String room) {
         errorValidUser();
         User user = User.findByGroupAndEmail(groupId, session.get(GroupController.USER_KEY));
         if (!user.group.groupId.equals(groupId)) {
             room(user.group.groupId, WELCOME_ROOM);
         }
         List<Message> events = new ArrayList<Message>();
         List<ChatRoom> rooms = ChatRoom.findPublicRoomsByGroup(groupId);
         List<ChatRoom> privateRooms = ChatRoom
                 .findPrivateRoomsByGroupAndUser(groupId, user);
         ChatRoom r = ChatRoom.findByGroupAndName(groupId, room);
         List<User> users = r.connectedUsers;//User.findByGroupAndConnected(groupId);
         String roomTitle = "";
         events = ChatRoom.findByGroupAndName(groupId, room).archiveSince(
                 Long.valueOf(session.get(GroupController.FROM_KEY)));
         roomTitle = ChatRoom.findByGroupAndName(groupId, room).title;
         OrganizationGroup group = OrganizationGroup.findByGroupId(groupId);
         if (r != null) {
             if (!r.connectedUsers.contains(user)) {
                 r.join(user);
             }
         }
         render(user, groupId, group, users, privateRooms, events, room, rooms, roomTitle);
     }
 
     public static void say(@Required String groupId, @Required String room, @Required String message) {
         errorValidUser();
         User user = User.findByGroupAndEmail(groupId, session.get(GroupController.USER_KEY));
         ChatRoom.findByGroupAndName(groupId, room).say(user, message);
         room(groupId, room);
     }
     
     public static void leaveAllRooms(@Required String groupId) {
         errorValidUser();
         User user = User.findByGroupAndEmail(groupId, session.get(GroupController.USER_KEY));
         for (ChatRoom chat : ChatRoom.findPublicRoomsByGroup(groupId)) {
             if (chat.connectedUsers.contains(user)) {
                 chat.leave(user);
             }
         }
        user = user.merge();
         user = User.disconnect(user);
         session.clear();
         GroupController.index(groupId);
     }
     
     public static void leave(@Required String groupId, String room) {
         errorValidUser();
         User user = User.findByGroupAndEmail(groupId, session.get(GroupController.USER_KEY));
         ChatRoom.findByGroupAndName(groupId, room).leave(user);
         room(groupId, WELCOME_ROOM);
     }
     
     public static void newPrivateRoom(@Required String groupId, @Required String user1, @Required String user2) {
         User u1 = User.findByGroupAndEmail(groupId, user1);
         User u2 = User.findByGroupAndEmail(groupId, user2);
         String value = PRIVATE_KEY + u1.username + "-" + u2.username;
         ChatRoom room = ChatRoom.getOrCreatePrivateRoom(groupId, value, 
                 "Private conversation between " + value, user1, user2);
         room.save();
         room(groupId, room.name);
     }
     
     public static void newChatRoom(@Required String groupId, @Required String name) {
         String value = ChatRoom.DEFAULT_TITLE;
         if (name != null && !name.equals("")) {
             value = name;
         }
         ChatRoom room = ChatRoom.getOrCreateRoom(groupId, value, ChatRoom.DEFAULT_TITLE);
         room.save();
         ok();
     }
     
     public static void rooms(@Required String groupId) {
         List<ChatRoom> rooms = ChatRoom.findAll();
         render(rooms);
     }
 
     public static void setTitle(@Required String groupId, @Required String room, @Required String value) {
         ChatRoom r = ChatRoom.findByGroupAndName(groupId, room);
         if (r != null) {
             r.title = value;
             r.save();
             ok();
         } else {
             error();
         }
     }
 
     public static void getTitle(@Required String groupId, @Required String room) {
         ChatRoom r = ChatRoom.findByGroupAndName(groupId, room);
         if (r != null) {
             renderText(r.title);
         } else {
             error();
         }
     }
 }
