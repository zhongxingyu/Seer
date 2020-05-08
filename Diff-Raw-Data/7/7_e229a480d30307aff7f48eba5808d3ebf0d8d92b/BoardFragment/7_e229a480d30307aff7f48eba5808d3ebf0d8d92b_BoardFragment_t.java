 package com.squirrelapps.aigameframework;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v7.widget.GridLayout;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.widget.Button;
 import android.widget.Toast;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Copyright (C) 2014 Francesco Vadicamo.
  */
 public class BoardFragment extends Fragment implements Button.OnClickListener
 {
     // Container Activity must implement this interface
     public interface BoardListener {
         public boolean onBoardLayout(ViewGroup rootLayout, ViewGroup boardLayout);
         public boolean onBoardCreate(Cell cell, Button button);
         public boolean onBoardUpdate(Cell cell, Button button);
     }
 
     private static final String TAG = BoardFragment.class.getSimpleName();
 
     protected BoardListener boardListener;
 
     //TODO da passare nel Bundle alla creazione del fragment
     final int xDim = 8, yDim = 8;
 
     final Board board = new SquareBoard(xDim, yDim);
 
     final Button[][] boardButtons = new Button[xDim][yDim];
 
     final Set<Button> animatedButtons = new HashSet<Button>();
     int lastDistance = 1;
     Button lastButton;
 
     @Override
     public void onAttach(Activity activity)
     {
         super.onAttach(activity);
         try{
             this.boardListener = (BoardListener) activity;
         }catch(ClassCastException e){
             throw new ClassCastException(activity.toString() + " must implement BoardListener");
         }
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         Log.d(TAG, "onCreate()");
         super.onCreate(savedInstanceState);
 
         //setHasOptionsMenu(false);
 
         //MainActivity mainActivity = (MainActivity)getActivity();
         //this.mainActivityWeakRef = new WeakReference<MainActivity>(mainActivity);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
         Log.d(TAG, "onCreateView()");
         final View rootView = inflater.inflate(R.layout.f_board, container, false);
         assert rootView != null;
 
         Activity activity = getActivity();
 
         GridLayout gridLayout = (GridLayout)rootView.findViewById(R.id.boardLayout);
         gridLayout.setUseDefaultMargins(false); //REMIND required for 0px gap between cells
         gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
         gridLayout.setColumnCount(xDim);
         gridLayout.setRowCount(yDim);
         //gridLayout.setBackgroundColor(Color.RED); //FIXME rimuovere a fine debug
 //        Configuration configuration = activity.getResources().getConfiguration();
 //        if ((configuration.orientation == Configuration.ORIENTATION_PORTRAIT)) {
 //            gridLayout.setColumnOrderPreserved(false);
 //        } else {
 //            gridLayout.setRowOrderPreserved(false);
 //        }
 
         for (int y = 0; y < yDim; ++y){
             //row.setBackgroundColor(Color.YELLOW); //FIXME rimuovere a fine debug
             for (int x = 0; x < xDim; ++x){
                 //TODO si potrebbe richiamare un metodo del listener per ricevere la view per la cella
                 Button btn = new Button(activity);
                 //width and height will be set later (see fillBoardLayout method)
 
                 Cell cell = board.cells[x][y];
 
                 //REMIND setTag e setOnClickListener vanno messi prima di richiamare il metodo del listener in modo che questo possa sovrascrivere eventualmente
                 if(!boardListener.onBoardCreate(cell, btn)){
                     onDefaultBoardCreate(cell, btn);
                 }
 
                 //TODO sono entrambi necessari, create e update!? o_O'
 
                 if(!boardListener.onBoardUpdate(cell, btn)){
                     onDefaultBoardUpdate(cell, btn);
                 }
 
                 GridLayout.Spec rowSpec = GridLayout.spec(x, GridLayout.CENTER);
                 GridLayout.Spec colSpec = GridLayout.spec(y, GridLayout.CENTER);
                 GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams(rowSpec, colSpec);
 //                cellParams.setGravity(Gravity.CENTER);
                 gridLayout.addView(btn, cellParams);
 
                 boardButtons[x][y] = btn;
             }
         }
 
        ViewTreeObserver obs = gridLayout.getViewTreeObserver();
         if(obs != null){
             obs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
                 @Override
                 public void onGlobalLayout()
                 {
                     ViewGroup rl = (ViewGroup)rootView.findViewById(R.id.rootLayout);
                     Log.v(TAG, "root layout: "+rl.getWidth()+", "+rl.getHeight());
 
                     GridLayout gl = (GridLayout)rootView.findViewById(R.id.boardLayout);
                     Log.v(TAG, "grid layout: "+gl.getWidth()+", "+gl.getHeight());
 
                     if(!boardListener.onBoardLayout(rl, gl)){
                         onDefaultBoardLayout(rl, gl);
                     }
 
                    ViewTreeObserver obs = gl.getViewTreeObserver();
                    assert obs != null;
                     obs.removeOnGlobalLayoutListener(this);
                 }
             });
         }
         //consider also the following
 //        gridLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
 //            @Override
 //            public boolean onPreDraw()
 //            {
 //                return false;
 //            }
 //        });
 
         return rootView;
     }
 
     protected boolean onDefaultBoardCreate(Cell cell, Button btn)
     {
         btn.setTag(cell);
         btn.setOnClickListener(this);
 
         return true;
     }
 
     protected boolean onDefaultBoardUpdate(Cell cell, Button btn)
     {
         //Cell cell = (Cell)btn.getTag();
         int x = cell.x;
         int y = cell.y;
 
         if(/*FIXME alternate*/false){
             btn.setBackgroundColor((x + y) % 2 == 0 ? Color.BLACK : Color.WHITE);
             btn.setTextColor((x + y) % 2 == 0 ? Color.WHITE : Color.BLACK);
         }else{
             btn.setBackgroundResource(R.drawable.cell_green_dark);
             btn.setTextColor(Color.WHITE);
         }
 
         btn.setText(/*board.cells[x][y].toString()*/"" + (y * xDim + x));
 
         return true;
     }
 
     protected boolean onDefaultBoardLayout(ViewGroup rootLayout, ViewGroup boardLayout)
     {
         int cell_size = (int)(Math.min(/*gridLayout*/rootLayout.getWidth()/xDim, /*gridLayout*/rootLayout.getHeight()/yDim));
         Log.v(TAG, "cell_size: "+cell_size);
 
         //TODO si potrebbero gestire Button, TextField e ImageView e utilizzare un Binder per gli altri (vd Adapter)
         Button btn;
         for(int i = 0; i < boardLayout.getChildCount(); i++){
             btn = (Button) boardLayout.getChildAt(i);
             assert btn != null;
 
             btn.setWidth(cell_size);
             btn.setMinimumWidth(cell_size);
             btn.setMaxWidth(cell_size);
 
             btn.setHeight(cell_size);
             btn.setMinimumHeight(cell_size);
             btn.setMaxHeight(cell_size);
         }
 
         return true;
     }
 
     /* (non-Javadoc)
      * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
      */
     @Override
     public void onActivityCreated(Bundle savedInstanceState)
     {
         Log.d(TAG, "onActivityCreated()");
         super.onActivityCreated(savedInstanceState);
     }
 
 
 
     /* (non-Javadoc)
      * @see android.support.v4.app.Fragment#onResume()
      */
     @Override
     public void onResume()
     {
         Log.d(TAG, "onResume()");
         super.onResume();
     }
 
     /* (non-Javadoc)
      * @see android.support.v4.app.Fragment#onPause()
      */
     @Override
     public void onPause()
     {
         Log.d(TAG, "onPause()");
         super.onPause();
     }
 
     /* (non-Javadoc)
      * @see android.support.v4.app.Fragment#onDestroy()
      */
     @Override
     public void onDestroy()
     {
         Log.d(TAG, "onDestroy()");
         super.onDestroy();
     }
 
 //    /* (non-Javadoc)
 //     * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
 //     */
 //    @Override
 //    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 //    {
 //        //super.onCreateOptionsMenu(menu, inflater);
 //        inflater.inflate(R.menu.f_board, menu);
 //    }
 //
 //    /* (non-Javadoc)
 //     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 //     */
 //    @Override
 //    public boolean onOptionsItemSelected(MenuItem item)
 //    {
 //        return true;
 //		/*
 //		// respond to menu item selection
 //		switch(item.getItemId()){
 //			case R.id.action_xxx:
 //				Intent xxx = new Intent(getActivity(), XXX.class);
 //				startActivity(xxx);
 //				return true;
 //				// break;
 //
 //			default:
 //				return super.onOptionsItemSelected(item);
 //		}
 //		*/
 //    }
 
 
     @Override
     public void onClick(View view)
     {
         if(view instanceof Button){
             Cell cell = (Cell)view.getTag();
 
             synchronized(BoardFragment.this){
                 Button button = (Button)view;
                 if(button == lastButton){
                     lastDistance = (lastDistance+1) % Math.max(Math.max(cell.x + 1, xDim - (cell.x /*+ 1*/)), Math.max(cell.y + 1, yDim - (cell.y /*+ 1*/)));
                 }else{
                     lastButton = button;
                     lastDistance = 1;
                 }
 
                 Toast.makeText(getActivity(), "" + cell + " (distance " + lastDistance + ")", Toast.LENGTH_SHORT).show();
 
                 stopAnimation();
 
                 Set<Cell> neighbors = board.borderNeighbors(cell, lastDistance);
                 Log.v(TAG, neighbors.size() + " neighbors of " + cell + " at distance "+lastDistance+" found: "+ Arrays.toString(neighbors.toArray()));
 //                for(Cell c : neighbors){
 //                    Button btn = boardButtons[c.x][c.y];
 //                    btn.setBackgroundResource(R.drawable.cell_neighbors_anim);
 //                    // Get the background, which has been compiled to an AnimationDrawable object.
 //                    AnimationDrawable frameAnimation = (AnimationDrawable)btn.getBackground();
 //                    // Start the animation (looped playback by default).
 //                    frameAnimation.start();
 //
 //                    animatedButtons.add(btn);
 //                }
                 animate(neighbors);
             }
         }
     }
 
     public synchronized void animate(Set<Cell> cells)
     {
         for(Cell c : cells){
             Button btn = boardButtons[c.x][c.y];
             btn.setBackgroundResource(R.drawable.cell_neighbors_anim);
             // Get the background, which has been compiled to an AnimationDrawable object.
             AnimationDrawable frameAnimation = (AnimationDrawable)btn.getBackground();
             assert frameAnimation != null;
             // Start the animation (looped playback by default).
             frameAnimation.start();
 
             animatedButtons.add(btn);
         }
 
     }
 
     public synchronized void stopAnimation()
     {
         for(Button btn : animatedButtons){
             AnimationDrawable frameAnimation = (AnimationDrawable)btn.getBackground();
             assert frameAnimation != null;
             // Stop the animation (looped playback by default).
             frameAnimation.stop();
 
             Cell cell = (Cell)btn.getTag();
             if(!boardListener.onBoardUpdate(cell, btn)){
                 onDefaultBoardUpdate(cell, btn);
             }
         }
         animatedButtons.clear();
     }
 }
