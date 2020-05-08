 package se.chalmers.chessfeud;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.chessfeud.constants.DbHandler;
 import se.chalmers.chessfeud.constants.Game;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnClickListener, PropertyChangeListener{
 	private Button bPlay, bMyProfile, bSettings, bAbout;
 	private ImageView iLogo;
 	private ListView finishedGames, startedGames;
  
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.menu_main);
         
         bMyProfile = (Button) findViewById(R.id.button_myprofile);
         bSettings = (Button) findViewById(R.id.button_settings);
         
         iLogo = (ImageView) findViewById(R.id.imageView1);
         iLogo.setOnClickListener(this);
 
         
         finishedGames = (ListView) findViewById(R.id.list_finishedGames);
         startedGames = (ListView) findViewById(R.id.list_ongoingGames);
         
         bMyProfile.setOnClickListener(this);
         bSettings.setOnClickListener(this);
         
         finishedGames = (ListView) findViewById(R.id.list_finishedGames);
         startedGames = (ListView) findViewById(R.id.list_ongoingGames);
     	try{
	        DbHandler db = DbHandler.getInstance();
 	        startedGames.setAdapter(new GameListAdapter(this, R.id.list_ongoingGames, db.getGames("hej")));
         }catch(Exception e){
         	
         }
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	
     	
     	//finishedGames.setListAdapter(this, R.id.list_finishedGames, getList());
 
 	}
 		
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void onClick(View v) {
     	int id = v.getId();
     	
     	switch(id){
     	case R.id.button_myprofile:
     		startActivity(new Intent(this, ProfileActivity.class));
     		break;
     	case R.id.button_settings:
     		startActivity(new Intent(this, SettingsActivity.class));
     		break;
     	case R.id.imageView1:
     		startActivity(new Intent(this, PlayActivity.class));
     		break;
     	default:
     		Log.d("Default", "Should not get here!");
     	}
     }
     
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		//evt.get
 	}
     
     private class GameListAdapter extends ArrayAdapter<String> {
     	private Context context;
     	private int resourceId;
     	private List<Game> gamesList;
     	private List<String> stringList;
     	
     	public GameListAdapter(Context context, int resId, List<String> l){
     		super(context, resId, l);
     		this.context = context;
     		this.resourceId = resId;
     		gamesList = new ArrayList<Game>();
     		stringList = new ArrayList<String>();
     		for(int i = 0; i < l.size(); i++){
     			gamesList.add(new Game(l.get(i), i));
     			
     			stringList.add(l.get(i));
     		}
     			
     		//To be fixed here when the view is finsihed
     	}
     	
     	@Override
     	public View getView(final int position, View convertView, ViewGroup parent) {
     		final Game game = gamesList.get(position);
     		final String gameString = stringList.get(position);
     		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     		View vRow = inflater.inflate(R.layout.menu_listitem, parent, false);
     		
     		/* Bind all the views */
     		TextView tTurn = (TextView)vRow.findViewById(R.id.player_turn);
     		TextView blackName = (TextView) vRow.findViewById(R.id.player_black);
     		TextView whiteName = (TextView) vRow.findViewById(R.id.player_white);
     		TextView tNbrOfTurns = (TextView) vRow.findViewById(R.id.nbr_turns);
     		
     		/*Fix this later
     		 *TextView blackPawnAmount = (TextView) vRow.findViewById(R.id.nbr_pawn_black);
     		 *TextView whitePawnAmount = (TextView) vRow.findViewById(R.id.nbr_pawn_white);
     		 */	
     		
     		/* Set all the data */
     		tTurn.setText(game.getCurrentPlayer() + "'s turn");
     		blackName.setText(game.getBlackPlayer());
     		whiteName.setText(game.getWhitePlayer());
     		tNbrOfTurns.setText(""+game.getTurns());
     		
     		vRow.setOnClickListener(new OnClickListener(){
     			@Override
     			public void onClick(View v) {
     				Intent i = new Intent(context, PlayActivity.class);
     				i.putExtra("GameString", gameString);
     				i.putExtra("Position", position);
     				startActivity(i);
     			}
     		});
     		
     		//Lets go pieces
     		return vRow;
     	}
     	
     }
 
 }
