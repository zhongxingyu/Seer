 package com.iut.ptut.view;
 
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.iut.ptut.R;
 import com.iut.ptut.ctrl.cron.CRONFetcher;
 import com.iut.ptut.model.ConfigManager;
 
 /**
  * 
  * @author Rmy Bienvenu, Hugo Roman, Benot Sauvre
  *
  */
 public class MainActivity extends Activity {
 
 	// definition de l'id correspondant  la notification
 	public static final int ID_NOTIFICATION = 1337;
 	
 	public static Context context = null;
 	public static MainActivity activity = null;
 	
 	private ActionBar.Tab tabToday;
 	private ActionBar.Tab tabSemaine;
 	private ActionBar.Tab tabMessage;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Locale.setDefault(Locale.FRANCE);
 
 		// On rcupre le conexte pour l'utliser ailleurs
 		MainActivity.context = this.getApplicationContext();
 		MainActivity.activity = this;
 		
		ConfigManager.getInstance().setProperty("user_semestre", "4");

		ConfigManager.getInstance().setProperty("user_group", "2A");
		
 		setContentView(R.layout.activity_main);
 		
 		ActionBar actionbar = getActionBar();
 		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// definition des tabs et de leurs texte
 		tabToday = actionbar.newTab().setText(R.string.tab_today);
 		tabSemaine = actionbar.newTab().setText(R.string.tab_week);
 		tabMessage = actionbar.newTab().setText(R.string.tab_message);
 
 		tabToday.setTabListener(new TabListener<TodayFragment>(this, "today", TodayFragment.class));
 		tabSemaine.setTabListener(new TabListener<WeekFragment>(this, "week", WeekFragment.class));
 		tabMessage.setTabListener(new TabListener<MessagesFragment>(this, "messages", MessagesFragment.class));
 
 		actionbar.addTab(tabToday);
 		actionbar.addTab(tabSemaine);
 		actionbar.addTab(tabMessage);
 
 		getActionBar().setDisplayShowTitleEnabled(false);
 		getActionBar().setDisplayShowHomeEnabled(false);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu_contextuel, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i = null;
 		
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.menu_refresh:
 	            launchRefresh();
 	            return true;
 	        case R.id.menu_parametres:
 	        	// On lance l'activit des paramtres
 	        	i = new Intent(this.getApplicationContext(), SettingsActivity.class);
 	        	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 	        	MainActivity.context.startActivity(i);
 	        	return true;
 	        case R.id.menu_about:
 	        	FragmentTransaction ft = getFragmentManager().beginTransaction(); 
 				Fragment mFragment = Fragment.instantiate(MainActivity.context, AboutFragment.class.getName(), null);
 				ft.replace(R.id.fragment_contenu, mFragment);
 				ft.commit();
 	        	return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	
 	/**
 	 * Lance le rafraichissement de la base dans une tche parallle.
 	 */
 	public void launchRefresh() {
 		
 		Toast toastConfirmation = Toast.makeText(MainActivity.context, "Rcupration en cours...", Toast.LENGTH_LONG);
 		toastConfirmation.show();
 		
 		CRONFetcher fetcher = new CRONFetcher();
 		fetcher.execute(new String[]{});
 		
 	}
 	
 	/**
 	 * Permet de rafraichir le tab actuellement affich.
 	 */
 	public void rafraichirFragment() {
 		
 		// On reslectionne le tab courrant pour le rafraichir
 		this.getActionBar().selectTab(this.getActionBar().getSelectedTab());
 	}
 	
 	/**
 	 * Force l'affichage de la semaine. (utili pour un retour  partir d'un jour ouvert dans l'onglet semaine)
 	 */
 	public void forceAffichageOngletWeek() {
 		this.getActionBar().selectTab(this.tabSemaine);
 	}
 }
