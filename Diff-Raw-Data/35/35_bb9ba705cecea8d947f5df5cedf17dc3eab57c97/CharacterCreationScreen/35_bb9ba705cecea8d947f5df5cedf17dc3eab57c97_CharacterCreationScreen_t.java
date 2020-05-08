 package yuuki.ui.screen;
 
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 
 @SuppressWarnings("serial")
 public class CharacterCreationScreen extends Screen implements MouseListener {
 	
 	private ArrayList<CharacterCreationScreenListener> listeners;
 	
 	private JButton createCharacterButton;
 	
 	private JTextField nameField;
 	
 	private JSpinner levelField;
	
	private final String DEFAULT_NAME = "Dekky";
 
 	public CharacterCreationScreen(int width, int height) {
 		super(width, height);
 		setLayout(new FlowLayout());
 		listeners = new ArrayList<CharacterCreationScreenListener>();
 		createCharacterButton = new JButton("Create Character");
 		nameField = new JTextField(40);
 		levelField = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
 		ActionListener enterActionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				fireCreateCharacterClicked();
 			}
 		};
 		KeyListener enterKeyListener = new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					fireCreateCharacterClicked();
 				}
 			}
 		};
 		nameField.addActionListener(enterActionListener);
		nameField.setText(DEFAULT_NAME);
 		createCharacterButton.addMouseListener(this);
 		createCharacterButton.addKeyListener(enterKeyListener);
 		add(new JLabel("Name: "));
 		add(nameField);
 		add(new JLabel("Level: "));
 		add(levelField);
 		add(createCharacterButton);
 	}
 	
 	public void setInitialFocus() {
 		nameField.requestFocus();
 	}
 	
 	public void addListener(CharacterCreationScreenListener l) {
 		listeners.add(l);
 	}
 	
 	public String getEnteredName() {
 		return nameField.getText();
 	}
 	
 	public int getEnteredLevel() {
 		Integer obj = (Integer) levelField.getValue();
 		return obj.intValue();
 	}
 	
 	public void mouseClicked(MouseEvent e) {
 		Component c = e.getComponent();
 		if (c == createCharacterButton) {
 			fireCreateCharacterClicked();
 		}
 	}
 	
 	public void mouseReleased(MouseEvent e) {}
 	
 	public void mousePressed(MouseEvent e) {}
 	
 	public void mouseExited(MouseEvent e) {}
 	
 	public void mouseEntered(MouseEvent e) {}
 	
 	private void fireCreateCharacterClicked() {
 		for (CharacterCreationScreenListener l: listeners) {
 			l.createCharacterClicked();
 		}
 	}
 	
 }
