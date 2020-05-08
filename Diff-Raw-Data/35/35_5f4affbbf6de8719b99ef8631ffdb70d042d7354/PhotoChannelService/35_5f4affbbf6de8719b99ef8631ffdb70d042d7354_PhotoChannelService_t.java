 package itu.perv.PicPush;
 
import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import edu.gvsu.cis.masl.channelAPI.ChannelService;
 
 public class PhotoChannelService implements ChannelService {
 	private PicturePushActivity ppa;
 	private String NFCid;

	public PhotoChannelService(PicturePushActivity ppa, String NFCid) {
 		this.ppa = ppa;
 		this.NFCid = NFCid;
 	}

 	@Override
 	public void onOpen() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onMessage(String message) {
 		try {
 			Log.i("msg", message);
			JSONObject json = new JSONArray(message).getJSONObject(0);

			if (json.getString("nfcId").equals(NFCid)
					&& !json.getString("source").equals("android")) {
 				ppa.getMessage(json);
 			}

 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onClose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onError(Integer errorCode, String description) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
