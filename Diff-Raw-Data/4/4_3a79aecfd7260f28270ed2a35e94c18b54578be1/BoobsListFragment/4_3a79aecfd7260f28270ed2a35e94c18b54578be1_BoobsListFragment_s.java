 package com.bytopia.oboobs.fragments;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.internal.widget.IcsProgressBar;
 import com.bytopia.oboobs.BoobsActivity;
 import com.bytopia.oboobs.BoobsListFragmentHolder;
 import com.bytopia.oboobs.ImageReceiver;
 import com.bytopia.oboobs.OboobsApp;
 import com.bytopia.oboobs.adapters.BoobsListAdapter;
 import com.bytopia.oboobs.model.Boobs;
 import com.bytopia.oboobs.providers.ImageProvider;
 import com.bytopia.oboobs.utils.Utils;
 
 public class BoobsListFragment extends SherlockListFragment implements
 		OnItemClickListener {
 
 	private static final int SENDER_TYPE = 42;
 	Activity activity;
 	OboobsApp app;
 	BoobsListAdapter adapter;
 	ImageProvider currentProvider;
 	View footer;
 	private int currentOffset;
 
 	BoobsListFragmentHolder holder;
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		this.activity = activity;
 		holder = (BoobsListFragmentHolder) activity;
 		app = (OboobsApp) activity.getApplication();
 		setRetainInstance(true);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		app.setCurentReceiver(mImageReceiver);
 	}
 
 	public void fill(List<Boobs> boobs) {
 		adapter = new BoobsListAdapter(activity, boobs, SENDER_TYPE);
		if (getListView() != null && getListView().isShown()) {
			adapter.setListBounds(getListView().getWidth(), getListView()
 					.getHeight());
 		}
 		setBoobsAdapter();
 	}
 
 	private void setBoobsAdapter() {
 		getListView().setAdapter(
 				new ArrayAdapter<String>(getSherlockActivity(),
 						android.R.layout.simple_list_item_2));
 		getListView().addFooterView(createFooter());
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		getListView().setDividerHeight(0);
 		getListView().setOnItemClickListener(this);
 		if (getListAdapter() != null) {
 			getListView().addFooterView(createFooter());
 			getListView().setAdapter(getListAdapter());
 		}
 		if (adapter != null) {
 			adapter.setListBounds(getListView().getWidth(), getListView()
 					.getHeight());
 		}
 	}
 
 	private View createFooter() {
 		if (footer != null) {
 			getListView().removeFooterView(footer);
 		}
 		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
 
 		IcsProgressBar bar = new IcsProgressBar(actionBar.getThemedContext()) {
 			@Override
 			protected void onAttachedToWindow() {
 				downloadMoreBoobs();
 			}
 		};
 		bar.setIndeterminate(true);
 
 		// if(Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB){
 		// bar.setIndeterminateDrawable(activity.getResources().getDrawable(R.styleable.SherlockActionBar_progressBarStyle));
 		// }
 		// bar.setIndeterminateDrawable(activity.getResources().getDrawable(android.R.drawable.prog));
 		// bar.addOnAttachStateChangeListener(mOnAttachStateChangeListener);
 		footer = bar;
 		return bar;
 	}
 
 	private ImageReceiver mImageReceiver = new ImageReceiver() {
 
 		@Override
 		public void receiveImage(int imageId, Bitmap bitmap) {
 			if (isVisible()) {
 				adapter.updateViews(imageId, bitmap, getListView());
 			}
 		}
 
 		@Override
 		public int getSenderType() {
 			return SENDER_TYPE;
 		}
 	};
 
 	public List<Boobs> manageNewBoobs(ImageProvider provider)
 			throws IOException {
 		currentProvider = provider;
 		return provider.getBoobs(currentOffset);
 	}
 
 	public void clearAdapter() {
 		setListAdapter(null);
 		setListShown(false);
 		currentOffset = 0;
 	}
 
 	// private OnAttachStateChangeListener mOnAttachStateChangeListener = new
 	// OnAttachStateChangeListener() {
 	//
 	// @Override
 	// public void onViewAttachedToWindow(View v) {
 	// downloadMoreBoobs();
 	// }
 	//
 	// @Override
 	// public void onViewDetachedFromWindow(View v) {
 	// // TODO Auto-generated method stub
 	//
 	// }
 	// };
 	private final BoobsLoadCallback newBoobsCallback = new BoobsLoadCallback() {
 
 		@Override
 		public void receiveBoobs(List<Boobs> boobs) {
 			if (boobs != null) {
 				fill(boobs);
 			} else {
 				holder.handleErrorWhileLoadingProvider();
 			}
 		}
 
 		@Override
 		public void getReadyForBoobs() {
 			clearAdapter();
 		}
 
 	};
 
 	private final BoobsLoadCallback moreBoobsCallback = new BoobsLoadCallback() {
 
 		@Override
 		public void receiveBoobs(List<Boobs> boobs) {
 			if (boobs != null) {
 				getListView().addFooterView(createFooter());
 				for (Boobs boob : boobs) {
 					adapter.add(boob);
 				}
 			} else {
 				getListView().removeFooterView(footer);
 			}
 		}
 
 		@Override
 		public void getReadyForBoobs() {
 			// TODO show progress
 		}
 
 	};
 
 	public void getBoobsFrom(ImageProvider provider) {
 		asyncLoadBoobs(newBoobsCallback, provider);
 	}
 
 	private void downloadMoreBoobs() {
 		currentOffset += Utils.getBoobsChunk();
 		asyncLoadBoobs(moreBoobsCallback, currentProvider);
 	}
 
 	private void asyncLoadBoobs(final BoobsLoadCallback callback,
 			ImageProvider provider) {
 		new AsyncTask<ImageProvider, Void, List<Boobs>>() {
 
 			protected void onPreExecute() {
 				callback.getReadyForBoobs();
 			};
 
 			@Override
 			protected List<Boobs> doInBackground(ImageProvider... params) {
 				try {
 					return manageNewBoobs(params[0]);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			protected void onPostExecute(final java.util.List<Boobs> result) {
 				callback.receiveBoobs(result);
 			};
 		}.execute(provider);
 
 		Log.d("provider", provider.getClass().getName());
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		Boobs item = (Boobs) parent.getItemAtPosition(position);
 
 		Intent intent = new Intent(activity, BoobsActivity.class);
 		intent.putExtra(BoobsActivity.BOOBS, item);
 
 		activity.startActivity(intent);
 
 	}
 
 }
 
 interface BoobsLoadCallback {
 	void receiveBoobs(List<Boobs> boobs);
 
 	void getReadyForBoobs();
 }
