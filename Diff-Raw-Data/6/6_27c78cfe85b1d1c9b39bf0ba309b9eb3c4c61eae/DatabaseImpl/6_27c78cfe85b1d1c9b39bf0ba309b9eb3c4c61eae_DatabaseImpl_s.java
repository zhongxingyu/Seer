 /*******************************************************************************
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package edu.upenn.mkse212.server;
 
 import edu.upenn.mkse212.client.Database;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.amazonaws.services.simpledb.*;
 import com.amazonaws.services.simpledb.model.*;
 import com.amazonaws.auth.*;
 import edu.upenn.mkse212.*;
 
 public class DatabaseImpl extends RemoteServiceServlet implements Database {
 	AmazonSimpleDBClient db;
 	
 	public DatabaseImpl()
 	{
 	String userID = "AKIAJ5VY5NNUZEYZMLSQ";
 	String authKey = "Nvbepp7+tGocblW/S8f9Eln4b/DqxQnEABAWQ/Ef";
 	db = new AmazonSimpleDBClient(new BasicAWSCredentials(userID, authKey));
 	}
 
 	//This code is copied from http://www.sha1-online.com/sha1-java/
 	private static String hash(String input) throws NoSuchAlgorithmException {
         MessageDigest mDigest = MessageDigest.getInstance("SHA1");
         byte[] result = mDigest.digest(input.getBytes());
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < result.length; i++) {
             sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
         }
         return sb.toString();
     }
 	
 	//Validates that the user logged in with a valid username and password.
 	//Returns true if he did, false if not.
 	@Override
 	public Boolean validateLogin(String username, String password) {
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest(Names.USERS, username));
 		List<Attribute> attributeList = result.getAttributes();
 	
 		String hash = "";
 		try {
 			hash = hash(password);
 		} catch (NoSuchAlgorithmException e1) {
 			System.out.println("Unable to hash password");
 			e1.printStackTrace();
 		}
 		for (Attribute a : attributeList) {
 			if (a.getName().equals(Names.PASSWORD)) {
 					return new Boolean(a.getValue().equals(hash));
 			}
 		}
 		return new Boolean(false);
 	}
 
 	//When called, it increases loginsSoFar by one for the given user
 	//and returns that new values;
 	@Override
 	public Integer incrementLogins(String username){
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest(Names.USERS, username));
 		List<Attribute> attributeList = result.getAttributes();
 		int loginsSoFar = 0;
 		
 		for (Attribute a : attributeList) {
 			if (a.getName().equals(Names.LOGINS_SO_FAR))
 				loginsSoFar = Integer.valueOf(a.getValue()).intValue();
 		}
 		
 		loginsSoFar ++;
 				
 		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
 		list.add(new ReplaceableAttribute(Names.LOGINS_SO_FAR, ""+loginsSoFar, false));
 		db.putAttributes(new PutAttributesRequest(Names.USERS, username, list,
 				new UpdateCondition()));
 		
 		return new Integer(loginsSoFar);
 	}
 	/*
 	 * Creates an account with the given information. Returns true if all were put
 	 * onto the database successfully, false otherwise.
 	 */
 	@Override
     public Boolean createAccount(String fName, String lName, String email, String password){
         if (!this.getUserAttributeList(email).isEmpty()) return new Boolean(false);
         
 		List<ReplaceableAttribute> attributeList = new ArrayList<ReplaceableAttribute>();
         attributeList.add(new ReplaceableAttribute(Names.FIRST_NAME,fName,true));
         attributeList.add(new ReplaceableAttribute(Names.LAST_NAME,lName,true));
         attributeList.add(new ReplaceableAttribute(Names.EMAIL,email,false));
         attributeList.add(new ReplaceableAttribute(Names.USERNAME,email,false));
         try {
 			attributeList.add(new ReplaceableAttribute(Names.PASSWORD,hash(password),true));
 		} catch (NoSuchAlgorithmException e1) {
 			System.out.println("Unable to hash password");
 			e1.printStackTrace();
 			return new Boolean(false);
 		}
        
         db.putAttributes(new PutAttributesRequest(Names.USERS,email,attributeList,
                 new UpdateCondition()));
         return new Boolean(true);
     }
 	
 	/*
 	 * Puts the given UNIQUE value to the given attribute of the given user.
 	 * Returns true once the put is complete.
 	 */
 	@Override
 	public Boolean putAttribute(String username, String attribute, String value) {
         List<ReplaceableAttribute> attributeList = new ArrayList<ReplaceableAttribute>();
         if (attribute.equals(Names.PASSWORD))
 			try {
 				value = hash(value);
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 				return false;
 			}
 		attributeList.add(new ReplaceableAttribute(attribute, value, true));
 		db.putAttributes(new PutAttributesRequest(Names.USERS, username, attributeList,
 				new UpdateCondition()));
 		return new Boolean(true);
 	}
 	
 	/*
 	 * Puts the given list of NON-UNIQUE values to the given attribute of the given user.
 	 * Returns false if the List is empty. Returns true once the put is complete.
 	 */
 	@Override
 	public Boolean putAttributes(String username, String attribute, List<String> values) {
         if (values.size() == 0) return false;
 		List<ReplaceableAttribute> attributeList = new ArrayList<ReplaceableAttribute>();
 		for (String v : values) {
 			attributeList.add(new ReplaceableAttribute(attribute, v, false));
 		}
 		db.putAttributes(new PutAttributesRequest(Names.USERS, username, attributeList,
 				new UpdateCondition()));
 		return new Boolean(true);
 	}
 	
 	private List<Attribute> getUserAttributeList(String username) {
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest(Names.USERS, username));
 		List<Attribute> attributeList = result.getAttributes();
 		return new ArrayList<Attribute>(attributeList);
 	}
 	
 	public List<String> getUsers(String prefix) {
 		List<String> list = new ArrayList<String>();
 		StringBuffer selectExpression = new StringBuffer("SELECT * FROM '");
 		selectExpression.append(Names.USERS);
 		selectExpression.append("'");
 		SelectResult result = db.select(new SelectRequest(
 				selectExpression.toString()));
 		for (Item i : result.getItems()) {
 			String name = i.getName();
 			if (name.startsWith(prefix)) list.add(i.getName());
 		}	
 		return list;
 	}
 	
 	//Gets the attribute of the user and returns it as a String.
 	@Override
 	public String getValue(String username, String attribute) {
 		for (Attribute a : this.getUserAttributeList(username)) {
 			if(a.getName().equals(attribute)) {
 				String value = a.getValue();
 				if (value==null) return "";
 				return new String(value);
 			}
 		}
 		return null;
 	}
 	
 	//Gets all of the associated values of an attribute for a user and
 	//returns them as a List of Strings.
 	@Override
 	public List<String> getValues(String username, String attribute) {
 		List<String> values = new ArrayList<String>();
 		for (Attribute a : this.getUserAttributeList(username)) {
 			if(a.getName().equals(attribute))
 				values.add(a.getValue());
 		}
 		return new ArrayList<String>(values);
 	}
 	
 	//Returns true if the "username" has "other" as a friend, false if not.
 	@Override
 	public Boolean isFriendsWith(String username, String other) {
 		for (Attribute a : this.getUserAttributeList(username)) {
 			System.out.println(a.getName());
 			if (a.getName().equals(Names.FRIEND)) {
 				System.out.println("Friend found :" + a.getValue());
 				if (a.getValue().equals(other)) {
 					System.out.println(username + " is friends with " + other);
 					return new Boolean(true);
 				}
 			}		
 		}
 		return new Boolean(false);
 	}
 	
 	//Returns false if username is already friends with other.
 	//Returns true once the put is complete.
 	@Override
 	public Boolean addFriend(String username, String other) {
 		if (this.isFriendsWith(username, other)) return new Boolean(false);
 		List<ReplaceableAttribute> attributeList = new ArrayList<ReplaceableAttribute>();
 		attributeList.add(new ReplaceableAttribute(Names.FRIEND, other, false));
 		db.putAttributes(new PutAttributesRequest(Names.USERS, username, attributeList,
 				new UpdateCondition()));
 		return new Boolean(true);
 	}
 	
 	@Override
 	public List<String> getFriendsOf(String username) {
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest(Names.USERS, username));
 		List<Attribute> attributeList = result.getAttributes();
 		List<String> friends = new ArrayList<String>();
 		for (Attribute a : attributeList) {
 			if (a.getName().equals(Names.FRIEND))
 				friends.add(a.getValue());
 		}
 		return new ArrayList<String>(friends);
 	}
 	
 	//Returns false if username is not friends with other.
 	//Returns true once other is removed as username's friend.
 	@Override
 	public Boolean removeFriend(String username, String other) {
		if (!this.isFriendsWith(username, other)) return new Boolean(false);
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest(Names.USERS, username));
 		List<Attribute> attributeList = result.getAttributes();
 		List<Attribute> toBeDeleted = new ArrayList<Attribute>();
 		for (Attribute a : attributeList) {
			if (a.equals(other)) toBeDeleted.add(a);
 		}
 		db.deleteAttributes(new DeleteAttributesRequest(Names.USERS, Names.FRIEND, toBeDeleted,
 				new UpdateCondition()));
 		return new Boolean(true);
 	}
 	
 	private List<String> getAllWallPostIDs() {
 		StringBuffer selectExpression = new StringBuffer("SELECT * FROM '");
 		selectExpression.append(Names.WALL);
 		SelectResult result = db.select(new SelectRequest(
 				selectExpression.toString()));
 		List<String> ids = new ArrayList<String>();
 		for (Item i : result.getItems()) ids.add(i.getName());
 		return ids;
 	}
 	
 	/*
 	 * Puts the content of the post, a reference to the sender (username), and
 	 * a reference to the receiver to the database. Returns the wallPostID.
 	 * The wallPostID is a String of the form:
 	 * [sender]qxztoqxz[receiver]qxzidqxz[number wall post on receiver's wall]
 	 * Example for A's first post to B's wall: AqxztoqxzBqxzidqxz1
 	 */
 	@Override
 	public String postOnWall(String username, String other, String content) {
 		if (content.trim().equals("")) return null;
 		
 		int numPosts = this.getAllWallPostIDs().size(); 
 		StringBuffer id = new StringBuffer(username);
 		id.append(Names.TO);
 		id.append(other);
 		id.append(Names.ID);
 		id.append(numPosts+1);
 		
 		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
 		list.add(new ReplaceableAttribute(Names.POST, content, false));
 		list.add(new ReplaceableAttribute(Names.SENDER, username, false));
 		list.add(new ReplaceableAttribute(Names.RECEIVER, other, false));
 		list.add(new ReplaceableAttribute(Names.POST_ID_NUM, ""+(numPosts + 1), false));
 		db.putAttributes(new PutAttributesRequest(Names.WALL, id.toString(), list,
 				new UpdateCondition()));
 		return new String(id);
 	}
 	
 	@Override
 	public String postStatus(String username, String content) {
 		return new String(this.postOnWall(username, username, content));
 	}
 	
 	//Finds the Item associated with the given wallPostID and returns its contents.
 	@Override
 	public String getWallPost(String wallPostID) {
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest(Names.WALL, wallPostID));
 		List<Attribute> attributeList = result.getAttributes();
 		for (Attribute a : attributeList) {
 			if (a.getName().equals(Names.POST)) return a.getValue();
 		}
 		return null;
 	}
 	
 	public String[] getWallPostInfo(String wallPostID) {
 		String[] info = new String[4];
 		GetAttributesResult result = db.getAttributes(
 				new GetAttributesRequest(Names.WALL, wallPostID));
 		List<Attribute> attributeList = result.getAttributes();
 		for (Attribute a : attributeList) {
 			if (a.getName().equals(Names.POST)) info[0] = a.getValue();
 			else if (a.getName().equals(Names.SENDER)) info[1] = a.getValue();
 			else if (a.getName().equals(Names.RECEIVER)) info[2] = a.getValue();
 			else if (a.getName().equals(Names.POST_ID_NUM)) info[3] = a.getValue();
 		}
 		return info;
 	}
 
 	@Override
 	public List<String> getWallPostsIDs(String receiver) {
 		List<String> allIDs = this.getAllWallPostIDs();
 		List<String> theseIDs = new ArrayList<String>();
 		for(String id : allIDs) {
 			if (this.getWallPostInfo(id)[2].equals(receiver))
 				theseIDs.add(id);
 		}
 		Queue<String> pq = new PriorityQueue<String>(theseIDs.size(),
 				new Comparator<String>() {
 					@Override
 					public int compare(String arg0, String arg1) {
 						int numPost0 = Integer.parseInt(
 								getWallPostInfo(arg0)[2]);
 						int numPost1 = Integer.parseInt(
 								getWallPostInfo(arg1)[2]);
 						return numPost1 - numPost0;
 					}
 		}); //This comparator sorts the wall posts from most recent to oldest
 		pq.addAll(theseIDs);
 		List<String> posts = new ArrayList<String>();
 		while (!pq.isEmpty())
 			posts.add(this.getWallPost(pq.poll()));
 		
 		return posts;
 	}
 	
 	/*
 	 * Parses the wallPostID according to the form shown above, and returns
 	 * a List of Strings containing the sender, reciever, and number wall post
 	 * from sender to receiver.
 	 */
 	private List<String> parseWallPostID(String wallPostID) {
 		String[] s = wallPostID.split(Names.SEPARATOR);
 		List<String> result = new ArrayList<String>();
 		result.add(s[0]);
 		result.add(s[2]);
 		result.add(s[4]);
 		return result;
 	}
 	
 	/*
 	 * First, this gets the parsed version of the wallPostID and checks that it's valid.
 	 * Then, it gets the total number of comments on the wall post and increments it.
 	 * It uses that to create this comment's ID. Next, it puts the content of the comment
 	 * to the database, associated with the commentID. It also puts an attribute called
 	 * "Commenter" with the associated value of the username concatenated with the
 	 * number comment. Finally, it returns the commentID.
 	 */
 	@Override
 	public String commentOnWallPost(String username, String wallPostID, String content) {
 		List<String> parsedID = this.parseWallPostID(wallPostID);
 		if(parsedID==null || parsedID.size() != 3) return null;
 		
 		Integer numComment = this.getNumComments(wallPostID) + 1;
 		String commentID = this.createCommentID(username, numComment);
 		
 		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
 		list.add(new ReplaceableAttribute(commentID, content, true));
 		
 		String num = this.getNumComments(wallPostID).toString();
 		list.add(new ReplaceableAttribute(Names.NUM_COMMENTS, num, true));
 		db.putAttributes(new PutAttributesRequest(Names.WALL, wallPostID, list,
 				new UpdateCondition()));
 		
 		return new String(commentID);
 	}
 	
 	/* Creates a String of the form:
 	 * commentqxz[commenter]qxz[comment number on the wall post]
 	 */
 	private String createCommentID(String commenter, Integer numComment) {
 		StringBuffer sb = new StringBuffer(Names.COMMENT);
 		sb.append(Names.SEPARATOR);
 		sb.append(commenter);
 		sb.append(Names.SEPARATOR);
 		sb.append(numComment);
 		return sb.toString();
 	}
 	
 	//Returns the number of comments on the wall post associated with the wallPostID
 	private Integer getNumComments(String wallPostID) {
 		GetAttributesResult result = 
 				db.getAttributes(new GetAttributesRequest(Names.WALL, wallPostID));
 		List<Attribute> attributes = result.getAttributes();
 		for (Attribute a : attributes) {
 			if (a.getName().equals(Names.NUM_COMMENTS))
 				return new Integer(Integer.parseInt(a.getValue()));
 		}
 		return new Integer(0);
 	}
 	
 }
 
 /*
 //Returns a list of Strings of all of the wall posts from username to other.
 @Override
 public List<String> getWallPosts(String username, String other) {
 	List<String> wallPostIDs = this.getWallPostsIDs(username, other);
 	List<String> wallPosts = new ArrayList<String>();
 	for (String id : wallPostIDs)
 		wallPosts.add(this.getWallPost(id));
 	return wallPosts;
 }
 
 //Gets all of the posts on the receiver's wall.
 @Override
 public List<String> getPostsOnWall(String receiver) {
 	SelectResult result = db.select(new SelectRequest(
 	"SELECT * FROM " + Names.WALL + " WHERE " + Names.RECEIVER + " = " + receiver));
 	Queue<Item> pq = new PriorityQueue<Item>(result.getItems().size(),
 			new Comparator<Item>() {
 				@Override
 				public int compare(Item arg0, Item arg1) {
 					int numPost0 = Integer.parseInt(
 							parseWallPostID(arg0.getName()).get(2));
 					int numPost1 = Integer.parseInt(
 							parseWallPostID(arg1.getName()).get(2));
 					return numPost1 - numPost0;
 				}
 	}); //This comparator sorts the wall posts from most recent to oldest
 	pq.addAll(result.getItems());
 	List<String> posts = new ArrayList<String>();
 	while (!pq.isEmpty())
 		posts.add(this.getWallPost(pq.poll().getName()));
 	
 	return posts;
 }*/
 
 //Returns a list of Strings of all of the wall post IDs from username to other.
 /*@Override
 public List<String> getWallPostsIDs(String username, String other) {
 	List<String> list = new ArrayList<String>();
 	SelectResult result = db.select(new SelectRequest(
 			"SELECT * FROM " + Names.WALL + " WHERE " + Names.SENDER + " = "
 			+ username + " AND " + Names.RECEIVER + " = " + other));
 	for (Item i : result.getItems())
 		list.add(i.getName());
 	return new ArrayList<String>(list);
 }*/
 
 /*
 @Override
 public String updateFollowStatus(String user1, String user2, Boolean b) {
 	List<ReplaceableAttribute> attributeList = new ArrayList<ReplaceableAttribute>();
 	attributeList.add(new ReplaceableAttribute(Names.RELATION, b.toString(), true));
 	db.putAttributes(new PutAttributesRequest(Names.FOLLOW_STATUS, user1 + "TO" + user2, attributeList,
 			new UpdateCondition()));
 	return new String("true");
 }
 
 
 @Override
 public String getFollowStatus(String user1, String user2) {
 	GetAttributesResult result = 
 			db.getAttributes(new GetAttributesRequest(Names.FOLLOW_STATUS, user1 + "TO" + user2));
 	System.out.println("Test");
 	List<Attribute> attributes = result.getAttributes();
 	for (Attribute a : attributes) {
 		if (a.getName().equals(Names.RELATION))
 			return new String(a.getValue());
 	}
 	return new String("false");
 }*/
