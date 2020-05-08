 package com.commanderZ;
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.PorterDuff;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Display;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.WindowManager;
 
 public class GameDisplay extends SurfaceView  implements SurfaceHolder.Callback {
 		/////////////////////////////////////////////////////////////////////////////////////
 		//PRIVATE VARIABLES
 		/////////////////////////////////////////////////////////////////////////////////////
 		private SurfaceHolder _holder;
         public GameDisplayThread _gameDisplayThread;
         private Rect _tileScreenLocation;
         private Rect _tileLocation;
         private Rect _backgroundSize;
         private Canvas _puck;
         private Bitmap _puckImage;
         private Bitmap _levelBitmap;
         private Canvas _level;
         private Rect _camera;
         private Rect _screen;
         private Rect _backgroundRect;
         private int _mapHeight = 0;
         private int _mapWidth = 0;
         private int tileSheetWidth = 13;
         private int _height = 0;
         private int _width = 0;
       
         
 		/////////////////////////////////////////////////////////////////////////////////////
 		//SETUP STUFF
 		/////////////////////////////////////////////////////////////////////////////////////
 		public GameDisplay(Context context)
 		{
 		    super(context);
 		    init();
 		}
 		public GameDisplay(Context context, AttributeSet attrs) {
 			super( context, attrs );
 			init();
 		}
 		public GameDisplay(Context context, AttributeSet attrs, int defStyle) {
 			super( context, attrs, defStyle );
 			init();
 		}
 		public void init(){
 			_holder = getHolder();
 		    _holder.addCallback(this);
 		    
 		}
 		
 		
 		@SuppressLint({ "UseValueOf", "NewApi" })
 		private void createBitmaps(){
 			/*********************************************************************************************************************************
 			 * This creates all of the bitmaps needed for the scene and sets up all of the screen size stuff and cameras
 			 *********************************************************************************************************************************/
 			 updateLevel();
 		}
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//HANDEL EVENTS
 		/////////////////////////////////////////////////////////////////////////////////////
 		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 			/*********************************************************************************************************************************
 			 * This does nothing at the moment
 			 *********************************************************************************************************************************/
 			_width = width;
 			_height = height;
 			_holder = getHolder();
 		    _holder.addCallback(this);
 		    _backgroundRect = new Rect(0,0,GameDataManager.getInstance().getBackground().getWidth(),GameDataManager.getInstance().getBackground().getHeight());
 			createBitmaps();
 			_gameDisplayThread = new GameDisplayThread (_holder, this);
 			_gameDisplayThread.setRunning(true);
 			_gameDisplayThread.start();
 			
 			
 		}
 
 		public void surfaceCreated(SurfaceHolder holder) {
 			/*********************************************************************************************************************************
 			 * This is called once the surface has been created and can be used
 			 *********************************************************************************************************************************/
 			
 		}
 		
 		
 	
 		
 		public void createThread(){
 			
 			_holder = getHolder();
 		    _holder.addCallback(this);
 			createBitmaps();
 			_gameDisplayThread = new GameDisplayThread (_holder, this);
 			_gameDisplayThread.setRunning(true);
 			_gameDisplayThread.start();
 			
 		}
 		
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			/*********************************************************************************************************************************
 			 * This is called when the surface is cleared from the screen
 			 *********************************************************************************************************************************/
 			clearThread();
 		}
 
 		@Override
 		public void onDraw(Canvas canvas) {
 				/*********************************************************************************************************************************
 				 * This happens every frame and handles the drawing 
 				 *********************************************************************************************************************************/
 				
 				//this completely clears the puck
 				_puck.drawColor( 0,PorterDuff.Mode.CLEAR );
 				
 				//this copies the tiles to the puck. idealy this will only update the areas that change one day lol
 				_puck.drawBitmap(_levelBitmap, 0,0 , null);
 		        
 			
 				if(GameDataManager.getInstance().getCharacter() != null){
 					
 					
 		        
 			        _tileScreenLocation.set( GameDataManager.getInstance().getCharacter().getX() ,GameDataManager.getInstance().getCharacter().getY(), GameDataManager.getInstance().getCharacter().getX() +  GameDataManager.getInstance().getCharWidth(), GameDataManager.getInstance().getCharacter().getY() +  GameDataManager.getInstance().getCharHeight());
 			        _tileLocation.set(0, 0 ,  GameDataManager.getInstance().getCharWidth() , GameDataManager.getInstance().getCharHeight());
 			        
 			        //this draws the player to the screen
 			        _puck.drawBitmap(GameDataManager.getInstance().getCharacter().draw(), _tileLocation, _tileScreenLocation, null); 
 				}
 				
 				
 		        canvas.drawBitmap(GameDataManager.getInstance().getBackground(), _backgroundRect, _screen, null);
 		        canvas.drawBitmap(_puckImage, _camera, _screen, null);
 		}
 		
 		public void updatePhysics(int fps){
 			/*********************************************************************************************************************************
 			 * This happens every frame and updates physics and the camera movement 
 			 *********************************************************************************************************************************/
 			if(GameDataManager.getInstance().getCharacter() != null){
				int paddingBottom = 250;//this fixed the clippping issue at the bottom but needs to be investigated more one day.
 		
 				int newX = GameDataManager.getInstance().getCharacter().getX() - (_camera.width()/2);
 				 
 				int newY = GameDataManager.getInstance().getCharacter().getY() + GameDataManager.getInstance().getCharHeight() -_camera.height() + paddingBottom;
 				 if(newX < 0){
 					 newX = 0;
 				 }else if(newX >  _mapWidth - _camera.width()){
 					 newX =  _mapWidth - _camera.width();
 				 }
 				if(newY < 0){
 					newY = 0;
 				}else if(newY > _mapHeight - _camera.height()){
 					newY = _mapHeight - _camera.height();
 				 }
 				
 				newX -= (int) Math.round((newX - _camera.left) * .7);
 				newY -= (int) Math.round((newY - _camera.top) * .7);
 				_camera.set(  newX, newY ,newX + _camera.width(),newY + _camera.height());
 					
 					
 			
 				GameDataManager.getInstance().getCharacter().updatePhysics(fps);
 			}
 			
 		}
 		 
 		/////////////////////////////////////////////////////////////////////////////////////
 		//OTHER FUNCTIONS
 		///////////////////////////////////////////////////////////////////////////////////// 
 		public void updateTiles(){
 			_level.drawColor( 0,PorterDuff.Mode.CLEAR );
 			_level = drawTiles(_level);
 			
 		}
 		private Canvas drawTiles(Canvas canvas){
 			/*********************************************************************************************************************************
 			 * This loop through the map and adds the tiles to the screen
 			 *********************************************************************************************************************************/
 			 int tileWidth = GameDataManager.getInstance().getTileWidth();
 		     int tileHeight = GameDataManager.getInstance().getTileHeight();
 		    
 		     _mapHeight = GameDataManager.getInstance().getCurrentMap().length * tileHeight;
 		        for( int i =0 ;i < GameDataManager.getInstance().getCurrentMap().length ; i++){
 		        	
 
 	        		
 		        	for( int j =0; j < GameDataManager.getInstance().getCurrentMap()[i].length ; j++){
 		        		
 		        		
 				        //this sets the location of the tile on the game screen
 		        		//note: this is diff from as3 or javascript because the rect represents top left corner and bottom right corner
 		        		// instead of top left corner and width and height
 				        _tileScreenLocation.set( j * tileWidth , i * tileHeight, (j * tileWidth) + tileWidth, (i * tileHeight) + tileHeight);
 				        
 				        //this sets the location of the tile on the tile map
 				        //note: this is diff from as3 or javascript because the rect represents top left corner and bottom right corner
 		        		// instead of top left corner and width and height
 				        int x = (GameDataManager.getInstance().getCurrentMap()[i][j]  %  tileSheetWidth ) * tileWidth;
 				        int y = (GameDataManager.getInstance().getCurrentMap()[i][j]  /  tileSheetWidth ) * tileHeight;
 				        _tileLocation.set(x, y , x + tileWidth ,  y + tileHeight);
 				     
 				        //Log.d("test", "dpi= " + dpi);
 				        //this draws the tile to the screen
 				        canvas.drawBitmap(GameDataManager.getInstance().getTiles(), _tileLocation, _tileScreenLocation, null);
 		        	}
 		        }
 			 return canvas;
 		}
 		
 		private void getMaxTileWidth(){
 			/*********************************************************************************************************************************
 			 * This loops through the level to see what the longest row is then sets the map size accordingly. it should probably be renamed
 			 *********************************************************************************************************************************/
 			int adjustedTileWidth =  GameDataManager.getInstance().getTileWidth();
 			 for( int i =0 ;i < GameDataManager.getInstance().getCurrentMap().length ; i++){
 		        	
 		        	int length = GameDataManager.getInstance().getCurrentMap()[i].length * adjustedTileWidth;
 	        		
 		        	if(_mapWidth < length  ){
 	        			_mapWidth = length;
 	        			
 	        		}
 			 }
 			
 		}
 		
 		public void clearThread(){
 			 boolean retry = true;
 			 _gameDisplayThread.setRunning(false);
 			    while (retry) {
 			        try {
 			        	_gameDisplayThread.join();
 			            retry = false;
 			        } catch (InterruptedException e) {
 			        }
 			    }
 		
 		}
 		
 		public void updateLevel(){
 		
 			_tileScreenLocation = new Rect();
 			_tileLocation = new Rect();
 			_camera = new Rect();
 			_screen = new Rect();
 			_backgroundSize =  new Rect();
 			
 			getMaxTileWidth();
 			int w =  _mapWidth, h =  GameDataManager.getInstance().getTileHeight() * GameDataManager.getInstance().getCurrentMap().length;
 		
 			_camera.set(0,0,_width,_height);
 			_screen.set(0,0,_width,_height);
 			_backgroundSize.set(0,0, _width,_height );
 			
 			Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
 			_puckImage = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
 			_puck = new Canvas(_puckImage);
 			
 			_levelBitmap = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
 			_level = new Canvas(_levelBitmap);
 			
 			_level = drawTiles(_level);
 			
 		}
 		
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//END :D
 		/////////////////////////////////////////////////////////////////////////////////////
 }
