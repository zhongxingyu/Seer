 package edu.sdsmt.cs492.example7.database;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import edu.sdsmt.cs492.example7.database.Model.Course;
 
 public class ViewDetailFragment extends Fragment
 {
 	//IMPORTANT NOTE: There are no public members.
 	
 	private ICourseControlListener _listener;
 	private Course _course = null;
	private boolean _isOrientationChanging = false;
 	
 	private TextView _textViewCourseNumber;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		// Keep member variables and state, not the best approach, 
		// but the Course class would need to implement Parceable
 		// in order to be passed in Bundle (from both outside the
 		// fragment and inside the fragment on rotation).
 		setRetainInstance(true);
 		
 		// Tells the host Activity to display the appropriate
 		// option menu.
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		// Inflate the UI.
 		View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
 
 		// Assign instances of Views from the Layout Resource.
 		_textViewCourseNumber = (TextView) rootView.findViewById(R.id.textViewCourseNumber);
 
 		return rootView;
 	}
 
 
 	@Override
 	public void onAttach(Activity activity)
 	{
 		try
 		{
 			// Assign listener reference from host activity.
 			_listener = (ICourseControlListener) activity;
 		}
 		catch (ClassCastException e)
 		{
 			throw new ClassCastException(activity.toString());
 		}
 		
 		super.onAttach(activity);
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
		// If we are changing orientation, use the existing _course
		// member to populate the view.
		if (_isOrientationChanging == false)
		{
			// Get a reference to the course that was selected from 
			// the list through the listener interface.
			_course = _listener.getCourse();
		}
		
 		displayCourse();
 	}
 	
 	@Override
	public void onPause()
	{
		// Provides a mechanism by which the Fragment knows if the
		// host Activity is being re-created.  If so, we will want
		// to just use the currently selected _course object to 
		// populate the view which is possible because we are using
		// setRetainInstance(true).
		_isOrientationChanging = getActivity().isChangingConfigurations();
		
		super.onPause();
	}

	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflator)
 	{
 		// Only provide the host activity with a menu if there is an 
 		// actual course that is being edited.  Otherwise, it is a new
 		// course and neither of the Update or Delete menu items should
 		// be available.
 		if (_course.ID > 0)
 		{
 			// Inflate the menu; this adds items to the action bar if it is present.
 			getActivity().getMenuInflater().inflate(R.menu.menu_detail, menu);
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.action_update_course:
 			{
 				_course.CourseNumber += " | UPDATED!";
 				_listener.updateCourse(_course);
 				return true;
 			}
 			case R.id.action_delete_course:
 			{
 				_listener.deleteCourse(_course);
 				return true;
 			}
 			default:
 			{
 				return super.onOptionsItemSelected(item);
 			}
 		}
 	}
 
 	private void displayCourse()
 	{
 		if (_course.ID > 0)
 		{
 			// Use the member Course object to populate the view.
 			_textViewCourseNumber.setText(_course.CourseNumber);
 		}
 		else
 		{
 			// Not fully implemented.
 			_textViewCourseNumber.setText("NEW COURSE COMING SOON!");
 		}
 	}
 }
