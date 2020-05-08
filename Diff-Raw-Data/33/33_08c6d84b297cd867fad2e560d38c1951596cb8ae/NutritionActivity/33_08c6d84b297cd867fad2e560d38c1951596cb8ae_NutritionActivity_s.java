 package org.xcube.nfc;
 
 import org.xcube.nfc.domain.NutritionTotals;
 import org.xcube.nfc.service.NutritionService;
 import org.xcube.nfc.service.NutritionServiceImpl;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.view.ViewGroup;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class NutritionActivity extends Activity {
 
     private static final String MAIN_LABEL = "Nutrition Info";
     private NutritionService nutritionService = new NutritionServiceImpl();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.nutrition);
     }
     @Override
     public void onResume() {
         super.onResume();
         ((ViewGroup) findViewById(R.id.main_table)).removeAllViews();
         setMainView();
     }
     private void setMainView() {
 
 
         TableLayout mainTable = (TableLayout) findViewById(R.id.main_table);
 
         NutritionTotals nutritionTotals = nutritionService.getTotal();
         mainTable.addView(getTableRow("Calories", nutritionTotals.getCalories().toString()));
         mainTable.addView(getTableRow("Carbohydrates", nutritionTotals.getCarbohydrate().toString()));
     }
 
     private TableRow getTableRow(String key, String value) {
         TableRow tableRow = new TableRow(this);
         TextView textKey = new TextView(this);
         textKey.setText(key);
         TextView textValue = new TextView(this);
         textValue.setText(value);
         tableRow.addView(textKey);
         tableRow.addView(textValue);
         
         return tableRow;
     }
 }
