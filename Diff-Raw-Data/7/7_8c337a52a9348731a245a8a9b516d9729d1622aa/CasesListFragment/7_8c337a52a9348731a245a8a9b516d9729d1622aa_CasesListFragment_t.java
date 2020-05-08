 package com.pawhub.lostandfound;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 
 public class CasesListFragment extends Fragment{
 
 	private final int SCREEN_ALERTS = 0;
 	private final int SCREEN_REPORTS = 1;
 	private final int SCREEN_LOST = 2;
 	private final int SCREEN_FOUND = 3;
 	private final int SCREEN_ABUSE = 4;
 	private final int SCREEN_HOMELESS = 5;
 	
 	private LinearLayout parentLayout;
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
         return inflater.inflate(R.layout.cases_list_layout, container, false);
     }
 	
 	@Override
     public void onActivityCreated(Bundle state) {
         super.onActivityCreated(state);
         parentLayout=(LinearLayout)getView().findViewById(R.id.layoutCasesList);
         
         Bundle arguments=getArguments();
         initScreen(arguments);
     }
 	
 	private void initScreen(Bundle arguments){
 		int screenType=arguments.getInt("TYPE");
 		
 		switch(screenType){
 			case SCREEN_ALERTS:
 				initScreenAlerts();
 				break;
 			case SCREEN_REPORTS:
 				initScreenReports();
 				break;
 			case SCREEN_LOST:
 				initScreenLosts();
 				break;
 			case SCREEN_FOUND:
 				initScreenFound();
 				break;
 			case SCREEN_ABUSE:
 				initScreenAbuse();
 				break;
 			case SCREEN_HOMELESS:
 				initScreenHomeless();
 				break;
 				
 		}
 	}
 	
 	private void initScreenAlerts(){
 		 addDetailChart();
 	}
 	
 	private void initScreenReports(){
 		 addDetailChart();
 		 addDetailChart();
 	}
 	
 	private void initScreenLosts(){
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();
 	}
 	
 	private void initScreenAbuse(){
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();
 	}
 
 	private void initScreenFound(){
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();
 	}
 	
 	private void initScreenHomeless(){
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();
 		 addDetailChart();	 
 	}
 	
 	
 	private void addDetailChart(){
 		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
 		//inflater.inflate(R.layout.detail_chart_1,parentLayout);
 		View v=inflater.inflate(R.layout.detail_chart_1, null);
 		parentLayout.addView(v);
 	}
	
	private void addDetailChart2(){
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		inflater.inflate(R.layout.detail_chart_2,parentLayout);
	}
 }
