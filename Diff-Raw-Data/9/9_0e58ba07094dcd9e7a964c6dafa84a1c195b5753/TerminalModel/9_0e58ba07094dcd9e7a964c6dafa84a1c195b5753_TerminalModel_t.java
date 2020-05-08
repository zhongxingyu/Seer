 package terminator.model;
 
 import java.awt.*;
 import java.util.*;
 import javax.swing.*;
 import e.util.*;
 import terminator.terminal.*;
 import terminator.view.*;
 
 public class TerminalModel {
 	private TerminalView view;
 	private int width;
 	private int height;
         private TextLines textLines = new TextLines(0, 0);
 	private short currentStyle = StyledText.getDefaultStyle();
 	private int firstScrollLineIndex;
 	private int lastScrollLineIndex;
 	private Location cursorPosition;
 	private boolean insertMode = false;
 	private int maxLineWidth = width;
 	
 	// Used for reducing the number of lines changed events sent up to the view.
 	private int firstLineChanged;
 	
 	public TerminalModel(TerminalView view, int width, int height) {
 		this.view = view;
 		setSize(width, height);
 		cursorPosition = view.getCursorPosition();
 	}
 	
 	public void updateMaxLineWidth(int aLineWidth) {
 		maxLineWidth = Math.max(getMaxLineWidth(), aLineWidth);
 	}
 	
 	public int getMaxLineWidth() {
 		return Math.max(maxLineWidth, width);
 	}
 	
 	public void sizeChanged(Dimension sizeInChars) {
 		setSize(sizeInChars.width, sizeInChars.height);
 		cursorPosition = getLocationWithinBounds(cursorPosition);
 	}
 	
 	private Location getLocationWithinBounds(Location location) {
 		if (location == null) {
 			return location;
 		}
 		int lineIndex = Math.min(location.getLineIndex(), height - 1);
 		int charOffset = Math.min(location.getCharOffset(), width - 1);
 		return new Location(lineIndex, charOffset);
 	}
 	
 	private int getNextTabPosition(int charOffset) {
 		// No special tab to our right; return the default 8-separated tab stop.
 		return (charOffset + 8) & ~7;
 	}
 	
 	public int getLineCount() {
                 return height;
 	}
 	
 	public void linesChangedFrom(int firstLineChanged) {
 		this.firstLineChanged = Math.min(this.firstLineChanged, firstLineChanged);
 	}
 	
 	public Dimension getCurrentSizeInChars() {
 		return new Dimension(getMaxLineWidth(), getLineCount());
 	}
 	
 	public Location getCursorPosition() {
 		return cursorPosition;
 	}
 	
 	public void processActions(TerminalAction[] actions) {
 		firstLineChanged = Integer.MAX_VALUE;
 		boolean wereAtBottom = view.isAtBottom();
 		boolean needsScroll = false;
 		Dimension initialSize = getCurrentSizeInChars();
 		for (TerminalAction action : actions) {
 			action.perform(this);
 		}
 		if (firstLineChanged != Integer.MAX_VALUE) {
 			needsScroll = true;
 			view.linesChangedFrom(firstLineChanged);
 		}
 		Dimension finalSize = getCurrentSizeInChars();
 		if (initialSize.equals(finalSize) == false) {
 			view.sizeChanged(initialSize, finalSize);
 		}
 		if (needsScroll) {
 			view.scrollOnTtyOutput(wereAtBottom);
 		}
 		view.setCursorPosition(cursorPosition);
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
 	
 	public int getFirstDisplayLine() {
                 return 0;
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public TextLine getTextLine(int index) {
                 return textLines.get(index);
 	}
 	
 	public void setSize(int width, int height) {
 		this.width = width;
 		this.height = height;
                 textLines.setSize(width, height);
 		firstScrollLineIndex = 0;
 		lastScrollLineIndex = height - 1;
 	}
 	
 	public void setInsertMode(boolean insertMode) {
 		this.insertMode = insertMode;
 	}
 	
 	/**
 	 * Process the characters in the given line. The string is composed of
 	 * normal printable characters, escape sequences having been extracted
 	 * elsewhere.
 	 */
 	public void processLine(String untranslatedLine) {
 		String line = view.getTerminalControl().translate(untranslatedLine);
 		TextLine textLine = getTextLine(cursorPosition.getLineIndex());
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
 		TextLine textLine = getTextLine(cursorPosition.getLineIndex());
 		updateMaxLineWidth(textLine.length());
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
 		TextLine textLine = getTextLine(cursorPosition.getLineIndex());
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
 	
 	/** Sets whether the cursor should be visible. */
 	public void setCursorVisible(boolean isDisplayed) {
 		view.setCursorVisible(isDisplayed);
 	}
 	
 	public void deleteCharacters(int count) {
 		TextLine line = getTextLine(cursorPosition.getLineIndex());
 		int start = cursorPosition.getCharOffset();
 		int end = start + count;
 		line.killText(start, end);
 		linesChangedFrom(cursorPosition.getLineIndex());
 	}
 	
 	public void killHorizontally(boolean fromStart, boolean toEnd) {
 		TextLine line = getTextLine(cursorPosition.getLineIndex());
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
 		TextLine line = getTextLine(cursorPosition.getLineIndex());
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
 		// Although the cursor positions are supposed to be measured
 		// from (1,1), there's nothing to stop a badly-behaved program
 		// from sending (0,0). ASUS routers do this (they're rubbish).
 		
 		int charOffset = cursorPosition.getCharOffset();
 		if (x != -1) {
 			// Translate from 1-based coordinates to 0-based.
 			charOffset = Math.max(0, x - 1);
 			charOffset = Math.min(charOffset, width - 1);
 		}
 		
 		int lineIndex = cursorPosition.getLineIndex();
 		if (y != -1) {
 			// Translate from 1-based coordinates to 0-based.
 			int lineOffsetFromStartOfDisplay = Math.max(0, y - 1);
 			lineOffsetFromStartOfDisplay = Math.min(lineOffsetFromStartOfDisplay, height - 1);
 			// Although the escape sequence was in terms of a line on the display, we need to take the lines above the display into account.
 			lineIndex = lineOffsetFromStartOfDisplay;
 		}
 		
 		cursorPosition = new Location(lineIndex, charOffset);
 	}
 	
 	/** Moves the cursor horizontally by the number of characters in xDiff, negative for left, positive for right. */
 	public void moveCursorHorizontally(int xDiff) {
 		int charOffset = cursorPosition.getCharOffset() + xDiff;
 		int lineIndex = cursorPosition.getLineIndex();
 		// Test cases:
 		// /bin/echo -e 'hello\n\bhello'
 		// /bin/echo -e 'hello\n\033[1Dhello'
 		if (charOffset < 0) {
 			charOffset = 0;
 		}
 		// Constraining charOffset here stops line editing working properly on Titan serial consoles.
 		//charOffset = Math.min(charOffset, width - 1);
 		cursorPosition = new Location(lineIndex, charOffset);
 	}
 	
 	/** Moves the cursor vertically by the number of characters in yDiff, negative for up, positive for down. */
 	public void moveCursorVertically(int yDiff) {
 		int y = cursorPosition.getLineIndex() + yDiff;
 		y = Math.max(0, y);
 		y = Math.min(y, height - 1);
 		cursorPosition = new Location(y, cursorPosition.getCharOffset());
 	}
 	
 	/** Sets the first and last lines to scroll.  If both are -1, make the entire screen scroll. */
 	public void setScrollingRegion(int firstLine, int lastLine) {
 		firstScrollLineIndex = ((firstLine == -1) ? 1 : firstLine) - 1;
 		lastScrollLineIndex = ((lastLine == -1) ? height : lastLine) - 1;
 	}
 	
 	/** Scrolls the display up by one line. */
 	public void scrollDisplayUp() {
                 /* TODO: Can this be implemented with insertLines? */
                 modifyOneLine(firstScrollLineIndex, firstScrollLineIndex, lastScrollLineIndex + 1);
 	}
 
         private void modifyOneLine(int index, int top, int bottom) {
                 textLines.insertLines(index, 1, 0, bottom);
                 linesChangedFrom(top);
                 view.repaint();
         }
 	
 	/** Delete one line, moving everything below up and inserting a blank line at the bottom. */
 	public void deleteLine() {
                 /* TODO: Can this be implemented with insertLines? */
                 modifyOneLine(lastScrollLineIndex + 1, cursorPosition.getLineIndex(), cursorPosition.getLineIndex());
 	}
 }
