 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.eolwral.osmonitor.interfaces;
 
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.View.OnTouchListener;
 import android.webkit.WebView;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 import com.eolwral.osmonitor.*;
 import com.eolwral.osmonitor.preferences.Preferences;
 
 public class InterfaceList extends Activity implements OnGestureListener, OnTouchListener
 {
     private ExpandableListView UpdateInterface = null;
 	private JNIInterface JNILibrary = JNIInterface.getInstance();;
 
 	// Gesture 
 	private GestureDetector gestureScanner = new GestureDetector(this);;
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent me)
 	{
 		return gestureScanner.onTouchEvent(me);
 	}
 	 
 	@Override
 	public boolean onDown(MotionEvent e)
 	{
 		return true;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
 	{
 		try {
 			if (Math.abs(e1.getY() - e2.getY()) > CommonUtil.SWIPE_MAX_OFF_PATH)
 				return false;
 			else if (e1.getX() - e2.getX() > CommonUtil.SWIPE_MIN_DISTANCE && 
 										Math.abs(velocityX) > CommonUtil.SWIPE_THRESHOLD_VELOCITY) 
 				((TabActivity) this.getParent()).getTabHost().setCurrentTab(2);
 			else if (e2.getX() - e1.getX() > CommonUtil.SWIPE_MIN_DISTANCE &&
 										Math.abs(velocityX) > CommonUtil.SWIPE_THRESHOLD_VELOCITY) 
 				((TabActivity) this.getParent()).getTabHost().setCurrentTab(0);
 			else
 				return false;
 		} catch (Exception e) {
 			// nothing
 		}
 
 		return true;
 	}
 	
 	@Override
 	public void onLongPress(MotionEvent e)
 	{
 		return;
 	}
 	
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
 	{
 		return false;
 	}
 	
 	@Override
 	public void onShowPress(MotionEvent e)
 	{
 		return;
 	} 
 	
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		return false;
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		
 		// avoid exception - https://review.source.android.com/#/c/21318/
 		try {
 			event.getY();
 		}
 		catch (Exception e) { 
 			return false;
 		}
 
 		if(gestureScanner.onTouchEvent(event))
 			return true;
 		else
 		{
 			if(v.onTouchEvent(event))
 				return true;
 			return false;
 		}
 	}
 		
 	// Refresh
 	private Runnable InterfcaeRunnable = new Runnable() {
 		public void run() {
 
 			if(JNILibrary.doDataLoad() == 1) {
 		    	JNILibrary.doDataSwap();
 		    	UpdateInterface.invalidateViews();
 			}
 			
 	        InterfaceHandler.postDelayed(this, 1000);
 		}
 	};   
 	
 	Handler InterfaceHandler = new Handler();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Use a custom layout file
         setContentView(R.layout.interfacelayout);
 
         // Setup our adapter
         UpdateInterface = (ExpandableListView) findViewById(R.id.interfacelist);
         UpdateInterface.setOnTouchListener(this);
         UpdateInterface.setAdapter(new AllInterfaceList());
         
     }
     
     public boolean onCreateOptionsMenu(Menu optionMenu) 
     {
      	optionMenu.add(0, 1, 0, getResources().getString(R.string.menu_options));
        	optionMenu.add(0, 5, 0, getResources().getString(R.string.menu_forceexit));
         
     	return true;
     }
 
     @Override
     protected Dialog onCreateDialog(int id) 
     {
     	switch (id)
     	{
     	case 0:
     		AlertDialog.Builder HelpWindows = new AlertDialog.Builder(this);
     		HelpWindows.setTitle(R.string.app_name);
 			HelpWindows.setMessage(R.string.help_info);
 			HelpWindows.setPositiveButton(R.string.button_close,
 			   new DialogInterface.OnClickListener() {
 				   public void onClick(DialogInterface dialog, int whichButton) { }
 				}
 			);
 
    	        WebView HelpView = new WebView(this);
             HelpView.loadUrl("http://wiki.android-os-monitor.googlecode.com/hg/phonehelp.html?r=b1c196ee43855882e59ad5b015b953d62c95729d");
             HelpWindows.setView(HelpView);
 
         	return HelpWindows.create(); 
     	}
     	
     	return null;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	
         super.onOptionsItemSelected(item);
         switch(item.getItemId())
         {
         case 1:
             Intent launchPreferencesIntent = new Intent().setClass( this, Preferences.class);
             startActivityForResult(launchPreferencesIntent, 0);
         	break;
         case 5:
         	if(OSMonitorService.getInstance() != null)
         		OSMonitorService.getInstance().stopSelf();
 
         	CommonUtil.killSelf(this);
         	break;
 
         }
         
         return true;
     }
 
 
     @Override
     public void onPause() 
     {
     	InterfaceHandler.removeCallbacks(InterfcaeRunnable);
     	JNILibrary.doTaskStop();
     	super.onPause();
     }
 
     @Override
     protected void onResume() 
     {
     	JNILibrary.doTaskStart(JNILibrary.doTaskInterface);
     	InterfaceHandler.post(InterfcaeRunnable);
     	super.onResume();
     }
     
     public class AllInterfaceList extends BaseExpandableListAdapter {
    	
         public Spanned getChild(int groupPosition, int childPosition) {
         	
         	DecimalFormat SpeedFormat = new DecimalFormat(",###");
         	
         	Resources ResourceManager = getApplication().getResources();
         	
         	StringBuilder m_strBuf = new StringBuilder();
         	m_strBuf.setLength(0);
         	
         	m_strBuf.append(ResourceManager.getText(R.string.network_mac)+": ")
         			.append("<b>"+JNILibrary.GetInterfaceMac(groupPosition)+"</b><br />")
         			.append(ResourceManager.getText(R.string.network_rx))
         			.append(": <font color=\"#808080\">");
         	
         	long RxSize = JNILibrary.GetInterfaceInSize(groupPosition);
         	if(RxSize >= 1024*1024*1024)
         		m_strBuf.append((RxSize/(1024*1024*1024))+"G ("+SpeedFormat.format(RxSize).toString()+")");
         	else if(RxSize >= 1024*1024)
         		m_strBuf.append((RxSize/(1024*1024))+"M ("+SpeedFormat.format(RxSize).toString()+")");
         	else if(RxSize >= 1024)
         		m_strBuf.append((RxSize/1024)+"K ("+SpeedFormat.format(RxSize).toString()+")");
         	else 
         		m_strBuf.append(RxSize);
         	
         	m_strBuf.append("</font><br />")
         			.append(ResourceManager.getText(R.string.network_tx))
         			.append(": <font color=\"#808080\">");
 
         	long TxSize = JNILibrary.GetInterfaceOutSize(groupPosition);
         	if(TxSize >= 1024*1024*1024)
         		m_strBuf.append((TxSize/(1024*1024*1024))+"G ("+SpeedFormat.format(TxSize).toString()+")");
         	else if(TxSize >= 1024*1024)
         		m_strBuf.append((TxSize/(1024*1024))+"M ("+SpeedFormat.format(TxSize).toString()+")");
         	else if(TxSize >= 1024)
         		m_strBuf.append((TxSize/1024)+"K ("+SpeedFormat.format(TxSize).toString()+")");
         	else 
         		m_strBuf.append(JNILibrary.GetInterfaceInSize(groupPosition));
         	
         	String Flags = JNILibrary.GetInterfaceFlags(groupPosition);
         	
         	Flags = Flags.replace("$up$", ResourceManager.getText(R.string.network_status_up));
         	Flags = Flags.replace("$down$", ResourceManager.getText(R.string.network_status_down));
         	Flags = Flags.replace("$broadcast$", ResourceManager.getText(R.string.network_status_broadcast));
         	Flags = Flags.replace("$loopback$", ResourceManager.getText(R.string.network_status_loopback));
         	Flags = Flags.replace("$point-to-point$", ResourceManager.getText(R.string.network_status_p2p));
         	Flags = Flags.replace("$running$", ResourceManager.getText(R.string.network_status_running));
         	Flags = Flags.replace("$multicast$", ResourceManager.getText(R.string.network_status_multicast));
         	
         	m_strBuf.append("</font><br/>")
         			.append(ResourceManager.getText(R.string.network_status)+": ")
         			.append(Flags)
         			.append("<br/>");
 			
 			return Html.fromHtml(m_strBuf.toString());
         }
 
         public long getChildId(int groupPosition, int childPosition) {
             return childPosition;
         }
 
         public int getChildrenCount(int groupPosition) {
             return 1;
         }
 
         public TextView getGenericView() {
             // Layout parameters for the ExpandableListView
            //AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
            //        ViewGroup.LayoutParams.FILL_PARENT, 80);
 
             TextView textView = new TextView(getApplication());
            //textView.setLayoutParams(lp);
             
             // Center the text vertically
             textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL | Gravity.LEFT);
 
             // Set the text starting position
             if(CommonUtil.getScreenSize() == 2)
             	textView.setPadding(60, 5, 0, 0);
             else if( CommonUtil.getScreenSize() == 0)
             	textView.setPadding(20, 5, 0, 0);
             else 
             	textView.setPadding(36, 5, 0, 0);
             
             return textView;
         }
         
         public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                 View convertView, ViewGroup parent) {
             TextView textView = getGenericView();
             textView.setText(getChild(groupPosition, childPosition));
 
             if(CommonUtil.getScreenSize() == 2)
             	textView.setPadding(60, 5, 0, 0);
             else if(CommonUtil.getScreenSize() == 0)
             	textView.setPadding(20, 5, 0 , 0);
             else
             	textView.setPadding(36, 5, 0, 0);
 
             if(groupPosition % 2 == 0)
 	     		textView.setBackgroundColor(0x80444444);
 	     	else
 	     		textView.setBackgroundColor(0x80000000);
 
             return textView;
         }
 
         public Object getGroup(int groupPosition) {
         	Resources ResourceManager = getApplication().getResources();
         	
         	String Info = ResourceManager.getText(R.string.network_interface)+": "+
          			   JNILibrary.GetInterfaceName(groupPosition)+ "\n"+
          			   ResourceManager.getText(R.string.network_ip)+": "+
          			   JNILibrary.GetInterfaceAddr(groupPosition)+"/"+
          			   JNILibrary.GetInterfaceNetMask(groupPosition) + " ";
      		if(!JNILibrary.GetInterfaceAddr6(groupPosition).equals(""))
      		{
      			Info += "\n"+
      					ResourceManager.getText(R.string.network_ip6)+": "+
      					JNILibrary.GetInterfaceAddr6(groupPosition)+"/"+
      					JNILibrary.GetInterfaceNetMask6(groupPosition) + " ";
      		}
 
          
      		return Info;
         }
 
         public int getGroupCount() {
            return JNILibrary.GetInterfaceCounts();
         }
 
         public long getGroupId(int groupPosition) {
             return groupPosition;
         }
 
         public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                 ViewGroup parent) {
             TextView textView = getGenericView();
             textView.setText(getGroup(groupPosition).toString());
             
 	     	if(groupPosition % 2 == 0)
 	     		textView.setBackgroundColor(0x80444444);
 	     	else
 	     		textView.setBackgroundColor(0x80000000);
 
 	     	return textView;
         }
         
         public boolean isChildSelectable(int groupPosition, int childPosition) {
             return true;
         }
 
         public boolean hasStableIds() {
             return true;
         }
 
     }
 }
