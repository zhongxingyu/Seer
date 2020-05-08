 package com.example.addbuttonprogramatically;
 
 import java.util.Random;
 
 import android.os.Bundle;
 import android.animation.ObjectAnimator;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.text.Layout;
 
 public class gameActivity extends Activity implements OnClickListener {
 
 
 	private Game game;
     private int row=6,column=6,difficulty=5,firstTurn=1,userId;
     SQLiteDatabase db;
     private String tag = "gameActivity";
     private boolean gameFinished = false;
     ConnectFourApplication connectFourApplication;
     private Display display;
     private int screenHeight;
     private int screenWidth;
     private int useableWidth;
     private int heightOfEachRow;
     private Context context;
     
     
 	@Override
     public void onCreate(Bundle savedInstanceState) 
 	{
     	
         super.onCreate(savedInstanceState);
         context = getApplicationContext();
         connectFourApplication= (ConnectFourApplication)getApplication();
         
         
         display = getWindowManager().getDefaultDisplay();
         connectFourApplication.screenHeight = display.getHeight();
         connectFourApplication.screenWidth = display.getWidth();
         screenHeight = connectFourApplication.screenHeight;
         screenWidth =  connectFourApplication.screenWidth;
         
         
         row = connectFourApplication.row;
         column = connectFourApplication.column;
         difficulty = connectFourApplication.difficulty;
         game = new Game(row, column, difficulty, firstTurn,connectFourApplication);
         
         
         /*	new relative layout	*/
         
         
         ScrollView scrollView = new ScrollView(this);
         
         RelativeLayout relativeLayout = new RelativeLayout(this);
         relativeLayout.setId(200);
         RelativeLayout.LayoutParams relativeLayoutParams =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT);
         relativeLayout.setLayoutParams(relativeLayoutParams);
         relativeLayout.setPadding(5, 5, 5, 5);
         //relativeLayout.setBackgroundColor(Color.GRAY);
         
         /*
         LinearLayout linearLayout = new LinearLayout(this);
         linearLayout.setId(200);
         linearLayout.setBackgroundColor(Color.BLACK);
         linearLayout.setPadding(5, 5, 5, 5);
         */
         
         useableWidth = screenWidth - 10;
         //linearLayout.setOrientation(LinearLayout.VERTICAL);
         int buttonNumber = 0;
         userId = connectFourApplication.userId;
         
         
         for(int i=0 ; i < row ; ++i)
         {
         	
         	LinearLayout innerLinearLayout = new LinearLayout(this);
         	innerLinearLayout.setId(i+1+200);
         	innerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
         	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
         	params.addRule(RelativeLayout.BELOW, 200+(i));
         	
         	for(int j = 0 ; j < column ; ++j)
         	{        		
         		Button button = new Button(this);
         		button.setOnClickListener(this);
         		button.setHeight(50);
         		button.setWidth(50);
         		button.setBackgroundResource(R.drawable.blankbackground);
         		button.setText("" + buttonNumber);
         		button.setId(buttonNumber);
         		innerLinearLayout.addView(button);
         		++buttonNumber;
         	}
         	
         	relativeLayout.addView(innerLinearLayout, params);
         	
         }
         
         
         scrollView.addView(relativeLayout);
         setContentView(scrollView);
         
         setButtonsInGameBoard();
         
         if(connectFourApplication.turn==0)
         {
         	game.symbol = 9;
 	        game.turn--;
 	        int col = game.AI_Turn();
 	        int rw = row-game.flag[col]-1;
 	        int id = column*rw + col;
 	        View v = findViewById(id);
 	        Log.d(tag, "column = "+col+" row = "+rw+" id = "+id);
 			AITurn(col,rw,v);
         }
         
     }
 	
     @Override
 	public void onWindowFocusChanged(boolean hasFocus) {
     	
 		super.onWindowFocusChanged(hasFocus);
 		
 		LinearLayout linearLayout = (LinearLayout) findViewById(201);
 		heightOfEachRow = linearLayout.getMeasuredHeight();
 		
 	}
 
     private void setButtonsInGameBoard()
     {
     	Button v;
     	int id,rw,col;
     	for(int i=row-1 ,ii=0; i >=0 ; --i,ii++)
         {
         	
         	for(int j = column-1, jj=0 ; j >= 0 ; --j,jj++)
         	{
         		id = i*column+jj;
         		v = (Button)findViewById(id);
         		if(game.mat[ii][jj]==4)
         		{
         			v.setBackgroundResource(R.drawable.brown);
         		}
         		else if(game.mat[ii][jj]==9)
         		{
         			v.setBackgroundResource(R.drawable.blueballs);
         		}
         		v.setText("" + id);
         	}
         }
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
 	public void onBackPressed() 
     {    	
     	if(gameFinished)
     	{
     		finish();
     		return;
     	}
 		AlertDialog.Builder builderForAlertBox = new AlertDialog.Builder(this);
 		builderForAlertBox.setCancelable(false).setMessage("Do You Wish To Save This Game ?").setPositiveButton("Yes", dialogClickListner).setNegativeButton("No", dialogClickListner).
 		setCancelable(true).show(); 
 	}
 
 	public void onClick(View v) 
 	{		
 		Log.d(tag, "CLICKED ON BUTTON ID "+String.valueOf(v.getId()));
 		
 		int id = v.getId();
		int rw = id/(column);
		int col = id%(column);
 		if((col<0 || col>=column) || (game.flag[col]>=row))
         {
             Toast.makeText(getApplicationContext(), "Invalid Move", Toast.LENGTH_SHORT).show();
         }
 		else
 		{
 			
 			if(!gameFinished)
 			{
 				game.turn--;
 				game.symbol = 4;
 				rw = row-game.flag[col]-1;
 				id = column*rw + col;
 		        v = findViewById(id);
 		        humanTurn(col,rw,v);
 			}
 	       
 	        if(!gameFinished)
 	        {
 		        game.symbol = 9;
 		        game.turn--;
 		        col = game.AI_Turn();
 		        rw = row-game.flag[col]-1;
 		        id = column*rw + col;
 		        v = findViewById(id);
 				AITurn(col,rw,v);
 	        }
 		}
 		
 		
 	}
 	void humanTurn(int col, int rw, View v)
 	{
 		
 		
 		Button button = (Button) v;
 		
 		float buttonXcordinate = button.getX();
 		float buttonYcordinate = (float) (heightOfEachRow * (row-game.flag[col]-1));
 		Log.d("animation", button.getX() + " "  + " " + ((row-game.flag[col]-1)) );
 		
 		
 		ImageView image = new ImageView(context);
 		image.setBackgroundResource(R.drawable.brown);
 		image.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		image.setX(buttonXcordinate);
 		image.setY(heightOfEachRow);
 		
 		RelativeLayout relativeLayout = (RelativeLayout) findViewById(200);
 		relativeLayout.addView(image);
 		
 		
 		ObjectAnimator animator = ObjectAnimator.ofFloat(image, "y", 0 , buttonYcordinate);
 		animator.setDuration(2000);
 		animator.start();
 		
 		
 		Log.d(tag, "In Human "+String.valueOf(v.getId())+" "+col);
 		game.mat[game.flag[col]++][col]=game.symbol;
         //v.setBackgroundResource(R.drawable.brown);
         AlertDialog.Builder builderForAlertBox = new AlertDialog.Builder(this);
         if(game.Win(game.flag[col]-1,col))
         {
         	gameFinished = true;
         	builderForAlertBox.setCancelable(false).setMessage("YOU WON\n"+"AI Says : I was going Eazy on you ;-)").
         	setPositiveButton("ok", gameFinishListener).show();
         }
         if(game.turn==0)
         {
         	gameFinished = true;
         	builderForAlertBox.setCancelable(false).setMessage("Game Draw\n"+"AI Says : Feeling Lucky Punk!!??").
          	setPositiveButton("ok", gameFinishListener).show();
         }
 	}
 	void AITurn (int col, int rw, View v)
 	{
 		
 		 Button button = (Button) v;
 		 
 		 float buttonXcordinate = button.getX();
 		 float buttonYcordinate = (float) (heightOfEachRow * (row-game.flag[col]-1));
 		 Log.d("animation", button.getX() + " " + " " + ((row-game.flag[col]-1)) );
 		 
 		 ImageView image = new ImageView(context);
 		 image.setBackgroundResource(R.drawable.blueballs);
 		 image.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		 image.setX(buttonXcordinate);
 		 image.setY(heightOfEachRow);
 		 
 		 RelativeLayout relativeLayout = (RelativeLayout) findViewById(200);
 		 relativeLayout.addView(image);
 		 
 		 ObjectAnimator animator = ObjectAnimator.ofFloat(image, "y", 0 , buttonYcordinate);
 		 animator.setDuration(2000);
 		 animator.start();
 		 
 		 Log.d(tag, "In AI "+String.valueOf(v.getId())+" "+col);
 		 //v.setBackgroundResource(R.drawable.blueballs);
 		 game.mat[game.flag[col]++][col]=game.symbol;
 		 AlertDialog.Builder builderForAlertBox = new AlertDialog.Builder(this);
 		 if(game.Win(game.flag[col]-1,col))
          {
 			gameFinished = true;
          	builderForAlertBox.setCancelable(false).setMessage("AI WON\n"+"AI Says : Well I guess it's time for another Evoulution >:P ").
          	setPositiveButton("ok", gameFinishListener).show();
          }
 		 if(game.turn==0)
          {
 			gameFinished = true;
         	builderForAlertBox.setCancelable(false).setMessage("Game Draw\n"+"AI Says : Feeling Lucky Punk!!??").
           	setPositiveButton("ok", gameFinishListener).show();
          }
 	}
 	DialogInterface.OnClickListener gameFinishListener = new DialogInterface.OnClickListener() 
 	{		
 		public void onClick(DialogInterface dialog, int which) 
 		{			
 			//finish();			
 		}
 	};
 	
 	DialogInterface.OnClickListener dialogClickListner = new DialogInterface.OnClickListener() 
 	{		
 		public void onClick(DialogInterface dialog, int which) {
 			
 			switch (which) {
 			case DialogInterface.BUTTON_POSITIVE:
 				saveGame();
 				Toast.makeText(getApplicationContext(), "Your Game Is Saved", Toast.LENGTH_SHORT).show();
 				finish();
 				break;
 				
 			case DialogInterface.BUTTON_NEGATIVE:
 				Toast.makeText(getApplicationContext(), "Your Game Is Not Saved", Toast.LENGTH_SHORT).show();
 				finish();
 			default:
 				break;
 			}
 			
 		}
 
 		private void saveGame() 
 		{
 			// TODO Auto-generated method stub
 			String gameState="";
 			for(int i=0;i<row;i++)
 			{
 				for(int j=0;j<column;j++)
 				{
 					gameState+=game.mat[i][j];
 				}
 			}
 			Log.d(tag, "gamestate = "+gameState);
 			db = homeActivity.dbHelper.getWritableDatabase();
 			try 
 			{
 				ContentValues values = new ContentValues();
 				values.put(DbHelper.GAME_STATE, gameState);
 				Log.d(tag, String.format("before insert %d: %s", connectFourApplication.userId, gameState));
 				
 				//db.insertWithOnConflict(DbHelper.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
 				
 				String whereClauses = DbHelper.USER_ID+" = ?";
 				String[] whereArgs = {connectFourApplication.userId+""};
 				db.updateWithOnConflict(DbHelper.TABLE, values, whereClauses, whereArgs,SQLiteDatabase.CONFLICT_IGNORE);
 				db.close();
 				Log.d(tag, "save game");
 			} 
 			catch (Exception e) 
 			{
 				db.close();
 				Log.d(tag, e.toString());
 			}		
 			
 			
 			return;
 		}
 	};
 
     
 }
