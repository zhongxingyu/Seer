 package net.thiagoalz.hermeto.panel;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.thiagoalz.hermeto.panel.listeners.SelectionEvent;
 import net.thiagoalz.hermeto.panel.listeners.SelectionListener;
 import net.thiagoalz.hermeto.player.DefaultPlayer;
 import net.thiagoalz.hermeto.player.Player;
 import net.thiagoalz.hermeto.player.Player.Direction;
 import net.thiagoalz.hermeto.player.PlayersManager;
 import android.util.Log;
 
 /**
  * Manages the interaction of the users and the panel.
  * 
  * @author Gabriel Ozeas de Oliveira
  * @version 0.1
  */
 public class GameManager implements SquarePanelManager, PlayersManager, ExecutionControl {
 	
 	private static final int COLUMNS_CONF = 16;
 	private static final int ROWS_CONF = 16;
 	
 	private static GameManager instance;
 	
 	private static final String tag = GameManager.class.getCanonicalName();
 	
 	private int columns;
 	private int rows;
 	
 	private Map<String, Player> players = new LinkedHashMap<String, Player>();
 	private int playerCounter = 0;
 	
 	private boolean playing;
 	
 	private Set<Position> markedSquares = new LinkedHashSet<Position>();
 	
 	private Timer sequencer;
 	private int currentPlayingLine = 0;
 	private int timeSequence = 120;
 	
 	private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();
 	private List<ExecutionListener> executionListeners = new ArrayList<ExecutionListener>();
 	
 	public synchronized static GameManager getInstance(){
 		return getInstance(COLUMNS_CONF, ROWS_CONF);
 	}
 	
 	public synchronized static GameManager getInstance(int columns, int rows) {
 		if (instance == null)
 			instance = new GameManager(columns, rows);
 		return instance;
 	}
 	
 	/**
 	 * Constructor that receives the dimension of the panel.
 	 * 
 	 * @param columns The number of columns of the panel.
 	 * @param rows The number of rows of the panel.
 	 */
 	private GameManager(int columns, int rows) {
 		Log.d(tag, "Creating game with [" + columns + ", " + rows + "]");
 		this.columns = columns > 1 ? columns : 2;
 		this.rows = rows > 1 ? columns : 2;
 	}
 	
 	@Override
 	public boolean move(Player player, Direction direction) {
 		int x = player.getPosition().getX();
 		int y = player.getPosition().getY();
 		
 		Log.d(tag, "Actual position of the player: [" + x + ", " + y + "]");
 		
 		switch (direction) {
 			case LEFT:
 				if (x - 1 < 0) {
 					return false;
 				} else {
 					Log.d(tag, "Changing position of the player: [" + (x - 1) + ", " + y + "]");
 					player.getPosition().setX(x - 1);
 					return true;
 				}
 				
 			case RIGHT:
 				if (x + 1 >= columns) {
 					return false;
 				} else {
 					Log.d(tag, "Changing position of the player: [" + (x + 1) + ", " + y + "]");
 					player.getPosition().setX(x + 1);
 					return true;
 				}
 				
 			case UP:
 				if (y - 1 < 0) {
 					return false;
 				} else {
 					Log.d(tag, "Changing position of the player: [" + x + ", " + (y - 1) + "]");
 					player.getPosition().setY(y - 1);
 					return true;
 				}
 				
 			case DOWN:
 				if (y + 1 >= rows) {
 					return false;
 				} else {
 					Log.d(tag, "Changing position of the player: [" + x + ", " + (y + 1) + "]");
 					player.getPosition().setY(y + 1);
 					return true;
 				}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean mark(Player player) {
 		Position selectedPosition = new Position(player.getPosition().getX(), player.getPosition().getY());
 		if (markedSquares.contains(selectedPosition)) {
 			markedSquares.remove(selectedPosition);
 			notifyDeselection(player, selectedPosition);
 			return false;
 		} else {
 			Log.d(tag, "Marking square [" + player.getPosition().getX() + ", "+ player.getPosition().getY() +"]");
 			markedSquares.add(player.getPosition());
 			notifySelection(player, selectedPosition);
 			return true;
 		}
 	}
 	
 	@Override
 	public Position[] getMarkedSquares() {
 		Log.d(tag, markedSquares.size() + " squares marked");
 		return (Position[]) markedSquares.toArray();
 	}
 
 	public int getColumns() {
 		return columns;
 	}
 
 	public int getRows() {
 		return rows;
 	}
 
 	@Override
 	public Player connectPlayer() {
 		String playerID = "playerID-" + System.nanoTime();
 		String playerName = "Player " + (++playerCounter);
 		
 		Position position = null;
 		Random myRandom = new Random(System.currentTimeMillis());
 		position = new Position(myRandom.nextInt(columns), myRandom.nextInt(rows));
 		
 		DefaultPlayer player = new DefaultPlayer(playerName, playerID, this);
 		player.setPosition(position);
 		
 		Log.d(tag, "Connection " + playerName + "("+ playerID +") at the position [" 
 				+ position.getX() + ", " + position.getY() + "]");
 		players.put(player.getId(), player);
 		return player;
 	}
 
 	@Override
 	public void disconnectPlayer(Player player) {
 		players.remove(player.getId());
 	}
 
 	@Override
 	public Map<String, Player> getPlayers() {
 		return players;
 	}
 	
 	@Override
 	public Player getPlayer(String playerID) {
 		return players.get(playerID);
 	}
 	
 	public void addSelectionListener(SelectionListener selectionListener) {
 		this.selectionListeners.add(selectionListener);
 	}
 	
 	public void addExecutionListener(ExecutionListener executionListener) {
 		this.executionListeners.add(executionListener);
 	}
 	
 	public void removeSelectionListener(SelectionListener selectionListener) {
 		this.selectionListeners.remove(selectionListener);
 	}
 	
 	public void removeExecutionListener(ExecutionListener executionListener) {
 		this.executionListeners.remove(executionListener);
 	}
 	
 	private void notifySelection(Player player, Position position) {
 		SelectionEvent event = new SelectionEvent(player, position);
 		for (SelectionListener listener : selectionListeners) {
 			listener.onSelected(event);
 		}
 	}
 	
 	private void notifyDeselection(Player player, Position position) {
 		SelectionEvent event = new SelectionEvent(player, position);
 		for (SelectionListener listener : selectionListeners) {
 			listener.onDeselected(event);
 		}
 	}
 
 	@Override
 	public boolean isPlaying() {
 		return playing;
 	}
 
 	@Override
 	public void start() {
 		playing = true;
 		
 		if (sequencer != null) {
 			sequencer.cancel();
 		}
 		
 		sequencer = new Timer();
 		sequencer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
 				if (currentPlayingLine > 0) {
 					stopPlayingGroup(currentPlayingLine - 1);
 				}
 				if (currentPlayingLine >= COLUMNS_CONF) {
 					currentPlayingLine = 0;
 				}else {
 					currentPlayingLine++;
 				}
 				startPlayingGroup(currentPlayingLine);
 			}
 		}, 10, 200);
 		
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onStart(event);
 		}
 	}
 
 	@Override
 	public void stop() {
 		playing = false;
 		if(sequencer != null){
 			sequencer.cancel();
 			sequencer = null;
 		}
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onStop(event);
 		}
 	}
 
 	@Override
 	public void pause() {
 		playing = false;
 		ExecutionEvent event = new ExecutionEvent();
 		if(sequencer != null){
 			sequencer.cancel();
 			sequencer = null;
 		}
 		for (ExecutionListener listener : executionListeners) {
 			listener.onPause(event);
 		}
 	}
 
 	@Override
 	public void reset() {
 		for (Position position : markedSquares) {
 			notifyDeselection(null, position);
 		}
 		markedSquares = new LinkedHashSet<Position>();
 		playing = false;
 		
 		ExecutionEvent event = new ExecutionEvent();
 		for (ExecutionListener listener : executionListeners) {
 			listener.onReset(event);
 		}
 	}
 	
 	private void startPlayingGroup(int group) {
 		Log.d(tag, "Starting playing group " + group);
 		List<Position> playingPositions = new ArrayList<Position>();
 		for (Position position : markedSquares) {
			if (position.getY() == group) {
 				playingPositions.add(position);
 			}
 		}
 		if (playingPositions.size() > 0) {
 			ExecutionEvent event = new ExecutionEvent();
 			event.setPositions(playingPositions);
 			for (ExecutionListener listener : executionListeners) {
 				listener.onStartPlayingGroup(event);
 			}
 		}
 	}
 	
 	private void stopPlayingGroup(int group) {
 		Log.d(tag, "Stoping playing group " + currentPlayingLine);
 		List<Position> playingPositions = new ArrayList<Position>();
 		for (Position position : markedSquares) {
			if (position.getY() == group) {
 				playingPositions.add(position);
 			}
 		}
 		if (playingPositions.size() > 0) {
 			ExecutionEvent event = new ExecutionEvent();
 			event.setPositions(playingPositions);
 			for (ExecutionListener listener : executionListeners) {
 				listener.onStopPlayingGroup(event);
 			}
 		}
 		
 	}
 }
