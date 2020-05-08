 package com.redhat.automationportalui.client.pav;
 
 import java.net.URLEncoder;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.redhat.automationportalui.client.AutomationPortalUIClientFactory;
 import com.redhat.automationportalui.client.AutomationPortalUIConstants;
 import com.redhat.automationportalui.client.data.AutomationPortalResponseData;
 import com.redhat.automationportalui.client.resources.APUI_Errors;
 import com.redhat.automationportalui.client.resources.BugzillaReportGeneratorUIStrings;
 import com.redhat.automationportalui.client.resources.CommonUIStrings;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 
 public class BugzillaReportGeneratorView
 {
 	private final static String REST_ENDPOINT = "rest/BugzillaReportGenerator/get/json";
 	private final BugzillaReportGeneratorUIStrings uiStrings;
 	private final CommonUIStrings commonUiStrings;
 	private TextBox bugzillaUsername;
	private PasswordTextBox bugzillaPassword; 
 	private TextBox message;
 	private TextArea output;
 	private PushButton go;
 
 	public BugzillaReportGeneratorView(final AutomationPortalUIClientFactory clientFactory, final CommonUIStrings commonUiStrings, final APUI_Errors apuiErrors)
 	{
 		/* Get the translates log messages */
 		uiStrings = (BugzillaReportGeneratorUIStrings) GWT.create(BugzillaReportGeneratorUIStrings.class);
 		this.commonUiStrings = commonUiStrings;
 	}
 
 	public void display(final AcceptsOneWidget panel)
 	{
 		final VerticalPanel topLevelPanel = new VerticalPanel();
 
 		topLevelPanel.add(new HTML(uiStrings.Description()));
 		
 		final HTML descriptionLineTwo = new HTML(uiStrings.DescriptionLineTwo());
 		descriptionLineTwo.getElement().getStyle().setMarginBottom(2, Unit.EM);
 		topLevelPanel.add(descriptionLineTwo);
 		
 		final Grid grid = new Grid(4, 2); 
 		topLevelPanel.add(grid);
 		
 		bugzillaUsername = new TextBox();
 		bugzillaUsername.setWidth("40em");
 		grid.setWidget(0, 0, new HTML(uiStrings.BugzillaUsername()));
 		grid.setWidget(0, 1, bugzillaUsername);
 		
		bugzillaPassword = new PasswordTextBox();
 		bugzillaPassword.setWidth("40em");
 		grid.setWidget(1, 0, new HTML(uiStrings.BugzillaPassword()));
 		grid.setWidget(1, 1, bugzillaPassword);
 		
 		message = new TextBox();
 		message.setReadOnly(true);
 		message.setWidth("40em");
 		grid.setWidget(2, 0, new HTML(commonUiStrings.Message()));
 		grid.setWidget(2, 1, message);
 		
 		output = new TextArea();
 		output.setReadOnly(true);
 		output.setWidth("40em");
 		output.setHeight("10em");
 		grid.setWidget(3, 0, new HTML(commonUiStrings.Output()));
 		grid.setWidget(3, 1, output);
 		
 		go = new PushButton(commonUiStrings.Go());
 		go.setWidth("10em");
 		go.setHeight("2em");
 		go.getElement().getStyle().setProperty("textAlign", "center");
 		go.getElement().getStyle().setProperty("display", "table-cell");
 		go.getElement().getStyle().setProperty("verticalAlign", "middle");
 		go.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(final ClickEvent event)
 			{
 				run();
 			}
 		});
 		topLevelPanel.add(go);
 		
 		panel.setWidget(topLevelPanel);
 		
 		final HTML requirements = new HTML(uiStrings.Requirements());
 		requirements.getElement().getStyle().setMarginTop(2, Unit.EM);
 		topLevelPanel.add(requirements);
 	}
 
 	private void run()
 	{
 		enableUI(false);
 		
 		final String username = bugzillaUsername.getText();
 		final String password = bugzillaPassword.getText();
 		
 		final String restUrl = AutomationPortalUIConstants.REST_SERVER_URL + REST_ENDPOINT + "?bugzillaUsername=" + URL.encode(username) + "&bugzillaPassword=" + URL.encode(password); 
 		
 		// Send request to server and catch any errors.
 		final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
 
 		try
 		{
 			builder.sendRequest(null, new RequestCallback()
 			{
 				public void onError(final Request request, final Throwable exception)
 				{
 					enableUI(true);
 				}
 
 				public void onResponseReceived(final Request request, final Response response)
 				{
 					if (200 == response.getStatusCode())
 					{
 						final String jsonResponse = response.getText();
 						final AutomationPortalResponseData responseData = asAutomationPortalResponseData(jsonResponse);
 						message.setText(responseData.getMessage());
 						output.setText(responseData.getOutput());
 					}
 					else
 					{
 						// displayError("Couldn't retrieve JSON (" +
 						// response.getStatusText() + ")");
 					}
 					
 					enableUI(true);
 				}
 			});
 		}
 		catch (final RequestException ex)
 		{
 			// displayError("Couldn't retrieve JSON");
 			enableUI(true);
 			
 		}
 	}
 	
 	private void enableUI(final boolean enabled)
 	{
 		go.setEnabled(enabled);
 		bugzillaUsername.setEnabled(enabled);
 		bugzillaPassword.setEnabled(enabled);	
 	}
 
 	private final native AutomationPortalResponseData asAutomationPortalResponseData(final String json) 
 	/*-{
 		return eval('(' + json + ')');
 	}-*/;
 }
