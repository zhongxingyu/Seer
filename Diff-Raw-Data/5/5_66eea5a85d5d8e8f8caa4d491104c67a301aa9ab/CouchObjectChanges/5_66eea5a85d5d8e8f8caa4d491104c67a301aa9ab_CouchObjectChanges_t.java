 package dk.teamonline.util.couchDo;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 public class CouchObjectChanges extends CouchObject{
 	private String filter;
 	private String application;
 	private int sequenceNo = 0;
 	private volatile JSONObject changesQueue = new JSONObject(); //contains changes not returned yet
 
 	public CouchObjectChanges( String dbUrl, String application, String filter ) {
 		super(dbUrl);
 		this.application = application;
 		this.filter = filter;
 	}
 	
 	public JSONObject getChangedObject() {
 		return getChangedObject( false );
 	}
 	
 	public JSONObject getChangedObject( boolean acceptTimeout ) {
 		JSONObject changedObject;
 
 		//First check if we already knows about the next change
 		changedObject = getChangeFromQueue();
 
 		//If we have not, get some changes from the database
 		if ( changedObject == null ) {
 			changedObject = getChangeFromDatabase();
 		}
 		
 		//If both proves without result, wait for a change. 
 		//If the database returns with no change try again.
 		do  {
 			changedObject = waitForChange( acceptTimeout );
 		} while( changedObject == null && acceptTimeout == false );
 		
 		return changedObject;
 	}
 	
 	private synchronized JSONObject getChangeFromQueue() {
 		JSONArray resultArray = ( JSONArray ) changesQueue.get( "results" );
 		if ( resultArray == null || resultArray.size() == 0 ) {
 			return null;
 		}
 		
 		JSONObject firstResult = ( JSONObject ) resultArray.get( 0 );
 		if ( firstResult == null ) {
 			return null;
 		}
 		
 		resultArray.remove( 0 );
 		return returnResult( firstResult );
 	}
 	
 	private JSONObject getChangeFromDatabase() {
 		try {
 			URL url = new URL( getDbUrl() + "/_changes?since=" + sequenceNo + "&filter=" + application + "/" + filter );
 			changesQueue = loadJSONObject( url );
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		// After trying to add some results to the changesQueue, we'll try to load one from it.
 		return getChangeFromQueue();
 	}
 	
 	private JSONObject waitForChange( boolean acceptTimeout ) {
 		JSONObject change = null;
 		try {
 			while ( change == null || !change.containsKey( "id" ) ) {
 				// I just want one change. If I choose more, when I will wait longer. 
 				URL url = new URL( getDbUrl() + "/_changes?feed=continuous&limit=1&since=" + sequenceNo + "&filter=" + application + "/" + filter );
 				change = loadJSONObject( url, true );
 				if ( acceptTimeout == true && change == null ) {
 					return null; 
 				}
 			} 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		return returnResult( change );
 	}
 	
 	private synchronized JSONObject returnResult( JSONObject change ) {
 		JSONObject result = null;
 		try {
 			sequenceNo = Integer.parseInt( change.get( "seq" ).toString() );
 			
 			String documentId = URLEncoder.encode( change.get( "id" ).toString(), "UTF-8" );
 			URL changedDocumentUrl = new URL( getDbUrl() + "/" + documentId );
 			result = loadJSONObject( changedDocumentUrl );
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 	
 	private JSONObject loadJSONObject( URL url, boolean oneLine ) throws FileNotFoundException {
 		if ( oneLine == false ) {
 			return loadJSONObject( url );
 		}
 		
 		System.out.println( "Request: " + url.toString() );
 		
 		JSONObject json = null;
 		URLConnection con;
 		try {
 			con = url.openConnection();
 			BufferedReader reader = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
 
 			while( json == null ) {
 				String jsonString = reader.readLine();
 				json = ( JSONObject ) JSONValue.parse( jsonString );
 			}
 			
 			reader.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return json;
 	}
 	
	public int getSequenceNo() {
 		return sequenceNo;
 	}
 
	public void setSequenceNo(int sequenceNo) {
 		this.sequenceNo = sequenceNo;
 	}
 }
