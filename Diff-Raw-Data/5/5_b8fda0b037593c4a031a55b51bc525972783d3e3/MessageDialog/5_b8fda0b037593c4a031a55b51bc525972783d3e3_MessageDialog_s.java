 package gui;
 
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ScrollPaneConstants;
 
 public class MessageDialog {
 
 
 	JDialog conversation;
 	
 	
 	public MessageDialog(){
 		conversation = new JDialog(null, "Conversation", Dialog.ModalityType.MODELESS);
 		setupGUI();
 		conversation.pack();
 		conversation.setVisible(true);
 	}
 	
 	public MessageDialog(String dialogName){
 		conversation = new JDialog(null, dialogName, Dialog.ModalityType.MODELESS);
 		setupGUI();
 		conversation.pack();
 		conversation.setVisible(true);
 	}
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		MessageDialog test = new MessageDialog();
 	}
 
 	private void setupGUI(){
 		BoxLayout bl = new BoxLayout(conversation.getContentPane(), BoxLayout.Y_AXIS);
 		conversation.setLayout(bl); 
 		//TODO: for adding a menu later
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(new JMenu("One"));
 		menuBar.add(new JMenu("Two"));
 		menuBar.add(new JMenu("Three"));
 		conversation.setJMenuBar(menuBar);
 		
 		JPanel topPanel = new JPanel();
 		JPanel separatorPanel = new JPanel();
 		JPanel bottomPanel = new JPanel();
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 	//	convoPanel.setPreferredSize(new Dimension(300, 200));
 		topPanel.setLayout(new BorderLayout(0,5));
 		bottomPanel.setLayout(new BorderLayout(0,5));
 		JScrollPane scrollWindow = new JScrollPane();
 		scrollWindow.getViewport().setLayout(new BorderLayout(0, 5));
 		scrollWindow.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollWindow.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		JTextPane convoWindow = new JTextPane();
 		convoWindow.setText("The first test! \n And another one!\n\n\n\n\n\n\nTest");
 		convoWindow.setEditable(false);
 		//TODO: Will need a method that keeps a large string of the conversation and inserts usernames and shit
 		
 
 		JButton sendMsgButton = new JButton("Send Message");
 		
 		scrollWindow.getViewport().add(convoWindow);
 		topPanel.add(scrollWindow);
 		//conversation.add(convoPanel);
 		JTextField inputField = new JTextField();
 		inputField.setPreferredSize(new Dimension(300, 150));
 		bottomPanel.add(inputField);
 		conversation.add(topPanel);
 		conversation.add(separatorPanel);
 		conversation.add(bottomPanel);
 		conversation.add(Box.createHorizontalStrut(3));
 		
 		buttonPanel.add(sendMsgButton);
 		//conversation.add(Box.createVerticalGlue());
 		conversation.add(buttonPanel);
 	}
 	
 }
