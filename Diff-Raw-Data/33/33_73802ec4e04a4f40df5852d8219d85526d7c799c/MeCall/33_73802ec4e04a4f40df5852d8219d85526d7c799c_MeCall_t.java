 package com.gurkensalat.android.xingsync.api;
 
 import org.androidannotations.annotations.EBean;
 import org.androidannotations.annotations.sharedpreferences.Pref;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.gurkensalat.android.xingsync.preferences.SyncPrefs_;

 @EBean
 public class MeCall // implements XingApiCall
 {
 	private static Logger LOG = LoggerFactory.getLogger(MeCall.class);
 
 	@Pref
 	SyncPrefs_ syncPrefs;
 
 	public JSONObject perform(final Object... args)
 	{
 		JSONObject json = new JSONObject();
 
 		if (syncPrefs.debugMockApiCalls().get())
 		{
 			StringBuffer sb = new StringBuffer(1024);
 
 			sb.append("{");
 			sb.append("  \"users\": [");
 			sb.append("    {");
 			sb.append("      \"id\": \"3382304_64b174\",");
 			sb.append("      \"first_name\": \"Hakan\",");
 			sb.append("      \"last_name\": \"Tandogan\",");
 			sb.append("      \"display_name\": \"Hakan Tandogan\"");
 			sb.append("    }");
 			sb.append("  ]");
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
 			LOG.info("Hardcoded Mock for now");
 		}
 
 		// LOG.info(json.toString());
 
 		return json;
 	}
 
 	public User performAndParse(final Object... args)
 	{
 		User user = null;
 		JSONObject json = perform(args);
 
 		try
 		{
 			user = User.fromJSON(json);
 		}
 		catch (JSONException e)
 		{
 			LOG.error("While parsing JSON", e);
 		}
 
 		return user;
 	}
 }
