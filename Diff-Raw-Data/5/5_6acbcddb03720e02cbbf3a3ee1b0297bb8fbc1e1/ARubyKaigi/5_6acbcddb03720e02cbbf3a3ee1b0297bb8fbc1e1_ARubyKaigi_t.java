 package net.takkaw.arubykaigi2011;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class ARubyKaigi extends Activity implements OnItemSelectedListener,
 		TextWatcher, OnItemClickListener {
 
 	private static DBHelper dbHelper;
 	private static Spinner day_selecter;
 	private static Spinner room_selecter;
 	private static Spinner lang_selecter;
 	private static ListView list_view;
 	private static EditText search_box;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// No Window
 		// requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
 		
 		setContentView(R.layout.main);
 		// Listener settings
 		day_selecter = (Spinner) findViewById(R.id.day_selecter);
 		day_selecter.setOnItemSelectedListener(this);
 		room_selecter = (Spinner) findViewById(R.id.room_selecter);
 		room_selecter.setOnItemSelectedListener(this);
 		lang_selecter = (Spinner) findViewById(R.id.lang_selecter);
 		lang_selecter.setOnItemSelectedListener(this);
 		search_box = (EditText) findViewById(R.id.search_box);
 		search_box.addTextChangedListener(this);
 
 		dbHelper = new DBHelper(this);
 
 		list_view = (ListView) findViewById(R.id.list);
 		update_list();
 		list_view.setEmptyView(findViewById(R.id.empty));
 		list_view.setOnItemClickListener(this);		
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		requery();
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 			long arg3) {
 		update_list();
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 	}
 
 	@Override
 	public void afterTextChanged(Editable s) {
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 	}
 
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		update_list();
 	}
 
 	private void update_list() {
 		String day = (String) day_selecter.getSelectedItem();
 		String room = (String) room_selecter.getSelectedItem();
 		String lang = (String) lang_selecter.getSelectedItem();
 		String keyword = search_box.getText().toString();
 		if (day.equals(getResources().getStringArray(R.array.days)[0]))
 			day = null;
 		if (room.equals(getResources().getStringArray(R.array.rooms)[0]))
 			room = null;
 		if (lang.equals(getResources().getStringArray(R.array.langs)[0]))
 			lang = null;
 		Cursor cursor = dbHelper.formSearch(day, room, lang, keyword);
 		this.startManagingCursor(cursor);
 //		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.item, cursor, DBHelper.FROM, TO);
 		CustomAdapter adapter = new CustomAdapter(this, cursor);
 		list_view.setAdapter(adapter);
 	}
 	
 	public void requery(){
 		String day = (String) day_selecter.getSelectedItem();
 		String room = (String) room_selecter.getSelectedItem();
 		String lang = (String) lang_selecter.getSelectedItem();
 		String keyword = search_box.getText().toString();
 		if (day.equals(getResources().getStringArray(R.array.days)[0]))
 			day = null;
 		if (room.equals(getResources().getStringArray(R.array.rooms)[0]))
 			room = null;
 		if (lang.equals(getResources().getStringArray(R.array.langs)[0]))
 			lang = null;
 		Cursor cursor = dbHelper.formSearch(day, room, lang, keyword);
 		CustomAdapter adapter = (CustomAdapter) list_view.getAdapter();
 		adapter.changeCursor(cursor);
 		adapter.notifyDataSetChanged();
 	}
 
 	// Menu
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Uri uri;
 		Intent intent;
 		switch (item.getItemId()) {
 		case R.id.menu_map:
 			intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=練馬区立練馬文化センター"));
 			startActivity(intent);
 			return true;
 		case R.id.menu_info:
 			new AlertDialog.Builder(this).setTitle(
 			getResources()
 			.getString(R.string.menu_info_title))
 			.setMessage(getResources().getString(R.string.menu_info_message))
 			.show();
 			return true;
 		case R.id.menu_guide:
 			uri = Uri.parse("http://jp.rubyist.net/magazine/?preRubyKaigi2011");
 			intent = new Intent(Intent.ACTION_VIEW,uri);
 			startActivity(intent);
 			return true;
 		case R.id.menu_tdiary:
 			uri = Uri.parse("http://rubykaigi.tdiary.net/");
 			intent = new Intent(Intent.ACTION_VIEW,uri);
 			startActivity(intent);
 			return true;
 		case R.id.menu_favorite:
 			intent = new Intent(this, FavoriteActivity.class);
 			startActivity(intent);
 			return true;
 /*
 		case R.id.menu_dbdrop:
 			dbHelper.reCreateDB();
 			return true;
 */
 		}
 		return false;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
 		Intent intent = new Intent(this, Description.class);
 		intent.putExtra("id", (int) id);
 		startActivity(intent);
 	}
 
 	class CustomAdapter extends CursorAdapter{
 		public CustomAdapter(Context context, Cursor c) {
 			super(context, c);
 		}
 
 		@Override
 		public void bindView(View v, Context context, Cursor cursor) {
 			TextView textViewDay = (TextView)v.findViewById(R.id.item_day);
 			TextView textViewRoom = (TextView)v.findViewById(R.id.item_room);
 			TextView textViewStart = (TextView)v.findViewById(R.id.item_start);
 			TextView textViewEnd = (TextView)v.findViewById(R.id.item_end);
 			TextView textViewTitle = (TextView)v.findViewById(R.id.item_title);
 			TextView textViewSpeaker = (TextView)v.findViewById(R.id.item_speaker);
 			
 			String day = cursor.getString(cursor.getColumnIndex("day"));
 			String start = cursor.getString(cursor.getColumnIndex("start"));
 			String end = cursor.getString(cursor.getColumnIndex("end"));
 			textViewDay.setText(day);
 			textViewStart.setText(start);
 			textViewEnd.setText(end);
 			
 			String room = "";
 			String desc = "";
 			String speaker = "";
 			String speakerBio = "";
 			if (Locale.getDefault().equals(Locale.JAPANESE) || Locale.getDefault().equals(Locale.JAPAN)){
 				room = cursor.getString(cursor.getColumnIndex("room_ja"));
 				desc = cursor.getString(cursor.getColumnIndex("desc_ja"));
 				speaker = cursor.getString(cursor.getColumnIndex("speaker_ja"));
 				speakerBio = cursor.getString(cursor.getColumnIndex("speaker_bio_ja"));
 				textViewTitle.setText(cursor.getString(cursor.getColumnIndex("title_ja")));
 			} else {
 				room = cursor.getString(cursor.getColumnIndex("room_en"));
 				desc = cursor.getString(cursor.getColumnIndex("desc_ja"));
 				speaker = cursor.getString(cursor.getColumnIndex("speaker_en"));
 				speakerBio = cursor.getString(cursor.getColumnIndex("speaker_bio_en"));
 				textViewTitle.setText(cursor.getString(cursor.getColumnIndex("title_en")));
 			}
 			textViewRoom.setText(room);
 			textViewSpeaker.setText(speaker);
 			LinearLayout subLauout = (LinearLayout)v.findViewById(R.id.item_subLayout);
			if ((room == null || room.length() == 0) && (speaker == null || speaker.length() == 0)){
 				subLauout.setVisibility(View.GONE);
 			} else {
 				subLauout.setVisibility(View.VISIBLE);
 			}
 			LinearLayout linearLayoutFavorite = (LinearLayout)v.findViewById(R.id.item_favoriteLayout);
 			
 			final ToggleButton toggleButtonFavolite = (ToggleButton)v.findViewById(R.id.item_favorite);
 			if (cursor.getInt(cursor.getColumnIndex("favorite")) == 1){
 				toggleButtonFavolite.setChecked(true);
 			}else{
 				toggleButtonFavolite.setChecked(false);
 			}
 			final int id = cursor.getInt(cursor.getColumnIndex("_id")); 
 			linearLayoutFavorite.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Boolean value = !toggleButtonFavolite.isChecked();
 					toggleButtonFavolite.setChecked(value);
 					ARubyKaigi.dbHelper.updateFavorite(id, value);
 					ARubyKaigi.this.requery();
 				}
 			});
 			toggleButtonFavolite.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					Boolean value = ((ToggleButton) v).isChecked();
 					ARubyKaigi.dbHelper.updateFavorite(id, value);
 					ARubyKaigi.this.requery();
 				}
 			});
 
			if ((desc == null || desc.length() == 0) && (speakerBio == null || speakerBio.length() == 0)){
 				v.setClickable(true);
 				linearLayoutFavorite.setVisibility(View.INVISIBLE);
 			} else {
 				v.setClickable(false);
 				linearLayoutFavorite.setVisibility(View.VISIBLE);
 			}
 			
 			LinearLayout timeLayout = (LinearLayout)v.findViewById(R.id.item_timeLayout);
 			if (cursor.moveToPrevious()){
 				if (day.equals(cursor.getString(cursor.getColumnIndex("day"))) 
 						&& start.equals(cursor.getString(cursor.getColumnIndex("start"))) 
 						&& end.equals(cursor.getString(cursor.getColumnIndex("end")))){
 					timeLayout.setVisibility(View.GONE);
 				}else{
 					timeLayout.setVisibility(View.VISIBLE);
 				}
 			}
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			View v = ARubyKaigi.this.getLayoutInflater().inflate(R.layout.item, null);
 			return v;
 		}		
 	}
 	
 }
