 package ru.inventos.yum;
 
 
import android.annotation.SuppressLint;
 import android.content.Context;
 
 import com.caldroid.CaldroidFragment;
 import com.caldroid.CaldroidGridAdapter;
 
 
 public class CaldroidCustomFragment extends CaldroidFragment {
 	private String[] mMonths;
	

 	public CaldroidCustomFragment(Context context) {
 		super();
 		mMonths = context.getResources().getStringArray(R.array.month);		 
 	}	
 	
 	@Override
 	public void refreshView() {
 		getMonthTitleTextView().setText(mMonths[month-1].toUpperCase() + " " + year);
 		for (CaldroidGridAdapter adapter : datePagerAdapters) {
 			adapter.setCaldroidData(getCaldroidData());
 			adapter.setExtraData(extraData);
 			adapter.notifyDataSetChanged();
 		}
 	}
 }
