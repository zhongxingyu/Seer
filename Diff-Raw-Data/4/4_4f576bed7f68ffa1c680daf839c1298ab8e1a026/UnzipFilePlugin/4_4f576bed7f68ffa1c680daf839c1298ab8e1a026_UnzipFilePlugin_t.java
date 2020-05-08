 package cz.zcu.kiv.kc.plugin.unzip;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import javax.swing.SwingWorker.StateValue;
 
 import cz.zcu.kiv.kc.plugin.AbstractPlugin;
 import cz.zcu.kiv.kc.plugin.I18N;
 
 /**
  * UnZip decompression plug-in. Executes compression in separate SwingWorker thread. Provides progress dialog and cancellation.
  * @author Michal
  *
  */
 public class UnzipFilePlugin extends AbstractPlugin implements PropertyChangeListener
 {
 	private class DecompressionTask extends SwingWorker<Void, Void>
 	{
 		private File fileToDecompress;
 		private File destinationDirectory;
 		
 		public DecompressionTask(
 			File fileToDecompress,
 			File destinationDirectory
 		)
 		{			
 			this.fileToDecompress = fileToDecompress;
 			this.destinationDirectory = destinationDirectory;
 		}
 		
 		@Override
 		protected Void doInBackground() throws Exception
 		{
 			this.exec(
 				this.fileToDecompress,
 				this.destinationDirectory.getAbsolutePath()
 			);
 			return null;
 		}
 
 		private void exec(
 			File fileToDecompress,
 			String destinationPath)
 		{
 			byte[] buffer = new byte[1024];
 			try (ZipFile zif = new ZipFile(fileToDecompress))
 			{
 				int totalEntries = zif.size();
 				int processedEntries = 0;
 				Enumeration<? extends ZipEntry> entries = zif.entries();
 				while (entries.hasMoreElements() && !this.isCancelled())
 				{
 					ZipEntry entry = entries.nextElement();
 					try (InputStream is = zif.getInputStream(entry))
 					{
 						File newFile = new File(destinationPath + File.separator + entry.getName());
 						if (entry.isDirectory())
 						{ // entry is directory, create and continue with other entry
 							newFile.mkdirs();
 						}
 						else
 						{ // entry is file, create parent directories and write content
 							if (newFile.exists())
 							{ // trying to overwrite existing file
 								int res = JOptionPane.showConfirmDialog(
 									UnzipFilePlugin.this.mainWindow,
									I18N.getText("overwriteExisting", newFile),
									I18N.getText("overwriteExistingTitle"),
 									JOptionPane.YES_NO_CANCEL_OPTION
 								);
 								if (res == JOptionPane.NO_OPTION)
 								{ // skip current file
 									continue;
 								}
 								else if (res == JOptionPane.CANCEL_OPTION)
 								{ // cancel whole operation
 									break;
 								}
 							}
 							
 							File parentDirs = new File(newFile.getParent());
 							parentDirs.mkdirs();
 							this.firePropertyChange("file", null, newFile);
 							try (FileOutputStream fos = new FileOutputStream(newFile))
 							{
 								int len;
 								while ((len = is.read(buffer)) > 0)
 								{
 									fos.write(buffer, 0, len);
 								}
 							}
 							catch (FileNotFoundException ex)
 							{ // should not occur
 								System.out.println("chyba zapisu: " + ex.getMessage());
 							}
 						}
 						this.setProgress((int) ( (float) ++processedEntries / totalEntries * 100) );
 					}
 				}
 			}
 			catch (ZipException e) { }
 			catch (IOException e) { }
 			finally
 			{
 				this.firePropertyChange("done", false, true);
 			}
 		}
 	}
 
 	JDialog progressDialog = new JDialog(this.mainWindow);
 	JProgressBar pb = new JProgressBar();
 	JLabel jl = new JLabel(I18N.getText("status") + " ");
 	DecompressionTask worker = null;
 	String destinationPath;
 	boolean canceled = false;
 	
 	public UnzipFilePlugin()
 	{
 		super();
 		UIManager.put("ClassLoader", getClass().getClassLoader());
 	}
 
 	
 	private Action exitAction = new AbstractAction()
 	{
 		private static final long serialVersionUID = 5409574734727190161L;
 
 		{
 			putValue(NAME, I18N.getText("cancelTitle"));
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (UnzipFilePlugin.this.worker == null) return;
 			
 			if (!UnzipFilePlugin.this.worker.isDone())
 			{
 				int res = JOptionPane.showConfirmDialog(
 					UnzipFilePlugin.this.mainWindow,
 					I18N.getText("cancelCurrentOperation"),
 					I18N.getText("cancelCurrentOperationTitle"),
 					JOptionPane.YES_NO_OPTION
 				);
 				if (res == JOptionPane.NO_OPTION)
 				{
 					return;
 				}
 				else
 				{
 					System.out.println("canceled: " + UnzipFilePlugin.this.worker.cancel(true));
 				}
 			}
 			UnzipFilePlugin.this.canceled = true;
 			UnzipFilePlugin.this.progressDialog.dispose();			
 		}
 	};
 	
 	@Override
 	public void executeAction(
 		List<File> selectedFiles,
 		String destinationPath,
 		String sourcePath)
 	{
 		this.destinationPath = destinationPath;
 		
 		for (File file : selectedFiles)
 		{
 			if (this.canceled) break;
 			
 			destinationPath = JOptionPane.showInputDialog(
 				UnzipFilePlugin.this.mainWindow,
 				I18N.getText("targetFolder"),
 				destinationPath
 				+ (destinationPath.endsWith(File.separator) ? "" : File.separator)
 				+ file.getName().substring(0, file.getName().lastIndexOf('.'))
 			);
 			if (destinationPath == null || destinationPath.trim().isEmpty())
 			{
 				JOptionPane.showMessageDialog(
 					UnzipFilePlugin.this.mainWindow,
 					I18N.getText("canceledByUser")
 				);
 				return;
 			}
 			
 			this.jl.setPreferredSize(new Dimension(480, -1));
 			this.pb.setStringPainted(true);
 			
 			JPanel panel = new JPanel(new GridLayout(3, 1));
 			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 			panel.add(this.jl);
 			panel.add(this.pb);
 			panel.add(new JButton(this.exitAction));
 			
 			this.progressDialog.add(panel);
 			this.progressDialog.pack();
 			this.progressDialog.setResizable(false);
 			this.progressDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 			this.progressDialog.addWindowListener(
 				new WindowAdapter()
 				{
 					@Override
 					 public void windowClosing(java.awt.event.WindowEvent arg0)
 					{
 						UnzipFilePlugin.this.exitAction.actionPerformed(null);
 					}
 				}
 			);
 			
 			this.worker = new DecompressionTask(
 				file,
 				new File(destinationPath)
 			);
 			worker.addPropertyChangeListener(this);
 			worker.execute();
 			
 			this.progressDialog.setVisible(true);
 		}
 	}
 
 	@Override
 	public String getName() {
 		return "unzip";
 	}
 	
 	@Override
 	public void propertyChange(PropertyChangeEvent evt)
 	{
 		if (evt.getPropertyName() == "state" && evt.getNewValue() == StateValue.STARTED)
 		{
 			this.pb.setValue(0);
 		}
 		if (evt.getPropertyName() == "file")
 		{
 			this.jl.setText(evt.getNewValue().toString() + I18N.getText("extracted"));
 		}
 		if (evt.getPropertyName() == "progress")
 		{
 			this.pb.setValue((int) evt.getNewValue());
 		}
 		if (evt.getPropertyName() == "done")
 		{
 			System.out.println("refreshing: " + this.destinationPath);
 			this.sendEvent(destinationPath);
 			this.progressDialog.dispose();
 		}
 	}
 
 }
