 package me.kukkii.cointoss;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 
 public class Cointoss extends Activity{
 
   private Coin guess;
   private Coin coin;
   private int result = -1;
 
 
   public void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
   }
 
 
   public void guess(View view){
     guess = null;
     int id = view.getId();
     if(id==R.id.button_HEAD){
       guess = Coin.HEAD;
     }
     if(id==R.id.button_TAIL){
       guess = Coin.TAIL;
     }
   }
 
   public void compare(Coin guess,Coin coin){
     if(guess == coin){
       result = 1;   //win
     }
     if(guess != coin){
       result = 0;   //lose
     }
   }
 
   public void showResult(int result){
     TextView textView = new TextView(this);
     if(result == 1){
      textView.setText(win);
     }
     if(result == 0){
      textView.setText(lose);
     }
   }
 
   public void game(){
     compare(guess,coin);
     showResult(result);
   }
 
 }
