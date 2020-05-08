 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  * Sudoku program that validates and finds a solution.
  * Completed on 11/21/12.
  * @author Miga
  * @version 1.0
  *
  */
 public class SudokuSolver extends JFrame {
 
 	/** UID */
 	private static final long serialVersionUID = 3883151525928534467L;
 
 	/** Contains SudokuCells */
 	private SudokuCell[][] fields;
 	/** Contains integers representing values in cells. */
 	private int[][] cells;
 
 	/**
 	 * Constructor.
 	 * Set up the components and initialize Sudoku.
 	 */
 	public SudokuSolver() {
 		super("Sudoku Solver");
 		
 		// Used to align the title, Sudoku grid, and button panels vertically
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 		
 		// Title panel
 		JPanel title = new JPanel();
 		title.add(new JLabel(new ImageIcon(getClass().getResource("title.png"))));
 		
 		// Sudoku grid panel
 		JPanel sudokuPanel = new JPanel();
 		sudokuPanel.setLayout(new GridLayout(3, 3, 1, 1));
 
 		// Set up 9 3x3 box panels
 		JPanel[] boxes = new JPanel[9];
 		boxes = prepareBoxPanels(sudokuPanel, boxes);
 		
 		// Set up SudokuCells
 		cells = new int[9][9];
 		fields = new SudokuCell[9][9];
 		preparefields(boxes);
 
 		// Bottom part containing buttons
 		// First row of buttons
 		JPanel bottom = new JPanel();
 		JButton submitButton = new JButton("Submit"); // Submit to validate the Sudoku
 		JButton solveButton = new JButton("Solve"); // Solve the Sudoku
 		JButton eraseButton = new JButton("Erase"); // Clear the cells that are not fixed
 		JButton eraseAllButton = new JButton("Erase All"); // Clear all cells including fixed ones
 		
 		bottom.add(submitButton);
 		bottom.add(solveButton);
 		bottom.add(eraseButton);
 		bottom.add(eraseAllButton);
 		
 		// Second row of buttons
 		JPanel bottom2 = new JPanel();
 		JButton presetButton = new JButton("Preset"); // Set filled cells as preset (not editable)
 		
 		bottom2.add(presetButton);
 		
 		submitButton.addActionListener(new ActionListener(){
  
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (isSudokuSolved())
 					JOptionPane.showMessageDialog(getRootPane(), "<html><center>Congratulations!<br>Sudoku has been Completed!</center></html>", "Sudoku Validation", JOptionPane.INFORMATION_MESSAGE);
 				else
 					JOptionPane.showMessageDialog(getRootPane(), "<html><center>Failed!<br>Sudoku is not complete!</center></html>", "Sudoku Validation", JOptionPane.INFORMATION_MESSAGE);
 			}
 			
 		});
 		
 		solveButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Don't do anything if Sudoku is already full.
 				if (isSudokuFull()) {
 					JOptionPane.showMessageDialog(getRootPane(), "<html><center>There are no cells open to start from.</center></html>", "Solving Sudoku", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				// Validate the current state and make the filled cells fixed before solving
 				if (presetCells()) {
 					long a = System.nanoTime();
 					if (!solve(0, 0))
 						JOptionPane.showMessageDialog(getRootPane(), "<html><center>Unable to solve.</center></html>", "Solving Sudoku", JOptionPane.ERROR_MESSAGE);
 					System.out.println(System.nanoTime() - a);
 				} else // Don't start solving if Sudoku is not valid at the start
 					JOptionPane.showMessageDialog(getRootPane(), "<html><center>This is not a valid Sudoku to start.</center></html>", "Solving Sudoku", JOptionPane.ERROR_MESSAGE);
 			}
 			
 		});
 		
 		presetButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Validate the current state and make the filled cells fixed
 				if (!presetCells())
 					JOptionPane.showMessageDialog(getRootPane(), "<html><center>This is not a valid Sudoku to start.</center></html>", "Sudoku Solver", JOptionPane.ERROR_MESSAGE);
 			}
 			
 		});
 		
 		eraseButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				for (int i = 0; i < 9; i++)
 					for (int j = 0; j < 9; j++) {
 						if (cells[i][j] != 0) {
 							if (fields[i][j].editable) {
 								fields[i][j].setText("");
 								cells[i][j] = 0;
 							}
 						}
 					}
 			}
 			
 		});
 		
 		eraseAllButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				for (int i = 0; i < 9; i++)
 					for (int j = 0; j < 9; j++) {
 						if (cells[i][j] != 0) {
 							if (!fields[i][j].editable) {
 								fields[i][j].editable = true;
 								fields[i][j].setEditable(true);
 								fields[i][j].setForeground(Color.BLACK);
 							}
 							fields[i][j].setText("");
 							cells[i][j] = 0;
 						}
 					}
 			}
 			
 		});
 
 		panel.add(title);
 		panel.add(sudokuPanel);
 		panel.add(bottom);
 		panel.add(bottom2);
 		add(panel);
 		
 		// JFrame property
 		setLayout(new FlowLayout(FlowLayout.CENTER));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pack();
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((d.width / 2 - 175), (d.height / 2 - 275));
 		setResizable(false);
 		setVisible(true);
 	}
 	
 	/**
 	 * Check if the Sudoku puzzle is solved.
 	 * @return true if Sudoku is correct.
 	 */
 	private boolean isSudokuSolved() {
 		for (int i = 0; i < 9; i++) {
 			int[] aRow = new int[9];
 			int[] aCol = new int[9];
 			
 			for (int j = 0; j < 9; j++) {
 				// If this cell is empty, quit because it's not complete
 				if (cells[i][j] == 0)
 					return false;
 				
 				aRow[j] = cells[i][j];
 				aCol[j] = cells[j][i];
 				
 				// Check if the value in this cell is duplicated in 3x3 box
 				if (containedIn3x3Box(i, j, cells[i][j]))
 					return false;
 			}
 			
 			// Check rows and columns
 			if (!isRowColCorrect(aRow, aCol))
 				return false;
 
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Check if specified row and column are correct.
 	 * Used when submitting the puzzle.
 	 * @param aRow 9 numbers in a row.
 	 * @param aCol 9 numbers in a column.
 	 * @return true if this row and column are correct.
 	 */
 	private boolean isRowColCorrect(int[] aRow, int[] aCol) {
 		Arrays.sort(aRow);
 		Arrays.sort(aCol);
 		
 		for (int i = 0; i < 9; i++) {
 			if (aRow[i] != i + 1 && aCol[i] != i + 1) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Check if a value contains in its 3x3 box for a cell.
 	 * @param row current row index.
 	 * @param col current column index.
	 * @return true if this cell is correct or not duplicated in its 3x3 box.
 	 */
 	private boolean containedIn3x3Box(int row, int col, int value) {
 		// Find the top left of its 3x3 box to start validating from
 		int startRow = row / 3 * 3;
 		int startCol = col / 3 * 3;
 		
 		// Check within its 3x3 box except its cell
 		for (int i = startRow; i < startRow + 3; i++)
 			for (int j = startCol; j < startCol + 3; j++) {
 				if (!(i == row && j == col)) {
 					if (cells[i][j] == value){
 						return true;
 					}
 				}
 			}
 
 		return false;
 	}
 	
 	/**
 	 * Check if a value is contained within its row and column.
 	 * Used when solving the puzzle.
 	 * @param row current row index.
 	 * @param col current column index.
 	 * @param value value in this cell.
	 * @return true if this value is not duplicated in its row and column.
 	 */
 	private boolean containedInRowCol(int row, int col, int value) {
 		for (int i = 0; i < 9; i++) {
 			// Don't check the same cell
 			if (i != col)
 				if (cells[row][i] == value)
 					return true;
 			if (i != row)
 				if (cells[i][col] == value)
 					return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Make the filled cells fixed.
 	 * @return true if submitted cells form a valid Sudoku to start solving.
 	 */
 	private boolean presetCells() {
 		for (int i = 0; i < 9; i++)
 			for (int j = 0; j < 9; j++) {
 				if (cells[i][j] != 0)
 					if (!containedIn3x3Box(i, j, cells[i][j]) && !containedInRowCol(i, j, cells[i][j])) {
 						fields[i][j].editable = false;
 						fields[i][j].setEditable(false);
 						fields[i][j].setForeground(new Color(150, 150, 150));
 					} else
 						return false;
 			}
 		
 		return true;
 	}
 	
 	/**
 	 * Solve Sudoku recursively.
 	 * @param row current row index.
 	 * @param col current column index.
 	 * @return false if Sudoku was not solved. true if Sudoku is solved.
 	 */
 	private boolean solve(int row, int col) {
 		// If it has passed through all cells, start quitting
 		if (row == 9)
 			return true;
 		
 		// If this cell is already set(fixed), skip to the next cell
 		if (cells[row][col] != 0) {
 			if (solve(col == 8? (row + 1): row, (col + 1) % 9))
 				return true;
 		} else {
 			// Random numbers 1 - 9
 			Integer[] randoms = generateRandomNumbers();
 			for (int i = 0; i < 9; i++) {
 				
 				// If no duplicates in this row, column, 3x3, assign the value and go to the next
 				if (!containedInRowCol(row, col, randoms[i]) && !containedIn3x3Box(row, col, randoms[i])) {
 					cells[row][col] = randoms[i];
 					fields[row][col].setText(String.valueOf(randoms[i]));
 					
 					// Move to the next cell left-to-right and top-to-bottom
 					if (solve(col == 8? (row + 1) : row, (col + 1) % 9))
 						return true;
 					else { // Initialize the cell when backtracking (case when the value in the next cell was not valid)
 						cells[row][col] = 0;
 						fields[row][col].setText("");
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Check if all cells are filled up.
 	 * @return true if Sudoku is full.
 	 */
 	private boolean isSudokuFull() {
 		for (int i = 0; i < 9; i++)
 			for (int j = 0; j < 9; j++)
 				if (cells[i][j] == 0)
 					return false;
 		
 		return true;
 	}
 	
 	/**
 	 * Generate 9 unique random numbers.
 	 * @return array containing 9 random unique numbers.
 	 */
 	private Integer[] generateRandomNumbers() {
 		ArrayList<Integer> randoms = new ArrayList<Integer>();
 		for (int i = 0; i < 9; i++)
 			randoms.add(i + 1);
 		Collections.shuffle(randoms);
 		
 		return randoms.toArray(new Integer[9]);
 	}
 	
 	/**
 	 * Set up and add 9 3x3 panels to Sudoku panel.
 	 * Use 9 panels to align as 3x3 boxes.
 	 * @param sudokuPanel 
 	 * @param boxes 3x3 box panels to be added on Sudoku panel.
 	 * @return instantiated 3x3 box panels.
 	 */
 	private JPanel[] prepareBoxPanels(JPanel sudokuPanel, JPanel[] boxes) {
 		for (int i = 0; i < 9; i++) {
 			boxes[i] = new JPanel();
 			boxes[i].setLayout(new GridLayout(3, 3, 0, 0));
 			boxes[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
 			sudokuPanel.add(boxes[i]);
 		}
 		
 		return boxes;
 	}
 	
 	/**
 	 * Set up fields(SudokuCell) for input and add them to the panels.
 	 * SudokuCells have to be added to proper panels meaning left-to-right and top-to-bottom order.
 	 * @param boxes
 	 */
 	private void preparefields(JPanel[] boxes) {
 		int index = 0;
 		
 		// Adjust current row
 		for (int i = 0; i < 9; i++) {
 			if (i <= 2)
 				index = 0;
 			else if (i <= 5)
 				index = 3;
 			else
 				index = 6;
 			
 			for (int j = 0; j < 9; j++) {
 				fields[i][j] = new SudokuCell(i, j);
 				boxes[index + (j / 3)].add(fields[i][j]);
 			}
 		}
 	}
 	
 	/**
 	 * A SudokuCell represents a cell for input.
 	 * @author Miga
 	 *
 	 */
 	public class SudokuCell extends JTextField {
 
 		/** UID */
 		private static final long serialVersionUID = 4690751052748480438L;
 		
 		/** Determine if this cell can accept input. */
 		private boolean editable;
 
 		/**
 		 * Constructor
 		 * @param row index for row of this cell.
 		 * @param col index for column of this cell.
 		 */
 		public SudokuCell(final int row, final int col) {
 			super(1);
 			
 			editable = true;
 			
 			setBackground(Color.WHITE);
 			setBorder(BorderFactory.createLineBorder(Color.GRAY));
 			setHorizontalAlignment(CENTER);
 			setPreferredSize(new Dimension(35, 35));
 			setFont(new Font("Lucida Console", Font.BOLD, 28));
 			
 			addFocusListener(new FocusListener(){
 
 				@Override
 				public void focusGained(FocusEvent arg0) {
 					// Change colors of fields located in vertical, horizontal, and 3x3 fields
 					int startRow = row / 3 * 3;
 					int startCol = col / 3 * 3;
 
 					for (int i = 0; i < 9; i++) {
 						// Horizontal
 						fields[i][col].setBackground(new Color(255, 227, 209));
 						// Vertical
 						fields[row][i].setBackground(new Color(255, 227, 209));
 					}
 
 					// 3x3 box
 					for (int i = startRow; i < startRow + 3; i++)
 						for (int j = startCol; j < startCol + 3; j++)
 							fields[i][j].setBackground(new Color(255, 227, 209));
 				}
 
 				@Override
 				public void focusLost(FocusEvent arg0) {
 					// Set the previous color of fields back to white
 					int startRow = row / 3 * 3;
 					int startCol = col / 3 * 3;
 					
 					// Reset focus (set background color back to white)
 					for (int i = 0; i < 9; i++) {
 						// Horizontal
 						fields[i][col].setBackground(Color.WHITE);
 						// Vertical
 						fields[row][i].setBackground(Color.WHITE);
 					}
 					
 					// 3x3 box
 					for (int i = startRow; i < startRow + 3; i++)
 						for (int j = startCol; j < startCol + 3; j++)
 							fields[i][j].setBackground(Color.WHITE);
 				}
 				
 			});
 			
 			addKeyListener(new KeyAdapter(){
 				
 				@Override
 				public void keyPressed(KeyEvent e) {
 					// Only allow numeric input
 					if (editable)
 						if (e.getKeyChar() >= '1' && e.getKeyChar() <= '9') {
 							setEditable(true);
 							setText(""); // Keep it 1 letter
 							cells[row][col] = e.getKeyChar() - 48;
 						} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
 							setEditable(true);
 							setText("0"); // Avoid beep sound
 							cells[row][col] = 0;
 						} else
 							setEditable(false);
 					
 					// Navigation by arrow keys
 					switch (e.getKeyCode()) {
 					case KeyEvent.VK_DOWN:
 						fields[(row + 1) % 9][col].requestFocusInWindow();
 						break;
 					case KeyEvent.VK_RIGHT:
 						fields[row][(col + 1) % 9].requestFocusInWindow();
 						break;
 					case KeyEvent.VK_UP:
 						fields[(row == 0)? 8 : (row - 1)][col].requestFocusInWindow();
 						break;
 					case KeyEvent.VK_LEFT:
 						fields[row][(col == 0)? 8 : (col - 1)].requestFocusInWindow();
 						break;
 					}
 				}
 			});
 		}
 
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new SudokuSolver();
 	}
 
 }
