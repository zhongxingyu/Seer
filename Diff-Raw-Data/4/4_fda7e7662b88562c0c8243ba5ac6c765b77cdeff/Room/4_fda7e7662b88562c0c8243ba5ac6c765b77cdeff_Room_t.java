 package ui.room;
 
 import java.util.List;
 import java.util.TreeSet;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ExtendedModifyEvent;
 import org.eclipse.swt.custom.ExtendedModifyListener;
 import org.eclipse.swt.custom.MovementEvent;
 import org.eclipse.swt.custom.MovementListener;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.wb.swt.layout.grouplayout.GroupLayout;
 import org.eclipse.wb.swt.layout.grouplayout.GroupLayout.ParallelGroup;
 import org.eclipse.wb.swt.layout.grouplayout.GroupLayout.SequentialGroup;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
 import scripting.Script;
 import scripting.ScriptManager;
 import shared.ControlCodeParser;
 import shared.RoomManager;
 import connection.Connection;
 import connection.KEllyBot;
 
 
 @Data
 @EqualsAndHashCode(callSuper = false)
 public class Room extends Composite {
 
 	private WhoListener curListener;
 	
 	private class WhoListener implements Listener {
 			public void handleEvent(Event event) {
 				Point point = new Point(event.x, event.y);
 				final TreeItem item = who.getItem(point);
 	
 				if (item != null && item.getData() != null
 						&& item.getData() instanceof User) {
 					Menu m = new Menu(item.getParent().getShell(),
 							SWT.POP_UP);
 					basicItems(item, m);
 					ctcpItems(item, m);
 					operatorItems(item, m);
 					customItems(item, m);
 					item.getParent().setMenu(m);
 				}
 			}
 
 			//TODO: make this support multiple context scripts
 			private void customItems(final TreeItem item, Menu m) {
 				final Script contextScript = findContextScript();
 				if(contextScript == null) return;
 				Object[] arr = contextScript.invoke("getContextCommands");
 				for(final Object s : arr){
 					MenuItem mitem = new MenuItem(m, SWT.PUSH);
 					mitem.setText((String)s);
 					mitem.addSelectionListener(new SelectionListener() {
 
 						@Override
 						public void widgetDefaultSelected(
 								SelectionEvent arg0) {
 						}
 
 						@Override
 						public void widgetSelected(SelectionEvent arg0) {
 							contextScript.invoke((String)s, getBot(), item.getData(), cChannel.getChannel());
 						}
 					});
 				}
 			}
 
 			private Script findContextScript() {
 				for(Script s : ScriptManager.scripts){
 					if(s.getFunctions().contains("getContextCommands")){
 						return s;
 					}
 				}
 				return null;
 			}
 
 			private void ctcpItems(TreeItem item, Menu m) {
 				final User tUser = (User) item.getData();
 				
 				Menu parent = new Menu(m);
 				MenuItem mitem = new MenuItem(m, SWT.CASCADE);
 				mitem.setText("CTCP");
 				mitem.setMenu(parent);
 				//TODO: make this a smarter list per person based on CLIENTINFO
 				createCTCP(tUser, parent, "CLIENTINFO");
 				createCTCP(tUser, parent, "FINGER");
 				createCTCP(tUser, parent, "PING");
 				createCTCP(tUser, parent, "TIME");
 				createCTCP(tUser, parent, "USERINFO");
 				createCTCP(tUser, parent, "VERSION");
 			}
 
 			private void createCTCP(final User tUser, Menu parent, final String command) {
 				MenuItem mitem = new MenuItem(parent, SWT.PUSH);
 				mitem.setText(command);
 				mitem.addSelectionListener(new SelectionListener() {
 
 					@Override
 					public void widgetDefaultSelected(
 							SelectionEvent arg0) {
 					}
 
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						bot.sendCTCPCommand(tUser, command);
 					}
 				});
 			}
 
 			private void basicItems(final TreeItem item, Menu m) {
 				MenuItem mitem = new MenuItem(m, SWT.PUSH);
 				mitem.setText("Query");
 				mitem.addSelectionListener(new SelectionListener() {
 
 					@Override
 					public void widgetDefaultSelected(
 							SelectionEvent arg0) {
 					}
 
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						serverConnection.createRoom(item.getText(), IO);
 
 					}
 				});
 			}
 			
 			private void operatorItems(final TreeItem item, Menu m) {
 				final Channel thisChan = cChannel.getChannel();
 				final User tUser = (User) item.getData();
 				if(bot.getUserBot().getChannelsOpIn().contains(thisChan)){
 					if(!tUser.getChannelsOpIn().contains(thisChan)){
 						MenuItem mitem = new MenuItem(m, SWT.PUSH);
 						mitem.setText("Op");
 						mitem.addSelectionListener(new SelectionListener() {
 
 							@Override
 							public void widgetDefaultSelected(
 									SelectionEvent arg0) {
 								//TODO: write this
 							}
 
 							@Override
 							public void widgetSelected(SelectionEvent arg0) {
 								bot.op(thisChan, tUser);
 
 							}
 						});
 					}
 					if(!tUser.getChannelsVoiceIn().contains(thisChan)){
 						MenuItem mitem = new MenuItem(m, SWT.PUSH);
 						mitem.setText("Voice");
 						mitem.addSelectionListener(new SelectionListener() {
 
 							@Override
 							public void widgetDefaultSelected(
 									SelectionEvent arg0) {
 								//TODO: write this
 							}
 
 							@Override
 							public void widgetSelected(SelectionEvent arg0) {
 								bot.voice(thisChan, tUser);
 
 							}
 						});
 					}
 				}
 			}
 		}
 	
 	// TODO: fake tooltips (http://dev.eclipse.org/viewcvs/viewvc.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet125.java?view=co)
 	
 	// TODO: change to booleans
 	public static final int WHO = 1, TOPIC = 2, IO = 4;
 
 	private CustomChannel cChannel;
 	private Connection serverConnection;
 	private KEllyBot bot;
 
 	// make clickable links by changing the style and the data of the individual
 	// messages
 	private StyledText output, input, topicBox;
 
 	private Tree who;
 
 	// private int layout;
 
 	// variables used with the tool tip
 	private int userCount;
 	private String lastMessage;
 	private String channelName;
 
 	public Room(Composite c, int style, int layout, Tree tree,
 			String channelstr, Connection newConnection, Channel channel) {
 		super(c, style);
		this.channelName = channelstr;
 		setServerConnection(newConnection);
 		this.setBot(newConnection.getBot());
 		this.cChannel = new CustomChannel(tree, channelstr, newConnection,
 				channel, this);
 		instantiate(layout);
 	}
 
 	public void instantiate(int layout) {
 		MovementListener linkClickListener = new MovementListener() {
 			@Override
 			public void getNextOffset(MovementEvent arg0) {
 				String[] message = arg0.lineText.split(" ");
 				int offset = arg0.offset - arg0.lineOffset;
 				for (String s : message) {
 					if (arg0.lineText.indexOf(s) > offset || arg0.lineText.indexOf(s)+s.length() < offset)
 						continue;
 					
 					if (s.contains("://")) {
 						Program.launch(s);
 					}
 				}
 			}
 
 			@Override
 			public void getPreviousOffset(MovementEvent arg0) {
 
 			}
 		};
 		
 		if ((layout & TOPIC) != 0) {
 			topicBox = new StyledText(this, SWT.BORDER | SWT.WRAP);
 			topicBox.setEditable(false);
 			topicBox.addWordMovementListener(linkClickListener);
 		}
 		if ((layout & IO) != 0) {
 			// set up the output window
 			output = new StyledText(this, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP
 					| SWT.MULTI);
 			output.setEditable(false);
 			output.addWordMovementListener(linkClickListener);
 
 			// set up the input box and it's enter-key listener
 			input = new StyledText(this, SWT.BORDER);
 			input.addKeyListener(new KeyAdapter() {
 				@Override
 				public void keyPressed(KeyEvent e) {
 					// CR == Carriage Return == Enter
 					if (e.character == SWT.CR) {
 						if (cChannel != null && serverConnection != null) {
 							serverConnection.getBot().sendMessage(
									channelName,
 									input.getText().replaceAll("\r\n", ""));
 						}
 						input.setText("");
 					}
 										
 					if(e.stateMask == SWT.CTRL) {
 						switch(e.keyCode) {
 						//KR's formatting stuff
 							case 'O': case 'o': //Normal (kills all formatting)
 								insertCode("\u000f"); break;
 							case 'B': case 'b': //Bold
 								insertCode("\u0002"); break;
 							case 'U': case 'u': //Underlin
 								insertCode("\u001f"); break;
 							case 'R': case 'r': //Italic
 								insertCode("\u0016"); break; 
 							case 'K': case 'k': //Color
 								insertCode("\u0003"); break;
 								
 						//key combinations
 							case 'A': case 'a': //Select all
 								input.selectAll();
 						}
 					}
 				}
 				
 				private void insertCode(String insertCode)
 				{
 					int insertPos = input.getCaretOffset();
 					input.replaceTextRange(insertPos,0,insertCode);
 					input.setCaretOffset(insertPos+1);
 				}
 			});
 			//prevent selected text from disappearing when you hit enter
 			input.addExtendedModifyListener(new ExtendedModifyListener(){
 
 				public void modifyText(ExtendedModifyEvent e) {
 					String text = ((StyledText)e.widget).getText();
 					if(text.contains(""+SWT.CR))
 						((StyledText) e.widget).setText((text.substring(0,
 								e.start) + e.replacedText + text
 								.substring(e.start))
 								.replaceAll("\r\n", ""));
 					
 				}
 			});
 		}
 
 		if ((layout & WHO) != 0) {
 			who = new Tree(this, SWT.BORDER);
 			updateWhoListener();
 			who.addListener(SWT.MouseDoubleClick, new Listener() {
 
 				@Override
 				public void handleEvent(Event event) {
 					Point point = new Point(event.x, event.y);
 					TreeItem item = who.getItem(point);
 					if (item != null) {
 						serverConnection.createRoom(item.getText(), IO);
 					}
 				}
 			});
 
 		}
 
 		// generate the anchors for the windows
 		GroupLayout gl_composite = new GroupLayout(this);
 
 		SequentialGroup ss = gl_composite.createSequentialGroup();
 		ParallelGroup p2 = gl_composite
 				.createParallelGroup(GroupLayout.LEADING);
 
 		if ((layout & TOPIC) != 0) {
 			p2.add(topicBox, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE);
 		}
 		if ((layout & IO) != 0) {
 			p2.add(output, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE);
 			p2.add(input, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE);
 		}
 		ss.add(p2);
 		if ((layout & WHO) != 0) {
 			ss.add(who, 150, 150, 150);
 		}
 		gl_composite.setHorizontalGroup(gl_composite.createParallelGroup(
 				GroupLayout.LEADING).add(ss));
 
 		ParallelGroup p = gl_composite.createParallelGroup(GroupLayout.LEADING);
 		SequentialGroup s = gl_composite.createSequentialGroup();
 
 		if ((layout & TOPIC) != 0) {
 			s.add(topicBox, GroupLayout.DEFAULT_SIZE, 15, 18);
 		}
 		if ((layout & IO) != 0) {
 			if ((layout & TOPIC) == 0) {
 				s.add(output, GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE);
 			} else {
 				s.add(output, GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE);
 			}
 			s.add(input);
 		}
 		p.add(s);
 		if ((layout & WHO) != 0) {
 			p.add(who, GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE);
 		}
 		gl_composite.setVerticalGroup(p);
 
 		this.setLayout(gl_composite);
 
 		// initial tab tool tip text setting
 		userCount = cChannel.getChannel() != null ? cChannel.getChannel()
 				.getUsers().size() : 0;
 		lastMessage = "N/A";
 		channelName = cChannel.getChannelString();
 	}
 	
 	private void updateWhoListener() {
 		if(curListener!=null){
 			who.removeListener(SWT.MouseDown, curListener);
 		}
 		curListener = new WhoListener();
 		who.addListener(SWT.MouseDown, curListener);
 	}
 
 	public void updateTopic() {
 		if(topicBox==null)return;
 		final String topic = this.getCChannel().getChannel().getTopic();
 		final String strippedTopic = Colors.removeFormattingAndColors(topic);
 		RoomManager.getMain().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				topicBox.setText(strippedTopic);
 				
 				List<StyleRange> styleRanges = ControlCodeParser.parseControlCodes(topic,
 						topicBox.getText().length() - strippedTopic.length() );
 				
 				for(StyleRange styleRange : styleRanges.toArray(new StyleRange[styleRanges.size()]))
 					topicBox.setStyleRange(styleRange);
 				
 				for (String s : strippedTopic.split(" ")) {
 					if (s.contains("://")) {
 						// TODO: make this conform to the global list, and make the
 						// global list work
 						Color blue = new Color(topicBox.getDisplay(), 0, 0, 255);
 						StyleRange styleRange = new StyleRange();
 						styleRange.start = topicBox.getCharCount()
 								- strippedTopic.length() + strippedTopic.indexOf(s);
 						styleRange.length = s.length();
 						styleRange.foreground = blue;
 						styleRange.data = s;
 						styleRange.underline = true;
 						styleRange.underlineStyle = SWT.UNDERLINE_LINK;
 						topicBox.setStyleRange(styleRange);
 					}
 				}
 				
 				topicBox.setToolTipText(topic);
 				updateToolTipText();
 			}
 		});
 	}
 
 	public void updateToolTipText() {
 //		 cChannel.getItemRef().setToolTipText("Channnel: " + channelName +
 //		 "\nCurrent Users: " + userCount +
 //		 "\nLast Message: " + lastMessage);
 	}
 
 	private void updateUserCount() {
 		userCount = cChannel.getChannel().getUsers().size();
 	}
 
 	public void updateLastMessage(String lastMessage) {
 		this.lastMessage = lastMessage;
 	}
 
 	public void updateWho() {
 		if(who==null)return;
 		final Room c = this;
 		RoomManager.getMain().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				updateUserCount();
 				
 				getWho().removeAll();
 		
 				Channel chan = c.getCChannel().getChannel();
 		
 				if (chan.getOwners().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Owners");
 					for (User u : new TreeSet<User>(chan.getOwners())) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u,
 								c.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 				if (chan.getSuperOps().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Super-Ops");
 					for (User u : new TreeSet<User>(chan.getSuperOps())) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u,
 								c.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 				if (chan.getOps().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Ops");
 					for (User u : new TreeSet<User>(chan.getOps())) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u,
 								c.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 				if (chan.getHalfOps().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Half-Ops");
 					for (User u : new TreeSet<User>(chan.getHalfOps())) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u,
 								c.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 				if (chan.getVoices().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Voices");
 					for (User u : new TreeSet<User>(chan.getVoices())) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u,
 								c.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 				
 				if (chan.getNormalUsers().size() > 0) {
 					TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
 					t.setText("Normal");
 					for (User u : new TreeSet<User>(chan.getNormalUsers())) {
 						UserTreeItem i = new UserTreeItem(t, SWT.NONE, u,
 								c.getCChannel());
 						i.getTree().setText(u.getNick());
 					}
 					t.setExpanded(true);
 				}
 			}
 		});
 	}
 }
