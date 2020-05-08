 package edu.rit.se.agile.randominsultapp;
 
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 import edu.rit.se.agile.data.Word;
 import edu.rit.se.agile.data.WordType;
 
 public class AddWord extends GenericActivity {
 	private Button addWordButton;
 	private EditText textField;
 	private Spinner spinner;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_word);
 		
 		addWordButton = (Button) findViewById(R.id.add_word_button);
 		textField = (EditText) findViewById(R.id.add_word_text);
 		spinner = (Spinner) findViewById(R.id.add_word_spinner);
 		
 		addWordButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String text = textField.getText().toString();
 				String spinnerText = spinner.getSelectedItem().toString().toLowerCase();
 				String wordType = "";
 				
 				if (spinnerText.equals("noun")) {
 					wordType = WordType.NOUN.val();
 				} else if (spinnerText.equals("verb")) {
 					wordType = WordType.VERB.val();
 				} else if (spinnerText.equals("adjective")) {
 					wordType = WordType.ADJECTIVE.val();
 				} else if (spinnerText.equals("adverb")) {
 					wordType = WordType.ADVERB.val();
 				}
 				
				Word newWord = wordDAO.createWord(spinnerText, text, "none");
 				Toast.makeText(v.getContext(), 
 						newWord.getWord() + " was added.",
 						Toast.LENGTH_SHORT).show();
 				
 				textField.setText("");
 			}
 		});
 	}
 }
