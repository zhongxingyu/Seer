 package com.tabbie.android.radar;
 
 /**
  *  EventDetailsPagerAdapter.java
  * 
  *  Created on: Aug 12, 2012
  *      Author: Justin Knutson
  * 
  *  Custom adapter to scroll through multiple
  *  events from the EventDetailsActivity with
  *  unimplemented methods for Tab Bar integration
  */
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.view.ViewPager;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class EventDetailsPagerAdapter
 	extends android.support.v4.view.PagerAdapter
 	implements ViewPager.OnPageChangeListener {
 	
 	private final Context context;
 	private final RadarCommonController controller;
 	private final int pageLayout;
 	private final ViewPager pager;
 	
 	public EventDetailsPagerAdapter(final Context context,
 			final RadarCommonController controller,
 			final int pageLayout,
 			final ViewPager pager) {
 		
 		this.context = context;
 		this.controller = controller;
 		this.pageLayout = pageLayout;
 		this.pager = pager;
 		pager.setAdapter(this);
 		pager.setOnPageChangeListener(this);
 	}
 	
 	@Override
 	public Object instantiateItem(android.view.ViewGroup container, int position) {
 		Log.d("EventDetailsPagerAdapter", "Adding View at position " + position);
 		final Event e = controller.eventsList.get(position);
 		final View v = bindEvent(e);
 		container.addView(v);
 		return v;
 	};
 	
 	
 	@Override
 	public void destroyItem(android.view.ViewGroup container, int position, Object object) {
 		Log.d("EventDetailsPagerAdapter", "Removing View at position " + position);
 		container.removeView((View) object);
 	};
 
 	@Override
 	public int getCount() {
 		Log.d("EventDetailsPagerAdapter", "Size is " + controller.eventsList.size());
 		return controller.eventsList.size();
 	}
 
 	@Override
 	public boolean isViewFromObject(View view, Object object) {
 		return view==object;
 	}
 	
 	private View bindEvent(final Event e) {
 		final View v = LayoutInflater.from(context).inflate(pageLayout, null);
 		
 	    ((TextView) v.findViewById(R.id.details_event_title)).setText(e.name);
 	    ((TextView) v.findViewById(R.id.details_event_time)).setText(e.time
 	        .makeYourTime());
 	    ((TextView) v.findViewById(R.id.details_event_location)).setText(e.venueName);
 	    ((TextView) v.findViewById(R.id.details_event_address)).setText(e.address);
 	    ((TextView) v.findViewById(R.id.details_event_num_radar)).setText(Integer
 	        .toString(e.radarCount));
 	    ((TextView) v.findViewById(R.id.details_event_description))
 	        .setText(e.description);
 	    Linkify.addLinks((TextView) v.findViewById(R.id.details_event_description),
 	        Linkify.WEB_URLS);
 	    
 	    ((ImageView) v.findViewById(R.id.location_image)).setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 			    Intent intent = new Intent((EventDetailsActivity) context,
 			            RadarMapActivity.class);
 			        intent.putExtra("controller", controller);
 			        intent.putExtra("event", e);
 			        context.startActivity(intent);
 			}
 		});
 	    
 	    final ImageView radarButton = (ImageView) v.findViewById(R.id.add_to_radar_image);
 	    radarButton.setSelected(e.isOnRadar());
	    
 	    radarButton.setOnClickListener(new OnClickListener() {
 			@Override
			public void onClick(View radar) {
 				((RadarSelectedListener) context).onRadarSelected(v, e);
 			}
 		});
 	    
 	    v.setTag(e);
 	    
 	    // TODO Code to load image, etc.
 	    
 	    return v;
 	}
 
 	
 	// TODO These methods give us access to
 	//		the action bar/tab bar if we ever
 	//		want to create such a display.
 	
 	@Override
 	public void onPageScrollStateChanged(int arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onPageScrolled(int arg0, float arg1, int arg2) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onPageSelected(int arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public interface RadarSelectedListener {
 		public void onRadarSelected(final View v, final Event e);
 	}
 }
