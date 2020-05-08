 package me.kukkii.janken;
 
 import me.kukkii.janken.bot.BotManager;
 import me.kukkii.janken.bot.AbstractBot;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.graphics.drawable.Drawable;
 import android.content.res.Resources;
 
 public class Janken extends Activity{
 
   private AbstractBot bot;
 //  private ImageView view = (ImageView) findViewById(R.id.view_BOT);
 
   private Hand userHand;
   private Hand botHand;
   private Result result;
   private Judge judge = new Judge();
 
   public void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
   }
 
   public void onResume(){
     super.onResume();
     newGame();
   }
 
   public void afterPon(){
     botHand = bot.hand2();
     result = judge.judge(userHand, botHand);
     String text = bot.getName() + "\n" + userHand.toString() + "\n" + botHand.toString() + "\n" + result.toString();
 
     Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
     newGame();
  }
 
     public void newGame(){
       bot =(AbstractBot) BotManager.getManager().next();
       Resources res = getResources();
       int drawableId = bot.getImage();
  //     Drawable drawable = res.getDrawable(drawableId);
       ImageView view = (ImageView) findViewById(R.id.view_BOT);
       view.setImageResource(drawableId); 
      TimerThread tt = new TimerThread(this);
      Thread t = new Thread(tt);
      t.start();
     }
 
     public void jan(){
       Toast.makeText(getApplicationContext(), "Jan", Toast.LENGTH_SHORT).show();
     }
 
     public void ken(){
       Toast.makeText(getApplicationContext(), "Ken", Toast.LENGTH_SHORT).show();
     }
     
     public void pon(){
       Toast.makeText(getApplicationContext(), "pon!", Toast.LENGTH_SHORT).show();
     }
 
     public void hand(View view){
       userHand = null;
       int id = view.getId();
       if(id==R.id.button_ROCK){
         userHand = Hand.ROCK;
       }
       if(id==R.id.button_SCISSOR){
         userHand = Hand.SCISSOR;
       }
       if(id==R.id.button_PAPER){
         userHand = Hand.PAPER;
       }
       
     }
 
 }
 
 
