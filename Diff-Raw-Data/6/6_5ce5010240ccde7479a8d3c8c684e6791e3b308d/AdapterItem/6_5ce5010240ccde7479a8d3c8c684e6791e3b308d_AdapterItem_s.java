 package org.pierrre.adapteritem;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AbsListView;
 
 public abstract class AdapterItem {
 	private static final RecyclerListener recyclerListener = new RecyclerListener();
 	
 	private Context context;
 	private View view;
 	
 	@SuppressWarnings("unchecked")
 	public static <T extends AdapterItem> T getItem(View view) {
 		Object tag = view.getTag(R.id.adapter_item_view_tag);
 		
 		if (tag == null) {
 			return null;
 		}
 		
 		try {
 			return (T) tag;
 		} catch (ClassCastException e) {
 			return null;
 		}
 	}
 	
 	public static void setRecyclerLister(AbsListView absListView) {
 		absListView.setRecyclerListener(AdapterItem.recyclerListener);
 	}
 	
 	public AdapterItem(Context context, View view) {
 		this.context = context;
 		
 		this.view = view;
 		this.view.setTag(R.id.adapter_item_view_tag, this);
 	}
 	
	public AdapterItem(Context context, int layoutResId) {
		this(context, LayoutInflater.from(context).inflate(layoutResId, null));
 	}
 	
 	public Context getContext() {
 		return this.context;
 	}
 	
 	public View getView() {
 		return this.view;
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected <T extends View> T findViewById(int id) {
 		return (T) this.view.findViewById(id);
 	}
 	
 	public void onViewMovedToScrapHeap() {
 		// Clean the item (async requests, etc..)
 	}
 	
 	private static class RecyclerListener implements AbsListView.RecyclerListener {
 		@Override
 		public void onMovedToScrapHeap(View view) {
 			AdapterItem item = AdapterItem.getItem(view);
 			
 			if (item != null) {
 				item.onViewMovedToScrapHeap();
 			}
 		}
 	}
 }
