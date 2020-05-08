 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import javax.swing.BoxLayout;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class NWA extends JFrame {
 
 	JTextField sequenceAInput;
 	JTextField sequenceBInput;
 	JTextField bonusInput;
 	JTextField penaltyInput;
 	JPanel gridPanel;
 	JButton quit;
 	JButton align;
 	JLabel banner;
 	JLabel paramsLabel;
 	JPanel sequenceInputs;
 	JPanel paramsInputs;
 	private static final long serialVersionUID = 1L;
 	final String allowedLetters = "aAcCgGtT";
 	final String numbers = "0123456789";
 	JPanel panel = new JPanel();
 	GridBagConstraints constraints = new GridBagConstraints();
 	int lengthOfA;
 	int lengthOfB;
 	int bonus = 2;
 	int penalty = 1;
 	String sequenceA;
 	String sequenceB;
 	boolean exists;
 
 	public NWA() {
 		banner = new JLabel("Enter two DNA sequences and press align  ");
 
 		sequenceAInput = new JTextField("", 15);
 		sequenceAInput.addKeyListener(new KeyAdapter() {
 			public void keyTyped(KeyEvent e) {
 				char c = e.getKeyChar();
 				textProcessing(c, e, allowedLetters);
 			}
 		});
 
 		paramsLabel = new JLabel("Choose weights for bonus and penalty");
 
 		sequenceBInput = new JTextField("", 15);
 		sequenceBInput.addKeyListener(new KeyAdapter() {
 			public void keyTyped(KeyEvent e) {
 				char c = e.getKeyChar();
 				textProcessing(c, e, allowedLetters);
 			}
 		});
 
 		bonusInput = new JTextField("2", 5);
 		bonusInput.addKeyListener(new KeyAdapter() {
 			public void keyTyped(KeyEvent e) {
 				char c = e.getKeyChar();
 				textProcessing(c, e, numbers);
 			}
 		});
 
 		penaltyInput = new JTextField("1", 5);
 		penaltyInput.addKeyListener(new KeyAdapter() {
 			public void keyTyped(KeyEvent e) {
 				char c = e.getKeyChar();
 				textProcessing(c, e, numbers);
 			}
 		});
 
 		sequenceInputs = new JPanel();
 		sequenceInputs
 				.setLayout(new BoxLayout(sequenceInputs, BoxLayout.Y_AXIS));
 		sequenceInputs.add(sequenceAInput);
 		sequenceInputs.add(sequenceBInput);
 
 		quit = new JButton("quit");
 		quit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int answer = JOptionPane.showConfirmDialog(null,
 						"Are you sure you want to quit?", "Quit",
 						JOptionPane.YES_NO_OPTION);
 				if (answer == 0) {
 					System.exit(0);
 				}
 			}
 		});
 
 		paramsInputs = new JPanel();
 		paramsInputs.setLayout(new BoxLayout(paramsInputs, BoxLayout.Y_AXIS));
 		paramsInputs.add(bonusInput);
 		paramsInputs.add(penaltyInput);
 
 		align = new JButton("Align");
 		align.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				prepareAlignment();
 			}
 		});
 		panel.setLayout(new GridBagLayout());
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		panel.add(banner, constraints);
 		constraints.gridx = 5;
 		constraints.gridy = 0;
 		panel.add(sequenceInputs, constraints);
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		panel.add(paramsLabel, constraints);
 		constraints.gridx = 5;
 		constraints.gridy = 1;
 		panel.add(paramsInputs, constraints);
 		constraints.gridx = 0;
 		constraints.gridy = 3;
 		panel.add(align, constraints);
 		constraints.gridx = 5;
 		constraints.gridy = 3;
 		panel.add(quit, constraints);
 		this.setContentPane(panel);
 		this.pack();
 		this.setTitle("Needleman-Wunsch Algorithm");
 		this.setResizable(true);
 	}
 
 	/**
 	 * If the character is not in the approved characters list, or a special
 	 * key, it is discarded
 	 * 
 	 * @param c
 	 *            the character typed in to the textField box
 	 * @param e
 	 *            the KeyEvent started by a key being typed
 	 */
 	public void textProcessing(char c, KeyEvent e, String checker) {
 		if (!(contains(c, checker) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
 			e.consume();
 		}
 	}
 
 	/**
 	 * I put this in to its own method just for ease of reading
 	 * 
 	 * @param c
 	 *            character to be checked against allowedLetters
 	 * @return whether c is in allowedLetters
 	 */
 	public boolean contains(char c, String checker) {
 		for (int i = 0; i < checker.length(); i++) {
 			if (checker.charAt(i) == c) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * The function where the actual algorithm takes place
 	 */
 	public void prepareAlignment() {
 		if (exists) {
 			gridPanel.removeAll();
 		}
 
 		System.out.println("A: " + sequenceAInput.getText());
 		System.out.println("B: " + sequenceBInput.getText());
 
 		sequenceA = "  " + sequenceAInput.getText().toUpperCase();
 		sequenceB = "  " + sequenceBInput.getText().toUpperCase();
 
 		lengthOfA = sequenceA.length();
 		lengthOfB = sequenceB.length();
 
 		/**
 		 * Due to the way the matrix is constructed (with letters at the start
 		 * of every row), there is not a reflexive property. This conditional
 		 * simply ensures the longer sequence is recorded as sequence A.
 		 * Matrix M = ( N x M ) 
 		 * where N >= M
 		**/	
 				
 		if (lengthOfB > lengthOfA) {
 			String tmp;
 			int temp;
 			tmp = sequenceB;
 			sequenceB = sequenceA;
 			sequenceA = tmp;
 			temp = lengthOfB;
 			lengthOfB = lengthOfA;
 			lengthOfA = temp;
 
 		}
 
 		bonus = Integer.parseInt(bonusInput.getText());
 		penalty = Integer.parseInt(penaltyInput.getText());
 
 		gridPanel = new JPanel();
 		gridPanel.setLayout(new GridLayout(lengthOfA + 1, lengthOfB + 1, 5, 5));
 
 		int differenceInLength = Math.abs(lengthOfA - lengthOfB);
 		if (differenceInLength > 5) {
 			int answer = JOptionPane.showConfirmDialog(null,
 					"Your strings differ by " + differenceInLength
 							+ ", are you sure you wish to proceed?",
 					"Verification", JOptionPane.YES_NO_OPTION);
 			if (answer == 0) {
 				align();
 
 			} else {
 				;
 			}
 		} else {
 			align();
 		}
 	}
 
 	public void align() {
 
 		int[][] score = new int[lengthOfA][lengthOfB];
 		for (int i = 0; i < lengthOfA; i++) {
 			for (int j = 0; j < lengthOfB; j++) {
 				if (j == 0) {
 					gridPanel.add(new JLabel(sequenceA.substring(i, i + 1)));
 					continue;
 				} else if (i == 0) {
 					gridPanel.add(new JLabel(sequenceB.substring(j, j + 1)));
 					continue;
 				} else if (j == 1) {
 					score[i][j] = 1 - i;
 				} else if (i == 1) {
 					score[i][j] = 1 - j;
 				} else {
 					score[i][j] = Math.max(
 							match(sequenceA.charAt(i), sequenceB.charAt(j),
 									score[i - 1][j - 1]), Math.max(
 									score[i - 1][j] - penalty, score[i][j - 1]
 											- penalty));
 				}
 				gridPanel.add(new JLabel(Integer.toString(score[i][j])));
 			}
 		}
 
 		constraints.gridx = 2;
 		constraints.gridy = 2;
 		panel.add(gridPanel, constraints);
 		exists = true;
 		this.pack();
 
 	}
 
 	public int match(char a, char b, int score) {
		if ((a == b) && (a != ' ')) {
 			return score + bonus;
 		} else {
 			return score - penalty;
 		}
 	}
 
 }
