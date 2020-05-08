 package drexel.edu.blackjack.server.commands;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import drexel.edu.blackjack.db.user.FlatfileUserManager;
 import drexel.edu.blackjack.db.user.UserManagerInterface;
 import drexel.edu.blackjack.server.BlackjackProtocol;
 import drexel.edu.blackjack.server.BlackjackProtocol.STATE;
 import drexel.edu.blackjack.server.ResponseCode;
 import drexel.edu.blackjack.server.game.User;
 
 /***/
 
 public class PasswordCommand extends BlackjackCommand {
 
 	private static final String COMMAND_WORD = "PASSWORD";
 
 	Set<STATE> validPasswordStates = null;
 	
 	/**@param protocol The protocol connection that made that
 	 * command. From there the user, state, and all sorts of
 	 * good information can be found.
 	 * @param cm Information derived from the client associated
 	 * with the user, what it sent in to the server
 	 * @return The string that should be sent back to the client
 	 * as the response.
 	 */
 	public String processCommand(BlackjackProtocol protocol, CommandMetadata cm) {
 
 		/** Step 0: Error 1: If either object is null, it's an internal error */
 		if (protocol == null || cm == null) {
 			return new ResponseCode( ResponseCode.CODE.INTERNAL_ERROR,
 					"PasswordCommand.processCommand() received null arguments").toString();	
 		}
 
 		/** Step 1-2: Error 2: Return an error if not in valid state for PASSWORD command */
 		if(!getValidStates().contains( protocol.getState()) ) {
 			
 			return new ResponseCode( ResponseCode.CODE.NOT_EXPECTING_PASSWORD,
 					"PasswordCommand.processCommand() received out-of-context command").toString();
 		}
 
 		/** Steps 3-4: Error 3: If the PASSWORD password parameter is not exactly one string,
 		 * send an error message.*/
		if ((cm.getParameters() == null) || cm.getParameters().size() != 1)  {
 			
 			return new ResponseCode( ResponseCode.CODE.SYNTAX_ERROR,
 					"Must include a single parameter indicating password").toString();
 		}
 		
 		//Get username from parameter protocol passed
 		String username = protocol.getUsername();
 		
 		//Get password from cm parameter passed; extract from list, the first -and only- token. 
 		String password = cm.getParameters().get(0);
 		
 		//Sigleton FlatfileUserManager
 		UserManagerInterface userManager = FlatfileUserManager.getDefaultUserManager();
 		
 		/**Check that login-credentials parameters are not null; else, send an error message.
 		 * If login-credentials valid, record which user is connected and send a 'success' message.*/
 		
 		if(userManager.loginUser(username, password) == null) {
 			
 			protocol.setState(STATE.WAITING_FOR_USERNAME);
 			
 			return new ResponseCode( ResponseCode.CODE.INVALID_LOGIN_CREDENTIALS, 
 					"PasswordCommand.loginCredentialsCommand() received invalid login credentials; try logging in again.").toString();
 		}
 		/**Associate the new user with the protocol*/
 		
 		User user = new User(userManager.loginUser(username, password));
 			
 		protocol.setUser(user);
 		
 		/**Step 7: Update state. The client has authenticated but is not in a session*/
 		
 		protocol.setState(STATE.NOT_IN_SESSION);
 			
 		/**Step 8: Generate the proper response */
 		
 		return new ResponseCode( ResponseCode.CODE.SUCCESSFULLY_AUTHENTICATED, 
 				"PasswordCommand.loginCredentialsCommand() received valid login credentials; " +
 				"still not in session" ).toString();
 	}
 	
 	@Override
 	public Set<STATE> getValidStates() {
 		validPasswordStates = new HashSet<STATE>();
 
 		//Add the only allowed state which is WAITING_FOR_PASSWORD
 		validPasswordStates.add(STATE.WAITING_FOR_PASSWORD);
 
 		return validPasswordStates;
 	}
 
 	/**
 	 * This returns a list of parameters that the command requires.
 	 * For PASSWORD there is a 'password' parameter
 	 * that the command needs. The CAPABILITIES command makes use of this
 	 * for sending the capabilities list
 	 * Parse the password command word into command word and parameter
 	 * and pass back -in an ArrayList- the only parameter PASSWORD command word is allowed
 	 * to have.
 	 * 
 	 * @return parameter The List with just one string, the password command-word
 	 * parameter.
 	 */
 	@Override
 	public List<String> getRequiredParameterNames() {
 		
 		List<String> names = new ArrayList<String>();
 		names.add( "password" );
 		return names;
 	}
 	
 	@Override
 	public String getCommandWord() { 
 		return COMMAND_WORD;
 	}
 
 }
