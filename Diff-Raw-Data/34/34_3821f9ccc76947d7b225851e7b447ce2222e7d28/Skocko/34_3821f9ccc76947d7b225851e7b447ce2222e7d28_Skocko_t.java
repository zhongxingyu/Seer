 package org.manojlovic.skockoj2me;
 
 import javax.microedition.lcdui.*;
 import javax.microedition.midlet.*;
 
 /**
  * @author Manojlović Miloš (manojlovic88@gmail.com)
  */
 public class Skocko extends MIDlet implements CommandListener  {
 
   private Displayable d;
   private Form form;
   private Command  ok, new_game, help, info, exit;
   private String version = "Skocko v0.2 BETA";
 
   public Skocko (){
 
     form = new Form("Command Form");
     ok = new Command("New Game", Command.OK, 1);
     new_game = new Command("New Game", Command.OK, 2);
     help = new Command("Help", Command.HELP, 2);
     info = new Command("Info", Command.HELP,3);
     exit = new Command("Exit", Command.EXIT, 10);
 
   }
 
      public void startApp(){
     d = new Graphic();
 
     form.addCommand(ok);
     form.addCommand(help);
     form.addCommand(info);
     form.addCommand(exit);
     form.setCommandListener(this);
     
     d.addCommand(new_game);
     d.addCommand(help);
     d.addCommand(info);
     d.addCommand(exit);
     d.setCommandListener(this);
     Display.getDisplay(this).setCurrent(d);
   }
 
   public void pauseApp(){}
 
   public void destroyApp(boolean unconditional){}
 
 
  public void exitCom(){
     notifyDestroyed();
   }
  public void okCom(){
     startApp();
   }
  public void helpCom(){
      form.deleteAll();
      form.setTitle(version);
      form.append("Opis igre:");
      form.append("\n");
     form.append("Treba naci pravu kombinaciju simbola. U tome ce vam pomoci" +
             "crveni kruzici koji oznacavaju da je sinbol na pravom mestu, dok " +
             "zuti kruzici oznacavaju da sinbol nije na pravom mestu. Igra je " +
             "identicna kao i na TV slagalici. Srecno :)");
      
     Display.getDisplay(this).setCurrent( form);
   }
  public void infoCom(){
     form.deleteAll();
     form.setTitle(version);
    form.append("Igra: " + version);
     form.append("\n");
     form.append("Autor: Manojlović Miloš");
     form.append("\n");
     form.append("E-Mail: manojlovic88@gmail.com");
     form.append("\n");
    form.append("Web sajt: http://manojlovic.org");
    form.append("\n");
    form.append("Greske, predloge i zamerke mozete pisati na adresi: " +
            "http://github.com/manojlovic/skocko-j2me/issues");
     form.append("\n");
     form.append("Beograd, Jun 2012");
     Display.getDisplay(this).setCurrent(form);
     
   }
 
     public void commandAction(Command c, Displayable d) {
         String label = c.getLabel();
     if(label.equals("Help")){
       helpCom();
     } else if(label.equals("New Game")){
       okCom();
     } else if(label.equals("Info")){
       infoCom();
     } else if(label.equals("Exit")){
       exitCom();
     }
     }
 } 
