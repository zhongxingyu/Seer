 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.db;
 
 import java.sql.Connection;
 import org.jamwiki.UserHandler;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.model.WikiUserInfo;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  *
  */
 public class DatabaseUserHandler implements UserHandler {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(DatabaseUserHandler.class.getName());
 
 	/**
 	 *
 	 */
 	public void addWikiUserInfo(WikiUserInfo userInfo, Object transactionObject) throws Exception {
 		Connection conn = null;
 		try {
 			conn = WikiDatabase.getConnection(transactionObject);
 			WikiDatabase.queryHandler().insertWikiUserInfo(userInfo, conn);
 		} catch (Exception e) {
 			DatabaseConnection.handleErrors(conn);
 			throw e;
 		} finally {
 			WikiDatabase.releaseConnection(conn, transactionObject);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public boolean authenticate(String login, String password) throws Exception {
 		// password is stored encrypted, so encrypt password
 		String encryptedPassword = Encryption.encrypt(password);
		WikiResultSet rs = WikiDatabase.queryHandler().lookupWikiUser(login, encryptedPassword);
		return (rs.size() == 0) ? false : true;
 	}
 
 	/**
 	 *
 	 */
 	private WikiUserInfo initWikiUserInfo(WikiResultSet rs) throws Exception {
 		WikiUserInfo userInfo = new WikiUserInfo();
 		userInfo.setUserId(rs.getInt("wiki_user_id"));
 		userInfo.setLogin(rs.getString("login"));
 		userInfo.setEmail(rs.getString("email"));
 		userInfo.setFirstName(rs.getString("first_name"));
 		userInfo.setLastName(rs.getString("last_name"));
 		userInfo.setEncodedPassword(rs.getString("encoded_password"));
 		return userInfo;
 	}
 
 	/**
 	 *
 	 */
 	public boolean isWriteable() {
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	public WikiUserInfo lookupWikiUserInfo(String login) throws Exception {
 		WikiResultSet rs = WikiDatabase.queryHandler().lookupWikiUserInfo(login);
 		if (rs.size() == 0) return null;
 		return initWikiUserInfo(rs);
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiUserInfo(WikiUserInfo userInfo, Object transactionObject) throws Exception {
 		Connection conn = null;
 		try {
 			conn = WikiDatabase.getConnection(transactionObject);
 			WikiDatabase.queryHandler().updateWikiUserInfo(userInfo, conn);
 		} catch (Exception e) {
 			DatabaseConnection.handleErrors(conn);
 			throw e;
 		} finally {
 			WikiDatabase.releaseConnection(conn, transactionObject);
 		}
 	}
 }
