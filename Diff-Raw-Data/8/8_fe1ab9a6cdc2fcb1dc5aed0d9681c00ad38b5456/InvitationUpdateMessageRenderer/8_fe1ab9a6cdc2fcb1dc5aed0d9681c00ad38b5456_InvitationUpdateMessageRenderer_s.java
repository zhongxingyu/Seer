 package de.flower.rmt.ui.panel.activityfeed.renderer;
 
 import de.flower.rmt.model.RSVPStatus;
 import de.flower.rmt.model.type.activity.InvitationUpdateMessage;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author flowerrrr
  */
 public class InvitationUpdateMessageRenderer implements IMessageRenderer<InvitationUpdateMessage> {
 
     private final static Logger log = LoggerFactory.getLogger(InvitationUpdateMessageRenderer.class);
 
     @Override
     public String toString(final InvitationUpdateMessage message) {
         String s = "";
         String status = (message.getStatus() != null) ? new ResourceModel(RSVPStatus.getResourceKey(message.getStatus())).getObject() : "";
         Object[] params = new Object[]{EventMessageRenderer.getEventLink(message), status};
         if (message.getStatus() != null) {
             s = new StringResourceModel("activity.invitation.status.${status}", Model.of(message), params).getObject();
             if (message.getComment() != null) {
                 s = new StringResourceModel("activity.invitation.statuscomment.update", Model.of(message), new Object[] {s}).getObject();
             }
         } else if (message.getComment() != null) {
             s += new StringResourceModel("activity.invitation.comment.update", Model.of(message), params).getObject();
         }
         if (message.getManagerComment() != null) {
             s += new StringResourceModel("activity.invitation.managercomment.update", Model.of(message), params).getObject();
         }
         log.debug(message.toString() + " -> [" + s + "]");
         return s;
     }
 }
