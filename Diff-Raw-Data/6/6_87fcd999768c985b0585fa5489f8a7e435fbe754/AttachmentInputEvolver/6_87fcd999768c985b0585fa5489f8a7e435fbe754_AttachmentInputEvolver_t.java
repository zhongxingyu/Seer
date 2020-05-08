 package org.sakaiproject.assignment2.tool.producers.evolvers;
 
 import org.sakaiproject.assignment2.logic.AttachmentInformation;
 import org.sakaiproject.assignment2.logic.ExternalContentLogic;
 
 import uk.org.ponder.beanutil.BeanGetter;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBasicListMember;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIInputMany;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
 import uk.org.ponder.rsf.uitype.UITypes;
 
 
 /**
  * This evolver renders the list of attachments on the Student Submission Editor
  * and previewer.  This includes teh name of the attachment, it's size, and an
  * optional remove link.
  * 
  * @author rjlowe
  * @author sgithens
  *
  */
 public class AttachmentInputEvolver {
 
     public static final String COMPONENT_ID = "attachment-list:";
     public static final String CORE_ID = "attachment-list-core:";
 
     private ExternalContentLogic contentLogic;
     public void setExternalContentLogic(ExternalContentLogic contentLogic) {
         this.contentLogic = contentLogic;
     }
 
     private MessageLocator messageLocator;
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
     private BeanGetter rbg;
     public void setRequestBeanGetter(BeanGetter rbg) {
         this.rbg = rbg;
     }
 
     /**
      * Renders the attachments.  There will be an option to remove them.
      * 
      * @param toevolve
      * @return
      */
     public UIJointContainer evolveAttachment(UIInputMany toevolve) {
         return evolveAttachment(toevolve, true);
     }
 
     /**
      * Renders the list of Attachments.  If removable is true, then a link for
      * removing the attachment is rendered as well.
      * 
      * @param toevolve
      * @param removable
      * @return
      */
     public UIJointContainer evolveAttachment(UIInputMany toevolve, boolean removable) {
         UIJointContainer togo = new UIJointContainer(toevolve.parent, toevolve.ID, COMPONENT_ID);
         toevolve.parent.remove(toevolve);
 
         toevolve.ID = "attachments-input";
         togo.addComponent(toevolve);
 
         String[] value = toevolve.getValue();
         // Note that a bound value is NEVER null, an unset value is detected via
         // this standard call
         if (UITypes.isPlaceholder(value)) {
             value = (String[]) rbg.getBean(toevolve.valuebinding.value);
             // May as well save on later fixups
             toevolve.setValue(value);
         }
 
         UIBranchContainer core = UIBranchContainer.make(togo, CORE_ID);
         int limit = Math.max(0, value.length);
         for (int i=0; i < limit; ++i) {
             UIBranchContainer row = UIBranchContainer.make(core, "attachment-list-row:", Integer.toString(i));
             String thisvalue = i < value.length ? value[i] : "";
 
             AttachmentInformation attach = contentLogic.getAttachmentInformation(thisvalue);
             if (attach != null) {
                 String fileSizeDisplay = "(" + attach.getContentLength() + ")";
 
                 UILink.make(row, "attachment_image", attach.getContentTypeImagePath());
                 UILink.make(row, "attachment_link", attach.getDisplayName(), attach.getUrl());
                 UIOutput.make(row, "attachment_size", fileSizeDisplay);
 
                 UIBasicListMember.makeBasic(row, "attachment_item", toevolve.getFullID(), i);
 
                 //Add remove link
                 if (removable) {
                     UIVerbatim.make(row, "attachment_remove", 
                             "<a href=\"#\" " +
                             "onclick=\"" +
                             "asnn2.removeAttachment(this);" +
                             "\">" +
                             messageLocator.getMessage("assignment2.remove") +
                     "</a>");
                 }
             }
 
         }
 
         // A few notes about this attachment functionality.  Currently all
         // the demo items are added in the javascript/html in AttachmentEvolver.html
         // and asnn2.updateAttachments javascript. It's a bit odd and fragile,
         // especially if the input names ever change.
         //if (limit == 0) {
             //output "demo" row, with styleClass of skip
            //UIBranchContainer row = UIBranchContainer.make(togo, "attachment-list-demo:", Integer.toString(0));
             //UILink.make(row, "attachment_image", "image.jpg");
             //UILink.make(row, "attachment_link", "demo", "demo.html");
             //UIOutput.make(row, "attachment_item", "demo");
            //UIBasicListMember.makeBasic(togo, "attachment_item_demo", toevolve.getFullID(), 0);
            UIOutput.make(togo, "attachments-binding-id", toevolve.getFullID());
             //UIOutput.make(row, "attachment_size", "demo");
             //Add remove link
             /*
             UIVerbatim.make(row, "attachment_remove", 
                     "<a href=\"#\" " +
                     "onclick=\"" +
                     "asnn2.removeAttachment(this);" +
                     "\">" +
                     messageLocator.getMessage("assignment2.remove") +
             "</a>");
             row.decorators = new DecoratorList(new UIStyleDecorator("skip"));
             */
         //}
 
         return togo;
     }
 
 
 }
