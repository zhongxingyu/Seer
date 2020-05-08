 /*
  * The geworkbench-core project
  * 
  * Copyright (c) 2008 Columbia University
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.geworkbench.builtin.projects;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.UIManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.components.parsers.FileFormat;
 import org.geworkbench.components.parsers.InputFileFormatException;
 import org.geworkbench.components.parsers.microarray.DataSetFileFormat;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.util.threading.SwingWorker;
 
 /**
  * This class is refactored out of ProjectPanel to handle the file open action,
  * especially tackles the progress bar requirement for multiple files.
  * 
  * @author zji
 * @version $Id$
  * 
  */
 public class FileOpenHandler {
 	static Log log = LogFactory.getLog(FileOpenHandler.class);
 
 	private final File[] dataSetFiles;
 	private final FileFormat inputFormat;
 	private final boolean mergeFiles;
 	private final ProjectPanel enclosingProjectPanel;
 
 	ProgressBarDialog pb = null;
 	OpenMultipleFileTask task = null;
 
 //	private String chipType = null;
 
 	FileOpenHandler(final File[] dataSetFiles, final FileFormat inputFormat,
 			final boolean mergeFiles, final ProjectPanel enclosingProjectPanel)
 			throws InputFileFormatException {
 		this.dataSetFiles = dataSetFiles;
 		this.inputFormat = inputFormat;
 
 		this.mergeFiles = mergeFiles;
 		this.enclosingProjectPanel = enclosingProjectPanel;
 
 		enclosingProjectPanel.progressBar.setStringPainted(true);
 		enclosingProjectPanel.progressBar.setString("Loading");
 		enclosingProjectPanel.progressBar.setIndeterminate(true);
 		enclosingProjectPanel.jDataSetPanel.setCursor(Cursor
 				.getPredefinedCursor(Cursor.WAIT_CURSOR));
 	}
 
 	/**
 	 * 
 	 */
 	public void openFiles() {
 		task = new OpenMultipleFileTask();
 		task.execute();
 
 		pb = new ProgressBarDialog(GeawConfigObject.getGuiWindow(),
 				"Files are being opened.");
 		pb.setMessageAndNote(String.format("Completed %d out of %d files.", 0,
 				dataSetFiles.length), String.format(
 				"Currently being processed is %s.", dataSetFiles[0].getName()));
 
 		task.addPropertyChangeListener(pb);
 	}
 
 	private class ProgressBarDialog extends JDialog implements ActionListener,
 			PropertyChangeListener {
 		private static final long serialVersionUID = -3259066552401380723L;
 
 		private JLabel message = null;
 		private JLabel note = null;
 		private JButton cancelButton = null;
 
 		protected void setMessageAndNote(String message, String note) {
 			this.message.setText(message);
 			this.note.setText(note);
 			this.message.invalidate();
 		}
 
 		ProgressBarDialog(JFrame ownerFrame, String title) {
 			// it is important to make it non-modal - for the same reason
 			// customizing dialog is necessary
 			// because this class FileOpenHandler is used within a file chooser
 			// event handler, so it would leave the file open dialog open
 			// otherwise
 			super(ownerFrame, title, false);
 
 			this.setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
 			JPanel leftPanel = new JPanel();
 			add(leftPanel);
 			leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);
 			JPanel rightPanel = new JPanel();
 			add(rightPanel);
 			rightPanel.setAlignmentY(Component.TOP_ALIGNMENT);
 			rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
 
 			JLabel icon = new JLabel(UIManager
 					.getIcon("OptionPane.informationIcon"));
 			icon.setAlignmentY(Component.TOP_ALIGNMENT);
 			leftPanel.add(icon);
 
 			// add two lines of messages
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 			message = new JLabel("");
 			message.setAlignmentX(Component.LEFT_ALIGNMENT);
 			rightPanel.add(message);
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 			note = new JLabel("");
 			note.setAlignmentX(Component.LEFT_ALIGNMENT);
 			rightPanel.add(note);
 
 			JProgressBar progress = new JProgressBar(0, 100);
 			progress.setIndeterminate(true);
 			progress.setMinimumSize(new Dimension(300, 22));
 			progress.setPreferredSize(new Dimension(300, 50));
 
 			progress.setAlignmentX(Component.LEFT_ALIGNMENT);
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 			rightPanel.add(progress);
 
 			cancelButton = new JButton("Cancel");
 			cancelButton.addActionListener(this);
 			cancelButton.setAlignmentX(Component.LEFT_ALIGNMENT);
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
 			rightPanel.add(cancelButton);
 			rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
 
 			pack();
 			setLocationRelativeTo(ownerFrame);
 
 			// disable exit
 			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 			//setVisible(true);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (cancelButton == e.getSource()) {
 				task.cancel(true);
 
 				// task is really stopped when checking isCancel between reading
 				// files, but UI should show canceled
 				enclosingProjectPanel.progressBar.setString("");
 				enclosingProjectPanel.progressBar.setIndeterminate(false);
 				enclosingProjectPanel.jDataSetPanel.setCursor(Cursor
 						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 
 				dispose();
 			}
 		}
 
 		/**
 		 * Invoked when task's progress property changes.
 		 */
 		public void propertyChange(PropertyChangeEvent evt) {
 			if ("progress" == evt.getPropertyName()) {
 				int progress = (Integer) evt.getNewValue();
 				String note = "";
 				if (progress >= 0 && progress < dataSetFiles.length)
 					note = String.format("Currently being processed is %s.",
 							dataSetFiles[progress].getName());
 				pb.setMessageAndNote(String.format(
 						"Completed %d out %d files.", progress,
 						dataSetFiles.length), note);
 			}
 		}
 	} // end of class ProgressBarDialog
 
 	private class OpenMultipleFileTask extends SwingWorker<Void, Void> {
 		@Override
 		protected void done() {
 			pb.dispose();
 		}
 
 		@Override
 		protected Void doInBackground() throws Exception {
 			boolean didMerge = false;
 			int n = dataSetFiles.length;
 			DSDataSet[] dataSets = new DSDataSet[n];
 			DSMicroarraySet mergedSet = null;
 			String mergedName = null;
 			if (dataSetFiles.length == 1) {
 				try {
 					DataSetFileFormat dataSetFileFormat = (DataSetFileFormat) inputFormat;
 					dataSets[0] = dataSetFileFormat
 							.getDataFile(dataSetFiles[0]);
 					dataSets[0].setAbsPath(dataSetFiles[0].getAbsolutePath());
 				} catch (InputFileFormatException iffe) {
 					// Let the user know that there was a problem
 					// parsing the file.
 					JOptionPane.showMessageDialog(null, iffe.getMessage(),
 							"Parsing Error", JOptionPane.ERROR_MESSAGE);
 				}
 
 			} else {
 				// watkin - none of the file filters implement the
 				// multiple getDataFile method.
 				// dataSets[0] =
 				// ((DataSetFileFormat)inputFormat).getDataFile(dataSetFiles);
 				// If the data sets are microarray sets, then merge them
 				// if "merge files" is checked
 
 				// invoking AnnotationParser.matchChipType with null dataset is
 				// different from the previous algorithm.
 				// also notice that this will block
 				String chipType = AnnotationParser.matchChipType(null, "", false);; //FileOpenHandler.this.chipType;
 				pb.setVisible(true);
 				
 				for (int i = 0; i < dataSetFiles.length; i++) {
 					if (isCancelled()) {
 						return null;
 					}
 					File dataSetFile = dataSetFiles[i];
 					try {
 						DataSetFileFormat dataSetFileFormat = (DataSetFileFormat) inputFormat;
 						try {
 							dataSets[i] = dataSetFileFormat.getDataFile(
 									dataSetFile, chipType);
 						} catch (UnsupportedOperationException e) {
 							log
 									.warn("This data type doesn't support chip type overrides, will have to ask user again.");
 							dataSets[i] = ((DataSetFileFormat) inputFormat)
 									.getDataFile(dataSetFile);
 						}
 						dataSets[i].setAbsPath(dataSetFiles[i]
 								.getAbsolutePath());
 					} catch (InputFileFormatException iffe) {
 						// Let the user know that there was a problem
 						// parsing the file.
 						JOptionPane
 								.showMessageDialog(
 										null,
 										"The input file does not comply with the designated format.",
 										"Parsing Error",
 										JOptionPane.ERROR_MESSAGE);
 					} // end of for loop
 					setProgress(i + 1);
 				}
 				if (mergeFiles) {
 					if (dataSets[0] instanceof DSMicroarraySet) {
 						DSMicroarraySet[] maSets = new DSMicroarraySet[dataSets.length];
 						for (int i = 0; i < dataSets.length; i++) {
 							maSets[i] = (DSMicroarraySet) dataSets[i];
 						}
 						enclosingProjectPanel.doMergeSets(maSets);
 						didMerge = true;
 					}
 				}
 			}
 			enclosingProjectPanel.progressBar.setString("");
 			enclosingProjectPanel.progressBar.setIndeterminate(false);
 			enclosingProjectPanel.jDataSetPanel.setCursor(Cursor
 					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			if (didMerge) {
 				// We're done
 				return null;
 			}
 
 			if (mergedSet != null) {
 				mergedSet.setLabel(mergedName);
 				dataSets = new DSDataSet[] { mergedSet };
 			}
 
 			// the last chance to cancel
 			if (isCancelled()) {
 				return null;
 			}
 
 			// If everything went OK, register the newly created
 			// microarray set.
 			// String directory = dataSetFile.getPath();
 			// System.setProperty("data.files.dir", directory);
 			boolean selected = false;
 			for (int i = 0; i < dataSets.length; i++) {
 				DSDataSet set = dataSets[i];
 
 				if (set != null) {
 					// Do intial color context update if it is a
 					// microarray
 					if (set instanceof DSMicroarraySet) {
 						DSMicroarraySet maSet = (DSMicroarraySet) set;
 						// Add color context
 						enclosingProjectPanel.addColorContext(maSet);
 					}
 					// String directory = dataSetFile.getPath();
 					// System.setProperty("data.files.dir", directory);
 					if (!selected) {
 						enclosingProjectPanel.addDataSetNode(set, true);
 						selected = true;
 					} else {
 						enclosingProjectPanel.addDataSetNode(set, false);
 					}
 				} else {
 					log.info("Datafile not loaded");
 				}
 			}
 			return null;
 		}
 	}
 }
