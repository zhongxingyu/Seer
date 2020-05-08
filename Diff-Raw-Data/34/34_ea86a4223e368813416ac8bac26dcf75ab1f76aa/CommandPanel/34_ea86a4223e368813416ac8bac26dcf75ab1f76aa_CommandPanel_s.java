 package net.rptools.maptool.client.ui.commandpanel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.ActionMap;
 import javax.swing.BorderFactory;
 import javax.swing.InputMap;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.Timer;
 
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.macro.MacroManager;
 import net.rptools.maptool.model.ObservableList;
 import net.rptools.maptool.model.TextMessage;
 
 public class CommandPanel extends JPanel implements Observer, MouseListener, MouseMotionListener {
 
 	private JTextField commandTextField;
 	private boolean mouseIsOver;
 	private MessagePanel messagePanel;
 	private Timer closeTimer;
 	
 	public CommandPanel() {
 		setLayout(new BorderLayout());
 		setBorder(BorderFactory.createLineBorder(Color.gray));
 		
 		add(BorderLayout.SOUTH, getCommandTextField());
 		add(BorderLayout.CENTER, getMessagePanel());
 		
 		SwingUtil.addMouseListenerToHierarchy(this, this);
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension(50, 50);
 	}
 
 	@Override
 	public Dimension getMinimumSize() {
 		return getPreferredSize();
 	}
 	
 	public JTextField getCommandTextField() {
 		if (commandTextField == null) {
 			commandTextField = new JTextField(){
 				@Override
 				protected void paintComponent(Graphics g) {
 					super.paintComponent(g);
 					
 					Dimension size = getSize();
 					g.setColor(Color.gray);
 					g.drawLine(0, 0, size.width, 0);
 				}
 			};
 			commandTextField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
 
 			ActionMap actions = commandTextField.getActionMap();
 			actions.put(AppActions.COMMIT_COMMAND_ID,
 							AppActions.COMMIT_COMMAND);
 			actions.put(AppActions.ENTER_COMMAND_ID, AppActions.ENTER_COMMAND);
 			actions.put(AppActions.CANCEL_COMMAND_ID, AppActions.CANCEL_COMMAND);
 
 			InputMap inputs = commandTextField.getInputMap();
 			inputs.put(KeyStroke.getKeyStroke("ESCAPE"),
 					AppActions.CANCEL_COMMAND_ID);
 			inputs.put(KeyStroke.getKeyStroke("ENTER"),
 					AppActions.COMMIT_COMMAND_ID);
 		}
 
 		return commandTextField;
 	}
 
 	/**
 	 * Execute the command in the command field.
 	 */
 	public void commitCommand() {
 		String text = commandTextField.getText().trim();
 		if (text.charAt(0) == '/') {
 			text = text.substring(1);
 		} else {
 			// Assume a "SAY"
 			text = "s " + text;
 		}
 		MacroManager.executeMacro(text);
 		cancelCommand();
 	}
 
 	/**
 	 * Cancel the current command in the command field.
 	 */
 	public void cancelCommand() {
 		commandTextField.setText("");
 		validate();
 		
 		if (!mouseIsOver) {
 			MapTool.getFrame().hideCommandPanel();
 		}
 	}
 	
 	public void startMacro() {
 		MapTool.getFrame().showCommandPanel();
 		
 		commandTextField.requestFocus();
 		commandTextField.setText("/");
 	}
 
 	public void startChat() {
 		MapTool.getFrame().showCommandPanel();
 		
 		commandTextField.requestFocus();
 	}
 
 	@Override
 	public void requestFocus() {
 		commandTextField.requestFocus();
 	}
 	
 	private MessagePanel getMessagePanel() {
 		if (messagePanel == null) {
 			messagePanel = new MessagePanel();
 		}
 
 		return messagePanel;
 	}
 
 	public void addMessage(String message) {
 		messagePanel.addMessage(message);
 	}
 	
 	////
 	// OBSERVER
 	public void update(Observable o, Object arg) {
 	    ObservableList<TextMessage> textList = MapTool.getMessageList();   
 	    ObservableList.Event event = (ObservableList.Event)arg; 
 	    switch (event) {
 	    case append:
 	      addMessage(textList.get(textList.size() - 1).getMessage());
 	      break;
 	    case add:
 	    case remove:
 	      //resetMessagePanel();
 	      break;
 	    case clear:
	      //clearMessagePanel();
 	      break;
 	    default:
 	      throw new IllegalArgumentException("Unknown event: " + event);
 	    } // endswitch
 	}	
 	
 	////
 	// MOUSE LISTENER
 	public void mouseClicked(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {
 		mouseIsOver = true;
 	}
 	public void mouseExited(MouseEvent e) {
 		mouseIsOver = false;
 
 		if (closeTimer != null) {
 			closeTimer.stop();
 		}
 		closeTimer = new Timer(500, new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				if (!mouseIsOver && getCommandTextField().getText().length() == 0) {
 					cancelCommand();
 				}
 				
 				if (closeTimer != null) {
 					closeTimer.stop();
 					closeTimer = null;
 				}
 			}
 		});
 		closeTimer.start();
 	}
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	
 	////
 	// MOUSE MOTION LISTENER
 	public void mouseDragged(MouseEvent e) {}
 	public void mouseMoved(MouseEvent e) {
 		mouseIsOver = true;
 	}
 }
