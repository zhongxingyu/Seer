 package se.chalmers.dat255.sleepfighter.activity;
 
 import se.chalmers.dat255.sleepfighter.challenge.Challenge;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengeFactory;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengeType;
 import se.chalmers.dat255.sleepfighter.challenge.SimpleMathChallenge;
 import se.chalmers.dat255.sleepfighter.challenge.TestChallenge;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Toast;
 
 /**
  * An activity for different types of challenges.<br/>
  * Started with a bundled {@link ChallengeType} with name defined by
  * {@code BUNDLE_CHALLENGE_TYPE}. The calling activity can check if the user has
  * completed the challenge by starting this using {@code startActivityForResult}
  * and checking that the {@code resultCode} is {@code Activity.RESULT_OK}.
  */
 public class ChallengeActivity extends Activity {
 
 	public static final String BUNDLE_CHALLENGE_TYPE = "bundle_challenge_type";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Object bundled = getIntent().getSerializableExtra(BUNDLE_CHALLENGE_TYPE);
 		if (!(bundled instanceof ChallengeType)) {
 			throw new IllegalArgumentException("No type sent");
 		}
 		ChallengeType type = (ChallengeType) bundled;
 		Challenge challenge = ChallengeFactory.getChallenge(type);
 		challenge.start(this);

		// TODO do something else to get an instance of Challenge and call
		// challenge.start(this)
		Challenge challenge1 = new SimpleMathChallenge();
		challenge1.start(this);
 	}
 
 	public void complete() {
 		Toast.makeText(this, "DEBUG: Completed challenge", Toast.LENGTH_SHORT).show();
 		setResult(Activity.RESULT_OK);
 		finish();
 	}
 
 	public void fail() {
 		setResult(Activity.RESULT_CANCELED);
 		finish();
 	}
 }
