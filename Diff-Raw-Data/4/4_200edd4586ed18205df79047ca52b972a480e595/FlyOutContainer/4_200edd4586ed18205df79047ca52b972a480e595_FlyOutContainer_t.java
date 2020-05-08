 package cornell.eickleapp;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import cornell.eickleapp.adapter.NavDrawerListAdapter;
 import cornell.eickleapp.fragments.GoalsFragment;
 import cornell.eickleapp.fragments.HelpFragment;
 import cornell.eickleapp.fragments.HomeFragment;
 import cornell.eickleapp.fragments.KiipFragment;
 import cornell.eickleapp.fragments.RemindersFragment;
 import cornell.eickleapp.fragments.SettingsFragment;
 import cornell.eickleapp.model.NavDrawerItem;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Point;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FlyOutContainer extends LinearLayout implements OnItemClickListener{
 
 	// References to groups contained in this view.
 	private ListView menu;
 	private View content;
 	private TextView day;
 	private TextView monthDate;
 	
 	
 	private ArrayList<NavDrawerItem> navDrawerItems;
 	private String[] navMenuTitles;
 	private TypedArray navMenuIcons;
 	private ListView mDrawerList;
 
 	// Constants
 	protected static final int menuSize = 80;
 	private static int menuMargin = 0;
 
 	public enum MenuState {
 		CLOSED, OPEN
 	};
 
 	// Position information attributes
 	protected int currentContentOffset = 0;
 	protected MenuState menuCurrentState = MenuState.CLOSED;
 
 	public FlyOutContainer(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	public FlyOutContainer(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public FlyOutContainer(Context context) {
 		super(context);
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onAttachedToWindow() {
 		super.onAttachedToWindow();
 
 		
 		this.menu = (ListView) this.getChildAt(0);
 		this.content = this.getChildAt(1);
		/*
 		day=(TextView)findViewById(R.id.tvDateDisplayTop);
 
 		monthDate=(TextView)findViewById(R.id.tvDateDisplayBot);
 		
 		Calendar today = Calendar.getInstance();
 		int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
 		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
 		String dayOfTheWeek = sdf.format(dayOfWeek);
 		int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
 		int month = today.get(Calendar.MONTH);
 		day.setText(dayOfTheWeek);
 		monthDate.setText(month+"/"+dayOfMonth);
		*/
 		
 		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		menuMargin=(display.getWidth())-menuSize; 
 		
 		this.menu.setVisibility(View.GONE);
 		navDrawerItems = new ArrayList<NavDrawerItem>();
 		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
 		navMenuIcons = getResources()
 				.obtainTypedArray(R.array.nav_drawer_icons);
 
 		ListView layout = (ListView) this.menu;
 		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons
 				.getResourceId(0, -1)));
 		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons
 				.getResourceId(1, -1)));
 		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons
 				.getResourceId(2, -1)));
 		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons
 				.getResourceId(3, -1)));
 
 		ListAdapter adapter = new NavDrawerListAdapter(this.getContext(),
 				navDrawerItems);
 		
 		menu.setAdapter(adapter);
 		menu.setOnItemClickListener(new SlideMenuClickListener());
 
 	}
 
 	@Override
 	protected void onLayout(boolean changed, int left, int top, int right,
 			int bottom) {
 		if (changed)
 			this.calculateChildDimensions();
 
 		this.menu.layout(left, top, right - menuMargin, bottom);
 
 		this.content.layout(left + this.currentContentOffset, top, right
 				+ this.currentContentOffset, bottom);
 
 	}
 
 	public void toggleMenu() {
 		switch (this.menuCurrentState) {
 		case CLOSED:
 			this.menu.setVisibility(View.VISIBLE);
 			this.currentContentOffset = this.getMenuWidth();
 			this.content.offsetLeftAndRight(currentContentOffset);
 			this.menuCurrentState = MenuState.OPEN;
 			break;
 		case OPEN:
 			this.content.offsetLeftAndRight(-currentContentOffset);
 			this.currentContentOffset = 0;
 			this.menuCurrentState = MenuState.CLOSED;
 			this.menu.setVisibility(View.GONE);
 			break;
 		}
 
 		this.invalidate();
 	}
 
 	private int getMenuWidth() {
 		return this.menu.getLayoutParams().width;
 	}
 
 	private void calculateChildDimensions() {
 		this.content.getLayoutParams().height = this.getHeight();
 		this.content.getLayoutParams().width = this.getWidth();
 
 		this.menu.getLayoutParams().width = this.getWidth() - menuMargin;
 		this.menu.getLayoutParams().height = this.getHeight();
 	}
 
 	private class SlideMenuClickListener implements
 			ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			// display view for selected nav drawer item
 			displayView(position);
 		}
 	}
 	
 	private void displayView(int position) {
 		// update the main content by replacing fragment
 		switch (position) {
 		default:
 			Toast.makeText(this.getContext(), "Something Here",
 					Toast.LENGTH_SHORT).show();
 			break;
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
 		// TODO Auto-generated method stub
 		displayView(position);
 	}
 
 }
