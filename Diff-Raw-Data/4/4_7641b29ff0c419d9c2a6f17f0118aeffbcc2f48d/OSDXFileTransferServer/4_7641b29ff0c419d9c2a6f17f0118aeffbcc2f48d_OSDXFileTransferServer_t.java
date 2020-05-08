 package org.fnppl.opensdx.file_transfer;
 /*
  * Copyright (C) 2010-2012 
  * 							fine people e.V. <opensdx@fnppl.org> 
  * 							Henning Thieß <ht@fnppl.org>
  * 
  * 							http://fnppl.org
  */
 
 /*
  * Software license
  *
  * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
  *  
  * This file is part of openSDX
  * openSDX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * openSDX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * and GNU General Public License along with openSDX.
  * If not, see <http://www.gnu.org/licenses/>.
  *      
  */
 
 /*
  * Documentation license
  * 
  * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
  * 
  * This file is part of openSDX.
  * Permission is granted to copy, distribute and/or modify this document 
  * under the terms of the GNU Free Documentation License, Version 1.3 
  * or any later version published by the Free Software Foundation; 
  * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
  * A copy of the license is included in the section entitled "GNU 
  * Free Documentation License" resp. in the file called "FDL.txt".
  * 
  */
 import java.io.BufferedReader;
 import java.io.Console;
 import java.io.File;
 import java.io.FileReader;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Vector;
 
 import org.fnppl.opensdx.file_transfer.helper.ClientSettings;
 import org.fnppl.opensdx.file_transfer.helper.FileTransferLog;
 import org.fnppl.opensdx.security.MasterKey;
 import org.fnppl.opensdx.security.OSDXKey;
 import org.fnppl.opensdx.xml.Document;
 import org.fnppl.opensdx.xml.Element;
 
 public class OSDXFileTransferServer {
 
 	private File configFile = new File("osdxserver_config.xml"); 
 	private File alterConfigFile = new File("src/org/fnppl/opensdx/file_transfer/resources/osdxfiletransferserver_config.xml");
 	
 	protected int port = 8899;
 	protected InetAddress address = null;
 	private int defaultMaxDirectoryDepth = -1;
 	
 	private OSDXKey mySigningKey = null;
 	private File clients_config_file = null;
 	private boolean backupClientsConfigOnUpdate = true;
 
 	
 	
 	//accessable for serverthreads
 	public FileTransferLog log = null;
 	private int maxByteLength = 4*1024*1024;
 	private HashMap<String, ClientSettings> clients = null; //client id := username::keyid
 	//private HashMap<OSDXSocketServerThread, FileTransferState> states = null;
 	
 	
 	public OSDXFileTransferServer(String pwS, File configFile) throws Exception {
		if(configFile!=null) {
			this.configFile = configFile;
		}
 		
 		readConfig();
 		if (mySigningKey!=null && !mySigningKey.isPrivateKeyUnlocked()) {
 			mySigningKey.unlockPrivateKey(pwS);
 		}
 		if (mySigningKey==null || !mySigningKey.hasPrivateKey() || !mySigningKey.isPrivateKeyUnlocked()) {
 			throw new Exception("signing key not accessable");
 		}
 	}
 	
 	public void readConfig() {
 		try {
 			if (!configFile.exists()) {
 				configFile = alterConfigFile;
 			}
 			if (!configFile.exists()) {
 				System.out.println("Sorry, "+configFile.getAbsolutePath()+" not found.");
 				System.exit(0);
 			}
 			Element root = Document.fromFile(configFile).getRootElement();
 
 			System.out.println("Using config-file: "+configFile.getAbsolutePath());
 			
 			//uploadserver base
 			Element ks = root.getChild("osdxfiletransferserver");
 			//			host = ks.getChildText("host");
 			port = ks.getChildInt("port");
 			String ip4 = ks.getChildText("ipv4");
 			try {
 				byte[] addr = new byte[4];
 				String[] sa = ip4.split("[.]");
 				for (int i=0;i<4;i++) {
 					int b = Integer.parseInt(sa[i]);
 					if (b>127) b = -256+b;
 					addr[i] = (byte)b;
 				}
 				address = InetAddress.getByAddress(addr);
 			} catch (Exception ex) {
 				System.out.println("CAUTION: error while parsing ip adress");
 				ex.printStackTrace();
 			}
 			
 			defaultMaxDirectoryDepth = ks.getChildInt("max_directory_depth");
 			if (defaultMaxDirectoryDepth == Integer.MIN_VALUE) {
 				defaultMaxDirectoryDepth = -1;
 			}
 			System.out.println("default max directory depth = "+defaultMaxDirectoryDepth);
 
 			String logFile = ks.getChildText("logfile");
 			if (logFile==null) {
 //				log = FileTransferLog.initNoLogging();
 				log = FileTransferLog.initTmpLog();
 			} else {
 				log = FileTransferLog.initLog(new File(logFile));
 			}
 
 			String extraClients = ks.getChildText("clients_config_file");
 			if (extraClients==null || extraClients.length()==0) {
 				clients_config_file = null;
 			} else {
 				clients_config_file = new File(extraClients);
 				new File(clients_config_file.getAbsolutePath()).getParentFile().mkdirs();
 				Element ecf = ks.getChild("clients_config_file");
 				String doBackup = ecf.getAttribute("backup");
 				if (doBackup!=null) {
 					try {
 						backupClientsConfigOnUpdate = Boolean.parseBoolean(doBackup);
 					} catch (Exception ex) {
 						backupClientsConfigOnUpdate = true;
 						ex.printStackTrace();
 					}
 				}
 			}
 
 			///Clients
 			clients = new HashMap<String, ClientSettings>();
 			//System.out.println("init clients");
 			Element eClients = root.getChild("clients");
 			Vector<Element> ecClients = eClients.getChildren("client");
 			for (Element e : ecClients) {
 				try {
 					Vector<ClientSettings> cs = ClientSettings.fromElement(e, defaultMaxDirectoryDepth);
 					for(int i=0; i<cs.size(); i++) {
 						ClientSettings c = cs.elementAt(i);
 						clients.put(c.getSettingsID(), c);
 						System.out.println("adding client: "+c.getSettingsID()+" -> "+c.getLocalRootPath().getAbsolutePath()+"\t max dir depth = "+c.getRightsAndDuties().getMaxDirectoryDepth());
 						if(i==0) {
 							c.getLocalRootPath().mkdirs();
 						}
 					}
 
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 
 			//clients from clients config file
 			if (clients_config_file!=null && clients_config_file.exists()) {
 				try {
 					eClients = Document.fromFile(clients_config_file).getRootElement();
 					ecClients = eClients.getChildren("client");
 					for (Element e : ecClients) {
 						try {
 //							ClientSettings cs = ClientSettings.fromElement(e, defaultMaxDirectoryDepth);
 //							clients.put(cs.getSettingsID(),cs);
 //							System.out.println("adding extra client: "+cs.getSettingsID()+" -> "+cs.getLocalRootPath().getAbsolutePath()+"\t max dir depth = "+cs.getRightsAndDuties().getMaxDirectoryDepth());							
 //							cs.getLocalRootPath().mkdirs();
 							
 							Vector<ClientSettings> cs = ClientSettings.fromElement(e, defaultMaxDirectoryDepth);
 							for(int i=0; i<cs.size(); i++) {
 								ClientSettings c = cs.elementAt(i);
 								clients.put(c.getSettingsID(), c);
 								System.out.println("adding extra client: "+c.getSettingsID()+" -> "+c.getLocalRootPath().getAbsolutePath()+"\t max dir depth = "+c.getRightsAndDuties().getMaxDirectoryDepth());
 								if(i==0) {
 									c.getLocalRootPath().mkdirs();
 								}
 							}
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 					}
 				} catch (Exception ex2) {
 					ex2.printStackTrace();
 				}
 			}
 
 			//SigningKey
 			try {
 				OSDXKey k = OSDXKey.fromElement(root.getChild("rootsigningkey").getChild("keypair"));
 				if (k instanceof MasterKey) {
 					mySigningKey = (MasterKey)k;					
 				} else {
 					System.out.println("ERROR: no master signing key in config.");	
 				}
 			} catch (Exception e) {
 				System.out.println("ERROR: no master signing key in config."); 
 			}
 			//TODO check localproofs and signatures 
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public ClientSettings getClientSetting(String userid_keyid) {
 		return clients.get(userid_keyid);
 	}
 	
 	public void startService() throws Exception {
 		ServerSocket so = new ServerSocket(port);
 		if (log!=null) log.logServerStart(address.getHostAddress(), port);
 		while (true) {
 			try {
 				final Socket socket = so.accept();
 				OSDXFileTransferServerThread t = new OSDXFileTransferServerThread(socket, mySigningKey,this);
 				t.start();	
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				Thread.sleep(250);// cooldown...
 			}
 		}
 	}
 	
 	
 	public static void main(String args[]) {
 		try {
 			if (args!=null && args.length==1 && args[0].equals("--makeconfig")) {
 				//makeConfig();
 				System.out.println("makeconfig not implemented");
 				return;
 			}
 
 			String pwS = null;
 			File configfile = null;
 			
 			if(args.length > 0 ) {
 				configfile = new File(args[0]);
 				if(!configfile.exists()) {
 					pwS = args[0];
 					configfile = null;
 				}
 			}
 			
 			if(pwS == null) {
 				File f = new File("osdxserver_pass.txt");
 				if(f.exists()) {
 					BufferedReader bin = new BufferedReader(new FileReader(f));
 					String zeile = bin.readLine();
 					pwS = zeile.trim();
 				}
 				else {
 					Console console = System.console();
 					pwS = console.readLine("Please enter password for unlocking private-key: ");
 				}
 			}
 			
 			
 			if(args.length > 1) {
 				configfile = new File(args[1]);	
 			}
 			
 			OSDXFileTransferServer server = new OSDXFileTransferServer(pwS, configfile);
 			server.startService();
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 }
