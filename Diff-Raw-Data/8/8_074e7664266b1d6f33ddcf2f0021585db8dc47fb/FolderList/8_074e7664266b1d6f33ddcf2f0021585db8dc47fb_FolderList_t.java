 package com.example.t_gallery;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.provider.MediaStore.Images.Media;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AbsoluteLayout;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class FolderList extends ListActivity {
 	private ArrayList<GalleryLayout> mGalleryLayouts = new ArrayList<GalleryLayout>();
 	private ArrayList<String> mAlbumName = new ArrayList<String>();
 	private ArrayList<Integer> mAlbumCount = new ArrayList<Integer>();
 	private Config mConfig;
 	private int mAlbumIndexArray[];
 	private CacheAndAsyncWork mCacheAndAsyncWork;
 	
 	private void fetchFolderList(){
 		
 		Cursor galleryList = getContentResolver().query(
 				Config.MEDIA_URI, 
 				Config.ALBUM_PROJECTION, 
 				Config.ALBUM_WHERE_CLAUSE, 
 				null, null);
 		
 		mAlbumIndexArray = new int[galleryList.getCount()];
 		for(int i = 0; i<mAlbumIndexArray.length; i++) {
 			mAlbumIndexArray[i] = -1;
 		}
 		
 		int badFolderNum = 0;
 		
 		for (int i=0; i<galleryList.getCount(); i++){
 
 			galleryList.moveToPosition(i);
 			
 			String album = galleryList.getString(galleryList.getColumnIndex(Media.BUCKET_DISPLAY_NAME));
 			
 			Cursor ImageList = getContentResolver().query(
 					Config.MEDIA_URI, 
 					Config.IMAGE_PROJECTION, 
 					Config.IMAGE_WHERE_CLAUSE, 
 					new String[]{galleryList.getString(galleryList.getColumnIndex(Media.BUCKET_ID))}, 
 					null);
 			
 			GalleryLayout galleryLayout = null;
 			
 			int albumWidth = 0;
 			if(0 == album.compareTo("Camera")) {
 				albumWidth = mConfig.screenWidth - 2* Config.ALBUM_PADDING;
 				galleryLayout = new GalleryLayout(albumWidth, Config.FOLDER_THUMBNAIL_PADDING);
 			}
 			else {
 				albumWidth = (mConfig.screenWidth - 3* Config.ALBUM_PADDING)/2;
 				galleryLayout = new GalleryLayout(albumWidth, Config.FOLDER_THUMBNAIL_PADDING);
 			}
 			
 			ImageList.moveToFirst();
 
 			while (false == ImageList.isAfterLast() && galleryLayout.getLineNum() < 3) {
 				long id = ImageList.getLong(ImageList.getColumnIndex(Media._ID));
 				int width = ImageList.getInt(ImageList.getColumnIndex(Media.WIDTH));
 				int height = ImageList.getInt(ImageList.getColumnIndex(Media.HEIGHT));
 
 				if (width != 0 && height != 0) {
 					galleryLayout.addImage(id, width, height, i);
 				}
 
 				ImageList.moveToNext();
 			}
 			galleryLayout.addImageFinish();
 			
 			if(galleryLayout.getLineNum() == 0) {
 				badFolderNum++;
 			}
 			else {
 				if (0 == album.compareTo("Camera")) {
 					mGalleryLayouts.add(0, galleryLayout);
 					mAlbumName.add(0, album);
 					mAlbumCount.add(0, ImageList.getCount());
 
 					storeAlbumIndex(0, i);
 				} else {
 					mGalleryLayouts.add(galleryLayout);
 					mAlbumName.add(album);
 					mAlbumCount.add(ImageList.getCount());
 
 					storeAlbumIndex(i - badFolderNum, i);
 				}
 			}
 		}
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_folder_list);
         
         mConfig = new Config(this);
         
         fetchFolderList();
         
         setListAdapter(new FolderListAdapter(this));
         
         mCacheAndAsyncWork = new CacheAndAsyncWork();
     }
     
     private void storeAlbumIndex(int index, int value) {
     	int end = 0;
     	
 		for(int i = 0; i<mAlbumIndexArray.length; i++) {
 			if(mAlbumIndexArray[i] == -1) {
 				end = i;
 				break;
 			}
 		}
 		
 		if(end == index) {
 			mAlbumIndexArray[index] = value;
 		}
 		else if(index < end) {
 			for(int i = end -1; i >= index; i--) {
 				mAlbumIndexArray[i+1] = mAlbumIndexArray[i];
 			}
 			mAlbumIndexArray[index] = value;
 		}
 		else {
 			//Have not case, maybe add assert
 		}
     }
     
 	class FolderListAdapter extends BaseAdapter {
 		private Context context;
 		private ViewHolder holder;
 
 		public FolderListAdapter(Context context) {
 			super();
 			this.context = context;
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 			
 			if(mGalleryLayouts.size() == 0) {
 				count = 0;
 			}
 			else if(0 == mAlbumName.get(0).compareTo("Camera")) {
 				count = (mGalleryLayouts.size()/2)+1;
 			}
 			else {
 				count = (mGalleryLayouts.size()+1)/2;
 			}
 			return count;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return position;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LinearLayout line;
 
 			holder = null;
 			if (convertView == null) {
 				line = new LinearLayout(context);
 			} else {
 				line = (LinearLayout) convertView;
 			}
 
 			if (position == 0) {
 				AbsoluteLayout albumLayout = new AbsoluteLayout(context);
 				
 				ImageLineGroup currentLine = mGalleryLayouts.get(0).lines.get(0);
 				
 				int hPad = (mConfig.screenWidth - currentLine.width)/2;
 				line.setPadding(hPad, Config.ALBUM_PADDING, hPad, Config.ALBUM_PADDING / 2);
 				
 				setView(line, albumLayout, convertView, 0, 0, Config.CAMARA_ALBUM_HEIGHT);
 				
 				line.addView(albumLayout);
 			} else {
 				line.setPadding(Config.ALBUM_PADDING, Config.ALBUM_PADDING / 2,
 						Config.ALBUM_PADDING, Config.ALBUM_PADDING / 2);
 					
 				// First album
 				int albumPosition = (position - 1) * 2 + 1;
 				AbsoluteLayout fristAlbum = new AbsoluteLayout(context);
 								
 				setView(line, fristAlbum, convertView, albumPosition, 0, Config.COMMON_ALBUM_HEIGHT);
 				line.addView(fristAlbum);
 
 				int temp = mGalleryLayouts.size();
 				// Second album
 				if (albumPosition < (mGalleryLayouts.size() - 1)) {
 					albumPosition += 1;
 					AbsoluteLayout secondAlbum = new AbsoluteLayout(context);
 
 					setView(line, secondAlbum, convertView, albumPosition, 1, Config.COMMON_ALBUM_HEIGHT);
 					line.addView(secondAlbum);
 				}
 			}
 
 			return line;
 		}
 
 		private void setView(LinearLayout parent, AbsoluteLayout line, View convertView,
 				int albumPosition, int locate, int album_max_height) {
 
 			/* Prepare the view (no content yet) */
 			if (holder == null) {
 				if (convertView == null) {
 					holder = new ViewHolder();
 
 					for (int i = 0; i < Config.THUMBNAILS_PER_LINE * 4; i++) {
 						// Image
 						holder.icons[i] = new ImageView(context);
 						holder.icons[i].setBackgroundColor(0xFF000000);
 						holder.icons[i].setAdjustViewBounds(true);
 						holder.icons[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
 						holder.icons[i].setVisibility(View.GONE);
 						holder.icons[i].setPadding(Config.FOLDER_THUMBNAIL_PADDING,
 								Config.FOLDER_THUMBNAIL_PADDING,
 								Config.FOLDER_THUMBNAIL_PADDING,
 								Config.FOLDER_THUMBNAIL_PADDING);
 
 						line.addView(holder.icons[i]);
 					}
 
 					// Album Description
 					for (int i = 0; i < 2; i++) {
 						holder.albumDesps[i] = new TextView(context);
 						holder.albumDesps[i].setTextColor(Color.WHITE);
 						holder.albumDesps[i].setTextSize(18);
 						holder.albumDesps[i].setVisibility(View.GONE);
 						holder.albumDesps[i].setGravity(Gravity.CENTER);
 						holder.albumDesps[i].setAlpha(0.6f);
 						line.addView(holder.albumDesps[i]);
 					}
 
 					parent.setTag(R.id.data_holder, holder);
 				} else {
 					holder = (ViewHolder) (parent.getTag(R.id.data_holder));
 
 					for (int i = 0; i < Config.THUMBNAILS_PER_LINE * 4; i++) {

						holder.icons[i].setVisibility(View.GONE);

 						if (holder.task[i] != null) {
 							holder.task[i].cancel(true);
 							holder.task[i] = null;
 						}
 					}

					for (int i = 0; i < 2; i++) {
						holder.albumDesps[i].setVisibility(View.GONE);
					}
 				}
 			}
 
 			int shiftLength = locate * (mConfig.screenWidth-Config.ALBUM_PADDING)/2;
 			int shiftIconIndex = 2*locate * Config.THUMBNAILS_PER_LINE;
 			
 			ImageLineGroup firstLine = mGalleryLayouts.get(albumPosition).lines.get(0);
 			
 			// Fill in the content
 			for (int i = 0; i < firstLine.getImageNum(); i++) {
 				ImageCell image = firstLine.getImage(i);
 				
 				if (image.outY < album_max_height) {
 
 					holder.icons[i + shiftIconIndex].setVisibility(View.VISIBLE);
 
 					int height = image.outY + image.outHeight + 2
 							* Config.FOLDER_THUMBNAIL_PADDING;
 
 					if (height <= album_max_height) {
 						holder.icons[i + shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 										image.outWidth + 2*Config.FOLDER_THUMBNAIL_PADDING,
 										image.outHeight + 2*Config.FOLDER_THUMBNAIL_PADDING,
 										image.outX + shiftLength, image.outY));
 						holder.icons[i + shiftIconIndex].setScaleType(ImageView.ScaleType.FIT_XY);
 					} else {
 						holder.icons[i + shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 										image.outWidth + 2*Config.FOLDER_THUMBNAIL_PADDING,
 										album_max_height - image.outY,
 										image.outX + shiftLength,
 										image.outY + Config.FOLDER_THUMBNAIL_PADDING));
 					}
 
 					CacheAndAsyncWork.BitmapWorkerTask task = mCacheAndAsyncWork.new BitmapWorkerTask(
 							holder.icons[i + shiftIconIndex], context.getContentResolver(), null);
 					task.execute(image.id);
 					holder.task[i + shiftIconIndex] = task;
 					holder.icons[i + shiftIconIndex].setImageResource(R.drawable.grey);
 
 					holder.icons[i + shiftIconIndex].setId(mAlbumIndexArray[albumPosition]);
 					holder.icons[i + shiftIconIndex].setOnClickListener(new OnClickListener() {
 								@Override
 								public void onClick(View v) {
 									entryImageList(v);
 								}
 							});
 				}
 			}
 
 			if (firstLine.getHeight() < album_max_height) {
 				
 				shiftIconIndex += Config.THUMBNAILS_PER_LINE;
 				
 				if(mGalleryLayouts.get(albumPosition).getLineNum() > 1) {
 						
 					ImageLineGroup secondLine = mGalleryLayouts.get(albumPosition).lines.get(1);
 					
 					for (int i = 0; i < secondLine.getImageNum(); i++) {
 						ImageCell image = secondLine.getImage(i);
 						int adjustY = image.outY + firstLine.getHeight();
 						
 						if (adjustY < album_max_height) {
 							holder.icons[i + shiftIconIndex].setVisibility(View.VISIBLE);
 
 							int height = adjustY + image.outHeight + 2* Config.FOLDER_THUMBNAIL_PADDING;
 
 							if (height <= album_max_height) {
 								holder.icons[i + shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 												image.outWidth + 2*Config.FOLDER_THUMBNAIL_PADDING,
 												image.outHeight + 2*Config.FOLDER_THUMBNAIL_PADDING,
 												image.outX + shiftLength, adjustY));
 								holder.icons[i + shiftIconIndex].setScaleType(ImageView.ScaleType.FIT_XY);
 							} else {
 								holder.icons[i + shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 												image.outWidth + 2*Config.FOLDER_THUMBNAIL_PADDING,
 												album_max_height - adjustY,
 												image.outX + shiftLength,
 												adjustY + Config.FOLDER_THUMBNAIL_PADDING));
 							}
 
 							CacheAndAsyncWork.BitmapWorkerTask task = mCacheAndAsyncWork.new BitmapWorkerTask(
 									holder.icons[i + shiftIconIndex], context.getContentResolver(), null);
 							task.execute(image.id);
 							holder.task[i + shiftIconIndex] = task;
 							holder.icons[i + shiftIconIndex].setImageResource(R.drawable.grey);
 
 							holder.icons[i + shiftIconIndex].setId(mAlbumIndexArray[albumPosition]);
 							holder.icons[i + shiftIconIndex].setOnClickListener(new OnClickListener() {
 										@Override
 										public void onClick(View v) {
 											entryImageList(v);
 										}
 									});
 						}
 					}
 				}
 				else {
 					holder.icons[shiftIconIndex].setVisibility(View.VISIBLE);
 					holder.icons[shiftIconIndex].setLayoutParams(new AbsoluteLayout.LayoutParams(
 							firstLine.getWidth(),
 							album_max_height - firstLine.getHeight(),
 							shiftLength,
 							firstLine.getHeight() + Config.FOLDER_THUMBNAIL_PADDING));
 					holder.icons[shiftIconIndex].setImageResource(R.drawable.placehold);
 					
 					holder.icons[shiftIconIndex].setId(mAlbumIndexArray[albumPosition]);
 					holder.icons[shiftIconIndex].setOnClickListener(new OnClickListener() {
 								@Override
 								public void onClick(View v) {
 									entryImageList(v);
 								}
 							});
 				}
 			}
 
 			int	despY = album_max_height - Config.ALBUM_DESCRIPTION_HEIGHT + Config.FOLDER_THUMBNAIL_PADDING;
 
 			// Fill Album Description
 			holder.albumDesps[locate].setVisibility(View.VISIBLE);
 			holder.albumDesps[locate].setLayoutParams(new AbsoluteLayout.LayoutParams(
 					firstLine.getWidth() - 2*Config.FOLDER_THUMBNAIL_PADDING,
 					Config.ALBUM_DESCRIPTION_HEIGHT,
 					Config.FOLDER_THUMBNAIL_PADDING + shiftLength,
 					despY));
 			holder.albumDesps[locate].setVisibility(View.VISIBLE);
 			holder.albumDesps[locate].setBackgroundResource(R.drawable.grey);
 			holder.albumDesps[locate].setText(mAlbumName.get(albumPosition)
 					+ " (" + mAlbumCount.get(albumPosition).toString() + ")");
 		}
 
 	    class ViewHolder{
 			ImageView icons[] = new ImageView[Config.THUMBNAILS_PER_LINE*4];
 			CacheAndAsyncWork.BitmapWorkerTask task[] = new CacheAndAsyncWork.BitmapWorkerTask[Config.THUMBNAILS_PER_LINE*4];
 			
 			TextView albumDesps[] = new TextView[2];
 		}
 	}
 	
     public void entryImageList(View view) {
     	
         final Intent i = new Intent(FolderList.this, ImageList.class);
         
 		int clickItemInfo[] = new int[2];
 		view.getLocationOnScreen(clickItemInfo);
 		
 		int contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
 		clickItemInfo[1] -= contentTop;
 		
         i.putExtra(Config.CLICK_ITEM_INFO, clickItemInfo);
         i.putExtra(Config.ALBUM_INDEX, view.getId());
         
         startActivity(i);
     }
 }
