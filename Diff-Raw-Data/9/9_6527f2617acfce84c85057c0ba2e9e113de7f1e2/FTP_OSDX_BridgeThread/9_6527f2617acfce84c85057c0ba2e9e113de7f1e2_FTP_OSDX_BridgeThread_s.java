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
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 
 import org.fnppl.opensdx.file_transfer.CommandResponseListener;
 import org.fnppl.opensdx.file_transfer.OSDXFileTransferClient;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferDeleteCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferListCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferMkDirCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferRenameCommand;
 import org.fnppl.opensdx.file_transfer.commands.OSDXFileTransferUploadCommand;
 import org.fnppl.opensdx.file_transfer.model.RemoteFile;
 import org.fnppl.opensdx.file_transfer.model.Transfer;
 import org.fnppl.opensdx.helper.QueueWaiting;
 import org.fnppl.opensdx.keyserver.helper.IdGenerator;
 
 public class FTP_OSDX_BridgeThread extends Thread {
 
 	private FTP_OSDX_Bridge control = null;
 	private String host;
 	
 	private boolean running;
 	
 	private Socket ftpSocket;
 	
 
 	private static final int MODE_NOT_SET = 0;
 	private static final int MODE_ACTIVE = 1;
 	private static final int MODE_PASSIVE = 2;
 	private int connectionMode = MODE_NOT_SET; 
 	private int next_port = -1;
 	private ServerSocket ftpPassiveDataServerSocket = null;
 	private Socket ftpPassiveDataSocket = null;
 	  
 	public PrintWriter out;
 	
 	
 	public FTP_OSDX_BridgeThread(Socket ftpsocket, FTP_OSDX_Bridge control) {
 		System.out.println("NEW FTP_OSDX_BridgeThread");
 		System.out.println("SOCKET: "+ftpsocket.getPort()+", "+ftpsocket.getLocalPort()+", "+ftpsocket.getInetAddress());
 		this.ftpSocket = ftpsocket;
 		this.control = control;
 	}
 	
 	
 	public void run() {
 		InetAddress inet;
 		try {
 			
 			inet = ftpSocket.getInetAddress();
 			host = inet.getHostName();
 			
 			System.out.println("host: "+host);
 			
 			//init connection
 			BufferedReader in = new BufferedReader(new InputStreamReader(ftpSocket.getInputStream()));
 			out = new PrintWriter(ftpSocket.getOutputStream(), true);
 			out.println("220 FTP Server ready.\r");
 
 			running = true;
 			
 			while (running) {
 				try {
 					String str = in.readLine();
 					if (str==null) {
 						running = false;
 					} else {
 						String command = str;
 						String param = null;
 						int ind = str.indexOf(' ');
 						if (ind>0) {
 							command = str.substring(0,ind);
 							if (str.length()>ind+1) {
 								param = str.substring(ind+1);
 							}
 						}
 						command = command.toUpperCase();
 						try {
 							Method commandHandler = getClass().getMethod("handle_"+command, String.class);
 							commandHandler.invoke(this, param);
 		
 						} catch (NoSuchMethodException ex) {
 							handle_command_not_implemented(str);
 						} catch (InvocationTargetException ex) {
 							handle_command_not_implemented(str);
 						} catch (IllegalAccessException ex) {
 							handle_command_not_implemented(str);
 						}
 					}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 					running = false;
 				}
 			}
 			ftpSocket.close();
 			//TODO osdxclient.closeConnection();
 		} catch (Exception e) { // System.out.println(e);
 			e.printStackTrace();
 		}
 	}
 	
 //	public boolean ensureConnection() {
 //		boolean connected = osdxclient.isConnected();
 //		if (!connected) {
 //			try {
 //				connected = osdxclient.connect(user.host, user.port, user.prepath, user.signingKey, user.username);
 //				
 //			} catch (Exception ex) {
 //				ex.printStackTrace();
 //				try {
 //					System.out.println("Closing connection to osdx server ...");	
 //					connected = false;
 //					osdxclient.closeConnection();
 //				} catch (Exception ex2) {
 //					ex2.printStackTrace();
 //				}				
 //			}
 //		}
 //		if (!connected) {
 //			System.out.println("ensureConnection -> false");
 //			out.println("426 Connection closed; transfer aborted.");
 //		}
 //		return connected;
 //	}
 	
 	public void handle_RETR(final String param) {
 
 		Thread t = new Thread() {
 			public void run() {
 				try {
 				//	if (ensureConnection()) {
 						//loading file data to tmp file
 						String filename = param;
 						if (!filename.startsWith("/")) {
 							if (control.pwd.equals("/")) {
 								filename = "/"+param;
 							} else {
 								filename = control.pwd+"/"+param;
 							}
 						}
 						
 						//download with stream
 						try {
 							System.out.println("downloading file: "+filename);
 							out.println("150 Binary data connection");
 							
 							Socket t = getDataSocket();
 							if (t==null) {
 								return;
 							}
 							BufferedOutputStream outData = new BufferedOutputStream(t.getOutputStream());
 							
 							long id = control.osdxclient.download(filename, outData);
 							control.commandThread.put(id, FTP_OSDX_BridgeThread.this);
 							
 						} catch (Exception ex) {
 							out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
 							ex.printStackTrace();
 						}
 						
 						
 						//download with temp file
 //						System.out.println("downloading file: "+filename);
 //						final File tmpFile = File.createTempFile("osdx"+System.currentTimeMillis(), ".tmp");
 //						tmpFile.delete();
 //						
 //						long id = control.osdxclient.download(filename, tmpFile);
 //						Transfer t = new Transfer();
 //						t.type = "download";
 //						t.file = tmpFile;
 //						
 //						control.transfersInProgress.put(id,t);
 //						control.commandThread.put(id,FTP_OSDX_BridgeThread.this);
 						
 						
 //						final FileTransferProgress progress = new FileTransferProgress(FileTransferProgress.END_UNKNOWN) {
 //							public void onUpdate() {
 //								super.onUpdate();
 //								if (hasFinished()) {
 //									try {
 //										if (tmpFile.exists() && tmpFile.length()==getProgressEnd()) {
 //											out.println("150 Binary data connection");
 //											
 //											//transfer downloaded file to ftp client
 //											FileInputStream fin = new FileInputStream(tmpFile);
 //											
 //											Socket t = new Socket(host, next_port);
 //											OutputStream out2 = t.getOutputStream();
 //											byte buffer[] = new byte[1024];
 //											int read;
 //											try {
 //												while ((read = fin.read(buffer)) != -1) {
 //													out2.write(buffer, 0, read);
 //												}
 //												out2.close();
 //												out.println("226 transfer complete");
 //												fin.close();
 //												tmpFile.delete();
 //												t.close();
 //											} catch (IOException e) {
 //												e.printStackTrace();
 //											}
 //										} else {
 //											out.println("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
 //										}
 //									} catch (Exception ex) {
 //										ex.printStackTrace();
 //									}
 //								}
 //							};
 //						};
 //						osdxclient.downloadFile(filename, tmpFile, progress);
 //						
 //					}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		};
 		t.start();
 	}
 	
 	public void handle_STOR(final String param) { //upload file to osdx server
 		Thread t = new Thread() {
 			public void run() {
 				try {
 					//if (ensureConnection()) {
 						String filename = removeDoubleSlashes(param);
 					
 						out.println("150 Binary data connection");
 						System.out.println("HANDLE_STOR :: "+filename);
 						
 						Socket t = getDataSocket();
 						if (t==null) return;
 						InputStream inFromFTP = t.getInputStream();
 						
 						File tmpFile = File.createTempFile("osdx"+System.currentTimeMillis(), ".tmp");
 						tmpFile.deleteOnExit();
 						FileOutputStream outFileBuffer = new FileOutputStream(tmpFile);
 						//String filename = param;
 						if (!filename.startsWith("/")) {
 							if (control.pwd.equals("/")) {
 								filename = "/"+filename;
 							} else {
 								filename = control.pwd+"/"+filename;
 							}
 						}
 						
 						byte buffer[] = new byte[1024];
 						int read;
 						try {
 							//write input stream from FTP to buffer file
 							while ((read = inFromFTP.read(buffer)) != -1) {
 								outFileBuffer.write(buffer, 0, read);
 							}
 							inFromFTP.close();
 							outFileBuffer.close();
 							t.close();
 							
 							//send ACK (caution: ack before transfer to OSDX Server ready
 							out.println("226 transfer complete");
 							
 							System.out.println("Queueing upload to OSDX Server of "+filename);
 							Transfer transfer = new Transfer();
 							transfer.type = "upload";
 							transfer.file = tmpFile;
 							transfer.fileLen = tmpFile.length();
 							transfer.originalFilename = filename;
 							control.waitingUploads.put(transfer);
 							
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					//}
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		};
 		t.start();
 	}
 		
 	
 	public void handle_TYPE(String str) {
 		out.println("200 type set");
 	}
 	
 	public void handle_SYST(String str) {
 		out.println("215 UNIX Type: L8");
 	}
 	
 	private String lastRenameFilename = null; 
 	public void handle_RNFR(String str) {
 		try {
 			String filename = str;
 			if (!filename.startsWith("/")) {
 				if (control.pwd.equals("/")) {
 					filename = "/"+str;
 				} else {
 					filename = control.pwd+"/"+str;
 				}
 			}
 			lastRenameFilename = filename;
 			out.println("350 Ready for destination name");
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			lastRenameFilename = null;
 			out.println("500 Error in RNFR command");
 		}
 	}
 	
 	public void handle_RNTO(String str) {
 		try {
 			if (lastRenameFilename!=null) {
 				String filename = str;
 				if (filename.contains("/")) {
 					filename = filename.substring(filename.lastIndexOf('/')+1);
 				}
 				System.out.println("Rename: "+lastRenameFilename+" -> "+filename);
 				long id = control.osdxclient.rename(lastRenameFilename, filename);
 				control.commandThread.put(id,this);
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			out.println("500 Error in RNTO command");
 		}
 	}
 	
 	public void handle_DELE(String str) {
 		try {
 			String filename = str;
 			if (!filename.startsWith("/")) {
 				if (control.pwd.equals("/")) {
 					filename = "/"+str;
 				} else {
 					filename = control.pwd+"/"+str;
 				}
 			}
 			System.out.println("Remove file: "+filename);
 			long id = control.osdxclient.delete(filename);
 			control.commandThread.put(id,this);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public long lastRMDCommandID = -1L;
 	public void handle_RMD(String str) {
 		try {
 			String filename = str;
 			if (!filename.startsWith("/")) {
 				if (control.pwd.equals("/")) {
 					filename = "/"+str;
 				} else {
 					filename = control.pwd+"/"+str;
 				}
 			}
 			lastRMDCommandID = control.osdxclient.delete(filename);
 			control.commandThread.put(lastRMDCommandID,this);
 			System.out.println("Remove dir: "+filename);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 	}
 	public void handle_CDUP(String str) {
 		try {
 			if (!control.pwd.equals("/")) {
 				int ind = control.pwd.lastIndexOf('/');
 				if (ind>0) {
 					control.pwd = control.pwd.substring(0,ind);
 				} else {
 					control.pwd="/";
 				}
 			}
 			System.out.println("PWD: "+control.pwd);
 			out.println("250 CWD command succesful");
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	public void handle_CWD(String param) {
 		try {
 				param = removeDoubleSlashes(param);
 				if (param.startsWith("/")) {
 					control.pwd = param;
 				} else {
 					if (control.pwd.equals("/")) {
 						control.pwd = "/"+param;
 					} else {
 						control.pwd = control.pwd+"/"+param;
 					}
 				}
 				if (control.pwd.endsWith("/") && control.pwd.length()>1) {
					control.pwd = control.pwd.substring(0,control.pwd.length()-2);
 				}
 				System.out.println("PWD after \"CWD "+param+"\" :: "+control.pwd);
 				out.println("250 CWD command succesful");
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	public void handle_QUIT(String param) {
 		//control.osdxclient.closeConnection();
 		//closePassiveSocket();
 		//running = false;
 		System.out.println("got QUIT command");
 		out.println("221 Goodbye");
 	}
 	public void handle_USER(String param) {
 		control.user = control.getUser(param);
 		out.println("331 Password");	
 	}
 	
 	public void handle_PASS(String param) {
 		if (control.user!=null && control.user.ftppassword.equals(param)) { //&& ensureConnection()) {
 			//try {
 				out.println("230 User " + control.user.ftpusername + " logged in.");
 				System.out.println("User "+control.user.ftpusername + " logged in.");
 			//} catch (Exception e) {
 			//	e.printStackTrace();
 			//	out.println("430 Error: cannot connect to given account");
 			//	System.out.println("Error: cannot connect to given account");
 			//}
 		} else {
 			out.println("430 Invalid username or password");
 			System.out.println("Invalid username or password");
 		}
 	}
 	
 	public void handle_PWD(String str) {
 		try {
 			out.println("257 \""+control.pwd+"\" is current directory");
 			System.out.println("PWD: "+control.pwd);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public void handle_MKD(String param) {
 		try {
 			if (param.startsWith("/")) {
 				long id = control.osdxclient.mkdir(param);
 				control.commandThread.put(id,this);
 				System.out.println("MKDIR: "+param);
 			} else {
 				if (control.pwd.equals("/")) {
 					long id = control.osdxclient.mkdir("/"+param);
 					control.commandThread.put(id,this);
 					System.out.println("MKDIR: /"+param);
 				} else {
 					long id = control.osdxclient.mkdir(control.pwd+"/"+param);
 					control.commandThread.put(id,this);
 					System.out.println("MKDIR: "+control.pwd+"/"+param);
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public void handle_SYS(String str) {
 		out.println("500 SYS not understood");
 	}
 	public void handle_PORT(String str) {
 		try {
 			out.println("200 PORT command successful");
 			int lng, lng2, lng1;
 			String a1="", a2="";
 			
 			lng = str.length() - 1;
 			lng2 = str.lastIndexOf(",");
 			lng1 = str.lastIndexOf(",", lng2 - 1);
 			for (int i = lng1 + 1; i < lng2; i++) {
 				a1 = a1 + str.charAt(i);
 			}
 			for (int i = lng2 + 1; i <= lng; i++) {
 				a2 = a2 + str.charAt(i);
 			}
 			int ip1 = Integer.parseInt(a1);
 			int ip2 = Integer.parseInt(a2);
 			next_port = ip1 * 16 * 16 + ip2;
 			connectionMode = MODE_ACTIVE;
 			closePassiveSocket();
 			//System.out.println("next port = "+next_port);	
 		} catch (Exception ex) {
 			connectionMode = MODE_NOT_SET;
 			ex.printStackTrace();
 		}
 	}
 	
 	// eg: EPRT |2|::1|36299|
 	public void handle_EPRT(String str) {
 		String[] part = null;
 		try {
 			String delimiter = str.substring(0,1);
 			part = str.split("["+delimiter+"]"); 
 			//part[1] = net-protocoll: 1 = IPv4, 2 = IPv6
 			//part[2] = net-address
 			//part[3] = tcp-port			
 			
 			next_port = Integer.parseInt(part[3]);
 			connectionMode = MODE_ACTIVE;
 			closePassiveSocket();
 			out.println("200 EPRT command successful");
 		} catch (Exception ex) {
 			connectionMode = MODE_NOT_SET;
 			ex.printStackTrace();
 			if (part!=null) {
 				System.out.println("EPRT parsing error: "+Arrays.toString(part));
 			}
 			out.println("500 EPRT command error");
 		}
 	}
 	//eg: EPSV |||6446|
 	public void handle_EPSV(String str) {
 		if (str==null) {
 			out.println("500 EPSV missing command parameter");
 			return;
 		}
 		String[] part = null;
 		try {
 			String delimiter = str.substring(0,1);
 			part = str.split("["+delimiter+"]"); 
 			//part[3] = tcp-port
 			
 			next_port = Integer.parseInt(part[3]);
 			connectionMode = MODE_ACTIVE;
 			closePassiveSocket();
 			out.println("200 EPSV command successful");
 		} catch (Exception ex) {
 			connectionMode = MODE_NOT_SET;
 			ex.printStackTrace();
 			if (part!=null) {
 				System.out.println("EPSV parsing error: "+Arrays.toString(part));
 			} else {
 				System.out.println("EPSV parsing error");
 			}
 			out.println("500 EPSV command error");
 			
 		}
 	}
 
 	public void handle_LIST(String param) {
 		try {
 			if (param==null || param.length()==0) {
 				long id = control.osdxclient.list(control.pwd,null);
 				control.commandThread.put(id,this);
 			}
 			else if (param.startsWith("/")) {
 				long id = control.osdxclient.list(param,null);
 				control.commandThread.put(id,this);
 			} else {
 				if (control.pwd.equals("/")) {
 					long id = control.osdxclient.list("/"+param,null);
 					control.commandThread.put(id,this);
 				} else {
 					long id = control.osdxclient.list(control.pwd+"/"+param,null);
 					control.commandThread.put(id,this);
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 //			try {
 //				System.out.println("Closing connection to osdx server ...");	
 //				osdxclient.closeConnection();
 //			} catch (Exception ex2) {
 //				ex2.printStackTrace();
 //			}
 //			out.println("426 Connection closed; transfer aborted.");
 		}
 	}
 	
 	public void handle_FEAT(String str) {
 		out.println("211-Features supported\n211 End");
 	}
 	
 	public void handle_NOOP(String str) {
 		out.println("200 NOOP command successful");
 	}
 	
 	public void handle_PASV(String str) {
 		//out.println("502 passive mode not implemented");
 		
 		if (ftpPassiveDataServerSocket==null) {
 			try {
 				ftpPassiveDataServerSocket = new ServerSocket(0); //0 = any free port
 			} catch (IOException e) {
 				ftpPassiveDataServerSocket = null;
 				e.printStackTrace();
 			}
 		}
 		if (ftpPassiveDataServerSocket==null) {
 			out.println("451 Requested action aborted: local error in processing");
 			return;
 		}
 		
 		try {
 			int port = ftpPassiveDataServerSocket.getLocalPort();
 			int p1 = port/256;
 			int p2 = port%256;
 			//next_port = p1 * 16 * 16 + p2;
 			connectionMode = MODE_PASSIVE;
 			out.println("227 Entering Passive Mode (127,0,0,1,"+p1+","+p2+")");
 			
 			if (ftpPassiveDataSocket!=null && ftpPassiveDataSocket.isConnected()) {
 				try {
 					ftpPassiveDataSocket.close();
 				} catch (IOException ex) {
 					ex.printStackTrace();
 				}
 			}
 			new Thread() {
 				public void run() {
 					try {
 						ftpPassiveDataSocket = ftpPassiveDataServerSocket.accept();
 					} catch (IOException ex) {
 						ftpPassiveDataServerSocket = null;
 						ftpPassiveDataSocket = null;
 						ex.printStackTrace();
 					}
 				}
 			}.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 	public void handle_command_not_implemented(String str) {
 		out.println("502 "+str+" not understood");
 		System.out.println("command: "+str+" not implemented");
 	}
 
 
 	public Socket getDataSocket() throws UnknownHostException, IOException {
 		if (connectionMode==MODE_ACTIVE && next_port>0) {
 			return new Socket(host, next_port); 
 		}
 		else if (connectionMode == MODE_PASSIVE && ftpPassiveDataSocket != null && ftpPassiveDataSocket.isConnected()) {
 			return ftpPassiveDataSocket;
 		}
 		out.println("425 Error: Can't open data connection");
 		return null;
 	}
 	
 	private void closePassiveSocket() {
 		if (ftpPassiveDataSocket !=null) {
 			try {
 				ftpPassiveDataSocket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			ftpPassiveDataSocket = null;
 		}
 		if (ftpPassiveDataServerSocket != null) {
 			try {
 				ftpPassiveDataServerSocket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			ftpPassiveDataServerSocket = null;
 		}
 	}
 	
 	private String removeDoubleSlashes(String text) {
 		while (text.contains("//")) {
 			text = text.replace("//", "/");
 		}
 		return text;
 	}
 }
