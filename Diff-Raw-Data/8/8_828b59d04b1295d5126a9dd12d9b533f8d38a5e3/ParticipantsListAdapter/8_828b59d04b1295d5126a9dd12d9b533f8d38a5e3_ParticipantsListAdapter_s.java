 package com.nguyenmp.gauchodroid.course;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Process;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.nguyenmp.gauchodroid.R;
 import com.nguyenmp.gauchodroid.common.HandledThread;
 import com.nguyenmp.gauchospace.thing.User;
 import com.nguyenmp.gauchospace.thing.User.Attribute;
 
 public class ParticipantsListAdapter extends BaseAdapter {
 	private final List<User> mParticipants;
 	private final Context mContext;
 	private final ImageStore mImageStore;
 	private final AvatarDownloadListener mListener;
 	
 	ParticipantsListAdapter(List<User> participants, Context context, AvatarDownloadListener listener) {
 		mParticipants = participants;
 		mContext = context;
 		mImageStore = new ImageStore();
 		mListener = listener;
 	}
 	
 	@Override
 	public int getCount() {
 		return mParticipants.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return mParticipants.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return 0;
 	}

 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		User participant = mParticipants.get(position);
 		
 		if (convertView == null) {
 			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_user, parent, false);
 		}
 		
 		ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.list_item_user_progress_bar);
 		progressBar.setVisibility(View.VISIBLE);
 		ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_user_avatar);
 		imageView.setVisibility(View.VISIBLE);
		imageView.setLayoutParams(new LinearLayout.LayoutParams(progressBar.getMeasuredHeight(), progressBar.getMeasuredHeight()));
 		Bitmap bitmap = mImageStore.download(participant.getAvatarUrl(), mListener);
 		if (bitmap != null) {
 			imageView.setImageBitmap(bitmap);
 			imageView.setVisibility(View.VISIBLE);
 			progressBar.setVisibility(View.GONE);
 		} else {
 			progressBar.setVisibility(View.VISIBLE);
 			imageView.setVisibility(View.GONE);
 		}
 
 		TextView nameView = (TextView) convertView.findViewById(R.id.list_item_user_name);
 		nameView.setText(participant.getName());
 
 		TextView cityView = (TextView) convertView.findViewById(R.id.list_item_user_city);
 		cityView.setText("");
 		TextView countryView = (TextView) convertView.findViewById(R.id.list_item_user_country);
 		countryView.setText("");
 		
 		for (Attribute attribute : participant.getAttributes()) {
 			if (attribute.getKey().equals(Attribute.KEY_CITY)) {
 				cityView.setText(attribute.getValue());
 			} else if (attribute.getKey().equals(Attribute.KEY_COUNTRY)) {
 				countryView.setText(attribute.getValue());
 			}
 		}
 		
 		return convertView;
 	}
 	
 	private static class ImageStore {
 		Map<String, Bitmap> mURLMap;
 		
 		ImageStore() {
 			mURLMap = new HashMap<String, Bitmap>();
 		}
 		
 		private Bitmap download(String url, AvatarDownloadListener listener) {
 			if (!mURLMap.containsKey(url)) {
 				mURLMap.put(url, null);
 				Handler handler = new AvatarDownloadHandler(mURLMap, url, listener);
 				Thread thread = new AvatarDownloadThread(url, handler);
 				thread.start();
 				
 				//Do nothing and wait for download
 				return null;
 			} else if (mURLMap.get(url) == null) {
 				//Downloading...
 				//Do nothing and wait for download
 				return null;
 			} else {
 				//Downloaded
 				return mURLMap.get(url);
 			}
 		}
 		
 		private static class AvatarDownloadThread extends HandledThread {
 			private final String mURL;
 			
 			AvatarDownloadThread(String url, Handler handler) {
 				setHandler(handler);
 				mURL = url;
 			}
 			
 			@Override
 			public void run() {
 				Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_BACKGROUND);
 				try {
 					URLConnection conn = new URL(mURL).openConnection();
 					Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
 					dispatchMessage(bitmap);
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 					dispatchMessage(e);
 				} catch (IOException e) {
 					e.printStackTrace();
 					dispatchMessage(e);
 				}
 			}
 		}
 		
 		private static class AvatarDownloadHandler extends Handler {
 			private final Map<String, Bitmap> mBitmapMap;
 			private final String mURL;
 			private final AvatarDownloadListener mListener;
 			
 			AvatarDownloadHandler(Map<String, Bitmap> bitmapMap, String url, AvatarDownloadListener listener) {
 				mBitmapMap = bitmapMap;
 				mURL = url;
 				mListener = listener;
 			}
 			
 			@Override
 			public void handleMessage(Message message) {
 				if (message.obj instanceof Bitmap) {
 					mBitmapMap.put(mURL, (Bitmap) message.obj);
 					mListener.onDownloaded((Bitmap) message.obj);
 				} else {
 					mBitmapMap.remove(mURL);
 				}
 			}
 		}
 	}
 
 }
