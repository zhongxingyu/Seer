 package mit.edu.stemplusplus;
 
 import java.util.ArrayList;
 
 import mit.edu.stemplusplus.helper.AlertDialogManager;
 import mit.edu.stemplusplus.helper.MyHorizontalLayout;
 import mit.edu.stemplusplus.helper.ParseDatabase;
 import mit.edu.stemplusplus.helper.Project;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.parse.ParseException;
 
 /**
  * This class display all of the projects currently active
  * 
  * @author trannguyen
  * 
  */
 
 public class AllProjectDisplayActivity extends StemPlusPlus {
 	/** The array to hold all of the captured image */
 	ArrayList<String> myPicture = new ArrayList<String>();
 
 	ArrayList<Project> projectDisplay = new ArrayList<Project>();
 
 	/** adapter to handle data for the image */
 	ImageAdapter adapter;
 
 	GridView imagegrid;
 	
 	Activity act = this;
 	
 	private static final String TITLE = "Project Title";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_all_project_display);
 		ParseDatabase.initProject(this);
 		
 		// call the horizontal layout
 		MyHorizontalLayout picture = (MyHorizontalLayout) findViewById(R.id.myphoto_all_project_display);
 		picture.setArrayList(myPicture);
 		// get the information from the
 		Intent intent = getIntent();
 		if (intent.getStringArrayListExtra(IMAGE_INTENT) != null) {
 			ArrayList<String> imagePath = intent
 					.getStringArrayListExtra(IMAGE_INTENT);
 			for (String i : imagePath) {
 				picture.add(i);
 			}
 		}
 
 		imagegrid = (GridView) findViewById(R.id.gridview_all_project_display);
 		/*********
 		 * Make test project here
 		 */
 		
 		GetData data = new GetData();
 		data.execute();
 		
 
 		Button addProjectButton = (Button) findViewById(R.id.add_project_all_project_display);
 		addProjectButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(v.getContext(), ProjectActivity.class);
 				startActivity(i);
 			}
 		});
 
 		Log.d("create", "ok");
 	}
 
 	/**
 	 * The adapter to handle to underlying array of pictures
 	 * 
 	 * @author trannguyen
 	 * 
 	 */
 	public class ImageAdapter extends BaseAdapter {
 		private LayoutInflater mInflater;
 
 		private ArrayList<Project> projects;
 
 		public ImageAdapter(ArrayList<Project> project) {
 			this.projects = project;
 			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		}
 
 		public ImageAdapter() {
 			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		@Override
 		public int getCount() {
 			return projects.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return position;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			final ViewHolder holder;
 			if (convertView == null) {
 				holder = new ViewHolder();
 				convertView = mInflater.inflate(R.layout.project_display, null);
 				holder.projectDescription = (TextView) convertView
 						.findViewById(R.id.project_description_all_project_display);
 				Log.d("get TextView", "ok");
 				holder.projectImage = (ImageView) convertView
 						.findViewById(R.id.project_avatar_all_project_display);
 				convertView.setTag(holder);
 			}
 			// picture is already there
 			else {
 				holder = (ViewHolder) convertView.getTag();
 
 			}
 
 			final Project project = projects.get(position);
 			Log.d("get project", "ok");
 			holder.projectDescription.setText(TITLE + " " + project.getName());
 			Log.d("set Text", "ok");
 			
 			if (project.getProfileImagePath() != null) {
 
 				holder.projectImage.setImageBitmap(BitmapFactory
 						.decodeFile(project.getProfileImagePath()));
				holder.projectImage.setMaxHeight(100);
 			} else
 				holder.projectImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.test_image));
 			Log.d("set Image", "ok");
 			holder.projectImage.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					Intent i = new Intent(v.getContext(),
 							ProjectDescription.class);
 					i.putExtra(StemPlusPlus.PROJECT_INTENT, project);
 					
 					startActivity(i);
 
 				}
 			});
 
 			holder.setId(position);
 
 			return convertView;
 
 		}
 
 	}
 
 	/**
 	 * The view holder to store the check box and imageView
 	 * 
 	 * @author trannguyen
 	 * 
 	 */
 	private class ViewHolder {
 		private TextView projectDescription;
 		private ImageView projectImage;
 		private int id;
 
 		public void setId(int id) {
 			this.id = id;
 		}
 	}
 	
 	
 	private class GetData extends AsyncTask<Void, Void, ArrayList<Project>> {
 		ProgressDialog progressDialog;
 		/**
 		 * Some constant default query to call on the server side
 		 */
 
 		/**
 		 * Place holder constructor to call excute method later
 		 */
 		public GetData() {
 
 		}
 
 		@Override
 		protected void onPreExecute() {
 			progressDialog = new ProgressDialog(act);
 			progressDialog.setMessage("Downloading data ...");
 			progressDialog.show();
 			Log.d("pD", "progess Dialog created");
 		}
 
 		@Override
 		protected ArrayList<Project> doInBackground(Void... params) {
 
 
 			Log.d("dataBase", "Creat data base");
 
 			ArrayList<Project> project = null;
 			try {
 				project = ParseDatabase.getListOfAllProjects();
 			} catch (ParseException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 				
 			return project;
 
 		}
 
 		@Override
 		protected void onPostExecute(ArrayList<Project> result) {
 			if (result.size() > 0) {
 				/** End Test */
 				projectDisplay = result;
 				adapter = new ImageAdapter(projectDisplay);
 				imagegrid.setAdapter(adapter);
 				adapter.notifyDataSetChanged();
 				progressDialog.cancel();
 			} else {
 				progressDialog.cancel();
 
 				new AlertDialogManager().showAlertDialog(act, "Not Found",
 						"No Item matches your query", false);
 			}
 
 		}
 
 
 
 	}
 
 /*	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.layout.activity_all_project_display, menu);
 		return true;
 	}*/
 
 }
