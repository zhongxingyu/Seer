 /**
 *   ORCC rapid content creation for entertainment, education and media production
 *   Copyright (C) 2012 Michael Heinzelmann, Michael Heinzelmann IT-Consulting
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package org.mcuosmipcuter.orcc.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.List;
 
 import javax.sound.sampled.AudioFormat;
 import javax.swing.JDesktopPane;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
 import org.mcuosmipcuter.orcc.gui.table.CustomTable;
 import org.mcuosmipcuter.orcc.soundvis.AppLogicException;
 import org.mcuosmipcuter.orcc.soundvis.AudioInput;
 import org.mcuosmipcuter.orcc.soundvis.Context;
 import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
 import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
 import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
 import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
 import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopHolder;
 import org.mcuosmipcuter.orcc.soundvis.SoundCanvasWrapper;
 import org.mcuosmipcuter.orcc.soundvis.gui.AboutBox;
 import org.mcuosmipcuter.orcc.soundvis.gui.CanvasClassMenu;
 import org.mcuosmipcuter.orcc.soundvis.gui.FrameRateMenu;
 import org.mcuosmipcuter.orcc.soundvis.gui.GraphPanel;
 import org.mcuosmipcuter.orcc.soundvis.gui.PlayBackPanel;
 import org.mcuosmipcuter.orcc.soundvis.gui.ResolutionMenu;
 import org.mcuosmipcuter.orcc.soundvis.gui.ZoomMenu;
 import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener;
 import org.mcuosmipcuter.orcc.soundvis.gui.listeners.FileDialogActionListener.CallBack;
 import org.mcuosmipcuter.orcc.soundvis.gui.listeners.StopActionListener;
 import org.mcuosmipcuter.orcc.soundvis.gui.widgets.TimeLabel;
 import org.mcuosmipcuter.orcc.soundvis.util.ExportUtil;
 import org.mcuosmipcuter.orcc.util.IOUtil;
 
 
 /**
  * Main method class
  * @author Michael Heinzelmann
  */
 public class Main {
 	
 	static final int infoW = 490;
 	static final int infoH = 200;
 	static final int playBackH = 240;
 	static final int minCells = 3;
 	
 	/**
 	 * @param args
 	 * @throws UnsupportedLookAndFeelException 
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 * @throws ClassNotFoundException 
 	 */
 	public static void main(String[] args) throws Exception {
 		
 		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 			public void uncaughtException(Thread thread, Throwable t) {
 				System.err.println("UNCAUGHT Exception in " + thread);
 				t.printStackTrace();
 				String msg = t.getClass().getSimpleName() + ": " + t.getMessage();
 				JOptionPane.showConfirmDialog(null, msg, "Error",
 	                    JOptionPane.DEFAULT_OPTION,
 	                    JOptionPane.ERROR_MESSAGE);
 			}
 		});
 		org.mcuosmipcuter.orcc.gui.Configuration.init(args);
 		
 		final JFrame frame = new JFrame("soundvis");
 		
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				exitRoutine();
 			}
 		});
 		
 		final GraphPanel graphicPanel = new GraphPanel();		
 
 		
 		org.mcuosmipcuter.orcc.gui.Configuration.stage1(args);
 		
 		JMenuBar mb = new JMenuBar();
 		frame.setJMenuBar(mb);
 		{
 			mb.add(new JMenu("  "));
 			
 			JMenu fileMenu = new JMenu("File");
 			mb.add(fileMenu);
 			{
 				JMenuItem openAudio = new JMenuItem("open audio");
 				fileMenu.add(openAudio);
 				CallBack openAudioCallback = new CallBack() {
 					public void fileSelected(File file) {
 						try {
 							Context.setAudioFromFile(file.getAbsolutePath());
 						} catch (AppLogicException ex) {
 							throw new RuntimeException(ex);
 						}
 					}
 				};
 				FileDialogActionListener importActionListener 
 					= new FileDialogActionListener(null, openAudioCallback, "open as audio input");
 				openAudio.addActionListener(importActionListener);
 				fileMenu.addSeparator();
 				
 				JMenuItem exit = new JMenuItem("exit");
 				fileMenu.add(exit);
 				exit.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						exitRoutine();
 					}
 				});
 			}
 			
 			final JMenu exportMenu = new JMenu("Export");
 			final JMenuItem exportStart = new JMenuItem("start");
 			final JMenuItem exportStop = new JMenuItem("stop");
 			mb.add(exportMenu);
 			{
 				CallBack exportVideo = new CallBack() {
 					public void fileSelected(File file) {
 						Context.setExportFileName(file.getAbsolutePath());
 						final PlayPauseStop exportThread = ExportUtil.getExportPlayPause(graphicPanel);
 						for(ActionListener a : exportStop.getActionListeners()) {
 							exportStop.removeActionListener(a);
 						}
 						exportStop.addActionListener(new StopActionListener(new PlayPauseStopHolder() {		
 							@Override
 							public PlayPauseStop getPlayPauseStop() {
 								return exportThread;
 							}
 						}));
 						exportThread.startPlaying();
 					}
 				};
 				FileDialogActionListener exportActionListener = new FileDialogActionListener(frame, exportVideo, "set as export file");
 				exportStart.addActionListener(exportActionListener);
 				if(ExportUtil.isExportEnabled()) {
 					exportMenu.add(exportStart);
 					exportMenu.addSeparator();
 					exportMenu.add(exportStop);
 				}
 				else {
 					exportMenu.add(new JMenuItem("not enabled"));
 				}
 				Context.addListener(new Listener() {
 					public void contextChanged(PropertyName propertyName) {
 						if(PropertyName.AppState.equals(propertyName)) {
 							exportMenu.setEnabled(Context.getAppState() == AppState.READY || Context.getAppState() == AppState.EXPORTING);
 							exportStart.setEnabled(Context.getAppState() != AppState.EXPORTING);
 							exportStop.setEnabled(Context.getAppState() == AppState.EXPORTING);
 						}
 					}
 				});
 			}
 			
 			final JMenu helpMenu = new JMenu("Help");
 			mb.add(helpMenu);
 			{
 				JMenuItem about = new JMenuItem("about");
 				helpMenu.add(about);
 				about.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						AboutBox.showModal();
 					}
 				});
 			}
 		}
 		final JInternalFrame playBackFrame = new JInternalFrame("Timeline");
 		final JInternalFrame graphicFrame = new JInternalFrame("Graph", false, false, true, false);
 		final JDesktopPane deskTop = new JDesktopPane();
 		deskTop.setDesktopManager(new CustomDeskTopManager(playBackFrame, graphicFrame));
 		deskTop.setVisible(true);
 		
 		frame.getContentPane().add(deskTop);
 		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
 		frame.setVisible(true);
 		
 		final PlayBackPanel playBackPanel = new PlayBackPanel(graphicPanel);
 		IOUtil.log("frame size: " + frame.getSize());
 		{
 			deskTop.add(playBackFrame);
 			
 			{
 				playBackFrame.getContentPane().add(playBackPanel, BorderLayout.SOUTH);
 				graphicPanel.setMixin(playBackPanel);
 			}
 			
 			playBackFrame.setSize(deskTop.getWidth(), playBackH);
 			playBackFrame.setVisible(true);
 			playBackPanel.init();
 			Context.addListener(new Listener() {		
 				@Override
 				public void contextChanged(PropertyName propertyName) {
 					if(PropertyName.AudioInputInfo.equals(propertyName)) {
 						AudioInput audioInput = Context.getAudioInput();
 						final AudioFormat audioFormat = audioInput.getAudioInputInfo().getAudioFormat();
 						TimeLabel tl = new TimeLabel();
 						tl.update(audioInput.getAudioInputInfo().getFrameLength(), audioFormat.getSampleRate());
 						playBackFrame.setTitle(audioInput.getName() + " | " + ((int)audioFormat.getSampleRate()) + " HZ | " + audioFormat.getSampleSizeInBits() + " bit | length " + tl.getText());
 					}
 				}
 			});
 		}
 
 		{
 			final JInternalFrame propertiesFrame = new JInternalFrame("Layers");
 			JMenuBar layersMenuBar = new JMenuBar();
 			final JMenu canvas = new JMenu("canvas");
 			layersMenuBar.add(canvas);
 			CanvasClassMenu classes = new CanvasClassMenu("add canvas");
 			canvas.add(classes);
 			propertiesFrame.setJMenuBar(layersMenuBar);
 			final CustomTable propTable = new CustomTable();
 			propTable.setListener(playBackPanel.getTimeLine());
 			
 	        JPanel container = new JPanel();
 	        container.setOpaque(true);
 	        container.setLayout(new BorderLayout());
 	        container.add(propTable, BorderLayout.NORTH);     
 	        JScrollPane scrollPane = new JScrollPane(container);
 	        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 			propertiesFrame.add(scrollPane);
 			
 			{
 				Context.addListener(new Listener() {	
 					@Override
 					public void contextChanged(PropertyName propertyName) {
 						if(PropertyName.SoundCanvasAdded.equals(propertyName)) {
 							List<SoundCanvasWrapper> list = Context.getSoundCanvasList();
 							
 							propTable.addLayer(list.get(list.size() - 1));
 						}
 						if(PropertyName.AppState.equals(propertyName)) {
 							propTable.setEnabled(Context.getAppState() == AppState.READY || Context.getAppState() == AppState.PAUSED);
 							canvas.setEnabled(Context.getAppState() == AppState.READY);
 						}
 					}
 				});
 
 			}
 			
 			propertiesFrame.setLocation(0, playBackH);
 			propertiesFrame.setVisible(true);
 			propertiesFrame.setSize(infoW, deskTop.getHeight() - playBackH);
 			deskTop.add(propertiesFrame);
 		}	
 
 		{
 			JMenuBar graphicMenuBar = new JMenuBar();
 			graphicFrame.setJMenuBar(graphicMenuBar);
 			{			
 				
 				final JMenu configMenu = new JMenu("Configuration");
 				graphicMenuBar.add(configMenu);
 
 				VideoOutputInfo v = Context.getVideoOutputInfo();
 				configMenu.add(new ResolutionMenu("video size", v.getWidth(), v.getHeight()));
 				final FrameRateMenu frameRates = new FrameRateMenu("frame rate", v.getFramesPerSecond());
 				configMenu.addSeparator();
 				configMenu.add(frameRates);
 
 				// context listener for menu enabling
 				Context.addListener(new Listener() {
 					public void contextChanged(PropertyName propertyName) {
 						if(PropertyName.AppState.equals(propertyName)) {
 							configMenu.setEnabled(Context.getAppState() == AppState.READY);
 						}
 						if(PropertyName.AudioInputInfo.equals(propertyName)) {
 							frameRates.checkFrameRatesEnabled(Context.getAudioInput().getAudioInputInfo());
 						}
 					}
 				});
 			}
 			final JMenu viewMenu = new JMenu("View");
 			graphicMenuBar.add(viewMenu);
 			viewMenu.add(new ZoomMenu("zoom", 0.0f, graphicPanel));
 			graphicPanel.setZoomFactor(0.0f);
 			graphicPanel.setOpaque(true);
 			
 			deskTop.add(graphicFrame);
 			{
 				graphicFrame.getContentPane().add(graphicPanel);
 			}
 			graphicFrame.setSize(deskTop.getWidth() - infoW, deskTop.getHeight() - playBackH);
 			graphicFrame.setLocation(infoW, playBackH);
 			graphicFrame.setVisible(true);
 			
 			Context.addListener(new Listener() {
 				public void contextChanged(PropertyName propertyName) {
 					if(PropertyName.SoundCanvasAdded.equals(propertyName)||
 							PropertyName.SoundCanvasRemoved.equals(propertyName)||
 							PropertyName.VideoDimension.equals(propertyName) || 
 							PropertyName.VideoFrameRate.equals(propertyName)) { 
 						String title = Context.getVideoOutputInfo().getWidth() 
 								+ "x" + Context.getVideoOutputInfo().getHeight() + "p  @"
 								+ Context.getVideoOutputInfo().getFramesPerSecond() + "fps | " +
 								Context.getSoundCanvasList();
 						graphicFrame.setTitle(title);
 						graphicPanel.preView();
 					}
 				}
 			});
 
 
 		}
 		
 		org.mcuosmipcuter.orcc.gui.Configuration.stage2(args);
 		
 	}
 	
 	private static void exitRoutine() {
 		if(Context.getAppState() != AppState.READY) {
 			int res = JOptionPane.showOptionDialog(null, "Confirm exit in state " 
 		+ Context.getAppState(), "Do you want to exit in state " + Context.getAppState() + " ?", 
 		JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] {"ok", "cancel"}, "cancel");
 			if(res != JOptionPane.OK_OPTION) {
 				return;
 			}
 		}
 		System.exit(0);
 	}
 }
 
