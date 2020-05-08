 package com.tassadar.lorrismobile;
 
 import java.util.ArrayList;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.GestureDetectorCompat;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.animation.AnimationUtils;
 import android.widget.LinearLayout;
 import android.widget.PopupMenu;
 import android.widget.PopupMenu.OnMenuItemClickListener;
 
 import com.tassadar.lorrismobile.connections.Connection;
 import com.tassadar.lorrismobile.connections.ConnectionMgr;
 import com.tassadar.lorrismobile.connections.ConnectionsActivity;
 import com.tassadar.lorrismobile.modules.Tab;
 import com.tassadar.lorrismobile.modules.Tab.TabSelectedListener;
 import com.tassadar.lorrismobile.modules.TabListItem;
 
 public class WorkspaceActivity extends FragmentActivity implements TabSelectedListener {
 
     private static final int REQ_SET_CONN = 1;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if(Build.VERSION.SDK_INT < 11)
             requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         setContentView(R.layout.workspace);
 
        m_gest_detect = new GestureDetectorCompat(this, m_gestListener);
         m_tabs = new ArrayList<Tab>();
         m_active_tab = -1;
 
         if(Build.VERSION.SDK_INT >= 11)
             setUpActionBar();
         createNewTab(Tab.TAB_TERMINAL);
     }
 
     @TargetApi(11)
     private void setUpActionBar() {
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         m_tabs.clear();
         m_active_tab = -1;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.workspace, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected (MenuItem item) {
         switch(item.getItemId()) {
             case R.id.connection:
                 return true;
             case R.id.set_connection:
                 Intent i = new Intent(this, ConnectionsActivity.class);
                 startActivityForResult(i, REQ_SET_CONN);
                 return true;
         }
         return false;
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(resultCode != Activity.RESULT_OK)
             return;
         switch(requestCode) {
             case REQ_SET_CONN:
             {
                 if(data != null && !m_tabs.isEmpty()) {
                     int id = data.getIntExtra("connId", -1);
                     Connection conn = ConnectionMgr.getConnection(id); 
                     m_tabs.get(m_active_tab).setConnection(conn);
                 }
                 break;
             }
         }
     }
 
     private void setTabPanelVisible(boolean visible) {
         LinearLayout l = (LinearLayout)findViewById(R.id.tab_panel);
         if(visible == (l.getVisibility() == View.VISIBLE))
             return;
 
         if(visible){
             l.setVisibility(View.VISIBLE);
             l.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tab_panel_show));
         }else {
             l.setVisibility(View.GONE);
             l.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tab_panel_hide));
         }
     }
 
     private void createNewTab(int type) {
         Tab t = Tab.createTab(this, type);
         assert(t != null);
 
         String name = getResources().getStringArray(R.array.tab_names)[type];
         TabListItem it = new TabListItem(this, null, name);
         t.setTabListItem(it);
 
         LinearLayout l = (LinearLayout)findViewById(R.id.tab_list);
         l.addView(it.getView());
         m_tabs.add(t);
 
         FragmentManager mgr = getSupportFragmentManager();
         FragmentTransaction transaction = mgr.beginTransaction();
         transaction.add(R.id.tab_content_layout, t);
         transaction.commit();
 
         setActiveTab(m_tabs.size()-1);
     }
 
     private void setActiveTab(int idx) {
         if(idx < 0 || idx >= m_tabs.size())
             return;
 
         if(idx == m_active_tab)
             return;
 
         Tab old = null; 
         if(m_active_tab != -1)
             old = m_tabs.get(m_active_tab);
         Tab curr = m_tabs.get(idx); 
 
         FragmentManager mgr = getSupportFragmentManager();
         FragmentTransaction transaction = mgr.beginTransaction();
 
         if(old != null) {
             transaction.hide(old);
             old.setActive(false);
         }
         transaction.show(curr);
         transaction.commit();
 
         curr.setActive(true);
 
         m_active_tab = idx;
     }
 
     private Tab findTabById(int tabId) {
         int size = m_tabs.size();
         Tab t;
         for(int i = 0; i < size; ++i)
         {
             t = m_tabs.get(i);
             if(t.getTabId() == tabId)
                 return t;
         }
         return null;
     }
 
     private int findTabIdxById(int tabId) {
         int size = m_tabs.size();
         for(int i = 0; i < size; ++i)
             if(m_tabs.get(i).getTabId() == tabId)
                 return i;
         return -1;
     }
 
     @Override
     public void onTabSelectedClicked(int tabId) {
         int idx = findTabIdxById(tabId);
         if(idx == -1)
             return;
         setActiveTab(idx);
     }
 
     @Override 
     public boolean onTouchEvent(MotionEvent event){ 
         m_gest_detect.onTouchEvent(event);
         return super.onTouchEvent(event);
     }
 
    private GestureDetector.SimpleOnGestureListener m_gestListener = new  GestureDetector.SimpleOnGestureListener() {
         @Override
         public boolean onFling(MotionEvent ev1, MotionEvent ev2, float velX, float velY) {
             if(Math.abs(velX) > Math.abs(velY))
                 setTabPanelVisible(velX > 0);
             return true;
         }
    };

    public GestureDetector.SimpleOnGestureListener getGestureListener() {
        return m_gestListener;
     }
 
     private class CreateTabListener implements OnMenuItemClickListener {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
             switch(item.getItemId()) {
             case R.id.analyzer:
                 createNewTab(Tab.TAB_ANALYZER);
                 return true;
             case R.id.terminal:
                 createNewTab(Tab.TAB_TERMINAL);
                 return true;
             }
             return false;
         }
         
     }
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void showCreateTabMenuICS(View v) {
         PopupMenu popup = new PopupMenu(this, v);
         popup.setOnMenuItemClickListener(new CreateTabListener());
         MenuInflater inflater = popup.getMenuInflater();
         inflater.inflate(R.menu.create_tab, popup.getMenu());
         popup.show();
     }
 
     private void showCreateTabMenuLegacy() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.new_tab)
             .setItems(R.array.tab_names, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     createNewTab(which);
                 }
             });
         builder.create().show();
     }
 
     public void on_add_tab_btn_clicked(View v) {
         if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
             showCreateTabMenuLegacy();
         else
             showCreateTabMenuICS(v);
     }
 
     private GestureDetectorCompat m_gest_detect;
     private ArrayList<Tab> m_tabs;
     private int m_active_tab;
 }
