 package com.thoughtworks.pumpkin.helper;
 
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import com.parse.ParseObject;
 
 import java.util.Map;
 
 public class BookViewHolder {
     public TextView rating;
     public TextView title;
     public ImageButton wishListButton;
     public ImageView image;
     public ProgressBar spinner;
     public ProgressBar bookSpinner;
     public Map<String, ParseObject> wishListBooks;
 }
