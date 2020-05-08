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
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import java.awt.BorderLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 import javax.swing.JScrollPane;
 import javax.swing.table.DefaultTableModel;
 
 public class MainView {
 	private static boolean mining = true;
 	private static int counter = 0;
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
 	public static DefaultTableModel solved = new DefaultTableModel(
 			new Object[] { "Solved" }, 0);
 	private static JLabel lblBLC;
 
 
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
 		frmBlcMiner.setTitle("BLC Miner");
 		frmBlcMiner.setBounds(100, 100, 442, 200);
 		frmBlcMiner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JPanel panel = new JPanel();
 		frmBlcMiner.getContentPane().add(panel, BorderLayout.CENTER);
 		panel.setLayout(null);
 
 		btnStartMining = new JButton("Start Mining");
 		btnStartMining.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				//disable start button
 				btnStartMining.setEnabled(false);
 				//Start mining
 				Thread miner = new Thread(new MinerHandler());
 				Thread khs = new Thread(new KhsClass());
 				miner.start();
 				khs.start();
 				mining = true;
 				updateStatusText("Mining started", Color.black);
 			}
 		});
 		btnStartMining.setBounds(10, 84, 179, 23);
 		panel.add(btnStartMining);
 
 		JButton btnStopMining = new JButton("Stop Mining");
 		btnStopMining.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Stop mining
 				mining = false;
 				//enable start button
 				btnStartMining.setEnabled(true);
 				updateStatusText("Mining stopped", Color.red);
 			}
 		});
 		btnStopMining.setBounds(10, 118, 179, 23);
 		panel.add(btnStopMining);
 
 		JLabel lblTried = new JLabel("Tried:");
 		lblTried.setBounds(10, 11, 46, 14);
 		panel.add(lblTried);
 
 		JLabel lblSolved = new JLabel("Solved:");
 		lblSolved.setBounds(10, 34, 46, 14);
 		panel.add(lblSolved);
 
 		JLabel lblKhs = new JLabel("Kh/s:");
 		lblKhs.setBounds(10, 59, 46, 14);
 		panel.add(lblKhs);
 
 		lblTriedAmount = new JLabel("0");
 		lblTriedAmount.setBounds(66, 11, 132, 14);
 		panel.add(lblTriedAmount);
 
 		lblSolvedAmount = new JLabel("0");
 		lblSolvedAmount.setBounds(66, 34, 46, 14);
 		panel.add(lblSolvedAmount);
 
 		lblKHsAmount = new JLabel("0.0");
 		lblKHsAmount.setBounds(66, 59, 46, 14);
 		panel.add(lblKHsAmount);
 
 		table = new JTable(1, 1);
 		table.setModel(solved);
 
 		JScrollPane scrollPane = new JScrollPane(table);
 		scrollPane.setToolTipText("Solved hashes");
 		scrollPane.setBounds(199, 11, 225, 131);
 		panel.add(scrollPane);
 		
 		lblStatus = new JLabel("Status: Loading user data");
 		lblStatus.setBounds(10, 152, 349, 14);
 		panel.add(lblStatus);
 		
 		lblBLC = new JLabel("BLC: 0");
 		lblBLC.setBounds(369, 152, 55, 14);
 		panel.add(lblBLC);
 		
 		JButton btnInfo = new JButton("");
 		btnInfo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(frmBlcMiner,
 					    " 2013 Mohatu.net\nLicenced under the GNU GPLv3 license","Info",JOptionPane.INFORMATION_MESSAGE);
 			}
 		});
		btnInfo.setIcon(new ImageIcon(getClass().getClassLoader().getResource("net/mohatu/bloocoin/miner/qm.png")));
 		btnInfo.setBounds(156, 43, 33, 35);
 		panel.add(btnInfo);
 		
 		loadData();
 		getCoins();
 	}
 
 	public static void updateCounter() {
 		counter++;
 	}
 
 	public static void updateKhs(double khs) {
 		lblKHsAmount.setText(Double.toString(khs));
 		lblTriedAmount.setText(Integer.toString(counter));
 	}
 
 	public static void updateSolved(String solution) {
 		lblSolvedAmount.setText(Integer.toString(Integer
 				.parseInt(lblSolvedAmount.getText()) + 1));
 		solved.addRow(new Object[] { solution });
 	}
 
 	public static int getCounter() {
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
 		} catch (FileNotFoundException fnfe) {
 			MainView.updateStatusText("Could not find the bloostamp file!",Color.red);
 			System.out.println("Unable to find the bloostamp file.");
 			fnfe.printStackTrace();
 		} catch (IOException ioe) {
 			MainView.updateStatusText("IOException",Color.red);
 			System.out.println("IOException.");
 			ioe.printStackTrace();
 		} finally {
 			MainView.updateStatusText("Bloostamp data loaded successfully",Color.black);
 			System.out.println("Bloostamp data loaded.");
 		}
 	}
 }
