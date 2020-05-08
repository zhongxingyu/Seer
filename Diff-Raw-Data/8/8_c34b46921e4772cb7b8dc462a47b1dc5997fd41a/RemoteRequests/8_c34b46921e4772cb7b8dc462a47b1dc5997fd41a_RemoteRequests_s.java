 /**
  * JMBS: Java Micro Blogging System
  *
  * Copyright (C) 2012  
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY.
  * See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * @author Younes CHEIKH http://cyounes.com
  * @author Benjamin Babic http://bbabic.com
  * 
  */
 
 package jmbs.common;
 
 import java.awt.image.BufferedImage;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public interface RemoteRequests extends Remote {
 
 	/**
 	 * @param em
 	 *            - user's email
 	 * @param psw
 	 *            - user's password
 	 * @return the user if pass and email match null if not
 	 * @throws RemoteException
 	 * @throws SecurityException if a user attempts to connect too many times
 	 */
 	public User connectUser(String em, String psw) throws RemoteException, SecurityException;
 
 	/**
 	 * @param m
 	 *            the message
 	 * @return true if adding message is successed
 	 * @throws RemoteException
 	 */
 	public int addMessage(Message m) throws RemoteException;
 
 	/**
 	 * Creat new user on DB
 	 * 
 	 * @param u
 	 *            User
 	 * @param hashedpassword
 	 * @return true if creating new user on db is successed
 	 * @throws RemoteException
 	 */
 	public boolean createUser(User u, String hashedpassword) throws RemoteException;
 
 	/**
 	 * Search for users on DB
 	 * 
 	 * @param userName
 	 *            string
 	 * @return a list of users who have name as the entred string
 	 * @throws RemoteException
 	 */
 	public ArrayList<User> searchUser(String userName, int param) throws RemoteException;
 
 	/**
 	 * add relation on follows table in the DB
 	 * 
 	 * @param idfollower
 	 *            the user id
 	 * @param idfollowed
 	 *            the folled id
 	 * @return true if editing on the DB is successed
 	 * @throws RemoteException
 	 */
 	public boolean follows(int idfollower, int idfollowed) throws RemoteException;
 
 	/**
 	 * remove relation from follows table in the DB
 	 * 
 	 * @param idfollower
 	 *            the user id
 	 * @param idfollowed
 	 *            the folled id
 	 * @return true if editing on the DB is successed
 	 * @throws RemoteException
 	 */
 	public boolean unFollow(int idfollower, int idfollowed) throws RemoteException;
 
 	/**
 	 * @param u
 	 *            user
 	 * @return list of users who follow the user u
 	 * @throws RemoteException
 	 */
 	public ArrayList<User> getFollowers(User u) throws RemoteException;
 
 	/**
 	 * get the latest messages from server
 	 * 
 	 * @param iduser
 	 *            the id of user who requests the latest messages from server
 	 * @param idlastmessage
 	 *            the id of the lasted messages requested from server by the
 	 *            client
 	 * @return list of messages
 	 * @throws RemoteException
 	 */
 	public ArrayList<Message> getLatestTL(int iduser, int idlastmessage, int maxMsg) throws RemoteException;
 
 	/**
 	 * Search for projects have a name like 'likeName' in the DB 
 	 * @param likeName the name of project
 	 * @return a list of projects 
 	 * @throws RemoteException
 	 */
 	public ArrayList<Project> searchForProject(String likeName) throws RemoteException ;
 	
 	
 	
 	/**
 	 * participate a user into a project with the default authorization level
 	 * @param idUser user id
 	 * @param idProject project id
 	 * @return true if participation was set, else false
 	 * @throws RemoteException
 	 */
 	public boolean participate(int idUser, int idProject) throws RemoteException ;
 	
 	/**
 	 * participate a user into a project with the given authorization level
 	 * @param idUser user id
 	 * @param idProject project id
 	 * @param auth authorization level
 	 * @return true if participation was set, else false
 	 * @throws RemoteException
 	 */
 	public boolean participate(int idUser, int idProject, int auth) throws RemoteException ;
 
 	
 	/**
 	 * un participate user from project
 	 * @param idUser user ID
 	 * @param idProject project ID
 	 * @return true if participation was unset<br />
 	 * 			example: if user is not participated into project we return false
 	 * @throws RemoteException
 	 */
 	public boolean unParticipate(int idUser, int idProject) throws RemoteException ; 
 	
 	
 	/**
 	 * Create new project
 	 * @param name the project name
 	 * @return the created project
 	 * @throws RemoteException
 	 */
 	public Project createProject(String name,int iduser) throws RemoteException ;
 	
 	
 	/**
 	 * or Remove project, to prevent the users to participate and write new messages in a closed project
 	 * @param idUser user id , to confirm that the owner who want to close his project
 	 * @param idProject the project to close
 	 * @return true if project closed successfully 
 	 * @throws RemoteException
 	 */
 	public boolean closeProject(int idUser, int idProject) throws RemoteException ;
 	
 	
 	/**
 	 * search for projects where the user is participated
 	 * @param idUser user id
 	 * @return a list of projects
 	 * @throws RemoteException
 	 */
 	public ArrayList<Project> getUserProjects (int idUser) throws RemoteException ;
 	
 	/**
 	 * search for all users have participated into a project
 	 * @param idProject project id
 	 * @return a list of users
 	 * @throws RemoteException
 	 */
 	public ArrayList<User> getProjectUsers (int idProject) throws RemoteException ;
 	
 	/**
 	 * Close the connection and return a boolean saying if the connection was closed or not.
 	 * @return boolean - true if the connection was closed - false if not
 	 */
 	public boolean close(int userid) throws RemoteException;
 	
 	/**
 	 * Changes the password of the given user.
 	 * 
 	 * @param userid id of the user whose pass u want to change
 	 * @param oldPass old pass for this user
 	 * @param newPass new pass
 	 * @return true if everything was alright false if the old password is wrong.
 	 * @throws RemoteException
 	 * @throws SQLException if it was unable to change the password
 	 */
 	public boolean changePassword(int userid, String oldPass, String newPass) throws RemoteException, SQLException;
 	
 	/**
	 * set connection to server
	 */
	//public void connect() throws RemoteException, SecurityException;
	
	/**
 	 * logs out a user
 	 */
 	public void logOut(int iduser) throws RemoteException;
 	
 	public ArrayList<Project> getOwnedProject(int userid) throws RemoteException;
         
         public boolean changeAvatar(int userid, BufferedImage img, String nom, boolean overwrite) throws RemoteException;
         
         public boolean changeMail(int userid, String pass, String mail) throws RemoteException;
         
         public int addMessage(Message m, int projectId) throws RemoteException; 
         
         public boolean changFname(int userid, String fname) throws RemoteException;
                 
         public boolean changName(int userid, String name) throws RemoteException;
         
        public boolean createUser(int userid, User u, String hashedPassword, int authlvl);
 }
