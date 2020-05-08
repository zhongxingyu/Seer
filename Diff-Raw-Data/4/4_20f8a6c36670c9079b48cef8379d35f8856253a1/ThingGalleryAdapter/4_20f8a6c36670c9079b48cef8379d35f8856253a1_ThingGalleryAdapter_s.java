 /*   Copyright 2012 Mario Bhmer
  *
  *   Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) 
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 package com.blogspot.marioboehmer.thingibrowse.adapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Adapter;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 
 import com.blogspot.marioboehmer.thingibrowse.R;
 import com.blogspot.marioboehmer.thingibrowse.ThingGalleryActivity;
 import com.blogspot.marioboehmer.thingibrowse.ThingiBrowseApplication;
 import com.novoda.imageloader.core.loader.Loader;
 import com.novoda.imageloader.core.model.ImageTag;
 import com.novoda.imageloader.core.model.ImageTagFactory;
 
 /**
  * {@link Adapter} implementation for the {@link ThingGalleryActivity}'s
  * {@link Gallery}.
  * 
  * @author Mario Bhmer
  */
 public class ThingGalleryAdapter extends BaseAdapter {
 
 	private Context mContext;
 	private List<String> imageUrls = new ArrayList<String>();
 
 	private Loader imageLoader;
 	private ImageTagFactory imageTagFactory;
 
 	public ThingGalleryAdapter(Context c) {
 		mContext = c;
 		imageTagFactory = new ImageTagFactory(c, R.drawable.image_loading);
 		imageTagFactory.setErrorImageId(R.drawable.image_not_found);
 		imageLoader = ThingiBrowseApplication.getImageManager().getLoader();
 	}
 
 	public int getCount() {
 		return imageUrls.size();
 	}
 
 	public Object getItem(int position) {
 		return imageUrls.get(position);
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ImageView imageView = new ImageView(mContext);
		imageView.setLayoutParams(new LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
 		imageView.setPadding(25, 0, 25, 0);
 		ImageTag tag = imageTagFactory.build(imageUrls.get(position));
 		imageView.setTag(tag);
 	    imageLoader.load(imageView);
 		return imageView;
 	}
 
 	public void setImageUrls(List<String> imageUrls) {
 		this.imageUrls = imageUrls;
 	}
 }
