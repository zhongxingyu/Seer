 package polly.rx.commands;
 
 import polly.rx.MyPlugin;
 import de.skuzzle.polly.sdk.DelayedCommand;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.User;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.ConstraintException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.time.Milliseconds;
 
 
 public class CrackerCommand extends DelayedCommand {
 
     private final static int CRACKER_DELAY = (int) Milliseconds.fromMinutes(5);
     private final static int CRACKER_INC = 1;
     
     
     
     public CrackerCommand(MyPolly polly) throws DuplicatedSignatureException {
         super(polly, "cracker", CRACKER_DELAY);
         this.createSignature("Gibt polly einen Cracker");
         this.createSignature("Gibt polly einen Cracker und rechnet ihn dem angegebenen " +
         		"User an", 
         		new Parameter("User", Types.USER));
         this.setHelpText("Gibt polly einen Cracker");
         this.setRegisteredOnly();
     }
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel, Signature signature)
             throws CommandException, InsufficientRightsException {
         
         try {
             
             User user = executer;
             if (this.match(signature, 1)) {
                 final String name = signature.getStringValue(0);
                 user = this.getMyPolly().users().getUser(name);
                 
                 if (user == null) {
                     throw new CommandException("Unbekannter Benutzer: " + name);
                 }
             }
             int crackers = this.incCracker(user, CRACKER_INC);
            this.reply(channel, "Danke f�r den Cracker! Das war der " + crackers + 
                    ". Cracker von " + executer.getName());
         } catch (DatabaseException e) {
            throw new CommandException(e); 
         }
         
         return false;
     }
     
     
     
     private int incCracker(User user, int amount) throws DatabaseException {
         int crackers = Integer.parseInt(user.getAttribute(MyPlugin.CRACKER));
         crackers += amount;
         try {
             this.getMyPolly().users().setAttributeFor(user, MyPlugin.CRACKER, 
                 Integer.toString(crackers));
         } catch (ConstraintException e) {
             throw new RuntimeException("this was thought to be impossibru to happen", e);
         }
         return crackers;
     }
 
 }
