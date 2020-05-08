 package zhang.abel.memmo.android;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import zhang.abel.memmo.android.entities.Album;
 import zhang.abel.memmo.android.repositories.AlbumRepository;
 
 public class AlbumCreateActivity extends Activity {
 
     private AlbumRepository albumRepository;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.createalbum);
         albumRepository = new AlbumRepository();
 
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.show();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuItem cancel = menu.add(0, 1, 0, R.string.back);
         cancel.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
         cancel.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 Intent intent = new Intent(AlbumCreateActivity.this, AlbumListActivity.class);
                 startActivity(intent);
                 return true;
             }
         });
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
         Intent intent;
         switch (item.getItemId()) {
             case android.R.id.home:
                 intent = new Intent(this, AlbumListActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
             Intent intent = new Intent(this, AlbumListActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             return true;
         } else
             return super.onKeyDown(keyCode, event);
     }
 
     public void createAlbum(View view) {
         EditText txtName = (EditText) findViewById(R.id.txt_name);
 
         String albumName = txtName.getText().toString();
         if (!albumName.isEmpty()) {
             albumRepository.create(new Album(albumName));
         }
 
        Intent intent = new Intent(AlbumCreateActivity.this, AlbumListActivity.class);
        startActivity(intent);
     }
 }
