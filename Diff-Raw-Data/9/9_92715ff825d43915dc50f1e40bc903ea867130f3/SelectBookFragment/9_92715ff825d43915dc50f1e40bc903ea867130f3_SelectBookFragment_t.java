 package ua.maker.gbible.fragment;
 
 import java.io.IOException;
 
 import ua.maker.gbible.R;
 import ua.maker.gbible.activity.ReadForEveryDayActivity;
 import ua.maker.gbible.activity.SettingActivity;
 import ua.maker.gbible.constant.App;
 import ua.maker.gbible.listeners.onDialogClickListener;
 import ua.maker.gbible.utils.DataBase;
 import ua.maker.gbible.utils.Tools;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class SelectBookFragment extends SherlockFragment {
 	
 	private static final String TAG = "StartFragment";
 	
 	private View view = null;
 	private ListView lvShowBooks = null;
 	private ArrayAdapter<String> adapter = null;
 	
 	private DataBase dataBase = null;
 	
 	private Button btnBook = null;
 	private Button btnChapter = null;
 	private Button btnPoem = null;
 	
 	private SharedPreferences sp = null, defPref = null;
 	
 	private int positionTop = 0;
 	private int positionClicked = 0;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		view = inflater.inflate(R.layout.activity_select_book, null);
 		lvShowBooks = (ListView)view.findViewById(R.id.lv_show_books);
 		
 		btnBook = (Button)view.findViewById(R.id.btn_book);
 		btnChapter = (Button)view.findViewById(R.id.btn_chapter);
 		btnPoem = (Button)view.findViewById(R.id.btn_poem);
 		if(!getSherlockActivity().getActionBar().isShowing())
 			getSherlockActivity().getActionBar().show();
 		
 		Log.d(TAG, "Start activity create");
 		return view;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setHasOptionsMenu(true);
 		getSherlockActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
 		getSherlockActivity().getActionBar().setTitle(getString(R.string.app_name));
 		dataBase = new DataBase(getSherlockActivity());
 		
 		try {
 			dataBase.createDataBase();
 		} catch (IOException e) {}
 		dataBase.openDataBase();
 		
 		btnBook.setOnClickListener(clickTestamentListener);
 		
 		sp = getSherlockActivity().getSharedPreferences(App.PREF_SEND_DATA, 0);
 		defPref = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
 
 		if(sp.contains(App.BOOK_ID) && sp.contains(App.CHAPTER)){
 			FragmentTransaction ft = getFragmentManager().
 					 beginTransaction();
 			ft.replace(R.id.flRoot, new ListPoemsFragment(), App.TAG_FRAGMENT_POEMS);
 			ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
 			ft.addToBackStack(null);
 			ft.commit();
 		}		
 		
 		btnChapter.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(sp.contains(App.BOOK_ID)){
 					FragmentTransaction ft = getFragmentManager().
 							 beginTransaction();
 					ft.replace(R.id.flRoot, new ListChaptersFragment(), App.TAG_FRAGMENT_CHAPTERS);
 					ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
 					ft.addToBackStack(null);
 					ft.commit();
 				}
 				else
 					Tools.showToast(getSherlockActivity(), getString(R.string.no_select_book));
 			}
 		});
 		
 		btnPoem.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(sp.contains(App.BOOK_ID) && sp.contains(App.CHAPTER)){
 					FragmentTransaction ft = getFragmentManager().
 							 beginTransaction();
 					ft.replace(R.id.flRoot, new ListPoemsFragment(), App.TAG_FRAGMENT_POEMS);
 					ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
 					ft.addToBackStack(null);
 					ft.commit();
 				}
 				else
 					Tools.showToast(getSherlockActivity(), getString(R.string.no_select_book));
 			}
 		});
 
 		adapter = new ArrayAdapter<String>(getSherlockActivity(), 
 				android.R.layout.simple_list_item_1,
 				dataBase.getBooks(DataBase.TABLE_NAME_RST));
 		lvShowBooks.setAdapter(adapter);
 		lvShowBooks.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int position,
 					long id) {
 				sp.edit().putInt(App.BOOK_SET_FOCUS, position).commit();
 				SharedPreferences sp = getSherlockActivity().getSharedPreferences(App.PREF_SEND_DATA, 0);
 				positionClicked = position;
 				sp.edit().putInt(App.BOOK_SET_FOCUS, position).commit();
 				Editor editor = sp.edit();
 				editor.putInt(App.BOOK_ID, position+1);
 				editor.commit();
 				FragmentTransaction ft = getFragmentManager().
 						 beginTransaction();
 				ft.replace(R.id.flRoot, new ListChaptersFragment(), App.TAG_FRAGMENT_CHAPTERS);
 				ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
 				ft.addToBackStack(null);
 				ft.commit();
 			}
 		});
 		
 		lvShowBooks.setOnScrollListener(scrolListViewListener);
 		
 		boolean isFirstStartBookSelect = true;
 		if(sp.contains("is_first_start_select_book_fragment")){
 			isFirstStartBookSelect = sp.getBoolean("is_first_start_select_book_fragment", true);
 		}
 		
 		if(isFirstStartBookSelect){
 			showDialogSelectTranslate();
 		}
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		EasyTracker.getInstance().activityStart(getSherlockActivity());
 		int pos = sp.getInt(App.BOOK_SET_FOCUS, 0);
 		selectPrefBook(pos);
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		EasyTracker.getInstance().activityStop(getSherlockActivity());
 	}
 	
 	private void showDialogSelectTranslate() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
 		builder.setTitle(R.string.dialogtitle_def_trans);
 		String[] listTrnsl = getSherlockActivity().getResources().getStringArray(R.array.trnaslaters_names);
 		builder.setSingleChoiceItems(listTrnsl, -1, new onDialogClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				defPref.edit().putString(getString(R.string.pref_default_translaters), ""+which).commit();
 				dialog.cancel();
 			}
 		});
 		builder.setNegativeButton(getString(R.string.dialog_cancel), new onDialogClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();				
 			}
 		});
 		builder.create().show();
 		sp.edit().putBoolean("is_first_start_select_book_fragment", false).commit();
 	}
 
 	private OnClickListener clickTestamentListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			if(positionTop < 39){
 				btnBook.setText(getString(R.string.new_testament));
 				lvShowBooks.setSelection(39);
 			}
 			else
 			{
 				btnBook.setText(getString(R.string.old_testament));
 				lvShowBooks.setSelection(0);
 			}
 		}
 	};
 	
 	private OnScrollListener scrolListViewListener = new OnScrollListener() {		
 		@Override
 		public void onScrollStateChanged(AbsListView view, int scrollState) {}		
 		@Override
 		public void onScroll(AbsListView view, int firstVisibleItem,
 				int visibleItemCount, int totalItemCount) {
 			positionTop = firstVisibleItem;
 			if(positionTop >= 39){
 				btnBook.setText(getString(R.string.new_testament));
 			}
 			else
 			{
 				btnBook.setText(getString(R.string.old_testament));
 			}
 		}
 	};
 	
 	private void selectPrefBook(int position){
 		lvShowBooks.setSelection(position);
 		lvShowBooks.smoothScrollToPosition(position);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		int pos = sp.getInt(App.BOOK_SET_FOCUS, 0);
 		selectPrefBook(pos);
 	};
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		sp.edit().putInt(App.BOOK_SET_FOCUS, positionClicked).commit();
 	};
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_main_in_list_poem, menu);
 		menu.setQwertyMode(true);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 	    
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	   	switch(item.getItemId()){
 	   	case R.id.action_exit:
 	   		getSherlockActivity().finish();
 	   		return true;
 	   	case R.id.action_setting_app:
 	   		Intent startSetting = new Intent(getSherlockActivity(), SettingActivity.class);
 			startActivity(startSetting);
 	   		return true;
 	   	case R.id.action_read_for_every_day:
 			Intent startRead = new Intent(getSherlockActivity(), ReadForEveryDayActivity.class);
 			getSherlockActivity().startActivity(startRead);
 			return true;
 	   	}
 	   	return super.onOptionsItemSelected(item);
 	}
 
 }
