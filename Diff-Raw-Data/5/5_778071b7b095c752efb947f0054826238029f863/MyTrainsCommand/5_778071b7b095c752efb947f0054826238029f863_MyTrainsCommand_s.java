 package commands;
 
 import core.TrainBillV2;
 import core.TrainManagerV2;
 import polly.core.MyPlugin;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 import entities.TrainEntityV2;
 
 
 public class MyTrainsCommand extends Command {
     
     private TrainManagerV2 trainManager;
 
 
     public MyTrainsCommand(MyPolly polly, TrainManagerV2 trainManager) 
             throws DuplicatedSignatureException {
         super(polly, "mytrains");
         this.createSignature("Listet die offene Capitrain Rechnung fr einen " +
        		"Benutzer auf.", MyPlugin.MYTRAINS_PERMISSION);
         this.createSignature("Listet eine detaillierte Capitrainrechnung fr einen " +
         		"Benutzer auf", 
         		MyPlugin.MYTRAINS_PERMISSION,
     		new Parameter("Details", Types.BOOLEAN));
         this.setHelpText("Listet die offene Capitrain Rechnung fr einen " +
                 "Benutzer auf.");
         this.trainManager = trainManager;
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
         Signature signature) {
         
         if (this.match(signature, 0)) {
             this.printTrains(false, signature.getStringValue(0), 
                 executer.getCurrentNickName(), channel);
         } else if (this.match(signature, 1)) {
             this.printTrains(signature.getBooleanValue(1), signature.getStringValue(0), 
                 executer.getCurrentNickName(), channel);
         }
 
         return false;
     }
     
     
     
     private void printTrains(boolean detailed, String trainerNick, String forUser, 
                 String channel) {
         User trainer = this.trainManager.getTrainer(trainerNick);
         TrainBillV2 b = this.trainManager.getBill(trainer, forUser);        
         if (detailed) {
             channel = forUser; // print detailed bill in query
             for (TrainEntityV2 train : b.getTrains()) {
                 this.reply(channel, train.format(this.getMyPolly().formatting()));
             }
             this.reply(channel, "=========================");
         }
         this.reply(channel, b.toString());
     }
 }
