 package com.test9.irc.display;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.BoundedRangeModel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import javax.swing.text.html.HTML.Tag;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 
 import com.test9.irc.display.notifications.HilightNotificationFrame;
 import com.test9.irc.engine.User;
 
 public class OutputPanel extends JPanel implements HyperlinkListener, MouseWheelListener{
 
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
 	private static Font font = new Font("Lucida Grande", Font.BOLD, 12);
 
 	/**
 	 * Something fancy, I forget what.
 	 */
 	private HTMLDocument doc;
 
 	private SimpleAttributeSet privMsg = new SimpleAttributeSet();
 
 	private SimpleAttributeSet hyperlink = new SimpleAttributeSet();
 
 	private SimpleAttributeSet highlight = new SimpleAttributeSet();
 	private HTMLEditorKit editorKit;
 
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
 		textPane.setFont(font);
 		textPane.addHyperlinkListener(this);
 		textPane.setEditorKit(new HTMLEditorKit());
 		textPane.setEditable(false);
 		textPane.addMouseWheelListener(this);
 		
 		editorKit = (HTMLEditorKit) textPane.getEditorKit();
 		doc = (HTMLDocument) editorKit.createDefaultDocument();
 		textPane.setDocument(doc);
 		scrollPane = new JScrollPane(textPane);
 		scrollPane.getVerticalScrollBar().setPreferredSize(ChatWindow.getScrollBarDim());
 		scrollPane.setBackground(Color.BLACK);
 		scrollPane.setBorder(null);
 		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		add(scrollPane, BorderLayout.CENTER);
 
 	}
 
 	private void initAttributes() {
 		highlight.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.FALSE);
 		highlight.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.RED);
 		privMsg.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.WHITE);
 		privMsg.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
 		privMsg.addAttribute(StyleConstants.CharacterConstants.Alignment, StyleConstants.CharacterConstants.ALIGN_LEFT);
 		hyperlink.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLUE);
 		hyperlink.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
 		hyperlink.addAttribute(StyleConstants.CharacterConstants.Underline, Boolean.TRUE);
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
 			editorKit.insertHTML(doc, doc.getLength(),wrapInSpanTag(message+"\r\n", privMsg),0,0,null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//		textArea.append(message+"\r\n");
 		textPane.setCaretPosition(textPane.getDocument().getLength());
 	}
 
	Pattern urlPattern = Pattern.compile("(.*)(http.?://[\\p{Alnum}\\./?\\-_&=]*)([\\s\\p{Punct}]*.*)",Pattern.CASE_INSENSITIVE);
 
 	/**
 	 * Appends a new PRIVMSG to the text area.
 	 * @param nick The nick of the sender.
 	 * @param message The message string.
 	 */
 	void newMessage(User user, String nick, String message, boolean isLocal)
 	{
 		if(isLocal){
 			try {
 				if(user != null)
 					editorKit.insertHTML(doc, doc.getLength(),wrapInSpanTag("["+nick+"] ", user.getUserSimpleAttributeSet())+wrapInSpanTag(message, privMsg),0,0,null);
 				else editorKit.insertHTML(doc, doc.getLength(),wrapInSpanTag(message, privMsg),0,0,null);
 
 				textPane.setCaretPosition(textPane.getDocument().getLength());
 			} catch (BadLocationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else{
 			@SuppressWarnings("rawtypes")
 			SwingMethodInvoker.Parameter[] parameters;
 			SwingMethodInvoker invoker;
 			try {
 				String line = "";
 				if(user != null) {
 					line += wrapInSpanTag("["+nick+"] ",user.getUserSimpleAttributeSet()) + " ";
 				}
 				line+=wrapInSpanTag(message+"\r\n",privMsg);
 
 				parameters = new SwingMethodInvoker.Parameter[6];
 				parameters[0] = new SwingMethodInvoker.Parameter<HTMLDocument>(doc, HTMLEditorKit.class);
 				parameters[1] = new SwingMethodInvoker.Parameter<Integer>(doc.getLength(),int.class);
 				parameters[2] = new SwingMethodInvoker.Parameter<String>(line, String.class);
 				parameters[3] = new SwingMethodInvoker.Parameter<Integer>(0,int.class);
 				parameters[4] = new SwingMethodInvoker.Parameter<Integer>(0,int.class);
 				parameters[5] = new SwingMethodInvoker.Parameter<Tag>(null,Tag.class);
 
 				invoker = new SwingMethodInvoker(editorKit, "insertHTML", parameters);
 				SwingUtilities.invokeAndWait(invoker);
 
 				if(invoker.hasBeenExecuted()){
 					parameters = new SwingMethodInvoker.Parameter[]{new SwingMethodInvoker.Parameter<Integer>(textPane.getDocument().getLength(), int.class)};
 					invoker.reconfigure(textPane, "setCaretPosition", parameters);
 					SwingUtilities.invokeAndWait(invoker);
 				}
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private String wrapInSpanTag(String message, SimpleAttributeSet attrs) {
 		String messageWithLiveLinks = wrapLinks(message);
 		String styleString = getStyleStringFromSimpleAttributeSet(attrs);
 		return "<span style='"+styleString+"'>"+messageWithLiveLinks+"</span>";
 	}
 
 	private String wrapLinks(String message){
 		Matcher matcher = urlPattern.matcher(message);
 		String messageWithLiveLinks = "";
 		boolean matches = matcher.matches();
 		if(!matches) messageWithLiveLinks = message;
 		else messageWithLiveLinks += wrapLinks(matcher.group(1)) + "<a href=\""+matcher.group(2)+"\">"+matcher.group(2)+"</a>" + matcher.group(3);
 		System.out.println("   ");
 		System.out.println(message);
 		System.out.println(messageWithLiveLinks);
 		return messageWithLiveLinks;
 	}
 
 	private String getStyleStringFromSimpleAttributeSet(SimpleAttributeSet attrs) {
 		String styleString = "";
 		Color foregroundAttribute = (Color)attrs.getAttribute(StyleConstants.CharacterConstants.Foreground);
 		styleString += "color:"+getHtmlColor((Color)foregroundAttribute)+";";
 		Boolean bold = (Boolean)attrs.getAttribute(StyleConstants.CharacterConstants.Bold);
 		if(bold!=null && bold){
 			styleString+="font-weight:bold;";
 		}else{
 			styleString+="font-weight:normal;";	
 		}
 		styleString+="font-family:\""+font.getFamily()+"\";";
 		styleString+="font-size:\""+font.getSize()+"pt\";";
 		System.out.println(styleString);
 		return styleString;
 	}
 
 	private String getHtmlColor(Color color) {
 		String hexString = Integer.toHexString(color.getRGB());
 		return hexString.substring(2, hexString.length());
 	}
 
 	//TODO: This is an example of how to use the SwingMethodInvoker class and SwingUtilities
 	//      to manipulate Swing classes without worrying about concurrency issues.
 	//      Note: invokeAndWait will block execution of the calling thread until it completes.
 	//			  in this case this is a requirement to ensure the correct caret position is set. 
 	// 		Note: Also notice that the SwingMethodInvoker instance is reused.                      --Mumbles
 
 	void newMessageHighlight(String nick, String message) {
 		@SuppressWarnings("rawtypes")
 		SwingMethodInvoker.Parameter[] parameters;
 		SwingMethodInvoker invoker;
 
 		try {
 			parameters = new SwingMethodInvoker.Parameter[3];
 			parameters[0] = new SwingMethodInvoker.Parameter<Integer>(doc.getLength(),int.class);
 			parameters[1] = new SwingMethodInvoker.Parameter<String>("["+nick+"] "+message+"\r\n",String.class);
 			parameters[2] = new SwingMethodInvoker.Parameter<AttributeSet>(highlight,AttributeSet.class);
 			invoker = new SwingMethodInvoker(doc, "insertString", parameters);
 			SwingUtilities.invokeAndWait(invoker);
 			if(invoker.hasBeenExecuted()){
 				parameters = new SwingMethodInvoker.Parameter[]{new SwingMethodInvoker.Parameter<Integer>(textPane.getDocument().getLength(), int.class)};
 				invoker.reconfigure(textPane, "setCaretPosition", parameters);
 				SwingUtilities.invokeAndWait(invoker);
 			}
 		} catch (RuntimeException e) {
 			// TODO There are cleaner ways to handle the many things this exception could be, but unfortunately it could be anything.
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
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
 
 	@Override
 	public void hyperlinkUpdate(HyperlinkEvent arg0) {
 		if(arg0.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
 			if(Desktop.isDesktopSupported()){
 				try {
 					System.out.println("Link clicked on, browsing to : " + arg0.getDescription());
 					Desktop.getDesktop().browse(new URI(arg0.getDescription()));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (URISyntaxException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent arg0) {
		getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		BoundedRangeModel model = getScrollPane().getVerticalScrollBar().getModel();
 		int scrollAmount = (arg0.getWheelRotation()*30);
 		model.setValue(model.getValue()+scrollAmount);
		getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
 	}
 }
