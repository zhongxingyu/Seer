 /*
  * #%L
  * Kipeto
  * %%
  * Copyright (C) 2010 - 2011 Ecclesia Versicherungsdienst GmbH
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 package de.ecclesia.kipeto.gui;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.net.URL;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 import de.ecclesia.kipeto.KipetoApp;
 
 public class ProgressWindow extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	JLabel mainProgressLabel;
 	JLabel subProgressLabel;
 
 	JProgressBar mainProgress;
 	JProgressBar subProgress;
 
 	/**
 	 * Create the shell.
 	 * 
 	 * @param display
 	 */
 	public ProgressWindow() {
 		super(KipetoApp.TITLE + " " + KipetoApp.VERSION);
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		createContents();
 		pack();
 
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - screenSize.width) / 2, (screenSize.height - screenSize.height) / 2);
 	}
 
 	private void createContents() {
 		URL url = ProgressWindow.class.getResource("icon32x32.png");
 		if (url != null) {
 			setIconImage(new ImageIcon(url).getImage());
 		}
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 		getContentPane().add(panel);
 
 		panel.setLayout(new GridLayout(4, 1, 0, 0));
 		
 		mainProgressLabel = new JLabel();
 		panel.add(mainProgressLabel);
 
 		mainProgress = new JProgressBar();
 		mainProgress.setPreferredSize(new Dimension(530, mainProgress.getPreferredSize().height));
 		panel.add(mainProgress);
 
 		subProgressLabel = new JLabel();
 		panel.add(subProgressLabel);
 
 		subProgress = new JProgressBar();
 		panel.add(subProgress);
 	}
 
 }
