 package se.kd35a.blekingskaSangbok;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class ListSongsActivity extends ListActivity implements OnItemClickListener {
 	private ArrayList<Song> songs;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		DatabaseHelper dbHelper = new DatabaseHelper(this);
 		songs = dbHelper.getSongs();
 		setListAdapter(new SongArrayAdapter(this, songs));
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 		lv.setOnItemClickListener(this);
 		registerForContextMenu(lv);
 	}


 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Intent intent = new Intent(this, ViewSongActivity.class);
 		intent.putExtra("song", songs.get(position).getId());
 		startActivity(intent);
 	}
 	
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	return true;
     }
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
 		case R.id.add_url:
 			addFromUrl();
 			return true;
 		case R.id.delete_all:
 			deleteAll();
 			return true;
 		}
     	return super.onOptionsItemSelected(item);
     }
 	
 	private void deleteAll() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCancelable(false);
 		builder.setMessage(getString(R.string.delete_all_warning));
 		builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				DatabaseHelper dbHelper = new DatabaseHelper(ListSongsActivity.this);
 				dbHelper.deleteAllSong();
 				refreshSongList();
 				getListView().postInvalidate();
 			}
 		});
 		builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
 		builder.create().show();
 	}


 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		if (v.getId() == getListView().getId()) {
 			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 			Song song = songs.get(info.position);
 			menu.setHeaderTitle(song.getTitle());
 			String[] menuItems = getResources().getStringArray(R.array.context_menu_options);
 			for (int i = 0; i < menuItems.length; i++) {
 				menu.add(Menu.NONE, i, i, menuItems[i]);
 			}
 		}
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// If Delete song was chosen
 		if (item.getItemId() == 0) {
 			final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 			builder.setMessage(getString(R.string.delete_warning));
 			builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					DatabaseHelper dbHelper = new DatabaseHelper(ListSongsActivity.this);
 					dbHelper.deleteSong(songs.get(info.position).getId());
 					refreshSongList();
 					getListView().postInvalidate();
 				}
 			});
 			builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			});
 			builder.create().show();
 			return true;
 		}
 		return false;
 	}
 	
 	private void addFromUrl() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		
 		alert.setTitle(getString(R.string.add_url_title));
 		alert.setMessage(getString(R.string.add_url_message));
 		
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		alert.setView(input);
 		
 		alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String url = input.getText().toString();
 				DatabaseHelper dbHelper = new DatabaseHelper(ListSongsActivity.this);
 				dbHelper.addToDatabaseFromUrl(url, null);
 				
 				ListSongsActivity.this.refreshSongList();
 				ListSongsActivity.this.getListView().postInvalidate();
 			}
 		});
 		
 		alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				dialog.cancel();
 			}
 		});
 		
 		alert.show();
 	}
 
 
 	protected void refreshSongList() {
 		DatabaseHelper dbHelper = new DatabaseHelper(this);
 		songs = dbHelper.getSongs();
 		setListAdapter(new SongArrayAdapter(this, songs));
 	}
 
 }
