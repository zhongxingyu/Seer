 package edu.iit.swyne.crawler.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import edu.iit.swyne.crawler.FeedAlreadyTrackedException;
 
 public class SwyneCrawlerServerProtocol {
 
	public static class ClientExitException extends Exception {
		public ClientExitException(String string) {
			super(string);
		}

		private static final long serialVersionUID = 1L;
	}

 	@SuppressWarnings("serial")
 	private static class ServerNotRunningException extends Exception {
 		public ServerNotRunningException(String string) {
 			super(string);
 		}
 	}
 
 	public static final String SHUTDOWN_SUCCESS	= "100";
 	public static final String SHUTDOWN_FAILURE	= "101";
 	public static final String ADD_SUCCESS		= "102";
 	public static final String ADD_FAILURE		= "103";
 	public static final String REMOVE_SUCCESS	= "104";
 	public static final String REMOVE_FAILURE	= "105";
 	public static final String UNKNOWN_COMMAND	= "107";
 	public static final String UNEXPECTED_ERROR	= "108";
 	
 	private static final String COMMAND_SHUTDOWN	= "shutdown";
 	private static final String COMMAND_ADD			= "add";
 	private static final String COMMAND_REMOVE		= "remove";
 	private static final String COMMAND_EXIT		= "exit";
 
 	private static final String ADD_USAGE = "add <url> <collection> <extractor class>";
 
 	public static final String SHUTDOWN_SUCCESS_MESSAGE							= SHUTDOWN_SUCCESS + " Shutdown Success";
 	public static final String SHUTDOWN_FAILURE_MESSAGE							= SHUTDOWN_FAILURE + " SHUTDOWN_FAILURE";
 	public static final String ADD_SUCCESS_MESSAGE								= ADD_SUCCESS + " Successfully added new feed";
 	public static final String ADD_FAILURE_MESSAGE								= ADD_FAILURE + " ADD_FAILURE Unknown reason";
 	public static final String ADD_FAILURE_MESSAGE_ALREADY_TRACKED				= ADD_FAILURE + " ADD_FAILURE Feed already being tracked";
 	public static final String ADD_FAILURE_MESSAGE_EXTRACTOR_INSTANTIATION 		= ADD_FAILURE + " ADD_FAILURE Couldn't instantiate specified extractor";
 	public static final String ADD_FAILURE_MESSAGE_ILLEGAL_ACCESS 				= ADD_FAILURE + " Insufficient permissions to access specified extractor";
 	public static final String ADD_FAILURE_MESSAGE_EXTRACTOR_NOT_FOUND 			= ADD_FAILURE + " Could not find specified extractor class";
 	public static final String ADD_FAILURE_MESSAGE_NOT_ENOUGH_ARGS				= ADD_FAILURE + " ADD_FAILURE Not enough arguments specified" + ADD_USAGE;
 	public static final String REMOVE_SUCCESS_MESSAGE							= REMOVE_SUCCESS + " Successfully removed feed";
 	public static final String REMOVE_FAILURE_MESSAGE							= REMOVE_FAILURE + " REMOVE_FAILURE Unknown reason";
 	public static final String REMOVE_FAILURE_MESSAGE_NO_URL_GIVEN				= REMOVE_FAILURE + " REMOVE_FAILURE No URL was specified";
 	public static final String UNKNOWN_COMMAND_MESSAGE							= UNKNOWN_COMMAND + " UNKNOWN_COMMAND";
 	public static final String UNEXPECTED_ERROR_MESSAGE							= UNEXPECTED_ERROR + " UNEXPECTED_ERROR";
 	public static final String BAD_URL_FAILURE_MESSAGE 							= UNEXPECTED_ERROR + " FAILURE Malformed URL specified";
 	public static final String INPUT_FAILURE_MESSAGE 							= UNEXPECTED_ERROR + " FAILURE Error reading input from socket";
 	public static final String SERVER_FAILURE_MESSAGE 							= UNEXPECTED_ERROR + " FAILURE Server error";
 
	public static String run(String command, CrawlerServer server) throws ClientExitException {
 		String response = "";
 		
 		BufferedReader in = new BufferedReader(new StringReader(command));
 		try {
 			if (!server.isRunning()) throw new ServerNotRunningException("Swyne crawler server not running.");
 			
 			String line = null;
 			while ((line = in.readLine()) != null) {
 				line = line.trim();
 				String[] args = line.split("\\s");
 				
 				if (args[0].compareToIgnoreCase(COMMAND_SHUTDOWN) == 0) {
 					server.stopServer();
 					if (!server.isRunning()) response += SHUTDOWN_SUCCESS_MESSAGE;
 					else response += SHUTDOWN_FAILURE_MESSAGE;
 					return response;
 				}
 				else if (args[0].compareToIgnoreCase(COMMAND_ADD) == 0) {
 					if (args.length < 4)
 						response += ADD_FAILURE_MESSAGE_NOT_ENOUGH_ARGS;
 					else {
 						URL feedURL = new URL(args[1]);
 						String collection = args[2];
 						String extractorClass = args[3];
 						server.getCrawler().addFeed(feedURL, collection, extractorClass);
 						response += !server.getCrawler().isTrackingFeed(feedURL) ? ADD_FAILURE_MESSAGE : ADD_SUCCESS_MESSAGE+" "+feedURL.toString();
 					}
 				}
 				else if (args[0].compareToIgnoreCase(COMMAND_REMOVE) == 0) {
 					if (args.length < 2)
 						response += REMOVE_FAILURE_MESSAGE_NO_URL_GIVEN;
 					else {
 						URL feedURL = new URL(args[1]);
 						server.getCrawler().removeFeed(feedURL);
 						response += server.getCrawler().isTrackingFeed(feedURL) ? REMOVE_FAILURE_MESSAGE : REMOVE_SUCCESS_MESSAGE+" "+feedURL.toString();
 					}
 				}
 				else if (args[0].compareToIgnoreCase(COMMAND_EXIT) == 0)
					throw new ClientExitException("'exit' command issued by client");
 				else response += UNKNOWN_COMMAND_MESSAGE+" \""+line+"\"";
 				
 				response += "\n";
 			}
 		}
 		catch (FeedAlreadyTrackedException e) {
 			response += ADD_FAILURE_MESSAGE_ALREADY_TRACKED + " \"" + e.getMessage() + "\"\n";
 		} catch (MalformedURLException e) {
 			response += BAD_URL_FAILURE_MESSAGE + " \"" + e.getMessage() + "\"\n";
 		} catch (ClassNotFoundException e) {
 			response += ADD_FAILURE_MESSAGE_EXTRACTOR_NOT_FOUND + " \"" + e.getMessage() + "\"\n";
 		} catch (IOException e) {
 			response += INPUT_FAILURE_MESSAGE + " \"" + e.getMessage() + "\"\n";
 		} catch (ServerNotRunningException e) {
 			response += SERVER_FAILURE_MESSAGE + " \"" + e.getMessage() + "\"\n";
 		}
 		return response;
 	}
 
 }
