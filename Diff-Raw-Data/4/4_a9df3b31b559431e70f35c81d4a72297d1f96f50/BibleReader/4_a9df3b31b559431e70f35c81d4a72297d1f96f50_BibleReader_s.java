 package edu.southern;
 
 import java.util.ArrayList;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.provider.SyncStateContract.Helpers;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.webkit.WebView.FindListener;
 import android.widget.Button;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.southern.resources.BibleHelper;
 import edu.southern.resources.Chapter;
 import edu.southern.resources.Verse;
 
 public class BibleReader extends Fragment {
 	static ArrayAdapter<Verse> adapter;
 	BibleHelper bibleHelper = new BibleHelper();
 	int scrollto = 0; //Keeps track of the selected verse's textview
 	LayoutInflater inflater;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		this.inflater = inflater;
 		return inflater.inflate(R.layout.fragment_bible_reader, container,
 				false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		// Adding the layout programatically
 		displayChapterText();
 		ImageButton backBtn = (ImageButton) getActivity().findViewById(R.id.back);
 		ImageButton nextBtn = (ImageButton) getActivity().findViewById(R.id.next);
 		//Button click logic
 		//On NextBtn click, check if end of book
 		//If true, go to next book chapter 1
 		//If false, go to same book next chapter
 		nextBtn.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
             	openNextChapter((ImageButton)v);
             }
         });
 				//On BackBtn click, check if beginning of book
 					//If true, go to previous book end chapter
 					//If false, go to same book previous chapter
 		backBtn.setOnClickListener(new OnClickListener() {
 		   public void onClick(View v) {
 			   openPreviousChapter((ImageButton)v);
 		   }
          });	    
 	}
 	
 	private void displayChapterText() {
 		final ScrollView scrollview = (ScrollView)getActivity().findViewById(R.id.BibleReaderScroll);
 		scrollview.removeAllViews();
 		// Adding the layout programmatically
 		final LinearLayout linearLayout = new LinearLayout(getActivity());
 		linearLayout.setOrientation(LinearLayout.VERTICAL);
 		scrollview.addView(linearLayout);
 		// Get the value of the book selected from SharedPreferences
 		SharedPreferences prefs = this.getActivity().getSharedPreferences(
 				"edu.southern", Context.MODE_PRIVATE);
 		
 		// prevent a bad book value from crashing the program by defaulting to Genesis
 		int book_value = prefs.getInt("book_value", 0);
 		if (book_value < 0 || book_value > 65)
 			book_value = 0;
 		int chapter_value = prefs.getInt("chapter_value", 0) + 1;
 		int verse_value = prefs.getInt("verse_value", 0) + 1;
 		final String bookName = bibleHelper.getBookName(book_value);
 		
 		Chapter chapter = null;
 		try {
 			chapter = bibleHelper.getChapterText(bookName, chapter_value);
 		} catch (Exception e) {}
 
 		// set the action bar layout
 		initializeActionBar(bookName, chapter_value);
 		
 		// display the text of the chapter
 		ArrayList<Verse> bible = chapter.verses;
 		int toSetTextSize = prefs.getInt("fontSize",10);
 		for (int i = 0; i < chapter.numVerses; i++) {
 			displayVerseText(linearLayout, verse_value, bible, i, toSetTextSize);
 		}
 
 		// set the text on the currently reading button in the nav drawer
 		((HomeScreen) getActivity()).updateCurrentlyReading(book_value,
 				chapter_value, verse_value);
 		scroll(scrollview);
 		
 		//Button stuff		
 		// Get the value of the book selected from SharedPreferences
 			//Get button IDs
 		ImageButton backBtn = (ImageButton) getActivity().findViewById(R.id.back);
 		ImageButton nextBtn = (ImageButton) getActivity().findViewById(R.id.next);
 			//Disable button logic
 		boolean shouldEnable = true;
 		if (book_value == 0 && chapter_value == 1){ //If at Gen 1, Disable the Back button
 			shouldEnable = false;
 		}
 		backBtn.setEnabled(shouldEnable);
 		backBtn.setClickable(shouldEnable);
 		shouldEnable = true;
 		int numBooks = bibleHelper.getBooks().length;
 		try {
 			if (book_value == numBooks-1 && chapter_value == bibleHelper.getChapterCount(numBooks-1)){ // If at Rev 22, Disable the Next button
 				shouldEnable = false;
 			}
 		} catch (Exception e) {}
 		nextBtn.setEnabled(shouldEnable);
 		nextBtn.setClickable(shouldEnable);
 	}
 
 	private void displayVerseText(final LinearLayout linearLayout,
 		int verse_value, ArrayList<Verse> bible, int i, int textSize) {
 		// Populating the layout with verses with different id
 		TextView bibleDisplay = new TextView(getActivity());
 		bibleDisplay.setTextSize(textSize);
 		bibleDisplay.setId(i + 1);
 		Verse verseInfo = bible.get(i);
 		String verse = verseInfo.getText();
 		int verseNumber = verseInfo.getVerseNumber();
 
 
 		String bibleInfo = "&nbsp;&nbsp;&nbsp;&nbsp;" + "<strong>" + verseNumber + "</strong>" + " "
 				+ "<font size=\"10\">" + verse + "</font>";
		if(i==0)bibleDisplay.setPadding(2, 20, 0, 10);
		else bibleDisplay.setPadding(2, 0, 0, 6);
 		
 		bibleDisplay.setLineSpacing(0.0f, 1.3f);
 		
 		bibleDisplay.setText(Html.fromHtml(bibleInfo));
 		if(i+1==verse_value)
 	    	scrollto = i+1;
 		linearLayout.addView(bibleDisplay);
 		// Verses onClick handler
 		bibleDisplay.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// create Account
 				int id = v.getId();
 				String text = String.valueOf(id);
 				Toast.makeText(getActivity().getApplicationContext(), text,
 						Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 	
 	/**
 	 * Scroll to the selected verse's textview
 	 * @param ScrollView
 	 *       			scrollview
 	 */
 	public void scroll(final ScrollView scrollview) {
 		scrollview.post(new Runnable() {
 	        @Override
 	        public void run() {
 	        	View contextV  = (TextView) getView().findViewById(scrollto);
 	        	scrollto = contextV.getTop();
             	scrollview.scrollTo(0, scrollto);
 	        }
 	    });
 	}
 	
 	private void openNextChapter(ImageButton b){
 		SharedPreferences prefs = this.getActivity().getSharedPreferences(
 				"edu.southern", Context.MODE_PRIVATE);
 		int book_value = prefs.getInt("book_value", 0);
 		try {
 			int chapterCount = bibleHelper.getChapterCount(book_value);
 			int chapter_value = prefs.getInt("chapter_value", 0);
 			if(chapter_value + 2 > chapterCount){
 				book_value++;
 				chapter_value = 0;
 			}else{
 				chapter_value++;
 			}
 
 			SharedPreferences.Editor editor = prefs.edit();
 			editor.putInt("book_value", book_value);
 			editor.putInt("chapter_value", chapter_value);
 			editor.putInt("verse_value", 0);
 			editor.commit();
 		} catch (Exception e) {}
 		displayChapterText();
 	}
 	
 	private void openPreviousChapter(ImageButton b){
 		SharedPreferences prefs = this.getActivity().getSharedPreferences(
 				"edu.southern", Context.MODE_PRIVATE);
 		int book_value = prefs.getInt("book_value", 0);
 		int chapter_value = prefs.getInt("chapter_value", 0);
 		if(chapter_value < 1){
 			book_value--;
 			try {
 				chapter_value = bibleHelper.getChapterCount(book_value) - 1;
 			} catch (Exception e) {}
 		}else{
 			chapter_value--;
 		}
 
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putInt("book_value", book_value);
 		editor.putInt("chapter_value", chapter_value);
 		editor.putInt("verse_value", 0);
 		editor.commit();
 		displayChapterText();
 	}
 	
 	/**
 	 * Go through the activity to display the correct action bar layout for this
 	 * fragment Initiallize the button to display the current book and chapter
 	 * 
 	 * @param book
 	 *            Current book name
 	 * @param chapter
 	 *            Current chapter number
 	 */
 	public void initializeActionBar(String book, int chapter) {
 		HomeScreen home = (HomeScreen) getActivity();
 		home.setActionBarView(R.layout.actionbar_reading);
 		updateActionBar(book, chapter);
 	}
 
 	/**
 	 * Set the text of the action bar button to display the current book and
 	 * chapter
 	 * 
 	 * @param book
 	 *            Current book name
 	 * @param chapter
 	 *            Current chapter number
 	 */
 	public void updateActionBar(String book, int chapter) {
 		// String building
 		String currentLocation = book.concat(" ").concat(
 				Integer.toString(chapter));
 		((Button) getActivity().findViewById(R.id.ActionBarReading))
 				.setText(currentLocation);
 	}
 }
