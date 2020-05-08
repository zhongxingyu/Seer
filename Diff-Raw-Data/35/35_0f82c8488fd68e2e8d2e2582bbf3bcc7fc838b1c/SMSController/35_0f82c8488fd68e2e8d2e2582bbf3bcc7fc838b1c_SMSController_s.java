 package final_project.control;
 
 import final_project.model.*;
 import java.net.*;
 import java.util.Collection;
 import java.io.*;
 
 /**
  * SMSController Class
  * This class handles sending out text messages to the instances of
  * the IMessagable interface stored in the IPersonStore class.
  * 
  * @author mksteele
  */
 
 public class SMSController implements Constants, Runnable{
 	
 	IDataStore _store; /* Reference to IDataStore */
 	int _lastRetrievedID;
 	boolean _listening;
 	
 	public SMSController(IDataStore s) {
 		_store = s;
 		_lastRetrievedID = 0;
 		_listening = true;
 
 	}
 
 	
 	/**
 	 * This is the main method that handles sending a certain message to 
 	 * a certain phone number. It is called by all of the other, more 
 	 * specialized send methods below.
 	 * 
 	 * @param message
 	 * @param number
 	 */
 	public void sendMessage(String message, String number) {
 		OutputStreamWriter wr = null;
 		BufferedReader rd = null;
 		try {
 			//Constructing the data
 			String data = "";
             data += "username=" + URLEncoder.encode("cs032fencing", "ISO-8859-1");
             data += "&password=" + URLEncoder.encode("F3ncing!", "ISO-8859-1");
             data += "&message=" + URLEncoder.encode(message, "ISO-8859-1");
             data += "&want_report=1";
             data += "&msisdn=1" + number;
 
             URL url = new URL("http://usa.bulksms.com:5567/eapi/submission/send_sms/2/2.0");
             URLConnection conn = url.openConnection();
             conn.setDoOutput(true);
             wr = new OutputStreamWriter(conn.getOutputStream());
             wr.write(data);
             wr.flush();
             
             // Get the response
             rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             String line;
             while ((line = rd.readLine()) != null) {
                 // Print the response output...
                 System.out.println(line);
             }
 
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
             try {
 				if(wr!=null) wr.close();
 				if(wr!=null) rd.close();
 			} catch (IOException e) {
 				e.printStackTrace(); //TODO make more useful
 			}
 		}
 	}
 	
 	/**
 	 * The following methods are convenience methods that the 
 	 * other classes can call so that they don't have to deal 
 	 * with following our SMS protocol. 
 	 */
 
 	//This method allows the admin to send a message to every member 
 	public void sendAllMessage(String message) {
 		for(IPerson i: _store.getPeople()) {
 			this.sendMessage(message, i.getPhoneNumber());
 		}
 	}
 	
 	public void sendGroupMessage(String group, String message) {
 		for(IPerson i: _store.getPeopleForGroup(group)) {
 			this.sendMessage(message, i.getPhoneNumber());
 		}
 	} 
 
 	public void sendFencerStripMessage(int id,  int strip) throws Exception {
 		String message = "Fencer id: " + id + " Strip assignment: " + strip;
 		
 		//Look up the fencer in the database to get their phone number
 		IPerson i = _store.getPerson(id);
 		if(i==null) { //TODO how does will deal with null lookups?
 			throw new Exception("No fencer found for id " + id);
 		}
 
 		String number = i.getPhoneNumber();
 		if(number.equals(INVALID_PHONE_NUMBER)) {
 			throw new Exception("Message could not be sent: No phone number for id " + id);
 		}
 				
 		this.sendMessage(message, number);
 	}
 	
 	/* RECEIVING MESSAGES */
 	
 	public void getInbox() {
 		OutputStreamWriter wr = null;
 		BufferedReader rd = null;
 		try {
 			//Constructing data
 			String data = "";
             data += "username=" + URLEncoder.encode("cs032fencing", "ISO-8859-1");
             data += "&password=" + URLEncoder.encode("F3ncing!", "ISO-8859-1");
             data += "&last_retrieved_id=" + _lastRetrievedID;
 			
             URL url = new URL("http://usa.bulksms.com:5567/eapi/reception/get_inbox/1/1.0");
             URLConnection conn = url.openConnection();
             conn.setDoOutput(true);
             wr = new OutputStreamWriter(conn.getOutputStream());
             wr.write(data);
             wr.flush();
             
             // Get the response
             rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             String line;
             while ((line = rd.readLine()) != null) {
                 // TODO Parse output to get out reply messages
                 System.out.println(line);
             }
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
             try {
 				if(wr!=null) wr.close();
 				if(wr!=null) rd.close();
 			} catch (IOException e) {
 				e.printStackTrace(); //TODO make more useful
 			}
 		}
 	}
 	
 	/**
 	 * SMS PROTOCOL: 
 	 * What kind of messages are people going to be able to sign up for?
 	 * 
 	 * SUBSCRIBING TO A CERTAIN FENCER:
 	 * they should type: "Follow Miranda Steele" --> Full name?
 	 * or for a team: "Follow team Brown"
 	 * 
 	 * STOPPING SUBSCRIPTIONS:
 	 * to stop all subscriptions: "Stop"
 	 * or to just stop a certain fencer: "Stop following Miranda Steele"
 	 * or to stop a certain team: "Stop following team Brown"
 	 *
 	 * OTHER THINGS: ??
 	 * 
 	 * Things we need to be able to handle: 
 	 * - case sensitivity
 	 * - common typos (fencre, follw) --> is this really necessary?
 	 * 
 	 */
 	public void parseMessage(String received, String number) {
 		//First, getting the first word
 		//Then, choosing a possible path based on that word
 
 		//This method will eventually call subscribeUser on a certain name
 	}
 	
	
 	
 	/**
 	 * This method subscribes a user to an IObeservable. 
 	 * 
 	 * @param nameToSubscribeTo
 	 * @param number
 	 * @return
 	 */
 	public boolean subscribeUser(String nameToSubscribeTo, String number) {
 		//Checking to see that the person is registered in the database -- linear search through all people.
 		// TODO: Look up subscriber in IDataStore by phone number
 		boolean found = false;
 		for (IPerson i: _store.getPeople()) {
 			if(i.getPhoneNumber().equals(number)) {
 				found = true;
 				break;
 			}
 		}
 		// Sending an error message 
 		if (!found) {
 			this.sendMessage("This phone number is not registered. Please enter a registration code.", number);
 			return false;
 		}
 		
 		//Checking to see that a name exists in the IPersonStore
 		for (IPerson i: _store.getPeople()) {
 			//if(i.getName().equals(nameToSubscribeTo)) {} TODO
 		}

 		return false;
 	}
 

 	@Override
 	public void run() {
 		while(_listening) {
 			this.getInbox();
 		}
 	}
 }
