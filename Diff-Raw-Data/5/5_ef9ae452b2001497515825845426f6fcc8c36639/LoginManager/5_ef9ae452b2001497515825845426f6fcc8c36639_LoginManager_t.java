 
 
 
 package overwatch.security;
 
 import overwatch.core.Gui;
 import overwatch.core.Main;
 import overwatch.db.Database;
 import overwatch.db.Personnel;
 
 
 
 
 
 /**
  * Manages the user login process.
  * 
  * @author  Lee Coakley
  * @version 4
  */
 
 
 
 
 
 public class LoginManager
 {
 	private static Integer           currentUser;
 	private static BackgroundMonitor monitor;
 	
 	
 	
 	
 	
 	/**
 	 * Check whether there is someone logged in right now.
 	 * @return boolean
 	 */
 	public static boolean hasLoggedInUser() {
 		return (getCurrentUser() != null);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get the person currently logged in.
 	 * @return personNo
 	 */
 	public static Integer getCurrentUser() {
 		return currentUser;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Check if a person is the currently logged in user.
 	 * @param personNo
 	 * @return boolean
 	 */
 	public static boolean isCurrentUser( Integer personNo ) {
 		return personNo.equals( currentUser );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get security level for the current user from the database.
 	 * @return integer security level
 	 */
 	public static Integer getCurrentSecurityLevel() {	
 		return Personnel.getPrivilegeLevel( getCurrentUser() );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Logs in a user.  If this function succeeds, the current user is set and
 	 * a background monitor is run to ensure they continue to exist.
 	 * @param user
 	 * @param pass
 	 * @return whether it succeeded
 	 */
 	public static boolean doLogin( String user, String pass )
 	{
 		Integer personNo = Personnel.getNumber( user );
 		
 		if (isPassValid( personNo, pass )) {
 			currentUser = personNo;
 			setupMonitor();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Check if a password matches that of a particular person.
 	 * @param personNo
 	 * @param pass
	 * @return boolean
 	 */
 	public static boolean isPassValid( Integer personNo, String pass )
 	{
		boolean personExists = (personNo != null);
 		
 		if (personExists) {
 			HashSaltPair hsp = Personnel.getHashSaltPair( personNo );
 			
 			if (hsp != null)
 			if (LoginCrypto.isPassValid( pass, hsp )) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	
 	
 	
 	public static void debugResetAllPasses( String resetTo ) {
 		HashSaltPair hsp = LoginCrypto.generateHashSaltPair( resetTo );
 		
 		int rowsChanged = Database.update(
 			"update Personnel  " +
 			"set loginHash = '" + hsp.hash + "'," +
 			"    loginSalt = '" + hsp.salt + "'"
 		);
 		
 		System.out.println( "resetAllPasses: changed " + rowsChanged + " HSPs." );
 	}
 	
 	
 	
 	
 	
 
 
 
 
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private static void setupMonitor()
 	{
 		if (monitor != null) {
 			monitor.stop();
 		}
 		
 		monitor = new BackgroundMonitor();
 		
 		
 		// Existence
 		monitor.addBackgroundCheck( new BackgroundCheck() {
 			public void onCheck()
 			{
 				if ( ! Personnel.exists( LoginManager.getCurrentUser() )) {
 					Gui.showError( "Account Deleted", "Your account has been deleted!" );
 					Main.shutdown();
 				}
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Test
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static void main( String[] args )
 	{	
 			
 		int personNo = Personnel.getNumber( "yea.boi" );
 		Personnel.setPass( personNo, "1234" );
 		
 		
 		
 		boolean loginSuccess = LoginManager.doLogin(
 			overwatch.util.Console.getString( "Enter login: " ),
 			overwatch.util.Console.getString( "Enter pass:  " )
 		);
 		
 		
 		
 		if (loginSuccess) {
 			System.out.println( "logged in as #" + LoginManager.getCurrentUser() );
 		} else {
 			System.out.println( "Invalid login details!" );
 		}
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
