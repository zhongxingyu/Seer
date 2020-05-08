 package com.tassadar.lorrismobile.modules;
 
 
 import jackpal.androidterm.emulatorview.EmulatorView;
 import jackpal.androidterm.emulatorview.TermSession;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 
import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 
 import com.tassadar.lorrismobile.R;
import com.tassadar.lorrismobile.WorkspaceActivity;
 
 public class Terminal extends Tab {
 
     public Terminal(TabSelectedListener listener) {
         super(listener);
         m_type = TAB_TERMINAL;
         m_termSession = new TermSession();
 
         PipedInputStream str = new PipedInputStream();
         try {
             m_outStr = new PipedOutputStream(str);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         m_termSession.setTermIn(str);
         m_termSession.setTermOut(new TermStream());
     }
 
     @Override
     public void onCreate (Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.terminal, container, false);

         EmulatorView e = (EmulatorView)v.findViewById(R.id.term);
         e.attachSession(m_termSession);

         DisplayMetrics metrics = new DisplayMetrics();
         getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
         e.setDensity(metrics);

        Activity act = getActivity();
        if(act instanceof WorkspaceActivity)
            e.setExtGestureListener(((WorkspaceActivity)act).getGestureListener());

         e.setTextSize(16);
         return v;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         m_termSession.finish();
     }
 
     @Override
     public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.terminal, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected (MenuItem item) {
         switch(item.getItemId()) {
             case R.id.toggle_keyboard:
                 toggleKeyboard();
                 return true;
         }
         return false;
     }
 
     @Override
     public void dataRead(byte[] data) {
         try {
             m_outStr.write(data);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private class TermStream extends OutputStream {
         @Override
         public void write(int oneByte) throws IOException {
             // not used
         }
 
         @Override
         public void write (byte[] buffer, int offset, int count) throws IOException {
             if(m_conn != null)
                 m_conn.write(buffer, offset, count);
         }
     }
 
     @Override
     public void connected(boolean connected) {
         //if(connected)
             //m_conn.write(new byte[] { 0x74, 0x7E, 0x7A, 0x33 });
     }
 
     private void toggleKeyboard() {
         InputMethodManager imm = (InputMethodManager)
                 getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
     }
     private TermSession m_termSession;
     private PipedOutputStream m_outStr;
 }
