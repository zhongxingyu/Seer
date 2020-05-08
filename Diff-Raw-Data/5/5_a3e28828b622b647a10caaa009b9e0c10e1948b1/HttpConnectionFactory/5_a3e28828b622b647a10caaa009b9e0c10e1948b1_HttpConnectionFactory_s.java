 /**
  * Copyright (c) E.Y. Baskoro, Research In Motion Limited.
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the 
  * Software is furnished to do so, subject to the following 
  * conditions:
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
  * OTHER DEALINGS IN THE SOFTWARE.
  * 
  * This License shall be included in all copies or substantial 
  * portions of the Software.
  * 
  * The name(s) of the above copyright holders shall not be used 
  * in advertising or otherwise to promote the sale, use or other 
  * dealings in this Software without prior written authorization.
  * 
  */
 package com.blackberry.util.network;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Vector;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.HttpConnection;
 
 import com.blackberry.util.log.RichTextLoggable;
 
 import net.rim.device.api.io.http.HttpHeaders;
 import net.rim.device.api.io.http.HttpProtocolConstants;
 import net.rim.device.api.servicebook.ServiceBook;
 import net.rim.device.api.servicebook.ServiceRecord;
 import net.rim.device.api.system.Branding;
 import net.rim.device.api.system.CoverageInfo;
 import net.rim.device.api.system.DeviceInfo;
 import net.rim.device.api.system.WLANInfo;
 
 public class HttpConnectionFactory implements RichTextLoggable {
 
 	public static final int TRANSPORT_WIFI = 1;
 	public static final int TRANSPORT_BES = 2;
 	public static final int TRANSPORT_BIS = 4;
 	public static final int TRANSPORT_DIRECT_TCP = 8;
 	public static final int TRANSPORT_WAP2 = 16;
 	public static final int TRANSPORT_SIM = 32;
 
 	public static final int TRANSPORTS_ANY = TRANSPORT_WIFI | TRANSPORT_BES | TRANSPORT_BIS | TRANSPORT_DIRECT_TCP | TRANSPORT_WAP2 | TRANSPORT_SIM;
 	public static final int TRANSPORTS_AVOID_CARRIER = TRANSPORT_WIFI | TRANSPORT_BES | TRANSPORT_BIS | TRANSPORT_SIM;
 	public static final int TRANSPORTS_CARRIER_ONLY = TRANSPORT_DIRECT_TCP | TRANSPORT_WAP2 | TRANSPORT_SIM;
 
 	public static final int DEFAULT_TRANSPORT_ORDER[] = { TRANSPORT_SIM, TRANSPORT_WIFI, TRANSPORT_BIS, TRANSPORT_BES, TRANSPORT_WAP2, TRANSPORT_DIRECT_TCP };
 
 	private static final int TRANSPORT_COUNT = DEFAULT_TRANSPORT_ORDER.length;
 
 	// private static ServiceRecord srMDS[], srBIS[], srWAP2[], srWiFi[];
 	private static ServiceRecord srWAP2[];
 	private static boolean serviceRecordsLoaded = false;
 
 	private int transports[];
 	private int lastTransport = -1;
 
 	public HttpConnectionFactory() {
 		this(0);
 	}
 
 	public HttpConnectionFactory(int allowedTransports) {
 		this(transportMaskToArray(allowedTransports));
 	}
 
 	public HttpConnectionFactory(int transportPriority[]) {
 		if (!serviceRecordsLoaded) {
 			loadServiceBooks(false);
 		}
 		transports = transportPriority;
 	}
 
 	public static String getUserAgent() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("BlackBerry");
 		sb.append(DeviceInfo.getDeviceName());
 		sb.append("/");
 		sb.append(DeviceInfo.getSoftwareVersion());
 		sb.append(" Profile/");
 		sb.append(System.getProperty("microedition.profiles"));
 		sb.append(" Configuration/");
 		sb.append(System.getProperty("microedition.configuration"));
 		sb.append(" VendorID/");
 		sb.append(Branding.getVendorId());
 
 		return sb.toString();
 	}
 
 	public static String getProfile() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("http://www.blackberry.net/go/mobile/profiles/uaprof/");
 		sb.append(DeviceInfo.getDeviceName());
 		sb.append("/");
 		sb.append(DeviceInfo.getSoftwareVersion().substring(0, 3)); //RDF file format is 4.5.0.rdf (does not include build version)
 		sb.append(".rdf");
 
 		return sb.toString();
 	}
 
 	public HttpConnection getHttpConnection(String pURL) {
 		return getHttpConnection(pURL, null, null);
 	}
 
 	public HttpConnection getHttpConnection(String pURL, HttpHeaders headers) {
 		return getHttpConnection(pURL, headers, null);
 	}
 
 	public HttpConnection getHttpConnection(String pURL, byte[] data) {
 		return getHttpConnection(pURL, null, data);
 	}
 
 	public HttpConnection getHttpConnection(String pURL, HttpHeaders headers, byte[] data) {
 
 		int curIndex = 0;
 		HttpConnection con = null;
 
 		while ((con = tryHttpConnection(pURL, curIndex, headers, data)) == null) {
 			try {
 				curIndex = nextTransport(curIndex);
 			} catch (HttpConnectionFactoryException e) {
 				e.printStackTrace();
 				break;
 			} finally {
 			}
 		}
 
 		if (con != null) {
 			setLastTransport(transports[curIndex]);
 		}
 
 		return con;
 	}
 
 	private int nextTransport(int curIndex) throws HttpConnectionFactoryException {
 		if ((curIndex >= 0) && (curIndex < transports.length - 1)) {
			return curIndex++;
 		} else {
 			throw new HttpConnectionFactoryException("No more transport available.");
 		}
 	}
 
 	private HttpConnection tryHttpConnection(String pURL, int tIndex, HttpHeaders headers, byte[] data) {
 
 		HttpConnection con = null;
 		OutputStream os = null;
 
 		log.debug("Trying " + getTransportName(transports[tIndex]) + "... ");
 		switch (transports[tIndex]) {
 		case TRANSPORT_SIM:
 			try {
				con = getSimConnection(pURL, true);
 			} catch (IOException e) {
 				log.debug(e.getMessage());
 			} finally {
 				break;
 			}
 		case TRANSPORT_WIFI:
 			try {
 				con = getWifiConnection(pURL);
 			} catch (IOException e) {
 				log.debug(e.getMessage());
 			} finally {
 				break;
 			}
 		case TRANSPORT_BES:
 			try {
 				con = getBesConnection(pURL);
 			} catch (IOException e) {
 				log.debug(e.getMessage());
 			} finally {
 				break;
 			}
 		case TRANSPORT_BIS:
 			try {
 				con = getBisConnection(pURL);
 			} catch (IOException e) {
 				log.debug(e.getMessage());
 			} finally {
 				break;
 			}
 		case TRANSPORT_DIRECT_TCP:
 			try {
 				con = getTcpConnection(pURL);
 			} catch (IOException e) {
 			} finally {
 				break;
 			}
 		case TRANSPORT_WAP2:
 			try {
 				con = getWap2Connection(pURL);
 			} catch (IOException e) {
 				log.debug(e.getMessage());
 			} finally {
 				break;
 			}
 		}
 
 		log.debug("con = " + con);
 
 		if (con != null) {
 			try {
 				log.debug("url = " + con.getURL());
 				//add headers to connection
 				if (headers != null) {
 					int size = headers.size();
 
 					for (int i = 0; i < size;) {
 						String header = headers.getPropertyKey(i);
 						String value = headers.getPropertyValue(i++);
 
 						if (value != null) {
 							con.setRequestProperty(header, value);
 
 						}
 					}
 				}
 				// post data
 				if (data != null) {
 					con.setRequestMethod(HttpConnection.POST);
 					con.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_TYPE, HttpProtocolConstants.CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);
 					con.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH, String.valueOf(data.length));
 
 					os = con.openOutputStream();
 					os.write(data);
 				} else {
 					con.setRequestMethod(HttpConnection.GET);
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return con;
 	}
 
 	public int getLastTransport() {
 		return lastTransport;
 	}
 
 	public String getLastTransportName() {
 		return getTransportName(getLastTransport());
 	}
 
 	private void setLastTransport(int pLastTransport) {
 		lastTransport = pLastTransport;
 	}
 
 	private HttpConnection getSimConnection(String pURL, boolean mdsSimulatorRunning) throws IOException {
 		if (DeviceInfo.isSimulator()) {
 			if (mdsSimulatorRunning) {
 				return getConnection(pURL, ";deviceside=false", null);
 			} else {
 				return getConnection(pURL, ";deviceside=true", null);
 			}
 		}
 		return null;
 	}
 
 	private HttpConnection getBisConnection(String pURL) throws IOException {
 		if (CoverageInfo.isCoverageSufficient(4 /* CoverageInfo.COVERAGE_BIS_B */)) {
 			return getConnection(pURL, ";deviceside=false;ConnectionType=mds-public", null);
 		}
 		return null;
 	}
 
 	private HttpConnection getBesConnection(String pURL) throws IOException {
 		if (CoverageInfo.isCoverageSufficient(2 /* CoverageInfo.COVERAGE_MDS */)) {
 			return getConnection(pURL, ";deviceside=false", null);
 		}
 		return null;
 	}
 
 	private HttpConnection getWifiConnection(String pURL) throws IOException {
 		if (WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED) {
 			return getConnection(pURL, ";interface=wifi", null);
 		}
 		return null;
 	}
 
 	private HttpConnection getWap2Connection(String pURL) throws IOException {
 		if (CoverageInfo.isCoverageSufficient(1 /* CoverageInfo.COVERAGE_DIRECT */) && (srWAP2 != null) && (srWAP2.length != 0)) {
 			return getConnection(pURL, ";deviceside=true;ConnectionUID=", srWAP2[0].getUid());
 		}
 		return null;
 	}
 
 	private HttpConnection getTcpConnection(String pURL) throws IOException {
 		if (CoverageInfo.isCoverageSufficient(1 /* CoverageInfo.COVERAGE_DIRECT */)) {
 			return getConnection(pURL, ";deviceside=true", null);
 		}
 		return null;
 	}
 
 	private HttpConnection getConnection(String pURL, String transportExtras1, String transportExtras2) throws IOException {
 		StringBuffer fullUrl = new StringBuffer();
 		fullUrl.append(pURL);
 		if (transportExtras1 != null) {
 			fullUrl.append(transportExtras1);
 		}
 		if (transportExtras2 != null) {
 			fullUrl.append(transportExtras2);
 		}
 		return (HttpConnection) Connector.open(fullUrl.toString());
 	}
 
 	public static void reloadServiceBooks() {
 		loadServiceBooks(true);
 	}
 
 	private static synchronized void loadServiceBooks(boolean reload) {
 		if (serviceRecordsLoaded && !reload) {
 			return;
 		}
 		ServiceBook sb = ServiceBook.getSB();
 		ServiceRecord[] records = sb.getRecords();
 		Vector mdsVec = new Vector();
 		Vector bisVec = new Vector();
 		Vector wap2Vec = new Vector();
 		Vector wifiVec = new Vector();
 
 		if (!serviceRecordsLoaded) {
 			for (int i = 0; i < records.length; i++) {
 				ServiceRecord myRecord = records[i];
 				String cid, uid;
 
 				if (myRecord.isValid() && !myRecord.isDisabled()) {
 					cid = myRecord.getCid().toLowerCase();
 					uid = myRecord.getUid().toLowerCase();
 					if ((cid.indexOf("wptcp") != -1) && (uid.indexOf("wap2") != -1) && (uid.indexOf("wifi") == -1) && (uid.indexOf("mms") == -1)) {
 						wap2Vec.addElement(myRecord);
 					}
 				}
 			}
 
 			srWAP2 = new ServiceRecord[wap2Vec.size()];
 			wap2Vec.copyInto(srWAP2);
 			wap2Vec.removeAllElements();
 			wap2Vec = null;
 
 			serviceRecordsLoaded = true;
 		}
 	}
 
 	public static int[] transportMaskToArray(int mask) {
 		if (mask == 0) {
 			mask = TRANSPORTS_ANY;
 		}
 		int numTransports = 0;
 		for (int i = 0; i < TRANSPORT_COUNT; i++) {
 			if ((DEFAULT_TRANSPORT_ORDER[i] & mask) != 0) {
 				numTransports++;
 			}
 		}
 		int transports[] = new int[numTransports];
 		int index = 0;
 		for (int i = 0; i < TRANSPORT_COUNT; i++) {
 			if ((DEFAULT_TRANSPORT_ORDER[i] & mask) != 0) {
 				transports[index++] = DEFAULT_TRANSPORT_ORDER[i];
 			}
 		}
 		return transports;
 	}
 
 	private static String getTransportName(int transport) {
 		String tName;
 		switch (transport) {
 		case TRANSPORT_WIFI:
 			tName = "WIFI";
 			break;
 		case TRANSPORT_BES:
 			tName = "BES";
 			break;
 		case TRANSPORT_BIS:
 			tName = "BIS";
 			break;
 		case TRANSPORT_DIRECT_TCP:
 			tName = "TCP";
 			break;
 		case TRANSPORT_WAP2:
 			tName = "WAP2";
 			break;
 		case TRANSPORT_SIM:
 			tName = "SIM";
 			break;
 		default:
 			tName = "UNKNOWN";
 			break;
 		}
 		return tName;
 	}
 
 }
