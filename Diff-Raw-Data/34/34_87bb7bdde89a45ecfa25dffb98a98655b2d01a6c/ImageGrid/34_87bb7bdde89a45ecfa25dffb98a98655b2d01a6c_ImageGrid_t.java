 /*
  * Copyright (C) 2012 The Android Open Source Project
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
 
 package com.iped.ipcam.bitmapfun;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewTreeObserver;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.iped.ipcam.bitmapfun.ImageCache.ImageCacheParams;
 import com.iped.ipcam.gui.BuildConfig;
 import com.iped.ipcam.gui.R;
 import com.iped.ipcam.pojo.ImageInfo;
 import com.iped.ipcam.utils.FileUtil;
 
 /**
  * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
  */
 public class ImageGrid extends FragmentActivity implements OnItemClickListener {
 
 	private static final String IMAGE_CACHE_DIR = "thumbs";
 	 
 	private static final String TAG = "ImageGridActivity";
 
 	private ImageGridAdapter imageAdapter;
 	
 	private GridView gridView = null;
 	
     private int mImageThumbSize;
 
     private int mImageThumbSpacing;
     
 	private static List<ImageInfo> imageList = new ArrayList<ImageInfo>();
 	
 	private ImageFetcher mImageFetcher;
 	
 	private int columnWidth = 0 ;
 	
 	private Handler handler = new Handler() {
 		public void handleMessage(android.os.Message msg) {
 			imageAdapter.notifyDataSetChanged();
 		};
 	};
 	
     @Override
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
     	columnWidth = mImageThumbSize;
     	mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
     	setContentView(R.layout.layout_all_image);
 		gridView = (GridView) findViewById(R.id.all_image_gridview);
 		ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);
 	    // Set memory cache to 25% of mem class
 		final DisplayMetrics displayMetrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
         final int height = displayMetrics.heightPixels;
         final int width = displayMetrics.widthPixels;
         final int longest = (height > width ? height : width) ;
         
 		mImageFetcher = new ImageFetcher(this, longest);
 		cacheParams.setMemCacheSizePercent(this, 0.25f);
 	    mImageFetcher.setLoadingImage(R.drawable.empty_photo);
 	    mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
 		imageAdapter = new ImageGridAdapter(ImageGrid.this, mImageFetcher, imageList);
 		gridView.setAdapter(imageAdapter);
 		new Thread(new Runnable() {
 			public void run() {
 				loadImageFiles();
 			}
 		}).start();
 		gridView.getViewTreeObserver().addOnGlobalLayoutListener(
                 new ViewTreeObserver.OnGlobalLayoutListener() {
                     @Override
                     public void onGlobalLayout() {
                         if (imageAdapter.getNumColumns() == 0) {
                             final int numColumns = (int) Math.floor(
                             		gridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                             if (numColumns > 0) {
                                 columnWidth =
                                         (gridView.getWidth() / numColumns) - mImageThumbSpacing;
                                 imageAdapter.setNumColumns(numColumns);
                                 imageAdapter.setItemHeight(columnWidth);
                                 if (BuildConfig.DEBUG) {
                                     Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                                 }
                             }
                         }
                     }
                 });
 		gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
 	            @Override
 	            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
 	                // Pause fetcher to ensure smoother scrolling when flinging
 	                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
 	                    mImageFetcher.setPauseWork(true);
 	                } else {
 	                    mImageFetcher.setPauseWork(false);
 	                }
 	            }
 
 	            @Override
 	            public void onScroll(AbsListView absListView, int firstVisibleItem,
 	                    int visibleItemCount, int totalItemCount) {
 	            }
 	        });
 		gridView.setOnItemClickListener(this);
     }
 	
 	@Override
     public void onItemClick(AdapterView<?> arg0, View v, int index, long id) {
     	/*final Intent i = new Intent(this, ImageDetailActivity.class);
         i.putExtra(ImageDetailActivity.EXTRA_IMAGE, index);
         i.putExtra(ImageDetailActivity.LIST_SIZE, imageList.size());
         if (Utils.hasJellyBean()) {
             // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
             // show plus the thumbnail image in GridView is cropped. so using
             // makeScaleUpAnimation() instead.
             ActivityOptions options =
                     ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
             startActivity(i, options.toBundle());
         } else {
             startActivity(i);
         }*/
 		Intent i = new Intent(this, FlingGalleryActivity.class);
 		 i.putExtra(ImageDetailActivity.EXTRA_IMAGE, index);
 		startActivity(i);
     }
     
     @Override
     public void onResume() {
         super.onResume();
         mImageFetcher.setExitTasksEarly(false);
         imageAdapter.notifyDataSetChanged();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mImageFetcher.setExitTasksEarly(true);
         mImageFetcher.flushCache();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mImageFetcher.closeCache();
         imageList.clear();
     }
     
 	public static String getFilePath(int position) {
 		return imageList.get(position).path;
 	}
 	
 	public static List<ImageInfo> getImageList() {
 		return imageList;
 	}
 	
 	private void loadImageFiles() {
 		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
 			String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();// ȡĿ¼
 			File file = new File(sdDir + FileUtil.parentPath + FileUtil.picForder);
 			if(!file.exists()) {
 				file.mkdirs();
 				File noMedia = new File(sdDir + FileUtil.parentPath + FileUtil.picForder + ".nomedia");
 				try {
 					noMedia.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			String fileThumbnail = sdDir + FileUtil.parentPath + FileUtil.picThumbnail;
 			File thumbnailFile = new File(fileThumbnail);
 			if(!thumbnailFile.exists()) {
 				thumbnailFile.mkdirs();
				File noMedia = new File(sdDir + FileUtil.parentPath + FileUtil.picThumbnail + ".nomedia");
				try {
					noMedia.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
 			}
 			File[] files = file.listFiles(new FileFilter() {
 				
 				@Override
 				public boolean accept(File pathname) {
 					if(pathname.isDirectory()) {
 						return true;
 					}
 					String name = pathname.getName().toLowerCase();
 					return name.endsWith(".jpeg") || name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif");
 				}
 			});
             for(int i = 0; i < files.length; i++){
             	if(files[i].isFile()) {
 	            	//paths[i] = files[i].getPath();
 	            	ImageInfo imageInfo = new ImageInfo();
 	            	imageInfo.path = files[i].getPath();
 	            	imageInfo.title = files[i].getName();
 	            	imageInfo.thumbnail = fileThumbnail + imageInfo.title;
 	            	System.out.println(imageInfo.thumbnail + "  " + imageInfo.title);
 	            	File thumbnail = new File(imageInfo.thumbnail);
 	            	if(!thumbnail.exists()) {
 	            		BitmapFactory.Options options = new BitmapFactory.Options();
 	            	    options.inJustDecodeBounds = true;
 	            	    Bitmap bitmap = BitmapFactory.decodeFile(imageInfo.path, options); //ʱbmΪ
 	            	    options.inJustDecodeBounds = false;
 	            	    int be = (int)(options.outWidth / (float)columnWidth);
 	            	    if (be <= 0)
 	            	    	be = 1;
 	            	    options.inSampleSize = be;
 	            	    bitmap=BitmapFactory.decodeFile(imageInfo.path,options);
 	            	    FileOutputStream out = null;
 						try {
 							out = new FileOutputStream(thumbnail);
 							if(bitmap!= null && bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)){
 								out.flush();
 							}
 						} catch (Exception e) {
 							e.printStackTrace();
 						} finally {
 							if(out != null ){
 								try {
 									out.close();
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 							}
 						}
 	
 	            	}
             		Uri uri = Uri.parse("file://"+files[i].getPath());
             		imageInfo.setActivity(uri, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
             		imageList.add(imageInfo);
             		handler.sendEmptyMessage(1);
             	}
             }
 		}
 	}
 	static class ViewHolder {
 
 		public ImageView icon;
 
 		public TextView name;
 	}
 }
