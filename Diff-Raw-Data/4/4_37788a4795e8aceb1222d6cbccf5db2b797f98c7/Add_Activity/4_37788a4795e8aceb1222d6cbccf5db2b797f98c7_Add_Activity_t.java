 package com.rtt_ku.pos;
 
 import com.database.pos.Database;
 import com.rtt_store.pos.StoreController;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 /**
  * activity on add button.
  * @author Suttanan
  *
  */
 public class Add_Activity extends Activity {
 	 StoreController sCT ;
 	 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_layout);
 		
 		// view matching
 		Button okButton = (Button)findViewById(R.id.OK_button);
 		Button cancelButton = (Button)findViewById(R.id.cancel_button);
 		
         Database myDb = new Database(this);
         myDb.getWritableDatabase();
         sCT = new StoreController(myDb);;
         
 		
         // add function on click at OK button.
 		okButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				EditText pc = (EditText)findViewById(R.id.pc_text);
 				EditText n = (EditText)findViewById(R.id.name_text);
 				EditText quan = (EditText)findViewById(R.id.quantity_text);
 				EditText p =(EditText)findViewById(R.id.price_text);
 				
 				
 				
 				// toString
 				
 				String product_code = pc.getText().toString();
 				String name = n.getText().toString();
 				String quantityString = quan.getText().toString();
 				String priceString = p.getText().toString();
 				
 				if (!(product_code.equals("") || name.equals("") || quantityString.equals("") || priceString.equals("")))
 				{
 				if (name.equals("")) name = "empty";
 				int quantity;
 				quantity = Integer.parseInt(quan.getText().toString());
 				int price;
 				price = Integer.parseInt(p.getText().toString());
 				
 				// when click add button
 				insertProduct(product_code, name, quantity, price,"","","","","","","");
 				}
 				else 
 				{
 					Toast.makeText(Add_Activity.this,"miss some necessary details", Toast.LENGTH_SHORT).show();
 				}
 			}
 			
 		});
 		
 		// add function on click at cancel button.
 		cancelButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
				//setContentView(R.layout.activity_main);
				startActivity(new Intent(Add_Activity.this, main_activity.class));
 			}
 			
 		});
 	}
 	
 	/**
 	 * insert product on list view.
 	 * @param product_code  code of each product.
 	 * @param name	name of product.
 	 * @param quantity	quantity of product.
 	 * @param price	price of product.
 	 */
 	public void insertProduct(String productCode, String name, int quantity, int price, String type, String date,
 			String barcode, String picture, String lastedit, String status, String stage)
 	{
 		int temp = sCT.addProduct(productCode, name, quantity, price,type,date,barcode,picture,lastedit,status,stage);
 		if (temp == 1) Toast.makeText(Add_Activity.this,"added <" + productCode + "> " + name + " Complete.", Toast.LENGTH_SHORT).show();
 		else if (temp == -1) Toast.makeText(Add_Activity.this, productCode + " is already exist.", Toast.LENGTH_SHORT).show();
 	}
 }
