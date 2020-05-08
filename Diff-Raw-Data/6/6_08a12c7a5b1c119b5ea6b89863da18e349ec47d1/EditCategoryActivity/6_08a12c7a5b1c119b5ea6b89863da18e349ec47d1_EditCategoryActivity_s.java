 package es.udc.santiago.view.categories;
 
 import java.sql.SQLException;
 
import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 
 import es.udc.santiago.R;
 import es.udc.santiago.model.backend.DatabaseHelper;
 import es.udc.santiago.model.exceptions.DuplicateEntryException;
 import es.udc.santiago.model.exceptions.EntryNotFoundException;
 import es.udc.santiago.model.facade.Category;
 import es.udc.santiago.model.facade.CategoryService;
 
 /**
  * 
  * @author Santiago Munín González
  * 
  */
 public class EditCategoryActivity extends OrmLiteBaseActivity<DatabaseHelper> {
 	TextView id;
 	long givenId;
 	TextView name;
 	EditText newName;
 	Button editButton;
 	CategoryService catServ;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_category);
 		fillViews();
 		editButton = (Button) findViewById(R.id.editCat_button);
 		editButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if (newName != null && newName.getText() != null
 						&& newName.getText().toString().length() > 0) {
 					String temp = newName.getText().toString().trim();
 					while (temp.startsWith(" ") && temp.length() > 0) {
 						temp.substring(1);
 					}
 					if (temp.length() > 0) {
 						Category c = new Category(givenId);
 						c.setName(temp);
 						try {
 							catServ.update(c);
							startActivity(new Intent(getApplicationContext(),
									ManageCategoriesActivity.class));
							// Error control
 						} catch (EntryNotFoundException e) {
 							Toast.makeText(getApplicationContext(),
 									R.string.error_notExists,
 									Toast.LENGTH_SHORT).show();
 						} catch (DuplicateEntryException e) {
 							Toast.makeText(getApplicationContext(),
 									R.string.error_cat_alreadyExists,
 									Toast.LENGTH_SHORT).show();
 						}
 					} else {
 						Toast.makeText(getApplicationContext(),
 								R.string.error_empty, Toast.LENGTH_SHORT)
 								.show();
 					}
 				}
 			}
 		});
 
 	}
 
 	private void fillViews() {
 		id = (TextView) findViewById(R.id.editCat_idValue);
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			givenId = extras.getLong("id");
 		} else {
 			// TODO error screen
 		}
 
 		id.setText(Long.toString(givenId));
 		name = (TextView) findViewById(R.id.editCat_NameValue);
 		try {
 			catServ = new CategoryService(getHelper());
 			try {
 				name.setText(catServ.get(givenId).getName());
 			} catch (EntryNotFoundException e1) {
 				// Should not reach here.
 			}
 			newName = (EditText) findViewById(R.id.editCat_newName);
 		} catch (SQLException e) {
 			// Should not reach here.
 		}
 	}
 }
