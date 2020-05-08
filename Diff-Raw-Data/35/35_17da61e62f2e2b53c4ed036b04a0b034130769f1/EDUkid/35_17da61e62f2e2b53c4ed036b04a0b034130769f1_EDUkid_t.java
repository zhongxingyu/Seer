 package bu.edu.cs673.edukid;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import bu.edu.cs673.edukid.db.Database;
 import bu.edu.cs673.edukid.db.model.Category;
 import bu.edu.cs673.edukid.db.model.UserAccount;
 import bu.edu.cs673.edukid.db.model.category.CategoryType;
 import bu.edu.cs673.edukid.learn.LearnContentView;
 import bu.edu.cs673.edukid.settings.SettingsView;
 import bu.edu.cs673.edukid.settings.utils.MathProblem;
 import bu.edu.cs673.edukid.settings.utils.MathProblemGenerator;
 
 /**
  * The main view of the application. Contains the main buttons to get to the
  * categories.
  * 
  * @author Kevin Graue
  * 
  * @see Category
  * 
  */
 public class EDUkid extends Activity implements OnClickListener {
 
 	public static final String CATEGORY_TYPE = "CategoryType";
 
 	public static final String ITEM_INDEX = "ItemIndex";
 
 	public static final String WORD_INDEX = "WordIndex";
 
 	private TextView timerValue;
 
 	private long startTime = 0L;
 
 	private Handler customHandler = new Handler();
 
 	long timeInMilliseconds = 0L;
 
 	long timeSwapBuff = 0L;
 
 	long updatedTime = 0L;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edukid);
 		final Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			public void run() {
 				AlertDialog.Builder alert = new AlertDialog.Builder(EDUkid.this);
 				alert.setTitle("Timed OUT! Go to bed");
 				alert.setPositiveButton("Ok",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO
 
 							}
 						});
 				alert.create();
 				alert.show();
 			}
 		}, 180000);
 		timerValue = (TextView) findViewById(R.id.timerValue);
 
 		startTime = SystemClock.uptimeMillis();
 		updateTimerThread.run();
 
 		setupCategoryButtons();
 		welcomeUserBack(true);
 	}
 
 	private Runnable updateTimerThread = new Runnable() {
 
 		public void run() {
 
 			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
 
 			updatedTime = timeSwapBuff + timeInMilliseconds;
 
 			int secs = (int) (updatedTime / 1000);
 
 			int mins = secs / 60;
 
 			secs = secs % 60;
 
 			int milliseconds = (int) (updatedTime % 1000);
 
 			timerValue.setText("" + mins + ":"
 
 			+ String.format("%02d", secs) + ":"
 
 			+ String.format("%03d", milliseconds));
 
 			customHandler.postDelayed(this, 0);
 
 		}
 
 	};
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onClick(View view) {
 		CategoryType categoryType = Database.getInstance(this).getCategories()
 				.get(view.getId());
 
 		Intent intent = new Intent(this, LearnContentView.class);
 		intent.putExtra(CATEGORY_TYPE, categoryType);
 		startActivity(intent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 		welcomeUserBack(false);
 	}
 
 	/**
 	 * Populates the 4 default category buttons along with any user added
 	 * categories from the database.
 	 */
 	private void setupCategoryButtons() {
 		LinearLayout categoryLayout = (LinearLayout) findViewById(R.id.categoryLinearLayout);
 
 		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		layoutParams.width = 275;
 		layoutParams.height = 275;
 		layoutParams.topMargin = 10;
 		layoutParams.rightMargin = 10;
 		layoutParams.bottomMargin = 10;
 		layoutParams.leftMargin = 10;
 
 		List<CategoryType> categories = Database.getInstance(this)
 				.getCategories();
 
 		for (int i = 0; i < categories.size(); i++) {
 			CategoryType category = categories.get(i);
 			ImageButton categoryButton = new ImageButton(this);
 			categoryButton.setId(i);
 			categoryButton.setLayoutParams(layoutParams);
 			categoryButton.setBackground(category.getCategoryImage(this));
 			categoryButton.setOnClickListener(this);
 			categoryLayout.addView(categoryButton);
 		}
 	}
 
 	/**
 	 * Welcomes the user back upon application startup (if one exists).
 	 */
 	private void welcomeUserBack(boolean toast) {
 		Database database = Database.getInstance(this);
 		List<UserAccount> userAccounts = database.getUserAccounts();
 
 		// There should only be 1 user account
 		if (userAccounts.size() == 1) {
 			UserAccount userAccount = userAccounts.get(0);
 
 			TextView userNameTextView = (TextView) findViewById(R.id.mainPageUserName);
 			userNameTextView.setText("Hi " + userAccount.getUserName());
 
 			// Say hello
 			if (toast) {
 				Toast.makeText(
 						this,
 						"Hello " + userAccount.getUserName()
 								+ ", Welcome Back!", Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 
 	/**
 	 * Callback for the settings button. Opens the settings view.
 	 * 
 	 * @param view
 	 *            the view used in the callback.
 	 */
 	public void onSettingsClick(View view) {
 
 		final MathProblem mathProblem = MathProblemGenerator
 				.generateMathProblem();
 
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Please answer this question to access the settings:");
 		alert.setMessage(mathProblem.getQuestion());
 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Submit",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						int userAnswer = Integer.MAX_VALUE;
 
 						try {
 							userAnswer = Integer.parseInt(input.getText()
 									.toString());
 						} catch (Exception e) {
 							showSettingsToast();
 							return;
 						}
 						if (userAnswer == mathProblem.getAnswer()) {
 							startActivity(new Intent(EDUkid.this,
 									SettingsView.class));
 						} else {
 							showSettingsToast();
 						}
 					}
 				});
 
 		alert.setNegativeButton("Cancel", null);
 
 		alert.create();
 		alert.show();
 	}
 
 	/**
 	 * Shows a message to the user to indicate that the math problem was
 	 * answered incorrectly and they will not be able to access the settings.
 	 * 
 	 * @see MathProblemGenerator
 	 * @see MathProblem
 	 */
 	private void showSettingsToast() {
 		Toast.makeText(this, "Incorrect answer. Please try again.",
 				Toast.LENGTH_LONG).show();
 	}
 }
