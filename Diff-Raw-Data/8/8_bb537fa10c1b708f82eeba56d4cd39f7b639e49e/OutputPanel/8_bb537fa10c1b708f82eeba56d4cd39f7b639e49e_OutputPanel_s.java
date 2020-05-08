 package com.test9.irc.display;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.Rectangle;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import com.test9.irc.engine.User;
 
 public class OutputPanel extends JPanel{
 
 	private static final long serialVersionUID = 3331343604631033360L;
 
 	/**
 	 * Used for calculating the bounds.
 	 */
 	private static Rectangle boundsRect;
 
 	/**
 	 * Name of the channel the panel is for.
 	 */
 	private String channel;
 
 	/**
 	 * Name of the server the panel is for.
 	 */
 	private String server;
 
 	/**
 	 * The scroll pane for the text area.
 	 */
 	private JScrollPane scrollPane;
 
 	/**
 	 * The text pane to hold the messages.
 	 */
 	private JTextPane textPane = new JTextPane();
 
 	/**
 	 * Font for the text area.
 	 */
 	private static Font font = new Font("Lucida Grande", Font.BOLD, 14);
 
 	/**
 	 * Something fancy, I forget what.
 	 */
 	private StyledDocument doc = textPane.getStyledDocument();
 
 	private SimpleAttributeSet privMsg = new SimpleAttributeSet();
 
 	private SimpleAttributeSet highlight = new SimpleAttributeSet();
 	
 	/**
 	 * Creates a new OutputPanel for a server or channel that
 	 * can be added to the JFrame.
 	 * @param server Name of the server.
 	 * @param channel Name of the channel.
 	 * @param width Width for new construction.
 	 * @param height Height for new construction.
 	 */
 	OutputPanel(String server, String channel, int width, int height)
 	{
 		initAttributes();
 		this.server = server;
 		this.channel = channel;
 		setLayout(new BorderLayout());
 		boundsRect = new Rectangle(0,0,width,height);
 		setBounds(boundsRect);
 		setBackground(Color.BLACK);
 		textPane.setBackground(Color.BLACK);
 		textPane.setMargin(new Insets(5,5,5,5));
 		textPane.setEditable(false);
 		textPane.setFont(font);
 		scrollPane = new JScrollPane(textPane);
 		scrollPane.getVerticalScrollBar().setPreferredSize (new Dimension(5,0));
 		scrollPane.setBackground(Color.BLACK);
 		add(scrollPane, BorderLayout.CENTER);
 
 	}
 
 	private void initAttributes() {
 		highlight.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
 		highlight.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.RED);
 		privMsg.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.WHITE);
 	}
 
 
 	/**
 	 * This is used to append a new string to a servers output 
 	 * panel.
 	 * @param message The new message.
 	 * @throws BadLocationException 
 	 */
 	void newMessage(String message)
 	{
 		try {
 			doc.insertString(doc.getLength(), message+"\r\n", privMsg);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//		textArea.append(message+"\r\n");
		textPane.setCaretPosition(textPane.getText().length());
 	}
 
 	/**
 	 * Appends a new PRIVMSG to the text area.
 	 * @param nick The nick of the sender.
 	 * @param message The message string.
 	 */
 	void newMessage(User user, String nick, String message)
 	{
 		try {
 			if(user != null)
 				doc.insertString(doc.getLength(),"["+nick+"] ", user.getUserSimpleAttributeSet());
 			doc.insertString(doc.getLength(), message+"\r\n", privMsg);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		textPane.setCaretPosition(textPane.getText().length());
 	}
 
 	/**
 	 * Sets the new bounds for resizing.
 	 * @param width
 	 * @param height
 	 */
 	static void setNewBounds(int width, int height)
 	{
 		boundsRect.setBounds(0, 0, width, height);
 	}
 
 
 	/**
 	 * @return the channel
 	 */
 	public String getChannel() {
 		return channel;
 	}
 
 
 	/**
 	 * @param channel the channel to set
 	 */
 	void setChannel(String channel) {
 		this.channel = channel;
 	}
 
 
 	/**
 	 * @return the scrollPane
 	 */
 	public JScrollPane getScrollPane() {
 		return scrollPane;
 	}
 
 
 	/**
 	 * @param scrollPane the scrollPane to set
 	 */
 	void setScrollPane(JScrollPane scrollPane) {
 		this.scrollPane = scrollPane;
 	}
 
 
 	/**
 	 * @return the textArea
 	 */
 	public JTextPane getTextArea() {
 		return textPane;
 	}
 
 
 	/**
 	 * @param textArea the textArea to set
 	 */
 	void setTextArea(JTextPane textArea) {
 		this.textPane = textArea;
 	}
 
 
 	/**
 	 * @return the serialversionuid
 	 */
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 
 	/**
 	 * @return the bounds
 	 */
 	public static Rectangle getBoundsRec() {
 		return boundsRect;
 	}
 
 
 	/**
 	 * @param bounds the bounds to set
 	 */
 	static void setBoundsRec(Rectangle bounds) {
 		OutputPanel.boundsRect = bounds;
 	}
 
 
 	/**
 	 * @return the server
 	 */
 	public String getServer() {
 		return server;
 	}
 
 
 	/**
 	 * @param server the server to set
 	 */
 	void setServer(String server) {
 		this.server = server;
 	}
 
 
 	public void newMessageHighlight(String nick, String message) {
 		try {
 			doc.insertString(doc.getLength(), "["+nick+"] "+message+"\r\n", highlight);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//		textArea.append(message+"\r\n");
		textPane.setCaretPosition(textPane.getText().length());
 
 	}
 }
