 package org.cirrus.tools.savemyapps;
 /**
  *	 This file is part of SaveMyApps
  *
  *   SaveMyApps is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   SaveMyApps is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with SaveMyApps.  If not, see <http://www.gnu.org/licenses/>.
  */
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import org.cirrus.mobi.savemyapps.shareddata.Command;
 import org.cirrus.mobi.savemyapps.shareddata.Response;
 
 import com.android.ddmlib.IDevice;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 
 public class ConnectionManager {
 
 	Logger logger = Logger.getLogger(ConnectionManager.class.getName());
 
 	public static final int STATE_CONNECTED = 1;
 	public static final int STATE_NOT_CONNECTED = 2;
 
 	private int connectionState = STATE_NOT_CONNECTED;
 
 	private ADBWrapper adbWrapper;
 	private IDevice currentDevice = null;
 	private Thread connectionThread = null;
 
 	private Gson gson = null;
 
 	private SaveMyAppsDaemon saveMyAppsDaemon;
 
 	public void init(SaveMyAppsDaemon saveMyAppsDaemon) {
		gson = new GsonBuilder().create();
 		this.saveMyAppsDaemon = saveMyAppsDaemon; 
 		this.adbWrapper = new ADBWrapper();
 		this.adbWrapper.init(this);
		
 	}
 
 	public void addDevice(IDevice device) {
 		logger.info("New device: "+device.getName());
 		//currently: first come, first server
 		if(this.currentDevice == null)
 		{
 			this.currentDevice = device;
 			this.adbWrapper.createPortForward(currentDevice);
 			this.createSocketConnection(currentDevice);
 		}
 	}
 
 	private void createSocketConnection(IDevice currentDevice) {
 		logger.info("Try to connect to: "+currentDevice.getName());
 		if(this.connectionThread == null)
 		{
 			this.connectionThread = new Thread(new Runnable() {
 				public void run() {
 					Socket socket = null;
 					PrintWriter out = null;
 					BufferedReader in = null;
 					Scanner sc = null;
 					try{
 						socket = new Socket("localhost", 7676);
 						socket.setKeepAlive(true);
 
 						out = new PrintWriter(socket.getOutputStream(), true);
 						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 						sc = new Scanner(in);
 						//logger.fine("Diag: connected: "+socket.isConnected()+ " closed: "+socket.isClosed() +" in: "+socket.isInputShutdown() +" out: "+socket.isOutputShutdown());
 						//read greeting, we do this to make sure that we really have a connection...
 						String greeting = sc.nextLine();
 						logger.fine("got greeting: "+greeting);
 						connectionState = STATE_CONNECTED;
 						saveMyAppsDaemon.setConnectionState(connectionState);
 						while(sc.hasNext())
 						{
 							String line = sc.nextLine();
							logger.info("recieved: "+line+ "gson: "+gson);;
							//we only get commands, do we? Deserialize from json							
 							Command cmd = gson.fromJson(line, Command.class);
 							//should we do the work in another thread? nope, let's keep it here for now
 							Response resp = doWork(cmd);
 							String json = gson.toJson(resp);
 							logger.fine("Response: "+json);
 							out.write(json);
 							out.flush();
 						}
 						//out.write("Hello from the client side!\n");
 
 					}
 					catch(Exception e)
 					{
 						e.printStackTrace();						
 					}
 					finally
 					{
 						connectionState = STATE_NOT_CONNECTED;
 						saveMyAppsDaemon.setConnectionState(connectionState);
 					}
 				}
 
 				private Response doWork(Command cmd) {
 					switch (cmd.command) {
 					case Command.COMMAND_BACKUP_PACKAGE:
 						return doBackups(cmd.params);
 					}
 					Response resp = new Response();
 					resp.responseCode = Response.RESPONSE_CMD_NOT_FOUND;
 					resp.message = "Could not find command: "+cmd.command;
 					return resp;
 				}
 
 				private Response doBackups(Map<String, String[]> params) {
 					String[] packages = params.get(Command.PARAM_BACKUP_PACKAGES);
 					Response result = new Response();
 					//try to backup each package
 					try {
 						adbWrapper.backupPackages(packages);
 						result.message="OK";
 						result.responseCode = Response.RESPONSE_OK;
 					}
 					catch(Exception e)
 					{
 						result.message = e.getMessage();
 						result.responseCode = Response.RESPONSE_ERROR;
 					}
 					return result;
 				}
 			});
 			this.connectionThread.start();
 		}
 	}
 
 	public void removeDevice(IDevice device) {
 		if(currentDevice != null && device.getName().equals(currentDevice.getName()))
 		{
 			//TODO: disconnect socket
 			currentDevice = null;
 		}
 	}
 
 	public void reconnect() {
 		// TODO Auto-generated method stub
 
 	}
 }
