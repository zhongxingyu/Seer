 /**
  *
  * consoles - Java based console terminals
  * Copyright (c) 2013, Sandeep Gupta
  * 
  * http://www.sangupta/projects/consoles
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.consoles.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.swing.JComponent;
 import javax.swing.Timer;
 
 import com.sangupta.consoles.core.ScreenPosition;
 
 /**
  * The class helps render one screen full information of character-map (a 2D array)
  * onto the JFrame in a monospace font.
  * 
  * This class is NOT responsible for anything except rendering via a font - key handling
  * and other operations should be kept outside this class.
  *  
  * @author sangupta
  *
  */
 public class Renderer extends JComponent {
 
 	/**
 	 * Generated via Eclipse
 	 */
 	private static final long serialVersionUID = -4588816081786680739L;
 
 	/**
 	 * Default font for this renderer object.
 	 * 
 	 */
 	private static final Font FONT = new Font("Courier New", Font.PLAIN, 14);
 	
 	/**
 	 * The currently associated {@link FontMetrics} for the current {@link Font}.
 	 *  
 	 */
 	private FontMetrics fontMetrics;
 	
 	/**
 	 * Width of one character in this {@link Renderer} based on the currently selected {@link Font}.
 	 * 
 	 */
 	private int characterWidth;
 	
 	/**
 	 * The number of rows that this renderer needs to support.
 	 * 
 	 */
 	private int numRows;
 	
 	/**
 	 * The number of columns that this renderer needs to support.
 	 * 
 	 */
 	private int numColumns;
 	
 	/**
 	 * The current dimensions of this instance.
 	 * 
 	 */
 	private Dimension dimension;
 	
 	/**
 	 * Holds the reference to the cursor position object as sent by the {@link SwingTerminal}.
 	 * 
 	 */
 	private final ScreenPosition cursorPosition;
 	
 	/**
 	 * Reference to the screen view full of information. The exact screen view is kept
 	 * by the parent {@link SwingTerminal} instance.
 	 * 
 	 */
 	private TerminalCharacter screenView[][];
 	
 	/**
 	 * Timer that is used to show/hide cursor to achieve cursor blinking
 	 * effect
 	 */
 	private final Timer cursorBlinkTimer;
 	
 	/**
 	 * Indicates the current state of cursor blink - visible and invisible
 	 */
 	private boolean cursorBlinkVisible = false;
 	
 	/**
 	 * Current location of visible area in the buffer view
 	 */
 	private final AtomicInteger screenLocationRow;
 	
 	/**
 	 * Current scroll position
 	 */
 	private volatile int screenScrollPosition;
 	
 	/**
 	 * Holds whether we are in writing mode or the scrolling mode
 	 */
 	private volatile boolean writingMode = true;
 	
 	/**
 	 * Create an instance of {@link Renderer} for the given number of rows and columns.
 	 * 
 	 * @param columns
 	 * @param rows
 	 */
 	public Renderer(int columns, int rows, TerminalCharacter screenView[][], ScreenPosition cursorPosition, AtomicInteger screenLocationRow) {
 		this.numColumns = columns;
 		this.numRows = rows;
 		this.screenView = screenView;
 		this.cursorPosition = cursorPosition;
 		this.cursorBlinkTimer = new Timer(500, new CursorBlinkAction());
 		this.cursorBlinkTimer.start();
 		
 		this.screenLocationRow = screenLocationRow;
 	}
 	
 	/**
 	 * Get the preferred size of this {@link Renderer} instance.
 	 * 
 	 * @see javax.swing.JComponent#getPreferredSize()
 	 */
 	public Dimension getPreferredSize() {
 		if(this.dimension != null) {
 			return this.dimension;
 		}
 		
 		if(this.fontMetrics == null) {
 			this.fontMetrics = getGraphics().getFontMetrics(FONT);
 			this.characterWidth = this.fontMetrics.charWidth(' ');
 		}
 		
 		recomputeDimension();
 		return this.dimension;
 	}
 	
 	/**
 	 * Method that redraws the entire screen state.
 	 * 
 	 */
 	@Override
 	protected void paintComponent(Graphics g) {
 		final Graphics2D graphics2D = (Graphics2D) g.create();
 		
 		// build up the instance
 		graphics2D.setFont(FONT);
 		graphics2D.fillRect(0, 0, this.dimension.width, this.dimension.height);
 		
 		// start rendering the characters
 		TerminalCharacter currentChar;
 		String charString;
 		
 		final int rowDelta;
 		if(this.writingMode) {
 			rowDelta = this.screenLocationRow.get();
 		} else {
 			// we are in scrolling mode
 			rowDelta = this.screenScrollPosition;
 		}
 		
 		for(int row = 0; row < this.numRows; row++) {
 			if((row + rowDelta) >= this.screenView.length) {
 				break;
 			}
 			
 			for(int column = 0; column < this.numColumns; column++) {
 				currentChar = this.screenView[row + rowDelta][column];
 				
 				if(currentChar != null) {
 					if(currentChar.character == 0) {
 						currentChar.character = ' ';
 					}
 					
 					charString = Character.toString(currentChar.character);
 					
 					if(currentChar.highlighted) {
 						graphics2D.setColor(Color.WHITE);
 						graphics2D.fillRect(column * this.characterWidth, row * this.fontMetrics.getHeight(), this.characterWidth, this.fontMetrics.getHeight());
 						graphics2D.setColor(currentChar.background);
 					} else if(this.cursorPosition.equals(row, column) && this.cursorBlinkVisible) {
 						// reverse
 						graphics2D.fillRect(column * this.characterWidth, row * this.fontMetrics.getHeight(), this.characterWidth, this.fontMetrics.getHeight());
 						graphics2D.setColor(currentChar.background);
 						graphics2D.setBackground(currentChar.foreground);
 					} else {
 						graphics2D.setColor(currentChar.foreground);
 						graphics2D.setBackground(currentChar.background);
 					}
 				} else {
 					charString = " ";
 				}
 				
 				graphics2D.drawString(charString, column * this.characterWidth, ((row + 1) * this.fontMetrics.getHeight()) - this.fontMetrics.getDescent());
 			}
 		}
 		
 		graphics2D.dispose();
 	}
 	
 	/**
 	 * Following method can be used to reset dimension in case number
 	 * of rows/columns change on screen.
 	 * 
 	 */
 	private void recomputeDimension() {
 		int width = this.numColumns * this.fontMetrics.charWidth(' ');
 		int height = this.numRows * this.fontMetrics.getHeight();
 		this.dimension = new Dimension(width, height);
 	}
 	
 	/**
 	 * Return the {@link FontMetrics} instance associated with this {@link Renderer} object.
 	 * 
 	 * @return
 	 */
 	public FontMetrics getFontMetrics() {
 		return this.fontMetrics;
 	}
 
 	/**
 	 * Return the width of one character in the currently set font.
 	 * 
 	 * @return
 	 */
 	public int getCharacterWidth() {
 		return this.characterWidth;
 	}
 	
 	/**
 	 * Dispose of this renderer.
 	 * 
 	 */
 	public void dispose() {
 		this.cursorBlinkTimer.stop();
 	}
 
 	// Other included classes follow
 	
 	/**
 	 * Action listener that mimicks cursor blinking effect
 	 * by showing/hiding cursor every few milli-seconds (500ms by
 	 * default).
 	 * 
 	 * @author sangupta
 	 *
 	 */
 	private class CursorBlinkAction implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			cursorBlinkVisible = !cursorBlinkVisible;
 			repaint();
 		}
 		
 	}
 
 	/**
 	 * Method allows to resize the renderer to a new size so that JFrame can be adjusted
 	 * accordingly.
 	 * 
 	 * @param newScreenView
 	 * @param newRows
 	 * @param newColumns
 	 */
 	public void resizeRenderer(TerminalCharacter[][] newScreenView, int newRows, int newColumns) {
 		this.numColumns = newColumns;
 		this.numRows = newRows;
 		this.screenView = newScreenView;
 
 		recomputeDimension();
 	}
 
 	public int[] getSizeInCharacterBlocks(Dimension dimension) {
 		int[] values = new int[2];
 		
 		values[0] = (int) (dimension.getHeight() / this.fontMetrics.getHeight());
 		values[1] = (int) (dimension.getWidth() / this.characterWidth);
 		
 		return values;
 	}
 
 	public ScreenPosition getScreenPosition(Point point) {
 		if(point == null) {
 			return null;
 		}
 		
 		int row = (int) (point.getY() / this.fontMetrics.getHeight());
 		int col = (int) (point.getX() / this.characterWidth);
 		
 		return new ScreenPosition(row, col);
 	}
 
 	void switchToWritingMode() {
 		this.writingMode = true;
 	}
 	
 	void scrollToPosition(int scrollPosition) {
 		this.screenScrollPosition = scrollPosition;
 		this.writingMode = false;
 	}
 
 }
