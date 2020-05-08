 package com.TeamKeyLimePie.KeyLifePlanner;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class StoreActivity extends Activity implements OnClickListener{
 	MediaPlayer backgroundShop;
 	public int bank;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_store);
 
 		backgroundShop = MediaPlayer.create(StoreActivity.this, R.raw.shop);
 		backgroundShop.setLooping(true);
 		backgroundShop.start();
 		
 		bank = ((GlobalApp)getApplication()).getmoney();
 
 		GridView gridview = (GridView) findViewById(R.id.gridview);
 		gridview.setAdapter(new ImageAdapter(this));
 
 		
 		
 		gridview.setOnItemClickListener(new OnItemClickListener() 
 		{
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
 			{
 				ArrayList<Item> i = new ArrayList<Item>();
 				Item i1 = new Item(100, false);
 				Item i2 = new Item(250, false);
 				Item i3 = new Item(150, false);
 				Item i4 = new Item(200, false);
 				Item i5 = new Item(200, false);
 				Item i6 = new Item(175, false);
 				Item i7 = new Item(150, false);
 				Item i8 = new Item(300, false);
 				Item i9 = new Item(250, false);
<<<<<<< HEAD
=======
				Item i10 = new Item(150, false);
>>>>>>> fixed another bug in avatar store :P
 				i.add(i1);
 				i.add(i2);
 				i.add(i3);
 				i.add(i4);
 				i.add(i5);
 				i.add(i6);
 				i.add(i7);
 				i.add(i8);
 				i.add(i9);
<<<<<<< HEAD
=======
				i.add(i10);
>>>>>>> fixed another bug in avatar store :P
 				
 				bank = ((GlobalApp)getApplication()).getmoney();
 				
 				//checks if the item is already bought
 				if(i.get(position).isBought())
 				{
 					Toast.makeText(StoreActivity.this, "Already bought!", Toast.LENGTH_SHORT).show();
 				}
 				//checks if enough money
 				else if((bank - i.get(position).getValue() < 0))
 				{
 					Toast.makeText(StoreActivity.this, "Not enough money!", Toast.LENGTH_SHORT).show();
 				}
 				//decreases amount of money in wallet
 				else
 				{
 					((GlobalApp)getApplication()).decmoney(i.get(position).getValue());
 					Toast.makeText(StoreActivity.this, "Bought item!", Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 		
 		
 		
 
 		TextView wallet = (TextView)findViewById(R.id.wallet);
 		wallet.setText("Amount of Coins: " + bank);
 	}
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		backgroundShop.stop();
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		backgroundShop.start();
 	}
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
