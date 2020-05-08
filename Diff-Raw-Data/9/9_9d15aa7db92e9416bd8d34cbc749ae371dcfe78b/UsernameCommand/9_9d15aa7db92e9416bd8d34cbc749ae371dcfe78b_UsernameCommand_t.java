 package drexel.edu.blackjack.server.commands;
 
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import drexel.edu.blackjack.server.BlackjackProtocol;
 import drexel.edu.blackjack.server.BlackjackProtocol.STATE;
 import drexel.edu.blackjack.server.ResponseCode;
 
 public class UsernameCommand extends BlackjackCommand {
 
 	private static final String COMMAND_WORD = "USERNAME";
 
 	Set<STATE> validUsernameStates = null;
 
 
 	/**@param protocol The protocol connection that made that
 	 * command. From there the user, state, and all sorts of
 	 * good information can be found.
 	 * @param cm Information derived from the client associated
 	 * with the user, what it sent in to the server
 	 * @return The string that should be sent back to the client
 	 * as the response. 
 	 */
 		public String processCommand(BlackjackProtocol protocol, CommandMetadata cm) {
 			
 			//Step 0: If either object is null, it's an internal error
 			
 			if (protocol == null || cm == null) {
 				return new ResponseCode( ResponseCode.CODE.INTERNAL_ERROR,
 					"UsernameCommand.processCommand() received null arguments").toString();
 			}
 
 			// Step 1-2: Return an error if not in valid state for USERNAME command
 			
 			if(!getValidStates().contains( protocol.getState()) ) {
 				return new ResponseCode( ResponseCode.CODE.NOT_EXPECTING_USERNAME,
 					"UsernameCommand.processCommand() received out-of-context command").toString();
 			}
 			
 			/** Steps 3-4: If the USERNAME username parameter is not exactly one string,
 			 * send an error message; otherwise .*/
			if ((cm.getParameters() == null || cm.getParameters().size() != 1 ))  {
 				
 				return new ResponseCode( ResponseCode.CODE.SYNTAX_ERROR ,
 						"Must include a single parameter indicating username").toString();
 			}
 			
 			/**Steps 6-8: Finally, if USERNAME command has only one parameter:*/
 			
 			/**6: Set the username for user of this protocol instance */
 			
 			String username = cm.getParameters().get(0);
 			
 				protocol.setUsername( username );
 			
 			/**Step 7: Update to next state. The client connected and has given a username, 
 			 * but needs to give a password */
 			
 			protocol.setState(STATE.WAITING_FOR_PASSWORD);
 			
 			/**Step 8: Generate the proper response */
 			return 	new ResponseCode( ResponseCode.CODE.WAITING_FOR_PASSWORD,
 					"UsernameCommand.processCommand() received valid username parameter;" +
 					"waiting for password").toString();	
 			}
 
 		/**
 		 * This returns a set of states that the command can validly be used in.
 		 * The CAPABILITIES command will use this to get a list of capabilities
 		 * for the current protocol state to return
 		 **/	
 
 		public Set<STATE> getValidStates() {
 
 				validUsernameStates = new HashSet<STATE>();
 
 				//Add the only allowed state which is WAITING_FOR_USERNAME
 				validUsernameStates.add(STATE.WAITING_FOR_USERNAME);
 
 				return validUsernameStates;
 		}
 
 		/**
 		 * This returns a list of parameters that the command requires.
 		 * For USERNAME there is a 'username' parameter
 		 * that the command needs. The CAPABILITIES command makes use of this
 		 * for sending the capabilities list
 		 * Parse the username command word into command word and parameter
 		 * and pass back -in an ArrayList- the only parameter USERNAME command word has.
 		 * 
 		 * @return parameter The List with just one string, the username command-word
 		 * parameter.
 		 */
 		public List<String> getRequiredParameterNames() {
 			List<String> names = new ArrayList<String>();
 			names.add( "username" );
 			return names;
 		}
 
 		@Override
 		public String getCommandWord() {
 			return COMMAND_WORD;
 		}
 
 }
