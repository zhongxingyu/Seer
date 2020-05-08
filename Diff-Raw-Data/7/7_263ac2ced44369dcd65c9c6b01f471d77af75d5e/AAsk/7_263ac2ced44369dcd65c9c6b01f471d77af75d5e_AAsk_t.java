 package npcs.actions;
 
 import fap_java.NPC;
 
 public class AAsk implements Action {
     private String message;
     private String yesOption;
     private String noOption;
     private boolean choice;
     private int iterator = 0;
     private Action failAction;
     
     public AAsk(String message, String yes, String no, Action failAction) {
         super();
         this.message = message;
         this.yesOption = yes;
         this.noOption = no;
         choice = true;
         this.failAction = failAction;
     }
 
     public void execute(NPC whoLaunches) {
         if(iterator == 0){
             System.out.println(message);
             iterator++;
             whoLaunches.setIterator(whoLaunches.getIterator()-1);
         }
         else{
             System.out.println("You chose : "+choice);
             if(choice){
                 //Loop
                 if(whoLaunches != null && whoLaunches.getIterator() <= whoLaunches.getActions().size()){
                    this.reinit();
                     whoLaunches.execute();
                 }
             }
             else{
                 //End NPC
                 whoLaunches.setIterator(whoLaunches.getActions().size()+2);
                 failAction.execute(whoLaunches);
             }
         }
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setYesOption(String yesOption) {
         this.yesOption = yesOption;
     }
 
     public String getYesOption() {
         return yesOption;
     }
 
     public void setNoOption(String noOption) {
         this.noOption = noOption;
     }
 
     public String getNoOption() {
         return noOption;
     }
 
     public void setIterator(int iterator) {
         this.iterator = iterator;
     }
 
     public int getIterator() {
         return iterator;
     }
 
     public void setChoice(boolean choice) {
         this.choice = choice;
     }
 
     public boolean isChoice() {
         return choice;
     }
 
     public void reinit() {
         iterator = 0;
         failAction.reinit();
     }
 }
