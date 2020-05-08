 package wei.mark.tabletennis;
 
 import java.util.ArrayList;
 
 import org.jared.commons.ui.OnLoadListener;
 import org.jared.commons.ui.OnScrollToScreenListener;
 import org.jared.commons.ui.WorkspaceView;
 
 import wei.mark.tabletennis.FragmentProgressBar.ProgressBarState;
 import wei.mark.tabletennis.TableTennisRatings.Navigation;
 import wei.mark.tabletennis.model.PlayerModel;
 import wei.mark.tabletennis.util.AppEngineParser;
 import wei.mark.tabletennis.util.Debuggable;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.telephony.TelephonyManager;
import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class FragmentPlayerSearch extends Fragment {
	private static final String TAG = "FragmentPlayerSearch";

 	TableTennisRatings app;
 
 	String mProvider;
 	int mCurrentScreen;
 
 	ArrayList<String> mRCHistory, mUSATTHistory;
 	String mRCQuery, mUSATTQuery;
 
 	ListView rcListView, usattListView;
 	Button rcSearchButton, usattSearchButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		app = (TableTennisRatings) getActivity().getApplication();
 
 		mRCHistory = retrieveHistory("rc");
 		mRCQuery = null;
 		mUSATTHistory = retrieveHistory("usatt");
 		mUSATTQuery = null;
 		mCurrentScreen = 0;
 
 		app.CurrentNavigation = Navigation.IDLE;
 		debug("Current navigation is now " + app.CurrentNavigation.toString());
 
 		setRetainInstance(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		final WorkspaceView work = new WorkspaceView(getActivity(), null);
 
 		View rcView = inflater.inflate(R.layout.fragment_player_search_rc,
 				null, false);
 		((TextView) rcView.findViewById(R.id.title)).setText("Ratings Central");
 		final EditText rcNameInput = (EditText) rcView
 				.findViewById(R.id.rcPlayerNameEditText);
 
 		rcListView = (ListView) rcView.findViewById(android.R.id.list);
 		rcListView.setAdapter(new ArrayAdapter<String>(getActivity(),
 				android.R.layout.simple_list_item_1, mRCHistory));
 		rcListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> l, View v, int position,
 					long id) {
 				beginSearch("rc", mRCHistory.get(position), true);
 			}
 		});
 		rcListView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> l, View v,
 					int position, long id) {
 				removeFromHistory("rc", mRCHistory.get(position));
 				return true;
 			}
 		});
 
 		rcNameInput.setOnKeyListener(new OnKeyListener() {
 
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getAction() == KeyEvent.ACTION_UP
 						&& keyCode == KeyEvent.KEYCODE_ENTER) {
 					beginSearch("rc", ((EditText) v).getText().toString(), true);
 					return true;
 				}
 				return false;
 			}
 		});
 
 		rcSearchButton = (Button) rcView.findViewById(R.id.searchButton);
 		rcSearchButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				beginSearch("rc", rcNameInput.getText().toString(), true);
 			}
 		});
 
 		View usattView = inflater.inflate(
 				R.layout.fragment_player_search_usatt, null, false);
 		((TextView) usattView.findViewById(R.id.title)).setText("USATT");
 		final EditText usattNameInput = (EditText) usattView
 				.findViewById(R.id.usattPlayerNameEditText);
 
 		usattListView = (ListView) usattView.findViewById(android.R.id.list);
 		usattListView.setAdapter(new ArrayAdapter<String>(getActivity(),
 				android.R.layout.simple_list_item_1, mUSATTHistory));
 		usattListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> l, View v, int position,
 					long id) {
 				beginSearch("usatt", mUSATTHistory.get(position), true);
 			}
 		});
 		usattListView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> l, View v,
 					int position, long id) {
 				removeFromHistory("usatt", mUSATTHistory.get(position));
 				return true;
 			}
 		});
 
 		usattNameInput.setOnKeyListener(new OnKeyListener() {
 
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getAction() == KeyEvent.ACTION_UP
 						&& keyCode == KeyEvent.KEYCODE_ENTER) {
 					beginSearch("usatt", ((EditText) v).getText().toString(),
 							true);
 					return true;
 				}
 				return false;
 			}
 		});
 
 		usattSearchButton = (Button) usattView.findViewById(R.id.searchButton);
 		usattSearchButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				beginSearch("usatt", usattNameInput.getText().toString(), true);
 			}
 		});
 
 		work.addView(usattView);
 		work.addView(rcView);
 
 		work.setTouchSlop(32);
 		work.setOnLoadListener(new OnLoadListener() {
 
 			@Override
 			public void onLoad() {
 				work.scrollToScreenImmediate(mCurrentScreen);
 			}
 		});
 		work.setOnScrollToScreenListener(new OnScrollToScreenListener() {
 
 			@Override
 			public void OnScrollToScreen(int screen) {
 				mCurrentScreen = screen;
 				if (screen == 0) {
 					usattNameInput.requestFocus();
 				} else if (screen == 1) {
 					rcNameInput.requestFocus();
 				}
 			}
 		});
 
 		SearchFragmentTouchListener l = new SearchFragmentTouchListener();
 		work.setOnTouchListener(l);
 		rcView.setOnTouchListener(l);
 		usattView.setOnTouchListener(l);
 		rcListView.setOnTouchListener(l);
 		usattListView.setOnTouchListener(l);
 		rcNameInput.setOnTouchListener(l);
 		usattNameInput.setOnTouchListener(l);
 		rcSearchButton.setOnTouchListener(l);
 		usattSearchButton.setOnTouchListener(l);
 
 		return work;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		View contentFrame = getActivity().findViewById(R.id.content);
 		app.DualPane = contentFrame != null
 				&& contentFrame.getVisibility() == View.VISIBLE;
 
 		if (app.DualPane) {
 			rcListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			usattListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 
 			rcSearchButton.setVisibility(View.GONE);
 			usattSearchButton.setVisibility(View.GONE);
 		}
 
 		debug("Current navigation is now " + app.CurrentNavigation.toString());
 
 		if (app.CurrentNavigation == Navigation.SEARCHING) {
 			if ("rc".equals(mProvider)) {
 				showProgressDialog(String.format("Searching %s for %s",
 						mProvider, mRCQuery), "");
 			} else if ("usatt".equals(mProvider)) {
 				showProgressDialog(String.format("Searching %s for %s",
 						mProvider, mUSATTQuery), "");
 			} else {
 				showProgressDialog(String.format("Searching %s", mProvider), "");
 			}
 		} else if (app.CurrentNavigation == Navigation.LIST || app.DualPane) {
 			boolean screenOrientationChange = app.DualPane
 					&& app.CurrentNavigation != Navigation.LIST;
 			if ("rc".equals(mProvider)) {
 				beginSearch(mProvider, mRCQuery, !screenOrientationChange);
 			} else if ("usatt".equals(mProvider)) {
 				beginSearch(mProvider, mUSATTQuery, !screenOrientationChange);
 			} else {
 				beginSearch(mProvider, null, !screenOrientationChange);
 			}
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getActivity());
 		Editor editor = prefs.edit();
 
 		editor.clear();
 
 		int i = 0;
 		for (String q : mRCHistory) {
 			editor.putString("rchistory" + i++, q);
 		}
 
 		i = 0;
 		for (String q : mUSATTHistory) {
 			editor.putString("usatthistory" + i++, q);
 		}
 		editor.commit();
 	}
 
 	@Override
 	public void onLowMemory() {
 		super.onLowMemory();
 		mRCQuery = null;
 		mUSATTQuery = null;
 		AppEngineParser.getParser().onLowMemory();
 	}
 
 	private ArrayList<String> retrieveHistory(String provider) {
 		ArrayList<String> history = new ArrayList<String>();
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getActivity());
 		for (String hist : prefs.getAll().keySet()) {
 			if (hist.contains(provider + "history")) {
 				String q = prefs.getString(hist, "");
 				if (q != "")
 					history.add(q);
 			}
 		}
 
 		if (history.isEmpty()) {
 			if ("rc".equals(provider)) {
 				history.add("Wei, Mark");
 				history.add("Boll, Timo");
 			} else if ("usatt".equals(provider)) {
 				history.add("Wei");
 				history.add("Boll");
 			}
 		}
 
 		return history;
 	}
 
 	private void removeFromHistory(final String provider, final String item) {
 		new AlertDialog.Builder(getActivity())
 				.setTitle(String.format("Remove %s from History?", item))
 				.setPositiveButton("Remove",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								if ("rc".equals(provider)) {
 									mRCHistory.remove(item);
 									((ArrayAdapter<?>) rcListView.getAdapter())
 											.notifyDataSetChanged();
 								} else if ("usatt".equals(provider)) {
 									mUSATTHistory.remove(item);
 									((ArrayAdapter<?>) usattListView
 											.getAdapter())
 											.notifyDataSetChanged();
 								}
 							}
 						})
 				.setNeutralButton("Clear All",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								if ("rc".equals(provider)) {
 									mRCHistory.clear();
 									((ArrayAdapter<?>) rcListView.getAdapter())
 											.notifyDataSetChanged();
 								} else if ("usatt".equals(provider)) {
 									mUSATTHistory.clear();
 									((ArrayAdapter<?>) usattListView
 											.getAdapter())
 											.notifyDataSetChanged();
 								}
 							}
 						}).setNegativeButton("Cancel", null).show();
 	}
 
 	protected void beginSearch(String provider, String query, boolean user) {
 		InputMethodManager imm = (InputMethodManager) getActivity()
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
 
 		mProvider = provider;
 		if ("rc".equals(provider)) {
 			mRCQuery = query;
 		} else if ("usatt".equals(provider)) {
 			mUSATTQuery = query;
 		}
 
 		if (provider == null || query == null) {
 			return;
 		}
 
 		String providerName;
 		if ("rc".equals(provider))
 			providerName = "Ratings Central";
 		else if ("usatt".equals(provider))
 			providerName = "USATT";
 		else
 			providerName = provider;
 		showProgressDialog(
 				String.format("Searching %s for %s", providerName, query), "");
 
 		new ProviderSearchTask().execute(getDeviceId(), provider, query,
 				String.valueOf(user));
 	}
 
 	protected void finishSearch(String provider, String query, boolean user,
 			ArrayList<PlayerModel> results) {
 		if ("rc".equals(provider)) {
 			if (mRCQuery != null) {
 				mRCHistory.remove(mRCQuery);
 				mRCHistory.add(0, mRCQuery);
 				((ArrayAdapter<?>) rcListView.getAdapter())
 						.notifyDataSetChanged();
 
 				int position = mRCHistory.indexOf(mRCQuery);
 				rcListView.setItemChecked(position, true);
 
 			}
 		} else if ("usatt".equals(provider)) {
 			if (mUSATTQuery != null) {
 				mUSATTHistory.remove(mUSATTQuery);
 				mUSATTHistory.add(0, mUSATTQuery);
 				((ArrayAdapter<?>) usattListView.getAdapter())
 						.notifyDataSetChanged();
 
 				int position = mUSATTHistory.indexOf(mUSATTQuery);
 				usattListView.setItemChecked(position, true);
 
 			}
 		}
 
 		// remove progress bar dialog
 		try {
			FragmentProgressBar dialog = ((FragmentProgressBar) getFragmentManager()
					.findFragmentByTag("dialog"));
			if (dialog != null)
				dialog.dismiss();
			
 			getFragmentManager().popBackStackImmediate();
 		} catch (Exception ex) {
			Log.d(TAG, ex.getMessage() == null ? "" : ex.getMessage());
 		}
 
 		if (app.DualPane) {
 			FragmentPlayerList fragment = FragmentPlayerList.getInstance(
 					provider, query, results);
 
 			getFragmentManager().beginTransaction()
 					.replace(R.id.content, fragment)
 					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
 					.addToBackStack(null).commit();
 		} else {
 			Intent intent = new Intent();
 			intent.setClass(getActivity(), ActivityPlayerList.class);
 			intent.putExtra("provider", provider);
 			intent.putExtra("query", query);
 			intent.putExtra("players", results);
 			startActivity(intent);
 		}
 
 		if (results == null) {
 			app.CurrentNavigation = Navigation.IDLE;
 			debug("Current navigation is now "
 					+ app.CurrentNavigation.toString());
 		}
 	}
 
 	private void showProgressDialog(String title, String message) {
 		// remove previous progress bar dialog
 		try {
			FragmentProgressBar dialog = ((FragmentProgressBar) getFragmentManager()
					.findFragmentByTag("dialog"));
			if (dialog != null)
				dialog.dismiss();
 		} catch (Exception ex) {
			Log.d(TAG, ex.getMessage() == null ? "" : ex.getMessage());
 		}
 
 		FragmentProgressBar fragment = FragmentProgressBar.getInstance(title,
 				message, ProgressBarState.INDETERMINATE.getCode());
 		if (app.DualPane) {
 			getFragmentManager().beginTransaction()
					.replace(R.id.content, fragment)
 					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
 					.addToBackStack(null).commit();
 		} else {
 			fragment.show(getFragmentManager(), "dialog");
 		}
 	}
 
 	private void debug(String msg) {
 		((Debuggable) getActivity()).debug(msg);
 	}
 
 	private String getDeviceId() {
 		TelephonyManager manager = (TelephonyManager) getActivity()
 				.getSystemService(Context.TELEPHONY_SERVICE);
 		String id = manager.getDeviceId();
 
 		if (id == null || id.equals("")) {
 			id = "unknown";
 		}
 		return id;
 	}
 
 	private class ProviderSearchTask extends
 			AsyncTask<String, Void, ArrayList<PlayerModel>> {
 		String provider, query, id;
 		boolean user;
 
 		@Override
 		protected ArrayList<PlayerModel> doInBackground(String... params) {
 			try {
 				id = params[0];
 				provider = params[1];
 				query = params[2];
 				user = Boolean.parseBoolean(params[3]);
 
 				if (user) {
 					app.CurrentNavigation = Navigation.SEARCHING;
 					debug("Current navigation is now "
 							+ app.CurrentNavigation.toString());
 				}
 
 				AppEngineParser parser = AppEngineParser.getParser();
 
 				return parser.execute(id, provider, query);
 			} catch (Exception ex) {
 				return null;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(ArrayList<PlayerModel> result) {
 			if (user) {
 				if (result == null)
 					app.CurrentNavigation = Navigation.IDLE;
 				else
 					app.CurrentNavigation = Navigation.LIST;
 				debug("Current navigation is now "
 						+ app.CurrentNavigation.toString());
 			}
 
 			finishSearch(provider, query, user, result);
 		}
 	}
 
 	private class SearchFragmentTouchListener implements View.OnTouchListener {
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			if (app.CurrentNavigation == Navigation.LIST) {
 				app.CurrentNavigation = Navigation.IDLE;
 				debug("Current navigation is now "
 						+ app.CurrentNavigation.toString());
 			}
 			return false;
 		}
 
 	}
 }
