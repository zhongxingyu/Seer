 package awesome.app.activity;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.TextView;
 import android.widget.Toast;
 import awesome.app.R;
 import awesome.app.connectivity.NetworkManager;
 import awesome.app.connectivity.SecurityHole;
 
 public class HelpActivity extends Activity {
 
 	private String mCurrentHelp;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.help);
 		makeButtonWork(R.id.scheduleLookupTextView, R.string.scheduleHelpString);
 		makeButtonWork(R.id.studentLookupTextView, R.string.studentHelpString);
 		makeButtonWork(R.id.araMenuTextView, R.string.araHelpString);
 		makeButtonWork(R.id.feedbackTextView, R.string.feedbackHelpString);
 		makeButtonWork(R.id.bandwidthMonitorTextView, R.string.bandwidthHelpString);
 
 	}
 
	public void makeButtonWork(int textViewId, final int helpStringId) {
 		final TextView textView = (TextView) findViewById(textViewId);
 		textView.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				TextView separatorTextView = (TextView) findViewById(R.id.helpSeparatorTextView);
 				String searchString = (String) getString(helpStringId);
 
 				separatorTextView.setText(textView.getText());
 				doHelpSearch(searchString);
 			}
 		});
 	}
 
 	private void doHelpSearch(String searchString) {
 		if (searchString == mCurrentHelp)
 			return;
 
 		mCurrentHelp = searchString;
 		TextView bodyText = (TextView) findViewById(R.id.helpBodyTextView);
 		bodyText.setText(getHelpData(searchString));
 	}
 
 	private String getHelpData(String helpToGet) {
 		if (NetworkManager.isOnline(this)) {
 			String url = getString(R.string.serverURL) + getString(R.string.helpPage);
 
 			String fieldName = getString(R.string.helpFieldName);
 			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
 			pairs.add(new BasicNameValuePair(fieldName, helpToGet));
 			String results = "";
 			try {
 				// HttpClient client = new DefaultHttpClient();
 				HttpClient client = SecurityHole.getNewHttpClient();
 				HttpPost post = new HttpPost(url);
 				post.setEntity(new UrlEncodedFormEntity(pairs));
 				HttpResponse response = client.execute(post);
 				HttpEntity entity = response.getEntity();
 				results = EntityUtils.toString(entity);
 			} catch (MalformedURLException e) {
 				Toast.makeText(this, "Error Fetching Data", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 			} catch (IOException e) {
 				Toast.makeText(this, "I/O Exception!", Toast.LENGTH_SHORT).show();
 				e.printStackTrace();
 			}
 
 			return results;
 		}
 		return null;
 	}
 }
