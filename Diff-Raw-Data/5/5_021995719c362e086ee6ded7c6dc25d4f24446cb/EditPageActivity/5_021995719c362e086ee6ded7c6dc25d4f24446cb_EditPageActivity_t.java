 /*
  * Copyright (C) <2013>  <Justin Hoy>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package c301.AdventureBook;
 
 import java.io.Serializable;
 
 import c301.AdventureBook.TakePhotoActivity;
 
 import com.example.adventurebook.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.AdapterView.OnItemClickListener;
 
 /**
  * The edit page activity allows the author to edit the contents of a page within a story.
  * 
  * @author Terence
  *
  */
 public class EditPageActivity extends Activity implements Serializable{
  
 	private static final int EDIT_OPTION = 0;
 	private EditText editStoryDescription;
 	private Button mButtonCreateOption;
 	private Button mButtonSavePage;
 	private CoverFlow coverFlow;
	int PHOTO_ACTIVITY_REQUEST = 1001;
 	//int my_current_position = 0;
 	ImageAdapter coverImageAdapter;
 
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(com.example.adventurebook.R.layout.edit_page);
 
 		editStoryDescription = (EditText)findViewById(com.example.adventurebook.R.id.editStoryDescription);
 		mButtonCreateOption = (Button) findViewById(R.id.new_option);
 		mButtonSavePage = (Button) findViewById(R.id.save_page);
 
 		coverFlow  = (CoverFlow) findViewById(com.example.adventurebook.R.id.gallery1);
 		coverFlow.setAdapter(new ImageAdapter(this));
 		coverImageAdapter =  new ImageAdapter(this);
 
 		//coverImageAdapter.createReflectedImages();
 
 		coverFlow.setAdapter(coverImageAdapter);
 
 		coverFlow.setSpacing(25);
 		coverFlow.setSelection(2, true);
 		coverFlow.setAnimationDuration(1000);
         coverFlow.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long arg3)
             {
                 // TODO Auto-generated method stub
             	Intent intent = new Intent(view.getContext(),TakePhotoActivity.class);
             	//my_current_position = position;
             	startActivityForResult(intent, PHOTO_ACTIVITY_REQUEST);
             	
            });
        }
 
 		
 		mButtonCreateOption.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				Intent intent = new Intent(EditPageActivity.this, EditOptionActivity.class);
 				startActivityForResult(intent, EDIT_OPTION);
 			}
 		});
 
 		mButtonSavePage.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				finish();
 			}
 		});
 	}
 
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == EDIT_OPTION) {
 			if (resultCode == RESULT_OK) {
 				// Retrieve the option description that the user entered in EditOptionActivity
 				// after they click SaveOption
 				String optionDescription = data.getExtras().getString("option_description");
 				editStoryDescription.setText(optionDescription);
 			}
 		}else if(requestCode == PHOTO_ACTIVITY_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				//String show_path;
 				//show_path = data.getStringExtra("path");
 				//coverImageAdapter.editAdapter(show_path, my_current_position);
 			}
 		}
 	}
 	
 
 
 }
