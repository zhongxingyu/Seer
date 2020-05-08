 package CockRoachCrafter;
 
 import org.powerbot.core.script.job.LoopTask;
 import org.powerbot.core.script.job.Task;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.input.Keyboard;
 import org.powerbot.game.api.methods.input.Mouse;
 
 public class RandomHandler extends LoopTask{
 
     @Override
     public int loop(){
    if(Widgets.get(137,56).getText()!="[Press Enter to Chat]" && Widgets.get(137,56).validate() && Game.getClientState() != 3){
         CockRoachCrafter.profit = "Correcting chat box.";
         Keyboard.sendText(" ",true);
         Task.sleep(150,250);
         Widgets.get(137,4).click(true);
     }
     return(2000);
     }
 
 }
