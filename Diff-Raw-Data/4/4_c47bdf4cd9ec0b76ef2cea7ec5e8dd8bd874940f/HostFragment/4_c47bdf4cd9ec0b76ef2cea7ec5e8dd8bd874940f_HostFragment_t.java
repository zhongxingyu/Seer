 package com.ppp.wordplayadvlib.fragments.hosts;
 
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.widget.DrawerLayout;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.ppp.wordplayadvlib.R;
 import com.ppp.wordplayadvlib.activities.HostActivity;
 import com.ppp.wordplayadvlib.activities.WordPlayActivity;
 import com.ppp.wordplayadvlib.fragments.BaseFragment;
 import com.ppp.wordplayadvlib.fragments.BaseFragment.HostFragmentInterface;
 
 public class HostFragment extends Fragment
 	implements
 		HostFragmentInterface
 {
 
     private boolean clearOnResume = false;
     private HostActivity host;    
     private boolean suppressPageView = false;
 
 	@Override
     public void onCreate(Bundle savedInstanceState)
     {
 
         super.onCreate(savedInstanceState);
 
         // In case any children have an options menu, turn this on.
         setHasOptionsMenu(true);
 
         host = getHostActivity();
 
         if (savedInstanceState == null)
             replaceStack(getInitialFragment());
 
     }
     
     @Override
     public void onResume()
     {
 
         super.onResume();
         
         if (clearOnResume)  {
             clearOnResume = false;
             replaceStack(getInitialFragment());
         }
 
        setHomeIcon();

     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
         return inflater.inflate(R.layout.base_fragment, null);
     }
 
     protected HostActivity getHostActivity() { return host; }
     public void setHostActivity(final HostActivity host) { this.host = host; }
     
     public static int getFragmentContainer() { return R.id.fragment_content; }
     public BaseFragment getInitialFragment() { return null; }
     public int getFragmentHelp() { return 0; }
     
     public boolean isSuppressPageView() { return suppressPageView; }
 	public void setSuppressPageView(boolean b) { suppressPageView = b; }
 
     public void pushToStack(BaseFragment newFragment)
     {
 
         // Give our fragment access to the tabHost
         newFragment.setHostFragment(this);
 
         FragmentTransaction ft = getChildFragmentManager().beginTransaction();
 
         // Replace whatever is in the fragment_container view with this fragment,
         // and add the transaction to the back stack
         ft.replace(getFragmentContainer(), newFragment, newFragment.getClass().getName());
         ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
         ft.addToBackStack(newFragment.getClass().getName());
 
         // Commit the transaction
         ft.commit();
         
         setHomeIcon();
 
     }
 
     public void replaceStack(BaseFragment newFragment)
     {
 
         // Clear out the back stack
         getChildFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
 
         // Give the fragment this reference
         newFragment.setHostFragment(this);
 
         FragmentTransaction ft = getChildFragmentManager().beginTransaction();
         ft.replace(getFragmentContainer(), newFragment, newFragment.getClass().getName());
         ft.setTransition(FragmentTransaction.TRANSIT_NONE);
         ft.commit();
         
         setHomeIcon();
 
     }
 
     public void popStack()
     {
 
     	if (getChildFragmentManager() != null)
     		getChildFragmentManager().popBackStack();
 
     	setHomeIcon();
 
     }
 
     public void clearStack()
     {
 
         if (getChildFragmentManager() != null)
             getChildFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
 
         setHomeIcon();
 
     }
 
     public void clearStackToFragment(String fragmentName)
     {
 
         if (getChildFragmentManager() != null)
 			if (inBackStack(fragmentName))
 				getChildFragmentManager().popBackStack(fragmentName, 0);
 			else
 				clearStack();
         
         setHomeIcon();
 
     }
 	
 	public boolean inBackStack(String fragmentName) 
 	{
 		if (getChildFragmentManager() != null)
 			for (int i=0; i<getChildFragmentManager().getBackStackEntryCount(); i++)
 				if (getChildFragmentManager().getBackStackEntryAt(i).getName().equals(fragmentName))
 					return true;
 		return false;
 	}
 
     public void setClearOnResume() { clearOnResume = true; }
 
     private boolean isBaseLevel()
     {
     	return getChildFragmentManager().getBackStackEntryCount() > 0 ? false : true;
     }
 
     private void setHomeIcon()
     {
 
     	if (host == null)
     		return;
     	
     	if (host instanceof WordPlayActivity)  {
     		
     		final ActionBarDrawerToggle toggle = (((WordPlayActivity) host)).getDrawerToggle();
     		DrawerLayout drawer = (((WordPlayActivity) host)).getDrawerLayout();
     		
     		if ((toggle != null) && (drawer != null))  {
     			drawer.postDelayed(new Runnable() {
     	            @Override
     	            public void run()
     	            {
     	            	if (host != null)  {
     	                	if (isBaseLevel()) 
     	                		toggle.setDrawerIndicatorEnabled(true);
     	                	else 
     	                		toggle.setDrawerIndicatorEnabled(false);
     	                }
     	            }
     	        }, 500);
     		}
 
     	}
 
     }
 
 }
