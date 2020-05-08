 package commands;
 
 
 import java.util.Date;
 
 import polly.reminds.MyPlugin;

 import core.RemindManager;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.User;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import entities.RemindEntity;
 
 
 public class ModRemindCommand extends AbstractRemindCommand {
 
     public ModRemindCommand(MyPolly polly, RemindManager manager) 
             throws DuplicatedSignatureException {
         super(polly, manager, "modr");
         this.createSignature("ndert das Datum des Reminds mit der angegebenen ID",
             MyPlugin.MODIFY_REMIND_PERMISSION,
             new Parameter("Remind-Id", Types.NUMBER), 
             new Parameter("Neue Zeit", Types.DATE));
         this.createSignature("ndert die Nachricht des angegebenen Reminds", 
             MyPlugin.MODIFY_REMIND_PERMISSION,
             new Parameter("Remind-Id", Types.NUMBER), 
             new Parameter("Nachricht", Types.STRING));
         this.createSignature("ndert Nachricht und Datum des angegebenen Reminds",
             MyPlugin.MODIFY_REMIND_PERMISSION,
             new Parameter("Remind-Id", Types.NUMBER), 
             new Parameter("Nachricht", Types.STRING), 
             new Parameter("Meue Zeit", Types.DATE));
         
         this.createSignature("ndert das Datum des letzten Reminds", 
             MyPlugin.MODIFY_REMIND_PERMISSION,
            new Parameter("Nachricht", Types.STRING));
         this.createSignature("ndert die Nachricht des letzten Reminds", 
             MyPlugin.MODIFY_REMIND_PERMISSION,
             new Parameter("Nachricht", Types.STRING));
         this.createSignature("ndert Nachricht und Datum des letzten Reminds",
             MyPlugin.MODIFY_REMIND_PERMISSION,
             new Parameter("Nachricht", Types.STRING), 
             new Parameter("Meue Zeit", Types.DATE));
         this.setHelpText("Mit diesem Befehl knnen bestehende Reminds modifiziert " +
         		"werden. Zum rausfinden der ID eines Reminds benutze :myreminds");
         this.setRegisteredOnly();
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
             Signature signature) throws CommandException {
         
         if (signature.getId() >= 3) {
             final RemindEntity re = this.remindManager.getLastRemind(executer);
             
             if (re == null) {
                 throw new CommandException("Kein letztes Remind vorhanden");
             }
             
             Date dueDate = re.getDueDate();
             String msg = re.getMessage();
             
             if (this.match(signature, 3)) {
                 dueDate = signature.getDateValue(0);
             } else if (this.match(signature, 4)) {
                 msg = signature.getStringValue(0);
             } else if (this.match(signature, 5)) {
                 msg = signature.getStringValue(0);
                 dueDate = signature.getDateValue(1);
             }
             
             try {
                 this.remindManager.modifyRemind(executer, re.getId(), dueDate, msg);
                 this.reply(channel, "Remind erfolgreich aktualisiert");
                 return false;
             } catch (DatabaseException e) {
                 throw new CommandException(e);
             }
         }
 
         
         int id = (int) signature.getNumberValue(0);
         RemindEntity remind = this.remindManager.getDatabaseWrapper().getRemind(id);
 
         Date dueDate = remind.getDueDate();
         String message = remind.getMessage();
         
         if (this.match(signature, 0)) {
             dueDate = signature.getDateValue(1);
         } else if (this.match(signature, 1)) {
             message = signature.getStringValue(1);
         } else if (this.match(signature, 2)) {
             message = signature.getStringValue(1);
             dueDate = signature.getDateValue(2);
         }
         
         try {
             this.remindManager.modifyRemind(executer, id, dueDate, message);
             this.reply(channel, "Remind erfolgreich aktualisiert");
         } catch (DatabaseException e) {
             throw new CommandException(e);
         }
         
         return false;
     }
 
 }
