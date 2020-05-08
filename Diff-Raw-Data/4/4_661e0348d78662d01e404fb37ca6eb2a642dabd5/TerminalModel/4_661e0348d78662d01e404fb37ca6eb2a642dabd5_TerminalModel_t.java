 package terminator.model;
 
 import java.awt.Dimension;
 import java.util.*;
 import e.util.*;
 import terminator.terminal.*;
 
 public class TerminalModel {
         private class TerminalListeners implements TerminalListener {
                 private List<TerminalListener> listeners = new ArrayList<TerminalListener>();
 
                 public void add(TerminalListener l) {
                         listeners.add(l);
                 }
 
                 public void contentsChanged(int fromLine) {
                         for (TerminalListener l : listeners)
                                 l.contentsChanged(fromLine);
                 }
 
                 public void cursorPositionChanged(Cursor oldPosition, Cursor newPosition) {
                         for (TerminalListener l : listeners)
                                 l.cursorPositionChanged(oldPosition, newPosition);
                 }
 
                 public void cursorVisibilityChanged(boolean isVisible) {
                         for (TerminalListener l : listeners)
                                 l.cursorVisibilityChanged(isVisible);
                 }
         }
 
         private TerminalListeners listeners = new TerminalListeners();
         private TextLines textLines = new TextLines(new Dimension(0, 0));
         private Cursor cursor = Cursor.origo();
 
 	public void addListener(TerminalListener l) {
 		listeners.add(l);
 	}
 
 	public int getLineCount() {
                 return textLines.count();
 	}
 
 	public TextLine getTextLine(int index) {
                 return textLines.get(index);
 	}
 
 	public Cursor getCursor() {
 		return cursor;
 	}
 
 	public void processActions(TerminalAction[] actions) {
                 modifier.reset();
 		for (TerminalAction action : actions)
 			action.perform(modifier);
                 modifier.notifyListeners();
 	}
 
         private TerminalModelModifier modifier = new TerminalModelModifier() {
                 private short currentStyle = StyledText.getDefaultStyle();
                 private Region scrollingRegion = new Region(0, 0);
                 private boolean insertMode = false;
                 private int firstLineChanged;
                 private Cursor oldCursor;
 
                 public void reset() {
                         firstLineChanged = Integer.MAX_VALUE;
                         oldCursor = cursor;
                 }
 
                 public void notifyListeners() {
                         if (!oldCursor.equals(cursor))
                                 listeners.cursorPositionChanged(oldCursor, cursor);
                         if (firstLineChanged != Integer.MAX_VALUE)
                                 listeners.contentsChanged(firstLineChanged);
                 }
 
                 private void linesChangedFrom(int line) {
                         firstLineChanged = Math.min(firstLineChanged, line);
                 }
 
                 private void linesChangedFromCursor() {
                         linesChangedFrom(cursor.getRow());
                 }
 
                 public void setSize(Dimension size) {
                         textLines.setSize(size);
                         scrollingRegion.set(0, getLineCount() - 1);
                         cursor = cursor.constrain(size);
                 }
 
                 public void setStyle(short style) {
                         currentStyle = style;
                 }
 
                 public short getStyle() {
                         return currentStyle;
                 }
 
                 public void insertLines(int count) {
                         insertLines(cursor.getRow(), count);
                 }
 
                 private void insertLines(int at, int count) {
                         int above = textLines.insertLines(at,
                                                           count,
                                                           scrollingRegion.top(),
                                                           scrollingRegion.bottom());
                         linesChangedFrom(above == count ? cursor.getRow() : scrollingRegion.top());
                 }
 
                 public void setInsertMode(boolean newInsertMode) {
                         insertMode = newInsertMode;
                 }
 
                 public void processLine(String line) {
                         TextLine textLine = getCursorTextLine();
                         if (insertMode) {
                                 //Log.warn("Inserting text \"" + line + "\" at " + cursor + ".");
                                 textLine.insertTextAt(cursor.getColumn(), line, currentStyle);
                         } else {
                                 //Log.warn("Writing text \"" + line + "\" at " + cursor + ".");
                                 textLine.writeTextAt(cursor.getColumn(), line, currentStyle);
                         }
                         textAdded(line.length());
                 }
 
                 private TextLine getCursorTextLine() {
                         return textLines.get(cursor.getRow());
                 }
 
                 private void textAdded(int length) {
                         moveCursorHorizontally(length);
                         linesChangedFromCursor();
                 }
 
                 public void processSpecialCharacter(char ch) {
                         switch (ch) {
                         case Ascii.CR:
                                 cursor = cursor.moveToColumn(0);
                                 return;
                         case Ascii.LF:
                                 moveToRow(cursor.getRow() + 1);
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
 
                 private void moveToRow(int index) {
                         // NOTE: We only really allow index to be
                         // scrollingRegion.bottom() + 1.
                         if (index > scrollingRegion.bottom())
                                 insertLines(index, 1);
                         else
                                 cursor = cursor.moveToRow(index);
                 }
 
                 private void insertTab() {
                         int nextTabLocation = getNextTabPosition(cursor.getColumn());
                         TextLine textLine = getCursorTextLine();
                         int startOffset = cursor.getColumn();
                         int tabLength = nextTabLocation - startOffset;
                         // We want to insert our special tabbing characters
                         // (see getTabString) when inserting a tab or
                         // outputting one at the end of a line, so that text
                         // copied from the output of (say) cat(1) will be
                         // pasted with tabs preserved.
                         boolean endOfLine = (startOffset == textLine.length());
                         if (insertMode || endOfLine) {
                                 textLine.insertTabAt(startOffset, tabLength, currentStyle);
                         } else {
                                 // Emacs, source of all bloat, uses \t\b\t
                                 // sequences around tab stops (in lines with no
                                 // \t characters) if you hold down right arrow.
                                 // The call to textAdded below moves the
                                 // cursor, which is all we're supposed to do.
                         }
                         textAdded(tabLength);
                 }
 
                 private int getNextTabPosition(int column) {
                         return (column + 8) & ~7;
                 }
 
                 public void setCursorVisible(boolean visible) {
                         if (cursor.isVisible() == visible)
                                 return;
                         cursor = cursor.setVisible(visible);
                         listeners.cursorVisibilityChanged(visible);
                 }
 
                 public void deleteCharacters(int count) {
                         getCursorTextLine().killText(cursor.getColumn(),
                                                      cursor.getColumn() + count);
                         linesChangedFromCursor();
                 }
 
                 public void clearToEndOfLine() {
                         TextLine line = getCursorTextLine();
                         line.killText(cursor.getColumn(), line.length());
                         linesChangedFromCursor();
                 }
 
                 public void clearToBeginningOfLine() {
                        getCursorTextLine().writeTextAt(0,
                                StringUtilities.nCopies(cursor.getColumn() + 1, ' '),
                                StyledText.getDefaultStyle());
                         linesChangedFromCursor();
                 }
 
                 public void clearToEndOfScreen() {
                         clearToEndOfLine();
                         for (TextLine line : textLines.region(cursor.getRow() + 1, getLineCount()))
                                 line.clear();
                 }
 
                 public void setCursorPosition(int row, int column) {
                         cursor = cursor.moveToRow(row).moveToColumn(column);
                 }
 
                 public void moveCursorHorizontally(int delta) {
                         cursor = cursor.adjustColumn(delta);
                 }
 
                 public void moveCursorVertically(int delta) {
                         cursor = cursor.adjustRow(delta);
                 }
 
                 public void setScrollingRegion(int top, int bottom) {
                         scrollingRegion.set((top == -1 ? 1 : top) - 1,
                                             (bottom == -1 ? getLineCount() : bottom) - 1);
                 }
 
                 public void scrollDisplayUp() {
                         insertLines(scrollingRegion.top(), 1);
                         linesChangedFrom(scrollingRegion.top());
                 }
 
                 public void deleteLines(int count) {
                         textLines.insertLines(scrollingRegion.bottom() + 1,
                                               count,
                                               cursor.getRow(),
                                               scrollingRegion.bottom());
                         linesChangedFrom(cursor.getRow());
                 }
         };
 }
