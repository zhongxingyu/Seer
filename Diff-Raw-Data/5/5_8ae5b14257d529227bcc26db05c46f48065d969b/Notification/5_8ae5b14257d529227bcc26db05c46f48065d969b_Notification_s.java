 package models;
 
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.db.jpa.JPA;
 import play.db.jpa.Model;
 import utils.Utils;
 
 import javax.persistence.*;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: sheldon
  * Date: 7/6/12
  * Time: 10:36 AM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 @Table(name="notification")
 public class Notification extends Model {
 
     @Required
     @MaxSize(500)
     @Column(name = "message", nullable = false)
     public String message;
 
     @Required
     @ManyToOne
     @JoinColumn(name="recipient_id", nullable = false)
     public User recipient;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "date_created", nullable = true)
     public Date dateCreated;
 
     @Column(name = "goto_url", nullable = true)
     public String gotoUrl;
 
     @Enumerated(EnumType.STRING)
     @Column(name = "notification_type", nullable = false)
     public NotificationType notificationType = null;
     
     public Notification() {
         dateCreated = new Date();
     }
 
     /**
      * create notifications on list change
      * @param notificationType
      * @param toDoList
      * @param initiator
      * @param oldListNameOnRename
      * @return
      */
     public static List<Notification> createOnListAction(NotificationType notificationType,
                                                   ToDoList toDoList, User initiator, String oldListNameOnRename) {
 
         // gets all the subscribers, except the user who initiate the notification action
         List<User> users = null;
         if (NotificationType.ADD_LIST.equals(notificationType)) {
             users = JPA.em()
                     .createQuery("select p.user from Participation p where p.project=:project and p.user<>:user")
                     .setParameter("project", toDoList.project)
                     .setParameter("user", initiator)
                     .getResultList();
         } else {
             users = JPA.em()
                     .createQuery("select u from User u left join u.watchedToDoLists l where l=:toDoList and u<>:user")
                     .setParameter("toDoList", toDoList)
                     .setParameter("user", initiator)
                     .getResultList();
         }
         if (users != null && users.size() > 0) {
             Map<String, String> messageMap = new HashMap<String, String>();
             messageMap.put("s1", initiator.fullName);
             messageMap.put("message", "notification_" + notificationType.name().toLowerCase());
            String message = Utils.mapToJson(messageMap);
             switch (notificationType) {
                 case RENAME_LIST:
                     messageMap.put("s2", oldListNameOnRename);
                     messageMap.put("s3", toDoList.name);
                    message = initiator.fullName + " renamed list \"" + oldListNameOnRename + "\" to \"" + toDoList.name + "\".";
                     break;
                 default:
                     messageMap.put("s2", toDoList.name);
                     messageMap.put("s3", toDoList.project.title);
                     break;
             }
             return createNotifications(notificationType, users, message);
         } else {
             return null;
         }
     }
     /**
      * create notifications on task change
      * @param notificationType
      * @param toDo
      * @param initiator
      * @param oldToDoList
      * @return
      */
     public static List<Notification> createOnTaskAction(NotificationType notificationType,
                                                         ToDo toDo, User initiator, ToDoList oldToDoList) {
 
         // gets all the subscribers, except the user who initiate the notification action
         List<User> users = JPA.em()
                     .createQuery("select u from User u left join u.watchedToDoLists l where l=:toDoList and u<>:user")
                     .setParameter("toDoList", toDo.toDoList)
                     .setParameter("user", initiator)
                     .getResultList();
         
         if (users != null && users.size() > 0) {
             Map<String, String> messageMap = new HashMap<String, String>();
             messageMap.put("s1", initiator.fullName);
             messageMap.put("s2", toDo.title);
             messageMap.put("s3", toDo.toDoList.name);
             messageMap.put("message", "notification_" + notificationType.name().toLowerCase());
             String message = Utils.mapToJson(messageMap);
             return createNotifications(notificationType, users, message);
         } else {
             return null;
         }
     }
 
     /**
      * creates notification on join by invite
      * @param invitee
      * @param inviter
      * @return
      */
     public static List<Notification> createOnJoinByInvite(User invitee, User inviter) {
         NotificationType notificationType = NotificationType.JOIN_BY_INVITE;
         Map<String, String> messageMap = new HashMap<String, String>();
         messageMap.put("s1", invitee.fullName);
         messageMap.put("message", "notification_" + notificationType.name().toLowerCase());
         String message = Utils.mapToJson(messageMap);
         List<User> recipients = new ArrayList<User>();
         recipients.add(inviter);
         return createNotifications(notificationType, recipients, message);
     }
 
     /**
      * creates notification on join by invite
      * @param inviter
      * @param project
      * @return
      */
     public static List<Notification> createOnInvite(User invitee, User inviter, 
                                                     Project project, String activationUrl) {
         NotificationType notificationType = NotificationType.INVITE_IN_PROJECT;
         Map<String, String> messageMap = new HashMap<String, String>();
         messageMap.put("s1", inviter.fullName);
         messageMap.put("s2", project.title);
         messageMap.put("message", "notification_" + notificationType.name().toLowerCase());
         String message = Utils.mapToJson(messageMap);
         List<User> recipients = new ArrayList<User>();
         recipients.add(invitee);
         return createNotifications(notificationType, recipients, message, activationUrl);
     }
 
     /**
      * creates one type of notifications for a list of recipients
      * @param notificationType
      * @param recipients
      * @param message
      * @return
      */
     private static List<Notification> createNotifications(NotificationType notificationType,
                                                           List<User> recipients, String message) {
         return createNotifications(notificationType,recipients, message, null);
     }
 
     /**
      * creates on type of notifications for a list of recipients
      * @param notificationType
      * @param recipients
      * @param message
      * @param gotoUrl
      * @return
      */
     private static List<Notification> createNotifications(NotificationType notificationType, 
                                                           List<User> recipients, String message,
                                                           String gotoUrl) {
         List<Notification> notifications = new ArrayList<Notification> ();
         for (User recipient : recipients) {
             Notification notification = new Notification();
             notification.message = message;
             notification.notificationType = notificationType;
             notification.recipient = recipient;
             notification.gotoUrl = gotoUrl;
             notifications.add(notification);
         }
         return notifications;
     }
 }
