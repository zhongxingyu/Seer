 package com.sfeir.websockets.poc.client;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWTBridge;
import com.google.gwt.dev.shell.GWTBridgeImpl;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.sfeir.websockets.poc.externals.WebSocketCallback;
 import com.sfeir.websockets.poc.externals.WebSocketClient;
 import com.sfeir.websockets.poc.shared.Message;
 import com.sfeir.websockets.poc.shared.News;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class PushPOC implements EntryPoint, ClickHandler, KeyPressHandler {
 
 	private static final int MAX_NEWS = 10;
 	private static final int DEFAULT_RETRY = 1000;
 	private WebSocketClient ws = new WebSocketClient();
 	private TextBox newsField;
 	private Button pushButton;
 	private Widget serviceStatus;
 
 	private boolean alreadyConnected = false;
 	
 	private int retry = DEFAULT_RETRY;
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		RootPanel adminPanel = RootPanel.get("admin");
 		newsField = new TextBox();
 		newsField.getElement().setId("news-field");
 		pushButton = new Button("Push");
 		adminPanel.add(newsField);
 		adminPanel.add(pushButton);
 		
 		serviceStatus = RootPanel.get("service");
 		
 		ws.setCallback(new WebSocketCallback() {
 			
 			@Override
 			public void message(String message) {
 				Message m = Message.read(message);
 				RootPanel newsPanel = RootPanel.get("news");
 				switch(m.getType()) {
 				case NEWS:
 					if (newsPanel.getWidgetCount() == MAX_NEWS) {
						newsPanel.remove(MAX_NEWS);
 					}
 					newsPanel.insert(createNewsLabel(m.getNews()), 0);
 					break;
 				case SCORE:
 //					final Score score = m.getScore();
 					break;
 				}
 			}
 
 			DateTimeFormat DTF = DateTimeFormat.getFormat("HH:mm");
 			
 			private Panel createNewsLabel(final News news) {
 				Label date = new InlineLabel(DTF.format(news.getDate()));
 				date.addStyleName("date");
 				
 				Label msg = new InlineLabel(news.getMessage());
 				msg.addStyleName("message");
 				
 				Panel p = new FlowPanel();
 				p.addStyleName("news-item");
 				
 				p.add(date);
 				p.add(msg);
 				
 				return p;
 			}
 			
 			@Override
 			public void connected() {
 				retry = DEFAULT_RETRY;
 				newsField.setEnabled(true);
 				pushButton.setEnabled(true);
 				serviceStatus.getElement().setInnerText("online");
 				serviceStatus.setStyleName("online");
 				if (alreadyConnected) log("Back online!");
 				else alreadyConnected = true;
 			}
 
 			@Override
 			public void disconnected() {
 				newsField.setEnabled(false);
 				pushButton.setEnabled(false);
 				serviceStatus.getElement().setInnerText("offline");
 				serviceStatus.setStyleName("offline");
 				triggerReconnect();
 			}
 			
 		});
 		
 		connect();
 		
 		pushButton.addClickHandler(this);
 		newsField.addKeyPressHandler(this);
 
 	}
 
 	private void connect() {
 		connect("ws://localhost:7777/pushws/ws");
 //		connect((GWT.getHostPageBaseURL()+"ws").replace("http://", "ws://"));
 	}
 	
 	private void connect(String url) {
 		ws.connect(url);
 	}
 		
 	public void triggerReconnect() {
 		Timer t = new Timer() {
 			@Override
 			public void run() {
 				connect();
 			}
 		};
 		log("Connection lost, retry in " + (retry / 1000) + " seconds.");
 		t.schedule(retry);
 		retry = 2*retry;
 	}
 	
 	private void log(String message) {
 		RootPanel.get("logger").getElement().setInnerText(message);
 		GWT.log(message);
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {
 		DeferredCommand.addCommand(new Command() {
 			
 			@Override
 			public void execute() {
 //				if (ws.getStatus()==WebSocketClient.OPEN) {
 					ws.send(Message.write(new Message(new News(new Date(), newsField.getValue()))));
 //				} else {
 //					Window.alert("Not connected, but didn't see the disconnection...");
 //				}
 				newsField.setValue("", false);
 			}
 		});
 	}
 
 	@Override
 	public void onKeyPress(KeyPressEvent event) {
 		if (event.getCharCode() == KeyCodes.KEY_ENTER) {
 			pushButton.click();
 		}
 		
 	}
 }
