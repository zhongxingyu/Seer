 package hu.harmakhis.shisha;
 
 import hu.harmakhis.shisha.charts.PlayerChart;
 import hu.harmakhis.shisha.entities.Session;
 import hu.harmakhis.shisha.utils.IntentManager;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.TabHost.TabSpec;
 
 
 public class Statistics extends TabActivity {
 
 	// Josh Clemm's tabhost setup is used here. :)
 	
 	Session s;
     TabHost mTabHost;
     
 	private void setupTabHost() {
 		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
 		mTabHost.setup();
 		PlayerChart pc = new PlayerChart();
 		pc.setSession(s);
 		Intent i = pc.execute(this);
		setupTab(new TextView(this), "Statistics", R.drawable.icon, IntentManager.getSessionSummaryIntent(this, s));
		setupTab(new TextView(this), "Usage time per round", R.drawable.icon, i);
 	}
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.statistics);
         s = (Session) getIntent().getExtras().getSerializable("session");
         setupTabHost();
         
     }
         
     
 	private void setupTab(final View view, final String tag, final int icon, final Intent intent) {
 		View tabview = createTabView(mTabHost.getContext(), tag, icon);
         
 		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
 		mTabHost.addTab(setContent);
 
 	}
 
 	private static View createTabView(final Context context, final String text, final int icon) {
 		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
 		TextView tv = (TextView) view.findViewById(R.id.tabsText);
 		tv.setText(text);
 		
 		Drawable rajz = view.getContext().getResources().getDrawable(icon);
 		
 		ImageView iv = (ImageView) view.findViewById(R.id.tabsImage);
 		iv.setImageDrawable(rajz);
 		
 		return view;
 	}
 }
