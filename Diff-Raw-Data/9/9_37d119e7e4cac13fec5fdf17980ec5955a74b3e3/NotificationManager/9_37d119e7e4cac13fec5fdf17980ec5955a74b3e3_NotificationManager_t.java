 /**
  * es.upm.dit.gsi.shanks
  * 02/04/2012
  */
 package es.upm.dit.gsi.shanks.notification;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import sim.engine.SimState;
 import sim.engine.Steppable;
 import es.upm.dit.gsi.shanks.ShanksSimulation;
 import es.upm.dit.gsi.shanks.model.event.Event;
 
 /**
  * 
  * @author darofar
  *
  */
 public class NotificationManager implements Steppable {
 
     /**
      * List of notifications saved trough the hole simulation. 
      */
     private static List<Notification> notifications;
     
     /**
      * List of elements defined for the user to be notified automatically when 
      * NotificationManager step its called. 
      */
     private static List<Notifable> notifables;
     
     /**
      * The object simulation needed to get some information for the current state 
      * of the simulation. 
      */
     private static ShanksSimulation sim;
     
     /**
      * Notification Identifier counter. 
      */
     private static int ID_COUNTER;
     
     private static Logger logger = Logger.getLogger(NotificationManager.class.getName());
 
     /**
      * Constructor with predefined  notification list. 
      * 
      * @param lNotifications 
      *          the predefined list of notifications, probably a list from an old simulation.
      * @param lNotifables
      *          the predefined list of notifables elements, probably a list from an old simulation.    
      * @param simulation
      *                  a reference to the simulation object, needed to obtain information 
      *                  for each notification.
      */
     public NotificationManager(List<Notification> lNotifications, List<Notifable> lNotifables,
             ShanksSimulation simulation) {
         NotificationManager.notifications = lNotifications;
         NotificationManager.notifables = lNotifables;
         NotificationManager.sim = simulation;
         NotificationManager.ID_COUNTER = 0;
         logger.info("NotificationManager set with custom initial fields..." +
         		"\nNotifications List: "+lNotifications+"\nNotifable elements"+lNotifables +
         		"\nCurrent simulation: "+simulation+"\nNotification idetifiers counter: "+ID_COUNTER);
     }
 
 
     /**
      * Default constructor. NotificatioManager needs a reference to the simulation to work
      * properly.
      * 
      * @param simulation
      *                  a reference to the simulation object, needed to obtain information 
      *                  for each notification.
      */
     public NotificationManager(ShanksSimulation simulation) {
         NotificationManager.notifications = new ArrayList<Notification>();
         NotificationManager.notifables = new ArrayList<Notifable>();
         NotificationManager.sim = simulation;
         NotificationManager.ID_COUNTER = 0;
         logger.info("NotificationManager set with default initial fields.\nNotification " +
         		"identifiers counter: "+ID_COUNTER);
     }
     
     /**
      * Whenever a new event triggers it will use this method to add the corresponding notification
      * to the notification lists. 
      *   
      * @param e
      *          the event that for the notification. 
      */
     @SuppressWarnings("unchecked")
      public static void addNotification(Event e) {
         NotificationManager.notifications.add(new InteractionNotification(getNotificationID(),
                                         NotificationManager.sim.getSchedule().getSteps(), 
                                         e.getLauncher(), e.getClass().getName(), 
                                         (List<Object>) e.getAffected()));
         logger.fine("New notification added to notifications list. There is currently: " +
                                notifications.get(notifications.size()-1)+" notifications." +
                                 "\n Notification added: "+NotificationManager.notifications
                                 .get(NotificationManager.notifications.size()-1));
     }
     
     /**
      * 
      * @param notifable
      */
     public static void addNotifable(Notifable notifable) {
         NotificationManager.notifables.add(notifable);
         logger.fine("New notifable element added to the notifables list: "+ notifable);
     }
 
     /**
      * Each new notification needs a notification-ID. NotificationManager monitors the number of 
      * notifications and generates a unique identifier for each of them, using this method to obtain it. 
      *   
      * @return
      *          a notification ID corresponding with the number of notifications saved on this simulation.  
      */
     private static String getNotificationID() {
         logger.fine("Notifications identifier counter: "+ID_COUNTER+1);
         return "Notification#" + NotificationManager.ID_COUNTER++;
     }
     
     /**
      * Search for a notification with the given id.
      *   
      * @param id 
      *          the identifier of the wanted notification. 
      * @return 
      *          the notification founded or null if no one can be found.
      */
     public Notification getByID(String id) {
         //TODO maybe SORT_By_ID then make a better array searching algorithm
         for (Notification n: NotificationManager.notifications){
             if(n.getId().equals(id)){
                 logger.fine("...found a match for getByID query. With id: "+id);
                 return n;
             }
         }
         logger.fine("There is no notification that match the given ID: "+id);
         return null;
     }
     
     /**
      * Search for notifications saved on the given step.
      * 
      * @param step 
      *              the simulation step number from which the notifications are asked
      * @return
      *              a List with the notifications that match the given step or null if no one can be found.           
      */
     public List<Notification> getByStep(int step) {
         List<Notification> found = new ArrayList<Notification>();
         for (Notification n: NotificationManager.notifications)
             if (n.getWhen() == step){
                 logger.fine("...found a match for getByStep query. With step: "+step);
                 found.add(n);
             }
         if(found.size()>0)        
             return found;
         return null;
     }
     
     /**
      * Search for notifications originated from the given source object. 
      * 
      * @param source
      *              the object of the simulation that generates the notifications 
      * @return
      */
     public List<Notification> getBySource(Object source) {
         List<Notification> found = new ArrayList<Notification>();
         for (Notification n: NotificationManager.notifications)
             if (n.getSource().equals(source)) {
                 logger.fine("...found a match for getBySource query. With source: "+source);
                 found.add(n);
             }
         if(found.size()>0)        
             return found;
         return null;
     }
     
     /**
      * Search for notifications of interaction type that match the given class name.
      *   
      * @param interaction
      *                  the class that defines the interaction type of desired notifications. 
      * @return
      *          a List with the notifications founded that match the given class name. 
      */
     public List<Notification> getByInteraction(String interaction) {
         List<Notification> found = new ArrayList<Notification>();
         for (Notification n: NotificationManager.notifications)
             if (((InteractionNotification)n).getInteraction().equals(interaction)){ 
                 logger.fine("...found a match for getByInteraction query. With interaction: " +
                             interaction);
                 found.add(n);
             }
         if(found.size()>0)        
             return found;
         return null;
     }
     
     
     /**
      * Search for notifications of interaction type that match the given target object
      * .
      * @param target
      *              the target object from which notifications are asked.
      * @return
      *          a List with notifications founded that match the given target object. 
      */
     public List<Notification> getByTarget(Object target) {
         List<Notification> found = new ArrayList<Notification>();
         for (Notification n: NotificationManager.notifications) {
             for (Object t : ((InteractionNotification)n).getTarget()) {
                 if (t.equals(target)){ 
                     logger.fine("...found a match for getByTarget query. With target: "+target);
                     found.add(n);
                 }
             }
         }
         if(found.size()>0)        
             return found;
         return null;
     }
     
     /**
      * Search for notifications of ElementValue type that match the given element identifier.
      * @param elementID
      *                  the identifier of the element that was notified. 
      * @return
      *          A list with the notifications saved for the given element identifier.  
      */
     public List<Notification> getByElementID(String elementID) {
 //        if(elementID == null)
 //            return null;
         List<Notification> found = new ArrayList<Notification>();
         for (Notification n: NotificationManager.notifications)
             if (((ValueNotification)n).getElementID().equals(elementID)) {
                 logger.fine("...found a match for getByElementID query. With elementID: "+elementID);
                 found.add(n);
             }
         if(found.size()>0)        
             return found;
         return null;
     }
     
     /**
      * Search for notifications that match the corresponding notifications realization.
      * 
      * @param type
      *              can be InteractionNotification.class or ElementValueNotification.class  
      * @return
      *          a List with all notifications from the selected type. 
      */
     @SuppressWarnings("unchecked")
     public ArrayList<Notification> getByType(Class<?> type) {
         List<?> byType = this.getByType();
         if (type.isAssignableFrom(InteractionNotification.class))
             return (ArrayList<Notification>) byType.get(0);
         return (ArrayList<Notification>) byType.get(1);
     }
     
     private List<?> getByType() {
         ArrayList <Notification> inList = new ArrayList<Notification>();
         ArrayList <Notification> evnList = new ArrayList<Notification>();
         for (Notification n: NotificationManager.notifications) {
             if(n.getClass().isAssignableFrom(InteractionNotification.class))
                 inList.add(n);
             else if (n.getClass().isAssignableFrom(ValueNotification.class))
                 evnList.add(n);
             else {
                 logger.warning("There is a notification from non-suppported type or the " +
                 		"notification type is missing .\nNotification: "+n);
             }
         }
         ArrayList<ArrayList<Notification>> byType = new ArrayList<ArrayList<Notification>>();
         byType.add(inList);
         byType.add(evnList);
         return byType;
     }
 
     /**
      * When the setp of NotificationManager its called generates a notification for each notifable 
      * that has been added to it. 
      */
     @Override
     public void step(SimState arg0) {
         logger.info("Notifying each element of the notifable element list...");
         for(Notifable n: NotificationManager.notifables){
             NotificationManager.addNotification(n);
         }
     }
     
     /**
      * Add a new notification of the ElementValueNotification type. The ones that are defined
      * by the user. 
      * 
      * @param n
      *          the notifable element to be recorded as a notification. 
      */
     private static void addNotification(Notifable n) {
         NotificationManager.notifications.add(new ValueNotification(
                 getNotificationID(),
                 NotificationManager.sim.getSchedule().getSteps(), 
                 n.getSource(), 
                 n.getID(), 
                 n.getElementValue()));
         logger.fine("Notification added. Element: "+ n.getID()+". Value ="+n.getElementValue());
     }
     
     private static final long serialVersionUID = 1528691206423084650L;
     
 }
