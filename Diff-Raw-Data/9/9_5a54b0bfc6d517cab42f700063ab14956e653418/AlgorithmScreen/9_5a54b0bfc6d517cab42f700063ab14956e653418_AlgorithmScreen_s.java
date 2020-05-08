 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.AlgorithmInfo;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.ServerInfo;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 public class AlgorithmScreen extends FragmentActivity {
 	private Session session;
 	private boolean started;
 
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putSerializable(Session.IDENTIFIER, session);
 		super.onSaveInstanceState(outState);
 	}
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.algorithmscreen);
 
 		// If we get created for the first time we get our data from the intent
 		Bundle data = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
 		session = (Session) data.getSerializable(Session.IDENTIFIER);
 
		started = false;

 		setupListView();
 		setupBillingButton();
 	}
 
 	private void setupListView() {
 		ListView listView = (ListView) findViewById(R.id.listViewAlgorithm);
 		ArrayList<AlgorithmInfo> algorithms = new ArrayList<AlgorithmInfo>();
 		for (AlgorithmInfo alg : session.getServerInfo().getAlgorithms()) {
 			if (!alg.isHidden())
 				algorithms.add(alg);
 		}
 
 		Collections.sort(algorithms);
 
 		ArrayAdapter<AlgorithmInfo> adapter = new ArrayAdapter<AlgorithmInfo>(this,
 				android.R.layout.simple_list_item_1, algorithms);
 		listView.setAdapter(adapter);
 
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
 				if (started)
 					return;
 				started = true;
 				AlgorithmInfo alg = (AlgorithmInfo) adapterView.getItemAtPosition(i);
 				session.setSelectedAlgorithm(alg);
 				Intent myIntent = new Intent(view.getContext(), MapScreen.class);
 				myIntent.putExtra(Session.IDENTIFIER, session);
 				startActivity(myIntent);
 			}
 		});
 	}
 
 	private void setupBillingButton() {
 		Button btnBilling = (Button) findViewById(R.id.btnBilling);
 		if (session.getServerInfo().getServerType() == ServerInfo.ServerType.PUBLIC) {
 			btnBilling.setVisibility(View.GONE);
 		} else {
 			btnBilling.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					// generates an intent from the class BillingScreen
 					Intent myIntent = new Intent(view.getContext(), BillingScreen.class);
 					myIntent.putExtra(Session.IDENTIFIER, session);
 					startActivity(myIntent);
 				}
 			});
 		}
 	}
 
 
 }
