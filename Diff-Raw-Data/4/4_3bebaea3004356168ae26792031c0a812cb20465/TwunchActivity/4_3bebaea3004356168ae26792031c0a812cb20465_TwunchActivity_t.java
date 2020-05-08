 /**
  *	Copyright 2010-2011 Norio bvba
  *
  *	This program is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation, either version 3 of the License, or
  *	(at your option) any later version.
  *	
  *	This program is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *	
  *	You should have received a copy of the GNU General Public License
  *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.norio.twunch.android;
 
 import greendroid.app.GDActivity;
 import greendroid.widget.ActionBarItem;
 import greendroid.widget.ActionBarItem.Type;
 
 import java.util.Arrays;
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Nickname;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.QuickContact;
 import android.text.Html;
 import android.text.format.DateUtils;
 import android.text.method.LinkMovementMethod;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.TextView;
 
 import com.cyrilmottier.android.greendroid.R;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 public class TwunchActivity extends GDActivity {
 
 	public static String PARAMETER_ID = "id";
 
 	private final static int MENU_MAP = 0;
 	private final static int MENU_REGISTER = 1;
 	private final static int MENU_SHARE = 2;
 	private final static int MENU_DIRECTIONS = 3;
 
 	private static String[] columns = new String[] { BaseColumns._ID, TwunchManager.COLUMN_TITLE, TwunchManager.COLUMN_ADDRESS,
 			TwunchManager.COLUMN_DATE, TwunchManager.COLUMN_NUMPARTICIPANTS, TwunchManager.COLUMN_LATITUDE,
 			TwunchManager.COLUMN_LONGITUDE, TwunchManager.COLUMN_PARTICIPANTS, TwunchManager.COLUMN_NOTE, TwunchManager.COLUMN_LINK,
 			TwunchManager.COLUMN_CLOSED };
 	private static final int COLUMN_DISPLAY_TITLE = 1;
 	private static final int COLUMN_DISPLAY_ADDRESS = 2;
 	private static final int COLUMN_DISPLAY_DATE = 3;
 	private static final int COLUMN_DISPLAY_NUMPARTICIPANTS = 4;
 	private static final int COLUMN_DISPLAY_LATITUDE = 5;
 	private static final int COLUMN_DISPLAY_LONGITUDE = 6;
 	private static final int COLUMN_DISPLAY_PARTICIPANTS = 7;
 	private static final int COLUMN_DISPLAY_NOTE = 8;
 	private static final int COLUMN_DISPLAY_LINK = 9;
 	private static final int COLUMN_DISPLAY_CLOSED = 10;
 
 	DatabaseHelper dbHelper;
 	SQLiteDatabase db;
 	Cursor cursor;
 	String[] participants;
 	Float distance;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		GoogleAnalyticsTracker.getInstance().trackPageView("Twunch");
 		setActionBarContentView(R.layout.twunch);
 		dbHelper = new DatabaseHelper(this);
 		db = dbHelper.getReadableDatabase();
 		cursor = db.query(TwunchManager.TABLE_NAME, columns,
 				BaseColumns._ID + " = " + String.valueOf(getIntent().getIntExtra(PARAMETER_ID, 0)), null, null, null, null);
		if (!cursor.moveToFirst()) {
			finish();
		}
 
 		TwunchManager.getInstance().setTwunchRead(this, cursor.getInt(0));
 
 		// Title
 		((TextView) findViewById(R.id.twunchTitle)).setText(cursor.getString(COLUMN_DISPLAY_TITLE));
 		// Address
 		((TextView) findViewById(R.id.twunchAddress)).setText(cursor.getString(COLUMN_DISPLAY_ADDRESS));
 		// Distance
 		distance = TwunchManager.getInstance().getDistanceToTwunch(this, cursor.getFloat(COLUMN_DISPLAY_LATITUDE),
 				cursor.getFloat(COLUMN_DISPLAY_LONGITUDE));
 		((TextView) findViewById(R.id.twunchDistance)).setText(String.format(getString(R.string.distance), distance));
 		findViewById(R.id.twunchDistance).setVisibility(distance == null ? View.GONE : View.VISIBLE);
 		findViewById(R.id.twunchDistanceSeparator).setVisibility(distance == null ? View.GONE : View.VISIBLE);
 		// Date
 		((TextView) findViewById(R.id.twunchDate)).setText(String.format(
 				getString(R.string.date),
 				DateUtils.formatDateTime(this, cursor.getLong(COLUMN_DISPLAY_DATE), DateUtils.FORMAT_SHOW_WEEKDAY
 						| DateUtils.FORMAT_SHOW_DATE),
 				DateUtils.formatDateTime(this, cursor.getLong(COLUMN_DISPLAY_DATE), DateUtils.FORMAT_SHOW_TIME)));
 		// Days
 		final long msInDay = 86400000;
 		int days = (int) (cursor.getLong(COLUMN_DISPLAY_DATE) / msInDay - new Date().getTime() / msInDay);
 		((TextView) findViewById(R.id.twunchDays)).setText(days == 0 ? getString(R.string.today) : String.format(getResources()
 				.getQuantityString(R.plurals.days_to_twunch, days), days));
 		// Note
 		TextView noteView = ((TextView) findViewById(R.id.twunchNote));
 		if (cursor.getString(COLUMN_DISPLAY_NOTE) == null || cursor.getString(COLUMN_DISPLAY_NOTE).length() == 0) {
 			noteView.setVisibility(View.GONE);
 		} else {
 			noteView.setMovementMethod(LinkMovementMethod.getInstance());
 			noteView.setText(Html.fromHtml(cursor.getString(COLUMN_DISPLAY_NOTE)));
 			noteView.setVisibility(View.VISIBLE);
 		}
 		// Number of participants
 		((TextView) findViewById(R.id.twunchNumberParticipants)).setText(String.format(
 				getResources().getQuantityString(R.plurals.numberOfParticipants, cursor.getInt(COLUMN_DISPLAY_NUMPARTICIPANTS)),
 				cursor.getInt(COLUMN_DISPLAY_NUMPARTICIPANTS)));
 		// Participants
 		GridView participantsView = ((GridView) findViewById(R.id.twunchParticipants));
 		participants = cursor.getString(COLUMN_DISPLAY_PARTICIPANTS).split(" ");
 		Arrays.sort(participants, String.CASE_INSENSITIVE_ORDER);
 		participantsView.setAdapter(new ContactAdapter(this));
 
 		// participantsView.setText(cursor.getString(COLUMN_DISPLAY_PARTICIPANTS));
 		// Linkify.addLinks(participantsView, Pattern.compile("@([A-Za-z0-9-_]+)"),
 		// "http://twitter.com/");
 
 		addActionBarItem(Type.Add);
 		addActionBarItem(Type.Share);
 		if (distance != null) {
 			addActionBarItem(Type.Locate);
 		}
 
 	}
 
 	private class ContactAdapter extends BaseAdapter {
 		private final Context context;
 
 		public ContactAdapter(Context c) {
 			context = c;
 		}
 
 		@Override
 		public int getCount() {
 			return participants.length;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return participants[position];
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			final TextView textView;
 			if (convertView == null) {
 				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 				textView = (TextView) inflater.inflate(R.layout.participant, null);
 			} else {
 				textView = (TextView) convertView;
 			}
 			textView.setText("@" + participants[position]);
 			textView.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY };
 					Cursor rawTwitterContact = getContentResolver().query(Data.CONTENT_URI, projection, Nickname.NAME + " = ?",
 							new String[] { participants[position] }, null);
 					if (rawTwitterContact.getCount() > 0) {
 						// Show the QuickContact action bar
 						rawTwitterContact.moveToFirst();
 						final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI,
 								rawTwitterContact.getString(rawTwitterContact.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
 						QuickContact.showQuickContact(context, textView, contactUri, ContactsContract.QuickContact.MODE_LARGE, null);
 					} else {
 						// Show the twitter profile
 						final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://twitter.com/"
 								+ participants[position]));
 						startActivity(myIntent);
 					}
 				}
 			});
 			return textView;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_REGISTER, 0, R.string.button_register).setIcon(R.drawable.ic_menu_add);
 		menu.add(0, MENU_SHARE, 0, R.string.menu_share).setIcon(R.drawable.ic_menu_share);
 		if (distance != null) {
 			menu.add(0, MENU_MAP, 0, R.string.button_map).setIcon(R.drawable.ic_menu_mapmode);
 			menu.add(0, MENU_DIRECTIONS, 0, R.string.menu_directions).setIcon(android.R.drawable.ic_menu_directions);
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_MAP:
 			doMap();
 			return true;
 		case MENU_REGISTER:
 			doRegister();
 			return true;
 		case MENU_SHARE:
 			doShare();
 			return true;
 		case MENU_DIRECTIONS:
 			doDirections();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Show the location of this Twunch on a map.
 	 */
 	private void doMap() {
 		final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q="
 				+ cursor.getDouble(COLUMN_DISPLAY_LATITUDE) + "," + cursor.getDouble(COLUMN_DISPLAY_LONGITUDE)));
 		startActivity(myIntent);
 	}
 
 	/**
 	 * Show the directions to this Twunch.
 	 */
 	private void doDirections() {
 		startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q="
 				+ cursor.getDouble(COLUMN_DISPLAY_LATITUDE) + "," + cursor.getDouble(COLUMN_DISPLAY_LONGITUDE))));
 	}
 
 	/**
 	 * Register for this Twunch.
 	 */
 	private void doRegister() {
 		if (cursor.getInt(COLUMN_DISPLAY_CLOSED) == 1) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(R.string.register_closed);
 			builder.setCancelable(false);
 			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// Do nothing
 				}
 			});
 			builder.create().show();
 
 		} else {
 			final Intent intent = new Intent(Intent.ACTION_SEND);
 			intent.setType("text/plain");
 			intent.putExtra(
 					Intent.EXTRA_TEXT,
 					String.format(getString(R.string.register_text), cursor.getString(COLUMN_DISPLAY_TITLE),
 							cursor.getString(COLUMN_DISPLAY_LINK)));
 			startActivity(Intent.createChooser(intent, getString(R.string.register_title)));
 
 		}
 	}
 
 	/**
 	 * Share information about this Twunch.
 	 */
 	private void doShare() {
 		final Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.setType("text/plain");
 		intent.putExtra(
 				Intent.EXTRA_TEXT,
 				String.format(
 						getString(R.string.share_text),
 						cursor.getString(COLUMN_DISPLAY_TITLE),
 						DateUtils.formatDateTime(this, cursor.getLong(COLUMN_DISPLAY_DATE), DateUtils.FORMAT_SHOW_WEEKDAY
 								| DateUtils.FORMAT_SHOW_DATE),
 						DateUtils.formatDateTime(this, cursor.getLong(COLUMN_DISPLAY_DATE), DateUtils.FORMAT_SHOW_TIME),
 						cursor.getString(COLUMN_DISPLAY_LINK)));
 		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
 		intent.putExtra(Intent.EXTRA_EMAIL, "");
 		startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
 	}
 
 	@Override
 	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
 		switch (position) {
 		case 0:
 			doRegister();
 			break;
 		case 1:
 			doShare();
 			break;
 		case 2:
 			doMap();
 			break;
 		default:
 			return false;
 		}
 		return true;
 	}
 
 }
