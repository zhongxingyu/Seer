 package com.pragyancsg.pragyanapp13;
 
 import android.support.v4.app.Fragment;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
import com.origamilabs.library.views.StaggeredGridView;
 
 
 public class StaggeredFragment extends Fragment {
 
 
 
 	private String urls[]={
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/events.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/manigma.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/highlights.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/exhi.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/workshops.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/PGT.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/codeit.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/brainworks.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/chillpill.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/core.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/robovigyan.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/psr.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/crossfire.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/infotainment.jpg",
 			"http://www.pragyan.org/13/cms/templates/mainpage/img/mainsite/pengufest.jpg"
 };
 	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
 		
 		StaggeredGridView gridView = new StaggeredGridView(getActivity());
 		
 		int margin = getResources().getDimensionPixelSize(R.dimen.margin);
 		
 		gridView.setItemMargin(margin); // set the GridView margin
 		
 		gridView.setPadding(margin, 0, margin, 0); // have the margin on the sides as well 
 		
 		StaggeredAdapter adapter = new StaggeredAdapter(getActivity(), R.id.imageView1, urls);
 		
 		gridView.setAdapter(adapter);
 		adapter.notifyDataSetChanged();
 		return gridView;
 		//return super.onCreateView(inflater, container, savedInstanceState);
 	}
 
 }
