 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 /**
  * Panel that contains the right sidebar (settings).
  */
 @SuppressWarnings("serial")
 public class RightPanel extends JPanel {
 
 	private JComboBox<BoardFile> cbPuzzles;
 	private JButton btnSolve;
 	private JRadioButton rbAStar;
 	private JRadioButton rbDFS;
 	private JRadioButton rbBFS;
 	public static boolean solving = false;
 
 	/**
 	 * Creates right sidebar
 	 */
 	public RightPanel() {
 		setPreferredSize(new Dimension(300,630));
 		setBackground(Color.LIGHT_GRAY);
 		setLayout(null);
 
 		initComponents();
 		loadPuzzles();
 	}
 
 	/**
 	 * Initializes combobox, radiobuttons and button
 	 */
 	private void initComponents() {
 		cbPuzzles = new JComboBox<>();
 		rbAStar   = new JRadioButton("A*");
 		rbDFS     = new JRadioButton("DFS");
 		rbBFS 	  = new JRadioButton("BFS");
 		btnSolve  = new JButton("Solve");
 
 		cbPuzzles.setBounds(50, 0, 200,50);
 		rbAStar.setBounds(50, 50, 50, 50);
 		rbBFS.setBounds(120, 50, 75, 50);
 		rbDFS.setBounds(190, 50, 75, 50);
 		btnSolve.setBounds(100, 150, 100, 50);
 
 		cbPuzzles.setToolTipText("Pick a puzzle to solve");
 		cbPuzzles.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		rbAStar.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		rbBFS.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		rbDFS.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		btnSolve.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		rbAStar.setFocusPainted(false);
 		rbBFS.setFocusPainted(false);
 		rbDFS.setFocusPainted(false);
 		btnSolve.setFocusPainted(false);
 
 		ButtonGroup radioGroup = new ButtonGroup();
 		radioGroup.add(rbAStar);
 		radioGroup.add(rbBFS);
 		radioGroup.add(rbDFS);
 
 		cbPuzzles.addActionListener(new ComboBoxListener());
 		btnSolve.addActionListener(new SolveButtonListener());
 
 		add(cbPuzzles);
 		add(rbAStar);
 		add(rbBFS);
 		add(rbDFS);
 		add(btnSolve);
 	}
 
 	public void runSolverThread() {
 		SolverThread t = new SolverThread();
 		t.run();
 	}
 
 	/**
 	 * Loads puzzles from .puzzle files in /puzzles
 	 */
 	private void loadPuzzles() {
 		File puzzlesDir = new File("puzzles/");
 		for (File file : puzzlesDir.listFiles()) {
 			String fileName = file.getName();
 			if (Pattern.matches(".*(\\.puzzle)$", fileName))
 				cbPuzzles.addItem(new BoardFile(file));
 		}
 	}
 
 	private class ComboBoxListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				Shared.board.load((BoardFile)cbPuzzles.getSelectedItem());
 			} catch (FileNotFoundException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 	}
 
 	private class SolveButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
			new SolverThread().start();
 
 		}
 	}
 	private class SolverThread extends Thread {
 
 		public void run() {
 			btnSolve.setEnabled(false);
 			BoardFile bFile= (BoardFile)cbPuzzles.getSelectedItem();
 			Scanner in = null;
 			try {
 				in = new Scanner(bFile.file());
 			} catch (FileNotFoundException e1) {}
 
 			in.next();
 			char[][] blocks = new char[6][6];
 			for (int i = 0; i < 6; i++)
 				for (int j = 0; j < 6; j++)
 					blocks[i][j] = in.next().charAt(0);
 
 			Board initial = new Board(blocks);
 			Solver s;
 			if (rbAStar.isSelected())
 				s = new AStarSolver(initial);
 			else if (rbBFS.isSelected())
 				s = new BFSSolver(initial);
 			else if (rbDFS.isSelected())
 				s = new DFSSolver(initial);
 			else {
 				JOptionPane.showMessageDialog(null,
 						"You should select an algorithm");
 				solving = false;
 				btnSolve.setEnabled(true);
 				return;
 			}
 			if (s.isSolvable())
 				for (Action a : s.solution()) {
 					if (a.getBlock() == 'X')
 						Shared.board.blocks[0].move(100 * a.getMoves());
 					else if (a.getBlock() != '?') {
 						System.out.println(a.getBlock() + " " +  a.getMoves());
 						Shared.board.blocks[a.getBlock() - 96].move(100 * a.getMoves());
 					}
 				}
 			else
 				JOptionPane.showMessageDialog(null,
 						"The board you selected has no solution");
 			btnSolve.setEnabled(true);
 		}
 	}
 
 }
