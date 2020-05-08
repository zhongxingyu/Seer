 //FILE:          BrowserWindow.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu, 4/14/11
 //
 // LICENSE:      This file is distributed under the BSD license.
 //               License text is included with the source distribution.
 //
 //               This file is distributed in the hope that it will be useful,
 //               but WITHOUT ANY WARRANTY; without even the implied warranty
 //               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 //
 //               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 //               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES./**
 //
 
 package edu.uchc.octane;
 
 import ij.IJ;
 import ij.ImageJ;
 import ij.ImagePlus;
 import ij.gui.GenericDialog;
 import ij.gui.Plot;
 
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import java.awt.GridBagConstraints;
 import java.io.IOException;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import edu.uchc.octane.Browser.IFSType;
 
 /**
  * The browser window.
  */
 public class BrowserWindow extends JFrame {
 	
 	//private String path_;
 	
 	Browser browser_;
 	TrajsTable trajsTable_;
 	NodesTable nodesTable_;
 
 	/**
 	 * Instantiates a new browser window.
 	 *
 	 * @param b the browser controller
 	 */
 	public BrowserWindow(Browser b) {
 		super();
 		if (IJ.isLinux()) setBackground(ImageJ.backgroundColor);
 		browser_ = b;
 		SetupWindow();
 	}
 
 	private JMenuBar createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F);
 		JMenu editMenu = new JMenu("Edit");
 		editMenu.setMnemonic(KeyEvent.VK_E);
 		JMenu viewMenu = new JMenu("View");
 		editMenu.setMnemonic(KeyEvent.VK_V);
 		JMenu processMenu = new JMenu("Process");
 		menuBar.add(fileMenu);
 		menuBar.add(editMenu);
 		menuBar.add(viewMenu);
 		menuBar.add(processMenu);
 
 		JMenuItem item; 
 		JCheckBoxMenuItem cbItem;
 
 		item = new JMenuItem("Save");
 		fileMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				browser_.saveDataset(); 
 			}			
 		});
 		
 		item = new JMenuItem("Rebuild");
 		fileMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
				Thread thread = new Thread() {
					public void run() {
						browser_.rebuildTrajectories();
						if (isDisplayable()) {
							setVisible(true);
						}
					}
				};
				thread.start();
 			}			
 		});
 		
 		fileMenu.addSeparator();
 		
 		item = new JMenuItem("Export All Nodes");
 		fileMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				try {
 					JFileChooser fc = new JFileChooser();
 					if (fc.showSaveDialog(browser_.getWindow()) == JFileChooser.APPROVE_OPTION) {
 						browser_.exportNodes(fc.getSelectedFile());
 					}
 				} catch (IOException e) {
 					IJ.showMessage("Can't save file! " + e.getMessage()); 
 				}
 			}			
 		});		
 
 		item = new JMenuItem("Export Selected Trajectories");
 		fileMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				try {
 					JFileChooser fc = new JFileChooser();
 					if (fc.showSaveDialog(browser_.getWindow()) == JFileChooser.APPROVE_OPTION) {
 						browser_.exportTrajectories(fc.getSelectedFile());
 					}
 				} catch (IOException e) {
 					IJ.showMessage("Can't save file! " + e.getMessage()); 
 				}
 			}			
 		});		
 
 		item = new JMenuItem("Delete");
 		editMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				browser_.deleteSelectedTrajectories();
 				trajsTable_.clearSelection();
 				trajsTable_.tableDataChanged();
 			}
 		});
 		
 		item = new JMenuItem("Hide Unmarked");
 		viewMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				trajsTable_.hideUnmarked();
 			}
 		});
 		
 		item = new JMenuItem("Show All");
 		viewMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				trajsTable_.showAll(); 
 			}
 		});
 		
 		viewMenu.addSeparator();
 
 		cbItem = new JCheckBoxMenuItem("Show Overlay", Prefs.showOverlay_);
 		viewMenu.add(cbItem);
 		cbItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
 				Prefs.showOverlay_ = cb.getState();
 			}
 		});
 
 		viewMenu.addSeparator();
 		item = new JMenuItem("Intensity Transients");
 		viewMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				int [] selected = getSelectedTrajectories();
 				Trajectory t = browser_.getData().getTrajectoryByIndex(selected[0]);
 				float [] x = new float[t.size()];
 				float [] y = new float[t.size()];
 				for (int i = 0; i < t.size(); i++) {
 					x[i] = t.get(i).frame;
 					y[i] = t.get(i).height;
 				}
 				Plot plotWin = new Plot("Transient", "Frame", "Intensity", x, y);
 				plotWin.show();
 			}
 		});
 		
 		item = new JMenuItem("Flow Map");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				browser_.constructFlowMap();
 			}
 		});
 
 		item = new JMenuItem("Mobility Map");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				browser_.constructMobilityMap();
 			}
 		});
 
 		item = new JMenuItem("PALM");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				browser_.constructPalm();
 			}
 		});
 
 		JMenu ifsMenu = new JMenu("IFS");
 		processMenu.add(ifsMenu);
 		item = new JMenuItem("Gaussian Spot");
 		ifsMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				browser_.constructIFS(IFSType.GaussianSpot);
 			}
 		});
 		item = new JMenuItem("Line Overlay");
 		ifsMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				browser_.constructIFS(IFSType.LineOverlay);
 			}
 		});
 		item = new JMenuItem("Square Overlay");
 		ifsMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				browser_.constructIFS(IFSType.SquareOverlay);
 			}
 		});
 		
 		processMenu.addSeparator();
 		
 		item = new JMenuItem("Trajectory Length Histogram");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				browser_.showLengthHistogram();
 			}
 		});
 
 		item = new JMenuItem("Residue Histogram");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				browser_.showResidueHistogram();
 			}
 		});
 
 		item = new JMenuItem("Displacement Histogram");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				int stepsize = 1;
 				GenericDialog gd = new GenericDialog("Step Size Input");
 				gd.addNumericField("Step Size: ", stepsize, 0);
 				gd.showDialog();
 				if (gd.wasCanceled())
 					return;
 				stepsize = (int) gd.getNextNumber();
 				browser_.showDisplacementHistogram(stepsize);
 			}
 		});
 
 		item = new JMenuItem("MSD Plot");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				browser_.showMSD();
 			}
 		});
 
 		return menuBar;
 	}
 	
 	private JPanel createButtonBox() {
 		final JPanel buttonBox = new JPanel(); // button box
 		JButton button;
 
 		buttonBox.setLayout(new BoxLayout(buttonBox,BoxLayout.LINE_AXIS));
 		buttonBox.setBorder(BorderFactory.createEmptyBorder(0,10,5,5));
 		buttonBox.add(Box.createHorizontalGlue());
 		button = new JButton("SelectRoi");
 		buttonBox.add(button);
 
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				trajsTable_.getSelectionModel().setValueIsAdjusting(true);
 				browser_.selectTrajectoriesWithinRoi();
 				trajsTable_.getSelectionModel().setValueIsAdjusting(false);
 			}
 		});
 		
 		buttonBox.add(Box.createRigidArea(new Dimension(10,0)));
 
 		button = new JButton("Mark");
 		buttonBox.add(button);
 		
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				trajsTable_.reverseMarkOfSelected();
 			}
 		});
 
 		buttonBox.add(Box.createRigidArea(new Dimension(10,0)));
 
 		button = new JButton("CopySelected");
 		buttonBox.add(button);
 
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				browser_.copySelectedTrajectories();
 			}
 		});
 
 		return buttonBox;
 	}
 	
 	
 	private void Layout(){
 		
 		setBounds(100, 100, 660, 495);
 		
 		setJMenuBar(createMenuBar());
 		
 		JPanel mainPanel = new JPanel();
 			
 		final GridBagLayout layout = new GridBagLayout();
 		mainPanel.setLayout(layout);
 		
 		GridBagConstraints constraints;
 		constraints = new GridBagConstraints();
 		constraints.anchor = GridBagConstraints.WEST;
 		constraints.gridx = 0; 
 		constraints.gridy = 0;
 		constraints.weightx =0;
 		constraints.weighty = 0;
 		mainPanel.add(new JLabel("List of Traces:"), constraints);
 
 		constraints.gridx = 1; 
 		constraints.gridy = 0;
 		constraints.weightx =0;
 		constraints.weighty = 0;
 		mainPanel.add(new JLabel("Current Trace:"), constraints);
 				
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.insets = new Insets(5,5,5,5);		
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.weightx = 0.3;
 		constraints.weighty = 1.0;
 		final JScrollPane trajsPane = new JScrollPane();
 		trajsTable_ = new TrajsTable(browser_.getData());
 		trajsTable_.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2 ) {
 					browser_.animate();
 				}
 			}
 		});
 		trajsTable_.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (e.getValueIsAdjusting())
 					return;
 				browser_.drawOverlay();
 				browser_.getImp().killRoi();
 				if (nodesTable_ != null) {
 					populateNodesTable();
 				}
 			}
 		});
 		trajsPane.setViewportView(trajsTable_);
 		mainPanel.add(trajsPane, constraints);
 		
 		constraints.gridx = 1;
 		constraints.gridy = 1;
 		constraints.weightx = 0.7;
 		constraints.weighty = 1.0;
 		final JScrollPane nodesPane = new JScrollPane();
 		nodesTable_ = new NodesTable(null);
 		nodesTable_.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (!e.getValueIsAdjusting())
 					browser_.drawBox();
 			}
 		} );
 		nodesPane.setViewportView(nodesTable_);		
 		mainPanel.add(nodesPane, constraints);
 
 		constraints.gridx = 0;
 		constraints.gridy = 2;
 		constraints.weightx = 0;
 		constraints.weighty = 0;		
 		mainPanel.add(createButtonBox(), constraints);
 		
 		add(mainPanel);
 		
 		ImagePlus imp = browser_.getImp();
 		if (imp != null) {
 			setTitle(imp.getTitle() + " Trajectories");
 //			trajsTable_.SetImp(imp_);
 //			nodesTable_.SetImp(imp_);
 		} else {
 			setTitle("Trajectories");
 		}
 	}
 	
 	private void SetupWindow() {
 		Layout();
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		ImagePlus imp = browser_.getImp();
 		if (imp != null ) {
 			imp.getWindow().addWindowListener(new WindowAdapter() {
				boolean isVisible;
 				@Override
 				public void windowIconified(WindowEvent e) {
					isVisible = isVisible();
 					setVisible(false);
 				}
 				
 				@Override
 				public void windowDeiconified(WindowEvent e) {
					setVisible(isVisible);
 				}
 				
 				@Override
 				public void windowClosed(WindowEvent e) {
 					dispose();
 				}
 			});
 		}
 	}
 	
 	protected void populateNodesTable() {
 		if (trajsTable_.getSelectedRowCount() > 0) {
 			int index = getSelectedTrajectoryIndex();
 			nodesTable_.setData(browser_.getData().getTrajectoryByIndex(index));
 		} else {
 			nodesTable_.setData(null);
 		}
 		if (trajsTable_.getSelectedRowCount() == 1) { 
 			nodesTable_.setRowSelectionInterval(0,0);
 		}
 	}
 
 	/**
 	 * Returns the index of currently selected trajectory.
 	 *
 	 * @return the index
 	 */
 	public int getSelectedTrajectoryIndex() {
 		int row = trajsTable_.getSelectedRow();
 		if (row >= 0) {
 			return trajsTable_.convertRowIndexToModel(row);
 		} else {
 			return -1;
 		}
 	}
 	
 	/**
 	 * Returns the indices of all selected trajectories.
 	 *
 	 * @return array of indices
 	 */
 	public int[] getSelectedTrajectories() {
 		int [] selected = trajsTable_.getSelectedRows();
 		for (int i = 0; i < selected.length; i++) {
 			selected[i] = trajsTable_.convertRowIndexToModel(selected[i]);
 		}
 		return selected;				
 	}
 	
 	/**
 	 * Returns the indices of multiple selected trajectories or all trajectories.
 	 * If none or one trajectory is selected, the indices of all trajectories are returned.
 	 *
 	 * @return the indices
 	 */
 	public int[] getSelectedTrajectoriesOrAll() {
 		if (trajsTable_.getSelectedRowCount() <= 1) {
 			int [] selected = new int[trajsTable_.getRowCount()];
 			for (int i = 0; i < selected.length; i++) {
 				selected[i] = i;
 			}
 			return selected;
 		} else {
 			return getSelectedTrajectories();
 		}		
 	}
 	
 	/**
 	 * Select a trajectory in the trajectory table by its index.
 	 *
 	 * @param index the index
 	 */
 	public void selectTrajectoryByIndex(int index) {
 		selectTrajectoryAndNodeByIndex(index, 0);
 	}
 	
 	public void selectTrajectoryAndNodeByIndex(int trajIndex, int nodeIndex) {
 		if (trajIndex < 0) {
 			trajsTable_.clearSelection();
 			return;
 		}
 		nodesTable_.getSelectionModel().setValueIsAdjusting(true);
 		int row = trajsTable_.convertRowIndexToView(trajIndex);
 		trajsTable_.setRowSelectionInterval(row,row);
 		Rectangle r = trajsTable_.getCellRect(row, 0, true);
 		trajsTable_.scrollRectToVisible(r);
 		nodesTable_.setRowSelectionInterval(nodeIndex,nodeIndex);
 		nodesTable_.getSelectionModel().setValueIsAdjusting(true);
 	}
 	
 	/**
 	 * Adds a trajectories to the current selection in the trajectory table.
 	 *
 	 * @param index the index
 	 */
 	public void addTrajectoriesToSelection(int index) {
 		int row = trajsTable_.convertRowIndexToView(index);
 		trajsTable_.addRowSelectionInterval(row,row);
 	}
 
 	/**
 	 * Returns the current selected molecule node (position) in node table.
 	 *
 	 * @return the current node
 	 */
 	public SmNode getCurrentNode() {
 		int row = nodesTable_.getSelectedRow();
 		if (row >=0 && nodesTable_.getData() != null) {
 			int index = nodesTable_.convertRowIndexToModel(row);
 			return nodesTable_.getData().get(index);
 		} else {
 			return null;
 		}				
 	}
 
 	/**
 	 * Update window to reflect new data.
 	 */
 	public void updateNewData() {
 		trajsTable_.setData(browser_.getData());
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.Window#dispose()
 	 */
 	@Override
 	public void dispose() {
 		browser_.saveDataset();
 		Prefs.savePrefs();
 		super.dispose();
 	}
 
 }
