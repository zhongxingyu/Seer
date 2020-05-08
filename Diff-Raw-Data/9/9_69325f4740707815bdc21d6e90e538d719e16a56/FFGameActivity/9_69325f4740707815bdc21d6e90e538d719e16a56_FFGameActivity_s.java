 package de.dennowar.view;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.dennowar.R;
 import de.dennowar.db.DataBaseHelper;
 import de.dennowar.plain.ActivityRegistry;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.util.Log;
 
 public class FFGameActivity extends Activity {
     /** Called when the activity is first created. */
 	private int pos, runde = 1;
 	private Context ctx = this;
 	private DataBaseHelper mAdapter;
 	private Cursor c;
 	private int grad;
 	private String antwort = null;
 	private List<Integer> list = new ArrayList<Integer>();
 	
 	private TextView tv_frage_nr;
 	private TextView tv_frage;
 	private RadioButton rba;
 	private RadioButton rbb;
 	private RadioButton rbc;
 	private RadioButton rbd;
 	private RadioGroup rg_antwort;
 	private Button btn_antwort;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         ActivityRegistry.register(this);
         
         grad = getIntent().getIntExtra("de.dennowar.view.grad", 1);
         Log.i("Grad: ", ""+grad);
         
         tv_frage_nr = (TextView) findViewById(R.id.tv_frage_nr);
         tv_frage = (TextView) findViewById(R.id.tv_frage);
 		rba = (RadioButton) findViewById(R.id.rb_a);
 		rbb = (RadioButton) findViewById(R.id.rb_b);
 		rbc = (RadioButton) findViewById(R.id.rb_c);
 		rbd = (RadioButton) findViewById(R.id.rb_d);
 		rg_antwort = (RadioGroup) findViewById(R.id.rg_antwort);
 		btn_antwort = (Button) findViewById(R.id.btn_antwort);
         
         mAdapter = new DataBaseHelper(this);
         
         try {
 			mAdapter.createDataBase();
 			mAdapter.openDataBase();
 			
			c = mAdapter.fetchQuestions(1);
 			pos = getRandPos(c.getCount());
 			list.add(pos);
 			c.moveToPosition(pos);
 						
 			show();
 			
 			rg_antwort.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				
 				@Override
 				public void onCheckedChanged(RadioGroup group, int checkedId) {
 					
 					switch(checkedId){
 					case R.id.rb_a:
 						antwort = "a";
 						break;
 					case R.id.rb_b:
 						antwort = "b";
 						break;
 					case R.id.rb_c:
 						antwort = "c";
 						break;
 					case R.id.rb_d:
 						antwort = "d";
 						break;
 					default:
 						antwort = "e";	
 					}
 				}
 			});
 			btn_antwort.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					
 					if(runde <= 15){
 						if(isRight()){
 													
 							pos = nextPosition();
 							
 							if(c.moveToPosition(pos)){ show();}
 							else { Log.i("Wertebereich überschritten: ", ""+pos);}
 						}
 						//falsch Antwort
 						else{
 							CharSequence s = "Falsche Antwort! Versuchs nochmal.";
 							Toast t = Toast.makeText(ctx, s, Toast.LENGTH_SHORT);
 							t.show();
 						}
 					}
 					//Spiel zu Ende
 					else{ startActivity(new Intent(FFGameActivity.this, WinActivity.class));}
 				}
 			});		
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         
     }
     
     public void show(){
     	
     	rg_antwort.clearCheck();
     	tv_frage_nr.setText(runde+". Frage:");
 		tv_frage.setText(c.getString(c.getColumnIndex("frage")));
 		rba.setText(c.getString(c.getColumnIndex("a")));
 		rbb.setText(c.getString(c.getColumnIndex("b")));
 		rbc.setText(c.getString(c.getColumnIndex("c")));
 		rbd.setText(c.getString(c.getColumnIndex("d")));
 		antwort = null;
 		runde++;
 	}
     
     private boolean isRight(){
     	boolean res = false;
     	if(c.getString(c.getColumnIndex("richtig")).equals(antwort)){
     		Log.i("isRight ", "Antwort: "+antwort+" Richtig: "+c.getString(c.getColumnIndex("richtig")));
     		res = true;
     	}
     	
     	return res;
     }
     
     private int getRandPos(int count){
     	double r = Math.random();
     	double d = r*100;
     	d= d/(100/count);
     	int i = (int)d;
     	if(i >= count){ i = getRandPos(count);}
     	Log.i("getRandPos: ", "Random: "+r+" double: "+d+" int: "+i);
     	return i;
     }
     
     private int nextPosition(){
     	int position = 0;
     	while(isNotNew(position = getRandPos(c.getCount()))){}
     	Log.i("nextPosition: ", ""+position);
     	return position;
     }
     
     private boolean isNotNew(int position){
     	boolean res = false;
     	
     	if(list.contains(position)){
     		Log.i("bereits vorhanden: ", ""+position);
     		res = true;
     	}
     	else {
     		Log.i("NEU: ", ""+position);
     		list.add(position);
     	}
     	return res;
     }
 }
