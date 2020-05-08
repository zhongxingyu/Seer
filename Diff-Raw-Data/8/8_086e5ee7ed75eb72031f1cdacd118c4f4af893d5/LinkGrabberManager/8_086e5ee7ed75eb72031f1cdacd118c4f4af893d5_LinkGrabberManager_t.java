 package polly.linkexpander.core;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 
 import de.skuzzle.polly.sdk.eventlistener.MessageEvent;
 
 import polly.linkexpander.core.grabbers.LinkGrabber;
 
 
 public class LinkGrabberManager {
 
     private List<LinkGrabber> grabbers;
     private boolean enabled;
     
     
     
     public LinkGrabberManager() {
         this.grabbers = new LinkedList<LinkGrabber>();
         this.enabled = true;
     }
     
     
     
     public void addLinkGrabber(LinkGrabber grabber) {
         this.grabbers.add(grabber);
     }
     
     
     
     public boolean isEnabled() {
         return this.enabled;
     }
     
     
     
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
     
     
     
     public void processMessageEvent(MessageEvent e) {
         if (!this.enabled) {
             return;
         }
         
         for (LinkGrabber grabber : this.grabbers) {
             Matcher m = grabber.getLinkPattern().matcher(e.getMessage());
             if (m.find()) {
                 String r = grabber.processMatch(e, m);
                 if (r != null) {
                     e.getSource().sendMessage(e.getChannel(), r + 
                         " (" + grabber.getLink(e.getMessage(), m) + ")", this);
                 }
             }
         }
     }
 }
