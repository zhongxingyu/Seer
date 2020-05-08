 package com.room.puzzles;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 import java.util.StringTokenizer;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.ViewConfiguration;
 import android.widget.Toast;
 
 import com.room.R;
 import com.room.media.MSoundManager;
 import com.room.scene.SLayout;
 import com.room.scene.SLayoutLoader;
 import com.room.scene.SSceneActivity;
 import com.room.utils.UBitmapUtil;
 
 public class PStatues extends SSceneActivity {
 	public static final int NUM_STATUES = 3;
 	public static final int ROTATIONS = 4;
 	private static final int MAXCLICK = 6;
 	private static final int HINT_MSG_DURATION = 10000;
 	private static final String TAG = "PStatues";
 
 	private RectF[] symbolArea = new RectF[NUM_STATUES];
 	private RectF[] statueArea = new RectF[NUM_STATUES];
 	private RectF[] statueArea_original = new RectF[NUM_STATUES];
 	private RectF submitArea;
 	private RectF tempRectf;
 	private ArrayList<Bitmap> symbolImages;
 	private ArrayList<Bitmap> statueImages;
 	private Bitmap submitImage;
 	private float statueWidth, statueHeight;
 	private float symbolWidth, symbolHeight;
 	private float submitWidth, submitHeight;
 
 	private int touchedStatue;
 	private int draggedStatue;
 	private float prevX;
 	private int touchSlop;
 
 	private enum SymbolState {
 		WHITE, BLACK, EMPTY
 	}
 
 	private int clickCounter;
 	private ArrayList<SymbolState> symbols = new ArrayList<SymbolState>();
 	private ArrayList<Integer> answers = new ArrayList<Integer>();
 	private int[] guesses = new int[NUM_STATUES];
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setLayout(SLayoutLoader.getInstance().puzzleStatues);
 		for (int i = 0; i < NUM_STATUES; ++i) {
 			symbolArea[i] = getBoxPixelCoords("symbol_" + i);
 			statueArea[i] = getBoxPixelCoords("statue_" + i);
 			statueArea_original[i] = new RectF(statueArea[i]);
 		}
 		submitArea = getBoxPixelCoords("submit");
 		statueWidth = statueArea[0].right - statueArea[0].left;
 		statueHeight = statueArea[0].bottom - statueArea[0].top;
 		symbolWidth = symbolArea[0].right - symbolArea[0].left;
 		symbolHeight = symbolArea[0].bottom - symbolArea[0].top;
 		submitWidth = submitArea.right - submitArea.left;
 		submitHeight = submitArea.bottom - submitArea.top;
 
 		statueImages = UBitmapUtil.populateBitmaps("puzzle_statue_",
 				NUM_STATUES * ROTATIONS, (int) statueWidth, (int) statueHeight);
 		symbolImages = UBitmapUtil.populateBitmaps("puzzle_symbol_", 2,
 				(int) symbolWidth, (int) symbolHeight);
 		submitImage = UBitmapUtil.loadScaledBitmap(R.drawable.puzzle_submit,
 				(int) submitWidth, (int) submitHeight);
 		tempRectf = new RectF();
 		final ViewConfiguration configuration = ViewConfiguration
 				.get(getApplicationContext());
 		touchSlop = configuration.getScaledTouchSlop();
 		init_puzzle();
 	}
 
 	@Override	
 	protected void onResume() {
 		super.onResume();
 		setBackgroundImage(R.drawable.puzzle_statues2);
 	}
 	
 	private void init_puzzle() {
 		Random rand = new Random();
 		// answers[0] = 1;
 		// answers[1] = 6;
 		// answers[2] = 11;
 		symbols.clear();
 		for (int i = 0; i < NUM_STATUES; ++i) {
 			answers.add(i);
 			guesses[i] = i * ROTATIONS; // all set as front initially
 			symbols.add(SymbolState.EMPTY);
 		}
 		Collections.shuffle(answers);
 		for (int i = 0; i < NUM_STATUES; ++i) {
 			answers.set(i, answers.get(i) * ROTATIONS + rand.nextInt(ROTATIONS));
 			Log.e(TAG, "answers[" + i + "]=" + answers.get(i));
 		}
 		draggedStatue = -1;
 		clickCounter = 0;
 	}
 
 	@Override
 	public void onDraw(Canvas canvas, Paint paint) {
 		super.onDraw(canvas, paint);
 		for (int i = 0; i < NUM_STATUES; ++i) {
 			canvas.drawBitmap(statueImages.get(guesses[i]), statueArea[i].left,
 					statueArea[i].top, paint);
 
 			if (symbols.get(i) != SymbolState.EMPTY) {
 				canvas.drawBitmap(symbolImages.get(symbols.get(i).ordinal()),
 						symbolArea[i].left, symbolArea[i].top, paint);
 			}
 		}
 		canvas.drawBitmap(submitImage, submitArea.left, submitArea.top, paint);
 	}
 
 	@Override
 	public void onBoxDown(SLayout.Box box, MotionEvent event) {
 		super.onBoxDown(box, event);
 		StringTokenizer st = new StringTokenizer(box.name, "_");
 		String clickedBox = st.nextToken();
 		float positionX = event.getRawX();
 		if (clickedBox.equals("submit")) {
 			handleSubmitTouch();
 		} else { // statue
 			touchedStatue = Integer.parseInt(st.nextToken());
 			prevX = positionX;
 		}
 	}
 
 	@Override
 	public void onBoxMove(SLayout.Box box, MotionEvent event) {
 		super.onBoxMove(box, event);
 		StringTokenizer st = new StringTokenizer(box.name, "_");
 		String clickedBox = st.nextToken();
 		if (!clickedBox.equals("statue"))
 			return;
 		float positionX = event.getRawX();
 		float deltaX = positionX - prevX;
 		if (Math.abs(deltaX) <= touchSlop)
 			return;
		if (draggedStatue == -1) {
			draggedStatue = touchedStatue;
		}
 		// Check the boundary
 		if ((statueArea[draggedStatue].left + deltaX) > statueArea_original[0].left
 				&& ((statueArea[draggedStatue].right + deltaX) < statueArea_original[NUM_STATUES - 1].right)) {
 			// moving left and overlapping with the left one
 			if (prevX > positionX
 					&& draggedStatue - 1 >= 0
 					&& RectF.intersects(statueArea[draggedStatue],
 							statueArea[draggedStatue - 1])) {
 				swapWithOverlappingStatue(-1);
 				MSoundManager.getInstance().playSoundEffect(R.raw.wood_whack);
 			}
 			// moving right and overlapping with the right one
 			else if (prevX < positionX
 					&& draggedStatue + 1 < NUM_STATUES
 					&& RectF.intersects(statueArea[draggedStatue],
 							statueArea[draggedStatue + 1])) {
 				swapWithOverlappingStatue(1);
 				MSoundManager.getInstance().playSoundEffect(R.raw.wood_whack);
 			}
 			prevX = positionX;
 			statueArea[draggedStatue].left += deltaX;
 			statueArea[draggedStatue].right += deltaX;
 			repaint();
 		}
 	}
 
 	@Override
 	public void onBoxRelease(SLayout.Box box, MotionEvent event) {
 		super.onBoxRelease(box, event);
 		StringTokenizer st = new StringTokenizer(box.name, "_");
 		String clickedBox = st.nextToken();
 		if (!clickedBox.equals("statue"))
 			return;
 		else if (draggedStatue == -1) {
 			// rotate
 			guesses[touchedStatue] = (guesses[touchedStatue] + 1) % ROTATIONS
 					+ guesses[touchedStatue] / ROTATIONS * ROTATIONS;
 			repaint();
 			MSoundManager.getInstance().playSoundEffect(R.raw.wood_whack);
 		} else {
 			statueArea[draggedStatue].set(statueArea_original[draggedStatue]);
 			draggedStatue = -1;
 			repaint();
 			MSoundManager.getInstance().playSoundEffect(R.raw.wood_whack);
 		}
 	}
 
 	private void swapWithOverlappingStatue(int offset) // offset is either -1 or +1
 	{
 		tempRectf.set(statueArea[draggedStatue]);
 		int tempGuess = guesses[draggedStatue + offset];
 		guesses[draggedStatue + offset] = guesses[draggedStatue];
 		guesses[draggedStatue] = tempGuess;
 		statueArea[draggedStatue].set(statueArea_original[draggedStatue]);
 		statueArea[draggedStatue + offset].set(tempRectf);
 		draggedStatue += offset;
 	}
 
 	private void handleSubmitTouch() {
 		draggedStatue = -1;
 		boolean result = updateSymbolStates();
 		repaint();
 		clickCounter++;
		MSoundManager.getInstance().playSoundEffect(R.raw.tick);
 		if (result)
 			handleSuccess();
 		else if (clickCounter == MAXCLICK)
 			handleFailure();
 	}
 
 	private boolean updateSymbolStates() {
 		int numBlack = 0;
 		symbols.clear();
 		for (int i = 0; i < NUM_STATUES; ++i) {
 			for (int j = 0; j < NUM_STATUES; ++j) {
 				if (i == j && guesses[i] == answers.get(j)) {
 					symbols.add(0, SymbolState.BLACK);
 					numBlack++;
 				} else if (guesses[i] == answers.get(j)) {
 					symbols.add(SymbolState.WHITE);
 				}
 			}
 		}
 		int fillEmpty = NUM_STATUES - symbols.size();
 		for (int i = 0; i < fillEmpty; ++i) {
 			symbols.add(SymbolState.EMPTY);
 		}
 		return (numBlack == NUM_STATUES);
 	}
 
 	private void handleSuccess() {
 		finish();
 	}
 
 	private void handleFailure() {
 		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		v.vibrate(300);
 		Context context = getApplicationContext();
 		Toast toast = Toast.makeText(context, R.string.hint_statue,
 				HINT_MSG_DURATION);
 		toast.show();
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			public void run() {
 				init_puzzle();
 				MSoundManager.getInstance().playSoundEffect(R.raw.swords);
 				repaint();
 			}
 		}, 3000);
 	}
 }
