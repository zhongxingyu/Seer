 /*
  * Copyright (C) 2012 Kazuya Yokoyama <kazuya.yokoyama@gmail.com>, Kazumine Matoba <matoyan@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.kazus.android.slidebento.ui.list;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import mobisocial.socialkit.musubi.DbObj;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kazus.android.slidebento.io.AlbumDataManager;
 import com.kazus.android.slidebento.ui.MultiPhotoGallery;
 import com.kazus.android.slidebento.ui.SinglePhotoGallery;
 import com.kazus.android.slidebento.util.BitmapHelper;
 import com.kazus.android.slidebento.util.CorralClient;
 import com.kazus.android.slidebento.util.ImageCache;
 import com.kazus.android.slidebento.R;
 
 public class FeedAlbumListItemAdapter extends ArrayAdapter<FeedAlbumListItem> {
 	private static final String TAG = "AlbumListItemAdapter";
 	private static final int MAX_IMG_WIDTH = 160;
 	private static final int MAX_IMG_HEIGHT = 160;
 	private static final int DUMMY_IMG_WIDTH = 160;
 	private static final int DUMMY_IMG_HEIGHT = 120;
 
 	private AlbumDataManager mManager = AlbumDataManager.getInstance();
 	private LayoutInflater mInflater;
 	private ListView mListView = null;
 	private Context mContext = null;
 
 	public FeedAlbumListItemAdapter(Context context, int resourceId,
 			ListView listView) {
 		super(context, resourceId);
 		mContext = context;
 		mInflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		mListView = listView;
 		Log.d(TAG, "FeedAlbumListItemAdapter mListView=" + mListView);
 	}
 
 	@Override
 	public int getCount() {
 		int count = mManager.getFeedAlbumListCount();
 		return count;
 	}
 
 	@Override
 	public FeedAlbumListItem getItem(int position) {
 		return mManager.getFeedAlbumListItem(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		final ViewHolder holder;
 		if (convertView == null) {
 			// Create view from Layout File
 			convertView = mInflater.inflate(R.layout.feedalbum_list_item, null);
 			holder = new ViewHolder();
 			holder.title = (TextView) convertView.findViewById(R.id.album_title);
 			holder.playing = (TextView) convertView.findViewById(R.id.album_playing);
 			holder.presenter = (TextView) convertView.findViewById(R.id.album_presenter);
 			holder.datetime = (TextView) convertView.findViewById(R.id.album_datetime);
 			holder.totalslides = (TextView) convertView.findViewById(R.id.album_totalslides);
 			holder.imageView = (ImageView) convertView.findViewById(R.id.album_top_image);
 			holder.btnview = (Button) convertView.findViewById(R.id.button_view);
 			holder.btndl = (Button) convertView.findViewById(R.id.button_download);
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		// Fetch item
 		FeedAlbumListItem item = getItem(position);
 		
 		// invisible if setup is not finished
 		if(item == null){
 			holder.title.setVisibility(View.GONE);
 			holder.playing.setVisibility(View.GONE);
 			holder.datetime.setVisibility(View.GONE);
 			holder.totalslides.setVisibility(View.GONE);
 			holder.imageView.setVisibility(View.GONE);
 			holder.btnview.setVisibility(View.GONE);
 			holder.btndl.setVisibility(View.GONE);
 			holder.presenter.setTextColor(convertView.getResources().getColor(R.color.body_text_2));
 			holder.presenter.setText("This deck has not been prepared yet.");
 			return convertView;
 		}else{
 			holder.title.setVisibility(View.VISIBLE);
 			holder.playing.setVisibility(View.VISIBLE);
 			holder.datetime.setVisibility(View.VISIBLE);
 			holder.totalslides.setVisibility(View.VISIBLE);
 			holder.imageView.setVisibility(View.VISIBLE);
 			holder.btnview.setVisibility(View.VISIBLE);
 			holder.btndl.setVisibility(View.VISIBLE);
 		}
 
 		// Set Title
 		holder.title.setText(item.title);
 		holder.title.setTextColor(convertView.getResources().getColor(R.color.body_text_1));
 
 		// Set Playing
 		if(item.playing){
 			holder.playing.setText(R.string.list_playing);
 			holder.playing.setTextColor(convertView.getResources().getColor(
 						R.color.body_playing));
 			holder.title.setTextColor(convertView.getResources().getColor(
 					R.color.body_playing));
 		}else{
 			holder.playing.setText("");
 			holder.title.setTextColor(convertView.getResources().getColor(
 					R.color.body_text_1));
 		}
 		
 		// Set totalslides
 		if(item.totalslides>1){
 			holder.totalslides.setText(item.totalslides+" Slides");
 		}else{
 			holder.totalslides.setText(item.totalslides+" Slide");
 		}
 		
 		// Set presenterName
 		if(item.presenterId == AlbumDataManager.MY_ID){
 			holder.presenter.setText("by you ("+item.presenterName+")");
 			holder.presenter.setTextColor(
 							convertView.getResources().getColor(R.color.accent_2));
 		}else{
 			holder.presenter.setText("by "+item.presenterName);
 			holder.presenter.setTextColor(
 						convertView.getResources().getColor(R.color.body_text_2));
 		}
 
 		// Set Date and Time
 		holder.datetime.setText(item.time);
 		holder.datetime.setTextColor(
 						convertView.getResources().getColor(R.color.body_text_2));
 
 		// Set Image
 		try {
 			holder.imageView.setTag("Feed"+position);
 			Bitmap bitmap = ImageCache.getImage(String.valueOf(position));
 			if (bitmap == null) {
 				holder.imageView.setImageBitmap(
 						BitmapHelper.getDummyBitmap(DUMMY_IMG_WIDTH, DUMMY_IMG_HEIGHT));
 				ImageGetTask task = new ImageGetTask(holder.imageView);
 				task.execute(String.valueOf(position));
 			} else {
 				holder.imageView.setImageBitmap(bitmap);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		// Set Button
 		if(item.presenterId == AlbumDataManager.MY_ID){
 			holder.btnview.setText("Resume Presentation");
 			holder.btndl.setVisibility(View.GONE);
 			
 			holder.btnview.setOnClickListener(new SetResumePresentationListener(position));
 
 		}else{
 			holder.btnview.setText("View");
 			holder.btnview.setOnClickListener(new SetViewDeckListener(position));
 			holder.btndl.setVisibility(View.VISIBLE);
 			holder.btndl.setOnClickListener(new SetDownloadDeckListener(position));
 		}
 
 		return convertView;
 	}
 
 	private final class SetResumePresentationListener implements OnClickListener {
 		private int mPosition;
 		public SetResumePresentationListener(int position){
 			super();
 			mPosition = position;
 		}
 		public void onClick(View v) {
 			DbObj album = mManager.getAlbumAppObj(mPosition);
 			
 			if(album != null){
 				mManager.setAlbumAppObj(album);
 				mManager.updateFromApp(album);
 				if(mManager.isOwner(album)){
 					mManager.setPlaying(true);
 					final CharSequence msg = 
 							mContext.getString(
 									R.string.feed_msg_added, 
 									album.getSender().getName(), 
 									mManager.convertFeedAlbumObj(album.getSubfeed().getLatestObj()).title);
 					StringBuilder html = new StringBuilder(msg);
 					mManager.pushUpdate(html.toString());
 					goMultiGallery();
 				}
 			}
 		}
 	}
 
 	private final class SetViewDeckListener implements OnClickListener {
 		private int mPosition;
 		public SetViewDeckListener(int position){
 			super();
 			mPosition = position;
 		}
 		public void onClick(View v) {
 			DbObj album = mManager.getAlbumAppObj(mPosition);
 			
 			if(album != null){
 				mManager.setAlbumAppObj(album);
 				mManager.updateFromApp(album);
 				goSingleGallery();
 			}
 		}
 	}
 
 	private final class SetDownloadDeckListener implements OnClickListener {
 		private int mPosition;
 		public SetDownloadDeckListener(int position){
 			super();
 			mPosition = position;
 		}
 		public void onClick(View v) {
 			new AlertDialog.Builder(mContext)
 			.setTitle("Import slides into your phone?")
 			.setMessage("Make sure the presenter phone is connected through Wifi.")
 			.setPositiveButton("Yes",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog,
 								int which) {
 							new DownloadDeck().execute(mPosition);
 						}
 					})
 			.setNegativeButton("No", new CancelledDialogListener())
 			.show();			
 		}
 	}
 	private final class CancelledDialogListener implements
 		DialogInterface.OnClickListener {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 		}
 	}
 	
 	class DownloadDeck extends AsyncTask<Integer, Void, Integer> {
 		ProgressDialog progress_;
 		private CorralClient mCorralClient;
 
 		@Override
 		protected void onPreExecute() {
 			progress_ = new ProgressDialog(mContext);
 			progress_.setCancelable(false);
 			progress_.setMessage("Importing Slides...");
 			progress_.show();
 	        mCorralClient = CorralClient.getInstance(mContext);
 		}
 		
 		@Override
 		protected Integer doInBackground(Integer... params) {
 			int position = params[0];
 			DbObj album = mManager.getAlbumAppObj(position);
 			if(album == null){
 				return -1;
 			}
 			mManager.updateFromApp(album);
 
 			Log.e(TAG, "position:"+position);
 			// make deck
 			FeedAlbumListItem item = mManager.getFeedAlbumListItem(position);
 			String title = Environment.getExternalStorageDirectory()+"/SlideBento/"+item.title;
 			File dir = new File(title);
 			int i = 1;
 			while (dir.exists()){
 				dir = new File(title+"_"+i);
 				i++;
 			}
 			dir.mkdirs();
 			
 			// add slides
 			boolean failed = false;
 			int low = 0;
         	for (i=0; i<mManager.getItemCount(); i++) {
         		DbObj obj = mManager.getObjByKey(i, album);
 
                 Uri fileUri = null;
                 if(mManager.getWanIpAddress()!=null){
                 	try {
 						fileUri = mCorralClient.fetchContent(obj, mManager.getMusubi(), mManager.getWanIpAddress());
 					} catch (IOException e) {
 						fileUri = null;
 					}
                 }
             	if (fileUri == null) {
                 	try {
 						fileUri = mCorralClient.fetchContent(obj, mManager.getMusubi(), mManager.getLanIpAddress());
 					} catch (IOException e) {
 						fileUri = null;
 					}
             	}
             	Log.e(TAG, "FileUri="+String.valueOf(fileUri));
                 String fname = "slide"+(i+1);
                 if(fileUri == null || copySlideFile(fileUri, dir, fname+".jpg") == false){
                 	if(copySlideFile(obj.getRaw(), dir, fname+"_low.jpg")){
                 		low++;
                 	}else{
                 		failed = true;
                 	}
                 }
         	}
         	if(failed){
         		return -1;
         	}else{
         		return low;
         	}
 		}
 		@Override
 		protected void onPostExecute(Integer result) {
 			progress_.dismiss();
 			String msg;
 			int len = Toast.LENGTH_LONG;
 			if(result < 0){
 				msg = "ERROR: Import failed.";
 				Log.e(TAG, msg);
 				}else if(result > 0){
 					if(result == 1){
						msg = "Import finished.\nBut, "+result+" slide is imported in LOW resolution.";
 					}else{
						msg = "Import finished.\nBut, "+result+" slides are imported in LOW resolution.";
 					}
 				Log.d(TAG, msg);
 			}else{
 				msg = "Import finished successfully.";
 				Log.d(TAG, msg);
 				len = Toast.LENGTH_SHORT;
 			}
 			Toast.makeText(mContext, msg, len).show();
 		}	
 	}
 	
 	public static boolean copySlideFile(Uri srcUri, File dir, String fname){
 		InputStream is;
 		try {
 			is = new FileInputStream(srcUri.getPath());
 			OutputStream os = new FileOutputStream(dir.getAbsolutePath()+"/"+fname);
 			byte buf[]=new byte[1024];
 		    int len;
 		    while((len=is.read(buf))>0)
 		    os.write(buf,0,len);
 		    os.close();
 		    is.close();
 	    
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "File copy failed.");
 			return false;
 		} catch (IOException e) {
 			Log.e(TAG, "File copy failed.");
 			return false;
 		}
 		return true;
 	}
 	public static boolean copySlideFile(byte[] bytedat, File dir, String fname){
 		if(bytedat == null){
 			Log.e(TAG, "Failed to load raw data.");
 			return false;
 		}
 		try {
 			OutputStream os = new FileOutputStream(dir.getAbsolutePath()+"/"+fname);
 		    os.write(bytedat,0,bytedat.length);
 		    os.close();
 		} catch (IOException e) {
 			Log.e(TAG, "File copy failed.");
 			return false;
 		}
 		return true;
 	}
 	
 	private void goMultiGallery(){
 		mManager.openPort(mContext);
 		Intent intent = new Intent(mContext, MultiPhotoGallery.class);
 		mContext.startActivity(intent);
 	}
 
 	private void goSingleGallery(){
 		Intent intent = new Intent(mContext, SinglePhotoGallery.class);
 		mContext.startActivity(intent);
 	}
 
 	static class ViewHolder {
 		TextView playing;
 		TextView title;
 		TextView presenter;
 		TextView datetime;
 		TextView totalslides;
 		ImageView imageView;
 		Button btnview;
 		Button btndl;
 	}
 
 	class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
 		private ImageView image;
 		private String tag;
 
 		public ImageGetTask(ImageView imageView) {
 			image = imageView;
 			tag = image.getTag().toString();
 		}
 
 		@Override
 		protected Bitmap doInBackground(String... params) {
 			synchronized (mContext) {
 				Log.d(TAG, "bgexec: position="+params[0]);
 				try {
 					Bitmap bitmap = mManager.getTitleBitmap(mManager.getAlbumAppObj(Integer.valueOf(params[0])));
 //							getFeedThumbBitmap(Integer.parseInt(params[0]),
 //							MAX_IMG_WIDTH, MAX_IMG_HEIGHT, 0);
 					ImageCache.setImage(params[0], bitmap);
 					return bitmap;
 				} catch (Exception e) {
 					return null;
 				}
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap result) {
 //			Log.d(TAG, "tags: "+tag+", "+image.getTag()+";");
 			if (tag.equals(image.getTag())) {
 				if (result != null) {
 					image.setImageBitmap(result);
 				} else {
 					image.setImageBitmap(BitmapHelper.getDummyBitmap(
 							DUMMY_IMG_WIDTH, DUMMY_IMG_HEIGHT));
 				}
 				image.setVisibility(View.VISIBLE);
 			}else{
 				Log.d(TAG, "tag maching failed???");
 			}
 		}
 	}
 }
