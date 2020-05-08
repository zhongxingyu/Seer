 package edu.ycp.cs481.ycpgames;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 
 public class MainActivity extends Activity {
     //declare instance of buttons
     protected ImageButton TicTacToeButton;
     protected ImageButton DotsButton;
     protected ImageButton CheckersButton;
     protected ImageButton SettingsButton;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         //Get references to main menu buttons
         TicTacToeButton = (ImageButton)findViewById(R.id.TicTacToeButton);
         DotsButton = (ImageButton)findViewById(R.id.DotsButton);
         CheckersButton = (ImageButton)findViewById(R.id.CheckersButton);
         SettingsButton = (ImageButton)findViewById(R.id.SettingsButton);
 
         //Set listeners for main menu buttons
         TicTacToeButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //When TicTacToe button is pressed:
                 //Placeholder "Toast" till TicTacToe is implemented
                 Toast.makeText(MainActivity.this, R.string.TicTacToe, Toast.LENGTH_SHORT).show();
             }
         });
         DotsButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //When dots button is pressed:
                 //Placeholder "Toast" till Dots is implemented
                 Toast.makeText(MainActivity.this, R.string.Dots, Toast.LENGTH_SHORT).show();
             }
         });
         CheckersButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //When Checkers button is pressed:
                 //Placeholder "Toast" till Checkers is implemented
                 Toast.makeText(MainActivity.this, R.string.Checkers, Toast.LENGTH_SHORT).show();
             }
         });
         SettingsButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //When Checkers button is pressed:
                 //Placeholder "Toast" till Checkers is implemented
                 Toast.makeText(MainActivity.this, R.string.Settings, Toast.LENGTH_SHORT).show();
             }
         });
 
 
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 }
