 /*
 	ChibiPaint
     Copyright (c) 2006-2008 Marc Schefer
 
     This file is part of ChibiPaint.
 
     ChibiPaint is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     ChibiPaint is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ChibiPaint. If not, see <http://www.gnu.org/licenses/>.
 
  */
 
 package chibipaint;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.prefs.Preferences;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import chibipaint.engine.CPArtwork;
 import chibipaint.engine.CPUndo;
 import chibipaint.file.CPAbstractFile;
 
 public class CPControllerApplication extends CPController {
 
 	JFrame mainFrame;
 	File currentFile;
 	CPUndo latestRedoAction = null;
 	CPUndo latestUndoAction = null;
 	boolean redoActionMayChange = false;
 	boolean changed;
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// Let's generate appropriate command reaction for everything
 		String[] supportedExts = CPAbstractFile.getSupportedExtensions();
 		for (int i = 0; i < supportedExts.length; i++)
 		{
 			if (e.getActionCommand().equals ("CPSave" + supportedExts[i].toUpperCase()))
 				saveLoadImageFile (supportedExts[i], action_save_load.ACTION_SAVE, "");
 
 			if (e.getActionCommand().equals ("CPLoad" + supportedExts[i].toUpperCase()))
 				saveLoadImageFile (supportedExts[i], action_save_load.ACTION_LOAD, "");
 		}
 
 		// Here we explicitly point that chi extension is native though, it's a little bit bad
 		if (e.getActionCommand().equals ("CPSave"))
 			save ();
 
 		if (e.getActionCommand().equals("CPNew")) {
 			newDialog();
 		}
 
 		if (e.getActionCommand().startsWith("CPOpenRecent")) {
 			String command = e.getActionCommand();
 			int num = command.charAt(command.length() - 1) - '0';
 			openRecent(num);
 		}
 		super.actionPerformed(e);
 	}
 
 	public boolean save ()
 	{
 		return saveLoadImageFile ("chi", action_save_load.ACTION_SAVE, getCurrentFile () != null ? getCurrentFile ().getAbsolutePath () : "");
 	}
 
 	public CPControllerApplication(JFrame mainFrame) {
 		this.mainFrame = mainFrame;
 	}
 
 	@Override
 	public Component getDialogParent() {
 		return mainFrame;
 	}
 
 	public void resetEverything(CPArtwork newArtwork) {
 		((ChibiApp) mainFrame).recreateEverything(newArtwork);
 	}
 
 	public void updateTitle() {
 		String titleString;
 		titleString = (changed ? "*" : "");
 		if (currentFile != null)
 			titleString += currentFile.getName() + " - ChibiPaintMod";
 		else
 			titleString += "Untitled - ChibiPaintMod";
 
 		// TODO: probably add some good visible progress bar
 		if (((ChibiApp) mainFrame).getAppIsBusy ())
 			titleString += " (Saving...)";
 		mainFrame.setTitle(titleString);
 	}
 
 	public void setCurrentFile(File file) {
 		if (file != null)
 			currentFile = new File(file.getAbsolutePath());
 		else
 			currentFile = null;
 		updateTitle();
 	}
 
 	public File getCurrentFile() {
 		return currentFile;
 	}
 
 	public void updateChanges(CPUndo undoAction, CPUndo redoAction) {
 		changed = (latestUndoAction != undoAction || latestRedoAction != redoAction); // Trivial
 		// logic
 		// -
 		// our
 		// position
 		// changed
 		// so
 		// file
 		// is
 		// changed
 		if (latestUndoAction == undoAction && redoAction == null) // We have
 			// returned
 			// to our
 			// saved
 			// state
 			// (which
 			// was
 			// without
 			// any redo
 			// action
 			// initially)
 			changed = false; // So file isn't changed (This logic is needed
 		// cause redo action may have changed in the
 		// future)
 		updateTitle();
 
 		if ((latestUndoAction == null && latestRedoAction == null) // If
 				// latestRedoaction
 				// wasn't
 				// set at
 				// all
 				|| (redoActionMayChange // Or latestRedoAction may change
 						// according to the later logic
 						&& (changed) // But change of position has happened
 						&& (redoAction != latestUndoAction || latestUndoAction == null))) // And
 			// it's
 			// not
 			// undo
 			// change
 		{
 			latestRedoAction = undoAction; // Then really changing
 			// latestRedoaction
 			redoActionMayChange = false; // And disabling flag
 		}
 
 		if (!changed) // lastestRedoAction may change when we returned to
 			// initial (saved) position
 			redoActionMayChange = true; // Cause changes may be rewritten
 		// Some bugs mat still be present, need further testing
 	}
 
 	public void setLatestAction(CPUndo undoAction, CPUndo redoAction) {
 		latestRedoAction = redoAction;
 		latestUndoAction = undoAction;
 		updateChanges(undoAction, redoAction);
 	}
 
 	void openRecent(int index) {
 		Preferences userRoot = Preferences.userRoot();
 		Preferences preferences = userRoot.node("chibipaintmod");
 		String recentFileName = preferences.get(recent_file_string(index), "");
 		saveLoadImageFile("CHI",
 				action_save_load.ACTION_LOAD, recentFileName);
 	}
 
 	void newDialog() {
 		JPanel panel1 = new JPanel();
 
 		panel1.add(new JLabel("Width:"));
 		JTextField widthNum = new JTextField(
 				String.valueOf(this.artwork.width), 10);
 		panel1.add(widthNum);
 
 		JPanel panel2 = new JPanel();
 
 		panel2.add(new JLabel("Height:"));
 		JTextField heightNum = new JTextField(
 				String.valueOf(this.artwork.height), 10);
 		panel2.add(heightNum);
 
 		Object[] array = { "Select Width and Height:\n\n", panel1, panel2 };
 		int choice = JOptionPane.showConfirmDialog(getDialogParent(), array,
 				"Create New Image", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.PLAIN_MESSAGE);
 
 		if (choice == JOptionPane.OK_OPTION) {
 			try {
 				if (!((ChibiApp) mainFrame).confirmDialog())
 					return;
 				CPArtwork new_artwork = new CPArtwork(Integer.valueOf(widthNum
 						.getText()), Integer.valueOf(heightNum.getText()));
 				setCurrentFile(null);
 				setLatestAction (null, null);
 				resetEverything(new_artwork);
 			} catch (OutOfMemoryError E) {
 				JOptionPane
 				.showMessageDialog(
 						mainFrame,
 						"Sorry, not Enough Memory. Please restart the application or try to use lesser image size.");
 			}
 		}
 	}
 
 	public enum action_save_load {
 		ACTION_SAVE, ACTION_LOAD
 	}
 	static String recent_file_string(int i) {
 		return "Recent File[" + i + "]";
 	}
 
 	// file_name used only in recent files handling, very scarce use actually
 	private boolean saveLoadImageFile(final String ext, // Extension characterizes type of file
 			action_save_load action, String file_name) {
 
 		CPAbstractFile file = CPAbstractFile.fromExtension (ext);
 
 		int returnVal = JFileChooser.CANCEL_OPTION;
 		Preferences userRoot = Preferences.userRoot();
 		Preferences preferences = userRoot.node("chibipaintmod");
 		String directoryName = preferences.get("lastDirectory", "");
 		File dir = new File(directoryName);
 
 		final JFileChooser fc = new JFileChooser(dir) {
 			/**
 			 *
 			 */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void approveSelection() {
 				File f = getSelectedFile();
 				if (getDialogType() == SAVE_DIALOG) {
 					String filePath = f.getPath();
 
 					if (!filePath.toLowerCase().endsWith(ext)) {
 						f = new File(filePath + "." + ext);
 					}
 					if (f.exists())
 					{
 						int result = JOptionPane.showConfirmDialog(this,
 								"The file exists, overwrite?", "Existing file",
 								JOptionPane.YES_NO_OPTION);
 						switch (result) {
 						case JOptionPane.YES_OPTION:
 							super.approveSelection();
 							return;
 						case JOptionPane.NO_OPTION:
 						case JOptionPane.CLOSED_OPTION:
 						default:
 							return;
 						}
 					}
 				}
 				super.approveSelection();
 			}
 		};
 
 		if (file_name == "") {
 
 			FileNameExtensionFilter filter = null;
 			filter = file.fileFilter ();
 			if (!file.isNative() && currentFile != null)
 			{
 				fc.setSelectedFile(new File(currentFile.getName()
 						.substring(0,
 								currentFile.getName().lastIndexOf('.'))));
 			}
 			fc.setAcceptAllFileFilterUsed(false);
 			fc.addChoosableFileFilter(filter);
 
 			returnVal = 0;
 
 			switch (action) {
 			case ACTION_LOAD:
 				returnVal = fc.showOpenDialog(canvas);
 				break;
 			case ACTION_SAVE:
 				returnVal = fc.showSaveDialog(canvas);
 				break;
 			}
 		}
 
 		if (returnVal == JFileChooser.APPROVE_OPTION || file_name != "") {
 			File selectedFile;
 			if (file_name != "")
 				selectedFile = new File(file_name);
 			else
 				selectedFile = fc.getSelectedFile();
 
 			if (action == action_save_load.ACTION_SAVE) {
 				String filePath = selectedFile.getPath();
 
 				if (!filePath.toLowerCase().endsWith(ext)) {
 					selectedFile = new File(filePath + "." + ext);
 				}
 			}
 
 			preferences.put("lastDirectory", selectedFile.getParent());
 			// Writing file to recent
 
 			if (action == action_save_load.ACTION_LOAD
 					&& !((ChibiApp) mainFrame).confirmDialog())
 				return false;
 
 			if (!selectedFile.exists()
 					&& action == action_save_load.ACTION_LOAD) {
 				// TODO:
 				// Here should be code about asking for removal from recent file
 				// list
 				return false;
 			}
 
 			if (file.isNative ()) {
 				Boolean found = false;
 				for (int i = 0; i < 10; i++) {
 					String file_name_from_list = preferences.get("Recent File["
 							+ i + "]", "");
 					if (file_name_from_list.length() != 0
 							&& file_name_from_list.equals(selectedFile
 									.getAbsolutePath())) {
 						for (int j = i - 1; j >= 0; j--)
 							preferences.put(recent_file_string(j + 1),
 									preferences.get(recent_file_string(j), ""));
 
 						found = true;
 						break;
 					}
 				}
 				if (!found) {
 					for (int j = 8; j >= 0; j--)
 						preferences.put(recent_file_string(j + 1),
 								preferences.get(recent_file_string(j), ""));
 				}
 
 				preferences.put(recent_file_string(0),
 						selectedFile.getAbsolutePath());
 			}
 
 			if (action == action_save_load.ACTION_SAVE) // settting that app is busy so program won't exit until saving is done
 			{
 				((ChibiApp) mainFrame).setAppIsBusy (true);
 			}
 
 			switch (action) {
 			case ACTION_LOAD:
 				try {
 					FileInputStream fos;
 					try {
 						fos = new FileInputStream(selectedFile);
 					} catch (FileNotFoundException e) {
 						return false;
 					}
 					CPArtwork tempArtwork = file.read(fos);
 					if (tempArtwork == null)
 					{
 						JOptionPane
 						.showMessageDialog(
 								mainFrame,
 								"Sorry, but this type of action is probably unsupported");
 						fos.close();
 						return false;
 					}
 					try {
 						fos.close();
 					} catch (IOException e) {
 						return false;
 					}
 					setLatestAction (null, null);
 					resetEverything(tempArtwork);
 				}
 				catch (OutOfMemoryError E) {
 					JOptionPane
 					.showMessageDialog(
 							mainFrame,
 							"Sorry, not Enough Memory. Please restart the application or try to use lesser image size.");
 					return false;
 				} catch (IOException e) {
 					return false;
 				}
 				break;
 			case ACTION_SAVE:
 				FileOutputStream fos;
 				try {
 					fos = new FileOutputStream(selectedFile);
 				} catch (FileNotFoundException e2) {
 					return false;
 				}
 				try {
 					boolean result = file.write(fos, artwork);
 					if (!result)
 					{
 						JOptionPane
 						.showMessageDialog(
 								mainFrame,
 								"Sorry, but this type of action is probably unsupported");
 						((ChibiApp) mainFrame).setAppIsBusy (false);
 						return false;
 					}
 					fos.close();
 					((ChibiApp) mainFrame).setAppIsBusy (false);
 				} catch (IOException e) {
 					((ChibiApp) mainFrame).setAppIsBusy (false);
 					return false;
 				}
 				break;
 			}
 
 			// Adding name to frame title
 			if (file.isNative ())
 			{
 				setCurrentFile(selectedFile);
 
 				if (action == action_save_load.ACTION_SAVE) {
 					((ChibiApp) mainFrame).resetMainMenu();
 					setLatestAction(artwork.getUndoList().size() > 0 ? artwork
 							.getUndoList().getFirst() : null, artwork
 							.getRedoList().size() > 0 ? artwork.getRedoList()
 									.getFirst() : null);
 					((ChibiApp) mainFrame).setAppIsBusy (false);
 				}
 			}
 			else if (action == action_save_load.ACTION_LOAD) // If native file is loaded image should turn to untitled
 			{
 
 				setCurrentFile (null);
 				setLatestAction (null, null);
 			}
 
 			return true;
 		}
 		return true; // Actually that's ok behaviour, just cancel was pressed
 	}
 
 	void saveControllerSettings() {
 		Preferences userRoot = Preferences.userRoot();
 		Preferences preferences = userRoot.node("chibipaintmod");
 		preferences.putInt ("Mode", getCurMode());
 		preferences.putInt ("Brush", getCurBrush());
 		for (int i = 0; i < T_MAX; i++) {
 			preferences.putInt("Tool " + String.valueOf(i) + " - Type",
 					tools[i].type);
 			preferences.putInt("Tool " + String.valueOf(i) + " - Size",
 					tools[i].size);
 			preferences.putInt("Tool " + String.valueOf(i) + " - Alpha",
 					tools[i].alpha);
 			preferences.putFloat("Tool " + String.valueOf(i) + " - Color",
 					tools[i].resat);
 			preferences.putFloat("Tool " + String.valueOf(i) + " - Blend",
 					tools[i].bleed);
 			preferences.putFloat("Tool " + String.valueOf(i) + " - Spacing",
 					tools[i].spacing);
 			preferences.putFloat("Tool " + String.valueOf(i) + " - Scattering",
 					tools[i].scattering);
 			preferences.putFloat("Tool " + String.valueOf(i) + " - Smoothing",
 					tools[i].smoothing);
 			preferences.putBoolean("Tool " + String.valueOf(i)
 					+ " - Alpha from Pressure", tools[i].pressureAlpha);
 			preferences.putBoolean("Tool " + String.valueOf(i)
 					+ " - Size from Pressure", tools[i].pressureSize);
 			preferences.putBoolean("Tool " + String.valueOf(i)
 					+ " - Scattering from Pressure",
 					tools[i].pressureScattering);
 		}
 	}
 
 	void loadUrgentSettings() {
 		// Settings that should be loaded before gui to avoid rewriting a lots of code
 		Preferences userRoot = Preferences.userRoot();
 		Preferences preferences = userRoot.node("chibipaintmod");
 		setMode (preferences.getInt ("Mode", getCurMode()));
 		if (getCurMode() == M_DRAW)
 			setTool (preferences.getInt ("Brush", getCurBrush()));
 	}
 
 	void loadControllerSettings() {
 		Preferences userRoot = Preferences.userRoot();
 		Preferences preferences = userRoot.node("chibipaintmod");
 
		setMode (preferences.getInt ("Mode", getCurMode())); // Note these settings reading two times
		if (getCurMode() == M_DRAW)					         // Because we should set them one more time after enabling
			setTool (preferences.getInt ("Brush", getCurBrush())); // of some canvas listeners

 		for (int i = 0; i < T_MAX; i++) {
 			tools[i].type = preferences.getInt("Tool " + String.valueOf(i)
 					+ " - Type", tools[i].type);
 			tools[i].size = preferences.getInt("Tool " + String.valueOf(i)
 					+ " - Size", tools[i].size);
 			tools[i].alpha = preferences.getInt("Tool " + String.valueOf(i)
 					+ " - Alpha", tools[i].alpha);
 			tools[i].resat = preferences.getFloat("Tool " + String.valueOf(i)
 					+ " - Color", tools[i].resat);
 			tools[i].bleed = preferences.getFloat("Tool " + String.valueOf(i)
 					+ " - Blend", tools[i].bleed);
 			tools[i].spacing = preferences.getFloat("Tool " + String.valueOf(i)
 					+ " - Spacing", tools[i].spacing);
 			tools[i].scattering = preferences.getFloat(
 					"Tool " + String.valueOf(i) + " - Scattering",
 					tools[i].scattering);
 			tools[i].smoothing = preferences.getFloat(
 					"Tool " + String.valueOf(i) + " - Smoothing",
 					tools[i].smoothing);
 			tools[i].pressureAlpha = preferences.getBoolean(
 					"Tool " + String.valueOf(i) + " - Alpha from Pressure",
 					tools[i].pressureAlpha);
 			tools[i].pressureSize = preferences.getBoolean(
 					"Tool " + String.valueOf(i) + " - Size from Pressure",
 					tools[i].pressureSize);
 			tools[i].pressureScattering = preferences.getBoolean("Tool "
 					+ String.valueOf(i) + " - Scattering from Pressure",
 					tools[i].pressureScattering);
 		}
 	}
 }
