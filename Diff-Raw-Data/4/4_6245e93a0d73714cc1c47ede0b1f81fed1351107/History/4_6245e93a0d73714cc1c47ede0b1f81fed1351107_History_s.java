 package jsk.sudoku.ui;
 
 import java.awt.event.ActionEvent;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.JMenu;
 import javax.swing.KeyStroke;
 
 import org.apache.log4j.Logger;
 
 import jsk.sudoku.model.Board;
 
 public class History extends JMenu {
 	private static final long serialVersionUID = 8651847418380452629L;
 	
 	private static final Logger log = Logger.getLogger(History.class);
 	
 	private final List<byte[]> history = new ArrayList<byte[]>();
 	private int historyIndex = 0;
 	private final Action undo = new Action("Undo", -1);
	private final Action redo = new Action("Redo", 1);
 	private final SudokuSolver owner;
 	
 	public History(String name, SudokuSolver owner) {
 		super(name);
 		this.owner = owner;
 		add(undo).setAccelerator(KeyStroke.getKeyStroke("ctrl z"));
 		add(redo).setAccelerator(KeyStroke.getKeyStroke("ctrl y"));
 		updateEnabled();
 	}
 	
 	public void record(Board board) {
 		invalidateNewer();
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		try {
 			board.save(out);
 		} catch (IOException e) {
 			log.error("impossible error writing to in-memory data", e);
 		}
 		history.add(out.toByteArray());
 		++historyIndex;
 		updateEnabled();
 	}
 	
 	public void clear() {
 		history.clear();
 		updateEnabled();
 	}
 	
 	public void invalidateNewer() {
 		if (historyIndex != history.size()) {
 			history.subList(historyIndex, history.size()).clear();
 		}
 	}
 	
 	public void undo() {
 		undo.actionPerformed(null);
 	}
 	
 	
 	private class Action extends AbstractAction {
 		private static final long serialVersionUID = 285572903799015476L;
 		
 		private final int movement;
 		Action(String name, int movement) {
 			super(name);
 			this.movement = movement;
 		}
 
 		@Override
 		public final void actionPerformed(ActionEvent event) {
 			if (!canMove()) {
 				log.warn("Tried to move but can't! index: " + historyIndex + " movement: "  + movement + " history size: " + history.size());
 			}
 			
 			historyIndex = historyIndex + movement;
 			
 			Board board;
 			try {
 				board = Board.load(new ByteArrayInputStream(history.get(historyIndex)));
 			} catch (IOException e) {
 				log.error("impossible error reading from in-memory data", e);
 				return;
 			}
 			
 			owner.setBoard(board, true);
 			
 			updateEnabled();
 		}
 		
 		private boolean canMove() {
 			int prospectiveIndex = historyIndex + movement;
 			return 0 <= prospectiveIndex && prospectiveIndex < history.size();
 		}
 	}
 	
 	private void updateEnabled() {
 		undo.setEnabled(undo.canMove());
 		redo.setEnabled(redo.canMove());
 	}
 	
 }
