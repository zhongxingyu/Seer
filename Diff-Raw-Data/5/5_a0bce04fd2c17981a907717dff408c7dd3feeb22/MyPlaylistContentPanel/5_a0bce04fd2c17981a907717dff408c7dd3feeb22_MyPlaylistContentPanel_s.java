 package com.robonobo.gui.panels;
 
 import static com.robonobo.common.util.TextUtil.*;
 import info.clearthought.layout.TableLayout;
 
 import java.awt.ComponentOrientation;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.common.util.FileUtil;
 import com.robonobo.core.Platform;
 import com.robonobo.core.api.PlaylistListener;
 import com.robonobo.core.api.model.Playlist;
 import com.robonobo.core.api.model.PlaylistConfig;
 import com.robonobo.gui.RoboColor;
 import com.robonobo.gui.RoboFont;
 import com.robonobo.gui.components.base.*;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.gui.model.PlaylistTableModel;
 import com.robonobo.gui.model.StreamTransfer;
 import com.robonobo.gui.sheets.DeletePlaylistSheet;
 import com.robonobo.gui.sheets.SharePlaylistSheet;
 import com.robonobo.gui.tasks.ImportFilesTask;
 
 @SuppressWarnings("serial")
 public class MyPlaylistContentPanel extends PlaylistContentPanel implements PlaylistListener {
 	protected RTextField titleField;
 	protected RTextArea descField;
 	protected RButton saveBtn;
 	protected RButton shareBtn;
 	protected RButton delBtn;
 	protected RCheckBox iTunesCB;
 	protected RRadioButton visMeBtn;
 	protected RRadioButton visFriendsBtn;
 	protected RRadioButton visAllBtn;
 	private ActionListener saveActionListener;
 	protected Map<String, RCheckBox> options = new HashMap<String, RCheckBox>();
 	boolean haveShown = false;
 
 	public MyPlaylistContentPanel(RobonoboFrame f, Playlist pl, PlaylistConfig pc) {
 		super(f, pl, pc, true);
 		tabPane.insertTab("playlist", null, new PlaylistDetailsPanel(), null, 0);
 		commentsPanel = new PlaylistCommentsPanel(f);
 		tabPane.insertTab("comments", null, commentsPanel, null, 1);
 		tabPane.setSelectedIndex(0);
 		setupComments();
 	}
 
 	protected MyPlaylistContentPanel(RobonoboFrame frame, Playlist p, PlaylistConfig pc, PlaylistTableModel model) {
 		super(frame, p, pc, model);
 		// Subclasses must call setupComments() themselves if they want comments
 	}
 
 	protected void setupComments() {
 		// Bug huntin
 		log.debug("Setting up comments in panel for playlist " + p.getPlaylistId());
 		addComponentListener(new ComponentAdapter() {
 			public void componentShown(ComponentEvent e) {
 				if (haveShown)
 					return;
 				haveShown = true;
 				if (getWidth() == 0)
 					throw new SeekInnerCalmException();
				log.debug("Adding comment listener for panel for playlist " + p.getPlaylistId());
 				frame.ctrl.addPlaylistListener(MyPlaylistContentPanel.this);
 				frame.ctrl.getExistingCommentsForPlaylist(p.getPlaylistId(), MyPlaylistContentPanel.this);
 			}
 		});
 		tabPane.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				if (tabPane.getSelectedIndex() == 1) {
 					if (unreadComments) {
 						unreadComments = false;
 						removeBangFromTab(1);
 						frame.leftSidebar.markPlaylistCommentsAsRead(p.getPlaylistId());
 						frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 							public void doRun() throws Exception {
 								frame.ctrl.markPlaylistCommentsAsSeen(p.getPlaylistId());
 							}
 						});
 					}
 				}
 			}
 		});
 	}
 
 	protected boolean allowShare() {
 		return true;
 	}
 
 	protected boolean allowDel() {
 		return true;
 	}
 
 	protected boolean showITunes() {
 		return true;
 	}
 
 	protected boolean detailsChanged() {
 		return isNonEmpty(titleField.getText());
 	}
 
 	protected void savePlaylist() {
 		frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				Playlist p = ptm().getPlaylist();
 				p.setTitle(titleField.getText());
 				p.setDescription(descField.getText());
 				frame.ctrl.updatePlaylist(p);
 				frame.ctrl.putPlaylistConfig(pc);
 			}
 		});
 	}
 
 	@Override
 	public void playlistChanged(Playlist p) {
 		if (p.equals(ptm().getPlaylist())) {
 			titleField.setText(p.getTitle());
 			descField.setText(p.getDescription());
 			String vis = p.getVisibility();
 			if (vis.equals(Playlist.VIS_ALL))
 				visAllBtn.setSelected(true);
 			else if (vis.equals(Playlist.VIS_FRIENDS))
 				visFriendsBtn.setSelected(true);
 			else if (vis.equals(Playlist.VIS_ME))
 				visMeBtn.setSelected(true);
 			else
 				throw new SeekInnerCalmException("invalid visibility " + vis);
 			ptm().update(p);
 			toolsPanel.checkPlaylistVisibility();
 		}
 	}
 
 	@Override
 	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
 		for (DataFlavor dataFlavor : transferFlavors) {
 			if (dataFlavor.equals(StreamTransfer.DATA_FLAVOR))
 				return true;
 		}
 		return Platform.getPlatform().canDnDImport(transferFlavors);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean importData(JComponent comp, Transferable t) {
 		JTable table = trackList.getJTable();
 		final PlaylistTableModel tm = (PlaylistTableModel) trackList.getModel();
 		// If we have a mouse location, drop things there, otherwise
 		// at the end
 		int mouseRow = (table.getMousePosition() == null) ? -1 : table.rowAtPoint(table.getMousePosition());
 		final int insertRow = (mouseRow >= 0) ? mouseRow : tm.getRowCount();
 		boolean transferFromRobo = false;
 		for (DataFlavor flavor : t.getTransferDataFlavors()) {
 			if (flavor.equals(StreamTransfer.DATA_FLAVOR)) {
 				transferFromRobo = true;
 				break;
 			}
 		}
 		if (transferFromRobo) {
 			// DnD streams from inside robonobo
 			List<String> streamIds;
 			try {
 				streamIds = (List<String>) t.getTransferData(StreamTransfer.DATA_FLAVOR);
 			} catch (Exception e) {
 				throw new SeekInnerCalmException();
 			}
 			tm.addStreams(streamIds, insertRow);
 			return true;
 		} else {
 			// DnD files from somewhere else
 			List<File> files = null;
 			try {
 				files = Platform.getPlatform().getDnDImportFiles(t);
 			} catch (IOException e) {
 				log.error("Caught exception dropping files", e);
 				return false;
 			}
 			List<File> allFiles = new ArrayList<File>();
 			for (File selFile : files)
 				if (selFile.isDirectory())
 					allFiles.addAll(FileUtil.getFilesWithinPath(selFile, "mp3"));
 				else
 					allFiles.add(selFile);
 			frame.ctrl.runTask(new PlaylistImportTask(frame, allFiles, insertRow));
 			return true;
 		}
 	}
 
 	public void addTracks(List<String> streamIds) {
 		PlaylistTableModel tm = (PlaylistTableModel) trackList.getModel();
 		tm.addStreams(streamIds, tm.getRowCount());
 	}
 
 	/** Return true to save playlist whenever user changes anything, false to only change when save button is pressed */
 	protected boolean saveOnFocusChange() {
 		return true;
 	}
 
 	class PlaylistImportTask extends ImportFilesTask {
 		int insertRow;
 
 		public PlaylistImportTask(RobonoboFrame frame, List<File> files, int insertRow) {
 			super(frame, files);
 			this.insertRow = insertRow;
 		}
 
 		@Override
 		protected void streamsAdded(List<String> streamIds) {
 			PlaylistTableModel tm = (PlaylistTableModel) trackList.getModel();
 			tm.addStreams(streamIds, insertRow);
 		}
 	}
 
 	class PlaylistDetailsPanel extends JPanel {
 		public PlaylistDetailsPanel() {
 			double[][] cellSizen = { { 5, 35, 5, 380, 10, 150, 5, TableLayout.FILL, 5 }, { 5, 25, 5, 25, 25, 0, TableLayout.FILL, 5, 30, 5 } };
 			setLayout(new TableLayout(cellSizen));
 			KeyListener kl = new KeyAdapter() {
 				@Override
 				public void keyTyped(KeyEvent e) {
 					saveBtn.setEnabled(detailsChanged());
 				}
 			};
 			saveActionListener = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (!detailsChanged())
 						return;
 					Playlist p = ptm().getPlaylist();
 					if (visAllBtn.isSelected())
 						p.setVisibility(Playlist.VIS_ALL);
 					else if (visFriendsBtn.isSelected())
 						p.setVisibility(Playlist.VIS_FRIENDS);
 					else if (visMeBtn.isSelected())
 						p.setVisibility(Playlist.VIS_ME);
 					pc.getItems().clear();
 					for (String opt : options.keySet()) {
 						JCheckBox cb = options.get(opt);
 						if (cb.isSelected())
 							pc.setItem(opt, "true");
 					}
 					savePlaylist();
 					saveBtn.setEnabled(false);
 				}
 			};
 			FocusListener fl = new FocusAdapter() {
 				@Override
 				public void focusLost(FocusEvent e) {
 					saveActionListener.actionPerformed(null);
 				}
 			};
 			final Playlist p = ptm().getPlaylist();
 			JLabel titleLbl = new JLabel("Title:");
 			titleLbl.setFont(RoboFont.getFont(13, false));
 			add(titleLbl, "1,1");
 			titleField = new RTextField(p.getTitle());
 			titleField.addKeyListener(kl);
 			titleField.addActionListener(saveActionListener);
 			if (saveOnFocusChange())
 				titleField.addFocusListener(fl);
 			add(titleField, "3,1");
 			toolsPanel = new PlaylistToolsPanel();
 			add(toolsPanel, "1,3,3,3");
 			RLabel descLbl = new RLabel13("Description:");
 			add(descLbl, "1,4,3,4");
 			descField = new RTextArea(p.getDescription());
 			descField.setBGColor(RoboColor.MID_GRAY);
 			descField.addKeyListener(kl);
 			if (saveOnFocusChange())
 				descField.addFocusListener(fl);
 			add(new JScrollPane(descField), "1,6,3,8");
 			add(new VisPanel(), "5,1,5,6");
 			add(new OptsPanel(), "7,1,7,6");
 			add(new ButtonsPanel(), "5,8,7,8");
 		}
 	}
 
 	class VisPanel extends JPanel {
 		public VisPanel() {
 			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 			RLabel visLbl = new RLabel13B("Show playlist to:");
 			add(visLbl);
 			add(Box.createVerticalStrut(5));
 			ButtonGroup bg = new ButtonGroup();
 			// TODO multiple owners?
 			Playlist p = ptm().getPlaylist();
 			String vis = p.getVisibility();
 			visMeBtn = new RRadioButton("Just me");
 			if (vis.equals(Playlist.VIS_ME))
 				visMeBtn.setSelected(true);
 			bg.add(visMeBtn);
 			add(visMeBtn);
 			visFriendsBtn = new RRadioButton("Friends");
 			if (vis.equals(Playlist.VIS_FRIENDS))
 				visFriendsBtn.setSelected(true);
 			bg.add(visFriendsBtn);
 			add(visFriendsBtn);
 			visAllBtn = new RRadioButton("Everyone");
 			if (saveOnFocusChange()) {
 				visMeBtn.addActionListener(saveActionListener);
 				visFriendsBtn.addActionListener(saveActionListener);
 				visAllBtn.addActionListener(saveActionListener);
 			}
 			if (vis.equals(Playlist.VIS_ALL))
 				visAllBtn.setSelected(true);
 			bg.add(visAllBtn);
 			add(visAllBtn);
 		}
 	}
 
 	class OptsPanel extends JPanel {
 		public OptsPanel() {
 			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 			add(Box.createVerticalStrut(20));
 			if (showITunes() && Platform.getPlatform().iTunesAvailable()) {
 				iTunesCB = new RCheckBox("Export playlist to iTunes");
 				iTunesCB.setSelected("true".equalsIgnoreCase(pc.getItem("iTunesExport")));
 				options.put("iTunesExport", iTunesCB);
 				if (saveOnFocusChange())
 					iTunesCB.addActionListener(saveActionListener);
 				add(iTunesCB);
 			}
 		}
 	}
 
 	class ButtonsPanel extends JPanel {
 		public ButtonsPanel() {
 			setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
 			// Laying out right-to-left
 			if (allowDel()) {
 				delBtn = new RRedGlassButton("Delete");
 				delBtn.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						DeletePlaylistSheet dPanel = new DeletePlaylistSheet(frame, ptm().getPlaylist());
 						frame.showSheet(dPanel);
 					}
 				});
 				add(delBtn);
 				add(Box.createHorizontalStrut(5));
 			}
 			if (allowShare()) {
 				shareBtn = new RGlassButton("Share");
 				shareBtn.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						SharePlaylistSheet shPanel = new SharePlaylistSheet(frame, ptm().getPlaylist());
 						frame.showSheet(shPanel);
 					}
 				});
 				add(shareBtn);
 				add(Box.createHorizontalStrut(5));
 			}
 			saveBtn = new RGlassButton("Save");
 			if (!saveOnFocusChange())
 				saveBtn.addActionListener(saveActionListener);
 			// Otherwise, Save button doesn't actually do anything, it's just a nonce to grab the focus from the text
 			// components - when they lose focus, playlist gets saved
 			saveBtn.setEnabled(false);
 			add(saveBtn);
 		}
 	}
 }
