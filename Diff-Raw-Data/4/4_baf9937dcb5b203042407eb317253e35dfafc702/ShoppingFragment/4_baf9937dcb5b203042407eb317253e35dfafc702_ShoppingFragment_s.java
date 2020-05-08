 package com.boh.flatmate;
 
 import com.boh.flatmate.FlatMate.ConnectionExchanger;
 import com.boh.flatmate.FlatMate.FlatDataExchanger;
 import com.boh.flatmate.FlatMate.contextExchanger;
 import com.boh.flatmate.connection.ShopItem;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.view.animation.Animation.AnimationListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ShoppingFragment extends Fragment {
 
 	private ViewGroup c;
 	ShoppingListFragment shoppingList;
 	private int addOpen = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public void setUserVisibleHint(boolean isVisibleToUser) {
 	super.setUserVisibleHint(isVisibleToUser);
 
 	if (isVisibleToUser == true) {
 		new refreshItems().execute();
 	}
 	else if (isVisibleToUser == false) {  }
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v1 = inflater.inflate(R.layout.shopping_page, container, false);
 		c = container;
 		return v1;
 	}
 	
 	@Override
 	public void onResume(){
 		GCMIntentService.resetCount();
 		super.onResume();
 		new refreshItems().execute();
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
 		shoppingList = new ShoppingListFragment();
 		ft.add(R.id.list2, shoppingList,"shopping_fragment");
 		//ft.addToBackStack(null);
 		ft.commit();
 		TextView toBuy = (TextView)c.findViewById(R.id.itemsToBuy);
 		toBuy.setText(FlatDataExchanger.flatData.shopItemsToBuy() + " items to buy");
 		ImageButton addButton = (ImageButton)c.findViewById(R.id.addButton);
 		addButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View view) {
 				View addItemBox = c.findViewById(R.id.addItem);
 				ImageButton addButton = (ImageButton)c.findViewById(R.id.addButton);
 				if(addOpen == 0){
 					addOpen = 1;
 					addButton.setImageResource(R.drawable.cross);
 					int offset = addItemBox.getHeight();
 					TranslateAnimation anim = new TranslateAnimation( 0, 0 , 0, offset );
 					anim.setDuration(250);
 					anim.setAnimationListener(new AnimationListener() {
 						@Override
 						public void onAnimationEnd(Animation animation) {
 							TranslateAnimation anim = new TranslateAnimation( 0, 0, 0, 0);
 							anim.setDuration(1);
 							c.findViewById(R.id.list2).startAnimation(anim);
 							c.findViewById(R.id.addItem).setVisibility(View.VISIBLE);
 						}
 						@Override
 						public void onAnimationRepeat(Animation animation) { }
 						@Override
 						public void onAnimationStart(Animation animation) { }
 					});
 					c.findViewById(R.id.list2).startAnimation(anim);
 				}else{
 					addOpen = 0;
 					addButton.setImageResource(R.drawable.new_shopping);
 					int offset = addItemBox.getHeight();
 					addItemBox.setVisibility(View.GONE);
 					TranslateAnimation anim = new TranslateAnimation( 0, 0 , offset, 0 );
 					anim.setDuration(250);
 					c.findViewById(R.id.list2).startAnimation(anim);
 				}
 			}
 		});
 		
 		ImageButton addShoppingButton = (ImageButton)c.findViewById(R.id.AddListItem);
 		addShoppingButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View view) {
 				EditText textInput = (EditText)c.findViewById(R.id.addShoppingItem);
 				Editable inputText = textInput.getText();
 				if(inputText.toString() == null || inputText.toString().length() == 0) {
 					Toast toast = Toast.makeText(contextExchanger.context, "Please enter valid item!", Toast.LENGTH_SHORT);
 					toast.show();
 				} else {
 					int forFlat = 0;
 					RadioGroup radioButtons = (RadioGroup)c.findViewById(R.id.radioGroup1);
 					RadioButton forMeRadioButton = (RadioButton)radioButtons.findViewById(R.id.radio0);
 					if(forMeRadioButton.isChecked()) forFlat = 0;
 					else forFlat = 1;
 					ShopItem item = new ShopItem(inputText.toString(),forFlat);
 					item.addItem();
 					textInput.setText("");
 				}
 			}
 		});
 	}
 	
 	public static void updateToBuy(){
 		TextView toBuy = (TextView)((Activity) contextExchanger.context).findViewById(R.id.itemsToBuy);
 		toBuy.setText(FlatDataExchanger.flatData.shopItemsToBuy() + " items to buy");
 	}
 	
 	public class refreshItems extends AsyncTask<Void,Void,Void> {
 		protected Void doInBackground(Void... item) {
 			FlatDataExchanger.flatData.updateData(ConnectionExchanger.connection.getMyFlat());
 			return null;
 		}
 
 		protected void onPostExecute(Void result) {
 			ShoppingListFragment.mAdapter.notifyDataSetChanged();
 			updateToBuy();
 		}
 	}
 
 }
