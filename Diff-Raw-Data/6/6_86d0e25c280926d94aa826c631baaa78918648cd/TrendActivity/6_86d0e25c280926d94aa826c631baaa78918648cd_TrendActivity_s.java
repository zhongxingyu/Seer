 /**
  * 
  */
 package org.weather.weatherman.activity;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 import org.weather.weatherman.R;
 import org.weather.weatherman.WeatherApplication;
 import org.weather.weatherman.achartengine.LineChartFactory;
 import org.weather.weatherman.achartengine.MyXYSeries;
 import org.weather.weatherman.content.Weather;
 
import android.R.color;
 import android.app.Activity;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 /**
  * @author gmz
  * 
  */
 public class TrendActivity extends Activity {
 
 	private WeatherApplication app;
 	private LinearLayout layout;
 	private Resources res;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.trend);
 		app = (WeatherApplication) getApplication();
 		layout = (LinearLayout) findViewById(R.id.trendContainer);
 		res = getResources();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
 		progressBar.setVisibility(View.VISIBLE);
 		String city = (app.getCity() != null ? app.getCity().getId() : null);
 		new TrendTask().execute(city);
 	}
 
 	class TrendTask extends AsyncTask<String, Integer, Cursor> {
 
		private DateFormat DF_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
 		public TrendTask() {
 		}
 
 		@Override
 		protected Cursor doInBackground(String... params) {
 			onProgressUpdate(0);
 			String city = (params != null && params.length > 0 ? params[0] : null);
 			if (city == null || city.length() == 0) {
 				return null;
 			}
 			onProgressUpdate(20);
 			Uri uri = Uri.withAppendedPath(Weather.ForecastWeather.CONTENT_URI, city);
 			onProgressUpdate(40);
 			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
 			onProgressUpdate(60);
 			return cursor;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 			int progress = (values != null && values.length > 0 ? values[0] : 0);
 			ProgressBar progressBar = (ProgressBar) getParent().findViewById(R.id.progressBar);
 			if (progressBar != null) {
 				Log.i(TrendTask.class.getSimpleName(), progress + "/" + progressBar.getMax());
 				progressBar.setProgress(progress);
 				if (progress >= progressBar.getMax()) {
 					progressBar.setVisibility(View.GONE);
 				}
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Cursor cursor) {
 			super.onPostExecute(cursor);
 			// clear
 			if (layout.getChildCount() > 0) {
 				layout.removeViews(0, layout.getChildCount());
 			}
 			onProgressUpdate(70);
 			// dataSet
 			XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
 			MyXYSeries daySeries = new MyXYSeries(res.getString(R.string.trend_temperature_day));
 			MyXYSeries nightSeries = new MyXYSeries(res.getString(R.string.trend_temperature_night));
 			Map<Double, String> xlabels = new HashMap<Double, String>();
 			if (cursor != null && cursor.moveToFirst()) {
 				double i = 0;
 				// update time
 				String text = cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.TIME));
 				Calendar cal = Calendar.getInstance();
 				try {
 					cal.setTime(DF_1.parse(text));
 				} catch (Exception e) {
 					Log.e(ForecastActivity.class.getSimpleName(), "parse update time failed", e);
 				}
 				cal.add(Calendar.HOUR_OF_DAY, -12);
 				do {
 					// date
 					cal.add(Calendar.HOUR_OF_DAY, 12);
 					String date = (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH);
 					if (!xlabels.containsValue(date)) {
 						xlabels.put(++i, date);
 					}
 					// temperature
 					String tp = cursor.getString(cursor.getColumnIndex(Weather.ForecastWeather.TEMPERATURE));
 					Double temp = Double.valueOf(tp.substring(0, tp.length() - 1));
 					boolean isNight = (cal.get(Calendar.HOUR_OF_DAY) >= 12);
 					if (isNight) {
 						nightSeries.add(i, temp, tp);
 					} else {
 						daySeries.add(i, temp, tp);
 					}
 				} while (cursor.moveToNext());
 			} else {
 				Toast.makeText(getApplicationContext(), getResources().getText(R.string.connect_failed),
 						Toast.LENGTH_LONG).show();
 				Log.e(ForecastActivity.class.getName(), "can't get forecast weather");
 			}
 			dataSet.addSeries(daySeries);
 			dataSet.addSeries(nightSeries);
 			onProgressUpdate(80);
 			// render
 			XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 			// renderer.setChartTitle(res.getString(R.string.trend_title));
 			// renderer.setAxisTitleTextSize(16);
 			// renderer.setChartTitleTextSize(20);
 			// renderer.setLabelsTextSize(15);
 			// renderer.setLegendTextSize(15);
 			renderer.setApplyBackgroundColor(true);
 			renderer.setBackgroundColor(Color.WHITE);
 			renderer.setMarginsColor(Color.WHITE);
 			renderer.setPointSize(4f);
 			renderer.setAxesColor(Color.DKGRAY);
 			renderer.setXLabelsColor(Color.BLACK);
 			// renderer.setXTitle("日期");
 			renderer.setYLabelsColor(0, Color.BLACK);
 			renderer.setYLabelsAlign(Align.RIGHT);
 			// renderer.setYTitle("温度");
 			renderer.setMargins(new int[] { 20, 20, 0, 10 });
 			// renderer.setShowGrid(true);
 			// renderer.setGridColor(Color.LTGRAY);
 			for (Double x : xlabels.keySet()) {
 				renderer.addXTextLabel(x, xlabels.get(x));
 			}
 			renderer.setXAxisMin(Math.min(daySeries.getMinX(), nightSeries.getMinX()) - 0.5);
 			renderer.setXAxisMax(Math.max(daySeries.getMaxX(), nightSeries.getMaxX()) + 0.5);
 			renderer.setXLabels(0);
 			double ymax = Math.max(daySeries.getMaxY(), nightSeries.getMaxY());
 			for (int t = 0; t <= ymax + 2; t += 2) {
 				renderer.addYTextLabel(t, t + "℃");
 			}
 			renderer.setYAxisMax(ymax + 2);
 			renderer.setYAxisMin(Math.min(daySeries.getMinY(), nightSeries.getMinY()) - 2);
 			renderer.setYLabels(0);
 			// renderer.setXTitle(res.getString(R.string.trend_x_title));
 			renderer.setZoomButtonsVisible(true);
 			renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
 			renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });
 			renderer.setZoomRate(1.05f);
 			XYSeriesRenderer dayRender = new XYSeriesRenderer();
 			dayRender.setColor(Color.BLUE);
 			dayRender.setDisplayChartValues(true);
 			dayRender.setPointStyle(PointStyle.DIAMOND);
 			dayRender.setFillPoints(true);
 			renderer.addSeriesRenderer(dayRender);
 			XYSeriesRenderer nightRender = new XYSeriesRenderer();
 			nightRender.setColor(Color.GREEN);
 			nightRender.setDisplayChartValues(true);
 			nightRender.setPointStyle(PointStyle.DIAMOND);
 			nightRender.setFillPoints(true);
 			renderer.addSeriesRenderer(nightRender);
 			onProgressUpdate(90);
 			// show
 			GraphicalView chart = LineChartFactory.getLineChartView(layout.getContext(), dataSet, renderer);
 			chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
 					LinearLayout.LayoutParams.FILL_PARENT));
 			layout.addView(chart);
 			onProgressUpdate(100);
 		}
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		Activity parent = getParent();
 		if (parent != null) {
 			parent.onBackPressed();
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 }
