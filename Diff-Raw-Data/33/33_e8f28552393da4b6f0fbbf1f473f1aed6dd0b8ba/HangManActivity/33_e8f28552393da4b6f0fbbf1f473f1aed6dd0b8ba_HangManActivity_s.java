 // HangManActivity.java --- 
 // 
 // Filename: HangManActivity.java
 // Description: 
 // Author: Andrew Smith
 // Maintainer: Team18
 // Created: Sat Mar 24 18:31:54 2012 (+0000)
 // Version: 1
// Last-Updated: Tue Apr 24 05:33:04 2012 (+0100)
//           By: Sevki Hasirci
//     Update #: 3
 // URL:
 // Keywords: 
 // Compatibility: 
 // 
 // 
 
 // Commentary: 
 // TODO: Move the facebook and server updates to gamescommon.
 // 
 // 
 // 
 
 // Change Log:
 // 24-Apr-2012    Sevki Hasirci  
 //    Last-Updated: Tue Apr 24 05:33:04 2012 (+0100) #3 (Sevki Hasirci)
 //    changed the game ending dialog to be handled by GamesCommon for
 //    compliance with the story mode.
 // 16-Apr-2012    Sevki Hasirci  
 //    Last-Updated: Mon Apr 16 23:00:17 2012 (+0100) #2 (Sevki Hasirci)
 //    updated the game to work with the new win story and facebook postings.
 // 24-Mar-2012    Sevki Hasirci  
 //    Last-Updated: Sat Mar 24 18:32:37 2012 (+0000) #1 (Sevki Hasirci)
 //    Change the button setup
 // 
 // 
 // 
 // 
 package team18.cs.ncl.ac.uk;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import com.facebook.android.AsyncFacebookRunner;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.view.View.OnClickListener;
 
 public class HangManActivity extends Activity implements OnClickListener
 {
     public  static TextView word;
     public  String ranWord;
     public ImageView drawing;
     String FILENAME = "AndroidSSO_data";
     int lives = 7; //Record the number of lives remaining until the hangman image is complete (lives = 0)
     int currentImg = R.drawable.hang1; //The current hangman image refresh
     public static DictionaryTools t = new DictionaryTools();
     int NextChapter = -1;
     /* Set the length of the word dash array to the length of the word. This will hold the dashes and correctly guessed
     letters. */
     protected String wordDash[];
     protected String wordArr[];
 	protected WordPair wordDefPair;
     protected String dashCollection = ""; //The collection of dashes in the word, e.g. "_ _ _ _"
     protected String dashString = ""; //The concatenated collection of dashes
 
     protected int correctGuess = 0; //Used to check against the word length. If correct guesses = word length, then the word has been discovered
     private OnClickListener benjiWhisperer = new OnClickListener() {
 	    @Override
 		public void onClick(View v) {
 		// TODO Auto-generated method stub
 		Button thisButton = (Button) v;
 		String s =thisButton.getText().toString();
 		
 		play(s.toLowerCase());
 	    }
 	};
 	    
 
 	    
     /** Called when the activity is first created. */
     @Override
 	public void onCreate(Bundle savedInstanceState)
     {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.hangman);
 	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 	//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 	Bundle b = getIntent().getExtras();
 	if(b!=null)
 	{
 		int value = b.getInt("nextChapter");
 	}
 	t.ReadDictionary();
 	
 		
 	setWidgetReferences();
 		
 	try {
 	    setup(); //Start the games initial setup
 	   
 	} catch (IOException e) {
 	    e.printStackTrace();
 	} catch (InterruptedException e) {
 	    e.printStackTrace();
 	}
 	buttonSetup(); //Set the buttons up along with their actions
 	
 	
     }
     @Override
 	protected void onStart(){
 	super.onStart();
 	
     }
    public void ShowReminder()
    {
 	   GamesCommon.displayWordHint(wordDefPair, this);
 
 		
    }
     public void setup() throws IOException, InterruptedException
     {		
 		
 	TextView word = (TextView) findViewById(R.id.textView1);
 	wordDefPair = getWord();
 	ranWord = wordDefPair.Word; //Get the random word from the array.
 
 	
 	ShowReminder();
 	//Set up the string arrays for the random word's dash representation
 	wordArr = new String[ranWord.length()];
 	wordDash = new String[ranWord.length()];

 	Button rmndBtn = (Button) findViewById(R.id.rmndBtn);
 	rmndBtn.setOnClickListener(new OnClickListener() {
 		
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			ShowReminder();
 		}
 	});
 	
 	//Initialise all of the wordDash[] elements to '_'. 
 	for (int i = 0; i < wordDash.length; i++) 
 	    {
 		wordDash[i]="_";
 		dashCollection += wordDash[i] + " ";
 	    }
 
 	//Set the words text to the word's initial dashes
 	word.setText(dashCollection);
 
 		
 	String wordForSub = ranWord; //String to allow the random word to be split down to an individual character
 
 	//Assign every element of the string array with a different letter of the random word
 	for(int i = 0; i<ranWord.length(); i++)
 	    {
 		wordArr[i] = wordForSub.substring(i,i+1);     
 	    }
 		
 	//Turn back into the dash string from the character array and set it as the in-game text		
 	for (int i = 0; i < wordDash.length; i++) 
 	    {
 		dashString += wordDash[i] + " ";
 	    }
 	word.setText(dashString);
 		 
     }
 	
     public void play(String letter)
     {
     	
 	
 	//Refresh the text box containing the letters/dashes.
 	dashString = "";
 	for (int i = 0; i < wordDash.length; i++) 
 	    {
 		dashString += wordDash[i] + " ";
 	    }
 	TextView word = (TextView) findViewById(R.id.textView1);
 	word.setText(dashString);
 			
 	dashString = "";//Reset the collection of letters/dashes to stop it from concatenating.
 
 	boolean found = false; //Initialise the 'has the letter been found' to false.
 
 	//If there the letter guess exists in the actual word, replace the dash with the correct. Increment the correctGuess
 	//by 1. This is used to determine if the user has won the game or not (if the correctGuess = the size of the word array,
 	//then all of the letters have been found.
 	for(int i = 0; i<ranWord.length(); i++)
 	    {
		if(letter.equals(wordArr[i]))
 		    {
 			wordDash[i] = letter;
 			found = true;
 			correctGuess++;
 		    }
 	    } 
 
 	//If the letter hasn't been found in the word, then it obviously doesn't exist. Therefore decrease the guessCount by 1
 	//and repaint the stick-figure (add a limb). When the guess count has counted down to 0, then there are no more limbs
 	//left to add to the stick figure, therefore the game has been lost.
 	if(found == false)
 	    {
 		lives --;
 		currentImg ++;
 		setHangImg();
 	    }
 
 	found = false; //Reset the 'found?' to false even if a letter has not been found.
 
 	youLost();	
 
 	youWon(word);
 			
 	dashString = "";
 	for (int i = 0; i < wordDash.length; i++)  
 	    {
 		dashString += wordDash[i] + " ";
 	    }
 			
 	word.setText(dashString);
     }
 
     private void youWon(TextView word) {
 	//If the entire word has been found
 	if(correctGuess == ranWord.length())
 	    {
 		//Update the text field displaying the word/dashes to the completed word.
 		for (int i = 0; i < wordDash.length; i++) 
 		    {
 			dashString += wordDash[i] + " "; 
 		    }
 		word.setText(dashString); 
 			try {
 			DogBoneServer.sendUserScoreJson(FbRelatedStuff.uid,1,wordDefPair.WordId, 1,-1);
 			 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 			GamesCommon.displayEndGame(GameStatus.Won,  this);
 		//displayEndGame(getString(R.string.WonTxt));
 	
 	    }
     }
 
     
 
     private void youLost() {
 	//If the guessCount has decreased to zero then there are no limbs left to be added to the stickman, therefore the game is
 	//Lost.
 	if(lives == 0)
 	    {
 		GamesCommon.displayEndGame(GameStatus.Lost,  this);
 
 		try {
 			DogBoneServer.sendUserScoreJson(FbRelatedStuff.uid,1,wordDefPair.WordId, -1,-1);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    }
     }
 	
     public void setHangImg()
     {
 	ImageView drawing = (ImageView)findViewById(R.id.imageView1);
    
 	drawing.setImageResource(currentImg);
     }
 	
     public  WordPair getWord() throws FileNotFoundException, InterruptedException
     {
     	
     	WordPair p=	t.getRandom();
     	return p;
     } 	
 	 	
     public void buttonSetup()
     {
     	Display display = getWindowManager().getDefaultDisplay();
     	int SDK_INT = android.os.Build.VERSION.SDK_INT;
     	int width = display.getWidth();
     	
     	LinearLayout benjaminContainer = (LinearLayout) findViewById(R.id.BenjaminContainer);
 	width -=202;
 	//AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	//builder.setMessage(SDK_INT+"-" +width);
 	//AlertDialog alert = builder.create();
 	//alert.show();
 		
 		
 	
 	
 	char[] letters= "abcdefghijklmn�opqrstuvwxyz".toCharArray();
 
 	LinearLayout lt = null;
 	int totalWidth=0;
 	for(int i = 0; i < letters.length; i++)
 	    {
 		
 		
 		int setWidth= SDK_INT<11? 50:70;
 	
 		if(i==0 || (totalWidth>width))
 		    {
 			lt = new LinearLayout(this);
 			benjaminContainer.addView(lt,new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT));
 			totalWidth=0;
 		    }
 		Button benjamin = new Button(this);
 		benjamin.setId(i+1); // ID of zero will not work
 		benjamin.setText(letters[i]+"");
 		benjamin.setWidth(setWidth);
 		benjamin.setPadding(10, 0, 10, 0);
 
 		benjamin.setOnClickListener(benjiWhisperer);
 	    
 	  
 		// New layout params for each button
 		lt.addView(benjamin);
 		totalWidth+= SDK_INT<11? setWidth+20:setWidth+40;
 	    }
     }
 
     private void setWidgetReferences() 
     {
 		
     }
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
 
 
