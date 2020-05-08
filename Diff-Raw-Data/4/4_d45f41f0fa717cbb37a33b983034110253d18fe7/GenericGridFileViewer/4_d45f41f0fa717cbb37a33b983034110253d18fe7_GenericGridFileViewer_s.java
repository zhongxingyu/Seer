 package org.vpac.grisu.frontend.view.swing.files.preview;
 
 import java.awt.CardLayout;
 import java.io.File;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import net.sf.jmimemagic.Magic;
 import net.sf.jmimemagic.MagicMatch;
 
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.frontend.control.clientexceptions.FileTransactionException;
 import org.vpac.grisu.frontend.view.swing.files.GridFileListListener;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.dto.GridFile;
 
 public class GenericGridFileViewer extends JPanel implements GridFileViewer,
 		GridFileListListener {
 
 	private static Set<String> viewers = null;
 
 	private static GridFileViewer createViewerPanel(File currentLocalCacheFile) {
 
 		// if ( )
 
 		final Magic parser = new Magic();
 		MagicMatch match = null;
 		try {
 			match = Magic.getMagicMatch(currentLocalCacheFile, true);
 			System.out.println(match.getMimeType());
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 
 		final Set<String> viewers = findViewers();
 		for (final String f : viewers) {
 
 			try {
 				final GridFileViewer viewerClass = (GridFileViewer) (Class
 						.forName(f).newInstance());
 
 				for (final String t : viewerClass.getSupportedMimeTypes()) {
 					if (match.getMimeType().contains(t)) {
 						return viewerClass;
 					}
 				}
 			} catch (final Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		// // if null, try known ones
 		// FileViewer viewer = new PlainTextFileViewer();
 		// for (String t : viewer.getSupportedMimeTypes()) {
 		// if (match.getMimeType().contains(t)) {
 		// return viewer;
 		// }
 		// }
 
 		// viewer = new ImageFileViewer();
 		// for (String t : viewer.getSupportedMimeTypes()) {
 		// if (match.getMimeType().contains(t)) {
 		// return viewer;
 		// }
 		// }
 
 		return null;
 	}
 
 	public static Set<String> findViewers() {
 
 		if (viewers == null) {
 			viewers = new HashSet<String>();
 
 			final String pckgname = "org.vpac.grisu.frontend.view.swing.files.preview.fileViewers";
 			String name = new String(pckgname);
 			if (!name.startsWith("/")) {
 				name = "/" + name;
 			}
 			name = name.replace('.', '/');
 
 			// Get a File object for the package
 			final URL url = GenericGridFileViewer.class.getResource(name);
 			final File directory = new File(url.getFile());
 			// New code
 			// ======
 			if (directory.exists()) {
 				// Get the list of the files contained in the package
 				final String[] files = directory.list();
 				for (final String file : files) {
 
 					// we are only interested in .class files
 					if (file.endsWith(".class")) {
 						// removes the .class extension
 						final String classname = file.substring(0,
 								file.length() - 6);
 						try {
 							// Try to create an instance of the object
 							final Object o = Class.forName(
 									pckgname + "." + classname).newInstance();
 							if (o instanceof GridFileViewer) {
 								viewers.add(pckgname + "." + classname);
 							}
 						} catch (final ClassNotFoundException cnfex) {
 							System.err.println(cnfex);
 						} catch (final InstantiationException iex) {
 							// We try to instantiate an interface
 							// or an object that does not have a
 							// default constructor
 						} catch (final IllegalAccessException iaex) {
 							// The class is not public
 						}
 					}
 				}
 			}
 		}
 
 		if ((viewers == null) || (viewers.size() == 0)) {
 			viewers = new HashSet<String>();
			viewers.add("org.vpac.grisu.frontend.view.swing.files.preview.fileViewers.PlainTextFileViewer");
			viewers.add("org.vpac.grisu.frontend.view.swing.files.preview.fileViewers.ImageFileViewer");
 		}
 		return viewers;
 	}
 
 	private ServiceInterface si;
 	private FileManager fm;
 
 	private boolean showsValidViewerAtTheMoment = false;
 
 	private File currentLocalCacheFile = null;
 
 	private GridFile currentGlazedFile = null;
 
 	private final JPanel emptyPanel = new JPanel();
 	private final String EMPTY_PANEL = "__empty__";
 
 	/**
 	 * Create the panel.
 	 */
 	public GenericGridFileViewer() {
 
 		setLayout(new CardLayout());
 
 		add(emptyPanel, EMPTY_PANEL);
 	}
 
 	public void directoryChanged(GridFile newDirectory) {
 		// TODO
 	}
 
 	public void fileDoubleClicked(GridFile file) {
 
 		setFile(file, null);
 	}
 
 	public void filesSelected(Set<GridFile> files) {
 
 	}
 
 	public JPanel getPanel() {
 		return this;
 	}
 
 	public String[] getSupportedMimeTypes() {
 		return new String[] { "*" };
 	}
 
 	public void isLoading(boolean loading) {
 
 	}
 
 	private void setEmptyPanel() {
 		SwingUtilities.invokeLater(new Thread() {
 
 			@Override
 			public void run() {
 				final CardLayout cl = (CardLayout) (getLayout());
 				cl.show(GenericGridFileViewer.this, EMPTY_PANEL);
 
 				revalidate();
 			}
 
 		});
 	}
 
 	public void setFile(GridFile file, File localCacheFile) {
 
 		currentGlazedFile = file;
 		if ((localCacheFile != null) && localCacheFile.exists()) {
 			currentLocalCacheFile = localCacheFile;
 		} else {
 			try {
 				if (fm.upToDateLocalCacheFileExists(file.getUrl())) {
 					currentLocalCacheFile = fm.getLocalCacheFile(file.getUrl());
 				} else {
 					if (fm.isBiggerThanThreshold(file.getUrl())) {
 
 						int n = JOptionPane
 								.showConfirmDialog(
 										getRootPane(),
 										"The file you selected is bigger than the default threshold\n"
 												+ FileManager
 														.calculateSizeString(FileManager
 																.getDownloadFileSizeThreshold())
 												+ "bytes. It may take a long time to load.\n"
 												+ "Do you still want to preview that file?",
 										"Warning: big file",
 										JOptionPane.YES_NO_OPTION);
 
 						if (n == JOptionPane.NO_OPTION) {
 							showsValidViewerAtTheMoment = false;
 							return;
 						}
 					}
 
 					currentLocalCacheFile = fm.downloadFile(file.getUrl());
 				}
 
 			} catch (final RemoteFileSystemException e) {
 				e.printStackTrace();
 			} catch (final FileTransactionException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		setEmptyPanel();
 
 		if (!currentLocalCacheFile.exists()) {
 			showsValidViewerAtTheMoment = false;
 			return;
 		} else if (currentLocalCacheFile.length() == 0L) {
 			showsValidViewerAtTheMoment = true;
 			return;
 		}
 
 		final GridFileViewer viewer = createViewerPanel(currentLocalCacheFile);
 
 		if (viewer != null) {
 			SwingUtilities.invokeLater(new Thread() {
 
 				@Override
 				public void run() {
 					viewer.setFile(currentGlazedFile, currentLocalCacheFile);
 					add(viewer.getPanel(), currentLocalCacheFile.toString());
 
 					final CardLayout cl = (CardLayout) (getLayout());
 					cl.show(GenericGridFileViewer.this,
 							currentLocalCacheFile.toString());
 
 					revalidate();
 				}
 
 			});
 
 			showsValidViewerAtTheMoment = true;
 
 		} else {
 			showsValidViewerAtTheMoment = true;
 			// TODO set no viewer found...
 			setEmptyPanel();
 		}
 
 	}
 
 	public void setServiceInterface(ServiceInterface si) {
 		this.si = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 	}
 
 	public boolean showsValidPreviewCurrently() {
 		return showsValidViewerAtTheMoment;
 	}
 }
