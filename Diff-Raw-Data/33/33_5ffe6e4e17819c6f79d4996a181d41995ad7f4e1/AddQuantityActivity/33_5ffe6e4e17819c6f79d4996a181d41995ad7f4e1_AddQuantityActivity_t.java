 package com.thoughtworks.hp.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import com.thoughtworks.hp.R;
 import com.thoughtworks.hp.datastore.ShoppingListProductTable;
 import com.thoughtworks.hp.models.ShoppingList;
 
 public class AddQuantityActivity extends Activity {
 
     EditText editText;
     private long productId;
     private long shoppingListId;
     private ShoppingListProductTable shoppingListProductTable;
 
     @Override
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.update_quantity);
         shoppingListProductTable=new ShoppingListProductTable();
         productId = getIntent().getLongExtra("product_id", 1L);
         shoppingListId = getIntent().getLongExtra("Shopping_list_id", 0L);
     }
 
     private int getValue() {
         editText = (EditText) findViewById(R.id.quantity_value);
         int quantity = Integer.parseInt(editText.getText().toString());
         if(quantity < 0)
             quantity = 0;
         return quantity;
     }
     public void incrementQuantity(View view) {
         int quantity = getValue() +1;
         editText.setText(String.valueOf(quantity));
     }
 
     public void decrementQuantity(View view) {
         EditText editText = (EditText) findViewById(R.id.quantity_value);
         int quantity = getValue()!=0 ? getValue()-1 : 0;
         editText.setText(String.valueOf(quantity));
     }
 
     @Override
     public void onBackPressed() {
         int quantity = getValue();
         shoppingListProductTable.updateQuantityForProduct(productId,quantity);
//        Intent intent = new Intent(AddQuantityActivity.this, AddProductActivity.class);
        Intent intent=getIntent();
        setResult(RESULT_OK,intent);
         intent.putExtra(ShoppingList.SHOPPING_LIST_ID, shoppingListId);
        finish();
     }
 }
