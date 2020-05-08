 package be.infogroep.justpoker;
 
 import android.animation.ObjectAnimator;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.Vibrator;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import be.infogroep.justpoker.GameElements.Card;
 import edu.vub.at.commlib.PokerButton;
 
 public class TapTestActivity extends Activity implements
 		AbstractPokerClientActivity {
 	boolean flippedCard1;
 	boolean flippedCard2;
 
 	PokerClient client;
 	private PowerManager pm;
 	private PowerManager.WakeLock wl;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// client = PokerClient.getInstance();
 		// client.sendHello();
 		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
 				 "be.infogroep.justpoker.ServerTableActivity");
 		wl.acquire();
 		
 		Intent incomingIntent = getIntent();
 		String ip = incomingIntent.getStringExtra("ip");
 		String name = incomingIntent.getStringExtra("name");
 		client = PokerClient.getInstance(TapTestActivity.this, name, ip);
 
 		flippedCard1 = flippedCard2 = false;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tap_test);
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		final ImageView cardContainer1 = (ImageView) findViewById(R.id.card1);
 		final ImageView cardContainer2 = (ImageView) findViewById(R.id.card2);
 		final ImageView betChip = (ImageView) findViewById(R.id.betChip);
 		final TextView betChipText = (TextView) findViewById(R.id.betChipText);
 
 		OnFlingGestureListener cardListener = new OnFlingGestureListener() {
 			private boolean longPressed = false;
 
 			@Override
 			public void onBottomToTop() {
 				client.fold(cardContainer1, cardContainer2);
 			}
 
 			@Override
 			public void onDoubletap() {
 				client.check(cardContainer1, cardContainer2);
 			}
 
 			@Override
 			public void onLongpress() {
 				cardContainer1.setImageDrawable(getDrawable(client.getCard1()
 						.toString()));
 				cardContainer2.setImageDrawable(getDrawable(client.getCard2()
 						.toString()));
 				longPressed = true;
 			}
 
 			public void onTouchevent(MotionEvent e) {
 				if (longPressed && e.getAction() == MotionEvent.ACTION_UP) {
 					cardContainer1.setImageResource(R.drawable.card_backside);
 					cardContainer2.setImageResource(R.drawable.card_backside);
 					longPressed = false;
 				}
 			}
 		};
 		OnFlingGestureListener chipListener = new OnFlingGestureListener() {
 			@Override
 			public void onBottomToTop() {
 				client.bet();
 			}
 		};
 
 		cardContainer2.setOnTouchListener(cardListener);
 		cardContainer1.setOnTouchListener(cardListener);
 		betChip.setOnTouchListener(chipListener);
 		betChipText.setOnTouchListener(chipListener);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_tap_test, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void printMessage(String s, TextView t) {
 		t.setText(s);
 	}
 
 	private void doFold(ImageView card) {
 		ObjectAnimator move = ObjectAnimator.ofFloat(card, "y", -225);
 		ObjectAnimator fade = ObjectAnimator.ofFloat(card, "alpha", 0);
 		ObjectAnimator spin = ObjectAnimator.ofFloat(card, "rotation", 180);
 		move.setDuration(300);
 		fade.setDuration(300);
 		spin.setDuration(300);
 		move.start();
 		// fade.start();
 		spin.start();
 	}
 
 	private void doCheck() {
 		Toast.makeText(getApplicationContext(), "You Checked!",
 				Toast.LENGTH_LONG).show();
 	}
 
 	@TargetApi(14)
 	private void doBet() {
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.tap_test_layout);
 		ImageView button = (ImageView) findViewById(R.id.betChip);
 		
 		ImageView button2 = cloneImageView(button);
 		layout.addView(button2);
 		ObjectAnimator move = ObjectAnimator.ofFloat(button2, "y", -225);
 		ObjectAnimator spin = ObjectAnimator.ofFloat(button2, "rotation", 180);
 		move.setDuration(300);
 		spin.setDuration(300);
 		move.start();
 		spin.start();
 		button2.destroyDrawingCache();
 		Toast.makeText(getApplicationContext(), "You Bet!", Toast.LENGTH_LONG)
 				.show();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		wl.release();
 	}
 
 	public void displayLoggingInfo(final Object m) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(getApplicationContext(), "received: " + m,
 						Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 
 	protected void runOnNotUiThread(Runnable runnable) {
 		new Thread(runnable).start();
 	}
 
 	public void setCards(Card[] cards) {
 		client.setCard1(cards[0]);
 		client.setCard2(cards[1]);
 	}
 
 	private Drawable getDrawable(String s) {
 		String s2 = "drawable/" + s;
 		int imageResource = getResources().getIdentifier(s2, null,
 				getPackageName());
 		return getResources().getDrawable(imageResource);
 	}
 
 	public void setBlind(final PokerButton b) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Toast.makeText(getApplicationContext(), "You are the " + b,
 						Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 
 	public void fold(final ImageView card1, final ImageView card2) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				doFold(card1);
 				doFold(card2);
 			}
 		});
 	}
 
 	public void check(ImageView cardContainer1, ImageView cardContainer2) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				doCheck();
 			}
 		});
 	}
 
 	public void bet() {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				doBet();
 			}
 		});
 	}
 	private ImageView cloneImageView(ImageView view){
 		ImageView result = new ImageView(this);
 		result.setX(view.getX());
 		result.setY(view.getY());
 		result.setScaleType(view.getScaleType());
 		result.setScaleX(view.getScaleX());
 		result.setScaleY(view.getScaleY());
 		result.setImageDrawable(view.getDrawable());
 		return result;
 	}
 
 	private void vibrate(int len){
 		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		v.vibrate(len);
 	}
 
	@Override
 	public void startTurn() {
 		runOnUiThread(new Runnable() {
			
			@Override
 			public void run() {
 				vibrate(500);
 			}
 		});
 		
 	}
 }
