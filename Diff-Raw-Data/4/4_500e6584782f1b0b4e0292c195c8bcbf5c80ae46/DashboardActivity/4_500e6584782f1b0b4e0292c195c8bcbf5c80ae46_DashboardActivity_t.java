 package no.uio.inf5750.assignment3.dashboard;
 
 import java.io.ByteArrayOutputStream;
 import java.util.LinkedList;
 import java.util.TreeMap;
 
 import no.uio.inf5750.assignment3.R;
 import no.uio.inf5750.assignment3.util.ConnectionManager;
 import no.uio.inf5750.assignment3.util.UpdateDaemon;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MotionEvent;
 
 public class DashboardActivity extends Activity {
 	private ProgressBar mProgressBar1, mProgressBar2;
 	
 	private ImageView mImageView1, mImageView2;
 	private TextView mTextViewPage;
 	private Button mButtonPrevPage, mButtonNextPage;
 
 	private Drawable mDrawable1;
 	private Drawable mDrawable2;
 	
 	// TEMPORARY SOLUTION UNTIL USER SETTINGS ARE RETRIEVABLE:
 	private TreeMap<String, String> mCharts = new TreeMap<String, String>();
 	private LinkedList<String> mChartURLs = new LinkedList<String>();
 	private int mCurrentPage = 0;
 	private static final int mChartsPerPage = 2;
 	private String mChart1 = null;
 	private String mChart2 = null;
 	private int mChartToReplace = -1;
 	private boolean mInContextMenu = false;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		initializeChartURLs(); // TEMPORARY SOLUTION UNTIL USER-SETTINGS ARE IMPLEMENTED
 
 		mButtonPrevPage = (Button) findViewById(R.id.dashboard_btnPrevPage);
 		mButtonNextPage = (Button) findViewById(R.id.dashboard_btnNextPage);
 		mImageView1 = (ImageView) findViewById(R.id.main_imageview1);
 		mImageView2 = (ImageView) findViewById(R.id.main_imageview2);
 		mTextViewPage = (TextView) findViewById(R.id.dashboard_pageNumber);
 		mProgressBar1 = (ProgressBar) findViewById(R.id.diagram_progress1);
 		mProgressBar2 = (ProgressBar) findViewById(R.id.diagram_progress2);
 
 		registerForContextMenu(mImageView1);
 		registerForContextMenu(mImageView2);
 		
 		setText();
 		setButtons();
 		setImages();
 	}
 	
 	/** Opens context menu when long-pressing a chart. */
 	@Override
 	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		mInContextMenu = true;
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		updateChartsMap();
 		
 		if (mCharts.isEmpty()) {
 			Toast toast = Toast.makeText(DashboardActivity.this,
 					"Error: No charts available.", Toast.LENGTH_LONG);
 			toast.show();
 			mChartToReplace = -1;
 			mInContextMenu = false;
 			return;
 		}
 
 		// Determine which chart triggered the context menu:
 		if (((ImageView) v).getId() == R.id.main_imageview1) {
 			mChartToReplace = mCurrentPage*mChartsPerPage;
 		} else if (((ImageView) v).getId() == R.id.main_imageview2) {
 			mChartToReplace = mCurrentPage*mChartsPerPage+1;
 		} else { // Long-press outside of a chart.
 			mChartToReplace = -1;
 			mInContextMenu = false;
 			return;
 		}
 		
 		menu.setHeaderTitle("Replace with...");
 		
 		// Populate menu with available charts:
 		for (String label : mCharts.keySet()) {
 			menu.add(0, v.getId(), 0, label);
 		}
 	}
 	
 	/** Called when an element is selected from context menu.
 	 * @return Returns true when chart is successfully replaced,
 	 *         otherwise false. */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// Get URL for chart user selected:
 		String tempURL = mCharts.get(item.getTitle());
 		
 		// Return if URL not found:
 		if (tempURL == null) {
 			mInContextMenu = false;
 			return false;
 		}
 		
 		if (mChartURLs.get(mChartToReplace) == null) {
 			mInContextMenu = false;
 			return false;
 		}
 
 		// Replace chart to display with newly selected chart:
 		mChartURLs.set(mChartToReplace, tempURL);
 
 		setImages();
 		mInContextMenu = false;
 		return true;
 	}
 	
 	// TEMPORARY SOLUTION UNTIL USER-SETTINGS ARE AVAILABLE:
 	private void initializeChartURLs() {
 		mChartURLs.add("http://apps.dhis2.org/demo/api/charts/UlfTKWZWV4u/data");
 		mChartURLs.add("http://apps.dhis2.org/demo/api/charts/fxIAgTaTFwJ/data");
 		mChartURLs.add("http://apps.dhis2.org/demo/api/charts/E9D9KmjyHnd/data");
 		mChartURLs.add("http://apps.dhis2.org/demo/api/charts/LYhJvlJxS5Y/data");
 		mChartURLs.add("http://apps.dhis2.org/demo/api/charts/IC97MoXqmrP/data");
 		mChartURLs.add("http://apps.dhis2.org/demo/api/charts/vkVZOjsLq0E/data");
 		mChartURLs.add("http://apps.dhis2.org/demo/api/charts/Emq3LEyWb15/data");
 		mChartURLs.add("http://apps.dhis2.org/demo/api/charts/wSWxbt3TueB/data");
 	}
 
 	/** Fetches available charts from the web API. */
 	public void updateChartsMap() {
 		// Contacts server to get currently available charts.
 		UpdateDaemon.getDaemon().updateCharts();
 		UpdateDaemon.getDaemon().repopulateSortedChartImageHrefTree(mCharts);
 	}
 	
 	/** Initializes button listeners for the Dashboard activity. */
 	private void setButtons() {
 		// "Previous page" button
 		mButtonPrevPage.setOnClickListener(new OnClickListener() 
 		{	
 			public void onClick(View v) 
 			{
 				if (mCurrentPage == 0) {
 					Toast toast = Toast.makeText(DashboardActivity.this,
 							"Error: Already on first page.", Toast.LENGTH_LONG);
 					toast.show();
 				}
 				else {
 					mCurrentPage -= 1;
 					setText();
 					setImages();
 				}
 			}
 		});
 
 		// "Next page" button
 		mButtonNextPage.setOnClickListener(new OnClickListener() 
 		{	
 			public void onClick(View v) 
 			{
 				if ((mCurrentPage+1)*mChartsPerPage >= mChartURLs.size()) {
 					Toast toast = Toast.makeText(DashboardActivity.this,
 							"Error: Already on last page.", Toast.LENGTH_LONG);
 					toast.show();
 				}
 				else {
 					mCurrentPage += 1;
 					setText();
 					setImages();
 				}
 			}
 		});
 	}
 	
 	/** Initializes chart images and their listeners for the Dashboard activity. */
 	private void setImages()
 	{ // Fetches data in a thread for making things appear smoother
    	mProgressBar1.setVisibility(View.VISIBLE);
    	mProgressBar2.setVisibility(View.VISIBLE);
		mImageView1.setVisibility(View.INVISIBLE);
		mImageView2.setVisibility(View.INVISIBLE);
 		mChart1 = mChartURLs.get(mCurrentPage*mChartsPerPage);
 		mChart2 = mChartURLs.get(mCurrentPage*mChartsPerPage+1);
 		
 		final Thread setImageThread1 = new Thread(){
 			public void run()
 			{
 				if(mDrawable1!=null)
 				{
 		        	mImageView1.setImageDrawable(mDrawable1);
 				}
 				else
 				{
 					mImageView1.setImageDrawable(getResources().getDrawable(R.drawable.notfound));
 				}
 				mImageView1.setVisibility(View.VISIBLE);
 	        	mProgressBar1.setVisibility(View.INVISIBLE);
 			}
 		};
 
 		final Thread setImageThread2 = new Thread(){
 			public void run()
 			{
 		        if(mDrawable2!=null)
 		        {
 		        	mImageView2.setImageDrawable(mDrawable2);
 		        }
 		        else
 				{
 					mImageView2.setImageDrawable(getResources().getDrawable(R.drawable.notfound));
 				}
 				mImageView2.setVisibility(View.VISIBLE);
 	        	mProgressBar2.setVisibility(View.INVISIBLE);
 			}
 		};
 
 		//Fetching images in separate threads
 		new Thread()
 		{
 			public void run()
 			{
 				mDrawable1 = ConnectionManager.getConnectionManager().getImage(mChart1);
 				mImageView1.setOnTouchListener(new DashboardOnTouchListener(mDrawable1));
 				runOnUiThread(setImageThread1);
 			}
 		}.start();
 
 		new Thread()
 		{
 			public void run()
 			{
 				mDrawable2 = ConnectionManager.getConnectionManager().getImage(mChart2);
 				mImageView2.setOnTouchListener(new DashboardOnTouchListener(mDrawable2));
 		        runOnUiThread(setImageThread2);
 			}
 		}.start();
 	}
 	
 	/** Updates the page number. */
 	@SuppressLint("DefaultLocale")
 	private void setText() {
 		String pageText = String.format("Page %d/%d", mCurrentPage+1, 
 				mChartURLs.size()/mChartsPerPage + ((mChartURLs.size() % mChartsPerPage) & 1));
 		mTextViewPage.setText(pageText);
 	}
 	
 	/** Launches the Chart activity to display a full size image. 
 	 * @param image Image to display. */
 	public void launchChartActivity(Drawable image)
 	{
 		// Convert to byte array for sending through Extras.
 		Bitmap bmp = ((BitmapDrawable) image).getBitmap();
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		bmp.compress(Bitmap.CompressFormat.PNG, 100, bos); // NOTE: THIS ASSUMES PNGs!
 		byte[] imageBytes = bos.toByteArray();
 
 		Intent intent = new Intent(this, DashboardChartActivity.class);
 		intent.putExtra("dashboard_chart", imageBytes);
 		startActivity(intent);
 	}
 	
 	/** Touch listener class for the chart images. */
 	class DashboardOnTouchListener implements OnTouchListener {
 		Drawable drawable;
 		
 		DashboardOnTouchListener(Drawable draw) {
 			drawable = draw;
 		}
 		
 		/** Called when a touch is registered on the charts.
 		 * Only registers short-presses to avoid interfering with long-press context menu. */
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP && !mInContextMenu) {
 				launchChartActivity(drawable);
 			}
 			
 			return false;
 		}
 	}
 }
