 package com.redhat.automationportalui.client.pav;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.redhat.automationportalui.client.AutomationPortalUIClientFactory;
 import com.redhat.automationportalui.client.constants.AutomationPortalUIConstants;
 import com.redhat.automationportalui.client.data.AutomationPortalResponseData;
 import com.redhat.automationportalui.client.data.AutomationPortalResponseDataFactory;
 import com.redhat.automationportalui.client.resources.APUI_Errors;
 import com.redhat.automationportalui.client.resources.BugzillaReportGeneratorUIStrings;
 import com.redhat.automationportalui.client.resources.CommonUIStrings;
 import com.redhat.automationportalui.client.resources.ParseToc;
 import com.redhat.automationportalui.client.template.AutomationPortalUITemplate;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 
 public class ParseTocView
 {
 	private final static String REST_ENDPOINT = "rest/ParseTOC/get/json";
 	private final AutomationPortalUITemplate template;
 	private final ParseToc uiStrings;
 	private final CommonUIStrings commonUiStrings;
 	private TextBox message;
 	private TextArea output;
 	private PushButton go;
 
 	public ParseTocView(final AutomationPortalUIClientFactory clientFactory, final AutomationPortalUITemplate template, final CommonUIStrings commonUiStrings, final APUI_Errors apuiErrors)
 	{
 		/* Get the translates log messages */
 		uiStrings = (ParseToc) GWT.create(ParseToc.class);
 		this.commonUiStrings = commonUiStrings;
 		this.template = template;
 	}
 
 	public void display()
 	{
 		template.getSubTitle().setText(commonUiStrings.ParseTOC());
 		
 		final VerticalPanel topLevelPanel = new VerticalPanel();
 
 		topLevelPanel.add(new HTML(uiStrings.Description()));
 		
 		final HTML descriptionLineTwo = new HTML(uiStrings.DescriptionLineTwo());
 		descriptionLineTwo.getElement().getStyle().setMarginBottom(2, Unit.EM);
 		topLevelPanel.add(descriptionLineTwo);
 		
 		final Grid grid = new Grid(2, 2); 
 		topLevelPanel.add(grid);
 		
 		message = new TextBox();
 		message.setReadOnly(true);
 		message.setWidth("40em");
 		grid.setWidget(0, 0, new HTML(commonUiStrings.Message()));
 		grid.setWidget(0, 1, message);
 		
 		output = new TextArea();
 		output.setReadOnly(true);
 		output.setWidth("40em");
 		output.setHeight("10em");
 		grid.setWidget(1, 0, new HTML(commonUiStrings.Output()));
 		grid.setWidget(1, 1, output);
 		
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
 		
 		template.getContentPanel().setWidget(topLevelPanel);
 	}
 
 	private void run()
 	{
 		enableUI(false);
 		
 		final String restUrl = AutomationPortalUIConstants.REST_SERVER_URL + REST_ENDPOINT; 
 		
 		// Send request to server and catch any errors.
 		final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
 
 		try
 		{
 			builder.sendRequest(null, new RequestCallback()
 			{
 				@Override
 				public void onError(final Request request, final Throwable exception)
 				{
 					enableUI(true);
 				}
 
 				@Override
 				public void onResponseReceived(final Request request, final Response response)
 				{
 					if (200 == response.getStatusCode())
 					{
 						final String jsonResponse = response.getText();
 						final AutomationPortalResponseData responseData = AutomationPortalResponseDataFactory.asAutomationPortalResponseData(jsonResponse);
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
 		template.showLoadingImage(!enabled);
 	}
 }
