 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import models.ChatRoom;
 import models.Message;
 import models.OrganizationGroup;
 import models.User;
 import play.data.validation.Required;
 import play.mvc.*;
 import utils.C;
 import utils.F.Function;
 
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
        Long lastMessage = C.eList(events).last().timestamp;
         render(user, groupId, group, users, privateRooms, events, room, rooms, roomTitle, lastMessage);
     }
 
     public static void say(@Required String groupId, @Required String room, @Required String message) {
         errorValidUser();
         User user = User.findByGroupAndEmail(groupId, session.get(GroupController.USER_KEY));
         ChatRoom.findByGroupAndName(groupId, room).say(user, message);
         ok();
     }
     
     public static void leaveAllRoomsAndDisconnect(@Required String groupId) {
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
         List<ChatRoom> rooms = ChatRoom.findPublicRoomsByGroup(groupId);
         render(rooms);
     }
     
     /***** UGLY stuff assuming perfs are better with it *****/
     
     public static void roomsUpdate(@Required String groupId, @Required String room) {
         StringBuilder builder = new StringBuilder();
         List<ChatRoom> rooms = ChatRoom.findPublicRoomsByGroup(groupId);
         for (ChatRoom r : rooms) {
             if (room.equals(r.name)) {
                 builder.append("<li class=\"active\">");
             } else {
                 builder.append("<li>");
             }
             builder.append("<a href=\"/").append(groupId)
                 .append("/rooms/").append(r.name)
                 .append("\">").append(r.name)
                 .append("</a></li>");
         }
         renderText(builder.toString());
     }
     
     public static void usersUpdate(@Required String groupId, @Required String room) {
         StringBuilder builder = new StringBuilder();
         User user = User.findByGroupAndEmail(groupId, session.get(GroupController.USER_KEY));
         ChatRoom r = ChatRoom.findByGroupAndName(groupId, room);
         List<User> users = r.connectedUsers;
         for (User u : users) {
             if (!u.mail.equals(user.mail)) {
                 builder.append("<div id=\"room\"><a href=\"");
                 builder.append("/").append(groupId).append("/rooms/private/")
                         .append(user.mail).append("/").append(u.mail);
                 builder.append("\">").append(u.username).append("</a></div>");
             }
         }
         renderText(builder.toString());
     }
     
     public static void messagesUpdate(@Required String groupId, @Required String room, @Required Long last) {
         List<Message> events = new ArrayList<Message>();
         final User user = User.findByGroupAndEmail(groupId, session.get(GroupController.USER_KEY));
         events = ChatRoom.findByGroupAndName(groupId, room).archiveSinceExcluded(last);
         if (!events.isEmpty()) {
             Long l = C.eList(events).last().timestamp;
             String messages = C.eList(events).map(new Function<Message, String>() {
                 public String apply(Message message) {
                     StringBuilder builder = new StringBuilder();
                     if (message.type == models.MessageType.HTML) {
                         if (message.user.equals(user.mail)) {
                             builder.append("<div class=\"message you\">");
                         } else {
                             builder.append("<div class=\"message\">");
                         }
                         builder.append("<h2>").append(message.username).append("</h2>");
                         builder.append("<p style=\"text-align: justify\">");
                         builder.append(message.text);
                         builder.append("</p></div>");
                     }
                     if (message.type == models.MessageType.JOIN 
                             || message.type == models.MessageType.LEAVE) {
                         builder.append("<div class=\"message notice\">");
                         builder.append("<h2></h2><p>");
                         if (message.type == models.MessageType.JOIN) {
                             builder.append(message.username).append(" joined the room");
                         } else {
                             builder.append(message.username).append(" left the room");
                         }
                         builder.append("</p></div>");
                     }
                     return builder.toString();
                 }
             }).mkString("");
             renderJSON(new Messages(messages, l));
         } else {
             renderJSON(new Messages("", null));
         }
     }
     
     public static class Messages {
         public String messages;
         public Long last;
         
         public Messages(){}
 
         public Messages(String messages, Long last) {
             this.messages = messages;
             this.last = last;
         }
     }
     
     /***** UGLY stuff assuming perfs are better with it *****/
 
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
