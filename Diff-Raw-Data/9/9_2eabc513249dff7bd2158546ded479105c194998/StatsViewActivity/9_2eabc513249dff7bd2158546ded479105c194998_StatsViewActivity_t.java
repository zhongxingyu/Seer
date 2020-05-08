 package com.teatime.game.activity;
 
 import android.os.Bundle;
 import android.widget.TextView;
 
 import com.teatime.game.R;
 import com.teatime.game.model.GathererTech;
 import com.teatime.game.model.HunterTech;
 import com.teatime.game.model.Tribe;
 import com.teatime.game.model.World;
 
 public class StatsViewActivity extends BaseActivity {
 	
 	Tribe tribe;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.layout_stats);
         
         String tribeName = getIntent().getStringExtra("tribeName");
         
         if ( tribeName != null ) {
         	tribe = World.getWorld().getTribe(tribeName);
         } else {
         	tribe = World.getWorld().getTribe("SuperTribe");
         }
         
         setUpStats();
     }
 	
 	private void setUpStats() {
 		TextView title = (TextView) findViewById(R.id.title);
 		title.setText(tribe.getName());
 		
 		TextView population = (TextView) findViewById(R.id.population_data);
 		population.setText(tribe.getPopulation());
 		
 		TextView food = (TextView) findViewById(R.id.food_data);
 		food.setText(tribe.getStoredFood());
 		
 		TextView adults = (TextView) findViewById(R.id.adults_data);
 		adults.setText(tribe.getNumberOfAdults());
 		
 		TextView hunterTech = (TextView) findViewById(R.id.hunter_tech_data);
 		hunterTech.setText(tribe.getTech(new HunterTech()).getLevel());
 		
 		TextView gathererTech = (TextView) findViewById(R.id.gatherer_tech_data);
		gathererTech.setText(tribe.getTech(new GathererTech()).getLevel());
 	}
 }
