 package eu.isweb.animeplayer;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 
 public class MainActivity extends FragmentActivity {
 
 	AnimeDatabaseManager db;
     SectionsPagerAdapter mSectionsPagerAdapter;
     ViewPager mViewPager;
     Context instance;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.activity_main);
         
         instance = this;
         db = ((AnimeApp) getApplication()).getDB();
         
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
         mViewPager.setCurrentItem(1);
         
         new FlashPlayerManager(this).install();
         
 //        Intent intent = new Intent(this,VideoActivity.class);
 //        intent.putExtra("anime", new Anime("tet", "tet"));
 //        intent.putExtra("epizode", new Epizode("tste", "test"));
 //        intent.putExtra("video", new Video("test", "", Video.TYPE_ANIME_SHIDEN));
//		startActivity(intent);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int i) {
         	switch (i) {
         		case 0: return new AnimeHistoryFragment();
         		case 1: return new AnimeListFragment();
         		case 2: return new AnimeFavoritesFragment();
         	}
             return null;
         }
 
         @Override
         public int getCount() {
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
                 case 0: return getString(R.string.history);
                 case 1: return getString(R.string.all);
                 case 2: return getString(R.string.favorites);
             }
             return null;
         }
     }
     
     private void about() {
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setMessage("All good stuff:\n- anime-shinden.info\n\nPizza lover:\n- JD\n\nBeta testing:\n- Madrim\n- Johniak")
     		   .setTitle(getString(R.string.about))
     	       .setNeutralButton(getString(R.string.about_back), new DialogInterface.OnClickListener() {
     	           @Override
 				public void onClick(DialogInterface dialog, int id) {
     	                dialog.cancel();
     	           }
     	       });
     	builder.create().show();
     }
     
     private String getFragmentTag(int pos){
         return "android:switcher:"+R.id.pager+":"+pos;
     }
     
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_about:
 			about();
 			return true;
 			
 		case R.id.menu_refresh:
 			AnimeFavoritesFragment.refreshFavorites();
 			AnimeHistoryFragment.refreshHistory();
 			AnimeListFragment f = (AnimeListFragment) MainActivity.this.getSupportFragmentManager().findFragmentByTag(getFragmentTag(1));
 			f.refresh();
 			return true;
 
 		case R.id.menu_clearHistory:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(getString(R.string.clear_history))
 					.setTitle(getString(R.string.menu_clearHistory))
 			       .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
 			           @Override
 					public void onClick(DialogInterface dialog, int id) {
 			        	   db.clearHistory();
 			   				AnimeHistoryFragment.refreshHistory();
 			           }
 			       })
 			       .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
 			           @Override
 					public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			           }
 			       });
 			AlertDialog alert = builder.create();
 			alert.show();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
