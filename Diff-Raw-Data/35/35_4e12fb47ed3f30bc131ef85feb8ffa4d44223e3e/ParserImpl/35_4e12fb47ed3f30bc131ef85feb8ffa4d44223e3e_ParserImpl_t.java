 package quoridor;
 
 import java.util.Hashtable;
 import java.util.Queue;
 import java.util.LinkedList;
 
 import java.util.Scanner;
 
 
 /**
  * 
  * This is a framework for parsing and handling command line
  * user input. It allows the assignment of commands to tokens
  * and a number of arguments.
  * 
  * @author Stephen Sherratt
  *
  */
 public class ParserImpl implements Parser {
 	
 	//flag so the first command is not removed
 	protected boolean firstRun;
 	
 	//stores all valid commands and information regarding
 	protected Hashtable <String, CommandSpec> syntax;
 	
 	//contains un-returned, valid commands
 	protected Queue <CommandEntry> commandBuffer;
 	
 	//for scanning commands
 	protected Scanner scanner;
 	
 	/*
 	 * This is returned if invalid input is given.
 	 * It must have its own variable, since the class using the
 	 * parser must be able to specify an invalid token (otherwise
 	 * they wouldn't know what to check for).
 	 */
 	protected short invalid;
 	
 	/**
 	 * Constructor for creating a new ParserImpl from a list of
 	 * commands, tokens, number of arguments and invalid token
 	 * 
 	 * @param commands
 	 * 			An array of strings storing all valid commands for
 	 * 			the parser
 	 * @param tokens
 	 * 			An array of tokens that correspond to the strings
 	 * 			in the commands array
 	 * @param argCount
 	 * 			The number of arguments that belong to each command
 	 * @param invalid
 	 * 			A token to be returned in the event that no valid
 	 * 			command is parsed
 	 */
 	public ParserImpl(String commands[], 
 			          short tokens[], 
 			          short argCount[],
 			          short invalid) {
 		/*
 		 * Each array element is associated with one in different
 		 * arrays so all arrays must be of the same length
 		 */
 		assert(commands.length == tokens.length &&
 				 tokens.length == argCount.length);
 		
 		//initialise some variables
 		syntax = new Hashtable <String, CommandSpec> ();
 		commandBuffer = new LinkedList <CommandEntry> ();
 		scanner = new Scanner(System.in);
 		firstRun = true;
		this.invalid = invalid;
 		
 		//create the syntax table
 		for (int i = 0;i<commands.length; i++) {
 			
 			syntax.put(commands[i], 
 					new CommandSpec(commands[i], 
 							        tokens[i], 
 							        argCount[i]));
 		}
 		
 
 	}
 	
 
 	/**
 	 * Returns true if the current command has its specified number
 	 * of arguments
 	 * @return true if the current command has its specified number
 	 * of arguments
 	 */
 	public boolean hasRequiredArgs() {
 		CommandEntry current = commandBuffer.peek();
 		
 		return current.getArgs().size() == 
 			syntax.get(current.getCommand()).getArgCount();
 	}
 	
 	/**
 	 * Returns true if the current command has a non-zero number
 	 * of arguments that is not its required number
 	 * @return true if the current command has a non-zero number
 	 * of arguments that is not its required number
 	 */
 	public boolean hasErroneousArgs() {
 		CommandEntry current = commandBuffer.peek();
 		
 		return current.getArgs().size() != 0 &&
 			!hasRequiredArgs();
 	}
 	
 	/**
 	 * Checks if the current command has any un-returned
 	 * arguments
 	 * @return true if the current command has any un-returned
 	 * arguments 
 	 */
 	@Override
 	public boolean hasNextArg() {
 		return !commandBuffer.peek().getArgs().isEmpty();
 	}
 
 	/**
 	 * Checks if there is currently a token in the command buffer
 	 * @return true if there is currently a token in the command
 	 * buffer
 	 */
 	@Override
 	public boolean hasNextToken() {
 		return !commandBuffer.isEmpty();
 	}
 	
 	/**
 	 * Returns the next un-returned argument of the current command
 	 * and then removes it from the list of arguments.
 	 * @return the next un-returned argument of the current command
 	 */
 	@Override
 	public String nextArg() {
 		return commandBuffer.peek().getArgs().remove(); 
 	}
 	
 	/**
 	 * Returns the token of the current command
 	 * @return the token of the current command
 	 */
 	@Override
 	public short currentToken() {
 		CommandEntry currentCmd = commandBuffer.peek();
 		return syntax.get(currentCmd.getCommand()).getToken();
 	}
 	
	@Override
	public String currentCommand() {
		CommandEntry currentCmd = commandBuffer.peek();
		return syntax.get(currentCmd.getCommand()).getCommand();
	}

	
 	/**
 	 * Removes the current command from the command buffer to expose
 	 * the next command
 	 */
 	@Override
 	public void progressToken() {
 		
 		commandBuffer.remove();
 		
 	}
 
 	/**
 	 * A wrapper for scanning and returning the token of a command
 	 * from standard input or the next command in the  command buffer
 	 * @return token of a command from standard input or the next 
 	 * command in the  command buffer
 	 */
 	@Override
 	public short ensureCommand() {
 
 		short token = invalid;
 		
 		if (firstRun) {
 			firstRun = false;
 		} else if (hasNextToken()){
 			progressToken();
 		}
 		
 		if (!hasNextToken()) {
 			if (scanLine()) {
 				token = currentToken();
 			}
 		}
 		
 		return token;
 	}
 
 
 
 	/**
 	 * Scans a line of input and returns true iff it contained a
 	 * valid command
 	 * @return true iff input contained a valid command
 	 */
 	@Override
 	public boolean scanLine() {
 		
 		boolean success;
 		
 		CommandEntry tmp = new CommandEntry(scanner.nextLine());
 		
 		if (syntax.containsKey(tmp.getCommand())) {
 			commandBuffer.add(tmp);
 			success = true;
 		} else {
 			success = false;
 		}
 		
 		return success;
 		
 	}
 	
 	/**
 	 * Returns a list of valid commands
 	 * @return a list of valid commands
 	 */
 	@Override
 	public String toString() {
 		String cmdList = "Commands:\n";
 		
 		for (CommandSpec cmd : syntax.values()) {
 			cmdList = cmdList.concat(cmd.getCommand()).concat("\n");
 		}
 		
 		return cmdList;
 	}
 	

 	
 	/**
 	 * 
 	 * A description of a command, grouped into a class for 
 	 * convenience
 	 * 
 	 * @author steve
 	 *
 	 */
 	private class CommandSpec {
 		protected String command;
 		protected short argCount;
 		protected short token;
 		
 		/**
 		 * Constructor for a CommandSpec from a command, token and
 		 * number of arguments.
 		 * @param command
 		 * 			the text that must be entered by the user to
 		 * 			invoke the command
 		 * @param token
 		 * 			a number associated with the command
 		 * @param argCount
 		 * 			the number of arguments required by the command
 		 */
 		private CommandSpec(String command, short token, short argCount) {
 			this.command = command;
 			this.argCount = argCount;
 			this.token = token;
 		}
 		
 		/**
 		 * Returns the text for the command
 		 * @return the text for the command
 		 */
 		protected String getCommand() {
 			return command;
 		}
 		
 		/**
 		 * Returns the number of arguments required by the command
 		 * @return the number of arguments required by the command
 		 */
 		protected short getArgCount() {
 			return argCount;
 		}
 		
 		/**
 		 * Returns the token associated with the command
 		 * @return the token associated with the command
 		 */
 		protected short getToken() {
 			return token;
 		}
 	}
 	
 	/**
 	 * 
 	 * This stores a line of text (usually from standard input) that
 	 * is split up, delimitered by spaces and seperated into the
 	 * command name and its arguments.
 	 * 
 	 * @author steve
 	 *
 	 */
 	private class CommandEntry {
 		protected String command;
 		protected Queue <String> args;
 		
 		/**
 		 * Creates a new CommandEntry based on a line of text
 		 * @param input
 		 * 			the line of text on which the CommandEntry is
 		 * 			based
 		 */
 		private CommandEntry(String input) {
 			args = new LinkedList <String> ();
 			parseInput(input);
 		}
 		
 		/**
 		 * Scan the line of text, split it into words and store the
 		 * command and arguments in their respective variables.
 		 * @param input the line of text to scan
 		 */
 		private void parseInput(String input) {
 			
 			//split the input up into "words" seperated by spaces
 			String[] broken = input.split(" ");
 			
 			//if there is a first word, store it as the command
 			if (broken.length > 0) {
 				command  = broken[0];
 			}
 			
 			//put the other words into the list of arguments
 			for (int i = 1;i<broken.length;i++) {
 				args.offer(broken[i]);
 			}
 			
 		}
 		
 		/**
 		 * Returns the command text
 		 * @return the command text
 		 */
 		private String getCommand() {
 			return command;
 		}
 		
 		/**
 		 * Returns the list of arguments passed to the command
 		 * @return the list of arguments passed to the command
 		 */
 		private Queue <String> getArgs() {
 			return args;
 		}
 	}
 
 
 
 }
