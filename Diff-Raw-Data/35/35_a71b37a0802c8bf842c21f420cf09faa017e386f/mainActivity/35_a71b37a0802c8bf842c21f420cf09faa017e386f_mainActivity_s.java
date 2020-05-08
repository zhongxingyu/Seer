 package activities;
 
 import java.util.Locale;
 
 import utils.Controller;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
import batmanmustdie.src.R;
 
 public class mainActivity extends ListActivity implements OnInitListener {
 
 	double[] lat = new double[2];
 	double[] lon = new double[2];
 	String[] msg = new String[2];
 	
 	private TextToSpeech textToSpeech;
 	private Boolean ttsReady = false;
 	private TextView start;
 	private TextView end;
 	ListView lv;
 	Controller controller = Controller.getInstance();
 	
 	public final int CHOOSE_ROUTE_CODE = 0;
 	public final int TEXT_TO_SPEECH_CHECK_CODE = 1;
 	private final String TAG = "mainActivity";
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         lv = getListView();
         setContentView(R.layout.rsslist);
         this.setListAdapter(controller.getRelevantFeedEntries(this));
         // temporary hardcoded coordinates for testing
         // controller class will provide this data
         lat[0] = 53.5667294257814;
         lat[1] = 52.3391636650016;
         lon[0] = -2.23450725437083;
         lon[1] = -0.206259282604262;
         msg[0] = "warning 1";
         msg[1] = "warning 2";
         
         // checks if text to speech is installed on the phone
         Intent checkIntent = new Intent();
         checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
         startActivityForResult(checkIntent, TEXT_TO_SPEECH_CHECK_CODE);
         
         Button button = (Button)findViewById(R.id.rssChangeRouteButton);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Load the route chooser activity
             	// It expects to get back values for To and From textviews
               	Intent i = new Intent(v.getContext(), ChooseRoute.class);
               	startActivityForResult(i, CHOOSE_ROUTE_CODE);
             }
         });
         
         Button mapButton = (Button) findViewById(R.id.showWarningsOnMapButton);
         mapButton.setOnClickListener(new View.OnClickListener() {       	
 	
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(v.getContext(), WarningOnMap.class);
 				i.putExtra("lat", lat);
 				i.putExtra("lon", lon);
 				i.putExtra("msg", msg);
 				startActivity(i);				
 			}
 		});
         
         start = (TextView) findViewById(R.id.rssFromText);
         end = (TextView) findViewById(R.id.rssToText);
         
         // TODO
         // implement a timer that pulls the RSS feed, updates the listview and reads to the user
     }
     protected void onActivityResult(
             int requestCode, int resultCode, Intent data) {
         if (requestCode == TEXT_TO_SPEECH_CHECK_CODE) {
             if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                 // success, create the TTS instance
             	textToSpeech = new TextToSpeech(this, this);
             	Log.d(TAG, "Text to speech exists");
             } else {
                 // missing data, install it
                 Intent installIntent = new Intent();
                 installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                 startActivity(installIntent);
             }
         } else if (requestCode == CHOOSE_ROUTE_CODE) {
         	// update the warnings for the current route
         	// TODO implement it!
         	
         	// Update the current To and From fields
         	Bundle fromTo = data.getExtras();
         	if (fromTo != null) {
         		// string names are "start" and "end"
         		this.start.setText(fromTo.getString("start"));
         		this.end.setText(fromTo.getString("end"));
         	}
         	// read out updates
             if (ttsReady) {
             	Log.d(TAG, "speaking");
                 String myText1 = "Did you sleep well?";
                 String myText2 = "I hope so, because it's time to wake up.";
             	textToSpeech.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);
             	textToSpeech.speak(myText2, TextToSpeech.QUEUE_ADD, null);
             } 
         }
     }
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Intent viewMessage = new Intent(Intent.ACTION_VIEW, 
 				Uri.parse(controller.relevantMessages.get(position).getLink().toExternalForm()));
 		this.startActivity(viewMessage);
 	}
 	
 
 	
 	
 	@Override
 	public void onInit(int status) {
 		if (status == TextToSpeech.SUCCESS) {
             int result = textToSpeech.setLanguage(Locale.UK);
             if (result == TextToSpeech.LANG_MISSING_DATA ||
                 result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language data is missing or the language is not supported.
                 Log.e(TAG, "Language is not available.");
             } else {
             	Log.d(TAG, "TextToSpeech is ready for use");
             	ttsReady = true;
             }
         } else {
             // Initialization failed.
             Log.e(TAG, "Could not initialize TextToSpeech.");
         }
 		
 	}
 
 	
 }
