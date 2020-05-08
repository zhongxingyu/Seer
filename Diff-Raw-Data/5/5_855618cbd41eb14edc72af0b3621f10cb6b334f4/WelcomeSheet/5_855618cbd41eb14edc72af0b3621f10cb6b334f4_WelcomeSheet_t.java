 package com.robonobo.gui.sheets;
 
 import static com.robonobo.gui.GuiUtil.*;
 import info.clearthought.layout.TableLayout;
 
 import java.awt.ComponentOrientation;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.*;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.core.Platform;
 import com.robonobo.gui.RoboFont;
 import com.robonobo.gui.components.FileChoosePanel;
 import com.robonobo.gui.components.base.*;
 import com.robonobo.gui.frames.RobonoboFrame;
 
 @SuppressWarnings("serial")
 public class WelcomeSheet extends Sheet {
 	private RButton feckOffBtn;
 	private FileChoosePanel filePanel;
 
 	public WelcomeSheet(RobonoboFrame rFrame) {
 		super(rFrame);
		Dimension sz = new Dimension(540, 375);
 		setPreferredSize(sz);
 		setSize(sz);
 		double[][] cells = { { 20, 270, 20, 220, 20 }, { 20, 40/* title */, 20, 20/*intro*/, 10, 40/* dir blurb */, 5, 25/* filechoose */, 20, TableLayout.FILL/* addstuff */, 20, 32/* feckoff */, 10 } };
 		setLayout(new TableLayout(cells));
 		setName("playback.background.panel");
 		JPanel titlePnl = new JPanel();
 		titlePnl.setLayout(new BoxLayout(titlePnl, BoxLayout.X_AXIS));
 		titlePnl.add(Box.createHorizontalStrut(72));
 		titlePnl.add(new RLabel36B("Welcome to"));
 		titlePnl.add(Box.createHorizontalStrut(10));
 		JLabel logo = new JLabel(createImageIcon("/rbnb-logo_mid-grey-bg.png", -1, 37));
 		logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
 		titlePnl.add(logo);
 		add(titlePnl, "1,1,3,1,LEFT,TOP");
 		LineBreakTextPanel intro = new LineBreakTextPanel("Now you and your friends can hear each others' music effortlessly.", RoboFont.getFont(16, false), 560);
 		add(intro, "1,3,3,3,LEFT,TOP");
 		LineBreakTextPanel dirBlurb = new LineBreakTextPanel("All the music you play or download will be saved on your computer, in this folder:", RoboFont.getFont(16, false),
 				560);
 		add(dirBlurb, "1,5,3,5,LEFT,TOP");
 		filePanel = new FileChoosePanel(frame, frame.ctrl.getConfig().getFinishedDownloadsDirectory(), true, new CatchingRunnable() {
 			public void doRun() throws Exception {
 				File f = filePanel.chosenFile;
 				frame.ctrl.getConfig().setFinishedDownloadsDirectory(f.getAbsolutePath());
 				frame.ctrl.saveConfig();
 			}
 		});
 		add(filePanel, "1,7,3,7,LEFT,TOP");
 		JPanel addFriendsPnl = new JPanel();
		double[][] afCells = { {120, TableLayout.FILL}, {20, 10, 40, 10, 32} };
 		addFriendsPnl.setLayout(new TableLayout(afCells));
 		RLabel18B afTitle = new RLabel18B("Add friends");
 		addFriendsPnl.add(afTitle, "0,0,1,0");
 		LineBreakTextPanel afExpln = new LineBreakTextPanel("You can add friends from Facebook, or using their email addresses.", RoboFont.getFont(16, false), 270);
 		addFriendsPnl.add(afExpln,"0,2,1,2");
 		RButton addFriendsBtn = new RGlassButton("Add friends...");
 		addFriendsBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 				frame.showAddFriendsSheet();
 			}
 		});
 		addFriendsPnl.add(addFriendsBtn,"0,4");
 		add(addFriendsPnl, "1,9");
 		JPanel shareTracksPnl = new JPanel();
 		shareTracksPnl.setLayout(new BoxLayout(shareTracksPnl, BoxLayout.Y_AXIS));
 		shareTracksPnl.add(new RLabel18B("Share tracks"));
 		shareTracksPnl.add(Box.createVerticalStrut(10));
 		RButton shareFilesBtn = new RGlassButton("Share MP3 files...");
 		shareFilesBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 				frame.showAddSharesDialog();
 			}
 		});
 		shareTracksPnl.add(shareFilesBtn);
 		if (Platform.getPlatform().iTunesAvailable()) {
 			shareTracksPnl.add(Box.createVerticalStrut(10));
 			RButton shareITunesBtn = new RGlassButton("Share from iTunes...");
 			shareITunesBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					setVisible(false);
 					frame.shareFromITunes();
 				}
 			});
 			shareTracksPnl.add(shareITunesBtn);
 		}
 		add(shareTracksPnl, "3,9");
 		JPanel feckOffPnl = new JPanel();
 		feckOffPnl.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 		feckOffPnl.setLayout(new BoxLayout(feckOffPnl, BoxLayout.PAGE_AXIS));
 		feckOffPnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
 		feckOffBtn = new RRedGlassButton("Close");
 		feckOffBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 				frame.guiCfg.setShowWelcomePanel(false);
 				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
 					public void doRun() throws Exception {
 						frame.ctrl.saveConfig();
 					}
 				});
 			}
 		});
 		feckOffPnl.add(feckOffBtn);
 		add(feckOffPnl, "1,11,3,11,RIGHT,TOP");
 	}
 
 	@Override
 	public void onShow() {
 	}
 
 	@Override
 	public JButton defaultButton() {
 		return feckOffBtn;
 	}
 }
