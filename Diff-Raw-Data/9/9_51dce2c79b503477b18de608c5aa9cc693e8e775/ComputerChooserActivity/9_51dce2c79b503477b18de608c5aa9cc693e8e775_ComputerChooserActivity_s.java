 // Copyright 2011-2012, Art Hare
 // This file is part of WifiLapper.
 
 //WifiLapper is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 
 //WifiLapper is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 
 //You should have received a copy of the GNU General Public License
 //along with WifiLapper.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.artsoft.wifilapper;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.artsoft.wifilapper.ComputerFinder.FoundComputer;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ComputerChooserActivity extends ListActivity implements Callback
 {
 	private ComputerFinder m_finder;
 	private Handler m_handler;
 	private Timer m_timer;
 	private static final int MSG_NEWCOMPUTER = 151;
 	
 	@Override
 	public void onCreate(Bundle extras)
 	{
 		super.onCreate(extras);
 		m_handler = new Handler(this);
 		m_finder = new ComputerFinder();
 		
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		m_finder.Init(m_handler, MSG_NEWCOMPUTER);
 		m_finder.StartFindComputers();
 
 		WifiManager pWifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 		WifiInfo pInfo = pWifi.getConnectionInfo();
 		if(pInfo != null && pWifi.isWifiEnabled() && pWifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
 		{
 			Toast.makeText(this, "Searching network '" + pInfo.getSSID() + "' for Pitside", Toast.LENGTH_SHORT).show();
 		}
 		else
 		{
 			Toast.makeText(this, "Not connected to any wifi networks.  Cannot search", Toast.LENGTH_LONG).show();
 			setResult(RESULT_CANCELED);
 			finish();
 		}
 		
 		m_timer = new Timer();
 		m_timer.schedule(new RefreshTask(), 1000);
 	}
 	private class RefreshTask extends TimerTask
 	{
 		@Override
 		public void run() 
 		{
 			m_finder.StartFindComputers();
			m_timer.schedule(new RefreshTask(), 1000);
 		}
 		
 	}
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		m_finder.Shutdown();
 		m_timer.cancel();
 	}
 
 	private void RefreshList()
 	{
 		List<FoundComputer> lstComps = m_finder.GetComputerList();
     	
     	ArrayAdapter<FoundComputer> adapter = new ArrayAdapter<FoundComputer>(this, R.layout.simplelistitem_defaultcolor,lstComps);
     	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     	setListAdapter(adapter);
 	}
 	
 	@Override
 	public boolean handleMessage(Message arg0) 
 	{
 		if(arg0.what == MSG_NEWCOMPUTER)
 		{
 			RefreshList();
 			return true;
 		}
 		return false;
 	}
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) 
 	{
 	  // ignore orientation/keyboard change
 	  super.onConfigurationChanged(newConfig);
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id)
 	{
 		FoundComputer fc = (FoundComputer)l.getItemAtPosition(position);
 		
 		Intent result = new Intent();
 		result.putExtra(Prefs.IT_IP_STRING, fc.GetIP());
 		this.setResult(RESULT_OK, result);
 		finish();
 	}
 }
