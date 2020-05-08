 package com.doyouspeak;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
import android.widget.ImageView;
 import android.widget.TextView;
 

 public class Expressions extends ListActivity {
 	Context context;
 	ImageButton actionHome;
 	ImageButton actionList;
 	ImageButton actionRecord;
 	Intent i = null;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d("Expressions", "Hello1");
         setContentView(R.layout.expressions);    
         
         context = getApplicationContext();
         
         
 	    actionHome = (ImageButton)findViewById(R.id.actionHome);
 	    actionList = (ImageButton)findViewById(R.id.actionMyList);
 	    actionRecord = (ImageButton)findViewById(R.id.actionRecord);
 	     
 	    actionHome.setOnClickListener(lHome);
 	    actionList.setOnClickListener(lList);
 	    actionRecord.setOnClickListener(lRecord);
         
         String[] myExpressions={"lorem", "ipsum", "dolor",
         		"sit", "amet",
         		"consectetuer", "adipiscing", "elit", "morbi", "vel",
         		"ligula", "vitae", "arcu", "aliquet", "mollis",
         		"etiam", "vel", "erat", "placerat", "ante",
         		"porttitor", "sodales", "pellentesque", "augue", "purus"};
             
         
          setListAdapter(new expressionsAdapter(context, R.layout.expressions_element, myExpressions));
        
    	}
 	
 	private View.OnClickListener lHome = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			i = new Intent(context, DoYouSpeak.class);
 			startActivity(i);
 		}
 	};
 
 	private View.OnClickListener lList = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			i = new Intent(context, MyList.class);
 			startActivity(i);
 		}
 	};
 
 	private View.OnClickListener lRecord = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			i = new Intent(context, RecordExpression.class);
 			startActivity(i);
 		}
 	};
 
 }
 
 class expressionsAdapter extends ArrayAdapter<String> {
 
 	private LayoutInflater mInflater;
 
 	private String[] mStrings;
 
 	private int mViewResourceId;
 
 	public expressionsAdapter(Context ctx, int viewResourceId, String[] strings) {
 		super(ctx, viewResourceId, strings);
 
 		mInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		mStrings = strings;
 
 		mViewResourceId = viewResourceId;
 		Log.d("Expressions", "Hello");
 
 	}
 
 	@Override
 	public int getCount() {
 		return mStrings.length;
 	}
 
 	@Override
 	public String getItem(int position) {
 		return mStrings[position];
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return 0;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		convertView = mInflater.inflate(mViewResourceId, null);
 
 		TextView tv = (TextView)convertView.findViewById(R.id.expressionsElement);
 		tv.setText(mStrings[position]);
 
 		return convertView;
 	}
 
 }
