 package com.allplayers.android;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.allplayers.objects.PhotoData;
 import com.allplayers.rest.RestApiV1;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 
 public class PhotoPager extends Activity {
     private ViewPager mViewPager;
     private PhotoPagerAdapter photoAdapter;
     private PhotoData currentPhoto;
     private int currentPhotoIndex;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         if (savedInstanceState != null) {
             currentPhotoIndex = savedInstanceState.getInt("photoToStart");
         }
         super.onCreate(savedInstanceState);
         currentPhoto = (new Router(this)).getIntentPhoto();
         mViewPager = new ViewPager(this);
         setContentView(mViewPager);
         photoAdapter = new PhotoPagerAdapter(this, currentPhoto);
         mViewPager.setAdapter(photoAdapter);
         mViewPager.setCurrentItem(currentPhotoIndex);
     }
 
     protected void onSaveInstanceState(Bundle icicle) {
         super.onSaveInstanceState(icicle);
        currentPhotoIndex = mViewPager.getCurrentItem();
         icicle.putInt("photoToStart", currentPhotoIndex);
     }
 
     public class PhotoPagerAdapter extends PagerAdapter {
         private Context mContext;
         private ImageView[] images;
         private List<PhotoData> photos;
 
         public PhotoPagerAdapter(Context context, PhotoData item) {
             mContext = context;
             photos = new ArrayList<PhotoData>();
 
             PhotoData temp = item;
             while (temp.previousPhoto() != null) {
                 photos.add(0, temp.previousPhoto());
                 temp = temp.previousPhoto();
                 System.out.println("Added a photo before.");
             }
             if (currentPhotoIndex == 0) {
                 currentPhotoIndex = photos.size();
             }
             photos.add(item);
             temp = item;
             while (temp.nextPhoto() != null) {
                 photos.add(temp.nextPhoto());
                 temp = temp.nextPhoto();
                 System.out.println("Added a photo after.");
             }
             images = new ImageView[photos.size()];
         }
 
         @Override
         public Object instantiateItem(View collection, int position) {
             ImageView image = new ImageView(PhotoPager.this);
             image.setImageResource(R.drawable.loading_image);
             if (images[position] != null) {
                 ((ViewPager) collection).addView(images[position], 0);
                 return images[position];
             }
             images[position] = image;
             new GetRemoteImageTask().execute(photos.get(position).getPhotoFull(), position);
             ((ViewPager) collection).addView(images[position], 0);
             return images[position];
         }
 
         @Override
         public void destroyItem(ViewGroup container, int position, Object object) {
             ((ViewPager) container).removeView((ImageView) object);
         }
 
         @Override
         public int getCount() {
             return photos.size();
         }
 
         @Override
         public boolean isViewFromObject(View view, Object obj) {
             return view == obj;
         }
 
         /**
          * Get's a user's image using a rest call and displays it.
          */
         public class GetRemoteImageTask extends AsyncTask<Object, Void, Bitmap> {
             int index;
             protected Bitmap doInBackground(Object... photoUrl) {
                 index = (Integer) photoUrl[1];
                 return RestApiV1.getRemoteImage((String) photoUrl[0]);
             }
 
             protected void onPostExecute(Bitmap image) {
                 images[index].setImageBitmap(image);
             }
         }
     }
 }
