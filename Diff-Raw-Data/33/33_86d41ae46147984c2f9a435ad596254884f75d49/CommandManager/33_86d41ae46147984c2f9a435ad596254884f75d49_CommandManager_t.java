 package de.skuzzle.polly.sdk;
 
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.exceptions.UnknownCommandException;
 import de.skuzzle.polly.sdk.exceptions.UnknownSignatureException;
 
 
 /**
  * This class manages all command related tasks.
  * 
  * @author Simon
  * @since zero day
  * @version RC 1.0
  */
 public interface CommandManager {
 	
 	/**
 	 * Registers a new command and all of its signatures.
 	 * 
 	 * @param cmd The command to add.
 	 * @throws DuplicatedSignatureException If the given commandname is already 
 	 *          registered.
 	 * @throws IllegalArgumentException If the commands name is shorter than 3 characters.
 	 */
 	public abstract void registerCommand(Command cmd) throws DuplicatedSignatureException;
 	
 	
 	/**
 	 * Registers a new command and all of its signatures. The command will be registered
 	 * with the given name, so this method can be used to create command aliases.
 	 * 
 	 * @param as The name as which the command should be added.
 	 * @param cmd The command to add.
      * @throws DuplicatedSignatureException If the given commandname is already 
      *          registered.
      * @throws IllegalArgumentException If the commands name is shorter than 3 characters.
      * @since 0.7
 	 */
 	public abstract void registerCommand(String as, Command cmd) 
 	        throws DuplicatedSignatureException;
 	
 	
 	/**
 	 * Unregisters a command and all of its signatures.
 	 * 
 	 * @param command The command which shall be removed.
 	 * @throws UnknownCommandException If the command you are trying to unregister
 	 * 		does not exist.
 	 */
 	public abstract void unregisterCommand(Command command) 
 			throws UnknownCommandException;
 	
 	
 	
 	/**
 	 * Returns a readonly {@link List} of all currently registered commands.
 	 * @return A set of commands.
 	 */
 	public abstract List<Command> getRegisteredCommands();
 	
 	
 	
 	/**
 	 * Determines whether the given command is currently registered.
 	 * @param cmd The command to check.
 	 * @return <code>true</code> if the command is registered, <code>false</code> 
 	 * 		otherwise.
 	 */
 	public abstract boolean isRegistered(Command cmd);
 	
 	
 	
 	/**
 	 * Determines whether a command with given name is currently registered.
 	 * @param name The command name to check.
 	 * @return <code>true</code> if the command is registered, <code>false</code> 
 	 * 		otherwise.
 	 */
 	public abstract boolean isRegistered(String name);
 	
 	
 	
 	/**
 	 * Retrieves a command with the given signature. If a command with the given 
 	 * signature exists, the passed signatures id is set to the found signatures
 	 * id.
 	 * 
 	 * @param signature The signature of the command to retrieve.
 	 * @return The found command.
 	 * @throws UnknownSignatureException If no command with the given signature exists.
 	 * @throws UnknownCommandException If no command with the signatures name exists.
 	 */
 	public abstract Command getCommand(Signature signature) 
 			throws UnknownSignatureException, UnknownCommandException;
 	
 	
 	
 	/**
 	 * <p>Retrieves a command with given name, disregarding any signatures.</p>
 	 * 
 	 * <p>Attention: Executing the resulting command with any signature may result in 
 	 * random errors, as it does not check whether the signature is compatible to this 
 	 * command. This method therefore should only be used to retrieve infos about
 	 * the command with given name but not for executing it!</p>
 	 * 
 	 * @param name The name of the command to retrieve.
 	 * @return The command with the given name.
 	 * @throws UnknownCommandException If no command with the name exists.
 	 */
 	public abstract Command getCommand(String name) throws UnknownCommandException;
 	
 	
 	
 	/**
 	 * Gets the command and signature for the last command that was executed on the given 
 	 * channel.
 	 * 
 	 * @param channel The channel.
 	 * @return The {@link CommandHistoryEntry} of the last command for that channel or
 	 *             <code>null</code> if no entry for that channel exists.
 	 * @since 0.8
 	 */
 	public abstract CommandHistoryEntry getLastCommand(String channel);
 	
 	
 	
 	/**
 	 * Executes the given String as a polly command.
 	 * 
 	 * @param input The command to parse and execute.
 	 * @param channel The channel in which the command shall be executed.
 	 * @param inQuery Whether the command shall be executed in a query.
 	 * @param executor The executing user.
 	 * @param ircManager The current irc manager isntance. Used to determine context 
 	 *             information for the parser.
	 * @return Whether the command has been executed.
 	 * @throws UnsupportedEncodingException If the parser encounters an unknown encoding.
 	 *             This will rarely happen.
 	 * @throws UnknownSignatureException The command that should be executed has no 
 	 *             signature that matches the parsed inputstring.
 	 * @throws InsufficientRightsException The given user can not execute that command.
 	 * @throws CommandException If the inputstring could not be parsed as a polly command
 	 *             or an error occured during execution of the command.                          
 	 * @throws UnknownCommandException If there is no command with that name.
 	 */
	public abstract boolean executeString(String input, String channel, boolean inQuery, 
 	    User executor, IrcManager ircManager) 
 	                    throws UnsupportedEncodingException, 
                                UnknownSignatureException, InsufficientRightsException, 
                                CommandException, UnknownCommandException;
 }
