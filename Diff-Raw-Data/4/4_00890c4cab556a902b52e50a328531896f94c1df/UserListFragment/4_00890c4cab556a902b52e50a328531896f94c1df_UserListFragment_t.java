 package com.phinmadvader.andcpp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.phinvader.libjdcpp.DCFileList;
 import com.phinvader.libjdcpp.DCMessage;
 import com.phinvader.libjdcpp.DCUser;
 
 /**
  * Created by invader on 8/11/13.
  */
 public class UserListFragment extends Fragment {
 	private MainActivity mainActivity;
 	private ListView userListView;
 	private TextView connectedUserCount;
 	private EditText filterInput;
 	private UserListAdapter userListAdapter;
 	private List<DCUserComparable> nickList;
 	private Set<DCUserComparable> nickSet;
 	public boolean is_ready = false;
 
 	/*
 	 * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
 	 * container, Bundle savedInstanceState) { this.mainActivity =
 	 * (MainActivity) getActivity();
 	 * 
 	 * List<DCUser> nickList = mainActivity.mService.get_nick_list(); return sv;
 	 * }
 	 */
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		this.mainActivity = (MainActivity) getActivity();
 		View rootView = inflater.inflate(R.layout.userlist_layout, container,
 				false);
 		nickSet = new TreeSet<DCUserComparable>();
 		nickList = new ArrayList<DCUserComparable>();
 		userListAdapter = new UserListAdapter(mainActivity,
 				android.R.layout.simple_list_item_1, nickList);
 
 		userListView = (ListView) rootView.findViewById(R.id.listView);
 		userListView.setAdapter(userListAdapter);
 		userListView
 				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					@Override
 					public void onItemClick(AdapterView<?> adapterView,
 							View view, int i, long l) {
 						DCUserComparable user = nickList.get(i);
 						mainActivity.open_file_list(user);
 					}
 				});
 
 		filterInput = (EditText) rootView.findViewById(R.id.filterText);
 		filterInput.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence arg0, int start, int before,
 					int count) {
 				if (before > count) {
 
 					// TODO : A better way to reload list on <BACKSPACE>
 					// This is just a stub for the functionality.
 
 					nickList.clear();
 					clearNicklistConstraints();
 				}
 				if (arg0.length() > 0)
 					userListAdapter.getFilter().filter(arg0);
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1,
 					int arg2, int arg3) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 			}
 		});
 
 		connectedUserCount = (TextView) rootView.findViewById(R.id.textView2);
 		if (mainActivity.mBound) {
 			List<DCUser> curlist = mainActivity.mService.get_nick_list();
 			List<DCUserComparable> nlist = new ArrayList<DCUserComparable>();
 			for (DCUser user : curlist)
 				nlist.add(new DCUserComparable(user));
 			addNick(nlist);
 		}
 		is_ready = true;
 		return rootView;
 	}
 
 	public void addOneNick(DCMessage nick_msg) {
 		List<DCUserComparable> nlist = new ArrayList<DCUserComparable>();
 		nlist.add(new DCUserComparable(nick_msg.myinfo));
 		addNick(nlist);
 	}
 
 	public void addNick(final List<DCUserComparable> nicks) {
 		synchronized (nickSet) {
 			for (int i = 0; i < nicks.size(); i++)
 				if (nickSet.contains(nicks.get(i))) {
 					nicks.remove(i);
 				}
 			for (int i = 0; i < nicks.size(); i++) {
 				nickSet.add(nicks.get(i));
 				nickList.add(nicks.get(i));
 			}
 			Collections.sort(nickList);
 		}
 		refreshViewFromData();
 	}
 
 	public void delNick(final DCUserComparable nick) {
 		synchronized (nickSet) {
 			if (!nickSet.contains(nick))
 				return;
 			for (int i = 0; i < nickList.size(); i++) {
 				if (nickList.get(i).equals(nick)) {
 					nickList.remove(i);
 					break;
 				}
 			}
 			nickSet.remove(nick);
 		}
 		refreshViewFromData();
 	}
 
 	public void clearNicklistConstraints() {
 		if (mainActivity.mService == null)
 			return;
 		if (mainActivity.mService.get_status() != DCPPService.DCClientStatus.CONNECTED)
 			return;
 		// connectActivity.moveToPage(1);
 		List<DCUser> nickList = mainActivity.mService.get_nick_list();
 		List<DCUserComparable> nickListString = new ArrayList<DCUserComparable>();
 		for (int i = 0; i < nickList.size(); i++) {
 			nickListString.add(new DCUserComparable(nickList.get(i)));
 		}
 		addNick(nickListString);
 		refreshViewFromData();
 	}
 
 	public void refreshViewFromData() {
 		mainActivity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				userListAdapter.notifyDataSetChanged();
 				connectedUserCount.setText(String.valueOf(nickList.size()));
 			}
 		});
 	}
 }
