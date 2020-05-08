 package com.example.t_gallery;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore.Images.Media;
 import android.provider.MediaStore.Images.Thumbnails;
 import android.animation.ObjectAnimator;
 import android.app.ExpandableListActivity;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.LruCache;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.ViewTreeObserver.OnDrawListener;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 
 
 public class GalleryList extends ExpandableListActivity 
 	implements OnScrollListener{
 	
 	
 	static public class Config{
 		/*Application configuration*/
 		static public final int MAX_ALBUM = 1000;
 		static public final int MAX_PICTURE_IN_ALBUM = 10000;
 		
 		static public final int THUMBNAILS_PER_LINE = 4;
 		static public final int THUMBNAIL_WIDTH = 270;
 		static public final int THUMBNAIL_HEIGHT = 270;
 		
 		/*RAM cache*/
 		static public final int RAM_CACHE_SIZE_KB = (int)(Runtime.getRuntime().maxMemory()/4096);
 		
 		/*Content query*/
 		static public final Uri MEDIA_URI = Media.EXTERNAL_CONTENT_URI;
 		static public final String ALBUM_PROJECTION[] = {Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME};
 		static public final String ALBUM_WHERE_CLAUSE = "1) GROUP BY (1"; /*This is a trick to use GROUP BY */
 		static public final String IMAGE_PROJECTION[] = {Media._ID};
 		static public final String IMAGE_WHERE_CLAUSE = Media.BUCKET_ID + " = ?";
 		
 		/*UI Effect*/
 		static public final int LIST_ANIM_DURATION = 500; /*count in ms*/
 	}
 	
 	private void fetchGalleryList(){
 		
 		mGalleryList = getContentResolver().query(
 				Config.MEDIA_URI, 
 				Config.ALBUM_PROJECTION, 
 				Config.ALBUM_WHERE_CLAUSE, 
 				null, null);
 	
 		mImageLists = new Cursor[mGalleryList.getCount()];
 		
 		for (int i=0; i<mGalleryList.getCount(); i++){
 			mGalleryList.moveToPosition(i);
 			String galleryId = mGalleryList.getString(mGalleryList.getColumnIndex(Media.BUCKET_ID));
 			mImageLists[i] = getContentResolver().query(
 					Config.MEDIA_URI, 
 					Config.IMAGE_PROJECTION, 
 					Config.IMAGE_WHERE_CLAUSE, 
 					new String[]{mGalleryList.getString(mGalleryList.getColumnIndex(Media.BUCKET_ID))}, 
 					null);
 		}
 	}
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_gallery_list);
 		
 		fetchGalleryList();						
 		
 		mRamCache = new LruCache<Long, Bitmap>(Config.RAM_CACHE_SIZE_KB){
 			protected int sizeOf(Long key, Bitmap bmp){
 				return (bmp.getByteCount()/1024);
 			}
 		};
 	
 		setListAdapter(new GalleryListAdapter());
 		//getExpandableListView().setOnScrollListener(this);
 	}
 	
 	private TextView shortcut;
 	
 	class Position {
 		Position(long id, int top, int bottom, View view){
 			mId = id;
 			mTop = top;
 			mBottom = bottom;
 			mView = view;
 		}
 		public long mId;
 		public int mTop;
 		public int mBottom;
 		public View mView;
 	}
 	
 	private ArrayList<Position> mPositionArray = new ArrayList<Position>();
 	
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 		shortcut = (TextView)findViewById(R.id.collapse_shortcut);
 		shortcut.setVisibility(View.INVISIBLE);
 		
 				
 		/*For expand animation*/
 		getExpandableListView().setOnGroupClickListener(new ExpandableListView.OnGroupClickListener(){
 
 			@Override
 			public boolean onGroupClick(ExpandableListView parent, View view,
 					int groupPosition, long id) {
 				
 				final boolean isCollapse = parent.isGroupExpanded(groupPosition);
 				
 				if (isCollapse){
 					return false; /*Only work for expand now*/
 				}
 				
 				
 				for (int i=0; i<parent.getChildCount(); i++){
 					long itemId = parent.getItemIdAtPosition(parent.getFirstVisiblePosition() + i);
 					View listItem = parent.getChildAt(i);
 					listItem.setTag(R.id.recyclable, false);
 					mPositionArray.add(new Position(itemId, listItem.getTop(), listItem.getBottom(), listItem));
 				}
 
 				final ViewTreeObserver observer = parent.getViewTreeObserver();
 				final ExpandableListView listView = parent;
 				
 				observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener (){
 
 					public ArrayList<View> mInvisibleItems = new ArrayList<View>();
 					
 					@Override
 					public boolean onPreDraw() {
 						observer.removeOnPreDrawListener(this);
 						
 						int firstMatchingItem = -1;
 						int lastMatchingItem = -1;
 						int lastMatchingItemInNewList = -1;
 						int upDist = 0; 
 						int downDist = 0;
 						
						listView.setWillNotDraw(true);
 						for (int i=0; i<listView.getChildCount(); i++){
 							View listItem = listView.getChildAt(i);
 							long itemId = listView.getItemIdAtPosition(listView.getFirstVisiblePosition()+i);
 							
 							for (int j=0; j<mPositionArray.size(); j++){
 								Position oldPos = mPositionArray.get(j);
 								
 								if (itemId == oldPos.mId){
 									
 									if (firstMatchingItem == -1){
 										firstMatchingItem = j;
 									}
 									lastMatchingItem = j;
 									lastMatchingItemInNewList = i;
 									
 									if (i==0){
 										/*The first item is found in old list, then we could anchor anim*/
 										upDist = listItem.getTop() - oldPos.mTop;
 									}
 									
 									if (i==listView.getChildCount()-1){
 										/*The last item is found in the old list, then we could anchor anim*/
 										downDist = listItem.getBottom() - oldPos.mBottom;
 									}
 									
 									listItem.setTranslationY(oldPos.mTop - listItem.getTop());
 									listItem.animate().translationY(0).setDuration(500);
 									
 									break;
 								}
 							}
 						}
 						
 						if (lastMatchingItemInNewList!=-1 && lastMatchingItemInNewList<listView.getChildCount()-1){
 							for (int i=lastMatchingItemInNewList+1; i<listView.getChildCount(); i++){
 								listView.getChildAt(i).setVisibility(View.INVISIBLE);
 								mInvisibleItems.add(listView.getChildAt(i));
 							}
 						}
 						
 						
 						if (upDist == 0) {
 							Position item = mPositionArray.get(firstMatchingItem);
 							upDist = 0 - item.mBottom; 
 						}
 						
 						if (downDist == 0) {
 							Position item = mPositionArray.get(lastMatchingItem);
 							downDist = listView.getBottom() - item.mTop;
 						}
					
 						ViewGroup pparent = (ViewGroup)listView.getParent();
 						
 						class ViewDetacher implements Runnable{
 							ViewGroup mParent;
 							View mChild;
 							
 							public ViewDetacher(ViewGroup parent, View child){
 								mParent = parent;
 								mChild = child;
 							}
 							
 							public void run(){
 								mParent.removeView(mChild);
 							}
 						}
 						
 						for (int i=0; i<mPositionArray.size(); i++){
 							Position pos = mPositionArray.get(i);
 							ImageView view = new ImageView(getApplicationContext()); 
 							pos.mView.buildDrawingCache();
 							Bitmap bmpCache = pos.mView.getDrawingCache();
 							view.setImageBitmap(bmpCache);
 							
 							view.setTag(R.id.recyclable, true);
 							
 							if (i<firstMatchingItem){
 								pparent.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
 								view.setTranslationY(pos.mTop);
 								view.animate().translationY(pos.mTop + upDist).setDuration(500).withEndAction(new ViewDetacher(pparent, view));
 							}
 							else if (i>lastMatchingItem){
 								pparent.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
 								view.setTranslationY(pos.mTop);
 								view.animate().translationY(pos.mTop + downDist).setDuration(500).withEndAction(new ViewDetacher(pparent, view));				
 							}
 							
 							final ViewTreeObserver parentObserver =  pparent.getViewTreeObserver();
 							parentObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
 								
 								@Override
 								public boolean onPreDraw() {
 									parentObserver.removeOnPreDrawListener(this);
 									
 									while (mInvisibleItems.size() > 0){
 										mInvisibleItems.get(0).setVisibility(View.VISIBLE);
 										mInvisibleItems.remove(0);
 									}
 									return false;
 								}
 							});
 						}
 						
 						int oldLastBottom = mPositionArray.get(mPositionArray.size()-1).mBottom;
 						
 						if (oldLastBottom < listView.getBottom()){
 							
 							shortcut.setText(" ");
 							shortcut.setTranslationY(oldLastBottom);
 							shortcut.setVisibility(View.VISIBLE);
 							shortcut.animate().translationY(downDist+oldLastBottom).setDuration(500).withEndAction(new Runnable(){
 
 								@Override
 								public void run() {
 									shortcut.setVisibility(View.INVISIBLE);									
 								}
 								
 							});
 						}
 
 						mPositionArray.clear();
						return false; /*Bypass the drawing !!! In case we re-layout the container*/
 					}
 
 					
 				});
 				return false;
 			}
 			
 		});
 		
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.activity_gallery_list, menu);
 		return true;
 	}
 		
 	private synchronized Bitmap getBitmapFromRamCace(long key){
 		return mRamCache.get(key);	
 	}
 	
 	private synchronized void addBitmapToRamCache(long key, Bitmap bmp){
 		if (null == getBitmapFromRamCace(key)){
 			mRamCache.put(key, bmp);
 		}
 	}
 	
 	private Cursor mGalleryList;
 	private Cursor mImageLists[];
 	private LruCache<Long, Bitmap> mRamCache;
 	
 	private ObjectAnimator ongoingAnim;
 	private boolean shortcutDisp = true;
 	
 	private void ShowShortcut(){
 		
 		if (shortcutDisp){
 			return;
 		}
 		
 		int height = shortcut.getHeight();
 		ongoingAnim.cancel();
 		
 		ObjectAnimator anim = ObjectAnimator.ofFloat(shortcut, "Y", 0).setDuration(500);
 		anim.start();
 		ongoingAnim = anim;
 		shortcutDisp = true;
 		
 		
 	}
 	
 	private void HideShortcut(boolean immediate){
 		
 		if(!shortcutDisp){
 			return;
 		}
 		
 		int height = shortcut.getHeight();
 		
 		if (ongoingAnim != null)
 		ongoingAnim.cancel();
 		
 		if (immediate){
 			shortcut.setY(0 - height);
 		}
 		else{
 			ObjectAnimator anim = ObjectAnimator.ofFloat(shortcut, "Y", 0-height).setDuration(500);
 			anim.start();
 			ongoingAnim = anim;
 		}
 		shortcutDisp = false;
 	}
 	
 	private int scrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
 
 	private int prevFirstItem = -1;
 	private int prevVisibleItemCount = -1;
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 
 		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
 			if (prevFirstItem == -1){
 				prevFirstItem = firstVisibleItem;
 				prevVisibleItemCount = visibleItemCount;
 			}
 			else {
 				if (prevFirstItem >  firstVisibleItem){
 					ShowShortcut();
 				}
 				else if (prevFirstItem < firstVisibleItem){
 					HideShortcut(false);
 				}
 			}
 		}
 		else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING){
 			prevFirstItem = -1;
 			ExpandableListView list = getExpandableListView();
 			long index = list.getExpandableListPosition(firstVisibleItem);
 			int groupIndex = list.getPackedPositionGroup(index);
 			int childIndex = list.getPackedPositionChild(index);
 			
 			if (childIndex == -1){
 				HideShortcut(false);
 			}
 		}
 	}
 
 	private Timer timerToHide;
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		
 		if (this.scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING 
 				&& scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
 			if (shortcutDisp){
 				timerToHide = new Timer();
 				timerToHide.schedule(new TimerTask(){
 					
 					Handler msgHandler = new Handler(){
 						public void handleMessage(Message msg){
 							if (shortcutDisp){
 								HideShortcut(false);
 							}
 						}
 					};
 
 					@Override
 					public void run() {
 						msgHandler.obtainMessage().sendToTarget();
 					}
 					
 				}, 1500);
 			}
 		}
 		
 		this.scrollState = scrollState;
 	}
 
 	
 	class GalleryListAdapter extends BaseExpandableListAdapter{
 		
 		class BitmapWorkerTask extends AsyncTask<Long, Void, Bitmap>{
 
 			private final WeakReference<ImageView> iconReference;
 			private long id;
 			
 			public BitmapWorkerTask(ImageView icon){
 				iconReference = new WeakReference<ImageView>(icon);
 			}
 			
 			@Override
 			protected  Bitmap doInBackground(Long... params) {
 			    BitmapFactory.Options options = new BitmapFactory.Options();
 			    
 			    id = params[0];
 				
 				Bitmap thumb = Thumbnails.getThumbnail(getContentResolver(), id, Thumbnails.MINI_KIND, options);
 				int height = thumb.getHeight();
 				int width = thumb.getWidth();
 				
 				if (height > width){
 					thumb = Bitmap.createBitmap(thumb, 0, (height-width)/2, width, width);
 				}
 				else{
 					thumb = Bitmap.createBitmap(thumb, (width-height)/2, 0, height, height);
 				}
 				
 		        return Bitmap.createScaledBitmap(thumb,Config.THUMBNAIL_WIDTH,Config.THUMBNAIL_HEIGHT,false);
 		
 			}
 			
 			public void onPostExecute(Bitmap bitmap){
 				
 				if (iconReference != null && bitmap != null){
 					final ImageView iconView = iconReference.get();
 					
 					if (iconView != null){
 						addBitmapToRamCache(id, bitmap);
 						iconView.setImageBitmap(bitmap);
 					}
 				}
 			}
 			
 		}
 		
 	    class ViewHolder{
 			ImageView icons[] = new ImageView[Config.THUMBNAILS_PER_LINE];
 			BitmapWorkerTask task[] = new BitmapWorkerTask[Config.THUMBNAILS_PER_LINE];
 			boolean inTransient = false;
 		}
 	    
 	    @Override
 	    public int getChildTypeCount(){
 	    	return 1;
 	    }
 	    
 	    @Override
 	    public int getChildType(int groupPosition, int childPosition){
 	    	return 0;
 	    }
 	    
 	    @Override
 	    public int getGroupTypeCount(){
 	    	return 1;
 	    }
 	    
 	    @Override
 	    public int getGroupType(int groupPosition){
 	    	return 0;
 	    }
 	    
 		@Override
 		public Object getChild(int groupPosition, int childPosition) {
 			// TODO Auto-generated method stub
 			return groupPosition*Config.MAX_PICTURE_IN_ALBUM + childPosition;
 		}
 
 		@Override
 		public long getChildId(int groupPosition, int childPosition) {
 			// TODO Auto-generated method stub
 			return groupPosition*Config.MAX_PICTURE_IN_ALBUM + childPosition;
 		}
 
 		@Override
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			LinearLayout line;			
 			ViewHolder holder;
 			
 			/*Prepare the view (no content yet)*/
 			if (convertView == null || !getRecyclable(convertView)){
 				line = new LinearLayout(getApplicationContext());
 				line.setOrientation(LinearLayout.HORIZONTAL);
 				
 	            holder = new ViewHolder();
 	            
 	            for (int i=0; i<Config.THUMBNAILS_PER_LINE; i++){
 	            	holder.icons[i] = new ImageView(getApplicationContext());
 	            	holder.icons[i].setAdjustViewBounds(true);
 					holder.icons[i].setScaleType(ImageView.ScaleType.CENTER_CROP);	
 	    			line.addView(holder.icons[i], new LinearLayout.LayoutParams(Config.THUMBNAIL_WIDTH,Config.THUMBNAIL_HEIGHT));
 	            }
 				
 				line.setTag(R.id.data_holder, holder);
 				line.setTag(R.id.recyclable, true);
 			}
 			else {
 				line = (LinearLayout)convertView;
 				holder = (ViewHolder)(line.getTag(R.id.data_holder)); 
 				
 				for (int i=0; i<Config.THUMBNAILS_PER_LINE; i++){
 					if (holder.task[i] != null){
 						holder.task[i].cancel(true);
 						holder.task[i] = null;
 					}
 				}
 			}
 			
 			/*Fill in the content*/
 	        for (int i=0; i<Config.THUMBNAILS_PER_LINE; i++){
 	        	
 	        	Cursor imageList = mImageLists[groupPosition];
 	        	
 	        	if (imageList.getCount() <= (childPosition*Config.THUMBNAILS_PER_LINE+i)){
 	        		holder.icons[i].setImageResource(R.drawable.black);
 	        	}
 	        	else {
 	        		imageList.moveToPosition(childPosition*Config.THUMBNAILS_PER_LINE+i);
 	        		long id = imageList.getLong (imageList.getColumnIndex(Media._ID));
 	        		Bitmap bmp = getBitmapFromRamCace(id);
 	        		
 	        		if (bmp != null){
 	        			holder.icons[i].setImageBitmap(bmp);
 	        		}
 	        		else {
 	        			BitmapWorkerTask task = new BitmapWorkerTask(holder.icons[i]);
 					    task.execute(id);
 					    holder.task[i] = task;
 					    holder.icons[i].setImageResource(R.drawable.empty_photo);
 	        		}
 	        	}	
 			}
 
 			return line;
 		}
 
 		@Override
 		public int getChildrenCount(int groupPosition) {
 			return (mImageLists[groupPosition].getCount() + Config.THUMBNAILS_PER_LINE - 1)/Config.THUMBNAILS_PER_LINE;
 		}
 
 		@Override
 		public Object getGroup(int groupPosition) {
 			return groupPosition;
 		}
 
 		@Override
 		public int getGroupCount() {
 			return mImageLists.length;
 		}
 
 		@Override
 		public long getGroupId(int groupPosition) {
 			return groupPosition;
 		}
 
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			
 			LinearLayout item;
 			
 			if (convertView == null || !getRecyclable(convertView)){
 				LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
 					      (Context.LAYOUT_INFLATER_SERVICE);
 				item = (LinearLayout)inflater.inflate(R.layout.gallery_item, null);
 				item.setTag(R.id.recyclable, true);
 			}
 			else {
 				item = (LinearLayout)convertView;
 			}
 			
 			mGalleryList.moveToPosition(groupPosition);
 			int count = mImageLists[groupPosition].getCount();
 			TextView text = (TextView)item.getChildAt(0);
 			text.setText(mGalleryList.getString(mGalleryList.getColumnIndex(Media.BUCKET_DISPLAY_NAME))+" ("+count+")");
 			
 			return item;
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			return true;
 		}
 
 		@Override
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			return false;
 		}
 		
 		public void setRecyclable(boolean flag, View item){
 			item.setTag(R.id.recyclable,flag);
 		}
 		
 		public boolean getRecyclable(View item){
 			return (Boolean)item.getTag(R.id.recyclable);
 		}
 	}	 
 }
