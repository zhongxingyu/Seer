 package telnet;
 
 import java.awt.event.KeyEvent;
 import java.io.PrintWriter;
 
 import telnet.ScrollingTextPane.TextPane;
 
 public class MainInputAreaListener extends InputAreaListener {
 
 	private int tabCount = 0;
 
 	public MainInputAreaListener(TextPane inputArea, TextPane outputArea,
 			PrintWriter socketInput) {
 		super(inputArea, outputArea, socketInput);
 	}
 
 	public void keyPressed(KeyEvent e) {
 		switch (e.getKeyCode()) {
 		// up key
 		case 38:
 			// if shift is on
 			if (e.getModifiersEx() == 64) {
 				inputArea.setText(CommandRecall.getInstance().prev());
 			}
 			break;
 
 		// down key
 		case 40:
 			// if shift is on
 			if (e.getModifiersEx() == 64) {
				CommandRecall.getInstance().add(inputArea.getText());
 				inputArea.setText(CommandRecall.getInstance().next());
 			}
 			break;
 		// l key
 		case 76:
 			// if control is on
 			if (e.getModifiersEx() == 128) {
 				outputArea.setText("");
 			}
 			break;
 		// e key
 		case 69:
 			// if control is on
 			e.consume();
 			String text = inputArea.getSelectedText();
 			if (e.getModifiersEx() == 128 && text != null) {
 				outputArea.append(text);
 				socketInput.println(text.replaceAll("\\n", ""));
 			}
 			break;
 		}
 
 	}
 
 	public void keyTyped(KeyEvent e) {
 		switch ((int) e.getKeyChar()) {
 		// tab key
 		case 9:
 			tabCount++;
 			e.consume();// Does not work
 			inputArea.setText(inputArea.getText().replaceAll("\\t", ""));
 			if (tabCount == 1) {
 				String autoCompleted = Tab.getInstance().autoComplete(
 						inputArea.getText(), inputArea.getCaretPosition());
 				if (!autoCompleted.equals(inputArea.getText())) {
 					inputArea.setText(autoCompleted);
 
 				}
 			} else if (tabCount == 2) {
 				String results = Tab.getInstance().suggestions(
 						inputArea.getText(), inputArea.getCaretPosition());
 				if (results.startsWith("doc ", 1)) {
 					outputArea.append(results);
 					socketInput.println(results);
 				} else {
 					outputArea.append(results);
 				}
 			}
 			break;
 		// enter key
 		case 10:
 			// if shift is on
 			if (e.getModifiersEx() == 64) {
 				String text = inputArea.getText().replaceAll("\\s+$", "");
 				outputArea.append(text);
 				CommandRecall.getInstance().add(text);
 				socketInput.println(text.replaceAll("\\n", ""));
 				inputArea.setText("");
 			}
 			tabCount = 0;
 			break;
 		default:
 			tabCount = 0;
 			break;
 		}
 	}
 
 }
