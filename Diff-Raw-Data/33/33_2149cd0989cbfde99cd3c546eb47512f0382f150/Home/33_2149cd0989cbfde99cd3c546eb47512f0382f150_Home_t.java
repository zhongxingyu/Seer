 package edu.southern;
 
 import java.util.Calendar;
 
 import android.os.Bundle;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.SharedPreferences;
 import edu.southern.R;
 import edu.southern.data.DailyVerseDBHelper;
 import edu.southern.resources.BibleHelper;
 import edu.southern.resources.SearchVerse;
 
 public class Home extends Fragment {
 	
 	private BibleHelper bibleHelper;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// set the action bar layout
 		((HomeScreen) getActivity()).setActionBarView(R.layout.actionbar_home);
 		// Inflate the layout for this fragment
 		View v = inflater.inflate(R.layout.fragment_home, container, false);
 		
 		// Get today's date and stored date
 		Calendar c = Calendar.getInstance();
 		String day = Integer.toString(c.get(Calendar.DATE));
     String month = Integer.toString(c.get(Calendar.MONTH));
     String year = Integer.toString(c.get(Calendar.YEAR));
     
     String todayDate = day.concat(month.concat(year));
 		String lastDate = getActivity().getSharedPreferences("edu.southern", 0).getString("dailyVerseDate", "No Date");
 		int verseReference;
 		
		Log.d("todayDate is", todayDate);
		Log.d("lastDate is", lastDate);
		
 		// If new day, grab next daily verse
 		if (!todayDate.equals(lastDate)) {
 			DailyVerseDBHelper dailyVerseDB = new DailyVerseDBHelper(getActivity());
 			verseReference = dailyVerseDB.getNextDailyVerse();
			
 			// Store today's date and today's reference.
			SharedPreferences.Editor prefs = getActivity().getSharedPreferences("edu.southern", 0).edit();
			prefs.putString("dailyVerseDate", todayDate);
			prefs.putString("dailyVerseReference", Integer.toString(verseReference));
			prefs.commit(); // Store changes to prefs
 		} 
 		else { // get reference number from shared prefs
 			String vr = getActivity().getSharedPreferences("edu.southern", 0).getString("dailyVerseReference", "0");
 			verseReference = Integer.parseInt(vr);
 		}
 		
 		// Get verse contents from verseReference number
 		bibleHelper = new BibleHelper();
 		SearchVerse dailyVerse = bibleHelper.getVerseByReferenceNumber(verseReference);
 		
 		// Update view with reference and text strings
 		TextView dailyVerseView = (TextView)v.findViewById(R.id.DailyVerse);
 		dailyVerseView.setText(dailyVerse.getReference());
 		TextView dailyVerseText = (TextView)v.findViewById(R.id.DailyVerseText);
 		dailyVerseText.setText(dailyVerse.getText());
 
 		return v;
 	}
 }
