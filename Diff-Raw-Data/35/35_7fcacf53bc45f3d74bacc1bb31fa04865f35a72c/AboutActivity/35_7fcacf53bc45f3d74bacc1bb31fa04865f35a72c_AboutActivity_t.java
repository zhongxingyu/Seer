 package nl.digitalica.skydivekompasroos;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class AboutActivity extends KompasroosBaseActivity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_about);
 
 		TextView aboutText = (TextView) findViewById(R.id.textViewAbout);
 
 		String aboutString = aboutText();
 
 		// put result in testview
 		aboutText.setText(aboutString);
 
 		// add on click handler to share button
 		ImageButton shareAboutButton = (ImageButton) findViewById(R.id.buttonShareAbout);
 		shareAboutButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				shareAbout();
 			}
 
 		});
 
 	}
 
 	/**
 	 * Return the about text from the raw resource, ammended with some version
 	 * information
 	 * 
 	 * @return
 	 */
 	private String aboutText() {
 		// read text file
 		InputStream iFile = getResources().openRawResource(R.raw.about);
 		String aboutString = "";
 		try {
 			aboutString = inputStreamToString(iFile);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// add version number and compile date/time
 		String mVersionNumber = "?";
 		try {
 			String pkg = getPackageName();
 			mVersionNumber = getPackageManager().getPackageInfo(pkg, 0).versionName;
 		} catch (Exception e) {
 		}
 
 		String compileDate = "";
 		try {
 			long time = getCompileDateTime();
 			compileDate = SimpleDateFormat.getInstance().format(
 					new java.util.Date(time));
 
 		} catch (Exception e) {
 			// TODO: something smart?
 		}
 		if (!compileDate.equals(""))
 			compileDate = " (" + compileDate + ")";
 
 		aboutString += "\n\nVersion: " + mVersionNumber + compileDate;
 		return aboutString;
 	}
 
 	private String inputStreamToString(InputStream iFile) throws IOException {
 		// TODO Auto-generated method stub
 		BufferedReader r = new BufferedReader(new InputStreamReader(iFile));
 		StringBuilder total = new StringBuilder();
 		String line;
 		while ((line = r.readLine()) != null) {
 			total.append(line);
 			total.append(System.getProperty("line.separator"));
 		}
 
 		return total.toString();
 	}
 
 	private void shareAbout() {
 		Intent sendIntent = new Intent();
 		sendIntent.setAction(Intent.ACTION_SEND);
 		sendIntent.putExtra(Intent.EXTRA_TEXT, aboutText());
 		sendIntent.putExtra(Intent.EXTRA_SUBJECT,
 				getString(R.string.shareaboutsubject));
 		sendIntent.putExtra(Intent.EXTRA_TITLE,
 				getString(R.string.shareaboutsubject));
 		sendIntent.setType("text/plain");
 		startActivity(sendIntent);
 	}
 
 }
