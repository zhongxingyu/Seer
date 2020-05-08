 package de.fhkoeln.gm.serientracker.client.utils;
 
 import de.fhkoeln.gm.serientracker.client.utils.HTTPClient.HTTPMethod;
 import de.fhkoeln.gm.serientracker.jaxb.User;
 import de.fhkoeln.gm.serientracker.utils.Hasher;
 import de.fhkoeln.gm.serientracker.utils.Logger;
 import de.fhkoeln.gm.serientracker.xmpp.utils.ConnectionHandler;
 
 public class LoginHandler {
 
 	private String XMPPHostname;
 	private int XMPPPort;
 	private ConnectionHandler ch;
 	private String username;
 	private char[] password;
 	private String error;
 
 	public LoginHandler( String username, char[] password, String XMPPHostname, int XMPPPort ) {
 		this.ch = ConnectionHandler.getInstance();
 		this.XMPPHostname = XMPPHostname;
 		this.XMPPPort = XMPPPort;
 		this.username = username;
 		this.password = password;
 	}
 
 	public void execute() {
 		if ( this.login() ) {
 			// Create a session
 			SessionStore session = SessionStore.getInstance();
 			// Fetch userdata
 			User userdata = this.fetchUserData();
 
 			if ( this.hasError() ) {
 				this.ch.disconnect();
 			} else {
 				session.setUser( userdata );
 			}
 		}
 	}
 
 	private boolean login() {
 		// Try to connect to the server
 		if ( ! this.ch.connect( XMPPHostname, XMPPPort ) ) {
 			this.error = "Connection failed.";
 			return false;
 		}
 
 		// Try to login
 		if ( ! this.ch.login( username, password, "trackerclient" ) ) {
 			this.error = "Login failed.";
 			return false;
 		}
 
 		return true;
 	}
 
 	public String getErrorMessage() {
 		return this.error;
 	}
 
 	public boolean hasError() {
 		return this.error != null;
 	}
 
 	private User fetchUserData() {
 		Logger.log( "Fetching userdata..." );
 
 		String id = "us_" + Hasher.createHash( username );
 
 		HTTPClient httpClient = new HTTPClient();
 		httpClient.setMethod( HTTPMethod.GET );
 		httpClient.setEndpoint( "/users/" + id );
 		httpClient.execute();
 
 		if ( httpClient.hasError() ) {
 			Logger.err( "HTTPClient has returned an error" );
 
 			if ( httpClient.getResponse().getStatus() == 404 ) {
 				this.error = "The userdata for user '" + username + "' doesn't exists!";
 			} else {
 				this.error = httpClient.getErrorMessage();
 			}
 
 			return null;
 		}
 
 		User user = httpClient.getResponse().getEntity( User.class );
 		Logger.log( "Userdata fetched for " + user.getFirstname() + " " + user.getLastname() );
 
 		return user;
 	}
 
 }
