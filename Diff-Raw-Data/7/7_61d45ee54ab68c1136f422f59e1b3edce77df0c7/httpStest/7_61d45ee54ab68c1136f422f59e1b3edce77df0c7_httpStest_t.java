 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package prairiebox;
 
 /**
  *
  * @author benlamb
  */
 
 import java.io.*;
 
 import javax.microedition.io.*;
 import javax.microedition.pki.*;
 import javax.microedition.midlet.MIDlet;
 
 public final class  httpStest {
 
     public String run() {
         String returnval;
         String url;
         url = "https://google.com";
          
     try {
       // Query the server and retrieve the response.
       HttpsConnection hc = (HttpsConnection)Connector.open(url);
       hc.setRequestMethod(HttpsConnection.GET);
       
       SecurityInfo si = hc.getSecurityInfo();
       Certificate c = si.getServerCertificate();
       String subject = c.getSubject();
       
       String s = "Server certificate subject: \n" + subject;
       System.out.println(s);
 
       hc.close();
      returnval = "Ready. Able to make https connections.";
     }
     catch (IOException ioe) {
      System.out.println("HTTPS Exception" + ioe.toString());
      returnval = "Error. Unable to make https connections.";
     }
     return(returnval);
          
       
 
   }   
 }
 
 /*
 Wireless Java 2nd edition 
 Jonathan Knudsen
 Publisher: Apress
 ISBN: 1590590775 
 */
 //
 //import java.io.*;
 //
 //import javax.microedition.io.*;
 //import javax.microedition.midlet.*;
 //import javax.microedition.lcdui.*;
 //import javax.microedition.pki.*;
 //
 //public class httpStest extends MIDlet
 //    implements CommandListener, Runnable {
 //  private Display mDisplay;
 //  private Form mForm;
 //  
 //  public void startApp() {
 //    mDisplay = Display.getDisplay(this);
 //    
 //    if (mForm == null) {
 //      mForm = new Form("HttpsMIDlet");
 //  
 //      mForm.addCommand(new Command("Exit", Command.EXIT, 0));
 //      mForm.addCommand(new Command("Send", Command.SCREEN, 0));
 //      mForm.setCommandListener(this);
 //    }
 //
 //    mDisplay.setCurrent(mForm);
 //  }
 //
 //  public void pauseApp() {}
 //
 //  public void destroyApp(boolean unconditional) {}
 //  
 //  public void commandAction(Command c, Displayable s) {
 //    if (c.getCommandType() == Command.EXIT) notifyDestroyed();
 //    else {
 //      Form waitForm = new Form("Connecting...");
 //      mDisplay.setCurrent(waitForm);
 //      Thread t = new Thread(this);
 //      t.start();
 //    }
 //  }
 //
 //  public void run() {
 //    String url = getAppProperty("HttpsMIDlet-URL");
 //
 //    try {
 //      // Query the server and retrieve the response.
 //      HttpsConnection hc = (HttpsConnection)Connector.open(url);
 //      
 //      SecurityInfo si = hc.getSecurityInfo();
 //      Certificate c = si.getServerCertificate();
 //      String subject = c.getSubject();
 //
 //      String s = "Server certificate subject: \n" + subject;
 //      Alert a = new Alert("Result", s, null, null);
 //      a.setTimeout(Alert.FOREVER);
 //      mDisplay.setCurrent(a, mForm);
 //
 //      hc.close();
 //    }
 //    catch (IOException ioe) {
 //      Alert a = new Alert("Exception", ioe.toString(), null, null);
 //      a.setTimeout(Alert.FOREVER);
 //      mDisplay.setCurrent(a, mForm);
 //    }
 //  }
 //}
 //
 //           
 //       
