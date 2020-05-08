 package tcsmobeel.prototype.levels;
 
 import java.util.Random;
import tcsmobeel.prototype.levels.Level;
 import tcsmobeel.prototype.R;
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 /**
  * 
  * @author BVCOE Team1 
  * 
  * @Description
  * Every Level is an activity .The name is of the form Level[n].  
  * Every Level activity has a specific level engine.The level engine implements 2 interfaces :
  * 
  * 1.OnClickListener : Listens to the click of 'ExecuteButton' .
  * 2.Level : Standard methods ->
  * 			 1. getSolution
  * 			 2. verifySolution
  * 			 3. displayResult
  */		     
 public class Level1Activity extends Activity {
     /** Called when the activity is first created. */
     StringBuffer executionOutput;
     Integer points;
     Button flip,execute;
     Spinner blackOrWhite; 
     EditText fly1,fly2,fly3,fly4,textS1,textS2,initLoop,loopInc,loopLimit;
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.level1);
         this.executionOutput=new StringBuffer(1000);
         OnClickListener uponDone=new LevelEngine();
         //Get The references
         blackOrWhite = (Spinner) findViewById(R.id.spinnerBOW);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.colors_array, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         blackOrWhite.setAdapter(adapter);
         fly1=(EditText)findViewById(R.id.editTextFly1);
        
         fly2=(EditText)findViewById(R.id.editTextFly2);
         fly3=(EditText)findViewById(R.id.editTextFly3);
         fly4=(EditText)findViewById(R.id.editTextFly4);
         textS1=(EditText)findViewById(R.id.editTextS1);
         textS2=(EditText)findViewById(R.id.editTextS5);
         initLoop=(EditText)findViewById(R.id.editTextInitCount);
        loopInc=(EditText)findViewById(R.id.editTextIncrement);
         loopLimit=(EditText)findViewById(R.id.editTextLimit);
         execute=(Button)findViewById(R.id.buttonDone);
         execute.setOnClickListener(uponDone);
         flip=(Button)findViewById(R.id.buttonFlip1);
         flip.setOnClickListener(new Flipper());
 	}
 	class  Flipper implements OnClickListener
 	{
 
 		@Override
 		public void onClick(View v) {
 			final Dialog d = new Dialog(Level1Activity.this);
 			Window w = d.getWindow();
 			w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			w.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
 					WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
 			d.setTitle("CodePlay");
 			d.setContentView(R.layout.level1lpic);
 			Button flip = (Button) d.findViewById(R.id.buttonFlip);
 			
 			
 			flip.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					d.dismiss();
 				}
 			});
 			d.show();
 		}
 			
 		}
 		
 
 	class LevelEngine implements OnClickListener,Level  //TODO: This should be a separate class,not an inner class
 	{
 
 		Integer s1,s2,loopInit,loopIncre,loopLimitVal,f1,f2,f3,f4,colorChoice;
 		Random r;
 		LevelEngine()
 		{
 			r=new Random(1000);
 		}
 		@Override
 		public void onClick(View v) {
 			// A) get the user's solution
 			getSolution();
 			// B) parse it,get the result indicating whether the problem has been solved or not
 			boolean result=verifySolution();
 			// C) display the result
 			displayResult(result);
 		}
 	
 		public void getSolution()
 		{
 			try
 			{
 			s1=Integer.parseInt(textS1.getText().toString());
 			loopInit=Integer.parseInt(initLoop.getText().toString());
 			loopIncre=Integer.parseInt(loopInc.getText().toString());
 			loopLimitVal=Integer.parseInt(loopLimit.getText().toString());
 			s2=Integer.parseInt(textS2.getText().toString());
 			f1=Integer.parseInt(fly1.getText().toString());
 			f2=Integer.parseInt(fly2.getText().toString());
 			f3=Integer.parseInt(fly3.getText().toString());
 			f4=Integer.parseInt(fly4.getText().toString());
 			}
 			catch(NumberFormatException nfe)
 			{
 				Log.e("Error Parsing Input", nfe.toString());
 			}
 			}
 		public boolean verifySolution()
 		{
 			String randomColor; //a random color that comes out of the box
 			int flightDistance; //corresponding flight distance
 			
 			points=0;
 			
 			executionOutput.append("");
 			executionOutput.append("Codee Moved "+s1.toString()+"steps forward\n");
 			//TODO append failure reasons to the if clauses
 			if(s1!=5)
 			{
 				return false;
 			}
 			points+=4;
 			executionOutput.append("After a good start,he got prepared to jump\n");
 			if(loopInit!=1)
 			{
 				return false;
 			}
 			points+=4;
 			executionOutput.append("After a smooth first jump,he increased his power by "+loopIncre.toString()+"\n");
 			if(loopIncre!=2)
 			{
 				return false;
 			}
 			points+=4;
 			executionOutput.append("Then he kept on jumping till the power became "+loopLimitVal.toString()+"\n");
 			if(loopLimitVal!=5)
 			{
 				return false;
 			}
 			points+=4;
 			executionOutput.append("he then moved "+s2.toString()+"steps forward\n");
 			if(s2!=7)
 			{
 				return false;
 			}
 			points+=4;
 			executionOutput.append("The difficult times stared at him as he reached the \"Alternate Valley\"");
 			if((f1!=3)||(f2!=5)||(f3!=5)||(f4!=7))
 			{
 				return false;  
 			}
 			executionOutput.append("\n Codee Managed To Strike the right balance of 3-5-5-7\n");
 			
 			executionOutput.append("but now he has to make a choice");
 			colorChoice=blackOrWhite.getSelectedItemPosition();
 			if(colorChoice==0)
 			{
 				executionOutput.append("But alas! he fails!");
 				return false;
 			}
 			if(r.nextInt()%2==0)
 			{
 				randomColor="Black";
 				flightDistance=13;
 			}
 			else
 			{
 				randomColor="White";
 				flightDistance=10;
 			}
 			
 			executionOutput.append("\nCodee found a "+randomColor+"Ball in the box,and made the right choice of flying "+flightDistance+ "Units\n");
 			points+=24;
 			executionOutput.append("\nAnd he finally reached to the other end safely!!");
 			
 			return true;
 			
 		}
 		public void displayResult(boolean result)
 		{
 			executionOutput.append("\nYour Score : "+points.toString());;
 			final Dialog d = new Dialog(Level1Activity.this);
 			Window w = d.getWindow();
 			w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			w.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
 					WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
 			if(result)
 			{
 			d.setTitle("CodePlay : You have done it!");
 			}
 			else
 			{
 				d.setTitle("\tCodePlay : Try Again :) ");
 					
 			}
 			d.setContentView(R.layout.result);
 			Button ok = (Button) d.findViewById(R.id.buttonOk);
 			TextView res = (TextView) d.findViewById(R.id.textViewResult);
 			res.setText(executionOutput);
 			
 			ok.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					d.dismiss();
 				}
 			});
 			d.show();
 		}
 	
 		
 	}
     
 }
