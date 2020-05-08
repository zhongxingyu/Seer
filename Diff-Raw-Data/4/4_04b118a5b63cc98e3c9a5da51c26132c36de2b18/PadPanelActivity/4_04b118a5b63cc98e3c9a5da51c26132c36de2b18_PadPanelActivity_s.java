 package net.thiagoalz.hermeto;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.thiagoalz.hermeto.audio.SoundManager;
 import net.thiagoalz.hermeto.control.ADKGameplayControl;
 import net.thiagoalz.hermeto.control.XMPPGameplayControl;
 import net.thiagoalz.hermeto.panel.GameManager;
 import net.thiagoalz.hermeto.panel.Position;
 import net.thiagoalz.hermeto.panel.controls.listeners.BPMBarListener;
 import net.thiagoalz.hermeto.panel.listeners.ConnectEvent;
 import net.thiagoalz.hermeto.panel.listeners.ExecutionEvent;
 import net.thiagoalz.hermeto.panel.listeners.ExecutionListener;
 import net.thiagoalz.hermeto.panel.listeners.MoveEvent;
 import net.thiagoalz.hermeto.panel.listeners.PlayerListener;
 import net.thiagoalz.hermeto.panel.listeners.SelectionEvent;
 import net.thiagoalz.hermeto.panel.listeners.SelectionListener;
 import net.thiagoalz.hermeto.player.Player;
 import android.app.AlertDialog;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.google.android.DemoKit.DemoKitActivity;
 
 public class PadPanelActivity extends DemoKitActivity implements SelectionListener, PlayerListener, ExecutionListener {
 	
 	private static final String tag = PadPanelActivity.class.getCanonicalName();
 	
 	private GameManager gameManager;
 	private SoundManager soundManager;
 	private ADKGameplayControl ADKControl;
 	@SuppressWarnings("unused")
 	private XMPPGameplayControl XMPPControl;
 	
 	private ImageButton[][] padsMatrix;
 	private TableLayout tableLayout;
 	
 	Player defaultPlayer;
 
 	Map<Player, PlayerNameView> playersName = new HashMap<Player, PlayerNameView>();
 	
 	private static final int ADK_PLAYERS = 4;
 	
 	
 	public PadPanelActivity(){
 		super();
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {		
 		super.onCreate(savedInstanceState);
 		configureScreen();
 		
 		soundManager = new SoundManager(this);
 		gameManager = GameManager.getInstance();
 		ADKControl = new ADKGameplayControl(gameManager, ADK_PLAYERS);
 		XMPPControl = new XMPPGameplayControl(gameManager);
 		gameManager.addSelectionListener(this);
 		gameManager.addExecutionListener(this);
 		defaultPlayer = gameManager.connectPlayer();
 		
 		constructView();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		gameManager.pause();
 	}
 	
 	@Override
 	public void onResume() {
		super.onPause();
		gameManager.start();
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		gameManager.pause();
 		soundManager.cleanUp();
 	}
 	
 	@Override
 	public void onRestart() {
 		super.onRestart();
 		soundManager = new SoundManager(this);
 	}
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		Log.d(tag, "Changing windows focus: " + hasFocus);
 		initializePlayersName();
 	}
 	
 	
 	private void configureScreen() {
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
     	requestWindowFeature(Window.FEATURE_NO_TITLE);
     	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     }
 	
 	private void constructView() {
 		setContentView(R.layout.padpanel);
 		tableLayout = (TableLayout) findViewById(R.id.padpanelgrid);
 		initializeSquarePanel();
 		initializeControls();
 	}
 	
 	private void initializeSquarePanel() {
 		padsMatrix = new ImageButton[gameManager.getColumns()][gameManager.getRows()];
 		for (int i = 0; i < padsMatrix.length; i++) {
 			TableRow tableRow = new TableRow(this);
 			for (int j = 0; j < padsMatrix[i].length; j++) {
 				ImageButton button = new ImageButton(this);
 				TableRow.LayoutParams params = new TableRow.LayoutParams(42, 42);
 				params.gravity = Gravity.CENTER;
 				button.setLayoutParams(params);
 				button.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.buttonstopped));
 				button.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						ImageButton selectedButton = (ImageButton) v;
 						for (int i = 0; i < padsMatrix.length; i++) {
 							for (int j = 0; j < padsMatrix[i].length; j++) {
 								if (padsMatrix[j][i] == selectedButton) {
 									MoveEvent event = new MoveEvent(defaultPlayer, defaultPlayer.getPosition(), new Position(j, i));
 									defaultPlayer.setPosition(new Position(j, i));
 									onPlayerMove(event);
 									gameManager.mark(defaultPlayer);
 								}
 							}
 						}
 						
 					}
 				});
 				padsMatrix[j][i] = button;
 				tableRow.addView(padsMatrix[j][i]);
 			}
 			tableLayout.addView(tableRow);
 		}
 	}
 	
 	private void initializePlayersName() {
 		RelativeLayout namesLayout = (RelativeLayout) findViewById(R.id.namesLayout);
 		Map<String, Player> players = gameManager.getPlayers();
 		
 		for (String playerID : players.keySet()) {
 			Player player = players.get(playerID);
 			PlayerNameView playerNameView = new PlayerNameView(this);
 			playerNameView.setText(player.getName());
 			playerNameView.setLocation(getLocation(player.getPosition()));
 			playersName.put(player, playerNameView);
 			namesLayout.addView(playerNameView);
 		}
 	}
 	
 	private Position getLocation(Position position) {
 		ImageButton button = padsMatrix[position.getX()][position.getY()];
 		int screenLocation[] = new int[2];
 		button.getLocationOnScreen(screenLocation);
 		return new Position(screenLocation[0], screenLocation[1]);
 	}
 	
 	private void initializeControls() {
 		SeekBar bpmBar = (SeekBar) findViewById(R.id.bpmBar);
 		bpmBar.setProgress(gameManager.getTimeSequence() / 4);
 		
 		TextView bpmView = (TextView) findViewById(R.id.bpmLabel);
 		bpmView.setText("BPM: " + gameManager.getTimeSequence());
 		bpmBar.setOnSeekBarChangeListener(new BPMBarListener(gameManager, bpmView, this));
 				
 		final ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (gameManager.isPlaying()) {
 					gameManager.pause();
 				} else {
 					gameManager.start();
 				}
 			}
 		});
 		
 		ImageButton stop = (ImageButton) findViewById(R.id.stop);
 		stop.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				gameManager.stop();
 			}
 		});
 		
 		ImageButton reset = (ImageButton) findViewById(R.id.reset);
 		reset.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				gameManager.reset();
 			}
 		});
 	}
 
 	@Override
 	public void onSelected(SelectionEvent event) {
 		int x = event.getPosition().getX();
 		int y = event.getPosition().getY();
 		padsMatrix[x][y].setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonselected));
 	}
 
 	@Override
 	public void onDeselected(SelectionEvent event) {
 		int x = event.getPosition().getX();
 		int y = event.getPosition().getY();
 		padsMatrix[x][y].setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonstopped));
 	}
 
 	@Override
 	public void onPlayerMove(MoveEvent event) {
 		final Position newLocation = getLocation(event.getNewPosition());
 		final Position oldLocation = getLocation(event.getOldPosition());
 	
 		MoveEvent newEvent = new MoveEvent(event.getPlayer(), oldLocation, newLocation);
 		PlayerNameView nameView = playersName.get(event.getPlayer());
 		nameView.onPlayerMove(newEvent);
 	}
 
 	@Override
 	public void onPlayerConnect(ConnectEvent event) {
 		Player player = event.getPlayer();
 		PlayerNameView playerNameView = new PlayerNameView(this);
 		playerNameView.setText(player.getName());
 		playerNameView.setLocation(getLocation(player.getPosition()));
 		playersName.put(player, playerNameView);
 		RelativeLayout namesLayout = (RelativeLayout) findViewById(R.id.namesLayout);
 		namesLayout.addView(playerNameView);
 	}
 	
 	@Override
 	public void onPlayerDisconnect(ConnectEvent event) {
 		RelativeLayout namesLayout = (RelativeLayout) findViewById(R.id.namesLayout);
 		PlayerNameView playerNameView = playersName.remove(event.getPlayer());
 		namesLayout.removeView(playerNameView);
 	}
 
 	@Override
 	public void onStart(ExecutionEvent event) {
 		ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.stop));
 		
 	}
 
 	@Override
 	public void onStop(ExecutionEvent event) {
 		ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.panel_play_button));
 	}
 
 	@Override
 	public void onReset(ExecutionEvent event) {
 		ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.panel_play_button));
 	}
 
 	@Override
 	public void onPause(ExecutionEvent event) {
 		ImageButton play = (ImageButton) findViewById(R.id.play);
 		play.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.panel_play_button));
 	}
 	
 	public void onStartPlayingGroup(final ExecutionEvent event) {
 		Log.d(tag, "Printing selected buttons with playing color ");
 		runOnUiThread(new Runnable() {
 			public void run() {
 				List<Position> positions = event.getPositions();
 				for (Position position : positions) {
 					ImageButton button = padsMatrix[position.getX()][position.getY()];
 					button.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.buttonplaying));
 					soundManager.playSound(position.getY());
 				}
 			}
 		});
 	}
 	
 	public void onStopPlayingGroup(final ExecutionEvent event) {
 		Log.d(tag, "Printing selected buttons with selected color ");
 		runOnUiThread(new Runnable() {
 			public void run() {
 				List<Position> positions = event.getPositions();
 				for (Position position : positions) {
 					ImageButton button = padsMatrix[position.getX()][position.getY()];
 					button.setBackgroundDrawable(PadPanelActivity.this.getResources().getDrawable(R.drawable.buttonselected));
 				}
 			}
 		});
 	}
 	
 	
 	////////////////////////ADK CODE/////////////////////
 	protected void handleJoyMessage(JoyMsg j) {
 //		if (mInputController != null) {
 //			mInputController.joystickMoved(j.getX(), j.getY());
 //		}
 	}
 
 	protected void handleLightMessage(LightMsg l) {
 //		if (mInputController != null) {
 //			mInputController.setLightValue(l.getLight());
 //		}
 	}
 
 	protected void handleTemperatureMessage(TemperatureMsg t) {
 //		if (mInputController != null) {
 //			mInputController.setTemperature(t.getTemperature());
 //		}
 	}
 
 	protected void handleSwitchMessage(SwitchMsg o) {
 //		if (mInputController != null) {
 //			byte sw = o.getSw();
 //			if (sw >= 0 && sw < 4) {
 //				mInputController.switchStateChanged(sw, o.getState() != 0);
 //			} else if (sw == 4) {
 //				mInputController
 //						.joystickButtonSwitchStateChanged(o.getState() != 0);
 //			}
 //		}
 		
 		if(o.getState() != 0){//Not release actions
 			//Test LECHUGA
 			Log.d("Lechuga","Button!");
 	        
 	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Check!");
 			AlertDialog alert = builder.create();
 			
 			alert.show();
 		}
 	}
 	
 	protected void handleSimpleJoyMessage(SwitchMsg k) {
 		ADKControl.processMessage(k.getSw()+"",k.getState()+"");
 	}
 }
