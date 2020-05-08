 package com.lastcrusade.soundstream.net.message;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 
 import com.lastcrusade.soundstream.model.UserList;
 
 public class UserListMessage extends ADataMessage {
 	private final String TAG = UserListMessage.class.getName();
 	
 	private UserList userList; 
 	
	/**
	 * Default constructor, required for Messenger.  All other users should use
	 * the other constructor.
	 * 
	 */
	UserListMessage() {
 	    this(new UserList());
 	}
 	
 	public UserListMessage(UserList userList) {
 	    this.userList = userList;
 	}
 	
 	@Override
 	public void deserialize(InputStream input) throws IOException {
 		int userListSize = readInteger(input);
 		for(int i = 0; i < userListSize; i++) {
 			String bluetoothID = readString(input);
 			String macAddress = readString(input);
 			userList.addUser(bluetoothID, macAddress);
 		}
 	}
 	
 	@Override
 	public void serialize(OutputStream output) throws IOException {
 		List<String> bluetoothIDs = userList.getBluetoothIDs();
 		List<String> macAddresses= userList.getMacAddresses();
 		
 		writeInteger(userList.getUsers().size(), output);
 		for(int i = 0; i < bluetoothIDs.size(); i++) {
 			writeString(bluetoothIDs.get(i), output);
 			writeString(macAddresses.get(i), output);
 		}
 	}
 
 	public UserList getUserList() {
 		return userList;
 	}
 
 	public void setUserList(UserList userList) {
 		this.userList = userList;
 	}
 	
 
 }
