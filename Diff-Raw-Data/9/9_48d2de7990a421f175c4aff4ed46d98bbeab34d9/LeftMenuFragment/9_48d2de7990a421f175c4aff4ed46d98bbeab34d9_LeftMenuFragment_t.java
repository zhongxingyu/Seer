 package net.jsiq.marketing.fragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.jsiq.marketing.R;
 import net.jsiq.marketing.activity.IndexDisplayActivity;
 import net.jsiq.marketing.activity.MainActivity;
 import net.jsiq.marketing.adapter.LeftMenuAdapter;
 import net.jsiq.marketing.constants.URLStrings;
 import net.jsiq.marketing.model.MenuItem;
 import net.jsiq.marketing.util.LoaderUtil;
 import net.jsiq.marketing.util.MessageToast;
 import net.jsiq.marketing.util.NetworkUtils;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 
 public class LeftMenuFragment extends SherlockListFragment {
 
 	private Context context;
 
 	private LeftMenuAdapter adapter;
 	private List<MenuItem> menuList;
 
 	private MainActivity mainActivity;
 
 	public enum LOADSTATUS {
 		LOADING, FAILED, SUCCEED;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		if (getSherlockActivity() == null)
 			return;
 		if (getSherlockActivity() instanceof MainActivity) {
 			mainActivity = (MainActivity) getSherlockActivity();
 		}
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		if (mainActivity != null) {
 			mainActivity = null;
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.context = getSherlockActivity();
 		menuList = new ArrayList<MenuItem>();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View behindContentView = inflater.inflate(R.layout.behind_menu, null);
 		adapter = new LeftMenuAdapter(context, menuList);
 		setListAdapter(adapter);
 		return behindContentView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		if (NetworkUtils.isNetworkConnected(context)) {
 			new LoadMenuTask().execute(URLStrings.GET_MENUS);
 		} else {
 			MessageToast.showText(context, R.string.notConnected);
 			refreshMainStatus(LOADSTATUS.FAILED);
 		}
 	}
 
 	class LoadMenuTask extends AsyncTask<String, Void, List<MenuItem>> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			refreshMainStatus(LOADSTATUS.LOADING);
 			adapter.clear();
 		}
 
 		@Override
 		protected List<MenuItem> doInBackground(String... params) {
 			try {
 				return LoaderUtil.loadMenuItems(params[0]);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(List<MenuItem> result) {
 			super.onPostExecute(result);
 			if (result == null) {
 				refreshMainStatus(LOADSTATUS.FAILED);
 				MessageToast.showText(context, R.string.loadFailed);
 			} else {
 				adapter.addAll(result);
 				int selectedPos = getSelectedMenuPos();
 				MenuItem selectedItem = result.get(selectedPos);
 				initMainWithSelectedMenu(selectedItem);
 				refreshMainStatus(LOADSTATUS.SUCCEED);
 			}
 		}
 	}
 
 	private int getSelectedMenuPos() {
 		int selectedPos = 0;
 		if (mainActivity != null) {
 			Intent intent = mainActivity.getIntent();
 			if (intent != null) {
 				selectedPos = intent.getIntExtra(
 						IndexDisplayActivity.MENU_SELECTED_POS, 0);
 			}
 		}
 		return selectedPos;
 	}
 
 	private void initMainWithSelectedMenu(MenuItem selectedItem) {
 		switchFragment(selectedItem);
 	}
 
 	private void refreshMainStatus(LOADSTATUS loadstatus) {
		if (mainActivity != null) {
			mainActivity.refreshMainStatus(loadstatus);
		}
 	}
 
 	@Override
 	public void onListItemClick(ListView lv, View v, int position, long id) {
 		MenuItem selectedItem = menuList.get(position);
 		if (selectedItem != null)
 			switchFragment(selectedItem);
 	}
 
 	private void switchFragment(MenuItem item) {
		if (mainActivity != null) {
			mainActivity.switchCatalogByMenu(item);
		}
 	}
 
 }
