 package telnet;
 
 import common.TextPane;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.PrintWriter;
 import javax.swing.undo.UndoManager;
 
 public class InputAreaListener extends KeyAdapter {
 
 	protected TextPane inputArea;
 	private TextPane outputArea = Telnet.getOutputArea();
 	protected PrintWriter socketInput = Telnet.getSocketInput();
 	private int tabCount = 0;
 	protected UndoManager undoManager;
 
 	InputAreaListener(TextPane inputArea) {
 		this.inputArea = inputArea;
 		undoManager = inputArea.getUndoManager();
 
 	}
 
 	public void keyPressed(KeyEvent e) {
 		// if control is on
 		if (e.getModifiersEx() == 128) {
 			switch (e.getKeyCode()) {
 			// l key
 			case 76:
 				outputArea.setText("");
 				break;
 			// e key
 			case 69:
 				e.consume();
 				eval(inputArea.getSelectedText());
 				break;
 			}
 		}
 	}
 
 	public void keyTyped(KeyEvent e) {
 		switch ((int) e.getKeyChar()) {
 		// tab key
 		case 9:
 			e.consume();
 			tabCount++;
 			int caretPos = inputArea.getCaretPosition();
 			String text = inputArea.getText();
			int textLength = 0;
			if (text != null && (textLength = text.length()) != 0) {
				switch (text.charAt(caretPos == 0 ? 0 : caretPos - 1)) {
				case '\n':
				case '\t':
					text = text.substring(0, caretPos) + '\t'
							+ text.substring(caretPos);
					break;
				default:
					if (text != null && Telnet.getTab() != null) {
						if (tabCount == 1) {
							text = Telnet.getTab().autoComplete(text, caretPos);
						} else if (tabCount == 2) {
							outputArea.append(Telnet.getTab().suggestions(text,
									caretPos));
						}
 					}
					break;
 				}
 			}
 			caretPos += text.length() - textLength;
 			inputArea.setText(text);
 			inputArea.setCaretPosition(caretPos);
 			break;
 		// enter key
 		case 10:
 			// if shift is on
 			if (e.getModifiersEx() == 64) {
 				eval(inputArea.getText());
 			}
 			tabCount = 0;
 			break;
 		default:
 			tabCount = 0;
 			break;
 		}
 	}
 
 	private void eval(String text) {
 		if (text != null) {
 			outputArea.append(text);
 			socketInput.println(text.replaceAll("\\n", ""));
 			String[] explode = text.split("[ )\n]");
 			for (int i = 0; i < explode.length - 1; i++) {
 				if (explode[i].startsWith("def", 1)) {
 					Tab.addDefinition(explode[i + 1]);
 				}
 			}
 		}
 	}
 }
