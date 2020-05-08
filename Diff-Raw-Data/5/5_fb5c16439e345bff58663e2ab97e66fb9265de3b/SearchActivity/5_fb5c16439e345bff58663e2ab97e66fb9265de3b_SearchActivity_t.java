 package com.sunshine;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.sunshine.Record.Header;
 import com.sunshine.Record.Question;
 import com.sunshine.Record.Section;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 //Activity to search for text
 //in the QnAs.
 public class SearchActivity extends Activity {
 
 	private final static int MAX_RESULTS = 15;
 	private final static String RESERVED = "b,i,a,ul,ol,li,br";
 	private CheckBox checkBoxQuestions, checkBoxAnswers;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.search_activity);
 
 		((LinearLayout)findViewById(R.id.linearLayoutMain)).setBackgroundColor(
 				MenuActivity.BACKGROUND_DARK);
 
 		((Button)findViewById(R.id.buttonSearch)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				search();
 			}
 		});
 
 		checkBoxQuestions = (CheckBox)findViewById(R.id.checkBoxQuestions);
 		checkBoxAnswers = (CheckBox)findViewById(R.id.checkBoxAnswers);
 	}
 
 	private void search() {
 		//Do we search through the question text? The answer text?
 		boolean searchQs = checkBoxQuestions.isChecked();
 		boolean searchAs = checkBoxAnswers.isChecked();
 
 		if (!searchQs && !searchAs) {
 			return;
 		}
 
 		String originalQuery = ((EditText)findViewById(R.id.editTextSearch)).getText().toString();
 		
 		//Lowercase for consistency and HTML encoded for safety
 		String query = originalQuery.toLowerCase(Locale.US);
 		query = TextUtils.htmlEncode(query);
 
 		if (query.length() == 0) return;
 
 		try {
 
 			String[] records = RecordCache.RECORDS;
 
 			//Let's build a new header (QnA) out of the resuts
 			String title = "Search: '" + originalQuery + "'";
 			Header h = new Header(title);
 
 			//Split the query into words
 			String[] words = query.split(" "); 
 			if (words.length < 1) return;
 			String patternS = "";
 			for (int i = 0; i < words.length; i++) {
 				String word = words[i];
 				//Don't add "reserved" words, like html elements
 				if (RESERVED.indexOf(word) == -1) {
					//search for any of the words
					if (patternS.length() > 0) patternS += "|";
 					//The word should be by itself
 					patternS += "\\b" + Pattern.quote(word) + "\\b";
 				}
 			}
 			
 			if (patternS.isEmpty()) return;
 			
 			//make sure we're searching case insensitive
 			Pattern pattern = Pattern.compile(patternS, Pattern.CASE_INSENSITIVE);
 
 			//Keep track of how good a result each one is
 			final HashMap<Question, Integer> map = new HashMap<Record.Question, Integer>();
 
 			//For every question we have...
 			for (String recordS : records) {
 				Record record = RecordCache.parseRector(recordS, getAssets());
 				for (Section section : record) {
 					for (Header header : section) {
 						for (Question question : header) {
 							//give it a weight
 							int count = 0;
 							if (searchQs) {
 								//finding the text in the question itself it weighted x3
 								count += searchWeight(pattern, question.question, query) * 3;
 							}
 							if (searchAs) {
 								count += searchWeight(pattern, question.answer, query);
 							}
 							//If we found anything, add it
 							if (count > 0) {
 								h.add(question);
 								map.put(question, count);
 							}
 						}
 					}
 				}
 			}
 
 
 			//Sort the headers
 			Collections.sort(h.questions, new Comparator<Question>() {
 				@Override
 				public int compare(Question lhs, Question rhs) {
 					return map.get(rhs) - map.get(lhs);
 				}
 			});
 
 			//Remove any more results than the max number
 			while (h.size() > MAX_RESULTS) {
 				h.questions.removeLast();
 			}
 
 			//If we had any results, show a new QnA with the results
 			if (h.size() > 0) {
 				Intent intent = new Intent(this, QnAActivity.class);
 				intent.putExtra("header", h);
 				intent.putExtra("pattern", patternS);
 				startActivity(intent);
 				return;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		//Otherwise, we have no results
 		new AlertDialog.Builder(this)
 		.setTitle("No Results")
 		.setMessage("No results were found for '" + originalQuery + "'.")
 		.setPositiveButton("Ok", null)
 		.show();
 	}
 
 	//tells how closely text matches a patter/query
 	public int searchWeight(Pattern pattern, String text, String query) {
 		int count = 0;
 		Matcher matcherAs = pattern.matcher(text);
 		while (matcherAs.find()) {
 			//add one for every match to the pattern (any of the words)
 			count++;
 		}
 		//If we had multiple words...
 		if (query.indexOf(" ") > -1) {
 			String qLower = text.toLowerCase(Locale.US);
 			int index = qLower.indexOf(query);
 			while (index >= 0) {
 				//add 10 for every exact match of the query
 				count += 10;
 				index += query.length();
 				index = qLower.indexOf(query, index);
 			}
 		}
 		return count;
 	}
 }
