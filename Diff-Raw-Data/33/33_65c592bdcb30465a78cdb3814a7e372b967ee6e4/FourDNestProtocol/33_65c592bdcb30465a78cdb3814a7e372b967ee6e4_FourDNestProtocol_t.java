 package org.fourdnest.androidclient.comm;
 
 import android.net.Uri;
 import android.os.Environment;
 import android.util.Log;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.Hex;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.impl.cookie.DateUtils;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.NameValuePair;
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.FourDNestApplication;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.Tag;
 import org.fourdnest.androidclient.ThumbnailManager;
 import org.fourdnest.androidclient.tools.*;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 public class FourDNestProtocol implements Protocol {
 	private static final String TAG = "FourDNestProtocol";
 	private static final String EGG_UPLOAD_PATH = "fourdnest/api/v1/egg/upload/";
 	private static final String EGG_DOWNLOAD_PATH = "fourdnest/api/v1/egg/";
 	private static final String TAG_DOWNLOAD_PATH = "fourdnest/api/v1/tag/";
 	private static final String HELP_PATH = "fourdnest/help/";
 	private static final String JSON_FORMAT = "?format=json";
 	private static final String SIZE_FORMAT = "?limit=";
 	private static final int HTTP_STATUSCODE_OK = 200;
 	private static final int HTTP_STATUSCODE_CREATED = 201;
 	private static final int HTTP_STATUSCODE_UNAUTHORIZED = 401;
 	private static final int HTTP_STATUSCODE_SERVER_ERROR = 500;
 
 	
 	public static final String THUMBNAIL_SIZE_SMALL = "-100x100-crop";
 	public static final String THUMBNAIL_SIZE_LARGE = "-600x600";
 
 	private static String THUMBNAIL_LOCATION = "/fourdnest/thumbnails/";
 
 	/** Location of thumbnails on the server */
 	public static final String THUMBNAIL_PATH = "content/instance/";
 
 	/** Thumbnails on the server are in jpg format */
 	private static final String THUMBNAIL_FILETYPE = ".jpg";
 	private Nest nest;
 
 	public FourDNestProtocol() {
 		this.nest = null;
 	}
 
 	/**
 	 * Parses egg's content and sends it in multipart mime format with HTTP
 	 * post.
 	 * 
 	 * @param egg
 	 *            The egg that we want to send to the server
 	 * 
 	 * @return HTTP status code and egg URI on server if creation successful
 	 **/
 	public ProtocolResult sendEgg(Egg egg) {
 
 		String concatedMd5 = "";
 		HttpClient client = CommUtils.createHttpClient();
 		HttpPost post = new HttpPost(this.nest.getBaseURI() + EGG_UPLOAD_PATH);
 		String metadata = eggToJSONstring(egg);
 		Log.d("METADATA", metadata);
 
 		// Create list of NameValuePairs
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 		pairs.add(new BasicNameValuePair("data", metadata));
 		String metadataMd5 = CommUtils.md5FromString(metadata);
 		Log.d("metadataMD5", metadataMd5);
 
 		concatedMd5 += metadataMd5;
 		if (egg.getLocalFileURI() != null) {
 			if (new File(egg.getLocalFileURI().getPath()).isFile()) {
 				pairs.add(new BasicNameValuePair("file", egg.getLocalFileURI()
 						.getPath()));
 
 				String fileMd5 = CommUtils.md5FromFile(egg.getLocalFileURI()
 						.getPath());
 				Log.d("fileMD5", fileMd5);
 
 				concatedMd5 += fileMd5;
 			}
 		}
 
 		String multipartMd5String = CommUtils.md5FromString(concatedMd5);
 		multipartMd5String = new String(Base64.encodeBase64(multipartMd5String
 				.getBytes()));
 
 		int status = 0;
 		try {
 			post.setEntity(CommUtils.createEntity(pairs));
 			addAuthentication(post, multipartMd5String);
 			Log.d("AUTH", post.getHeaders("Authorization")[0].getValue());
 			HttpResponse response = client.execute(post);
 			status = response.getStatusLine().getStatusCode();
 
 			return this.parseResult(status, response);
 
 		} catch (ClientProtocolException e) {
 			Log.e(TAG,
 					"ClientProtocolException, egg not sent " + e.getMessage());
 			return new ProtocolResult(null, ProtocolResult.SENDING_FAILED);
 		} catch (IOException e) {
 			Log.e(TAG, "IOException, egg not sent " + e.getMessage());
 			return new ProtocolResult(null, ProtocolResult.SENDING_FAILED);
		} catch (IllegalStateException e) {
		    Log.e(TAG, "IllegalStateException, egg not sent " + e.getMessage());
		    return new ProtocolResult(null, ProtocolResult.INVALID_ADDRESS);
 		}
 	}
 
 	public ProtocolResult overwriteEgg(Egg egg) {
 		if (egg.getExternalId() == null) {
 			return new ProtocolResult(null, ProtocolResult.SENDING_FAILED);
 		}
 		HttpClient client = CommUtils.createHttpClient();
 		HttpPut request = new HttpPut(this.nest.getBaseURI()
 				+ EGG_DOWNLOAD_PATH + egg.getExternalId() + "/");
 		Log.d("OVERURI", request.getURI().getPath());
 		String metadata = eggToJSONstring(egg);
 
 		String metadataMd5 = CommUtils.md5FromString(metadata);
 		String multipartMd5String = CommUtils.md5FromString(metadataMd5);
 		multipartMd5String = new String(Base64.encodeBase64(multipartMd5String
 				.getBytes()));
 		int status = 0;
 		try {
 			StringEntity se = new StringEntity(metadata, HTTP.UTF_8);
 			request.addHeader("Content-Type", "application/json");
 			request.setEntity(se);
 			addAuthentication(request, multipartMd5String);
 			HttpResponse response = client.execute(request);
 			status = response.getStatusLine().getStatusCode();
 			Log.d("OVERSTATUS", String.valueOf(status));
 			return this.parseResult(status, response);
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, "Failed to overwrite egg: ClientProtocolException");
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to overwrite egg: IOException");
		} catch (IllegalStateException e) {
		    Log.e(TAG, "Failed to overwrite egg: IllegalStateException");
		    return new ProtocolResult(null, ProtocolResult.INVALID_ADDRESS);
 		}
 		return new ProtocolResult(null, ProtocolResult.SENDING_FAILED);
 	}
 
 	/**
 	 * Creates the proper ProtocolResult object from the server response
 	 * 
 	 * @param statusCode
 	 *            The statuscode given in the server response
 	 * @param response
 	 *            The response from the server.
 	 * @return The created ProtocolResult
 	 */
 	private ProtocolResult parseResult(int statusCode, HttpResponse response) {
 		if (statusCode == HTTP_STATUSCODE_CREATED) {
 			return new ProtocolResult(
 					response.getHeaders("Location")[0].getValue(),
 					ProtocolResult.RESOURCE_UPLOADED);
 		} else if (statusCode == HTTP_STATUSCODE_UNAUTHORIZED) {
 			return new ProtocolResult(null, ProtocolResult.AUTHORIZATION_FAILED);
 		} else if (statusCode == HTTP_STATUSCODE_SERVER_ERROR) {
 			return new ProtocolResult(null,
 					ProtocolResult.SERVER_INTERNAL_ERROR);
 		} else {
 			Log.d("sendEgg: UNKNOWN_RESULT", String.valueOf(statusCode));
 			return new ProtocolResult(null, ProtocolResult.UNKNOWN_REASON);
 		}
 	}
 
 	/**
 	 * Fetches the top tags from the server
 	 * @param count
 	 *         The amount of tags fetched from the server
 	 * @return
 	 *         A list of tags received, or empty list if exception occurred
 	 */
 	public List<Tag> topTags(int count) {
 		ArrayList<Tag> tags = new ArrayList<Tag>();
 		HttpClient client = CommUtils.createHttpClient();
 		HttpGet request = new HttpGet();
 		String uriPath = this.nest.getBaseURI() + TAG_DOWNLOAD_PATH
 				+ SIZE_FORMAT + count;
 		Log.d("TAGURI", uriPath);
 
 		try {
 			request.setURI(new URI(uriPath));
 			addAuthentication(request, "");
 			String jsonStr = CommUtils
 					.responseToString(client.execute(request));
 			JSONObject outer = new JSONObject(jsonStr);
 			JSONArray jsonTags = outer.getJSONArray("objects");
 			for (int i = 0; i < jsonTags.length(); i++) {
 				JSONObject current = jsonTags.getJSONObject(i);
 				tags.add(new Tag(current.getString("name")));
 				Log.d(("TAG" + i), tags.get(i).getName());
 			}
 			return tags;
 		} catch (URISyntaxException e) {
 			Log.e(TAG, "Failed to fetch tags: UriSyntaxException");
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, "Failed to fetch tags: ClientProtocolException");
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to fetch tags: IoException");
 		} catch (JSONException e) {
 			Log.e(TAG, "Failed to fetch tags: JSONException");
		} catch (IllegalStateException e) {
		    Log.e(TAG, "Failed to fetch tags: IllegalStateException");
 		}
 		return new ArrayList<Tag>();
 	}
 
 	public int getProtocolId() {
 		return ProtocolFactory.PROTOCOL_4DNEST;
 	}
 
 	public void setNest(Nest nest) {
 		this.nest = nest;
 
 	}
 
 	/**
 	 * Retrieves a single egg from the server and returns it.
 	 * 
 	 * @param uid
 	 *            The server side id of the egg that we want to retrieve.
 	 * @return The retrieved egg
 	 */
 	public Egg getEgg(String uid) {
 		HttpClient client = CommUtils.createHttpClient();
 		HttpGet request = new HttpGet();
 		String uriPath = this.nest.getBaseURI() + EGG_DOWNLOAD_PATH + uid + "/" + JSON_FORMAT;
 		// Log.d("URI", uriPath);
 
 		try {
 			request.setURI(new URI(uriPath));
 			addAuthentication(request, "");
 			String jsonStr = CommUtils
 					.responseToString(client.execute(request));
 			JSONObject js = new JSONObject(jsonStr);
 			return jSONObjectToEgg(js);
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			Log.e(TAG, "Failed to fetch egg: IllegalStateException");
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			Log.e(TAG, "Failed to fetch egg: ClientProtocolException");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			Log.e(TAG, "Failed to fetch egg: IOException");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			Log.e(TAG, "Failed to fetch egg: JSONException");
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			Log.e(TAG, "Failed to fetch egg: UriSyntaxException");
 		}
 		return null;
 	}
 
 	/**
 	 * Retrieves the metadata of all the eggs in the server. Parses this
 	 * metadata and creates a list of eggs from it
 	 * 
 	 * @return List of egg objects, obtained from the server.
 	 */
 	public List<Egg> getStream(int size) {
 		Egg current = null;
 		ArrayList<Egg> eggList = new ArrayList<Egg>();
 		HttpClient client = CommUtils.createHttpClient();
 		HttpGet request = new HttpGet();
 		String uriPath = this.nest.getBaseURI() + EGG_DOWNLOAD_PATH
 				+ SIZE_FORMAT + size;
 		// Log.d("URIStream", uriPath);
 		try {
 			request.setURI(new URI(uriPath));
 			addAuthentication(request, "");
 			String jsonStr = CommUtils
 					.responseToString(client.execute(request));
 			JSONObject outer = new JSONObject(jsonStr);
 			JSONArray jsonArr = outer.getJSONArray("objects");
 			for (int i = 0; i < jsonArr.length(); i++) {
 				current = jSONObjectToEgg(jsonArr.getJSONObject(i));
 				if (current != null) {
 					// Log.d("CURRENTEGG", current.getExternalId());
 					eggList.add(current);
 				}
 			}
 			return eggList;
 		} catch (URISyntaxException e) {
 			Log.d(TAG, "getStream: Invalid URI");
 		} catch (ClientProtocolException e) {
 			Log.d(TAG, "getStream: client execute failed");
 		} catch (IOException e) {
 			Log.d(TAG, "getStream: got IOException");
 		} catch (JSONException e) {
 			Log.d(TAG, "JSONstring formatted incorrectly");
		} catch (IllegalStateException e) {
		    Log.d(TAG, "Invalid base uri address");
 		}
 		return eggList;
 	}
 
 	/**
 	 * Computes the Hmac-Sha1 signature for the given string
 	 * 
 	 * @param stringToSign
 	 *            String to be signed
 	 * @param secretKey
 	 *            The key that is used to sign the string
 	 * @return The signature
 	 */
 	private String computeSignature(String stringToSign, String secretKey) {
 		String result = "";
 		byte[] keyBytes = secretKey.getBytes();
 		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
 
 		Mac mac;
 		try {
 			mac = Mac.getInstance("HmacSHA1");
 			mac.init(signingKey);
 			String hexStr = new String(Hex.encodeHex(mac.doFinal(stringToSign
 					.getBytes())));
 			result = new String(Base64.encodeBase64(hexStr.getBytes()));
 
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 
 		}
 		return result;
 	}
 
 	/**
 	 * Creates the needed headers for authentication and fills them.
 	 * 
 	 * @param base
 	 *            The Httpmessage base
 	 * @param multipartMd5
 	 *            the multipart md5 string
 	 */
 	private void addAuthentication(HttpRequestBase base, String multipartMd5) {
 		/*
 		 * StringToSign = HTTP-Verb + '\n' + base64(Content-MD5) + '\n' +
 		 * base64(x-4dnest-multipartMD5) + '\n' + Content-Type + '\n' + Date +
 		 * '\n' + RequestURI
 		 * 
 		 * Should be in utf-8 automatically.
 		 */
 		String user = this.nest.getUserName();
 		String key = this.nest.getSecretKey();
 		String verb = base.getMethod();
 		String requestUri = base.getURI().getPath();
 		Date date = new Date();
 		String stringToSign = verb + "\n" + "" + "\n" + multipartMd5 + "\n"
 				+ "" + "\n" + DateUtils.formatDate(date) + "\n" + requestUri;
 		// Log.d("stringtosign", stringToSign);
 
 		String authHead = URLEncoder.encode(user) + ":" + computeSignature(stringToSign, key);
 		// Log.d("HASH", authHead);
 		base.setHeader("Authorization", authHead);
 
 		base.setHeader("Date", DateUtils.formatDate(date));
 
 		base.setHeader("x-4dnest-multipartMD5", multipartMd5);
 	}
 
 	/**
 	 * Retrieves a file from uri to localpath over HTTP
 	 * 
 	 * @param uri
 	 *            Location of the file in server
 	 * @param localPath
 	 *            Local path where the file is to be saved
 	 * 
 	 * @return true if file retrieved successfully, false otherwise
 	 */
 	public boolean getMediaFile(String uri, String localPath) {
 		HttpClient client = CommUtils.createHttpClient();
 		try {
 			HttpGet request = new HttpGet(new URI(uri));
 			addAuthentication(request, "");
 			HttpResponse resp = client.execute(request);
 			Log.d(TAG, String.valueOf(resp.getStatusLine().getStatusCode()));
 			if (resp.getStatusLine().getStatusCode() != HTTP_STATUSCODE_OK) {
 				return false;
 			}
 			InputStream is = resp.getEntity().getContent();
 			GenericUtils.writeInputStreamToFile(is, localPath);
 			return true;
 
 		} catch (URISyntaxException e) {
 			Log.e(TAG, "getMediaFile: Invalid URI");
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, "getMediaFile: Execute failed");
 		} catch (IOException e) {
 			Log.e(TAG, "getMediaFile: Write operation failed");
 		}
 
 		return false;
 
 	}
 
 	/**
 	 * Turns egg into a JSON formatted string
 	 * 
 	 * @param egg
 	 * @return JSON formatted string, containing egg metadata
 	 */
 	public static String eggToJSONstring(Egg egg) {
 		JSONObject temp = new JSONObject();
 		try {
 			temp.put("author", egg.getAuthor());
 			temp.put("caption", egg.getCaption());
 			JSONArray tags = new JSONArray();
 
             for (int i = 0; i<egg.getTags().size(); i++) {
                 tags.put(egg.getTags().get(i).getName());
             }
             temp.put("tags", tags);
             if (egg.getLatitude() != 0 || egg.getLongitude() != 0) {
                 temp.put("lon", egg.getLongitude());
                 temp.put("lat", egg.getLatitude());
             }
 			return temp.toString();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			Log.d(TAG, "eggToJsonString: JSONException");
 		}
 		return "";
 	}
 
 	/**
 	 * Turns a JSONObject into an egg object
 	 * 
 	 * @param js
 	 *            The JSONobject
 	 * @return created egg
 	 */
 	private Egg jSONObjectToEgg(JSONObject js) {
 		try {
 			String caption = js.getString("caption");
 			String externalFileUriStr;
 			Uri externalFileUri = null;
 			try {
 				externalFileUriStr = js.getString("content_uri");
 				externalFileUri = Uri.parse(externalFileUriStr);
 			} catch (Exception e) {
 
 			    externalFileUri = null;
 				//No content_uri means text egg, so we leave the external file uri as null
 			}
 			String author = js.getString("author");
 			String thumbnailUriStr = null;
 			try {
 				thumbnailUriStr = js.getString("thumbnail_uri");
 			} catch (JSONException e) {
 				thumbnailUriStr = null;
 			}
 			Uri thumbNailUri = null;
 			if (thumbnailUriStr != null) {
 				thumbNailUri = Uri.parse(thumbnailUriStr);
 			}
 			ArrayList<Tag> tags = new ArrayList<Tag>();
 			try {
 				JSONArray tagar = js.getJSONArray("tags");
 				for (int i = 0; i < tagar.length(); i++) {
 					tags.add(new Tag(tagar.getString(i)));
 				}
 			} catch (JSONException e) {
 				// No tags
 			}
 			String dateStr = js.getString("created");
 			// Log.d("DATESTR", dateStr);
 			DateFormat formatter = new SimpleDateFormat(
 					("yyyy-MM-dd'T'HH:mm:ss"));
 			Date date;
 
             try {
                 date = (Date) formatter.parse(dateStr);
             } catch (ParseException e) {
                Log.e(TAG, "Failed to parse date");
                date = new Date();
             }
             double latitude = 0;
             double longitude = 0;
             try {
                 latitude = js.getDouble("lat");
                 longitude = js.getDouble("lon");
                 Log.d(TAG, "succesfully parsed location data");
             }catch (JSONException e) {
                 // No location information
             }
 			Egg egg = new Egg(0, this.nest.getId(), author, null, externalFileUri,thumbNailUri, caption, tags, 0);
 			String uid = js.getString("uid");
 			egg.setExternalId(uid);
 			egg.setLatitude(latitude);
 			egg.setLongitude(longitude);
 			egg.setCreationDate(date);
 			Log.d("EGGLATI", ":" + egg.getLatitude());
 			return egg;
 		} catch (JSONException e) {
 			Log.e("JSONTOEGG", "Got JSONexception");
 		}
 		return null;
 	}
 
 
 
 
 
 
 	/**
 	 * Checks whether the media file for the egg is in local storage and gets the file from 4dnest server if it isn't
 	 * 
 	 * @param Egg
 	 *            whose media file is in question
 	 * 
 	 * @return boolean whether media file can be found in predefined location
 	 */
 	public boolean getMedia(Egg egg) {
 		String path = MediaManager.getMediaUriString(egg);
 		return getMediaFile(egg.getRemoteFileURI().toString(), path);
 	}
 	
 	/**
 	 * Checks whether the thumbnail for the egg is in local storage and gets the file from 4dnest server if it isn't
 	 * 
 	 * @param Egg
 	 *            whose thumbnail is in question
 	 * @param size is thumbnail size from protocol public static field THUMBNAIL_SIZE_(LARGE or SMALL)
 	 * 
 	 * @return boolean whether thumbnail can be found in predefined location
 	 */
 	
 	public boolean getThumbnail(Egg egg, String size) {
 		String path = ThumbnailManager.getThumbnailUriString(egg, size);
 		boolean res = true;
 		if (!ThumbnailManager.thumbNailExists(egg, size)) {
 			String externalUriString = this.nest.getBaseURI()
 					+ THUMBNAIL_PATH + egg.getExternalId() + size
 					+ THUMBNAIL_FILETYPE;
 			Log.d(TAG, externalUriString);
 			String thumbnail_dir = Environment.getExternalStorageDirectory()
 					.getAbsolutePath() + File.separator + THUMBNAIL_LOCATION;
 			if (!new File(thumbnail_dir).exists()) {
 				new File(thumbnail_dir).mkdirs();
 			}
 			Log.d("SAVELOC", path);
 			if (getMediaFile(externalUriString, path)) {
 				Log.d(TAG, "Thumbnail written succesfully");
 				res = true;
 			} else {
 				Log.d(TAG, "Thumbnail failed to write");
 				res = false;
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * The url containing help for using the application with this nest.
 	 * @return A full url to a human-readable help webpage.
 	 */
 	public String getHelpURL() {
 		return this.nest.getBaseURI() + HELP_PATH;
 	}
 }
