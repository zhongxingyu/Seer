 /* Copyright 2011 Massimo Gengarelli <gengarel@cs.unibo.it>
  * This file is part of Floz Configurator.
  * Floz Configurator is free software: you can redistribute it and/or modify it 
  * under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or 
  * (at your option) any later version.
  *
  * Floz Configurator is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License 
  * along with Floz Configurator. If not, see http://www.gnu.org/licenses/.
  */
 
 
 package it.unibo.cs.v2.client;
 
 import it.unibo.cs.v2.servlets.AcceptShare;
 import it.unibo.cs.v2.servlets.AcceptShareAsync;
 import it.unibo.cs.v2.servlets.GetActiveMachines;
 import it.unibo.cs.v2.servlets.GetActiveMachinesAsync;
 import it.unibo.cs.v2.servlets.GetMachines;
 import it.unibo.cs.v2.servlets.GetMachinesAsync;
 import it.unibo.cs.v2.servlets.GetNotifications;
 import it.unibo.cs.v2.servlets.GetNotificationsAsync;
 import it.unibo.cs.v2.shared.MachineInfo;
 import it.unibo.cs.v2.shared.MachineProcessInfo;
 import it.unibo.cs.v2.shared.Notification;
 import it.unibo.cs.v2.shared.ShareMachineNotification;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.StackLayoutPanel;
 
 public class MainPanel extends StackLayoutPanel {
 	private final int HEADERSIZE = 2;
 	
 	private final String WELCOME_STRING = "Welcome to <b>Live VirtuaCluster</b>. <br/>" +
 			"Using this simple web app you can easily create a personal virtual machine to use whenever you want, " +
 			"you can also share it with your friends and see the progresses they're making with your machines. Creating a " +
 			"new machine is as simple as clicking some buttons and following some instructions. If you should encounter a " +
 			"problem, please contact <a href=\"mailto:gengarel@cs.unibo.it\">the webmaster</a>. <b>Enjoy!</b>";
 	
 	private final String ZERO_NOTIFICATIONS = "<b>No new notifications</b>";
 	
 	private final HTMLPanel firstPanel = new HTMLPanel(WELCOME_STRING);
 	private final HTMLPanel activeMachinesPanel = new HTMLPanel("<h2>Active machines</h2>");
 	private final HTMLPanel machines = new HTMLPanel("");
 	private final HTMLPanel notificationsPanel = new HTMLPanel("");
 	
 	private LinkedList<Notification> notifications = new LinkedList<Notification>();
 	private LinkedList<MachineProcessInfo> activeMachines = new LinkedList<MachineProcessInfo>();
 	
 	private final Timer notificationsTimer = new Timer() {
 		
 		@Override
 		public void run() {
 			buildNotificationsList();
 		}
 	};
 	
 	private final Timer activeMachinesTimer = new Timer() {
 		
 		@Override
 		public void run() {
 			getActiveMachines();
 		};
 	};
 	
 	private final HashMap<String, String> userInfo;
 
 	private final GetMachinesAsync getMachinesProxy = (GetMachinesAsync) GWT.create(GetMachines.class);
 	private final GetNotificationsAsync getNotificationsProxy = (GetNotificationsAsync) GWT.create(GetNotifications.class);
 	private final AcceptShareAsync acceptShareProxy = (AcceptShareAsync) GWT.create(AcceptShare.class);
 	private final GetActiveMachinesAsync getActiveMachinesProxy = (GetActiveMachinesAsync) GWT.create(GetActiveMachines.class);
 	
 	public MainPanel(final HashMap<String, String> userInfo) {
 		super(Unit.EM);
 
 		this.userInfo = userInfo;
 		
 		final Anchor refresh = new Anchor("Refresh Machines' list");
 		
 		buildMachinesList();
 		
 		firstPanel.add(new HTML("<h2>Navigation Menu</h2>"));
 		firstPanel.add(new Hyperlink("HomePage", HistoryTokens.START));
 		firstPanel.add(refresh);
 		
 		firstPanel.add(new HTML("<h2>New machine</h2>"));
 		firstPanel.add(new Hyperlink("Create from scratch", HistoryTokens.NEWMACHINE));
 		firstPanel.add(new Hyperlink("Create from an existing one", HistoryTokens.PREBUILT));
 		
 		firstPanel.add(activeMachinesPanel);
 		
 		firstPanel.add(new HTML("<h2>Credits</h2>"));
 		firstPanel.add(new Anchor("VirtualSquare", "http://wiki.virtualsquare.org", "_blank")); 
 		firstPanel.add(new HTML("&nbsp;&nbsp;Home of VDE"));
 		
 		firstPanel.add(new Anchor("KVM", "http://www.linux-kvm.org", "_blank")); 
 		firstPanel.add(new HTML("&nbsp;&nbsp;Kernel Virtual Machine"));
 		
 		firstPanel.add(new Anchor("jBCrypt", "http://www.mindrot.org/projects/jBCrypt/", "_blank")); 
 		firstPanel.add(new HTML("&nbsp;&nbsp;internally used for hashing passwords"));
 		
 		firstPanel.add(new Anchor("Google Web Toolkit", "http://code.google.com/webtoolkit/", "_blank")); 
 		firstPanel.add(new HTML("&nbsp;&nbsp;the framework used for this webapp"));
 		
 		add(new ScrollPanel(firstPanel), userInfo.get("displayname"), HEADERSIZE);
 		add(new ScrollPanel(notificationsPanel), "Sharing requests", HEADERSIZE);
 		
 		refresh.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				clear();
 				add(new ScrollPanel(firstPanel), userInfo.get("displayname"), HEADERSIZE);
 				add(new ScrollPanel(notificationsPanel), "Sharing requests", HEADERSIZE);
 				buildMachinesList();
 			}
 		});
 		
 		// Get notifications every 3 seconds
 		notificationsTimer.scheduleRepeating(3000);
 		
 		// Get active machines every 3 seconds
 		activeMachinesTimer.scheduleRepeating(3000);
 	}
 	
 	private final void buildMachinesList() {
 		machines.clear();
 
 		getMachinesProxy.getMachines(new AsyncCallback<LinkedList<MachineInfo>>() {
 			@Override
 			public void onSuccess(LinkedList<MachineInfo> result) {
 				if (result != null) {
 					for (final MachineInfo machineInfo : result) 
						insert(new ScrollPanel(new MachinePanel(machineInfo, machineInfo.getRealOwner().equals(userInfo.get("login")))), 
 								machineInfo.getName(), HEADERSIZE, getWidgetCount() - 1);
 				}
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				machines.add(new HTML("General error"));
 			}
 		});
 	}
 
 	// Changes the Notifications' panel accordingly
 	private final void buildNotificationsList() {
 		getNotificationsProxy.getNotifications(new AsyncCallback<LinkedList<Notification>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				notificationsPanel.clear();
 				notificationsPanel.add(new HTML("<b>Failure while getting the notifications :-(</b>"));
 				notificationsTimer.cancel();
 			}
 
 			@Override
 			public void onSuccess(LinkedList<Notification> result) {
 				
 				if (result == null)
 					notificationsTimer.cancel();
 				
 				// if the notifications' list has changed in some way, then refresh the panel
 				if (!notifications.equals(result)) {
 					notifications = result;
 					notificationsPanel.clear();
 					
 					for (final Notification notification : notifications) {
 						notificationsPanel.add(new HTML("<b>New notification from: " + notification.getFrom() + "</b><br />"));
 						switch (notification.getType()) {
 						case SHAREMACHINE:
 							final Button accept = new Button("Accept");
 							final Button refuse = new Button("Refuse");
 							final HorizontalPanel container = new HorizontalPanel();
 							final ShareMachineNotification n = (ShareMachineNotification) notification;
 							notificationsPanel.add(new HTML("He/She wants to share " + n.getMachineName() + " with you.<br />"));
 							notificationsPanel.add(new HTML(n.getMessage() + "<br />"));
 							container.add(accept);
 							container.add(new HTML("&nbsp;&nbsp;&nbsp;"));
 							container.add(refuse);
 							
 							accept.addClickHandler(new ClickHandler() {
 
 								@Override
 								public void onClick(ClickEvent event) {
 									acceptShareProxy.acceptShare(n, new AsyncCallback<Boolean>() {
 
 										@Override
 										public void onFailure(Throwable caught) {
 											Window.alert(caught.getMessage());
 										}
 
 										@Override
 										public void onSuccess(Boolean result) {
 											if (result)
 												Window.alert("Share accepted successfully. Refresh your machines' list to see it.");
 										}
 									});
 								}
 							});
 							
 						
 							notificationsPanel.add(container);
 							notificationsPanel.add(new HTML("<br /><br />"));
 							
 							break;
 						}
 					}
 				}
 				
 				else if (result.size() < 1) {
 					notificationsPanel.clear();
 					notificationsPanel.add(new HTML(ZERO_NOTIFICATIONS));
 				}
 			}
 		});
 	}
 	
 	public void getActiveMachines() {
 		getActiveMachinesProxy.getActiveMachines(new AsyncCallback<LinkedList<MachineProcessInfo>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				activeMachinesPanel.clear();
 				activeMachinesPanel.add(new HTML("<b>Failure while getting the active machines' list :-(</b>"));
 			}
 
 			@Override
 			public void onSuccess(LinkedList<MachineProcessInfo> result) {
 				if (result == null) {
 					activeMachinesPanel.clear();
 					activeMachinesPanel.add(new HTML("No running machines found."));
 					return;
 				}
 				
 				if (!activeMachines.equals(result)) {
 					activeMachines = result;
 					
 					activeMachinesPanel.clear();
 					for (MachineProcessInfo mp : activeMachines) 
 						activeMachinesPanel.add(new HTML("<b>" + mp.getMachineName() + "</b> on server " + mp.getVncServer() + "<br />"));
 				}
 			}
 		});
 	}
 }
