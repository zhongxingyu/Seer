 /**
  * Copyright 2012
  * Author: Hazirah Hamdani
  * Title: WikiM2E: Cross Language Wikipedia Link Discovery Malay to English
  * Date: July - Nov 2012
  * 
  */
 
 package com.crosslink.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class Crosslink_malay implements EntryPoint {
 
 	private wikiMEServiceAsync wikiMEService = GWT.create(wikiMEService.class);
 	public String MalayTable = MyResource.INSTANCE.loadTable().getText();
 	public String webarticle;
 	protected String wikiLink = "";
 
 	public void onModuleLoad() {
 		// inject css source
 		 MyResource.INSTANCE.css().ensureInjected(); 
 		/* create UI */
 		
 		
 		final TextBox urlText = new TextBox();
 		final HTML html = new HTML("<center>Loading...</center>");
 		html.setSize("500px", "500px");
 		html.addStyleName("loading");
 		//urlText.setWidth("600");
 		urlText.setText("http://");
 		urlText.addStyleName("urlField");
 		
 		Label lblName = new Label("Enter Malay Newspaper webpage: ");
 		lblName.addStyleName("label1");
 
 		Button goButton = new Button("GO");
 		goButton.addStyleName("goButton");
 		
 
 		RootPanel.get("labelText").add(lblName);
 	
 		RootPanel.get("urlField").add(urlText);
 		RootPanel.get("goButton").add(goButton);
 		urlText.addKeyUpHandler(new KeyUpHandler() {
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 					RootPanel.get("htmlContainer").clear();
 					// Verify input url
 					if (isUrl(urlText.getText())) {
 						Window.alert("Please enter a Malay web source!");
 						return;
 
 					} else {
 
 						/* make remote call to server to get the message */
 						wikiMEService.getwebcontent(urlText.getValue(),
 								MalayTable, new ContentCallBack());
 					}
 
 				}
 			}
 		});
 		
 		goButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				// if Html panel is there
 				RootPanel.get("htmlContainer").clear();
 				// inform user about time
				Window.alert("The webpage might take few minutes to load! Please wait!");
 				RootPanel.get("loadText").add(html);
 				// Verify input url
 				if (isUrl(urlText.getText())) {
 					Window.alert("Please enter a Malay web source!");
 					return;
 
 				} else {
 					/* make remote call to server to get the message */
 					wikiMEService.getwebcontent(urlText.getValue(), MalayTable,
 							new ContentCallBack());
 
 				}
 			}
 		});
 
 
 	}
 
 	/**
 	 * Checking User's input
 	 * 
 	 * @param Weburl
 	 *            input
 	 * @return True or False
 	 */
 	protected boolean isUrl(String input) {
 		if (input.isEmpty() || input.contentEquals("http://")
 				|| !input.contains("http://")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * ContentCallBack for retrieving website HTML source. Will return error if
 	 * cannot obtain server response
 	 * 
 	 * @author Hazirah Hamdani
 	 * 
 	 */
 
 	private class ContentCallBack implements AsyncCallback<Webcontent> {
 		@Override
 		public void onFailure(Throwable caught) {
 			/* server side error occured */
 			Window.alert("Unable to obtain server response");
 			RootPanel.get("loadText").clear();
 			HTMLPanel html = new HTMLPanel(caught.getMessage());
 			RootPanel.get("htmlContainer").add(html);
 		}
 
 		@Override
 		public void onSuccess(Webcontent result) {
 			
 			/* server returned result, show user the message */
 			webarticle = result.getwebContent();
 			HTMLPanel html = new HTMLPanel(webarticle);
 			html.addStyleName("htmlStyle");
 			// Get MalayList
 			//anchorArticle(html);
 			RootPanel.get("loadText").clear();
 			RootPanel.get("htmlContainer").add(html);
 
 		}
 
 	}
 
 }
