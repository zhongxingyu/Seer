 package net.rptools.maptool.client.ui.commandpanel;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.Rectangle;
 import java.awt.event.MouseListener;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.UIManager;
 
 public class MessagePanel extends JPanel {
 
 	private StringBuffer messages;
 	private JTextPane textPane;
 	private JScrollPane scrollPane;
 	
 	public MessagePanel() {
 		setLayout(new BorderLayout());
 		messages = new StringBuffer();
 		textPane = new JTextPane();
 		textPane.setContentType("text/html");
 		textPane.setBorder(null);
 		textPane.setEditable(false);
 		
 		scrollPane = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setBorder(null);
 		
 		add(BorderLayout.CENTER, scrollPane);
 	}
 
	public void clearMessages() {
		messages.setLength(0);
		textPane.setText("");
	}
	
 	public void addMessage(final String message) {
 		
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				messages.append("<div>").append(message).append("</div>");
 				textPane.setText("<html <style>div {font-family: sans-serif; font-size: 11pt} body {margin: 5px, 5px, 5px, 5px}</style>><body>" + messages.toString() + "</body></html>");
 				textPane.scrollRectToVisible(new Rectangle(0, textPane.getSize().height, 1, 1));
 			}
 		});
 	}
 	
 }
