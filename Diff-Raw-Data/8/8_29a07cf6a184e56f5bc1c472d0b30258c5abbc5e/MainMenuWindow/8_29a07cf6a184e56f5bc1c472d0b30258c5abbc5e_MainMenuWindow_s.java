 package com.jpii.navalbattle.gui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.data.*;
 import com.jpii.navalbattle.game.*;
 
 public class MainMenuWindow {
 	JFrame f;
 
 	public MainMenuWindow() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {}
 		f = new JFrame();
 		f.setTitle("NavalBattle");
 		f.getContentPane().setLayout(null);
 
 		JLabel lblVersion = new JLabel(Constants.NAVALBATTLE_VERSION_TITLE + " (" + Constants.NAVALBATTLE_CODENAME + ")");
 		lblVersion.setBounds(10, 276, 238, 14);
 		f.getContentPane().add(lblVersion);
 
		JLabel lblHawkridge = new JLabel("NAVALBATTLE");
		lblHawkridge.setFont(new Font("Prestige Elite Std", Font.BOLD, 33));
		lblHawkridge.setBounds(101, 11, 248, 51);
		f.getContentPane().add(lblHawkridge);
 
 		JButton btnSingleplayer = new JButton("Singleplayer");
 		btnSingleplayer.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Starting SinglePlayer");
 				new SPOptions();
 				NavalBattle.getDebugWindow().printInfo("Disposing MainMenuWindow");
 				f.dispose();
 			}
 		});
 		btnSingleplayer.setBounds(177, 73, 99, 23);
 		f.getContentPane().add(btnSingleplayer);
 
 		JButton btnHelp = new JButton("Help");
 		btnHelp.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Disposing MainMenuWindow");
 				f.dispose();
 				NavalBattle.getDebugWindow().printInfo("Starting Help");
 			}
 		});
 		btnHelp.setBounds(177, 141, 99, 23);
 		f.getContentPane().add(btnHelp);
 
 		JButton btnRoketGamer = new JButton("RoketGamer");
 		btnRoketGamer.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Disposing MainMenuWindow");
 				f.dispose();
 				NavalBattle.getDebugWindow().printInfo("Starting RoketGamer");
 			}
 		});
 		btnRoketGamer.setBounds(177, 175, 99, 23);
 		f.getContentPane().add(btnRoketGamer);
 
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.close();
 			}
 		});
 		btnQuit.setBounds(177, 209, 99, 23);
 		f.getContentPane().add(btnQuit);
 
 		JButton btnCredits = new JButton("Credits");
 		btnCredits.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printInfo("Disposing MainMenuWindow");
 				f.dispose();
 				NavalBattle.getDebugWindow().printInfo("Starting Credits");
 			}
 		});
 		btnCredits.setBounds(398, 267, 67, 23);
 		f.getContentPane().add(btnCredits);
 		
 		JButton btnMultiplayer = new JButton("Multiplayer");
 		btnCredits.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				NavalBattle.getDebugWindow().printWarning("Multiplayer doesn't work");
 			}
 		});
 		btnMultiplayer.setBounds(177, 107, 99, 23);
 		f.getContentPane().add(btnMultiplayer);
 
 		f.setSize(491,339);
 		f.setVisible(true);
 		f.setLocation(500,300);
 
 		f.addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent we){
 				NavalBattle.close();
 			}
 		});
 		
 		f.setFocusable(true);
 		f.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent k) {	
 				if(k.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					NavalBattle.close();
 				}
 			}
 			@Override
 			public void keyReleased(KeyEvent arg0) { 
 			}
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 	}
 
 	public JFrame getFrame() {
 		return f;
 	}
 }
