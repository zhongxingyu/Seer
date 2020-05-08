 package org.lds.community.CallingWorkFlow.activity;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import org.lds.community.CallingWorkFlow.Adapter.CallingViewItemAdapter;
 import org.lds.community.CallingWorkFlow.R;
 import org.lds.community.CallingWorkFlow.api.CallingManager;
 import org.lds.community.CallingWorkFlow.api.CwfNetworkUtil;
 import org.lds.community.CallingWorkFlow.domain.Calling;
 import org.lds.community.CallingWorkFlow.domain.CallingViewItem;
 import org.lds.community.CallingWorkFlow.domain.WorkFlowDB;
 import org.lds.community.CallingWorkFlow.domain.WorkFlowStatus;
 import roboguice.fragment.RoboListFragment;
 
 import javax.inject.Inject;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class CallingListFragment extends RoboListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
     @Inject
     WorkFlowDB db;
 
     @Inject
     CwfNetworkUtil networkUtil;
 
	@Inject
	CallingManager callingManager;

 	private CallingViewItemAdapter callingViewItemAdapter;
 	private List<CallingViewItem> callingViewItems;
 
     public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
     }
 
     private int currentPositionInList = 0;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		callingViewItems = db.getCallings(false);
 		this.callingViewItemAdapter = new CallingViewItemAdapter(getActivity(), android.R.layout.simple_list_item_1, callingViewItems);
 		setListAdapter(callingViewItemAdapter);
 		ListView listView = getListView();
 
 		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
 				//Toast.makeText(getActivity(), "long click", Toast.LENGTH_SHORT).show();
 				displayStatusPopup(position);
 				return true;
 			}
 		});
 
 	}
 
 	private void displayStatusPopup(final int position) {
 		LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
 		final View popupView = layoutInflater.inflate(R.layout.calling_status_popup, null);
 
 		final Spinner statusSpinner = (Spinner) popupView.findViewById(R.id.calling_status_spinner);
         List<WorkFlowStatus> statusList = db.getWorkFlowStatuses();
         List<CharSequence> statusOptions = new ArrayList<CharSequence>();
         for(WorkFlowStatus s: statusList) { statusOptions.add(s.getStatusName()); }
         ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, statusOptions);
         spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         statusSpinner.setAdapter(spinnerAdapter);
 
 		final PopupWindow popupWindow = new PopupWindow(popupView,
 												        LinearLayout.LayoutParams.WRAP_CONTENT,
 												        LinearLayout.LayoutParams.WRAP_CONTENT);
 
 		Button btnDismiss = (Button)popupView.findViewById(R.id.cancelCallingStatusChangeBtn);
 		btnDismiss.setOnClickListener(new Button.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				popupWindow.dismiss();
 			}
 		});
 		Button btnSaveStatus = (Button)popupView.findViewById(R.id.updateCallingStatusBtn);
 		btnSaveStatus.setOnClickListener(new Button.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String selectedItem = (String)statusSpinner.getSelectedItem();
 				CallingViewItem callingViewItem = callingViewItems.get(position);
 				Calling calling = callingViewItem.getCalling();
 				calling.setStatusName(selectedItem);
 				callingManager.saveCalling(calling, getActivity());
 				popupWindow.dismiss();
 			}
 		});
 
 		popupWindow.showAtLocation(getListView(), 1, 0, 0);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		CallingViewItem callingViewItem = callingViewItems.get(position);
 		Intent intent = new Intent(getActivity(), DetailActivity.class);
 		intent.putExtra("callingViewItems", (Serializable)callingViewItem);
 
 		startActivity(intent);
 	}
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
 	    return getLayoutInflater( savedInstanceState ).inflate( R.layout.callingworkflow_list, container );
     }
 	/*
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 		//outState.putInt("LIST_POS", getListView().getFirstVisiblePosition());
     }
 
     public void selectPosition(int position, long id) {
         currentPositionInList = position;
     }
     */
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
 		return null;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> cursorLoader) {
 	}
 }
