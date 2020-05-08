 /*
  * Copyright 2011 Colin Paton - cozzarp@googlemail.com
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus.ui;
 
 import org.redbus.R;
 import org.redbus.ui.arrivaltime.NearbyBookmarkedArrivalTimeActivity;
 import org.redbus.ui.stopmap.StopMapActivity;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.widget.TabHost;
 
 public class RedbusTabView extends TabActivity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.redbustablayout);
 
 	    TabHost tabHost = getTabHost();
 	    
 	    tabHost.addTab(tabHost.newTabSpec("bookmarks").setIndicator("Bookmarks")
             .setContent(new Intent().setClass(this, BookmarksActivity.class)));
 
 	    tabHost.addTab(tabHost.newTabSpec("map").setIndicator("Map")
 	                  .setContent(new Intent().setClass(this, StopMapActivity.class)));
 	    
//	    tabHost.addTab(tabHost.newTabSpec("nearby").setIndicator("Nearby")
//                .setContent(new Intent().setClass(this, NearbyBookmarkedArrivalTimeActivity.class)));
 	}
 	
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 		// make the back button switch to bookmarks tab unless we're already on bookmarks tab
 	    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 	    	if (getTabHost().getCurrentTab() != 0) {
 	    		getTabHost().setCurrentTab(0);
 		        return true;	    		
 	    	}
 	    }
 		
 		return super.dispatchKeyEvent(event);
 	}
 }
