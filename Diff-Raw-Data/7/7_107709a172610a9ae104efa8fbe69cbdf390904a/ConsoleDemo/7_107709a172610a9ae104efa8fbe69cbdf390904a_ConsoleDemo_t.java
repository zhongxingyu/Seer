 package console;
 
 import akka.actor.*;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.terminal.Sizeable;
 import com.vaadin.ui.*;
 import org.vaadin.artur.icepush.ICEPush;
 
 import com.vaadin.Application;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import static akka.camel.CamelServiceManager.*;
 
 public class ConsoleDemo extends Application {
 
     // For handling long-polling/push to browser
     private ICEPush pusher = new ICEPush();
 
     // Track if we have already started Camel service
     static boolean startedCamel = false;
 
     // start the camel service
     // TODO figure out why this is causing exception
     static public void startCamel() {
 
         if (startedCamel == false) {
             startCamelService();
             startedCamel = true;
             System.out.println("Starting Camel service");
         } else {
             System.out.println("Camel service started already");
         }
     }
 
 
     // For displaying our tasks
     Table taskTable = new Table("Task Table");
 
 
     @Override
     public void init() {
 
         startCamel();
 
 
         Window mainWindow = new Window("Icepushaddon Application");// new SplitPanel());
         setMainWindow(mainWindow);
 

 
         VerticalLayout root = new VerticalLayout();
 
         //turn on spacing and margin (configure in css)
         root.setSpacing(true);
         root.setMargin(true);
 
         mainWindow.setContent(root);
 
        // Add the push component - so we can push stuff to it
        mainWindow.addComponent(pusher);

         // Add the topmost component.
         Label title = new Label("WFE console");
         title.setWidth(400, Sizeable.UNITS_PIXELS);
         root.addComponent(title);
 
 
         // Create a sample menu bar
         final MenuBar menubar = new MenuBar();
         root.addComponent(menubar);
         menubar.addItem("Ingest", null, null);
         menubar.addItem("Transcode", null, null);
         menubar.addItem("QC", null, null);
         menubar.addItem("Delta", null, null);
         menubar.addItem("Subtitles", null, null);
         menubar.addItem("Publication", null, null);
 
 
         // Add a button for starting some example background work
         root.addComponent( new Button("Do stuff in the background", new ClickListener() {
             public void buttonClick(ClickEvent event) {
                 getMainWindow() .addComponent( new Label( "Waiting for background process to complete..."));
 
                 // trigger thread when we click it
                 new BackgroundThread().start();
             }
         }));
 
 
         // A wrapper with a caption for the login form
 //        Panel loginPanel = new Panel("Login");
 //        loginPanel.setWidth("250px");
 //
 //        LoginForm login = new LoginForm();
 //        loginPanel.addComponent(login);
 //        root.addComponent(loginPanel);
 
 
         HorizontalLayout transcodeTables = new HorizontalLayout();
         transcodeTables.setSpacing(true);
         transcodeTables.setMargin(true);
         root.addComponent(transcodeTables);
 
         taskTable.setWidth(400, Sizeable.UNITS_PIXELS);
         taskTable.setSizeFull();
 
         // increase from default of 2 to smooth out scrolling
 //        taskTable.setCacheRate(5);
         transcodeTables.addComponent(taskTable);
 
         // Create table headings
         taskTable.addContainerProperty("TaskId", Integer.class, null);
         taskTable.addContainerProperty("Vpid", String.class, null);
         taskTable.addContainerProperty("Bpid", String.class, null);
         taskTable.addContainerProperty("Title", String.class, null);
         taskTable.addContainerProperty("Status", String.class, null);
         taskTable.addContainerProperty("EcmJobId", Integer.class, null);
         taskTable.addContainerProperty("percent", Integer.class, null);
 
         //Add some initial rows
         for (int i = 0; i < 10; i++) {
             System.out.println("Adding item " + i);
             taskTable.addItem(new Object[]{
                     new Integer(i),
                     "v00" + i,
                     "b00" + i,
                     "Neighbours ep: " + i,
                     "InProgress",
                     "123",
                     "50"
             }, new Integer(i));
         }
 
 
         Table targetTable = new Table("Target Table");
         transcodeTables.addComponent(targetTable);
 
         String[] targetFields = {"Target Id", "wfe_profile", "Title"};
         for (String p : targetFields) {
             targetTable.addContainerProperty(p, String.class, null);
         }
         for (int i = 0; i < 10; i++) {
             System.out.println("Adding target item " + i);
             targetTable.addItem(new Object[]{i, "flv_avc1", "Neighbours"}, new Integer(i));
         }
 
 
         // Done with PageLayout
 
         // Lets create an Actor to Monitor for updates via REST
 
         ActorRef actor = Actors.actorOf(new UntypedActorFactory() {
             public UntypedActor create() {
 
                 return new RestConsumerActor() {
                     public void doSomething(Map<String, List<String>> params) {
                         System.out.println("doSomething " + params);
                         List<String> taskIds = params.get("taskId");
 
                         // If we have provided at least 1 taskId
                         if (taskIds != null) {
                             for (String taskId : taskIds) {
                                 Integer tId = Integer.parseInt(taskId);
 
                                 Item row = taskTable.getItem(tId);
 
                                 for (String key : params.keySet()) {
                                     System.out.println("Processing '" + key + "'");
 
                                     if (key.equals("taskId")) {
                                         System.out.println("Skipping " + key);
                                         continue;
                                     }
                                     String value = params.get(key).get(0);
                                     System.out.println("Setting " + key + " to " + value); //+params.get(key).get(0));
 
                                     //Property p = row.getItemProperty(key);
                                     Property p = taskTable.getContainerProperty(tId, key);
                                     if (p != null) {
                                         p.setValue(value);//params.get(key).get(0));
                                     } else {
                                         System.out.println("No field called " + key);
                                     }
                                 }
                             }
                         }
 
                         // push the updates to the view
                         pusher.push();
                     }
                 };
             }
         });
         actor.start();
 
 
     }
 
     public class BackgroundThread extends Thread {
 
         @Override
         public void run() {
 
             for (int i = 0; i < 25; i++) {
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
 
                 }
                 for (Iterator ii = taskTable.getItemIds().iterator(); ii.hasNext();) {
                     
                     // Get the current item identifier, which is an integer.
                     int iid = (Integer) ii.next();
                     Property p = taskTable.getContainerProperty(iid, "percent");
 
                     // increase percent value
                     p.setValue((Integer) p.getValue() + 2);
 
                     // increate the Status if complete
                     if ((Integer) p.getValue() == 100) {
                         taskTable.getContainerProperty(iid, "Status").setValue("Completed");
                     }
 
                     // push changes as we go
                     pusher.push();
                 }
             }
 
 
             // Tell them we are done
             synchronized (ConsoleDemo.this) {
                 getMainWindow().addComponent(new Label("All done"));
             }
 
             // Push the changes
             pusher.push();
 
         }
 
     }
 
 
 }
