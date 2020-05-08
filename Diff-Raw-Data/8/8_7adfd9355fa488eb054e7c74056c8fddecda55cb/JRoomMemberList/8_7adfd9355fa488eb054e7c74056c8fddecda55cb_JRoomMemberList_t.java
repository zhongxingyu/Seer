 package edu.cs319.client.customcomponents;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.ListCellRenderer;
 
 import edu.cs319.server.CoLabPrivilegeLevel;
 import edu.cs319.server.IServer;
 import edu.cs319.util.Util;
 
 public class JRoomMemberList extends JList {
 
 	private JFlexibleListModel<RoomMemberLite> model;
 	private String username;
 	private IServer server;
 	private String roomName;
 
 	public JRoomMemberList(String username, IServer server) {
 		this.server = server;
 		this.username = username;
 		model = new JFlexibleListModel<RoomMemberLite>();
 		setCellRenderer(new RoomMemberCellRenderer());
 		setModel(model);
 		addMouseListener(rightClickListen);
 	}
 
 	@Override
 	public JFlexibleListModel<RoomMemberLite> getModel() {
 		return model;
 	}
 
 	public void setRoom(String room) {
 		roomName = room;
 	}
 
 	public void setServer(IServer server) {
 		this.server = server;
 	}
 
 	public boolean removeMember(String userID) {
 		RoomMemberLite dummy = new RoomMemberLite(userID, null);
 		return model.remove(dummy);
 	}
 	
 	public void removeAllMembers() {
 		model.clearList();
 	}
 
 	public boolean setMemberPriv(String id, CoLabPrivilegeLevel priv) {
 		// super admins stay super admins
 		if (Util.isSuperAdmin(id))
 			return true;
 		RoomMemberLite dummy = new RoomMemberLite(id, priv);
 		int idx = model.indexOf(dummy);
 		if (idx == -1)
 			return false;
 		RoomMemberLite mem = (RoomMemberLite) model.getElementAt(idx);
 		mem.setPriv(priv);
 		model.update();
 		return true;
 	}
 
 	public RoomMemberLite getFromID(String username) {
 		RoomMemberLite dummy = new RoomMemberLite(username, CoLabPrivilegeLevel.OBSERVER);
 		int idx = model.indexOf(dummy);
 		if (idx == -1)
 			return null;
 		return (RoomMemberLite) model.getElementAt(idx);
 	}
 
 	private MouseListener rightClickListen = new MouseAdapter() {
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			if (e.isPopupTrigger())
 				doPop(e);
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			if (e.isPopupTrigger())
 				doPop(e);
 		}
 
 		private void doPop(MouseEvent e) {
			if(getSelectedValue() != null) {
				JPopupMenu menu = new RoomMemberPopupMenu(username,
						((RoomMemberLite) getSelectedValue()).getName());
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
 		}
 	};
 
 	private class RoomMemberCellRenderer extends JLabel implements ListCellRenderer {
 		
 		public RoomMemberCellRenderer() {
 			setOpaque(true);
 		}
 
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value, int idx,
 				boolean isSelected, boolean hasFocus) {
 			RoomMemberLite mem = (RoomMemberLite) value;
 			setText(mem.toString());
 			if (mem.getPriv() == CoLabPrivilegeLevel.ADMIN
 					|| mem.getPriv() == CoLabPrivilegeLevel.SUPER_ADMIN) {
 				if (isSelected) {
 					setForeground(new Color(000, 100, 200));
 					setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
 					setBackground(new Color(185, 196, 225));
 				} else {
 					setForeground(new Color(000, 100, 200));
 					setBorder(BorderFactory.createEmptyBorder());
 					setBackground(Color.WHITE);
 				}
 			} else {
 				setForeground(Color.BLACK);
 				if (isSelected) {
 					setBorder(BorderFactory.createEmptyBorder());
 					setBackground(new Color(185, 196, 225));
 				} else {
 					setBorder(BorderFactory.createEmptyBorder());
 					setBackground(Color.WHITE);
 				}
 			}
 			return this;
 		}
 	}
 
 	private class RoomMemberPopupMenu extends JPopupMenu {
 		private JMenuItem kickOutMember;
 		private JMenuItem promoteUser;
 		private JMenuItem demoteUser;
 
 		private String clicker, clickee;
 
 		public RoomMemberPopupMenu(String clicker, String clickee) {
 			this.clickee = clickee;
 			this.clicker = clicker;
 			RoomMemberLite clickerMem = getFromID(clicker);
 			RoomMemberLite clickeeMem = getFromID(clickee);
 			if (clickerMem.equals(clickeeMem))
 				return;
 			// observers can't do anything, and super admins can't have anything done to them
 			if (clickerMem.getPriv() == CoLabPrivilegeLevel.OBSERVER
 					|| clickeeMem.getPriv() == CoLabPrivilegeLevel.SUPER_ADMIN) {
 				return;
 			}
 			// admins can kick any user out
 			if (clickerMem.getPriv() == CoLabPrivilegeLevel.ADMIN
 					|| clickerMem.getPriv() == CoLabPrivilegeLevel.SUPER_ADMIN) {
 				kickOutMember = new JMenuItem("Kick Out Member");
 				kickOutMember.addActionListener(kickOutAction);
 				add(kickOutMember);
 				demoteUser = new JMenuItem("Demote Member");
 				demoteUser.addActionListener(demoteAction);
 				add(demoteUser);
 			}
 			// Participants can only promote observers
 			if (clickerMem.getPriv() == CoLabPrivilegeLevel.PARTICIPANT
 					&& clickeeMem.getPriv() != CoLabPrivilegeLevel.OBSERVER) {
 				return;
 			}
 			promoteUser = new JMenuItem("Promote Member");
 			promoteUser.addActionListener(promoteAction);
 			add(promoteUser);
 		}
 
 		private ActionListener promoteAction = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				CoLabPrivilegeLevel newPriv = getFromID(clickee).getPriv() == CoLabPrivilegeLevel.OBSERVER ? CoLabPrivilegeLevel.PARTICIPANT
 						: CoLabPrivilegeLevel.ADMIN;
 				if (newPriv == CoLabPrivilegeLevel.ADMIN) {
 					int choice = JOptionPane
 							.showConfirmDialog(
 									JRoomMemberList.this,
 									"Promoting this user will cause them to become the admin, and you to become a participant.\nAre you sure you want to do that?");
 					if (choice != JOptionPane.OK_OPTION) {
 						return;
 					} else {
 						server.changeUserPrivledge(clicker, roomName,
 								CoLabPrivilegeLevel.PARTICIPANT);
 						server.changeUserPrivledge(clickee, roomName, newPriv);
 					}
 				} else {
 					server.changeUserPrivledge(clickee, roomName, newPriv);
 				}
 			}
 		};
 		private ActionListener demoteAction = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (getFromID(clickee).getPriv() == CoLabPrivilegeLevel.OBSERVER)
 					return;
 				server.changeUserPrivledge(clickee, roomName, CoLabPrivilegeLevel.OBSERVER);
 			}
 		};
 		private ActionListener kickOutAction = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				server.leaveCoLabRoom(clickee, roomName);
 			}
 		};
 	}
 
 	public void setUser(String username) {
 		this.username = username;
 	}
 }
