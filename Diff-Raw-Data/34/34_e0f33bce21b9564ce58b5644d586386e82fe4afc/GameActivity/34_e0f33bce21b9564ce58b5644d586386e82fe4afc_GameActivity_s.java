 package android.games.minesweeper;
 
 import java.util.Random;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.Button;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GameActivity extends BaseActivity {
 
 	private String TAG = this.toString();
 
 	private TableLayout mineField;
 
 	private float currentX, currentY = 0;
 	private float oldX, oldY = 0;
 
 	private boolean started = false;
 
 	private ScrollView vScroll;
 	private HorizontalScrollView hScroll;
 
 	private final static int BoxPadding = 8;
 	private final static int BoxWH = 15;
 	private int secondsPassed;
 	private int minutesPassed;
 	private int duration;
 	private Option options;
 	private Globals globals;
 	public int totalRows;
 	public int totalCols;
 	public boolean gameLost;
 
 	//INFO: header controls
 	private ImageView boxImageView;
 	private ProgressBar boxProgressBar;
 
 	private ImageView flagImageView;
 	private ProgressBar flagProgressBar;
 
 
 	private Button startPauseButton;
 	private Button stopButton;
 
 	private Handler timer;
 	private TextView timerText;
 
 
 	//INFO: main
 	public UIBox[][] Boxes;
 
 	private Boolean startPauseButtonIsSelected;
 
 	//INFO: stats
 	private int totalDiscoveredFlagNumber;
 	private int totalDiscoveredValidFlagNumber;
 	private int totalBoxNumber;
 	private int totalOpenBoxNumber;
 	private int totalBombNumber;
 
 	private void setup() {
 		//INFO: global
 		startPauseButtonIsSelected = false;
 
 		//INFO: Header
 		vScroll = (ScrollView) findViewById(R.id.vScroll);
 		hScroll = (HorizontalScrollView) findViewById(R.id.hScroll);
 		timerText = (TextView)findViewById(R.id.Timer);
 
 		startPauseButton = (Button)findViewById(R.id.playPauseButton);
 		stopButton  = (Button)findViewById(R.id.stopButton);
 
 		boxImageView = (ImageView)findViewById(R.id.boxImageView);
 		boxProgressBar = (ProgressBar)findViewById(R.id.boxProgressBar);
 
 		flagImageView = (ImageView)findViewById(R.id.flagImageView);
 		flagProgressBar = (ProgressBar)findViewById(R.id.flagProgressBar);
 
 		//INFO: mine field
 		mineField = (TableLayout)findViewById(R.id.MineField);
 	}
 
 	private void init() {
 		startPauseButton.setBackgroundResource(R.drawable.pause_icon);
 		stopButton.setBackgroundResource(R.drawable.stop_icon);
 
 		boxImageView.setImageResource(R.drawable.empty);
 		flagImageView.setImageResource(R.drawable.flag);	
 	}
 
 	private void event() {
 		startPauseButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 
 				startPauseButtonIsSelected = !startPauseButtonIsSelected;
 
 				if (startPauseButtonIsSelected == true) {
 					startPauseButton.setBackgroundResource(R.drawable.play_icon);
 					pause();
 					Toast.makeText(GameActivity.this, "Pause", Toast.LENGTH_LONG).show();
 				}
 				else {
 					startPauseButton.setBackgroundResource(R.drawable.pause_icon);
 					restart();
 					Toast.makeText(GameActivity.this, "Continue", Toast.LENGTH_LONG).show();
 				}
 
 			}
 		});
 
 		stopButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				stop();
 			}
 		});
 	}
 
 	private void showDialog(final String title, String message) {
 		//INFO: Instantiate an AlertDialog.Builder with its constructor
 		AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
 
 		//INFO: Chain together various setter methods to set the dialog characteristics
 		builder.setMessage(message)
 		.setTitle(title);
 
 		//INFO: Setting Positive "Yes" Button
 		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int which) {
 
 				// Write your code here to invoke YES event
 				Toast.makeText(getApplicationContext(), "Let's continue", Toast.LENGTH_SHORT).show();
 
 				if (title == "Loose") {
 					Log.d(TAG, "New game selected");
 					endGame();
 					start();
 				}
 				else {
 					Log.d(TAG, "Restart selected");
 					restart();
 				}
 			}
 		});
 
 		//INFO: Setting Negative "NO" Button
 		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				Toast.makeText(getApplicationContext(), "End of the game", Toast.LENGTH_SHORT).show();
 				dialog.cancel();
 				stop();
 			}
 		});
 
 
 		//INFO: Get the AlertDialog from create()
 		AlertDialog dialog = builder.create();
 
 		dialog.show();
 	}
 
 	public void onStopButtonClick(View v)
 	{
 		Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
 	} 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 
 		setup();
 		init();
 		event();
 
 		//INFO: hide action bar
 		final ActionBar bar = getActionBar();
 		bar.hide();
 
 		timer = new Handler();
 
 		start();
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		if (isPlaying())
 			showDialog("Pause", "Do you want to continue ?");
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (isPlaying())
 			stopTimer();
 	}
 
 	public void initializeGame() {
 		// Get options from the database
 		globals = (Globals)getApplication();
 		options = globals.getOptionDataSource().getOption();
 
 		// Set total rows and columns based on the difficulty
 		totalRows = options.getSize() * 5 + 10;
 		totalCols = options.getSize() * 5 + 10;
 
 		duration = 0;
 		secondsPassed = 0;
 		minutesPassed = 0;
 		gameLost = false;
 
 		totalDiscoveredFlagNumber = 0;
 		totalDiscoveredValidFlagNumber = 0;
 		totalBoxNumber = (totalRows * totalCols) - totalCols;
 		totalOpenBoxNumber = 0;
 		totalBombNumber = 0;
 
 		boxProgressBar.setProgress(0);
		flagProgressBar.setProgress(0);
 	}
 
 	public void showGameBoard()
 	{
 		mineField.removeAllViews();
 		//INFO: for every row
 		for (int row = 0; row < totalRows; row++)
 		{
 			//INFO: create a new table row
 			TableRow tableRow = new TableRow(this);
 			//set the height and width of the row
 			tableRow.setLayoutParams(new TableRow.LayoutParams((BoxWH * BoxPadding) * totalCols, BoxWH * BoxPadding));
 
 			//INFO: for every column
 			for (int col = 0; col < totalCols; col++)       
 			{
 				//INFO: set the width and height of the Box
 				Boxes[row][col].setLayoutParams(new TableRow.LayoutParams(BoxWH * BoxPadding,  BoxWH * BoxPadding)); 
 				//INFO: add some padding to the Box
 				Boxes[row][col].setPadding(BoxPadding, BoxPadding, BoxPadding, BoxPadding);
 				//INFO: add the Box to the table row
 				tableRow.addView(Boxes[row][col]);
 			}
 			//INFO: add the row to the minefield layout
 			mineField.addView(tableRow, new TableLayout.LayoutParams((BoxWH * BoxPadding) * totalCols, BoxWH * BoxPadding)); 
 		}
 	}
 
 	public void setupMine() {
 		for (int row = 0; row < totalRows; row++)
 		{
 			for (int col = 0; col < totalCols; col++)
 			{
 				Boxes[row][col].setSurroundingNumber(mineNumberTowardBox(row, col));
 			}
 		}
 	}
 
 	public void createGameBoard(int diff)
 	{
 		//INFO: setup the Boxes array
 		Boxes = new UIBox[totalRows][totalCols];
 
 		for (int row = 0; row < totalRows; row++)
 		{
 			for (int col = 0; col < totalCols; col++)
 			{
 				final int curRow = row;
 				final int curCol = col;
 
 				//create a Box
 				Boxes[row][col] = new UIBox(this);
 
 				//set the Box defaults
 				Boxes[row][col].setDefaults();
 				Boxes[row][col].setRow(curRow);
 				Boxes[row][col].setColumn(curCol);
 
 				//set the Box properties
 				int randDifficulty = (int)(new Random().nextInt() % (10 - options.getLevel()));
 				if (randDifficulty == 0) {
 					Boxes[row][col].plantMine();
 					totalBombNumber++;
 				}
 
 				//add a click listener
 				Boxes[row][col].setOnClickListener(new OnClickListener()
 				{
 					@Override
 					public void onClick(View view)
 					{
 						if (!isPlaying())
 							return;
 
 
 						Log.d(TAG, "Clicked on box (" + curCol + ", " + curRow + ")");
 						UIBox box = (UIBox)view;
 
 						if (box.isFlag()) {
 							box.toggleFlag();
 							totalDiscoveredFlagNumber--;
 							if (box.isMine())
 								totalDiscoveredValidFlagNumber--;
 
 						}
 						else {
 							box.openBox();
 							//INFO: data needed for stats 
 
 						}
 					}
 				});
 
 				//INFO: add a long click listener
 				Boxes[row][col].setOnLongClickListener(new OnLongClickListener()
 				{
 					@Override
 					public boolean onLongClick(View view)
 					{
 						if (!isPlaying())
 							return false;
 
 						Log.d(TAG, "Clicked on box (" + curCol + ", " + curRow + ")");
 
 						UIBox box = (UIBox)view;
 						if (!box.isCovered())
 							return false;
 						box.toggleFlag();
 						totalDiscoveredFlagNumber++;
 						if (box.isMine())
 							totalDiscoveredValidFlagNumber++;
 						return true;
 					}
 				});
 			}
 		}
 	}
 
 	public int mineNumberTowardBox(int row, int col) {
 		int res = 0;
 
 		int startX = row - 1;
 		int startY = col - 1;
 
 		for (int y = startY; y < startY + 3; y++) {
 			for (int x = startX; x < startX + 3; x++) {
 				if ((x >= 0 && y >= 0) && (x < totalRows && y < totalCols) && Boxes[x] != null && Boxes[x][y] != null) {
 					if (Boxes[x][y].isMine()) {
 						res += 1;
 					}
 				}
 			}
 		}
 		return res;
 	}
 
 	public void startTimer()
 	{
 		if (secondsPassed == 0) {
 			minutesPassed = 0;
 			timer.removeCallbacks(updateTimer);
 		}
 		timer.postDelayed(updateTimer, 1000);
 	}
 
 	public void stopTimer()
 	{
 		timer.removeCallbacks(updateTimer);
 	}
 
 	private Runnable updateTimer = new Runnable()
 	{
 		public void run()
 		{
 			long currentMilliseconds = System.currentTimeMillis();
 			++secondsPassed;
 			++duration;
 			String curSecs = Integer.toString(secondsPassed);
 			if (secondsPassed < 10)
 				curSecs = "0" + curSecs;
 			else if (secondsPassed == 59) {
 				secondsPassed = 0;
 				minutesPassed++;
 			}
 			String curMins = Integer.toString(minutesPassed);
 			if (minutesPassed < 10)
 				curMins = "0" + curMins;
 			timerText.setText(curMins + ":" + curSecs);
 
 			timer.postAtTime(this, currentMilliseconds);
 			timer.postDelayed(updateTimer, 1000);
 		}
 	};
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		if (!isPlaying())
 			return false;
 		currentX = event.getX();
 		currentY = event.getY();
 		int dx = (int) (oldX - currentX);
 		int dy = (int) (oldY - currentY);
 
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_MOVE:
 			if (started) {
 				vScroll.scrollBy(0, dy);
 				hScroll.scrollBy(dx, 0);
 			} else {
 				started = true;
 			}
 			oldX = currentX;
 			oldY = currentY;
 			break;
 		case MotionEvent.ACTION_UP: 
 			vScroll.scrollBy(0, dy);
 			hScroll.scrollBy(dx, 0);
 			started = false;
 			break;
 		}
 		return true;
 	}
 
 	public void loseGame()
 	{
 		gameLost = true;
 		Log.d(TAG, "Lost the game.");
 		stopTimer();
 		//INFO: display all boxes 
 		/*
 			for (int i = 0; i < totalRows; i++)
 			{
 				for (int j = 0; j < totalCols; j++)
 				{
 					//if the Box is covered
 					if (Boxes[i][j].isCovered())
 					{
 						//if there is no flag or mine
 						if (!Boxes[i][j].isFlag() && !Boxes[i][j].isMine()) {
 							Boxes[i][j].openBox();
 						}
 						//if there is a mine but no flag
 						else if(Boxes[i][j].isMine() && !Boxes[i][j].isFlag()) {
 							Boxes[i][j].openBox();
 						}
 						//if there is a mine and flag
 						else if(Boxes[i][j].isMine() && Boxes[i][j].isFlag()) {
 							//addScore(Boxes[i][j].getNoSurroundingMines() * 20);
 							Boxes[i][j].openBox();
 						}
 
 					}
 				}
 			}
 		}
 		 */
 		finalizeScore();
 
 		showDialog("Loose", getScore() + "% of mine discovered.\nYou LOOSE in " + minutesPassed +  " minutes " + secondsPassed + " seconds.\n" + "Do you want to continue ?");
 	}
 
 	public void finalizeScore() {
 		this.globals.getScoreDataSource().createScore(getScore(), options.getName(), options.getSize(), options.getLevel(), duration);
 	}
 
 	//INFO: main functions
 
 	private void newGame() {
 		initializeGame();
 		createGameBoard(options.getLevel());
 		setupMine();
 		showGameBoard();
 		Log.d(TAG, "New game started with options: " + options.toString());
 	}
 
 	private Boolean isPlaying() {
 		return !startPauseButtonIsSelected;
 	}
 
 	public void endGame() {
 		mineField.removeAllViews();
 
 		// reset variables
 		this.initializeGame();
 	}
 
 	private void start() {
 		newGame();
 		startTimer();
 	}
 
 	private void pause() {
 		stopTimer();
 	}
 
 	private void restart() {
 		startTimer();
 	}
 
 	private void stop() {
 		stopTimer();
 		GameActivity.this.finish();
 	}
 
 	//INFO: logic
 
 	public void didOpenBox(UIBox sender) {
 		totalOpenBoxNumber++;
 
 		int bombDiscoveredAverage = (totalDiscoveredFlagNumber * 100) / totalBombNumber;//Could be wrong number if the box selected is not a bomb
 		flagProgressBar.setProgress(bombDiscoveredAverage);
 		int boxDiscoveredAverage =  (totalOpenBoxNumber * 100) / totalBoxNumber;
 		boxProgressBar.setProgress(boxDiscoveredAverage);
 		Log.d(TAG, "bombDiscoveredAverage: " + bombDiscoveredAverage + " boxDiscoveredAverage: " + boxDiscoveredAverage);
 		Log.d(TAG, "totalDiscoveredFlagNumber: " + totalDiscoveredFlagNumber + " totalBombNumber: " + totalBombNumber);
 		Log.d(TAG, "totalOpenBoxNumber: " + totalOpenBoxNumber + " totalBombNumber: " + totalBoxNumber);
 
		if (getScore() == 100) {
			//INFO: 
			finalizeScore();
			showDialog("Win", getScore() + "% of mine discovered.\nYou WIN in " + minutesPassed +  " minutes " + secondsPassed + " seconds\n" + "Do you want to continue ?");
		}
 	}
 
 	private int getScore() {
 
 		return (totalDiscoveredValidFlagNumber * 100) / totalBombNumber ;
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.game_menu, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.new_game:
 			//INFO: old code
 			Toast.makeText(this, "New game!", Toast.LENGTH_SHORT).show();
 			start();
 			break;
 
 		default:
 			break;
 		}
 		return true;
 	}
 }
