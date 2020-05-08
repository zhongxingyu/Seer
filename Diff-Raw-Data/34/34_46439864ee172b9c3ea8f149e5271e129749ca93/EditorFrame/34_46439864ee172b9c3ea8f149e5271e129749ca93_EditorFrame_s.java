 /**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis.ui;
 
 import org.nees.rpi.vis.*;
 import org.nees.rpi.vis.ui.dialogs.TimeChannelPickerDialog;
 import org.nees.rpi.vis.model.*;
 import org.nees.rpi.vis.exporters.M3DVCExporter;
 import org.nees.rpi.vis.loaders.DataFileLoader;
 import org.nees.rpi.vis.loaders.DelimiterType;
 import org.nees.rpi.vis.loaders.DataFileLoaderException;
 import org.nees.rpi.vis.loaders.M3DVCLoader;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TableModelEvent;
 import javax.vecmath.Point3f;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Collection;
 
 /**
  * An M3DV model editor window
  */
 public class EditorFrame extends VisFrame
 {
 	// ############################################################################################################
 	// INSTANCE VARIABLES - START
 	/** The top-most component container of the frame */
 	JPanel contentPane;
 
 	/** The panel used as the middle column of the display */
 	JPanel middlePane;
 
 	/** the file to save the model in editing to */
 	File saveFile;
 
 	/** the new DVModel edited by the user */
 	DVModel modelInEditing;
 
 	/** The listener object that will be attached to the modelInEditing object */
 	ModelChangeHandler modelListener = new ModelChangeHandler();
 
 	/** the panel containing the main menu */
 	EditorMainMenuPanel mainMenu = new EditorMainMenuPanel();
 
 	/** the panel containing the high-level model properties to be edited */
 	EditorModelEditPanel modelEditPanel = new EditorModelEditPanel();
 
 	/** the panel containing the sensor list being edited */
 	EditorSensorEditPanel sensorEditPanel = new EditorSensorEditPanel();
 
 	/** the panel containing the chart and plotting related logic */
 	EditorChartPanel chartingPanel = new EditorChartPanel();
 
 	/** the panel containing the GUI elements for the model data files */
 	EditorDataFilePanel datafilePanel = new EditorDataFilePanel();
 
 	/** the panel containing the UI elements for import related actions */
 	EditorImportMenuPanel importMenuPanel = new EditorImportMenuPanel();
 
 	/** panel shown when the model in editing goes in a modified state, hidden otherwise */
 	EditorModifiedNotifierPanel notifierPanel = new EditorModifiedNotifierPanel();
 
 	/** internal flag used to keep track if the frame has gotten notification that the model in editing has been modified */
 	boolean modifiedSet = false;
 
 	// INSTANCE VARIABLES - END
 	// ############################################################################################################
 	// ############################################################################################################
 	// CONSTRUCTORS & INIT - START
 	public EditorFrame()
 	{
 		super("modeleditorwindow");
 		initFrame();
 		initMenusAndSettings();
 		initMiddlePane();
 		initModelEditPanel();
 		initShapeEditList();
 		initChartArea();
 		initChannelList();
 	}
 
 	/**
 	 * initializes the main frame and sets it up before any child
 	 * components are added
 	 */
 	private void initFrame()
 	{
 		setTitle("3DDV Model Editor");
 		contentPane = new JPanel();
 		setContentPane(contentPane);
 		contentPane.setOpaque(true);
 		contentPane.setBackground(Color.WHITE);
 		contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
 		setLayout(new BorderLayout());
 	}
 
 	/**
 	 * initializes the section of the window devoted to menus and settings
 	 */
 	private void initMenusAndSettings()
 	{
 		JPanel menusPanel = new JPanel();
 		menusPanel.setOpaque(false);
 		menusPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
 
 		menusPanel.setLayout(new BoxLayout(menusPanel, BoxLayout.Y_AXIS));
 		menusPanel.add(notifierPanel);
 		menusPanel.add(importMenuPanel);
 		menusPanel.add(mainMenu);
 
 		// Hack to addjust the spacing for this panel
 		JPanel spacingPanel = new JPanel();
 		spacingPanel.setPreferredSize(new Dimension(200, 2000));
 		spacingPanel.setOpaque(false);
 		menusPanel.add(spacingPanel);
 
 		contentPane.add(menusPanel, BorderLayout.WEST);
 
 		//set actions
 		mainMenu.setOpenModelAction(new OpenModelAction());
 		mainMenu.setNewModelAction(new NewModelAction());
 		mainMenu.setSaveModelAction(new SaveModelInEditingAction());
 		mainMenu.setSaveModelAsAction(new SaveModelInEditingAsAction());
 		mainMenu.setCloseWindowAction(new CloseFrameAction());
 		importMenuPanel.setImportDataFileAction(new ImportDataFileAction());
 	}
 
 	/**
 	 * Initializes the middle "column" of the window so that other panels
 	 * can be attached to it
 	 */
 	private void initMiddlePane()
 	{
 		middlePane = new JPanel();
 		middlePane.setOpaque(false);
 		middlePane.setLayout(new BorderLayout());
 		middlePane.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
 
 		contentPane.add(middlePane, BorderLayout.CENTER);
 	}
 
 	private void initModelEditPanel()
 	{
 		middlePane.add(modelEditPanel, BorderLayout.NORTH);
 	}
 
 	/**
 	 * initializes the section of the window used to display the M3DV model elements
 	 */
 	private void initShapeEditList()
 	{
 		sensorEditPanel.setTableModelListener(new TableModelHandler());
 		sensorEditPanel.addShapeSelectionListener(new ShapeSelectionChangeHandler());
 		sensorEditPanel.setRemoveSelectedAction(new RemoveSelectedShapesAction());
 		middlePane.add(sensorEditPanel, BorderLayout.CENTER);
 	}
 
 	/**
 	 * initializes the charting area used to display plots
 	 */
 	private void initChartArea()
 	{
 		middlePane.add(chartingPanel, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * initializes the section of the window used to display the data chennels imported
 	 * from data files
 	 */
 	private void initChannelList()
 	{
 		contentPane.add(datafilePanel, BorderLayout.EAST);
 		datafilePanel.setAddSelectedAction(new AddSelectedChannelsAction());
 		datafilePanel.setRemoveSelectedDataFilesAction(new RemoveSelectedDataFilesAction());
 		datafilePanel.setChangeTimeChannelAction(new ChangeTimeChannelAction());
 	}
 	// CONSTRUCTORS & INIT - END
 	// ############################################################################################################
 	// ############################################################################################################
 	// OTHER PUBLIC METHODS - START
 	/**
 	 * initializes the frame to edit a new model
 	 */
 	public void loadNewModel()
 	{
 		DVModel model = new DVModel("Untitled Model");
 		model.addGroup(new DVGroup("Data Channels"));
 		setModel(model);
 	}
 	// OTHER PUBLIC METHODS - END
 	// ############################################################################################################
 	// ############################################################################################################
 	// PRIVATE WORKER METHODS - START
 	private void setModel(DVModel model)
 	{
 		if (modelInEditing != null)
 			modelInEditing.removeChangeListener(modelListener);
 
 		modelInEditing = model;
 		modelInEditing.addChangeListener(modelListener);
 		modelEditPanel.setModel(modelInEditing);
 		sensorEditPanel.setModel(modelInEditing);
 		datafilePanel.clearAll();
 		chartingPanel.clearPlot();
 		resetModifiedState();
 		setSaveFile(model.getFile());
 	}
 
 	/**
 	 * Adds a new shape to the model being edited
 	 */
 	private void addShape(DVShape shape, DVGroup parent)
 	{
 		modelInEditing.addShape(shape, parent);
 		sensorEditPanel.addShape(shape);
 	}
 
 	/**
 	 * highlights in the datafilePanel the sensors
 	 * selected in the shape list by the user, overwriting
 	 * perviously highlighted channels
 	 */
 	private void highlightSelectedSensors()
 	{
 		ArrayList<DVDataChannel> channels = new ArrayList<DVDataChannel>();
 		for (DVShape shape : sensorEditPanel.getSelectedShapes())
 		{
 			if (!channels.contains(shape.getXChannel())) channels.add(shape.getXChannel());
 			if (!channels.contains(shape.getYChannel())) channels.add(shape.getYChannel());
 		}
 		datafilePanel.setHighlighted(channels);
 	}
 
 	/**
 	 * loads data channels from a data file and imports the channel info
 	 * into the frame UI
 	 *
 	 * @param datafile
 	 */
 	private void loadChannels(File datafile)
 	{
 		DataFileLoader datafileLoader;
 		DVDataFile dvDataFile = null;
 		boolean loaderror = false;
 		String errorMsg = "";
 
 		if (importMenuPanel.getDataFileDelimiter() == DelimiterType.TAB_DELIMITED)
 			datafileLoader = new DataFileLoader(DelimiterType.TAB_DELIMITED);
 		else
 			datafileLoader = new DataFileLoader(DelimiterType.COMMA_DELIMITED);
 
 		try
 		{
 			dvDataFile = datafileLoader.loadFile(datafile.toURL());
 		}
 		catch (MalformedURLException e)
 		{
 			errorMsg = e.getMessage();
 			loaderror = true;
 		}
 		catch (DataFileLoaderException e)
 		{
 			errorMsg = e.getMessage();
 			loaderror = true;
 		}
 		finally
 		{
 			if (loaderror)
 			{
 				String errorMsgStart = "An error occured while importing your file. ";
 				errorMsgStart += "Please verify that is a valid text data file.\n\n";
 				errorMsg = errorMsgStart + errorMsg;
 				JOptionPane.showMessageDialog(this, errorMsg, "Import Load Error", JOptionPane.ERROR_MESSAGE);
 			}
 			else
 			{
 				modelInEditing.addDataFile(dvDataFile);
 				showAndProcessTimeChannelSelection(dvDataFile);
 			}
 		}
 	}
 
 	private void resetModifiedState()
 	{
 		modelInEditing.resetModified();
 		notifierPanel.setVisible(false);
 		modifiedSet = false;
 	}
 
 	private void setModifiedState()
 	{
 		if (!modifiedSet)
 		{
 			notifierPanel.setVisible(true);
 			modifiedSet = true;
 		}
 	}
 
 	private void saveModel()
 	{
 		M3DVCExporter.export(modelInEditing, saveFile);
 		resetModifiedState();
 	}
 
 	private void setSaveFile(File savefile)
 	{
 		saveFile = savefile;
 		if (saveFile == null)
 			mainMenu.saveModelButton.setEnabled(false);
 		else
 			mainMenu.saveModelButton.setEnabled(true);
 	}
 
 	private void showAndProcessTimeChannelSelection(DVDataFile datafile)
 	{
 		ArrayList<DVDataFile> datafiles = new ArrayList<DVDataFile>();
 		datafiles.add(datafile);
 		showAndProcessTimeChannelSelection(datafiles);
 	}
 
 	private void showAndProcessTimeChannelSelection(Collection<DVDataFile> datafiles)
 	{
 		HashMap<DVDataFile, DVDataChannel> selected =
 				TimeChannelPickerDialog.showTimeChannelPicker(EditorFrame.this, datafiles);
 		for ( DVDataFile datafile : selected.keySet() )
 			datafile.setTimeChannel(selected.get(datafile));
 	}
 	// PRIVATE WORKER METHODS - END
 	// ############################################################################################################
 	// ############################################################################################################
 	// INTERNAL CLASSES - START
 	class ChangeTimeChannelAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			showAndProcessTimeChannelSelection( modelInEditing.getDataFiles() );
 		}
 	}
 
 	/**
 	 * Action to close the main model editor window
 	 */
 	class CloseFrameAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			EditorFrame.this.setVisible(false);
 		}
 	}
 
 	/**
 	 * Action to replace the current model in editing with a new blank one
 	 */
 	class NewModelAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			EditorFrame.this.loadNewModel();
 		}
 	}
 
 	/**
 	 * Action to open an existing model for editing
 	 */
 	class OpenModelAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			AppSettings APP = AppSettings.getInstance();
 			String lastDir = APP.getString(frameid + ".lastsavedir");
 			JFileChooser fc = new JFileChooser();
 			if (lastDir != null)
 				fc.setCurrentDirectory(new File(lastDir));
 			fc.setFileFilter(new SimpleFileFilter("3ddv", APP.getM3dvFileChooserDescriptor()));
 			int returnval = fc.showOpenDialog(EditorFrame.this);
 			if (returnval == JFileChooser.APPROVE_OPTION)
 			{
 				AppSettings.getInstance().setProperty(
 								frameid + ".lastsavedir", fc.getCurrentDirectory().toString());
 				M3DVCLoader loader = new M3DVCLoader(fc.getSelectedFile());
 				DVModel loadedmodel = loader.load();
 				if (loadedmodel == null || loader.isError())
 				{
 					String errorMsg = "The following errors occured while loading your model file:\n\n";
 					for (int i=0; i<loader.getErrors().length; i++)
 						errorMsg += loader.getErrors()[i].toString() + "\n";
 					System.out.println(errorMsg);
 					JOptionPane.showMessageDialog(EditorFrame.this, errorMsg, "Model File Load Error", JOptionPane.ERROR_MESSAGE);
 				}
 				else
 					setModel(loadedmodel);
 			}
 		}
 	}
 
 	/**
 	 * Action to save a model to an existing file
 	 */
 	class SaveModelInEditingAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			saveModel();
 		}
 	}
 
 	/**
 	 * Action to save a model in editing as a new file name
 	 */
 	class SaveModelInEditingAsAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			AppSettings APP = AppSettings.getInstance();
 			String lastDir = APP.getString(frameid + ".lastsavedir");
 			JFileChooser fc = new JFileChooser();
 			if (lastDir != null)
 				fc.setCurrentDirectory(new File(lastDir));
 			fc.setFileFilter(new SimpleFileFilter("3ddv", APP.getM3dvFileChooserDescriptor()));
 			int returnval = fc.showSaveDialog(EditorFrame.this);
 			if (returnval == JFileChooser.APPROVE_OPTION)
 			{
 				AppSettings.getInstance().setProperty(
 								frameid + ".lastsavedir", fc.getCurrentDirectory().toString());
 				setSaveFile(fc.getSelectedFile());
 				saveModel();
 			}
 		}
 	}
 
 	/**
 	 * Action to prompt user for a data file to import and initiate the
 	 * data import process.
 	 *
 	 */
 	class ImportDataFileAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			String lastDir = AppSettings.getInstance().getString(frameid + ".lastimportdir");
 			JFileChooser fc = new JFileChooser();
 			if (lastDir != null)
 				fc.setCurrentDirectory(new File(lastDir));
 			fc.setFileFilter(new SimpleFileFilter("txt", "Text Data File (*.txt)"));
 			int returnval = fc.showOpenDialog(EditorFrame.this);
 			if (returnval == JFileChooser.APPROVE_OPTION)
 			{
 				AppSettings.getInstance().setProperty(
 								frameid + ".lastimportdir", fc.getCurrentDirectory().toString());
 				File file = fc.getSelectedFile();
 				if ( modelInEditing.containsDataFile(file) )
 				{
 					 JOptionPane.showMessageDialog(
 							 EditorFrame.this,
 							 "This data file has already been added to this model!",
 							 "Data File Error",
 							 JOptionPane.ERROR_MESSAGE);
 				}
 				else
 					loadChannels(file);
 			}
 		}
 	}
 
 	/**
 	 * Action to add the user selected data channels to the model
 	 */
 	class AddSelectedChannelsAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			java.util.List<DVDataChannel> channels = datafilePanel.getSelectedChannels();
 
 			// time channel is the first one
 			DVDataFile parent = null;
 
 			for (DVDataChannel channel : channels)
 			{
 				if (parent != channel.getParent())
 				{
 					parent = channel.getParent();
 					modelInEditing.addDataFile(parent);
 				}
 				DVShape newshape = DVShapeFactory.createPressureSensor(new Point3f(0,0,0));
 				newshape.setName(channel.getName());
 				newshape.setTimeSeries(channel);
 				//add to the first group until logic is added to let the user select the group
 				addShape(newshape, modelInEditing.getGroups().get(0));
 			}
 
 			datafilePanel.clearChannelSelection();
 		}
 	}
 
 	/**
 	 * Action to remove the selected data files from the model
 	 */
 	class RemoveSelectedDataFilesAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			List<DVDataFile> selected = datafilePanel.getSelectedDataFiles();
 
 			if (selected.size() == 0)
 			{
 				showEmptyDataFileListWarning();
 				return;
 			}
 
 			for (DVDataFile datafile : selected)
 			{
 				int shapecount = modelInEditing.getDataFileShapeCount(datafile);
 				if ( shapecount > 0 && ! showShapesWillBeDeletedWarning(datafile.getFileName(), shapecount) )
 					continue;
 
 				modelInEditing.removeDataFile(datafile);
 			}
 		}
 
 		void showEmptyDataFileListWarning()
 		{
 			String errorMsg = "You have not selected any Data File(s) to be removed!";
 			JOptionPane.showMessageDialog(EditorFrame.this, errorMsg, "Selection Error", JOptionPane.ERROR_MESSAGE);
 		}
 
 		boolean showShapesWillBeDeletedWarning(String filename, int shapeCount)
 		{
 			String msg = "The file " + filename + " has " + shapeCount + " shapes that use its data.\n";
 			msg += "Deleting the data file will also delete those shapes. Are you sure you want to continue?";
 			return JOptionPane.YES_OPTION ==
 					JOptionPane.showConfirmDialog(
 							EditorFrame.this, msg, "Do you want to continue?", JOptionPane.YES_NO_OPTION);
 		}
 	}
 
 	/**
 	 * Action to remove the user selected shapes from the model list
 	 */
 	class RemoveSelectedShapesAction extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			java.util.List<DVShape> shapes = sensorEditPanel.getSelectedShapes();
 			sensorEditPanel.cancelEditing();
 			for (DVShape shape : shapes)
 				modelInEditing.removeShape(shape);
 		}
 	}
 
 	/**
 	 * Listener listening in on changes to the DVModel in editing
 	 * and updating the UI elements of the EditorFrame as is
 	 * necessary based on the event.
 	 */
 	class ModelChangeHandler implements DVModelChangeListener
 	{
 		public void modified(DVModel model)
 		{
 			setModifiedState();
 		}
 
 		public void datafileAdded(DVDataFile datafile)
 		{
 			datafilePanel.addDataFile(datafile);
 		}
 
 		public void datafileChanged(DVDataFile datafile)
 		{
 			// The only reason this would happen within the ME
 			// at this time is if a time channel change occurs
 			// so, rehighlight selected channels in the datafilePanel
 			// and replot them
 			highlightSelectedSensors();
 			chartingPanel.plotShapes(sensorEditPanel.getSelectedShapes());
 		}
 
 		public void datafileRemoved(DVDataFile datafile)
 		{
 			datafilePanel.removeDataFile(datafile);
 		}
 
 		public void groupAdded(DVGroup group) { }
 
 		public void groupRemoved(DVGroup group) { }
 
 		public void groupAppearanceChanged(DVGroup group) { }
 
 		public void groupNameChanged(DVGroup group) { }
 
 		public void shapeAdded(DVShape shape)
 		{
 			datafilePanel.addToActiveChannels(shape.getYChannel());
 		}
 
 		public void shapeRemoved(DVShape shape)
 		{
 			sensorEditPanel.removeShape(shape);
 			datafilePanel.removeFromActiveChannels(shape.getYChannel());
 		}
 
 		public void shapeAppearanceChanged(DVShape shape) { }
 
 		public void shapeLocationChanged(DVShape shape) { }
 
 		public void shapeMetadataChanged(DVShape shape) { }
 
 		public void shapeNameChanged(DVShape shape) { }
 
 		public void shapeSeriesChanged(DVShape shape) { }
 	}
 
 	/**
 	 * Listener that listens to the model table and triggers when the
 	 * user changes the shape row(s) he/she has selected.
 	 *
 	 * The action is takes is plot the row data to the ME plot and highlights
 	 * the channels used by the selected shapes in the datafilepanel
 	 */
 	class ShapeSelectionChangeHandler implements ListSelectionListener
 	{
 		private List<DVShape> selectedshapes = new ArrayList<DVShape>();
 
 		public void valueChanged(ListSelectionEvent listSelectionEvent)
 		{
 			if (!isNewSelection())
 				return; //don't do anything
 			highlightSelectedSensors();
 			chartingPanel.plotShapes(selectedshapes);
 		}
 
 		private boolean isNewSelection()
 		{
 			boolean isnew;
 			List<DVShape> selectednow = sensorEditPanel.getSelectedShapes();
 
 			if (selectedshapes.size() != selectednow.size())
 				isnew = true;
 			else
 				isnew = ! selectedshapes.containsAll(selectednow);
 
 			selectedshapes = selectednow;
 			return isnew;
 		}
 	}
 
 	/**
 	 * Listens to changes to the model object of the shapes table and
 	 * resets the plot to reflect user changes immediately the shape
 	 * names.
 	 */
 	class TableModelHandler implements TableModelListener
 	{
 		public void tableChanged(TableModelEvent tableModelEvent)
 		{
 			//replot everything to get the names right
 			chartingPanel.clearPlot();
 			chartingPanel.plotShapes(sensorEditPanel.getSelectedShapes());
 		}
 	}
 	// INTERNAL CLASSES - END
 	// ############################################################################################################
 }
 
