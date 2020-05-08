 package com.rava.voting.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import retrofit.Callback;
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 import android.app.ListFragment;
 import android.os.Bundle;
 import android.support.v4.widget.SwipeRefreshLayout;
 import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.rava.voting.R;
 import com.rava.voting.RavaApplication;
 import com.rava.voting.api.ElectionService;
 import com.rava.voting.model.Election;
 import com.rava.voting.model.User;
 import com.rava.voting.ui.adapter.ElectionsArrayAdapter;
 import com.rava.voting.utils.SettingsManager;
 import com.rava.voting.utils.Utils;
 
 public class UserOpenElectionsFragment extends ListFragment implements
 		OnRefreshListener {
 
 	public static final String TAG = "UserOpenElectionsFragment";
 
 	private ElectionsArrayAdapter mAdapter;
 	private SwipeRefreshLayout mRefreshLayout;
 
 	public static UserOpenElectionsFragment newInstance() {
 		UserOpenElectionsFragment fragment = new UserOpenElectionsFragment();
 		Bundle args = new Bundle();
 		fragment.setArguments(args);
 		return fragment;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View root = inflater.inflate(R.layout.fragment_elections, container,
 				false);
 		mRefreshLayout = (SwipeRefreshLayout) root
 				.findViewById(R.id.swipe_container);
 		mRefreshLayout.setOnRefreshListener(this);
 		mRefreshLayout
 				.setColorScheme(android.R.color.holo_blue_dark,
 						android.R.color.holo_green_dark,
 						android.R.color.holo_orange_dark,
 						android.R.color.holo_red_dark);
 		return root;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		// mElections = new ArrayList<Election>();
 		// mElections.add(new Election(1, "Presidental Election", "",
 		// ElectionState.ONE));
 		// mElections.add(new Election(1, "Best soccer player", "",
 		// ElectionState.ONE));
 		// mElections.add(new Election(1, "Best tennis player", "",
 		// ElectionState.ZERO));
 		// mElections.add(new Election(1, "Test election", "",
 		// ElectionState.TWO));
 
 		setList();
 		mAdapter = new ElectionsArrayAdapter(getActivity(), 0,
 				new ArrayList<Election>());
 		setListAdapter(mAdapter);
 		load();
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Toast.makeText(getActivity(), mAdapter.getItem(position).getName(),
 				Toast.LENGTH_SHORT).show();
 	}
 
 	private void setList() {
 		getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
 			@Override
 			public void onScrollStateChanged(AbsListView view, int scrollState) {
 
 			}
 
 			@Override
 			public void onScroll(AbsListView view, int firstVisibleItem,
 					int visibleItemCount, int totalItemCount) {
 				int topRowVerticalPosition = (getListView() == null || getListView()
 						.getChildCount() == 0) ? 0 : getListView()
 						.getChildAt(0).getTop();
 				mRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
 			}
 		});
 
 	}
 
 	private void load() {
 		mRefreshLayout.setRefreshing(true);
 		User user = SettingsManager.getUser();
 		if (user == null) {
 			mRefreshLayout.setRefreshing(false);
 			mAdapter.setElections(new ArrayList<Election>());
 			return;
 		}
 		RavaApplication app = (RavaApplication) getActivity().getApplication();
 		ElectionService electionService = app.getElectionService();
 		electionService.getUserOpenElections(user.getId(),
 				new Callback<List<Election>>() {
 
 					@Override
 					public void success(List<Election> elections,
 							Response response) {
 						mRefreshLayout.setRefreshing(false);
 						mAdapter.setElections(elections);
 					}
 
 					@Override
 					public void failure(RetrofitError error) {
						mRefreshLayout.setRefreshing(false);
 						Utils.parseError(error, getActivity());
 					}
 				});
 	}
 
 	@Override
 	public void onRefresh() {
 		load();
 	}
 
 }
