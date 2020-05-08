 package org.iplantc.de.client.sysmsgs.view;
 
import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.appearance.widgets.InternalAnchorDefaultAppearance;
 import org.iplantc.core.uicommons.client.widgets.InternalAnchor;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.logical.shared.OpenHandler;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.core.client.dom.XElement;
 
 /**
  * An object of this class can display a new message announcement as HTML.
  */
 final class DefaultNewMessageView extends InlineHTML implements NewMessageView {
 
 	interface Templates extends XTemplates {
 		@XTemplate("<div>You have an important announcement. {anchor}</div>")
 		SafeHtml make(SafeHtml anchor);
 	}
 	
 	private static final Templates FACTORY = GWT.create(Templates.class);
     private static final InternalAnchorDefaultAppearance ANCHOR_APPEARANCE
 			= new InternalAnchorDefaultAppearance(true);
 	
 	private final InternalAnchor<Void> anchor;
 
     /**
      * the constructor
      * 
      * @param presenter the presenter managing the view
      */
     DefaultNewMessageView(final Presenter presenter) {
        super(FACTORY.make(ANCHOR_APPEARANCE.render(I18N.DISPLAY.openMessage())));
         final XElement elmt = ANCHOR_APPEARANCE.getAnchorElement(XElement.as(getElement()));
         anchor = InternalAnchor.wrap(null, elmt);
 		anchor.addOpenHandler(new OpenHandler<Void>() {
 			@Override
 			public void onOpen(final OpenEvent<Void> event) {
                 presenter.handleDisplayMessages();
 			}});			
  	}
 
 }
