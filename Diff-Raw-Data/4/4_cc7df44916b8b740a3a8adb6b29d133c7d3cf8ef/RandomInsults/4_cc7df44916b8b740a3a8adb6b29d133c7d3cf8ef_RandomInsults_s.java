 package edu.rit.se.agile.randominsultapp;
 
 import java.util.List;
 import java.util.Random;
 
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.rit.se.agile.data.Template;
 import edu.rit.se.agile.data.TemplateDAO;
 import edu.rit.se.agile.data.WordDAO;
 import edu.rit.se.agile.data.WordsTemplate;
 
 public class RandomInsults extends GenericActivity {
 	public static WordDAO wordDAO;
 	public static TemplateDAO templateDAO;
 
 	private Button generateButton;
 	private Button favoriteButton;
 	private TextView insultTextField;
 	private Spinner categorySpinner;
 	private Random rand = new Random();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_random_insults);
 
 		insultTextField = (TextView) findViewById( R.id.insult_display );
 		generateButton = (Button) findViewById(R.id.button_generate);
 		favoriteButton = (Button) findViewById(R.id.button_save_favorite);
 		categorySpinner = (Spinner) findViewById(R.id.category_spinner);
 
 		Cursor categoryCursor = wordDAO.getCategories();
 		
 		categorySpinner.setAdapter(
 				new SimpleCursorAdapter(this, 
 						R.layout.category_list, 
 						categoryCursor, 
 						new String[]{ WordsTemplate.COLUMN_CATEGORY }, 
 						new int[]{ R.id.category_list_entry }, 
 						SimpleCursorAdapter.FLAG_AUTO_REQUERY ));
 
 		generateButton.setOnClickListener( new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				List<Template> temp = templateDAO.getAllTemplates();
 				insultTextField.setText("Some insult.");
 
 				if(temp.size() > 0 ) {
 					int randomTemplate = rand.nextInt(temp.size() -1);
 //					insultTextField.setText(temp.get(randomTemplate).getTemplate());
					insultTextField.setText(temp.get(randomTemplate).fillTemplate(wordDAO));
 				}
 			}
 
 		});
 
 		favoriteButton.setOnClickListener( new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(RandomInsults.this, 
 						"Saved to favorites.", 
 						Toast.LENGTH_LONG).show();
 
 			}
 
 		});
 	}
 }
