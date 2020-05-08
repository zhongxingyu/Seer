 package zhang.abel.memmo.android;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.ListView;
 import zhang.abel.memmo.android.adapters.AlbumListAdapter;
 import zhang.abel.memmo.android.entities.Album;
 import zhang.abel.memmo.android.repositories.AlbumRepository;
 
 import java.util.List;
 
 public class AlbumListActivity extends ListActivity {
     public final static String SER_KEY = "";
     AlbumRepository albumRepository = new AlbumRepository();
     private List<Album> albums;
     private AlbumListAdapter adapter;
 
     public AlbumListActivity() {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         renderAlbumListPage();
 
         ListView listView = getListView();
         listView.setOnTouchListener(new View.OnTouchListener() {
 
             float x
                     ,
                     y
                     ,
                     upx
                     ,
                     upy;
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_DOWN) {
                     x = event.getX();
                     y = event.getY();
                 }
                 if (event.getAction() == MotionEvent.ACTION_UP) {
                     upx = event.getX();
                     upy = event.getY();
 
                     int position1 = ((ListView) v).pointToPosition((int) x, (int) y);
                     int position2 = ((ListView) v).pointToPosition((int) upx, (int) upy);
                     if (position1 == position2 && Math.abs(x - upx) > 10) {
                         v = ((ListView) v).getChildAt(position1);
                         removeListItem(v, position1);
                     }
                 }
                 return false;
             }
         });
     }
 
     protected void removeListItem(View rowView, final int position) {
         albums.remove(position);
         //TODO: need to delete the album folder and all photos inside it.
         //Create dialog to confirm with user whether he wants to delete or not
         runOnUiThread(new Runnable() {
             public void run() {
                 adapter.notifyDataSetChanged();
             }
         });
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuItem create = menu.add(0, 1, 0, R.string.btn_create);
 
         MenuItem edit = menu.add(0, 2, 1, R.string.edit);
 
         create.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         edit.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case 1:
                 Intent intent = new Intent(this, AlbumCreateActivity.class);
                startActivity(intent);
                 return true;
             case 2:
                 //The Edit logic.
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         Intent mIntent = new Intent(this, AlbumActivity.class);
         Bundle mBundle = new Bundle();
         mBundle.putSerializable(SER_KEY, albums.get(position));
         mIntent.putExtras(mBundle);
         startActivity(mIntent);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
             showExitDialog();
             return true;
         } else
             return super.onKeyDown(keyCode, event);
     }
 
     private void showExitDialog() {
         Dialog dialog = new AlertDialog.Builder(this)
                 .setTitle(R.string.exit_title)
                 .setMessage(R.string.exit_msg)
                 .setPositiveButton(R.string.exit_ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         finish();
                     }
                 })
                 .setNeutralButton(R.string.exit_cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                     }
                 })
                 .create();
         dialog.show();
     }
 
     private void renderAlbumListPage() {
         albums = albumRepository.list();
         initAlbumList(albums, false);
         showActionBar();
     }
 
     private void initAlbumList(List<Album> albumList, boolean isRefresh) {
         if (albumList.size() == 0) {
             return;
         }
         adapter = new AlbumListAdapter(this, albumList);
         if (!isRefresh) {
             setListAdapter(adapter);
         } else {
             adapter.notifyDataSetChanged();
         }
     }
 
     private void showActionBar() {
         ActionBar actionBar = getActionBar();
         actionBar.show();
     }
 
 }
