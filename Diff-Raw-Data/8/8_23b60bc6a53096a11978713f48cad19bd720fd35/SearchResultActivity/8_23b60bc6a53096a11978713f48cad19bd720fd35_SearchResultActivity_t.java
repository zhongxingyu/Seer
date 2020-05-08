 package ua.avtopoisk.activites;
 
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ListView;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.Extra;
 import domain.Car;
 import parsers.AvtopoiskParser;
 import ua.avtopoisk.CarAdapter;
 import ua.avtopoisk.R;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Search result activity. List of cars returned by parser
  *
  * @author ibershadskiy <a href="mailto:iBersh20@gmail.com">Ilya Bershadskiy</a>
  * @since 12.10.12
  */
 
 @EActivity(R.layout.search_result)
 public class SearchResultActivity extends ListActivity {
     private ProgressDialog progressDialog;
 
    @Extra(SearchActivity.BRAND_ID_KEY)
     int brandId;
 
    @Extra(SearchActivity.MODEL_ID_KEY)
     int modelId;
 
    @Extra(SearchActivity.REGION_ID_KEY)
     int regionId;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         progressDialog = ProgressDialog.show(this, "", getString(R.string.dlg_progress_data_loading), true);
     }
 
     @AfterViews
     protected void init() {
         new SearchAsyncTask().execute(getListView());
     }
 
     @Override
     protected void onDestroy() {
         if (progressDialog != null) {
             progressDialog.dismiss();
         }
         super.onDestroy();
     }
 
     private class SearchAsyncTask extends AsyncTask<ListView, Void, Void> {
         private static final String HEADER = "header";
         private static final String PRICE = "price";
         private static final String IMAGE = "image";
         private static final String CITY = "city";
         private static final String DATE_POSTED = "datePosted";
         private static final String ENGINE_DESC = "engineDesc";
 
         private ListView listView;
         private ArrayList<HashMap<String, Object>> items;
 
         @Override
         protected Void doInBackground(ListView... lv) {
             this.listView = lv[0];
             AvtopoiskParser parser = new AvtopoiskParser();
 
             ArrayList<Car> cars = null;
             try {
                 cars = parser.parse(brandId, modelId, regionId);
             } catch (IOException e) {
                 Log.e(listView.getContext().getString(R.string.app_name), e.getMessage());
             }
 
             if (cars == null) {
                 return null;
             }
 
             items = new ArrayList<HashMap<String, Object>>();
 
             for (Car car : cars) {
                 final HashMap<String, Object> hm = new HashMap<String, Object>();
                 String header = car.getYear() + "' " + car.getBrand() + " " + car.getModel();
                 hm.put(HEADER, header);
                 hm.put(PRICE, Long.valueOf(car.getPrice() / 100).toString() + " $");
                 hm.put(CITY, car.getCity());
                 hm.put(DATE_POSTED, car.getDatePosted());
                 hm.put(ENGINE_DESC, car.getEngineDesc());
 
                 URL url;
                 Bitmap bmp = null;
                 if (!car.getImageUrl().contains("no_foto")) {     //if no photo default image will be loaded in adapter
                     try {
                         url = new URL(car.getImageUrl());
                         bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                     } catch (Exception e) {
                         Log.e(listView.getContext().getString(R.string.app_name), e.getMessage());
                     }
                 }
                 hm.put(IMAGE, bmp);
                 items.add(hm);
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Void result) {
             CarAdapter adapter = new CarAdapter(listView.getContext(),
                     items, R.layout.cars_list_item,
                     new String[]{HEADER, PRICE, IMAGE, CITY, DATE_POSTED, ENGINE_DESC},
                     new int[]{R.id.car_info_header, R.id.price, R.id.img, R.id.city, R.id.date_posted, R.id.engine_desc},
                     IMAGE);
 
             listView.setAdapter(adapter);
             progressDialog.dismiss();
         }
     }
 
 
 }
