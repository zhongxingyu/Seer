 package ru.inventos.yum;
 
 import java.util.Locale;
 
 import org.holoeverywhere.widget.Spinner;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class Checkout extends Activity implements DeliveryPriceReceiver {
 	private Cart mCart;
 	private Spinner mTimeSpinner;
 	private ImageButton mButton;
 	private ImageButton mOrderButton;
 	private TextView mPrice;
 	private TextView mDeliveryPrice;
 	private TextView mDescription;
 	private ImageView mRuble;
 	private ImageView mGift;
 	private Resources mResources;
 	private NetStorage mNetStorage;
 	ArrayAdapter<CharSequence> mTimeAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_checkout);
 		mResources = this.getResources();
 		mCart = new Cart();
 		mNetStorage = new NetStorage(this);
 		makeActionBar();
 		makeTimeSpinner();
 		findViews();
 		waitServerAnswer();
 		mNetStorage.getDeliveryPrice(this);		
 	}
 	
 	private void makeActionBar() {		
 		TextView tv = (TextView) findViewById(R.id.checkout_actionbar_order_count);
 		tv.setText(Integer.toString(mCart.getCount()));
 	}	
 	
 	private void makeTimeSpinner() {
 		mTimeSpinner = (Spinner) findViewById(R.id.checkout_time);
 		mTimeAdapter = ArrayAdapter.createFromResource(this,
 					R.array.checkout_times, R.layout.checkout_spinner_item);
 		mTimeAdapter.setDropDownViewResource(R.layout.dropdown_item);
 		mTimeSpinner.setAdapter(mTimeAdapter);
 	}	
 	
 	public void onOrderBtnClick(View v) {
		v.setClickable(false);
 		Intent intent = new Intent(this, Order2.class);
 		startActivityForResult(intent, Consts.ORDER2_REQUEST);
 	}
 	
 	private void findViews() {
 		mPrice = (TextView) findViewById(R.id.checkout_price);
 		mDeliveryPrice = (TextView) findViewById(R.id.checkout_delivery);
 		mButton = (ImageButton) findViewById(R.id.checkout_btn);
 		mDescription = (TextView) findViewById(R.id.checkout_delivery_description);
 		mRuble = (ImageView) findViewById(R.id.checkout_ruble);
 		mOrderButton = (ImageButton) findViewById(R.id.checkout_actionbar_order_btn);
 		mGift = (ImageView) findViewById(R.id.checkout_image);
 	}
 	
 	private void waitServerAnswer() {
 		mPrice.setText(R.string.checkout_wait_text);
 		mRuble.setVisibility(ImageView.INVISIBLE);
 		mDescription.setText("");
 		mDeliveryPrice.setText(R.string.checkout_wait_text);
 		mButton.setEnabled(false);
 		mOrderButton.setEnabled(false);
 		mGift.setVisibility(ImageView.INVISIBLE);
 	}
 	
 	private void startReport() {
 		Intent  intent = new Intent(this, Report.class);
 		int position = mTimeSpinner.getSelectedItemPosition(); 
 		intent.putExtra(Consts.CHECKOUT_TIME, mTimeAdapter.getItem(position));
 		startActivity(intent);
 	}
 	
 	public void onCheckoutBtnClick(View v) {
		v.setClickable(false);
 		startReport();
 		finish();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mOrderButton.setClickable(true);
 		if (requestCode == Consts.ORDER2_REQUEST && data != null) {
 			int answer = data.getIntExtra(Consts.ORDER2_ANSWER, -1);
 			if (answer == Consts.ORDER2_CHECKOUT_REQUEST) {
 				startReport();
 			}				
 			finish();
 		}
 	}
 	
 	@Override
 	public void receiveDeliveryPrice(int price, int freePrice) {
 		float lunchPrice = mCart.getTotalPrice();
 		String str;
 		if (lunchPrice >= freePrice) {
 			str = ' ' + String.format(Locale.US, "%.2f", lunchPrice) + ' ';
 			mPrice.setText(str);
 			mDeliveryPrice.setText(R.string.checkout_delivery_free);
 			str = Integer.toString(freePrice); 
 			str = mResources.getString(R.string.checkout_delivery_free_description) 
 						+ ' ' + str + ' ' + Consts.RU_SYMBOL + ')';
 			mDescription.setText(str);
 			mGift.setImageResource(R.drawable.icon_gift);
 			mGift.setVisibility(ImageView.VISIBLE);
 		}
 		else {
 			str = ' ' + String.format(Locale.US, "%.2f", lunchPrice + price) + ' ';
 			mPrice.setText(str);
 			str = ' ' + Integer.toString(price) + ' ' + Consts.RU_SYMBOL;
 			mDeliveryPrice.setText(str);
 			mDescription.setText(R.string.checkout_delivery_unfree_description);
 			mGift.setImageResource(R.drawable.icon_ruble_black);
 			mGift.setVisibility(ImageView.VISIBLE);
 		}		
 		mRuble.setVisibility(ImageView.VISIBLE);
 		mButton.setEnabled(true);
 		mOrderButton.setEnabled(true);
 	}
 }
