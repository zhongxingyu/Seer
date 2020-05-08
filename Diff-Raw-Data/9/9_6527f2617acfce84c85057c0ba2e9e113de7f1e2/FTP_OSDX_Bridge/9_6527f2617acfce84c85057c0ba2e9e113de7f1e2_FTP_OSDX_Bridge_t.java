 package org.fnppl.opensdx.ftp_bridge;
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
 import java.awt.BorderLayout;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import org.fnppl.opensdx.file_transfer.CommandResponseListener;
 import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferDeleteCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferDownloadStreamCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferListCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferMkDirCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferRenameCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferUploadCommand;
 import org.fnppl.opensdx.file_transfer.model.RemoteFile;
 import org.fnppl.opensdx.file_transfer.model.Transfer;
 import org.fnppl.opensdx.helper.QueueWaiting;
 import org.fnppl.opensdx.security.OSDXKey;
 import org.fnppl.opensdx.xml.Document;
 import org.fnppl.opensdx.xml.Element;
 
 
 public class FTP_OSDX_Bridge implements CommandResponseListener {
 
	public final static String VERSION = "v.2012-07-20";
 	
 	private File configFile = new File("ftp_bridge_config.xml"); 
 	private File alterConfigFile = new File("src/org/fnppl/opensdx/ftp_bridge/resources/ftp_bridge_config.xml"); 
 
 	private int port = 2221;
 	private HashMap<String, FTP_OSDX_BridgeUser> users = new HashMap<String, FTP_OSDX_BridgeUser>();
 
 
 	public FTP_OSDX_Bridge() {
 		readConfig();
 	}
 
 	public void readConfig() {
 		users = new HashMap<String, FTP_OSDX_BridgeUser>();
 		try {
 			if (!configFile.exists()) {
 				configFile = alterConfigFile;
 			}
 			if (!configFile.exists()) {
 				System.out.println("Sorry, ftp_bridge_config.xml not found.");
 				System.exit(0);
 			}
 			Element root = Document.fromFile(configFile).getRootElement();
 			String sPort  = root.getChildText("ftp_server_port");
 			if (sPort!=null) {
 				try {
 					port = Integer.parseInt(sPort); 
 				} catch (Exception ex) {
 					System.out.println("error in config: could not parse ftp_server_port: "+sPort);
 				}
 			}
 
 			Vector<Element> userConfig =  root.getChildren("user");
 			if (userConfig == null) return;
 
 			for (Element e : userConfig) {
 				try {
 					String ftp_username = e.getChildTextNN("ftp_username"); 
 					String ftp_password = e.getChildTextNN("ftp_password");
 					OSDXKey mysigning = OSDXKey.fromElement(e.getChild("keypair"));
 					mysigning.unlockPrivateKey(e.getChildTextNN("password"));
 					String username = e.getChildTextNN("username");
 
 					FTP_OSDX_BridgeUser c = new FTP_OSDX_BridgeUser();
 					c.ftpusername = ftp_username;
 					c.ftppassword = ftp_password;
 					c.host = e.getChildTextNN("host");
 					c.port = Integer.parseInt(e.getChildTextNN("port"));
 					c.prepath = e.getChildTextNN("prepath");
 					c.signingKey = mysigning;
 					c.username = username;
 					System.out.println("adding user: "+ftp_username+" -> "+username+"::"+mysigning.getKeyID());
 					users.put(ftp_username, c);
 				} catch (Exception exIn) {
 					exIn.printStackTrace();
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 
 	public void startService() {
 		try {
 			System.out.println("Starting FTP-to-OSDXFiletransfer-Bridge "+VERSION+" on port "+port);
 
 			osdxclient = new OSDXFileTransferClient();
 			osdxclient.addResponseListener(this);
 			try {
 				if (users.isEmpty()) {
 					System.out.println("No osdx user configured. Please check your config file.");
 					return;
 				}
 				user = users.values().iterator().next();
 				osdxclient.connect(user.host, user.port, user.prepath, user.signingKey, user.username);	
 			} catch (Exception e) {
 				System.out.println("Error connecting to osdx fileserver. Please check your config file.");
 			}
 			uploadThread = new Thread() {
 				public void run() {
 					while (true) {
 						Transfer t = waitingUploads.get();
 						if (t==null) {
 							System.out.println("Upload Thread: NULL");
 							return; //queue of waiting uploads has stopped
 						}
 						System.out.println("Upload Thread: "+t.originalFilename);
 						try {
 							long id = osdxclient.upload(t.file, t.originalFilename);
 							transfersInProgress.put(id, t);
 						} catch (Exception ex)  {
 							ex.printStackTrace();
 						}
 					}
 				}
 			};
 			uploadThread.start();
 
 
 			ServerSocket s = new ServerSocket(port);
 			while(true) {
 				Socket incoming = s.accept();
 				new FTP_OSDX_BridgeThread(incoming, this).start();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public FTP_OSDX_BridgeUser getUser(String username) {
 		return users.get(username);
 	}
 
 	public static void main(String[] args) {
 		new FTP_OSDX_Bridge().startService();
 	}
 
 
 	public OSDXFileTransferClient osdxclient = null;
 	public FTP_OSDX_BridgeUser user = null;
 	public PrintWriter lastOut = null;
 	
 	public HashMap<Long,Transfer> transfersInProgress = new HashMap<Long, Transfer>();
 	public HashMap<Long,FTP_OSDX_BridgeThread> commandThread = new HashMap<Long, FTP_OSDX_BridgeThread>();
 
 
 	public String pwd = "/";
 	public QueueWaiting<Transfer> waitingUploads = new QueueWaiting<Transfer>();
 	private Thread uploadThread = null;
 	
 
 
 	public void onSuccess(OSDXFileTransferCommand command) {
 		
 		//System.out.println("Command "+command.getID()+" successful :: "+command.getClass().getSimpleName());
 		Transfer transfer = transfersInProgress.get(command.getID());
 		FTP_OSDX_BridgeThread thread = commandThread.get(command.getID());
 		
 		//if (thread==null) {
 		//	System.out.println("NO THREAD TO COMMAND "+command.getID()+" FOUND");
 		//}
 		//if (transfer==null) {
 		//		System.out.println("no transfer type");
 		//}
 		if (transfer!=null) {
 			if (transfer.type.equals("upload")) {
 				if (command instanceof OSDXFileTransferUploadCommand) {
 
 					System.out.println("upload succeeded\n");
 					waitingUploads.readyForNext();
 					transfersInProgress.remove(command.getID());
 					updateFileTransfers();
 
 				}
 			} else {
 				//download
 				if (thread!=null) {
 //					try {
 //						File tmpFile = transfer.file;
 //						if (tmpFile.exists()) {
 //							thread.out.println("150 Binary data connection");
 //							
 //							//transfer downloaded file to ftp client
 //							FileInputStream fin = new FileInputStream(tmpFile);
 //	
 //							Socket t = thread.getDataSocket();
 //							if (t==null) {
 //								return;
 //							}
 //							OutputStream out2 = t.getOutputStream();
 //							byte buffer[] = new byte[1024];
 //							int read;
 //							try {
 //								while ((read = fin.read(buffer)) != -1) {
 //									out2.write(buffer, 0, read);
 //								}
 //								out2.close();
 //								thread.out.println("226 transfer complete");
 //								fin.close();
 //								tmpFile.delete();
 //								t.close();
 //							} catch (IOException e) {
 //								e.printStackTrace();
 //							}
 //						} else {
 //							thread.out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
 //						}
 //					} catch (Exception ex) {
 //						ex.printStackTrace();
 //					}
 					thread.out.println("226 transfer complete");
 				} else {
 					System.out.println("Connection to FTP Client lost");
 				}
 			}
 		} else {
 			if (command instanceof OSDXFileTransferDownloadStreamCommand) {
 				System.out.println("streamed download succeeded.");
 				if (thread!=null) {
 					thread.out.println("226 transfer complete");
 				} else {
 					System.out.println("Connection to FTP Client lost");
 				}
 			}
 			else if (command instanceof OSDXFileTransferDeleteCommand) {
 				if (command.getID() == thread.lastRMDCommandID) {
 					thread.out.println("250 RMD command succesful");
 				} else {
 					thread.out.println("250 DELE command succesful");
 				}
 			}
 			else if (command instanceof OSDXFileTransferRenameCommand) {
 				thread.out.println("250 Rename command succesful");
 			}
 			else if (command instanceof OSDXFileTransferListCommand) {
 				try {
 					thread.out.println("150 ASCII data");
 					Socket t = thread.getDataSocket();
 					if (t==null) {
 						return;
 					}
 					PrintWriter out2 = new PrintWriter(t.getOutputStream(),	true);
 					Vector<RemoteFile> list = ((OSDXFileTransferListCommand)command).getList();
 					for (RemoteFile f : list) {
 						//System.out.println("LIST::"+f.getName());
 						String di;
 						if (f.isDirectory()) {
 							di = "drwxr-xr-x ";
 						} else {
 							di = "-rw-r--r--";
 						}
 						String name = f.getName();
 						//name = name.replace(' ', '_');
 						String e = di+"1 user group "+f.getLength()+" Jul 04 20:00 "+name+"";
 						out2.println(e);
 					}
 					t.close();
 					thread.out.println("226 transfer complete");
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 
 			}
 			else if (command instanceof OSDXFileTransferMkDirCommand) {
 				thread.out.println("250 MKD command succesful");
 			}
 		}
 		
 		//command not longer needed in commandThread map
 		commandThread.remove(command.getID());
 	}
 
 	public void onError(OSDXFileTransferCommand command, String msg) {
 		System.out.println("ERROR: "+msg);
 		
 		Transfer transfer = transfersInProgress.get(command.getID());
 		FTP_OSDX_BridgeThread thread = commandThread.get(command.getID());
 		
 		if (transfer!=null) {
 			if (transfer.type.equals("upload")) {
 				if (command instanceof OSDXFileTransferUploadCommand) {
 
 					System.out.println("ERROR upload failed!!!\n");
 					waitingUploads.readyForNext();
 					transfersInProgress.remove(command.getID());
 					updateFileTransfers();
 
 				}
 			}
 		}
 		else if (command instanceof OSDXFileTransferDownloadStreamCommand) {
 			System.out.println("streamed download failed.");
 			if (thread!=null) {
 				thread.out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
 			} else {
 				System.out.println("Connection to FTP Client lost");
 			}
 		}
 		else {
 			if (thread!=null) thread.out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
 		}
 		
 		
 		
 		//		if (command instanceof OSDXFileTransferListCommand) {
 		//			out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
 		//		}
 		//		else if (command instanceof OSDXFileTransferMkDirCommand) {
 		//			out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
 		//		}
 		//		if (command instanceof OSDXFileTransferRenameCommand) {
 		//			out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
 		//		}
 		//		else {
 		
 		//		}
 	}
 
 	public synchronized void onStatusUpdate(OSDXFileTransferCommand command, long progress,long maxProgress, String msg) {
 		Transfer transfer = transfersInProgress.get(command.getID());
 		if (transfer!=null) {
 			if (transfer.type.equals("upload")) {
 				transfer.pos = (int)progress;
 				transfer.fileLen = maxProgress;
 				//System.out.println("STATUS UPDATE: "+transfer.file.getName()+" :: "+progress+" of "+maxProgress);
 				updateFileTransfers();
 			}
 		}
 	}
 
 //	private JFrame progressWnd = null;
 //	private JTable tableProgress = null;
 //	private synchronized void updateFileTransfers() {
 //		if (progressWnd==null) {
 //			progressWnd = new JFrame("FTP to OSDX Bridge :: Uploads");
 //			progressWnd.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 //			progressWnd.setSize(400, 400);
 //
 //			tableProgress = new JTable();
 //			progressWnd.setLayout(new BorderLayout());
 //			progressWnd.add("Center",new JScrollPane(tableProgress));
 //			progressWnd.setAlwaysOnTop(true);
 //			progressWnd.setVisible(true);
 //		}
 //
 //		
 //		Collection<Transfer> trans = transfersInProgress.values(); 
 //		int countInProgress = trans.size();
 //		List<Transfer> transWaiting = waitingUploads.list();
 //		int countWaiting = transWaiting.size();
 //		int count = countInProgress + countWaiting;
 //		String[] head = new String[] {"filename","transferred","length"};
 //		String[][] data = new String[count][3];
 //		int row = 0;
 //		for (Transfer t : trans) {
 //			data[row][0] = t.originalFilename;
 //			//data[row][1] = t.type;
 //			data[row][1] = (t.pos/1024)+" kB";
 //			data[row][2] = (t.fileLen/1024)+" kB";
 //			row++;
 //		}
 //		for (Transfer t : transWaiting) {
 //			data[row][0] = t.originalFilename;
 //			//data[row][1] = t.type;
 //			data[row][1] = " waiting...";
 //			data[row][2] = (t.fileLen/1024)+" kB";
 //			row++;
 //		}
 //		tableProgress.setModel(new DefaultTableModel(data, head));		
 //	}
 
 	private long minUpdateDuration = 5000; //update view every 5 seconds or when upload finished
 	private long lastProgressTime = 0;
 	private long lastProgress = -1L;
 	private synchronized void updateFileTransfers() {
 		Collection<Transfer> trans = transfersInProgress.values(); 
 		int countInProgress = trans.size();
 		int countWaiting = waitingUploads.countWaiting();
 		long now = System.currentTimeMillis();
 	
 		for (Transfer t : trans) {
 			if (lastProgressTime + minUpdateDuration < now || t.pos == t.fileLen) {
 				String speed = "";
 				if (lastProgress>0 && (now-lastProgressTime)>0) {
 					long bytePerMS = (t.pos-lastProgress)/(now-lastProgressTime);
 					speed = "\t"+bytePerMS+" kB/s"; // byte/ms nearly kB/s
 				}
 				lastProgressTime = now;
 				lastProgress = t.pos;
 				if (t.pos == t.fileLen) {
 					lastProgress = -1L;
 				}
 				if ("upload".equals(t.type)) {
 					String msg = "Upload progress "+t.originalFilename
 								+ "\t"+(t.pos/1024)+" kB of "+(t.fileLen/1024)+" kB"+speed+"\t\t "
 								+countWaiting+" file(s) in queue" ;
 					System.out.println(msg);
 				}
 			}
 		}
 	}
 	
 }
