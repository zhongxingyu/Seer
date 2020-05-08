 /* Author: zafar.khaydarov 
  * Date: 2011 Apr 17, 2011 6:41:24 PM 
  * Project: rpc_server_test
  * Zafar.Khaydarov @cs.joensuu.fi
  * */
package fi.uef.caao;
 
 /**
  * The main purpose of the class is provide methods for registering new users
  * and updating existing user's information.
  * 
  * @author zafar.khaydarov
  * @version $Revision: 1.5 $
  */
 public class CaaoUserUtils {
 	/**
 	 * Checks the uniqueness of the user name (e-mail in database)
 	 * 
 	 * @param pUserName
 	 *            String
 	 * 
 	 * @return boolean true says that everything went well and false otherwise
 	 */
 	public boolean isUserNameIsUniq(String pUserName) {
 
 		return true;
 	}
 
 	/**
 	 * updates the password of the specified user.
 	 * 
 	 * @param pUsername
 	 *            String - the user name for which the password will be updated.
 	 * @param pNewPassword
 	 *            String
 	 * 
 	 * @return boolean true says that everything went well and false otherwise
 	 */
 	public boolean updateUserPassword(String pUsername, String pNewPassword) {
 		return true;
 	}
 
 	// TODO: the rest of the methods
 	/**
 	 * Returns short capabilities of the server side
 	 * 
 	 * @return String
 	 */
 	public String getServerInfo() {
 		return "1.0";
 	}
 
 	public String toString() {
 		return this.getClass().getName();
 	}
 }
