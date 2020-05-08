 /*
  * RapidEnv: CmdRenv.java
  *
  * Copyright (C) 2010 Martin Bluemel
  *
  * Creation Date: 05/20/2010
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the
  * GNU Lesser General Public License as published by the Free Software Foundation;
  * either version 3 of the License, or (at your option) any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  * You should have received a copies of the GNU Lesser General Public License and the
  * GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
  */
 package org.rapidbeans.rapidenv.cmd;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.logging.LogManager;
 
 import org.rapidbeans.core.type.TypePropertyCollection;
 import org.rapidbeans.rapidenv.CmdRenvCommand;
 import org.rapidbeans.rapidenv.CmdRenvOption;
 import org.rapidbeans.rapidenv.RapidEnvCmdException;
 import org.rapidbeans.rapidenv.RapidEnvException;
 import org.rapidbeans.rapidenv.RapidEnvInterpreter;
 import org.rapidbeans.rapidenv.config.cmd.ExceptionMap;
 import org.rapidbeans.rapidenv.config.cmd.ExceptionMapping;
 
 /**
  * The main class interpreting an "renv" command line
  *
  * @author Martin Bluemel
  */
 public class CmdRenv {
 
 	/**
 	 * The command main method.
 	 *
 	 * @param args all command line arguments
 	 */
 	public static void main(final String[] args) {
 		int errorcode = 0;
 		CmdRenv command = null;
 		RapidEnvInterpreter interpreter = null;
 		try {
 			final LogManager lm = LogManager.getLogManager();
 			try {
 				lm.readConfiguration(CmdRenv.class.getClassLoader().getResourceAsStream(
 						"org/rapidbeans/rapidenv/logging.properties"));
 			} catch (SecurityException e) {
 				throw new RapidEnvException(e);
 			} catch (FileNotFoundException e) {
 				throw new RapidEnvException(e);
 			} catch (IOException e) {
 				throw new RapidEnvException(e);
 			}
 			TypePropertyCollection.setDefaultCharSeparator(',');
 			command = new CmdRenv(args);
 			interpreter = new RapidEnvInterpreter(command);
 			interpreter.execute();
 		} catch (RapidEnvException e) {
 			if (e.getErrorcode() > 0 && e.getErrorcode() < 10000) {
 				errorcode = e.getErrorcode();
 				final ExceptionMapping mapping = ExceptionMap.load().map(e);
 				String message = "ERROR: ";
 				if (mapping != null) {
 					message += mapping.getMessage(Locale.ENGLISH);
 				} else {
 					message += e.getMessage();
 				}
 				System.out.println(message);
 			} else if (e.getErrorcode() > 20000) {
 				final ExceptionMapping mapping = ExceptionMap.load().map(e);
				String message = "INFO: ";
 				if (mapping != null) {
 					message += mapping.getMessage(Locale.ENGLISH);
 				} else {
 					message += e.getMessage();
 				}
 				System.out.println(message);
 			} else {
 				e.printStackTrace();
 				errorcode = 1;
 			}
		} catch (Throwable t) {
			t.printStackTrace();
			errorcode = 2;
 		} finally {
 			if (command != null && interpreter != null) {
 				switch (command.getCommand()) {
 				case boot:
 				case config:
 				case deinstall:
 				case install:
 				case update:
 					System.out.println("------------------------------------------------------------------------");
 					System.out.println(interpreter.getStatisicsAsString());
 					break;
 				default:
 					break;
 				}
 			}
 			System.exit(errorcode);
 		}
 	}
 
 	// IN
 	/**
 	 * The array of command argument strings.
 	 */
 	private String[] args = null;
 
 	// OUT
 	/**
 	 * the command chosen (default == stat).
 	 */
 	private CmdRenvCommand command = CmdRenvCommand.stat;
 
 	/**
 	 * the options (tagged parameters) containing n string values
 	 */
 	private Map<CmdRenvOption, String[]> options = new HashMap<CmdRenvOption, String[]>();
 
 	/**
 	 * the tool name strings.
 	 */
 	private List<String> installunitOrPropertyNames = new ArrayList<String>();
 
 	/**
 	 * @return the command
 	 */
 	public CmdRenvCommand getCommand() {
 		return command;
 	}
 
 	/**
 	 * @return the toolNames
 	 */
 	public List<String> getInstallunitOrPropertyNames() {
 		return installunitOrPropertyNames;
 	}
 
 	/**
 	 * @return the options
 	 */
 	public Map<CmdRenvOption, String[]> getOptions() {
 		return options;
 	}
 
 	/**
 	 * @return the configuration file as defined by means of the "-env" option.
 	 *         By default the file is "${RAPID_ENV_HOME}/env.xml".
 	 */
 	public File getConfigfile() {
 		File configfile = null;
 		if (this.options.containsKey(CmdRenvOption.env)) {
 			configfile = new File(this.options.get(CmdRenvOption.env)[0]);
 		} else {
 			configfile = new File(System.getenv("RAPID_ENV_HOME"), "env.xml");
 		}
 		return configfile;
 	}
 
 	private enum InterpreterState {
 		options,
 		command,
 		tools
 	}
 
 	/**
 	 * constructor of the command line interpreter.
 	 *
 	 * @param args all arguments from the command line
 	 */
 	public CmdRenv(final String[] args) {
 		this.args = args;
 		this.parse();
 	}
 
 	/**
 	 * Parse the command.
 	 */
 	private void parse() {
 		switch(this.args.length) {
 		case 0:
 			// take the default command and settings file
 			break;
 		default:
 			if (!parseHelp(args[0])
 					&& !parseVersion(args[0])) {
 				InterpreterState state = InterpreterState.options;
 				final int len = this.args.length;
 				for (int i = 0; i < len; i++) {
 					switch (state) {
 					case options:
 						if (this.args[i].startsWith("-")) {
 							i += parseOption(this.args, i);
 						} else {
 							state = InterpreterState.command;
 							i--;
 						}
 						break;
 					case command:
 						this.command = parseCommand(this.args[i]);
 						state = InterpreterState.tools;
 						break;
 					case tools:
 						this.installunitOrPropertyNames.add(args[i]);
 						break;
 					default:
 						throw new AssertionError(
 								"Unforseen interpreter state \"" + state + "\"");
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Parse an option
 	 *
 	 * @param string the option string
 	 *
 	 * @return the count of option arguments
 	 */
 	private int parseOption(final String[] args, final int pos) {
 		int optionArgumentCount = 0;
 		final String strippedOptionString = args[pos].substring(1);
 		CmdRenvOption option = null;
 		try {
 			option = CmdRenvOption.valueOf(strippedOptionString);
 			optionArgumentCount= option.getArgcount();
 		} catch (IllegalArgumentException e) {
 			for (final CmdRenvOption currenOption : CmdRenvOption.values()) {
 				if (currenOption.getShort1().equals(strippedOptionString)) {
 					option = currenOption;
 					break;
 				}
 			}
 			if (option == null) {
 				throw new RapidEnvCmdException("Illegal option: " + args[pos]);
 			}
 		}
 		if (option != null) {
 			final String[] sa = new String[option.getArgcount()];
 			for (int i = 0; i < option.getArgcount(); i++) {
 				sa[i] = args[pos + 1 + i];
 			}
 			this.options.put(option, sa);
 		}
 		return optionArgumentCount;
 	}
 
 	/**
 	 * Parse a command.
 	 *
 	 * @param string the command string.
 	 *
 	 * @return the command enum
 	 */
 	private CmdRenvCommand parseCommand(final String string) {
 		CmdRenvCommand cmd = null;
 		if (string.length() == 1) {
 			for (final CmdRenvCommand curCmd : CmdRenvCommand.values()) {
 				if (string.equals(curCmd.getShort1())) {
 					cmd = curCmd;
 					break;
 				}
 			}
 			if (cmd == null) {
 				throw new RapidEnvCmdException("Illegal command: " + string);
 			}
 		} else {
 			try {
 				cmd = CmdRenvCommand.valueOf(string);
 			} catch (IllegalArgumentException e) {
 				throw new RapidEnvCmdException("Illegal command: " + string);
 			}
 		}
 		return cmd;
 	}
 
 	/**
 	 * Parse an argument for help command given in different variants.
 	 *
 	 * @param arg the argument string to parse
 	 *
 	 * @return if a help command has been detected
 	 */
 	private boolean parseHelp(String arg) {
 		if (arg.equalsIgnoreCase("h") || arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--h")
 				|| arg.equals("?") || arg.equals("-?") || arg.equals("/?") || arg.equals("--?")
 				|| arg.equalsIgnoreCase("help") || arg.equalsIgnoreCase("-help") || arg.equalsIgnoreCase("--help")
 				) {
 			this.command = CmdRenvCommand.help;
 		}
 		return this.command == CmdRenvCommand.help;
 	}
 
 	/**
 	 * Parse an argument for version command given in different variants.
 	 *
 	 * @param arg the argument string to parse
 	 *
 	 * @return if a help command has been detected
 	 */
 	private boolean parseVersion(String arg) {
 		if (arg.equalsIgnoreCase("v") || arg.equalsIgnoreCase("--v")
 				|| arg.equalsIgnoreCase("version") || arg.equalsIgnoreCase("-version") || arg.equalsIgnoreCase("--version")
 				) {
 			this.command = CmdRenvCommand.version;
 		}
 		return this.command == CmdRenvCommand.version;
 	}
 }
