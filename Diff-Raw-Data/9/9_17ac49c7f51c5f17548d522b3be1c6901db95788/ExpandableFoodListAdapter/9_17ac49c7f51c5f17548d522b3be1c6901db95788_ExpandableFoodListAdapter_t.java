 package com.chips.adapters;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.chips.R;
 import com.chips.dataclient.DataPushClient;
 import com.chips.datarecord.FoodRecord;
 import com.chips.user.PersistentUser;
 
 public class ExpandableFoodListAdapter extends BaseExpandableListAdapter {
     private Context context;
     private List<FoodRecord> items;
     
     public ExpandableFoodListAdapter(Context newContext, 
             List<FoodRecord> newItems) {
         context = newContext;
         items = newItems;
     }
     
     @Override
     public Object getChild(int groupPosition, int childPosition) {
         return items.get(groupPosition);
     }
 
     @Override
     public long getChildId(int groupPosition, int childPosition) {
         return childPosition;
     }
 
     @Override
     public View getChildView(int groupPosition, int childPosition, 
             boolean isLastChild, View convertView, ViewGroup parent) {
         FoodRecord food = (FoodRecord) getChild(groupPosition, childPosition);
         if (convertView == null) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.food_child, null);
         }
         
         setTextViewString(convertView, R.id.childCaloriesDisplay, 
                           "" + food.getCalories());
         setTextViewString(convertView, R.id.childCarbohydratesDisplay, 
                 "" + food.getCarbohydrates());
         setTextViewString(convertView, R.id.childProteinDisplay, 
                 "" + food.getProtein());
         setTextViewString(convertView, R.id.childFatDisplay, 
                 "" + food.getFat());
         EditText quantityEditText 
             = (EditText) convertView.findViewById(R.id.childQuantityEditText);
         quantityEditText.setText(food.getQuantity() + "");
         Button updateButton 
             = (Button) convertView.findViewById(R.id.childUpdateButton);
        updateButton.setFocusable(false);
         updateButton.setOnClickListener(
                 new InventoryFoodUpdateOnClickListener(
                         groupPosition, childPosition, quantityEditText));
         
         return convertView;
     }
     
     private void setTextViewString(View outerView, int textViewId, 
             String newText) {
         TextView childView 
             = (TextView) outerView.findViewById(textViewId);
         childView.setText(newText);
     }
 
     @Override
     public int getChildrenCount(int arg0) {
         return 1;
     }
 
     @Override
     public Object getGroup(int groupPosition) {
         return items.get(groupPosition);
     }
 
     @Override
     public int getGroupCount() {
         return items.size();
     }
 
     @Override
     public long getGroupId(int groupPosition) {
         return groupPosition;
     }
 
     @Override
     public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
             ViewGroup parent) {
         FoodRecord food = (FoodRecord) getGroup(groupPosition);
         
         String group = food.toString();
         if (convertView == null) {
             LayoutInflater infalInflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = infalInflater.inflate(R.layout.food_group, null);
         }
         TextView groupName = (TextView) convertView.findViewById(R.id.groupName);
         groupName.setText(group);
         
         setTextViewString(convertView, R.id.groupQuantity, 
                 food.getQuantity() + "g");
         return convertView;
     }
 
     @Override
     public boolean hasStableIds() {
         return true;
     }
 
     @Override
     public boolean isChildSelectable(int arg0, int arg1) {
         return true;
     }
     
     private class InventoryFoodUpdateOnClickListener 
             implements OnClickListener, Observer {
         private static final String QUANTITY_UPDATE_URL 
             = "http://cs110chips.phpfogapp.com/index.php/mobile/"
               + "set_quantity_of_food_in_inventory";
         
         public InventoryFoodUpdateOnClickListener(int groupPosition, 
                 int childPosition, EditText associatedQuantityEditText) {
             buttonGroupPosition = groupPosition;
             buttonChildPosition = childPosition;
             quantityEditText = associatedQuantityEditText;
             
             pushClient = new DataPushClient();
             pushClient.addObserver(this);
         }
 
         @Override
         public void onClick(View v) {
             FoodRecord food = (FoodRecord) getChild(buttonGroupPosition, 
                     buttonChildPosition);
             food.setQuantity(Integer.parseInt(
                     quantityEditText.getText().toString().trim()));
             
             ArrayList<String> quantityUpdateArguments = new ArrayList<String>();
             quantityUpdateArguments.add(PersistentUser.getSessionID());
             quantityUpdateArguments.add(food.getId() + "");
             quantityUpdateArguments.add(food.getQuantity() + "");
             
             pushClient.setURL(QUANTITY_UPDATE_URL, quantityUpdateArguments);
             pushClient.refreshClient();
             
             notifyDataSetChanged();
         }
         
         @Override
         public void update(Observable observable, Object data) {
             if (!pushClient.lastCompletedPushSuccessful()) {
                 Toast.makeText(context, "Update Error", 
                                Toast.LENGTH_LONG).show();
             }
         }
         
         private DataPushClient pushClient;
         private int buttonGroupPosition;
         private int buttonChildPosition;
         private EditText quantityEditText;
     }
 }
