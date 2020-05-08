 package no.ntnu.idi.socialhitchhiking;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.client.ClientProtocolException;
 
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.UserRequest;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.map.GetImage;
 import no.ntnu.idi.socialhitchhiking.utility.SocialHitchhikingActivity;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MyAccountMeActivity extends SocialHitchhikingActivity {
 
 	private ImageView picture;
 	private ImageView gender;
 	private EditText age;
 	private EditText phone;
 	private EditText aboutMe;
 	private TextView name;
 	private User user;
 	
 	private String ageString;
 	private String aboutMeString;
 	private String phoneString;
 	
 	private boolean ageChanged;
 	private boolean aboutMeChanged;
 	private boolean phoneChanged;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Getting the user
 		user = getApp().getUser();
 		// Setting the loading layout
 		setContentView(R.layout.main_loading);
 		
 		// Adding image of the driver
 		// Execute the Asynctask: Get image from url and add it to the ImageView
 		new GetImage(picture, this).execute(user.getPictureURL());
 	}
 	@Override
 	public void onBackPressed(){
 		ageChanged = false;
 		aboutMeChanged = false;
 		phoneChanged = false;
 		
 		if(phoneString == null){
 			phoneString = "";
 		}
 		if(aboutMeString == null){
 			aboutMeString = "";
 		}
 		
 		// Checking if a new age is set
 		if(ageString.equals(age.getText().toString())){
 			ageChanged = true;
 		}
 		// Checking if a new phone number is set
 		if(!phoneString.equals(phone.getText().toString())){
 			phoneChanged = true;
 		}
 		// Checking if a new "about me" is set
 		if(!aboutMeString.equals(aboutMe.getText().toString())){
 			aboutMeChanged = true;
 		}
 		// Setting the age
 		boolean isValidAge = true;
 		boolean isEmpty = false;
 		try{
 			Integer.parseInt(age.getText().toString());
 		}catch(NumberFormatException e){
 			if(age.getText().toString().length() > 0){
 				isValidAge = false;
 				Toast.makeText(this, "Age have to be a number!", Toast.LENGTH_LONG).show();
 				return;
 			}else{
 				isEmpty = true;
 			}
 		}
 		if(isEmpty){
 			ageString = "";
 		}else if(isValidAge){
 			ageString = age.getText().toString();
 		}
 		// Setting the phone number
 		boolean isValidNumber = true;
 		isEmpty = false;
 		try{
 			Integer.parseInt(phone.getText().toString());
 		}catch(NumberFormatException e){
 			if(phone.getText().toString().length() > 0){
 				isValidNumber = false;
 				Toast.makeText(this, "Phone number can only consist of numbers! Use 00 extensions instead of +.", Toast.LENGTH_LONG).show();
 				return;
 			}else{
 				isEmpty = true;
 			}
 		}
 		if(isEmpty){
 			phoneString = "";
 		}
 		else if(isValidNumber){
 			phoneString = phone.getText().toString();
 		}
 		// Setting the About me
 		aboutMeString = aboutMe.getText().toString();
 		
 		// Make changes in the user object
 		if(ageChanged){
 			user.setAge(Integer.parseInt(ageString));
 		}
 		if(phoneChanged){
 			user.setPhone(phoneString);
 		}
 		if(aboutMeChanged){
 			user.setAbout(aboutMeString);
 		}
 		// Add the changes to the user object to the database
 		Request userReq = new UserRequest(RequestType.UPDATE_USER, user);
 		try {
 			RequestTask.sendRequest(userReq, getApp());
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		super.onBackPressed();
 	}
 	/**
 	 * Displays the user info in the layout.
 	 * @param result
 	 */
 	public void showProfile(Bitmap result){
 		setContentView(R.layout.my_account_me_);
 		// Initializing views
 		gender = (ImageView) findViewById(R.id.gender);
 		picture = (ImageView)findViewById(R.id.meImage);
 		name = (TextView)findViewById(R.id.meName);
 		age = (EditText)findViewById(R.id.meAge);
 		phone = (EditText)findViewById(R.id.mePhone);
 		aboutMe = (EditText)findViewById(R.id.meAboutMe);
 		
 		// Adding the picture of the user
 		picture.setImageBitmap(result);
 		
 		// Adding the name of the user
 		name.setText(user.getFullName());
 		
 		// Adding the age of the user
 		if(user.getAge() == 0){
 			ageString = "";
 		}else{
 			ageString = Integer.toString(user.getAge());
 		}
 		age.setText(ageString);
 		
 		// Adding the phone number of the user
 		phoneString = user.getPhone();
 		phone.setText(phoneString);
 		
 		// Adding the About Me of the user
 		aboutMeString = user.getAbout();
 		aboutMe.setText(aboutMeString);
 		
 		//Adding Gender to the driver
 	    if(user.getGender().equals("m")){
 	    	Drawable male = getResources().getDrawable(R.drawable.male);
 	    	gender.setImageDrawable(male);
 	    }
 	    else if(user.getGender().equals("f")){
 	    	Drawable female = getResources().getDrawable(R.drawable.female);
 	    	gender.setImageDrawable(female);
 	    }
 	}
 	
 }
