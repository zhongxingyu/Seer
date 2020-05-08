 package org.cotrix.web.codelistmanager.client.manager;
 
 
 import org.cotrix.web.share.client.resources.CommonResources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.SimpleLayoutPanel;
 import com.google.gwt.user.client.ui.SplitLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CodeListManagerViewImpl extends ResizeComposite implements CodeListManagerView {
 
 	@UiTemplate("CodeListManager.ui.xml")
 	interface CodeListManagerUiBinder extends UiBinder<Widget, CodeListManagerViewImpl> {}
 	private static CodeListManagerUiBinder uiBinder = GWT.create(CodeListManagerUiBinder.class);
 
	@UiField(provided=true) SplitLayoutPanel mainPanel;
 	@UiField SimpleLayoutPanel westPanel;
 	@UiField(provided=true) ContentPanel contentPanel;
 
 	@Inject
 	public CodeListManagerViewImpl(ContentPanel contentPanel) {
 		this.contentPanel = contentPanel;
		this.mainPanel = new SplitLayoutPanel(5);
 		initWidget(uiBinder.createAndBindUi(this));
 		CommonResources.INSTANCE.css().ensureInjected();
 		mainPanel.setWidgetToggleDisplayAllowed(westPanel, true);
 	}
 
 	public HasWidgets getWestPanel() {
 		return westPanel;
 	}
 	
 	public ContentPanel getContentPanel() {
 		return contentPanel;
 	}
 
 	public void showWestPanel(boolean show) {
 		mainPanel.setWidgetHidden(westPanel, !show);
 	}
 	
 	@Override
 	public void showAlert(String message)
 	{
 	    final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 	    simplePopup.setWidth("150px");
 	    simplePopup.setWidget(new HTML(message));
 	    simplePopup.center();
 	}
 
 }
