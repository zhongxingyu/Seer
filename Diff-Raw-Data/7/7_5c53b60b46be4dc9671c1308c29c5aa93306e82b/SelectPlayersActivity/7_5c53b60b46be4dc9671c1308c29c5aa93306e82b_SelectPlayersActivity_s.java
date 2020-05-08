 package ru.cyberwasp.teamsplitter.activities;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ru.cyberwasp.teamsplitter.Player;
 import ru.cyberwasp.teamsplitter.db.DataSource;
 import ru.cyberwasp.teamsplitter.views.PlayerListView;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SelectPlayersActivity extends Activity {
 
     private static final int PICK_FILE_RESULT_CODE = 1;
 	private PlayerListView view;
     private DataSource datasource;
     private List<Player> players = new ArrayList<Player>();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         view = new PlayerListView(this);
         datasource = new DataSource(this);
         datasource.open();
         fillData();
         setContentView(view);
         view.getSplitButton().setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 getSplitResult();
             }
         });
         registerForContextMenu(view.getList());
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         datasource.open();
         fillData();
     }
 
     @Override
     protected void onPause() {
         datasource.close();
         super.onPause();
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         if (v == view.getList()) {
             menu.setHeaderTitle("Operations");
             menu.add(Menu.NONE, 0, 0, "Edit");
             menu.add(Menu.NONE, 1, 0, "Delete");
         }
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
             case 0:
                 editPlayer(info.position);
             	break;
             case 1:
                 deletePlayer(info.position);
                 break;
         }
         return true;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuItem item = menu.add(0, 0, 0, "New player");
         item.setIcon(android.R.drawable.ic_menu_add);
         item = menu.add(0, 1, 0, "Load");
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case 0:
                 addPlayer();
                 break;
             case 1:
             	loadPlayersStart();
             	break;
         }
         return true;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data){
         switch (requestCode) {
             case PICK_FILE_RESULT_CODE: {
                 if (resultCode==RESULT_OK && data!=null && data.getData()!=null) {
                     String fileName = data.getData().getPath();
                     loadPlayersEnd(fileName);
                 }
                 break;
             }
         }
     }
    
     private void loadPlayersEnd(String fileName){
         File file = new File(fileName);
         try {
 			datasource.loadPlayersFromFile(file);
 		} catch (Exception e) {
 			Toast t = Toast.makeText(this, "Error:" + e.getClass().getName() + " - " + e.getMessage() , Toast.LENGTH_SHORT);
 			t.show();
 			e.printStackTrace();
 		}
     }
     
     private void loadPlayersStart() {
         Intent theIntent = new Intent("org.openintents.action.PICK_FILE");
         startActivityForResult(Intent.createChooser(theIntent, "Select file"), PICK_FILE_RESULT_CODE);
 	}
 
 	private void getSplitResult() {
         Intent intent = new Intent(this, SplitResultActivity.class);
         intent.putExtra(SplitResultActivity.PARAM_NAME_TEAM_COUNT, this.getTeamCount());
         intent.putExtra(SplitResultActivity.PARAM_NAME_SELECTED_IDS, this.getSelectedIds());
         startActivity(intent);
     }
 
     private void editPlayer(int position) {
         Player player = getPlayerByPosition(position);
         Intent intent = new Intent(this, PlayerEditorActivity.class);
         intent.putExtra(PlayerEditorActivity.PARAM_NAME_PLAYER_ID, player.getId());
         startActivity(intent);
     }
 
     private void deletePlayer(int position) {
         Player player = getPlayerByPosition(position);
         datasource.delete(player);
     }
 
     private void addPlayer() {
         Intent intent = new Intent(this, PlayerEditorActivity.class);
         startActivity(intent);
     }
 
     private Player getPlayerByPosition(int position) {
         return (Player) view.getList().getItemAtPosition(position);
     }
 
     public long[] getSelectedIds() {
         long [] res = view.getList().getCheckedItemIds();
         return res;
     }
 
     public long[] getAllIds() {
         long res[] = new long[players.size()];
         for (int i = 0; i < players.size(); i++) {
             res[i] = players.get(i).getId();
         }
         return res;
     }
 
     public int getTeamCount() {
         TextView textView = (TextView) view.getNumOfTeams().getSelectedView();
         return Integer.valueOf(textView.getText().toString());
     }
 
     private Player[] getAllPlayers() {
         return datasource.getAllPlayers();
     }
 
     private void fillData() {
         long selectedIDs[] = getSelectedIds();
         Arrays.sort(selectedIDs);
         long allIDs[] = getAllIds();
         Arrays.sort(allIDs);
         Player players[] = getAllPlayers();
         this.players.clear();
         Map<Player, Boolean> checkInfo = new HashMap<Player, Boolean>();
         for (Player player : players) {
             boolean selected = (Arrays.binarySearch(selectedIDs, player.getId()) >= 0) ||
                     (Arrays.binarySearch(allIDs, player.getId()) < 0);
             this.players.add(player);
             checkInfo.put(player, selected);
         }
         view.setPlayers(this.players);
         
        int position = 0;
        for (Player player : players) {
            view.getList().setItemChecked(position, checkInfo.get(player));
            position += 1;
         }
   
     }
 }
