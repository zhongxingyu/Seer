 /*
  * Copyright 2012 Aleksi Niiranen 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.blogspot.fwfaill.lunchbuddy;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.preference.PreferenceManager;
 import android.support.v4.widget.ResourceCursorAdapter;
 import android.view.View;
 import android.widget.TextView;
 
 public class CourseCursorAdapter extends ResourceCursorAdapter {
 	
 	private static final String TAG = "CourseCursorAdapter";
 	
 	private String mLang;
 	
 	private int mColumnIndexTitleFi;
 	private int mColumnIndexTitleEn;
 	private int mColumnIndexTitleSe;
 	private int mColumnIndexTitlePrice;
 	private int mColumnIndexTitleProperties;
 	
 	public CourseCursorAdapter(int layout, Context context, Cursor c) {
 		super(context, layout, c);
 		mColumnIndexTitleFi = c.getColumnIndexOrThrow(LunchBuddy.Courses.COLUMN_NAME_TITLE_FI);
 		mColumnIndexTitleEn = c.getColumnIndexOrThrow(LunchBuddy.Courses.COLUMN_NAME_TITLE_EN);
 		mColumnIndexTitleSe = c.getColumnIndexOrThrow(LunchBuddy.Courses.COLUMN_NAME_TITLE_SE);
 		mColumnIndexTitlePrice = c.getColumnIndexOrThrow(LunchBuddy.Courses.COLUMN_NAME_PRICE);
 		mColumnIndexTitleProperties = c.getColumnIndexOrThrow(LunchBuddy.Courses.COLUMN_NAME_PROPERTIES);
 		
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
 		mLang = preferences.getString("language_preference", context.getString(R.string.default_language));
 	}
 
 	@Override
 	public void bindView(View view, Context context, Cursor c) {
 		ViewHolder holder = (ViewHolder) view.getTag();
 		
 		if (holder == null) {
 			holder = new ViewHolder();
 			holder.title = (TextView) view.findViewById(R.id.course_title);
 			holder.price = (TextView) view.findViewById(R.id.course_price);
 			holder.properties = (TextView) view.findViewById(R.id.course_properties);
 			
 			view.setTag(holder);
 		}
		if (mLang.equals("fi") || c.getString(mColumnIndexTitleSe).equals("null") || c.getString(mColumnIndexTitleEn).equals("null"))
			holder.title.setText(c.getString(mColumnIndexTitleFi));
		else if (mLang.equals("se"))
 			holder.title.setText(c.getString(mColumnIndexTitleSe));
 		else
			holder.title.setText(c.getString(mColumnIndexTitleEn));
 		holder.price.setText(c.getString(mColumnIndexTitlePrice) + " €");
 		String properties = c.getString(mColumnIndexTitleProperties).equals("null") ? "" : c.getString(mColumnIndexTitleProperties);
 		holder.properties.setText(properties);
 	}
 
 	static class ViewHolder {
 		public TextView title;
 		public TextView price;
 		public TextView properties;
 	}
 }
