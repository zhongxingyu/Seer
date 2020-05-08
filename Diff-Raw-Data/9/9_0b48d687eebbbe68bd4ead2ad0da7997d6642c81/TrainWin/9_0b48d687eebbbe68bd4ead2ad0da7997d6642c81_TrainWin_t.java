 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import neuralnetwork.NeuralNetwork;
 import utils.ImageUtils;
 
 @SuppressWarnings("serial")
 public class TrainWin extends JFrame implements WindowListener {
 
 	private DrawPanel drawPanel;
 	private JButton btnSave;
 	private JTextField txtIterLimit;
 	private JTextField txtLearningRate;
 	private JTextField txtHLNeurons;
 	private JButton btnTrain;
 	private JComboBox<Integer> cbDigit;
 	private JLabel lblError;
 	private JLabel lblIters;
 	private JLabel lblSuccess;
 	private JLabel lblMSE;
 
 	private FileWriter trainDataFile;
 	private NeuralNetwork nn;
 	private int[][] trainPatterns;
 	private double[] expectedOutputs;
 	private int N;
 
 	public TrainWin() {
 		setTitle("Handwritten Digit Recognition Training");
 		setSize(new Dimension(600, 380));
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setResizable(false);
 		setLayout(null);
 
 		drawPanel = new DrawPanel();
 		btnSave = new JButton("Save Pattern");
 		btnSave.setBounds(150, 300, 100, 30);
 		btnSave.setFocusPainted(false);
 		btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		btnSave.addActionListener(new SaveListener());
 		cbDigit = new JComboBox<Integer>();
 		for (int i = 0; i < 10; i++)
 			cbDigit.addItem(i);
 		cbDigit.setBounds(60, 300, 80, 30);
 
 		createRightSide();
 		loadWriteFile();
 
 		addWindowListener(this);
 		getContentPane().add(drawPanel);
 		getContentPane().add(btnSave);
 		getContentPane().add(cbDigit);
 		setVisible(true);
 	}
 
 	private void createRightSide() {
 		JLabel lblIterLimit = new JLabel("Iterations Limit");
 		lblIterLimit.setBounds(363, 10, 100, 50);
 		JLabel lblLearningRate = new JLabel("Learning Rate");
 		lblLearningRate.setBounds(373, 40, 100, 50);
 		JLabel lblHLNeurons = new JLabel("Hidden Layer Neurons");
 		lblHLNeurons.setBounds(320, 70, 150, 50);
 		JLabel lblItersTxt = new JLabel("Iterations performed:");
 		lblItersTxt.setBounds(320, 180, 150, 50);
 		JLabel lblSuccessTxt = new JLabel("Success rate:");
 		lblSuccessTxt.setBounds(370, 210, 100, 50);
 		JLabel lblMSETxt = new JLabel("Mean Squared Error:");
 		lblMSETxt.setBounds(327, 240, 150, 50);
 
 		txtIterLimit = new JTextField();
 		txtIterLimit.setBounds(465, 25, 100, 20);
 		txtIterLimit.setCursor(new Cursor(Cursor.TEXT_CURSOR));
 		txtLearningRate = new JTextField();
 		txtLearningRate.setBounds(465, 55, 100, 20);
 		txtLearningRate.setCursor(new Cursor(Cursor.TEXT_CURSOR));
 		txtHLNeurons = new JTextField();
 		txtHLNeurons.setBounds(465, 85, 100, 20);
 		txtHLNeurons.setCursor(new Cursor(Cursor.TEXT_CURSOR));
 
 		btnTrain = new JButton("Train");
 		btnTrain.setBounds(400, 120, 100, 30);
 		btnTrain.setFocusPainted(false);
 		btnTrain.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		btnTrain.addActionListener(new TrainListener());
 		
 		lblError = new JLabel("");
 		lblError.setBounds(320, 150, 300, 30);
 		lblError.setForeground(Color.RED);
 		
 		lblIters = new JLabel("");
 		lblIters.setBounds(465, 180, 100, 50);
 		lblIters.setForeground(Color.BLUE);
 		lblSuccess = new JLabel("");
 		lblSuccess.setBounds(465, 210, 100, 50);
 		lblSuccess.setForeground(Color.BLUE);
 		lblMSE = new JLabel("");
 		lblMSE.setBounds(465, 240, 100, 50);
 		lblMSE.setForeground(Color.BLUE);
 
 		getContentPane().add(lblIterLimit);
 		getContentPane().add(lblLearningRate);
 		getContentPane().add(lblHLNeurons);
 		getContentPane().add(txtIterLimit);
 		getContentPane().add(txtLearningRate);
 		getContentPane().add(txtHLNeurons);
 		getContentPane().add(btnTrain);
 		getContentPane().add(lblError);
 		getContentPane().add(lblItersTxt);
 		getContentPane().add(lblSuccessTxt);
 		getContentPane().add(lblMSETxt);
 		getContentPane().add(lblIters);
 		getContentPane().add(lblSuccess);
 		getContentPane().add(lblMSE);
 	}
 
 	private class SaveListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			try {
 				boolean[][] data = drawPanel.getData();
 				data = ImageUtils.getBits(ImageUtils.getRectangle(data), data);
 				for (int i = 0; i < 10; i++)
 					for (int j = 0; j < 10; j++)
						trainDataFile.write((data[j][i])? "1 " : "0 ");
 				trainDataFile.write(Shared.getDigit((Integer)cbDigit.getSelectedItem()) + "\n");
 				N++;
 				drawPanel.clear();
 			} catch (IOException e1) { e1.printStackTrace(); }
 		}
 	}
 
 	private class TrainListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			new Thread() {
 				public void run() {
 					btnTrain.setEnabled(false);
 					btnTrain.setText("Training");
 					lblIters.setText("");
 					lblSuccess.setText("");
 					lblMSE.setText("");
 					if (!txtIterLimit.getText().matches("[1-9][0-9]*"))
 						lblError.setText("Please enter the number of iterations.");
 					else if (!txtLearningRate.getText().matches("[0-9]*\\.[0-9]+"))
 						lblError.setText("Please enter a valid Learning Rate.");
 					else if (!txtHLNeurons.getText().matches("[1-9][0-9]*"))
 						lblError.setText("Please enter a valid number for HL neurons.");
 					else {
 						double lr = Double.parseDouble(txtLearningRate.getText());
 						int hn = Integer.parseInt(txtHLNeurons.getText());
 						nn = new NeuralNetwork(lr, 100, hn);
 						try {
 							nn.loadWeights("res/nn_weights.txt");
 							saveLastData();
 							loadTrainPatterns();
 							int iters = nn.train(trainPatterns, expectedOutputs, Integer.parseInt(txtIterLimit.getText()));
 							lblIters.setText(iters + "");
 							double[] r = nn.test(trainPatterns, expectedOutputs, false);
 							lblSuccess.setText(String.format("%.2f%%", r[0] * 100));
 							nn.saveWeights("res/nn_weights.txt");
 							lblMSE.setText(String.format("%f", r[1]));
 						} catch (Exception e1) { e1.printStackTrace(); }
 					}
 					btnTrain.setEnabled(true);
 					btnTrain.setText("Train");
 				}
 			}.start();
 		}
 	}
 
 	private void loadTrainPatterns() {
 		try {
 			saveLastData();
 			Scanner in = new Scanner(new File("res/train.txt"));
 			int N = in.nextInt();
 			int size = in.nextInt();
 			trainPatterns = new int[N][size];
 			expectedOutputs = new double[N];
 			for (int i = 0; i < N; i++) {
 				for (int j = 0; j < size; j++)
 					trainPatterns[i][j] = in.nextInt();
 				expectedOutputs[i] = in.nextDouble();
 			}
 			in.close();
 		} catch (FileNotFoundException e) {e.printStackTrace();}
 	}
 
 	private void loadWriteFile() {
 		try {
 			Scanner in = new Scanner(new File("res/train.txt"));
 			try { N = in.nextInt(); }
 			catch (NoSuchElementException e) { N = 0; }
 			in.close();
 			trainDataFile = new FileWriter("res/train.txt",true);
 		}
 		catch(IOException e) { e.printStackTrace(); }
 	}
 	
 	private void saveLastData() {
 		try {
 			trainDataFile.close();
 			File tmpFile = new File("res/train.tmp");
 			File trainFile = new File("res/train.txt");
 			Scanner in = new Scanner(trainFile);
 			FileWriter tmpFileOut = new FileWriter(tmpFile);
 			tmpFileOut.write(N + " 100\n");
 			in.nextInt(); in.nextInt();
 			for (int i = 0; i < N; i++) {
 				for (int j = 0; j < 100; j++)
 					tmpFileOut.write(in.nextInt() + " ");
 				tmpFileOut.write(in.nextDouble() + "\n");
 			}
 			tmpFileOut.close();
 			in.close();
 			tmpFile.renameTo(trainFile);
 			loadWriteFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void windowClosing(WindowEvent e) {
 		saveLastData();
 		try { trainDataFile.close(); }
 		catch (IOException e1) { e1.printStackTrace(); }
 	}
 	
 	public void windowClosed(WindowEvent e) {}
 	public void windowOpened(WindowEvent e) {}
 	public void windowIconified(WindowEvent e) {}
 	public void windowDeiconified(WindowEvent e) {}
 	public void windowActivated(WindowEvent e) {}
 	public void windowDeactivated(WindowEvent e) {}
 
 	public static void main(String[] args) {
 		new TrainWin();
 	}
 }
