 package terminator.model;
 
 import java.awt.Dimension;
 import java.util.*;
 import e.util.*;
 import terminator.terminal.*;
 
 public class TerminalModel {
         private Dimension size = new Dimension(0, 0);
         private TextLines textLines = new TextLines(new Dimension(0, 0));
 	private short currentStyle = StyledText.getDefaultStyle();
 	private int firstScrollLineIndex;
 	private int lastScrollLineIndex;
 	private Location cursorPosition = new Location(0, 0);
         private boolean cursorVisible = true;
 	private boolean insertMode = false;
 
         private class TerminalListeners implements TerminalListener {
                 private List<TerminalListener> listeners = new ArrayList<TerminalListener>();
 
                 public void add(TerminalListener l) {
                         listeners.add(l);
                 }
 
                 public void contentsChanged(int fromLine) {
                         for (TerminalListener l : listeners)
                                 l.contentsChanged(fromLine);
                 }
 
                 public void cursorPositionChanged(Location oldPosition, Location newPosition) {
                         for (TerminalListener l : listeners)
                                 l.cursorPositionChanged(oldPosition, newPosition);
                 }
 
                 public void cursorVisibilityChanged(boolean isVisible) {
                         for (TerminalListener l : listeners)
                                 l.cursorVisibilityChanged(isVisible);
                 }
         }
 
         private TerminalListeners listeners = new TerminalListeners();
 	
 	private int firstLineChanged = Integer.MAX_VALUE;
 	
 	private void clampCursor() {
 		if (cursorPosition == null)
                         return;
                 cursorPosition = new Location(clampVertically(cursorPosition.getLineIndex()),
                                               clampHorizontally(cursorPosition.getCharOffset()));
 	}
 	
         private int clampVertically(int value) {
                 return clamp(value, 0, size.height - 1);
         }
 
         private int clampHorizontally(int value) {
                 return clamp(value, 0, size.width - 1);
         }
 
         private int clamp(int value, int min, int max) {
                 return Math.min(Math.max(min, value), max);
         }
 	
 	private int getNextTabPosition(int charOffset) {
 		// No special tab to our right; return the default 8-separated tab stop.
 		return (charOffset + 8) & ~7;
 	}
 	
         // TODO: Should be delegated to textLines.
 	public int getLineCount() {
                 return size.height;
 	}
 	
 	public void linesChangedFrom(int firstLineChanged) {
 		this.firstLineChanged = Math.min(this.firstLineChanged, firstLineChanged);
 	}
 
         public int getFirstLineChanged() {
                 return firstLineChanged;
         }
 	
 	public Dimension getCurrentSizeInChars() {
                 return new Dimension(size);
 	}
 	
 	public Location getCursorPosition() {
 		return cursorPosition;
 	}
 	
 	public void processActions(TerminalAction[] actions) {
                 Location oldCursorPosition = cursorPosition;
 		firstLineChanged = Integer.MAX_VALUE;
 		for (TerminalAction action : actions)
 			action.perform(this);
                 if (!oldCursorPosition.equals(cursorPosition))
                         listeners.cursorPositionChanged(oldCursorPosition, cursorPosition);
                 if (firstLineChanged != Integer.MAX_VALUE)
                         listeners.contentsChanged(firstLineChanged);
 	}
 	
 	public void setStyle(short style) {
 		this.currentStyle = style;
 	}
 	
 	public short getStyle() {
 		return currentStyle;
 	}
 	
 	public void moveToLine(int index) {
                 // NOTE: We only really allow index to be lastScrollLineIndex + 1
                 if (index > lastScrollLineIndex)
                         insertLines(index, 1);
                 else
                         cursorPosition = new Location(index, cursorPosition.getCharOffset());
 	}
 	
 	/** Inserts lines at the current cursor position. */
 	public void insertLines(int count) {
                 insertLines(cursorPosition.getLineIndex(), count);
         }
 
         private void insertLines(int at, int count) {
                 int above = textLines.insertLines(at,
                                                   count,
                                                   firstScrollLineIndex,
                                                   lastScrollLineIndex);
                 linesChangedFrom(above == count ? cursorPosition.getLineIndex() : firstScrollLineIndex);
                 if (above > 0)
                         cursorPosition = new Location(cursorPosition.getLineIndex() + above,
                                                       cursorPosition.getCharOffset());
 	}
 	
 	public TextLine getTextLine(int index) {
                 return textLines.get(index);
 	}
 
         private TextLine getCursorTextLine() {
                 return textLines.get(cursorPosition.getLineIndex());
         }
 	
 	public void setSize(Dimension size) {
                 this.size.setSize(size);
                 textLines.setSize(size);
 		firstScrollLineIndex = 0;
 		lastScrollLineIndex = size.height - 1;
                 clampCursor();
 	}
 	
 	public void setInsertMode(boolean insertMode) {
 		this.insertMode = insertMode;
 	}
 	
 	/**
 	 * Process the characters in the given line. The string is composed of
 	 * normal printable characters, escape sequences having been extracted
 	 * elsewhere.
 	 */
 	public void processLine(String line) {
                 TextLine textLine = getCursorTextLine();
 		if (insertMode) {
 			//Log.warn("Inserting text \"" + line + "\" at " + cursorPosition + ".");
 			textLine.insertTextAt(cursorPosition.getCharOffset(), line, currentStyle);
 		} else {
 			//Log.warn("Writing text \"" + line + "\" at " + cursorPosition + ".");
 			textLine.writeTextAt(cursorPosition.getCharOffset(), line, currentStyle);
 		}
 		textAdded(line.length());
 	}
 	
 	private void textAdded(int length) {
 		linesChangedFrom(cursorPosition.getLineIndex());
 		moveCursorHorizontally(length);
 	}
 	
 	public void processSpecialCharacter(char ch) {
 		switch (ch) {
 		case Ascii.CR:
 			cursorPosition = new Location(cursorPosition.getLineIndex(), 0);
 			return;
 		case Ascii.LF:
 			moveToLine(cursorPosition.getLineIndex() + 1);
 			return;
 		case Ascii.VT:
 			moveCursorVertically(1);
 			return;
 		case Ascii.HT:
 			insertTab();
 			return;
 		case Ascii.BS:
 			moveCursorHorizontally(-1);
 			return;
 		default:
 			Log.warn("Unsupported special character: " + ((int) ch));
 		}
 	}
 	
 	private void insertTab() {
 		int nextTabLocation = getNextTabPosition(cursorPosition.getCharOffset());
 		TextLine textLine = getCursorTextLine();
 		int startOffset = cursorPosition.getCharOffset();
 		int tabLength = nextTabLocation - startOffset;
 		// We want to insert our special tabbing characters (see getTabString) when inserting a tab or outputting one at the end of a line, so that text copied from the output of (say) cat(1) will be pasted with tabs preserved.
 		boolean endOfLine = (startOffset == textLine.length());
 		if (insertMode || endOfLine) {
 			textLine.insertTabAt(startOffset, tabLength, currentStyle);
 		} else {
 			// Emacs, source of all bloat, uses \t\b\t sequences around tab stops (in lines with no \t characters) if you hold down right arrow. The call to textAdded below moves the cursor, which is all we're supposed to do.
 		}
 		textAdded(tabLength);
 	}
 	
 	public void setCursorVisible(boolean cursorVisible) {
                 this.cursorVisible = cursorVisible;
                 listeners.cursorVisibilityChanged(this.cursorVisible);
 	}
 
         public boolean getCursorVisible() {
                 return cursorVisible;
         }
 	
 	public void deleteCharacters(int count) {
 		TextLine line = getCursorTextLine();
 		int start = cursorPosition.getCharOffset();
 		int end = start + count;
 		line.killText(start, end);
 		linesChangedFrom(cursorPosition.getLineIndex());
 	}
 	
 	public void killHorizontally(boolean fromStart, boolean toEnd) {
 		TextLine line = getCursorTextLine();
 		int oldLineLength = line.length();
 		int start = fromStart ? 0 : cursorPosition.getCharOffset();
 		int end = toEnd ? oldLineLength : cursorPosition.getCharOffset();
 		line.killText(start, end);
 		linesChangedFrom(cursorPosition.getLineIndex());
 	}
 	
 	/** Erases from either the top or the cursor, to either the bottom or the cursor. */
 	public void eraseInPage(boolean fromTop, boolean toBottom) {
 		// Should produce "hi\nwo":
 		// echo $'\n\n\nworld\x1b[A\rhi\x1b[B\x1b[J'
 		// Should produce "   ld":
 		// echo $'\n\n\nworld\x1b[A\rhi\x1b[B\x1b[1J'
 		// Should clear the screen:
 		// echo $'\n\n\nworld\x1b[A\rhi\x1b[B\x1b[2J'
 		int start = fromTop ? 0 : cursorPosition.getLineIndex();
 		int startClearing = fromTop ? start : start + 1;
 		int endClearing = toBottom ? getLineCount() : cursorPosition.getLineIndex();
 		for (int i = startClearing; i < endClearing; i++) {
 			getTextLine(i).clear();
 		}
 		TextLine line = getCursorTextLine();
 		int oldLineLength = line.length();
 		if (fromTop) {
 			// The current position is always erased, hence the + 1.
 			// Is overwriting with spaces in the currentStyle correct?
 			line.writeTextAt(0, StringUtilities.nCopies(cursorPosition.getCharOffset() + 1, ' '), currentStyle);
 		}
 		if (toBottom) {
 			line.killText(cursorPosition.getCharOffset(), oldLineLength);
 		}
 		linesChangedFrom(start);
 	}
 	
 	/**
 	 * Sets the position of the cursor to the given x and y coordinates, counted from 1,1 at the top-left corner.
 	 * If either x or y is -1, that coordinate is left unchanged.
 	 */
 	public void setCursorPosition(int x, int y) {
 		int charOffset = cursorPosition.getCharOffset();
 		if (x != -1)
                         charOffset = clampHorizontally(x - 1);
 		
 		int lineIndex = cursorPosition.getLineIndex();
 		if (y != -1)
                         lineIndex = clampVertically(y - 1);
 		
 		cursorPosition = new Location(lineIndex, charOffset);
 	}
 	
 	/** Moves the cursor horizontally by the number of characters in xDiff, negative for left, positive for right. */
 	public void moveCursorHorizontally(int delta) {
                 int x = clampHorizontally(cursorPosition.getCharOffset() + delta);
                 cursorPosition = new Location(cursorPosition.getLineIndex(), x);
 	}
 	
 	/** Moves the cursor vertically by the number of characters in yDiff, negative for up, positive for down. */
 	public void moveCursorVertically(int delta) {
                 int y = clampVertically(cursorPosition.getLineIndex() + delta);
 		cursorPosition = new Location(y, cursorPosition.getCharOffset());
 	}
 
 	/** Sets the first and last lines to scroll.  If both are -1, make the entire screen scroll. */
 	public void setScrollingRegion(int firstLine, int lastLine) {
 		firstScrollLineIndex = ((firstLine == -1) ? 1 : firstLine) - 1;
 		lastScrollLineIndex = ((lastLine == -1) ? size.height : lastLine) - 1;
 	}
 	
 	/** Scrolls the display up by one line. */
 	public void scrollDisplayUp() {
                /* TODO: Can this be implemented with insertLines? */
                 modifyOneLine(firstScrollLineIndex, firstScrollLineIndex, lastScrollLineIndex + 1);
 	}
 
         private void modifyOneLine(int index, int top, int bottom) {
                 textLines.insertLines(index, 1, 0, bottom);
                 linesChangedFrom(top);
         }
 	
 	/** Delete one line, moving everything below up and inserting a blank line at the bottom. */
 	public void deleteLine() {
                /* TODO: Can this be implemented with insertLines? */
                modifyOneLine(lastScrollLineIndex + 1, cursorPosition.getLineIndex(), cursorPosition.getLineIndex());
 	}
 
 	public void addListener(TerminalListener l) {
 		listeners.add(l);
 	}
 }
