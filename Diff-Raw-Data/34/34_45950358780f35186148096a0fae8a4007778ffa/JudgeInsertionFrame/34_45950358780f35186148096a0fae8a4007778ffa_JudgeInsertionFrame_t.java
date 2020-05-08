 /* This file is part of TAberystwyth, a debating competition organiser
  * Copyright (C) 2010, Roberto Sarrionandia and Cal Paterson
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package taberystwyth.view;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.*;
 
 import net.miginfocom.swing.MigLayout;
 
 import taberystwyth.controller.JudgeInsertionFrameListener;
 
 public class JudgeInsertionFrame extends JFrame {
 	
 	private static final long serialVersionUID = 1L;
 	
 	/*
 	 * Labels
 	 */
 	private JLabel judgeNameLabel = new JLabel("Name:");
 	private JLabel ratingLabel = new JLabel("Rating:");
 	
 	/*
 	 * Textfields
 	 */
	private JTextField judgeName = new JTextField(20);
 	private JTextField rating = new JTextField(4);
 	
 	/*
 	 * Buttons
 	 */
 	private JButton clear = new JButton("Clear");
 	private JButton save = new JButton("Save");
 	
 	/*
 	 * Singleton boilerplate
 	 */
 	private static JudgeInsertionFrame instance = new JudgeInsertionFrame();
 	
 	public static JudgeInsertionFrame getInstance(){
 		return instance;
 	}
 	
 	private JudgeInsertionFrame(){
 		setLayout(new MigLayout("wrap 2, flowx, fillx", "[left]rel[right]"));
 		setTitle("Insert Judges");
 		
 		/*
 		 * When the window is closed, make it invisible
 		 */
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent evt) {
 				LocationInsertionFrame.getInstance().setVisible(false);
 			}
 		});
 		
 		add(judgeNameLabel);
 		add(judgeName, "grow");
 		
 		add(ratingLabel);
		add(rating, "");
 		
		add(clear, "tag cancel");
		add(save, "tag apply");
 		
 		pack();
 		
 		clear.addActionListener(new JudgeInsertionFrameListener());
 		save.addActionListener(new JudgeInsertionFrameListener());
 		
 	}
 
 	public synchronized JTextField getJudgeName() {
 		return judgeName;
 	}
 
 	public synchronized JTextField getRating() {
 		return rating;
 	}
 
 	public synchronized JButton getClear() {
 		return clear;
 	}
 
 	public synchronized JButton getSave() {
 		return save;
 	}
 
 }
