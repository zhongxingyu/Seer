 /**
  * @author MetaGalactic Merchants
  * @version 1.0
  * 
  */
 
 package edu.gatech.cs2340.group29.spacemerchant.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import edu.gatech.cs2340.group29.spacemerchant.R;
 import edu.gatech.cs2340.group29.spacemerchant.model.Game;
 import edu.gatech.cs2340.group29.spacemerchant.model.Player;
 import edu.gatech.cs2340.group29.spacemerchant.model.Ship;
 import edu.gatech.cs2340.group29.spacemerchant.util.GameDataSource;
 
 /**
  * The Class GameActivity.
  */
 public class GameActivity extends Activity
 {
     public static final String GAME_ID_EXTRA = "GAME_ID_EXTRA";
     
     private boolean            showHelpOverlay;
     
     Game                       g;
     Player                     p;
     Ship                       s;
     
     /**
      * Override:
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     public void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
         setContentView( R.layout.activity_game );
         
         Intent i = getIntent();
         long gameID = i.getLongExtra( GAME_ID_EXTRA, -1 );
         
         GameDataSource gds = new GameDataSource( getApplicationContext() );
         gds.open();
         g = gds.getGameByID( gameID );
         p = g.getPlayer();
         s = p.getShip();
         gds.close();
         
         // Setting images for player and ship
         ( ( ImageView ) findViewById( R.id.head ) ).setImageResource( p.getHead() );
         ( ( ImageView ) findViewById( R.id.body_player ) ).setImageResource( p.getBody() );
         ( ( ImageView ) findViewById( R.id.legs ) ).setImageResource( p.getLegs() );
         ( ( ImageView ) findViewById( R.id.feet ) ).setImageResource( p.getFeet() );
         ( ( ImageView ) findViewById( R.id.cabin ) ).setImageResource( s.getCabin() );
         ( ( ImageView ) findViewById( R.id.fuselage ) ).setImageResource( s.getFuselage() );
         ( ( ImageView ) findViewById( R.id.boosters ) ).setImageResource( s.getBoosters() );
         
         if ( showHelpOverlay )
         {
             // show overlay
         }
     }
     
     /**
      * Override:
      * 
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public boolean onCreateOptionsMenu( Menu menu )
     {
         getMenuInflater().inflate( R.menu.activity_game, menu );
         return true;
     }
     
    /** 
     *
     * Override:
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
     @Override
     public boolean onOptionsItemSelected( MenuItem item )
     {
         switch ( item.getItemId() )
         {
             case R.id.menu_travel :
                 // to do gotoTravelActivity
                 return true;
             case  R.id.menu_help:
                 // load the help overlay
                 return true;
             default:
                 return super.onOptionsItemSelected( item );
         }
     }
 
     /**
      * Goes to the player info.
      * 
      * @param v
      *            the View
      */
     public void gotoPlayerInfo( View v )
     {
         // do stuff later!
     }
 }
