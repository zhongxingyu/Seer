 /*******************************************************************************
  * Copyright (c) 2012 rmateus.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package cm.aptoide.pt2.adapters;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 import cm.aptoide.pt2.Database;
 import cm.aptoide.pt2.R;
 import cm.aptoide.pt2.contentloaders.ImageLoader;
 import cm.aptoide.pt2.services.AIDLServiceDownloadManager;
 import cm.aptoide.pt2.services.ServiceDownloadManager;
 import cm.aptoide.pt2.views.ViewApk;
 import cm.aptoide.pt2.views.ViewCache;
 import cm.aptoide.pt2.views.ViewDownloadManagement;
 
 public class UpdatesAdapter extends CursorAdapter {
 
 	private AIDLServiceDownloadManager serviceDownloadManager = null;
 
 	private ServiceConnection serviceManagerConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the object we can use to
 			// interact with the service.  We are communicating with the
 			// service using AIDL, so here we set the remote service interface.
 			serviceDownloadManager = AIDLServiceDownloadManager.Stub.asInterface(service);
 
 			Log.v("Aptoide-UpdatesAdapter", "Connected to ServiceDownloadManager");
 
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected -- that is, its process crashed.
 			serviceDownloadManager = null;
 
 			Log.v("Aptoide-UpdatesAdapter", "Disconnected from ServiceDownloadManager");
 		}
 	};
 
 	
 	private ImageLoader loader;
 
 	public UpdatesAdapter(Context context, Cursor c, int flags,Database db) {
 		super(context, c, flags);
 		loader = ImageLoader.getInstance(context);
 		context.bindService(new Intent(context, ServiceDownloadManager.class), serviceManagerConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		
 		View v = LayoutInflater.from(context).inflate(R.layout.app_update_row, null);
 		
 		return v;
 	}
 
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		ViewHolder holder = (ViewHolder) view.getTag();
         if (holder == null) {
             holder = new ViewHolder();
             holder.name = (TextView) view.findViewById(R.id.app_name);
             holder.icon= (ImageView) view.findViewById(R.id.app_icon);
             holder.vername= (TextView) view.findViewById(R.id.uptodate_versionname);
             holder.update = (ImageView) view.findViewById(R.id.app_update);
 //            holder.downloads= (TextView) view.findViewById(R.id.downloads);
 //            holder.rating= (RatingBar) view.findViewById(R.id.stars);
             view.setTag(holder);
         }
         
        final String name = cursor.getString(1);
 		final String apkId = cursor.getString(7);
 		final String vername = cursor.getString(2);
 		final int vercode = cursor.getInt(8);
 		final String md5 = cursor.getString(10);
 		final String apkpath = cursor.getString(11) + cursor.getString(12);  
 		String iconspath = cursor.getString(9)+cursor.getString(4);
 		final String hash = (cursor.getString(cursor.getColumnIndex("apkid"))+"|"+cursor.getString(cursor.getColumnIndex("vercode")));
		holder.name.setText(name);
 		loader.DisplayImage(iconspath, holder.icon, context,hash);
 //		 try{
 //	        	holder.rating.setRating(Float.parseFloat(cursor.getString(5)));	
 //	        }catch (Exception e) {
 //	        	holder.rating.setRating(0);
 //			}
 //		 holder.downloads.setText(cursor.getString(6));
		holder.vername.setText(context.getString(R.string.update_to)+": "+vername);
 		holder.update.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				ViewApk apk = new ViewApk();
 				apk.setApkid(apkId);
				apk.setName(name);
 				apk.setVercode(vercode);
 				apk.setVername(vername);
 				apk.setMd5(md5);
 				try {
 					serviceDownloadManager.callStartDownload(
 							new ViewDownloadManagement(
 							apkpath,
 							apk,
 							new ViewCache(apk.hashCode(), 
 									apk.getMd5())));
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	private static class ViewHolder {
 		TextView name;
 		TextView vername;
 		ImageView icon;
 		ImageView update;
 //		RatingBar rating;
 //		TextView downloads;
 	}
 
 }
