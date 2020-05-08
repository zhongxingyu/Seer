 package com.pc.programmerslife.fragments;
 
 import java.util.ArrayList;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.pc.programmerslife.Commic;
 import com.pc.programmerslife.CommicManager;
 import com.pc.programmerslife.CommicManager.CommicManagerListener;
 import com.pc.programmerslife.R;
 import com.pc.programmerslife.activities.CommicActivity;
 import com.pc.programmerslife.adapters.ItemListAdapter;
 
 public class CommicsFragment extends SherlockListFragment implements CommicManagerListener {
 	private static final String COMMICS_SIZE_TAG = "CommicsSize";
 	private static final int QUANTITY = 10;
 	
 	private ArrayList<Commic> commics;
 	private View footerView;
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		configureView();
 		
 		commics = new ArrayList<Commic>();
 		
 		setHasOptionsMenu(true);
 		
 		if (savedInstanceState == null) {
 			update();
 			reloadCommics();
 		} else
 			reloadCommics(0, savedInstanceState.getInt(COMMICS_SIZE_TAG, QUANTITY));
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		outState.putInt(COMMICS_SIZE_TAG, commics.size());
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.commics_fragment_menu, menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.commicsFragmentMenu_refresh) {
 			update();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		if (position == commics.size()) {
 			loadMore();
 		} else {
 			Commic commic = commics.get(position);
 			
 			commic.setRead(true);
 			
 			startActivity(new Intent(getSherlockActivity(), CommicActivity.class).putExtra(Commic.EXTRA_COMMIC, commic));
 			
 			if (CommicManager.getInstance().updateCommicReaded(commic) == true)
 				((ItemListAdapter) getListAdapter()).notifyDataSetChanged();
 		}
 	}
 	
 	private void configureView() {
 		getView().setBackgroundColor(Color.WHITE);
 		getListView().setDivider(new ColorDrawable(Color.BLACK));
 		getListView().setDividerHeight(1);
 		
 		setEmptyText(getString(R.string.emptyText));
 	}
 	
 	private void update() {
 		setListShown(commics.size() > 0);
 		
 		CommicManager manager = CommicManager.getInstance();
 		manager.setLink("http://feeds.feedburner.com/VidaDeProgramador?format=xml");
 		manager.update(this);
 	}
 	
 	private int quantity() {
 		return (commics.size() < QUANTITY) ? QUANTITY : commics.size();
 	}
 	
 	private void reloadCommics() {
 		reloadCommics(0, quantity());
 	}
 	
 	private void reloadCommics(int s, int q) {
 		ArrayList<Commic> dbCommics = CommicManager.getInstance().getCommics(s, q);
 		if (dbCommics != null) {
 			commics.clear();
 			
 			commics.addAll(dbCommics);
 			
 			reloadViews();
 		}
 	}
 	
 	private void loadMore() {
 		int s = commics.size();
 		ArrayList<Commic> dbCommics = CommicManager.getInstance().getCommics(s, QUANTITY);
 		if (dbCommics != null) {
 			commics.addAll(dbCommics);
 			
 			reloadViews();
 		}
 		
		getListView().setSelectionFromTop(s, 0);
 	}
 	
 	private void reloadViews() {
 		setListAdapter(null);
 		
 		int count = CommicManager.getInstance().getCommicsCount();
 		if (count > commics.size()) {
 			if (footerView == null) {
 				footerView = LayoutInflater.from(getSherlockActivity()).inflate(R.layout.footer_view, null, false);
 				getListView().addFooterView(footerView, null, true);
 			}
 		} else {
 			getListView().removeFooterView(footerView);
 			footerView = null;
 		}
 		
 		setListAdapter(new ItemListAdapter(getSherlockActivity(), R.layout.item_list_layout, commics));
 	}
 
 	@Override
 	public void onFinishUpdate(CommicManager manager) {
 		if (isResumed() == true) {
 			reloadCommics(0, QUANTITY);
 			setListShown(true);
 		}
 	}
 
 	@Override
 	public void onFailUpdate(Exception e, CommicManager manager) {
 		if (isResumed() == true) {
 			setListShown(true);
 			Toast.makeText(getSherlockActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
 		}
 	}
 }
