 package com.vodismetka.activities;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.vodismetka.R;
 import com.vodismetka.sql.ReceiptDAO;
 import com.vodismetka.workers.ImageFactory;
 
 public class AddNewReceiptActivity extends Activity {
 
 	public static final String TAG = "AddNewReceiptActivity";
 	static final int DATE_DIALOG_ID = 0;
 	
 	private ImageView receiptPhoto;
 	private EditText dateText;
 	private EditText priceText;
 	private Button submit;
 	
 	private ReceiptDAO dbDao;
 	
 	private int extractedPrice;
 	private String extractedDate;
 	private String imgId;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_purchase);
 		
 		//set up a data access object
 		dbDao = new ReceiptDAO(getApplicationContext());
 		
 		//get references to the views
 		receiptPhoto = (ImageView) findViewById(R.id.newReceipt);
 		dateText = (EditText) findViewById(R.id.newReceiptDate);
 		priceText = (EditText) findViewById(R.id.newReceiptTotalAmount);
 		submit = (Button) findViewById(R.id.confirm);
 		
 		//get the calling intent so we can set the initially recognized price and date
 		Intent callingIntent = getIntent();
 		extractedPrice = callingIntent.getExtras().getInt(LaunchActivity.PRICE_KEY);
 		extractedDate = callingIntent.getExtras().getString(LaunchActivity.DATE_KEY);
		extractedDate = callingIntent.getExtras().getString(LaunchActivity.IMG_KEY);
 		
 		//set the initial price and date
 		priceText.setText(Integer.toString(extractedPrice));
 		dateText.setText(extractedDate);
 		
 		//load and set the image
 		receiptPhoto.setImageBitmap(ImageFactory.loadImage(ImageFactory.IMAGES_PATH + imgId));
 		
 		//view the full receipt
 		receiptPhoto.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent viewFullImg = new Intent(getApplicationContext(), ViewReceiptActivity.class);
 				viewFullImg.putExtra(LaunchActivity.IMG_KEY, imgId);
 				startActivity(viewFullImg);
 			}
 		});
 		
 		//remind the user to check and correct the price
 		priceText.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(getApplicationContext(), "Please verify the correct price of the receipt...", Toast.LENGTH_SHORT).show();
 			}
 		});
 		
 		//insert the new receipt in the database
 		submit.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) { 
 				
 				int rPrice = Integer.parseInt(priceText.getText().toString());
 				String rDate = dateText.getText().toString();
				String[] dateParts = extractedDate.split("(\\W{1})");
 	        	int rMonth = Integer.parseInt(dateParts[1]);
 	        	
 				dbDao.insertNewItem(rPrice, rDate, imgId, rMonth);
 			}
 		});
         
         //show a datePicker dialog to correct the date
         dateText.setOnTouchListener(new OnTouchListener(){ 
         	public boolean onTouch(View v, MotionEvent event) { 
         		if(v == dateText)
         			showDialog(DATE_DIALOG_ID);
         		return false;
         	}
         });
         
 	}
 	
 	@Override
     protected Dialog onCreateDialog(int id) {
         
 		DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
         		// onDateSet method
         	@Override
 			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
         		String date_selected = String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year);
         		//Toast.makeText(getApplicationContext(), "Selected Date is ="+date_selected, Toast.LENGTH_SHORT).show();
         		dateText.setText(date_selected);
         	}
         };
         
         int year = 2014;
         int month = 1;
         int day = 1;
         
         try{
         	String[] dateParts = extractedDate.split("(\\W{1})");
         	day = Integer.parseInt(dateParts[0]);
         	month = Integer.parseInt(dateParts[1]);
         	year = Integer.parseInt(dateParts[2]);
         }catch(Exception e) {
         	Toast.makeText(getApplicationContext(), "Sorry... The date of the receipt could not be recognized...", Toast.LENGTH_LONG).show();
         	year = 2014;
             month = 1;
             day = 1;
         }
         
         switch (id) {
         case DATE_DIALOG_ID:
             return new DatePickerDialog(this,  mDateSetListener,  year, month, day);
         }
         return null;
     }
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 	
 }
