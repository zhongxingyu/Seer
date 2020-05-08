 package fr.univnantes.atal.web.piubella.webservices;
 
 import fr.univnantes.atal.web.piubella.model.Address;
 import fr.univnantes.atal.web.piubella.model.Notification;
 import fr.univnantes.atal.web.piubella.model.NotificationTransport;
 import fr.univnantes.atal.web.piubella.model.User;
 import fr.univnantes.atal.web.piubella.persistence.PMF;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Webservice to CRUD notifications.
  *
  * Workflow is GET as usual and PUT to POST/PUT/DELETE. This is easier to handle
  * for the datastore.
  */
 public class NotificationService extends AuthWebService {
 
     @Override
     public void init() {
         allowed_methods =
                 new ArrayList(Arrays.asList(false, false, false, false));
         authenticated_methods =
                 new ArrayList(Arrays.asList(true, false, true, false));
     }
 
     @Override
     protected void auth_get(
             HttpServletRequest request,
             HttpServletResponse response,
             User user)
             throws ServletException, IOException {
         Map<String, Object> data = new HashMap<>();
         List<Map<String, Object>> notificationsData = new ArrayList<>();
 
         PersistenceManager pm = PMF.get().getPersistenceManager();
         try {
             User userManaged = (User) pm.getObjectById(
                     User.class,
                     user.getGoogleId());
             Set<Notification> notifications = userManaged.getNotifications();
             int i = 0;
             for (Notification notification : notifications) {
                 Map<String, Object> notificationData = new HashMap<>();
                 notificationData.put("street",
                         notification.getAddress().getStreet());
                 notificationData.put("blue",
                         notification.getNotificationsOnBlueDay());
                 notificationData.put("yellow",
                         notification.getNotificationsOnYellowDay());
                 notificationData.put("id", i);
                 notificationsData.add(notificationData);
                 i++;
             }
         } finally {
             pm.close();
         }
         data.put("status", "success");
         data.put("data", notificationsData);
 
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         try (PrintWriter out = response.getWriter()) {
             out.println(mapper.writeValueAsString(data));
         }
     }
 
     @Override
     protected void auth_put(
             HttpServletRequest request,
             HttpServletResponse response,
             User user)
             throws ServletException, IOException {
         String json = request.getParameter("json");
 
 
 
 
         if (json != null) {
             List<Map<String, Object>> array = mapper.readValue(json, List.class);
             if (array != null) {
                 PersistenceManager pm = PMF.get().getPersistenceManager();
                 try {
                     User userManaged = pm.getObjectById(User.class,
                             user.getGoogleId());
                     userManaged.removeNotifications();
                     for (Map<String, Object> data : array) {
                         List<String> yellowRaw, blueRaw;
                         Address address = null;
                         Set<NotificationTransport> yellow = new HashSet<>(),
                                 blue = new HashSet<>();
                         if (data
                                 != null) {
                             if (data.containsKey("street")) {
                                 String street = (String) data.get("street");
                                street = street.replaceAll("'","\'");
                                 Query q = pm.newQuery(Address.class);
                                q.setFilter("street == \"" + street + "\"");
                                 try {
                                     List<Address> results =
                                             (List<Address>) q.execute();
                                     if (!results.isEmpty()) {
                                         address = results.get(0);
                                     }
                                 } finally {
                                     q.closeAll();
                                 }
                             }
                             if (address == null) {
                                 error(request,
                                         response,
                                         "The street field must be present and set "
                                         + "to a valid street contained in the NOD "
                                         + "dataset.",
                                         422);
                                 return;
 
                             }
                             if (data.containsKey("yellow")) {
                                 yellowRaw = (List<String>) data.get("yellow");
                                 for (String transport : yellowRaw) {
                                     try {
                                         yellow.add(NotificationTransport.valueOf(transport));
                                     } catch (IllegalArgumentException e) {
                                         error(request,
                                                 response,
                                                 "The yellow field isn't correct. Should be "
                                                 + "of the form [\"XMPP\", \"EMAIL\"].",
                                                 422);
                                         return;
                                     }
                                 }
                                 if (data.containsKey("blue")) {
                                     blueRaw = (List<String>) data.get("blue");
                                     for (String transport : blueRaw) {
                                         try {
                                             blue.add(NotificationTransport.valueOf(transport));
                                         } catch (IllegalArgumentException e) {
                                             error(request,
                                                     response,
                                                     "The blue field isn't correct. Should be "
                                                     + "of the form [\"XMPP\", \"EMAIL\"].",
                                                     422);
                                             return;
                                         }
                                     }
                                 }
                             }
                             Notification notification = new Notification();
                             notification.setAddress(address);
                             for (NotificationTransport transport : yellow) {
                                 notification.addNotificationOnYellowDay(transport);
                             }
                             for (NotificationTransport transport : blue) {
                                 notification.addNotificationOnBlueDay(transport);
                             }
                             userManaged.addNotification(notification);
                         }
                     }
                 } finally {
                     pm.close();
                 }
             }
         }
     }
 }
