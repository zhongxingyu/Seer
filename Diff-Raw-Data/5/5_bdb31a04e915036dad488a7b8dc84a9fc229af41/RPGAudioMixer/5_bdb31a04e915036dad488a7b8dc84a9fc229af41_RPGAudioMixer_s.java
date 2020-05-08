 package com.rpgaudiomixer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import com.rpgaudiomixer.audioengine.AudioEngine;
 import com.rpgaudiomixer.audioengine.AudioEngineListener;
 import com.rpgaudiomixer.audioengine.JavaZoomAudioEngine;
 import com.rpgaudiomixer.model.Adventure;
 import com.rpgaudiomixer.model.AliasCollector;
 import com.rpgaudiomixer.model.Folder;
 import com.rpgaudiomixer.model.IResource;
 import com.rpgaudiomixer.model.Palette;
 import com.rpgaudiomixer.model.Playlist;
 import com.rpgaudiomixer.model.Alias;
 import com.rpgaudiomixer.util.DirectoryFilter;
 import com.rpgaudiomixer.util.FilenameExtensionFilter;
 
 import com.thoughtworks.xstream.XStream;
 
 public class RPGAudioMixer extends ApplicationWindow implements AudioEngineListener {
	
 	public static void main(String[] args) {
 		RPGAudioMixer rpgam = new RPGAudioMixer();
 		rpgam.setBlockOnOpen(true);
 		rpgam.open();
 		Display.getCurrent().dispose();
 		rpgam.quit();
 	}
 
 	private Action newFolderAction, deleteFolderAction, renameFolderAction;
 	private Action newPlaylistAction, playPlaylistAction, deletePlaylistAction, renamePlaylistAction;
 	private Action newPaletteAction, deletePaletteAction, renamePaletteAction;
 	
 	private Action newSongAction;
 	private Action playSongAction;
 	private Action deleteSongAction;
 	private Action renameSongAction;
 	private Action moveSongDownAction;
 	private Action moveSongUpAction;
 
 	private Action playEffectAction, newEffectAction, renameEffectAction, deleteEffectAction;
 	
 	private Action directoryAddPlaylistAction, directoryAddPaletteAction;
 	private Action fileAddPlaylistAction, fileAddPaletteAction;
 	private Action filePreviewAction;
 
 	private Action newAdventureAction, openAdventureAction, saveAdventureAction, saveAdventureAsAction, closeAdventureAction, exitAction;
 	
 	private Adventure adventure;
 	private Alias activeSong;
 	private AudioEngine audioEngine;
 	private boolean bDirtyData;
 	private FileDialog saveAdventureDialog, openFileDialog, songDialog;
 	private FilenameExtensionFilter audioFileFilter = new FilenameExtensionFilter(new String[] {"wav", "mp3", "ogg"});
 	private Group explorerComposite, paletteComposite, playlistComposite, resourceComposite;
 	private Image folderImage, playlistImage, paletteImage;
 	private InputDialog renameDialog;
 	private IResource selectedResource;
 	private MenuManager directoryPopupMenuManager;
 	private MenuManager effectTableContextMenu;
 	private MenuManager filePopupMenuManager;
 	private MenuManager folderContextMenu, playlistContextMenu, paletteContextMenu, songTableContextMenu;
 	private MenuManager newMenu;
 	private MessageBox confirmDialog;
 	private Palette selectedPalette;
 	private Playlist activePlaylist, selectedPlaylist;
 	private SashForm explorerSash;
 	private Scale songVolumeScale;
 	private String adventurePath;
 	private String[] audioExtensions = new String[] {"*.mp3;*.wav;*.ogg","*.wav","*.mp3","*.ogg"};
 	private String[] rpgamExtension = new String[] {"*.raa"};
 	private Table effectTable;
 	private Table songTable;
 	private TableViewer effectTableViewer, songTableViewer, fileViewer;
 	private Transfer[] fileFormat = new Transfer[] {FileTransfer.getInstance()};
 	private TreeViewer directoryViewer, resourceViewer;
 	private XStream xstream;
 	private Image deleteImage;
 	
 	public RPGAudioMixer() {
 		super(null);
 		
 		audioEngine = new JavaZoomAudioEngine();
 		audioEngine.init();
 		audioEngine.addAudioEngineListener(this);
 		
 		//adventure = new Adventure();
 
 		xstream = new XStream();
 		xstream.alias("adventure", Adventure.class);
 		xstream.alias("alias", Alias.class);
 		xstream.alias("folder", Folder.class);
 		xstream.alias("palette", Palette.class);
 		xstream.alias("playlist", Playlist.class);
 		createActions();
 		createContextMenus();
 
 		addMenuBar();
 	}
 	
 	private void createActions() {
 		// File
 		newAdventureAction = new Action("&New Adventure", IAction.AS_PUSH_BUTTON) {public void run () {newAdventure();}};
 		openAdventureAction = new Action("&Open Adventure", IAction.AS_PUSH_BUTTON) {public void run () {openAdventure();}};
 		saveAdventureAction = new Action("&Save Adventure", IAction.AS_PUSH_BUTTON) {public void run () {saveAdventure();}};
 		saveAdventureAsAction = new Action("Save Adventure &As", IAction.AS_PUSH_BUTTON) {public void run () {saveAdventureAs();}};
 		closeAdventureAction = new Action("&Close Adventure", IAction.AS_PUSH_BUTTON) {public void run () {closeAdventure();}};
 		exitAction = new Action("E&xit", IAction.AS_PUSH_BUTTON) {public void run () {quit();}};
 		
 		newAdventureAction.setAccelerator(SWT.CTRL + 'N');
 		openAdventureAction.setAccelerator(SWT.CTRL + 'O');
 		saveAdventureAction.setAccelerator(SWT.CTRL + 'S');
 		
 		newAdventureAction.setEnabled(true);
 		openAdventureAction.setEnabled(true);
 		saveAdventureAction.setEnabled(false);
 		saveAdventureAsAction.setEnabled(false);
 		closeAdventureAction.setEnabled(false);
 		exitAction.setEnabled(true);
 		
 		// Resources
 		newFolderAction = new Action("Folder", IAction.AS_PUSH_BUTTON) {public void run() {newFolder();}};
 		newPlaylistAction = new Action("Playlist", IAction.AS_PUSH_BUTTON) {public void run() {newPlaylist();}};
 		newPaletteAction = new Action("Palette", IAction.AS_PUSH_BUTTON) {public void run() {newPalette();}};
 		
 		// Resource - Folder
 		renameFolderAction = new Action("Rename", IAction.AS_PUSH_BUTTON) {public void run() {renameFolder();}};
 		deleteFolderAction = new Action("Delete", IAction.AS_PUSH_BUTTON) {public void run() {deleteSelectedFolder();}};
 
 		// Resource - Playlist
 		playPlaylistAction = new Action("Play", IAction.AS_PUSH_BUTTON) {public void run() {activatePlaylist();}};
 		renamePlaylistAction = new Action("Rename", IAction.AS_PUSH_BUTTON) {public void run() {renamePlaylist();}};
 		deletePlaylistAction = new Action("Delete", IAction.AS_PUSH_BUTTON) {public void run() {deleteSelectedPlaylist();}};
 
 		// Resource - Palette
 		renamePaletteAction = new Action("Rename", IAction.AS_PUSH_BUTTON) {public void run() {renamePalette();}};
 		deletePaletteAction = new Action("Delete", IAction.AS_PUSH_BUTTON) {public void run() {deleteSelectedPalette();}};
 
 		// Song
 		playSongAction = new Action("Play", IAction.AS_PUSH_BUTTON) {public void run() {playSelectedSong();}};
 		newSongAction = new Action("New", IAction.AS_PUSH_BUTTON) {public void run() {newSong();}};
 		renameSongAction = new Action("Rename", IAction.AS_PUSH_BUTTON) {public void run() {renameSelectedSongs();}};
 		deleteSongAction = new Action("Delete", IAction.AS_PUSH_BUTTON) {public void run() {deleteSelectedSongs();}};
 		moveSongUpAction = new Action("Move Up", IAction.AS_PUSH_BUTTON) {public void run() {moveSongUp();}};
 		moveSongDownAction = new Action("Move Down", IAction.AS_PUSH_BUTTON) {public void run() {moveSongDown();}};
 
 		/*
 		moveSongUpAction.setAccelerator(SWT.ALT + SWT.ARROW_UP);
 		moveSongDownAction.setAccelerator(SWT.ALT + SWT.ARROW_DOWN);
 		*/
 
 		// Effect
 		playEffectAction = new Action("Play", IAction.AS_PUSH_BUTTON) {public void run() {playSelectedEffect();}};
 		newEffectAction = new Action("New", IAction.AS_PUSH_BUTTON) {public void run() {newEffect();}};
 		renameEffectAction = new Action("Rename", IAction.AS_PUSH_BUTTON) {public void run() {renameSelectedEffects();}};
 		deleteEffectAction = new Action("Delete", IAction.AS_PUSH_BUTTON) {public void run() {deleteSelectedEffects();}};
 
 		// Audio Explorer - Directory
 		directoryAddPlaylistAction = new Action("Add Files To Playlist", IAction.AS_PUSH_BUTTON) {public void run () {directoryAddPlaylist();}};
 		directoryAddPaletteAction = new Action("Add Files To Palette", IAction.AS_PUSH_BUTTON) {public void run () {directoryAddPalette();}};
 
 		// Audio Explorer - File
 		fileAddPlaylistAction = new Action("Add to Playlist", IAction.AS_PUSH_BUTTON) {public void run () {fileAddPlaylist();}};
 		fileAddPaletteAction = new Action("Add to Palette", IAction.AS_PUSH_BUTTON) {public void run () {fileAddPalette();}};
 		filePreviewAction = new Action("Preview", IAction.AS_PUSH_BUTTON) {public void run () {filePreview();}};
 
 		filePreviewAction.setEnabled(false);
 		fileAddPlaylistAction.setEnabled(false);
 		fileAddPaletteAction.setEnabled(false);
 		
 	}
 
 	// UI Creation Methods
 	
 	protected void stopSong() {
 		audioEngine.stopSong();
 	}
 
 	protected Control createContents(Composite parent) {
 		getShell().setText("RPG Audio Mixer");
 		parent.setSize(800,600);
 		parent.setLocation(16,16);
 
 		createDialogs();
 
 		folderImage = new Image(getShell().getDisplay(), "icons/folder.gif");
 		playlistImage = new Image(getShell().getDisplay(), "icons/playlist.gif");
 		paletteImage = new Image(getShell().getDisplay(), "icons/palette.gif");
 		deleteImage = new Image(getShell().getDisplay(), "icons/delete.gif");
 
 		newFolderAction.setImageDescriptor(ImageDescriptor.createFromImage(getFolderImage()));
 		newPlaylistAction.setImageDescriptor(ImageDescriptor.createFromImage(getPlaylistImage()));
 		newPaletteAction.setImageDescriptor(ImageDescriptor.createFromImage(getPaletteImage()));
 
 		ImageDescriptor deleteImageDescriptor = ImageDescriptor.createFromImage(deleteImage);
 		deleteFolderAction.setImageDescriptor(deleteImageDescriptor);
 		deletePlaylistAction.setImageDescriptor(deleteImageDescriptor);
 		deletePaletteAction.setImageDescriptor(deleteImageDescriptor);
 		deleteSongAction.setImageDescriptor(deleteImageDescriptor);
 		deleteEffectAction.setImageDescriptor(deleteImageDescriptor);
 		
 		SashForm mainSash = new SashForm(parent, SWT.HORIZONTAL);
 		mainSash.SASH_WIDTH = 4;
 		
 		// Resource Viewer
 		resourceComposite = new Group(mainSash, SWT.SHADOW_NONE);
 		resourceComposite.setLayout(new FillLayout());
 		resourceComposite.setText("Resources");
 		createResourceViewer(resourceComposite);
 		
 		// Middle Column
 		Composite middleComposite = new Composite(mainSash, SWT.NULL);
 		middleComposite.setLayout(new FillLayout(SWT.VERTICAL));
 
 		// Middle Column - Playlist Viewer
 		playlistComposite = new Group(middleComposite, SWT.SHADOW_NONE);
 		playlistComposite.setLayout(new FillLayout());
 		playlistComposite.setText("Playlist");
 		createPlaylistViewer(playlistComposite);
 
 		// Middle Column - Song Player
 		Group playerComposite = new Group(middleComposite, SWT.SHADOW_NONE);
 		playerComposite.setLayout(new FillLayout());
 		playerComposite.setText("Song Player");
 		createSongPlayer(playerComposite);
 		
 		// Middle Column - Palette Viewer
 		paletteComposite = new Group(middleComposite, SWT.SHADOW_NONE);
 		paletteComposite.setLayout(new FillLayout());
 		paletteComposite.setText("Palette");
 		createPaletteViewer(paletteComposite);
 
 		// Audio Explorer
 		explorerComposite = new Group(mainSash, SWT.SHADOW_NONE);
 		explorerComposite.setLayout(new FillLayout());
 		explorerComposite.setText("Audio Explorer");
 		createAudioExplorer(explorerComposite);
 
 		mainSash.setWeights(new int[] {20,60,20});
 		
 		return mainSash;
 	}
 
 	private void createSongPlayer(Composite c) {
 		Composite controlComposite = new Composite(c, SWT.NULL);
 		GridLayout controlLayout = new GridLayout();
 		controlLayout.numColumns = 7;
 		controlLayout.horizontalSpacing = 2;
 		controlLayout.verticalSpacing = 2;
 		controlLayout.marginHeight = 2;
 		controlLayout.marginWidth = 2;
 		controlComposite.setLayout(controlLayout);
 		Button playButton = new Button(controlComposite,SWT.PUSH);
 		playButton.setText("Play");
 		
 		Button stopButton = new Button(controlComposite,SWT.PUSH);
 		stopButton.setText("Stop");
 		stopButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent se) {
 				stopSong();
 			}
 		});
 		
 		Button nextButton = new Button(controlComposite, SWT.PUSH);
 		nextButton.setText("Next");
 		nextButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent se) {
 				//nextSong();
 			}
 		});
 		
 		ProgressBar songProgressBar = new ProgressBar(controlComposite, SWT.SMOOTH);
 		songProgressBar.setMinimum(0);
 		songProgressBar.setMaximum(1000);
 		songProgressBar.setSelection(0);
 		
 		songVolumeScale = new Scale(controlComposite, SWT.HORIZONTAL);
 		songVolumeScale.setMinimum(0);
 		songVolumeScale.setMaximum(100);
 		songVolumeScale.setIncrement(1);
 		songVolumeScale.setPageIncrement(10);
 		songVolumeScale.setSelection(100);
 		songVolumeScale.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event e) {
 				audioEngine.setSongVolume(songVolumeScale.getSelection());
 			}
 		});
 	}
 
 	private void createAudioExplorer(Composite c) {
 		explorerSash = new SashForm(c, SWT.VERTICAL);
 
 		// Drive List
 		ListViewer driveViewer = new ListViewer(explorerSash);
 		
 		// Drive data comes from file system roots and never changes
 		driveViewer.setContentProvider(new IStructuredContentProvider() {
 			public void dispose() {}
 			public Object[] getElements(Object arg0) {
 				return File.listRoots();
 			}
 			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}
 		});
 		
 		driveViewer.setLabelProvider(new LabelProvider() {
 	    	public String getText(Object element) {
 	    		return ((File) element).getPath();
 	    	}
 	    });
 
 		// Drive Viewer informs directory viewer of changes
 		driveViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 	    	public void selectionChanged(SelectionChangedEvent event) {
 		        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 		        if (selection.size() > 0) {   	
 			        Object selectedFile = selection.getFirstElement();
 			        directoryViewer.setInput(selectedFile);
 
 		        } else {
 		        	directoryViewer.setInput(null);
 		        	
 		        }
 	    	}
 	    });
 
 		driveViewer.setInput(File.listRoots());
 		
 		// Directory Tree
 		directoryViewer = new TreeViewer(explorerSash);
 		
 		// Directory viewer gets its info from currently selected drives
 		directoryViewer.setContentProvider(new ITreeContentProvider() {
 			public void dispose() {}
 
 			public Object[] getChildren(Object element) {
 				Object[] children = ((File) element).listFiles(new DirectoryFilter());
 				return children == null ? new Object[0] : children;
 			}
 
 			public Object[] getElements(Object element) {
 				return getChildren(element);
 			}
 
 			public Object getParent(Object element) {
 				return ((File)element).getParent();
 			}
 			public boolean hasChildren(Object element) {
 				return getChildren(element).length > 0;
 			}
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 		});
 		
 	    directoryViewer.setLabelProvider(new LabelProvider() {
 	    	public String getText(Object element) {
 	    		return ((File) element).getName();
 	    	}
 	    });
 
 		// Directory Viewer informs file viewer of changes
 		directoryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 	    	public void selectionChanged(SelectionChangedEvent event) {
 		        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 
 		        if (selection.size() > 0) {   	
 			        Object selected_file = selection.getFirstElement();
 			        fileViewer.setInput(selected_file);
 			        
 			        //directoryCreatePlaylistAction.setEnabled(true);
 			        //directoryCreatePaletteAction.setEnabled(true);
 			        
 		        } else {
 		        	fileViewer.setInput(null);
 
 			        //directoryCreatePlaylistAction.setEnabled(false);
 			        //directoryCreatePlaylistAction.setEnabled(false);
 		        	
 		        }
 		        
 	    	}
 	    });
 
 		directoryViewer.addDragSupport(DND.DROP_COPY, fileFormat, new DragSourceListener() {
 			public void dragFinished(DragSourceEvent dse) {}
 			public void dragStart(DragSourceEvent dse) {}		
 
 			public void dragSetData(DragSourceEvent dse) {
 				if (FileTransfer.getInstance().isSupportedType(dse.dataType)) {
 					IStructuredSelection selection = (IStructuredSelection) directoryViewer.getSelection();
 					File selectedDirectory = (File) selection.getFirstElement();
 					File[] paths = selectedDirectory.listFiles(audioFileFilter);
 
 					String[] files = new String[paths.length];
 					for (int i = 0; i < paths.length; i++) {
 						files[i] = paths[i].getAbsolutePath();
 					}
 					
 					dse.data = files;
 					dse.doit = true;
 
 				} else {
 					dse.doit = false;
 
 				}	
 			}
 		});
 		
 	    // Directory Viewer popup context menu
 		directoryViewer.getTree().setMenu(directoryPopupMenuManager.createContextMenu(directoryViewer.getTree()));
 
 	    directoryViewer.setInput(File.listRoots()[0]);
 
 	    // File List
 		fileViewer = new TableViewer(explorerSash, SWT.BORDER | SWT.MULTI);
 
 		// File Viewer gets content from currently selected directory
 		fileViewer.setContentProvider(new IStructuredContentProvider() {
 			public void dispose() {}
 
 			public Object[] getElements(Object element) {
 				Object[] children = null;
 				children = ((File) element).listFiles(audioFileFilter);		
 				return children == null ? new Object[0] : children;
 			}
 			public void inputChanged(Viewer viewer, Object oldObject, Object newObject) {}
 
 		});
 		
 		fileViewer.setLabelProvider(new ITableLabelProvider() {
 			  public void addListener(ILabelProviderListener ilabelproviderlistener) {}
 
 			  public void dispose() {}
 
 			  public Image getColumnImage(Object arg0, int arg1) {
 			    return null;
 			  }
 
 			  public String getColumnText(Object obj, int i) {
 			    return ((File) obj).getName();
 			  }
 
 			  public boolean isLabelProperty(Object obj, String s) {
 			  	return false;
 			  }
 			  
 			  public void removeListener(ILabelProviderListener ilabelproviderlistener) {}
 		});
 
 		// File Viewer popup context menu
 		fileViewer.getTable().setMenu(filePopupMenuManager.createContextMenu(fileViewer.getTable()));
 
 		fileViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 				if (selection.size() > 0) {
 					// Only allow previewing of single files
 					if (selection.size() == 1) {
 						filePreviewAction.setEnabled(true);
 					} else {
 						filePreviewAction.setEnabled(false);
 					}
 
 					// TODO: Also need to check if a Playlist/Palette is open
 					if (selectedPlaylist != null) {
 						fileAddPlaylistAction.setEnabled(true);
 					}
 					
 					if (selectedPalette != null) {
 						fileAddPaletteAction.setEnabled(true);
 					}
 					
 				} else {
 					filePreviewAction.setEnabled(false);
 					fileAddPlaylistAction.setEnabled(false);
 					fileAddPaletteAction.setEnabled(false);
 
 				}
 			}
 		});
 	
 		// File Viewer informs application of double-clicked files
 		fileViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 				File selectedFile = (File) selection.getFirstElement();
 				previewSong(selectedFile);
 			}
 		});
 
 		// File Viewer drag and drop support
 		fileViewer.addDragSupport(DND.DROP_COPY, fileFormat, new DragSourceListener() {
 			public void dragFinished(DragSourceEvent dse) {}
 			public void dragStart(DragSourceEvent dse) {}		
 
 			public void dragSetData(DragSourceEvent dse) {
 				if (FileTransfer.getInstance().isSupportedType(dse.dataType)) {
 					IStructuredSelection selection = (IStructuredSelection) fileViewer.getSelection();
 					Object[] items = selection.toArray();
 					String[] paths = new String[items.length];
 					for (int i = 0; i < items.length; i++) {
 						paths[i] = ((File) items[i]).getAbsolutePath();
 					}
 					dse.data = paths;
 					dse.doit = true;
 
 				} else {
 					dse.doit = false;
 
 				}	
 			}
 		});
 		
 		explorerSash.setWeights(new int[] {20,40,40});
 
 	}
 
 	private void createDialogs() {
 		saveAdventureDialog = new FileDialog(getShell(), SWT.SAVE | SWT.SINGLE);
 		saveAdventureDialog.setFilterExtensions(rpgamExtension);
 
 		openFileDialog =  new FileDialog(getShell(), SWT.SINGLE);
 		openFileDialog.setFilterExtensions(rpgamExtension);
 
 		songDialog = new FileDialog(getShell(), SWT.MULTI);
 		songDialog.setFilterExtensions(audioExtensions);
 		
 		confirmDialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
 		confirmDialog.setText("RPG Audio Mixer");
 	}
 
 	private void createPaletteViewer(Composite c) {
 		effectTable = new Table(c, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
 		TableColumn tcEffectName = new TableColumn(effectTable, SWT.LEFT);
 		tcEffectName.setText("Name");
 		
 		TableColumn tcEffectPath = new TableColumn(effectTable, SWT.LEFT);
 		tcEffectPath.setText("Path");
 		
 		tcEffectName.setWidth(192);
 		tcEffectPath.setWidth(256);
 		
 		effectTableViewer = new TableViewer(effectTable);
 		effectTableViewer.getTable().setLinesVisible(true);
 		effectTableViewer.getTable().setHeaderVisible(true);
 		effectTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
 		effectTableViewer.setSorter(new ViewerSorter() {
 			public int compare(Viewer viewer, Object a, Object b) {
 				return ((Alias)a).getName().compareTo(((Alias)b).getName());
 			}
 		});
 
 		effectTableViewer.setContentProvider(new IStructuredContentProvider() {
 			public Object[] getElements(Object inputElement) {
 				return ((Palette) inputElement).getEffects().toArray();
 			}
 			public void dispose() {}
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 		});
 
 		effectTableViewer.setLabelProvider(new ITableLabelProvider() {
 			public Image getColumnImage(Object element, int columnIndex) {
 				return null;
 			}
 
 			public String getColumnText(Object element, int columnIndex) {
 				Alias c = (Alias) element;
 				if (columnIndex == 0) {
 					String sName = c.getName();	
 					return sName;
 
 				} else if (columnIndex == 1) {
 					return c.getPath();
 					
 				} else { 
 					return c.getPath();
 
 				}
 			}
 
 			public void addListener(ILabelProviderListener listener) {}
 			public void dispose() {}
 			public boolean isLabelProperty(Object element, String property) {
 				return false;
 			}
 			public void removeListener(ILabelProviderListener listener) {}
 		});
 
 		// Effect Table Context Popup Menu
 		effectTableViewer.getTable().setMenu(effectTableContextMenu.createContextMenu(effectTableViewer.getTable()));
 		
 		effectTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 				if (selection.size() == 0) {
 					//renameEffectAction.setEnabled(false);
 					//deleteEffectAction.setEnabled(false);
 
 				} else {
 					//renameEffectAction.setEnabled(true);
 					//deleteEffectAction.setEnabled(true);
 
 				}
 
 			}
 		});
 
 		effectTableViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
 				Alias a = (Alias) selection.getFirstElement();
 				activateEffect(a);				
 			}
 		});
 
 		effectTableViewer.addDropSupport(DND.DROP_COPY, fileFormat, new DropTargetListener () {
 			public void dragEnter(DropTargetEvent dte) {
 				dte.detail = DND.DROP_COPY;
 				
 			}
 			
 			public void drop(DropTargetEvent dte) {				
 				try {
 					if (FileTransfer.getInstance().isSupportedType(dte.currentDataType)) {
 						String[] paths = (String[]) dte.data;						
 						addFiles(paths, effectTableViewer, selectedPalette);
 					}
 				} catch (Exception e) {
 					System.out.println(e.toString());
 				}
 			}
 
 			public void dragLeave(DropTargetEvent dte) {}
 			public void dragOperationChanged(DropTargetEvent dte) {}
 			public void dragOver(DropTargetEvent dte) {}
 			public void dropAccept(DropTargetEvent dte) {}
 		});
 		
 		effectTable.addKeyListener(new KeyListener() {
 
 			public void keyPressed(KeyEvent e) {
 				// TODO Auto-generated method stub
 				if (e.keyCode == SWT.F2) {
 					renameSelectedEffects();
 				}
 				
 				if (e.keyCode == SWT.DEL) {
 					deleteSelectedEffects();
 				}
 
 			}
 
 			public void keyReleased(KeyEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		effectTable.setVisible(false);
 	}
 
 	private void createPlaylistViewer(Composite c) {
 		songTable = new Table(c, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
 		TableColumn tcNumber = new TableColumn(songTable, SWT.LEFT);
 		tcNumber.setText("#");
 		tcNumber.setWidth(32);
 		
 		TableColumn tcName = new TableColumn(songTable, SWT.LEFT);
 		tcName.setText("Name");
 		tcName.setWidth(192);
 		
 		TableColumn tcPath = new TableColumn(songTable, SWT.LEFT);
 		tcPath.setText("Path");
 		tcPath.setWidth(192);
 		
 		songTableViewer = new TableViewer(songTable);
 		songTableViewer.getTable().setLinesVisible(true);
 		songTableViewer.getTable().setHeaderVisible(true);
 		songTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
 		songTableViewer.getTable().setMenu(songTableContextMenu.createContextMenu(songTableViewer.getTable()));
 		
 		songTableViewer.setContentProvider(new IStructuredContentProvider() {
 			public void dispose() {}
 
 			public Object[] getElements(Object inputElement) {
 				return ((Playlist) inputElement).getSongs().toArray();
 			}
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 		});
 		
 		songTableViewer.setLabelProvider(new SongLabelProvider());
 
 		songTableViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
 				Alias a = (Alias) selection.getFirstElement();
 				activateSong(a);
 			}
 		});
 
 		songTableViewer.addDropSupport(DND.DROP_COPY, fileFormat, new DropTargetListener () {
 			public void dragEnter(DropTargetEvent dte) {
 				dte.detail = DND.DROP_COPY;
 				
 				//dte.currentDataType = dte.dataTypes[0];
 			}
 			
 			public void dragLeave(DropTargetEvent dte) {}
 
 			public void dragOperationChanged(DropTargetEvent dte) {}
 			public void dragOver(DropTargetEvent dte) {}
 			public void drop(DropTargetEvent dte) {
 				try {
 					if (FileTransfer.getInstance().isSupportedType(dte.currentDataType)) {
 						String[] paths = (String[]) dte.data;						
 						addFiles(paths, songTableViewer, selectedPlaylist);
 					}
 				} catch (Exception e) {
 					System.out.println(e.toString());
 				}
 			}
 			public void dropAccept(DropTargetEvent dte) {}
 		});
 		
 		songTable.addKeyListener(new KeyListener() {
 
 			public void keyPressed(KeyEvent e) {
 				// TODO Auto-generated method stub
 				if (e.keyCode == SWT.F2) {
 					renameSelectedSongs();
 				}
 
 				if (e.keyCode == SWT.DEL) {
 					deleteSelectedSongs();
 				}
 
 			}
 
 			public void keyReleased(KeyEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		songTable.setVisible(false);
 	}
 
 	private void createResourceViewer(Composite c) {
 		resourceViewer = new TreeViewer(c, SWT.BORDER);
 		resourceViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		resourceViewer.setContentProvider(new ITreeContentProvider() {
 			public void dispose() {}
 
 			public Object[] getChildren(Object o) {
 				return ((IResource)o).getItems().toArray();
 			}
 
 			public Object[] getElements(Object o) {
 				return ((IResource)o).getItems().toArray();
 			}
 
 			public Object getParent(Object o) {
 				return ((IResource)o).getParent();
 			}
 
 			public boolean hasChildren(Object o) {
 				IResource resource = (IResource) o;
 				List<IResource> items = resource.getItems();
 				if (items == null || items.size() == 0) return false;
 
 				return true;
 			}
 
 			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}
 
 		});
 
 		resourceViewer.setLabelProvider(new LabelProvider() {
 			public Image getImage(Object o) {
 				if (o.getClass() == Folder.class) return getFolderImage();
 				if (o.getClass() == Playlist.class) return getPlaylistImage();
 				if (o.getClass() == Palette.class) return getPaletteImage();
 				return getFolderImage();
 			}
 
 			public String getText(Object o) {
 				return ((IResource)o).getName();
 			}
 		});
 
 		resourceViewer.setSorter(new ViewerSorter() {
 			public int category(Object o) {
 				IResource resource = (IResource) o;
 				if (resource.getClass() == Folder.class) return 0;
 				return 1;
 			}
 		});
 		
 		resourceViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				if(event.getSelection().isEmpty()) {
 					return;
 				}
 				
 				if(event.getSelection() instanceof IStructuredSelection) {
 			           IStructuredSelection selection = (IStructuredSelection)event.getSelection();
 			           selectedResource = (IResource) selection.getFirstElement();
 			           
 			           if (selectedResource.getClass() == Folder.class) {
 			        	   resourceViewer.getTree().setMenu(folderContextMenu.createContextMenu(getShell()));
 			           }
 
 			           if (selectedResource.getClass() == Playlist.class) {
 			        	   resourceViewer.getTree().setMenu(playlistContextMenu.createContextMenu(getShell()));
 			           }
 
 			           if (selectedResource.getClass() == Palette.class) {
 			        	   resourceViewer.getTree().setMenu(paletteContextMenu.createContextMenu(getShell()));
 			           }
 			
 				}
 			}
 			
 		});
 
 		resourceViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 				IResource resource = (IResource) selection.getFirstElement();
 				
 				if (resourceViewer.isExpandable(resource)) {
 					if (resourceViewer.getExpandedState(resource) == true) {
 						// TODO: What is the second argument here?
 						resourceViewer.collapseToLevel(resource, 1);
 						
 					} else {
 						// TODO: What is the second argument here?
 						resourceViewer.expandToLevel(resource, 1);
 					}	
 
 				} else {
 					if (resource instanceof Playlist) {
 						selectPlaylist((Playlist) resource);
 						
 					} else if (resource instanceof Palette) {
 						selectPalette((Palette) resource);
 
 					}
 				}
 			}
 	
 		});
 
 		resourceViewer.addDropSupport(DND.DROP_COPY, fileFormat, new DropTargetListener () {
 			public void dragEnter(DropTargetEvent event) {
 				event.detail = DND.DROP_COPY;
 			}
 
 			public void dragLeave(DropTargetEvent event) {}
 			public void dragOperationChanged(DropTargetEvent event) {}
 			public void dragOver(DropTargetEvent event) {}
 
 			public void drop(DropTargetEvent event) {
 				// Figure out what item they're trying to drop it on
 				Tree tree = resourceViewer.getTree();
 				TreeItem item = tree.getItem(tree.toControl(new Point(event.x, event.y)));
 				
 				if (item != null) {
 					IResource resource = (IResource) item.getData();
 										
 					try {
 						if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
 							String[] paths = (String[]) event.data;
 							AliasCollector ac = (AliasCollector) resource;
 
 							if (resource instanceof Playlist) {
 								addFiles(paths, songTableViewer, ac);
 								
 							} else if (resource instanceof Palette) {
 								addFiles(paths, effectTableViewer, ac);
 
 							}
 
 						}
 					} catch (Exception e) {
 						System.out.println(e.toString());
 					}
 				}
 			}
 
 			public void dropAccept(DropTargetEvent event) {}
 			
 		});
 
 		//treeViewer.setInput(adventure.getResources());
 		
 		resourceViewer.getTree().addKeyListener(new KeyListener() {
 
 			public void keyPressed(KeyEvent e) {
 				// TODO Auto-generated method stub
 				if (e.keyCode == SWT.F2) {
 					// TODO: Implement TreeViewer's F2 Key
 					renameSelectedResource();
 				}
 				
 				if (e.keyCode == SWT.DEL) {
 					// TODO: Implement TreeViewer's DEL Key
 					deleteSelectedResource();
 				}
 
 			}
 
 			public void keyReleased(KeyEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		resourceViewer.getTree().setVisible(false);
 	}
 
 	private void createContextMenus() {
 		// Adventure - New
 		newMenu = new MenuManager("New");
 		newMenu.add(newFolderAction);
 		newMenu.add(newPlaylistAction);
 		newMenu.add(newPaletteAction);
 
 		// Adventure - Folder
 		folderContextMenu = new MenuManager("Folder");
 		folderContextMenu.add(newMenu);
 		folderContextMenu.add(new Separator());
 		folderContextMenu.add(deleteFolderAction);
 		folderContextMenu.add(renameFolderAction);
 
 		// Adventure - Playlist
 		playlistContextMenu = new MenuManager("Playlist");
 		playlistContextMenu.add(playPlaylistAction);
 		playlistContextMenu.add(new Separator());
 		playlistContextMenu.add(deletePlaylistAction);
 		playlistContextMenu.add(renamePlaylistAction);
 
 		// Adventure - Palette
 		paletteContextMenu = new MenuManager("Palette");
 		paletteContextMenu.add(deletePaletteAction);
 		paletteContextMenu.add(renamePaletteAction);
 
 		// Song Table
 		songTableContextMenu = new MenuManager("Playlist");
 		songTableContextMenu.add(playSongAction);
 		songTableContextMenu.add(new Separator());
 		songTableContextMenu.add(newSongAction);
 		songTableContextMenu.add(new Separator());
 		songTableContextMenu.add(renameSongAction);
 		songTableContextMenu.add(deleteSongAction);
 		songTableContextMenu.add(moveSongUpAction);
 		songTableContextMenu.add(moveSongDownAction);
 
 		// Effect Table
 		effectTableContextMenu = new MenuManager("Palette");
 		effectTableContextMenu.add(playEffectAction);
 		effectTableContextMenu.add(new Separator());
 		effectTableContextMenu.add(newEffectAction);
 		effectTableContextMenu.add(new Separator());
 		effectTableContextMenu.add(renameEffectAction);
 		effectTableContextMenu.add(deleteEffectAction);
 
 		// Audio Explorer - Directory
 		directoryPopupMenuManager = new MenuManager();
 		directoryPopupMenuManager.add(directoryAddPlaylistAction);
 		directoryPopupMenuManager.add(directoryAddPaletteAction);
 		
 		// Audio Explorer - File
 		filePopupMenuManager = new MenuManager();
 		filePopupMenuManager.add(filePreviewAction);
 		filePopupMenuManager.add(fileAddPlaylistAction);
 		filePopupMenuManager.add(fileAddPaletteAction);
 	}
 
 	protected MenuManager createMenuManager() {
 		MenuManager fileMenuManager	= new MenuManager("&File");
 		fileMenuManager.add(newAdventureAction);
 		fileMenuManager.add(openAdventureAction);
 		fileMenuManager.add(new Separator());
 		fileMenuManager.add(saveAdventureAction);
 		fileMenuManager.add(saveAdventureAsAction);
 		fileMenuManager.add(new Separator());
 		fileMenuManager.add(closeAdventureAction);
 		fileMenuManager.add(new Separator());
 		fileMenuManager.add(exitAction);
 
 		MenuManager adventureMenuManager = new MenuManager("&Adventure");
 		//adventureMenuManager.add(newMenu);
 		
 		MenuManager playlistMenuManager = new MenuManager("Playlist");
 
 		MenuManager paletteMenuManager = new MenuManager("Palette");
 		
 		MenuManager mainMenu = new MenuManager();
 		mainMenu.add(fileMenuManager);
 		mainMenu.add(adventureMenuManager);
 		mainMenu.add(playlistMenuManager);
 		mainMenu.add(paletteMenuManager);
 
 		return mainMenu;
 	}
 
 	private Image getFolderImage() {
 		return folderImage;
 	}
 	
 	private Image getPaletteImage() {
 		return paletteImage;
 	}
 
 	private Image getPlaylistImage() {
 		return playlistImage;
 	}
 	
 	// Action Methods
 	
 	// Audio Explorer Actions
 	
 	private void directoryAddPlaylist() {
 		addSelectedDirectory(directoryViewer, songTableViewer, selectedPlaylist);
 	}
 
 	private void directoryAddPalette() {
 		addSelectedDirectory(directoryViewer, effectTableViewer, selectedPalette);
 	}
 
 	private void fileAddPlaylist() {
 		addSelectedFiles(fileViewer, songTableViewer, selectedPlaylist);
 	}
 
 	private void fileAddPalette() {
 		addSelectedFiles(fileViewer, effectTableViewer, selectedPalette);
 	}
 
 	private void filePreview() {
 		IStructuredSelection selection = (IStructuredSelection) fileViewer.getSelection();
 		File selectedFile = (File) selection.getFirstElement();
 		previewSong(selectedFile);
 	}
 	
 	private void addSelectedDirectory(Viewer sourceViewer, Viewer destinationViewer, AliasCollector ac) {
 		IStructuredSelection selection = (IStructuredSelection) sourceViewer.getSelection();
 		File f = (File) selection.getFirstElement();
 
 		// Get all its files
 		File[] paths = f.listFiles(audioFileFilter);
 
 		// Loop through and add the to the currently selected Playlist
 		int iPathCount = paths.length;
 		if (iPathCount > 0) {
 			String sName;
 			File g;
 			for (int i = 0; i < iPathCount; i++) {
 				g = paths[i];
 				if (g.exists()) {
 					sName = g.getName();
 
 					if (!sName.equals("")) {
 						Alias a = new Alias(sName, g.getPath());
 						ac.add(a);
 						destinationViewer.refresh();
 					}
 				}
 			}
 
 			touchAdventure();
 		}
 	}
 
 	private void addSelectedFiles(Viewer sourceViewer, Viewer destinationViewer, AliasCollector ac) {
 		IStructuredSelection selection = (IStructuredSelection) sourceViewer.getSelection();
 		Iterator i = selection.iterator();
 		File f;
 		Alias a;
 		while (i.hasNext()) {
 			f = (File) i.next();
 			a = new Alias(f.getName(), f.getPath());
 
 			ac.add(a);
 		}
 		destinationViewer.refresh();
 		touchAdventure();
 	}
 
 	// Resource Viewer Actions
 
 	private void deleteSelectedFolder() {
 		deleteSelectedResource();
 	}
 
 	private void deleteSelectedPalette() {
 		deleteSelectedResource();		
 	}
 
 	private void deleteSelectedPlaylist() {
 		deleteSelectedResource();
 	}
 
 	private void deleteSelectedResource() {
 		confirmDialog.setMessage("Delete '" + selectedResource.getName() + "'?");
 		if (selectedResource instanceof Folder) {
 				int childrenCount = selectedResource.getItems().size();
 				if (childrenCount > 0) {
					confirmDialog.setMessage("Delete '" + selectedResource.getName() + "' and it's " + childrenCount + " resources?");
 				}
 		}
 		int iResponse = confirmDialog.open();
 
 		switch (iResponse) {
 			case SWT.OK:
 				Folder f = (Folder) selectedResource.getParent();
 				f.removeItem(selectedResource);
 				resourceViewer.refresh(f);
 				touchAdventure();
 				
 			case SWT.CANCEL:
 				break;
 			
 		}
 
 	}
 
 	// TODO: These 3 look pretty similar, no?
 	private void newFolder() {
 		InputDialog newFolderDialog = new InputDialog(this.getShell(), "New Folder" , "Enter Folder name", "Default Folder", null);
 		if (newFolderDialog.open() == InputDialog.OK) {
 			Folder f = new Folder(newFolderDialog.getValue());
 			((Folder)selectedResource).addItem(f);
 			touchAdventure();
 			resourceViewer.refresh(selectedResource);
 			resourceViewer.expandToLevel(selectedResource, 1);
 		}
 	}
 
 	private void newPalette() {
 		InputDialog newPaletteDialog = new InputDialog(this.getShell(), "New Playlist" , "Enter Palette name", "Default Palette", null);
 		if (newPaletteDialog.open() == InputDialog.OK) {
 			Palette p = new Palette(newPaletteDialog.getValue());
 			((Folder)selectedResource).addItem(p);
 			touchAdventure();
 			resourceViewer.refresh(selectedResource);
 			resourceViewer.expandToLevel(selectedResource, 1);
 		}	
 	}
 	
 	private void newPlaylist() {
 		InputDialog newPlaylistDialog = new InputDialog(this.getShell(), "New Playlist" , "Enter Playlist name", "Default Playlist", null);
 		if (newPlaylistDialog.open() == InputDialog.OK) {
 			Playlist p = new Playlist(newPlaylistDialog.getValue());
 			((Folder)selectedResource).addItem(p);
 			touchAdventure();
 			resourceViewer.refresh(selectedResource);
 			resourceViewer.expandToLevel(selectedResource, 1);
 		}
 	}
 
 	private void renameFolder() {
 		renameResource("Folder", selectedResource);
 	}
 
 	private void renamePalette() {
 		renameResource("Palette", selectedResource);
 	}
 
 	private void renamePlaylist() {
 		renameResource("Playlist", selectedResource);
 	}
 	
 	private void renameSelectedResource() {
 		if (selectedResource instanceof Folder) {
 			renameFolder();
 		} else if (selectedResource instanceof Palette) {
 			renamePalette();
 			
 		} else if (selectedResource instanceof Playlist) {
 			renamePlaylist();
 		}
 	}
 
 	private void renameResource(String typeName, IResource resource) {
 		// Prompt for new name
 		InputDialog renameDialog = new InputDialog(this.getShell(), "Rename " + typeName + ": " + resource.getName() , "Enter new name", resource.getName(), null);
 		if (renameDialog.open() == InputDialog.OK) {
 			resource.setName(renameDialog.getValue());
 			touchAdventure();
 			resourceViewer.refresh(resource.getParent());
 			
 		}
 	}
 
 	// Playlist and Palette Actions
 
 	private void activateEffect(Alias a) {
 		playEffect(a);
 	}
 
 	private void activateSong(Alias a) {
 		playSong(a);
 	}
 
 	private void deleteSelectedSongs() {
 		deleteSelectedAliases(selectedPlaylist, songTableViewer);
 	}
 
 	private void deleteSelectedEffects() {
 		deleteSelectedAliases(selectedPalette, effectTableViewer);		
 	}
 
 	private void deleteSelectedAliases(AliasCollector ac, Viewer v) {
 		Alias a;
 		IStructuredSelection selection = (IStructuredSelection) v.getSelection();
 		Iterator i = selection.iterator();
 		while (i.hasNext()) {
 			a = (Alias) i.next();
 			ac.remove(a);
 		}
 		v.refresh();
 
 		touchAdventure();
 	}
 
 	private void moveSongUp() {
 		Alias a;
 		IStructuredSelection selection = (IStructuredSelection) songTableViewer.getSelection();
 		Iterator i = selection.iterator();
 		while (i.hasNext()) {
 			a = (Alias) i.next();
 			selectedPlaylist.moveUp(a);
 		}
 		songTableViewer.refresh();
 		touchAdventure();
 	}
 	
 	private void moveSongDown() {
 		Alias a;
 		IStructuredSelection selection = (IStructuredSelection) songTableViewer.getSelection();
 
 		// TODO: Need to reverse my list otherwise moving down becomes pointless when starting with the first item
 		// This can probably be done a much better way...
 		Object[] foo = selection.toArray();
 		
 		for (int i = foo.length - 1; i >= 0; i--) {
 			a = (Alias) foo[i];
 			selectedPlaylist.moveDown(a);				
 		}
 		
 		songTableViewer.refresh();
 		touchAdventure();
 		
 	}
 
 	private void newSong() {
 		addChosenFiles(selectedPlaylist, songTableViewer);	
 	}
 
 	private void newEffect() {
 		addChosenFiles(selectedPalette, effectTableViewer);		
 	}
 
 	private void addChosenFiles(AliasCollector ac, Viewer v) {
 		boolean bDataChanged = false;
 		// Prompt user for files
 		songDialog.open();
 
 		// Get files and path
 		String[] paths = songDialog.getFileNames();
 		String directory = songDialog.getFilterPath();
 
 		// Loop through files and add to Playlist
 		int iPathCount = paths.length;
 		if (iPathCount > 0) {
 			String sName;
 			File f;
 			for (int i = 0; i < iPathCount; i++) {
 				f = new File(directory + "/" + paths[i]);
 				if (f.exists()) {
 					sName = f.getName();
 
 					if (!sName.equals("")) {
 						Alias a = new Alias(sName, f.getPath());
 						ac.add(a);
 						bDataChanged = true;
 					}
 				}
 			}
 			
 			if (bDataChanged) {
 				v.refresh();
 				touchAdventure();
 			}
 		}
 	}
 
 	private void playSelectedSong() {
 		IStructuredSelection selection = (IStructuredSelection) songTableViewer.getSelection();
 		Alias a = (Alias) selection.getFirstElement();
 		playSong(a);
 	}
 	
 	private void playSelectedEffect() {
 		IStructuredSelection selection = (IStructuredSelection) effectTableViewer.getSelection();
 		Alias a = (Alias) selection.getFirstElement();
 		playEffect(a);
 		
 	}
 	
 	private void playEffect(Alias a) {
 		if (a != null) {
 			File file = new File(a.getPath());
 			
 			if (file.exists()) {
 				boolean success = audioEngine.playEffect(file);
 
 				if (success) {
 					// Update UI?
 				}
 			}
 			
 		} else {
 			// TODO: Handle non-existant file
 
 		}
 		
 	}
 
 	private void playSong(Alias a) {
 		if (a != null) {
 			File file = new File(a.getPath());
 
 			if (file.exists()) {
 				activeSong = a;
 				activePlaylist = selectedPlaylist;
 				audioEngine.playSong(file);
 
 			} else {
 				// TODO: Handle non-existant file
 			}
 			
 			// See http://dev.eclipse.org/viewcvs/index.cgi/platform-swt-home/faq.html?rev=1.56#uithread
 			getShell().getDisplay().syncExec(
 				new Runnable() {
 		        	public void run(){
 		        		//songTableViewer.refresh();
 		        	}
 		        }
 		    );
 
 		}
 
 	}
 
 	private void renameSelectedSongs() {
 		renameSelectedAliases(songTableViewer, "Song");
 	}
 
 	private void renameSelectedEffects() {
 		renameSelectedAliases(effectTableViewer, "Effect");
 	}
 
 	private void renameSelectedAliases(Viewer v, String type) {
 		Alias a;
 		IStructuredSelection selection = (IStructuredSelection) v.getSelection();
 		Iterator i = selection.iterator();
 		while (i.hasNext()) {
 			a = (Alias) i.next();
 			renameDialog = new InputDialog(this.getShell(), "Rename " + type + ": " + a.getName() , "Enter new name", a.getName(), null);
 			if (renameDialog.open() == InputDialog.OK) {
 				a.setName(renameDialog.getValue());
 				touchAdventure();
 				v.refresh();
 			}
 		}
 	}
 
 	// Other Actions
 	
 	private void addFiles(String[] paths, Viewer v, AliasCollector ac) {
 		int numPaths = paths.length;
 		File f;
 		for (int i = 0; i < numPaths; i++) {
 			f = new File(paths[i]);
 			Alias a = new Alias(f.getName(), f.getAbsolutePath());
 
 			ac.add(a);
 		}
 		v.refresh();
 		touchAdventure();
 	}
 	
 	private void nextSong() {
 		Alias a = activePlaylist.getNext(activeSong);
 		playSong(a);
 	}
 
 	private void activatePlaylist() {
 		activePlaylist = (Playlist) selectedResource;
 		playSong(activePlaylist.getFirst());
 	}
 	
 	private void previewSong(File f) {
 		if (f.exists()) {
 			audioEngine.previewSong(f);
 		}
 	}
 
 	private void selectPlaylist(Playlist p) {
 		selectedPlaylist = p;
 		playlistComposite.setText("Playlist: " + p.getName());
 		songTableViewer.setInput(selectedPlaylist);
 		songTable.setVisible(true);		
 		fileAddPlaylistAction.setEnabled(true);
 
 	}
 
 	private void selectPalette(Palette p) {
 		selectedPalette = p;
 		paletteComposite.setText("Palette: " + p.getName());	
 		effectTableViewer.setInput(selectedPalette);
 		effectTable.setVisible(true);
 		fileAddPaletteAction.setEnabled(true);
 
 	}
 	
 	// File Methods
 
 	private void newAdventure() {
 		boolean bContinue = confirmSaveDialog();
 		
 		if (bContinue) {
 			audioEngine.stop();
 			adventure = new Adventure();
 			adventurePath = null;
 			untouchAdventure();
 			
 			// TODO: Update a bunch of UI here - probably shared by Open Adventure as well
 			resourceViewer.setInput(adventure.getResources());
 			resourceViewer.getTree().setVisible(true);
 
 			songTableViewer.setInput(null);
 			songTableViewer.getTable().setVisible(false);
 
 			effectTableViewer.setInput(null);
 			effectTableViewer.getTable().setVisible(false);
 			
 			saveAdventureAction.setEnabled(true);
 			closeAdventureAction.setEnabled(true);
 		}
 	}
 	
 	private void openAdventure() {
 		boolean bContinue = confirmSaveDialog();
 		
 		if (bContinue) {
 			openFileDialog.open();
 			String openFilename = openFileDialog.getFileName();
 			String openDirectory = openFileDialog.getFilterPath();
 			
 			if (!openFilename.equals("")) {
 				audioEngine.stop();
 				adventurePath = openDirectory + File.separator + openFilename;
 				//adventureFilename = openFilename;
 				loadAdventureXML(adventurePath);
 				untouchAdventure();
 
 				resourceViewer.setInput(adventure.getResources());
 				resourceViewer.getTree().setVisible(true);
 
 				songTableViewer.setInput(null);
 				songTableViewer.getTable().setVisible(false);
 
 				effectTableViewer.setInput(null);
 				effectTableViewer.getTable().setVisible(false);
 				
 				saveAdventureAction.setEnabled(false);
 				saveAdventureAsAction.setEnabled(true);
 				closeAdventureAction.setEnabled(true);
 				
 			}
 		}
 	}
 
 	private boolean saveAdventure() {
 		boolean bSuccess = false;
 		
 		if (adventurePath != null) {
 			// Already have a path, just save the Adventure
 			bSuccess = saveAdventureXML();
 
 		} else {
 			// Otherwise, prompt for a path and filename
 			bSuccess = showSaveDialog();
 		}
 		
 		if (bSuccess) {
 			untouchAdventure();
 		}
 		
 		return bSuccess;
 	}
 
 	private void saveAdventureAs() {
 		// TODO: Implement saveAdventureAs
 		// Show saveDialog asking for new Adventure filename
 	}
 
 	private void closeAdventure() {
 		boolean bContinue = confirmSaveDialog();
 		
 		if (bContinue) {
 			audioEngine.stop();
 
 			adventure = null;
 			adventurePath = null;
 			untouchAdventure();
 
 			resourceViewer.setInput(null);
 			resourceViewer.getTree().setVisible(false);
 
 			songTableViewer.setInput(null);
 			songTableViewer.getTable().setVisible(false);
 
 			effectTableViewer.setInput(null);
 			effectTableViewer.getTable().setVisible(false);
 			
 			saveAdventureAction.setEnabled(false);
 			saveAdventureAsAction.setEnabled(false);
 			closeAdventureAction.setEnabled(false);
 		}
 
 	}
 
 	private boolean confirmSaveDialog() {
 		if (adventure != null && isAdventureDirty()) {
 			confirmDialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
 			confirmDialog.setText("Save Current Adventure?");
 			confirmDialog.setMessage("The currently opened Adventure is not saved.  Do you want to save it before continuing?");
 			
 			int iResponse = confirmDialog.open();
 
 			switch (iResponse) {
 				case SWT.YES:
 					// If they cancel out of the save, cancel out of this as well
 					return saveAdventure();
 					
 				case SWT.NO:
 					return true;
 					
 				case SWT.CANCEL:
 					return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	private void quit() {
 		audioEngine.stop();
 		close();
 	}
 
 	private void loadAdventureXML(String adventureFilePath) {
 		File adventureFile = new File(adventureFilePath);
 
 		if (adventureFile.exists()) {
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(adventureFile));
 				StringBuffer xml = new StringBuffer();
 				String str;
 				while((str = in.readLine()) != null) {
 					xml.append(str);
 				}
 				in.close();
 								
 				adventure = (Adventure) xstream.fromXML(xml.toString());
 								
 			} catch (Exception e) {
 				System.out.println(e.toString());
 				
 			}			
 		}
 		
 	}
 	
 	private boolean saveAdventureXML() {
 		String xml = xstream.toXML(adventure);
 		
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(adventurePath));
 			out.write(xml);
 			out.close();
 			untouchAdventure();
 			
 			return true;
 			
 		} catch (Exception e) {
 		
 			return false;
 		}
 	}
 
     private boolean showSaveDialog() {
 		saveAdventureDialog.open();
 		String newFilename = saveAdventureDialog.getFileName();
 		String newDirectory = saveAdventureDialog.getFilterPath();
 		
 		if (!newFilename.equals("")) {
 			adventurePath = newDirectory + File.separator + newFilename;
 			System.out.println(adventurePath);	
 
 			//adventureFilename = newFilename;
 			return saveAdventureXML();
 			
 		} else {
 			return false;
 			
 		}
 	}
 
     // Adventure Data 
 
 	private boolean isAdventureDirty() {
 		return bDirtyData;
 	}
         
 	private void touchAdventure() {
 		bDirtyData = true;
 		saveAdventureAction.setEnabled(true);
 		updateWindowTitle();
 	}
 
 	private void untouchAdventure() {
 		bDirtyData = false;
 		saveAdventureAction.setEnabled(false);
 		updateWindowTitle();
 	}
 	
 	// Misc Support Methods
 	public void updateWindowTitle() {
 		StringBuffer title = new StringBuffer("RPG Audio Mixer");
 
 		if (adventure != null) {
 			if (adventurePath != null) {
 				title.append(": " + adventurePath);
 			} else {
 				title.append(": Unsaved Adventure");
 			}
 			
 			if (isAdventureDirty()) {
 					title.append(" [unsaved]");
 			}
 
 		}
 		
 		getShell().setText(title.toString());
 	}
 
 	// AudioEngineListener Implementation
 	public void songFinished() {
 		nextSong();		
 	}
 
     // Private Classes
 	private final class SongLabelProvider implements ITableLabelProvider, IColorProvider {
 		public String getColumnText(Object object, int columnIndex) {
 			Alias a = (Alias) object;
 
 			switch (columnIndex) {
 				case 0:
 					return Integer.toString(selectedPlaylist.getSongs().indexOf(a) + 1);
 				
 				case 1:
 					return a.getName();
 					
 				case 2:
 					return a.getPath();
 						
 				default:
 					return a.getName();
 			}
 			
 		}
 		
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 
 		public Color getForeground(Object element) {
 			Alias c = (Alias) element;
 			if (c == activeSong) {
 				return Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);				
 			}
 			return null;
 		}
 
 		public Color getBackground(Object element) {
 			Alias c = (Alias) element;
 			if (c == activeSong) {
 				return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);				
 			}
 			return null;
 		}
 
 		public Image getColumnImage(Object arg0, int arg1) {
 			return null;
 		}
 
 		public void addListener(ILabelProviderListener listener) {}
 		public void removeListener(ILabelProviderListener listener) {}
 		public void dispose() {}
 
 	}
 
 }
