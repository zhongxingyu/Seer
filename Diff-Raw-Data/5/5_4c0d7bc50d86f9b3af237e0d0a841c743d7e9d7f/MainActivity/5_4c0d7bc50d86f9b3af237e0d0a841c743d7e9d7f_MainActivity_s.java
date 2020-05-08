 package com.example.cuncurrentlistview;
 
 import java.lang.ref.WeakReference;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.example.cuncurrentlistview.ConcurrentItemManager.TaskStatusChangedListener;
 import com.github.kevinsawicki.wishlist.ViewFinder;
 
 public class MainActivity extends Activity implements OnItemClickListener {
 
 	private static final int NORMAL_ORDER   = 0;
 	private static final int REVERSE_ORDER  = 1;
 	private static final String ORDER_TYPE  = "ordertype";
 	
 	
 	private ListView mListView;
 	private static Item[] mItems;
 	private int mType;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         ViewFinder finder = new ViewFinder(this);
         
         mType = getIntent().getIntExtra(ORDER_TYPE, -1);
         mItems = getItems();
         mListView = finder.find(R.id.listview);
         
 		switch (mType) {
 		case NORMAL_ORDER:
 			mListView.setAdapter(new ConcurrentItemsAdapter(mItems));
 			break;
 		case REVERSE_ORDER:
 			Item[] reverseArray = new Item[mItems.length];
 			List<Item> reverseList = Arrays.asList(mItems.clone());
 			Collections.reverse(reverseList);
 			reverseList.toArray(reverseArray);
 			mListView.setAdapter(new ConcurrentItemsAdapter(reverseArray));
 			break;
 
 		default:
 			mListView.setAdapter(new ArrayAdapter<String>(
 					getApplicationContext(),
 					android.R.layout.simple_list_item_1, new String[] {
 							"NormalOrder", "ReverseOrder" }));
 			break;
 		}
 		mListView.setOnItemClickListener(this);
     } 
 
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0,final View view, int position, long arg3) {
 		
 		switch (mType) {
 		case NORMAL_ORDER:
 			final Item item_normal = mItems[position];
 			startLoad(item_normal,view);
 			break;
 		case REVERSE_ORDER:
 				Item [] items = ((ConcurrentItemsAdapter)mListView.getAdapter()).getData();
 				final Item item_reversed = items[position];
 				startLoad(item_reversed,view);
 			break;
 
 		default:
 			showOrder(position);
 			break;
 		}
 	}
 	
 	public void startLoad(final Item item,View view){
 		ConcurrentItemManager.getInstance().execute(item, new ViewUpdater(view) {
 			
 			@Override
 			public void onComplete() {
 				ConcurrentItemManager.getInstance().removeFromQueue(item);
 			}
 			
 			@Override
 			public void onUpdateView(View view) {
				if(((TextView)((ItemView)view).findViewById(R.id.item_text_view)).getText().equals(item.mName))
 					((ProgressBar)((ItemView)view).findViewById(R.id.item_progress_bar)).setProgress(item.mProgress);
 			}
 		});
 	}
 	
 	private final void showOrder(int position){
 		Intent i = new Intent(this, MainActivity.class);
 		i.putExtra(ORDER_TYPE, position);
 		startActivity(i);
 	}
 	
 	private Item[] getItems(){
 		
 		return mItems == null ? new Item[]{new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item(),new Item()} : mItems;
 	}
 	
 	private class ConcurrentItemsAdapter extends BaseAdapter{
 		private Item[] mItems;
 		
 		public ConcurrentItemsAdapter(Item[] items){
 			mItems = items;
 		}
 		
 		public Item[] getData(){
 			return mItems;
 		}
 		
 		
 		@Override
 		public int getCount() {
 			return mItems.length;
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			return mItems[arg0];
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 		
 
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			final ItemView itemView;
 			Item item = mItems[position];
 			if(null == convertView){ 
 				itemView = new ItemView(MainActivity.this);
 			}else{
 				itemView = (ItemView)convertView;
 			}
 			if(ConcurrentItemManager.getInstance().hasInQueue(item)){
 				((ViewUpdater)ConcurrentItemManager.getInstance().getTaskFromQuue(item).getCallback()).updateView(itemView);
 			}
 			itemView.setItem(item);
 			return itemView;
 		}}
 	
 	public abstract class ViewUpdater implements TaskStatusChangedListener{
 		
 		private WeakReference<View> itemView;
 		
 		public ViewUpdater(View view) {
 			itemView = new WeakReference<View>(view);
 		}
 		
 		public void updateView(View view){
 			itemView.clear();
 			itemView = new WeakReference<View>(view);
 		}
 
 		public abstract void onUpdateView(View view);
 		public void onCreateView(View view){}
 		
 		@Override
 		public void onUpdate() {
 		runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				onUpdateView(itemView.get());
 			}
 		});	
 		}
 		
 		@Override
 		public void onCreate() {
 			runOnUiThread(new Runnable() {
 				
 				@Override
 				public void run() {
 					onCreateView(itemView.get());
 				}
 			});
 		}
 	}
     
 }
