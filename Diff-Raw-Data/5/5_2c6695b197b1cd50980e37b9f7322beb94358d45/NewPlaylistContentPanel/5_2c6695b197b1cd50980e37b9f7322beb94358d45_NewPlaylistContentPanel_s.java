 package com.robonobo.gui.panels;
 
 import static com.robonobo.gui.GuiUtil.*;
 
 import javax.swing.JComponent;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.core.RobonoboController;
 import com.robonobo.core.api.model.Playlist;
 import com.robonobo.core.api.model.PlaylistConfig;
 import com.robonobo.core.metadata.PlaylistCallback;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.gui.model.NewPlaylistTableModel;
 
 @SuppressWarnings("serial")
 public class NewPlaylistContentPanel extends MyPlaylistContentPanel {
 	public NewPlaylistContentPanel(RobonoboFrame frame) {
 		this(frame, new Playlist());
 	}
 
 	private NewPlaylistContentPanel(RobonoboFrame frame, Playlist p) {
 		super(frame, p, new PlaylistConfig(), NewPlaylistTableModel.create(frame, p));
 		tabPane.insertTab("playlist", null, new PlaylistDetailsPanel(), null, 0);
 		tabPane.setSelectedIndex(0);
 		if (frame.guiCfg.getShowNewPlaylistDesc()) {
 			runOnUiThread(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					showMessage("How do I create a playlist?",
 							"<html>Drag tracks from your library, or a friend's library, or any other playlist, and drop them on the highlighted 'New Playlist' entry on the left, or on the track list below.<br>You can also drag files directly from your computer.</html>",
 							"showNewPlaylistDesc");
 				}
 			});
 		}
 		setupComments();
 	}
 
 	@Override
 	public JComponent defaultComponent() {
 		return titleField;
 	}
 
 	@Override
 	protected void savePlaylist() {
 		final Playlist p = ptm().getPlaylist();
 		p.setTitle(titleField.getText());
 		p.setDescription(descField.getText());
 		// Create the new playlist in midas
 		RobonoboController control = frame.ctrl;
 		p.getOwnerIds().add(control.getMyUser().getUserId());
 		control.createPlaylist(p, new PlaylistCallback() {
 			public void success(final Playlist newP) {
 				runOnUiThread(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						// A content panel should have been created for the new
 						// playlist - switch to it now
 						frame.leftSidebar.selectMyPlaylist(newP);
 						// Now that they're not looking, re-init everything with
 						// a new empty playlist
 						Playlist newP = new Playlist();
 						titleField.setText("");
 						descField.setText("");
 						if (iTunesCB != null)
 							iTunesCB.setSelected(false);
 						ptm().update(newP);
 					}
 				});
 			}
 
 			public void error(long playlistId, Exception ex) {
 				log.error("Error saving new playlist", ex);
 			}
 		});
 	}
 
 	@Override
 	protected boolean allowDel() {
 		return false;
 	}
 
 	@Override
 	protected boolean allowShare() {
 		return false;
 	}
 
 	@Override
 	protected boolean showITunes() {
 		return false;
 	}
 	
 	@Override
 	protected boolean saveOnFocusChange() {
 		return false;
 	}
 }
