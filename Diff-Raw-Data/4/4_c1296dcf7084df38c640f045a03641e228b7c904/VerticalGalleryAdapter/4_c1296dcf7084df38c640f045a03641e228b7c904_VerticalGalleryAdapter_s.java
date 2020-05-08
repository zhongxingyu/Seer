 package com.cs301w01.meatload.adapters;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.drawable.Drawable;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.model.Picture;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * Use this adapter when you want to create a vertical photo gallery that slides up and down.
  * @author Derek Dowling
  */
 public class VerticalGalleryAdapter extends BaseAdapter{
 
     int mGalleryItemBackground;
     private Context mContext;
     private ArrayList<Picture> pictures;
 
     public VerticalGalleryAdapter(Context c, Collection<Picture> pictures) {
 
         mContext = c;
         TypedArray attr = mContext.obtainStyledAttributes(R.styleable.GalleryActivity);
         mGalleryItemBackground = attr.getResourceId(
                 R.styleable.GalleryActivity_android_galleryItemBackground, 0);
         this.pictures = new ArrayList<Picture>(pictures);
         attr.recycle();
     }
 
     public int getCount() {
         return pictures.size();
     }
 
     public Picture getItem(int position) {
         return pictures.get(position);
     }
 
     public long getItemId(int position) {
         return pictures.get(position).getPictureID();
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
         ImageView imageView = new ImageView(mContext);
 
         imageView.setImageDrawable(Drawable.createFromPath(pictures.get(position).getPath()));
         imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
         imageView.setScaleType(ImageView.ScaleType.FIT_XY);
         imageView.setBackgroundResource(mGalleryItemBackground);
 
         return imageView;
     }

}
