 /*
  * GroupChatPanel.java
  *
  * Created on __DATE__, __TIME__
  */
 
 package com.lorent.vovo.ui;
 
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.util.Date;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 
 import com.lorent.vovo.Vovo;
 import com.lorent.vovo.dto.FontStyle;
 import com.lorent.vovo.dto.GroupInfo;
 import com.lorent.vovo.ui.InputArea.EventListener;
 import com.lorent.vovo.util.DataUtil;
 import com.lorent.vovo.util.FontUtil;
 import com.lorent.vovo.util.VovoStringUtil;
import com.lorent.vovo.util.Constants.DataKey;
 
 /**
  *
  * @author  Jack
  */
 public class GroupChatPanel extends javax.swing.JPanel {
 
 	private Logger log = Logger.getLogger(GroupChatPanel.class);
 
 	/** Creates new form GroupChatPanel */
 	public GroupChatPanel() {
 		initComponents();
 		jPanel6.add(this.inputArea);
 		inputArea.setEventListener(new EventListener() {
 
 			@Override
 			public void sendMsg(String msg, FontStyle style, Map<String,String> imgs) {
 				try {
 					long time = Vovo.getLcmUtil().getSystemTime();
 					Vovo.exeC("groupChat", "sendGroupChatMsg", msg, style, info
 							.getRoomJID(),imgs, time);
 					insertMyMsg(msg, style, new Date(time),imgs);
 				} catch (Exception e) {
 					log.error("insert my msg error", e);
 					JOptionPane.showMessageDialog(GroupChatPanel.this,
 							"insert my msg error", "error",
 							JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		shareFileListPanel.setParentFtpPath("GroupChatFilePath");
 		centerMainPanel.add(shareFileListPanel, "shareFilePanel");
 		this.soundBtn.setVisible(false);
 		this.videoBtn.setVisible(false);
 		msgRecordPanel = new MsgRecordPanel();
 	}
 
 	//GEN-BEGIN:initComponents
 	// <editor-fold defaultstate="collapsed" desc="Generated Code">
 	private void initComponents() {
 		java.awt.GridBagConstraints gridBagConstraints;
 
 		jPanel1 = new javax.swing.JPanel();
 		headLbl = new javax.swing.JLabel();
 		nameLbl = new javax.swing.JLabel();
 		jPanel2 = new javax.swing.JPanel();
 		videoBtn = new javax.swing.JButton();
 		soundBtn = new javax.swing.JButton();
 		groupSetButton = new javax.swing.JButton();
 		shareFileToggleButton = new javax.swing.JToggleButton();
 		showMsgBtn = new javax.swing.JToggleButton();
 		descLbl = new javax.swing.JLabel();
 		centerMainPanel = new javax.swing.JPanel();
 		chatPanel = new javax.swing.JPanel();
 		jPanel4 = new javax.swing.JPanel();
 		jScrollPane1 = new javax.swing.JScrollPane();
 		showPane = new com.lorent.vovo.ui.MyTextPane();
 		jPanel6 = new javax.swing.JPanel();
 		jPanel5 = new javax.swing.JPanel();
 		operateTab = new javax.swing.JTabbedPane();
 
 		setBorder(javax.swing.BorderFactory.createEtchedBorder());
 		setOpaque(false);
 		setLayout(new java.awt.GridBagLayout());
 
 		jPanel1.setOpaque(false);
 		jPanel1.setLayout(new java.awt.GridBagLayout());
 
 		headLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/vovo/resource/images/group60.png"))); // NOI18N
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.gridheight = 2;
 		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
 		jPanel1.add(headLbl, gridBagConstraints);
 
 		nameLbl.setText("Jack");
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
 		jPanel1.add(nameLbl, gridBagConstraints);
 
 		jPanel2.setOpaque(false);
 		jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
 
 		videoBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/vovo/resource/images/camera.png"))); // NOI18N
 		videoBtn.setToolTipText(VovoStringUtil
 				.getUIString("chatwindow.videobtn.tip"));
 		videoBtn.setContentAreaFilled(false);
 		videoBtn.setPreferredSize(new java.awt.Dimension(32, 32));
 		jPanel2.add(videoBtn);
 
 		soundBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/vovo/resource/images/microphone.png"))); // NOI18N
 		soundBtn.setToolTipText(VovoStringUtil
 				.getUIString("chatwindow.audiobtn.tip"));
 		soundBtn.setContentAreaFilled(false);
 		soundBtn.setPreferredSize(new java.awt.Dimension(32, 32));
 		jPanel2.add(soundBtn);
 
 		groupSetButton.setIcon(new javax.swing.ImageIcon(getClass()
 				.getResource("/com/lorent/vovo/resource/images/gear.png"))); // NOI18N
 		groupSetButton.setToolTipText(VovoStringUtil
 				.getUIString("chatwindow.groupchatsetbtn.tip"));
 		groupSetButton.setContentAreaFilled(false);
 		groupSetButton.setPreferredSize(new java.awt.Dimension(32, 32));
 		groupSetButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				groupSetButtonActionPerformed(evt);
 			}
 		});
 		jPanel2.add(groupSetButton);
 
 		shareFileToggleButton.setIcon(new javax.swing.ImageIcon(getClass()
 				.getResource(
 						"/com/lorent/vovo/resource/images/folder-remote.png"))); // NOI18N
 		shareFileToggleButton.setToolTipText(VovoStringUtil
 				.getUIString("chatwindow.groupfilebtn.tip"));
 		shareFileToggleButton.setContentAreaFilled(false);
 		shareFileToggleButton.setPreferredSize(new java.awt.Dimension(32, 32));
 		shareFileToggleButton
 				.addItemListener(new java.awt.event.ItemListener() {
 					public void itemStateChanged(java.awt.event.ItemEvent evt) {
 						shareFileToggleButtonItemStateChanged(evt);
 					}
 				});
 		jPanel2.add(shareFileToggleButton);
 
 		showMsgBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/vovo/resource/images/show_msg.png"))); // NOI18N
 		showMsgBtn.setToolTipText(VovoStringUtil
 				.getUIString("chatwindow.chatrecordbtn.tip"));
 		showMsgBtn.setContentAreaFilled(false);
 		showMsgBtn.setPreferredSize(new java.awt.Dimension(32, 32));
 		showMsgBtn.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				showMsgBtnActionPerformed(evt);
 			}
 		});
 		jPanel2.add(showMsgBtn);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.gridwidth = 2;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
 		jPanel1.add(jPanel2, gridBagConstraints);
 
 		descLbl.setText("desc");
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 2;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
 		jPanel1.add(descLbl, gridBagConstraints);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.weightx = 1.0;
 		add(jPanel1, gridBagConstraints);
 
 		centerMainPanel.setLayout(new java.awt.CardLayout());
 
 		chatPanel.setLayout(new java.awt.GridBagLayout());
 
 		jPanel4.setMinimumSize(new java.awt.Dimension(450, 23));
 		jPanel4.setPreferredSize(new java.awt.Dimension(450, 23));
 		jPanel4.setLayout(new java.awt.BorderLayout());
 
 		jScrollPane1.setMinimumSize(new java.awt.Dimension(450, 356));
 		jScrollPane1.setPreferredSize(new java.awt.Dimension(450, 23));
 
 		showPane.setEditable(false);
 		jScrollPane1.setViewportView(showPane);
 
 		jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
 		jPanel6.setLayout(new java.awt.BorderLayout());
 		jPanel4.add(jPanel6, java.awt.BorderLayout.SOUTH);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		chatPanel.add(jPanel4, gridBagConstraints);
 
 		jPanel5.setMinimumSize(new java.awt.Dimension(200, 5));
 		jPanel5.setPreferredSize(new java.awt.Dimension(200, 5));
 		jPanel5.setLayout(new java.awt.BorderLayout());
 		jPanel5.add(operateTab, java.awt.BorderLayout.CENTER);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
 		gridBagConstraints.weighty = 1.0;
 		chatPanel.add(jPanel5, gridBagConstraints);
 
 		centerMainPanel.add(chatPanel, "chatPanel");
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		add(centerMainPanel, gridBagConstraints);
 	}// </editor-fold>
 	//GEN-END:initComponents
 
 	private void showMsgBtnActionPerformed(java.awt.event.ActionEvent evt) {
 		if (existTab(msgRecordPanel)) {
 			removeOperateTab(msgRecordPanel);
 		} else {
 			addOperateTab(Vovo.getViewManager().getUIString(
 					"operateTab.msgrecord"), msgRecordPanel);
 			Vovo.exeC("groupChat", "getLatestRecord", info.getRoomJID(),
 					msgRecordPanel);
 			CardLayout layout = (CardLayout) centerMainPanel.getLayout();
 			layout.show(centerMainPanel, "chatPanel");
 		}
 	}
 
 	private void shareFileToggleButtonItemStateChanged(
 			java.awt.event.ItemEvent evt) {
 		CardLayout layout = (CardLayout) centerMainPanel.getLayout();
 		layout.next(centerMainPanel);
 	}
 
 	private void groupSetButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		GroupMemberListPanel panel = DataUtil.getGroupMemberListPanel(info
 				.getRoomJID());
 		Vovo.exeC("groupChat", "showGroupChatSetFrame", panel);
 	}
 
 	//GEN-BEGIN:variables
 	// Variables declaration - do not modify
 	private javax.swing.JPanel centerMainPanel;
 	private javax.swing.JPanel chatPanel;
 	private javax.swing.JLabel descLbl;
 	private javax.swing.JButton groupSetButton;
 	private javax.swing.JLabel headLbl;
 	private javax.swing.JPanel jPanel1;
 	private javax.swing.JPanel jPanel2;
 	private javax.swing.JPanel jPanel4;
 	private javax.swing.JPanel jPanel5;
 	private javax.swing.JPanel jPanel6;
 	private javax.swing.JScrollPane jScrollPane1;
 	private javax.swing.JLabel nameLbl;
 	private javax.swing.JTabbedPane operateTab;
 	private javax.swing.JToggleButton shareFileToggleButton;
 	private javax.swing.JToggleButton showMsgBtn;
 	private com.lorent.vovo.ui.MyTextPane showPane;
 	private javax.swing.JButton soundBtn;
 	private javax.swing.JButton videoBtn;
 	// End of variables declaration//GEN-END:variables
 	private ShareFileListPanel shareFileListPanel = new ShareFileListPanel();
 	private GroupMemberListPanel groupMemberPanel;
 	private InputArea inputArea = new InputArea();
 	private MsgRecordPanel msgRecordPanel;
 
 	public void setGroupMemberPanel(GroupMemberListPanel groupMemberPanel) {
 		this.groupMemberPanel = groupMemberPanel;
 		addOperateTab(Vovo.getViewManager().getUIString(
 				"operateTab.groupmember"), groupMemberPanel);
 	}
 
 	private void addOperateTab(String title, JPanel panel) {
 		boolean exist = existTab(panel);
 		if (!exist) {
 			operateTab.addTab(title, panel);
 		}
 	}
 
 	private void removeOperateTab(JPanel panel) {
 		operateTab.remove(panel);
 	}
 
 	private boolean existTab(JPanel panel) {
 		Component[] components = operateTab.getComponents();
 		boolean exist = false;
 		for (Component component : components) {
 			if (component == panel) {
 				exist = true;
 				break;
 			}
 		}
 		return exist;
 	}
 
 	private GroupInfo info;
 
 	public void setInfo(GroupInfo info) {
 		this.info = info;
 
 		this.nameLbl.setText(info.getTopic());
 		this.descLbl.setText(info.getDesc());
 	}
 
 	public void hiddenGroupSetButton() {
 		this.groupSetButton.setVisible(false);
 	}
 
 	public void insertFriendMsg(String name, String msg, FontStyle fontStyle,
 			Date sendDate, Map<String,String> imgs) throws Exception {
 		showPane.insertMsgAndTitle(name, FontUtil.getFriendNameStyle(),
 				sendDate, FontUtil.getFriendDateStyle(), msg, (Map) DataUtil
 						.getValue(DataKey.SYSTEM_EMOTION), fontStyle, imgs);
 	}
 
 	public void insertMyMsg(String msg, FontStyle fontStyle, Date sendDate, Map<String,String> imgs)
 			throws Exception {
 		showPane.insertMsgAndTitle(DataUtil.getMyInfo().getRealName(), FontUtil
 				.getMyNameStyle(), sendDate, FontUtil.getMyDateStyle(), msg,
 				(Map) DataUtil.getValue(DataKey.SYSTEM_EMOTION), fontStyle,imgs);
 	}
 
 	public void initShareFilePanel(String sessionID) throws Exception {
 		//GROUP_CHAT_SESSION_PREFIX + info.getRoomJid
 		shareFileListPanel.setSessionID(sessionID);
 		Vovo.exeC("sharefile", "getDefaultFileList", shareFileListPanel);
 	}
 
 }
