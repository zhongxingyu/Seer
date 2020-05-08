 package com.covermymeds.claimserverpost;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import java.net.URI;
 import java.awt.Desktop;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpResponseException;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 
 /*
  * Http Post to a claim Server and open point browser to response
  */
 public class ClaimServerPost {
 
 	private static Map<Integer, String> errors = createErrors();
 	private static final int FAIL = 1;
 	private static final int EMPTYRESPONSE = 2;
 
 	// Parameters to send in POST
 	private static String url = null;
 	private static String username = null;
 	private static String password = null;
 	private static String claim = null;
 	private static String apiKey = null;
 
 	/*
 	 * Sample Post request to claims server.
 	 */
 	public static void main(String[] args) throws ClientProtocolException,
 			IOException, URISyntaxException {
 
 		// Parse commandLine options
 		JCommandLine parsedObject = null;
 		try {
 			parsedObject = buildParser(args);
 		} catch (ParameterException e) {
 			System.err.println("Error: " + e.getMessage());
 			printUsage(new JCommandLine());
 			System.exit(FAIL);
 		}
 
 		// Validate that a claim is present
		if (claimSupplied(parsedObject)) {
 			// Assign values to POST parameters
 			url = parsedObject.getService_url();
 			username = parsedObject.getUsername();
 			password = parsedObject.getPassword();
 			apiKey = parsedObject.getApiKey();
 			claim = getClaim(parsedObject);
 
 			// Create an instance of HttpClient.
 			HttpClient client = new DefaultHttpClient();
 
 			// Creat and Encode parameters
 			List<BasicNameValuePair> formparams = Arrays.asList(
 					new BasicNameValuePair("username", username),
 					new BasicNameValuePair("password", password),
 					new BasicNameValuePair("ncpdp_claim", claim),
 					new BasicNameValuePair("physician_fax", apiKey));
 			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams,
 					"UTF-8");
 
 			// Create HttpPost and set its Entity
 			HttpPost httpPost = new HttpPost(url);
 			httpPost.setEntity(entity);
 			boolean failed = false;
 			try {
 				// Execute POST with BasicResponseHandler object
 				String response = client.execute(httpPost,
 						new BasicResponseHandler());
 
 				// Check for empty response;
 				if (response.isEmpty()) {
 					System.out.println("Warning: Empty response, check parameters entered.");
 					System.exit(EMPTYRESPONSE);
 				} else {
 					String[] addresses = response.split("\n");
 
 					// If supported and suppress not present, open browser and
 					// point to each address
 					if (parsedObject.isSuppress()) {
 						if (parsedObject.isVerbose()) {
 							for (String address : addresses) {
 								System.out.println("Request created at: "+ address);
 							}
 						} else {
 							if (Desktop.isDesktopSupported()) {
 								Desktop desktop = Desktop.getDesktop();
 								if (desktop.isSupported(Desktop.Action.BROWSE)) {
 									for (String address : addresses) {
 										if (parsedObject.isVerbose()) {
 											System.out.println("Opening browser to: " + address);
 										}
 										desktop.browse(new URI(address));
 									}
 								} else {
 									System.err.println("Browse action is not supported.");
 									failed = true;
 								}
 							} else {
 								System.err.println("Desktop is not supported.");
 								failed = true;
 							}
 						}
 					}
 				}
 			}
 			// Catch any errors(400,500...) and handle them as appropriate
 			catch (HttpResponseException e) {
 				if (parsedObject.isVerbose()) {
 					System.err.println(e.getStatusCode() + errors.get(e.getStatusCode()));
 				} else {
 					System.err.println(e.getStatusCode());
 				}
 				failed = true;
 			}
 			// Catch generally any malformed urls passed to HttpPost object
 			catch (IllegalStateException e) {
 				System.err.println("Error: " + e.getMessage()
 						+ " Check server url. Value passed was "
 						+ parsedObject.getService_url());
 				failed = true;
 			} finally {
 				// Close all connections and free up system resources
 				client.getConnectionManager().shutdown();
 				if (failed) {
 					System.exit(FAIL);
 				}
 			}
 		} else {
 			System.err
 					.println("Error: must specify a claim file argument or '-' to read from stdin");
 			System.exit(FAIL);
 		}
 	}
 
 	/*
 	 * Parses arguments and returns an object containing the values for each
 	 * option
 	 */
 	private static JCommandLine buildParser(String[] args)
 			throws ParameterException {
 		JCommandLine parsedObject = new JCommandLine();
 		//Parsed Object by calling JCommander Constructor
 		new JCommander(parsedObject, args);
 		return parsedObject;
 	}
 
 	/*
 	 * Checks that a claim is present as either a file or directly as a String
 	 */
	private static boolean claimSupplied(JCommandLine parsedObject) {
 		File claimFile = parsedObject.getClaimInFile();
 		return ((claimFile != null && claimFile.exists()) || parsedObject.readFromStdin());
 	}
 
 	/*
 	 * Returns the claim as a String
 	 */
 	private static String getClaim(JCommandLine parsedObject)
 			throws IOException {
 		if (parsedObject.readFromStdin()) {
 			System.out.println("Enter claim:");
 			Scanner stdin = new Scanner(System.in);
 			return stdin.nextLine();
 		} else {
 			File claimFile = parsedObject.getClaimInFile();
 			BufferedReader reader = new BufferedReader(
 					new FileReader(claimFile));
 			char[] buf = new char[1024];
 			int numRead = 0;
 			StringBuilder fileData = new StringBuilder();
 			while ((numRead = reader.read(buf)) != -1) {
 				String readData = String.valueOf(buf, 0, numRead);
 				fileData.append(readData);
 			}
 			return fileData.toString();
 		}
 	}
 
 	/*
 	 * Returns a map conatining errors
 	 */
 	private static Map<Integer, String> createErrors() {
 		Map<Integer, String> result = new HashMap<Integer, String>();
 		result.put(400, ". Oops, there was a connection problem. Please"
 				+ " try one more time, then contact CoverMyMeds at 1-866-452-"
 				+ "5017/help@covermymeds.com and they will help you diagnose"
 				+ " this issue.");
 		result.put(403, ".Oops, login failed for the username or password that"
 						+ " was submitted. Please check the username and password in your"
 						+ " account settings in your Pharmacy System and at the CMM website to"
 						+ " make sure they match. If you still have trouble, please contact"
 						+ " CoverMyMeds at 1-866-452-5017/help@covermymeds.com and they will"
 						+ " help you fix this issue.");
 		result.put(404, ". Oops, there was a problem. Please check the username and"
 						+ " password in your account settings in your Pharmacy System and at the"
 						+ " CMM website to make sure they match. If you still have trouble, please"
 						+ " contact CoverMyMeds at 1-866-452-5017/help@covermymeds.com and they will"
 						+ " help you fix this issue.");
 		result.put(408, ". Oops, there was a timeout. Please try the request again in one"
 						+ " minute. If you still have trouble, please contact CoverMyMeds at 1-866-452"
 						+ "-5017/help@covermymeds.com and they will help you fix this issue.");
 		result.put(500, ". Oops, there was a problem. Please try the request again in one minute."
 						+ " If you still have trouble, please contact CoverMyMeds at 1-866-452-5017"
 						+ "/help@covermymeds.com and they will help you diagnose this issue.");
 		return Collections.unmodifiableMap(result);
 	}
 
 	/*
 	 * Prints possible options associated with object
 	 */
 	private static void printUsage(Object object) {
 		JCommander help = new JCommander(object);
 		help.setProgramName("ClaimServerPost");
 		help.usage();
 	}
 }
