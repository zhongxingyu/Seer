 package polly.eventhandler;
 
 import java.util.concurrent.ExecutorService;
 
 import org.apache.log4j.Logger;
 
 import de.skuzzle.polly.sdk.CommandManager;
 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.eventlistener.MessageEvent;
 import de.skuzzle.polly.sdk.eventlistener.MessageListener;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 import polly.core.users.UserManagerImpl;
 
 
 
 
 public class MessageHandler implements MessageListener {
 
 	private static Logger logger = Logger.getLogger(MessageHandler.class.getName());
     private CommandManager commands;
     private UserManagerImpl userManager;
     private ExecutorService executorThreadPool;
     
     public MessageHandler(CommandManager commandManager, UserManagerImpl userManager, 
             ExecutorService executorThreadPool) {
         this.commands = commandManager;
         this.userManager = userManager;
         this.executorThreadPool = executorThreadPool;
     }
     
 
     
     @Override
     public void publicMessage(MessageEvent e) {
         this.execute(e, false);
     }
 
     
     
     @Override
     public void privateMessage(MessageEvent e) {
         this.execute(e, true);
     }
 
     
     
     @Override
     public void actionMessage(MessageEvent e) {}
     
     
     
     private void execute(final MessageEvent e, final boolean isQuery) {
         final User executor = this.getUser(e.getUser());
 
         Runnable command = new Runnable() {
             @Override
             public void run() {
                 try {
                     MessageHandler.this.commands.executeString(e.getMessage(), e
                         .getChannel(), isQuery, executor, e.getSource());
                 } catch (InsufficientRightsException e1) {
                     e.getSource().sendMessage(e.getChannel(), "Du kannst den Befehl '" + 
                             e1.getCommand().getCommandName() + "' nicht ausführen.");
                 } catch (Exception e1) {
                     logger.error("Exception while executing command: " + 
                             e1.getMessage(), e1);
                     e.getSource().sendMessage(e.getChannel(), 
                             "Interner Fehler beim Ausführen des Befehls.");
                 }
             }
         };
         
         this.executorThreadPool.execute(command);
     }
     
     
     
     private User getUser(IrcUser user) {
         User u = this.userManager.getUser(user);
         if (u == null) {
             u = new polly.data.User("~UNKNOWN", "blabla", 0);
             u.setCurrentNickName(user.getNickName());
         }
         return u;
     }
 }
