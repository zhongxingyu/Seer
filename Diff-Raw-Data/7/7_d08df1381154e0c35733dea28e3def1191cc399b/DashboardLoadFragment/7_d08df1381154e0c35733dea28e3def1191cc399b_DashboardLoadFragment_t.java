 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.friedran.appengine.dashboard.gui;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.util.LruCache;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AnimationUtils;
 import android.widget.*;
 import com.friedran.appengine.dashboard.R;
 import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;
 
 import java.io.InputStream;
 
 public class DashboardLoadFragment extends Fragment implements AdapterView.OnItemSelectedListener {
 
     public static final String CHART_URL_BACKGROUND_COLOR_SUFFIX = "&chf=bg,s,E8E8E8";
     public static final int CHART_HEIGHT_PIXELS = 240;
     public static final int CHART_MAX_WIDTH_PIXELS = 1000;
     private AppEngineDashboardClient mAppEngineClient;
     private String mApplicationId;
     private ChartAdapter mChartGridAdapter;
     private DisplayMetrics mDisplayMetrics;
     private LruCache<String, Bitmap> mChartsMemoryCache;
 
     int mDisplayedTimeID;
 
     public DashboardLoadFragment(AppEngineDashboardClient client, String applicationID) {
         mAppEngineClient = client;
         mApplicationId = applicationID;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.load_fragment, container, false);
 
         setSpinnerWithItems(layout, R.array.load_time_options, R.id.load_chart_time_spinner);
         mDisplayedTimeID = 0;
 
         mDisplayMetrics = new DisplayMetrics();
         getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
 
         mChartGridAdapter = new ChartAdapter(getActivity());
         GridView chartsGridView = (GridView) layout.findViewById(R.id.load_charts_grid);
         chartsGridView.setAdapter(mChartGridAdapter);
 
         mChartsMemoryCache = initChartsMemoryCache();
 
         return layout;
     }
 
     private LruCache<String, Bitmap> initChartsMemoryCache() {
         final int maxMemoryInKBs = (int) (Runtime.getRuntime().maxMemory() / 1024);
         final int cacheSize = maxMemoryInKBs / 10;
 
         return new LruCache<String, Bitmap>(cacheSize) {
             @Override
             protected int sizeOf(String key, Bitmap bitmap) {
                 // Measured in kilobytes
                 return bitmap.getByteCount() / 1024;
             }
         };
     }
 
     private Spinner setSpinnerWithItems(LinearLayout layout, int optionsListResourceID, int spinnerResourceID) {
         Spinner spinner = (Spinner) layout.findViewById(spinnerResourceID);
 
         // Set options list
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                 optionsListResourceID, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
 
         // Set listener
         spinner.setOnItemSelectedListener(this);
 
         return spinner;
     }
 
     @Override
     public void onResume() {
         super.onResume();
     }
 
     /**
      * Called when the time spinner is selected
      */
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         if (position != mDisplayedTimeID) {
             Log.i("DashboardLoadFragment", "Time option selected: " + mDisplayedTimeID + " ==> " + position);
             mDisplayedTimeID = position;
             mChartGridAdapter.notifyDataSetChanged();
         }
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> parent) {
         // Do Nothing
     }
 
     private class ChartAdapter extends BaseAdapter {
         private Context mContext;
         private String[] mAppEngineMetrics;
 
         public ChartAdapter(Context c) {
             mContext = c;
             mAppEngineMetrics = getResources().getStringArray(R.array.load_metric_options);
         }
 
         // Disables highlighting items
         @Override
         public boolean areAllItemsEnabled()
         {
             return false;
         }
 
         // Disables highlighting items
         @Override
         public boolean isEnabled(int position)
         {
             return false;
         }
 
         @Override
         public int getCount() {
             return mAppEngineMetrics.length;
         }
 
         @Override
         public Object getItem(int position) {
             return mAppEngineMetrics[position];
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(int position, View chartView, ViewGroup parent) {
             LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             if (chartView == null) {
                 chartView = inflater.inflate(R.layout.load_charts_grid_item, null);
             }
 
             TextView textView = (TextView) chartView.findViewById(R.id.load_chart_title);
             textView.setText(mAppEngineMetrics[position]);
 
             Bitmap chartImage = getChartImageFromCache(position, mDisplayedTimeID);
             if (chartImage != null) {
                 updateChartImage(chartView, chartImage);
 
             } else {
                 // Load the image asynchronously, while displaying the progress animation
                 switchChartToProgress(chartView);
                 executeGetAndDisplayChart(chartView, mDisplayedTimeID, position);
             }
 
             return chartView;
         }
     }
 
     private void executeGetAndDisplayChart(final View chartView, final int selectedTimeWindow, final int metricTypeID) {
         mAppEngineClient.executeGetChartUrl(mApplicationId, metricTypeID, selectedTimeWindow,
                 new AppEngineDashboardClient.PostExecuteCallback() {
                     @Override
                     public void run(Bundle result) {
                         if (!result.getBoolean(AppEngineDashboardClient.KEY_RESULT)) {
                             Log.e("DashboardLoadFragment", "GetChartURL has failed");
                             return;
                         }
 
                         String chartUrl = result.getString(AppEngineDashboardClient.KEY_CHART_URL);
                         chartUrl = chartUrl.replaceAll("chs=\\d+x\\d+",
                                 String.format("chs=%sx%s", Math.min(mDisplayMetrics.widthPixels, CHART_MAX_WIDTH_PIXELS), CHART_HEIGHT_PIXELS));
                         chartUrl += CHART_URL_BACKGROUND_COLOR_SUFFIX;
 
                        new ChartDownloadTask(getActivity(), chartView, selectedTimeWindow, metricTypeID, chartUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                     }
                 });
     }
 
     /** Downloads a chart image and displays it asynchronously */
     private class ChartDownloadTask extends AsyncTask<String, Void, Bitmap> {
         Context mContext;
         View mChartView;
         int mTimeWindowID;
         int mMetricTypeID;
         String mUrl;
 
         public ChartDownloadTask(Context context, View chartView, int timeWindowID, int metricTypeID, String url) {
             mContext = context;
             mChartView = chartView;
             mTimeWindowID = timeWindowID;
             mMetricTypeID = metricTypeID;
             mUrl = url;
         }
 
         @Override
         protected Bitmap doInBackground(String... params) {
             Bitmap decodedBitmap = null;
             try {
                Log.i("DashboardLoadFragment", String.format("Downloading chart (%s, %s) from: %s", mTimeWindowID, mMetricTypeID, mUrl));
                 InputStream in = new java.net.URL(mUrl).openStream();
                 decodedBitmap = BitmapFactory.decodeStream(in);
             } catch (Exception e) {
                 Log.e("Error", e.getMessage());
                 e.printStackTrace();
             }
 
             updateChartImageInCache(mMetricTypeID, mTimeWindowID, decodedBitmap);
 
             return decodedBitmap;
         }
 
         @Override
         protected void onPostExecute(Bitmap result) {
             updateChartImage(mChartView, result);
         }
     }
 
     private Bitmap getChartImageFromCache(int metricID, int windowID) {
         return mChartsMemoryCache.get(String.format("%s.%s", metricID, windowID));
     }
 
     private void updateChartImageInCache(int metricID, int windowID, Bitmap image) {
         mChartsMemoryCache.put(String.format("%s.%s", metricID, windowID), image);
     }
 
     private void switchChartToProgress(View chartView) {
         ViewSwitcher switcher = (ViewSwitcher) chartView.findViewById(R.id.load_chart_switcher);
         if (switcher.getDisplayedChild() != 0) {
             switcher.showPrevious();
         }
     }
 
     private void updateChartImage(View chartView, Bitmap image) {
         ImageView chartImageView = (ImageView) chartView.findViewById(R.id.load_chart_image);
         chartImageView.setImageBitmap(image);
 
         ViewSwitcher switcherView = (ViewSwitcher) chartView.findViewById(R.id.load_chart_switcher);
         if (switcherView.getDisplayedChild() != 1) {
             switcherView.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fadein));
             switcherView.showNext();
         }
     }
 }
