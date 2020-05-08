 package net.bioclipse.xws4j;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 import net.bioclipse.xws4j.actions.ActionPulldown;
 import net.bioclipse.xws4j.preferences.PreferenceConstants;
 import net.bioclipse.xws4j.exceptions.Xws4jException;
 import net.bioclipse.xws.client.Client;
 import net.bioclipse.xws.client.IExecutionPipe;
 import net.bioclipse.xws.client.listeners.IConnectionListener;
 import net.bioclipse.xws.XwsLogger;
 import net.bioclipse.xws4j.views.servicediscovery.ServiceDiscoveryView;
 /**
  * 
  * This file is part of the Bioclipse xws4j Plug-in.
  * 
  * Copyright (C) 2008 Johannes Wagener
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * @author Johannes Wagener
  */
 public class DefaultClientCurator {
         private Client default_client = null;
         private String clientJID = "", pwd = "", server = "", server_port = "";
         
         private IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
         public DefaultClientCurator() {
                 // setup xws4j log pipe
                 XwsLogPipe log_pipe = new XwsLogPipe();
                 XwsLogger.setLogPipe(log_pipe);
         }
         private boolean isAccountDataSet() {
                 if (preferences != null) {
                         if (preferences.getString(PreferenceConstants.P_STRING_SERVER).length() > 0 &&
                                 preferences.getString(PreferenceConstants.P_STRING_JID).length() > 0 &&
                                 preferences.getString(PreferenceConstants.P_STRING_PASSWORD).length() > 0 &&
                                 preferences.getString(PreferenceConstants.P_STRING_RESOURCE).length() > 0 &&
                                 preferences.getString(PreferenceConstants.P_STRING_SERVERPORT).length() > 0)
                         return true;
                 }
                 return false;
         }
         private boolean isAccountDataChanged() {
             if (preferences != null) {
                     if (server.equals(preferences.getString(PreferenceConstants.P_STRING_SERVER)) &&
                             clientJID.equals(preferences.getString(PreferenceConstants.P_STRING_JID) +
                                     "/" +
                                     preferences.getString(PreferenceConstants.P_STRING_RESOURCE)) &&
                             pwd.equals(preferences.getString(PreferenceConstants.P_STRING_PASSWORD)) &&
                             server_port.equals(preferences.getString(PreferenceConstants.P_STRING_SERVERPORT)))
                     return false;
             }
             return true;
         }
         protected void stop() {
                 // disconnect!
                 disconnectClient();		 
         }
         public Client getDefaultClient() throws Xws4jException {
                 // if there is already a connected client return this
                 if (default_client != null && !isAccountDataChanged()) {
                         return default_client;
                 }
                 // create new client with (new) default data
                 if (!isAccountDataSet()) {
                         XwsConsole.writeToConsoleBlueT("Could not create default client:" +
                                        " the plug-in has set invalide account data in preferences.");
                         throw new Xws4jException("Could not create default client:" +
                                        " the plug-in has set invalide account data in preferences.");
                 }
                 clientJID = preferences.getString(PreferenceConstants.P_STRING_JID) +
                                                         "/" +
                                                         preferences.getString(PreferenceConstants.P_STRING_RESOURCE);
                 pwd = preferences.getString(PreferenceConstants.P_STRING_PASSWORD);
                 server = preferences.getString(PreferenceConstants.P_STRING_SERVER);
                 server_port = preferences.getString(PreferenceConstants.P_STRING_SERVERPORT);
                 
                 IExecutionPipe exec = new IExecutionPipe() {
                         public void exec(Runnable r) {
                                 // Eclipse specific code to inject Runnables in the GUI thread
                                 Display.getDefault().asyncExec(r);
                         }
                 };
                 try {
                         default_client = new Client(clientJID,
                                         pwd,
                                         server,
                                         Integer.parseInt(server_port),
                                         exec);
                 } catch (Exception e) {
                         XwsConsole.writeToConsoleBlueT("Could not create default client: " + e);
                         throw new Xws4jException("Could not create default client: " + e.getMessage());
                 }
                 XwsConsole.writeToConsoleBlueT("Created client: JID=" +
                                 clientJID + " Server=" + server + ":" + server_port);
                 // finally set a connection listener:
                 IConnectionListener listener = new IConnectionListener() {
                         public void onConnected() {
                                 XwsConsole.writeToConsoleBlueT("Default client connected.");
                                 ActionPulldown.setStatusConnected(true);
                                 ServiceDiscoveryView.setStatusConnected(true);
                         }			
                         public void onDisconnected() {
                                 XwsConsole.writeToConsoleBlueT("Default client disconnected.");
                                 ActionPulldown.setStatusConnected(false);
                                 ServiceDiscoveryView.setStatusConnected(false);
                         }
                 };
                 default_client.addConnectionListener(listener);
                 return default_client;
         }
         public boolean isClientConnected() {
                 if (default_client != null)
                         return default_client.isConnected();
                 return false;
         }
         public void connectClient(final boolean with_GUI_error)
         										throws Xws4jException {
                 final Client client = getDefaultClient();
                 Runnable r = new Runnable() {
                         public void run() {
                                 XwsConsole.writeToConsoleBlueT("Connecting default client ...");
                                 try {
                                         client.connect();
                                 } catch (Exception e) {
                                 	final String error_s = e.getLocalizedMessage();
                                 	XwsConsole.writeToConsoleBlueT("Could not connect default client: " + error_s);
                                     if (with_GUI_error) {
                                     	Runnable r = new Runnable() {
                                     	public void run() {
                                     		MessageDialog.openError(
                                     				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                 					"XMPP Error",
                                 					"Connecting to XMPP server failed." +
                                 					System.getProperty("line.separator") +
                                 					System.getProperty("line.separator") +
                                 					error_s);
 	                                    	}
 	                                    };
 	                                	Display.getDefault().asyncExec(r);
                                     }
                                 }
                         }
                 };
                 Thread connectThread = new Thread(r);
                 connectThread.start();
         }
         public void disconnectClient() {
                 if (default_client != null && default_client.isConnected()) {
                         final Client client = default_client;
                         Runnable r = new Runnable() {
                                 public void run() {
                                         XwsConsole.writeToConsoleBlueT("Disconnecting default client ...");
                                         try {
                                                 client.disconnect();
                                         } catch (Exception e) {
                                                 XwsConsole.writeToConsoleBlueT("Could not disconnect default client: " + e);
                                         }		
                                 }
                         };
                         Thread disconnectThread = new Thread(r);
                         disconnectThread.start();
                 }
         }
 }
