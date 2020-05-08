 package com.teamblobby.studybeacon;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.teamblobby.studybeacon.datastructures.*;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class SBCourseResourceActivity extends ListActivity {
 
 	private static final String TAG = "SBCourseResourceListActivity";
 	
 	private List<CourseListable> availableCourses;
 	private CourseInfo[] currentCourses;
 	private ArrayAdapter<CourseListable> arrayAdapter;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	
 	    this.setContentView(R.layout.mycourses);
 	    // set the title text
 	    ((TextView) this.findViewById(R.id.coursesTitleText))
 	    	.setText(Global.res.getString(R.string.courseResourceTitleText));
 	    
 	    currentCourses = Global.getMyCourseInfos();
 	    
 	    // This is where we load the courses, otherwise get from savedBundle
 	    if (savedInstanceState != null)
 	    	this.availableCourses =
 	    	    savedInstanceState.getParcelableArrayList("availableCourses");
 	    
 	    if (this.availableCourses == null) {
 	    	// create empty list and call to load courses
 	    	this.availableCourses = new ArrayList<CourseListable>(); // this is something else
 	    	String categoryName = null;
 	    	if ( this.getIntent().getExtras() != null )
 	    		categoryName = this.getIntent().getExtras().getString("category");
 	    	if ( categoryName == null ) categoryName = CourseLoadTask.ROOT_CATEGORY;
 	    	(new CourseLoadTask(this)).execute(categoryName); // executes AsyncTask
 	    }
 	    
 		this.arrayAdapter = new CourseListAdapter(this, 
 										 R.layout.mycoursesrow, 
 										 R.id.mcrCourseNameTextView, 
 										 this.availableCourses);
 		this.setListAdapter(this.arrayAdapter);
 		
 		this.getListView().setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> listView, View itemView, int position,
 					long rowId) {
 				// TODO Auto-generated method stub
 				CourseListable courseListItem = (CourseListable) listView.getAdapter().getItem(position);
 				
 				if ( courseListItem.listableType() == CourseListable.TYPE_CATEGORY ) { 
 					Intent i = new Intent(SBCourseResourceActivity.this,
 							              SBCourseResourceActivity.class);
 					i.putExtra("category", courseListItem.getName());
 					startActivity(i);
 				}
 			}
 		
 		});
 	    
 	}
 	
 	private void setStarredCourses() {
 		
 		for (CourseListable course : this.availableCourses){
 				course.setStarred(Arrays.asList(CourseInfo.getCourseNames(
 									this.currentCourses)).contains(course.getName()));
 		}
 		
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putParcelableArrayList("availableCourses",
 				       (ArrayList<CourseListable>) this.availableCourses);
 		super.onSaveInstanceState(outState);
 	}
 	
 	private class CourseListAdapter extends ArrayAdapter<CourseListable> {
 
 		public CourseListAdapter(Context context, int resource,
 				int textViewResourceId, List<CourseListable> objects) {
 			super(context, resource, textViewResourceId, objects);
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View viewToReturn =  super.getView(position, convertView, parent);
 			
 			// remove notify and new beacon buttons
 			viewToReturn.findViewById(R.id.mcrNotificationButton).setVisibility(View.GONE);
 			viewToReturn.findViewById(R.id.mcrNewBeaconButton).setVisibility(View.GONE);
 			
 			// set starred based on what's in the CourseInfo object
 			final CourseListable courseInfo = this.getItem(position);
 			
 			CheckBox notifyCheckBox = (CheckBox) viewToReturn.findViewById(R.id.mcrStarButton);
 			
 			notifyCheckBox.setVisibility(View.VISIBLE);
 			// here split depending on if it's a course or course category			
 			switch (courseInfo.listableType()) {
 				case CourseListable.TYPE_COURSE_STARRED:
 				case CourseListable.TYPE_COURSE_UNSTARRED:
 					notifyCheckBox.setChecked(courseInfo.getStarred());
 					// TODO add onCheck listener to checkbox
 					break;
 				case CourseListable.TYPE_CATEGORY:
 					notifyCheckBox.setVisibility(View.INVISIBLE);
 					break;
 			}
 			
 			Log.d(TAG,"getting course View for "+courseInfo.getName());
 			return viewToReturn;
 		}
 	}
     
     private class CourseLoadTask extends AsyncTask<String, Void, List<CourseListable>> {
     	
 		private static final String ROOT_CATEGORY = "_root";
 		private SBCourseResourceActivity callingActivity;
     	
     	public CourseLoadTask(SBCourseResourceActivity activity) {
     		this.callingActivity = activity;
     	}
     	
     	@Override
     	protected void onPreExecute() {
     		Toast.makeText(this.callingActivity,
     				Global.res.getString(R.string.loadingcoursestoast),
     				Toast.LENGTH_SHORT).show();
     	}
     	
 		@Override
 		protected List<CourseListable> doInBackground(String... category) {
 			Log.v(TAG, "Loading Course Resources");
 			final String[] pulledCourseList;
 			// TODO load the resources from MIT somehow
 			try {
 				Thread.sleep(3000); //simulate load time
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			List<CourseListable> courses = new ArrayList<CourseListable>();
 			if (category[0].equals(ROOT_CATEGORY)) {
 				pulledCourseList = new String[] {"1","2","3","4","5","6","7","8"};
 				for (String course : pulledCourseList) {
 					courses.add(new CourseCategory(course));
 					}
 			} else {
 				pulledCourseList = new String[] {"1.334","2.217","6.570","8.101","8.901"};
 				for (String course : pulledCourseList) {
 					courses.add(new CourseInfoSimple(course));
 				}
 			}
 			
 			Log.v(TAG,"load finished");
 			
 			return courses;
 		}
 		
 		@Override
 		protected void onPostExecute(List<CourseListable> fetchedCourses) {
 			Log.v(TAG, "Post Load Action");
 			this.callingActivity.availableCourses.clear();
 			this.callingActivity.availableCourses.addAll(fetchedCourses);
 			Log.d(TAG,this.callingActivity.availableCourses.toString());
 			this.callingActivity.setStarredCourses();
 			this.callingActivity.arrayAdapter.notifyDataSetChanged();
 		}
 		
     }
 }
