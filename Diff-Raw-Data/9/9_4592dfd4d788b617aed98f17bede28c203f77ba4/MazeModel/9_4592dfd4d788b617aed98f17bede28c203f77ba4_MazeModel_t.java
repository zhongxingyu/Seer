 package uk.ac.ox.cs.sokobanexam.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.ac.ox.cs.sokobanexam.model.Maze;
 import uk.ac.ox.cs.sokobanexam.model.Rules;
 import uk.ac.ox.cs.sokobanexam.model.ValidationResult;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Room;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Sprite;
 import uk.ac.ox.cs.sokobanexam.util.Dir;
 import uk.ac.ox.cs.sokobanexam.util.Point;
 
 /**
  * The Application State Model of the application.
  * This class contains no state information like MazeController does,
  * so you must take the necessary care only to use it as makes sense.
  */
 public class MazeModel {
 	
 	private Maze mMaze;
 	private Rules mRules;
 	
 	private Point mSelected;
 	private boolean mSelectionVisible = false;
 	private Point mHighlighted;
 	private String mHoverMessage;
 	private Class<? extends Sprite> mTypeForInsertion;
 	
 	public MazeModel(Maze maze, Rules rules) {
 		setMaze(maze);
 		setRules(rules);
 	}
 	
 	public void setRules(Rules rules) {
 		mRules = rules;
 	}
 	public Rules getRules() {
 		return mRules;
 	}
 	public void setMaze(Maze maze) {
 		mMaze = maze;
 		fireMazeChangeEvent();
 	}
 	public Maze getMaze() {
 		return mMaze;
 	}
 	
 	// --------------- Listeners
 	
 	private List<MazeChangeListener> mMazeChangeListeners
 			= new ArrayList<MazeChangeListener>();
 	public void addMazeChangeListener(MazeChangeListener listener) {
 		mMazeChangeListeners.add(listener);
 	}
 	private void fireMazeChangeEvent() {
 		for (MazeChangeListener listener : mMazeChangeListeners)
 			listener.onChange(this);
 	}
 	
 	private List<SelectionChangeListener> mSelectionChangeListeners 
 			= new ArrayList<SelectionChangeListener>();
 	public void addSelectionChangeListener(SelectionChangeListener listener) {
 		mSelectionChangeListeners.add(listener);
 	}
 	public void removeSelectionChangeListener(SelectionChangeListener listener) {
 		mSelectionChangeListeners.remove(listener);
 	}
 	private void fireSelectionChangeEvent(Point from, Point to) {
 		for (SelectionChangeListener listener : mSelectionChangeListeners)
 			listener.onSelectionChanged(this, from, to);
 	}
 	private void fireSelectionVisibilityChangeEvent() {
 		for (SelectionChangeListener listener : mSelectionChangeListeners)
 			listener.onSelectionVisibilityChanged(this);
 	}
 	private void fireHighlightChangeEvent(Point from, Point to) {
 		for (SelectionChangeListener listener : mSelectionChangeListeners)
 			listener.onHighlightChanged(this, from, to);
 	}
 	
 	// --------------- Domain shortcuts
 	// These helps us follow the "Law of Demeter" by avoiding getter chains.
 	
 	public boolean move(Dir direction) {
 		assert mRules.validateMaze(mMaze).isLegal();
 		if (!mRules.validateMove(mMaze,direction).isLegal())
 			return false;
 		mRules.applyMove(mMaze,direction);
 		fireMazeChangeEvent();
 		return true;
 	}
 	public boolean isWon() {
 		return mRules.isMazeWon(mMaze);
 	}
 	public ValidationResult validateMazePlayable() {
 		return mRules.validateMazePlayable(mMaze);
 	}
 	public ValidationResult validateMaze() {
 		return mRules.validateMaze(mMaze);
 	}
 	public Room getRoom(Point point) {
 		return mMaze.getRoom(point);
 	}
 	public void putRoom(Room room) {
 		mMaze.putRoom(room);
 		// An obvious optimization would be to here have a special event, that
 		// allowed repaint of only the changed point.
 		fireMazeChangeEvent();
 	}
 	
 	// --------------- Non domain specific state
 	
 	public void setTypeForInsertion(Class<? extends Sprite> typeForInsertion) {
 		mTypeForInsertion = typeForInsertion;
 		Point old = mSelected;
 		mSelected = null;
 		fireSelectionChangeEvent(old, null);
 	}
 	public Class<? extends Sprite> getTypeForInsertion() {
 		return mTypeForInsertion;
 	}
 	public void setSelected(Point point) {
 		Point old = mSelected;
 		mSelected = point;
 		fireSelectionChangeEvent(old, point);
 	}
 	public Point getSelected() {
 		return mSelected;
 	}
 	public void setHighlighted(Point point) {
 		Point old = mHighlighted;
 		mHighlighted = point;
 		fireHighlightChangeEvent(old, point);
 	}
 	public Point getHighlighted() {
 		return mHighlighted;
 	}
 	public void setSelectionVisible(boolean selectionVisible) {
 		mSelectionVisible = selectionVisible;
 		fireSelectionVisibilityChangeEvent();
 	}
 	public boolean isSelectionVisible() {
 		return mSelectionVisible;
 	}
 	public void setHoverMessage(String hoverMessage) {
 		mHoverMessage = hoverMessage;
		// We might want a more specific event here.
		fireMazeChangeEvent();
 	}
 	public String getHoverMessage() {
 		return mHoverMessage;
 	}
 }
