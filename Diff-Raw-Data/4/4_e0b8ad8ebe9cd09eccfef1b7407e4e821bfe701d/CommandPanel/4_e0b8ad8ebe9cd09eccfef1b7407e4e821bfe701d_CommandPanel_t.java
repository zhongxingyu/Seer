 package net.rptools.maptool.client.ui.commandpanel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.BorderFactory;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.macro.MacroManager;
 import net.rptools.maptool.model.ObservableList;
 import net.rptools.maptool.model.TextMessage;
 
 public class CommandPanel extends JPanel implements Observer {
 
 	private JTextField commandTextField;
 	private MessagePanel messagePanel;
 	private List<String> commandHistory = new LinkedList<String>();
 	private int commandHistoryIndex;
 	
 	public CommandPanel() {
 		setLayout(new BorderLayout());
 		setBorder(BorderFactory.createLineBorder(Color.gray));
 		
 		add(BorderLayout.NORTH, createTopPanel());
 		add(BorderLayout.SOUTH, getCommandTextField());
 		add(BorderLayout.CENTER, getMessagePanel());
 	}
 	
 	public String getMessageHistory() {
 		return messagePanel.getMessagesText();
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
 			actions.put(AppActions.COMMAND_UP_ID, new CommandHistoryUpAction());
 			actions.put(AppActions.COMMAND_DOWN_ID, new CommandHistoryDownAction());
 			
 			InputMap inputs = commandTextField.getInputMap();
 			inputs.put(KeyStroke.getKeyStroke("ESCAPE"),
 					AppActions.CANCEL_COMMAND_ID);
 			inputs.put(KeyStroke.getKeyStroke("ENTER"),
 					AppActions.COMMIT_COMMAND_ID);
 			inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), AppActions.COMMAND_UP_ID);
 			inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), AppActions.COMMAND_DOWN_ID);
 		}
 
 		return commandTextField;
 	}
 
 	private JPanel createTopPanel() {
 		JPanel panel = new JPanel(new GridBagLayout()){
 			@Override
 			protected void paintComponent(Graphics g) {
 				g.setColor(Color.gray);
 				
 				Dimension size = getSize();
 				g.drawLine(0, size.height-1, size.width, size.height-1);
 			}
 		};
 
 		GridBagConstraints constraints = new GridBagConstraints();
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		constraints.weightx = 1;
 		constraints.anchor = GridBagConstraints.WEST;
 		
 		panel.add(createMacroButtonPanel(), constraints);
 		
 		constraints.weightx = 0;
 		constraints.gridx = 1;
 		JLabel spacer = new JLabel();
 		spacer.setMinimumSize(new Dimension(20, 10));
 		panel.add(spacer, constraints);
 		
 		constraints.gridx = 2;
 		constraints.insets = new Insets(0, 0, 0, 10);
 		panel.add(createCloseButton(), constraints);
 		
 		return panel;
 	}
 	
 	private JPanel createMacroButtonPanel() {
 		JPanel panel = new JPanel();
 		panel.setOpaque(false);
 		
 		for (int i = 1; i < 20; i++) {
 			panel.add(new MacroButton(Integer.toString(i), null));
 		}
 		
 		return panel;
 	}
 	
 	private JButton createCloseButton() {
 		JButton button = new JButton("X");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				MapTool.getFrame().hideCommandPanel();
 			}
 		});
 		
 		return button;
 	}
 	
 	/**
 	 * Execute the command in the command field.
 	 */
 	public void commitCommand() {
 		String text = commandTextField.getText().trim();
 		if (text.length() == 0) {
 			return;
 		}
 
 		// Command history
 		// Don't store up a bunch of repeats
 		if (commandHistory.size() == 0 || !text.equals(commandHistory.get(commandHistory.size()-1))) {
 			commandHistory.add(text);
 		}
 		commandHistoryIndex = commandHistory.size();
 		
 		if (text.charAt(0) != '/') {
 			// Assume a "SAY"
 			text = "/s " + text;
 		}
 		MacroManager.executeMacro(text);
 		commandTextField.setText("");
 	}
 	
 	public void clearMessagePanel() {
 		messagePanel.clearMessages();
 	}
 
 	/**
 	 * Cancel the current command in the command field.
 	 */
 	public void cancelCommand() {
 		commandTextField.setText("");
 		validate();
 		
 		MapTool.getFrame().hideCommandPanel();
 	}
 	
 	public void startMacro() {
 		MapTool.getFrame().showCommandPanel();
 		
 		commandTextField.requestFocusInWindow();
 		commandTextField.setText("/");
 	}
 
 	public void startChat() {
 		MapTool.getFrame().showCommandPanel();
 		
 		commandTextField.requestFocusInWindow();
 	}
 
 	private class CommandHistoryUpAction extends AbstractAction {
 		
 		public void actionPerformed(ActionEvent e) {
 			if (commandHistory.size() == 0) {
 				return;
 			}
 			commandHistoryIndex --;
 			if (commandHistoryIndex < 0) {
 				commandHistoryIndex = 0;
 			}
 
 			commandTextField.setText(commandHistory.get(commandHistoryIndex));
 		}
 	}
 	
 	private class CommandHistoryDownAction extends AbstractAction {
 		
 		public void actionPerformed(ActionEvent e) {
 			if (commandHistory.size() == 0) {
 				return;
 			}
 			commandHistoryIndex ++;
 			if (commandHistoryIndex >= commandHistory.size()) {
 				commandTextField.setText("");
 				commandHistoryIndex = commandHistory.size();
 			} else {
 				commandTextField.setText(commandHistory.get(commandHistoryIndex));
 			}
 		}
 	}
 	
 	@Override
 	public void requestFocus() {
 		commandTextField.requestFocus();
 	}
 	
 	private MessagePanel getMessagePanel() {
 		if (messagePanel == null) {
 			messagePanel = new MessagePanel();
 			messagePanel.addFocusListener(new FocusAdapter() {
 				public void focusGained(java.awt.event.FocusEvent e) {
 					getCommandTextField().requestFocusInWindow();
 				}
 			});
 		}
 
 		return messagePanel;
 	}
 
 	public void addMessage(String message) {
 		messagePanel.addMessage(message);
 	}
 	
 	public class MacroButton extends JButton {
 		
 		private String command;
 		
 		public MacroButton(String label, String command) {
 			super(label);
 			setCommand(command);
 			addMouseListener(new MouseHandler());
 		}
 		
 		public void setCommand(String command) {
 			this.command = command;
 			setBackground(command != null ? Color.orange : null);
 			
 			String tooltip = "Left click to execute, Right click to set, User '/' at the end of command to execute immediately";
 			setToolTipText(command != null ? command : tooltip);
 		}
 		
 		private class MouseHandler extends MouseAdapter {
 			@Override
 			public void mousePressed(MouseEvent e) {
 
 				if (SwingUtilities.isLeftMouseButton(e)) {
 					if (command != null) {
 						JTextField commandField = MapTool.getFrame().getCommandPanel().getCommandTextField();
 
 						String commandToExecute = command;
 						
 						boolean commitCommand = command.endsWith("/");
 						if (commitCommand) {
 							// Strip the execute directive
 							commandToExecute = command.substring(0, command.length()-1);
 						}
 						
 						commandField.setText(commandToExecute);
 						commandField.requestFocusInWindow();
 						
 						if (commitCommand) {
 							MapTool.getFrame().getCommandPanel().commitCommand();
 						}
 					}
 				}
 				if (SwingUtilities.isRightMouseButton(e)) {
 					
 					String newCommand = JOptionPane.showInputDialog(MapTool.getFrame(), "Command:", command);
					if (newCommand != null) {
						setCommand(newCommand);
					}
 				}
 			}
 		}
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
 	      clearMessagePanel();
 	      break;
 	    default:
 	      throw new IllegalArgumentException("Unknown event: " + event);
 	    } // endswitch
 	}	
 	
 }
