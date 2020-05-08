 /**
  * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
  */
 /**
  * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
  */
 package us.stangl.crostex;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import us.stangl.crostex.autofill.AutoFiller7;
 import us.stangl.crostex.command.ClearCellCommand;
 import us.stangl.crostex.command.CommandBuffer;
 import us.stangl.crostex.command.EnterCharacterToCellCommand;
 import us.stangl.crostex.command.SetCurrentCellBlackCommand;
 import us.stangl.crostex.command.ToggleCurrentCellBlackCommand;
 import us.stangl.crostex.command.ToggleCurrentCellCircledCommand;
 import us.stangl.crostex.dictionary.Dictionary;
 import us.stangl.crostex.io.DOMSerializer;
 import us.stangl.crostex.io.IoClue;
 import us.stangl.crostex.io.IoGrid;
 import us.stangl.crostex.util.RowColumnPair;
 
 /**
  * Crossword grid.
  * @author Alex Stangl
  */
 public class Grid implements IoGrid
 {
 	// logger
 	private static final Logger LOG = Logger.getLogger(Grid.class.getName());
 
 	private static final Color LIGHT_YELLOW = new Color(255, 255, 170);
 	
 	private static final Color LIGHT_GREEN = new Color(170, 255, 170);
 	
 	// # of points per inch
 	private static final double POINTS_PER_INCH = 72.0;
 	
 	// cell width
 	private int cellWidth = 24;
 	
 	// cell height
 	private int cellHeight = 24;
 	
 	// x-offset of upper-left corner of grid in graphics context
 	private int xoffset = 0;
 	
 	// y-offset of upper-left corner of grid in graphics context
 	private int yoffset = 0;
 	
 	// grid width, in number of cells
 	private final int width;
 	
 	// height, in number of cells
 	private final int height;
 	
 	// name of grid
 	private String name;
 	
 	// description for grid
 	private String description;
 	
 	// 2-dimensional array of Cells representing this grid
 	private final Cell[][] cells;
 	
 	// currently selected cell, if any, else null
 	private Cell currentCell;
 	
 	// row (y position) of currently selected cell, if any, else -1
 	private int currentRow = -1;
 	
 	// column (x position) of currently selected cell, if any, else -1
 	private int currentColumn = -1;
 	
 	// current direction that cursor is moving, as letters are typed -- Across or Down
 	private AcrossDownDirection currentDirection = AcrossDownDirection.ACROSS;
 
 	// title of crossword
 	private String title = "";
 	
 	// author of crossword
 	private String author = "";
 	
 	// copyright notice associated with crossword
 	private String copyright = "";
 	
 	// notes associated with crossword
 	private String notes = "";
 	
 	// command buffer to hold mutating user commands, for undo/redo purposes
 	private CommandBuffer<Grid> commandBuffer = new CommandBuffer<Grid>(this);
 	
 	// whether rotational symmetry is being maintained
 	private boolean maintainingSymmetry = true;
 	
 	// registered grid change listeners; these get notified whenever this grid changes
 	private List<GridChangeListener> gridChangeListeners = new ArrayList<GridChangeListener>();
 	
 	// registered title change listeners; these get notified whenever this grid's title changes
 	private List<GridChangeListener> titleChangeListeners = new ArrayList<GridChangeListener>();
 	
 	// registered cell change listeners; these get notified whenever a cell in this grid changes
 	private List<CellChangeListener> cellChangeListeners = new ArrayList<CellChangeListener>();
 	
 	// registered full grid change listeners; these get notified whenever whole grid substantially changes
 	private List<FullGridChangeListener> fullGridChangeListeners = new ArrayList<FullGridChangeListener>();
 	
 	// whether word numbers are being displayed in grid
 	private boolean displayingWordNumbers = true;
 
 	// previous mouse button that was pressed (1 == left mouse button)
 	private int prevMouseButton = -1;
 	
 	// whether cursor is wrapping from one side to the other
 	private boolean wrappingCursor = true;
 	
 	// cursor skipping behavior
 	private CursorSkipBehavior cursorSkipBehavior = CursorSkipBehavior.SKIP_BLACK_CELLS;
 	
 	// across clues
 	private List<Clue> acrossClues = new ArrayList<Clue>();
 	
 	// down clues
 	private List<Clue> downClues = new ArrayList<Clue>();
 	
 	/**
 	 * Constructor for Grid
 	 * @param width width of grid, in number of cells
 	 * @param height height of grid, in number of cells
 	 */
 	public Grid(int width, int height)
 	{
 		this.width = width;
 		this.height = height;
 		cells = new Cell[this.height][this.width];
 		for (int row = 0; row < height; ++row) {
 			for (int col = 0; col < width; ++col) {
 				cells[row][col] = new Cell();
 			}
 		}
 	}
 	
 	/**
 	 * Constructor for Grid
 	 * @param width width of grid, in number of cells
 	 * @param height height of grid, in number of cells
 	 * @param name short name for grid
 	 * @param description longer description for grid
 	 */
 	public Grid(int width, int height, String name, String description)
 	{
 		this(width, height);
 		this.name = name;
 		this.description = description;
 	}
 	
 	/**
 	 * Copy constructor, copies another Grid
 	 * NOTE: Currently just treats gridToCopy as a template, not copying its full state.
 	 * currentCell is set according to currentRow/currentColumn, and commandBuffer is left empty
 	 * and listeners are not currently copied
 	 * @param gridToCopy source grid to copy to this
 	 */
 	public Grid(Grid gridToCopy) {
 		this(gridToCopy.getWidth(), gridToCopy.getHeight(), gridToCopy.getName(), gridToCopy.getDescription());
 		for (int row = 0; row < height; ++row) {
 			for (int col = 0; col < width; ++col) {
 				getCell(row, col).copyFrom(gridToCopy.getCell(row, col));
 			}
 		}
 		this.cellWidth = gridToCopy.cellWidth;
 		this.cellHeight = gridToCopy.cellHeight;
 		this.xoffset = gridToCopy.xoffset;
 		this.yoffset = gridToCopy.yoffset;
 		this.currentRow = gridToCopy.currentRow;
 		this.currentColumn = gridToCopy.currentColumn;
 		this.currentCell = currentRow == -1 || currentColumn == -1 ? null : getCell(currentRow, currentColumn);
 		this.currentDirection = gridToCopy.currentDirection;
 		this.title = gridToCopy.title;
 		this.author = gridToCopy.author;
 		this.copyright = gridToCopy.copyright;
 		this.notes = gridToCopy.notes;
 		this.maintainingSymmetry = gridToCopy.maintainingSymmetry;
 		this.displayingWordNumbers = gridToCopy.displayingWordNumbers;
 		this.wrappingCursor = gridToCopy.wrappingCursor;
 		this.cursorSkipBehavior = gridToCopy.cursorSkipBehavior;
 	}
 
 	/**
 	 * Return whether this grid is structurally equivalent to the specified grid, i.e., same dimensions, same pattern of blacks.
 	 * @param other
 	 * @return whether this grid is structurally equivalent to the specified grid, i.e., same dimensions, same pattern of blacks
 	 */
 	public boolean isStructureEqualTo(Grid other) {
 		if (getWidth() != other.getWidth() || getHeight() != other.getHeight())
 			return false;
 		
 		for (int row = 0; row < height; ++row)
 			for (int col = 0; col < width; ++col)
 				if (getCell(row, col).isBlack() != other.getCell(row, col).isBlack())
 					return false;
 		return true;
 	}
 
 	/**
 	 * Render grid to graphics context.
 	 * @param g graphics context
 	 */
 	public void render(Graphics2D g) {
 		render(g, getCellWidth(), getCellHeight(), false);
 	}
 	
 	/**
 	 * Render grid to graphics context.
 	 * @param g graphics context
 	 */
 	public void renderThumbnail(Graphics2D g, int containerWidth, int containerHeight) {
 		render(g, containerWidth / getWidth(), containerHeight / getHeight(), true);
 	}
 	
 
 	/**
 	 * Render grid to graphics context.
 	 * @param g graphics context
 	 */
 	public void render(Graphics2D g, int cellWidth, int cellHeight, boolean thumbnail) {
 		Font originalFont = g.getFont();
 		
 		// derive new font point size by converting cellHeight in pixels to # of points
 		Font font = originalFont.deriveFont(pixelsToPoints(cellHeight));
 		float smallFontSize = pixelsToPoints((int)(cellHeight / 2.25));
 		Font smallFont = font.deriveFont(smallFontSize);
 		FontMetrics fontMetrics = g.getFontMetrics(font);
 		int ascent = fontMetrics.getAscent();
 		int cellHeightResidual = cellHeight - ascent;
		int cellWidthResidual = cellWidth - fontMetrics.charWidth('X');
		LOG.finest("font size = " + font.getSize() + ", cellHeightResidual = " + cellHeightResidual
				+ ", cellWidthResidual = " + cellWidthResidual);
 		g.setColor(Color.BLACK);
 		g.setFont(font);
 		for (int row = 0; row < height; ++row) {
 			for (int col = 0; col < width; ++col) {
 				Cell cell = getCell(row, col);
 				if (cell.isBlack()) {
 					g.fillRect(xoffset + col * cellWidth, yoffset + row * cellHeight, cellWidth, cellHeight);
 				} else {
 					if (! thumbnail) {
 						// Color currently selected cell light yellow, and color next cell to visit with a triangle indicator
 						if (currentRow == row && currentColumn == col) {
 							g.setColor(LIGHT_GREEN);
 							g.fillRect(xoffset + col * cellWidth, yoffset + row * cellHeight, cellWidth, cellHeight);
 							g.setColor(Color.BLACK);
 						} else if (adjoinsCurrentCell(row, col)) {
 							g.setColor(LIGHT_YELLOW);
 							g.fillRect(xoffset + col * cellWidth, yoffset + row * cellHeight, cellWidth, cellHeight);
 							g.setColor(Color.BLACK);
 						}
 						String string = cell.getContents();
 						if (string.length() > 1) {
 							// Handle rebus cell special, computing optimal font size to squeeze rebus in
 							FontMetrics rebusFontMetrics = scaleFontToFitWidth(g, originalFont, string, cellWidth - 2);
 							g.setFont(rebusFontMetrics.getFont());
 							int rebusHeightResidual = cellHeight - rebusFontMetrics.getAscent();
 							int rebusWidthResidual = cellWidth - rebusFontMetrics.stringWidth(string);
 							g.drawString(string, xoffset + col * cellWidth + rebusWidthResidual / 2,
 									yoffset + row * cellHeight + rebusFontMetrics.getAscent() + rebusHeightResidual / 2);
 							g.setFont(font);
 						} else {
 							g.drawString(string, xoffset + col * cellWidth + cellWidthResidual / 2,
 									yoffset + row * cellHeight + ascent + cellHeightResidual / 2);
 						}
 						if (displayingWordNumbers && cell.getNumber() > 0) {
 							g.setFont(smallFont);
 							g.drawString(Integer.toString(cell.getNumber()), xoffset + col * cellWidth, yoffset + row * cellHeight + (int)smallFontSize);
 							g.setFont(font);
 						}
 						if (cell.isCircled()) {
 							g.drawArc(xoffset + col * cellWidth, yoffset + row * cellHeight, cellWidth, cellHeight, 0, 360);
 						}
 					}
 				}
 				g.drawRect(xoffset + col * cellWidth, yoffset + row * cellHeight, cellWidth, cellHeight);
 			}
 		}
 		g.setFont(originalFont);
 	}
 
 	/**
 	 * Print grid to specitied PrintStream.
 	 * @param stream PrintStream to print grid to
 	 */
 	public void printToStream(PrintStream stream) {
 		StringBuilder temp = new StringBuilder();
 		for (int row = 0; row < height; ++row) {
 			for (int col = 0; col < width; ++col) {
 				Cell cell = getCell(row, col);
 				if (cell.isBlack()) {
 					temp.append('#');
 				} else if (cell.isEmpty()) {
 					temp.append(' ');
 				} else {
 					temp.append(cell.getContents());
 				}
 			}
 			stream.println(temp.toString());
 			temp.setLength(0);
 		}
 	}
 	
 	/**
 	 * Regenerate numbers in all the cells by examining each cell to see which ones start words.
 	 */
 	public void renumberCells() {
 		int currNumber = 1;
 		
 		for (int row = 0; row < height; ++row)
 			for (int col = 0; col < width; ++col)
 				cells[row][col].setNumber(isStartOfAcrossWord(row, col) || isStartOfDownWord(row, col) ? currNumber++ : 0);
 		notifyGridChangeListeners();
 	}
 
 	/**
 	 * Return whether the specified (row, col) starts an across word.
 	 * It starts an across word if it is not black AND (leftmost on the
 	 * line, or has a black to its left) AND non-black to the right.
 	 * @param row 0-based row coordinate
 	 * @param col 0-based column coordinate
 	 * @return whether the specified (row, col) starts an across word
 	 */
 	public boolean isStartOfAcrossWord(int row, int col) {
 		return ! cells[row][col].isBlack()
 				&& (col == 0 || cells[row][col - 1].isBlack())
 				&& col < width - 1
 				&& ! cells[row][col + 1].isBlack();
 	}
 
 	/**
 	 * Return whether the specified (row, col) starts a down word.
 	 * It starts an down word if it is not black AND (it is topmost in
 	 * its column, or has a black above it) AND and non-black below it.
 	 * @param row 0-based row coordinate
 	 * @param col 0-based column coordinate
 	 * @return whether the specified (row, col) starts a down word
 	 */
 	public boolean isStartOfDownWord(int row, int col) {
 		return ! cells[row][col].isBlack()
 				&& (row == 0 || cells[row - 1][col].isBlack())
 				&& row < height - 1
 				&& ! cells[row + 1][col].isBlack();
 	}
 
 	/**
 	 * @param row row
 	 * @param col column
 	 * @return cell based upon 0-based row and col
 	 */
 	public Cell getCell(int row, int col) {
 		return cells[row][col];
 	}
 
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#isBlackCell(int, int)
 	 */
 	@Override
 	public boolean isBlackCell(int row, int col) {
 		return getCell(row, col).isBlack();
 	}
 	
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#setBlackCell(int, int, boolean)
 	 */
 	@Override
 	public void setBlackCell(int row, int col, boolean isBlack) {
 		getCell(row, col).setBlack(isBlack);
 	}
 	
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#isCircledCell(int, int)
 	 */
 	@Override
 	public boolean isCircledCell(int row, int col) {
 		return getCell(row, col).isCircled();
 	}
 	
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#setCircledCell(int, int, boolean)
 	 */
 	@Override
 	public void setCircledCell(int row, int col, boolean isCircled) {
 		getCell(row, col).setCircled(isCircled);
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#getCellContents(int, int)
 	 */
 	@Override
 	public String getCellContents(int row, int col) {
 		return getCell(row, col).getContents();
 	}
 	
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#setCellContents(int, int, java.lang.String)
 	 */
 	@Override
 	public void setCellContents(int row, int col, String contents) {
 		getCell(row, col).setContents(contents);
 	}
 	
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#getCellNumber(int, int)
 	 */
 	public int getCellNumber(int row, int col) {
 		return getCell(row, col).getNumber();
 	}
 	
 	/**
 	 * Handle mouse clicked event.
 	 * @param evt mouse clicked event from AWT
 	 */
 	public void mouseClicked(MouseEvent evt) {
 		LOG.finest("mouse clicked at " + evt.getX() + ", " + evt.getY());
 		int row = getRowForMouseYPosition(evt.getY());
 		int column = getColumnForMouseXPosition(evt.getX());
 		int currButton = evt.getButton();
 		// If user clicks left button in same cell in succession, toggle direction
 		if (currButton == 1 && prevMouseButton == 1 && row == currentRow && column == currentColumn) {
 			if (currentDirection == AcrossDownDirection.ACROSS)
 				currentDirection = AcrossDownDirection.DOWN;
 			else if (currentDirection == AcrossDownDirection.DOWN)
 				currentDirection = AcrossDownDirection.ACROSS;
 		}
 		if (row != -1 && column != -1) {
 			currentCell = getCell(row, column);
 			currentRow = row;
 			currentColumn = column;
 		}
 		
 		prevMouseButton = currButton;
 	}
 
 	/**
 	 * Handle key typed event.
 	 * @param evt key typed event from AWT
 	 */
 	public void keyTyped(KeyEvent evt) {
 		char c = evt.getKeyChar();
 		if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
 			c = Character.toUpperCase(c);
 			Cell currentCell = getCurrentCell();
 			if (currentCell != null && !currentCell.isBlack()) {
 				commandBuffer.applyCommand(new EnterCharacterToCellCommand(this, c));
 			}
 		} else if (c == ' ') {
 			Cell currentCell = getCurrentCell();
 			if (currentCell != null && !currentCell.isBlack()) {
 				commandBuffer.applyCommand(new ClearCellCommand(this));
 			}
 		}
 	}
 	
 	/**
 	 * Execute undoable command to toggle current cell black.
 	 */
 	public void toggleCellBlackCommand() {
 		commandBuffer.applyCommand(new ToggleCurrentCellBlackCommand(this));
 	}
 	
 	/**
 	 * Execute undoable command to toggle current cell circled.
 	 */
 	public void toggleCellCircledCommand() {
 		commandBuffer.applyCommand(new ToggleCurrentCellCircledCommand(this));
 	}
 	
 	/**
 	 * Execute undoable command to set current cell black.
 	 */
 	public void setCellBlackCommand() {
 		commandBuffer.applyCommand(new SetCurrentCellBlackCommand(this));
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getPixelHeight() {
 		return height * cellHeight + yoffset + 1;
 	}
 	
 	public int getPixelWidth() {
 		return width * cellWidth + xoffset + 1;
 	}
 	
 	/**
 	 * @return the total number of black cells
 	 */
 	public int getNumberBlackCells() {
 		int retval = 0;
 		for (int row = 0; row < height; ++row)
 			for (int col = 0; col < width; ++col)
 				if (cells[row][col].isBlack())
 					++retval;
 		return retval;
 	}
 	
 	/**
 	 * @return the total number of across words + down words
 	 */
 	public int getNumberOfWords() {
 		int retval = 0;
 		for (int row = 0; row < height; ++row)
 			for (int col = 0; col < width; ++col) {
 				if (isStartOfAcrossWord(row, col))
 					retval++;
 				if (isStartOfDownWord(row, col))
 					retval++;
 			}
 		return retval;
 	}
 	
 	/**
 	 * @return list of across words
 	 */
 	public List<GridWord> getAcrossWords() {
 		int counter = 0;
 		List<GridWord> retval = new ArrayList<GridWord>();
 		for (int row = 0; row < height; ++row)
 			for (int col = 0; col < width; ++col) {
 				if (isStartOfAcrossWord(row, col)) {
 					counter++;
 					RowColumnPair startOfWord = new RowColumnPair(row, col);
 					int colEnd = col;
 					while (colEnd < width && ! getCell(row, colEnd).isBlack())
 						colEnd++;
 					Cell[] cells = new Cell[colEnd - col];
 					--colEnd;
 					RowColumnPair endOfWord = new RowColumnPair(row, colEnd);
 					for (int i = 0; i < cells.length; ++i)
 						cells[i] = getCell(row, col + i);
 					retval.add(new GridWord(cells, AcrossDownDirection.ACROSS,
 							counter, startOfWord, endOfWord));
 				}
 				else if (isStartOfDownWord(row, col))
 					counter++;
 			}
 		return retval;
 	}
 	
 	/**
 	 * @return list of down words
 	 */
 	public List<GridWord> getDownWords() {
 		int counter = 0;
 		List<GridWord> retval = new ArrayList<GridWord>();
 		for (int row = 0; row < height; ++row)
 			for (int col = 0; col < width; ++col) {
 				if (isStartOfDownWord(row, col)) {
 					counter++;
 					RowColumnPair startOfWord = new RowColumnPair(row, col);
 					int rowEnd = row;
 					while (rowEnd < height && ! getCell(rowEnd, col).isBlack())
 						rowEnd++;
 					Cell[] cells = new Cell[rowEnd - row];
 					--rowEnd;
 					RowColumnPair endOfWord = new RowColumnPair(rowEnd, col);
 					for (int i =0; i < cells.length; ++i)
 						cells[i] = getCell(row + i, col);
 					retval.add(new GridWord(cells, AcrossDownDirection.DOWN,
 							counter, startOfWord, endOfWord));
 				}
 				else if (isStartOfAcrossWord(row, col))
 					counter++;
 			}
 		return retval;
 	}
 	
 	public boolean autoFill(Dictionary<char[], Word> dict) {
 		return new AutoFiller7().autoFill(this, dict);
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 		notifyGridChangeListeners();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 		notifyGridChangeListeners();
 	}
 	
 	public void save(String dataDirectory, String filename) throws ServiceException {
 		Document doc = DOMSerializer.newDocument("puzzle");
 		Element rootElement = doc.getDocumentElement();
 		Element crosswordElement = doc.createElement("crossword");
 		crosswordElement.setAttribute("language", "en");
 		
 		Element metadataElement = doc.createElement("metadata");
 		/*
 		Element titleElement = doc.createElement("title");
 		titleElement.appendChild(doc.createTextNode(getTitle()));
 		metadataElement.appendChild(titleElement);
 		addOptionalSimpleElement(doc, metadataElement, "date", getDate());
 		addOptionalSimpleElement(doc, metadataElement, "creator", getCreator());
 		addOptionalSimpleElement(doc, metadataElement, "rights", getRights());
 		addOptionalSimpleElement(doc, metadataElement, "publisher", getPublisher());
 		addOptionalSimpleElement(doc, metadataElement, "identifier", getIdentifier());
 		addOptionalSimpleElement(doc, metadataElement, "description", getDescription());
 		*/
 		crosswordElement.appendChild(metadataElement);
 		
 		Element americanElement = doc.createElement("american");
 		Element gridElement = doc.createElement("grid");
 		gridElement.setAttribute("rows", Integer.toString(getHeight()));
 		gridElement.setAttribute("columns", Integer.toString(getWidth()));
 		for (int row = 0; row < getHeight(); ++row) {
 			for (int col = 0; col < getWidth(); ++col) {
 				Cell cell = getCell(row, col);
 				Element cellElement;
 				if (cell.isBlack()) {
 					cellElement = doc.createElement("blank");
 				} else {
 					cellElement = doc.createElement("letter");
 					cellElement.setAttribute("id", (row + 1) + "," + (col + 1));
 					cellElement.appendChild(doc.createTextNode(cell.getContents()));
 				}
 				gridElement.appendChild(cellElement);
 			}
 		}
 		americanElement.appendChild(gridElement);
 
 		Element cluesElement = doc.createElement("clues");
 		
 		americanElement.appendChild(cluesElement);
 		crosswordElement.appendChild(americanElement);
 		rootElement.appendChild(crosswordElement);
 		
 		//TODO assume DOM tree is complete here -- need to finish population of it
 		new DOMSerializer().serialize(doc, new File(dataDirectory, filename));
 	}
 	
 	/** add simple text element to parent if childValue not null, not blank */
 	private void addOptionalSimpleElement(Document doc, Element parent, String childName, String childValue) {
 		if (childValue != null && childValue.trim().length() > 0) {
 			Element childElement = doc.createElement(childName);
 			childElement.appendChild(doc.createTextNode(childValue));
 			parent.appendChild(childElement);
 		}
 	}
 
 	private Cell getCellForXY(int x, int y) {
 		if (x < 0 || x > getCellWidth() * width || y < 0 || y > getCellHeight() * height)
 			return null;
 		int row = y / getCellHeight();
 		int col = x / getCellWidth();
 		return getCell(row, col);
 	}
 	
 	// return grid column corresponding to the specified mouse click x position, if any, else -1
 	private int getColumnForMouseXPosition(int x) {
 		return x < xoffset || x > xoffset + cellWidth * width ? -1 : (x - xoffset) / cellWidth;
 	}
 	
 	// return grid row corresponding to the specified mouse click y position, if any, else -1
 	private int getRowForMouseYPosition(int y) {
 		return y < yoffset || y > yoffset + cellHeight * height ? -1 : (y - yoffset) / cellHeight;
 	}
 	
 	// move cursor one position in the currently-selected direction, if possible
 	private void advanceCursor() {
 		if (currentDirection == AcrossDownDirection.DOWN && currentRow < height - 1) {
 			++currentRow;
 		} else if (currentDirection == AcrossDownDirection.ACROSS && currentColumn < width - 1) {
 			++currentColumn;
 		} else {
 			throw new RuntimeException("Unhandled currentDirection " + currentDirection);
 		}
 		currentCell = getCell(currentRow, currentColumn);
 		notifyGridChangeListeners();
 	}
 	
 	/**
 	 * Move cursor left, if possible.
 	 */
 	public void cursorLeft() {
 		cursorMove(NsewDirection.WEST);
 	}
 	
 	/**
 	 * Move cursor right, if possible.
 	 */
 	public void cursorRight() {
 		cursorMove(NsewDirection.EAST);
 	}
 	
 	/**
 	 * Move cursor up, if possible.
 	 */
 	public void cursorUp() {
 		cursorMove(NsewDirection.NORTH);
 	}
 	
 	/**
 	 * Move cursor down, if possible.
 	 */
 	public void cursorDown() {
 		cursorMove(NsewDirection.SOUTH);
 	}
 	
 	private void cursorMove(NsewDirection direction) {
 		RowColumnPair rowCol = getNextCursorPositionInDirection(direction);
 		currentRow = rowCol.row;
 		currentColumn = rowCol.column;
 	}
 	
 	private int getCellWidth()	{
 		return cellWidth;
 	}
 	
 	private int getCellHeight()	{
 		return cellHeight;
 	}
 
 	/**
 	 * @param cellWidth the cellWidth to set
 	 */
 	public void setCellWidth(int cellWidth) {
 		this.cellWidth = cellWidth;
 	}
 
 	/**
 	 * @param cellHeight the cellHeight to set
 	 */
 	public void setCellHeight(int cellHeight) {
 		this.cellHeight = cellHeight;
 	}
 
 	public Cell getCurrentCell() {
 		return currentCell;
 	}
 
 	public void setCurrentCell(Cell currentCell) {
 		this.currentCell = currentCell;
 		notifyGridChangeListeners();
 	}
 
 	public int getCurrentRow() {
 		return currentRow;
 	}
 
 	public void setCurrentRow(int currentRow) {
 		this.currentRow = currentRow;
 		notifyGridChangeListeners();
 	}
 
 	public int getCurrentColumn() {
 		return currentColumn;
 	}
 
 	public void setCurrentColumn(int currentColumn) {
 		this.currentColumn = currentColumn;
 		notifyGridChangeListeners();
 	}
 
 	public boolean isMaintainingSymmetry() {
 		return maintainingSymmetry;
 	}
 
 	public void setMaintainingSymmetry(boolean maintainingSymmetry) {
 		this.maintainingSymmetry = maintainingSymmetry;
 		notifyGridChangeListeners();
 	}
 	
 	public boolean isAbleToUndo() {
 		return commandBuffer.haveCommandsToUndo();
 	}
 
 	public boolean isAbleToRedo() {
 		return commandBuffer.haveCommandsToRedo();
 	}
 	
 	public void undo() {
 		commandBuffer.undo();
 		//notifyGridChangeListeners();
 	}
 	
 	public void redo() {
 		commandBuffer.redo();
 		//notifyGridChangeListeners();
 	}
 	
 	/**
 	 * @return the currentDirection
 	 */
 	public AcrossDownDirection getCurrentDirection() {
 		return currentDirection;
 	}
 
 	/**
 	 * @param currentDirection the currentDirection to set
 	 */
 	public void setCurrentDirection(AcrossDownDirection currentDirection) {
 		this.currentDirection = currentDirection;
 	}
 	
 	/**
 	 * Add change listener to collection of listeners, to be notified whenever
 	 * the grid changes state.
 	 * @param changeListener listener to add
 	 */
 	public void addGridChangeListener(GridChangeListener listener) {
 		gridChangeListeners.add(listener);
 	}
 
 	/**
 	 * Add change listener to collection of listeners, to be notified whenever
 	 * the grid substantially (e.g., more than 1 cell/entire grid) changes state.
 	 * @param changeListener listener to add
 	 */
 	public void addFullGridChangeListener(FullGridChangeListener listener) {
 		fullGridChangeListeners.add(listener);
 	}
 
 	/**
 	 * Add change listener to collection of listeners, to be notified whenever
 	 * a grid cell changes state.
 	 * @param changeListener listener to add
 	 */
 	public void addCellChangeListener(CellChangeListener listener) {
 		cellChangeListeners.add(listener);
 	}
 
 	/**
 	 * Add title change listener to collection of listeners, to be notified whenever
 	 * the grid's title changes.
 	 * @param titleChangeListener title listener to add
 	 */
 	public void addTitleChangeListener(GridChangeListener titleChangeListener) {
 		titleChangeListeners.add(titleChangeListener);
 	}
 
 	/**
 	 * Notify all the registered change listeners that this Grid has changed.
 	 */
 	public void notifyGridChangeListeners() {
 		for (final GridChangeListener changeListener : gridChangeListeners) {
 			/*
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					changeListener.handleChange(Grid.this);
 				}
 			});
 			*/
 			changeListener.handleChange(this);
 		}
 	}
 	
 	/**
 	 * Notify all the registered full-grid change listeners that this Grid has substantially changed.
 	 */
 	public void notifyFullGridChangeListeners() {
 		for (final FullGridChangeListener listener : fullGridChangeListeners) {
 			/*
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					changeListener.handleChange(Grid.this);
 				}
 			});
 			*/
 			listener.handleFullGridChange(this);
 		}
 	}
 	
 	/**
 	 * Notify all registered cell change listeners that the specified cell in this grid has changed.
 	 * @param cell cell that has changed
 	 * @param row row of cell
 	 * @param column column of cell
 	 */
 	public void notifyCellChangeListeners(Cell cell, int row, int column) {
 		for (final CellChangeListener listener : cellChangeListeners) {
 			listener.handleChange(this, cell, row, column);
 		}
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * Set new title for grid, and notify any title change listeners that title has changed.
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 		for (GridChangeListener listener : titleChangeListeners)
 			listener.handleChange(this);
 	}
 
 	/**
 	 * @return the author
 	 */
 	public String getAuthor() {
 		return author;
 	}
 
 	/**
 	 * @param author the author to set
 	 */
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	/**
 	 * @return the copyright
 	 */
 	public String getCopyright() {
 		return copyright;
 	}
 
 	/**
 	 * @param copyright the copyright to set
 	 */
 	public void setCopyright(String copyright) {
 		this.copyright = copyright;
 	}
 
 	/**
 	 * @return the notes associated with this grid
 	 */
 	public String getNotes() {
 		return notes;
 	}
 
 	/**
 	 * @param notes the notes to set
 	 */
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 
 	/**
 	 * @return the displayingWordNumbers
 	 */
 	public boolean isDisplayingWordNumbers() {
 		return displayingWordNumbers;
 	}
 
 	/**
 	 * @param displayingWordNumbers the displayingWordNumbers to set
 	 */
 	public void setDisplayingWordNumbers(boolean displayingWordNumbers) {
 		this.displayingWordNumbers = displayingWordNumbers;
 	}
 
 	/**
 	 * Validate Across clues, making sure they still match the letters in the grid, discarding
 	 * and creating new ones as necessary, and returning the resultant list.
 	 * @return validated across clues
 	 */
 	public List<Clue> validateAndGetAcrossClues() {
 		acrossClues = validateClues(getAcrossWords(), acrossClues, AcrossDownDirection.ACROSS);
 		return acrossClues;
 	}
 	
 	@Override
 	public List<Clue> getAcrossClues() {
 		return acrossClues;
 	}
 
 	/**
 	 * Validate Down clues, making sure they still match the letters in the grid, discarding
 	 * and creating new ones as necessary, and returning the resultant list.
 	 * @return validated across clues
 	 */
 	public List<Clue> validateAndGetDownClues() {
 		downClues = validateClues(getDownWords(), downClues, AcrossDownDirection.DOWN);
 		return downClues;
 	}
 	
 	@Override
 	public List<Clue> getDownClues() {
 		return downClues;
 	}
 	
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#setAcrossClues(java.util.List)
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public void setAcrossClues(List<? extends IoClue> clues) {
 		populateIoClues(getAcrossWords(), clues, AcrossDownDirection.ACROSS);
 		acrossClues = (List<Clue>)clues;
 	}
 
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#setDownClues(java.util.List)
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public void setDownClues(List<? extends IoClue> clues) {
 		populateIoClues(getDownWords(), clues, AcrossDownDirection.DOWN);
 		downClues = (List<Clue>)clues;
 	}
 
 	// load clues from list of IoClue that (presumably) came from import, so only has #/direction/text and needs to be fully populated
 	private void populateIoClues(List<GridWord> gridWords, List<? extends IoClue> clues, AcrossDownDirection direction) {
 
 		int size = gridWords.size();
 		if (size != clues.size())
 			throw new RuntimeException("Error: collection size mismatch: " + size + " gridWords vs " + clues.size() + " clues.");
 //		List<Clue> retval = new ArrayList<Clue>(size);
 		for (int i = 0; i < size; ++i) {
 			Clue clue = (Clue)clues.get(i);
 			GridWord gridWord = gridWords.get(i);
 			RowColumnPair gridWordStart = gridWord.getStartOfWord();
 			RowColumnPair gridWordEnd = gridWord.getEndOfWord();
 
 			clue.setStartOfWord(gridWordStart);
 			clue.setEndOfWord(gridWordEnd);
 
 			clue.setGridWord(gridWord.getContents());
 			clue.setWordComplete(gridWord.isComplete());
 			clue.setCells(gridWord.getCells());
 		}
 	}
 	// Validate clues to make sure they are still applicable, replacing/creating new ones as necessary
 	// Simplifying assumptions for now:
 	// 1. Not retaining discarded clues for now.  (This will probably come later, maybe in the form of tracking clues by grid position rather than word #.)
 	// 2. Not implementing any sort of references between clues for now. (This will come later.)
 	private List<Clue>  validateClues(List<GridWord> gridWords, List<Clue> currClues, AcrossDownDirection direction) {
 		List<Clue> retval = new ArrayList<Clue>(gridWords.size());
 
 		int i = 0, j = 0;
 		for (; i < currClues.size() && j < gridWords.size(); ) {
 
 			Clue clue = currClues.get(i);
 			GridWord gridWord = gridWords.get(j);
 			RowColumnPair clueStart = clue.getStartOfWord();
 			RowColumnPair clueEnd = clue.getEndOfWord();
 			RowColumnPair gridWordStart = gridWord.getStartOfWord();
 			RowColumnPair gridWordEnd = gridWord.getEndOfWord();
 			int startCompare = clueStart.compareTo(gridWordStart);
 			if (startCompare < 0) {
 				// we are already past this clue, so discard it (for now)
 				++i;
 				continue;
 			}
 			if (startCompare > 0) {
 				// clue is past this point. Make a new clue for this point
 				clue = new Clue();
 				clue.setDirection(direction);
 				clue.setClueText("");
 				clue.setStartOfWord(gridWordStart);
 				clue.setEndOfWord(gridWordEnd);
 				clue.setGridWord(new String(gridWord.getPattern()));
 				clue.setNumber(gridWord.getNumber());
 				clue.setWordComplete(gridWord.isComplete());
 				clue.setCells(gridWord.getCells());
 				retval.add(clue);
 				++j;
 			} else if (clueEnd.equals(gridWordEnd) && clue.getGridWord().equals(new String(gridWord.getPattern()))){
 				// clue matches grid space
 				clue.setNumber(gridWord.getNumber());
 				retval.add(clue);
 				++i;
 				++j;
 			} else {
 				// discard this old clue and create a new one
 				clue = new Clue();
 				clue.setDirection(direction);
 				clue.setClueText("");
 				clue.setStartOfWord(gridWordStart);
 				clue.setEndOfWord(gridWordEnd);
 				clue.setGridWord(new String(gridWord.getPattern()));
 				clue.setNumber(gridWord.getNumber());
 				clue.setWordComplete(gridWord.isComplete());
 				clue.setCells(gridWord.getCells());
 				retval.add(clue);
 				++i;
 				++j;
 			}
 		}
 		// process any remaining gridWords that did not get handled
 		for (; j < gridWords.size(); ++j) {
 			GridWord gridWord = gridWords.get(j);
 			Clue clue = new Clue();
 			clue.setDirection(direction);
 			clue.setClueText("");
 			clue.setStartOfWord(gridWord.getStartOfWord());
 			clue.setEndOfWord(gridWord.getEndOfWord());
 			clue.setGridWord(new String(gridWord.getPattern()));
 			clue.setNumber(gridWord.getNumber());
 			clue.setWordComplete(gridWord.isComplete());
 			clue.setCells(gridWord.getCells());
 			retval.add(clue);
 		}
 		
 		return retval;
 	}
 	
 	// return whether the specified (row, col) adjoins the current cell (part of same word, no intervening black squares)
 	private boolean adjoinsCurrentCell(int row, int col) {
 		if (row == -1 || col == -1 || currentRow == -1 || currentColumn == -1)
 			return false;
 		if (row == currentRow && col == currentColumn)
 			return true;				// trivial case
 		if (currentDirection == AcrossDownDirection.ACROSS && row == currentRow) {
 			int min = Math.min(col,  currentColumn);
 			int max = Math.max(col,  currentColumn);
 			for (int i = min; i <= max; ++i) {
 				if (getCell(row, i).isBlack())
 					return false;
 			}
 			return true;
 		} else if (currentDirection == AcrossDownDirection.DOWN && col == currentColumn) {
 			int min = Math.min(row,  currentRow);
 			int max = Math.max(row,  currentRow);
 			for (int i = min; i <= max; ++i) {
 				if (getCell(i, col).isBlack())
 					return false;
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see us.stangl.crostex.io.IoGrid#newClue()
 	 */
 	public Clue newClue() {
 		return new Clue();
 	}
 	
 	/**
 	 * @return next cursor position as a (row, column) pair,
 	 * taking into current cursor direction and wrap/skip behavior
 	 * returns current position if it is not possible to automatically move to another cell 
 	 */
 	public RowColumnPair getNextCursorPosition() {
 		if (currentDirection == AcrossDownDirection.ACROSS)
 			return getNextCursorPositionInDirection(NsewDirection.EAST);
 		if (currentDirection == AcrossDownDirection.DOWN)
 			return getNextCursorPositionInDirection(NsewDirection.SOUTH);
 		return new RowColumnPair(currentRow, currentColumn);
 	}
 	
 	private RowColumnPair getNextCursorPositionInDirection(NsewDirection direction) {
 		int row = currentRow;
 		int column = currentColumn;
 		
 		if (direction == NsewDirection.NORTH) {
 			while (true) {
 				// If we can't wrap and we try to wrap, then just return current position
 				if (! wrappingCursor && row == 0)
 					return new RowColumnPair(currentRow, currentColumn);
 				--row;
 				if (row < 0) {
 					row = height - 1;
 					--column;
 					if (column < 0)
 						column = width - 1;
 				}
 				
 				// If we've wrapped all the way back to current cell, or else
 				// found a good next one, either way return it
 				if (isGoodCandidateForNextCursor(row, column))
 					return new RowColumnPair(row, column);
 			}
 		} else if (direction == NsewDirection.SOUTH) {
 			while (true) {
 				// If we can't wrap and we try to wrap, then just return current position
 				if (! wrappingCursor && row == height - 1)
 					return new RowColumnPair(currentRow, currentColumn);
 				++row;
 				if (row >= height) {
 					row = 0;
 					++column;
 					if (column >= width)
 						column = 0;
 				}
 				
 				// If we've wrapped all the way back to current cell, or else
 				// found a good next one, either way return it
 				if (isGoodCandidateForNextCursor(row, column))
 					return new RowColumnPair(row, column);
 			}
 		} else if (direction == NsewDirection.EAST) {
 			while (true) {
 				// If we can't wrap and we try to wrap, then just return current position
 				if (! wrappingCursor && column == width - 1)
 					return new RowColumnPair(currentRow, currentColumn);
 				++column;
 				if (column >= width) {
 					column = 0;
 					++row;
 					if (row >= height)
 						row = 0;
 				}
 				
 				// If we've wrapped all the way back to current cell, or else
 				// found a good next one, either way return it
 				if (isGoodCandidateForNextCursor(row, column))
 					return new RowColumnPair(row, column);
 			}
 		} else if (direction == NsewDirection.WEST) {
 			while (true) {
 				// If we can't wrap and we try to wrap, then just return current position
 				if (! wrappingCursor && column == 0)
 					return new RowColumnPair(currentRow, currentColumn);
 				--column;
 				if (column < 0) {
 					column = width - 1;
 					--row;
 					if (row < 0)
 						row = height - 1;
 				}
 				
 				// If we've wrapped all the way back to current cell, or else
 				// found a good next one, either way return it
 				if (isGoodCandidateForNextCursor(row, column))
 					return new RowColumnPair(row, column);
 			}
 		} else {
 			return new RowColumnPair(row, column);
 		}
 	}
 	
 	// return whether specified (row, column) is a good candidate for the next cursor,
 	// based upon whether it's wrapped all the way around back to current position or
 	// else satisfies the cursor skip behavior
 	private boolean isGoodCandidateForNextCursor(int row, int column) {
 		Cell cell = getCell(row, column);
 		return row == currentRow && column == currentColumn
 			|| cursorSkipBehavior == CursorSkipBehavior.SKIP_NOTHING
 			|| cursorSkipBehavior == CursorSkipBehavior.SKIP_BLACK_CELLS && !cell.isBlack()
 			|| cursorSkipBehavior == CursorSkipBehavior.SKIP_BLACK_AND_FILLED_CELLS
 				&& !cell.isBlack() && cell.isEmpty();
 	}
 
 	// convert # of pixels to typography points (where point is 1/72 of an inch)
 	private float pixelsToPoints(int pixels) {
 		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
 		return (float)((double)pixels * POINTS_PER_INCH / (double)dpi);
 	}
 
 	// scale specified font so that string will fit in specified width, erring on low side
 	private FontMetrics scaleFontToFitWidth(Graphics2D g, Font originalFont, String string, int desiredWidth) {
 		Font font = originalFont;
 		FontMetrics fontMetrics = g.getFontMetrics(font);
 		float hiFactor = pixelsToPoints(desiredWidth + 3);
 		float loFactor = (float)2.0;	// use 2 points as our lower limit
 		// try maximum of 10 iterations to nail it. After that, just return whatever we have
 		for (int i = 0; i < 10; ++i) {
 			float scalingFactor = (float)((loFactor + hiFactor) / 2.0);
 			font = originalFont.deriveFont(scalingFactor);
 			fontMetrics = g.getFontMetrics(font);
 			int actualWidth = fontMetrics.stringWidth(string);
 			if (actualWidth == desiredWidth)
 				return fontMetrics;
 			if (actualWidth > desiredWidth)
 				hiFactor = scalingFactor;
 			else
 				loFactor = scalingFactor;
 		}
 		
 		// err on side of returning low side font
 		return g.getFontMetrics(originalFont.deriveFont(loFactor));
 	}
 	
 	/**
 	 * @return whether the cursor is wrapping from one side of the grid to the other
 	 */
 	public boolean isWrappingCursor() {
 		return wrappingCursor;
 	}
 
 	/**
 	 * @param wrappingCursor the new value of the wrappingCursor flag, governing whether
 	 * the cursor wraps from one side of the grid to the other
 	 */
 	public void setWrappingCursor(boolean wrappingCursor) {
 		this.wrappingCursor = wrappingCursor;
 	}
 
 	/**
 	 * @return the cursorSkipBehavior
 	 */
 	public CursorSkipBehavior getCursorSkipBehavior() {
 		return cursorSkipBehavior;
 	}
 
 	/**
 	 * @param cursorSkipBehavior the cursorSkipBehavior to set
 	 */
 	public void setCursorSkipBehavior(CursorSkipBehavior cursorSkipBehavior) {
 		this.cursorSkipBehavior = cursorSkipBehavior;
 	}
 
 	/**
 	 * @return whether this grid is ready for export
 	 * Currently defined by whether all non-black squares are non-empty
 	 * Ultimately should take into account whether all clues are defined.
 	 */
 	public boolean isReadyToExport() {
 		for (int row = 0; row < height; ++row) {
 			for (int column = 0; column < width; ++column) {
 				Cell cell = getCell(row, column);
 				if (! cell.isBlack() && cell.getContents().isEmpty())
 					return false;
 			}
 		}
 		return true;
 	}
 }
