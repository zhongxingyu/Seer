 package com.gui_tab_inventory.pos;
 
 import com.database.pos.InventoryDatabase;
 import com.inventory_record.pos.InventoryRecord;
 import com.inventory_record.pos.InventoryRecordController;
 import com.rtt_ku.pos.R;
 import com.rtt_ku.pos.main_activity;
 import com.rtt_ku.pos.R.id;
 import com.rtt_ku.pos.R.layout;
 import com.rtt_store.pos.StoreController;
 
 import Inventory.Product;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * edit information of item in inventory.
  * 
  * @author rtt team
  * 
  */
 public class Inventory_editItem extends Activity {
 	public static StoreController sCT;
 	public static InventoryRecordController iRC;
 
 	private EditText quantityTextEdit;
 	private EditText costTextEdit;
 	private TextView product_code_textview;
 	private TextView name_textview;
 	private TextView old_quantity_textview;
 	private Button okButton;
 	private Button removeButton;
 
 	private Intent intend;
 	private Product p;
 	private String product_code;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.inventory_edit_item);
 
 		// call Store Controller
 		InventoryDatabase myDb = new InventoryDatabase(this);
 		myDb.getWritableDatabase();
 		sCT = new StoreController(myDb);
 		InventoryRecord iR = (new InventoryRecord(this));
 		iR.getWritableDatabase();
 		iRC = InventoryRecordController.getInstance(iR);
 
 		// get product
 		intend = getIntent();
 		product_code = intend.getExtras().getString("pc");
 		p = sCT.getProduct(product_code);
 
 		initWidget();
 		setTextView();
 		addButton();
 	}
 
 	// add function on click.
 	private void addButton() {
 		okButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(final View v) {
 				try {
 					int oldQuan = p.getQuantity();
 					final int diff = Integer.parseInt(quantityTextEdit
 							.getText().toString());
 					int newQuan = oldQuan + diff;
					int costt = Integer.parseInt(costTextEdit.getText().toString());
 					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 							Inventory_editItem.this);
 
 					alertDialogBuilder.setTitle("change quantity");
 					alertDialogBuilder
 							.setMessage(
 									"Do you sure to change quantity <"
 											+ p.getProduct_Code() + "> from "
 											+ oldQuan + " to " + newQuan + "")
 							.setCancelable(false)
 							.setPositiveButton("Yes",
 									new DialogInterface.OnClickListener() {
 										public void onClick(
 												DialogInterface dialog, int id) {
 											sCT.addQuantity(product_code, diff);
 											iRC.addInventoryRecord(
 													product_code,
 													diff,
 													Integer.parseInt(costTextEdit
 															.getText()
 															.toString()));
 											// iRC.addInventoryRecord(product_code,diff,null);
 											onBackPressed();
 										}
 									})
 							.setNegativeButton("No",
 									new DialogInterface.OnClickListener() {
 										public void onClick(
 												DialogInterface dialog, int id) {
 											dialog.cancel();
 										}
 									});
 					AlertDialog alertDialog = alertDialogBuilder.create();
 					alertDialog.show();
 
 				} catch (Exception e) {
 					final AlertDialog.Builder dialog_not = new AlertDialog.Builder(
 							Inventory_editItem.this);
 
 					dialog_not.setTitle("Warning!!!");
 					dialog_not.setMessage("Plese enter amount of Add Quantity and Cost");
 					dialog_not.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									// TODO Auto-generated method stub
 								}
 							}).show();
 				}
 			}
 		});
 
 		removeButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(final View v) {
 
 				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 						Inventory_editItem.this);
 
 				alertDialogBuilder.setTitle("Remove product");
 				alertDialogBuilder
 						.setMessage(
 								"Do you sure to remove product <"
 										+ p.getProduct_Code() + ">")
 						.setCancelable(false)
 						.setPositiveButton("Yes",
 								new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog,
 											int id) {
 										sCT.removeProduct(p.getProduct_Code());
 										startActivity(new Intent(
 												v.getContext(),
 												main_activity.class));
 									}
 								})
 						.setNegativeButton("No",
 								new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog,
 											int id) {
 										dialog.cancel();
 									}
 								});
 				AlertDialog alertDialog = alertDialogBuilder.create();
 				alertDialog.show();
 
 			}
 		});
 	}
 
 	// set text of information.
 	private void setTextView() {
 		product_code_textview.setText(p.getProduct_Code());
 		name_textview.setText(p.getName());
 		old_quantity_textview.setText(p.getQuantity() + "");
 	}
 
 	// view matching.
 	private void initWidget() {
 		quantityTextEdit = (EditText) findViewById(R.id.inventoryEdit_quantity_edittext);
 		costTextEdit = (EditText) findViewById(R.id.inventoryEdit_cost_edittext);
 		product_code_textview = (TextView) findViewById(R.id.inventoryEdit_productcode_textview);
 		name_textview = (TextView) findViewById(R.id.inventoryEdit_name_textview);
 		old_quantity_textview = (TextView) findViewById(R.id.inventoryEdit_oldquantity_textview);
 		okButton = (Button) findViewById(R.id.inventoryEdit_ok_button);
 		removeButton = (Button) findViewById(R.id.inventoryEdit_remove_button);
 	}
 
 }
