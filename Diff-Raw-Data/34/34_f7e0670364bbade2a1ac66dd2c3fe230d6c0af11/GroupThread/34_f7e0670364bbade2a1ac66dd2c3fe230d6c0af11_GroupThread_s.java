 /* This thread does all the work. It communicates with the client through Envelopes.
  * 
  */
 import java.lang.Thread;
 import java.net.Socket;
 import java.io.*;
 import java.util.*;
 
 public class GroupThread extends Thread 
 {
 	private final Socket socket;
 	private GroupServer my_gs;
 	
 	public GroupThread(Socket _socket, GroupServer _gs)
 	{
 		socket = _socket;
 		my_gs = _gs;
 	}
 	
 	public void run()
 	{
 		boolean proceed = true;
 
 		try
 		{
 			//Announces connection and opens object streams
 			System.out.println("*** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + "***");
 			final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
 			final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
 			
 			do
 			{
 				Envelope message = (Envelope)input.readObject();
 				System.out.println("Request received: " + message.getMessage());
 				Envelope response;
 				
 				if(message.getMessage().equals("GET"))//Client wants a token
 				{
 					String username = (String)message.getObjContents().get(0); //Get the username
 					if(username == null)
 					{
 						response = new Envelope("FAIL");
 						response.addObject(null);
 						output.writeObject(response);
 					}
 					else
 					{
 						UserToken yourToken = createToken(username); //Create a token
 						
 						//Respond to the client. On error, the client will receive a null token
 						response = new Envelope("OK");
 						response.addObject(yourToken);
 						output.writeObject(response);
 					}
 				}
 				else if(message.getMessage().equals("CUSER")) //Client wants to create a user
 				{
 					if(message.getObjContents().size() < 2)
 					{
 						System.out.println("HERE_1");
 						response = new Envelope("FAIL");
 					}
 					else
 					{
 						System.out.println("HERE_2");
 						response = new Envelope("FAIL");
 						
 						if(message.getObjContents().get(0) != null)
 						{
 							if(message.getObjContents().get(1) != null)
 							{
 								String username = (String)message.getObjContents().get(0); //Extract the username
 								UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 								
 								System.out.println("username: " + username);
 								if(createUser(username, yourToken))
 								{
 									response = new Envelope("OK"); //Success
 								}
 							}
 						}
 					}
 					
 					output.writeObject(response);
 				}
 				else if(message.getMessage().equals("DUSER")) //Client wants to delete a user
 				{
 					
 					if(message.getObjContents().size() < 2)
 					{
 						response = new Envelope("FAIL");
 					}
 					else
 					{
 						response = new Envelope("FAIL");
 						
 						if(message.getObjContents().get(0) != null)
 						{
 							if(message.getObjContents().get(1) != null)
 							{
 								String username = (String)message.getObjContents().get(0); //Extract the username
 								UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 								
 								if(deleteUser(username, yourToken))
 								{
 									response = new Envelope("OK"); //Success
 								}
 							}
 						}
 					}
 					
 					output.writeObject(response);
 				}
 				else if(message.getMessage().equals("CGROUP")) //Client wants to create a group
 				{
 				    /* Create Group:
 						Any user can create a group
 						Owner of the group.
 				    */
 					// Make sure contents are correct
 					if(message.getObjContents().size() < 2){
 						response = new Envelope("FAIL");
 						output.writeObject(response);
 						return;
 					}
 					String groupname = (String)message.getObjContents().get(0);
 					UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 					String username = yourToken.getSubject();
 
 					my_gs.userList.addGroup(username, groupname);
 					my_gs.userList.addOwnership(username, groupname);
 
 					my_gs.groupList.addGroup(groupname);
 					my_gs.groupList.addOwner(groupname, username);
 
 					response = new Envelope("OK");
 					output.writeObject(response);
 				}
 				else if(message.getMessage().equals("DGROUP")) //Client wants to delete a group
 				{
 				    /*
 						boolean deleteGroup(String groupname, UserToken token)
 						This method allows the owner of token to delete the specified group, provided that
 						they are the owner of that group. After deleting a group, no user should be a member
 						of that group.
 				    */
 
 					if(message.getObjContents().size() < 2){
 						response = new Envelope("FAIL");
 						output.writeObject(response);
 						return;
 					}
 
 					String groupname = (String)message.getObjContents().get(0);
 					UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 					String username = yourToken.getSubject();
 
 					if(!my_gs.groupList.isOwner(groupname, username)){
 						response = new Envelope("FAIL");
 						output.writeObject(response);
 					}
 					else{
 						my_gs.userList.removeGroup(username, groupname);	
 
 						//Go through all the group members and remove them!
 						for(String user: my_gs.groupList.getMembers(groupname)){
 							my_gs.userList.removeGroup(user, groupname);
 						}
 						// @FIXME: Is there more than one user as an owner? If yes, fix this to remove all groups
 						my_gs.groupList.deleteGroup(groupname);
 						response = new Envelope("OK");
 						output.writeObject(response);
 
 					}
 				}
 				else if(message.getMessage().equals("LMEMBERS")) //Client wants a list of members in a group
 				{
 				    /*
 						List<String> listMembers(String group, UserToken token)
 						Provided that the owner of token is also the owner of group, this method will return
 						a list of all users that are currently members of group
 				    */
 					if(message.getObjContents().size() < 2){
 						response = new Envelope("FAIL");
 						output.writeObject(response);
 						return;
 					}
 
 					String groupname = (String)message.getObjContents().get(0);
 					UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 					String username = yourToken.getSubject();
 
 					if(!my_gs.groupList.isOwner(groupname, username)){
 						response = new Envelope("FAIL");
 						output.writeObject(response);
 					}
 					else{
 						List<String> members = new ArrayList<String>(my_gs.groupList.getMembers(groupname));
 						response = new Envelope("OK");
 						response.addObject(members);
 						output.writeObject(response);
 
 					}
 
 				}
 				else if(message.getMessage().equals("AUSERTOGROUP")) //Client wants to add user to a group
 				{
 				    /*
 						boolean addUserToGroup(String user, String group, UserToken token)
 						This method enables the owner of token to add the user user to the group group.
 						This operation requires that the owner of token is also the owner of group.
 				    */
 					if(message.getObjContents().size() < 2){
 						response = new Envelope("FAIL");
 						output.writeObject(response);
 						return;
 					}
 
 					String userToAdd = (String)message.getObjContents().get(0);
 					String groupname = (String)message.getObjContents().get(1);
 					UserToken yourToken = (UserToken)message.getObjContents().get(2); //Extract the token
 					String username = yourToken.getSubject();
 
 					if(!my_gs.groupList.isOwner(groupname, username)){
 						response = new Envelope("FAIL");
 						output.writeObject(response);
 					}
 					else{
						my_gs.userList.addGroup(username, groupname);
 						my_gs.groupList.addMember(groupname, userToAdd);
 
 						response = new Envelope("OK");
 						output.writeObject(response);
 					}
 
 				}
 				else if(message.getMessage().equals("RUSERFROMGROUP")) //Client wants to remove user from a group
 				{
 				    /*
 						boolean deleteUserFromGroup(String user, String group, UserToken token)
 						This method enables the owner of token to remove the user user from the group
 						group. This operation requires that the owner of token is also the owner of group.
 				    */
 					if(message.getObjContents().size() < 3){
 						response = new Envelope("FAIL");
 						output.writeObject(response);
 						return;
 					}
 
 					String userToDelete = (String) message.getObjContents().get(0);
 					String groupname = (String)message.getObjContents().get(1);
 					UserToken yourToken = (UserToken)message.getObjContents().get(2); //Extract the token
 					String username = yourToken.getSubject();
 
 					if( userToDelete.equals(username) || !my_gs.groupList.isOwner(groupname, username)){
 						response = new Envelope("FAIL");
 						output.writeObject(response);
 					}
 					else{
 						my_gs.userList.removeGroup(userToDelete, groupname);
 						my_gs.groupList.removeMember(groupname, userToDelete);
 
 						response = new Envelope("OK");
 						output.writeObject(response);
 					}
 
 
 				}
 				else if(message.getMessage().equals("DISCONNECT")) //Client wants to disconnect
 				{
 					socket.close(); //Close the socket
 					proceed = false; //End this communication loop
 				}
 				else
 				{
 					response = new Envelope("FAIL"); //Server does not understand client request
 					output.writeObject(response);
 				}
 			}while(proceed);	
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 	
 	//Method to create tokens
 	private UserToken createToken(String username) 
 	{
 		//Check that user exists
 		if(my_gs.userList.checkUser(username))
 		{
 			//Issue a new token with server's name, user's name, and user's groups
 			UserToken yourToken = new Token(my_gs.name, username, my_gs.userList.getUserGroups(username));
 			return yourToken;
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	
 	//Method to create a user
 	private boolean createUser(String username, UserToken yourToken)
 	{
 		String requester = yourToken.getSubject();
 		
 		//Check if requester exists
 		if(my_gs.userList.checkUser(requester))
 		{
 			//Get the user's groups
 			ArrayList<String> temp = my_gs.userList.getUserGroups(requester);
 			//requester needs to be an administrator
 			if(temp.contains("ADMIN"))
 			{
 				//Does user already exist?
 				if(my_gs.userList.checkUser(username))
 				{
 					return false; //User already exists
 				}
 				else
 				{
 					my_gs.userList.addUser(username);
 					return true;
 				}
 			}
 			else
 			{
 				return false; //requester not an administrator
 			}
 		}
 		else
 		{
 			return false; //requester does not exist
 		}
 	}
 	
 	//Method to delete a user
 	private boolean deleteUser(String username, UserToken yourToken)
 	{
 		String requester = yourToken.getSubject();
 		
 		//Does requester exist?
 		if(my_gs.userList.checkUser(requester))
 		{
 			ArrayList<String> temp = my_gs.userList.getUserGroups(requester);
 			//requester needs to be an administer
 			if(temp.contains("ADMIN"))
 			{
 				//Does user exist?
 				if(my_gs.userList.checkUser(username))
 				{
 					//User needs deleted from the groups they belong
 					ArrayList<String> deleteFromGroups = new ArrayList<String>();
 					
 					//This will produce a hard copy of the list of groups this user belongs
 					for(int index = 0; index < my_gs.userList.getUserGroups(username).size(); index++)
 					{
 						deleteFromGroups.add(my_gs.userList.getUserGroups(username).get(index));
 					}
 					
 					//Delete the user from the groups
 					//If user is the owner, removeMember will automatically delete group!
 					for(int index = 0; index < deleteFromGroups.size(); index++)
 					{
 						my_gs.groupList.removeMember(username, deleteFromGroups.get(index));
 					}
 					
 					//If groups are owned, they must be deleted
 					ArrayList<String> deleteOwnedGroup = new ArrayList<String>();
 					
 					//Make a hard copy of the user's ownership list
 					for(int index = 0; index < my_gs.userList.getUserOwnership(username).size(); index++)
 					{
 						deleteOwnedGroup.add(my_gs.userList.getUserOwnership(username).get(index));
 					}
 					
 					//Delete owned groups
 					for(int index = 0; index < deleteOwnedGroup.size(); index++)
 					{
 						//Use the delete group method. Token must be created for this action
 						deleteGroup(deleteOwnedGroup.get(index), new Token(my_gs.name, username, deleteOwnedGroup));
 					}
 					
 					//Delete the user from the user list
 					my_gs.userList.deleteUser(username);
 					
 					return true;	
 				}
 				else
 				{
 					return false; //User does not exist
 					
 				}
 			}
 			else
 			{
 				return false; //requester is not an administer
 			}
 		}
 		else
 		{
 			return false; //requester does not exist
 		}
 	}
 	
 	private void deleteGroup(String groupname, UserToken token){
 		String username = token.getSubject();
 		my_gs.userList.removeGroup(username, groupname);	
 
 		//Go through all the group members and remove them!
 		for(String user: my_gs.groupList.getMembers(groupname)){
 			my_gs.userList.removeGroup(user, groupname);
 		}
 		// @FIXME: Is there more than one user as an owner? If yes, fix this to remove all groups
 		my_gs.groupList.deleteGroup(groupname);
 
 	}
 }
