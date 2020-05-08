 package dk.vinael.fog;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class MainActivity extends Activity implements OnClickListener {
 
 	ArrayList<String> items = new ArrayList<String>();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		items.add("Nizzle Party1");
 		items.add("Nizzle Party2");
 		items.add("Nizzle Party3");
 		items.add("Nizzle Party1");
 		items.add("Nizzle Party2");
 		items.add("Nizzle Party3");
 		items.add("Nizzle Party1");
 		items.add("Nizzle Party2");
 		items.add("Nizzle Party3");
 		items.add("Nizzle Party1");
 		items.add("Nizzle Party2");
 		items.add("Nizzle Party3");
 		
 		ListView lv = (ListView)	findViewById(R.id.listView1);
 		ArrayAdapter<String> myarrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
 		lv.setAdapter(myarrayAdapter);
 		lv.setTextFilterEnabled(true);
 		findViewById(R.id.btn_private_party).setOnClickListener(this);
		findViewById(R.id.btn_profil_settings).setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	public static void changeFragment(Class<? extends Fragment> instanstiate, FragmentManager activeFM) {
 		
 		try {
 			FragmentManager fm = activeFM;
 			FragmentTransaction transaction;
 			transaction = fm.beginTransaction();
 			Fragment lastFrag = instanstiate.newInstance();
 			transaction.replace(R.id.frameLayout2, lastFrag);
 			transaction.commit();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 	}
 	@Override
 	public void onClick(View v) {
 		setContentView(R.layout.activity_framelayout);
		if (v.getId() == R.id.btn_private_party) {
			MainActivity.changeFragment(partyFragment.class, getFragmentManager());
		} else if (v.getId() == R.id.btn_profil_settings) {
			MainActivity.changeFragment(settingsFragment.class, getFragmentManager());
		}
 	}
 
 }
