 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.textpane;
 
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.ui.interfaces.Window;
 import com.dmdirc.ui.messages.IRCDocument;
 import com.dmdirc.ui.messages.IRCDocumentListener;
 import com.dmdirc.ui.messages.LinePosition;
 import com.dmdirc.ui.messages.Styliser;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 import javax.swing.BoundedRangeModel;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JScrollBar;
 import javax.swing.SwingConstants;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Styled, scrollable text pane.
  */
 public final class TextPane extends JComponent implements MouseWheelListener,
         AdjustmentListener, IRCDocumentListener, ConfigChangeListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 5;
     /** Scrollbar model. */
     private final BoundedRangeModel scrollModel;
     /** Canvas object, used to draw text. */
     private final TextPaneCanvas canvas;
     /** IRCDocument. */
     private final IRCDocument document;
     /** Parent Frame. */
     private final Window frame;
     /** Indicator to show whether new lines have been added. */
     private final JLabel newLineIndicator;
     /** Last seen line. */
     private int lastSeenLine = 0;
     /** Show new line notifications. */
     private boolean showNotification;
 
     /**
      * Creates a new instance of TextPane.
      *
      * @param frame Parent Frame
      */
     public TextPane(final Window frame) {
         super();
         this.frame = frame;
 
         setUI(new TextPaneUI());
         document = frame.getContainer().getDocument();
         newLineIndicator = new JLabel("", SwingConstants.CENTER);
         newLineIndicator.setBackground(Color.RED);
         newLineIndicator.setForeground(Color.WHITE);
         newLineIndicator.setOpaque(true);
         newLineIndicator.setVisible(false);
 
         setLayout(new MigLayout("fill, hidemode 3"));
         canvas = new TextPaneCanvas(this, document);
         add(canvas, "dock center");
         add(newLineIndicator, "dock south, center, grow");
         scrollModel = new TextPaneBoundedRangeModel();
         scrollModel.setExtent(0);
         final JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL);
         scrollBar.setModel(scrollModel);
         add(scrollBar, "dock east");
         scrollBar.addAdjustmentListener(this);
         scrollBar.addAdjustmentListener(canvas);
         frame.getContainer().getConfigManager().addChangeListener(
                 ((SwingController) frame.getController()).getDomain(),
                 "textpanelinenotification", this);
         configChanged("", "textpanelinenotification");
 
         addMouseWheelListener(this);
         document.addIRCDocumentListener(this);
         setAutoscrolls(true);
 
         final MouseMotionListener doScrollRectToVisible
                 = new MouseMotionAdapter() {
 
             /** {@inheritDoc} */
             @Override
             public void mouseDragged(final MouseEvent e) {
                 if (e.getXOnScreen() > getLocationOnScreen().getX()
                         && e.getXOnScreen() < (getLocationOnScreen().getX()
                         + getWidth()) && e.getModifiersEx()
                         == MouseEvent.BUTTON1_DOWN_MASK) {
                     if (getLocationOnScreen().getY() > e.getYOnScreen()) {
                         scrollModel.setValue(scrollBar.getValue() - 1);
                     } else if (getLocationOnScreen().getY() + getHeight() 
                             < e.getYOnScreen()) {
                         scrollModel.setValue(scrollBar.getValue() + 1);
                     }
                     canvas.highlightEvent(MouseEventType.DRAG, e);
                 }
             }
         };
         addMouseMotionListener(doScrollRectToVisible);
 
         setScrollBarMax(document.getNumLines(), document.getNumLines() - 1);
     }
 
     /** {@inheritDoc} */
     @Override
     public void updateUI() {
         setUI(new TextPaneUI());
     }
 
     /**
      * Returns the last visible line in the textpane.
      *
      * @return Last visible line index
      */
     public int getLastVisibleLine() {
         return scrollModel.getValue();
     }
 
     /**
      * Sets the scrollbar's maximum position. If the current position is
      * within <code>linesAllowed</code> of the end of the document, the
      * scrollbar's current position is set to the end of the document.
      *
      * @param lines Current number of lines
      * @param linesAllowed The number of lines allowed below the
      * current position
      * @since 0.6
      */
     protected void setScrollBarMax(final int lines, final int linesAllowed) {
         final int currentLine = scrollModel.getValue();
         final int allowedDeviation = lines - 1 - linesAllowed;
         boolean setToMax = currentLine == allowedDeviation;
 
         if (allowedDeviation <= 0) {
             setToMax = true;
         }
 
         if (lines <= 1) {
             scrollModel.setRangeProperties(lines, 0, lines, lines, false);
         } else if (setToMax) {
             scrollModel.setRangeProperties(lines - 1, 0, 0, lines - 1, false);
         } else {
             scrollModel.setRangeProperties(scrollModel.getValue(), 0, 0,
                     lines - 1, false);
         }
     }
 
     /**
      * Sets the new position for the scrollbar and the associated position
      * to render the text from.
      * @param position new position of the scrollbar
      */
     public void setScrollBarPosition(final int position) {
         scrollModel.setValue(position);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse wheel event
      */
     @Override
     public void adjustmentValueChanged(final AdjustmentEvent e) {
         if (showNotification && e.getValue() >= scrollModel.getMaximum()) {
             newLineIndicator.setVisible(false);
         }
 
         lastSeenLine = Math.max(lastSeenLine, e.getValue());
 
         final int lines = scrollModel.getMaximum() - lastSeenLine;
         newLineIndicator.setText("↓ " + lines + " new line"
                 + (lines == 1 ? "" : "s") + " ↓");
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse wheel event
      */
     @Override
     public void mouseWheelMoved(final MouseWheelEvent e) {
         if (e.getWheelRotation() > 0) {
             scrollModel.setValue(scrollModel.getValue() + e.getScrollAmount());
         } else {
             scrollModel.setValue(scrollModel.getValue() - e.getScrollAmount());
         }
     }
 
     /**
      *
      * Returns the line information from a mouse click inside the textpane.
      *
      * @param point mouse position
      *
      * @return line number, line part, position in whole line
      */
     public LineInfo getClickPosition(final Point point) {
         return canvas.getClickPosition(point, true);
     }
 
     /**
      *
      * Returns the line information from a mouse click inside the textpane.
      *
      * @param point mouse position
      * @param selection Are we selecting text?
      *
      * @return line number, line part, position in whole line
      *
      * @since 0.6.3
      */
     public LineInfo getClickPosition(final Point point,
             final boolean selection) {
         return canvas.getClickPosition(point, selection);
     }
 
     /**
      * Returns the selected text.
      *
      * @return Selected text
      */
     public String getSelectedText() {
         return getSelectedText(false);
     }
 
     /**
      * Returns the selected text.
      *
      * @return Selected text
      */
     public String getStyledSelectedText() {
         return getSelectedText(true);
     }
 
     /**
      * Returns the selected text.
      *
      * @param styled Return styled text?
      *
      * @return Selected text
      */
     public String getSelectedText(final boolean styled) {
         final StringBuffer selectedText = new StringBuffer();
         final LinePosition selectedRange = canvas.getSelectedRange();
 
         if (selectedRange.getStartLine() == -1) {
             return null;
         }
 
         for (int i = selectedRange.getStartLine(); i
                 <= selectedRange.getEndLine(); i++) {
             if (i != selectedRange.getStartLine()) {
                 selectedText.append('\n');
             }
             if (scrollModel.getMaximum() < i) {
                 return selectedText.toString();
             }
             final String line;
             if (styled) {
                 line = document.getLine(i).getStyledText();
             } else {
                 line = document.getLine(i).getText();
             }
             if (!line.isEmpty()) {
                 if (selectedRange.getEndLine()
                         == selectedRange.getStartLine()) {
                     //loop through range
                     if (selectedRange.getStartPos() != -1 && selectedRange.
                             getEndPos() != -1) {
                         selectedText.append(getText(line,
                                 selectedRange.getStartPos(),
                                 selectedRange.getEndPos(), styled));
                     }
                 } else if (i == selectedRange.getStartLine()) {
                     //loop from start of range to the end
                     if (selectedRange.getStartPos() != -1) {
                        selectedText.append(getText(line, selectedRange
                                .getStartPos(), line.length(), styled));
                     }
                 } else if (i == selectedRange.getEndLine()) {
                     //loop from start to end of range
                     if (selectedRange.getEndPos() != -1) {
                         selectedText.append(getText(line, 0, selectedRange
                                 .getEndPos(), styled));
                     }
                 } else {
                     //loop the whole line
                     selectedText.append(getText(line, 0, line.length(),
                             styled));
                 }
             }
         }
 
         return selectedText.toString();
     }
 
     /**
      * Gets a range of text (styled or unstyled) from the given text.
      *
      * @param text Text to extract text from
      * @param start Start index
      * @param end End index
      * @param styled Styled text?
      *
      * @return Requested text range as a String
      */
     private String getText(final String text, final int start, final int end,
             final boolean styled) {
         if (styled) {
             return Styliser.getStyledText(text, start, end);
         } else {
             return text.substring(start, end);
         }
     }
 
     /**
      * Returns the selected range.
      *
      * @return selected range
      */
     public LinePosition getSelectedRange() {
         return canvas.getSelectedRange();
     }
 
     /**
      * Returns whether there is a selected range.
      *
      * @return true iif there is a selected range
      */
     public boolean hasSelectedRange() {
         final LinePosition selectedRange = canvas.getSelectedRange();
         return !(selectedRange.getStartLine() == selectedRange.getEndLine()
                 && selectedRange.getStartPos() == selectedRange.getEndPos());
     }
 
     /**
      * Selects the specified region of text.
      *
      * @param position Line position
      */
     public void setSelectedTexT(final LinePosition position) {
         canvas.setSelectedRange(position);
     }
 
     /**
      * Returns the type of text this click represents.
      *
      * @param lineInfo Line info of click.
      *
      * @return Click type for specified position
      */
     public ClickTypeValue getClickType(final LineInfo lineInfo) {
         return canvas.getClickType(lineInfo);
     }
 
     /**
      * Returns the surrouding word at the specified position.
      *
      * @param lineNumber Line number to get word from
      * @param index Position to get surrounding word
      *
      * @return Surrounding word
      */
     public String getWordAtIndex(final int lineNumber, final int index) {
         if (lineNumber == -1) {
             return "";
         }
         final int[] indexes = canvas.getSurroundingWordIndexes(document
                 .getLine(lineNumber).getText(), index);
         return document.getLine(lineNumber).getText().substring(indexes[0],
                 indexes[1]);
     }
 
     /** Adds the selected text to the clipboard. */
     public void copy() {
         copy(false);
     }
 
     /**
      * Adds the selected text to the clipboard.
      *
      * @param copyControlCharacters Should we copy control codes, or strip them?
      */
     public void copy(final boolean copyControlCharacters) {
         if (getSelectedText() != null && !getSelectedText().isEmpty()) {
             Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                         new StringSelection(getSelectedText(
                         copyControlCharacters)), null);
         }
     }
 
     /** Clears the textpane. */
     public void clear() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc}. */
             @Override
             public void run() {
                 document.clear();
             }
         });
     }
 
     /** Clears the selection. */
     public void clearSelection() {
         canvas.clearSelection();
     }
 
     /** Scrolls one page up in the textpane. */
     public void pageDown() {
         scrollModel.setValue(scrollModel.getValue() + 10);
     }
 
     /** Scrolls one page down in the textpane. */
     public void pageUp() {
         scrollModel.setValue(scrollModel.getValue() - 10);
     }
 
     /** Scrolls to the beginning of the TextPane. */
     public void goToHome() {
         scrollModel.setValue(0);
     }
 
     /** Scrolls to the end of the TextPane. */
     public void goToEnd() {
         scrollModel.setValue(scrollModel.getMaximum());
     }
 
     /** {@inheritDoc}. */
     @Override
     @Deprecated
     public void lineAdded(final int line, final int size) {
         // DO NOTHING, MWAHAHAHAHAHA
     }
 
     /** {@inheritDoc}. */
     @Override
     public void trimmed(final int newSize, final int numTrimmed) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc}. */
             @Override
             public void run() {
                 final LinePosition selectedRange = getSelectedRange();
                 selectedRange.setStartLine(selectedRange.getStartLine()
                         - numTrimmed);
                 selectedRange.setEndLine(selectedRange.getEndLine()
                         - numTrimmed);
                 if (selectedRange.getStartLine() < 0) {
                     selectedRange.setStartLine(0);
                 }
                 if (selectedRange.getEndLine() < 0) {
                     selectedRange.setEndLine(0);
                 }
                 setSelectedTexT(selectedRange);
                 setScrollBarMax(newSize, 1);
             }
         });
     }
 
     /** {@inheritDoc}. */
     @Override
     public void cleared() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc}. */
             @Override
             public void run() {
                 scrollModel.setMaximum(0);
                 scrollModel.setValue(0);
                 canvas.recalc();
             }
         });
     }
 
     /** {@inheritDoc}. */
     @Override
     public void linesAdded(final int line, final int length, final int size) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc}. */
             @Override
             public void run() {
                 if (showNotification && scrollModel.getValue() != line) {
                     newLineIndicator.setVisible(true);
                 }
 
                 setScrollBarMax(size, length);
             }
         });
     }
 
     /** {@inheritDoc}. */
     @Override
     public void repaintNeeded() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc}. */
             @Override
             public void run() {
                 canvas.recalc();
             }
         });
     }
 
     /**
      * Retrieves this textpane's IRCDocument.
      *
      * @return This textpane's IRC document
      */
     public IRCDocument getDocument() {
         return document;
     }
 
     /**
      * Retrives the parent window for this textpane.
      *
      * @return Parent window
      */
     public Window getWindow() {
         return frame;
     }
 
     /**
      * Adds a textpane listener to this textpane.
      *
      * @param listener Listener to add
      */
     public void addTextPaneListener(final TextPaneListener listener) {
         canvas.addTextPaneListener(listener);
     }
 
     /**
      * Removes a textpane listener from this textpane.
      *
      * @param listener Listener to remove
      */
     public void removeTextPaneListener(final TextPaneListener listener) {
         canvas.removeTextPaneListener(listener);
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         showNotification = frame.getContainer().getConfigManager()
                 .getOptionBool(((SwingController) frame.getController())
                 .getDomain(), "textpanelinenotification");
         if (!showNotification) {
             UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc}. */
             @Override
             public void run() {
                 newLineIndicator.setVisible(false);
                 }
             });
         }
     }
 }
