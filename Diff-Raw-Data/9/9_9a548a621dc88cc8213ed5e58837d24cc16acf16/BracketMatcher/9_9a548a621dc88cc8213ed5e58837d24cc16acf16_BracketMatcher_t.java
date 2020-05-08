 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.gui.text;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.swing.event.CaretListener;
 
 import javax.swing.event.CaretEvent;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultHighlighter;
 import javax.swing.text.Highlighter;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.Utilities;
 
 import wombat.gui.frames.MainFrame;
 import wombat.util.errors.ErrorManager;
 
 /**
  * Used to highlight matching brackets (both square and rounded, but not angle
  * or curly, although those could be added easily enough.
  */
 public class BracketMatcher implements CaretListener {
 	// The text area that
 	SchemeTextArea textArea;
 
 	// If we can't match brackets enough times, disable them.
 	static boolean disabled = false;
 
 	// Any active brackets are moved before each new highlight.
 	List<Object> activeTags = new ArrayList<Object>();
 
 	/**
 	 * Create a bracket matcher for a given text area.
 	 * 
 	 * @param text
 	 */
 	public BracketMatcher(SchemeTextArea text) {
 		textArea = text;
 	}
 
 	/**
 	 * When the caret moves, update the matched brackets.
 	 * 
 	 * Also used to show the current line and column. TODO: Move this code to
 	 * its own object.
 	 * 
 	 * @param event
 	 *            Event parameters (ignored).
 	 */
 	public void caretUpdate(CaretEvent event) {
 		// Update the current row and column of the caret.
 		try {
 			// Find the current row of the caret.
 			int caretPos = textArea.code.getCaretPosition();
 			int rowNum = (caretPos == 0) ? 1 : 0;
 			for (int offset = caretPos; offset > 0;) {
 				offset = Utilities.getRowStart(textArea.code, offset) - 1;
 				rowNum++;
 			}
 
 			// Use that to find the current column.
 			int offset = Utilities.getRowStart(textArea.code, caretPos);
 			int colNum = caretPos - offset + 1;
 
 			MainFrame.Singleton().RowColumn.setText(rowNum + ":" + colNum);
 
 		}
 
 		// Can't find the caret correctly, reset the row:column indicator.
 		catch (BadLocationException ex) {
 			MainFrame.Singleton().RowColumn.setText("row:column");
 		}
 
 		// If the bracket matcher has broken, don't keep trying.
 		if (disabled)
 			return;
 
 		try {
 			// Get the highlighter and remove all active tags.
 			Highlighter h = textArea.code.getHighlighter();
 			for (Object tag : activeTags)
 				h.removeHighlight(tag);
 			activeTags.clear();
 
 			// Get the caret position.
 			int pos = event.getDot() - 1;
 
 			// If the caret is in the document and adjacent to a bracket.
 			if (pos >= 0 
 					&& pos < textArea.getText().length()
 					&& "()[]".contains(textArea.code.getDocument().getText(pos, 1))) {
 				
 				// Get a direct link to the full text of the document to speed up future access.
 				String text = textArea.code.getDocument().getText(0, textArea.code.getDocument().getLength());
 
 				// Skip character literals
 				if (pos >= 2 && "#\\".equals(text.substring(pos - 2, pos)))
 					return;
 
 				// Skip if we're in a comment
 				if ((text.lastIndexOf("#|", pos) > text.lastIndexOf("|#", pos))
 						|| (text.lastIndexOf(';', pos) > text.lastIndexOf('\n', pos)))
 					return;
 				
 				// Which way are we going?
 				char orig, c;
 				orig = c = text.charAt(pos);
 				int matchPos, d = ((orig == '(' || orig == '[') ? 1 : -1);
 				
 				// Keep a stack of brackets so we find the correct level.
 				Stack<Character> brackets = new Stack<Character>();
 				
 				// Loop either forward or back depending on opening or closing initial bracket.
 				int index;
 				boolean foundMatch = false;
 				for (matchPos = pos; matchPos >= 0 && matchPos < text.length(); matchPos += d) {
 					// When moving towards the front:
 					if (d < 0) {
 						// Ignore line comments.
 						index = text.lastIndexOf(';', matchPos);
 						if (index >= 0 && text.lastIndexOf('\n', matchPos) < index) {
 							matchPos = index;
 							continue;
 						}
 
 						// Ignore block comments.
 						index = text.lastIndexOf("|#", matchPos);
 						if (index >= 0
 								&& text.lastIndexOf('\n', matchPos) < index) {
 							matchPos = text.lastIndexOf("#|", index);
 							continue;
 						}
 					} 
 					// When moving towards the end:
 					else {
 						// Ignore line comments.
						index = text.lastIndexOf(';', matchPos);
						if (index >= 0 && text.lastIndexOf('\n', matchPos) < index) {
 							matchPos = index;
 							continue;
 						}
 
 						// Ignore block comments.
						index = text.lastIndexOf("#|", matchPos);
						if (index >= 0 && text.lastIndexOf('\n', matchPos) < index) {
 							matchPos = text.indexOf("|#", index);
 							continue;
 						}
 					}
 
 					// Get the current character.
 					c = text.charAt(matchPos);
 
 					// Ignore character literals
 					if (matchPos >= 2 && "#\\".equals(text.substring(matchPos - 2, matchPos)))
 						continue;
 
 					// We're only done when we're at the correct level and find the correct bracket.
 					if (!brackets.isEmpty() && brackets.peek() == c) {
 						brackets.pop();
 						if (brackets.isEmpty()) {
 							foundMatch = true;
 							break;
 						}
 					}
 					
 					// If we still have brackets to deal with, make sure it's the correct kind.
 					// If it's the incorrect kind, highligh with an error (default = red).
 					else if (!brackets.isEmpty()
 							&& ((brackets.peek() == '(' && c == '[')
 									|| (brackets.peek() == '[' && c == '(')
 									|| (brackets.peek() == ')' && c == ']') 
 									|| (brackets.peek() == ']' && c == ')'))) {
 						foundMatch = false;
 						break;
 					}
 
 					// Remember bracket level.
 					else if (c == '(')
 						brackets.push(')');
 					else if (c == ')')
 						brackets.push('(');
 					else if (c == '[')
 						brackets.push(']');
 					else if (c == ']')
 						brackets.push('[');
 				}
 
 				// Highlight it.
 				Highlighter.HighlightPainter hp = new DefaultHighlighter.DefaultHighlightPainter(
 						StyleConstants.getForeground(SchemeDocument.attributes.get(foundMatch ? "bracket" : "invalid-bracket")));
 
 				// Remember the bracket so we can remove it on the next cycle.
 				try {
 					activeTags.add(h.addHighlight(pos, pos + 1, hp));
 					activeTags.add(h.addHighlight(matchPos, matchPos + 1, hp));
 				} catch (BadLocationException ble) {
 				}
 			}
 		} 
 		
 		// Ignore bad locations. This is usually empty documents.
 		catch (BadLocationException ex) {
 
 		} 
 		
 		// If we get this, we broke the bracket matcher. Remember the error and disable it for the future.
 		// This shouldn't happen unless things go badly wrong.
 		catch (Exception ex) {
 			ErrorManager.logError("Unable to match paranthesis: " + ex.getMessage());
 			ex.printStackTrace();
 			disabled = true;
 		}
 	}
 }
