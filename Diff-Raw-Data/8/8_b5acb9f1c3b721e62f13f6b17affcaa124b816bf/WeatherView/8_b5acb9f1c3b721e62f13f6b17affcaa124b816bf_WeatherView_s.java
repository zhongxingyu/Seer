 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.weather.view;
 
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 
 import org.gots.R;
 import org.gots.weather.WeatherConditionInterface;
 
 import android.content.Context;
 import android.text.Layout;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class WeatherView extends LinearLayout {
 	Context mContext;
 	WeatherConditionInterface mWeather = null;
 	LayoutInflater inflater;
 	Layout layout;
 	public static final int IMAGE = 0;
 	public static final int TEXT = 1;
 	public static final int FULL = 2;
 	private int mType = FULL;
 
 	public WeatherView(Context context) {
 		super(context);
 		this.mContext = context;
 		initView();
 
 	}
 
 	public WeatherView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.mContext = context;
 
 		initView();
 
 	}
 
 	@Override
 	protected void onFinishInflate() {
 		super.onFinishInflate();
 		setupView();
 
 	}
 
 	private void initView() {
 		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View view = inflater.inflate(R.layout.weather_widget, this);
 
 	}
 
 	private void setupView() {
 		LinearLayout boxTemp = (LinearLayout) findViewById(R.id.idWeatherTemp);
 		ImageView weatherWidget = (ImageView) findViewById(R.id.idWeatherImage);
 		TextView weatherDay = (TextView) findViewById(R.id.idWeatherDay);
 		TextView tempMax = (TextView) findViewById(R.id.idWeatherMax);
 		TextView tempMin = (TextView) findViewById(R.id.idWeatherMin);
 
 		switch (mType) {
 		case TEXT:
 			weatherWidget.setVisibility(View.GONE);
 			if (mWeather.getIconURL() == null) {
 				boxTemp.setVisibility(View.GONE);
 				weatherDay.setVisibility(View.GONE);
 			}
 			break;
 		case IMAGE:
 			boxTemp.setVisibility(View.GONE);
 			weatherDay.setVisibility(View.GONE);
 			
 			break;
 
 		default:
 			break;
 		}
 		if (mWeather == null) {
 			return;
 		}
 
 		weatherWidget.setImageResource(getWeatherResource(mWeather));
 
 		tempMin.setText("" + mWeather.getTempCelciusMin());
 
 		tempMax.setText("" + mWeather.getTempCelciusMax());
 
		SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.FRENCH);
 		if (mWeather.getDate() != null)
 			weatherDay.setText("" + sdf.format(mWeather.getDate()));
 		invalidate();
 	}
 
 	@Override
 	protected void onLayout(boolean changed, int l, int t, int r, int b) {
 		super.onLayout(changed, l, t, r, b);

 	}
 
 	public void setWeather(WeatherConditionInterface weather) {
 		this.mWeather = weather;
 		setupView();
 	}
 
 	public static int getWeatherResource(WeatherConditionInterface weatherCondition) {
 		if (weatherCondition.getIconURL() == null)
 			return R.drawable.weather_nonet;
 
 		if (weatherCondition.getIconURL().contains("rain"))
 			return R.drawable.weather_rain;
 		else if (weatherCondition.getIconURL().contains("mostly_sunny"))
 			return R.drawable.weather_mostlysunny;
 		else if (weatherCondition.getIconURL().contains("cloud"))
 			return R.drawable.weather_cloud;
 
 		return R.drawable.weather_sun;
 	}
 
 	public void setType(int mType) {
 		this.mType = mType;
 	}
 }
