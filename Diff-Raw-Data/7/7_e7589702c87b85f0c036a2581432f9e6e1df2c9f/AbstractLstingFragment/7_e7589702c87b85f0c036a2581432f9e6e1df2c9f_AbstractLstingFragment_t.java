 package eu.trentorise.smartcampus.android.common.listing;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
 import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
 
 public abstract class AbstractLstingFragment<T> extends SherlockFragment implements OnScrollListener {
 
 	protected int lastSize = 0;
 	protected int position = 0;
 	protected int size = ListingRequest.DEFAULT_ELEMENTS_NUMBER;
 	protected ArrayAdapter<T> adapter;
 
 	public int getSize() {
 		return size;
 	}
 
 	public void setSize(int size) {
 		this.size = size;
 	}
 
 	public ArrayAdapter<T> getAdapter() {
 		return adapter;
 	}
 
 	public void setAdapter(ArrayAdapter<T> adapter) {
 		this.adapter = adapter;
 	}
 
 	@Override
 	public void onStart() {
 		ListView contentsListView = getListView();
 		position = 0;
		lastSize = 0;
 		if (adapter != null) {
 			if (loadOnStart()) load();
 			contentsListView.setAdapter(adapter);
 		}
 		contentsListView.setOnScrollListener(this);
 		super.onStart();
 	}
 
 	protected boolean loadOnStart() {
 		return true;
 	}
 
 	protected abstract ListView getListView();
 
 	
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) { }
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 		boolean loadMore = 
 				(firstVisibleItem + visibleItemCount >= totalItemCount) && // end of visible list reached 
				(lastSize < adapter.getCount()) &&  // last load has been successful
				adapter.getCount() >= size;
 		if (loadMore) {
 			lastSize = adapter.getCount();
 			position += size; 
 			load();
 		}
 	}
 
 	protected void load() {
 		if (position == 0) {
 			adapter.clear();
 		}
 		new SCListingFragmentTask<ListingRequest, Void>(getActivity(), getLoader()).execute(new ListingRequest(position, size));
 	}
 
 	protected class SCListingFragmentTask<Params, Progress> extends SCAsyncTask<Params, Progress, List<T>> {
 
 		public SCListingFragmentTask(Activity activity, SCAsyncTask.SCAsyncTaskProcessor<Params, List<T>> processor) {
 			super(activity, processor);
 		}
 
 		@Override
 		protected void handleSuccess(List<T> result) {
 			super.handleSuccess(result);
 			if (result != null) {
 				for (T o: result) {
 					adapter.add(o);
 				}
 			}
 			adapter.notifyDataSetChanged();
 		}
 	}
 	
 	protected abstract SCAsyncTaskProcessor<ListingRequest, List<T>> getLoader();
 
 	public static class ListingRequest {
 		public static final int DEFAULT_ELEMENTS_NUMBER = 20;
 		
 		public int position = 0;
 		public int size = 0;
 		
 		public ListingRequest(int position, int size) {
 			super();
 			this.position = position;
 			this.size = size;
 		}
 		
 		
 	}
 	
 }
