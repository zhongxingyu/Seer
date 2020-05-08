 package net.ecoarttech.ihplus.network;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import net.ecoarttech.ihplus.model.ActionType;
 import net.ecoarttech.ihplus.model.Hike;
 import net.ecoarttech.ihplus.model.ScenicVista;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class UploadHikeTask extends AsyncTask<Void, Void, HttpResponse> {
 
 	private static final String TAG = "IH+ - UploadHikeTask";
 	private Context mContext;
 	private Hike mHike;
 	private Handler mHandler;
 	private ProgressDialog mDialog;
 
 	public UploadHikeTask(Context context, Hike hike, Handler handler) {
 		this.mContext = context;
 		this.mHike = hike;
 		this.mHandler = handler;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		mDialog = new ProgressDialog(mContext);
 		mDialog.setMessage("uploading hike data.");
 		mDialog.show();
 	}
 
 	@Override
 	protected HttpResponse doInBackground(Void... arg0) {
 		Uri.Builder builder = Uri.parse(NetworkConstants.UPLOAD_HIKE_URL).buildUpon();
 		Uri uri = builder.build();
 		try {
 			Log.d(TAG, "Uri: " + uri);
 			// Log.d(TAG, "params: " + parameters);
 			HttpPost request = new HttpPost(uri.toString());
 			MultipartEntity entity = new MultipartEntity();
 			entity.addPart(NetworkConstants.REQUEST_JSON_HIKE_NAME, new StringBody(mHike.getName()));
 			entity.addPart(NetworkConstants.REQUEST_JSON_HIKE_USER, new StringBody(mHike.getUsername()));
 			entity.addPart(NetworkConstants.REQUEST_JSON_HIKE_DESC, new StringBody(mHike.getDescription()));
 			entity.addPart(NetworkConstants.REQUEST_JSON_HIKE_VISTAS, new StringBody(mHike.getVistasAsJson(mContext)));
 			entity.addPart(NetworkConstants.REQUEST_JSON_HIKE_ORIG, new StringBody(mHike.isOriginal().toString()));
 			if (mHike.isOriginal()) {
 				entity.addPart(NetworkConstants.REQUEST_JSON_HIKE_LAT, new StringBody(mHike.getStartLat().toString()));
 				entity.addPart(NetworkConstants.REQUEST_JSON_HIKE_LNG, new StringBody(mHike.getStartLng().toString()));
 				entity.addPart(NetworkConstants.REQUEST_JSON_HIKE_POINTS, new StringBody(mHike.getPointsAsJson()));
 			}
 			Log.d(TAG, "entity? " + entity);
 			// add any photos
			for (ScenicVista vista : mHike.getVistas()) {
 				if (vista.getActionType() == ActionType.PHOTO) {
					entity.addPart(NetworkConstants.REQUEST_JSON_HIKE_PHOTOS, vista.getUploadFile(mContext));
 				}
 			}
 			request.setEntity(entity);// new UrlEncodedFormEntity(parameters));
 			DefaultHttpClient httpClient = new DefaultHttpClient(NetworkConstants.getHttpParams());
 			HttpResponse response;
 			response = httpClient.execute(request);
 			return response;
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(HttpResponse result) {
 		mDialog.dismiss();
 		if (result != null) {
 			Message msg = new Message();
 			msg.what = NetworkConstants.FAILURE;
 			try {
 				InputStreamReader is = new InputStreamReader(result.getEntity().getContent());
 				BufferedReader br = new BufferedReader(is);
 				StringBuilder responseText = new StringBuilder();
 				String line;
 				while ((line = br.readLine()) != null) {
 					responseText.append(line);
 				}
 				Log.d(TAG, "Server response: " + responseText);
				// parse out success/failure
 				try {
 					JSONObject respJson = new JSONObject(responseText.toString());
					if (respJson.getBoolean(NetworkConstants.SERVER_RESULT))
 						msg.what = NetworkConstants.SUCCESS;
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			mHandler.sendMessage(msg);
 		}
 		super.onPostExecute(result);
 	}
 }
