 package edu.berkeley.cs160.smartnature;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 
 public class StartScreen extends ListActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
 	
 	static GardenAdapter adapter;
 	static ArrayList<Garden> gardens;
 	AlertDialog dialog;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		initMockData();
 		
 		getListView().setOnItemClickListener(this);
 		((Button) findViewById(R.id.new_garden)).setOnClickListener(this);
 	}
 	
 	public void initMockData() {
 		gardens = new ArrayList<Garden>();
 		Garden g1 = new Garden(R.drawable.preview1, "Berkeley Youth Alternatives");
 		Garden g2 = new Garden(R.drawable.preview2, "Karl Linn");
 		//Garden g3 = new Garden(R.drawable.preview3, "Peralta");
 		
 		Rect bounds1 = new Rect(40, 60, 90, 200);
 		Rect bounds2 = new Rect(140, 120, 210, 190);
 		Rect bounds3 = new Rect(270, 120, 270 + 90, 120 + 100);
 		float[] pts = { 0, 0, 50, 10, 90, 100 };
 		g1.addPlot("Jerry's Plot", bounds1, 10, Plot.RECT);
 		g1.addPlot("Amy's Plot", bounds2, 0, Plot.OVAL);
 		g1.addPlot("Shared Plot", bounds3, 0, pts);
 		
 		Rect bounds4 = new Rect(40, 200, 90, 300);
 		Rect bounds5 = new Rect(140, 50, 210, 190);
 		Rect bounds6 = new Rect(270, 120, 270 + 90, 120 + 140);
 		float[] pts2 = { 0, 0, 50, 10, 90, 100, 70, 140, 60, 120 };
 		g2.addPlot("Cyndi's Plot", bounds4, 0, Plot.RECT);
 		g2.addPlot("Alex's Plot", bounds5, 10, Plot.OVAL);
 		g2.addPlot("Flowers", bounds6, 0, pts2);
 		
 		gardens.add(g1);
 		gardens.add(g2);
 		adapter = new GardenAdapter(this, R.layout.list_item, gardens);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onClick(View view) {
 		startActivity(new Intent(this, GardenScreen.class));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Intent intent = new Intent(this, GardenScreen.class);
 		Bundle bundle = new Bundle();
 		bundle.putInt("id", position);
 		//bundle.putString("name", ((TextView)view.findViewById(R.id.garden_name)).getText().toString());
 		intent.putExtras(bundle);
 		startActivity(intent);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.m_contact:
 				Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
 				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 				intent.setData(Uri.parse("mailto:" + getString(R.string.dev_email)));
 				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GardenGnome feedback");
 				startActivity(intent); //startActivity(Intent.createChooser(intent, "Send mail..."));
 				break;
 			case R.id.m_global_options:
 				startActivity(new Intent(this, GlobalSettings.class));
 				break;
 			case R.id.m_help:
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	class GardenAdapter extends ArrayAdapter<Garden> {
 		private ArrayList<Garden> items;
 		private LayoutInflater li;
 		
 		public GardenAdapter(Context context, int textViewResourceId, ArrayList<Garden> items) {
 			super(context, textViewResourceId, items);
 			li = ((ListActivity) context).getLayoutInflater();
 			this.items = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null)
 				v = li.inflate(R.layout.list_item, null);
 			Garden g = items.get(position);
 			((TextView) v.findViewById(R.id.garden_name)).setText(g.getName());
 			((ImageView) v.findViewById(R.id.preview_img)).setImageResource(g.getPreviewId());
 			
 			return v;
 		}
 	}
 }
