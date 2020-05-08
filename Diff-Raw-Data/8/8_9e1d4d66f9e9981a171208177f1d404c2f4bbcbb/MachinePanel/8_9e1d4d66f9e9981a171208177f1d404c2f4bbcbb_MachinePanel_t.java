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
 
 import it.unibo.cs.v2.servlets.DeleteMachine;
 import it.unibo.cs.v2.servlets.DeleteMachineAsync;
 import it.unibo.cs.v2.servlets.RemoveShare;
 import it.unibo.cs.v2.servlets.RemoveShareAsync;
 import it.unibo.cs.v2.servlets.ShareMachine;
 import it.unibo.cs.v2.servlets.ShareMachineAsync;
 import it.unibo.cs.v2.servlets.StartMachine;
 import it.unibo.cs.v2.servlets.StartMachineAsync;
 import it.unibo.cs.v2.shared.MachineInfo;
 import it.unibo.cs.v2.shared.MachineProcessInfo;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 // TODO implement a timer that checks the server to see if the machine has changed in some way (new
 // accepted share?)
 
 public class MachinePanel extends HTMLPanel {
 	private final MachineInfo machineInfo;
 	
 	// HTML items
 	private final HTML descriptionHTML;
 	private final HTML basicHTML;
 	private final HTML storageHdaHTML;
 	private final HTML storageHdbHTML;
 	private final HTML virtuaClusterHTML;
 	private final HTML secondNetworkHTML;
 	private final Widget workingSharesHTML;
 	private final HTML pendingSharesHTML = new HTML();
 	private final HTML newLine = new HTML("<br />");
 	
 	// Share items
 	private final UsersSuggestBox usersBox = new UsersSuggestBox();
 	private Anchor addShare = new Anchor("Add");
 	
 	// ListBox for ISOs
 	private final ListBox bootFrom = new ListBox();
 	
 	// Checkbox to allow the user to open the Java Applet in a new Window
 	private final CheckBox newWindow = new CheckBox("Open in a new Window");
 	
 	// Checkbox to allow the user to set the -usbdevice tablet for this particular machine
 	private final CheckBox tabletDevice = new CheckBox("Set tablet device (-usbdevice tablet)");
 	
 	// Start/Delete machine buttons
 	private final HorizontalPanel startDeletePanel = new HorizontalPanel();
 	private final Button startButton = new Button("Start/View machine");
 	private final Button deleteButton = new Button("Delete machine");
 	
 	// Proxies
 	private final StartMachineAsync startMachineProxy = GWT.create(StartMachine.class);
 	private final DeleteMachineAsync deleteMachineProxy = GWT.create(DeleteMachine.class);
 	private final ShareMachineAsync shareMachineProxy = GWT.create(ShareMachine.class);
 	private final RemoveShareAsync removeShareProxy = GWT.create(RemoveShare.class);
 	
 	// AsyncCallback for addShareButton
 	private final AsyncCallback<Boolean> addShareCallback = new AsyncCallback<Boolean>() {
 
 		@Override
 		public void onFailure(Throwable caught) {
 			Window.alert(caught.getMessage());
 		}
 
 		@Override
 		public void onSuccess(Boolean result) {
 			if (result) {
 				machineInfo.addPendingShare(usersBox.getText());
 				usersBox.setText("");
 				refreshPendingShares();
 				// TODO save machine status on the server
 			}
 			else
 				usersBox.setText("");
 		}
 	};
 	
 	// Handler for startButton
 	private final ClickHandler startButtonClickHandler = new ClickHandler() {
 		
 		@Override
 		public void onClick(ClickEvent event) {
 			startButton.setEnabled(false);
 			machineInfo.setBootCdrom(bootFrom.getValue(bootFrom.getSelectedIndex()).equals("d"));
 			machineInfo.setTabletDevice(tabletDevice.getValue());
 			final Image loadingGif = new Image("loading.gif");
 			add(loadingGif);
 			
 			startMachineProxy.startViewMachine(machineInfo, new AsyncCallback<MachineProcessInfo>() {
 
 				@Override
 				public void onFailure(Throwable caught) {
 					Window.alert(caught.getMessage());
 					remove(loadingGif);
 					startButton.setEnabled(true);
 				}
 
 				@Override
 				public void onSuccess(MachineProcessInfo result) {
 					MainPage.getInstance().showApplet(result, newWindow.getValue());
 					startButton.setEnabled(true);
 					remove(loadingGif);
 				}
 			});
 		}
 	};
 	
 	// Handler for deleteButton
 	// TODO handle this event in a cleaner way
 	private final ClickHandler deleteButtonClickHandler = new ClickHandler() {
 		
 		@Override
 		public void onClick(ClickEvent event) {
 			final FlozDialogBox confirmDelete = new FlozDialogBox("Delete " + machineInfo.getName() + "?");
 			final Button okButton = new Button("Yes, do it!");
 			final Button cancelButton = new Button("No please! Don't!");
 			confirmDelete.add(new HTML("<h3>Confirmation box</h3>Are you sure you want to delete " + machineInfo.getName() + "?"));
 			
 			okButton.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					deleteMachineProxy.deleteMachine(machineInfo.getName(), new AsyncCallback<Boolean>() {
 
 						@Override
 						public void onFailure(Throwable caught) {
 							Window.alert(caught.getMessage());
 							confirmDelete.hide();
 						}
 
 						@Override
 						public void onSuccess(Boolean result) {
 							clear();
 							add(new HTML("<span style=\"color: green\">Machine deleted successfully!</span>"));
 							confirmDelete.hide();
 						}
 					});
 					
 				}
 			});
 			
 			cancelButton.addClickHandler(new ClickHandler() {
 				
 				@Override
 				public void onClick(ClickEvent event) {
 					confirmDelete.hide();
 				}
 			});
 			
 			confirmDelete.addButton(okButton);
 			confirmDelete.addButton(cancelButton);
 			
 			confirmDelete.showRelativeTo(descriptionHTML);
 		}
 	};
 	
 	// Handler for addShare Anchor
 	private final ClickHandler addShareClickHandler = new ClickHandler() {
 
 		@Override
 		public void onClick(ClickEvent event) {
 			if (usersBox.getText().equals(""))
 				return;
 			
 			if (machineInfo.getPendingShares().length > 0) {
 				for (String user : machineInfo.getPendingShares())
 					if (user.equals(usersBox.getText())) {
 						usersBox.setText("");
 						return;
 					}
 			}
 			
 			if (machineInfo.getShares().length > 0) {
 				for (String user : machineInfo.getShares())
 					if (user.equals(usersBox.getText())) {
 						usersBox.setText("");
 						return;
 					}
 			}
 			
 			shareMachineProxy.shareMachine(machineInfo, usersBox.getText(), addShareCallback);
 		}
 		
 	};
 
 	public MachinePanel(final MachineInfo machineInfo, boolean belongs) {
 		super("<b>Description</b>");
 		this.machineInfo = machineInfo;
 		
 		// Setup the objects before building the main HTMLPanel
 		bootFrom.addItem("Hard Disk", "c");
 		bootFrom.addItem("CD-Rom", "d");
 		
 		startButton.addClickHandler(startButtonClickHandler);
 		deleteButton.addClickHandler(deleteButtonClickHandler);
 		addShare.addClickHandler(addShareClickHandler);
 		
 		startDeletePanel.add(startButton);
 		startDeletePanel.add(deleteButton);
 		
 		descriptionHTML = new HTML(machineInfo.getDescription());
 		basicHTML = new HTML("&nbsp;&nbsp;CD-ROM: " + machineInfo.getIso());
 		storageHdaHTML = new HTML("&nbsp;&nbsp;HDA: " + machineInfo.getHda() + " (" + machineInfo.getHdaSize() + ")");
 		storageHdbHTML = new HTML("&nbsp;&nbsp;HDB: " + (machineInfo.isHdbEnabled()? machineInfo.getHdb() + " (" + machineInfo.getHdbSize() + ")" : "not enabled"));
 		virtuaClusterHTML = new HTML("&nbsp;&nbsp;VirtuaCluster support is <b>" + (machineInfo.isVirtuacluster()? "enabled" : "disabled") + "</b>");
 		secondNetworkHTML = new HTML("&nbsp;&nbsp;Secondary network: " + 
 				(machineInfo.isSecondNetwork()? machineInfo.getSocketPath() + " (" + machineInfo.getMacAddress() + ")"  : 
 					"not enabled"));
 		
 		// get the shares
 		if (belongs) {
 			refreshPendingShares();
 			
 			if (machineInfo.getShares().length < 1)
 				workingSharesHTML = new HTML("&nbsp;&nbsp;No accepted shares<br />");
 			
 			else {
 				String[] shares = machineInfo.getShares();
 				
 				workingSharesHTML = new FlexTable();
 				int row = 0;
 				
 				for (final String s : shares) {
 					final Anchor removeShare = new Anchor("Remove");
 					final int currentRow = row;
 					
 					removeShare.addClickHandler(new ClickHandler() {
 						
 						@Override
 						public void onClick(ClickEvent event) {
 							final Image loadingGif = new Image("loading.gif");
 							
 							((FlexTable) workingSharesHTML).remove(removeShare);
 							((FlexTable) workingSharesHTML).setWidget(currentRow, 1, loadingGif);
 							
 							removeShareProxy.removeShare(machineInfo, s, new AsyncCallback<Boolean>() {
 
 								@Override
 								public void onFailure(Throwable caught) {
 									Window.alert(caught.getMessage());
 								}
 
 								@Override
 								public void onSuccess(Boolean result) {
 									if (result) {
 										Window.alert("Share with " + s + " successfully removed. Refresh the machines' list to see the changes.");
 										((FlexTable) workingSharesHTML).remove(loadingGif);
 										((FlexTable) workingSharesHTML).setWidget(currentRow, 1, new HTML("<b>No longer sharing</b>"));
 										
 									}
 								}
 							});
 						}
 					});
 					
 					((FlexTable) workingSharesHTML).setWidget(row, 0, new HTML(s + "&nbsp;&nbsp;"));
 					((FlexTable) workingSharesHTML).setWidget(row, 1, removeShare);
 				
 					row++;
 				}
 			}
 		}
 		
 		else {
 			pendingSharesHTML.setHTML("&nbsp;&nbsp;This machine belongs to: <b>" + machineInfo.getRealOwner() + "</b>");
 			workingSharesHTML = new HTML("&nbsp;&nbsp;This machine belongs to: <b>" + machineInfo.getRealOwner() + "</b>");
 		}
 		
 		// Build up the HTML
 		add(descriptionHTML);
 		add(newLine);
 		
 		add(new HTML("<br /><b>Basic settings</b><br />"));
 		add(basicHTML);
 		add(newLine);
 		
 		add(new HTML("<br /><b>Storage</b><br />"));
 		add(storageHdaHTML);
 		add(newLine);
 		add(storageHdbHTML);
 		add(newLine);
 		
 		add(new HTML("<br /><b>Networking</b><br />"));
 		add(virtuaClusterHTML);
 		add(newLine);
 		add(secondNetworkHTML);
 		add(newLine);
 		
 		add(new HTML("<br /><b>Pending Shares</b><br />"));
 		add(pendingSharesHTML);
 		add(newLine);
 		
 		add(new HTML("<br /><b>Accepted Shares</b><br />"));
 		add(workingSharesHTML);
 		add(newLine);
 		if (belongs) {
 			add(new HTML("<br /><b>Add a new share</b><br />"));
 			add(usersBox);
 			add(addShare);
 		}
 		
 		if (belongs) {
 			add(new HTML("<br /><b>Boot order</b><br />"));
 			add(bootFrom);
 			add(newLine);
 		}
 		
 		add(new HTML("<br /><b>Command</b></br />"));
 		tabletDevice.setValue(true);
 		add(tabletDevice);
		add(newLine);
 		add(newWindow);
 		add(startDeletePanel);
 	}
 	
 	private void refreshPendingShares() {
 		if (machineInfo.getPendingShares().length < 1)
 			pendingSharesHTML.setHTML("&nbsp;&nbsp;No pending shares");
 		
 		else {
 			String[] shares = machineInfo.getPendingShares();
 			String html = "";
 			for (String s : shares)
 				html += "&nbsp;&nbsp;" + s + "<br />";
 			pendingSharesHTML.setHTML(html);
 		}
 	}
 }
 
