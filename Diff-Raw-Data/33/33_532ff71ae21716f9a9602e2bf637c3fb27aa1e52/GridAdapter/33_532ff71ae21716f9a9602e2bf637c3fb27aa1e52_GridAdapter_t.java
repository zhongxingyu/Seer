 package com.manas.anurag.calculator;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 
 /**
  *
  * @author anuragjain
  */
 
 public class GridAdapter extends BaseAdapter{
 	private Context ctx;
 	private Actions a;
 
 	public GridAdapter(Context c, Actions a){
 		ctx = c;
 		this.a = a;
 	}
 
 	@Override
 	public int getCount() {
 		return MainActivity.button_text_values.length;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Button btn;        
 		if (convertView == null) {          
 			btn = new Button(ctx); 
 		} 
 		else{           
 			btn = (Button) convertView;       
 		}       
 		final int pos = position;
 
 		btn.setOnClickListener(new View.OnClickListener() { 
 			public void onClick(View v) { 
 				Log.d("clicked", Integer.toString(pos));
 				a.performButtonClick(pos);
 			} 
 		}); 
 		btn.setGravity(Gravity.CENTER);
 		btn.setText(MainActivity.button_text[position]);  
		btn.setBackgroundResource(R.drawable.button1);
 		btn.setTextColor(Color.LTGRAY);
 		btn.setLines(1);
		Typeface font= Typeface.createFromAsset(ctx.getAssets(), "fonts/DejaVuSerif.ttf");
		btn.setTypeface(font); 

 		return btn;    	
 	}
 }
