 package nl.sense.demo;
 
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 
 import android.widget.TableRow.LayoutParams;
 import android.widget.HorizontalScrollView;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class FragmentDisplay extends Fragment 
 {
 	public static final String TITLE = "TITLE";	
	public JSONArray output = new JSONArray();
 	public View v = null;
 	private float mx, my;	
 	private FragmentActivity fa;
 	private String pager = "";
 	
 	private ScrollView vScroll;
 	private HorizontalScrollView hScroll;
 
 	public static final FragmentDisplay newInstance(String message)
 	{
 		FragmentDisplay f = new FragmentDisplay();
 		Bundle bdl = new Bundle(1);
 		bdl.putString(TITLE, message);
 		f.setArguments(bdl);
 		return f;
 	}
 	
 	public void setPager(String pager)
 	{
 		this.pager = pager;
 	}
 
 	/**
 	 * Add text to the table of the fragment
 	 * 
 	 * @param message The json string data
 	 */
 	public void addText(final String message)
 	{		
 		JSONObject data = null;
 		try {
 			data = new JSONObject(message);
 		} catch (JSONException e) {
 			data = new JSONObject();
 			try {
 				data.put("value", message);
 			} catch (JSONException e1) { e1.printStackTrace();}
 		}
 
 		try {
 			Calendar cal = Calendar.getInstance();
 			cal.getTime();
 			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);		
 			data.put("time", sdf.format(cal.getTime()));
 		} catch (JSONException e) {		
 			e.printStackTrace();
 		}
		output.put(data);	
 		final JSONObject d = data;		
 		Handler h = new Handler(Looper.getMainLooper());
 		h.post(new Runnable() {			
 			@Override
 			public void run() {
 				if(v == null)
 					return;				
 				addRow(d);
 			}
 		});
 	}
 
 	/**
 	 * Display the fragment
 	 * 
 	 * @param inflater
 	 * @param container
 	 * @param savedInstanceState
 	 * @return
 	 */
 	@SuppressLint("NewApi")
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
 	{
 		String message = getArguments().getString(TITLE);		
 		this.v = inflater.inflate(R.layout.fragmentdisplay_layout, container, false);
 		vScroll = (ScrollView) v.findViewById(R.id.vScroll);
 		hScroll = (HorizontalScrollView) v.findViewById(R.id.hScroll); 
 		if(fa == null)
 			fa = getActivity();
 		if(fa != null)
 		{
 			new GestureDetector(fa,
 					new GestureDetector.SimpleOnGestureListener() {
 
 				@Override
 				public boolean onDown(MotionEvent e) {
 					return true;
 				}
 
 				@Override
 				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 						float velocityY) {
 
 					return super.onFling(e1, e2, velocityX, velocityY);
 				}
 			});
 
 
 			v.setOnTouchListener(new View.OnTouchListener() {
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					float curX, curY;	            	 
 					switch (event.getAction()) {
 
 					case MotionEvent.ACTION_DOWN:
 						mx = event.getX();
 						my = event.getY();
 						break;
 					case MotionEvent.ACTION_MOVE:
 						curX = event.getX();
 						curY = event.getY();
 						vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
 						hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
 						mx = curX;
 						my = curY;
 						break;
 					case MotionEvent.ACTION_UP:
 						curX = event.getX();
 						curY = event.getY();
 						vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
 						hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
 						break;
 					}
 
 					return true;
 				}
 			});
 		}
 		else
 			Log.e("FragmentDisplay", "Empty Fragment Activity");
 		TextView messageTextView = (TextView)v.findViewById(R.id.textViewTitle);
 		messageTextView.setText(message);	
 		TextView pagerText = (TextView)v.findViewById(R.id.pager);
 		pagerText.setText(pager);	
 		TableLayout tl = (TableLayout)v.findViewById(R.id.tableLayoutOutput);		
 		tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		for(int i = 0; i < output.length(); i++)
 		{
			try {
				JSONObject json = output.getJSONObject(i);				
				addRow(json);
			} catch (JSONException e) {			
				e.printStackTrace();
			}
 		}
 		return v;
 	}
 
 	/**
 	 * Add a row to the TableLayout
 	 * @param message
 	 */
 	@SuppressLint("NewApi")
 	private void addRow(JSONObject message)
 	{
 		try
 		{
 			if(v == null || fa == null)
 				return;
 			TableLayout tl = (TableLayout)v.findViewById(R.id.tableLayoutOutput);		
 			tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
 			tl.setBackgroundColor(Color.WHITE);
 			boolean createHeader = (tl.getChildCount() == 0);						
 			TableRow tr = new TableRow(fa);			
 			TextView date = new TextView(fa);
 			createView(tr, date,createHeader?"time":message.getString("time"));
 
 			Iterator<?> keys = message.keys();
 			SortedSet<String> header = new TreeSet<String>();
 			while(keys.hasNext())
 			{				
 				String key = (String)keys.next();
 				header.add(key);
 			}
 			keys = header.iterator();
 			while(keys.hasNext())
 			{
 				String key = (String)keys.next();
 				if(key.equals("time"))
 					continue;
 				TextView tv1 = new TextView(fa);
 				if(message.optJSONObject(key) != null)
 					addRow(message.optJSONObject(key));
 				else 
 					createView(tr, tv1,createHeader?key:message.getString(key));
 			}	
 			// skip the header but put on top
 			if(tl.getChildCount() > 0)
 				tl.addView(tr, 1);
 			else
 			{
 				tl.addView(tr);
 				addRow(message);
 			}

 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This function adds a TextView to a row and sets the layout
 	 *  
 	 * @param tr The table row
 	 * @param t The TextView
 	 * @param viewdata The text to set in the TextView
 	 */
 	@SuppressLint("NewApi")
 	public void createView(TableRow tr, TextView t, String viewdata) {
 		t.setText(viewdata);
 		//adjust the properties of the textView
 		t.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
 		t.setBackgroundResource(android.R.color.white);
 		t.setTextColor(Color.WHITE);
 		t.setTextSize(12);
 		t.setBackgroundColor(Color.BLACK);		
 		t.setPadding(10, 1, 5, 1);
 		//t.setWidth(0);
 		t.setGravity(1);
 		tr.setPadding(0, 0, 0, 1);
 		tr.setBackgroundColor(Color.WHITE);
 		tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));		
 		tr.addView(t); // add TextView to row.
 	}
 }
