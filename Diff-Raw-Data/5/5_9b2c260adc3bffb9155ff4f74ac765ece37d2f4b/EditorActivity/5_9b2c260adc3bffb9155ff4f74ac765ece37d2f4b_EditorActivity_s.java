 package com.example.digplay;
 
 import java.io.IOException;
 
 import com.businessclasses.Field;
 import com.businessclasses.Formation;
 import com.businessclasses.Location;
 import com.businessclasses.Path;
 import com.businessclasses.Player;
 import com.businessclasses.Route;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Picture;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.FloatMath;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class EditorActivity extends Activity implements OnClickListener  {
 
 	private static Context context;
 	
 	private static boolean arrowRoute;
 	private static boolean solidPath;
 	
 	private static boolean movePlayer;
 	private static int selectionColor;
 	private static boolean drawingRoute;
 	
 	private static Route playerRoute;
 	private static Path playerPath;
 	
 	private static final int TAN_COLOR = 0xFFFF8000;
 
 	private static Field field; // the one field
 	private static int playerIndex = -1; // index of array for which player has been selected
 	private static int previousPlayerIndex = -1;
 	
 	private static int x; // for locating screen X value
 	private static int y; // for locating screen Y value
 	
 	private static int lastPlayerX;
 	private static int lastPlayerY;
 	
 	private static DrawView drawView; // custom view
 	
 	private static Button save;
 	private static Button clearRoutes;
 	private static Button clearField;
 	
 	private static Button arrowButton;
 	private static Button dashButton;
 	private Button clearPlayerRoute;
 	private Button testButton;
 	
 	private static Button trashCan;
 	
 	private static boolean tooManyPlayers = true;
 	private static boolean lineOfScrimmage = true;
 	private static boolean playersOnTop = true;
 	
 	private static float DENSITY; // DENSITY coefficient
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		DENSITY = getResources().getDisplayMetrics().density;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.editor);
 
 		arrowRoute = true;
 		solidPath = true;
 		
 		movePlayer = false;
 		selectionColor = TAN_COLOR;
 		
 		playerRoute = Route.ARROW;
 		playerPath = Path.SOLID;
 
 		drawView = (DrawView) findViewById(R.id.DrawView);
 		
 		save = (Button) findViewById(R.id.save);
 		trashCan = (Button) findViewById(R.id.editor_trash_can);
 		arrowButton  = (Button)findViewById(R.id.edi_arrow_button);
 		dashButton = (Button)findViewById(R.id.edi_dash_button);
 		clearPlayerRoute = (Button)findViewById(R.id.edi_clear_player_route);
 		testButton = (Button)findViewById(R.id.edi_test_button);
 
 		trashCan.setOnClickListener(this);
 		save.setOnClickListener(this);
 		arrowButton.setOnClickListener(this);
 		dashButton.setOnClickListener(this);
 		clearPlayerRoute.setOnClickListener(this);
 		testButton.setOnClickListener(this);
 		
 		playerIndex = 1;
 		
 		trashCan.setBackgroundResource(R.drawable.trashcan);
 		save.setBackgroundResource(R.drawable.floppy_disk);
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras == null)field = new Field();
 		else{
 			Formation formation = (Formation)extras.getSerializable("Formation");
 			if(formation == null)field = new Field();
 			else field = formation.getFormation();
 		}
 	}
 	
 	public static Bitmap getBitmap()
 	{
 		return drawView.getBitmap();
 	}
 	
 	public static class DrawView extends View implements OnTouchListener {
 		
 		private Canvas c;
 		private Paint paint;
 		
 		private boolean drawCreatedPlayers;
 		private boolean drawField;
 		private boolean moreThanElevenPlayers;
 		private boolean onOtherSideOfScrimmage;
 		private boolean putOnTopOfOtherPlayer;
 		private boolean addingPlayer;
 		
 		// this field is used to just store the bottom 8 "players"
 		// that users can drag onto the field
 		private Field fieldForCreatePlayer;
 		
 		// static final values
 		static final private float FIELD_HEIGHT = 575*DENSITY;
 		static final private float LEFT_MARGIN = 40*DENSITY;
 		static final private float RIGHT_MARGIN = 1240*DENSITY;
 		static final private float TOP_MARGIN = 60*DENSITY;
 		static final private float BOTTOM_MARGIN = TOP_MARGIN+FIELD_HEIGHT;
 		static final private float PIXELS_PER_YARD = 13*DENSITY;
 		static final private float FIELD_LINE_WIDTHS = 4*DENSITY;
 		static final private float PLAYER_ICON_RADIUS = 25*DENSITY;
 		static final private float TOP_ANDROID_BAR = 50*DENSITY;
 		static final private float TOUCH_SENSITIVITY = 35*DENSITY;
 		
 		Picture createdPlayersPicture; 
 		Picture fieldPicture;
 		
 		Canvas createdPlayersCanvas;
 		Canvas fieldCanvas;
 		
 		static private Bitmap bitmap;
 		static private Canvas bitmapCanvas;
 
 		public DrawView(Context context, AttributeSet attrs) throws IOException {
 			super(context, attrs);
 			build(context, attrs);
 			EditorActivity.setContext(context);
 		}
 
 		public Bitmap getBitmap() {
 			
 			return bitmap;
 		}
 
 		public void build(Context context, AttributeSet attrs) throws IOException
 		{		
 			drawField = true;
 			drawCreatedPlayers = true;
 			moreThanElevenPlayers = false;
 			onOtherSideOfScrimmage = false;
 			putOnTopOfOtherPlayer = false;
 			addingPlayer = false;
 			
 			createdPlayersPicture = new Picture();
 			fieldPicture = new Picture();
 			
 			createdPlayersCanvas = createdPlayersPicture.beginRecording((int)(LEFT_MARGIN + RIGHT_MARGIN), (int)(800));
 			fieldCanvas = fieldPicture.beginRecording((int)(LEFT_MARGIN + RIGHT_MARGIN), (int)(800));
 			
 			bitmap = Bitmap.createBitmap((int)(RIGHT_MARGIN-LEFT_MARGIN), (int)(FIELD_HEIGHT), Bitmap.Config.ARGB_8888);
 			bitmapCanvas = new Canvas(bitmap);
 			
 			fieldForCreatePlayer = new Field();
 			
 			// the main field
 			//field = new Field();
 			
 			paint = new Paint();
 			
 			c = new Canvas();
 
 			this.setOnTouchListener(this);
 		}
 
 		@Override 
 		protected void onDraw(Canvas canvas) {
 			
 			paint.setColor(Color.BLACK);
 			
 			c = canvas;
 			
 			if (drawField)
 			{
 				// 2 = out of bounds spacing, the number of pixels between out of bounds and the hash mark
 				// 18 = length of the hash marks in pixels 
 				DrawingUtils.drawField(LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN, DENSITY, FIELD_LINE_WIDTHS, PIXELS_PER_YARD, 
 						2*DENSITY, 18*DENSITY, fieldCanvas, paint);
 				fieldPicture.endRecording();
 				drawField = false;
 			}
 			c.drawPicture(fieldPicture);
 			
 			DrawingUtils.drawRoutes(field, 0, TOP_ANDROID_BAR, FIELD_LINE_WIDTHS, c, paint, PIXELS_PER_YARD);
 	
 			if (drawCreatedPlayers)
 			{
 				DrawingUtils.drawCreatePlayers(fieldForCreatePlayer, createdPlayersCanvas, paint, DENSITY, TOP_ANDROID_BAR, PLAYER_ICON_RADIUS);
 				createdPlayersPicture.endRecording();
 				drawCreatedPlayers = false;
 			}
 			c.drawPicture(createdPlayersPicture);
 			
 			DrawingUtils.drawPlayers(field, 0, TOP_ANDROID_BAR, canvas, paint, playerIndex, selectionColor, PLAYER_ICON_RADIUS, DENSITY);
 		}
 		
 		private void drawToBitmap()
 		{
 			paint.setColor(Color.BLACK);
 			
 			// 2 = out of bounds spacing, the number of pixels between out of bounds and the hash mark
 			// 18 = length of the hash marks in pixels 
 			DrawingUtils.drawField(0, RIGHT_MARGIN-LEFT_MARGIN, 0, BOTTOM_MARGIN-TOP_MARGIN, DENSITY, FIELD_LINE_WIDTHS, PIXELS_PER_YARD, 
 					2*DENSITY, 18*DENSITY, bitmapCanvas, paint);
 			
			DrawingUtils.drawRoutes(field, 0, TOP_MARGIN + TOP_ANDROID_BAR, FIELD_LINE_WIDTHS, bitmapCanvas, paint, PIXELS_PER_YARD);
 			
			DrawingUtils.drawPlayers(field, 0, TOP_MARGIN + TOP_ANDROID_BAR, bitmapCanvas, paint, playerIndex, selectionColor, PLAYER_ICON_RADIUS, DENSITY);
 			
 			bitmapCanvas.drawBitmap(bitmap, 0, 0, paint);
 		}
 		
 		public boolean onTouch(View v, MotionEvent event) {
 
 			x = (int) (event.getRawX()*DENSITY);
 			y = (int) (event.getRawY()*DENSITY);
 
 			switch (event.getAction() & MotionEvent.ACTION_MASK) {
 			case MotionEvent.ACTION_DOWN:
 				boolean[] returnedFlags = new boolean[2];
 				returnedFlags = DrawingUtils.actionDown(field, fieldForCreatePlayer, TOUCH_SENSITIVITY, x, y, playerIndex, playerRoute, playerPath);
 				moreThanElevenPlayers = returnedFlags[0];
 				addingPlayer = returnedFlags[1];
 				
 				if (playerIndex != -1)
 				{
 					Player selectedPlayer = field.getPlayer(playerIndex);
 					boolean arrowRouteForPlayer;
 					if (selectedPlayer.getRoute() == Route.ARROW)
 					{
 						arrowRouteForPlayer = true;
 					}
 					// block route
 					else
 					{
 						arrowRouteForPlayer = false;
 					}
 					boolean solidPathForPlayer;
 					if (selectedPlayer.getPath() == Path.SOLID)
 					{
 						solidPathForPlayer = true;
 					}
 					// dashed path
 					else
 					{
 						solidPathForPlayer = false;
 					}
 					if (selectedPlayer.getRouteLocations().size() != 0)
 					{
 						if (!arrowRouteForPlayer && arrowRoute || arrowRouteForPlayer && !arrowRoute)
 						{
 							arrowRoute = EditorActivity.toggleArrowButton(arrowRoute);
 						}
 						if (!solidPathForPlayer && solidPath || solidPathForPlayer && !solidPath)
 						{
 							solidPath = EditorActivity.toggleSolidButton(solidPath);
 						}
 					}
 					else
 					{
 						if (arrowRoute)
 						{
 							selectedPlayer.changeRoute(Route.ARROW);
 						}
 						else
 						{
 							selectedPlayer.changeRoute(Route.BLOCK);
 						}
 						if (solidPath)
 						{
 							selectedPlayer.changePath(Path.SOLID);
 						}
 						else
 						{
 							selectedPlayer.changePath(Path.DOTTED);
 						}
 					}
 					lastPlayerX = selectedPlayer.getLocation().getX();
 					lastPlayerY = selectedPlayer.getLocation().getY();
 					if (selectionColor == TAN_COLOR || previousPlayerIndex != playerIndex)
 					{
 						selectionColor = Color.RED;
 					}
 				}
 				else
 				{
 					selectionColor = TAN_COLOR;
 				}
 				invalidate(); // redraw
 				break;
 			case MotionEvent.ACTION_UP:
 				boolean[] returnedErrors = new boolean[3];
 				returnedErrors = DrawingUtils.actionUp(field, playerIndex, LEFT_MARGIN, PLAYER_ICON_RADIUS, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN, 
 						TOP_ANDROID_BAR, PIXELS_PER_YARD, FIELD_LINE_WIDTHS, DENSITY, x, y, moreThanElevenPlayers, movePlayer, addingPlayer);
 				moreThanElevenPlayers = returnedErrors[0];
 				onOtherSideOfScrimmage = returnedErrors[1];
 				putOnTopOfOtherPlayer = returnedErrors[2];
 				if (moreThanElevenPlayers)
 				{
 					tooManyPlayersError();
 				}
 				if (onOtherSideOfScrimmage)
 				{
 					playerOnOtherSideError();
 				}
 				if (putOnTopOfOtherPlayer)
 				{
 					playerOnTopError();
 				}
 				if (movePlayer)
 				{
 					selectionColor = Color.RED;	
 				}
 				else if (!movePlayer && previousPlayerIndex == playerIndex)
 				{
 					if (selectionColor == Color.RED)
 					{
 						selectionColor = Color.BLUE;
 					}
 					else if (selectionColor == Color.BLUE && !drawingRoute)
 					{
 						selectionColor = Color.RED;
 					}
 				}
 				previousPlayerIndex = playerIndex;
 				drawingRoute = false;
 				movePlayer = false;
 				addingPlayer = false;
 				invalidate(); // redraw
 				break;
 			case MotionEvent.ACTION_POINTER_DOWN:
 				break;
 			case MotionEvent.ACTION_POINTER_UP:
 				break;
 			case MotionEvent.ACTION_MOVE:
 				// update the player location when dragging
 				if (!movePlayer)
 				{
 					float dist = -1;
 					if (selectionColor == Color.RED)
 					{
 						dist = FloatMath.sqrt((x-lastPlayerX)*(x-lastPlayerX) + (y-lastPlayerY)*(y-lastPlayerY));
 						if (dist > TOUCH_SENSITIVITY)
 						{
 							field.getPlayer(playerIndex).clearRouteLocations();
 							movePlayer = true;
 							DrawingUtils.actionMove(field, playerIndex, x, y);
 						}
 					}
 					else if (selectionColor == Color.BLUE)
 					{
 						if (x < LEFT_MARGIN + FIELD_LINE_WIDTHS/2)
 						{
 							x = (int) (LEFT_MARGIN + FIELD_LINE_WIDTHS/2);
 						}
 						else if (x > RIGHT_MARGIN - FIELD_LINE_WIDTHS/2)
 						{
 							x = (int) (RIGHT_MARGIN - FIELD_LINE_WIDTHS/2);
 						}
 						if (y < TOP_MARGIN + TOP_ANDROID_BAR + FIELD_LINE_WIDTHS/2)
 						{
 							y = (int) (TOP_MARGIN + TOP_ANDROID_BAR + FIELD_LINE_WIDTHS/2);
 						}
 						else if (y > BOTTOM_MARGIN + TOP_ANDROID_BAR - FIELD_LINE_WIDTHS/2)
 						{
 							y = (int) (BOTTOM_MARGIN + TOP_ANDROID_BAR - FIELD_LINE_WIDTHS/2);
 						}
 						dist = FloatMath.sqrt((x-lastPlayerX)*(x-lastPlayerX) + (y-lastPlayerY)*(y-lastPlayerY));
 						if (dist > TOUCH_SENSITIVITY)
 						{
 							if (x < LEFT_MARGIN + FIELD_LINE_WIDTHS/2)
 							{
 								lastPlayerX = (int) (LEFT_MARGIN + FIELD_LINE_WIDTHS/2);
 							}
 							else if (x > RIGHT_MARGIN - FIELD_LINE_WIDTHS/2)
 							{
 								lastPlayerX = (int) (RIGHT_MARGIN - FIELD_LINE_WIDTHS/2);
 							}
 							else
 							{
 								lastPlayerX = x;
 							}
 							if (y < TOP_MARGIN + TOP_ANDROID_BAR + FIELD_LINE_WIDTHS/2)
 							{
 								lastPlayerY = (int) (TOP_MARGIN + TOP_ANDROID_BAR + FIELD_LINE_WIDTHS/2);
 							}
 							
 							else if (y > BOTTOM_MARGIN + TOP_ANDROID_BAR - FIELD_LINE_WIDTHS/2)
 							{
 								lastPlayerY = (int) (BOTTOM_MARGIN + TOP_ANDROID_BAR - FIELD_LINE_WIDTHS/2);
 							}
 							else
 							{
 								lastPlayerY = y;
 							}
 							if (!drawingRoute)
 							{
 								field.getPlayer(playerIndex).clearRouteLocations();
 							}
 							Location temp = new Location(lastPlayerX, lastPlayerY);
 							field.getPlayer(playerIndex).addRouteLocation(temp);
 							drawingRoute = true;
 						}
 					}
 				}
 				else
 				{
 					if (selectionColor == Color.RED)
 					{
 						DrawingUtils.actionMove(field, playerIndex, x, y);
 					}
 				}
 				invalidate(); // redraw
 				break;
 			default:
 				break;
 			}
 			return true;
 		}
 
 		public static void flipField() {
 			field.flip((int)(LEFT_MARGIN + RIGHT_MARGIN));
 		}
 	}	
 	//used for database to get the field object.
 	public static Field getField(){
 		return field;
 	}
 	
 	public static boolean toggleArrowButton(boolean arrowRoute)
 	{
 		if(arrowRoute){
 			arrowButton.setBackgroundResource(R.drawable.perpendicular_line);
 			playerRoute = Route.BLOCK;
 			return false;
 		}
 		else {
 			arrowButton.setBackgroundResource(R.drawable.right_arrow);
 			playerRoute = Route.ARROW;
 			return true;
 		}
 	}
 	
 	public static boolean toggleSolidButton(boolean solidPath)
 	{
 		if(solidPath){
 			dashButton.setBackgroundResource(R.drawable.dotted_line);
 			playerPath = Path.DOTTED;
 			return false;
 		}
 		else {
 			dashButton.setBackgroundResource(R.drawable.line);
 			playerPath = Path.SOLID;
 			return true;
 		}
 	}
 	public void onClick(View v) {
 		Intent intent = null;
 		int id = v.getId();
 		if(id == save.getId()){
 			drawView.drawToBitmap();
 			intent = new Intent(v.getContext(),SaveActivity.class);
 			startActivity(intent);
 		}else if(id == arrowButton.getId()){
 			arrowRoute = toggleArrowButton(arrowRoute);
 			if (playerIndex != -1)
 			{
 				field.getPlayer(playerIndex).changeRoute(playerRoute);
 				drawView.invalidate();
 			}
 		}else if(id == dashButton.getId()){
 			solidPath = toggleSolidButton(solidPath);
 			if (playerIndex != -1)
 			{
 				field.getPlayer(playerIndex).changePath(playerPath);
 				drawView.invalidate();
 			}
 		}else if(id == clearPlayerRoute.getId()){
 			if (playerIndex != -1)
 			{
 				Player selectedPlayer = field.getPlayer(playerIndex);
 				selectedPlayer.clearRoute();
 				drawView.invalidate();
 			}
 		}else if(id== testButton.getId()){
 			DrawView.flipField();
 			drawView.invalidate();
 		}
 		else if (id == clearRoutes.getId())
 		{
 			field.clearRoutes(playerRoute);
 			field.clearPaths(playerPath);
 			field.clearRouteLocations();
 			playerIndex = -1;
 			previousPlayerIndex = -1;
 			drawView.invalidate(); // redraw the screen
 		}
 		else if (id == clearField.getId())
 		{
 			field.clearField();
 			playerIndex = -1;
 			previousPlayerIndex = -1;
 			drawView.invalidate(); // redraw the screen
 		}
 	}
 	public static void setContext(Context c)
 	{
 		context = c;
 	}
 	public static void tooManyPlayersError()
 	{
 		if(!tooManyPlayers)return;
 		Builder alert = new AlertDialog.Builder(context);
 		alert.setTitle("Caution");
 		alert.setMessage("You have placed more than 11 players on the field. Place in trash can if you want to remove.");
 		alert.setPositiveButton("Yes",null);
 		alert.show();
 	}
 	public static void playerOnOtherSideError(){
 		if(!lineOfScrimmage)return;
 		Builder alert = new AlertDialog.Builder(context);
 		alert.setTitle("Caution");
 		alert.setMessage("You have placed a player on the other side of the line of scrimmage. Click ignore to turn off warning.");
 		alert.setPositiveButton("Whoops, my mistake", new DialogInterface.OnClickListener() {	
 			public void onClick(DialogInterface dialog, int which) {
 				field.getAllPlayers().remove(playerIndex);
 				playerIndex = -1;
 			}
 		});
 		alert.setNegativeButton("Ignore", null);
 		alert.show();
 	}
 	public static void playerOnTopError(){
 		if(!playersOnTop)return;
 		Builder alert = new AlertDialog.Builder(context);
 		alert.setTitle("Caution");
 		alert.setMessage("You have placed a player on top of another!");
 		alert.setPositiveButton("Ok",null);
 		alert.show();
 	}
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState){
 		super.onSaveInstanceState(savedInstanceState);
 		savedInstanceState.putSerializable("Field", field);
 	}
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState){
 		super.onRestoreInstanceState(savedInstanceState);
 		field = (Field) savedInstanceState.getSerializable("Field");
 	}
 	@Override
 	public boolean onKeyDown(int keyCode,KeyEvent event){
 		if(keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK){
 			verifyBack();
 		}
 		return false;
 	}
 	
 	private void verifyBack(){
 			Builder alert = new AlertDialog.Builder(this);
 			alert.setTitle("Caution");
 			alert.setMessage("Are you sure you want to leave this screen? Data will be lost");
 			alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {	
 				public void onClick(DialogInterface dialog, int which) {
 					finish();
 				}
 			});
 			alert.setNegativeButton("Cancel", null);
 			alert.show();
 	}
 	public static void setPlayerIndex(int index)
 	{
 		playerIndex = index;
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		super.onCreateOptionsMenu(menu);
 		menu.add("Clear all routes");
 		menu.add("Clear entire field");
 		menu.add("Disable too many players warning");
 		menu.add("Disable players on top of each other warning");
 		menu.add("Disable line of scrimmage warning");
 		return true;
 	}
 	public boolean onOptionsItemSelected(MenuItem item){
 		if(item.getTitle().equals("Clear entire field")){
 			field.clearField();
 			playerIndex = -1;
 			previousPlayerIndex = -1;
 			drawView.invalidate(); // redraw the screen
 		}else if(item.getTitle().equals("Clear all routes")){
 			field.clearRoutes(playerRoute);
 			field.clearPaths(playerPath);
 			field.clearRouteLocations();
 			playerIndex = -1;
 			previousPlayerIndex = -1;
 			drawView.invalidate(); // redraw the screen
 		}else if(item.getTitle().equals("Disable too many players warning")){
 			tooManyPlayers = false;
 			item.setTitle("Enable too many players warning");
 		}else if(item.getTitle().equals("Enable too many players warning")){
 			tooManyPlayers = true;
 			item.setTitle("Disable too many players warning");
 		}
 		else if(item.getTitle().equals("Disable players on top of each other warning")){
 			playersOnTop = false;
 			item.setTitle("Enable players on top of each other warning");
 		}else if(item.getTitle().equals("Enable players on top of each other warning")){
 			playersOnTop = true;
 			item.setTitle("Disable players on top of each other warning");
 		}
 		else if(item.getTitle().equals("Disable line of scrimmage warning")){
 			lineOfScrimmage = false;
 			item.setTitle("Enable line of scrimmage warning");
 		}else if(item.getTitle().equals("Enable line of scrimmage warning")){
 			lineOfScrimmage = true;
 			item.setTitle("Disable line of scrimmage warning");
 		}
 		
 		return true;
 	}
 }
