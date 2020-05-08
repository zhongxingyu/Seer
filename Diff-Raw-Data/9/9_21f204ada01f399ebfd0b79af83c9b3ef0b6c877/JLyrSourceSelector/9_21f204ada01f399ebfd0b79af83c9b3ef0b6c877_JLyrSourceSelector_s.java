 package com.jlyr.preference;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.jlyr.R;
 import com.jlyr.util.ProvidersCollection;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.util.Log;
 
 public class JLyrSourceSelector extends ListActivity {
 	
 	List<String> mArray = null;
 	MySimpleArrayAdapter mAdapter = null;
 	ChangeListener mChangeListener = null;
 	
 	String mKey = null;
 	
 	public static final String TAG = "JLyrSourceSelector"; 
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Intent i = getIntent();
         if (i != null) {
         	mKey = i.getStringExtra("key");
         }
         
         setContentView(R.layout.source_selector);
         
         Button okBtn = (Button) findViewById(R.id.ok_btn);
         Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
         
         okBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (mKey != null) {
 					SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 					Editor editor = SP.edit();
 					editor.putString(mKey, getPreferenceValue());
 					editor.commit();
 				}
 				finish();
 			}
         });
         
         cancelBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
         });
         
         populateList();
     }
     
     private void populateList() {
         ListView lv = getListView();
         lv.setTextFilterEnabled(true);
         
         lv.setCacheColorHint(0);
         ((TouchInterceptor) lv).setDragListener(mDragListener);
         ((TouchInterceptor) lv).setDropListener(mDropListener);
         ((TouchInterceptor) lv).setRemoveListener(mRemoveListener);
         lv.setDivider(null);
         //lv.setSelector(R.drawable.list_selector_background);
 
         ProvidersCollection providers = new ProvidersCollection(getBaseContext(), null);
         mArray = new ArrayList<String>(providers.getSources());
         
         mAdapter = new MySimpleArrayAdapter(this, mArray);
         setListAdapter(mAdapter);
     }
     
     public String getPreferenceValue() {
     	return join(mArray, ",");
     }
     
     static public String join(List<String> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
           if (first) {
              first = false;
           } else {
              sb.append(conjunction);
           }
           sb.append(item);
        }
        return sb.toString();
     }
     
     private void doMove(int from, int to) {
 		String obj_from = mArray.get(from);
        String obj_to = mArray.get(to);
        mArray.set(from, obj_to);
        mArray.set(to, obj_from);
         mAdapter.notifyDataSetChanged();
 	}
     
     private void doRemove(int which) {
     	mAdapter.remove(mArray.get(which));
     }
     
     public void setOnChangeListener(ChangeListener listener) {
     	mChangeListener = listener;
     }
     
     public static interface ChangeListener {
     	public void change(List<String> sources);
     }
     
     private TouchInterceptor.DragListener mDragListener =
         new TouchInterceptor.DragListener() {
         public void drag(int from, int to) {
             Log.i(TAG, "Drag from "+from+" to "+to);
         }
     };
     
     private TouchInterceptor.DropListener mDropListener =
         new TouchInterceptor.DropListener() {
         public void drop(int from, int to) {
             Log.i(TAG, "Drop from "+from+" to "+to);
             doMove(from, to);
             if (mChangeListener != null) {
             	mChangeListener.change(mArray);
             }
         }
     };
     
     private TouchInterceptor.RemoveListener mRemoveListener =
         new TouchInterceptor.RemoveListener() {
         public void remove(int which) {
         	Log.i(TAG, "Remove which "+which);
         	doRemove(which);
         	if (mChangeListener != null) {
             	mChangeListener.change(mArray);
             }
         }
     };
     
     static public class MySimpleArrayAdapter extends ArrayAdapter<String> {
     	private final Context context;
     	private List<String> values;
 
     	public MySimpleArrayAdapter(Context context, List<String> values) {
     		super(context, R.layout.draggable_list_item, values);
     		this.context = context;
     		this.values = values;
     	}
 
     	@Override
     	public View getView(int position, View convertView, ViewGroup parent) {
     		LayoutInflater inflater = (LayoutInflater) context
     				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     		View rowView = inflater.inflate(R.layout.draggable_list_item, parent, false);
     		
     		TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
     		textView.setText(values.get(position));
     		
     		// Change the icon for Windows and iPhone
     		/*
     		ImageView imageView = (ImageView) rowView.findViewById(android.R.id.icon);
     		String s = values.get(position);
     		if (s.startsWith("iPhone")) {
     			imageView.setImageResource(R.drawable.no);
     		} else {
     			imageView.setImageResource(R.drawable.ok);
     		}
     		*/
 
     		return rowView;
     	}
     }
 }
