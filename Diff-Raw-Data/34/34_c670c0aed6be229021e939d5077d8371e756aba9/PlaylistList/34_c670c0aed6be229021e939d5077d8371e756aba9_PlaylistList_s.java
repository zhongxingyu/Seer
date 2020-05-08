 package com.robonobo.gui.components;
 
 import static com.robonobo.gui.GuiUtil.*;
 import static com.robonobo.gui.RoboColor.*;
 import static javax.swing.SwingUtilities.*;
 
 import java.awt.*;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.*;
import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.*;
 
 import org.jdesktop.swingx.renderer.DefaultListRenderer;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.core.Platform;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.model.*;
 import com.robonobo.gui.RoboFont;
 import com.robonobo.gui.components.base.RLabel12;
 import com.robonobo.gui.components.base.RMenuItem;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.gui.model.PlaylistListModel;
 import com.robonobo.gui.model.StreamTransfer;
 import com.robonobo.gui.panels.ContentPanel;
 import com.robonobo.gui.panels.LeftSidebar;
 import com.robonobo.gui.sheets.DeletePlaylistSheet;
 import com.robonobo.gui.sheets.SharePlaylistSheet;
 
 @SuppressWarnings("serial")
 public class PlaylistList extends LeftSidebarList implements UserListener, PlaylistListener, LoginListener {
 	private static final int MAX_LBL_WIDTH = 170;
 
 	ImageIcon playlistIcon;
 	PopupMenu popup = new PopupMenu();
 
 	public PlaylistList(LeftSidebar sideBar, RobonoboFrame frame) {
 		super(sideBar, frame, new PlaylistListModel(frame.getController()));
 		playlistIcon = createImageIcon("/icon/playlist.png", null);
 		setCellRenderer(new CellRenderer());
 		setName("robonobo.playlist.list");
 		setAlignmentX(0.0f);
 		setMaximumSize(new Dimension(65535, 65535));
 		// We do the listener stuff here rather than in the model as we may need to reselect or resize as a consequence
 		frame.getController().addUserListener(this);
 		frame.getController().addPlaylistListener(this);
 		frame.getController().addLoginListener(this);
 		setTransferHandler(new DnDHandler());
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				maybeShowPopup(e);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				maybeShowPopup(e);
 			}
 
 			private void maybeShowPopup(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					int idx = locationToIndex(e.getPoint());
 					if(idx != getSelectedIndex())
 						setSelectedIndex(idx);
 					popup.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		});
 	}
 
 	public PlaylistListModel getModel() {
 		return (PlaylistListModel) super.getModel();
 	}
 
 	public void selectPlaylist(final Playlist p) {
 		invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				setSelectedIndex(getModel().getPlaylistIndex(p));
 			}
 		});
 	}
 
 	@Override
 	public void loginSucceeded(User me) {
 		runOnUiThread(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				getModel().clear();
 			}
 		});
 	}
 	
 	@Override
 	public void loginFailed(String reason) {
 		// Do nothing
 	}
 	
 	@Override
 	public void userChanged(final User u) {
 		if (u.getUserId() != frame.getController().getMyUser().getUserId())
 			return;
 		invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 
 				Playlist selP = (getSelectedIndex() < 0) ? null : getModel().getPlaylistAt(getSelectedIndex());
 				boolean selPGone = false;
 
 				// Check for removed playlists
 				List<Playlist> toRm = new ArrayList<Playlist>();
 				for (Playlist p : getModel()) {
 					if (!u.getPlaylistIds().contains(p.getPlaylistId()))
 						toRm.add(p);
 				}
 				for (Playlist p : toRm) {
 					if (p.equals(selP))
 						selPGone = true;
 					getModel().remove(p);
 				}
 
 				// Removing items might have buggered up the selection, so put it back.
 				// If the selected playlist has gone, then go to my library
 				if (selP != null) {
 					if (selPGone)
 						frame.getLeftSidebar().selectMyMusic();
 					else {
 						int idx = getModel().getPlaylistIndex(selP);
 						setSelectedIndex(idx);
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void playlistChanged(final Playlist p) {
		Long plId = p.getPlaylistId();
		if (frame.getController().getMyUser().getPlaylistIds().contains(plId)) {
 			invokeLater(new CatchingRunnable() {
 				public void doRun() throws Exception {
 					Playlist selP = (getSelectedIndex() < 0) ? null : getModel().getPlaylistAt(getSelectedIndex());
 					boolean needReselect = p.equals(selP);
 					getModel().remove(p);
 					getModel().insertSorted(p);
 					if (needReselect) {
 						int idx = getModel().getPlaylistIndex(p);
 						setSelectedIndex(idx);
 					}
 				}
 			});		
		}
 	}
 
 	@Override
 	public void userConfigChanged(UserConfig cfg) {
 		// Do nothing
 	}
 
 	@Override
 	protected void itemSelected(int index) {
 		frame.getMainPanel().selectContentPanel("playlist/" + getModel().getPlaylistAt(index).getPlaylistId());
 		int unseen = getModel().numUnseen(index);
 		if(unseen > 0) {
 			frame.getController().markAllAsSeen(getModel().getPlaylistAt(index));
 			getModel().markAllAsSeen(index);
 		}
 	}
 
 	class PopupMenu extends JPopupMenu implements ActionListener {
 		public PopupMenu() {
 			addItem("Post to facebook...", "fb");
 			addItem("Post to twitter...", "twit");
 			addItem("Share...", "share");
 			addItem("Delete", "del");
 		}
 
 		private void addItem(String text, String cmd) {
 			RMenuItem rmi = new RMenuItem(text);
 			rmi.setActionCommand(cmd);
 			rmi.addActionListener(this);
 			add(rmi);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			int selIdx = getSelectedIndex();
 			if (selIdx < 0)
 				return;
 			Playlist p = getModel().getPlaylistAt(selIdx);
 			String action = e.getActionCommand();
 			if (action.equals("fb"))
 				frame.postToFacebook(p);
 			else if (action.equals("twit"))
 				frame.postToTwitter(p);
 			else if (action.equals("share")) {
 				SharePlaylistSheet sps = new SharePlaylistSheet(frame, p);
 				frame.showSheet(sps);
 			} else if (action.equals("del")) {
 				DeletePlaylistSheet dps = new DeletePlaylistSheet(frame, p);
 				frame.showSheet(dps);
 			}
 		}
 	}
 
 	class ItemLbl extends RLabel12 {
 		public ItemLbl() {
 			setOpaque(true);
 			setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
 			setMaximumSize(new Dimension(MAX_LBL_WIDTH, 65535));
 			setPreferredSize(new Dimension(MAX_LBL_WIDTH, 65535));
 		}
 	}
 
 	class CellRenderer extends DefaultListRenderer {
 		JLabel lbl = new ItemLbl();
 		Font normalFont, boldFont;
 		
 		public CellRenderer() {
 			normalFont = RoboFont.getFont(12, false);
 			boldFont = RoboFont.getFont(12, true);
 		}
 		
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
 				boolean cellHasFocus) {
 			String text = (String) value;
 			int unseen = getModel().numUnseen(index);
 			if(unseen > 0) {
 				text = text + "[" + unseen+ "]";
 				lbl.setFont(boldFont);
 			} else
 				lbl.setFont(normalFont);
 			lbl.setText(text);
 			lbl.setIcon(playlistIcon);
 			if (isSelected) {
 				lbl.setBackground(LIGHT_GRAY);
 				lbl.setForeground(BLUE_GRAY);
 			} else {
 				lbl.setBackground(MID_GRAY);
 				lbl.setForeground(DARK_GRAY);
 			}
 			return lbl;
 		}
 	}
 
 	class DnDHandler extends TransferHandler {
 		@Override
 		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
 			for (DataFlavor dataFlavor : transferFlavors) {
 				if (dataFlavor.equals(StreamTransfer.DATA_FLAVOR))
 					return true;
 			}
 			return Platform.getPlatform().canDnDImport(transferFlavors);
 		}
 
 		@Override
 		public boolean importData(JComponent comp, Transferable t) {
 			Playlist p = getModel().getPlaylistAt(getSelectedIndex());
 			String cpName = "playlist/" + p.getPlaylistId();
 			ContentPanel cp = frame.getMainPanel().getContentPanel(cpName);
 			return cp.importData(comp, t);
 		}
 	}
 }
