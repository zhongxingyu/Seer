 package com.ogameproxy;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 
 import com.ogamelib.LeveledElement;
 import com.ogamelib.Planet;
 import com.ogamelib.User;
 import com.ogameproxy.view.LeveledElementView;
 import com.ogameproxy.view.ListLayout;
 
 public class MainActivity extends Activity {
 	/*
 	 * Ogame API:
 	 * http://board.origin.ogame.de/board6-origin/board38-tools-scripts
 	 * -skins/3927-ogame-api/
 	 */
 
 	private User user = null;
 	private LinearLayout mainLayout;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		user = createUser();
 		mainLayout = (LinearLayout) LinearLayout.inflate(this, R.layout.main,
 				null);
 
 		updatePlanets();
 		updateDomains();
 		updateContentView();
 
 		setContentView(mainLayout);
 	}
 
 	private void updateDomains() {
 		/*
 		 * TODO: display the lists of buildings
 		 */
 		final Spinner domainSpinner = (Spinner) mainLayout
 				.findViewById(R.id.domainSpinner);
 		ArrayAdapter<View> domainAdapter = new ArrayAdapter<View>(this,
 				android.R.layout.simple_spinner_item);
 		final ListLayout resourcesLayout = new ListLayout(this, getResources()
 				.getString(R.string.resources));
 		ArrayAdapter<LeveledElementView> adapter = new ArrayAdapter<LeveledElementView>(
 				this, android.R.layout.simple_spinner_item);
 		for (LeveledElement element : getCurrentPlanet().getBuildings()) {
 			adapter.add(new LeveledElementView(this, element));
 			Log.i("domains", element.toString());
 		}
 		domainAdapter.add(resourcesLayout);
 		domainSpinner.setAdapter(domainAdapter);
 		domainSpinner.setSelection(0);
 	}
 
 	private void updatePlanets() {
 		final Spinner planetSpinner = (Spinner) mainLayout
 				.findViewById(R.id.planetSpinner);
 		planetSpinner.setAdapter(new ArrayAdapter<Planet>(this,
 				android.R.layout.simple_spinner_item, user.getPlanets()));
 	}
 
 	private Planet getCurrentPlanet() {
 		final Spinner planetSpinner = (Spinner) mainLayout
 				.findViewById(R.id.planetSpinner);
 		return (Planet) planetSpinner.getSelectedItem();
 	}
 
 	private void updateContentView() {
 		final Spinner domainSpinner = (Spinner) mainLayout
 				.findViewById(R.id.domainSpinner);
 		final ScrollView contentView = (ScrollView) mainLayout
 				.findViewById(R.id.contentView);
 		contentView.addView((View) domainSpinner.getSelectedItem());
 	}
 
 	private User createUser() {
 		User user = new User();
 
 		user.setName("test-user");
 
 		{
			Planet planet = new Planet();
 			planet.getMetal().setAmount(10L);
 			planet.getCrystal().setAmount(20L);
 			planet.getDeuterium().setAmount(30L);
 			planet.getEnergy().setAmount(10L);
 
 			planet.getBuildings().metalMine.setLevel(4);
 			planet.getBuildings().crystalMine.setLevel(4);
 			planet.getBuildings().deuteriumMine.setLevel(2);
 			planet.getBuildings().deuteriumMine.setMaximumProductionRate(0.6);
 			planet.getBuildings().solarCentral.setLevel(4);
 
 			planet.setName("planète 1");
 
 			user.acquirePlanet(planet);
 		}
 
 		{
			Planet planet = new Planet();
 			planet.getMetal().setAmount(100L);
 			planet.getCrystal().setAmount(200L);
 			planet.getDeuterium().setAmount(300L);
 			planet.getEnergy().setAmount(100L);
 
 			planet.getBuildings().metalMine.setLevel(8);
 			planet.getBuildings().crystalMine.setLevel(7);
 			planet.getBuildings().deuteriumMine.setLevel(6);
 			planet.getBuildings().solarCentral.setLevel(10);
 
 			planet.setName("planète 2");
 
 			user.acquirePlanet(planet);
 		}
 		return user;
 	}
 }
