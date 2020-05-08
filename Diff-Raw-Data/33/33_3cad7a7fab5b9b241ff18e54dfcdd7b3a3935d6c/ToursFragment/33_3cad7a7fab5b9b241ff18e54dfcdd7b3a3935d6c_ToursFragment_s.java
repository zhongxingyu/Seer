 package com.ss.stg;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.ss.stg.R;
 import com.ss.stg.dto.TourItem;
 import com.ss.stg.ws2.IWebService;
 import com.ss.stg.ws2.WSThread;
 
 /**
  * A fragment representing a list of Items.
  * <p />
  * Large screen devices (such as tablets) are supported by replacing the
  * ListView with a GridView.
  * <p />
  * Activities containing this fragment MUST implement the {@link Callbacks}
  * interface.
  */
 public class ToursFragment extends Fragment implements AbsListView.OnItemClickListener, OnItemLongClickListener {
 
 	private Toast toast = null;
 	private Handler handler = null;
 
 	private OnTourFragmentInteractionListener mListener;
 
 	/**
 	 * The fragment's ListView/GridView.
 	 */
 	private ListView mListView;
 
 	/**
 	 * The Adapter which will be used to populate the ListView/GridView with
 	 * Views.
 	 */
 	private ToursAdapter mAdapter;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public ToursFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.d("MyLog", "ToursFragment_onCreate");
 		super.onCreate(savedInstanceState);
 
 		if (getArguments() != null) {
 		}
 
 		handler = new TourHandler(getActivity());
 		WSThread thread = null;
 		String userId = ((MainActivity) getActivity()).getLoginUserId();
 		if (!userId.equals("")) {
 			HashMap<String, Object> params = new HashMap<String, Object>();
 			params.put(IWebService.PARAM__GET_TOURS_BY_USERID, userId);
 			thread = new WSThread(handler, IWebService.ID__GET_TOURS_BY_USERID, params);
 			thread.startWithProgressDialog(getActivity());
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		Log.d("MyLog", "ToursFragment_onCreateView");
 
 		View view = inflater.inflate(R.layout.fragment_tours_list, container, false);
 
 		// Set the adapter
 		mListView = (ListView) view.findViewById(R.id.tours_list);
 		mListView.setAdapter(mAdapter);
 
 		// Set OnItemClickListener so we can be notified on item clicks
 		mListView.setOnItemClickListener(this);
 		mListView.setOnItemLongClickListener(this);
 
 		return view;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			mListener = (OnTourFragmentInteractionListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString() + " must implement OnTourFragmentInteractionListener");
 		}
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		mListener = null;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		if (null != mListener) {
 			// Notify the active callbacks interface (the activity, if the
 			// fragment is attached to one) that an item has been selected.
 
 			String id2 = mAdapter.getItem(position).getTourId();
 			mListener.onTourFragmentInteraction(id2);
 		}
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 
 		final int pos = position;
 		// final Context context = this.layoutInflater.getContext();
 		final CharSequence[] items = { "浏览", "编辑", "删除" };
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setTitle("操作列表");
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 
 				// 浏览
 				case 0:
 					String id2 = mAdapter.getItem(pos).getTourId();
 					mListener.onTourFragmentInteraction(id2);
 					break;
 
 				// 编辑
 				case 1:
 					Intent intent2 = new Intent(getActivity(), TourEditActivity.class);
 					intent2.putExtra("tourid", mAdapter.getItem(pos).getTourId());
 					intent2.putExtra("userid", ((MainActivity) getActivity()).getLoginUserId());
 					startActivity(intent2);
 					break;
 
 				// 删除
 				case 2:
 
 					toast = Toast.makeText(getActivity(), "暂未实现", Toast.LENGTH_SHORT);
 					toast.show();
 					break;
 				}
 			}
 		});
 		AlertDialog alertDialog = builder.create();
 		alertDialog.setView(null, 0, 0, 0, 0);
 		alertDialog.show();
 
 		return false;
 	}
 
 	/**
 	 * The default content for this Fragment has a TextView that is shown when
 	 * the list is empty. If you would like to change the text, call this method
 	 * to supply the text it should use.
 	 */
 	// public void setEmptyText(CharSequence emptyText) {
 	// View emptyView = mListView.getEmptyView();
 	//
 	// if (emptyText instanceof TextView) {
 	// ((TextView) emptyView).setText(emptyText);
 	// }
 	// }
 
 	/**
 	 * This interface must be implemented by activities that contain this
 	 * fragment to allow an interaction in this fragment to be communicated to
 	 * the activity and potentially other fragments contained in that activity.
 	 * <p>
 	 * See the Android Training lesson <a href=
 	 * "https://developer.android.com/training/basics/fragments/communicating.html"
 	 * >Communicating with Other Fragments</a> for more information.
 	 */
 	public interface OnTourFragmentInteractionListener {
 		// TODO: Update argument type and name
 		public void onTourFragmentInteraction(String id);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == Activity.RESULT_OK) {
			mAdapter.notifyDataSetChanged();
 		}
 	}
 
 	private class TourHandler extends Handler {
 
 		private Activity activity = null;
 
 		private TourHandler(Activity activity) {
 			this.activity = activity;
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 
 			super.handleMessage(msg);
 
 			if (msg.what == IWebService.ID__GET_TOURS_BY_USERID) {
 				List<TourItem> list = (List<TourItem>) msg.getData().getSerializable(IWebService.WS_RETURN);
 				mAdapter = new ToursAdapter(activity, list);
 				mListView.setAdapter(mAdapter);
 			} else {
 				showNetworkErrorDialog();
 			}

 		}
 
 		private void showNetworkErrorDialog() {
 
 			new AlertDialog.Builder(this.activity).setTitle("error").setMessage("网络连接出错").setNeutralButton("返回", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dlg, int sumthin) {
 
 				}
 			}).show();
 
 		}
 	}
 }
