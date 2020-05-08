 package com.taiwan.imageload;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.taiwan.news.entity.News;
 import com.taiwan.realtime.news.R;
 
 public class GalleryAdapter extends BaseAdapter {
 
     private final Activity        activity;
     private final ArrayList<News> data;
     private static LayoutInflater inflater = null;
     public ImageLoader            imageLoader;
     private final int             width;
 
     public GalleryAdapter(Activity a, ArrayList<News> d, int myWidth) {
         activity = a;
         data = d;
         inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         imageLoader = new ImageLoader(activity.getApplicationContext(), 70);
         width = myWidth;
     }
 
     public int getCount() {
         return data.size();
     }
 
     public Object getItem(int position) {
         return position;
     }
 
     public long getItemId(int position) {
         return position;
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
         View vi = convertView;
         if (convertView == null)
             vi = inflater.inflate(R.layout.item_gallery, null);
 
         TextView textTitle = (TextView) vi.findViewById(R.id.text_gallery_title);
         ImageView image = (ImageView) vi.findViewById(R.id.image_gallery);
         TextView textCategory = (TextView) vi.findViewById(R.id.text_gallery_category);
 
         textTitle.setText(data.get(position).getTitle());
         textCategory.setText(data.get(position).getCategoryName());
        try{
        	imageLoader.DisplayImage(data.get(position).getPictures().get(0).getUrl(), image);
        }catch(Exception e){
        	
        }
 
         vi.setLayoutParams(new Gallery.LayoutParams(width, 200));
         return vi;
     }
 }
