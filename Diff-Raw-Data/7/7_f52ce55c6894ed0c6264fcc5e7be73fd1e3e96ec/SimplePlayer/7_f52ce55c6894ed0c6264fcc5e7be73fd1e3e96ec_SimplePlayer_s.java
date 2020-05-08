 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl.viz;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import ditl.*;
 
 @SuppressWarnings("serial")
 public abstract class SimplePlayer extends JFrame {
 	
 	private JPanel sidebar;
 	protected ControlsPanel controls;
 	protected JButton openButton;
 	protected Store _store;
 	
 	protected void init(Scene scene, SceneRunner runner, List<JPanel> widgets){
 		
 		runner.addTimeChangeListener(scene);
 		
 		openButton = new JButton(new ImageIcon(SimplePlayer.class.getResource("icons/Open24.gif")));
 		openButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 			    JFileChooser chooser = new JFileChooser();
 			    chooser.setMultiSelectionEnabled(true);
 			    chooser.setFileFilter( new FileNameExtensionFilter("Trace Store files", "jar") );
 			    int returnVal = chooser.showOpenDialog(null);
 			    if(returnVal == JFileChooser.APPROVE_OPTION) {
 			    	load(chooser.getSelectedFiles());
 			    }
 			}
 		});
 		
 		JPanel toolbarPanel = new JPanel();
 		toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS) );
 		toolbarPanel.add(openButton);
 		controls = new ControlsPanel(runner);
 		toolbarPanel.add(controls);
 		
 		JPanel widgetsPanel = new JPanel();
 		widgetsPanel.setLayout(new BoxLayout(widgetsPanel,BoxLayout.PAGE_AXIS));
 		for ( JPanel widget : widgets ){
 			widgetsPanel.add(widget);
 			widgetsPanel.add(Box.createVerticalStrut(5));
 		}
 		
 		sidebar = new JPanel();
 		sidebar.setLayout(new BorderLayout());
 		sidebar.add(widgetsPanel, BorderLayout.NORTH);
 		sidebar.setVisible(false);
 		
 		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, 
         		scene, sidebar);
         sidebar.setMinimumSize(new Dimension(0,0));
         splitPane.setResizeWeight(1.0);
         splitPane.setOneTouchExpandable(true);
         
 		setLayout(new BorderLayout());
 		add(splitPane, BorderLayout.CENTER);
 		
 		add(toolbarPanel, BorderLayout.SOUTH);
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				close();
 			}
 		});
 		
 		pack();
 		setVisible(true);
 	}
 	
 	public void load(File...files){
 		close();
 		try {
 			_store = Store.open(files);
 			loadReaders();
 			enableControls(true);
 		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Failed to load files: '"+files+"'", "Warning", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	public void loadTracesFromClassPath(String[] traceNames, Object[] klasses){
 		close();
 		try {
 			_store = Store.open();
 			for ( Object obj : klasses ){
 				if ( obj instanceof Class<?> )
 					Store.addTraceClass((Class<?>)obj);
 			}
 			for ( String traceName : traceNames )
 				_store.loadTrace(traceName);
 			loadReaders();
 			enableControls(true);
 		} catch (Exception e) {
 			System.err.println(e);
 			JOptionPane.showMessageDialog(this, "Failed to load traces from classpath.", "Warning", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	protected abstract void loadReaders();
 	
 	public void close(){
 		try {
 			if ( _store != null)
 				_store.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected void enableControls(boolean b){
 		sidebar.setVisible(b);
 		sidebar.setPreferredSize(sidebar.getPreferredSize());
 		controls.setReady(b);
 	}
 	
 	public void enableOpenButton(boolean b){
 		openButton.setVisible(b);
 	}
 }
