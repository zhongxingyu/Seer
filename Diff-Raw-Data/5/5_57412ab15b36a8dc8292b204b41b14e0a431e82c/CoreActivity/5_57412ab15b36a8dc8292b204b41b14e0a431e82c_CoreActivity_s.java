 package com.lastcrusade.fanclub;
 
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 
 import com.actionbarsherlock.view.MenuItem;
 import com.lastcrusade.fanclub.util.Titleable;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 
 public class CoreActivity extends SlidingFragmentActivity{
     private Fragment mainContent;
     
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
 
         if (savedInstanceState != null){
             mainContent = getSupportFragmentManager().getFragment(savedInstanceState, "mainContent");
         }
         if (mainContent == null){
             mainContent = new PlaylistFragment(); 
         }
         
         setContentView(R.layout.content_frame);
         switchContent(mainContent);
         
         setBehindContentView(R.layout.menu_frame);
         
         MenuFragment menuFragment = new MenuFragment();
         switchFragment(menuFragment, false);
         
         //setup the sliding bar
         getSlidingMenu().setBehindOffset(60);
         setSlidingActionBarEnabled(false);
         
         //enables the icon to act as the up 
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     }
     
     
     public boolean onOptionsItemSelected(MenuItem item) {
         //references the app icon
        if(item.getItemId()==android.R.id.home){
            toggle(); //toggles the state of the sliding menu
            setTitle(getString(R.string.app_name)); 
            return true;
    
        }
        
        return false;
     }
     
     
     public void switchContent(Fragment content){
         //switch out the content fragments
         switchFragment(content, true);
         
         //close the sliding menu and show the full content fragment
         getSlidingMenu().showContent();
         
         //update the activity title
        if(content instanceof Titleable){
             setTitle(((Titleable)content).getTitle());
        }
     }
     
     /*
      * Switches out the current fragment with the fragment
      * that is passed in. If content is true, the fragment is replaced
      * as the main content - if it is false, the fragment is replaced as the menu
      */
     private void switchFragment(Fragment newFragment, boolean content){
         if(content){
             mainContent = newFragment;
             getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.content_frame, mainContent)
                 .commit(); 
         }
         else{
             getSupportFragmentManager()
             .beginTransaction()
             .replace(R.id.menu_frame, newFragment)
             .commit(); 
         }
         
     }
 }
