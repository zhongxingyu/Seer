 package com.cedar.app.test;
 
 import java.util.*;
 
 import com.cedar.app.ListofRides;
 import com.cedar.app.R;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 import android.widget.*;
 
 public class ListofRidesScrollTEST extends
 		ActivityInstrumentationTestCase2<ListofRides> {
 	public List<String> ridesList;
 	ListofRides LOR;
 	CheckBox check;
 
 	public ListofRidesScrollTEST() {
 		super(ListofRides.class);
 	}
 
 	protected void setUp() throws Exception {
		super.setUp();
		//setActivityInitialTouchMode(false);
		
 		LOR = getActivity();
 		ridesList = new LinkedList<String>();
 		ridesList = LOR.ridesList;
 	}
 	
 	public void testScrolling()
 	{
 		CheckBox[] checks = new CheckBox[2];
 		checks[0] = (CheckBox) LOR.findViewById(R.id.dodgem_button);
 		checks[1] = (CheckBox) LOR.findViewById(R.id.calypso_button);
 		
 		for (CheckBox c : checks)
 		{
 			TouchUtils.clickView(this, c);	
 		}
 		
 		TouchUtils.drag(this, 100, 100, 375, 0, 12);
 
 		
 		checks[0] = (CheckBox) LOR.findViewById(R.id.matterhorn_button);
 		checks[1] = (CheckBox) LOR.findViewById(R.id.magnum_xl200_button);
 
 		for (CheckBox c : checks)
 		{
 			TouchUtils.clickView(this, c);	
 		}
 		
 		TouchUtils.dragQuarterScreenDown(this, getActivity());
 		TouchUtils.dragQuarterScreenDown(this, getActivity());
		TouchUtils.dragQuarterScreenDown(this, getActivity());

 
 		checks[0] = (CheckBox) LOR.findViewById(R.id.dodgem_button);
 		checks[1] = (CheckBox) LOR.findViewById(R.id.calypso_button);
 		
 		for (CheckBox c : checks)
 		{
 			TouchUtils.clickView(this, c);	
 		}
 		
 		assertTrue(LOR.ridesList.contains(LOR.getString(R.string.matterhorn)));
 		assertTrue(LOR.ridesList.contains(LOR.getString(R.string.magnum_xl200)));	
 		
 		assertFalse(LOR.ridesList.contains(LOR.getString(R.string.dodgem)));
 		assertFalse(LOR.ridesList.contains(LOR.getString(R.string.calypso)));	
 		
 		assertFalse(((CheckBox) LOR.findViewById(R.id.dodgem_button)).isChecked());
 		assertFalse(((CheckBox) LOR.findViewById(R.id.calypso_button)).isChecked());
 
 		assertTrue(((CheckBox) LOR.findViewById(R.id.matterhorn_button)).isChecked());
 		assertTrue(((CheckBox) LOR.findViewById(R.id.magnum_xl200_button)).isChecked());
 		}
 }
