 package server.model;
 
 import java.rmi.RemoteException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class MemberOfGroup extends Model {
 
 	private int memberOfGroupID, groupID, userID;
 	
	public MemberOfGroup(int roomID) throws RemoteException, SQLException {
		super("Room", createTableFields(), "roomID");
		ResultSet result = super.getFromDB(roomID);
 		if(result.next()) {
 			this.memberOfGroupID = result.getInt("memberOfGroupID");
 			this.groupID = result.getInt("groupID");
 			this.userID = result.getInt("userID");
 		}
 	}
 	
 	public MemberOfGroup() throws RemoteException, SQLException {
		super("Room", createTableFields(), "roomID");
 		ArrayList<Integer> keyList = super.addToDB();
 		this.memberOfGroupID = keyList.get(0);
 	}
 	private static ArrayList<String> createTableFields() {
 		ArrayList<String> tableFields = new ArrayList<String>();
 		tableFields.add("roomID");
 		tableFields.add("roomName");
 		tableFields.add("personCapacity");
 		return tableFields;
 	}
 
 	public int getGroupID() {
 		return groupID;
 	}
 
 	public void setGroupID(int groupID) throws SQLException {
 		super.updateField("groupID", groupID, memberOfGroupID);
 		this.groupID = groupID;
 	}
 
 	public int getUserID() {
 		return userID;
 	}
 
 	public void setUserID(int userID) throws SQLException {
 		super.updateField("userID", userID, memberOfGroupID);
 		this.userID = userID;
 	}
 
 	public int getMemberOfGroupID() {
 		return memberOfGroupID;
 	}
 	
 	@Override
 	public void delete() {
 		super.delete(memberOfGroupID);		
 	}
 }
