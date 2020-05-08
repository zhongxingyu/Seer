 package com.ottmatt.munchies;
 
 import java.util.List;
 
 import roboguice.inject.InjectResource;
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.content.Context;
 import android.content.Loader;
 import android.os.Bundle;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.ottmatt.munchies.PlacesParser.Message;
 
 public class PlacesListFragment extends ListFragment implements
 		LoaderManager.LoaderCallbacks<List<Message>> {
 	@InjectResource(R.string.no_results)
 	String no_results;
 	PlacesAdapter mAdapter;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setEmptyText(no_results);
 		mAdapter = new PlacesAdapter(getActivity());
 		setListAdapter(mAdapter);
 		setListShown(false);
 		getLoaderManager().initLoader(0, null, this);
 	}
 
 	@Override
 	public Loader<List<Message>> onCreateLoader(int id, Bundle args) {
 		return new PlacesLoader(getActivity());
 	}
 
 	@Override
 	public void onLoadFinished(Loader<List<Message>> loader, List<Message> data) {
 		mAdapter.setData(data);
 		if (isResumed())
 			setListShown(true);
 		else
 			setListShownNoAnimation(true);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<List<Message>> loader) {
 		mAdapter.setData(null);
 	}
 
 	public static class PlacesAdapter extends ArrayAdapter<Message> {
 		private final LayoutInflater mInflater;
 
 		public PlacesAdapter(Context context) {
 			super(context, R.layout.places_list_view);
 			mInflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		public void setData(List<Message> data) {
 			clear();
 			if (data != null)
 				addAll(data);
 		}
 
 		// Populate new items in the list
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view;
 			if (convertView == null)
 				view = mInflater.inflate(R.layout.places_list_view, parent,
 						false);
 			else
 				view = convertView;
 			Message item = getItem(position);
 			((TextView) view.findViewById(R.id.rating)).setText(Double
 					.toString(item.getRating()));
			((TextView) view.findViewById(R.id.price_level)).setText(Integer
					.toString(item.getPriceLevel()));
 			((TextView) view.findViewById(R.id.places_name)).setText(item
 					.getStore());
 			((TextView) view.findViewById(R.id.vicinity)).setText(item
 					.getVicinity());
 
 			return view;
 		}
 	}
 
 }
