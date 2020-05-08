 package gui;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.swing.AbstractAction;
 import javax.swing.KeyStroke;
 import javax.swing.text.BadLocationException;
 
 /**
  * Run commands.
  */
 public class REPLTextArea extends SchemeTextArea {
 	private static final long serialVersionUID = 8753865168892947915L;
 	MainFrame Main;
 	
 	// Store previously entered commands.
 	List<String> commandHistory;
 	int currentCommand = 0;
 	
 	/**
 	 * Create a new REPL area.
 	 */
 	public REPLTextArea(final MainFrame main) {
 		Main = main;
 		commandHistory = new ArrayList<String>();
 		setPreferredSize(new Dimension(100, 100));
 		
 		// When the user hits the 'ENTER' key, check for a complete command.
         code.getInputMap().put(
                 KeyStroke.getKeyStroke("ENTER"),
                 new AbstractAction() {
                     private static final long serialVersionUID = 723647997099071931L;
 
 					public void actionPerformed(ActionEvent e) {
 						if (Main.Running) return;
 						
 						checkRun();
                     }
                 });
         
         // On CTRL-ENTER (command-ENTER on a mac).
         code.getInputMap().put(
         		KeyStroke.getKeyStroke(
                     KeyEvent.VK_ENTER,
                     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                 ),
                 new AbstractAction() {
                     private static final long serialVersionUID = 723647997099071931L;
 
 					public void actionPerformed(ActionEvent e) {
 						if (Main.Running) return;
 						if (getText().trim().isEmpty()) return;
 						
 						commandHistory.add(getText());
 			        	currentCommand = commandHistory.size();
 			        	
 						main.doCommand(getText());
 						setText("");
                     }
                 });
         
         // When the user hits the up arrow, it they are on the first line, reload the previous command.
         code.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent arg0) {
 				if (arg0.getKeyCode() == KeyEvent.VK_UP) {
					if (getText().lastIndexOf("\n", code.getCaretPosition() - 1) == -1) {
 						if (currentCommand == commandHistory.size())
 							if (!getText().isEmpty())
 								commandHistory.add(getText());
 						
 						if (currentCommand == 0)
 							return;
 						
 						currentCommand--;
 						setText(commandHistory.get(currentCommand));
						code.setCaretPosition(0);
 					}
 				}
 				
 				if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
 					if (getText().indexOf("\n", code.getCaretPosition()) == -1) {
 						if (currentCommand == commandHistory.size()) {
 							return;
 						} else if (currentCommand == commandHistory.size() - 1) {
 							setText(commandHistory.remove(commandHistory.size() - 1));
 						} else {
 							currentCommand++;
 							setText(commandHistory.get(currentCommand));
							code.setCaretPosition(getText().length());
 						}
 					}
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 				
 			}
         });
 	}
 
 	/**
 	 * Check if the command should run, run it if it should.
 	 * 
 	 * To run:
 	 * - Last line
 	 * - Brackets are matched
 	 */
 	protected void checkRun() {
 		// The cursor should be on the last line.
 		if (getText().substring(code.getCaretPosition()).trim().isEmpty()) {
 			
 			// Check to see that we have a matched pair of brackets.
 	        Stack<Character> brackets = new Stack<Character>();
 	        for (char c : getText().toCharArray()) {
 	            if (c == '(') brackets.push(')');
 	            else if (c == '[') brackets.push(']');
 	            else if (c == ')' || c == ']')
 	                if (!brackets.empty() && brackets.peek() == c)
 	                    brackets.pop();
 	                else
 	                    return;
 	        }
 	
 	        // This means we matched them all.
 	        if (brackets.empty()) {
 	        	commandHistory.add(getText());
 	        	currentCommand = commandHistory.size();
 	        	
 	            Main.doCommand(getText());
 	            setText("");
 	            return;
 	        }
 		}
         
         // Either we didn't match the brackets or we aren't on the last line. Insert the return normally.
         try {
             code.getDocument().insertString(code.getCaretPosition(), SchemeTextArea.NL, null);
         } catch (BadLocationException ble) {
             System.err.println("badwolf");
         }
         tab();
 	}
 }
