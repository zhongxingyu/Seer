 package de.dhbw.contents;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
 
 import de.dhbw.container.R;
 import de.dhbw.tracking.DistanceSegment;
 
 public class EvaluationViewPager extends Fragment {
 
 	public static final String TAG = EvaluationViewPager.class
 			.getSimpleName();
 	private List<DistanceSegment> mSegmentList = new ArrayList<DistanceSegment>();
 
 	public static EvaluationViewPager newInstance() {
 		return new EvaluationViewPager();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		Bundle bundle = this.getArguments();
 		for (int i=0; i < bundle.getInt("segmentlength"); i++)
 		{
 			DistanceSegment tempSegment = new DistanceSegment();
 			tempSegment.fromStringArray(bundle.getStringArray("segment"+String.valueOf(i)));
 			mSegmentList.add(tempSegment);
 		}
 		
 		for (DistanceSegment ds : mSegmentList)
 			Log.d("Test", ds.toString());
 		
 		return inflater.inflate(R.layout.evaluation_pager, container, false);
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) view
 				.findViewById(R.id.tabs);
 		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
 		EvaluationPagerAdapter adapter = new EvaluationPagerAdapter(getChildFragmentManager());
 		pager.setAdapter(adapter);
 		tabs.setViewPager(pager);
 
 	}
 	
 	public class EvaluationPagerAdapter extends FragmentPagerAdapter {
 
 		public EvaluationPagerAdapter(android.support.v4.app.FragmentManager fm) {
 			super(fm);
 		}
 
 		private final String[] TITLES = {"Diagramme","Zwischentabelle","Hauptansicht"};
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return TITLES[position];
 		}
 
 		@Override
 		public int getCount() {
 			return TITLES.length;
 		}
 
 		@Override
 	    public SherlockFragment getItem(int position) {
 	        switch (position) {
 	        case 0:
 	            return new EvaluationDiagrammFragment();
 	        case 1:
 	            return new EvaluationStagesFragment(mSegmentList);
 	        case 2:
 	            return new EvaluationSummaryFragment();
 	
 	        default:
 	            return null;
 	        }
 	    }
 	}
 }
