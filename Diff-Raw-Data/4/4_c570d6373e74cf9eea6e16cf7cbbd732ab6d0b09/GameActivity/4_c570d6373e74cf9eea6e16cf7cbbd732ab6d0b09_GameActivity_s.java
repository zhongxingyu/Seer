 package dk.aau.cs.giraf.train.opengl;
 
 import java.awt.font.NumericShaper;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ClipData;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.DragShadowBuilder;
 import android.view.View.OnClickListener;
 import android.view.View.OnDragListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import dk.aau.cs.giraf.pictogram.Pictogram;
 import dk.aau.cs.giraf.train.R;
 import dk.aau.cs.giraf.train.opengl.game.GameData;
 import dk.aau.cs.giraf.train.profile.GameConfiguration;
 import dk.aau.cs.giraf.train.profile.GameConfiguration.Station;
 
 public class GameActivity extends Activity {
 
 	private GlView openGLView;
 	
 	private static ArrayList<LinearLayout> stationLinear;
 	private ArrayList<LinearLayout> cartsLinear;
 	public static LinearLayout stationCategoryLinear;
 	private LinearLayout trainDriverLinear;
 	private static GameConfiguration gameConf;
 	public static ImageButton fluteButton;
 	
 	private final static SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
     private static int sound;
     
     private AlertDialog alertDialog;
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.activity_game);
 		
 		GameActivity.sound = soundPool.load(this, R.raw.train_whistle, 1);
 		this.openGLView = (GlView) findViewById(R.id.openglview);
 		gameConf = new GameConfiguration(this, "Game 3", 2, -3);
 		gameConf.addStation(gameConf.new Station(2L));
 		gameConf.addStation(gameConf.new Station(4L));
 		gameConf.addStation(gameConf.new Station(3L));
 		gameConf.getStation(0).addAcceptPictogram(2L);
 		gameConf.getStation(1).addAcceptPictogram(4L);
 		gameConf.getStation(2).addAcceptPictogram(3L);
 		
 		
 		this.addFrameLayoutsAndPictograms(getNumberOfFrameLayouts(gameConf.getNumberOfPictogramsOfStations()));
 		
 		GameData.resetGameData();
 		
 		this.alertDialog = this.createAlertDialog();
 		
 		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
 	}
 	
 	private AlertDialog createAlertDialog() {
 	    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
         //myAlertDialog.setTitle("Title");
         alertDialogBuilder.setCancelable(false);
         alertDialogBuilder.setMessage("Er du sikker p at du vil afslutte?");
         alertDialogBuilder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface arg0, int arg1) {
                 //'Ja' button is clicked
                 finish();
             }
         });
         alertDialogBuilder.setNegativeButton("Annuller", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface arg0, int arg1) {
                 //'Annuller' button is clicked
                 GameData.onResume();
             }
         });
         return alertDialogBuilder.create();
 	}
 	
 	private int getNumberOfFrameLayouts(int numberOfPictogramsOfStations) {
 		int numberOfFrames = 0;
 		switch (numberOfPictogramsOfStations) {
 			case 5: case 6:
 				numberOfFrames = 6;
 				break;
 
 			case 3: case 4:
 				numberOfFrames = 4;
 				break;
 				
 			case 1: case 2:
 				numberOfFrames = 2;
 				break;
 			default:
 				break;
 			}
 		return numberOfFrames;
 	}
 
 	/**
 	 * Dynamically adds FrameLayout defined by numbersOfPictograms, The
 	 * Framelayout is then filled with pictograms.
 	 * 
 	 * @param numbersOfFrameLayouts
 	 */
 	private void addFrameLayoutsAndPictograms(int numbersOfFrameLayouts) {
 		initLayouts();
 		Drawable normalShape = getResources().getDrawable(R.drawable.shape);
 		int height = 300/(numbersOfFrameLayouts/2);
 		
 		for (LinearLayout stationlinear : stationLinear) {
 			for (int j = 0; j < (numbersOfFrameLayouts / 2); j++) {
 				LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(0,height,1.0f);
 				
 				FrameLayout frameLayout = new FrameLayout(this);
 				frameLayout.setOnDragListener(new DragListener());
 				frameLayout.setLayoutParams(linearLayoutParams);
 				
 				stationlinear.addView(frameLayout,j);
 			}
 		}
 
 		for (LinearLayout cartlinear : cartsLinear) {
 			for (int j = 0; j < (numbersOfFrameLayouts / 2); j++) {
 				LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(0,height,1.0f);
 				
 				FrameLayout frameLayout = new FrameLayout(this);
 				frameLayout.setOnDragListener(new DragListener());
 				frameLayout.setLayoutParams(linearLayoutParams);
 
 				cartlinear.addView(frameLayout,j);
 			}
 		}
 
 		// frame settings for StationCategory
 		LayoutParams categoryParams = new LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 		
 		FrameLayout categoryFrame = new FrameLayout(this);
 		categoryFrame.setBackgroundDrawable(normalShape);
 		stationCategoryLinear.addView(categoryFrame, categoryParams);
 
 		// frame setttings for TrainDriver
 		LayoutParams trainDriverParams = new LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.MATCH_PARENT);
 
 		FrameLayout trainDriverFrame = new FrameLayout(this);
 		trainDriverFrame.setBackgroundDrawable(normalShape);
 		trainDriverLinear.addView(trainDriverFrame, trainDriverParams);
 
 		// add pictograms to the frames
 		this.addPictogramsToFrames();
 		
 		ArrayList<LinearLayout> test = new ArrayList<LinearLayout>();
 		test.addAll(cartsLinear);
 		test.addAll(stationLinear);
 		for (LinearLayout lin : test) {
 			for (int i = 0; i < lin.getChildCount(); i++) {
 				lin.getChildAt(i).setBackgroundDrawable(normalShape);
 			}
 		}
 	}
 
 	/**
 	 * Find the LinearLayouts sepcified in activty_game.xml and stores the ref
 	 * in different lists.
 	 */
 	private void initLayouts() {
 		// StationLeft and Right
 		stationLinear = new ArrayList<LinearLayout>();
 		stationLinear.add((LinearLayout) findViewById(R.id.StationLeftLinearLayout));
 		stationLinear.add((LinearLayout) findViewById(R.id.StationRightLinearLayout));
 
 		// StationCategory
 		stationCategoryLinear = (LinearLayout) findViewById(R.id.StationCategoryLinearLayout);
 
 		// FluteButton
 		fluteButton = (ImageButton) findViewById(R.id.FluteImageButton);
 		fluteButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				trainDrive(true);
 			}
 		});
 		// Carts1 and 2
 		cartsLinear = new ArrayList<LinearLayout>();
 		cartsLinear.add((LinearLayout) findViewById(R.id.Cart1LinearLayout));
 		cartsLinear.add((LinearLayout) findViewById(R.id.Cart2LinearLayout));
 
 		// TrainDriver
 		trainDriverLinear = (LinearLayout) findViewById(R.id.TrainDriverLinearLayout);
 	}
 	
 	/**
 	 * Adds pictograms to Station, StationCategory and TrainDriver
 	 */
 	private void addPictogramsToFrames() {
 		List<Pictogram> pictogramsToAdd = new ArrayList<Pictogram>();
 		
 		if(GameData.numberOfStops == 0){
 			for (Station station : gameConf.getStations()) {
 				pictogramsToAdd.addAll(station.getAcceptPictograms());
 			}
 		}
 		
 		int nextpic = 0;
 		for (LinearLayout lin : stationLinear) {
 			for (int i = 0; i < lin.getChildCount(); i++) {
 				if(nextpic < pictogramsToAdd.size()){
 					Pictogram pic = pictogramsToAdd.get(nextpic);
 					nextpic++;
 					
 					pic.setOnTouchListener(new TouchListener());
 					
 					FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(
 							FrameLayout.LayoutParams.MATCH_PARENT,
 							FrameLayout.LayoutParams.MATCH_PARENT);
 					try {
 						((FrameLayout) lin.getChildAt(i)).addView(pic, frameLayoutParams);
 						((FrameLayout) lin.getChildAt(i)).setTag("filled");
 					} 
 					catch (Exception e) {
 						Log.d(GameActivity.class.getSimpleName(),
 								"Null value, when adding pictograms to FrameLayouts");
 					}
 					
 					pic.renderAll();
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		this.openGLView.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		this.openGLView.onResume();
 	}
 	/**
 	 * The method checks wether the pictograms on the station is the correct pictograms that are suppose to be dropped
 	 * the method draws the selected pictograms on to OpenGL surface
 	 * and makes the layouts invisble or visble depending on @param drive.
 	 * @param drive
 	 */
 	public static void trainDrive(boolean drive){
 		if (drive) {
 			if(GameData.currentTrainVelocity == 0f && GameData.numberOfStops < GameData.numberOfStations) {
 				boolean readyToGo = true;
 				if(GameData.numberOfStops == 0){
 					for (LinearLayout lin : stationLinear) {
 						for (int i = 0; i< lin.getChildCount();i++) {
 							FrameLayout frame = (FrameLayout)lin.getChildAt(i);
 							if(frame.getChildAt(0) != null){
 								readyToGo = false;
 							}
 						}
 					}
 				}
 				else{
 					//check if it is the correct pictogram on the right station.
 					if(checkPictogramsOnStaion(gameConf.getStation(GameData.numberOfStops - 1)) ==  false){
 						readyToGo = false;
 					}
 				}
 				
 				if(readyToGo){
 					//Draw pictograms with opengl
 					
 					
 					
 					stationCategoryLinear.setVisibility(View.GONE);
 					stationCategoryLinear.dispatchDisplayHint(View.VISIBLE);
 					
 					for (LinearLayout lin : stationLinear) {
 						lin.setVisibility(View.INVISIBLE);
 						lin.dispatchDisplayHint(View.INVISIBLE);
 					}
 					fluteButton.setVisibility(View.GONE);
 					fluteButton.dispatchDisplayHint(View.VISIBLE);
 					
 					deletePictogramsFromStation();
 					
 					setCategoryForNextStation(gameConf.getStation(GameData.numberOfStops));
 					
 					GameData.accelerateTrain();
 					
 					soundPool.play(sound, 1f, 1f, 0, 0, 0.5f);
 				}
             }
 			
 		} 
 		else {
 			for (LinearLayout lin : stationLinear) {
 				lin.setVisibility(View.VISIBLE);
 				lin.dispatchDisplayHint(View.VISIBLE);
 			}
 
 			stationCategoryLinear.setVisibility(View.VISIBLE);
 			stationCategoryLinear.dispatchDisplayHint(View.VISIBLE);
 			if(GameData.numberOfStops + 1 != GameData.numberOfStations){
 				fluteButton.setVisibility(View.VISIBLE);
 			}
 		}
 	}
 	
 	private static void setCategoryForNextStation(Station station) {
 		Pictogram cat = station.getCategory();
 		cat.renderAll();
 		((FrameLayout)stationCategoryLinear.getChildAt(0)).addView(cat, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 	}
 
 	private static void deletePictogramsFromStation() {
 		((FrameLayout)stationCategoryLinear.getChildAt(0)).removeAllViews();
 		for (LinearLayout lin : stationLinear) {
 			for (int i = 0; i < lin.getChildCount(); i++) {
 				((FrameLayout)lin.getChildAt(i)).removeAllViews();
 			}
 		}
 	}
 
 	private static boolean checkPictogramsOnStaion(Station station){
 		boolean answer = false;
 		int acceptedPics = 0;
 		for (LinearLayout lin : stationLinear) {
 			for (int i = 0; i < lin.getChildCount(); i++) {
 				if(((FrameLayout)lin.getChildAt(i)).getChildCount() > 0){
 					boolean foundAccPic = false;
 					for (Pictogram accPictogram : station.getAcceptPictograms()) {
 						if(accPictogram == ((FrameLayout)lin.getChildAt(i)).getChildAt(0)){
 							foundAccPic = true;
 							acceptedPics++;
 						}
 					}
 					
 					if(foundAccPic == false){
 						answer = false;
 						return answer;
 					}
 				}
 			}
 		}
 		if(acceptedPics == station.getAcceptPictograms().size()){
 			answer = true;
 		}
 		return answer;
 	}
 
 
 	/**
 	 * A touch listener that starts a drag event. There should also be a
 	 * receiver implementing {@link OnDragListener}.
 	 * 
 	 * @see DragListener
 	 */
 	private final class TouchListener implements OnTouchListener {
 		@Override
 		public boolean onTouch(View view, MotionEvent motionEvent) {
 			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
 				ClipData data = ClipData.newPlainText("", "");
 				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
 				view.startDrag(data, shadowBuilder, view, 0);
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 	
 
 	/**
 	 * A drag listner implementing an onDrag() method that runs when something
 	 * is dragged to it.
 	 */
 	private final class DragListener implements OnDragListener {
 		private Drawable enterShape;
 		private Drawable normalShape;
 
 		public DragListener() {
 			Resources resources = getResources();
 
 			this.enterShape = resources.getDrawable(R.drawable.shape_droptarget);
 			this.normalShape = resources.getDrawable(R.drawable.shape);
 		}
 
 		@Override
 		public boolean onDrag(View v, DragEvent event) {
 			if (event.getLocalState() != null) {
 				// do nothing, maybe return false..
 				final View draggedView = (View) event.getLocalState();
 
 				switch (event.getAction()) {
 				case DragEvent.ACTION_DRAG_STARTED:
 					// makes the draggedview invisible in ownerContainer
 					draggedView.setVisibility(View.INVISIBLE);
 					break;
 
 				case DragEvent.ACTION_DRAG_ENTERED:
 					// Change the background of droplayout(purely style)
 					v.setBackgroundDrawable(enterShape);
 					break;
 
 				case DragEvent.ACTION_DRAG_EXITED:
 					// Change the background back when exiting droplayout(purely
 					// style)
 					v.setBackgroundDrawable(normalShape);					
 					break;
 
 				case DragEvent.ACTION_DROP:
 					// Dropped, assigns the draggedview to the dropcontainer if
 					// the container does not already contain a view.
 					ViewGroup ownerContainer = (ViewGroup) draggedView.getParent();
 
 					FrameLayout dropContainer = (FrameLayout) v;
 					Object tag = dropContainer.getTag();
 
 					if (tag == null) {
 						ownerContainer.removeView(draggedView);
 						ownerContainer.setTag(null);
 						dropContainer.addView(draggedView);
 						dropContainer.setTag("filled");
 					}
 					break;
 
 				case DragEvent.ACTION_DRAG_ENDED:
 					// Makes the draggedview visible again after the view has
 					// been moved or the drop wasn't valid.
 					v.setBackgroundDrawable(normalShape);
 
 					// The weird bug is solves by this.
 					draggedView.post(new Runnable() {
 						@Override
 						public void run() {
 							draggedView.setVisibility(View.VISIBLE);
 						}
 					});
 					break;
 
 				}
 				return true;
 			} 
 			else {
 				return false;
 			}
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 	    super.onSaveInstanceState(outState);
 	    GameData.resetGameData();
 	}
 
 	
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         //Stop the user from unexpected back presses
 	    if (keyCode == KeyEvent.KEYCODE_BACK) {
 	        GameData.onPause();
 	        
             this.alertDialog.show();
 	        return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 }
