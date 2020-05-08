 package com.android.Oasis.recent;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.android.Oasis.R;
 import com.android.Oasis.diary.OldDiary;
 import com.android.Oasis.story.Story;
 
 public class Recent extends Activity {
 
 	//TextView viewPager;
 	TextView tv_recent;
 	TextView tv_letter;
 	ImageView img_letter;
 	ScrollView sv_letter;
 	LinearLayout ll_pager;
 	
 	Intent intent = new Intent();
 	Bundle bundle = new Bundle();
 	int PLANT = 0;
 	
 	private ViewPager viewPager;
 	private Context cxt;
 	private pagerAdapter pageradapter;
 
 	String[] plantstrs;
 	
 	private int RAIN = 1;
 	private int BUG = 2;
 	private int LEAF = 3;
 	private int SICK = 4;
 	private int pageType = 1;
 	
 	String[] rainStrs;
 	String[] bugStrs;
 	String[] leafStrs;
 	String[] sickStrs;
 	
 	//String[] recentstrs;
 	/*
 	private int[] recentarray = {
 			R.array.recent0,R.array.recent0,R.array.recent0,R.array.recent0,R.array.recent0,
 			R.array.recent0,R.array.recent0,R.array.recent0,R.array.recent0,R.array.recent0,
 			R.array.recent0,R.array.recent0,R.array.recent0,R.array.recent0,R.array.recent0,
 			R.array.recent0,R.array.recent0,R.array.recent0,R.array.recent0,R.array.recent0
 	};*/
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.recent);
 		cxt = this;
 		
 		bundle = this.getIntent().getExtras();
 		PLANT = bundle.getInt("plant");
 		
 		Resources res = Recent.this.getResources();
 		plantstrs = res.getStringArray(R.array.plantname);
 		
 		sv_letter = (ScrollView)findViewById(R.id.sv_letter);
 		ll_pager = (LinearLayout)findViewById(R.id.ll_pager);
 		
 		DisplayMetrics displaymetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
 		int height = displaymetrics.heightPixels;
 		
 		rainStrs = res.getStringArray(R.array.hurt);
 		bugStrs = res.getStringArray(R.array.bug);
 		leafStrs = res.getStringArray(R.array.leaf);
 		sickStrs = res.getStringArray(R.array.sick);
 		//recentstrs = res.getStringArray(recentarray[PLANT]);
 		
 		pageradapter = new pagerAdapter();
 		viewPager = (ViewPager) findViewById(R.id.pager);
 		viewPager.setAdapter(pageradapter);
 
 		//viewPager = (TextView) findViewById(R.id.pager);
 		//viewPager.setTextColor(Color.BLACK);
 		//viewPager.setTextSize(20);
 		//viewPager.setTypeface(Typeface.createFromAsset(getAssets(),
 		//		"fonts/fontw3.ttc"));
 		//viewPager.setText(recentstrs[0]);
 		
 		tv_recent = (TextView)findViewById(R.id.tv_recent);
 		tv_recent.setText(defaultRecentString());
 		tv_recent.setTextSize(20);
 		tv_recent.setPadding(30, 30, 30, 30);
 		tv_recent.setTypeface(Typeface.createFromAsset(getAssets(),
 				"fonts/fontw3.ttc"));
 		
 		tv_letter = (TextView)findViewById(R.id.tv_letter);
 		tv_letter.setText("您有一封來自" + plantstrs[PLANT] + "的訊息");
 		tv_letter.setTextSize(20);
 		tv_letter.setTypeface(Typeface.createFromAsset(getAssets(),
 				"fonts/fontw3.ttc"));
 		img_letter = (ImageView)findViewById(R.id.img_letter);
 		img_letter.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				img_letter.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.letter_open));
 				handler.sendEmptyMessageDelayed(1, 1000);
 			}
 		});
 		img_letter.setPadding(0, (int) (height*0.3-110), 0, 0);
 
 		final ImageButton btn_rain = (ImageButton) findViewById(R.id.recent_btn_rain);
 		final ImageButton btn_worm = (ImageButton) findViewById(R.id.recent_btn_worm);
 		final ImageButton btn_leaf = (ImageButton) findViewById(R.id.recent_btn_leaf);
 		final ImageButton btn_sick = (ImageButton) findViewById(R.id.recent_btn_sick);
 		//btn_rain.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_rain_y));
 		
 		btn_rain.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				btn_rain.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_rain_y));
 				btn_worm.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_worm));
 				btn_leaf.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_leaf));
 				btn_sick.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_sick));
 				if(tv_recent.isShown()){
 					sv_letter.setVisibility(View.GONE);
 					ll_pager.setVisibility(View.VISIBLE);
 				}
 				pageType = RAIN;
 				viewPager.setAdapter(pageradapter);
 				//viewPager.setText(recentstrs[0]);
 			}
 		});
 
 		btn_worm.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				btn_rain.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_rain));
 				btn_worm.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_worm_y));
 				btn_leaf.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_leaf));
 				btn_sick.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_sick));
 				if(tv_recent.isShown()){
 					sv_letter.setVisibility(View.GONE);
 					ll_pager.setVisibility(View.VISIBLE);
 				}
 				pageType = BUG;
 				viewPager.setAdapter(pageradapter);
 				//viewPager.setText(recentstrs[1]);
 			}
 		});
 		
 		btn_leaf.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				btn_rain.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_rain));
 				btn_worm.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_worm));
 				btn_leaf.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_leaf_y));
 				btn_sick.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_sick));
 				if(tv_recent.isShown()){
 					sv_letter.setVisibility(View.GONE);
 					ll_pager.setVisibility(View.VISIBLE);
 				}
 				pageType = LEAF;
 				viewPager.setAdapter(pageradapter);
 				//viewPager.setText(recentstrs[2]);
 			}
 		});
 		
 		btn_sick.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				btn_rain.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_rain));
 				btn_worm.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_worm));
 				btn_leaf.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_leaf));
 				btn_sick.setImageDrawable(Recent.this.getResources().getDrawable(R.drawable.recent_btn_sick_y));
 				if(tv_recent.isShown()){
 					sv_letter.setVisibility(View.GONE);
 					ll_pager.setVisibility(View.VISIBLE);
 				}
 				pageType = SICK;
 				viewPager.setAdapter(pageradapter);
 				//viewPager.setText(recentstrs[3]);
 			}
 		});
 		
 		ImageButton btn_story = (ImageButton) findViewById(R.id.main_btn_story);
 		btn_story.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				intent.putExtras(bundle);
 				intent.setClass(Recent.this, Story.class);
 				startActivity(intent);
 				System.gc();
 				Recent.this.finish();
 			}
 		});
 		
 		ImageButton btn_diary = (ImageButton) findViewById(R.id.main_btn_diary);
 		btn_diary.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				intent.putExtras(bundle);
 				intent.setClass(Recent.this, OldDiary.class);
 				startActivity(intent);
 				System.gc();
 				Recent.this.finish();
 			}
 		});
 		
 		ImageButton btn_recent = (ImageButton) findViewById(R.id.main_btn_recent);
 		btn_recent.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				intent.putExtras(bundle);
 				intent.setClass(Recent.this, Recent.class);
 				startActivity(intent);
 				System.gc();
 				Recent.this.finish();
 			}
 		});
 		
 		ImageButton btn_life = (ImageButton) findViewById(R.id.main_btn_life);
 		btn_life.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Uri uri = Uri.parse(Recent.this.getResources().getString(R.string.fb_url));
 				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 				startActivity(intent);
 			}
 		});
 	}
 	private String defaultRecentString(){
 		String recent;
 		HttpClient hc = new DefaultHttpClient(); 
 		HttpGet get = new HttpGet(this.getString(R.string.recentURL));
 		try {
 			SharedPreferences settings = this.getSharedPreferences(this.getString(R.string.app_name), 0);
 			HttpResponse rp = hc.execute(get);
 			if(rp.getStatusLine().getStatusCode() != 200)
 				throw new Exception();
			recent = EntityUtils.toString(hc.execute(get).getEntity(), "UTF-8");
			Log.e("lmr3796", "幹");
			Log.e("lmr3796", recent);
 			settings.edit().putString("cache", recent);
 			return recent;
 		} catch (Exception e) {
 			return defaultRecentStringFromCache();
 		}		
 	}
 	private String defaultRecentStringFromCache(){
 		SharedPreferences settings = this.getSharedPreferences(this.getString(R.string.app_name), 0);
 		String cacheString = settings.getString("cache", "");
 		if(!cacheString.equals("")){
 			return cacheString;
 		}
 		return this.getString(R.string.recent);
 	}
 	
 	private class pagerAdapter extends PagerAdapter {
 
 		@Override
 		public int getCount() {
 			// return NUM_VIEWS;
 			if(pageType==RAIN) return rainStrs.length;
 			else if(pageType==BUG) return bugStrs.length;
 			else if(pageType==LEAF) return leafStrs.length;
 			else return sickStrs.length;
 		}
 
 		/**
 		 * Create the page for the given position. The adapter is responsible
 		 * for adding the view to the container given here, although it only
 		 * must ensure this is done by the time it returns from
 		 * {@link #finishUpdate()}.
 		 * 
 		 * @param container
 		 *            The containing View in which the page will be shown.
 		 * @param position
 		 *            The page position to be instantiated.
 		 * @return Returns an Object representing the new page. This does not
 		 *         need to be a View, but can be some other container of the
 		 *         page.
 		 */
 		@Override
 		public Object instantiateItem(View collection, int position) {
 
 			ScrollView sv = new ScrollView(cxt);
 			LinearLayout ll = new LinearLayout(cxt);
 			ll.setOrientation(LinearLayout.VERTICAL);
 
 			TextView myTextView = new TextView(cxt);
 			
 			if(pageType==RAIN) myTextView.setText(rainStrs[position]);
 			else if(pageType==BUG) myTextView.setText(bugStrs[position]);
 			else if(pageType==LEAF) myTextView.setText(leafStrs[position]);
 			else myTextView.setText(sickStrs[position]);
 			
 			myTextView.setTextColor(Color.BLACK);
 			myTextView.setTextSize(20);
 			myTextView.setTypeface(Typeface.createFromAsset(getAssets(),
 					"fonts/fontw3.ttc"));
 			myTextView.setPadding(30, 30, 30, 30);
 				
 			ll.addView(myTextView);
 			sv.addView(ll);
 			((ViewPager) collection).addView(sv, 0);
 			
 			return sv;
 		}
 
 		/**
 		 * Remove a page for the given position. The adapter is responsible for
 		 * removing the view from its container, although it only must ensure
 		 * this is done by the time it returns from {@link #finishUpdate()}.
 		 * 
 		 * @param container
 		 *            The containing View from which the page will be removed.
 		 * @param position
 		 *            The page position to be removed.
 		 * @param object
 		 *            The same object that was returned by
 		 *            {@link #instantiateItem(View, int)}.
 		 */
 		@Override
 		public void destroyItem(View collection, int position, Object view) {
 			((ViewPager) collection).removeView((ScrollView) view);
 		}
 
 		@Override
 		public boolean isViewFromObject(View view, Object object) {
 			return view == ((ScrollView) object);
 		}
 
 		/**
 		 * Called when the a change in the shown pages has been completed. At
 		 * this point you must ensure that all of the pages have actually been
 		 * added or removed from the container as appropriate.
 		 * 
 		 * @param container
 		 *            The containing View which is displaying this adapter's
 		 *            page views.
 		 */
 		@Override
 		public void finishUpdate(View arg0) {
 		}
 
 		@Override
 		public void restoreState(Parcelable arg0, ClassLoader arg1) {
 		}
 
 		@Override
 		public Parcelable saveState() {
 			return null;
 		}
 
 		@Override
 		public void startUpdate(View arg0) {
 		}
 
 	}
 	
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			switch (msg.what) {
 			case 1:
 				img_letter.setVisibility(View.GONE);
 				tv_letter.setVisibility(View.GONE);
 				tv_recent.setVisibility(View.VISIBLE);
 				break;
 			}
 		}
 	};
 
 
 }
