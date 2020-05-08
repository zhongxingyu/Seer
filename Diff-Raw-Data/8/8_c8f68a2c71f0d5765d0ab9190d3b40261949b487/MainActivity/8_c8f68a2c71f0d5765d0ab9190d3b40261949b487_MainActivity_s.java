 package com.megginson.sloop.ui;
 
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.Loader;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.megginson.sloop.R;
 import com.megginson.sloop.model.DataCollection;
 
 public class MainActivity extends FragmentActivity implements
 		LoaderCallbacks<DataCollection> {
 
 	public static String SAMPLE_FILE = "pwgsc_pre-qualified_supplier_data.csv";
 
 	/**
 	 * The {@link PagerAdapter} for the current data collection.
 	 */
 	private DataCollectionPagerAdapter mPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the data collection.
 	 */
 	private ViewPager mViewPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_main);
 
 		mPagerAdapter = new DataCollectionPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the data collection adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mPagerAdapter);
 
 		Button goButton = (Button) findViewById(R.id.goButton);
 		goButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String url = ((EditText) findViewById(R.id.urlField)).getText()
 						.toString();
 				if (url != null && url.length() > 0) {
 					Bundle args = new Bundle();
 					args.putString("url", url);
 					getLoaderManager()
 							.restartLoader(0, args, MainActivity.this);
 					setProgressBarIndeterminateVisibility(true);
 					System.err.println("Loading URL " + url);
 				}
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public Loader<DataCollection> onCreateLoader(int id, Bundle args) {
 		// only one loader for now, so ignore id
 		// XXX do we have to do anything with args?
 		DataCollectionLoader loader = new DataCollectionLoader(
 				getApplicationContext());
 		loader.setURL(args.getString("url"));
 		loader.setResourceName(args.getString("resourceName"));
 		return loader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<DataCollection> loader,
 			DataCollection dataCollection) {
 		mPagerAdapter.setDataCollection(dataCollection);
 		MainActivity.this.setProgressBarIndeterminateVisibility(false);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<DataCollection> dataCollection) {
 		// TODO clear old data from the viewpager
 	}
 
 }
