 //FILE:          Browser.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu, 1/16/11
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
 
 import java.awt.GridBagConstraints;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.GeneralPath;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.gui.HistogramWindow;
 import ij.gui.ImageCanvas;
 import ij.gui.Roi;
 import ij.io.FileInfo;
 import ij.process.FloatProcessor;
 import ij.process.ShortProcessor;
 
 public class Browser extends JFrame implements ClipboardOwner{
 
 	private ImagePlus imp_ = null;
 	private TrajDataset dataset_ = null;
 	TrajsTable trajsTable_;
 	NodesTable nodesTable_;
 	String path_;
 
 	public Browser(ImagePlus imp) {
 		super();
 		
 		setupPath(imp);
 
 		try {
 			loadDataset();
 		} catch (Exception e) {
 			IJ.log("Can't load analysis results from default location.");
 			return;
 		}
 
 		SetupWindow();
 	}
 
 	public Browser(ImagePlus imp, TrajDataset data) {
 		super();
 		
 		setupPath(imp);
 		dataset_ = data;
 		SetupWindow();
 	}
 	
 	public Browser(ImagePlus imp, Vector<SmNode> nodes) {
 		super();
 		
 		setupPath(imp);
 		dataset_ = new TrajDataset();
 		dataset_.setNodes(nodes);
 		dataset_.buildTrajectoriesFromNodes();
 		
 		SetupWindow();
 	}
 
 	private void setupPath(ImagePlus imp) {
 		path_ = null;
 		imp_ = imp;
 		FileInfo fi = imp.getOriginalFileInfo();
 		if (fi != null) {
 			path_ = fi.directory; 
 		} 
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
 				saveDataset(); 
 			}			
 		});
 		
 		item = new JMenuItem("Rebuild");
 		fileMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ev) { 
 				rebuildTrajectories();
 				trajsTable_.setData(dataset_.getTrajectories());
 				trajsTable_.clearSelection();
 				//nodesTable_.setData(null);
 			}			
 		});
 
 		item = new JMenuItem("Delete");
 		editMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				trajsTable_.deleteSelected(); 
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
 
 		item = new JMenuItem("Flow Map");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				constructFlowMap();
 			}
 		});
 
 		item = new JMenuItem("Mobility Map");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				constructMobilityMap();
 			}
 		});
 
 		item = new JMenuItem("PALM");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				constructPalm();
 			}
 		});
 
 		item = new JMenuItem("IFS");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				constructIFS();
 			}
 		});
 		
 		processMenu.addSeparator();
 		
 		item = new JMenuItem("Trajectory Length Histogram");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				showLengthHistogram();
 			}
 		});
 
 		item = new JMenuItem("Residue Histogram");
 		processMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				showResidueHistogram();
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
 				if (imp_ != null) {
 					selectRoi();
 				}
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
 
 		button = new JButton("Export");
 		buttonBox.add(button);
 
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (imp_ != null) {
 					copySelectedTrajectories();
 				}
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
 		trajsTable_ = new TrajsTable(dataset_.getTrajectories());
 		trajsPane.setViewportView(trajsTable_);
 		mainPanel.add(trajsPane, constraints);
 		
 		constraints.gridx = 1;
 		constraints.gridy = 1;
 		constraints.weightx = 0.7;
 		constraints.weighty = 1.0;
 		final JScrollPane nodesPane = new JScrollPane();
 		nodesTable_ = new NodesTable(null);
 		nodesPane.setViewportView(nodesTable_);		
 		mainPanel.add(nodesPane, constraints);
 
 		constraints.gridx = 0;
 		constraints.gridy = 2;
 		constraints.weightx = 0;
 		constraints.weighty = 0;		
 		mainPanel.add(createButtonBox(), constraints);
 		
 		add(mainPanel);
 		
 		if (imp_ != null) {
 			setTitle(imp_.getTitle() + " Trajectories");
 			trajsTable_.SetImp(imp_);
 			nodesTable_.SetImp(imp_);
 		} else {
 			setTitle("Trajectories");
 		}
 	}
 	
 	private void SetupWindow() {
 		Layout();
 
 		trajsTable_.setNodesTable(nodesTable_);
 		
 		imp_.getCanvas().addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2 && imp_ != null) {
 					findMolecule();
 				}
 				
 			}
 		});
 		
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		
 		if (imp_ != null ) {
 			imp_.getWindow().addWindowListener(new WindowAdapter() {
 				@Override
 				public void windowIconified(WindowEvent e) {
 					setVisible(false);
 				}
 				
 				@Override
 				public void windowDeiconified(WindowEvent e) {
 					setVisible(true);
 				}
 				
 				@Override
 				public void windowClosed(WindowEvent e) {
 					dispose();
 				}
 			});
 		}
 	}
 
 	private void selectRoi() {
 		Roi roi = imp_.getRoi();
 		if (roi == null) {
 			return;
 		}
 		trajsTable_.getSelectionModel().setValueIsAdjusting(true);
 		trajsTable_.clearSelection();
 		int firstSel = -1;
 		for (int i = 0; i < dataset_.getTrajectories().size(); i++) {
 			Trajectory t = dataset_.getTrajectories().get(i);
 			for (int j = 0; j< t.size(); j++) {
 				if (roi.contains( (int)t.getX(j), (int)t.getY(j) )) {
 					int l = trajsTable_.convertRowIndexToView(i);
 					trajsTable_.addRowSelectionInterval(l,l);
 					if (firstSel < 0) {
 						firstSel = l;					
 					break;
 					}
 				}
 			}
 		}
 
 		if (firstSel >=0) {
 			Rectangle r = trajsTable_.getCellRect(firstSel, 0, true);
 			trajsTable_.scrollRectToVisible(r);
 		}
 		trajsTable_.getSelectionModel().setValueIsAdjusting(false);
 	}
 	
 	private void copySelectedTrajectories() {
 		//Vector<Trajectory> trajs = dataset_.getTrajectories();
 		StringBuilder buf = new StringBuilder();
 		int [] selected = trajsTable_.getSelectedRows();
 		Trajectory traj;
 		for (int i = 0; i < selected.length; i++) {
 			int index = trajsTable_.convertRowIndexToModel(selected[i]);
 			traj = dataset_.getTrajectories().get(index);
 			for (int j = 0; j < traj.size(); j++) {
 				buf.append(String.format("%10.4f, %10.4f, %10d, %5d%n", traj.getX(j), traj.getY(j), traj.getFrame(j), i));
 			}
 		}
 		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		StringSelection contents = new StringSelection(buf.toString());
 		clipboard.setContents(contents, this);		
 	}
 
 	private void findMolecule() {
 		ImageCanvas canvas = imp_.getCanvas();
 		Point p = canvas.getCursorLoc();
 		int frame = imp_.getSlice();
 		
 		int index = 0;
 		boolean found = false;
 		int lastIndex = dataset_.getTrajectories().size();
 		while (!found && index < lastIndex) {
 			Trajectory t = dataset_.getTrajectories().get(index);
 			if (t.getFrame(0) <= frame && t.getFrame(t.size()-1) >= frame) {
 				int fi = frame - t.getFrame(0);
 				if (fi >= t.size()) { 
 					fi = t.size() - 1;
 				}
 				while (t.getFrame(fi) > frame) {
 					fi --;
 				}
 				if (t.getFrame(fi) == frame) {
 					if ( Math.abs(t.getX(fi) - p.x) < 2.5 && Math.abs(t.getY(fi) - p.y) < 2.5) {
 						found = true;
 					}
 				}
 			}
 			index ++;
 		}
 		if (found) {
 			index = trajsTable_.convertRowIndexToView(index-1);
 			trajsTable_.setRowSelectionInterval(index,index);
 			Rectangle r = trajsTable_.getCellRect(index, 0, true);
 			trajsTable_.scrollRectToVisible(r);
 		}
 	}
 
 	private int[] getSelectedOrAll() {
 		int [] selected = trajsTable_.getSelectedRows();
 		if (selected.length <= 1) {
 			selected = new int[trajsTable_.getRowCount()];
 			for (int i = 0; i < selected.length; i++) {
 				selected[i] = i;
 			}
 		} else {
 			for (int i = 0; i < selected.length; i++) {
 				selected[i] = trajsTable_.convertRowIndexToModel(selected[i]);
 			}
 		}
 		return selected;
 	}
 
 /*	private FloatProcessor gaussianImage(ImageProcessor img) {
 		int width = img.getWidth();
 		int height = img.getHeight();
 		double psdWidth = Prefs.palmPSDWidth_ * Prefs.palmRatio_;
 		FloatProcessor ip = new FloatProcessor(width, height);
 		for (int x = 0; x < width; x ++) {
 			for (int y = 0; y < height; y++) {
 				double v = Math.exp( -((x*2-width) * (x*2-width) + (y*2-height)*(y*2-height))/(8.0*psdWidth*psdWidth) );
 				ip.setf(x, y, (float)v);
 			}
 		}
 
 		return ip;
 	}*/
 
 	public void constructIFS() {
 		Rectangle rect;
 		imp_.killRoi();
 		rect = imp_.getProcessor().getRoi();
 
 		ImageStack is =  new ImageStack(rect.width, rect.height);
 		for (int i = 0; i < imp_.getNSlices(); i++) {
 			is.addSlice(""+i, new FloatProcessor(rect.width, rect.height));
 		}
 		ImagePlus img = new ImagePlus("IFS", is);
 		double psdWidth = 0.85;
 
 		int [] selected = getSelectedOrAll();
 		for ( int i = 0; i < selected.length; i ++) {
 			Trajectory traj = dataset_.getTrajectories().get(selected[i]);
 			for (int j = 0; j < traj.size(); j++ ) {
 				double xs = (traj.getX(j) - rect.x);
 				double ys = (traj.getY(j)- rect.y);
 				//IJ.log(String.format("%5d%6d%6d", rowIndex, x, y));
 				for (int x = Math.max(0, (int)(xs - 3*psdWidth)); x < Math.min(rect.width, (int)(xs + 3*psdWidth)); x ++) {
 					for (int y = Math.max(0, (int)(ys - 3*psdWidth)); y < Math.min(rect.height, (int)(ys + 3*psdWidth)); y++) {
 						double v = Math.exp( -((x-xs) * (x-xs) + (y-ys)*(y-ys))/(2.0*psdWidth*psdWidth) );
 						FloatProcessor ip = (FloatProcessor) is.getProcessor(traj.getFrame(j));
 						ip.setf(x, y, (float)v + ip.getf(x,y));
 					}
 				}
 			} 
 		}
 		img.show();
 		
 	}
 	
 	public void constructPalm() {
 		FloatProcessor ip = new FloatProcessor((int) (imp_.getProcessor().getWidth() * Prefs.palmRatio_) + 1, (int) (imp_.getProcessor().getHeight() * Prefs.palmRatio_) + 1);
 		double psdWidth = Prefs.palmPSDWidth_ * Prefs.palmRatio_;
 		int nPlotted = 0;
 		int nSkipped = 0;
 		int [] selected = getSelectedOrAll();
 		for ( int i = 0; i < selected.length; i ++) {
 			Trajectory traj = dataset_.getTrajectories().get(selected[i]);
 			double xx = traj.getX(0);
 			double yy = traj.getY(0);
 			boolean converge = true;
 			for (int j = 1; j < traj.size(); j++ ) {
 				if (Math.abs(xx / j - traj.getX(j)) > Prefs.palmThreshold_ || Math.abs(yy / j - traj.getY(j)) > Prefs.palmThreshold_ ) {
 					converge = false;
 					break;
 				}
 				xx += traj.getX(j);
 				yy += traj.getY(j);
 			}
 			if (converge) {
 				xx /= traj.size();
 				yy /= traj.size();
 				double xs = xx * Prefs.palmRatio_;
 				double ys = yy * Prefs.palmRatio_;
 
 				for (int x = Math.max(0, (int)(xs - 3*psdWidth)); x < Math.min(ip.getWidth(), (int)(xs + 3*psdWidth)); x ++) {
 					for (int y = Math.max(0, (int)(ys - 3*psdWidth)); y < Math.min(ip.getHeight(), (int)(ys + 3*psdWidth)); y++) {
 						double v = Math.exp( -((x-xs) * (x-xs) + (y-ys)*(y-ys))/(2.0*psdWidth*psdWidth) );
 						ip.setf(x, y, (float)v + ip.getf(x,y));
 					}
 				}
 				nPlotted ++;
 			} else {
 				nSkipped ++;
 			}
 		}
 		ImagePlus img = new ImagePlus("PALM", ip);
 		img.show();
 		IJ.log(String.format("Plotted %d molecules, skipped %d molecules.", nPlotted, nSkipped));
 	}
 
 	public void constructMobilityMap() {
 		Rectangle rect;
 		imp_.killRoi();
 		rect = imp_.getProcessor().getRoi();
 
 		float [][] m = new float[rect.width][rect.height];
 		float [][] n = new float[rect.width][rect.height];
 		int [] selected = getSelectedOrAll();
 		int i,j;
 		for (i =0; i < selected.length; i++) {
 			Trajectory t = dataset_.getTrajectories().get(selected[i]);
 			for (j = 1; j < t.size(); j++) {
 				if ( rect.contains(t.getX(j-1), t.getY(j-1))) {
 					int x = (int) t.getX(j-1) - rect.x + 1;
 					int y = (int) t.getY(j-1) - rect.y + 1;
 					double dx = (t.getX(j) - t.getX(j-1))/(t.getFrame(j)-t.getFrame(j-1));
 					double dy = (t.getY(j) - t.getY(j-1))/(t.getFrame(j)-t.getFrame(j-1));
 					double dr = Math.sqrt(dx*dx+dy*dy);
 					n[x][y] += 1.0f;
 					m[x][y] += dr;
 				}
 			}
 		}
 		
 		for (i = 0; i < rect.width; i ++) {
 			for (j = 0; j < rect.height; j++) {
 				if (n[i][j] > 0) {
 					m[i][j] = m[i][j] / n[i][j];
 				}
 			}
 		}
 		
 		FloatProcessor fp = new FloatProcessor(m);
 		FloatProcessor np = new FloatProcessor(n);
 		
 		new ImagePlus(imp_.getTitle() + " MobilityMap", fp).show();
 		new ImagePlus(imp_.getTitle() + " MobilityCnt", np).show();
 	}
 	
 	public void constructFlowMap() {
 		Rectangle rect;
 		imp_.killRoi();
 		rect = imp_.getProcessor().getRoi();
 
 		float [][] dxs = new float[rect.width][rect.height];
 		float [][] dys = new float[rect.width][rect.height];
 		float [][] n = new float[rect.width][rect.height];
 		int [] selected = getSelectedOrAll();
 		int i,j;
 		for (i =0; i < selected.length; i++) {
 			Trajectory t = dataset_.getTrajectories().get(selected[i]);
 			for (j = 1; j < t.size(); j++) {
 				if ( rect.contains(t.getX(j-1), t.getY(j-1))) {
 					int x = (int) t.getX(j-1) - rect.x + 1;
 					int y = (int) t.getY(j-1) - rect.y + 1;
 					double dx = (t.getX(j) - t.getX(j-1))/(t.getFrame(j)-t.getFrame(j-1));
 					double dy = (t.getY(j) - t.getY(j-1))/(t.getFrame(j)-t.getFrame(j-1));
 					dxs[x][y] += dx;
 					dys[x][y] += dy;
 					n[x][y] += 1.0f;
 				}
 			}
 		}
 		
 		float maxDx = -1.0f, maxDy = -1.0f;
 		for (i = 0; i < rect.width; i ++) {
 			for (j = 0; j < rect.height; j++) {
 				if (n[i][j] > 0) {
 					dxs[i][j] = dxs[i][j] / n[i][j];
 					dys[i][j] = dys[i][j] / n[i][j];
 					if (Math.abs(dxs[i][j]) > maxDx) 
 						maxDx = Math.abs(dxs[i][j]);
 					if (Math.abs(dys[i][j]) > maxDx) 
 						maxDy = Math.abs(dys[i][j]);
 				}
 			}
 		}
 		
 		
 		GeneralPath gp = new GeneralPath();
 		float max = (maxDx > maxDy? maxDx:maxDy) * 2.0f;
 		for (i = 0; i < rect.width; i ++) {
 			for (j = 0; j < rect.height; j++) {
 				if (n[i][j] > 0) {
 					dxs[i][j] = dxs[i][j] / max;
 					dys[i][j] = dys[i][j] / max;
 					gp.moveTo(rect.x + i + 0.5f, rect.y + j + 0.5f);
 					gp.lineTo(rect.x + i + 0.5f + dxs[i][j], rect.y + j + 0.5f + dys[i][j]);					
 				}
 			}
 		}
 		
 		FloatProcessor fp = new FloatProcessor(n);
 		ImagePlus imp = new ImagePlus(imp_.getTitle() + " Flowmap", fp);
 		imp.show();
 		imp.setOverlay(gp, Color.yellow, new BasicStroke(1f));
 		
 	}
 
 	public ImagePlus getImp() {
 		return imp_;
 	}
 
 	public void selectMarked(boolean b) {
 		trajsTable_.clearSelection();
 		for (int i = 0; i < dataset_.getTrajectories().size(); i++) {
 			if (dataset_.getTrajectories().get(i).marked == b) {
 				int row = trajsTable_.convertRowIndexToView(i);
 				trajsTable_.addRowSelectionInterval(row, row);
 			}
 		}
 	}
 
 	public void rebuildTrajectories(){
 		dataset_.buildTrajectoriesFromNodes();
 	}
 
 	public void showLengthHistogram() {
 		int [] selected = getSelectedOrAll();
 		Vector<Trajectory> trajs = dataset_.getTrajectories();
 		short [] d = new short[selected.length];
 		
 		int min = 10000;
 		int max = -1;
 		for (int i = 0; i < selected.length; i++) {
 			Trajectory t = trajs.get(selected[i]);
 			d[i] = (short) (t.getLength());
 			if (d[i] > max) {
 				max = d[i];
 			}
 			if (d[i]< min) {
 				min = d[i];
 			}
 		}
 		ShortProcessor ip = new ShortProcessor(1, d.length, d, null);
 		ImagePlus imp = new ImagePlus("",ip);
		HistogramWindow hw = new HistogramWindow(imp);
		hw.showHistogram(imp, max-min);
 		hw.setVisible(true);
 		imp.close();
 	} 
 
 	public void showResidueHistogram() {
 		int [] selected = getSelectedOrAll();
 		double [] d = new double[dataset_.getNodes().size()];
 		int cnt = 0;
 		for ( int i= 0; i < selected.length; i++) {
 			Iterator<SmNode> itr = dataset_.getTrajectories().get(selected[i]).iterator();
 			while (itr.hasNext()) {
 				d[cnt ++] = itr.next().reserved;
 			}
 		}
 		FloatProcessor ip = new FloatProcessor(1, cnt, Arrays.copyOf(d, cnt));
 		ImagePlus imp = new ImagePlus("", ip);
		HistogramWindow hw = new HistogramWindow(imp);
		hw.showHistogram(imp, Prefs.histogramBins_);
 		hw.setVisible(true);
 		imp.close();		
 	}
 	
 	String defaultSaveFilename() {
 		final String s = path_ + File.separator + imp_.getTitle() + ".dataset";
 		return s;
 	}
 
 	public void saveDataset(String pathname) {
 		try {
 			File file = new File(pathname);
 			dataset_.saveDataset(file);
 		} catch (IOException e) {
 			IJ.showMessage("IOError: Failed to save file.");
 		}
 		
 	}
 
 	public void saveDataset() {
 		saveDataset(defaultSaveFilename());
 	}
 
 	public void loadDataset(String pathname) throws IOException, ClassNotFoundException {
 		File file = new File(pathname);
 		dataset_ = TrajDataset.loadDataset(file);
 	}
 
 	public void loadDataset() throws IOException, ClassNotFoundException {
 		loadDataset(defaultSaveFilename());
 	}
 	
 	@Override
 	public void dispose() {
 		saveDataset();
 		Prefs.savePrefs();
 		super.dispose();
 	}
 
 	@Override
 	public void lostOwnership(Clipboard arg0, Transferable arg1) {
 		// who cares		
 	}
 }
