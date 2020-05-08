 package iidenki.android;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 
 import vocab.DynamicTest;
 import vocab.LatestTest;
 import vocab.SimpleTest;
 import vocab.Tester;
 import vocab.Word;
 import iidenki.android.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WordTester extends Activity implements OnClickListener{
 	
 	private Word currentword;
 	private Tester<Word> test;
 	private int correct;
 	private int total;
 	private boolean wc;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    setContentView(R.layout.vocabtest);
 	    correct = 0;
 	    total = 0;
 	    
 	    String fil = getIntent().getStringExtra("file");
 	    String testtype = getIntent().getStringExtra("test type");
 		String wordclass = getIntent().getStringExtra("test word class");
 		String reset = getIntent().getStringExtra("reset list");
 		String num = getIntent().getStringExtra("number");
 		wc = wordclass.equals("true") ? true : false;
 	    
 	    Button okbutton = (Button) findViewById(R.id.button1);
     	okbutton.setOnClickListener(this);
     	
     	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.word_classes, android.R.layout.simple_spinner_item);
     	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     	Spinner s = (Spinner) findViewById(R.id.spinner1);
     	s.setAdapter(adapter);
     	
     	File readfrom = new File(fil);
     	ArrayList<Word> temp = null;
     	
     	try{
 			ObjectInputStream in=new ObjectInputStream(new FileInputStream(readfrom));
 			temp =(ArrayList<Word>)in.readObject();
 		}catch(Exception e){
 			Toast.makeText(getApplicationContext(), "File read error", Toast.LENGTH_SHORT).show();
 			e.printStackTrace();
 			finish();
 		}
     	
    	if (reset.equals("true")){
    		resetList(temp);
     	}
    	
    	if (testtype.equals("Test all words")){
    		test = new SimpleTest<Word>(temp);
    	}
    	if (testtype.equals("Test the most difficult words")){
    		test = new DynamicTest<Word>(temp, num);
    	}
    	if (testtype.equals("Test the latest words")){
    		test = new LatestTest<Word>(temp, num);
    	}
    	
    	chooseWord();
 	}
 
 	private void chooseWord() {
 		TextView engword = (TextView) findViewById(R.id.textView2);
 		EditText textbox = (EditText) findViewById(R.id.editText1);
 		currentword = test.getNext();
 		if (currentword == null){
 			Toast.makeText(getApplicationContext(), "end of quiz!", Toast.LENGTH_SHORT).show();
 			Toast.makeText(getApplicationContext(), correct + " correct out of " + total, Toast.LENGTH_SHORT).show();
 			finish();
 		}else{
 			engword.setText(currentword.toString());
 		}
 		textbox.setText("");
 	}
 
 	/**
 	 * Check the answer
 	 */
 	public void onClick(View arg0) {
 		EditText textbox = (EditText) findViewById(R.id.editText1);
 		String answer = textbox.getText().toString();
 		
 		Spinner spin = (Spinner) findViewById(R.id.spinner1);
 		String wordclass = (String) spin.getSelectedItem();
 		
 		if (currentword.check(answer, wordclass, wc)){
 			Toast.makeText(getApplicationContext(), "correct!", Toast.LENGTH_SHORT).show();
 			test.success();
 			currentword.right++;
 			correct++;
 			total++;
 		}else{
 			Toast.makeText(getApplicationContext(), "wrong!", Toast.LENGTH_SHORT).show();
 			Toast.makeText(getApplicationContext(), "correct answer: " + currentword.getRomaji(), Toast.LENGTH_SHORT).show();
 			Toast.makeText(getApplicationContext(), "word class: " + currentword.type, Toast.LENGTH_SHORT).show();
 			test.fail();
 			currentword.wrong++;
 			total++;
 		}
 		updateStats();
 		
 		chooseWord();
 		
 	}
 	
 	private void updateStats() {
 		TextView stats = (TextView) findViewById(R.id.textView3);
 		stats.setText(correct + " correct out of " + total);
 	}
 
 	/**
 	 * Reset score
 	 *
 	 * @param newlist the list to reset
 	 */
 	private void resetList(ArrayList<Word> newlist) {
 		for (Object w : newlist.toArray()){
 			((Word)w).right = 0;
 			((Word)w).wrong = 0;
 		}
 		Toast.makeText(getApplicationContext(), "scores have been reset", Toast.LENGTH_SHORT).show();
 	}
 }
