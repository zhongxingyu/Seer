 package de.shop.ui.kunde;
 
 import static android.app.ActionBar.NAVIGATION_MODE_TABS;
 import static de.shop.util.Constants.KUNDE_KEY;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import de.shop.R;
 import de.shop.data.Kunde;
 import de.shop.util.TabListener;
 
 public class KundeDetails extends Fragment {
 	private static final String LOG_TAG = KundeDetails.class.getSimpleName();
 	
 	private Kunde kunde;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         kunde = (Kunde) getArguments().get(KUNDE_KEY);
        //Log.d(LOG_TAG, kunde.toString());
         
 		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
 		return inflater.inflate(R.layout.details_tabs, container, false);
 	}
 	
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		final Activity activity = getActivity();
 		final ActionBar actionBar = activity.getActionBar();
 		// (horizontale) Tabs; NAVIGATION_MODE_LIST fuer Dropdown Liste
 		actionBar.setNavigationMode(NAVIGATION_MODE_TABS);
 	    actionBar.setDisplayShowTitleEnabled(false);  // Titel der App ausblenden, um mehr Platz fuer die Tabs zu haben
 
 	    final Bundle args = new Bundle(1);
     	args.putSerializable(KUNDE_KEY, kunde);
     	
 	    Tab tab = actionBar.newTab()
 	                       .setText(getString(R.string.k_stammdaten))
 	                       .setTabListener(new TabListener<KundeStammdaten>(activity,
 	                    		                                            KundeStammdaten.class,
 	                    		                                            args));
 	    actionBar.addTab(tab);
 	    
 	    tab = actionBar.newTab()
                        .setText(getString(R.string.k_bestellungen))
                        .setTabListener(new TabListener<KundeBestellungen>(activity,
                     		                                              KundeBestellungen.class,
                     		                                              args));
 	    actionBar.addTab(tab);
 	}
 	
 	
 }
