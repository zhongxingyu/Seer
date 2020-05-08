 package ru.ith.android.flocal.io;
 
 import android.R;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.text.Html;
 import android.widget.ImageView;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.lang.ref.WeakReference;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import ru.ith.android.flocal.engine.SessionContainer;
 import ru.ith.lib.flocal.FLDataLoader;
 import ru.ith.lib.flocal.data.AvatarMetaData;
 
 /**
  * Created by adminfthi on 28.06.13.
  */
 public class ImageFactory implements Html.ImageGetter {
 	private final Activity context;
 	final SQLiteDatabase DB;
 
 	public ImageFactory(Activity context) {
 		this.context = context;
 		DB = new AvatarCacheDB(context).getWritableDatabase();
 	}
 
 	@Override
 	public Drawable getDrawable(String source) {
 		Drawable result = context.getResources().getDrawable(android.R.drawable.ic_delete);
 		result.setBounds(0, 0, result.getIntrinsicWidth(), result
 				.getIntrinsicHeight());
 		return result;
 	}
 
	private Map<String, WeakReference<Drawable>> avatarCache = new HashMap<String, WeakReference<Drawable>>();
 	private Map<String, List<ImageView>> waiters = new TreeMap<String, List<ImageView>>();
 
 	public synchronized void getAvatar(String user, ImageView target) {
 		if (avatarCache.containsKey(user)){
			WeakReference<Drawable> ref = avatarCache.get(user);
 			if (ref==null){
 				target.setImageDrawable(null);
 				return;
 			} else {
 				Drawable avatar = ref.get();
 				if (avatar!=null){
 					target.setImageDrawable(avatar);
 					return;
 				}
 			}
 		}
 		Drawable loading = context.getResources().getDrawable(R.drawable.spinner_background);
 		target.setImageDrawable(loading);
 		List<ImageView> avatarWaiters = waiters.get(user);
 		if (avatarWaiters==null){
 			avatarWaiters = new LinkedList<ImageView>();
 			waiters.put(user, avatarWaiters);
 			new avatarLoaderTask(user, this).execute();
 		}
 		avatarWaiters.add(target);
 	}
 
	public synchronized void avatarLoaded(String user, final Drawable drawable) {
		avatarCache.put(user, new WeakReference<Drawable>(drawable));
 		final List<ImageView> victims = waiters.get(user);
 		if (victims==null)
 			return;
		if (drawable!=null)
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
 				.getIntrinsicHeight());
 		for (final ImageView view:victims){
 			context.runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
					view.setImageDrawable(drawable);
 				}
 			});
 		}
 	}
 
 	public Drawable loadFromCache(String cachedFileName) {
 		File cachedFile = new File(context.getCacheDir(), cachedFileName);
 		Drawable result = null;
 		if (cachedFile.isFile()){
 			result = Drawable.createFromPath(cachedFile.getPath());
 		}
 		//TODO: some marker that avatar failed to load
 		return result;
 	}
 
 	public String saveToCache(InputStream newAvatarStream) throws IOException {
 		if (newAvatarStream==null)
 			return null;
 		String fileName = UUID.randomUUID().toString();
 		File result = new File(context.getCacheDir(), fileName);
 		FileOutputStream cacheStream = new FileOutputStream(result);
 		byte[]buf = new byte[2048];
 		int len;
 		while ((len = newAvatarStream.read(buf))>=0){
 			cacheStream.write(buf, 0, len);
 		}
 		cacheStream.close();
 		return fileName;
 	}
 }
 
 class avatarLoaderTask extends AsyncTask<String, Void, Drawable> {
 
 	private String mUser;
 	private ImageFactory mImageFactory;
 
 	public avatarLoaderTask(String user, ImageFactory imageFactory) {
 		mUser = user;
 		mImageFactory = imageFactory;
 	}
 
 	@Override
 	protected Drawable doInBackground(String... params) {
 		boolean useCachedVersion = false;
 		AvatarMetaData meta = null;
 		String whereClause =  AvatarCacheDB.ROW_USER + "= ?";
 		String[] whereValues = new String[]{mUser};
 		try {
 			//first: check out cache
 			Cursor c = null;
 			try {
 				c = mImageFactory.DB.query(AvatarCacheDB.AVATAR_TABLE, new String[]{AvatarCacheDB.ROW_CACHED_FILE, AvatarCacheDB.ROW_LAST_CHECKED},whereClause, whereValues, null, null, null);
 				if (c.isBeforeFirst()) {
 					c.moveToNext();
 					String cachedFileName = c.getString(1);
 					Long cachedAt = c.getLong(2);
 					if (cachedAt >= System.currentTimeMillis() - 2 * 7 * 86400 * 1000) {
 						useCachedVersion = true;
 					} else {
 						meta = FLDataLoader.getAvatarMetadata(SessionContainer.getSessionInstance(), mUser, false);
 						if (meta.URL==null){
 							if (cachedFileName==null)
 								useCachedVersion = true;
 						} else if (meta.lastModified<cachedAt){
 							useCachedVersion = true;
 						}
 					}
 					if (useCachedVersion) {
 						if (cachedFileName == null)
 							return null;
 						Drawable result = mImageFactory.loadFromCache(cachedFileName);
 						if (result != null){
 							ContentValues updatedDate=new ContentValues();
 							updatedDate.put(AvatarCacheDB.ROW_LAST_CHECKED, System.currentTimeMillis());
 							mImageFactory.DB.update(AvatarCacheDB.AVATAR_TABLE, updatedDate, whereClause, whereValues);
 							return result;
 						}
 					}
 					c.close();
 					c = null;
 					mImageFactory.DB.delete(AvatarCacheDB.AVATAR_TABLE, whereClause, whereValues);
 				}
 			} finally {
 				if (c!=null)
 					c.close();
 			}
 
 			if (meta==null)
 				meta = FLDataLoader.getAvatarMetadata(SessionContainer.getSessionInstance(), mUser, true);
 
 			String cacheID = null;
 			if (meta.URL!=null){
 				InputStream newAvatarStream = null;
 				newAvatarStream = FLDataLoader.fetchAvatar(meta);
 				cacheID = mImageFactory.saveToCache(newAvatarStream);
 			}
 
 			if (cacheID==null)
 				return null;
 
 			return mImageFactory.loadFromCache(cacheID);
 		} catch (Exception e1) {
 			return null;
 		}
 	}
 
 	@Override
 	protected void onPostExecute(Drawable drawable) {
 		mImageFactory.avatarLoaded(mUser, drawable);
 	}
 }
