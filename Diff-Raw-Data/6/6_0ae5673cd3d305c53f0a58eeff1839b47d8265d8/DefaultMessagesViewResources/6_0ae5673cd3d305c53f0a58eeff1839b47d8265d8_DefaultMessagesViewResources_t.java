 package org.iplantc.de.client.sysmsgs.view;
 
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.ImageResource;
 
 /**
  * This defines the resources required for displaying system messages.
  */
 public interface DefaultMessagesViewResources extends ClientBundle {
 
     /**
      * This is the interface of the style used for rendering the system messages view.
      */
 	public interface Style extends CssResource {
         /**
          * The styling of a summary list item
          */
         String summaryItem();
 
         /**
          * The styling of a summary item that is currently selected.
          */
         String selected();
 
         /**
          * The styling of a message summary that has already been seen.
          */
         String summary();
 
         /**
          * The styling of the dismiss button
          */
         String dismiss();
 
         /**
          * The styling of a message summary that has not already been seen.
          */
         String unseen();
 
         /**
          * The styling of the activation date
          */
         String activationTime();
 
         /**
          * The styling of applied to the view of the body of the selected message.
          */
         String messageView();
 
         /**
          * The styling applied to the expiration date.
          */
         String expiry();

        /**
         * The styling applied to the status panels (loading, no messages)
         */
        String statusPanel();

 	}
 	
     /**
      * The image used to render a dismiss button when the mouse is not over the button.
      */
 	@Source("button_exit.png")
 	ImageResource dismissImg();
 
     /**
      * The image used to render a dismiss button when the mouse is over the button.
      */
 	@Source("button_exit_hover.png")
 	ImageResource dismissOnHoverImg();
 	
     /**
      * The style used to render the system messages view.
      */
     @Source("DefaultMessagesView.css")
 	Style style();
 
 }
