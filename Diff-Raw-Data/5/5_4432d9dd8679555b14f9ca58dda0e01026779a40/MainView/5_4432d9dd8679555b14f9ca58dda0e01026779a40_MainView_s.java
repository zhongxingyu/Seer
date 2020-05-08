 /*
 * Copyright (C) 2013 Mohatu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 package net.mohatu.bloocoin.miner;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import java.awt.BorderLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 import javax.swing.JScrollPane;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.SwingConstants;
 import java.awt.Font;
 
 public class MainView{
 	private static boolean mining = true;
 	private static long counter = 0;
 	private JFrame frmBlcMiner;
 	private static String addr = "";
 	private static String key = "";
 	private static final String url = "bloocoin.zapto.org";
 	private static final int port = 3122;
 	private static JLabel lblStatus;
 	private static JLabel lblTriedAmount;
 	private static JLabel lblSolvedAmount;
 	private static JLabel lblKHsAmount;
 	private static JTable table;
 	private static JButton btnStartMining;
 	private static JLabel lblThreads;
 	private static JLabel lblThreadAmount;
 	private static JButton btnLeft;
 	private static JButton btnRight;
 	public static JScrollPane scrollPane;
 	public static DefaultTableModel solved = new DefaultTableModel(
 			new Object[] { "Solved" }, 0);
 	public static DefaultTableModel transactions = new DefaultTableModel(
 			new Object[] { "To:", "From:","Amount:" }, 0);
 	private static JLabel lblBLC;
 	private static long startTime = System.nanoTime();
 	private JLabel lblTime;
 	private static JLabel lblTimeAmount;
 	private JLabel lblTotalBlc;
 	private static JLabel lblTotalBLC;
 	
	private static final double VERSION = 2.51;
 
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainView window = new MainView();
 					window.frmBlcMiner.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public MainView() {
 		System.out.println("Program start time: " + startTime);
 		versionCheck();
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		try {
 			UIManager.setLookAndFeel(UIManager
 					.getSystemLookAndFeelClassName());
 		} catch (UnsupportedLookAndFeelException e) {
 			// handle exception
 		} catch (ClassNotFoundException e) {
 			// handle exception
 		} catch (InstantiationException e) {
 			// handle exception
 		} catch (IllegalAccessException e) {
 			// handle exception
 		}
 		frmBlcMiner = new JFrame();
 		frmBlcMiner.setResizable(false);
 		frmBlcMiner.setTitle("BLC Client v"+VERSION);
 		frmBlcMiner.setBounds(100, 100, 442, 500);
 		frmBlcMiner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JPanel panel = new JPanel();
 		frmBlcMiner.getContentPane().add(panel, BorderLayout.CENTER);
 		panel.setLayout(null);
 
 		btnStartMining = new JButton("Start");
 		btnStartMining.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		btnStartMining.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				startTime = System.nanoTime();
 				//disable start button
 				btnStartMining.setEnabled(false);
 				btnLeft.setEnabled(false);
 				btnRight.setEnabled(false);
 				//Start mining
 				Thread miner = new Thread(new MinerHandler());
 				Thread khs = new Thread(new KhsClass());
 				miner.start();
 				khs.start();
 				mining = true;
 				updateStatusText("Mining started", Color.black);
 			}
 		});
 		btnStartMining.setBounds(10, 120, 80, 23);
 		panel.add(btnStartMining);
 
 		JButton btnStopMining = new JButton("Stop");
 		btnStopMining.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		btnStopMining.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Stop mining
 				mining = false;
 				//enable start button
 				btnStartMining.setEnabled(true);
 				btnRight.setEnabled(true);
 				btnLeft.setEnabled(true);
 				updateStatusText("Mining stopped", Color.red);
 			}
 		});
 		btnStopMining.setBounds(110, 120, 80, 23);
 		panel.add(btnStopMining);
 
 		JLabel lblTried = new JLabel("Tried:");
 		lblTried.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblTried.setBounds(10, 10, 46, 14);
 		panel.add(lblTried);
 
 		JLabel lblSolved = new JLabel("Solved:");
 		lblSolved.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblSolved.setBounds(10, 30, 46, 14);
 		panel.add(lblSolved);
 
 		JLabel lblKhs = new JLabel("Kh/s:");
 		lblKhs.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblKhs.setBounds(10, 50, 46, 14);
 		panel.add(lblKhs);
 
 		lblTriedAmount = new JLabel("0");
 		lblTriedAmount.setBounds(70, 10, 123, 14);
 		panel.add(lblTriedAmount);
 
 		lblSolvedAmount = new JLabel("0");
 		lblSolvedAmount.setBounds(70, 30, 123, 14);
 		panel.add(lblSolvedAmount);
 
 		lblKHsAmount = new JLabel("0.0");
 		lblKHsAmount.setBounds(70, 50, 123, 14);
 		panel.add(lblKHsAmount);
 
 		table = new JTable(1, 1);
 		table.setModel(solved);
 
 		scrollPane = new JScrollPane(table);
 		scrollPane.setToolTipText("Solved hashes");
 		scrollPane.setBounds(199, 44, 225, 99);
 		panel.add(scrollPane);
 		
 		lblStatus = new JLabel("Status: Loading user data");
 		lblStatus.setBounds(10, 447, 300, 14);
 		panel.add(lblStatus);
 		
 		lblBLC = new JLabel("BLC: 0");
 		lblBLC.setHorizontalAlignment(SwingConstants.TRAILING);
 		lblBLC.setBounds(299, 447, 101, 14);
 		panel.add(lblBLC);
 		
 		JButton btnInfo = new JButton("");
 		btnInfo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
				String creatorAddress = "d40d1749657b1c36d24ebda0642c6b5af028c35cc  ";
 				JTextArea address = new JTextArea("Donate address: \n"+creatorAddress+"\n\n2013 Mohatu.net\nLicenced under the GNU GPLv3 license\nhttp://github.com/mohatu/blc_miner");
 				address.setEditable(false);
 				JOptionPane.showMessageDialog(frmBlcMiner,address
 					    ,"Info",JOptionPane.INFORMATION_MESSAGE);
 			}
 		});
 		btnInfo.setIcon(new ImageIcon(getClass().getClassLoader().getResource("net/mohatu/bloocoin/miner/qm.png")));
 		btnInfo.setBounds(391, 5, 33, 35);
 		panel.add(btnInfo);
 		
 		table = new JTable(1, 1);
 		table.setModel(transactions);
 		
 		scrollPane = new JScrollPane(table);
 		scrollPane.setToolTipText("Transaction history");
 		scrollPane.setBounds(10, 154, 414, 248);
 		panel.add(scrollPane);
 		
 		JButton btnSendCoins = new JButton("Send Coins");
 		btnSendCoins.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		btnSendCoins.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Thread sv = new Thread(new SendView());
 				sv.start();
 			}
 		});
 		btnSendCoins.setBounds(10, 413, 135, 23);
 		panel.add(btnSendCoins);
 		
 		JButton btnNewButton = new JButton("Refresh Transactions");
 		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				getTransactions();
 			}
 		});
 		btnNewButton.setBounds(150, 413, 135, 23);
 		panel.add(btnNewButton);
 		
 		lblThreads = new JLabel("Threads:");
 		lblThreads.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblThreads.setHorizontalAlignment(SwingConstants.TRAILING);
 		lblThreads.setBounds(176, 14, 67, 14);
 		panel.add(lblThreads);
 		
 		btnLeft = new JButton("");
 		btnLeft.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(getThreads()>1){
 					setThreads(getThreads()-1);
 				}
 			}
 		});
 		btnLeft.setIcon(new ImageIcon(getClass().getClassLoader().getResource("net/mohatu/bloocoin/miner/left.png")));
 		btnLeft.setBounds(245, 10, 39, 23);
 		panel.add(btnLeft);
 		
 		btnRight = new JButton("");
 		btnRight.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setThreads(getThreads()+1);
 			}
 		});
 		btnRight.setIcon(new ImageIcon(getClass().getClassLoader().getResource("net/mohatu/bloocoin/miner/right.png")));
 		btnRight.setBounds(340, 10, 39, 23);
 		panel.add(btnRight);
 		
 		lblThreadAmount = new JLabel("0");
 		lblThreadAmount.setHorizontalAlignment(SwingConstants.CENTER);
 		lblThreadAmount.setBounds(290, 14, 40, 14);
 		panel.add(lblThreadAmount);
 		
 		lblTime = new JLabel("Time:");
 		lblTime.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblTime.setBounds(10, 70, 46, 14);
 		panel.add(lblTime);
 		
 		lblTimeAmount = new JLabel("00:00:00");
 		lblTimeAmount.setBounds(70, 70, 123, 14);
 		panel.add(lblTimeAmount);
 		
 		JButton btnFromList = new JButton("From List");
 		btnFromList.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				//Send list to server
 				Object[] options = { "Yes", "No" };
 				int answer = JOptionPane.showOptionDialog(
 						MainView.scrollPane,
 						"This will send ALL solutions to\nthe server, and may take a while.\n\nContinue?","Send All?",
 						JOptionPane.YES_NO_OPTION,
 						JOptionPane.INFORMATION_MESSAGE, null, options,
 						options[1]);
 				if (answer == JOptionPane.YES_OPTION) {
 					Thread slc = new Thread(new SubmitListClass());
 					slc.start();
 				} else {
 					// cancelled
 				}
 			}
 		});
 		btnFromList.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		btnFromList.setBounds(289, 413, 135, 23);
 		panel.add(btnFromList);
 		
 		lblTotalBlc = new JLabel("Total BLC:");
 		lblTotalBlc.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		lblTotalBlc.setBounds(10, 90, 55, 14);
 		panel.add(lblTotalBlc);
 		
 		lblTotalBLC = new JLabel("0");
 		lblTotalBLC.setBounds(70, 90, 123, 14);
 		panel.add(lblTotalBLC);
 		
 		JButton btnRef = new JButton("");
 		btnRef.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Thread refresh = new Thread(new CoinClass());
 				refresh.start();
 			}
 		});
 		btnRef.setIcon(new ImageIcon(getClass().getClassLoader().getResource("net/mohatu/bloocoin/miner/ref.png")));
 		btnRef.setBounds(400, 443, 23, 23);
 		panel.add(btnRef);
 		
 		loadData();
 	}
 	
 	private static void versionCheck() {
 		try {
 			URL versionURL = new URL(
 					"https://raw.github.com/Mohatu/BLC_Miner/master/BLCMiner/version");
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					versionURL.openStream()));
 			double version = Double.parseDouble(in.readLine());
 
 			if (version > VERSION) {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"There is a new version available!\nhttp://www.mohatu.net/files/miner.jar");
 			}
 
 			in.close();
 		} catch (Exception e) {
 		}
 	}
 
 	public static void updateCounter() {
 		counter++;
 	}
 
 	public static void updateKhs(double khs) {
 		lblKHsAmount.setText(Double.toString(khs));
 		lblTriedAmount.setText(Long.toString(counter));
 	}
 	
 	public static void setTime(int hour, int minute, int second){
 		String hourString, minuteString, secondString;
 		hourString = Integer.toString(hour);
 		minuteString = Integer.toString(minute);
 		secondString = Integer.toString(second);
 		
 		if(hour<10){
 			hourString = "0"+hour;
 		}
 		
 		if(minute<10){
 			minuteString = "0"+minute;
 		}
 		if(second<10){
 			secondString = "0"+second;
 		}
 		lblTimeAmount.setText(hourString+":"+minuteString+":"+secondString);
 	}
 
 	public static void updateSolved(String solution) {
 		lblSolvedAmount.setText(Integer.toString(Integer
 				.parseInt(lblSolvedAmount.getText()) + 1));
 		solved.addRow(new Object[] { solution });
 	}
 
 	public static long getCounter() {
 		return counter;
 	}
 
 	public static boolean getStatus() {
 		return mining;
 	}
 	
 	public static void updateStatusText(String status, Color color){
 		lblStatus.setText("Status: " + status);
 		lblStatus.setForeground(color);
 	}
 	
 	public static String getAddr(){
 		return addr;
 	}
 	
 	public static String getKey(){
 		return key;
 	}
 	
 	public static String getURL(){
 		return url;
 	}
 	
 	public static int getPort(){
 		return port;
 	}
 	
 	public static void updateBLC(int blc){
 		lblBLC.setText("BLC: " + Integer.toString(blc));
 	}
 	
 	private static void getCoins(){
 		Thread gc = new Thread(new CoinClass());
 		gc.start();
 	}
 	
 	private static void getTransactions(){
 		clearDFM();
 		Thread gt = new Thread(new TransactionClass());
 		gt.start();
 	}
 	
 	public static void clearDFM(){
 		for (int i = transactions.getRowCount() - 1; i >= 0; i--) {
 	        transactions.removeRow(i);
 	    }	
 	}
 	
 	public static int getThreads(){
 		return Integer.parseInt(lblThreadAmount.getText());
 	}
 	
 	public static void setThreads(int threads){
 		lblThreadAmount.setText(Integer.toString(threads));
 	}
 	
 	public static void addTransaction(String trans){
 		String[] transactionData = trans.split(",");
 		transactionData[1]=transactionData[1].replace(" amount: ", "");
 		transactionData[2]=transactionData[2].replace(" from: ", "");
 		transactionData[2]=transactionData[2].replace("}", "");
 		transactionData[2]=transactionData[2].replace("]", "");
 		transactions.addRow(new Object[] {transactionData[0],transactionData[2],transactionData[1]});
 	}
 	
 	public static long getStartTime(){
 		return startTime;
 	}
 	
 	public static void loadDataPub(){
 		loadData();
 	}
 	
 	public static void setTotalBLC(long tot){
 		lblTotalBLC.setText(Long.toString(tot));
 	}
 	
 	private static void loadData() {
 		try {
 			FileInputStream stream = new FileInputStream(new File(
 					"bloostamp"));
 			FileChannel fc = stream.getChannel();
 			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
 					fc.size());
 			String data = (Charset.defaultCharset().decode(bb).toString());
 			addr = data.split(":")[0];
 			key = data.split(":")[1];
 			stream.close();
 			MainView.updateStatusText("Bloostamp data loaded successfully",Color.black);
 			System.out.println("Bloostamp data loaded.");
 			System.out.println("Getting transactions.");
 			getTransactions();
 			System.out.println("Getting coin count.");
 			getCoins();
 			setThreads((Runtime.getRuntime().availableProcessors()/2)+1);
 		} catch (FileNotFoundException fnfe) {
 			MainView.updateStatusText("Could not find the bloostamp file!",Color.red);
 			System.out.println("Unable to find the bloostamp file");
 			
 			Object[] options = { "Yes", "No" };
 			int answer = JOptionPane.showOptionDialog(
 					MainView.scrollPane,
 					"Bloostamp file not found.\nGenerate a new one?\n(Will exit if No)", "New Registration",
 					JOptionPane.YES_NO_OPTION,
 					JOptionPane.INFORMATION_MESSAGE, null, options,
 					options[1]);
 			if (answer == JOptionPane.YES_OPTION) {
 				Thread rc = new Thread(new RegisterClass());
 				rc.start();
 			} else {
 				System.exit(0);
 			}
 		} catch (IOException ioe) {
 			MainView.updateStatusText("IOException",Color.red);
 			System.out.println("IOException.");
 			ioe.printStackTrace();
 		}
 	}
 }
