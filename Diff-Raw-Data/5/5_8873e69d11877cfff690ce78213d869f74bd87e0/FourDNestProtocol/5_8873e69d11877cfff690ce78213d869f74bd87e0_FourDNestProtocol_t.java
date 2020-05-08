 package org.fourdnest.androidclient.comm;
 
 import android.app.Application;
 import android.net.Uri;
 import android.util.Log;
 
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.HttpResponse;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.impl.cookie.DateUtils;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.NameValuePair;
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.FourDNestApplication;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.Tag;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
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
 	private static final String JSON_FORMAT = "?format=json";
 	private static final String UNICODE = "UTF-8";
 	private static final int HTTP_STATUSCODE_OK = 200;
 	private static final int HTTP_STATUSCODE_CREATED = 201;
 	private static final int HTTP_STATUSCODE_UPDATED = 204;
 	private static final int HTTP_STATUSCODE_UNAUTHORIZED = 401;
 	private static final int HTTP_STATUSCODE_SERVER_ERROR = 500;
 	private static final int CONNECTION_TIMEOUT = 15000;
 	private Nest nest;
 
 	public FourDNestProtocol() {
 	    this.nest = null;
 	}
 
     /**
      * Parses egg's content and sends it in multipart mime format with HTTP
      * post.
      * 
      * @param egg The egg that we want to send to the server
      * 
      * @return HTTP status code and egg URI on server if creation successful
      **/
     public ProtocolResult sendEgg(Egg egg) {
         
         String concatedMd5 = "";
         HttpClient client = createHttpClient();
         HttpPost post = new HttpPost(this.nest.getBaseURI() + EGG_UPLOAD_PATH);
         String metadata = eggToJSONstring(egg);
         Log.d("METADATA", metadata);
 
         // Create list of NameValuePairs
         List<NameValuePair> pairs = new ArrayList<NameValuePair>();
         pairs.add(new BasicNameValuePair("data", metadata));
         String metadataMd5 = md5FromString(metadata);
         Log.d("metadataMD5", metadataMd5);
 
         concatedMd5 += metadataMd5;
         if (egg.getLocalFileURI() != null) {
             if (new File(egg.getLocalFileURI().getPath()).isFile()) {
                 pairs.add(new BasicNameValuePair("file", egg.getLocalFileURI()
                         .getPath()));
                 
                 String fileMd5 = md5FromFile(egg.getLocalFileURI().getPath());
                 Log.d("fileMD5", fileMd5);
                 
                 concatedMd5 += fileMd5;
             }
         }
         
         String multipartMd5String = md5FromString(concatedMd5);
         multipartMd5String = new String(Base64.encodeBase64(multipartMd5String.getBytes()));
         
         int status = 0;
         try {
             post.setEntity(this.createEntity(pairs));
             addAuthentication(post, multipartMd5String);
             Log.d("AUTH", post.getHeaders("Authorization")[0].getValue());
             HttpResponse response = client.execute(post);
             status = response.getStatusLine().getStatusCode();
 
             return this.parseResult(status, response);
 
         } catch (ClientProtocolException e) {
             Log.e(TAG, "ClientProtocolException, egg not sent "
                     + e.getMessage());
             return new ProtocolResult(null, ProtocolResult.SENDING_FAILED);
         } catch (IOException e) {
             Log.e(TAG, "IOException, egg not sent " + e.getMessage());
             return new ProtocolResult(null, ProtocolResult.SENDING_FAILED);
         }
     }
     
     /**
      * Overwrites the metadata on the server with the data from the egg given as a parameter.
      * @param egg The egg containing the new metadata
      * 
      * @return ProtocolResult detailing if the update was successful
      */
     public ProtocolResult overwriteEgg(Egg egg) {
     	if (egg.getExternalId() == null) {
     		 return new ProtocolResult(null, ProtocolResult.SENDING_FAILED);
     	}
     	 HttpClient client = createHttpClient();
          HttpPut request = new HttpPut(this.nest.getBaseURI() + EGG_DOWNLOAD_PATH + egg.getExternalId() + "/");
          Log.d("OVERURI", request.getURI().getPath());
          String metadata = eggToJSONstring(egg);
          Log.d("OVERMETA", metadata);
 
          // Create list of NameValuePairs
          List<NameValuePair> pairs = new ArrayList<NameValuePair>();
          pairs.add(new BasicNameValuePair("data", metadata));
          String metadataMd5 = md5FromString(metadata);
          String multipartMd5String = md5FromString(metadataMd5);
          multipartMd5String = new String(Base64.encodeBase64(multipartMd5String.getBytes()));
          int status = 0;
          try {
              StringEntity se = new StringEntity(metadata, UNICODE);
         	 request.setEntity(se);
         	 request.setHeader("Content-type", "application/json");
 			 addAuthentication(request, multipartMd5String);
 	         HttpResponse response = client.execute(request);
 	         status = response.getStatusLine().getStatusCode();
 	         Log.d("OVERSTATUS", String.valueOf(status));
 	         return this.parseResult(status, response);
          } catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
			Log.e(TAG, "Failed to overwrite egg: ClientProtocolException");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
		    Log.e(TAG, "Failed to overwrite egg: IOException");
 		}
          return new ProtocolResult(null, ProtocolResult.SENDING_FAILED);
     }
     
     /**
      * Creates the proper ProtocolResult object from the server response
      * 
      * @param statusCode The statuscode given in the server response
      * @param response The response from the server.
      * @return The created ProtocolResult
      */
     private ProtocolResult parseResult(int statusCode, HttpResponse response) {
         if (statusCode == HTTP_STATUSCODE_CREATED) {
             return new ProtocolResult(response.getHeaders("Location")[0]
                     .getValue(), ProtocolResult.RESOURCE_UPLOADED);
         }
         else if (statusCode == HTTP_STATUSCODE_UNAUTHORIZED) {
             return new ProtocolResult(null, ProtocolResult.AUTHORIZATION_FAILED);
         }
         else if (statusCode == HTTP_STATUSCODE_SERVER_ERROR) {
             return new ProtocolResult(null, ProtocolResult.SERVER_INTERNAL_ERROR);
         }else if (statusCode == HTTP_STATUSCODE_UPDATED) {
            return new ProtocolResult(null, ProtocolResult.RESOURCE_UPDATED);
         } else {
             Log.d("sendEgg: UNKNOWN_RESULT", String.valueOf(statusCode));
             return new ProtocolResult(null, ProtocolResult.UNKNOWN_REASON);
         }
     }
 
     /**
      * Creates the MultipartEntity from name-value -pair list
      * 
      * @throws UnsupportedEncodingException
      */
     private MultipartEntity createEntity(List<NameValuePair> pairs)
             throws UnsupportedEncodingException {
     	Charset charset = Charset.forName(UNICODE);
         MultipartEntity entity = new MultipartEntity(HttpMultipartMode.STRICT, null, charset);
 
         for (int i = 0; i < pairs.size(); i++) {
             File file = new File(pairs.get(i).getValue());
             if (pairs.get(i).getName().equalsIgnoreCase("file")) {
                 entity.addPart(pairs.get(i).getName(), new FileBody(file));
             } else {
             	StringBody strbd = new StringBody(pairs
                         .get(i).getValue(), charset);
                 entity.addPart(pairs.get(i).getName(), strbd);
                 //Log.d("STRINGBODY", strbd.getCharset());
             }
         }
         Log.d("CONTENTTYPE", entity.getContentType().getValue());
         return entity;
     }
     /**
      * Creates a new HTTPClient with configured parameters and schemes.
      * @return DefaultHttpClient
      */
     private DefaultHttpClient createHttpClient() {
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         // http scheme
         schemeRegistry.register(new Scheme("http", PlainSocketFactory
                 .getSocketFactory(), 80));
         FourDNestApplication app = FourDNestApplication.getApplication();
         boolean aac;
         if (app == null) {
         	Log.d(TAG, "app was null");
         	aac = true;
         } else {
 			aac = app.getAllowAllCerts();
 		}
         if (aac) {
 			// https scheme, all certs allowed
 			schemeRegistry.register(new Scheme("https",
 					new EasySSLSocketFactory(), 443));
 		}else {
 			// doesn't allow all certs
 			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
 		}
 		HttpParams params = new BasicHttpParams();
         params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
         HttpProtocolParams.setContentCharset(params, UNICODE);
         HttpProtocolParams.setHttpElementCharset(params, UNICODE);
         ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
         return new DefaultHttpClient(cm, params);
     }
     
     //FIXME: Count not yet implemented here, because no implementation server side
     public List<Tag> topTags(int count) {
     	ArrayList<Tag> tags = new ArrayList<Tag>();
         HttpClient client = createHttpClient();
         HttpGet request = new HttpGet();
         String uriPath = this.nest.getBaseURI() + TAG_DOWNLOAD_PATH + JSON_FORMAT;
         Log.d("TAGURI", uriPath);
         
         try {
 			request.setURI(new URI(uriPath));
 			addAuthentication(request, "");
 			String jsonStr = responseToString(client.execute(request));
 			JSONObject outer = new JSONObject(jsonStr);
 			JSONArray jsonTags = outer.getJSONArray("objects");
 			for (int i = 0; i < jsonTags.length(); i++) {
 				JSONObject current = jsonTags.getJSONObject(i);
 				tags.add(new Tag(current.getString("name")));
 				Log.d(("TAG" + i), tags.get(i).getName());
 			}
 			return tags;
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
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
      * @param uid The server side id of the egg that we want to retrieve.
      * @return The retrieved egg
      */
     public Egg getEgg(String uid) {
     	HttpClient client = createHttpClient();
         HttpGet request = new HttpGet();
         String temp = "http://test42.4dnest.org/";
         String uriPath = temp + EGG_DOWNLOAD_PATH + uid + "/" + JSON_FORMAT;
         Log.d("URI", uriPath);
        
     	try {
     		request.setURI(new URI(uriPath));
     		addAuthentication(request, "");
     		String jsonStr = responseToString(client.execute(request));
 	    	JSONObject js = new JSONObject(jsonStr);
 	    	return jSONObjectToEgg(js);
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	return null;
     }
     
     /**
      * Retrieves the metadata of all the eggs in the server. Parses this metadata and creates a list of eggs from it
      * 
      * @return List of egg objects, obtained from the server.
      */
     public List<Egg> getStream() {
     	Egg current = null;
     	ArrayList<Egg> eggList = new ArrayList<Egg>();
     	HttpClient client = createHttpClient();
         HttpGet request = new HttpGet();
         String uriPath = "http://test42.4dnest.org/fourdnest/api/v1/egg/?format=json"; //this.nest.getBaseURI() + EGG_DOWNLOAD_PATH + JSON_FORMAT;
         Log.d("URIStream", uriPath);
         try {
 			request.setURI(new URI(uriPath));
 			addAuthentication(request, "");
 			String jsonStr = responseToString(client.execute(request));
 			JSONObject outer = new JSONObject(jsonStr);
 			JSONArray jsonArr = outer.getJSONArray("objects");
 			for (int i = 0; i < jsonArr.length(); i++) {
 				current = jSONObjectToEgg(jsonArr.getJSONObject(i));
 				if (current != null) {
 					Log.d("CURRENTEGG", current.getExternalId());
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
 		}
         return eggList;
     }
     
     /**
      * Retrieves a file from uri to localpath over HTTP
      * 
      * @param uri Location of the file in server
      * @param localPath Local path where the file is to be saved
      * 
      * @return true if file retrieved successfully, false otherwise
      */
     public boolean getMediaFile(String uri, String localPath) {
     	HttpClient client = createHttpClient();
     	try {
 			HttpGet request = new HttpGet(new URI(uri));
 			addAuthentication(request, "");
 			HttpResponse resp = client.execute(request);
 			if (resp.getStatusLine().getStatusCode() != HTTP_STATUSCODE_OK) {
 				return false;
 			}
 			InputStream is = resp.getEntity().getContent();
 			writeInputStreamToFile(is, localPath);
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
     
     private void writeInputStreamToFile(InputStream is, String path) throws IOException {
         BufferedInputStream bis = new BufferedInputStream(is);
         FileOutputStream os = new FileOutputStream(new File(path));
         BufferedOutputStream bos = new BufferedOutputStream(os);
         int c;
         try {
             while ((c = bis.read()) != -1) {
                 bos.write(c);
             }
         } finally {
             bos.close();
             bis.close();
         }
     }
     /**
      * Turns a JSONObject into an egg object
      * @param js The JSONobject
      * @return created egg
      */
     private Egg jSONObjectToEgg(JSONObject js) {
     	try {
 			String caption = js.getString("caption");
 			String externalFileUri = js.getString("resource_uri");
 			String author = js.getString("author");
 			ArrayList<Tag> tags = new ArrayList<Tag>();
 			try {
 				JSONArray tagar = js.getJSONArray("tags");
 				for (int i = 0; i < tagar.length(); i++) {
 					tags.add(new Tag(tagar.getString(i)));
 				}
 			} catch (JSONException e) {
 				// No tags
 			}
 			Egg egg = new Egg(0, this.nest.getId(), author, null, Uri.parse(externalFileUri), caption, tags, 0);
 			String uid = js.getString("uid");
 			egg.setExternalId(uid);
 			return egg;
 		} catch (JSONException e) {
 			Log.e("JSONTOEGG", "Got JSONexception");
 		}
     	return null;
     }
     
     /**
      * Turns egg into a JSON formatted string
      * @param egg
      * @return JSON formatted string, containing egg metadata
      */
     private String eggToJSONstring(Egg egg) {
     	JSONObject temp = new JSONObject();
     	try {
 			temp.put("author", egg.getAuthor());
 			temp.put("caption", egg.getCaption());
 			JSONArray tags = new JSONArray();
             for (int i = 0; i<egg.getTags().size(); i++) {
                 tags.put(new String(egg.getTags().get(i).getName()));
             }
             temp.put("tags", tags);
 			return temp.toString();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	return "";
     }
     
     private String responseToString(HttpResponse response) throws IOException {
     	InputStream content = null;
     	content = response.getEntity().getContent();
 		ByteArrayOutputStream bout = new ByteArrayOutputStream();
     	byte[] buf = new byte[1024];
     	int len;
     	while ((len = content.read(buf)) > 0) {
     	bout.write(buf, 0, len);
     	}
     	content.close();
     	return bout.toString();
     }
     
     
     /**
      * Hashes the string in MD5 and returns a base64 encoded string
      * of the hash
      * 
      * @param String to be hashed
      * 
      * @return Base64 encoded md5 hash
      **/
     private String md5FromString(String s) {
         String result = "";
         if (s != null) {
             try {
                 byte[] bytes = DigestUtils.md5(s.getBytes(UNICODE));
                 result = new String(Hex.encodeHex(bytes));
     
             } catch (UnsupportedEncodingException e) {
                 // TODO Auto-generated catch block
                 
             }
             
         }
         return result;
     }
     /**
      * Hashes the file with the given path in MD5 and returns a base64 encoded string
      * of the hash
      * 
      * @param Path of the file
      * 
      * @return Base64 encoded md5 hash
      **/
     private String md5FromFile(String path) {
         String result = "";
         try {
             FileInputStream fis = new FileInputStream( new File(path));
             byte[] bytes = DigestUtils.md5(fis);
             result = new String(Hex.encodeHex(bytes));
             
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             
         } catch (IOException e) {
             // TODO Auto-generated catch block
            
         }
         return result;
         
     }
     /**
      * Computes the Hmac-Sha1 signature for the given string
      * @param stringToSign String to be signed
      * @param secretKey The key that is used to sign the string
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
             String hexStr = new String(Hex.encodeHex(mac.doFinal(stringToSign.getBytes())));
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
      * @param base The Httpmessage base
      * @param multipartMd5 the multipart md5 string
      */
     private void addAuthentication(HttpRequestBase base, String multipartMd5) {
         /* StringToSign = HTTP-Verb + '\n' +
          * base64(Content-MD5) + '\n' +
          * base64(x-4dnest-multipartMD5) + '\n' +
          * Content-Type + '\n' +
          * Date + '\n' +
          * RequestURI
          * 
          * Should be in utf-8 automatically.
          */
         String user = "Hard-coded";
         String key = "secret";
         String verb = base.getMethod();
         String requestUri = base.getURI().getPath();
         Date date = new Date();
         String stringToSign = verb + "\n" + "" + "\n" + multipartMd5 + "\n"
                 + "" + "\n" + DateUtils.formatDate(date) + "\n" + requestUri;
         Log.d("stringtosign", stringToSign);
 
         String authHead = user + ":" + computeSignature(stringToSign, key);
         Log.d("HASH", authHead);
         base.setHeader("Authorization", authHead);
         
         base.setHeader("Date", DateUtils.formatDate(date));
         
         base.setHeader("x-4dnest-multipartMD5", multipartMd5);
     }
     
 }
