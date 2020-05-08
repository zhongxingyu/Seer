 package commands;
 
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.Types.UserType;
 import de.skuzzle.polly.sdk.Types.StringType;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.UserExistsException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 
 public class RegisterCommand extends Command {
 
     public RegisterCommand(MyPolly polly) throws DuplicatedSignatureException {
         super(polly, "register");
         this.createSignature("Gib deinen gewnschten Benutzernamen " +
        		"(am besten me) und dein gewnschtes Passwort ein.", 
         		new UserType(), new StringType());
         this.setHelpText("Befehl um dich bei Polly zu registrieren.");
         this.setUserLevel(UserManager.UNKNOWN);
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
             Signature signature) {
         return true;
     }
     
     
     
     @Override
     protected void executeOnChannel(User executer, String channel,
             Signature signature) {
        this.reply(channel, "Dieser Befehl ist nur im Query ausfhrbar. Zudem solltest " +
        		"du jetzt ein anderes Passwort whlen.");
     }
     
     
     
     @Override
     protected void executeOnQuery(User executer, Signature signature) {
         if (this.getMyPolly().users().isSignedOn(executer)) {
             this.reply(executer, "Du bist bereits angemeldet.");
             return;
         }
         
         if (this.match(signature, 0)) {
             String userName = signature.getStringValue(0);
             String password = signature.getStringValue(1);
             
             try {
                 this.getMyPolly().users().addUser(
                         userName, password, UserManager.REGISTERED);
                 this.reply(executer, "Registrierung erfolgreich. Du kannst dich jetzt " +
                 		"mit \":auth @" + userName + "\" \"" + password + "\" anmelden.");
             } catch (UserExistsException e) {
                 this.reply(executer, "Der Benutzer '" + userName + 
                         "' existiert bereits.");
             } catch (DatabaseException e) {
                 this.reply(executer, "Interner Datenbankfehler!");
                 e.printStackTrace();
             }
         }
     }
 }
