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
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Arrays;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.swing.JFrame;
 
 import com.sangupta.consoles.core.InputKey;
 import com.sangupta.consoles.core.ScreenPosition;
 
 /**
  * Java Swing based terminal that uses {@link JFrame} to render the terminal.
  * 
  * @author sangupta
  *
  */
 public class SwingTerminal {
 	
 	/**
 	 * Number of default columns in a terminal
 	 */
 	private static final int DEFAULT_COLUMNS = 80;
 	
 	/**
 	 * Number of default rows in a terminal
 	 */
 	private static final int DEFAULT_ROWS = 25;
 	
 	/**
 	 * Default background color for a terminal
 	 */
 	private static final Color BACKGROUND_COLOR = new Color(0, 0, 0);
 	
 	/**
 	 * Default foreground color for a terminal
 	 */
 	private static final Color FOREGROUND_COLOR = new Color(255, 255, 255);
 	
 	/**
 	 * Default tab stops, 4 chars per tab
 	 */
 	private static final int TAB_STOP = 4;
 	
 	/**
 	 * Reference to the internal {@link JFrame} instance.
 	 */
 	private final JFrame hostFrame;
 	
 	/**
 	 * Reference to the internal {@link Renderer} instance. The renderer
 	 * picks up the screen view and displays in the current frame instance.
 	 * The renderer is responsible to display one screen-size of information.
 	 * This screen-view is provided by this {@link SwingTerminal} instance.
 	 * 
 	 */
 	private final Renderer renderer;
 	
 	/**
 	 * An instance of the empty character represented by a <b>SPACE</b> character
 	 * in the currently set foreground/background color.
 	 * 
 	 */
 	private final TerminalCharacter emptyCharacter;
 	
 	/**
 	 * Holds one screen-view of information for this console.
 	 * 
 	 */
 	private final TerminalCharacter screenView[][];
 	
 	/**
 	 * Holds the current position of the cursor
 	 */
 	private final ScreenPosition cursorPosition;
 	
 	/**
 	 * Holds the list of key-strokes as they keep coming in
 	 */
 	protected final Queue<InputKey> inputKeys;
 	
 	/**
 	 * Mutex lock to make sure that only one thread changes the screen
 	 * display at one time. Used when we are writing to the terminal, 
 	 * clearing the terminal, or resizing the terminal. 
 	 */
 	private final Object CHANGE_MUTEX = new Object();
 	
 	/**
 	 * Signals the keyboard input thread to break immediately as 
 	 * we are closing down.
 	 */
 	private boolean closingTerminal = false;
 	
 	/**
 	 * Default constructor - uses the default number of rows and columns
 	 * to construct and instance.
 	 * 
 	 */
 	public SwingTerminal() {
 		this(DEFAULT_COLUMNS, DEFAULT_ROWS);
 	}
 
 	/**
 	 * Construct an instance of Swing based terminal instance with the given
 	 * number of rows and columns. The pixel-size of the instance is obtained
 	 * by the height/width of a character in the font associated with the
 	 * renderer instance.
 	 * 
 	 * @param defaultColumns
 	 * @param defaultRows
 	 */
 	public SwingTerminal(int defaultColumns, int defaultRows) {
 		this.hostFrame = new JFrame();
 		
 		this.emptyCharacter = new TerminalCharacter(' ', FOREGROUND_COLOR, BACKGROUND_COLOR);
 		this.screenView = new TerminalCharacter[defaultRows][defaultColumns];
 		
 		// initialize screen view
 		for(int row = 0; row < this.DEFAULT_ROWS; row++) {
 			Arrays.fill(this.screenView[row], this.emptyCharacter);
 		}
 		
 		this.cursorPosition = new ScreenPosition();
 		this.renderer = new Renderer(defaultColumns, defaultRows, this.screenView, this.cursorPosition);
 		
 		this.inputKeys = new ConcurrentLinkedQueue<InputKey>();
 		
 		// initialize
 		this.hostFrame.getContentPane().add(this.renderer);
 		this.hostFrame.pack();
 		
 		this.hostFrame.addKeyListener(new InputKeyListener(this.inputKeys));
 		
 		this.hostFrame.setLocationByPlatform(true);
 		this.hostFrame.setResizable(false);
 		this.hostFrame.setSize(this.renderer.getPreferredSize());
 		
 		// add the closing handler for the terminal
 		this.hostFrame.addWindowListener(new WindowAdapter() {
 			
 			@Override
 			public void windowClosing(WindowEvent e) {
 				closeTerminal();
 			}
 			
 		});
 
 		this.hostFrame.setVisible(true);
 		this.hostFrame.pack();
 	}
 	
 	/**
 	 * Close this terminal and dispose of all associated resources.
 	 * 
 	 */
 	public void closeTerminal() {
 		this.closingTerminal = true;
 		this.renderer.dispose();
 		this.hostFrame.setVisible(false);
 		this.hostFrame.dispose();
 	}
 	
 	/**
 	 * Set the title of the window being used.
 	 * 
 	 * @param title
 	 */
 	public void setTitle(String title) {
 		this.hostFrame.setTitle(title);
 	}
 	
 	/**
 	 * Method that reads a string from the terminal and sends it back.
 	 * 
 	 */
 	public String readString(boolean echo, char mask) {
 		final StringBuilder builder = new StringBuilder();
 		
 		InputKey key;
 		while(true) {
 			key = this.getKey();
 			
 			if(this.closingTerminal) {
 				break;
 			}
 			
 			// ENTER
 			if(key.ch == '\n' && !key.altPressed && !key.ctrlPressed) {
 				int currentRow = this.cursorPosition.getRow();
 				currentRow++;
 				if(currentRow == this.DEFAULT_ROWS) {
 					scrollUp();
 					currentRow--;
 				}
 				
 				this.cursorPosition.setPosition(currentRow, 0);
 				break;
 			}
 			
 			// BACKSPACE
 			if(key.ch == 8) {
 				if(builder.length() > 0) {
 					builder.deleteCharAt(builder.length() - 1);
 				}
 				
 				setRelativeChar(0, -1, ' ');
 				continue;
 			}
 			
 			// SPECIAL KEYS
 			if(key.specialKey != null) {
 				
 				switch(key.specialKey) {
 					
 					case LeftArrow:
 						this.setRelativeCursorPosition(0, -1);
 						continue;
 						
 					default:
 						// do nothing
 						break;
 				}
 			}
 			
 			// all well
 			// add the character to string
 			builder.append(key.ch);
 			
 			// display on screen as needed
 			if(echo) {
 				if(mask == 0) {
 					writeChar(key.ch);
 				} else {
 					writeChar(mask);
 				}
 			}
 		}
 		
 		return builder.toString();
 	}
 	
 	/**
 	 * Set the char at position relative to current position to the given
 	 * char. This also sets the current cursor position to the given element.
 	 * 
 	 * @param rows
 	 * @param columns
 	 * @param ch
 	 */
 	void setRelativeChar(int rows, int columns, char ch) {
 		this.setRelativeCursorPosition(rows, columns);
 		this.writeChar(ch);
 		this.setRelativeCursorPosition(0, -1);
 	}
 	
 	/**
 	 * Set relative cursor position
 	 * 
 	 * @param rows
 	 * @param columns
 	 */
 	void setRelativeCursorPosition(int rows, int columns) {
 		int row = this.cursorPosition.getRow() + rows;
 		int col = this.cursorPosition.getColumn() + columns;
 		
 		while(col < 0) {
 			col = col + this.DEFAULT_COLUMNS;
 			row--;
 		}
 		
 		if(row < 0) {
 			return;
 		}
 		
 		this.cursorPosition.setPosition(row, col);
 		this.refresh();
 	}
 	
 	/**
 	 * Write a string to the terminal and repaint.
 	 * 
 	 * @param string
 	 */
 	public void writeString(String string) {
 		if(string == null) {
 			return;
 		}
 
 		this.write(string);
 		this.refresh();
 	}
 	
 	/**
 	 * Write a character to the terminal and repaint.
 	 * 
 	 * @param ch
 	 */
 	public void writeChar(char ch) {
 		if(ch == 0) {
 			return;
 		}
 		
 		this.write(ch);
 		this.refresh();
 	}
 	
 	/**
 	 * Write a string to the renderer. This will not repaint the
 	 * renderer. Method {@link SwingTerminal#refresh()} must be called
 	 * to explicitly make the string visible.
 	 * 
 	 * External callees should use the public methods as exposed than
 	 * using this method. 
 	 * 
 	 * @param string
 	 */
 	void write(String string) {
 		if(string == null) {
 			return;
 		}
 		
 		int length = string.length();
 		char[] chars = string.toCharArray();
 		write(chars, 0, length);
 	}
 	
 	/**
 	 * Write a set of characters to the renderer.
 	 * 
 	 * @param chars
 	 * @param offset
 	 * @param length
 	 */
 	void write(final char[] chars, final int offset, final int length) {
 		if(chars == null) {
 			return;
 		}
 		
 		char charToWrite;
 		
 		synchronized (CHANGE_MUTEX) {
 			int col = this.cursorPosition.getColumn();
 			int row = this.cursorPosition.getRow();
 			
 			for(int index = offset; index < length; index++) {
 				charToWrite = chars[index];
 				
 				switch(charToWrite) {
 					case '\n':
 					case '\r':
 						row++;
 						col = 0;
 						break;
 						
 					case '\t':
 						// compute the number of tab stops that we need to add
 						int spaces = TAB_STOP - (col % TAB_STOP);
 						if(spaces == 1) {
 							this.screenView[row][col++] = new TerminalCharacter(' ', FOREGROUND_COLOR, BACKGROUND_COLOR);
 						} else {
 							// TODO: optimize this to prevent recursive call
 							this.cursorPosition.setPosition(row, col);
 							String spaced = "";
 							for(int i = 0; i < spaces; i++) {
 								spaced += " ";
 							}
 							this.write(spaced);
 							
 							col = this.cursorPosition.getColumn();
 							row = this.cursorPosition.getRow();
 						}
 						break;
 						
 					default:
 						this.screenView[row][col++] = new TerminalCharacter(charToWrite, FOREGROUND_COLOR, BACKGROUND_COLOR);
 				}
 				
 				// check for next line
 				if(col == this.DEFAULT_COLUMNS) {
 					col = 0;
 					row++;
 				}
 				
 				// check if we need to move to next line
 				if(row == this.DEFAULT_ROWS) {
 					// scroll up
 					scrollUp();
 					row--;
 				}
 			}
 			
 			this.cursorPosition.setPosition(row, col);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param ch
 	 */
 	void write(char ch) {
 		if(ch == 0) {
 			return;
 		}
 		
 		synchronized (CHANGE_MUTEX) {
 			int col = this.cursorPosition.getColumn();
 			int row = this.cursorPosition.getRow();
 			
 			this.screenView[row][col++] = new TerminalCharacter(ch, FOREGROUND_COLOR, BACKGROUND_COLOR);
 
 			// check for next line
 			if(col == this.DEFAULT_COLUMNS) {
 				col = 0;
 				row++;
 			}
 			
 			// check if we need to move to next line
 			if(row == this.DEFAULT_ROWS) {
 				// scroll up
 				scrollUp();
 				row--;
 			}
 			
 			this.cursorPosition.setPosition(row, col);
 		}
 	}
 	
 	/**
 	 * Method that will scroll the window up by one row.
 	 * 
 	 */
 	void scrollUp() {
 		synchronized (CHANGE_MUTEX) {
 			for(int row = 1; row < this.DEFAULT_ROWS; row++) {
 				this.screenView[row - 1] = this.screenView[row];
 			}
 
 			int row = this.DEFAULT_ROWS - 1;
 			this.screenView[row] = new TerminalCharacter[this.DEFAULT_COLUMNS];
 			Arrays.fill(this.screenView[row], this.emptyCharacter);
 		}
 	}
 	
 	/**
 	 * Clear this terminal. This basically means that we need to render the empty
 	 * character in the entire screen space.
 	 * 
 	 */
 	public void clearTerminal() {
 		synchronized (CHANGE_MUTEX) {
 			for(int row = 0; row < this.screenView.length; row++) {
 				Arrays.fill(this.screenView[row], this.emptyCharacter);
 			}
 			
 			this.moveCursor(0, 0);
 		}
 	}
 	
 	/**
 	 * Move the cursor to the designated position on screen.
 	 * 
 	 * @param row
 	 * @param column
 	 */
 	public void moveCursor(int row, int column) {
 		this.cursorPosition.setPosition(row, column);
 		this.refresh();
 	}
 	
 	/**
 	 * Refresh the internal renderer
 	 */
 	public void refresh() {
 		this.renderer.repaint();
 	}
 	
 	/**
 	 * Read a key in a non-blocking fashion. If no key is 
 	 * available, returns <code>null</code>.
 	 * 
 	 * @return
 	 */
 	public InputKey readKey() {
 		return this.inputKeys.poll();
 	}
 	
 	/**
 	 * Read a key blockingly. If no key is available, the thread
 	 * will wait till one is available.
 	 * 
 	 * This method will never return a <code>null</code>
 	 * 
 	 * @return
 	 */
 	public InputKey getKey() {
 		InputKey key = null;
 		while(key == null) {
 			if(this.closingTerminal) {
 				break;
 			}
 			
 			key = this.inputKeys.poll();
 		}
 		
 		return key;
 	}
 
 }
