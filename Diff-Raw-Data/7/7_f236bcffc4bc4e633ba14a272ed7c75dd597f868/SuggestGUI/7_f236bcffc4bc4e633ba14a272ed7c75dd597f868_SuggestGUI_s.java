 package suggest;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JViewport;
 import javax.swing.KeyStroke;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Document;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import networking.Networking;
 
 public class SuggestGUI extends JPanel {
 	private JTextField input;
 	private JTextArea wikioutput;
 	private JTextArea duckoutput;
 	private JTextArea dictoutput;
 	private QueryService queryService;
 	private JPanel _suggestPanel;
 	private JPanel _networkPanel;
 	private JTextField _usernameField;
 	private JTextField _ipField;
 	private JPanel _findPanel;
 	private Networking _net;
 	private JTextPane _chatPane, _wikiPane;
 	private JTextArea _chatMessage;
 	private JScrollPane _chatScrollPane;
 	private BlockingQueue<String> _bqueue;
 	private SuggestThread _suggestThread;
 	private JButton _beHostButton, _joinButton, _sendMessageButton, _leaveButton, _backButton;
 	private int _role;
 	private ArrayList<ClickText> _textList;
 	private Stack<String> _back;
 	
 	public SuggestGUI(Dimension interfaceSize) {
 		super(new java.awt.BorderLayout());
 		
 		buildSuggestTab();
 		buildNetworkTab();
 		buildFindTab();
 		
 		JTabbedPane tabbedPane = new JTabbedPane();
 		
 		ImageIcon suggest = new ImageIcon("./lib/question.jpeg");
 		tabbedPane.addTab("Suggestions", suggest, _suggestPanel, "Get Suggestions");
 		
 		ImageIcon network = new ImageIcon("./lib/web.jpeg");
 		tabbedPane.addTab("Networking", network, _networkPanel, "Set Up Networking");
 		
 		ImageIcon find = new ImageIcon("./lib/find.jpeg");
 		tabbedPane.addTab("Find", find, _findPanel, "Find Stuff");
 		
 		this.add(tabbedPane);
 		this.setPreferredSize(interfaceSize);
 		this.setSize(interfaceSize);
 		queryService = new QueryService();
 		_bqueue = new LinkedBlockingQueue<String>();
 		_suggestThread = new SuggestThread();
 		_suggestThread.start();
 		_role = 0;
 		_back = new Stack<String>();
 	}
 	
 	public void setNetworking(Networking net) {
 		_net = net;
 		_net.setSuggestPanel(this);
 	}
 	
 	private void buildFindTab() {
 		_findPanel = new JPanel();
 		JPanel searchPanel = new JPanel();
 		JTextField searchField = new JTextField(25);
 		JPanel buttonPanel = new JPanel();
 		JButton searchButton = new JButton("Find it");
 		searchPanel.add(searchField, BorderLayout.CENTER);
 		buttonPanel.add(searchButton);
 		_findPanel.setLayout(new FlowLayout());
 		_findPanel.add(searchPanel);
 		_findPanel.add(buttonPanel);
 	}
 
 	public boolean networkingSet() {
 		return _net!=null;
 	}
 
 	private void buildNetworkTab() {
 		_networkPanel = new JPanel();
 		JPanel hostPanel = new JPanel();
 		JPanel clientPanel = new JPanel();
 		JPanel usernamePanel = new JPanel();
 		JLabel usernamelabel = new JLabel("Username: ");
 		_usernameField = new JTextField(15);
 		_usernameField.setEditable(true);
 		_beHostButton = new JButton("Host a Brainstorm");
 		_beHostButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(_usernameField.getText().isEmpty()) {
 					// no username specified
 					JOptionPane.showMessageDialog(_networkPanel, "You must have a username.", "No Username", JOptionPane.ERROR_MESSAGE);
 				}
 				else {
 					// call networking becomehost method
 					//_usernameField.getText for parameter
 					// check for bad connection boolean
 					if (_net != null) {
 						_role = 1;
 						if (_net.becomeHost(_usernameField.getText())) {
 							_chatMessage.setEnabled(true);
 							_chatPane.setEnabled(true);
 							_sendMessageButton.setEnabled(true);
 							_leaveButton.setEnabled(true);
 							_chatMessage.grabFocus();
 							_usernameField.setEnabled(false);
 							_ipField.setEnabled(false);
 							_beHostButton.setEnabled(false);
 							_joinButton.setEnabled(false);
 							SimpleAttributeSet set = new SimpleAttributeSet();
 							StyleConstants.setFontSize(set, 18);
 							StyleConstants.setForeground(set, Color.CYAN);
 							StyleConstants.setFontFamily(set, "Veranda");
 							StyledDocument doc = _chatPane.getStyledDocument();
 							StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 							doc.setParagraphAttributes(doc.getLength(), 15, set, true);
 							try {
 								doc.insertString(doc.getLength(), "You just joined the Brainstrom!\n", set);
 							} catch (BadLocationException e2) {
 								e2.printStackTrace();
 							}
 						}
 						else {
 							// handle connection error
 							connectionError();
 						}
 					} else {
 						System.out.println("suggest: networking is null, has not be set");
 					}
 				}
 			}
 		});
 		
 		JLabel ipLabel = new JLabel("IP Address: ");
 		_ipField = new JTextField(18);
 		_ipField.setEditable(true);
 		
 		JPanel joinPanel = new JPanel();
 		_joinButton = new JButton("Join a Brainstorm");
 		_joinButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(_ipField.getText().isEmpty()) {
 					// no ip address specified
 					JOptionPane.showMessageDialog(_networkPanel, "You must enter the host's IP address to connect to.", "Invalid IP Address", JOptionPane.ERROR_MESSAGE);
 				} else if(_usernameField.getText().isEmpty()) {
 					// no username specified
 					JOptionPane.showMessageDialog(_networkPanel, "You must enter a username before connecting.", "No Username ", JOptionPane.ERROR_MESSAGE);
 				}
 				else {
 					// call networking becomeclient method
 					// _ipField.getText and _usernameField.getText for parameters
 					// check for bad connection boolean
 					//
 					if (_net != null) {
 						_role = 2;
 						if(_net.becomeClient(_ipField.getText(), _usernameField.getText())) {
 							_chatMessage.setEnabled(true);
 							_chatPane.setEnabled(true);
 							_leaveButton.setEnabled(true);
 							_sendMessageButton.setEnabled(true);
 							_chatMessage.grabFocus();
 							_usernameField.setEnabled(false);
 							_ipField.setEnabled(false);
 							_beHostButton.setEnabled(false);
 							_joinButton.setEnabled(false);
 							SimpleAttributeSet set = new SimpleAttributeSet();
 							StyleConstants.setFontSize(set, 18);
 							StyleConstants.setForeground(set, Color.CYAN);
 							StyleConstants.setFontFamily(set, "Veranda");
 							StyledDocument doc = _chatPane.getStyledDocument();
 							StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 							doc.setParagraphAttributes(doc.getLength(), 15, set, true);
 							try {
 								doc.insertString(doc.getLength(), "You just joined the Brainstrom!\n", set);
 							} catch (BadLocationException e2) {
 								e2.printStackTrace();
 							}
 						}
 						else {
 							// handle connection error
 							connectionError();
 						}
 					} else {
 						System.out.println("suggest: networking is null, has not be set");
 					}
 				}
 			}
 		});
 		
 		JPanel leavePanel = new JPanel();
 		_leaveButton = new JButton("Leave Brainstorm");
 		_leaveButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				_chatMessage.setEnabled(false);
 				_chatPane.setEnabled(false);
 				_sendMessageButton.setEnabled(false);
 				_leaveButton.setEnabled(false);
 				_usernameField.grabFocus();
 				_usernameField.setEnabled(true);
 				_ipField.setEnabled(true);
 				_beHostButton.setEnabled(true);
 				_joinButton.setEnabled(true);
 				_role = 0;
 				try {
 					StyledDocument document = _chatPane.getStyledDocument();
 					document.remove(0, document.getLength());
 					document.insertString(0, "Chat:\n", null);
 				} catch (BadLocationException e1) {
 					e1.printStackTrace();
 				}
 				// tell networking you are leaving
 				_net.signOff();
 				
 			}
 			
 		});
 		_leaveButton.setEnabled(false);
 		leavePanel.add(_leaveButton);
 		
 		JPanel chatPanel = new JPanel();
 		_chatPane = createChatPane();
 		_chatScrollPane = new JScrollPane(_chatPane);
 		_chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		_chatScrollPane.setPreferredSize(new Dimension(340, 600));
 		chatPanel.add(_chatScrollPane);
 		JPanel messagePanel = new JPanel();
 		_chatMessage = new JTextArea(5, 20);
 		_chatMessage.setEditable(true);
 		_chatMessage.setLineWrap(true);
 		_chatMessage.setWrapStyleWord(true);
 		_sendMessageButton = new JButton("Send");
 		JScrollPane chatMessageScrollPane = new JScrollPane(_chatMessage, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
 		Action action = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addMessage();
 			}};
 		KeyStroke keyStroke = KeyStroke.getKeyStroke("ENTER");
 		InputMap im = _chatMessage.getInputMap();
 		_chatMessage.getActionMap().put(im.get(keyStroke), action);
 		_chatMessage.setEnabled(false);
 		_chatPane.setEnabled(false);
 		
 		
 		_sendMessageButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addMessage();
 				_chatMessage.grabFocus();
 			}
 		});
 		messagePanel.add(chatMessageScrollPane,BorderLayout.WEST);
 		messagePanel.add(_sendMessageButton, BorderLayout.EAST);
 		
 		_networkPanel.setLayout(new FlowLayout());
 		usernamePanel.add(usernamelabel,BorderLayout.WEST);
 		usernamePanel.add(_usernameField, BorderLayout.EAST);
 		hostPanel.add(_beHostButton, BorderLayout.CENTER);
 		clientPanel.add(ipLabel, BorderLayout.WEST);
 		clientPanel.add(_ipField, BorderLayout.EAST);
 		joinPanel.add(_joinButton, BorderLayout.CENTER);
 		
 		_networkPanel.add(usernamePanel);
 		_networkPanel.add(hostPanel);
 		_networkPanel.add(clientPanel);
 		_networkPanel.add(joinPanel);
 		_networkPanel.add(leavePanel);
 		_networkPanel.add(chatPanel);
 		_networkPanel.add(messagePanel);
 		
 	}
 	
 	// Customized entering and exiting methods below
 	
 //	// networking needs to call me please
 //	public void newUser(String username) {
 //		SimpleAttributeSet set = new SimpleAttributeSet();
 //		StyleConstants.setFontSize(set, 18);
 //		StyleConstants.setForeground(set, Color.CYAN);
 //		StyledDocument doc = _chatPane.getStyledDocument();
 //		try {
 //			doc.insertString(doc.getLength(), username + "just joined the Brainstrom!\n", set);
 //		} catch (BadLocationException e) {
 //			e.printStackTrace();
 //		}
 //	}
 	
 //	// networking needs to call me please
 //	public void userExited(String username) {
 //		SimpleAttributeSet set = new SimpleAttributeSet();
 //		StyleConstants.setFontSize(set, 18);
 //		StyleConstants.setForeground(set, Color.RED);
 //		StyledDocument doc = _chatPane.getStyledDocument();
 //		try {
 //			doc.insertString(doc.getLength(), username + "just left the Brainstrom!\n", set);
 //		} catch (BadLocationException e) {
 //			e.printStackTrace();
 //		}
 //	}
 	
 	public void connectionError() {
 		Object[] options = {"Ok", "Retry Connection"};
 		int n = JOptionPane.showOptionDialog(_networkPanel, "A connection error has disrupted the Brainstorm.", "Connection Error", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
 		if (n == 1) {
 			System.out.println("retried");
 			// retry
 			if (_role == 1) {
 				if (_net.becomeHost(_usernameField.getText())) {
 					_chatMessage.setEnabled(true);
 					_chatPane.setEnabled(true);
 					_sendMessageButton.setEnabled(true);
 					_leaveButton.setEnabled(true);
 					_chatMessage.grabFocus();
 					_usernameField.setEnabled(false);
 					_ipField.setEnabled(false);
 					_beHostButton.setEnabled(false);
 					_joinButton.setEnabled(false);
 					SimpleAttributeSet set = new SimpleAttributeSet();
 					StyleConstants.setFontSize(set, 18);
 					StyleConstants.setForeground(set, Color.MAGENTA);
 					StyleConstants.setFontFamily(set, "Veranda");
 					StyledDocument doc = _chatPane.getStyledDocument();
 					StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 					doc.setParagraphAttributes(doc.getLength(), 10, set, false);
 					try {
 						doc.insertString(doc.getLength(), "Connection Restored!\n", set);
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 				} else {
 					connectionError();
 				}
 				
 			} else if (_role == 2) {
 				// retry
 				if (_net.becomeClient(_ipField.getText(), _usernameField.getText())) {
 					_chatMessage.setEnabled(true);
 					_chatPane.setEnabled(true);
 					_sendMessageButton.setEnabled(true);
 					_leaveButton.setEnabled(true);
 					_chatMessage.grabFocus();
 					_usernameField.setEnabled(false);
 					_ipField.setEnabled(false);
 					_beHostButton.setEnabled(false);
 					_joinButton.setEnabled(false);
 					SimpleAttributeSet set = new SimpleAttributeSet();
 					StyleConstants.setFontSize(set, 18);
 					StyleConstants.setForeground(set, Color.MAGENTA);
 					StyleConstants.setFontFamily(set, "Veranda");
 					StyledDocument doc = _chatPane.getStyledDocument();
 					StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 					doc.setParagraphAttributes(doc.getLength(), 10, set, false);
 					try {
 						doc.insertString(doc.getLength(), "Connection Restored!\n", set);
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 				}
 				else {
 					connectionError();
 				}
 					
 			}
 		}
 		else {
 			_role = 0;
 			_chatMessage.setEnabled(false);
 			_chatPane.setEnabled(false);
 			_sendMessageButton.setEnabled(false);
 			_leaveButton.setEnabled(false);
 			_usernameField.grabFocus();
 			_usernameField.setEnabled(true);
 			_ipField.setEnabled(true);
 			_beHostButton.setEnabled(true);
 			_joinButton.setEnabled(true);
 		}
 	}
 	
 	private JTextPane createChatPane() {
 		JTextPane pane = new JTextPane();
 		pane.setSize(20, 50);
 		pane.setEditable(false);
 		// Chat Header
 		SimpleAttributeSet set = new SimpleAttributeSet();
 		StyleConstants.setBold(set, true);
 		StyleConstants.setFontSize(set, 26);
 		StyleConstants.setFontFamily(set, "Veranda");
 		StyleConstants.setAlignment(set, StyleConstants.ALIGN_CENTER);
 		StyledDocument doc = pane.getStyledDocument();
 		try {
 			doc.insertString(doc.getLength(), "Chat:\n", set);
 			doc.setParagraphAttributes(0, doc.getLength(), set, true);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		
 		return pane;
 	}
 
 	public void addMessage(){
 		String text = _chatMessage.getText();
 		if(!text.equals("")){
 			_chatMessage.setText("");
 			SimpleAttributeSet set = new SimpleAttributeSet();
 			StyledDocument doc = _chatPane.getStyledDocument();
 			StyleConstants.setFontFamily(set, "Veranda");
 			StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 			doc.setParagraphAttributes(doc.getLength(), text.length(), set, true);
 			StyleConstants.setBold(set, true);
 			StyleConstants.setForeground(set, Color.BLUE);
 			StyleConstants.setFontSize(set, 16);
 			try {
 				doc.insertString(doc.getLength(), "Me:   ", set);
 				set = new SimpleAttributeSet();
 				StyleConstants.setFontSize(set, 14);
 				doc.insertString(doc.getLength(), text + "\n", set);
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}
 			_chatMessage.grabFocus();
 			_net.sendMessage(text);
 			JViewport vport = _chatScrollPane.getViewport();
 			Point vp = vport.getViewPosition();
 			vp.translate(0, _chatScrollPane.getSize().height);
 			_chatPane.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
 			_chatPane.repaint();
 		}
 	}
 	
 	public void newMessage(String username, String message) {
 		System.out.println("newmessage called");
 		SimpleAttributeSet set = new SimpleAttributeSet();
 		StyledDocument doc = _chatPane.getStyledDocument();
 		StyleConstants.setFontFamily(set, "Veranda");
 		StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 		doc.setParagraphAttributes(doc.getLength(), message.length(), set, true);
 		StyleConstants.setBold(set, true);
 		StyleConstants.setForeground(set, Color.GREEN);
 		StyleConstants.setFontSize(set, 16);
 		try {
 			doc.insertString(doc.getLength(), username + ":   ", set);
 			set = new SimpleAttributeSet();
 			StyleConstants.setFontSize(set, 14);
 			doc.insertString(doc.getLength(), message + "\n", set);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		JViewport vport = _chatScrollPane.getViewport();
 		Point vp = vport.getViewPosition();
 		vp.translate(0, _chatScrollPane.getSize().height);
 		_chatPane.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
 		_chatPane.repaint();
 	}
 	
 	private void buildSuggestTab() {
 		_suggestPanel = new JPanel();
 		JPanel inputpanel = new JPanel();
 		input = new JTextField(25);
 		input.setEditable(true);
 		inputpanel.add(input);
 		
 		Action action = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String query = input.getText();
 				if (!query.isEmpty()) {
 					suggest(query);
 				}
 			}};
 		KeyStroke keyStroke = KeyStroke.getKeyStroke("ENTER");
 		InputMap im = input.getInputMap();
 		input.getActionMap().put(im.get(keyStroke), action);
 		
 		JPanel buttonPanel = new JPanel();
 		JButton suggestButton = new JButton("Suggestions?");
 		suggestButton.addActionListener(new SuggestionListener());
 		buttonPanel.add(suggestButton);
 		
 		JPanel wikiPanel = new JPanel();
 		
 //		wikioutput = new JTextArea(20, 50);
 //		wikioutput.setEditable(false);
 //		wikioutput.setLineWrap(true);
 //		wikioutput.setWrapStyleWord(true);
 //		JScrollPane wikiScrollPane = new JScrollPane(wikioutput);
 		
 		_wikiPane = new JTextPane();
 		_wikiPane.setSize(20, 50);
 		_wikiPane.setEditable(false);
 		// Chat Header
 		_wikiPane.addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				
 			}
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 				
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				
 			}
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				Point point = e.getPoint();
 				int viewToModel = _wikiPane.viewToModel(point);
 				searchIt(viewToModel);
 				System.out.println("VIEW: " + viewToModel);
 			}
 		});
 		_textList = new ArrayList<ClickText>();
 		
 		JScrollPane wikiScrollPane = new JScrollPane(_wikiPane);
 		wikiScrollPane.setVerticalScrollBarPolicy(
 		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		wikiScrollPane.setPreferredSize(new Dimension(340, 600));
 		wikiPanel.add(wikiScrollPane);
 		JPanel backPanel = new JPanel();
 		_backButton = new JButton("Back");
 		_backButton.setEnabled(false);
 		_backButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				_back.pop();
 				String popBack = _back.pop();
 				suggest(popBack);
 				input.setText(popBack);
 				if(_back.size() <= 1) {
 					_backButton.setEnabled(false);
 				}
 			}
 		});
 		backPanel.add(_backButton);
 		
 		JPanel dictPanel = new JPanel();
 		dictoutput = new JTextArea(20, 50);
 		dictoutput.setEditable(false);
 		dictoutput.setLineWrap(true);
 		dictoutput.setWrapStyleWord(true);
 		JScrollPane dictScrollPane = new JScrollPane(dictoutput);
 		dictScrollPane.setVerticalScrollBarPolicy(
 		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		dictScrollPane.setPreferredSize(new Dimension(340, 600));
 		dictPanel.add(dictScrollPane);
 		
 		JPanel duckPanel = new JPanel();
 		duckoutput = new JTextArea(20, 50);
 		duckoutput.setEditable(false);
 		duckoutput.setLineWrap(true);
 		duckoutput.setWrapStyleWord(true);
 		JScrollPane duckScrollPane = new JScrollPane(duckoutput);
 		duckScrollPane.setVerticalScrollBarPolicy(
 		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		duckScrollPane.setPreferredSize(new Dimension(340, 600));
 		duckPanel.add(duckScrollPane);
 		
 		JTabbedPane tabbedPane = new JTabbedPane();
 		
 		ImageIcon wiki = new ImageIcon("./lib/wiki.jpeg");
 		tabbedPane.addTab("Wikipedia", wiki, wikiPanel,
 		"Wikipedia");
 		
 		ImageIcon google = new ImageIcon("./lib/dict.jpeg");
 		tabbedPane.addTab("Dictionary", google, dictPanel,
 		"Google Dictionary");
 		
 		ImageIcon duck = new ImageIcon("./lib/duckduck.jpeg");
 		tabbedPane.addTab("DuckDuckGo", duck, duckPanel,
 		"DuckDuckGo");
 		
 		_suggestPanel.setLayout(new FlowLayout());
 		_suggestPanel.add(inputpanel);
 		_suggestPanel.add(buttonPanel);
 		_suggestPanel.add(tabbedPane);
 		_suggestPanel.add(backPanel);
 	}
 	
 	public void searchIt(int cursor) {
 		for (ClickText text:_textList) {
 			if (cursor >= text.getStart() && cursor <= text.getEnd()) {
 				System.out.println(text.getQuery());
 				input.setText(text.getQuery());
 				suggest(text.getQuery());
 //				try {
 //					String result = queryService.submit(text.getQuery(), 0).get();
 //					styleWiki(result);
 //				} catch (InterruptedException e) {
 //					e.printStackTrace();
 //				} catch (ExecutionException e) {
 //					e.printStackTrace();
 //				} catch (BadLocationException e) {
 //					e.printStackTrace();
 //				}
 				break;
 			}
 		}
 	}
 
 	public void suggest(String query) {
 		try {
 			_bqueue.put(query);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void submitQuery(String query) {
 		_back.push(query);
 		if(_back.size() > 1) {
 			_backButton.setEnabled(true);
 		}
 		_textList = new ArrayList<ClickText>();
 		List<Future<String>> futures = new ArrayList<Future<String>>();
 		for (int i = 0; i < 3; i++) {
 			futures.add(i, queryService.submit(query, i));
 		}
 		int j =0;
 		for (Future<String> future:futures) {
 			try {
 				String result = future.get();
 				if (j == 0) {
 					if (result.startsWith("#REDIRECT")) {
 						int index = result.indexOf("[");
 						int index2 = result.indexOf("]", index);
 						String requery = result.substring(index+2, index2);
 						result = queryService.submit(requery, 0).get();
 					}
 					try {
 						styleWiki(result);
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 					//wikioutput.setText(result);
 				}
 				else if (j==1) {
 					dictoutput.setText(result);
 				}
 				else if (j==2) {
 					duckoutput.setText(result);
 				}
 				j++;
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			} catch (ExecutionException e3) {
 				e3.printStackTrace();
 			}
 		}
 		
 	}
 	
 	private void styleWiki(String result) throws BadLocationException {
 		StyledDocument doc = _wikiPane.getStyledDocument();
 		doc.remove(0, doc.getLength());
 		SimpleAttributeSet set = new SimpleAttributeSet();
 		SimpleAttributeSet click = new SimpleAttributeSet();
 		StyleConstants.setForeground(click, Color.BLUE);
 		StyleConstants.setItalic(click, true);
 		int end = 0;
 		int index = result.indexOf("[[");
 		int index2 = result.indexOf("]]", index);
 		int middle = -1;
 		String query = "";
 		String text = "";
 		while (index >= 0 && index2 >= 0) {
 			doc.insertString(doc.getLength(), result.substring(end, index), set);
 			middle = result.indexOf("|", index);
 			if (middle > index && middle < index2) {
 				query = result.substring(index+2, middle);
 				text = result.substring(middle+1, index2);
 				end = index2-3-query.length();
 				doc.insertString(doc.getLength(), text, click);
 				ClickText clickText = new ClickText(query, index, end);
 				_textList.add(clickText);
 				result = result.substring(0, index) + result.substring(middle+1, index2) + result.substring(index2+2, result.length());
 			} else {
 				query = result.substring(index+2, index2);
 				end = index2-2;
 				doc.insertString(doc.getLength(), query, click);
 				ClickText clickText = new ClickText(query, index, end);
 				_textList.add(clickText);
 				result = result.substring(0, index) + result.substring(index+2, index2) + result.substring(index2+2, result.length());
 			}
 			
 			index = result.indexOf("[[");
 			index2 = result.indexOf("]]", index);
 		}
 		
 		doc.insertString(doc.getLength(), result.substring(end, result.length()), set);
 		
 	}
 
 	private class SuggestThread extends Thread {
 		
 		@Override
 		public void run() {
 			while (true) {
 				try {
 					String query = _bqueue.take();
 					submitQuery(query);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private class SuggestionListener implements ActionListener {
 		
 		public void actionPerformed(ActionEvent e) {
 			String query = input.getText();
 			if (!query.isEmpty()) {
 				suggest(query);
 			}
 		}
 		
 		
 	}
 }
