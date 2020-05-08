 package org.sakaiproject.assignment2.tool.producers.evolvers;
 
 import uk.org.ponder.rsf.components.UIBasicListMember;
 import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIInputMany;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.uitype.UITypes;
 import uk.org.ponder.beanutil.BeanGetter;
 
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 
 
 public class AttachmentInputEvolver {
 	
 	public static final String COMPONENT_ID = "attachment-list:";
 	public static final String CORE_ID = "attachment-list-core:";
 	
 	private ContentHostingService contentHostingService;
 	public void setContentHostingService(ContentHostingService contentHostingService) {
 		this.contentHostingService = contentHostingService;
 	}
 	
 	private ExternalLogic externalLogic;
 	public void setExternalLogic(ExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 	
 	private MessageLocator messageLocator;
 	public void setMessageLocator(MessageLocator messageLocator) {
 		this.messageLocator = messageLocator;
 	}
 	
 	private BeanGetter rbg;
 	public void setRequestBeanGetter(BeanGetter rbg) {
 	    this.rbg = rbg;
 	}
 	public UIJointContainer evolveAttachment(UIInputMany toevolve) {
		//TODO FIXME SWG
		// The parent seems to be getting nulled after the toevolve.parent.remove
		// call.
		UIContainer oldparent = toevolve.parent;
 		toevolve.parent.remove(toevolve);
		//UIJointContainer togo = new UIJointContainer(toevolve.parent, toevolve.ID, COMPONENT_ID);
		UIJointContainer togo = new UIJointContainer(oldparent, toevolve.ID, COMPONENT_ID);
 		
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
 			try {
 				//TODO - but all contentHosting calls in an external Logic
 				ContentResource cr = contentHostingService.getResource(thisvalue);
 				UILink.make(row, "attachment_image", externalLogic.getContentTypeImagePath(cr));
 				UILink.make(row, "attachment_link", cr.getProperties().getProperty(cr.getProperties().getNamePropDisplayName()),
 	    				cr.getUrl());
 				UIOutput.make(row, "attachment_item", thisvalue);
 				UIBasicListMember.makeBasic(row, "attachment_item", toevolve.getFullID(), i);
 				String file_size = externalLogic.getReadableFileSize(cr.getContentLength());
 	    		UIOutput.make(row, "attachment_size", file_size);
 	    		//Add remove link
     			UIVerbatim.make(row, "attachment_remove", 
     					"<a href=\"#\" " +
     					"onclick=\"" +
     					"jQuery(this).parent('span').parent('li').remove();updateDisplayNoAttachments();" +
     					"\">" +
     					messageLocator.getMessage("assignment2.remove") +
     					"</a>");
 			} catch (Exception e) {
 				//do nothing
 			}
 		}
 		
 		if (limit == 0) {
 			//output "demo" row, with styleClass of skip
 			UIBranchContainer row = UIBranchContainer.make(core, "attachment-list-row:", Integer.toString(0));
 			UILink.make(row, "attachment_image", "image.jpg");
 			UILink.make(row, "attachment_link", "demo", "demo.html");
			//UIOutput.make(row, "attachment_item", "demo");
 			UIBasicListMember.makeBasic(row, "attachment_item", toevolve.getFullID(), 0);
 			UIOutput.make(row, "attachment_size", "demo");
 			//Add remove link
 			UIVerbatim.make(row, "attachment_remove", 
 					"<a href=\"#\" " +
 					"onclick=\"" +
 					"jQuery(this).parent('span').parent('li').remove();updateDisplayNoAttachments();" +
 					"\">" +
 					messageLocator.getMessage("assignment2.remove") +
 					"</a>");
     		row.decorators = new DecoratorList(new UIStyleDecorator("skip"));
 		}
 		
 		return togo;
 	}
 	
 	
 }
