 package com.gurkensalat.android.xingsync.api;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 
 import org.androidannotations.annotations.EBean;
 import org.androidannotations.annotations.sharedpreferences.Pref;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gurkensalat.android.xingsync.keys.XingOAuthKeys;
 
 @EBean
 public class ContactsCall
 {
 	private static Logger LOG = LoggerFactory.getLogger(ContactsCall.class);
 
 	@Pref
 	SyncPrefs_ syncPrefs;
 
 	public JSONObject perform(final Object... args)
 	{
 		JSONObject json = new JSONObject();
 
 		if (syncPrefs.debugMockApiCalls().get())
 		{
 			LOG.info("About to mock API call");
 
 			StringBuffer sb = new StringBuffer(1024);
 
 			sb.append("{");
 			sb.append("  \"contacts\": {");
 			sb.append("    \"total\": 297,");
 			sb.append("    \"users\": [");
 			sb.append("      {");
 			sb.append("        \"id\": \"6841253_007b9e\",");
 			sb.append("        \"first_name\": \"Hasmet\",");
 			sb.append("        \"last_name\": \"Acar\",");
 			sb.append("        \"display_name\": \"Hasmet Acar\",");
 			sb.append("        \"active_email\": null");
 			sb.append("      },");
 			sb.append("      {");
 			sb.append("        \"id\": \"4261890_ffd916\",");
 			sb.append("        \"first_name\": \"Murat\",");
 			sb.append("        \"last_name\": \"Acun\",");
 			sb.append("        \"display_name\": \"Murat Acun\",");
 			sb.append("        \"active_email\": null");
 			sb.append("      },");
 			sb.append("      {");
 			sb.append("        \"id\": \"12824542_5078cc\",");
 			sb.append("        \"first_name\": \"Petra\",");
 			sb.append("        \"last_name\": \"Ghotra\",");
 			sb.append("        \"display_name\": \"Petra Ghotra\",");
 			sb.append("        \"active_email\": null");
 			sb.append("      }");
 			sb.append("    ]");
 			sb.append("  }");
 			sb.append("}");
 
 			LOG.info(sb.toString());
 
 			try
 			{
 				json = new JSONObject(sb.toString());
 			}
 			catch (JSONException e)
 			{
 				LOG.error("while parsing answer string", e);
 			}
 		}
 		else
 		{
 			LOG.info("About to make real API call");
 
 			// For OAuth.debugOut()
 			System.setProperty("debug", "true");
 
 			String access_token = syncPrefs.oauth_token().get();
 			String token_secret = syncPrefs.oauth_token_secret().get();
 
 			LOG.info("    TOKEN: '{}'", access_token);
 			LOG.info("    TOKEN SECRET: '{}'", token_secret);
 
 			// create a consumer object and configure it with the access
 			// token and token secret obtained from the service provider
 			//
 			// OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY,
 			// CONSUMER_SECRET);
 			OAuthConsumer consumer = new CommonsHttpOAuthConsumer(XingOAuthKeys.CONSUMER_KEY, XingOAuthKeys.CONSUMER_SECRET);
 			LOG.info("    consumer: {}", consumer);
 			consumer.setTokenWithSecret(access_token, token_secret);
 			LOG.info("    consumer: {}", consumer);
 
 			StringBuilder callUrl = new StringBuilder();
 
 			callUrl.append(XingOAuthKeys.API_URL_BASE);
 			callUrl.append("/users/me/contacts.json");
 			callUrl.append("?limit=100");
 			// callUrl.append("&offset=86");
 			// callUrl.append("&order_by=last_name");
 			callUrl.append("&user_fields=").append(syncPrefs.fieldsToFetch().get());
 			// callUrl.append("");
 			// callUrl.append("");
 
 			try
 			{
 				// create an HTTP request to a protected resource
 				LOG.info("    url: {}", callUrl.toString());
 
 				DefaultHttpClient httpclient = new DefaultHttpClient();
 				LOG.info("    httpclient: {}", httpclient);
 
 				HttpGet request = new HttpGet(callUrl.toString());
 				LOG.info("    request: {}", request);
 
 				// sign the request
 				consumer.sign(request);
 				LOG.info("    consumer: {}", consumer);
 
 				// send the request
 				HttpResponse response = httpclient.execute(request);
 				LOG.info("    status : {}", response.getStatusLine());
 
 				InputStream data = response.getEntity().getContent();
 				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data));
 				String responseLine;
 				StringBuilder responseBuilder = new StringBuilder();
 				while ((responseLine = bufferedReader.readLine()) != null)
 				{
 					LOG.info("    read: '{}'", responseLine);
 					responseBuilder.append(responseLine);
 				}
 				// Log.i(TAG,"Response : " + responseBuilder.toString());
 				// return responseBuilder.toString();
 
 				LOG.info(responseBuilder.toString());
 
 				try
 				{
 					json = new JSONObject(responseBuilder.toString());
 				}
 				catch (JSONException e)
 				{
 					LOG.error("while parsing answer string", e);
 				}
 			}
 			catch (Exception e)
 			{
 				LOG.error("While making real API call", e);
 			}
 		}
 
 		return json;
 	}
 
 	public List<User> parse(JSONObject json)
 	{
 		List<User> allUsers = new ArrayList<User>();
 
 		if (json != null)
 		{
 			json = json.optJSONObject("contacts");
 			// System.err.println("OBJECT-contacts: " + json);
 			if (json != null)
 			{
 				JSONArray array = json.optJSONArray("users");
 				// System.err.println("ARRAY-users: " + array);
 				if ((array != null) && (array.length() > 0))
 				{
 					for (int i = 0; i < array.length(); i++)
 					{
 						JSONObject innerJson = array.optJSONObject(i);
 
 						// System.err.println("INNER #" + i + ": " + innerJson);
 						try
 						{
 							User user = User.fromJSON(innerJson);
 							if (user != null)
 							{
 								allUsers.add(user);
 							}
 						}
 						catch (JSONException e)
 						{
 							LOG.error("while creating inner JSON object", e);
 						}
 					}
 				}
 			}
 		}
 
 		return allUsers;
 	}
 
 	public List<User> performAndParse(final Object... args)
 	{
 		JSONObject json = perform(args);
 		return parse(json);
 	}
 }
