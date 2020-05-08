 package edu.umich.sbolt;
 
 import java.awt.Color;
 import java.awt.MenuShortcut;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
 import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
 
 import abolt.lcmtypes.robot_command_t;
 import april.util.TimeUtil;
 
 import com.soartech.bolt.BOLTLGSupport;
 
 public class ChatFrame extends JFrame
 {
 
     private JTextArea chatArea;
 
     private JTextField chatField;
     
     private JButton sendButton;
 
     private List<String> chatMessages;
 
     private SBolt sbolt;
     
     private BOLTLGSupport lgSupport;
     
     private ArrayList<String> history;
     
     private int historyIndex = 0;
     
     private InteractionStack stack;
     
     private boolean ready = false;
 
     public ChatFrame(SBolt sbolt, BOLTLGSupport lg) {
         super("SBolt");
         
         history = new ArrayList<String>();
 
         this.sbolt = sbolt;
         lgSupport = lg;
         
         chatMessages = new ArrayList<String>();
 
         chatArea = new JTextArea();
         JScrollPane pane = new JScrollPane(chatArea);
         chatField = new JTextField();
         chatField.addKeyListener(new KeyAdapter(){
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if(arg0.getKeyCode() == KeyEvent.VK_UP) {
 					if(historyIndex > 0){
 						historyIndex--;
 					}
 					if(history.size() > 0){
 						chatField.setText(history.get(historyIndex));
 					}
 				} else if(arg0.getKeyCode() == KeyEvent.VK_DOWN){
 					historyIndex++;
 					if(historyIndex > history.size()){
 						historyIndex = history.size();
 					}
 					if(historyIndex == history.size()){
 						chatField.setText("");
 					} else {
 						chatField.setText(history.get(historyIndex));
 					}
 				}
 			}
         });
         
         
         sendButton = new JButton("Send Message");
         sendButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
             	sendButtonClicked();
             }
         });
 
         JSplitPane pane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                 chatField, sendButton);
         JSplitPane pane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane,
                 pane2);
 
         pane1.setDividerLocation(325);
         pane2.setDividerLocation(600);
 
         this.add(pane1);
         this.setSize(800, 450);
         this.getRootPane().setDefaultButton(sendButton);
         
         JMenuBar menuBar = new JMenuBar();        
         
         JButton clearButton  = new JButton("Clear Text");
         clearButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				clear();
 			}
         });
         menuBar.add(clearButton);
         
         JButton resetButton  = new JButton("Reset Arm");
         resetButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				resetArm();
 			}
         });
         menuBar.add(resetButton);
         
         stack = new InteractionStack();
         JButton stackButton = new JButton("Interaction Stack");
         stackButton.addActionListener(new ActionListener(){
         	@Override
 			public void actionPerformed(ActionEvent arg0) {
 				stack.showFrame();
 			}
         });
         menuBar.add(stackButton);
        
         setJMenuBar(menuBar);
         
         addWindowListener(new WindowAdapter() {
         	public void windowClosing(WindowEvent w) {
         		exit();
         	}
      	});
         
         setReady(false);
     }
     
     public void setReady(boolean isReady){
     	ready = isReady;
     	if(ready){
     		sendButton.setBackground(new Color(150, 255, 150));
     		sendButton.setText("Send Message");
     	} else {
     		sendButton.setBackground(new Color(255, 100, 100));
     		sendButton.setText("Not Ready");
     	}
     }
     
     public InteractionStack getStack(){
     	return stack;
     }
     
     private void sendButtonClicked(){
     	if(!ready){
     		return;
     	}
     	history.add(chatField.getText());
     	historyIndex = history.size();
         addMessage(chatField.getText());
         sendSoarMessage(chatField.getText());
         chatField.setText("");
         chatField.requestFocus();
     }
     
     
     public void clear(){
     	chatMessages.clear();
     	chatField.setText("");
     	chatArea.setText("");
     	sbolt.getInputLink().clearLGMessages();
     	sbolt.getWorld().destroyMessage();
     }
     
     private void resetArm(){
 		robot_command_t command = new robot_command_t();
 		command.utime = TimeUtil.utime();
 		command.action = "RESET";
 		command.dest = new double[6];
 		sbolt.broadcastRobotCommand(command);
     }
     
     public void exit(){
     	sbolt.getAgent().KillDebugger();
     	// sbolt.getKernel().DestroyAgent(sbolt.getAgent());
     	
     	// SBW removed DestroyAgent call, it hangs in headless mode for some reason
     	// (even when the KillDebugger isn't there)
     	// I don't think there's any consequence to simply exiting instead.
     	
     	System.exit(0);
     }
 
     public void addMessage(String message)
     {
         chatMessages.add(message);
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < chatMessages.size(); ++i)
         {
             sb.append(chatMessages.get(i));
             if (i + 1 < chatMessages.size())
             {
                 sb.append('\n');
             }
         }
         chatArea.setText(sb.toString());
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
     }
 
     private void sendSoarMessage(String message)
     {
     	if (lgSupport == null) {
     		sbolt.getWorld().newMessage(message);
     	}
     	else if(message.length() > 0 && message.charAt(0) == ':'){
     		if(message.equals(":reset")){
     			resetArm();
     		} else {
         		// Prefixing with a : goes to Soar's message processing
         		sbolt.getWorld().newMessage(message.substring(1));
     		}
     	} else {
     		lgSupport.handleInput(message);
     		// LGSupport has access to the agent object and handles all WM interaction from here
     	}
     }
 
     public void showFrame()
     {
         this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
         this.setVisible(true);
     }
 
     public void hideFrame()
     {
         this.setVisible(false);
     }
 
 }
