 package rpisdd.rpgme.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.items.Item;
 import rpisdd.rpgme.gamelogic.player.Player;
 import rpisdd.rpgme.popups.AnnoyingPopup;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.squareup.picasso.Picasso;
 
 public class ShopMenu extends ListFragment implements OnClickListener {
 
 	private List<Item> itemsInStock;
 
 	private int defaultPriceColor;
 
 	ImageButton buy;
 	ImageButton details;
 	View selectedItemSlot;
 	int selectedItemIndex;
 
 	TextView goldWidget;
 
 	// Load in the items that the shop stocks from external XML
 	public void setItems() {
 
 		itemsInStock = new ArrayList<Item>();
 		itemsInStock.add(Item.createItemFromName("Energy Potion"));
 		itemsInStock.add(Item.createItemFromName("Light Sword"));
 		itemsInStock.add(Item.createItemFromName("Cracked Plate"));
 		itemsInStock.add(Item.createItemFromName("Revival Potion"));
 	}
 
 	public ShopMenu() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 
 		selectedItemSlot = null;
 		selectedItemIndex = -1;
 
 		setItems();
 
 		View v = inflater.inflate(R.layout.shop_menu, container, false);
 
 		this.defaultPriceColor = android.R.color.secondary_text_dark;
 
 		buy = (ImageButton) v.findViewById(R.id.shopBuyButton);
 		buy.setOnClickListener(this);
 		details = (ImageButton) v.findViewById(R.id.shopDetailButton);
 		details.setOnClickListener(this);
 
 		Player p = Player.getPlayer();
 		goldWidget = (TextView) v.findViewById(R.id.shopGoldDisplay);
 		goldWidget.setText("" + p.getGold());
 
 		updateButtons();
 
		fillListView(v);

 		return v;
 	}
 
 	// Will enable or disable buttons, depending on the situation.
 	public void updateButtons() {
 		if (selectedItemSlot == null) {
 			buy.setEnabled(false);
 			buy.setVisibility(View.INVISIBLE);
 			details.setEnabled(false);
 			details.setVisibility(View.INVISIBLE);
 		} else {
 
 			if (this.itemsInStock.get(this.selectedItemIndex).getPrice() > Player
 					.getPlayer().getGold()) {
 				this.buy.setColorFilter(getResources().getColor(
 						R.color.disabled01));
 				buy.setEnabled(false);
 			} else {
 				this.buy.setColorFilter(null);
 				buy.setEnabled(true);
 			}
 			buy.setVisibility(View.VISIBLE);
 			details.setEnabled(true);
 			details.setVisibility(View.VISIBLE);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	// Take a list of items, and fill up the list view with those entries
 	public void fillListView(View v) {
 
 		ItemAdapter adapter = new ItemAdapter(getActivity(),
 				R.layout.shop_item, itemsInStock);
 
 		setListAdapter(adapter);
 
 		updateButtons();
 	}
 
 	private class ItemAdapter extends ArrayAdapter<Item> {
 
 		List<Item> items;
 
 		public ItemAdapter(Context context, int textViewResourceId,
 				List<Item> itemsInStock) {
 			super(context, textViewResourceId, itemsInStock);
 			this.items = itemsInStock;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				v = LayoutInflater.from(getActivity()).inflate(
 						R.layout.shop_item, null);
 			}
 			Item i = items.get(position);
 			if (i != null) {
 				TextView name = (TextView) v.findViewById(R.id.shopItemName);
 				TextView price = (TextView) v.findViewById(R.id.shopItemPrice);
 				ImageView image = (ImageView) v
 						.findViewById(R.id.shopItemImage);
 				name.setText(i.getName());
 				price.setText("Price: " + Integer.toString(i.getPrice()));
 
 				// Red price to alert player they can't afford
 				if (i.getPrice() > Player.getPlayer().getGold()) {
 					price.setTextColor(getResources().getColor(R.color.darkRed));
 				} else {
 					int defaultCol = android.R.color.primary_text_dark;
 					Log.d("ShopDebug", "Default color: " + defaultCol);
 					price.setTextColor(getResources().getColor(
 							defaultPriceColor));
 				}
 
 				Picasso.with(getActivity())
 						.load(items.get(position).getImagePath()).into(image);
 
 			}
 			return v;
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 
 		super.onListItemClick(l, v, position, id);
 
 		selectedItemIndex = position;
 
 		if (selectedItemSlot != null) {
 			selectedItemSlot.setBackgroundColor(Color.TRANSPARENT);
 		}
 
 		v.setBackgroundColor(Color.GRAY);
 
 		v.setSelected(true);
 		selectedItemSlot = v;
 
 		updateButtons();
 	}
 
 	@Override
 	public void onClick(View v) {
 		final Item selected = itemsInStock.get(selectedItemIndex);
 		switch (v.getId()) {
 
 		case R.id.shopBuyButton:
 			boolean wasSold = sellItemToPlayer();
 
 			if (wasSold) {
 				AnnoyingPopup.notice(getActivity(),
 						"You purchased " + selected.getName());
 				fillListView(getView());
 			}
 
 			break;
 		case R.id.shopDetailButton:
 			AnnoyingPopup.notice(getActivity(), selected.getDescription());
 		default:
 			break;
 		}
 	}
 
 	public boolean sellItemToPlayer() {
 		Player p = Player.getPlayer();
 		Item i = itemsInStock.get(selectedItemIndex);
 
 		if (p.getGold() < i.getPrice()) {
 			AnnoyingPopup.notice(getActivity(), "You don't have enough gold!");
 			return false;
 		} else if (p.getInventory().isInventoryFull()) {
 			AnnoyingPopup.notice(getActivity(), "Your inventory is full!");
 			return false;
 		}
 
 		p.getInventory().addNewItem(i);
 		p.deductGold(i.getPrice());
 		goldWidget.setText("" + p.getGold());
 		return true;
 	}
 
 }
