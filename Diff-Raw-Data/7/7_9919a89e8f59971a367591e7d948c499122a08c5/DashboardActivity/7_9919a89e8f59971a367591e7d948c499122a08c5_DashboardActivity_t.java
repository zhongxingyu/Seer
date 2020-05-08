 package phillykeyspots.frpapp;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 
 public class DashboardActivity extends FragmentActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_dashboard);
 		Fragment fragment;
 		switch(Integer.parseInt(getIntent().getExtras().get("ID").toString())){
 		case R.id.b_finder:
 			fragment = new FinderFragment();
 			break;
		case R.id.b_events:
			fragment = new EventsFragment();
 			break;
 		case R.id.b_resources:
			fragment = new ResourcesFragment();
			break;
 		case R.id.b_joml:
 			fragment = new JomlFragment();
 			break;
 		default:
 			fragment = new FinderFragment();
 		}
 		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();	
 	}
 
 }
