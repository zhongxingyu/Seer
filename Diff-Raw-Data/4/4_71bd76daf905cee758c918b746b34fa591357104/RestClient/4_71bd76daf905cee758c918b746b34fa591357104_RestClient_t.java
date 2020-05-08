 package cz.muni.fi.pa1685.pujcovnaStroju.restclient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.ConnectException;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.xml.sax.SAXException;
 
 import cz.muni.fi.pa165.pujcovnastroju.dto.MachineDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.SystemUserDTO;
 import cz.muni.fi.pa1685.pujcovnaStroju.restclient.util.ClientErrorEnum;
 import cz.muni.fi.pa1685.pujcovnaStroju.restclient.util.MessageResolver;
 
 /**
  * CLI client
  * 
  * @author Michal Merta
  * 
  */
 public class RestClient {
 
 	private static final String BASIC_URL = "http://localhost:8080/pa165/rest/";
 
 	private static final String COMMAND_MACHINE = "machine";
 	private static final String COMMAND_USER = "user";
 
 	private static final String COMMAND_TYPES = "types";
 
 	private static final String COMMAND_HELP = "help";
 	private static final String COMMAND_EXIT = "exit";
 	private static final String COMMAND_LIST = "list";
 	private static final String COMMAND_ADD = "add";
 	private static final String COMMAND_DETAIL = "detail";
 	private static final String COMMAND_UPDATE = "update";
 	private static final String COMMAND_DELETE = "delete";
 	private static final String COMMAND_TIMEOUT = "timeout";
 
 	private static String HELP_CONTENT = null;
 	
 	private static final int DEFAULT_CONNECTION_TIMEOUT = 18000;
 	
 	private static int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT; // in millis
 
 	private static Options machineListOptions = new Options();
 	private static Options machineAddOptions = new Options();
 	private static Options machineUpdateOptions = new Options();
 	private static Options machineDetailOptions = new Options();
 	private static Options machineDeleteOptions = new Options();
 
 	private static Options userListOptions = new Options();
 	private static Options userAddOptions = new Options();
 	private static Options userUpdateOptions = new Options();
 	private static Options userDetailOptions = new Options();
 	private static Options userDeleteOptions = new Options();
 	
 	private static boolean exit;
 
 	/*
 	 * each command has three parts 1) machine || user || help 2) action (list,
 	 * add, update, delete...) 3) arguments of action represented as Option
 	 * objects
 	 * 
 	 * example: machine list machine add --label karel --description karel2
 	 * --type buldozer machine add -l karel -d karel2 -t buldozer
 	 */
 
 	public static void main(String[] args) {
 		
 		initializeOptions();
 		
 		exit = false;
 		String url = null;
 		Scanner scanner = new Scanner(System.in,"utf-8");
 		
 		
 		do {
 			System.out.print("> ");
 			String arg[] = scanner.nextLine().split(
 					"(?<!\\\\)" + Pattern.quote(" "));
 			
 			unescapeArgs(arg);
 			
 			url = getUrl(arg);
 
 			try {
 				if (url == null) continue;
 				
 				String responseString = sendRequest(url);
 				if (responseString == null) {
 					continue;
 				}
 
 				MessageResolver resolver = new MessageResolver(
 						responseString);
 				System.out.println(handleResponse(resolver.getResponse()));
 			} catch (SAXException | ConnectException e) {
 				printError(ClientErrorEnum.CONNECTION_ERROR);
 			} catch (IOException e) {
 				printError(ClientErrorEnum.IO_ERROR);
 				System.exit(2);
 			} catch (ParserConfigurationException e) {
 				printError(ClientErrorEnum.PARSER_CONFIGURATION_ERROR);
 				System.exit(3);
 
 			}
 		} while (!exit);
 	}
 	
 	private static void initializeOptions() {
 		machineListOptions.addOption("l", "label", true, "Label of machine")
 				.addOption("d", "description", true, "Description of Machine")
 				.addOption("t", "type", true, "Type of Machine");
 		machineAddOptions.addOption("l", "label", true, "Label of machine")
 				.addOption("d", "description", true, "Description of Machine")
 				.addOption("t", "type", true, "Type of Machine");
 		machineUpdateOptions.addOption("i", "id", true, "ID of machine")
 				.addOption("l", "label", true, "Label of machine")
 				.addOption("d", "description", true, "Description of Machine")
 				.addOption("t", "type", true, "Type of Machine");
 		machineDetailOptions.addOption("i", "id", true, "ID of machine");
 		machineDeleteOptions.addOption("i", "id", true, "ID of machine");
 
 		userListOptions.addOption("f", "firstName", true, "First name of user")
 				.addOption("l", "lastName", true, "last name of user")
 				.addOption("t", "type", true, "Type of user");
 		userAddOptions.addOption("f", "firstName", true, "First name of user")
 				.addOption("l", "lastName", true, "last name of user")
 				.addOption("t", "type", true, "Type of user");
 		userUpdateOptions.addOption("i", "id", true, "ID of user")
 				.addOption("f", "firstName", true, "First name of user")
 				.addOption("l", "lastName", true, "last name of user")
 				.addOption("t", "type", true, "Type of user");
 		userDeleteOptions.addOption("i", "id", true, "ID of user");
 		userDetailOptions.addOption("i", "id", true, "ID of user");
 	}
 	
 	/**
 	 * returns url string for the given arg
 	 * 
 	 * @param arg
 	 * @return url for the given arg; if command need processing only by
 	 *	    client side, returns null
 	 */
 	private static String getUrl(String[] arg) {
 		String url = null;
 		
 		switch (arg[0]) {
 		case COMMAND_HELP:
 			System.out.println(renderHelp());
 			break;
 		case COMMAND_TIMEOUT:
 			handleTimeout(arg);
 			break;
 		case COMMAND_EXIT:
 			exit = true;
 			break;
 		case COMMAND_TYPES:
 			url = BASIC_URL + "types";
 			break;
 		case COMMAND_MACHINE:
 			url = getMachineUrl(arg);
 			break;
 		case COMMAND_USER:
 			url = getUserUrl(arg);
 			break;
 		default:
 			break;
 		}
 		
 		return url;
 	}
 
 	/**
 	 * renders help content and returns as String
 	 * 
 	 * @return
 	 */
 	private static String renderHelp() {
 		if (HELP_CONTENT == null) {
 			StringBuilder builder = new StringBuilder();
 			builder.append(String.format("%s\t\t%s\n", COMMAND_HELP,
 					"print this help"));
 			builder.append(String.format("%s\t\t%s\n", COMMAND_EXIT,
 					"exit application"));
 			builder.append(String.format("%s\t\t%s\n", COMMAND_TIMEOUT,
 					"return current value of timeout"));
 			builder.append(String.format("%s [time]\t%s\n", COMMAND_TIMEOUT,
 					"set timeout to given [time] in milliseconds; default is "
 							+ DEFAULT_CONNECTION_TIMEOUT));
 			builder.append(String.format("%s\t\t%s\n", COMMAND_TYPES,
 					"display supported types of users and machines"));
 
 			builder.append(getOptionHelp(COMMAND_MACHINE + " " + COMMAND_LIST,
 					machineListOptions));
 			builder.append(getOptionHelp(COMMAND_MACHINE + " " + COMMAND_ADD,
 					machineAddOptions));
 			builder.append(getOptionHelp(
 					COMMAND_MACHINE + " " + COMMAND_DETAIL,
 					machineDetailOptions));
 			builder.append(getOptionHelp(
 					COMMAND_MACHINE + " " + COMMAND_DELETE,
 					machineDeleteOptions));
 			builder.append(getOptionHelp(
 					COMMAND_MACHINE + " " + COMMAND_UPDATE,
 					machineUpdateOptions));
 
 			builder.append(getOptionHelp(COMMAND_USER + " " + COMMAND_LIST,
 					userListOptions));
 			builder.append(getOptionHelp(COMMAND_USER + " " + COMMAND_ADD,
 					userAddOptions));
 			builder.append(getOptionHelp(COMMAND_USER + " " + COMMAND_UPDATE,
 					userUpdateOptions));
 			builder.append(getOptionHelp(COMMAND_USER + " " + COMMAND_DETAIL,
 					userDetailOptions));
 			builder.append(getOptionHelp(COMMAND_USER + " " + COMMAND_DELETE,
 					userDeleteOptions));
 			HELP_CONTENT = builder.toString();
 		}
 		return HELP_CONTENT;
 
 	}
 
 	private static String getOptionHelp(String begin, Options options) {
 		StringBuilder builder = new StringBuilder();
 		Object[] optionArray = options.getOptions().toArray();
 		Option current;
 		builder.append(begin);
 		builder.append("\n");
 		for (int i = 0; i < optionArray.length; i++) {
 			current = (Option) optionArray[i];
 			builder.append(String.format("\t\t-%s  --%s\t%s\n",
 					current.getOpt(), current.getLongOpt(),
 					current.getDescription()));
 		}
 		return builder.toString();
 	}
 
 	/**
 	 * sends request represented by given url
 	 * 
 	 * @param url
 	 * @return String - response
 	 */
 	private static String sendRequest(String url) {
 		BufferedReader reader = null;
 		StringBuffer response = null;
 		try {
 			URL restURL = new URL(url);
 			URLConnection restURLConnection = restURL.openConnection();
 			restURLConnection.setConnectTimeout(connectionTimeout);
 			restURLConnection.setReadTimeout(connectionTimeout);
 
 			reader = new BufferedReader(new InputStreamReader(
 					restURLConnection.getInputStream(), "UTF-8"));
 			response = new StringBuffer();
 			for (String line; (line = reader.readLine()) != null;) {
 				response.append(line);
 			}
 		}
 		catch (MalformedURLException e) {
 			printError(ClientErrorEnum.PARSE_ERROR);
 			return null;
 		}
 		catch (SocketTimeoutException e) {
 			printError(ClientErrorEnum.TIMEOUT_ERROR);
 			return null;
 		}
 		catch (IOException e) {
 			printError(ClientErrorEnum.CONNECTION_ERROR);
 			return null;
 		} finally {
 			if (reader != null)
 				try {
 					reader.close();
 				} catch (IOException ignore) {}
 		}
 		return response.toString();
 	}
 
 	/**
 	 * Transforms response object into readable form
 	 * 
 	 * @param response
 	 * @return
 	 */
 	private static String handleResponse(List<? extends Object> response) {
 		if (response == null) {
 			return "Error occured during processing of response";
 		}
 		if (response.isEmpty()) {
 			return "Nothing was found";
 		}
 		Object sample = response.get(0);
 		StringBuilder builder = new StringBuilder();
 		if (sample instanceof MachineDTO) {
 			builder.append(String.format("%4s | %20s | %15s | %50s\n", "ID",
 					"LABEL", "TYPE", "DESCRIPTION"));
 			char line[] = builder.toString().toCharArray();
 			for (int i = 0; i < line.length; i++) {
 				line[i] = '=';
 			}
 			builder.append(line);
 			builder.append("\n");
 			for (Object machine : response.toArray()) {
 				if (machine instanceof MachineDTO) {
 					builder.append(formatMachine((MachineDTO) machine));
 				}
 			}
 			return builder.toString();
 		}
 		if (sample instanceof String) {
 			for (Object message : response.toArray()) {
 				if (message instanceof String) {
 					builder.append(formatMessage((String) message));
 				}
 			}
 			return builder.toString();
 		}
 
 		if (sample instanceof SystemUserDTO) {
 			builder.append(String.format("%4s | %15s | %20s | %20s\n", "ID",
 					"TYPE", "FIRST NAME", "LAST NAME"));
 			char line[] = builder.toString().toCharArray();
 			for (int i = 0; i < line.length; i++) {
 				line[i] = '=';
 			}
 			builder.append(line);
 			builder.append("\n");
 			for (Object user : response.toArray()) {
 				if (user instanceof SystemUserDTO) {
 					builder.append(formatUser((SystemUserDTO) user));
 				}
 			}
 			return builder.toString();
 		}
 		if (sample instanceof List<?>) {
 			builder.append("---machine types---\n");
 			List<?> machineTypes = (List<?>) response.get(0);
 			for (Object mType : machineTypes.toArray()) {
 				if (mType instanceof String) {
 					builder.append(mType);
 					builder.append("\n");
 				}
 			}
 
 			builder.append("---user types---\n");
 			List<?> userTypes = (List<?>) response.get(1);
 			for (Object uType : userTypes.toArray()) {
 				if (uType instanceof String) {
 					builder.append(uType);
 					builder.append("\n");
 				}
 			}
 			return builder.toString();
 		}
 		return "Nothing found";
 	}
 
 	/**
 	 * sets and prints timeout value
 	 * 
 	 * @param arg 
 	 */
 	private static void handleTimeout(String[] arg) {
 		if (arg.length > 1) {
 			try {
 				int newTimeout = Integer.parseInt(arg[1]);
 				connectionTimeout = newTimeout;
 				System.out.println("Timeout set to " + connectionTimeout + "ms.");
 			} catch (NumberFormatException e) {
 				printError(ClientErrorEnum.TIMEOUT_NUMBER_FORMAT_ERROR);
 			}
 		}
 		else {
 			System.out.println("Timeout is set to " + connectionTimeout + "ms.");
 		}
 	}
 	
 	/**
 	 * prints error according to its string value in error enum
 	 * 
 	 * @param error 
 	 */
 	private static void printError(ClientErrorEnum error) {
 		System.out.println(error.errorString());
 	}
 
 	/**
 	 * returns printable String of {@link MachineDTO}
 	 * 
 	 * @param machine
 	 * @return
 	 */
 	private static String formatMachine(MachineDTO machine) {
 		String label = machine.getLabel();
 		String description = machine.getDescription();
 		if (label != null && label.length() > 17) {
 			label = label.substring(0, 16) + "...";
 		}
 		if (description != null && description.length() > 47) {
 			description = description.substring(0, 46) + "...";
 		}
 
 		return String.format("%4s | %20s | %15s | %50s\n", machine.getId()
 				.toString(), label, machine.getType().getTypeLabel(),
 				description);
 	}
 
 	/**
 	 * returns printable String of {@link SystemUserDTO}
 	 * 
 	 * @param user
 	 * @return
 	 */
 	private static String formatUser(SystemUserDTO user) {
 		String firstName = user.getFirstName();
 		String lastName = user.getLastName();
 		if (firstName != null && firstName.length() > 20) {
 			firstName = firstName.substring(0, 17) + "...";
 		}
 		if (lastName != null && lastName.length() > 20) {
 			lastName = lastName.substring(0, 17) + "...";
 		}
 		return String.format("%4s | %15s | %20s | %20s\n", user.getId(), user
 				.getType().getTypeLabel(), firstName, lastName);
 
 	}
 
 	/**
 	 * returns printable String of message
 	 * 
 	 * @param message
 	 * @return
 	 */
 	private static String formatMessage(String message) {
 		return message + "\n";
 	}
 
 	/**
 	 * returns url string for the given arg for machine-connected commands
 	 * 
 	 * @param arg
 	 * @return
 	 */
 	private static String getMachineUrl(String[] arg) {
 		CommandLineParser parser = new BasicParser();
 		CommandLine cmd;
 		String fixedArgs[] = null;
 		String url = null;
 		StringBuilder builder;
 		try {
 			if (arg.length < 2) throw new ParseException("");
 			
			arg[0] = arg[0].toLowerCase();
			arg[1] = arg[0].toLowerCase();
 			switch (arg[1]) {
 			case COMMAND_LIST:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(machineListOptions, fixedArgs);
 
 				builder = new StringBuilder(BASIC_URL + COMMAND_MACHINE);
 				builder.append("/" + COMMAND_LIST + "/");
 				boolean firstParam = true;
 				if (cmd.getOptionValue("l") != null) {
 					if (firstParam) {
 						builder.append("?");
 						firstParam = false;
 					}
 					builder.append("label=" + URLEncoder.encode(cmd.getOptionValue("l"), "utf-8")
 							+ "&");
 				}
 				if (cmd.getOptionValue("d") != null) {
 					if (firstParam) {
 						builder.append("?");
 						firstParam = false;
 					}
 					builder.append("description="
 							+ URLEncoder.encode(cmd.getOptionValue("d"), "utf-8") + "&");
 				}
 				if (cmd.getOptionValue("t") != null) {
 					if (firstParam) {
 						builder.append("?");
 						firstParam = false;
 					}
 					builder.append("type=" + URLEncoder.encode(cmd.getOptionValue("t"), "utf-8"));
 				}
 				url = builder.toString();
 				break;
 			case COMMAND_DETAIL:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(machineDetailOptions, fixedArgs);
 
 				if (cmd.getOptionValue("i") != null) {
 					url = BASIC_URL + COMMAND_MACHINE + "/"
 							+ COMMAND_DETAIL + "?id="
 							+ URLEncoder.encode(cmd.getOptionValue('i'), "utf-8");
 				}
 				break;
 			case COMMAND_DELETE:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(machineDeleteOptions, fixedArgs);
 
 				if (cmd.getOptionValue("i") != null) {
 					url = BASIC_URL + COMMAND_MACHINE + "/"
 							+ COMMAND_DELETE + "?id="
 							+ URLEncoder.encode(cmd.getOptionValue('i'), "utf-8");
 				}
 				break;
 			case COMMAND_ADD:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(machineAddOptions, fixedArgs);
 
 				if (cmd.getOptionValue("l") != null
 						&& cmd.getOptionValue("d") != null
 						&& cmd.getOptionValue("t") != null) {
 					url = BASIC_URL + COMMAND_MACHINE + "/"
 							+ COMMAND_ADD + "" + "?label="
 							+ URLEncoder.encode(cmd.getOptionValue("l"), "utf-8") + "&description="
 							+ URLEncoder.encode(cmd.getOptionValue("d"), "utf-8") + "&type="
 							+ URLEncoder.encode(cmd.getOptionValue("t"), "utf-8");
 				}
 				break;
 			case COMMAND_UPDATE:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(machineUpdateOptions, fixedArgs);
 
 				if (cmd.getOptionValue("i") != null) {
 					builder = new StringBuilder(BASIC_URL
 							+ COMMAND_MACHINE);
 					builder.append("/" + COMMAND_UPDATE + "/?");
 					builder.append("id=" + URLEncoder.encode(cmd.getOptionValue("i"), "utf-8") + "&");
 					if (cmd.getOptionValue("l") != null) {
 						builder.append("label="
 								+ URLEncoder.encode(cmd.getOptionValue("l"), "utf-8") + "&");
 					}
 					if (cmd.getOptionValue("d") != null) {
 						builder.append("description="
 								+ URLEncoder.encode(cmd.getOptionValue("d"), "utf-8") + "&");
 					}
 					if (cmd.getOptionValue("t") != null) {
 						builder.append("type="
 								+ URLEncoder.encode(cmd.getOptionValue("t"), "utf-8"));
 					}
 					url = builder.toString();
 				}
 				break;
 			}
 			
 			if (url == null) throw new ParseException("");
 			
 		} catch (UnsupportedEncodingException e) {
 			printError(ClientErrorEnum.UNSUPPORTED_ENCODING_ERROR);
 		} catch (ParseException e) {
 			printError(ClientErrorEnum.PARSE_ERROR);
 		}
 		return url;
 	}
 	
 	/**
 	 * returns url string for the given arg for user-connected commands
 	 * 
 	 * @param arg
 	 * @return
 	 */
 	private static String getUserUrl(String[] arg) {
 		CommandLineParser parser = new BasicParser();
 		CommandLine cmd;
 		String fixedArgs[] = null;
 		String url = null;
 		StringBuilder builder;
 		try {
 			if (arg.length < 2) throw new ParseException("");
 			
 			switch (arg[1]) {
 			case COMMAND_LIST:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(userListOptions, fixedArgs);
 
 				builder = new StringBuilder(BASIC_URL + COMMAND_USER);
 				builder.append("/" + COMMAND_LIST + "/");
 				boolean firstParam = true;
 				if (cmd.getOptionValue("f") != null) {
 					if (firstParam) {
 						builder.append("?");
 						firstParam = false;
 					}
 					builder.append("firstName="
 							+ URLEncoder.encode(cmd.getOptionValue("f"), "utf-8") + "&");
 				}
 				if (cmd.getOptionValue("l") != null) {
 					if (firstParam) {
 						builder.append("?");
 						firstParam = false;
 					}
 					builder.append("lastName="
 							+ URLEncoder.encode(cmd.getOptionValue("l"), "utf-8") + "&");
 				}
 				if (cmd.getOptionValue("t") != null) {
 					if (firstParam) {
 						builder.append("?");
 						firstParam = false;
 					}
 					builder.append("type=" + URLEncoder.encode(cmd.getOptionValue("t"), "utf-8"));
 				}
 				url = builder.toString();
 				break;
 
 			case COMMAND_DETAIL:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(userDetailOptions, fixedArgs);
 
 				if (cmd.getOptionValue("i") != null) {
 					url = BASIC_URL + COMMAND_USER + "/"
 							+ COMMAND_DETAIL + "?id="
 							+ URLEncoder.encode(cmd.getOptionValue('i'), "utf-8");
 				}
 				break;
 			case COMMAND_DELETE:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(userDetailOptions, fixedArgs);
 
 				if (cmd.getOptionValue("i") != null) {
 					url = BASIC_URL + COMMAND_USER + "/"
 							+ COMMAND_DELETE + "?id="
 							+ URLEncoder.encode(cmd.getOptionValue('i'), "utf-8");
 				}
 				break;
 
 			case COMMAND_ADD:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(userAddOptions, fixedArgs);
 
 				if (cmd.getOptionValue("f") != null
 						&& cmd.getOptionValue("l") != null
 						&& cmd.getOptionValue("t") != null) {
 					url = BASIC_URL + COMMAND_USER + "/" + COMMAND_ADD
 							+ "" + "?firstName="
 							+ URLEncoder.encode(cmd.getOptionValue("f"), "utf-8") + "&lastName="
 							+ URLEncoder.encode(cmd.getOptionValue("l"), "utf-8") + "&type="
 							+ URLEncoder.encode(cmd.getOptionValue("t"), "utf-8");
 				}
 				break;
 
 			case COMMAND_UPDATE:
 				fixedArgs = Arrays.copyOfRange(arg, 2, arg.length);
 				cmd = parser.parse(userUpdateOptions, fixedArgs);
 
 				if (cmd.getOptionValue("i") != null) {
 					builder = new StringBuilder(BASIC_URL
 							+ COMMAND_USER);
 					builder.append("/" + COMMAND_UPDATE + "/?");
 					builder.append("id=" + URLEncoder.encode(cmd.getOptionValue("i"), "utf-8") + "&");
 					if (cmd.getOptionValue("f") != null) {
 						builder.append("firstName="
 								+ URLEncoder.encode(cmd.getOptionValue("f"), "utf-8") + "&");
 					}
 					if (cmd.getOptionValue("l") != null) {
 						builder.append("lastName="
 								+ URLEncoder.encode(cmd.getOptionValue("l"), "utf-8") + "&");
 					}
 					if (cmd.getOptionValue("t") != null) {
 						builder.append("type="
 								+ URLEncoder.encode(cmd.getOptionValue("t"), "utf-8"));
 					}
 					url = builder.toString();
 				}
 				break;
 			}
 			
 			if (url == null) {
 				throw new ParseException("");
 			}
 		} catch (UnsupportedEncodingException ex) {
 			printError(ClientErrorEnum.UNSUPPORTED_ENCODING_ERROR);
 		} catch (ParseException e) {
 			printError(ClientErrorEnum.PARSE_ERROR);
 		}
 		return url;
 	}
 
 	/** unescape giveg array of arguments
 	 * 
 	 * @param arg
 	 */
 	private static void unescapeArgs(String arg[]) {
 		for (int i = 0; i < arg.length; i++) {
 			arg[i] = StringEscapeUtils.unescapeJava(arg[i]);
 		}
 	}
 
 }
