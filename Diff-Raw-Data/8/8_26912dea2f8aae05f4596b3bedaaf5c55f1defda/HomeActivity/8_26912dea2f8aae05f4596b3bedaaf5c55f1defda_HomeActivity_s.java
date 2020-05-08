 package edu.nkuresearch.securitychecker;
 
 import android.os.Bundle;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 
 import edu.nkuresearch.securitychecker.fragments.AppListFrag;
 import edu.nkuresearch.securitychecker.fragments.InstallObserverFrag;
 import edu.nkuresearch.securitychecker.fragments.InstallReviewFrag;
 
 public class HomeActivity extends BaseActivity{
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setTitle("Chooser");
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		Tab tab = actionBar.newTab().setText("Apps").setTabListener(new HomeTabListener<AppListFrag>(this, "Apps", AppListFrag.class));
 		actionBar.addTab(tab);
 		tab = actionBar.newTab().setText("Permissions").setTabListener(new HomeTabListener<PermSearchFrag>(this, "Perms", PermSearchFrag.class));
 		actionBar.addTab(tab);
 		tab = actionBar.newTab().setText("Observer").setTabListener(new HomeTabListener<InstallObserverFrag>(this, "Observer", InstallObserverFrag.class));
 		actionBar.addTab(tab);
 		
 		Tab straceTab = actionBar.newTab().setText("Result").setTabListener(new HomeTabListener<InstallReviewFrag>(this, "Result", InstallReviewFrag.class));
 		actionBar.addTab(tab);
 		
 		if(getIntent().getStringExtra("STRACE") != null)
 			actionBar.selectTab(straceTab);
 	}
 }
