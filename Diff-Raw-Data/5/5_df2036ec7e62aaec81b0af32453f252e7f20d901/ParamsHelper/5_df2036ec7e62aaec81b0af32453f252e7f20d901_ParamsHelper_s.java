 package eu.trentorise.smartcampus.android.common.params;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import eu.trentorise.smartcampus.android.common.Utils;
 
 public class ParamsHelper {
 
 	public static <T> T load(Context mContext, String assetFilename, Class<T> clazz) {
 		AssetManager assetManager = mContext.getResources().getAssets();
 		try {
 			InputStream in = assetManager.open(assetFilename);
 			String jsonParams = getStringFromInputStream(in);
 			return Utils.convertJSONToObject(jsonParams, clazz);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public static Map<Object, Object> load(Context mContext, String assetFilename) {
 		return load(mContext, assetFilename, Map.class);
 	}
 
 	private static String getStringFromInputStream(InputStream is) {
 		String output = new String();
 
 		BufferedReader br = null;
 		StringBuilder sb = new StringBuilder();
 		String line;
 
 		try {
 			br = new BufferedReader(new InputStreamReader(is));
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		String json = sb.toString();
 
 		try {
 			JSONObject jsonObject = new JSONObject(json);
 			output = jsonObject.toString();
 		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
 
 		return output;
 	}
 
 }
