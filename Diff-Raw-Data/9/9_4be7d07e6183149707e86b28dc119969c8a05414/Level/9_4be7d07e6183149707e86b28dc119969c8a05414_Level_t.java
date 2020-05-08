 package ch.gibb.project.activity;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.RelativeLayout;
 import ch.gibb.project.R;
 import ch.gibb.project.controller.ActionHandler;
 import ch.gibb.project.elements.Ball;
 import ch.gibb.project.elements.Maze;
 import ch.gibb.project.elements.Point;
 import ch.gibb.project.elements.Text;
 import ch.gibb.project.elements.Wall;
 import ch.gibb.project.enums.StageEnum;
 import ch.gibb.project.util.MessageUtil;
 
 public class Level extends Activity implements SensorEventListener {
 	public SensorManager sensorManager;
 	private RelativeLayout layout;
 	private Maze mazeElement;
 	private Ball ballElement;
 	private Wall wallElement;
 	private Text textElement;
 	private Point pointElement;
 	private ActionHandler actionHandler;
 	private android.graphics.Point displaySize;
 	private Timer timer;
 	private int stageNumber = 1;
 	private long millis;
 	public static Context LevelContext;
 
 	private static Bitmap backgroundImage;
 	private static Bitmap wallImage;
 	private static Bitmap finishImage;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Display display = getWindowManager().getDefaultDisplay();
 		displaySize = new android.graphics.Point();
 		display.getSize(displaySize);
 		setStaticBitmaps(displaySize.x, displaySize.y);
 		initObjects(stageNumber);
 		LevelContext = this;
 		// FIXME zinggpa stars have to appear if play button pressed
 		createTimer();
 	}
 
 	protected void initObjects(int stageNumber) {
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		StageEnum.setDisplayMetrics(this.getResources().getDisplayMetrics());
 		Text.stage = stageNumber;
 		initViews(displaySize.x, displaySize.y);
 		addelementsToView();
 		actionHandler = new ActionHandler(this);
 		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
 			Log.d("Message", "No accelerometer installed");
 		} else {
 			Sensor accelerometer = sensorManager.getSensorList(
 					Sensor.TYPE_ACCELEROMETER).get(0);
 			if (!sensorManager.registerListener(this, accelerometer,
 					SensorManager.SENSOR_DELAY_FASTEST)) {
 				Log.d("Message", "Couldn't register sensor listener");
 			}
 		}
 	}
 
 	private void initViews(int x, int y) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inSampleSize = 8;
 		mazeElement = new Maze(this, x, y);
 		ballElement = new Ball(this);
 		wallElement = new Wall(this);
 		textElement = new Text(this);
 		pointElement = new Point(this);
 	}
 
 	private void addelementsToView() {
 		layout = new RelativeLayout(this);
 		layout.addView(mazeElement);
 		layout.addView(textElement);
 		layout.addView(pointElement);
 		layout.addView(wallElement);
 		layout.addView(ballElement);
 		setContentView(layout);
 	}
 
 	private void createTimer() {
 		final Handler handler = new Handler();
 		timer = new Timer(false);
 
 		TimerTask timerTask = new TimerTask() {
 			@Override
 			public void run() {
 				handler.post(new Runnable() {
 					@Override
 					public void run() {
 						millis++;
 						Text.usedTime = millis;
 						textElement.postInvalidate();
 					}
 				});
 			}
 		};
 		timer.scheduleAtFixedRate(timerTask, 0, 1);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 	}
 
 	@Override
 	public void onSensorChanged(final SensorEvent event) {
 		if (!actionHandler.checkIfFinished()) {
 			actionHandler.moveAndCheckX(event.values[0]);
 			actionHandler.moveAndCheckY(event.values[1]);
 			actionHandler.checkStarTouch();
 		} else {
 			performStageChangeAction();
 		}
 		if (actionHandler.ballInHole()) {
 			performHoleAction();
 		}
 	}
 
 	private void performStageChangeAction() {
 		if (stageNumber == StageEnum.values().length) {
 			sensorManager.unregisterListener(this);
 			timer.cancel();
 			nextActivity(Finish.class);
 			return;
 		} else {
 			Animation animation = AnimationUtils.loadAnimation(this,
 					R.anim.push_down_out);
 			layout.startAnimation(animation);
 			initObjects(++stageNumber);
 			return;
 		}
 	}
 
 	private void performHoleAction() {
 		// layout.removeView(mazeElement);
 		// layout.addView(mazeElement);
 		MessageUtil.getInstance().createShortToastMessage(Level.this,
 				"Oh no! You felt into a hole");
 		sensorManager.unregisterListener(Level.this);
 		Animation animation = AnimationUtils.loadAnimation(this,
 				R.anim.push_up_out);
 		layout.startAnimation(animation);
 		initObjects((stageNumber == 1) ? stageNumber : --stageNumber);
 	}
 
 	public StageEnum getStage() {
 		switch (stageNumber) {
 		case 1:
 			return StageEnum.STAGE_1;
 		case 2:
 			return StageEnum.STAGE_2;
 		case 3:
 			return StageEnum.STAGE_3;
 		case 4:
 			return StageEnum.STAGE_4;
 		default:
 			nextActivity(Finish.class);
 			return null;
 		}
 	}
 
 	public void setStaticBitmaps(int width, int height) {
 		BitmapFactory.Options opts = new BitmapFactory.Options();
 		opts.inDither = false;
 		opts.inPurgeable = true;
 		opts.inInputShareable = true;
 		opts.inTempStorage = new byte[32 * 1024];
 		// TODO: Shrink Image Correctly
 		backgroundImage = Bitmap.createScaledBitmap(BitmapFactory
 				.decodeResource(getResources(), R.drawable.wood, opts),
 				width - 40, height - 40, true);
 		wallImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
 				getResources(), R.drawable.wall, opts), width, height, true);
 		finishImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
 				getResources(), R.drawable.finish, opts), 73, 73, true);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			sensorManager.unregisterListener(this);
 			timer.cancel();
 			MessageUtil.getInstance().createAlertMessage(Level.this,
 					MessageUtil.DIALOG_LEVELEXIT);
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public SensorManager getSensorManager() {
 		return sensorManager;
 	}
 
 	public void setSensorManager(SensorManager sensorManager) {
 		this.sensorManager = sensorManager;
 	}
 
 	public Maze getMazeElement() {
 		return mazeElement;
 	}
 
 	public void setMazeElement(Maze mazeElement) {
 		this.mazeElement = mazeElement;
 	}
 
 	public Ball getBallElement() {
 		return ballElement;
 	}
 
 	public void setBallElement(Ball ballElement) {
 		this.ballElement = ballElement;
 	}
 
 	public Wall getWallElement() {
 		return wallElement;
 	}
 
 	public void setWallElement(Wall wallElement) {
 		this.wallElement = wallElement;
 	}
 
 	public Point getPointElement() {
 		return pointElement;
 	}
 
 	public void setPointElement(Point pointElement) {
 		this.pointElement = pointElement;
 	}
 
 	public Text getTextElement() {
 		return textElement;
 	}
 
 	public int getStageNumber() {
 		return stageNumber;
 	}
 
 	public void setStageNumber(int stageNumber) {
 		this.stageNumber = stageNumber;
 	}
 
 	public static Bitmap getBackgroundImage() {
 		return backgroundImage;
 	}
 
 	public static Bitmap getWallImage() {
 		return wallImage;
 	}
 
 	public static Bitmap getFinishImage() {
 		return finishImage;
 	}
 
 }
