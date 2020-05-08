 package coupling.app;
 
 
 import com.nit.coupling.R;
 
 import android.content.Context;
 import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.CursorAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AdapterShopList extends CursorAdapter {
 
 	private LayoutInflater mLayoutInflater;
 	
 	private BLShopList blShoplist;
 	
 	
 	public AdapterShopList(Context context, BLShopList blShoplist) {
 		super(context, blShoplist.getSource(), true);
 		mLayoutInflater = LayoutInflater.from(context); 
 		
 		this.blShoplist = blShoplist;
 	}
 
 	@Override
 	public void bindView(View row, Context context, Cursor cursor) {
 		//Retrieve data from database
 		Long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
 		Long Uid = cursor.getLong(cursor.getColumnIndex("UId"));
 		String name =  cursor.getString(cursor.getColumnIndexOrThrow("ItemName"));
 		int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("ItemQuantity"));
 		boolean isDone = cursor.getInt(cursor.getColumnIndexOrThrow("ItemStatus")) > 0;
 		
 		Ids ids = new Ids(id, Uid);
 		
 		//Construct views
 		Button btnRemoveItem = (Button)row.findViewById(R.id.btnRemoveItem);
 		btnRemoveItem.setTag(ids);
 
 		final TextView itemName = (TextView) row.findViewById(R.id.item_name);
 		itemName.setText(name);
 
 		TextView itemQuantity = (TextView) row.findViewById(R.id.item_count);
 		itemQuantity.setText(Integer.toString(quantity));
 
 		CheckBox check = (CheckBox) row.findViewById(R.id.item_check);
 		check.setChecked(isDone);
 		check.setTag(ids);
 		
 		final ImageView redLine = (ImageView) row.findViewById(R.id.red_line_view);
 		
 		if (isDone)
 		{
 			redLine.setVisibility(View.VISIBLE);
 			//itemName.setPaintFlags(itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
 			//itemName.setTextColor(Color.GRAY);
 		} else {
 			redLine.setVisibility(View.INVISIBLE);
 		}
 
 		btnRemoveItem.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				boolean isSucceed = blShoplist.deleteItem((Ids)v.getTag());
 				if(!isSucceed)
 					Log.e("adaptor_shoplist", "failed to delete item: " + (Long)v.getTag());
 				refresh();
 			}
 		});
 
 		//Set checkbox action
 		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				boolean isSucceed = blShoplist.updateItem((Ids)buttonView.getTag(), null, null, isChecked);
 				if(!isSucceed)
 					Log.e("adaptor_shoplist", "failed to update item");
 				if(isChecked){
 					//itemName.setPaintFlags(itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
 					//itemName.setTextColor(Color.GRAY);
 					redLine.setVisibility(View.VISIBLE);
 				} else{
 					//itemName.setPaintFlags(itemName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
 					//itemName.setTextColor(Color.BLACK);
 					redLine.setVisibility(View.INVISIBLE);
 				}
 				refresh();
 			}
 		});
 	}
 	
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		return mLayoutInflater.inflate(R.layout.shoplist_item, parent, false);
 	}
 
 	public void refresh(){
 		swapCursor(blShoplist.getSource());
 		notifyDataSetChanged();
 	}
 
 }
