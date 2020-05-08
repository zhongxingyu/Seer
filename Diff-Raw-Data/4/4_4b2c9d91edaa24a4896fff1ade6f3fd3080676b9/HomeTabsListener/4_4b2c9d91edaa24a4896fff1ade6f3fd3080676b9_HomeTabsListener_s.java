 package edu.uiuc.whosinline.listeners;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 
 public class HomeTabsListener implements ActionBar.TabListener {
 
 	final private FragmentManager fragmentManager;
 	final private Fragment fragment;
 	final private int fragmentContainerId;
 	
 	public HomeTabsListener(FragmentManager fragmentManager, Fragment fragment,
 			int fragmentContainerId){
 		
 		this.fragmentManager = fragmentManager;
 		this.fragment = fragment;
 		this.fragmentContainerId = fragmentContainerId;
 	}
 
 	@Override
 	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
 		// Do nothing.
 	}
 
 	@Override
 	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
 		// Create new transaction.
 		FragmentTransaction transaction = fragmentManager.beginTransaction();
 		
 		// Replace whatever is in the fragment_container view with this
		// fragment, and add the transaction to the back stack.
 		transaction.replace(fragmentContainerId, fragment);
		transaction.addToBackStack(null);
 		
 		// Commit the transaction.
 		transaction.commit();
 	}
 
 	@Override
 	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
 		// Do nothing.
 	}
 }
