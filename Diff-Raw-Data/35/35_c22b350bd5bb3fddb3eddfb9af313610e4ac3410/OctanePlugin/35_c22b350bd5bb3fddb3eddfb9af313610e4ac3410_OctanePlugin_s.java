 //FILE:          OctanePlugin.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu 2/15/08
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
 
 import java.awt.Cursor;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.swing.JFileChooser;
 import javax.swing.UIManager;
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.WindowManager;
 import ij.io.FileInfo;
 import ij.plugin.PlugIn;
 
 
 /**
  * The PlugIn adaptor.
  *
  */
 public class OctanePlugin implements PlugIn{
 
 	ImagePlus imp_;
 
 	protected static HashMap<ImagePlus, Browser> dict_ = new HashMap<ImagePlus,Browser>();
 	
 	
 	/**
 	 * Constructor
 	 */
 	public OctanePlugin() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			Prefs.loadPrefs();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open browser.
 	 *
 	 * @param dataset a prior built dataset
 	 * @throws IOException 
 	 * @throws ClassNotFoundException 
 	 */
 	public void openBrowser(TrajDataset dataset) throws IOException, ClassNotFoundException {
 		Browser browser = new Browser(imp_);
 		browser.setup(dataset);
 		dict_.put(imp_, browser);
 		browser.getWindow().addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosed(WindowEvent e) {
 				dict_.remove(imp_);
 			}
 		});
 	}
 
 	/**
 	 * Analyze current image stack
 	 */
 	public void analyze() {
 		ThresholdDialog dlg = new ThresholdDialog(imp_);
 		if (dlg.openDialog() == true) {
 			Browser browser = new Browser(imp_);
 			browser.setup(dlg.getProcessedNodes());
 			dict_.put(imp_, browser);
 			browser.getWindow().addWindowListener(new WindowAdapter() {
 				@Override
 				public void windowClosed(WindowEvent e) {
 					dict_.remove(imp_);
 				}
 			});
 		} else {
 			dict_.remove(imp_);
 		}
 	}
 	
 	TrajDataset readDataset(File f) throws IOException, ClassNotFoundException {
 		TrajDataset dataset;
 		try {
 			IJ.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 			IJ.showStatus("Loading data ...");
 			dataset = TrajDataset.loadDataset(f);
 		} finally {
 			IJ.showStatus("");
 			IJ.getInstance().setCursor(Cursor.getDefaultCursor());
 		}
 		return dataset;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ij.plugin.PlugIn#run(java.lang.String)
 	 */
 	@Override
 	public void run(String cmd) {
 		if (!IJ.isJava16()) {
 			IJ.showMessage("Octane requires Java version 1.6 or higher. Please upgrade the JVM.");
 			return;
 		}
 		
 		String path;		
 		
 		if (cmd.equals("options")) {
 			PrefDialog.openDialog();
 			return;
 		}
 		imp_ = WindowManager.getCurrentImage();
 		if (imp_ == null || imp_.getStack().getSize() < 2) {
 			IJ.showMessage("This only works on a stack");
 			return;
 		}
 		FileInfo fi = imp_.getOriginalFileInfo();
 		if (fi != null) {
 			path = fi.directory; 
 		} else {
 			IJ.showMessage("Can't find image's disk location. You must save the data under a unique folder.");
 			return;
 		}
 
 		if (dict_.containsKey(imp_)) { // window already open
 			dict_.get(imp_).getWindow().setVisible(true);
 			return;
 		}
 			
 		try {
 			if (cmd.equals("browser")) {
 				dict_.put(imp_, null);
 				analyze();
 				return;
 			} 
 			if (cmd.equals("load")){					
 				if (path != null) {
 					File file = new File(path + File.separator + imp_.getTitle() + ".dataset");
 					if (file.exists()) { 
 						openBrowser(readDataset(file));
 						return;
 					}
 				}
 				IJ.showMessage("You don't seem to have a previously saved " +
 				"analysis at the default location. Please specify another path.");
 				JFileChooser fc = new JFileChooser();
 				if (fc.showOpenDialog(IJ.getApplet()) == JFileChooser.APPROVE_OPTION) {
 					openBrowser(readDataset(fc.getSelectedFile()));
 				}
 				return;
 			} 
 			if (cmd.equals("import")) { 
 				JFileChooser fc = new JFileChooser();
 				if (fc.showOpenDialog(IJ.getApplet()) == JFileChooser.APPROVE_OPTION) {
 					openBrowser(TrajDataset.importDatasetFromText(fc.getSelectedFile()));
 				}
 				return;
 			}				
 		} catch (Exception e) {
			IJ.showMessage("Can't load the file! Is it in the correct format? " + e.getMessage()); 
 		} 				
 	}
 }
