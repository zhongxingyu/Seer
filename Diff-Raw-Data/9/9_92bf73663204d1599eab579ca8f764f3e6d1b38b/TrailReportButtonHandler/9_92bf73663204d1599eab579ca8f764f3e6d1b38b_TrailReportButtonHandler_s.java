 package org.dsanderson.morctrailreport;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dsanderson.android.util.AndroidIntent;
 import org.dsanderson.android.util.Maps;
 import org.dsanderson.morctrailreport.parser.MorcFactory;
 import org.dsanderson.morctrailreport.parser.MorcSpecificTrailInfo;
 import org.dsanderson.xctrailreport.core.ISourceSpecificTrailInfo;
 import org.dsanderson.xctrailreport.core.TrailInfo;
 import org.dsanderson.xctrailreport.core.android.TrailReportList;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 
 public class TrailReportButtonHandler {
 	final SherlockListActivity context;
 	final TrailReportList trailReports;
 	
 	public TrailReportButtonHandler(SherlockListActivity context, TrailReportList trailReports) {
 		this.context = context;
 		this.trailReports = trailReports;
 	}
 	
 	public void launchUrl(String url) {
 		AndroidIntent.launchIntent(url, context);
 	}
 	
 	public void onAllReportsButtonClick(View view) {
 		TrailInfo info = infoFromButton(view);
 		if (info == null)
 			return;
 		openAllReports(info);
 	}
 	
 	public void onMoreButtonClicked(View view) {
 		final TrailInfo info = infoFromButton(view);
 		if (info == null)
 			return;
 
 		final List<String> options = new ArrayList<String>();
 		
 		String location = info.getLocation();
 		if (location != null && location.length() > 0)
 			options.add("Map View");
 		
 		ISourceSpecificTrailInfo sourceSpecificTrailInfo = info.getSourceSpecificInfo(MorcFactory.SOURCE_NAME);
 		
 		if (sourceSpecificTrailInfo != null) {
 			MorcSpecificTrailInfo morcInfo = (MorcSpecificTrailInfo) sourceSpecificTrailInfo;
 	
 			final String infoUrl = morcInfo.getTrailInfoUrl();
 			if (infoUrl != null && infoUrl.length() > 0)
 				options.add("Trail Info");
 			
 			final String composeUrl = morcInfo.getComposeUrl();
 			if (composeUrl != null && composeUrl.length() > 0)
 				options.add("Compose Report");
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(context);
 			builder.setTitle(info.getName());
 			String[] optionsArray = new String[options.size()];
 			for (int i = 0; i < options.size(); i++) {
 				optionsArray[i] = options.get(i);
 			}
 			
 			builder.setItems(optionsArray, new OnClickListener() {
 
 				public void onClick(DialogInterface dialog, int which) {
 					if (which >= options.size())
 						return;
 
 					String url = null;
 					String option = (String) options.get(which);
 					if (option.equals("Map View")) {
 						Maps.launchMap(info.getLocation(), info.getName(), info.getSpecificLocation(), context);
 					} else if (option.equals("Trail Info")) {
 						url = infoUrl;
 					} else if (option.equals("Compose Report")) {
 						url = composeUrl;
 					} else {
 						Toast.makeText(context.getApplicationContext(), (CharSequence) "Invalid selection: " + option, Toast.LENGTH_SHORT).show();
 					}
 					dialog.dismiss();
 					if (url != null)
 						launchUrl(url);
 				}
 			});
 			builder.show();
 		}
 	}
 	
 	private TrailInfo infoFromButton(View view) {
		ViewGroup linearLayout = (ViewGroup) view.getParent();
		ViewGroup tableRow = (ViewGroup) linearLayout.getParent();
		ViewGroup table = (ViewGroup) tableRow.getParent();
		ViewGroup parentView = (ViewGroup) table.getParent();
 		
		String trailName = ((TextView) parentView
				.findViewById(R.id.trailNameView)).getText().toString();
 		return trailReports.find(trailName).getTrailInfo();
 	}
 	
 	// / Launch all reports activity
 	private void openAllReports(TrailInfo info) {
 		MorcFactory.getInstance().setAllReportsInfo(info);
 		MorcFactory.getInstance().setAllReportsDate(trailReports.getTimestamp());
 		Intent i = new Intent(context, AllReportActivity.class);
 		context.startActivity(i);
 	}
 	
 	// / Launch Preference activity
 	public void openPreferencesMenu() {
 		Intent i = new Intent(context, PreferencesActivity.class);
 		context.startActivity(i);
 	}
 
 	// / Launch about menu activity
 	public void openAbout() {
 		Intent i = new Intent(context, AboutActivity.class);
 		context.startActivity(i);
 	}
 }
