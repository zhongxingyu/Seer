 package uk.ac.ox.cs.sokobanexam.ui;
 
import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
import javax.swing.JPanel;
 
 import uk.ac.ox.cs.sokobanexam.domainmodel.Board;
 import uk.ac.ox.cs.sokobanexam.domainmodel.sprites.Floor;
 import uk.ac.ox.cs.sokobanexam.domainmodel.sprites.Nothing;
 import uk.ac.ox.cs.sokobanexam.domainmodel.sprites.Room;
 import uk.ac.ox.cs.sokobanexam.domainmodel.sprites.Sprite;
 import uk.ac.ox.cs.sokobanexam.util.Point;
 
 public class EditState implements ControllerState, MouseListener,
 		MouseMotionListener, KeyListener, ActionListener,
 		SelectionChangeListener, SpriteChangeListener {
 	
 	private MazeView mView;
 	private MazeModel mModel;
 	
 	private Toolbar mToolbar;
 	private JComponent mConfigurationPanel;
 	private JButton mDeleteButton;
 	
 	public EditState(Toolbar toolbar) {
 		mToolbar = toolbar;		
 		mDeleteButton = new JButton("Delete");
 		mDeleteButton.addActionListener(this);
 	}
 	
 	@Override
 	public void attach(MazeController controller, MazeModel model, MazeView view) {
 		mView = view;
 		mModel = model;
 		mView.addMouseListener(this);
 		mView.addMouseMotionListener(this);
 		mView.addKeyListener(this);
 		mView.requestFocusInWindow();
 		mModel.addSelectionChangeListener(this);
 		mModel.setSelectionVisible(true);
 		mToolbar.add(mDeleteButton);
 		onSelectionChanged(mModel, null, null);
 	}
 	
 	@Override
 	public void detach() {
 		mView.removeMouseListener(this);
 		mView.removeMouseMotionListener(this);
 		mView.removeKeyListener(this);
 		mModel.removeSelectionChangeListener(this);
 		mModel.setSelectionVisible(false);
 		if (mConfigurationPanel != null)
 			mToolbar.remove(mConfigurationPanel);
 		mToolbar.remove(mDeleteButton);
 		forceDoLayout();
 	}
 	
 	@Override
 	public void onSelectionChanged(MazeModel model, Point from, Point to) {
 		// Clear any old, custom configuration
 		if (mConfigurationPanel != null)
 			mToolbar.remove(mConfigurationPanel);
 		// If nothing is selected, we must disable the delete button
 		if (model.getSelected() == null) {
 			mDeleteButton.setEnabled(false);
 			mConfigurationPanel = null;
 		}
 		// Otherwise we generate a new component with the selected sprite
 		else {
 			mDeleteButton.setEnabled(true);
 			Room room = model.getBoard().getRoom(model.getSelected());
 			Sprite sprite = MazeModel.isEditableType(room.inner()) ? room.inner() : room;
 			SpriteConfigurationCreator confCreator = new SpriteConfigurationCreator(this);
 			sprite.accept(confCreator);
 			mConfigurationPanel = confCreator.getResult();
 			mToolbar.add(mConfigurationPanel);
 		}
 		
 		forceDoLayout();
 	}
 
 	private void forceDoLayout() {
 		// Hack to get relayouting to work.
 		mToolbar.doLayout();
 		mToolbar.revalidate();
 		mToolbar.repaint();
 		/*if (mToolbar.getParent() != null) {
 			Container parent = mToolbar.getParent();
 			while (!(parent instanceof JFrame))
 				parent = parent.getParent();
 			((JFrame)parent).pack();
 		}*/
 	}
 	
 	@Override
 	public void onSpriteChanged(Sprite oldSprite, Sprite newSprite) {
 		Board board = mModel.getBoard();
 		if (newSprite instanceof Room)
 			board.putRoom((Room)newSprite);
 		else board.putRoom(board.getRoom(oldSprite.point()).withInner(newSprite));
 		mModel.setBoard(board);
 		mModel.setSelected(mModel.getSelected());
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		mModel.setHighlighted(mView.pos2Point(e.getX(), e.getY()));
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		mouseMoved(e);
 	}
 	
 	@Override
 	public void mouseExited(MouseEvent e) {
 		mModel.setHighlighted(null);
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		Point point = mView.pos2Point(e.getX(), e.getY());
 		if (point == null) {
 			mModel.setSelected(null);
 			return;
 		}
 		Room room = mModel.getBoard().getRoom(point);
 		if (MazeModel.isEditableType(room.inner())
 				|| MazeModel.isEditableType(room)) {
 			mModel.setSelected(point);
 			// We request focus, so the delete keyboard shortcut works.
 			mView.requestFocusInWindow();
 		}
 		else
 			mModel.setSelected(null);
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		Point point = mView.pos2Point(e.getX(), e.getY());
 		if (mModel.getSelected() == null || point == null)
 			return;
 		
 		Room from = mModel.getBoard().getRoom(mModel.getSelected());
 		Room to = mModel.getBoard().getRoom(point);
 		if (from.point().equals(to.point()))
 			return;
 		
 		// See if we are moving an inner
 		if (MazeModel.isEditableType(from.inner())) {
 			// We can't move it to somewhere with an inner
 			if (MazeModel.isEditableType(to.inner())) {
 				JOptionPane.showMessageDialog(mView, "You can't drag on top of other things.");
 				return;
 			}
 			// We can move it somewhere else
 			else {
 				// Create new
 				mModel.getBoard().putRoom(to.withInner(from.inner().move(to.point())));
 				// Remove old
 				mModel.getBoard().putRoom(from.withInner(new Nothing(from.point())));
 			}
 		}
 		// If we are moving a room
 		else {
 			assert MazeModel.isEditableType(from);
 			// If there is already a room
 			if (MazeModel.isEditableType(to)) {
 				JOptionPane.showMessageDialog(mView, "You can't drag on top of other things.");
 				return;
 			}
 			// Move the room
 			else {
 				// Create new
 				mModel.getBoard().putRoom(from.withInner(to.inner()));
 				// Remove old
 				mModel.getBoard().putRoom(new Floor(from.point()));
 			}
 		}
 		
 		// Check validation
 		if (!mModel.getRules().validateBoard(mModel.getBoard())) {
 			// Restore board
 			mModel.getBoard().putRoom(to);
 			mModel.getBoard().putRoom(from);
 			JOptionPane.showMessageDialog(mView, "Dragging here is against the rules.");
 		}
 		else mModel.setSelected(to.point());
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_DELETE)
 			deleteSelected();
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// This is only connected to the delete button
 		deleteSelected();
 	}
 	
 	private void deleteSelected() {
 		Point point = mModel.getSelected();
 		if (point == null)
 			// Nothing selected
 			return;
 		Board board = mModel.getBoard();
 		Room room = board.getRoom(point);
 		if (MazeModel.isEditableType(room.inner()))
 			board.putRoom(room.withInner(new Nothing(room.point())));
 		else board.putRoom(new Floor(room.inner()));
 		mModel.setBoard(board);
 		mModel.setSelected(null);
 	}
 	
 	@Override public void mouseClicked(MouseEvent e) {}
 	@Override public void mouseEntered(MouseEvent e) {}
 	@Override public void keyTyped(KeyEvent e) {}
 	@Override public void keyReleased(KeyEvent e) {}
 	@Override public void onHighlightChanged(MazeModel mazeModel, Point from, Point to) {}
 }
