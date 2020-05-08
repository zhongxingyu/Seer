 
 /*
  *
  *  MainFrame
  *  Copyright (C) 2012 Gaurav Vaidya
  *
  *  This file is part of TaxRef.
  *
  *  TaxRef is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  TaxRef is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with TaxRef.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.ggvaidya.TaxRef.UI;
 
 import com.ggvaidya.TaxRef.*;
 // import com.ggvaidya.TaxRef.Common.*;
 import com.ggvaidya.TaxRef.Model.*;
 import com.ggvaidya.TaxRef.Net.*;
 import java.awt.*;
 import java.awt.datatransfer.*;
 import java.awt.dnd.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 /**
  * MainFrame is the main window for TaxRef. It can open a file containing 
  * biodiversity information and allow the user to edit it. It is (or should
  * be) designed to allow for maximal ease in editing files.
  * 
  * @author Gaurav Vaidya <gaurav@ggvaidya.com>
  */
 public class MainFrame implements TableCellRenderer {
 	/* CONSTANTS OR CONSTANT-LIKE THINGS */
 	/** A basic title -- we'll stick the filename on at the end if necessary. */
 	private String basicTitle = TaxRef.getName() + "/" + TaxRef.getVersion();
 	
 	/* USER INTERFACE VARIABLES */
 	/** The main frame encapsulated herein. */
 	private final JFrame mainFrame;
 	/** The table which displays the names. */
 	private final JTable table = new JTable();
 	/** The operations drop down, which controls the right rightPanel. */
 	private final JComboBox operations = new JComboBox();
 	/** The text area which shows what's happening on the right rightPanel. */
 	private final JTextArea results = new JTextArea("Please choose an operation from the dropdown above.");
 	/** A progress bar which displays memory usage continuously. */
 	private final JProgressBar progressBar = new JProgressBar(0, 100);
 	/** A match information rightPanel. */
 	MatchInformationPanel matchInfoPanel;
 	
 	/* VARIABLES */
 	/** 
 	 * The DarwinCSV which is currently open. Remember that this encapsulates
 	 * a RowIndex, but it can also be used to write the file back out. I'm not
 	 * really sure what the plan here is.
 	 */
 	DarwinCSV currentCSV = null;
 	/** The match currently in progress. */
 	RowIndexMatch currentMatch = null; 
 	/** A blank data model. */
 	TableModel blankDataModel = new AbstractTableModel() {
 		public String getColumnName(int x) { return ""; }
 		public int getColumnCount() { return 6; }
 		public int getRowCount() { return 6;}
 		public Object getValueAt(int row, int col) { return ""; }
 	};
 
 	void lookUpTaxonID(String taxonID) {
 		URI url;
 		
 		try {
 			// We should look up the miITIS_TSN status, but since we don't
 			// have any options there ...
 			url = new URI("http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=" + taxonID);
 		} catch(URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
 		
 		try {
 			Desktop desktop = Desktop.getDesktop();
 			desktop.browse(url);
 		
 		} catch(IOException e) {
 			MessageBox.messageBox(
 				mainFrame, 
 				"Could not open URL '" + url + "'", 
				"The following error occurred while looking up URL '" + url + "': " + e.getMessage(), 
 				MessageBox.ERROR
 			);
 		}
 	}
 
 	void searchName(String nameToMatch) {
 		URI url;
 		
 		try {
 			// We should look up the miITIS_TSN status, but since we don't
 			// have any options there ...
 			url = new URI("http", "www.google.com", "/search", "q=" + nameToMatch);
 			// I think the URI handles the URL encoding?
 			
 		} catch(URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
 		
 		try {
 			Desktop desktop = Desktop.getDesktop();
 			desktop.browse(url);
 		
 		} catch(IOException e) {
 			MessageBox.messageBox(
 				mainFrame, 
 				"Could not open URL '" + url + "'", 
				"The following error occurred while looking up URL '" + url + "': " + e.getMessage(), 
 				MessageBox.ERROR
 			);
 		}
 	}
 	
 	/* CLASSES */
 	private class MainFrameWorker extends SwingWorker<Object, Object> {
 		protected Object input;
 		
 		public MainFrameWorker() {
 			// Turn on indeterminate when initialized.
 			progressBar.setIndeterminate(true);
 		}
 		
 		public MainFrameWorker(Object input) {
 			this();
 			this.input = input;
 		}
 		
 		@Override
 		protected Object doInBackground() throws Exception {
 			// Needs to be overridden.
 			throw new UnsupportedOperationException("Not supported yet.");
 		}
 		
 		@Override
 		protected void done() {
 			// Turn off indeterminate.
 			progressBar.setIndeterminate(false);
 			
 			// Check for exceptions, and display them if necessary.
 			try {
 				get();
 			} catch(Exception e) {
 				MessageBox.messageBox(
 					mainFrame, 
 					"Error during processing", 
 					"The following error occurred during processing: " + e.getMessage(), 
 					MessageBox.ERROR
 				);
 			}
 		}
 	};
 	
 	/**
 	 * Create a new, empty, not-visible TaxRef window. Really just activates 
 	 * the setup frame and setup memory monitor components, then starts things 
 	 * off.
 	 */
 	public MainFrame() {
 		setupMemoryMonitor();
 		
 		// Set up the main frame.
 		mainFrame = new JFrame(basicTitle);
 		mainFrame.setJMenuBar(setupMenuBar());
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Set up the JTable.
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		table.setSelectionBackground(COLOR_SELECTION_BACKGROUND);
 		table.setShowGrid(true);
 		
 		// Add a blank table model so that the component renders properly on
 		// startup.
 		table.setModel(blankDataModel);
 		
 		// Add a list selection listener so we can tell the matchInfoPanel
 		// that a new name was selected.
 		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if(currentCSV == null)
 					return;
 				
 				int row = table.getSelectedRow();
 				int column = table.getSelectedColumn();
 				
 				Object o = table.getModel().getValueAt(row, column);
 				if(Name.class.isAssignableFrom(o.getClass())) {
 					matchInfoPanel.nameSelected(currentCSV.getRowIndex(), (Name) o, row, column);
 				} else {
 					matchInfoPanel.nameSelected(currentCSV.getRowIndex(), null, -1, -1);
 				}
 			}
 		});
 		
 		// Set up the left panel.
 		JPanel leftPanel = new JPanel();
 		
 		matchInfoPanel = new MatchInformationPanel(this);
 		leftPanel.setLayout(new BorderLayout());
 		leftPanel.add(matchInfoPanel, BorderLayout.SOUTH);
 		leftPanel.add(new JScrollPane(table));
 		
 		// Set up the right panel.
 		JPanel rightPanel = new JPanel();
 		rightPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
 		
 		progressBar.setStringPainted(true);
 		
 		rightPanel.setLayout(new BorderLayout());
 		rightPanel.add(operations, BorderLayout.NORTH);
 		rightPanel.add(new JScrollPane(results));
 		rightPanel.add(progressBar, BorderLayout.SOUTH);
 		
 		// Set up an item listener for when the operations bar changes.
 		operations.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if(e.getStateChange() != ItemEvent.SELECTED)
 					return;
 				
 				String actionCmd = (String) e.getItem();
 				String colName = null;
 				
 				if(actionCmd.startsWith("Summarize name identification")) {
 					colName = null;
 				} else if(actionCmd.startsWith("Summarize column '")) {
 					colName = actionCmd.split("'")[1];
 				}
 				
 				// TODO: item state changed.
 				
 				if(currentCSV != null)
 					results.setText("O NO");
 				else
 					results.setText("No file loaded.");
 				
 				results.setCaretPosition(0);
 			}
 		});
 		operations.addItem("No files loaded as yet.");	
 		
 		// Set up a JSplitPane to split the panels up.
 		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, leftPanel, rightPanel);
 		split.setResizeWeight(1);
 		mainFrame.add(split);
 		mainFrame.pack();
 		
 		// Set up a drop target so we can pick up files 
 		mainFrame.setDropTarget(new DropTarget(mainFrame, new DropTargetAdapter() {
 			@Override
 			public void dragEnter(DropTargetDragEvent dtde) {
 				// Accept any drags.
 				dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
 			}
 
 			@Override
 			public void dragOver(DropTargetDragEvent dtde) {
 			}
 
 			@Override
 			public void dropActionChanged(DropTargetDragEvent dtde) {
 			}
 
 			@Override
 			public void dragExit(DropTargetEvent dte) {
 			}
 
 			@Override
 			public void drop(DropTargetDropEvent dtde) {
 				// Accept a drop as long as its File List.
 				
 				if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
 					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
 					
 					Transferable t = dtde.getTransferable();
 					
 					try {
 						java.util.List<File> files = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
 						
 						// If we're given multiple files, pick up only the last file and load that.
 						File f = files.get(files.size() - 1);
 						loadFile(f, DarwinCSV.FILE_CSV_DELIMITED);
 						
 					} catch (UnsupportedFlavorException ex) {
 						dtde.dropComplete(false);
 						
 					} catch (IOException ex) {
 						dtde.dropComplete(false);
 					}
 				}
 			}
 		}));
 	}
 	
 	/**
 	 * Set up the menu bar.
 	 * 
 	 * @return The JMenuBar we set up.
 	 */
 	private JMenuBar setupMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		
 		/* File */
 		JMenu fileMenu = new JMenu("File");
 		menuBar.add(fileMenu);
 		
 		/* File -> Open CSV */
 		JMenuItem miFileOpenCSV = new JMenuItem(new AbstractAction("Open CSV") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				FileDialog fd = new FileDialog(mainFrame, "Open Darwin CSV file ...", FileDialog.LOAD);
 				fd.setVisible(true);
 				File file;
 				if(fd.getDirectory() != null) {
 					file = new File(fd.getDirectory(), fd.getFile());
 				} else if(fd.getFile() != null) {
 					file = new File(fd.getFile());
 				} else {
 					return;
 				}
 				
 				// Clear out old file.
 				loadFile(null);
 				
 				// SwingWorker MAGIC!
 				new MainFrameWorker(file) {
 					@Override
 					protected Object doInBackground() throws Exception {
 						System.err.println("Loading file: " + input);
 						loadFile((File)input, DarwinCSV.FILE_CSV_DELIMITED);
 						
 						return null;
 					}
 				}.execute();
 			}
 		});
 		fileMenu.add(miFileOpenCSV);
 		
 		/* File -> Open CSV without UI */
 		JMenuItem miFileOpenCSVnoUI = new JMenuItem(new AbstractAction("Open CSV without UI") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				FileDialog fd = new FileDialog(mainFrame, "Open Darwin CSV file ...", FileDialog.LOAD);
 				fd.setVisible(true);
 
 				File file;
 				if(fd.getDirectory() != null) {
 					file = new File(fd.getDirectory(), fd.getFile());
 				} else if(fd.getFile() != null) {
 					file = new File(fd.getFile());
 				} else {
 					return;
 				}
 				
 				// Clear out old file
 				loadFile(null);
 				
 				loadFile(file, DarwinCSV.FILE_CSV_DELIMITED);
 			}
 		});
 		fileMenu.add(miFileOpenCSVnoUI);
 		
 		/* File -> Open tab-delimited */
 		JMenuItem miFileOpenTab = new JMenuItem(new AbstractAction("Open tab-delimited") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				FileDialog fd = new FileDialog(mainFrame, "Open Darwin tab-delimited file ...", FileDialog.LOAD);
 				fd.setVisible(true);
 				
 				File file;
 				if(fd.getDirectory() != null) {
 					file = new File(fd.getDirectory(), fd.getFile());
 				} else if(fd.getFile() != null) {
 					file = new File(fd.getFile());
 				} else {
 					return;
 				}
 				
 				// Clear out old file
 				loadFile(null);
 				
 				// SwingWorker MAGIC!
 				new MainFrameWorker(file) {
 					@Override
 					protected Object doInBackground() throws Exception {
 						loadFile((File)input, DarwinCSV.FILE_TAB_DELIMITED);
 						
 						return null;
 					}
 				}.execute();
 			}
 		});
 		fileMenu.add(miFileOpenTab);
 		
 		
 		/* File -> Save CSV */
 		JMenuItem miFileSave = new JMenuItem(new AbstractAction("Save as CSV") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				FileDialog fd = new FileDialog(mainFrame, "Save Darwin CSV file ...", FileDialog.SAVE);
 				fd.setVisible(true);
 				
 				File file;
 				if(fd.getDirectory() != null) {
 					file = new File(fd.getDirectory(), fd.getFile());
 				} else if(fd.getFile() != null) {
 					file = new File(fd.getFile());
 				} else {
 					return;
 				}
 				
 				// SwingWorker MAGIC!
 				new MainFrameWorker(file) {
 					@Override
 					protected Object doInBackground() throws Exception {
 						currentCSV.saveToFile((File)input, DarwinCSV.FILE_CSV_DELIMITED);
 						
 						return null;
 					}
 				}.execute();
 			}
 		});
 		fileMenu.add(miFileSave);
 		
 		/* File -> Exit */
 		JMenuItem miFileExit = new JMenuItem(new AbstractAction("Exit") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mainFrame.setVisible(false);
 				mainFrame.dispose();
 			}
 		});
 		fileMenu.add(miFileExit);
 		
 		/* Match */
 		JMenu matchMenu = new JMenu("Match");
 		menuBar.add(matchMenu);
 		
 		/* Match -> Against CSV */
 		JMenuItem miMatchCSV = new JMenuItem(new AbstractAction("Match against CSV") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				FileDialog fd = new FileDialog(mainFrame, "Open Darwin CSV file for matching ...", FileDialog.LOAD);
 				fd.setVisible(true);
 				
 				if(fd.getFile() == null)
 					return;
 				
 				File file = new File(fd.getFile());
 				if(fd.getDirectory() != null) {
 					file = new File(fd.getDirectory(), fd.getFile());
 				}
 				
 				// Clear out old match against.
 				matchAgainst(null);
 				
 				// SwingWorker MAGIC!
 				new MainFrameWorker(file) {
 					@Override
 					protected Object doInBackground() throws Exception {
 						matchAgainst(new DarwinCSV((File)input, DarwinCSV.FILE_CSV_DELIMITED));
 						
 						return null;
 					}
 				}.execute();
 			}
 		});
 		matchMenu.add(miMatchCSV);
 		
 		/* Match -> Against ITIS */
 		JMenuItem miMatchITIS = new JMenuItem(new AbstractAction("Match against ITIS") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				DarwinCSV csv = DownloadITIS.getIt(mainFrame);
 				matchAgainst(csv);
 				table.repaint();
 			}
 		});
 		matchMenu.add(miMatchITIS);
 		
 		/* TaxonID */
 		JMenu taxonIDMenu = new JMenu("TaxonIDs");
 		menuBar.add(taxonIDMenu);
 		
 		/* TaxonID -> Treat TaxonIDs as ... */
 		JMenu treatTaxonIDsAs = new JMenu("Treat TaxonIDs as ...");
 		taxonIDMenu.add(treatTaxonIDsAs);
 		
 		/* TaxonID -> Treat -> ITIS TSNs */
 		JCheckBoxMenuItem miITIS_TSNs = new JCheckBoxMenuItem(new AbstractAction("ITIS TSNs") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Don't let the user unselect this.
 				((JCheckBoxMenuItem)e.getSource()).setSelected(true);
 			}
 		});
 		miITIS_TSNs.setSelected(true);
 		treatTaxonIDsAs.add(miITIS_TSNs);
 		
 		/* TaxonID -> Create family column */
 		JMenuItem miTaxonID_createFamily = new JMenuItem(new AbstractAction("Create family column") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				/*
 				if(currentCSV == null)
 					return;
 				
 				if(currentMatch == null)
 					return;
 				
 				int col = getJTable().getSelectedColumn();
 				if(col == -1)
 					return;
 				
 				RowIndex rowIndex = currentCSV.getRowIndex();
 				
 				String colName = rowIndex.getColumnName(col);
 				if(rowIndex.hasColumn(colName + "_family")) {
 					// TODO MessageBox
 					return;
 				}
 				
 				if(Name.class.isAssignableFrom(currentCSV.getRowIndex().getColumnClass(col))) {
 					// A name class! Make a new column!
 					currentCSV.getRowIndex().setColumnClass(colName + "_family", String.class);
 					currentCSV.getRowIndex().createNewColumn(colName + "_family", col + 1, colName, new MapOperation() {
 						@Override
 						public Object mapTo(Object value) {
 							return "family";
 						}
 					});
 					
 					// Repaint the table.
 					getJTable().repaint();
 				}
 				*/
 			}
 		});
 		taxonIDMenu.add(miTaxonID_createFamily);
 		
 		/* Help */
 		JMenu helpMenu = new JMenu("Help");
 		menuBar.add(helpMenu);
 		
 		/* Help -> Memory information */
 		JMenuItem miHelpMemory = new JMenuItem(new AbstractAction("Memory information") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.gc();
 				
 				MessageBox.messageBox(mainFrame, "Memory information",
 					"Maximum memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB\n" +
 					"Total memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB\n" +
 					"Used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB\n" +
 					"Free memory: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " MB\n" +
 					"Available memory: " + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB"
 				);
 			}
 		});
 		helpMenu.add(miHelpMemory);
 		
 		return menuBar;
 	}
 	
 	private java.util.Timer memoryTimer;
 	
 	/**
 	 * The Memory Monitor sets up a 5 second Timer which displays the amount
 	 * of remaining memory (compared to the total memory we have).
 	 */
 	private void setupMemoryMonitor() {
 		memoryTimer = new java.util.Timer("Memory monitor", true);
 		
 		memoryTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				// We need to set this off in the Event Thread.
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						// Calculate the memory we have.
 						long value = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
 						long max = Runtime.getRuntime().maxMemory() / (1024 * 1024);
 						int percentage = (int)(((double)value)/max*100);
 						
 						// Set the progress bar.
 						progressBar.setMinimum(0);
 						progressBar.setMaximum(100);
 						progressBar.setValue(percentage);
 						
 						progressBar.setString(value + " MB out of " + max + " MB (" + percentage + "%)");
 					}
 					
 				});
 			}
 		
 		}, new Date(), 5000);	// Every five seconds.
 	}
 	
 	/**
 	 * Set the current open DarwinCSV. You should really, really, really
 	 * setCurrentCSV(null) before you load a new DarwinCSV.
 	 * 
 	 * @param csv The new DarwinCSV object.
 	 */
 	private void setCurrentCSV(DarwinCSV csv) {
 		// Clear the old currentCSV object and matchAgainst object.
 		operations.removeAllItems();
 		currentCSV = null;
 		matchAgainst(null);
 		
 		// Load the new currentCSV object.
 		currentCSV = csv;
 		table.removeAll();
 		table.setDefaultRenderer(Name.class, this);
 		
 		// Set the currentCSV 
 		// TODO: This causes an exception occasionally, because we shouldn't
 		// be calling setModel outside of the Event Queue thread; however, we're
 		// currently in a worker thread, so dipping back into the Event thread 
 		// would just cause more problems. Sorry!
 		if(csv != null) {
 			table.setModel(currentCSV.getRowIndex());
 		} else {
 			table.setModel(blankDataModel);
 		}
 		
 		table.repaint();
 	}
 	
 	/**
 	 * Set the DarwinCSV to match this against.
 	 * 
 	 * @param against The DarwinCSV object to match against.
 	 */
 	private void matchAgainst(DarwinCSV against) {
 		// System.err.println("matchAgainst: " + against);
 		
 		// Reset previous match information.
 		currentMatch = null;
 		table.repaint();
 		
 		// If all we're doing is a reset, we can get out now.
 		if(against == null)
 			return;
 		
 		// long t1 = System.currentTimeMillis();
 		currentMatch = currentCSV.getRowIndex().matchAgainst(against.getRowIndex());
 		table.repaint();
 		matchInfoPanel.matchChanged(currentMatch);
 		// long t2 = System.currentTimeMillis();
 		
 		// System.err.println("matchAgainst finished: " + (t2 - t1) + " ms");
 	}
 	
 	/**
 	 * Helper function for loading a file without type information, just for
 	 * convenience. Right now, this just assumes it's CSV and moves it on.
 	 * 
 	 * @param file The file to load.
 	 */
 	private void loadFile(File file) {
 		// Eventually, this will be some code to figure out what kind of file
 		// it is. But for now ...
 		loadFile(file, DarwinCSV.FILE_CSV_DELIMITED);
 	}
 	
 	/**
 	 * Load a file of a particular type. It's a pretty standard helper function,
 	 * which uses DarwinCSV and setCurrentCSV(...) to make file loading happen
 	 * with messaging and whatnot. We can also reset the display: just call
 	 * loadFile(null).
 	 * 
 	 * @param file The file to load.
 	 * @param type The type of file (see DarwinCSV's constants).
 	 */
 	private void loadFile(File file, short type) {
 		// If the file was reset, reset the display and keep going.
 		if(file == null) {
 			mainFrame.setTitle(basicTitle);
 			setCurrentCSV(null);
 			return;
 		}
 		
 		System.err.println("Not a reset!");
 		
 		// Load up a new DarwinCSV and set current CSV.
 		try {
 			setCurrentCSV(new DarwinCSV(file, type));
 
 		} catch(IOException ex) {
 			MessageBox.messageBox(mainFrame, 
 				"Could not read file '" + file + "'", 
 				"Unable to read file '" + file + "': " + ex
 			);
 		}
 		
 		// Set up the 'operations' variable.
 		operations.addItem("Summarize name identification");
 		for(String column: currentCSV.getRowIndex().getColumnNames()) {
 			operations.addItem("Summarize column '" + column + "'");
 		}
 		
 		// Set the main frame title, based on the filename and the index.
 		mainFrame.setTitle(basicTitle + ": " + file.getName() + " (" + String.format("%,d", currentCSV.getRowIndex().getRowCount()) + " rows)");
 	}
 	
 	/**
 	 * The default table cell renderer. We pass on cell rendering instructions to it.
 	 */
 	private DefaultTableCellRenderer defTableCellRenderer = new DefaultTableCellRenderer();
 	
 	/**
 	 * Renders a table cell in the main JTable. As a TableCellRenderer, we have 
 	 * to implement this method, but we use it to colour different types of
 	 * matches in different ways. Remember that this is run every time a cell
 	 * is displayed on the screen, so it needs to be as fast as can be.
 	 * 
 	 * @param table The table which needs rendering.
 	 * @param value The object which needs rendering. For now, this can only be
 	 *	a Name object, but later on we might colour different types of cells in
 	 *	different ways.
 	 * @param isSelected Is this cell selected, i.e. is the row selected?
 	 * @param hasFocus Is this cell focused, i.e. is this individual cell selected?
 	 * @param row The row coordinate of this cell.
 	 * @param column The column coordinate of this cell.
 	 * @return A component representing this cell.
 	 */
 	@Override
 	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 		// TODO: Check if we can get a saving out of this by just rendering a JLabel/JTextField directly.
 		Component c = defTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 		
 		// Set all backgrounds to white.
         c.setBackground(Color.WHITE);
 		
 		if(value == null) {
 			// Null values look null-ish.
 			c.setBackground(COLOR_NULL);
 			return c;
 			
 		} else if(hasFocus) {
 			// ANY cell with focus should look focussed.
 			c.setBackground(COLOR_FOCUS);	
 			return c;
 			
 		} else if(Name.class.isAssignableFrom(value.getClass())) {
 			// Aha, a Name! Color it special.
 			Name name = (Name) value;
 			int str_length = name.toString().length();
 			
 			if(currentMatch == null) {
 				// No current match? Then just colour blank cells blank,
 				// and unmatched name colours special so people know that
 				// they have been recognized as names.
 				
 				if(str_length == 0) {
 					c.setBackground(COLOR_BLANK_CELL);
 				} else {
 					c.setBackground(COLOR_NAME_UNMATCHED);
 				}
 			} else {
 				// So which RowIndex is the match against?
 				RowIndex against = currentMatch.getAgainst();
 				
 				// System.err.println("Checking against: " + against);
 				
 				if(str_length == 0) {
 					// Mark blank cells as such.
 					c.setBackground(COLOR_BLANK_CELL);
 				} else if(against.hasName(name)) {
 					// Perfect match!
 					c.setBackground(COLOR_NAME_FULL_MATCH);
 				} else if(against.hasName(name.getGenus())) {
 					// Genus-match.
 					c.setBackground(COLOR_NAME_GENUS_MATCH);
 				} else {
 					// No match!
 					c.setBackground(COLOR_NAME_NO_MATCH);
 				}
 			}
 			
 		} else {
 			// Not a name? Note that Strings will NOT make it here: we don't
 			// push Strings through this. So this is really just for later.
 			c.setBackground(COLOR_NULL);
 		}
 		
 		// If the row is selected, make it darker.
 		if(isSelected)
 			c.setBackground(c.getBackground().darker());
 		
 		return c;
 	}
 	
 	/** A blank or non-existent cell. */
 	public static final Color COLOR_NULL = new Color(255, 159, 0);
 	
 	/** A name which could not be matched. */
 	public static final Color COLOR_NAME_NO_MATCH = new Color(226, 6, 44);
 	
 	/** A name which was matched at the genus level but not the species level. */
 	public static final Color COLOR_NAME_GENUS_MATCH = new Color(255, 117, 24);
 	
 	/** A name which was completely and properly matched. */
 	public static final Color COLOR_NAME_FULL_MATCH = new Color(0, 128, 0);
 	
 	/** A cell which has a valid Name, which has a zero-length string. */
 	public static final Color COLOR_BLANK_CELL = Color.GRAY;
 	
 	/** A Name cell which has not matched against anything. */
 	public static final Color COLOR_NAME_UNMATCHED = new Color(137, 207, 230);
 	
 	/** A focused Name cell has its own, distinctive colour, so you know that you
 	 can edit it. Maybe? */
 	public static final Color COLOR_FOCUS = Color.RED;
 
 	/** 
 	 * The colour of non-Name cells which have been selected. Note that selected
 	 * Name cells will currently be in COLOR_NAME_UNMATCHED.darker() or
 	 * COLOR_NAME_*_MATCH.darker(). Also, ALL focused cells are COLOR_FOCUS,
 	 * whether Name or not.
 	 */
 	public static final Color COLOR_SELECTION_BACKGROUND = Color.BLUE;
 
 	/** Return the JFrame which is the main frame of this application. */
 	public JFrame getMainFrame() {
 		return mainFrame;
 	}
 	
 	/** Return the JTable which is at the heart of this application. */
 	public JTable getJTable() {
 		return table;
 	}
 	
 	/**
 	 * Move the currently selected cell up or down by one cell.
 	 * 
 	 * @param direction -1 for previous, +1 for next.
 	 */
 	public void goToRow(int direction) {
 		int row = table.getSelectedRow();
 		int column = table.getSelectedColumn();
 		
 		table.changeSelection(row + direction, column, false, false);
 	}
 
 	public void goToColumn(int direction) {
 		// TODO
 	}
 }
