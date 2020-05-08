 package com.shashank.tictactoe;
 
 import java.awt.*;
 
 import java.awt.event.*;
 
 import javax.swing.*;
 
 public class TicTacToeV1 implements ActionListener {
 
 	/* Instance Variables */
 	static GameState gState = new GameState();
 
 	private JFrame window = new JFrame("Tic-Tac-Toe");
 
 	private JButton button1 = new JButton("");
 
 	private JButton button2 = new JButton("");
 
 	private JButton button3 = new JButton("");
 
 	private JButton button4 = new JButton("");
 
 	private JButton button5 = new JButton("");
 
 	private JButton button6 = new JButton("");
 
 	private JButton button7 = new JButton("");
 
 	private JButton button8 = new JButton("");
 
 	private JButton button9 = new JButton("");
 
 	public TicTacToeV1() {
 
 		/* Create Window */
 
 		window.setSize(300, 300);
 
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		window.setLayout(new GridLayout(3, 3));
 
 		/* Add Buttons To The Window */
 
 		window.add(button1);
 
 		window.add(button2);
 
 		window.add(button3);
 
 		window.add(button4);
 
 		window.add(button5);
 
 		window.add(button6);
 
 		window.add(button7);
 
 		window.add(button8);
 
 		window.add(button9);
 
 		/* Add The Action Listener To The Buttons */
 
 		button1.addActionListener(this);
 
 		button2.addActionListener(this);
 
 		button3.addActionListener(this);
 
 		button4.addActionListener(this);
 
 		button5.addActionListener(this);
 
 		button6.addActionListener(this);
 
 		button7.addActionListener(this);
 
 		button8.addActionListener(this);
 
 		button9.addActionListener(this);
 
 		/* Make The Window Visible */
 
 		window.setVisible(true);
 
 	}
 
 	public void actionPerformed(ActionEvent a) {
 		boolean turn = gState.isX_turn();
 		PlayerType[][] pl = gState.getPl();
 		if (a.getSource() == button1) {
 			if (turn) {
 				button1.setText("X");
 				gState.setX_turn(false);
 				pl[0][0] = new PlayerType("X");
 			} else {
 				button1.setText("O");
 				gState.setX_turn(true);
 				pl[0][0] = new PlayerType("O");
 			}
 			gState.setPl(pl);
 
 		} else if (a.getSource() == button2) {
 
 			if (turn) {
 				button2.setText("X");
 				gState.setX_turn(false);
 				pl[0][1] = new PlayerType("X");
 			} else {
 				button2.setText("O");
 				gState.setX_turn(true);
 				pl[0][1] = new PlayerType("O");
 			}
 			gState.setPl(pl);
 
 		} else if (a.getSource() == button3) {
 
 			if (turn) {
 				button3.setText("X");
 				gState.setX_turn(false);
 				pl[0][2] = new PlayerType("X");
 			} else {
 				button3.setText("O");
 				gState.setX_turn(true);
 				pl[0][2] = new PlayerType("O");
 			}
 			gState.setPl(pl);
 
 		} else if (a.getSource() == button4) {
 
 			if (turn) {
 				button4.setText("X");
 				gState.setX_turn(false);
 				pl[1][0] = new PlayerType("X");
 			} else {
 				button4.setText("O");
 				gState.setX_turn(true);
 				pl[1][0] = new PlayerType("O");
 			}
 			gState.setPl(pl);
 
 		} else if (a.getSource() == button5) {
 
 			if (turn) {
 				button5.setText("X");
 				gState.setX_turn(false);
 				pl[1][1] = new PlayerType("X");
 			} else {
 				button5.setText("O");
 				gState.setX_turn(true);
				pl[1][1] = new PlayerType("O");
 			}
 			gState.setPl(pl);
 
 		} else if (a.getSource() == button6) {
 
 			if (turn) {
 				button6.setText("X");
 				gState.setX_turn(false);
 				pl[1][2] = new PlayerType("X");
 			} else {
 				button6.setText("O");
 				gState.setX_turn(true);
 				pl[1][2] = new PlayerType("O");
 			}
 			gState.setPl(pl);
 
 		} else if (a.getSource() == button7) {
 			if (turn) {
 				button7.setText("X");
 				gState.setX_turn(false);
 				pl[2][0] = new PlayerType("X");
 			} else {
 				button7.setText("O");
 				gState.setX_turn(true);
 				pl[2][0] = new PlayerType("O");
 			}
 			gState.setPl(pl);
 
 		} else if (a.getSource() == button8) {
 
 			if (turn) {
 				button8.setText("X");
 				gState.setX_turn(false);
 				pl[2][1] = new PlayerType("X");
 			} else {
 				button8.setText("O");
 				gState.setX_turn(true);
				pl[2][1] = new PlayerType("O");
 			}
 			gState.setPl(pl);
 		} else if (a.getSource() == button9) {
 
 			if (turn) {
 				button9.setText("X");
 				gState.setX_turn(false);
 				pl[2][2] = new PlayerType("X");
 			} else {
 				button9.setText("O");
 				gState.setX_turn(true);
				pl[2][2] = new PlayerType("O");
 			}
 			gState.setPl(pl);
 
 		}
 		gState.isWin();
 	}
 
 	public static void main(String[] args) {
 		gState.initialize();
 		new TicTacToeV1();
 
 	}
 
 }
