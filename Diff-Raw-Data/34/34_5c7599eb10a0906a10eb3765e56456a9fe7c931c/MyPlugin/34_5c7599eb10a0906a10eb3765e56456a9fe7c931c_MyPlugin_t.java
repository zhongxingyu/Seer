 package polly.reminds;
 
 
 import org.apache.log4j.Logger;
 
 import commands.ToggleMailCommand;
 import commands.DeleteRemindCommand;
 import commands.LeaveCommand;
 import commands.MailRemindCommand;
 import commands.ModRemindCommand;
 import commands.MyRemindsCommand;
 import commands.OnReturnCommand;
 import commands.RemindCommand;
 import commands.SnoozeCommand;
 
 import core.DeliverRemindHandler;
 import core.RemindManager;
 import core.RemindManagerImpl;
 import core.RemindTraceNickchangeHandler;
 
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.PollyPlugin;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.constraints.Constraints;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.IncompatiblePluginException;
 import de.skuzzle.polly.sdk.model.User;
 import entities.RemindEntity;
 
 
 /**
  * 
  * @author Simon
  * @version 27.07.2011 e1a9f7c
  */
 public class MyPlugin extends PollyPlugin {
 
     public final static String REMIND_FORMAT_NAME = "REMIND_FORMAT";
     public final static String REMIND_FORMAT_VALUE = "@%r%: %m%. (Hinterlassen von: %s% am %ld%)";
     
     public final static String MESSAGE_FORMAT_NAME = "MESSAGE_FORMAT";
     public final static String MESSAGE_FORMAT_VALUE = "@%r%: %m%. (Hinterlassen von: %s% am %ld%)";
     
     public final static String SNOOZE_TIME = "SNOOZE_TIME";
     public final static String SNOOZE_DEFAULT_VALUE = "60000";
     
     public final static String DEFAULT_MSG = "REMIND_DEFAULT_MSG";
     public final static String DEFAULT_MSG_VALUE = "Reminder!";
 	
 	public final static String EMAIL = "EMAIL";
 	public final static String DEFAULT_EMAIL = "none";
 	
 	public final static String LEAVE_AS_MAIL = "LEAVE_AS_MAIL";
 	public final static String DEFAULT_LEAVE_AS_MAIL = "false";
 	
 	public final static String REMIND_TRACK_NICKCHANGE = "REMIND_TRACK_NICKCHANGE";
 	public final static String DEFAULT_REMIND_TRACK_NICKCHANGE = "true";
 	
	public final static String REMIND_DOUBLE_DELIVERY = "REMIND_DOUBLE_DELIVERY";
	public final static String DEFAULT_REMIND_DOUBLE_DELIVERY = "true";
	
 	public final static String REMIND_IDLE_TIME = "REMIND_IDLE_TIME";
     
 	
 	
     private Logger logger;
     private RemindManager remindManager;
     private RemindTraceNickchangeHandler remindNickChangeTracer;
     private DeliverRemindHandler deliverRemindHandler;
 
     
 
     public MyPlugin(MyPolly myPolly) throws IncompatiblePluginException, 
             DuplicatedSignatureException {
         super(myPolly);
         
         this.logger = Logger.getLogger(myPolly.getLoggerName(this.getClass()));
         
         myPolly.persistence().registerEntity(RemindEntity.class);
         
         this.remindManager = new RemindManagerImpl(myPolly);
         
         this.deliverRemindHandler = new DeliverRemindHandler(this.remindManager, 
             myPolly.users());
         this.remindNickChangeTracer = new RemindTraceNickchangeHandler(
                 this.remindManager);
         
         this.addDisposable(this.remindManager);
         
         this.addCommand(new LeaveCommand(myPolly, this.remindManager));
         this.addCommand(new OnReturnCommand(myPolly, this.remindManager));
         this.addCommand(new RemindCommand(myPolly, this.remindManager));
         this.addCommand(new MyRemindsCommand(myPolly, this.remindManager));
         this.addCommand(new DeleteRemindCommand(myPolly, this.remindManager));
         this.addCommand(new SnoozeCommand(myPolly, this.remindManager));
         this.addCommand(new ModRemindCommand(myPolly, this.remindManager));
         this.addCommand(new MailRemindCommand(myPolly, this.remindManager));
         this.addCommand(new ToggleMailCommand(myPolly, this.remindManager));
     }
     
     
     
     @Override
     public void actualDispose() throws DisposingException {
         this.getMyPolly().irc().removeNickChangeListener(this.remindNickChangeTracer);
         this.getMyPolly().irc().removeJoinPartListener(this.deliverRemindHandler);
         this.getMyPolly().irc().removeMessageListener(this.deliverRemindHandler);
         this.getMyPolly().users().removeUserListener(this.deliverRemindHandler);
         super.actualDispose();
     }       
     
     
     
     private void assignListeners() {
         this.getMyPolly().irc().addNickChangeListener(this.remindNickChangeTracer);
         this.getMyPolly().irc().addJoinPartListener(this.deliverRemindHandler);
         this.getMyPolly().irc().addMessageListener(this.deliverRemindHandler);
         this.getMyPolly().users().addUserListener(this.deliverRemindHandler);
     }
 
     
     
     @Override
     public void onLoad() {
         logger.info("Scheduling all reminds...");
         this.remindManager.rescheduleAll();
         
         try {
             UserManager users = this.getMyPolly().users();
             
             users.addAttribute(REMIND_FORMAT_NAME, REMIND_FORMAT_VALUE);
             users.addAttribute(MESSAGE_FORMAT_NAME, MESSAGE_FORMAT_VALUE);
             users.addAttribute(SNOOZE_TIME, SNOOZE_DEFAULT_VALUE, Constraints.INTEGER);
             users.addAttribute(DEFAULT_MSG, DEFAULT_MSG_VALUE);
             users.addAttribute(EMAIL, DEFAULT_EMAIL, Constraints.MAILADDRESS);
             users.addAttribute(LEAVE_AS_MAIL, DEFAULT_LEAVE_AS_MAIL, Constraints.BOOLEAN);
             users.addAttribute(REMIND_IDLE_TIME, "" + User.IDLE_AFTER, 
                 Constraints.INTEGER);
             users.addAttribute(REMIND_TRACK_NICKCHANGE, DEFAULT_REMIND_TRACK_NICKCHANGE, 
                 Constraints.BOOLEAN);
            users.addAttribute(REMIND_DOUBLE_DELIVERY, DEFAULT_REMIND_DOUBLE_DELIVERY, 
                Constraints.BOOLEAN);
         } catch (DatabaseException e) {
             e.printStackTrace();
         }
         
         this.assignListeners();
     }
     
     
     
     @Override
     public void uninstall() {
         try {
             this.getMyPolly().persistence().dropTable("REMINDENTITY");
         } catch (DatabaseException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
