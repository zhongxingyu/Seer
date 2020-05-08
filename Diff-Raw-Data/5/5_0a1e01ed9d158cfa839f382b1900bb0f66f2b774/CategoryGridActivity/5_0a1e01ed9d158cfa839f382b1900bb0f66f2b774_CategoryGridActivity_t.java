 package uk.ac.dur.duchess.activity;
 
 import uk.ac.dur.duchess.ImageGridAdapter;
 import uk.ac.dur.duchess.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 
 public class CategoryGridActivity extends Activity
 {
 
 	private GridView gridView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.category_grid_layout);
 
 		gridView = (GridView) findViewById(R.id.categoryGridViewID);
 
 		Integer[] imageIDs = new Integer[] { R.drawable.university, R.drawable.college,
 				R.drawable.music, R.drawable.theatre, R.drawable.exhibitions, R.drawable.sport,
 				R.drawable.conference, R.drawable.community };
 
		String[] categoryLabels = new String[] {"University", "College", "Music", "Theatre", "Exhibitions", "Sport", "Conferences", "Community"};
 		
 		gridView.setAdapter(new ImageGridAdapter(this, imageIDs, categoryLabels));
 
 		gridView.setOnItemClickListener(new OnItemClickListener()
 		{
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
 			{
 				Intent i = new Intent(v.getContext(), EventListActivity.class);
 				i.putExtra("category_filter", categorySelection(position));
 				startActivity(i);
 				finish();
 			}
 		});
 
 	}
 
 	private String categorySelection(int i)
 	{
 		switch (i)
 		{
 		case 0:
 			return "University";
 		case 1:
 			return "College";
 		case 2:
 			return "Music";
 		case 3:
 			return "Theatre";
 		case 4:
 			return "Exhibitions";
 		case 5:
 			return "Sport";
 		case 6:
			return "Conferences";
 		case 7:
 			return "Community";
 		default:
 			return "NULL Category";
 		}
 	}
 }
