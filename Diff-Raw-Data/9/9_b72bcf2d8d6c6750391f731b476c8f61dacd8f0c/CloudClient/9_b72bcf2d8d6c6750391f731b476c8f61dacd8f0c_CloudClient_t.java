 package com.abbyy.cloudocr.utils;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Bundle;
 
 import com.abbyy.cloudocr.R;
 
 /**
  * Client implementation. Handles the connection to the web service.
  * 
  * @author Denis Lapuente
  * 
  */
 public class CloudClient {
 	private static final String BASE_URL = "http://cloud.ocrsdk.com/";
 
 	// This are all the available options for the web service.
 	public static final String ACTION_GET_TASK_LIST = "listTasks";
 	public static final String ACTION_GET_FINISHED_TASK_LIST = "listFinishedTasks";
 	public static final String ACTION_PROCESS_IMAGE = "processImage";
 	public static final String ACTION_PROCESS_SUBMIT_IMAGE = "submitImage";
 	public static final String ACTION_PROCESS_DOCUMENT = "processDocument";
 	public static final String ACTION_PROCESS_BUSINESS_CARD = "processBusinessCard";
 	public static final String ACTION_PROCESS_TEXT_FIELD = "processTextField";
 	public static final String ACTION_PROCESS_BARCODE_FIELD = "processBarcodeField";
 	public static final String ACTION_PROCESS_CHECKMARK_FIELD = "processCheckmarkField";
 	public static final String ACTION_PROCESS_FIELDS = "processFields";
 	public static final String ACTION_GET_TASK_STATUS = "getTaskStatus";
 	public static final String ACTION_DELETE_TASK = "deleteTask";
 	public static final String ACTION_DOWNLOAD_RESULT = "downloadResult";
 
 	// This are all the options available for the different actions. Not all are
	// available for every action!!! See the API for this matter
 	public static final String ARGUMENT_LANGUAGE = "language";
 	public static final String ARGUMENT_PROFILE = "profile";
 	public static final String ARGUMENT_TEXT_TYPE = "textType";
 	public static final String ARGUMENT_IMAGE_SOURCE = "imageSource";
 	public static final String ARGUMENT_CORRECT_ORIENTATION = "correctOrientation";
 	public static final String ARGUMENT_CORRECT_SKEW = "correctSkew";
 	public static final String ARGUMENT_EXPORT_FORMAT = "exportFormat";
 	public static final String ARGUMENT_XML_WRITE_RECOGNITION_VARIANTS = "xml:writeRecognitionVariants";
 	public static final String ARGUMENT_DESCRIPTION = "description";
 	public static final String ARGUMENT_PDF_PASSWORD = "pdfPassword";
 	public static final String ARGUMENT_TASK_ID = "taskId";
 	public static final String ARGUMENT_REGION = "region";
 	public static final String ARGUMENT_LETTER_SET = "letterSet";
 	public static final String ARGUMENT_REG_EXP = "regExp";
 	public static final String ARGUMENT_ONE_TEXT_LINE = "oneTextLine";
 	public static final String ARGUMENT_ONE_WORD_PER_TEXT_LINE = "oneWordPerTextLine";
 	public static final String ARGUMENT_MARKING_TYPE = "markingType";
 	public static final String ARGUMENT_PLACEHOLDERS_COUNT = "placeHoldersCount";
 	public static final String ARGUMENT_WRITING_STYLE = "writingStyle";
 	public static final String ARGUMENT_BARCODE_TYPE = "barcodeType";
 	public static final String ARGUMENT_CONTAINS_BINARY_DATA = "containsBinaryData";
 	public static final String ARGUMENT_CHECKMARK_TYPE = "checkmarkType";
 	public static final String ARGUMENT_CORRECTION_ALLOWED = "correctionAllowed";
 
 	public URL mUrl;
 	public Uri mFilePath;
 	public String mProcessType;
 
 	private Context mContext;
 
 	/**
 	 * Constructor for the client.
 	 * 
 	 * @param context
 	 *            Required context to make the connection and access the
 	 *            different resources
 	 */
 	public CloudClient(Context context) {
 		mContext = context;
 	}
 
 	/**
 	 * Method that will set the correct URL in its place for the required
 	 * processing type. If arguments are passed they are parsed so the final URL
 	 * has also the arguments
 	 * 
 	 * WARNING!: This method is MANDATORY before the petition is done. If it is
 	 * not called, the method will return null and make no petition
 	 * 
 	 * @param processType
 	 *            one of the public ACTION_* elements. Tells the cloud SDK what
 	 *            action to perform
 	 * @param args
 	 *            arguments for the method call.
 	 * @throws MalformedURLException
 	 *             if the URL passed is not correct MalformedURLException will
 	 *             be thrown
 	 */
 	public void setUrl(String processType, Bundle args)
 			throws MalformedURLException {
 		mProcessType = processType;
 		String url = BASE_URL + processType;
 		if (args != null) {
 			url = url + createArgs(args);
 		}
 		mUrl = new URL(url);
 	}
 
 	/**
 	 * When trying to process a file, the Filepath should be specified here. It
 	 * can be processed both as a file:// or a content://
 	 * 
 	 * @param filePath
 	 *            the path to the file.
 	 */
 	public void setFilePath(String filePath) {
 		mFilePath = Uri.parse(filePath);
 	}
 
 	/**
 	 * Convenience method for downloading results. It is included here as it
 	 * should still be part of the cloud client, but it is not any of the cloud
 	 * available actions.
 	 * 
 	 * @param url
 	 *            The url provided by the cloud sdk with the result file.
 	 * @throws MalformedURLException
 	 *             if the URL passed is not correct MalformedURLException will
 	 *             be thrown
 	 */
 	public void setDownloadUrl(String url) throws MalformedURLException {
 		mProcessType = ACTION_DOWNLOAD_RESULT;
 		mUrl = new URL(url);
 	}
 
 	/**
 	 * Main method for the class. Will make the request to the cloud and return
 	 * the response, whatever it is.
 	 * 
 	 * @return InputStream with the response from the server.
 	 */
 	public HttpResponse makePetition() {
 		if (!isConnected()) {
 			return null;
 		}
 		if (mUrl == null) {
 			return null;
 		}
 
 		DefaultHttpClient httpClient = createHttpClient();
 
 		try {
 			// The cloud server uses both GET and POST requests based on the
 			// type of action performed
 			HttpUriRequest request;
 
 			if (isGet()) {
 				request = new HttpGet(mUrl.toExternalForm());
 			} else {
 				// For posts requests, a file must be added to be sent and
 				// processed. If the file is not present, we just exit
 				HttpPost postRequest = new HttpPost(mUrl.toExternalForm());
 				if (mFilePath != null) {
 					addFile(postRequest);
 				} else {
 					return null;
 				}
 				request = postRequest;
 			}
 
 			// When everything is set, execute the response and return the
 			// content.
 			return httpClient.execute(request);
 
 			// If any exception happens, we won't do anything and just return a
 			// null response, as it will be a server error.
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private boolean isConnected() {
 		ConnectivityManager cm = (ConnectivityManager) mContext
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		return cm.getActiveNetworkInfo().isConnected();
 	}
 
 	/**
 	 * 
 	 * Adds a file to process to a request.
 	 * 
 	 * @param request
 	 *            the HttpPost request object where we should add the file
 	 * @throws IOException
 	 *             when there are problems when trying to open the file.
 	 */
 	private void addFile(HttpPost request) throws IOException {
 		InputStreamEntity entity = null;
 		entity = new InputStreamEntity(mContext.getContentResolver()
 				.openInputStream(mFilePath), -1);
 		entity.setContentType("application/octet-stream");
 		entity.setChunked(true);
 		BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
 		request.setEntity(bufferedEntity);
 	}
 
 	/**
 	 * Creates and sets up the web client that will connect to the server
 	 * 
 	 * @return DefaultHttpClient with the credentials and parameters ready
 	 */
 	private DefaultHttpClient createHttpClient() {
 		DefaultHttpClient client = new DefaultHttpClient(setHttpParams());
 		setupCredentials(client);
 		return client;
 	}
 
 	/**
 	 * Sets the HttpParams object for the HttpClient
 	 * 
 	 * @return HttpParams containing the wanted parameters
 	 */
 	private HttpParams setHttpParams() {
 		HttpParams httpParams = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
 		HttpConnectionParams.setSoTimeout(httpParams, 15000);
 		return httpParams;
 	}
 
 	/**
 	 * Convenience method that sets up the credentials for the connection. The
 	 * credentials are stored in the credentials.xml file under res/values
 	 * 
 	 * @param client
 	 *            the HttpClient where the credentials should be added
 	 */
 	private void setupCredentials(DefaultHttpClient client) {
 		String appId = mContext.getString(R.string.credentials_app_id);
 		String password = mContext.getString(R.string.credentials_app_password);
 
 		CredentialsProvider credentials = client.getCredentialsProvider();
 		credentials.setCredentials(AuthScope.ANY,
 				new UsernamePasswordCredentials(appId, password));
 		client.setCredentialsProvider(credentials);
 	}
 
 	/**
 	 * Convenience method to check if the request is of the GET Type or the POST
 	 * type
 	 * 
 	 * @return true if is a GET petition. false if is a POST petition
 	 */
 	private boolean isGet() {
 		return mProcessType.equals(ACTION_PROCESS_DOCUMENT)
 				|| mProcessType.equals(ACTION_GET_TASK_STATUS)
 				|| mProcessType.equals(ACTION_DELETE_TASK)
 				|| mProcessType.equals(ACTION_GET_TASK_LIST)
 				|| mProcessType.equals(ACTION_GET_FINISHED_TASK_LIST)
 				|| mProcessType.equals(ACTION_DOWNLOAD_RESULT);
 	}
 
 	/**
 	 * This method parses the options that have been provided and adds them as
 	 * an argument to the client petition
 	 * 
 	 * @param args
 	 *            Bundle with the options.
 	 * @return The whole string with all the options
 	 */
 	private String createArgs(Bundle args) {
 		String result = "?";
 		if (args.containsKey(ARGUMENT_DESCRIPTION)) {
 			result = result.concat(ARGUMENT_DESCRIPTION + "="
 					+ args.getString(ARGUMENT_DESCRIPTION) + "&");
 		}
 		if (args.containsKey(ARGUMENT_WRITING_STYLE)) {
 			result = result.concat(ARGUMENT_WRITING_STYLE + "="
 					+ args.getString(ARGUMENT_WRITING_STYLE) + "&");
 		}
 		if (args.containsKey(ARGUMENT_BARCODE_TYPE)) {
 			result = result.concat(ARGUMENT_BARCODE_TYPE + "="
 					+ args.getString(ARGUMENT_BARCODE_TYPE) + "&");
 		}
 		if (args.containsKey(ARGUMENT_ONE_TEXT_LINE)) {
 			result = result.concat(ARGUMENT_ONE_TEXT_LINE + "="
 					+ args.getString(ARGUMENT_ONE_TEXT_LINE) + "&");
 		}
 		if (args.containsKey(ARGUMENT_CORRECTION_ALLOWED)) {
 			result = result.concat(ARGUMENT_CORRECTION_ALLOWED + "="
 					+ args.getString(ARGUMENT_CORRECTION_ALLOWED) + "&");
 		}
 		if (args.containsKey(ARGUMENT_LETTER_SET)) {
 			result = result.concat(ARGUMENT_LETTER_SET + "="
 					+ args.getString(ARGUMENT_LETTER_SET) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_ONE_WORD_PER_TEXT_LINE)) {
 			result = result.concat(ARGUMENT_ONE_WORD_PER_TEXT_LINE + "="
 					+ args.getString(ARGUMENT_ONE_WORD_PER_TEXT_LINE) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_MARKING_TYPE)) {
 			result = result.concat(ARGUMENT_MARKING_TYPE + "="
 					+ args.getString(ARGUMENT_MARKING_TYPE) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_PLACEHOLDERS_COUNT)) {
 			result = result.concat(ARGUMENT_PLACEHOLDERS_COUNT + "="
 					+ args.getString(ARGUMENT_PLACEHOLDERS_COUNT) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_EXPORT_FORMAT)) {
 			result = result.concat(ARGUMENT_EXPORT_FORMAT + "="
 					+ args.getString(ARGUMENT_EXPORT_FORMAT) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_IMAGE_SOURCE)) {
 			result = result.concat(ARGUMENT_IMAGE_SOURCE + "="
 					+ args.getString(ARGUMENT_IMAGE_SOURCE) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_TASK_ID)) {
 			result = result.concat(ARGUMENT_TASK_ID + "="
 					+ args.getString(ARGUMENT_TASK_ID) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_TEXT_TYPE)) {
 			result = result.concat(ARGUMENT_TEXT_TYPE + "="
 					+ args.getString(ARGUMENT_TEXT_TYPE) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_REG_EXP)) {
 			result = result.concat(ARGUMENT_REG_EXP + "="
 					+ args.getString(ARGUMENT_REG_EXP) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_REGION)) {
 			result = result.concat(ARGUMENT_REGION + "="
 					+ args.getString(ARGUMENT_REGION) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_CHECKMARK_TYPE)) {
 			result = result.concat(ARGUMENT_CHECKMARK_TYPE + "="
 					+ args.getString(ARGUMENT_CHECKMARK_TYPE) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_CONTAINS_BINARY_DATA)) {
 			result = result.concat(ARGUMENT_CONTAINS_BINARY_DATA + "="
 					+ args.getString(ARGUMENT_CONTAINS_BINARY_DATA) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_CORRECT_ORIENTATION)) {
 			result = result.concat(ARGUMENT_CORRECT_ORIENTATION + "="
 					+ args.getString(ARGUMENT_CORRECT_ORIENTATION) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_CORRECT_SKEW)) {
 			result = result.concat(ARGUMENT_CORRECT_SKEW + "="
 					+ args.getString(ARGUMENT_CORRECT_SKEW) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_XML_WRITE_RECOGNITION_VARIANTS)) {
 			result = result.concat(ARGUMENT_XML_WRITE_RECOGNITION_VARIANTS
 					+ "="
 					+ args.getString(ARGUMENT_XML_WRITE_RECOGNITION_VARIANTS)
 					+ "&");
 		}
 
 		if (args.containsKey(ARGUMENT_PDF_PASSWORD)) {
 			result = result.concat(ARGUMENT_PDF_PASSWORD + "="
 					+ args.getString(ARGUMENT_PDF_PASSWORD) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_PROFILE)) {
 			result = result.concat(ARGUMENT_PROFILE + "="
 					+ args.getString(ARGUMENT_PROFILE) + "&");
 		}
 
 		if (args.containsKey(ARGUMENT_LANGUAGE)) {
 			result = result.concat(ARGUMENT_LANGUAGE + "="
 					+ args.getString(ARGUMENT_LANGUAGE));
 		}
 		return result;
 	}
 }
