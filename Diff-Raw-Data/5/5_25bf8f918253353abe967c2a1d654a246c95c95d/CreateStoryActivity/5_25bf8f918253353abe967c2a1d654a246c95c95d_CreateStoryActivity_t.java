 
 package ualberta.g12.adventurecreator;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 /**
  * Activity when an author creates a new story. Will create a new story entry
  * and save the title and author strings. Will take the user to corresponding
  * activities upon button clicks.
  */
 public class CreateStoryActivity extends Activity {
     private Story story;
     private StoryList storyList;
     private StoryListController storyListController;
     private StoryController storyController;
     private int storyPos;
     private Button createButton;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_create_story);
 
         createButton = (Button) findViewById(R.id.editTextSave);
 
         storyListController = AdventureCreator.getStoryListController();
         storyController = AdventureCreator.getStoryController();
 
         // obtain the intent
         Intent editActIntent = getIntent();
         storyPos = (Integer) editActIntent.getSerializableExtra("StoryPos");
 
         setListeners();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         storyList = AdventureCreator.getStoryList();
         story = storyList.getAllStories().get(storyPos);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // TODO Auto-generated method stub
         return super.onOptionsItemSelected(item);
     }
 
     private void setListeners() {
 
         createButton.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
 
                 // get the editTexts!
                 EditText title = (EditText) findViewById(R.id.editStoryTitle);
                 EditText author = (EditText) findViewById(R.id.editStoryAuthor);
 
                 // create a new story (might want to use a controller here
                 // instead!)
                 // story.setStoryTitle(title.getText().toString());
                 // story.setAuthor(author.getText().toString());
 
                 storyController.setTitle(story, title.getText().toString());
                 storyController.setAuthor(story, author.getText().toString());
 
                 // save the new story
                 saveStory();
 
                 finish();
             }
         });
     }
 
     private void saveStory() {
        storyListController.setStory(story, storyPos);
         storyListController.saveOfflineStories(storyList);
     }
 }
