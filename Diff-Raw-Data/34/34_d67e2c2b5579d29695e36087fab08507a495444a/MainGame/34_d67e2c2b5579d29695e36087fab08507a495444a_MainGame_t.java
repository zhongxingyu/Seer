 package com.game.pileon;
 
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 @TargetApi(11)
 public class MainGame extends Activity
 implements View.OnTouchListener
 {
 	private static int NUM_PILES = 3;
 	private static int NUM_HANDS = 5;
 	private GameEngine mGameEngine;
 	private DragController mDragController;   // Object that sends out drag-drop events while a view is being moved.
 	private DragLayer mDragLayer;             // The ViewGroup that supports drag-drop.
 	private PointTracker mPointTracker;
 	private TextView mPointView;
 	private boolean gameInProgress;
 	private SavedGame savedGameState;
 	private ArrayList<PileView> mPileViews;
 	private ArrayList<HandView> mHandViews;
 	private float mScaleFactor;
 	private boolean scalingComplete = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		Log.i("PO Save", "onCreate");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game_screen);
 
 		Intent intent = getIntent();
 		gameInProgress = intent.getBooleanExtra("com.game.pileon.GameInProgress", false);
 
 		mDragController = new DragController(this);
 
 		mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
 		mDragLayer.setDragController(mDragController);
 
 		if(gameInProgress == false){
 			mGameEngine = new GameEngine();
 			setupViews();
 
 			//must be run after setupViews has initialized the text view for the point tracker
 			mPointTracker = new PointTracker(0); 
 			mPointTracker.setPointView(mPointView);
 			mGameEngine.setPointTracker(mPointTracker);
 		}
 		else{
 			//recreate game
 			//writing and reading
 			Log.i("PO Save", "continuing game");
 			readSaveData();
 
 			if(savedGameState != null){
 				mGameEngine = new GameEngine(savedGameState);
 				setupViews();
 				mPointTracker = savedGameState.savePointTracker;
 				mPointTracker.setPointView(mPointView);
 			}
 			Log.i("PO Save", "finished read");
 		}
 	}
 
 
 	@Override
 	public void onPause()
 	{
 		savedGameState = new SavedGame(
 				mGameEngine.getPileList(), 
 				mGameEngine.getHandList(), 
 				mGameEngine.Deck, 
 				mPointTracker
 				);
 		writeSaveData();
 		Log.i("PO Save", "finished write");
 		super.onPause();
 		Log.i("PO Save", "onPause");
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		Log.i("PO Save", "onResume");
 	}
 
 
 	public void backToMain(View view)
 	{
 		//TODO insert code to save the state of the game
 		Log.i("PO Save", "backToMain");
 		Intent intent = new Intent(MainGame.this, GameMenu.class);
 		intent.putExtra("com.game.pileon.GameInProgress", true);
 		startActivity(intent);
 	}
 
 	public boolean onTouch(View v, MotionEvent ev) 
 	{
 		final int action = ev.getAction();
 
 		if (action == MotionEvent.ACTION_DOWN) {
 			startDrag(v);
 		}
 
 		return true;
 	}
 
 	public void updatePoints(Card cardPlayed, Card pileCard){
 		mPointTracker.processMove(cardPlayed, pileCard);
 	}
 
 	public void writeSaveData(){
 		Serializer serializer = new Persister();
 
 		try
 		{
 			FileOutputStream result = openFileOutput("saveData.xml", Context.MODE_PRIVATE);
 			try
 			{
 				serializer.write(savedGameState, result);
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e1)
 		{
 			e1.printStackTrace();
 		}
 	}
 
 	public void readSaveData(){
 		Serializer serializer = new Persister();
 
 		try
 		{
 			FileInputStream source = openFileInput("saveData.xml");
 			try
 			{
 				savedGameState = serializer.read(SavedGame.class, source);
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e1)
 		{
 			e1.printStackTrace();
 		}	
 	}
 
 
 	/**
 	 * Start dragging a view.
 	 *
 	 */    
 
 	public boolean startDrag (View v)
 	{
 		// Let the DragController initiate a drag-drop sequence.
 		// I use the dragInfo to pass along the object being dragged.
 		// I'm not sure how the Launcher designers do this.
 		Object dragInfo = v;
 		Log.i("PO Drag", "startDrag in MainGame runs");
 		mDragController.startDrag (v, mDragLayer, dragInfo, DragController.DRAG_ACTION_MOVE);
 		return true;
 	}
 
 	protected void onDestroy()
 	{
 		super.onDestroy();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState)
 	{
 		//run FIRST
 		super.onSaveInstanceState(savedInstanceState);
 		Log.i("PO Save", "onSaveInstanceState");
 
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState)
 	{
 		super.onRestoreInstanceState(savedInstanceState);
 		Log.i("PO Save", "onRestoreInstanceState");
 	}
 
 	/**
 	 * One-time setup of initial PileViews and HandViews. After this is done, the GameEngine will update
 	 * the PileViews and HandViews with the underlying card(s) as the game progresses
 	 */
 	public void setupViews()
 	{
 		//setup PileViews - three total
 		mPileViews = new ArrayList<PileView>();
 		ArrayList<Pile> pileList = mGameEngine.getPileList();
 		TableRow pileRow = (TableRow)findViewById(R.id.PileRow);
 
 		for(int pileCount = 0; pileCount < NUM_PILES; pileCount++){
 			PileView pileView = new PileView(this, pileList.get(pileCount));
 			pileView.setGameEngine(mGameEngine);
 			mDragController.addDropTarget(pileView);
 			mPileViews.add(pileView);
 			pileRow.addView(pileView, pileCount);
 		}
 
 		//setup HandViews - five total
 		mHandViews = new ArrayList<HandView>();
 		ArrayList<Hand> handList = mGameEngine.getHandList();
 		TableRow handRow = (TableRow)findViewById(R.id.HandRow);
 
 		for(int handCount = 0; handCount < NUM_HANDS; handCount++){
 			HandView handView = new HandView(this, handList.get(handCount));
 			handView.setOnTouchListener(this);
 			TableRow.LayoutParams hlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
 			handView.setLayoutParams(hlp);
 
 			mHandViews.add(handView);
 
 			handRow.addView(handView, handCount);
 		}
		
 		mPointView = (TextView) findViewById(R.id.pointTracker);
 	}
 
 
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		if (!scalingComplete) // only do this once
 		{
 			mScaleFactor = getScaleFactor(findViewById(R.id.game_table_layout), findViewById(R.id.drag_layer));
 			scaleViewAndChildren(findViewById(R.id.game_table_layout), mScaleFactor);
 			scalingComplete = true;
 		}
 		super.onWindowFocusChanged(hasFocus);
 	}
 
 	private float getScaleFactor(View rootView, View container)
 	{
 		// Compute the scaling ratio. Note that there are all kinds of games you could
 		// play here - you could, for example, allow the aspect ratio to be distorted
 		// by a certain percentage, or you could scale to fill the *larger* dimension
 		// of the container view (useful if, for example, the container view can scroll).
 		Log.i("POScale", "container width is: " + container.getWidth());
 		Log.i("POScale", "rootview width is: " + rootView.getWidth());
 		Log.i("POScale", "container height is: " + container.getHeight());
 		Log.i("POScale", "rootview height is: " + rootView.getHeight());
 		float xScale = (float)container.getWidth() / rootView.getWidth();
 		float yScale = (float)container.getHeight() / rootView.getHeight();
 		//scale = Math.min(xScale, yScale);
 		float scale;
 
 		if( Float.compare(yScale, xScale) > 0 ) { 
 			scale = yScale;
 		}
 		else {
 			scale = xScale;
 		}
 
 		if( Float.compare(scale, (float)1.75) > 0){
 			scale = (float) 1.75;
 		}
 
 		Log.i("POScale", "scale factor is: " + Float.toString(scale));
 
 		// Scale our contents
 		return scale;
 	}
 
 	/** Scale the given view, its contents, and all of its children by the given factor.
 	 * @param root	The root view of the UI subtree to be scaled
 	 * @param scale	The scaling factor
 	 */
 	public void scaleViewAndChildren(View root, float scale)
 	{
 		// Retrieve the view's layout information
 		Log.i("POScale", "seeing about scaling view: " + root.toString());
 		ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
 
 		// Scale the view itself
 		if (layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT && 
 				layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT)
 		{
 			Log.i("POScale", "scaling view: " + root.toString() + " old width: " + layoutParams.width + " new width: " + layoutParams.width*scale);
 			layoutParams.width *= scale;
 		}
 		if (layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT && 
 				layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT)
 		{
 			Log.i("POScale", "scaling view: " + root.toString() + " old height: " + layoutParams.height + " new height: " + layoutParams.height*scale);
 			layoutParams.height *= scale;
 		}
 
 
 		if( root instanceof HandView){
 			HandView HandRoot = (HandView) root;
 			Log.i("POScale", "scaling a Hand view: " + HandRoot.toString());
 			int width = (int) (HandRoot.getDrawable().getIntrinsicWidth() * scale);
 			int height = (int) (HandRoot.getDrawable().getIntrinsicHeight() * scale);
 			TableRow.LayoutParams handLayoutParams = new TableRow.LayoutParams(width, height, 1.0f);
 			root.setLayoutParams(handLayoutParams);
 			return;
 		}
 		if( root instanceof PileView){
 			PileView pileRoot = (PileView) root;
 			Log.i("POScale", "scaling a Pile view: " + pileRoot.toString());
 			int width = (int) (pileRoot.getDrawable().getIntrinsicWidth() * scale);
 			int height = (int) (pileRoot.getDrawable().getIntrinsicHeight() * scale);
 			TableRow.LayoutParams pileLayoutParams = new TableRow.LayoutParams(width, height, 1.0f);
 			root.setLayoutParams(pileLayoutParams);
 			return;
 		}
 
 
 		// If this view has margins, scale those too
 		if (layoutParams instanceof ViewGroup.MarginLayoutParams)
 		{
 			ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams)layoutParams;
 			marginParams.leftMargin *= scale;
 			marginParams.rightMargin *= scale;
 			marginParams.topMargin *= scale;
 			marginParams.bottomMargin *= scale;
 		}
 
 		// Set the layout information back into the view
 		root.setLayoutParams(layoutParams);
 
 		// Scale the view's padding
 		root.setPadding(
 				(int)(root.getPaddingLeft() * scale), 
 				(int)(root.getPaddingTop() * scale), 
 				(int)(root.getPaddingRight() * scale), 
 				(int)(root.getPaddingBottom() * scale));
 
 		// If the root view is a TextView, scale the size of its text. Note that this is not quite precise -
 		// it appears that text can't be exactly scaled to any desired size, presumably due to limitations
 		// of the font system. You may have to make your fonts a little bit smaller than you otherwise might
 		// in order to make sure that the text will always fit at any scaling factor.
 		if (root instanceof TextView)
 		{
 			TextView textView = (TextView)root; 
 			Log.d("POScale", "Scaling text size from " + textView.getTextSize() + " to " + textView.getTextSize() * scale);
 			textView.setTextSize(textView.getTextSize() * scale);
 			InputMethodManager imm = (InputMethodManager)getSystemService(
 					Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
 		}
 
 		// If the root view is a ViewGroup, scale all of its children recursively
 		if (root instanceof ViewGroup)
 		{
 			Log.i("POScale", "root is instanceof ViewGroup: " + root.toString());
 			ViewGroup groupView = (ViewGroup)root;
 			for (int cnt = 0; cnt < groupView.getChildCount(); ++cnt){
 				//Log.i("POScale", "child of viewgroup: " + root.toString() + " is " + groupView.getChildAt(cnt).toString());
 				scaleViewAndChildren(groupView.getChildAt(cnt), scale);
 			}
 		}
 	}  
 
 
 
 }
