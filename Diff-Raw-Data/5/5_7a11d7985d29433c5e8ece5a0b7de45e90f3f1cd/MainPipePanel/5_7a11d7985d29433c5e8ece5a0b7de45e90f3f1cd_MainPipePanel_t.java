 package pleocmd.itfc.gui;
 
 import java.io.File;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import pleocmd.Log;
 import pleocmd.cfg.Configuration;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.pipe.Pipe;
 
 public final class MainPipePanel extends JPanel {
 
 	private static final long serialVersionUID = 5361715509143723415L;
 
 	private final JLabel pipeLabel;
 
 	private final JButton btnModify;
 
 	private final JButton btnSave;
 
 	private final JButton btnLoad;
 
 	private PipeConfigDialog cfgDialog;
 
 	public MainPipePanel() {
 		final Layouter lay = new Layouter(this);
 		pipeLabel = new JLabel();
 		updatePipeLabel();
 		lay.addWholeLine(pipeLabel, false);
 
 		btnModify = lay.addButton(Button.Modify,
 				"Modify the currently active pipe", new Runnable() {
 					@Override
 					public void run() {
 						changeConfig();
 					}
 				});
 		lay.addSpacer();
 		btnSave = lay.addButton(Button.SaveTo,
 				"Save the current pipe to a file", new Runnable() {
 					@Override
 					public void run() {
 						writePipeConfigToFile();
 					}
 				});
 		btnLoad = lay.addButton(Button.LoadFrom,
 				"Load a previously saved pipe from a file", new Runnable() {
 					@Override
 					public void run() {
 						readPipeConfigFromFile();
 					}
 				});
 	}
 
 	public void updatePipeLabel() {
 		String fn = Pipe.the().getLastSaveFile().getName();
 		if (fn.contains("."))
 			fn = ": \"" + fn.substring(0, fn.lastIndexOf('.')) + "\"";
 		pipeLabel.setText(String.format(
 				"Pipe has %d input%s, %d converter and %d output%s%s", Pipe
 						.the().getInputList().size(), Pipe.the().getInputList()
 						.size() == 1 ? "" : "s", Pipe.the().getConverterList()
 						.size(), Pipe.the().getOutputList().size(), Pipe.the()
 						.getOutputList().size() == 1 ? "" : "s", fn));
 	}
 
 	public void changeConfig() {
 		Log.detail("GUI-Frame starts configuration");
		if (cfgDialog == null)
			cfgDialog = new PipeConfigDialog();
		else
			cfgDialog.toFront();
 	}
 
 	public void writePipeConfigToFile() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileNameExtensionFilter(
 				"Pipe-Configuration", "pca"));
 		fc.setSelectedFile(Pipe.the().getLastSaveFile());
 		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
 			File file = fc.getSelectedFile();
 			if (!file.getName().contains("."))
 				file = new File(file.getPath() + ".pca");
 			writePipeConfigToFile(file);
 		}
 	}
 
 	public void writePipeConfigToFile(final File file) {
 		try {
 			Configuration.the().writeToFile(file, Pipe.the());
 			Pipe.the().setLastSaveFile(file);
 			updatePipeLabel();
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		}
 	}
 
 	public void readPipeConfigFromFile() {
 		final JFileChooser fc = new JFileChooser();
 		fc.setAcceptAllFileFilterUsed(false);
 		fc.addChoosableFileFilter(new FileNameExtensionFilter(
 				"Pipe-Configuration", "pca"));
 		fc.setSelectedFile(Pipe.the().getLastSaveFile());
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 			readPipeConfigFromFile(fc.getSelectedFile());
 	}
 
 	public void readPipeConfigFromFile(final File file) {
 		try {
 			Configuration.the().readFromFile(file, Pipe.the());
 			Configuration.the().writeToDefaultFile();
 			Pipe.the().setLastSaveFile(file);
 			updateState();
 			updatePipeLabel();
 		} catch (final ConfigurationException e) {
 			Log.error(e);
 		}
 	}
 
 	public void updateState() {
 		btnModify.setEnabled(!MainFrame.the().isPipeRunning());
 		btnSave.setEnabled(Pipe.the().getInputList().isEmpty()
 				|| !Pipe.the().getConverterList().isEmpty()
 				|| !Pipe.the().getOutputList().isEmpty());
 		btnLoad.setEnabled(!MainFrame.the().isPipeRunning());
 		if (cfgDialog != null) cfgDialog.updateState();
 	}
 
 	void configDialogDisposed() {
 		cfgDialog = null;
 	}
 
 }
