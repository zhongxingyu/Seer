 package session;
 
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.concurrent.*;
 
 public class Session {
 	private String sessionID;
 	private Integer versionNumber;
 	private Timestamp expires = new Timestamp(0);
 	protected static ConcurrentHashMap<String, String[]> sessionTable = new ConcurrentHashMap<String, String[]>();
 	
 	/**Creates session object*/
 	public Session(){
 		Date date = new Date();
 		Timestamp stamp = new Timestamp(date.getTime());
 		//Convert stamp to seconds
 		expires.setTime((stamp.getTime()/1000) + 600);
 		versionNumber = 0;
 	}
 	
 	/**Create new session*/
 	public void getSession(String data, String clientIP){
 		//Creating UniqueID
 		String sID = (clientIP + this.expires.toString()).replaceAll("[^0-9]","");
 		setSessionID(sID);
 		
 		//Add This Session Object To The ConcurrentHashtable
 		writeData(data);
 	}
 	/**Fetches existing session*/
 	public void fetchSession(String sID){
 		Date date = new Date();
 		Timestamp stamp = new Timestamp(date.getTime());
 		expires.setTime(stamp.getTime() + 6000000);
 		setSessionID(sID);
 	}
 	
 	protected String getSessionID() {
 		return sessionID;
 	}
 	
 	protected void setSessionID(String sessionID) {
 		this.sessionID = sessionID;
 	}
 	
 	protected int getVersionNumber() {
 		return versionNumber;
 	}
 
 	protected Timestamp getExpires() {
 		return expires;
 	}
 	
 	protected void writeData(String data){
 		versionNumber++;
		String[] temp = {data, versionNumber.toString(), expires.toString()};
 		sessionTable.put(sessionID, temp);
 	}
 	
 	protected String readData(){
 		try
 		{
 			return sessionTable.get(sessionID)[0];
 		}
 		catch(NullPointerException e){
 			return "Error: Session ID not found";
 		}
 	}
 }
