 package in.ccl.adapters;
 
 import in.ccl.helper.CommonAsync;
 import in.ccl.helper.DelegatesResponse;
 import in.ccl.model.Items;
 import in.ccl.photo.ScaleImageView;
 import in.ccl.ui.R;
 import in.ccl.util.Constants;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 
 public class FullPagerAdapter extends PagerAdapter implements DelegatesResponse {
 
 	private LayoutInflater inflater;
 
 	private ArrayList <Items> itemsList;
 
 	private Activity activity;
 
 	private Dialog shareDialog;
 
 	private ListView imageList;
 
 	private String network_provider;
 
 	static Facebook mFaceBook = new Facebook("429104203832155");
 
 	private String mFaceBookUser_Id;
 
 	private SharedPreferences myPrefs;
 
 	private String mFinalResponse;
 
 	private SharedPreferences.Editor prefsEditor;
 
 	ArrayAdapter <String> adapter;
 
 	private String myFacebookId;
 
 	private String url = "";
 
	private Context context;

 	private String access_token;
 
 	private String[] imageDetails = { "LIKE", "COMMENT", "SHARE" };
 
 	// private Category mCategory;
 	public Handler handler = new Handler() {
 
 		@Override
 		public void handleMessage (Message msg) {
 			if (msg.what == Constants.LOGIN_SUCCESS) {
 
 				if (mFaceBook.getAccessToken() != null) {
 					String url = "https://graph.facebook.com/me?access_token=" + mFaceBook.getAccessToken();
					CommonAsync mCommonAsync = new CommonAsync(context, FullPagerAdapter.this);
 					mCommonAsync.execute(url);
 
 				}
 			}
 
 		}
 	};
 
 	@Override
 	public void destroyItem (ViewGroup container, int position, Object object) {
 		((ViewPager) container).removeView((View) object);
 	}
 
 	public FullPagerAdapter (Activity ctx, ArrayList <Items> list) {
 		activity = ctx;
 		itemsList = list;
 		// mCategory = category;
 		inflater = activity.getLayoutInflater();
 	}
 
 	@Override
 	public int getCount () {
 		return itemsList.size();
 	}
 
 	@Override
 	public View instantiateItem (View view, int position) {
 		View imageLayout = null;
 		ScaleImageView imageView = null;
 		// ImageView spinner = null;
 
 		imageLayout = inflater.inflate(R.layout.full_image, null);
 		imageView = (ScaleImageView) imageLayout.findViewById(R.id.image);
 		// imageView.setFullScreen(true);
 		// spinner = (ImageView) imageLayout.findViewById(R.id.loading);
 		imageView.setTag(itemsList.get(position).getPhotoOrVideoUrl());
 		TextView errorTxt = (TextView) imageLayout.findViewById(R.id.error_title);
 		// loadingImage = (ImageView) imageLayout.findViewById(R.id.loading);
 		imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.blackbackground));
 		imageView.setTag(itemsList.get(position).getPhotoOrVideoUrl());
 		imageView.setImageURL(itemsList.get(position).getPhotoOrVideoUrl(), true, activity.getResources().getDrawable(R.drawable.blackbackground), errorTxt);
 
 		((ViewPager) view).addView(imageLayout, 0);
 
 		imageView.setOnLongClickListener(new OnLongClickListener() {
 
 			@Override
 			public boolean onLongClick (final View imageViewUrl) {
 				shareDialog = new Dialog(activity);
 				shareDialog.setContentView(R.layout.dialog_layout);
 				shareDialog.setTitle("Image displayed in FaceBook");
 				imageList = (ListView) shareDialog.findViewById(R.id.dialog_list);
 				adapter = new ArrayAdapter <String>(activity, android.R.layout.simple_list_item_1, imageDetails);
 				imageList.setAdapter(adapter);
 				shareDialog.show();
 				imageList.setOnItemClickListener(new OnItemClickListener() {
 
 					@Override
 					public void onItemClick (AdapterView <?> arg0, View arg1, int position, long arg3) {
 						url = imageViewUrl.getTag().toString();
 						myPrefs = activity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
 						myFacebookId = myPrefs.getString("FaceBook_User_Id", null);
 						access_token = myPrefs.getString("access_token", null);
 						if (position == 0) {
 
 							Toast.makeText(activity, "Need to Implement", Toast.LENGTH_LONG).show();
 
 						}
 						else if (position == 1) {
 							Toast.makeText(activity, "Need to Implement", Toast.LENGTH_LONG).show();
 
 						}
 						else {
 							if (mFaceBook != null) {
 								if (access_token == null && myFacebookId == null) {
 									authorization();
 								}
 								else {
 									shareOnFaceBook(url);
 
 								}
 
 							}
 						}
 
 						shareDialog.cancel();
 					}
 
 					private void authorization () {
 						mFaceBook.authorize(activity, new String[] { "publish_stream" }, new DialogListener() {
 
 							public void onFacebookError (FacebookError e) {
 							}
 
 							public void onError (DialogError e) {
 							}
 
 							public void onComplete (Bundle values) {
 								myPrefs = activity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
 								prefsEditor = myPrefs.edit();
 								prefsEditor.putString("access_token", mFaceBook.getAccessToken());
 								prefsEditor.commit();
 
 								handler.sendEmptyMessage(Constants.LOGIN_SUCCESS);
 							}
 
 							public void onCancel () {
 							}
 						});
 					}
 				});
 				return true;
 			}
 
 		});
 		return imageLayout;
 
 	}
 
 	@Override
 	public boolean isViewFromObject (View view, Object object) {
 		return view.equals(object);
 	}
 
 	@Override
 	public void setData (String jsonData, String isFrom) {
 		if (isFrom.equalsIgnoreCase("CommonAsync")) {
 			mFinalResponse = jsonData;
 			try {
 				JSONObject fbJsonObject = new JSONObject(mFinalResponse);
 				if (fbJsonObject.has("id")) {
 					if (!fbJsonObject.isNull("id")) {
 						mFaceBookUser_Id = fbJsonObject.getString("id");
 						SharedPreferences myPrefs = activity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
 						SharedPreferences.Editor prefsEditor = myPrefs.edit();
 						prefsEditor.putString("FaceBook_User_Id", mFaceBookUser_Id);
 						prefsEditor.commit();
 					}
 				}
 
 				network_provider = "facebook";
 
 				shareOnFaceBook(url);
 
 			}
 			catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		else if (isFrom.equalsIgnoreCase("SampleUploadListener")) {
 			if (jsonData.equals("Success")) {
 				Looper.prepare();
 				Toast.makeText(activity, "Successfully Shared", Toast.LENGTH_LONG).show();
 				Looper.loop();
 			}
 			else if (jsonData.equals("error")) {
 				Looper.prepare();
 				Toast.makeText(activity, "Failed", Toast.LENGTH_LONG).show();
 				Looper.loop();
 			}
 		}
 
 	}
 
 	private void shareOnFaceBook (String shareImageUrl) {
 		Bundle params = new Bundle();
 		params.putString("link", url);
 		params.putString("message", " From CCL Android Application");
 
 		if (!mFaceBook.isSessionValid()) {
 
 			// this is getting access token from the Shared preferences
 			SharedPreferences mypref = activity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
 			mFaceBook.setAccessToken(mypref.getString("access_token", null));
 		}
 
 		AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(mFaceBook);
 		mAsyncRunner.request("me/feed", params, "POST", new in.ccl.helper.SampleUploadListener(FullPagerAdapter.this), null);
 
 	}
 
 }
