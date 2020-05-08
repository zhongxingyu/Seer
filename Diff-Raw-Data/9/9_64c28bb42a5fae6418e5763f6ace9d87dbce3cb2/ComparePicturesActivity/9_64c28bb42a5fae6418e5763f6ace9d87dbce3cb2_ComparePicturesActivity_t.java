 package com.cs301w01.meatload.activities;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.adapters.HorizontalGalleryAdapter;
 import com.cs301w01.meatload.controllers.GalleryManager;
 import com.cs301w01.meatload.model.GalleryData;
 import com.cs301w01.meatload.model.Picture;
 
 import java.util.ArrayList;
 
 /**
  * Implements the logic of the Compare Pictures interface.
  * @author Derek Dowling
  */
 public class ComparePicturesActivity extends Skindactivity {
 
     private Gallery topGallery;
     private Gallery bottomGallery;
     private HorizontalGalleryAdapter gAdapter;
 
     private ImageView topPicture;
     private ImageView bottomPicture;
     
     private GalleryManager galleryManager;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.compare_picture);
 
         //create gallery manager
         Bundle b = getIntent().getExtras();
         GalleryData gallerydata = (GalleryData) b.getSerializable("gallery");
         galleryManager = new GalleryManager(gallerydata);
         galleryManager.setContext(this);
 
         //create adapter used by galleries
         gAdapter = new HorizontalGalleryAdapter(this, galleryManager.getPictureGallery(),
                R.styleable.ComparePicturesActivity, R.styleable.ComparePicturesActivity_android_galleryItemBackground);
 
         //set adapter
         topGallery.setAdapter(gAdapter);
         bottomGallery.setAdapter(gAdapter);
 
         ArrayList<Picture> pictures = new ArrayList<Picture>(galleryManager.getPictureGallery());
         
         //set pictures
         topPicture = new ImageView(this);
         topPicture.setImageDrawable(Drawable.createFromPath(pictures.get(0).getPath()));
         bottomPicture = new ImageView(this);
         bottomPicture.setImageDrawable(Drawable.createFromPath(pictures.get(1).getPath()));
     }
 
     //@Override
     public void update(Object model) {
     	
     }
 
     public void createListeners() {
 
         topGallery.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
             
                 String pPath = galleryManager.getPhoto(view.getId()).getPath();
                 updateTopPicture(pPath);
                 
             }
         });
 
         bottomGallery.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
 
                 String pPath = galleryManager.getPhoto(view.getId()).getPath();
                 updateBottomPicture(pPath);
 
             }
         });
 
     }
 
     public void updateTopPicture(String path) {
 
         topPicture.setImageDrawable(Drawable.createFromPath(path));
         topPicture.refreshDrawableState();
 
     }
 
     public void updateBottomPicture(String path) {
 
         bottomPicture.setImageDrawable(Drawable.createFromPath(path));
         bottomPicture.refreshDrawableState();
     }
 }
