 package com.example.zonedhobbitsportfolio;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.text.Html;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ProfileActivity extends Activity {
 
 	Person p;
 	
 	TextView profileText;
     ImageView funImg;
     TextView quote;
     TextView image_funquote_text;
     TextView bio;
     TextView contactHeader;
     TextView contact;
     TextView projectListHeader;
     
    GridView listSelectedWork;
 	static Project[] projects;
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_profile);
 		
 		//	Receive & unpack package
 		Intent i = getIntent();
 		i.getExtras();
 		//p = (Person) i.getParcelableExtra("pOi");
 
 		p = MainActivity.returnPerson(i.getIntExtra("pos", 0));
 		projects = new Project[p.getProjects().length];
 		
 		for(int iterator = 0; iterator < projects.length; iterator++) {
 			projects[iterator] = p.projects[iterator];
 		}
 		
 		//	Grab view resources
         profileText = (TextView) findViewById(R.id.text_header_profile);
         
         funImg = (ImageView) findViewById(R.id.image_funimg_profile);
         quote = (TextView) findViewById(R.id.image_funtext_profile);
         image_funquote_text = (TextView) findViewById(R.id.image_funqoute_profile);
         
         bio = (TextView) findViewById(R.id.text_desc_profile);
         
         contactHeader = (TextView) findViewById(R.id.text_socialheader_profile);
         contact = (TextView) findViewById(R.id.text_social_profile);
         
         projectListHeader = (TextView) findViewById(R.id.text_projectlistheader_profile);
        listSelectedWork = (GridView) findViewById(R.id.list_selectedwork);
         
         //	Set up list items
         
         listSelectedWork.setOnItemClickListener(new OnItemClickListener() {
             
         	@Override
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         	
         		moveToProject(position, view);
         		
         	}
         });
         
         CustomAdapter test1 = new CustomAdapter(this, listSelectedWork.getId(), projects);  
         listSelectedWork.setAdapter(test1);
 		
         //	Dump in vars
         profileText.setText(p.getName());
         
         funImg.setImageBitmap(p.getFun_img());
         quote.setText(p.getQuote().toUpperCase());
         image_funquote_text.setText("&ndash; "+p.getNickName().toUpperCase());
         
         bio.setText(p.getBio());
         
         //	Let's build us a proper lil' contact string
         String contactString = "<b>E-mail</b>\t\t"+p.getEmail()+"<br/><b>Twitter</b>\t\t"+p.getTwitter()+"<br/><b>Phone</b>\t\t"+p.getPhone()+"<br/><b>URL</b>\t\t\t\t"+p.getUrl()+"<br/><b>GitHub</b>\t\t"+p.getGithub();
         
         contact.setText(Html.fromHtml(contactString));
         
 		//	Test fonts        		
         Typeface EdmondBold = Typeface.createFromAsset(getAssets(),"fonts/Edmondsans-Bold.otf");
         Typeface EdmondMed = Typeface.createFromAsset(getAssets(), "fonts/Edmondsans-Medium.otf");
         Typeface Edmond = Typeface.createFromAsset(getAssets(), "fonts/Edmondsans-Regular.otf");
         
         Typeface PTSans = Typeface.createFromAsset(getAssets(), "fonts/PTSans.ttc");
         
         profileText.setTypeface(EdmondBold);
         contactHeader.setTypeface(EdmondBold);
         projectListHeader.setTypeface(EdmondBold);
         
         quote.setTypeface(EdmondMed);
         
         bio.setTypeface(PTSans);
         contact.setTypeface(PTSans);
         
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.profile, menu);
 		return true;
 	}
 	
 	public void moveToProject(int position, View v){
     	
     	Intent i = new Intent(this, ProjectActivity.class);
 
     	//i.putExtra("Person", (Person) test.getItemAtPosition(position));
     	
     	i.putExtra("pos", position);
     	
     	startActivity(i); 
     }
 	
 	public static Project returnProject(int pos){
 		return projects[pos];
 	}
 
 }
