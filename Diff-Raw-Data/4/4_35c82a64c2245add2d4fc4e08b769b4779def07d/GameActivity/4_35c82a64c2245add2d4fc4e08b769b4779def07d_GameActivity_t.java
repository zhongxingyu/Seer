 package com.burningman.tictactoe;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GameActivity extends Activity {
     //Creating variables
     ImageView imageView1;
     ImageView imageView2;
     ImageView imageView3;
     ImageView imageView4;
     ImageView imageView5;
     ImageView imageView6;
     ImageView imageView7;
     ImageView imageView8;
     ImageView imageView9;
     ImageView[] imageViews;
     TextView stateView;
     Button clearButton;
     Mark current;
     Core game;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.game);
 
         game = new Core();
 
         imageView1 = (ImageView) findViewById(R.id.ImageView1);
         imageView2 = (ImageView) findViewById(R.id.ImageView2);
         imageView3 = (ImageView) findViewById(R.id.ImageView3);
         imageView4 = (ImageView) findViewById(R.id.ImageView4);
         imageView5 = (ImageView) findViewById(R.id.ImageView5);
         imageView6 = (ImageView) findViewById(R.id.ImageView6);
         imageView7 = (ImageView) findViewById(R.id.ImageView7);
         imageView8 = (ImageView) findViewById(R.id.ImageView8);
         imageView9 = (ImageView) findViewById(R.id.ImageView9);
 
         imageViews = new ImageView[10];
         imageViews[1] = imageView1;
         imageViews[2] = imageView2;
         imageViews[3] = imageView3;
         imageViews[4] = imageView4;
         imageViews[5] = imageView5;
         imageViews[6] = imageView6;
         imageViews[7] = imageView7;
         imageViews[8] = imageView8;
         imageViews[9] = imageView9;
 
         stateView = (TextView) findViewById(R.id.state);
 
         clearButton = (Button) findViewById(R.id.clearButton);
 
         View.OnClickListener myListener = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 int i;
                 for (i = 1; i < 10; i++) {
                     if (imageViews[i] == view)
                         buttonPressed(i);
                 }
             }
         };
 
         int i;
         for (i = 1; i < 10; i++) {
             imageViews[i].setOnClickListener(myListener);
         }
 
         clearButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 onCreate(null);
             }
         });
 
         current = Mark.cross;
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
         savedInstanceState.putParcelable("core", game);
        savedInstanceState.putBoolean("current", (current == Mark.cross));
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         game = savedInstanceState.getParcelable("core");
        if (!savedInstanceState.getBoolean("current"))
            current = Mark.nought;
         //restoring the ImageViews
         int i, resId = 0;
         for (i = 1; i < 10; i++) {
             if (game.get_Field(i) == Mark.nought)
                 resId = R.drawable.nought;
             if (game.get_Field(i) == Mark.cross)
                 resId = R.drawable.cross;
             if (game.get_Field(i) == Mark.empty)
                 resId = R.drawable.empty;
             imageViews[i].setImageResource(resId);
         }
         checkEnd();
     }
 
     private void buttonPressed(int nr) {
         int resId;
         Mark next;
 
         if (current == Mark.cross) {
             resId = R.drawable.cross;
             next = Mark.nought;
         } else {
             resId = R.drawable.nought;
             next = Mark.cross;
         }
 
         imageViews[nr].setImageResource(resId);
         imageViews[nr].setOnClickListener(null);
         game.setField(nr, current);
         current = next;
         checkEnd();
     }
 
     private void checkEnd() {
         if (game.search_won() != Mark.empty) {
             if (game.search_won() == Mark.nought)
                 stateView.setText(R.string.circle_won);
             else
                 stateView.setText(R.string.cross_won);
 
             int i;
             for (i = 1; i < 10; i++)
                 imageViews[i].setOnClickListener(null);
             clearButton.setVisibility(View.VISIBLE);
 
         } else {
             if (game.check_draw()) {
                 stateView.setText(R.string.draw);
                 clearButton.setVisibility(View.VISIBLE);
             }
         }
     }
 }
