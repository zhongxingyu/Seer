 package TokenGenerator;
 import java.util.*;
 import javax.microedition.midlet.*;
 import javax.microedition.lcdui.*;
 
 /**
  * @author bruj0
  */
 public class Main extends MIDlet implements CommandListener {
 
     private boolean midletPaused = false;
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
     private Command CancelCommand;
     private Command OKCommand;
     private Command exitCommand;
     private Command itemCommand;
     private Form form;
     private StringItem Token_stringItem;
     private Form form1;
     private TextField SecretTextfield;
     //</editor-fold>//GEN-END:|fields|0|
     private Timer timer;
     private MyTimerTask timerTaskOne;
     private TokenGen TokenGen;
     private String TokenKey="Test";
     private StockDB db = null;
 
     /**
      * The HelloMIDlet constructor.
      */
     public Main() {
 
     }
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
     //</editor-fold>//GEN-END:|methods|0|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Method: initialize ">//GEN-BEGIN:|0-initialize|0|0-preInitialize
     /**
      * Initilizes the application.
      * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
      */
     private void initialize() {//GEN-END:|0-initialize|0|0-preInitialize
         // write pre-initialize user code here
 
        timer = new Timer();
        timerTaskOne = new MyTimerTask();
        timer.schedule(timerTaskOne, 0, 1000);
         db = new StockDB("TokenKey");
         TokenKey = db.readRecord(1);
         TokenGen = new TokenGen(TokenKey);
 //GEN-LINE:|0-initialize|1|0-postInitialize
         // write post-initialize user code here
     }//GEN-BEGIN:|0-initialize|2|
     //</editor-fold>//GEN-END:|0-initialize|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Method: startMIDlet ">//GEN-BEGIN:|3-startMIDlet|0|3-preAction
     /**
      * Performs an action assigned to the Mobile Device - MIDlet Started point.
      */
     public void startMIDlet() {//GEN-END:|3-startMIDlet|0|3-preAction
         // write pre-action user code here
         switchDisplayable(null, getForm());//GEN-LINE:|3-startMIDlet|1|3-postAction
         // write post-action user code here
     }//GEN-BEGIN:|3-startMIDlet|2|
     //</editor-fold>//GEN-END:|3-startMIDlet|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
     /**
      * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
      */
     public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
         // write pre-action user code here
 //GEN-LINE:|4-resumeMIDlet|1|4-postAction
         // write post-action user code here
     }//GEN-BEGIN:|4-resumeMIDlet|2|
     //</editor-fold>//GEN-END:|4-resumeMIDlet|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Method: switchDisplayable ">//GEN-BEGIN:|5-switchDisplayable|0|5-preSwitch
     /**
      * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
      * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
      * @param nextDisplayable the Displayable to be set
      */
     public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-END:|5-switchDisplayable|0|5-preSwitch
         // write pre-switch user code here
         Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
         if (alert == null) {
             display.setCurrent(nextDisplayable);
         } else {
             display.setCurrent(alert, nextDisplayable);
         }//GEN-END:|5-switchDisplayable|1|5-postSwitch
         // write post-switch user code here
     }//GEN-BEGIN:|5-switchDisplayable|2|
     //</editor-fold>//GEN-END:|5-switchDisplayable|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
     /**
      * Called by a system to indicated that a command has been invoked on a particular displayable.
      * @param command the Command that was invoked
      * @param displayable the Displayable where the command was invoked
      */
     public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
         // write pre-action user code here
         if (displayable == form) {//GEN-BEGIN:|7-commandAction|1|19-preAction
             if (command == exitCommand) {//GEN-END:|7-commandAction|1|19-preAction
                 // write pre-action user code here
                 exitMIDlet();//GEN-LINE:|7-commandAction|2|19-postAction
                 // write post-action user code here
             } else if (command == itemCommand) {//GEN-LINE:|7-commandAction|3|60-preAction
                 // write pre-action user code here
                 switchDisplayable(null, getForm1());//GEN-LINE:|7-commandAction|4|60-postAction
                 // write post-action user code here
             }//GEN-BEGIN:|7-commandAction|5|69-preAction
         } else if (displayable == form1) {
             if (command == CancelCommand) {//GEN-END:|7-commandAction|5|69-preAction
                 // write pre-action user code here
 
                 switchDisplayable(null, getForm());//GEN-LINE:|7-commandAction|6|69-postAction
                 // write post-action user code here
             } else if (command == OKCommand) {//GEN-LINE:|7-commandAction|7|66-preAction
                 // write pre-action user code here
                 TokenKey = SecretTextfield.getString();
                 /*if(TokenKey.length() > 0)
                 {
                     String tmp="newToken="+TokenKey;
                     form1.append(tmp);
                 }*/
                 TokenGen.setKey(TokenKey);
                 db.saveRecord(TokenKey);
                 switchDisplayable(null, getForm());//GEN-LINE:|7-commandAction|8|66-postAction
                 // write post-action user code here
             }//GEN-BEGIN:|7-commandAction|9|7-postCommandAction
         }//GEN-END:|7-commandAction|9|7-postCommandAction
         // write post-action user code here
     }//GEN-BEGIN:|7-commandAction|10|
     //</editor-fold>//GEN-END:|7-commandAction|10|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|18-getter|0|18-preInit
     /**
      * Returns an initiliazed instance of exitCommand component.
      * @return the initialized component instance
      */
     public Command getExitCommand() {
         if (exitCommand == null) {//GEN-END:|18-getter|0|18-preInit
             // write pre-init user code here
             exitCommand = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|18-getter|1|18-postInit
             // write post-init user code here
         }//GEN-BEGIN:|18-getter|2|
         return exitCommand;
     }
     //</editor-fold>//GEN-END:|18-getter|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Getter: form ">//GEN-BEGIN:|14-getter|0|14-preInit
     /**
      * Returns an initiliazed instance of form component.
      * @return the initialized component instance
      */
     public Form getForm() {
         if (form == null) {//GEN-END:|14-getter|0|14-preInit
             // write pre-init user code here
 
             form = new Form("Google Auth Generator", new Item[] { getToken_stringItem() });//GEN-BEGIN:|14-getter|1|14-postInit
             form.addCommand(getExitCommand());
             form.addCommand(getItemCommand());
             form.setCommandListener(this);//GEN-END:|14-getter|1|14-postInit
             // write post-init user code here
         }//GEN-BEGIN:|14-getter|2|
         return form;
     }
     //</editor-fold>//GEN-END:|14-getter|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Getter: Token_stringItem ">//GEN-BEGIN:|34-getter|0|34-preInit
     /**
      * Returns an initiliazed instance of Token_stringItem component.
      * @return the initialized component instance
      */
     public StringItem getToken_stringItem() {
         if (Token_stringItem == null) {//GEN-END:|34-getter|0|34-preInit
             // write pre-init user code here
             Token_stringItem = new StringItem("Token:", "");//GEN-BEGIN:|34-getter|1|34-postInit
             Token_stringItem.setLayout(StringItem.LAYOUT_CENTER | StringItem.LAYOUT_VCENTER);//GEN-END:|34-getter|1|34-postInit
             // write post-init user code here
         }//GEN-BEGIN:|34-getter|2|
         return Token_stringItem;
     }
     //</editor-fold>//GEN-END:|34-getter|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Getter: itemCommand ">//GEN-BEGIN:|59-getter|0|59-preInit
     /**
      * Returns an initiliazed instance of itemCommand component.
      * @return the initialized component instance
      */
     public Command getItemCommand() {
         if (itemCommand == null) {//GEN-END:|59-getter|0|59-preInit
             // write pre-init user code here
             itemCommand = new Command("New Key", "New Key", Command.ITEM, 0);//GEN-LINE:|59-getter|1|59-postInit
             // write post-init user code here
         }//GEN-BEGIN:|59-getter|2|
         return itemCommand;
     }
     //</editor-fold>//GEN-END:|59-getter|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Getter: OKCommand ">//GEN-BEGIN:|65-getter|0|65-preInit
     /**
      * Returns an initiliazed instance of OKCommand component.
      * @return the initialized component instance
      */
     public Command getOKCommand() {
         if (OKCommand == null) {//GEN-END:|65-getter|0|65-preInit
             // write pre-init user code here
             OKCommand = new Command("OK", Command.ITEM, 0);//GEN-LINE:|65-getter|1|65-postInit
             // write post-init user code here
         }//GEN-BEGIN:|65-getter|2|
         return OKCommand;
     }
     //</editor-fold>//GEN-END:|65-getter|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Getter: CancelCommand ">//GEN-BEGIN:|68-getter|0|68-preInit
     /**
      * Returns an initiliazed instance of CancelCommand component.
      * @return the initialized component instance
      */
     public Command getCancelCommand() {
         if (CancelCommand == null) {//GEN-END:|68-getter|0|68-preInit
             // write pre-init user code here
             CancelCommand = new Command("Cancel", Command.ITEM, 0);//GEN-LINE:|68-getter|1|68-postInit
             // write post-init user code here
         }//GEN-BEGIN:|68-getter|2|
         return CancelCommand;
     }
     //</editor-fold>//GEN-END:|68-getter|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Getter: form1 ">//GEN-BEGIN:|61-getter|0|61-preInit
     /**
      * Returns an initiliazed instance of form1 component.
      * @return the initialized component instance
      */
     public Form getForm1() {
         if (form1 == null) {//GEN-END:|61-getter|0|61-preInit
             // write pre-init user code here
             form1 = new Form("New Key", new Item[] { getSecretTextfield() });//GEN-BEGIN:|61-getter|1|61-postInit
             form1.addCommand(getOKCommand());
             form1.addCommand(getCancelCommand());
             form1.setCommandListener(this);//GEN-END:|61-getter|1|61-postInit
             // write post-init user code here
         }//GEN-BEGIN:|61-getter|2|
         return form1;
     }
     //</editor-fold>//GEN-END:|61-getter|2|
 
     //<editor-fold defaultstate="collapsed" desc=" Generated Getter: SecretTextfield ">//GEN-BEGIN:|71-getter|0|71-preInit
     /**
      * Returns an initiliazed instance of SecretTextfield component.
      * @return the initialized component instance
      */
     public TextField getSecretTextfield() {
         if (SecretTextfield == null) {//GEN-END:|71-getter|0|71-preInit
             // write pre-init user code here
             SecretTextfield = new TextField("New Secret Key:", null, 32, TextField.ANY);//GEN-LINE:|71-getter|1|71-postInit
             // write post-init user code here
         }//GEN-BEGIN:|71-getter|2|
         return SecretTextfield;
     }
     //</editor-fold>//GEN-END:|71-getter|2|
 
     /**
      * Returns a display instance.
      * @return the display instance.
      */
     public Display getDisplay () {
         return Display.getDisplay(this);
     }
 
     /**
      * Exits MIDlet.
      */
     public void exitMIDlet() {
         switchDisplayable (null, null);
         destroyApp(true);
         notifyDestroyed();
     }
 
     /**
      * Called when MIDlet is started.
      * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
      */
     public void startApp() {
         if (midletPaused) {
             resumeMIDlet ();
         } else {
             initialize ();
             startMIDlet ();
         }
         midletPaused = false;
     }
 
     /**
      * Called when MIDlet is paused.
      */
     public void pauseApp() {
         midletPaused = true;
     }
 
     /**
      * Called to signal the MIDlet to terminate.
      * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
      */
     public void destroyApp(boolean unconditional) {
         try {
             db.close();
         } catch(Exception e) {}
     }
 
     private class MyTimerTask extends TimerTask{
         public final void run(){
             Token_stringItem.setText(TokenGen.GenToken());
         }
     }
 }
