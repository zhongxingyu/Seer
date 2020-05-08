 package com.davidykay.funge;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.davidykay.funge.interpreter.FungePlane;
 import com.davidykay.funge.interpreter.tiles.IntegerTile;
 import com.davidykay.funge.interpreter.tiles.Tile;
import com.eclecticself.funge.R;
 
 /**
  * Shows the funge grid. This is the signature activity in our application.
  * @author dk
  *
  */
 public class FungeGridActivity extends Activity {
 
   private FungePlane mFunge;
   private FungeView mView;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     // TODO Auto-generated method stub
     super.onCreate(savedInstanceState);
     setContentView(R.layout.funge_grid);
 
     mView = (FungeView) findViewById(R.id.game_view);
     mFunge = new FungePlane();
     Tile sampleTile = new IntegerTile(5);
     mFunge.setTileAtLocation(sampleTile, 0, 0);
     mFunge.setTileAtLocation(sampleTile, 1, 1);
     mFunge.setTileAtLocation(sampleTile, 2, 2);
     mView.setModel(mFunge);
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     // TODO Auto-generated method stub
     return super.onCreateOptionsMenu(menu);
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     // TODO Auto-generated method stub
     return super.onOptionsItemSelected(item);
   }
 
 }
