 package org.xcube.nfc;
 
 import java.util.List;
 
 import org.xcube.nfc.domain.ItemInfo;
 import org.xcube.nfc.domain.ItemWithQuantity;
 import org.xcube.nfc.service.FridgeService;
 import org.xcube.nfc.service.FridgeServiceImpl;
 import org.xcube.nfc.service.NutritionService;
 import org.xcube.nfc.service.NutritionServiceImpl;
 import org.xcube.nfc.util.LayoutUtil;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class FridgeActivity extends Activity {
 
     private FridgeService fridgeService = new FridgeServiceImpl();
     private NutritionService nutritionService = new NutritionServiceImpl();
 
     private static final String COUNT_LABEL = "";
     private static final String ITEM_LABEL = "item";
     private static final String CALORIES_LABEL = "calories";
 
     private static final String TABLE_BODY_TAG = "tableBody";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
         setMainView();
     }
 
     @Override
     public void onResume() {
         super.onResume();
//        LayoutUtil.clearTaggedChildren(
//                (ViewGroup) findViewById(R.id.main_table), TABLE_BODY_TAG);
        ((ViewGroup) findViewById(R.id.main_table)).removeAllViews();
        setMainView();
         setItems();
     }
 
     private void setMainView() {
 
         setContentView(R.layout.fridge);
 
         TextView countLabel = (TextView) findViewById(R.id.heading_count_label);
         countLabel.setText(COUNT_LABEL);
 
         TextView itemLabel = (TextView) findViewById(R.id.heading_item_label);
         itemLabel.setText(ITEM_LABEL);
 
         TextView caloriesLabel = (TextView) findViewById(R.id.heading_calories_label);
         caloriesLabel.setText(CALORIES_LABEL);
     }
 
     private void setItems() {
 
         List<ItemWithQuantity> items = fridgeService.getFridge().getItems();
        System.out.println("--- number of items == " + items.size());
 
         TableLayout mainTable = (TableLayout) findViewById(R.id.main_table);
         for (ItemWithQuantity item : items) {
             mainTable.addView(getItemRow(item));
         }
     }
 
     public TableRow getItemRow(final ItemWithQuantity item) {
 
         TableRow tableRow = new TableRow(this);
         tableRow.setTag(TABLE_BODY_TAG);
         int quantity = item.getQuantity();
         tableRow.addView(getTextView(Integer.toString(quantity)));
         ItemInfo itemInfo = item.getItem().getInfo();
         TextView nameTextView = getTextView(itemInfo.getName());
         tableRow.addView(nameTextView);
         tableRow.addView(getTextView(itemInfo.getPer100g().getCalories()));
 
         Button eatButton = new Button(this);
         eatButton.setText("eat");
         tableRow.addView(eatButton);
 
         final Intent itemDetailsIntent = new Intent(this,
                 ItemDetailActivity.class);
         itemDetailsIntent.putExtra(ItemDetailActivity.ITEM_KEY,
                 itemInfo.getUpc());
         itemDetailsIntent.putExtra(ItemDetailActivity.BACK_KEY,
                 ItemDetailActivity.FROM_FRIDGE);
 
         /* add on click listener */
         nameTextView.setOnTouchListener(new OnTouchListener() {
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_UP) {
                     // do something here when the element is clicked
                     startActivity(itemDetailsIntent);
                 }
 
                 // true if the event was handled
                 // and should not be given further down to other views.
                 // if no other child view of this should get the event then
                 // return false
                 return true;
             }
         });
         /* eat button listener */
         eatButton.setOnTouchListener(new OnTouchListener() {
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_UP) {
                     // do something here when the element is clicked
                     nutritionService.addItem(item.getItem());
                     fridgeService.getFridge().removeItem(item.getItem());
                     onResume();
                 }
 
                 // true if the event was handled
                 // and should not be given further down to other views.
                 // if no other child view of this should get the event then
                 // return false
                 return true;
             }
         });
         return tableRow;
     }
 
     public TextView getTextView(String text) {
 
         TextView textView = new TextView(this);
         textView.setText(text);
         return textView;
     }
 }
