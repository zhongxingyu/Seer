 package fi.iki.joker.sevedroid;
 
 /**
  * This is an utility class to communicate with the severa API
  * It uses HTTP(S) client and XML pull parser to handle the comms. For the time being this seems viable
  * but if this class explodes in functionality, consider taking some SOAP client library into use
  * (like ksoap for android or something).
  * 
  * To manage the size of this class, all of the constants (the soap messages) are in the
  * SevedroidConstants class.
  * 
  * @author juha
  *
  */
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import android.app.Activity;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.http.AndroidHttpClient;
 import android.util.Log;
 
 public class SeveraCommsUtils {
 
 	private static final String TAG = "Sevedroid";
 	private String apiKey = null;
 	private AndroidHttpClient httpClient = null;
 	
 	private class S3Response {
 		// magic number to denote device is not connected - could be anything, just not a valid HTTP status code.
 		private static final int RESPONSE_DEVICE_NOT_CONNECTED = 219913; 
 		private int responseCode = 0;
 		private String responseXML = "";
 		public int getResponseCode() {
 			return responseCode;
 		}
 		public void setResponseCode(int responseCode) {
 			this.responseCode = responseCode;
 		}
 		public String getResponseXML() {
 			return responseXML;
 		}
 		public void setResponseXML(String responseXML) {
 			this.responseXML = responseXML;
 		}
 		
 		public String toString() {
 			return "Response code:"+this.responseCode+": XML: "+this.responseXML;
 		}
 	}
 	
 	/**
 	 * Utility method to safely get all bytes out of the inputstream
 	 * @param is
 	 * @return
 	 * @throws IOException
 	 */
 	
 	private byte[] getBytes(InputStream is) throws IOException {
 
 	    int len;
 	    int size = 1024;
 	    byte[] buf;
 
 	    if (is instanceof ByteArrayInputStream) {
 	      size = is.available();
 	      buf = new byte[size];
 	      len = is.read(buf, 0, size);
 	    } else {
 	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
 	      buf = new byte[size];
 	      while ((len = is.read(buf, 0, size)) != -1)
 	        bos.write(buf, 0, len);
 	      buf = bos.toByteArray();
 	    }
 	    return buf;
 	  }
 	
 	/**
 	 * Utility method to check if device is connected or not
 	 */
 	
 	public static boolean checkIfConnected(Activity context) {
 		ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
 		NetworkInfo ni = connMgr.getActiveNetworkInfo();
 		if(ni != null && ni.isConnected()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Utility method to make the actual soap call
 	 * @param parent The activity that invoked this call in the first palce
 	 * @param soapMessage the body part of the soap message. This method will append the header part where there's the required API key, but
 	 * caller shoud substitute other parts to make an executable call.
 	 * @return Returns S3Responseobject containing the return code and xml, or null in case of complete fail.
 	 * @throws IOException 
 	 */
 	
 	private S3Response requestWithMessage(Activity parent,String soapEnvelope, String soapAction) {
 		
 		SevedroidContentStore scs = new SevedroidContentStore(parent);
 		String soapMessage = null;
 		S3Response res = new S3Response();
 		apiKey = scs.fetchApiKey();
 		soapMessage = soapEnvelope.replace(SevedroidConstants.API_KEY_SUBSTR, apiKey);
 		Log.d(TAG, "Sending the following message: "+soapMessage);
 		if(apiKey == null) {
 			Log.d(TAG,"API key is null... thus returning null");
 			return null;
 		} else {
 			URL url = null;
 			//TODO: We need some kind of check here whether the device is connected to the internet or not...
 			HttpURLConnection urlConnection = null;
 			try {
 				url = new URL(SevedroidConstants.S3_API_URL);
 			} catch (MalformedURLException e) {
 				Log.e(TAG, "URL to Severa 3 web service is malformed.");
 				throw new IllegalStateException ("Cannot form URLConnection from url:"+SevedroidConstants.S3_API_URL);
 			}
 			byte[] soapBytes = soapMessage.getBytes();
 			Log.d(TAG,"Posting soapBytes:"+soapBytes.toString());
 			int httpResponseCode = 0;
 			String httpResponseMessage = "";
 			if(url != null) {
 				try {
 					urlConnection = (HttpURLConnection)url.openConnection();
 					Log.d(TAG,"Got to url connection: "+urlConnection.toString());
 					urlConnection.setDoInput(true);
 					urlConnection.setDoOutput(true);
 					urlConnection.setRequestMethod("POST");
 					urlConnection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
 					urlConnection.setRequestProperty("SOAPAction", soapAction);
 					urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(soapBytes.length));
 					Log.d(TAG,"Set I/O, method and headers...(length was: "+Integer.toString(soapBytes.length)+")");
 					OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
 					Log.d(TAG,"Writing...");
 					out.write(soapBytes);
 					Log.d(TAG,"Flushing...");
 					out.flush();
 					Log.d(TAG,"Closing...");
 					out.close();
 					httpResponseCode = urlConnection.getResponseCode();
 				    httpResponseMessage = urlConnection.getResponseMessage();
 					InputStream in = new BufferedInputStream(urlConnection.getInputStream());
 				    Log.d(TAG,"Instantiated inputstream.");
 				    byte [] responseBytes = getBytes(in);
 				    in.close();
 				    res.setResponseCode(httpResponseCode);
 				    res.setResponseXML(new String(responseBytes));
 				    //the following just supresses very long XML strings from the debug log...
 				    if(responseBytes.length < 1024) {
 				    	Log.d(TAG,"S3 Response:"+new String(responseBytes));
 				    } else {
 				    	Log.d(TAG,"S3 Response (supressed):"+new String(responseBytes).substring(0, 1024));
 				    }
 				    return res;
 				} catch (SocketTimeoutException ste) {	
 					//TODO: This should be universally handled somehow. Connection problems should be detected and dialog shown..
 					Log.e(TAG, "Got SocketTimeOutException..:"+ste.getMessage(),ste);
 					return null;
 				}  catch (IOException e) {
 					Log.e(TAG, "Got IOException while connecting to S3 SOAP URL..."+e.getMessage(),e);
 					res.setResponseCode(httpResponseCode);
 					res.setResponseXML(null);
 					return res;
 				} finally {
 					if(urlConnection != null) {
 						urlConnection.disconnect();
 					}
 				}
 			} 
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the user info (most importantly, the user object GUID) by doing a search based on first name and last name)
 	 * DANGER: what if there's two persons by the same name, ONLY NOW HANDLES ONE!!!
 	 * @param parent
 	 * @return true if connection attempt was ok, otherwise false
 	 */
 	
 	protected String getUserByName(Activity parent, String fName, String lName) {
 		S3Response res = null;
 		String soapBody = SevedroidConstants.SOAP_AUTHN_ENVELOPE.replace(
 				SevedroidConstants.SOAP_BODY_SUBSTR, SevedroidConstants.GET_USER_BY_NAME_BODY);
 		String soapMessage = soapBody.replace(SevedroidConstants.USER_FIRST_NAME_HERE,fName);
 		soapMessage = soapMessage.replace(SevedroidConstants.USER_LAST_NAME_HERE,lName);	
 		res = requestWithMessage(parent, soapMessage, SevedroidConstants.SOAP_ACTION_GET_USER_BY_NAME);
 		if (res == null) {
 			Log.d(TAG, "Response was null while testing api connection.");
 			return null;
 		} else {
 			Log.d(TAG, "While testing API connection, response was:"+res);
 			if(res.responseCode == SevedroidConstants.CODE_INTERNAL_SERVER_ERROR && (res.responseXML == null)) {
 				return null;
 			} else {
 				return res.responseXML;
 			}
 		}
 	}
 	
 	/**
 	 * This invokes the "GetAllCases" operation. Returns a ton of XML, so caller 
 	 * should prepare for a big String in response.
 	 * @param parent
 	 * @return
 	 */
 	
 	protected String getAllCasesXml(Activity parent) {
 		S3Response res = null;
 		res = requestWithMessage(parent, SevedroidConstants.SOAP_AUTHN_ENVELOPE.replace(
 					SevedroidConstants.SOAP_BODY_SUBSTR, SevedroidConstants.GET_ALL_CASES_BODY),
 					SevedroidConstants.SOAP_ACTION_GET_ALL_CASES);
 		if (res == null) {
 			Log.d(TAG, "Response was null while getting all cases.");
 			return null;
 			
 		} else {
 			Log.d(TAG, "While getting all cases xml, response valueOf was:"+String.valueOf(res.responseXML));
 			if(res.responseCode == SevedroidConstants.CODE_INTERNAL_SERVER_ERROR && (res.responseXML == null)) {
 				return null;
 			} else {
 				return res.responseXML;
 			}
 		}
 	}
 	
 	/**
 	 * This invokes the "GetCaseByGuid" operation. 
 	 * @param parent
 	 * @param caseGuid
 	 * @return
 	 */
 	
 	protected String getCaseXMLByGUID(Activity parent, String caseGuid) {
 		S3Response res = null;
 		String soapMessage = SevedroidConstants.SOAP_AUTHN_ENVELOPE.replace(
 				SevedroidConstants.SOAP_BODY_SUBSTR, SevedroidConstants.GET_CASE_BY_GUID_BODY);
 		soapMessage = soapMessage.replace(SevedroidConstants.CASE_GUID_SUBSTR, caseGuid);
 		res = requestWithMessage(parent, soapMessage,
 					SevedroidConstants.SOAP_ACTION_GET_CASE_BY_GUID);
 		if (res == null) {
 			Log.d(TAG, "Response was null while getting case by case GUID.");
 			return null;
 		} else {
 			Log.d(TAG, "While getting case by case guid xml, response valueOf was:"+String.valueOf(res.responseXML));
 			if(res.responseCode == SevedroidConstants.CODE_INTERNAL_SERVER_ERROR && (res.responseXML == null)) {
 				return null;
 			} else {
 				return res.responseXML;
 			}
 		}
 	}
 	
 	/**
 	 * This invokes the "GetPhasesByCaseGUID" operation. Returns potentially a ton of xml, so caller
 	 * should prepare for a big String in response.
 	 */
 	
 	protected String getPhasesXMLByCaseGUID(Activity parent, String caseGUID) {
 		S3Response res = null;
 		String soapBody = SevedroidConstants.SOAP_AUTHN_ENVELOPE.replace(
 				SevedroidConstants.SOAP_BODY_SUBSTR, SevedroidConstants.GET_PHASES_BY_CASE_GUID_BODY);
 		String soapMessage = soapBody.replace(SevedroidConstants.CASE_GUID_SUBSTR,caseGUID);
 		res = requestWithMessage(parent, soapMessage, SevedroidConstants.SOAP_ACTION_GET_PHASES_BY_CASE_GUID);
 		if (res == null) {
 			Log.d(TAG, "Response was null while getting phases by case guid.");
 			return null;
 		} else {
 			Log.d(TAG, "While getting phases by case guid xml, response valueOf was:"+String.valueOf(res.responseXML));
 			if(res.responseCode == SevedroidConstants.CODE_INTERNAL_SERVER_ERROR && (res.responseXML == null)) {
 				return null;
 			} else {
 				return res.responseXML;
 			}
 		}
 	}
 	
 	/**
 	 * This invokes the "GetWorkTypesByPhaseGUID" operation. Returns potentially a ton of xml, so caller
 	 * should prepare for a big String in response.
 	 */
 	
 	protected String getWorkTypesXMLByPhaseGUID(Activity parent, String phaseGUID) {
 		S3Response res = null;
 		String soapBody = SevedroidConstants.SOAP_AUTHN_ENVELOPE.replace(
 				SevedroidConstants.SOAP_BODY_SUBSTR, SevedroidConstants.GET_WORKTYPES_BY_PHASE_BODY);
 		String soapMessage = soapBody.replace(SevedroidConstants.WORKTYPES_PHASE_GUID_HERE,phaseGUID);
 		res = requestWithMessage(parent, soapMessage, SevedroidConstants.SOAP_ACTION_GET_WORKTYPES_BY_PHASE_GUID);
 		if (res == null) {
 			Log.d(TAG, "Response was null while getting work types by phase guid.");
 			return null;
 		} else {
 			Log.d(TAG, "While getting worktypes by phase guid xml, response valueOf was:"+String.valueOf(res.responseXML));
 			if(res.responseCode == SevedroidConstants.CODE_INTERNAL_SERVER_ERROR && (res.responseXML == null)) {
 				return null;
 			} else {
 				return res.responseXML;
 			}
 		}
 	}
 
 	public boolean publishHourEntry(Activity parent, String description,
 			String eventDate, String phaseGuid, String quantity,
			String userGuid, String workTypeGuid) { 	
		Log.d(TAG,"Publish hour entry args: "+description+":"+eventDate+":"+phaseGuid+":"+quantity+":"+userGuid+":"+workTypeGuid);
 		S3Response res = null;
 		String soapBody = SevedroidConstants.SOAP_AUTHN_ENVELOPE.replace(
 				SevedroidConstants.SOAP_BODY_SUBSTR, SevedroidConstants.HOUR_ENTRY_BODY);
		//TODO:Test what happens here is some of the dropdowns are not selected (but have their default value)
 		String soapMessage = soapBody.replace(SevedroidConstants.HOUR_ENTRY_DESC_SUBSTR, description);
 		soapMessage = soapMessage.replace(SevedroidConstants.HOUR_ENTRY_EVENT_DATE_SUBSTR, eventDate);
 		soapMessage = soapMessage.replace(SevedroidConstants.HOUR_ENTRY_PHASE_GUID_SUBSTR, phaseGuid);
 		soapMessage = soapMessage.replace(SevedroidConstants.HOUR_ENTRY_QUANTITY_SUBSTR, quantity);
 		soapMessage = soapMessage.replace(SevedroidConstants.HOUR_ENTRY_USER_GUID_SUBSTR, userGuid);
 		soapMessage = soapMessage.replace(SevedroidConstants.HOUR_ENTRY_WORKTYPE_GUID_SUBSTR, workTypeGuid);
 		res = requestWithMessage(parent, soapMessage, SevedroidConstants.SOAP_ACTION_PUBLISH_HOURENTRY);
 		if (res == null) {
 			Log.d(TAG, "Response was null while getting work types by phase guid.");
 			return false;
 		} else {
 			Log.d(TAG, "While getting worktypes by phase guid xml, response valueOf was:"+String.valueOf(res.responseXML));
 			if(res.responseCode == SevedroidConstants.CODE_INTERNAL_SERVER_ERROR && (res.responseXML == null)) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 		//return false;
 	}
 	
 	public String getHourEntriesByDateAndUserGUID(Activity parent, String startDate, String endDate, String userGuid) {
 		S3Response res = null;
 		String soapBody = SevedroidConstants.SOAP_AUTHN_ENVELOPE.replace(
 					SevedroidConstants.SOAP_BODY_SUBSTR, SevedroidConstants.GET_HOUR_ENTRIES_BY_DATE_AND_USER_GUID_BODY);
 		String soapMessage = soapBody.replace(SevedroidConstants.HOUR_ENTRY_USER_GUID_HERE,userGuid);
 		soapMessage = soapMessage.replace(SevedroidConstants.FIRST_HOUR_ENTRY_DATE_HERE, startDate);
 		soapMessage = soapMessage.replace(SevedroidConstants.LAST_HOUR_ENTRY_DATE_HERE, endDate);
 		res = requestWithMessage(parent, soapMessage, SevedroidConstants.SOAP_ACTION_GET_HOURENTRIES_BY_DATE_AND_USER_GUID);
 		if (res == null) {
 			Log.d(TAG, "Respoanse was null whic getting hour entries by date, userguid.");
 			return null;
 		} else {
 			Log.d(TAG,"While getting hour entries by date, userguid, response was: "+String.valueOf(res.responseXML));
 			if(res.responseCode == SevedroidConstants.CODE_INTERNAL_SERVER_ERROR && (res.responseXML == null)) {
 				return null;
 			} else {
 				return res.responseXML;
 			}
 		}
 	}
 
 }
