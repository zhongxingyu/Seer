 package be.ugent.zeus;
 
 import ch9k.plugins.Plugin;
 import ch9k.chat.Conversation;
 
 /**
  * A dummy plugin that does nothing at all.
  * @author Jasper Van der Jeugt
  */
 public class DummyPlugin implements Plugin {
     @Override
    public void enable(Conversation conversation) {
     }
 
     @Override
    public void disable() {
     }
 }
