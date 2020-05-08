 package org.openremote.android.console.ssl;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.net.URLEncoder;
 import java.security.InvalidKeyException;
 import java.security.KeyPair;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.Security;
 import java.security.SignatureException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.security.auth.x500.X500Principal;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.openremote.android.console.Constants;
 import org.openremote.android.console.util.PhoneInformation;
 import org.spongycastle.asn1.ASN1ObjectIdentifier;
 import org.spongycastle.asn1.DEROctetString;
 import org.spongycastle.asn1.DERSet;
 import org.spongycastle.asn1.pkcs.Attribute;
 import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
 import org.spongycastle.asn1.x509.GeneralName;
 import org.spongycastle.asn1.x509.GeneralNames;
 import org.spongycastle.asn1.x509.X509Extension;
 import org.spongycastle.asn1.x509.X509Extensions;
 import org.spongycastle.jce.PKCS10CertificationRequest;
 import org.spongycastle.jce.provider.BouncyCastleProvider;
 import org.spongycastle.util.encoders.Base64;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.TextView.SavedState;
 
 public class CertificationRequest {
 	
 	/**
 	 * Register the SpongyCastle Provider
 	 */
 	static {
 		Security.addProvider(new BouncyCastleProvider());
 	}
 	
 	// Constants ------------------------------------------------------------------------------------
 	public final static String LOG_CATEGORY = Constants.LOG_CATEGORY + MyKeyPair.class.getName();
 	 
 	private static final String CSR_ALGORITHM = "SHA1WithRSA";
 
 	public static final String TIMESTAMP_FILE = "timestamp";
 		
 	/**
 	 * Generate a PKCS10 Certification Request. This request could be used to sing a certificate
 	 * which can be used to authenticate to the server.
 	 * 
 	 * This method returns it Base64 encoded
 	 * @param context The current application context
 	 * @param device The Common Name to be included in the certificate, should be the name of the device
 	 * @param email The email address of the registered user.
 	 * @return The Certification Request in Base64
 	 */
 	public static String getCertificationRequestAsBase64(Context context, String device, String email)
 	{
 		String basecert = new String(Base64.encode(getCertificationRequest(context, device, email).getDEREncoded()));
 				
 		return basecert;
 	}
 	
 	/**
 	 * Generate a PKCS10 Certification Request. This request could be used to sing a certificate
 	 * which can be used to authenticate to the server.
 	 * @param context The current application context
 	 * @param device The Common Name to be included in the certificate, should be the name of the device
 	 * @param email The email address of the registered user.
 	 * @return The Certification Request
 	 */
 	public static PKCS10CertificationRequest getCertificationRequest(Context context, String devicename, String email)
 	{
 		KeyPair keypair = MyKeyPair.getInstance().getKeyPair(context);
 		
 		X500Principal              dnName = new X500Principal("CN=" + devicename);
 		PKCS10CertificationRequest kpGen = null;
 		
 		// create the extension value
 		GeneralNames subjectAltName = new GeneralNames(
 		                   new GeneralName(GeneralName.rfc822Name, email));
 
 		// create the extensions object and add it as an attribute
 		Vector<ASN1ObjectIdentifier> oids = new Vector<ASN1ObjectIdentifier>();
 		Vector<X509Extension> values = new Vector<X509Extension>();
 
 		oids.add(X509Extension.subjectAlternativeName);
 		values.add(new X509Extension(false, new DEROctetString(subjectAltName)));
 
 		X509Extensions extensions = new X509Extensions(oids, values);
 
 		Attribute attribute = new Attribute(
 		                           PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
 		                           new DERSet(extensions));
 		
 		try {
 			kpGen = new PKCS10CertificationRequest(
 	                                  CSR_ALGORITHM,
 	                                  dnName,
 	                                  keypair.getPublic(),
 	                                  new DERSet(attribute),
 	                                  keypair.getPrivate());
 		} catch (InvalidKeyException e) {
 			Log.e(LOG_CATEGORY, e.getMessage());
 		} catch (NoSuchAlgorithmException e) {
 			Log.e(LOG_CATEGORY, e.getMessage());
 		} catch (NoSuchProviderException e) {
 			Log.e(LOG_CATEGORY, e.getMessage());
 		} catch (SignatureException e) {
 			Log.e(LOG_CATEGORY, e.getMessage());
 		}
 		
 		return kpGen;
 	}
 	
 	/**
 	 * Generate a certification request and submit it to the server, where it can be approved or disproved.
 	 * @param context The current application context
 	 * @param host The host to send CSR to, should be entire url to OpenRemote controller root, for example http://192.168.1.2:8080/controller
 	 * @return The HTTP status code of the request or -1 if something local has gone wrong
 	 */
 	public static int submitCertificationRequest(Context context, String host)
 	{
 	    HttpClient httpclient = new DefaultHttpClient();
 	    PhoneInformation phoneInfo = PhoneInformation.getInstance();
 	    
 	    String devicename = phoneInfo.getDeviceName();
 	    String email = phoneInfo.getEmailAddress(context);
 	    
 	    HttpPost httppost = new HttpPost(host + "/rest/cert/put/" + devicename);
 
 	    try {
 	    	String csr = getCertificationRequestAsBase64(context, devicename, email);
 	    	
 	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 	        nameValuePairs.add(new BasicNameValuePair("csr", URLEncoder.encode(csr)));
 	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 	        // Execute HTTP Post Request
 	        HttpResponse response = httpclient.execute(httppost);
 	        
 	        InputStreamReader is = new InputStreamReader(response.getEntity().getContent());
 	        BufferedReader br = new BufferedReader(is);
	        String read = br.readLine();

	        while(read != null) {
	            read += br.readLine();
 	        }
 	        
 	        saveTimestamp(read, context);
 	        
 	        return response.getStatusLine().getStatusCode();
 	    } catch (ClientProtocolException e) {
 			Log.e(LOG_CATEGORY, e.getMessage());
 	    } catch (IOException e) {
 			Log.e(LOG_CATEGORY, e.getMessage());
 	    }
 	    return -1;
 	}
 	
 	/**
 	 * Write a timestamp to a file, used later to retrieve signed certificate
 	 * @param timestamp The timestamp to write 
 	 * @param context The current application context
 	 */
 	private static void saveTimestamp(String timestamp, Context context)
 	{
 		timestamp.trim();
 		OutputStreamWriter out;
 		try {
 			out = new OutputStreamWriter(
 					context.openFileOutput(TIMESTAMP_FILE, Context.MODE_PRIVATE));
 			out.write(timestamp);
 			out.close();
 		} catch (FileNotFoundException e) {
 			Log.e(LOG_CATEGORY, e.getMessage());
 		} catch (IOException e) {
 			Log.e(LOG_CATEGORY, e.getMessage());
 		} 
 	}
 
 	public static boolean isPending(Context context) {
 		File dir = context.getFilesDir();
 		File file = new File(dir, TIMESTAMP_FILE);	
 		return file.exists();
 	}
 }
