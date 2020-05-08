 package uk.ac.ox.cs.sokobanexam.ui;
 
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 
 import uk.ac.ox.cs.sokobanexam.domainmodel.sprites.Sprite;
 
 public class Toolbar extends JToolBar implements StateChangeListener {
 	private static final long serialVersionUID = -3632217082483213540L;
 	
 	private ButtonGroup mButtonGroup;
 
 	private JToggleButton mEditButton;
 	private JToggleButton mPlayButton;
 	private Map<Class<? extends Sprite>, JToggleButton> mCreateButtons
 			= new HashMap<Class<? extends Sprite>, JToggleButton>();
 	
 	public Toolbar(final MazeController controller) {
 		
 		controller.addStateChangeListener(this);
 		
 		setMargin(new Insets(10,10,10,10));
 		
 		final EditState editState = new EditState(Toolbar.this);
 		
 		// Insert buttons into toolbar
 		mButtonGroup = new ButtonGroup();
 		mEditButton = new JToggleButton("Edit");
 		mEditButton.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
 				controller.setCurrentState(editState);
 			}
 		});
 		mButtonGroup.add(mEditButton);
 		for (final Map.Entry<Class<? extends Sprite>, String> entry : MazeModel.PHYSICAL_SPRITES.entrySet()) {
 			JToggleButton createButton = new JToggleButton("Create " + entry.getValue());
 			mCreateButtons.put(entry.getKey(), createButton);
 			createButton.addActionListener(new ActionListener() {
 				@Override public void actionPerformed(ActionEvent e) {
 					CreateState createState = new CreateState(entry.getKey());
 					controller.setCurrentState(createState);
 				}
 			});
 			mButtonGroup.add(createButton);
 		}
 		mPlayButton = new JToggleButton("Play");
 		mPlayButton.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
 				controller.setCurrentState(new PlayState());
 			}
 		});
 		mButtonGroup.add(mPlayButton);
 		for (Enumeration<AbstractButton> e = mButtonGroup.getElements(); e.hasMoreElements(); )
 			add(e.nextElement());
 		
 		addSeparator();
 		
 		// Set edit button as default
 		mEditButton.doClick();
 	}
 
 	@Override
 	public void onStateChanged(MazeController source) {
 		if (source.getCurrentState() instanceof EditState)
 			if (!mEditButton.isSelected())
 				mEditButton.doClick();
 		if (source.getCurrentState() instanceof CreateState) {
 			JToggleButton createButton = mCreateButtons.get(((CreateState)source.getCurrentState()).getCreatedType());
 			if (!createButton.isSelected())
 				createButton.doClick();
 		}
 		if (source.getCurrentState() instanceof PlayState)
 			if (!mPlayButton.isSelected())
 				mPlayButton.doClick();
 	}
 }
