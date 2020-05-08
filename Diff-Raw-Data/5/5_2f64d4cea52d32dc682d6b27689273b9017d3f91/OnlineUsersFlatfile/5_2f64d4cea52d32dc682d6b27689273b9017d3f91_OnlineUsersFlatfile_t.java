 package com.wimbli.onlineusers;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class OnlineUsersFlatfile extends OnlineUsersDataSource {
 
 	protected static final Logger log = Logger.getLogger("Minecraft");
 	public static final int OFFLINE = 0;
 	public static final int ONLINE  = 1;
 
 	@Override
 	public synchronized boolean init() {
 		return (initFile(OnlineUsers.directory+OnlineUsers.flatfile) && initFile(OnlineUsers.directory+OnlineUsers.flatfileData) && initTemplateFile());
 	}
 	
 	private boolean initFile(String filename) {
 		try {
 			File f = new File(filename);
 			if (!f.exists()) {
 				f.createNewFile();
 			}
 			File f2 = new File(filename);
 			if (f2.exists()) {
 				return true;
 			}
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 		}
 		return false;
 	}
 
 	private boolean initTemplateFile() {
 		String location = OnlineUsers.directory+OnlineUsers.flatfileTemplate;
 		if (!new File(location).exists()) {
 			FileWriter writer = null;
 			try {
 				writer = new FileWriter(location);
 				writer.write("#This is the template file for OnlineUsers\r\n");
 				writer.write("#Replacement Values Are:\r\n");
 				writer.write("#{username}, {timestamp}, {shorttime}, {longtime}, {online-true-false}, {online-offline}, {online-0-1}\r\n");
 				writer.write("#Be sure to leave the #beginusers and #endusers lines in!\r\n");
 				writer.write("Online Users Are:\r\n");
 				writer.write("#beginusers\r\n");
 				writer.write("{username} last logged on at {longtime}\r\n");
 				writer.write("#endusers\r\n");
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Exception while creating " + location, e);
 			} finally {
 				try {
 					if (writer != null) {
 						writer.close();
 						return true;
 					}
 				} catch (IOException e) {
 					log.log(Level.SEVERE, "Exception while closing writer for "	+ location, e);
 				}
 			}
 		} else {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public synchronized boolean addUser(String username) {
 		removeUser(username);
 		BufferedWriter bw = null;
 		try {
 			bw = new BufferedWriter(new FileWriter(OnlineUsers.directory+OnlineUsers.flatfileData, true));
 			bw.newLine();
 			bw.append(username+":"+System.currentTimeMillis()+":"+String.valueOf(ONLINE));
 		} catch (Exception e2) {
 			log.log(Level.SEVERE, "Exception while writing new user to " + OnlineUsers.directory+OnlineUsers.flatfileData, e2);
 		} finally {
 			try {
 				if (bw != null) {
 					bw.close();
 					return regenFlatFile();
 				}
 			} catch (IOException ex) {
 			}
 		}
 		return false;
 	}	
 
 	@Override
 	public synchronized boolean removeUser(String username) {
 		FileWriter writer = null;
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(new File(OnlineUsers.directory+OnlineUsers.flatfileData)));
 			String line = "";
 			StringBuilder toSave = new StringBuilder();
 			while ((line = reader.readLine()) != null) {
 				if (line.contains(":")) {
 					String user = line.split(":")[0];
 					if (!user.equalsIgnoreCase(username.toLowerCase())) {
 						toSave.append(line).append("\r\n");
 					}
 				}
 			}
 			reader.close();
 
 			writer = new FileWriter(OnlineUsers.directory+OnlineUsers.flatfileData);
 			writer.write(toSave.toString());
 		} catch (Exception e1) {
 			log.log(Level.SEVERE, "Exception while removing player '" + username + "' from " + OnlineUsers.directory+OnlineUsers.flatfileData, e1);
 		} finally {
 			try {
 				if (writer != null) {
 					writer.close();
 					return regenFlatFile();
 				}
 			} catch (IOException ex) {
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean setUserOffline(String username) {
 		FileWriter writer = null;
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(new File(OnlineUsers.directory+OnlineUsers.flatfileData)));
 			String line = "";
 			StringBuilder toSave = new StringBuilder();
 			while ((line = reader.readLine()) != null) {
 				if (line.contains(":")) {
 					String user = line.split(":")[0];
 					String time = line.split(":")[1];
 					if (!user.equalsIgnoreCase(username.toLowerCase())) {
 						toSave.append(line).append("\r\n");
 					} else {
 						toSave.append(user + ":" + time + ":" + String.valueOf(OFFLINE));
 					}
 				}
 			}
 			reader.close();
 			writer = new FileWriter(OnlineUsers.directory+OnlineUsers.flatfileData);
 			writer.write(toSave.toString());
 		} catch (Exception e1) {
 			log.log(Level.SEVERE, "Exception while removing player '" + username + "' from " + OnlineUsers.directory+OnlineUsers.flatfileData, e1);
 		} finally {
 			try {
 				if (writer != null) {
 					writer.close();
 					return regenFlatFile();
 				}
 			} catch (IOException ex) {
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean setAllOffline() {
 		FileWriter writer = null;
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(new File(OnlineUsers.directory+OnlineUsers.flatfileData)));
 			String line = "";
 			StringBuilder toSave = new StringBuilder();
 			while ((line = reader.readLine()) != null) {
 				if (line.contains(":")) {
 					String user = line.split(":")[0];
 					String time = line.split(":")[1];
 					toSave.append(user + ":" + time + ":" + String.valueOf(OFFLINE));
 				}
 			}
 			reader.close();
 			writer = new FileWriter(OnlineUsers.directory+OnlineUsers.flatfileData);
 			writer.write(toSave.toString());
 		} catch (Exception e1) {
 			log.log(Level.SEVERE, "Exception setting all offline from " + OnlineUsers.directory+OnlineUsers.flatfileData, e1);
 		} finally {
 			try {
 				if (writer != null) {
 					writer.close();
 					return regenFlatFile();
 				}
 			} catch (IOException ex) {
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean removeAllUsers() {
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(OnlineUsers.directory+OnlineUsers.flatfileData);
 			writer.write("");
 		} catch (Exception e1) {
 			log.log(Level.SEVERE, "Exception setting all offline from " + OnlineUsers.directory+OnlineUsers.flatfileData, e1);
 		} finally {
 			try {
 				if (writer != null) {
 					writer.close();
 					return regenFlatFile();
 				}
 			} catch (IOException ex) {
 			}
 		}
 		return false;
 	}
 	
 	private synchronized boolean regenFlatFile() {
 		FileWriter writer = null;
 		try {
 			BufferedReader templatereader = new BufferedReader(new FileReader(new File(OnlineUsers.directory+OnlineUsers.flatfileTemplate)));
 			BufferedReader datareader = new BufferedReader(new FileReader(new File(OnlineUsers.directory+OnlineUsers.flatfileData)));
 			String line = "";
 			ArrayList<String> userLines = new ArrayList<String>();
 			boolean users = false;
 			StringBuilder toSave = new StringBuilder();
 			while ((line = templatereader.readLine()) != null) {
 				if (line.startsWith("#beginusers")) {
 					users = true;
 					toSave.append("###USERS###");
 				} else if(line.startsWith("#endusers")) {
 					users = false;
 				}			
 				if (users && !line.startsWith("#")) {
 					userLines.add(line);
 				} else if (!line.startsWith("#")) {
 					toSave.append(line+"\r\n");
 				}
 			}
 			
 			StringBuilder toreplace = new StringBuilder();
 			String dline = "";
 			while ((dline = datareader.readLine()) != null) {
 				if (dline.contains(":")) {
 					String[] split = dline.split(":");
 					String username = split[0].trim();
 					String timestamp = split[1].trim();
 					String status = split[2].trim();
 					for (String userline : userLines) {
 						toreplace.append(userline
 							.replace("{username}", username.trim())
 							.replace("{timestamp}", timestamp.trim())
 							.replace("{online-0-1}", status.trim())
 							.replace("{shorttime}", shorttime(timestamp.trim()))
 							.replace("{longtime}", longtime(timestamp.trim()))
 							.replace("{online-true-false}", String.valueOf(Boolean.parseBoolean(status)))
 							.replace("{online-offline}", onoff(status))
 						+ "\r\n");
 					}
 				}
 			}
 			datareader.close();
 			templatereader.close();
 			writer = new FileWriter(OnlineUsers.directory+OnlineUsers.flatfile);
 			writer.write(toSave.toString().replace("###USERS###", toreplace.toString()));
 		} catch (Exception e1) {
 			log.log(Level.SEVERE, "Exception setting all offline from " + OnlineUsers.directory+OnlineUsers.flatfileData, e1);
 		} finally {
 			try {
 				if (writer != null) {
 					writer.close();
 					return true;
 				}
 			} catch (IOException ex) {
 			}
 		}
 		return false;
 	}
 	
 	private String formatTime (String timestamp, String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
 		Date date = new Date(Long.parseLong(timestamp));
 		return dateFormat.format(date);
 	}
 	
 	private String shorttime (String timestamp) {
 		return formatTime(timestamp, "yyyy-MM-dd hh:mm:ss");
 	}
 	
 	private String longtime (String timestamp) {
		return formatTime(timestamp, "EEE, MMM d yyyy HH:mm:ss");
 	}
 	
 	private String onoff (String status) {
 		if (status.equalsIgnoreCase("0")) {
 			return "offline";
 		} else {
 			return "online";
 		}
 		
 	}
 }
