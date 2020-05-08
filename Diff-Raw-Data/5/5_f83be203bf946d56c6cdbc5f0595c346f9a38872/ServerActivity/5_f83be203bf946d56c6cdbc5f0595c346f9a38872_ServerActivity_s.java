 package edu.vub.at.nfcpoker.ui;
 
 import java.util.HashMap;
 
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.nfcpoker.Card;
 import edu.vub.at.nfcpoker.ConcretePokerServer;
 import edu.vub.at.nfcpoker.ConcretePokerServer.GameState;
 import edu.vub.at.nfcpoker.R;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.AnimatorSet;
 import android.animation.ObjectAnimator;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 @TargetApi(11)
 public class ServerActivity extends Activity implements ServerViewInterface {
 	
 	@SuppressLint("UseSparseArrays")
 	HashMap<Integer, View> playerBadges = new HashMap<Integer, View>(); 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.activity_server);
     	View tablet_layout = findViewById(R.id.tablet_layout);
     	boolean isDedicated = tablet_layout != null;
    	ConcretePokerServer cps = new ConcretePokerServer(
    			this, isDedicated,
    			CommLib.getIpAddress(this),
    			CommLib.getBroadcastAddress(this));
     	cps.start();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_server, menu);
         return true;
     }
     
     int nextToReveal = 0;
     
 	public void revealCards(final Card[] cards) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				for (Card c : cards) {
 					Log.d("PokerServer", "Revealing card " + c);
 					LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
 					ImageButton ib = (ImageButton) ll.getChildAt(nextToReveal++);
 					ib.setImageResource(cardToResourceID(c));
 					ObjectAnimator anim = ObjectAnimator.ofFloat(ib, "alpha", 0.f, 1.f);
 					anim.setDuration(1000);
 					anim.start();
 				}
 			}
 
 			public int cardToResourceID(Card c) {
 				return getResources().getIdentifier("edu.vub.at.nfcpoker:drawable/" + c.toString(), null, null);
 			}
 		});
 	}
 	
 
 	public void resetCards() {
 		Log.d("PokerServer", "Hiding cards again");
 		nextToReveal = 0;
 		runOnUiThread(new Runnable() {
 			public void run() {
 				LinearLayout ll = (LinearLayout) findViewById(R.id.cards);
 				for (int i = 0; i < 5; i++) {
 					final ImageButton ib = (ImageButton) ll.getChildAt(i);
 					ObjectAnimator animX = ObjectAnimator.ofFloat(ib, "scaleX", 1.f, 0.f);
 					ObjectAnimator animY = ObjectAnimator.ofFloat(ib, "scaleY", 1.f, 0.f);
 					animX.setDuration(500); animY.setDuration(500);
 					final AnimatorSet scalers = new AnimatorSet();
 					scalers.play(animX).with(animY);
 					scalers.addListener(new AnimatorListenerAdapter() {
 
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							ib.setScaleX(1.f);
 							ib.setScaleY(1.f);
 							ib.setImageResource(R.drawable.backside);
 						}
 
 					});
 					scalers.start();
 				}
 			}
 		});
 	}
 
 	public void showStatechange(final GameState newState) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				TextView phase = (TextView)findViewById(R.id.current_phase);
 				phase.setText(newState.toString());
 			}
 		});
 	}
 
 	@Override
 	public void addPlayer(final int clientID, final String clientName, final int initialMoney) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				Log.d("PokerServer", "Adding player name " + clientName);
 				LinearLayout users = (LinearLayout) findViewById(R.id.users);
 				View badge = getLayoutInflater().inflate(R.layout.user, null);
 				
 				TextView name = (TextView) badge.findViewById(R.id.playerName);
 				name.setText(clientName);
 				TextView money = (TextView) badge.findViewById(R.id.playerMoney);
 				money.setText("\u20AC" + initialMoney);
 
 				playerBadges.put(clientID, badge);
 				users.addView(badge);
 			}
 		});
 	}
 
 	@Override
 	public void setPlayerMoney(final Integer player, final int current) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				View badge = playerBadges.get(player);
 				if (badge != null) {
 					TextView money = (TextView) badge.findViewById(R.id.playerMoney);
 					money.setText("\u20AC" + current);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void removePlayer(final Integer player) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				View badge = playerBadges.get(player);
 				if (badge != null) {
 					LinearLayout users = (LinearLayout) findViewById(R.id.users);
 					users.removeView(badge);
 					playerBadges.remove(player);
 				}
 			}
 		});
 	};
 }
