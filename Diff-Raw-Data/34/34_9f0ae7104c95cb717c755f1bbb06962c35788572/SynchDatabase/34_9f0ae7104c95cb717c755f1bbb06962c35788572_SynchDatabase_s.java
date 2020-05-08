 package eecs.berkeley.edu.cs294;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import android.app.Activity;
 import android.text.format.Time;
 import android.util.Log;
 
 
 /*
  * This class takes care of synchronizing the local database with the remote one
  */
 public class SynchDatabase extends Activity {
 	
 	/*
 	 * Synchronize sent invitations in the local db w/ the server
 	 */
 	public static void SynchSentInvitations(ArrayList<MySentInvitation> sentInvitationList) {
 		Log.d("ServerDEBUG", "SynchSentInvitations");
 		
 		List<String> deletedRailsID = ToDo_Replica.dh.select_all_sent_invitations("sent_rails_id");
 		
 		for(Iterator<MySentInvitation> it = sentInvitationList.iterator(); it.hasNext();) {
 			MySentInvitation currInvitation= it.next();
 			
 			int currID = Integer.parseInt(currInvitation.getRailsID());
 			Time time = new Time();
 			Assert.assertTrue(time.parse(currInvitation.getTimestamp()));
 			long serverTimestamp = time.normalize(false);
 			List<String> entry = ToDo_Replica.dh.select_sent_invitation("sent_rails_id", 
 					Integer.toString(currID));
 			
 			/* The entry hasn't existed in the local db yet */
 			if(entry.size() == 0) {
 				Log.d("ServerDEBUG", "Server entry not inside local db yet");
 				ToDo_Replica.dh.insert_sent_invitation(currInvitation.getRecipient(), 
 						currInvitation.getGroup(), currInvitation.getStatus(), currInvitation.getDescription(),
 						Long.toString(serverTimestamp), currInvitation.getRailsID());
 				
 				DatabaseHelper.recent_updates.add(String.format("Invited %s to %s", currInvitation.getRecipient(), currInvitation.getGroup()));
 
 				continue;
 			}
 			
 			long dbTimestamp = Long.parseLong(entry.get(DatabaseHelper.TIMESTAMP_INDEX_S));
 			Log.d("ServerDEBUG", "comparing " + currInvitation.getRailsID() + 
 					" server timestamp: " + serverTimestamp + " local timestamp: " + dbTimestamp);
 		
 			deletedRailsID.remove(Integer.toString(currID));
 			
 			/* No need for any local database update */
 			if(dbTimestamp >= serverTimestamp) {
 				Log.d("ServerDEBUG", "Local Db has the last update");
 				continue;
 			}
 
 			/* Need to update local database since changes in the server is more recent */
 			else {
 				String recipient = currInvitation.getRecipient(); 
 				String group = currInvitation.getGroup();
 				String status = currInvitation.getStatus();
 				String description = currInvitation.getDescription();
 				String timestamp = Long.toString(serverTimestamp);
 				String railsID = currInvitation.getRailsID();
 				int sent_id = Integer.parseInt(entry.get(DatabaseHelper.SENT_ID_INDEX_S));
 				
 				ToDo_Replica.dh.update_sent_invitation(sent_id, recipient, group, status, 
 						description, timestamp, railsID);
 				
 				DatabaseHelper.recent_updates.add(String.format("Invitation to group %s for %s: %s",group, recipient, status));
 			}
 		}
 		
 		pruneLocalSentInvitations(deletedRailsID);
 	}
 	
 	/*
 	 * Delete local sent invitations that do not exist in the server anymore 
 	 */
 	private static void pruneLocalSentInvitations(List<String> deletedRailsID)  {
 		for(Iterator<String> it = deletedRailsID.iterator(); it.hasNext();) {
 			String railsID = it.next();
 			Log.d("ServerDEBUG", "deleting sent_invitation with id: " + railsID);
 			ToDo_Replica.dh.delete_sent_invitation("sent_rails_id", railsID);
 		}	
 	}
 	
 	/*
 	 * Synchronize received invitations in the local db w/ the server
 	 */
 	public static void SynchRecvInvitations(ArrayList<MyRecvInvitation> recvInvitationList) {
 		Log.d("ServerDEBUG", "SynchRecvInvitations");
 		
 		List<String> deletedRailsID = ToDo_Replica.dh.
 		select_all_recv_invitations("recv_rails_id");
 		
 		for(Iterator<MyRecvInvitation> it = recvInvitationList.iterator(); it.hasNext();) {
 			MyRecvInvitation currInvitation= it.next();
 			
 			int currID = Integer.parseInt(currInvitation.getRailsID());
 			Time time = new Time();
 			Assert.assertTrue(time.parse(currInvitation.getTimestamp()));
 			long serverTimestamp = time.normalize(false);
 			List<String> entry = ToDo_Replica.dh.select_recv_invitation("recv_rails_id", 
 					Integer.toString(currID));
 			
 			/* The entry hasn't existed in the local db yet */
 			if(entry.size() == 0) {
 				Log.d("ServerDEBUG", "Server entry not inside local db yet");
 				ToDo_Replica.dh.insert_recv_invitation(currInvitation.getSender(), 
 						currInvitation.getGroup(), Long.toString(serverTimestamp), 
 						currInvitation.getRailsID());
 				
 				DatabaseHelper.recent_updates.add(String.format("%s invites you to join %s", currInvitation.getSender(), currInvitation.getGroup()));
 
 				continue;
 			}
 			
 			long dbTimestamp = Long.parseLong(entry.get(DatabaseHelper.TIMESTAMP_INDEX_R));
 			Log.d("ServerDEBUG", "comparing " + currInvitation.getRailsID() + 
 					" server timestamp: " + serverTimestamp + 
 					" local timestamp: " + dbTimestamp);
 		
 			deletedRailsID.remove(Integer.toString(currID));
 			
 			/* No need for any local database update */
 			if(dbTimestamp >= serverTimestamp) {
 				Log.d("ServerDEBUG", "Local Db has the last update");
 				continue;
 			}
 
 			/* Need to update local database since changes in the server is more recent */
 			else {
 				String sender = currInvitation.getSender(); 
 				String group = currInvitation.getGroup();
 				String timestamp = Long.toString(serverTimestamp);
 				String railsID = currInvitation.getRailsID();
 				int recv_id = Integer.parseInt(entry.get(DatabaseHelper.RECV_ID_INDEX_R));
 				
 				ToDo_Replica.dh.update_recv_invitation(recv_id, sender, group, timestamp, 
 						railsID);
 				
 				DatabaseHelper.recent_updates.add(String.format("%s invited you to %s",sender, group));
 			}
 		}
 		
 		pruneLocalRecvInvitations(deletedRailsID);		
 	}
 
 	/*
 	 * Delete local recv invitations that do not exist in the server anymore 
 	 */
 	private static void pruneLocalRecvInvitations(List<String> deletedRailsID)  {
 		for(Iterator<String> it = deletedRailsID.iterator(); it.hasNext();) {
 			String railsID = it.next();
 			Log.d("ServerDEBUG", "deleting recv_invitation with id: " + railsID);
 			ToDo_Replica.dh.delete_recv_invitation("recv_rails_id", railsID);
 		}	
 	}
 	
 	/*
 	 * Synchronize groups in the local db w/ the server
 	 */
 	public static void SynchGroups(ArrayList<MyGroup> groupList) {
 		Log.d("ServerDEBUG", "SynchGroups");
 		
 		List<String> deletedRailsID = ToDo_Replica.dh.select_all_groups("group_rails_id");	
 		
 		for(Iterator<MyGroup> it = groupList.iterator(); it.hasNext();) {
 			MyGroup currGroup = it.next();
 			
 			int currID = Integer.parseInt(currGroup.getRailsID());
 			Time time = new Time();
 			Assert.assertTrue(time.parse(currGroup.getTimestamp()));
 			long serverTimestamp = time.normalize(false);
 			List<String> entry = ToDo_Replica.dh.select_group("group_rails_id", 
 					Integer.toString(currID));
 			
 			/* The entry hasn't existed in the local db yet */
 			if(entry.size() == 0) {
 				Log.d("ServerDEBUG", "Server entry not inside local db yet");
 				ToDo_Replica.dh.insert_group(currGroup.getName(), 
 						currGroup.getDescription(), "", Long.toString(serverTimestamp),  
 						currGroup.getRailsID());
 				
 				DatabaseHelper.recent_updates.add(String.format("Group %s added", currGroup.getName()));
 
 				continue;
 			}
 			
 			long dbTimestamp = Long.parseLong(entry.get(DatabaseHelper.TIMESTAMP_INDEX_G));
 			Log.d("ServerDEBUG", "comparing " + currGroup.getRailsID() + 
 					" server timestamp: " + serverTimestamp + 
 					" local timestamp: " + dbTimestamp);
 		
 			deletedRailsID.remove(Integer.toString(currID));
 			
 			/* No need for any local database update */
 			if(dbTimestamp >= serverTimestamp) {
 				Log.d("ServerDEBUG", "Local Db has the last update");
 				continue;
 			}
 
 			/* Need to update local database since changes in the server is more recent */
 			else {
 				String name = currGroup.getName(); 
 				String description = currGroup.getDescription();
 				String timestamp = Long.toString(serverTimestamp);
 				String railsID = currGroup.getRailsID();
 				int group_id = Integer.parseInt(entry.get(DatabaseHelper.GROUP_ID_INDEX_G));
 				
 				ToDo_Replica.dh.update_group(group_id, name, description, null, timestamp, 
 						railsID);
 				
 				DatabaseHelper.recent_updates.add(String.format("%s updated", name));
 			}
 		}
 		
 		pruneLocalGroups(deletedRailsID);
 	}
 
 	/*
 	 * Delete local groups that do not exist in the server anymore 
 	 */
 	private static void pruneLocalGroups(List<String> deletedRailsID)  {
 		for(Iterator<String> it = deletedRailsID.iterator(); it.hasNext();) {
 			String railsID = it.next();
 			if(railsID.equalsIgnoreCase("0")) {
 				continue;
 			}
 			
 			Log.d("ServerDEBUG", "deleting group with id: " + railsID);
 			ToDo_Replica.dh.delete_group("group_rails_id", railsID);
 		}	
 	}
 	
 	/*
 	 * Synchronize local database w/ changes from the server
 	 */
 	public static void SynchTodos(ArrayList<MyTodo> todoList) {
		
		Log.d("ServerDEBUG", "SynchGroupMembers");
 		
 		List<String> deletedRailsID = ToDo_Replica.dh.select_all_to_do("to_do_rails_id");
 		List<String> deletedGroupsID = ToDo_Replica.dh.select_all_to_do("group_id");
 		
 		for(Iterator<MyTodo> it = todoList.iterator(); it.hasNext();) {
 			MyTodo currTodo = it.next();
 			
 			int currID = Integer.parseInt(currTodo.getTodoRailsId());
 			Time time = new Time();
 			Assert.assertTrue(time.parse(currTodo.getTodoTimestamp()));
 			long serverTimestamp = time.normalize(false);
 			List<String> entry = ToDo_Replica.dh.select_to_do("to_do_rails_id", Integer.toString(currID));
 			
 			/* The entry hasn't existed in the local db yet */
 			if(entry.size() == 0) {
 				Log.d("ServerDEBUG", "Server entry not inside local db yet");
 				ToDo_Replica.dh.insert_to_do(currTodo.getTodoTitle(), currTodo.getTodoPlace(), 
 						currTodo.getTodoNote(), currTodo.getTodoTag(), currTodo.getTodoGroupId(), 
 						currTodo.getTodoStatus(), currTodo.getTodoPriority(), 
 						Long.toString(serverTimestamp), currTodo.getTodoDeadline(), currTodo.getTodoRailsId());
 				
 				DatabaseHelper.recent_updates.add(String.format("Added todo: %s", currTodo.getTodoTitle()));
 				continue;
 			}
 			
 			long dbTimestamp = Long.parseLong(entry.get(DatabaseHelper.TIMESTAMP_INDEX_T));
 			Log.d("ServerDEBUG", "comparing " + currTodo.getTodoTitle() + 
 					" server timestamp: " + serverTimestamp + " local timestamp: " + dbTimestamp);
 		
 			deletedRailsID.remove(Integer.toString(currID));
 			
 			/* No need for any local database update */
 			if(dbTimestamp >= serverTimestamp) {
 				Log.d("ServerDEBUG", "Local Db has the last update");
 				continue;
 			}
 
 			/* Need to update local database since changes in the server is more recent */
 			else {
 				String title = currTodo.getTodoTitle();
 				String place = currTodo.getTodoPlace();
 				String note = currTodo.getTodoNote();
 				String tag = currTodo.getTodoTag();
 				String group = currTodo.getTodoGroupId();
 				String status = currTodo.getTodoStatus();
 				String priority = currTodo.getTodoPriority();
 				String dateStr = Long.toString(serverTimestamp);
 				String deadline = currTodo.getTodoDeadline();
 				int pk = Integer.parseInt(entry.get(DatabaseHelper.TD_ID_INDEX_T));
 				
 				ToDo_Replica.dh.update_to_do(pk, title, place, note, tag, group, status, priority, deadline, dateStr, null);
 				
 				DatabaseHelper.recent_updates.add(String.format("Changed todo: %s",title));
 			}
 		}
 		
 		pruneLocalTodos(deletedRailsID, deletedGroupsID);				
 	}
 
 	/*
 	 * Delete local todos that do not exist in the server anymore 
 	 */
 	private static void pruneLocalTodos(List<String> deletedRailsID, 
 			List<String> deletedGroupsID)  
 	{
		for(Iterator<String> it = deletedRailsID.iterator(); it.hasNext();) {
			String railsID = it.next();
 			Log.d("ServerDEBUG", "deleting todo with id: " + railsID);
 			ToDo_Replica.dh.delete_to_do("to_do_rails_id", railsID);
 		}	
 	}
 	
 	/*
 	 * Synchronize group members in the local db w/ the server
 	 */
 	public static void SynchGroupMembers(ArrayList<MyGroupMember> groupMemberList) {
 		
 		Log.d("ServerDEBUG", "SynchGroupMembers");
 		
 		List<String> deletedRailsID = ToDo_Replica.dh.select_all_members("member_rails_id");
 		
 		for(Iterator<MyGroupMember> it = groupMemberList.iterator(); it.hasNext();) {
 			MyGroupMember currGroupMember = it.next();
 			
 			if(currGroupMember.getRailsID().equalsIgnoreCase(ToDo_Replica.dh.select_user().get(DatabaseHelper.USER_RAILS_ID_INDEX_U))) {
 				continue;
 			}
 			
 			int currID = Integer.parseInt(currGroupMember.getRailsID());
 			Time time = new Time();
 			Assert.assertTrue(time.parse(currGroupMember.getTimestamp()));
 			long serverTimestamp = time.normalize(false);
 			List<String> entry = ToDo_Replica.dh.select_member("member_rails_id", 
 					Integer.toString(currID));
 			
 			/* The entry hasn't existed in the local db yet */
 			if(entry.size() == 0) {
 				Log.d("ServerDEBUG", "Server entry not inside local db yet");
 				ToDo_Replica.dh.insert_member(currGroupMember.getName(), 
 						currGroupMember.getNumber(), currGroupMember.getEmail(), 
 						currGroupMember.getGroupID(), Long.toString(serverTimestamp), 
 						currGroupMember.getRailsID());
 			
 				List<String> group = ToDo_Replica.dh.select_group("group_rails_id", 
 						currGroupMember.getGroupID());
 				ToDo_Replica.dh.update_group(
 						Integer.parseInt(group.get(DatabaseHelper.GROUP_ID_INDEX_G)), null, 
 						null, group.get(DatabaseHelper.MEMBER_INDEX_G) + " " + 
 						currGroupMember.getRailsID(), null, null);
 				
 				DatabaseHelper.recent_updates.add(String.format("%s joined %s", currGroupMember.getName(), group.get(DatabaseHelper.NAME_INDEX_G)));
 				
 				continue;
 			}
 			
 			long dbTimestamp = Long.parseLong(entry.get(DatabaseHelper.TIMESTAMP_INDEX_M));
 			Log.d("ServerDEBUG", "comparing " + currGroupMember.getRailsID() + 
 					" server timestamp: " + serverTimestamp + 
 					" local timestamp: " + dbTimestamp);
 		
 			deletedRailsID.remove(Integer.toString(currID));
 			
 			/* No need for any local database update */
 			if(dbTimestamp >= serverTimestamp) {
 				Log.d("ServerDEBUG", "Local Db has the last update");
 				continue;
 			}
 
 			/* Need to update local database since changes in the server is more recent */
 			else {
 				String name = currGroupMember.getName(); 
 				String number = currGroupMember.getNumber();
 				String email = currGroupMember.getEmail(); 
 				String group_id = currGroupMember.getGroupID();
 				String timestamp = Long.toString(serverTimestamp); 
 				String railsID = currGroupMember.getRailsID();
 				int m_id = Integer.parseInt(entry.get(DatabaseHelper.MEMBER_ID_INDEX_M));
 								
 				ToDo_Replica.dh.update_member(m_id, name, number, email, group_id, timestamp, 
 						railsID);
 				
 				DatabaseHelper.recent_updates.add(String.format("Groupmember updated: %s", name));
 			}
 		}
 		
 		pruneLocalGroupMembers(deletedRailsID);		
 	}
 
 	/*
 	 * Delete local group members that do not exist in the server anymore 
 	 */
 	private static void pruneLocalGroupMembers(List<String> deletedRailsID)  {
 		for(Iterator<String> it = deletedRailsID.iterator(); it.hasNext();) {
 			String railsID = it.next();
 			Log.d("ServerDEBUG", "deleting members with id: " + railsID);
 			ToDo_Replica.dh.delete_member("member_rails_id", railsID);
 		}	
 	}
 
 }
