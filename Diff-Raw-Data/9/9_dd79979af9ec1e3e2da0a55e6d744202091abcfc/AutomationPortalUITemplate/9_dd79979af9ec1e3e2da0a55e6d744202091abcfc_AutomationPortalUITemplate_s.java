 package com.redhat.automationportalui.client.template;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.redhat.automationportalui.client.pav.BugzillaReportGeneratorPlace;
 import com.redhat.automationportalui.client.resources.CommonUIStrings;
 
 /**
  * This class builds the base UI into which the other GWT Views will add their
  * UI components.
  */
 public class AutomationPortalUITemplate
 {
 	/** The planel that the views will add their content to */
 	final private SimplePanel contentPanel;
 	/** A reference to the common translated strings */
 	final private CommonUIStrings commonUiStrings;
 	/** The panel that will hold the links to the various views */
 	final VerticalPanel linksVerticalPanel;
 	
 	public SimplePanel getContentPanel()
 	{
 		return contentPanel;
 	}
 	
 	public AutomationPortalUITemplate(final CommonUIStrings commonUiStrings)
 	{
 		this.commonUiStrings = commonUiStrings;
 		
 		final VerticalPanel verticalPanel = new VerticalPanel();
 		
 		final Label heading = new Label(commonUiStrings.AutomationPortal());
 		heading.getElement().getStyle().setFontSize(4, Unit.EM);
 		heading.getElement().getStyle().setMargin(0.5, Unit.EM);
 		verticalPanel.add(heading);
 		
 		final HorizontalPanel horizontalPanel = new HorizontalPanel();
 		verticalPanel.add(horizontalPanel);
 		
 		linksVerticalPanel = new VerticalPanel();
 		linksVerticalPanel.getElement().getStyle().setMarginLeft(2, Unit.EM);
 		linksVerticalPanel.getElement().getStyle().setMarginRight(2, Unit.EM);
 		horizontalPanel.add(linksVerticalPanel);
 		
 		contentPanel = new SimplePanel();
 		horizontalPanel.add(contentPanel);
 		
 		buildLinks();
 		
 		/* Add the template to the page */
 		final RootPanel rootPanel = RootPanel.get();
 		rootPanel.add(verticalPanel);	
 	}
 	
 	private void buildLinks()
 	{
		final BugzillaReportGeneratorPlace bugzillaReportGeneratorPlace = new BugzillaReportGeneratorPlace();
		final Hyperlink bugzillaReportGenerator = new Hyperlink(commonUiStrings.BugzillaReportGenerator(), bugzillaReportGeneratorPlace.toString());
 		linksVerticalPanel.add(bugzillaReportGenerator);
 	}
 }
