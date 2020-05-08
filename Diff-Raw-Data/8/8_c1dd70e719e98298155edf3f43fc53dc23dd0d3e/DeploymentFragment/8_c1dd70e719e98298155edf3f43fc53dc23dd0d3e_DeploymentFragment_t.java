 package com.tortel.deploytrack;
 
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.echo.holographlibrary.PieGraph;
 import com.echo.holographlibrary.PieSlice;
 import com.tortel.deploytrack.data.Deployment;
 
 /**
  * Fragment that displays the fancy deployment graph and info
  */
 public class DeploymentFragment extends SherlockFragment {
 	private Deployment deployment;
 	
 	/**
 	 * Creates a new DeploymentFragment with the provided
 	 * Deployment
 	 * @param deployment
 	 * @return
 	 */
 	public static DeploymentFragment newInstance(Deployment deployment){
 		DeploymentFragment fragment = new DeploymentFragment();
 		fragment.deployment = deployment;
 		return fragment;
 	}
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState){
 		Resources resources = getActivity().getResources();
 		
 		View view = inflater.inflate(R.layout.fragment_deployment, container, false);
 		
 		//Name
 		TextView name = (TextView) view.findViewById(R.id.name);
 		name.setText(deployment.getName());
 		
 		//Date range
 		TextView dateRange = (TextView) view.findViewById(R.id.daterange);
 		dateRange.setText(deployment.getFormattedStart()
 				+resources.getString(R.string.to)
 				+deployment.getFormattedEnd());
 		
 		//Figure out the percentage
 		DateTime now = new DateTime();
 		int days = Days.daysBetween(deployment.getStart(), deployment.getEnd()).getDays();
 		int percent = 0;
 		int completed = 0;
 		
		//Check if its started
 		if(now.compareTo(deployment.getStart()) > 0){
 			completed = Days.daysBetween(deployment.getStart(), now).getDays();
 			percent = (int) ((double) completed / (double) days * 100);
 		}
 		
		//Extra check for completed events
		if(now.compareTo(deployment.getEnd()) >= 0) {
			completed = days;
			percent = 100;
		}
		
 		//Days completed, days left
 		TextView stats = (TextView) view.findViewById(R.id.time_stats);
 		stats.setText(completed+resources.getString(R.string.days_complete)+
 				(days - completed)+resources.getString(R.string.days_left));
 		
 		//Percentage
 		TextView percentage = (TextView) view.findViewById(R.id.percentage);
 		percentage.setText(percent+"%");
 		
 		//Fill the graph
 		PieGraph pie = (PieGraph) view.findViewById(R.id.graph);
 		
 		//Fill it
 		pie.setThickness(150);
 		
 		
 		PieSlice completedSlice = new PieSlice();
 		completedSlice.setColor(Color.GREEN);
 		completedSlice.setValue(completed);
 		pie.addSlice(completedSlice);
 		PieSlice togoSlice = new PieSlice();
 		togoSlice.setColor(Color.RED);
 		togoSlice.setValue(days - completed);
 		pie.addSlice(togoSlice);
 		
 		return view;
 	}
 }
