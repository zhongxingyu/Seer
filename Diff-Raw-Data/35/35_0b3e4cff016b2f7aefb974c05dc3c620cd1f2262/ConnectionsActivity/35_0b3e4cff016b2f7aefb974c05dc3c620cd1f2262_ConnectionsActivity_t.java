 package com.tassadar.lorrismobile.connections;
 
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 
 import com.tassadar.lorrismobile.R;
 
 public class ConnectionsActivity extends FragmentActivity implements ConnFragmentInterface {
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if(Build.VERSION.SDK_INT < 11)
             requestWindowFeature(Window.FEATURE_NO_TITLE);
         else
             requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
         setContentView(R.layout.connections);
 
         if(Build.VERSION.SDK_INT >= 11)
             setUpActionBar();
 
         m_connBtns = new ImageButton[3];
         m_connBtns[Connection.CONN_BT_SP] = (ImageButton)findViewById(R.id.con_bt_btn);
         m_connBtns[Connection.CONN_TCP] = (ImageButton)findViewById(R.id.con_tcp_btn);
         m_connBtns[Connection.CONN_USB] = (ImageButton)findViewById(R.id.con_usb_btn);

         m_currType = Connection.CONN_BT_SP;
         if(savedInstanceState != null) {
             m_currType = savedInstanceState.getInt("currType", m_currType);
             Fragment f = getSupportFragmentManager().findFragmentById(R.id.conn_fragment_area);
             if(f != null)
                 ((ConnFragment)f).setConnInterface(this);
         } else
             switchFragment(m_currType);
         m_connBtns[m_currType].setSelected(true);
     }
 
     @TargetApi(11)
     private void setUpActionBar() {
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
 
         savedInstanceState.putInt("currType", m_currType);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //getMenuInflater().inflate(R.menu.connections, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected (MenuItem item) {
         switch(item.getItemId()) {
         }
         return false;
     }
 
     @Override
     public void onConnectionSelected(Connection conn) {
         Intent data = new Intent();
         data.putExtra("connId", conn.getId());
         setResult(RESULT_OK, data);
         finish();
     }
 
     public void on_conBtn_clicked(View btn) {
         int type = -1;
         for(int i = 0; i < m_connBtns.length; ++i)
         {
             m_connBtns[i].setSelected(m_connBtns[i] == btn);
             if(m_connBtns[i] == btn)
                 type = i;
         }
 
         if(type == -1 || m_currType == type)
             return;
 
         switchFragment(type);
     }
 
     private void switchFragment(int type) {
         ConnFragment f;
         switch(type) {
             case Connection.CONN_BT_SP:
                 f = new BTConnFragment();
                 break;
             case Connection.CONN_TCP:
                 f = new TCPConnFragment();
                 break;
             case Connection.CONN_USB:
                 f = new USBConnFragment();
                 break;
             default:
                 return;
         }
 
         f.setConnInterface(this);
 
         FragmentManager mgr = getSupportFragmentManager();
         FragmentTransaction transaction = mgr.beginTransaction();
         transaction.replace(R.id.conn_fragment_area, f);
         transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
         transaction.commit();
 
         m_currType = type;
     }
 
     public void setProgressIndicator(boolean visible) {
         if(Build.VERSION.SDK_INT < 11) {
             ProgressBar p = (ProgressBar)findViewById(R.id.progress);
             if(p != null)
                 p.setVisibility(visible ? View.VISIBLE : View.GONE);
         } else
             setProgressBarIndeterminateVisibility(visible);
     }
 
     private ImageButton[] m_connBtns;
     private int m_currType;
 }
