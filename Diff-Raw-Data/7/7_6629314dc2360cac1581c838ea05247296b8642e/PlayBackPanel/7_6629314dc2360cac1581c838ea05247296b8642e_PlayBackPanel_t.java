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
 package org.mcuosmipcuter.orcc.soundvis.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.sound.sampled.FloatControl;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JToggleButton;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.mcuosmipcuter.orcc.api.soundvis.AudioInputInfo;
 import org.mcuosmipcuter.orcc.api.soundvis.VideoOutputInfo;
 import org.mcuosmipcuter.orcc.api.util.TimeAndRateHelper;
 import org.mcuosmipcuter.orcc.soundvis.Context;
 import org.mcuosmipcuter.orcc.soundvis.Context.AppState;
 import org.mcuosmipcuter.orcc.soundvis.Context.Listener;
 import org.mcuosmipcuter.orcc.soundvis.Context.PropertyName;
 import org.mcuosmipcuter.orcc.soundvis.Mixin;
 import org.mcuosmipcuter.orcc.soundvis.PlayPauseStop;
 import org.mcuosmipcuter.orcc.soundvis.PlayPauseStopFactory;
 import org.mcuosmipcuter.orcc.soundvis.Renderer;
 import org.mcuosmipcuter.orcc.soundvis.gui.listeners.StopActionListener;
 import org.mcuosmipcuter.orcc.soundvis.gui.widgets.FrameLabel;
 import org.mcuosmipcuter.orcc.soundvis.gui.widgets.PlayPauseButton;
 import org.mcuosmipcuter.orcc.soundvis.gui.widgets.TimeLabel;
 import org.mcuosmipcuter.orcc.soundvis.threads.PlayThread;
 
 
 
 /**
  * Panel that allows play back and provides the necessary GUI elements 
  * and listens to the {@link Context}
  * @author Michael Heinzelmann
  */
 public class PlayBackPanel extends JPanel implements Mixin{
 
 	private static final long serialVersionUID = 1L;
 
 	private long sampleLength;
 	private float sampleRate;
 	private long sampleCount;
 	private int samplesPerFrame;
 	
 	private int progressPaintState;
 	
 	private TimeLinePanel timeLine = new TimeLinePanel();
 	private JScrollPane timeLineScrollPane = new JScrollPane(timeLine);
 	private JSlider volume;
 	private FrameLabel frameCountlabel = new FrameLabel();
 	private TimeLabel timeLabel = new TimeLabel();
 	private JLabel stateLabel = new JLabel();
 	private JProgressBar jProgressBar = new JProgressBar();
 	private PlayPauseButton playPause;
 	private JToggleButton autoZoom = new JToggleButton("fit/zoom");
 
 	private final SpinnerNumberModel modelPreRun = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 10);
 	private final JSpinner preRunFrames = new JSpinner(modelPreRun);
 	
 	/**
 	 * Sets up a stop, play/pause button and a status label
 	 * @param renderer the renderer to work with
 	 */
 	public PlayBackPanel(final Renderer renderer) {
 		PlayPauseStopFactory np = new PlayPauseStopFactory() {		
 			@Override
 			public PlayPauseStop newPlayPauseStop() {
 				progressPaintState = 1;
 				return new PlayThread(renderer);
 			}
 		};
 		volume  = new JSlider(){
 			private static final long serialVersionUID = 1L;
 			@Override
 			protected void paintComponent(Graphics g) {
 				super.paintComponent(g);
 				g.setXORMode(Color.BLACK);
 				g.drawString("volume", 20, 20);
 				g.setPaintMode();
 			}};
 		playPause = new PlayPauseButton(np);
 		volume.setToolTipText("Volume");
 		volume.setValue(0);
 		volume.setEnabled(false);
 		volume.setPaintTrack(false);
 		volume.setBorder(new EtchedBorder());
 		volume.addChangeListener(new ChangeListener() {
 			
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				FloatControl vol = Context.getVolumeControl();
 				setVolume(vol);
 			}
 		});
 	}
 	private void setVolume(FloatControl vol) {
 		if(vol != null) {
 			int value = volume.getValue();
 			if(value >= vol.getMinimum() && value <= vol.getMaximum()) {
 				vol.setValue(value);
 			}
 			else {
 				int v = (int)Math.max((int)vol.getValue(), vol.getMinimum());
 				volume.setValue(v);
 			}
 		}
 	}
 	/**
 	 * Setup the panel components
 	 */
 	public void init() {
 		setBorder(new LineBorder(Color.WHITE, 5));
 		jProgressBar.setMaximum(100);
 		jProgressBar.setStringPainted(true);
 		frameCountlabel.reset();
 		timeLabel.reset();
 		stateLabel.setText(String.valueOf(Context.getAppState()));
 		final JButton stop = new JButton("[]");
 
 
 		playPause.setEnabled(false);
 		stop.addActionListener(new StopActionListener(playPause));
 		stop.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				progressPaintState = 0;
 				playPause.reset();
 				long startFrame = Context.getSongPositionPointer();
 				frameCountlabel.update(startFrame);
 				sampleCount = startFrame * samplesPerFrame;
 				updateProgress();
 				timeLine.repaint();
 			}
 		});
 		Context.addListener(new Listener() {
 			
 			@Override
 			public void contextChanged(PropertyName propertyName) {
 				if(PropertyName.AppState.equals(propertyName)) {
 					AppState appState = Context.getAppState();
 					stateLabel.setText(String.valueOf(appState));
 					stop.setEnabled(appState != AppState.EXPORTING);
 					if(appState == AppState.READY) {
 						playPause.reset();
 					}
 					playPause.setEnabled(appState != AppState.EXPORTING);
					if(appState == AppState.EXPORTING) {
						progressPaintState = 1;
						playPause.reset();
						frameCountlabel.update(0);
						sampleCount = 0;
						updateProgress();
					}
 				}
 				if(PropertyName.AudioInputInfo.equals(propertyName)) {
 					playPause.setEnabled(Context.getAudioInput() != null);
 					autoZoom.setEnabled(Context.getAudioInput() != null);
 				}
 				if(PropertyName.AudioInputInfo.equals(propertyName) || PropertyName.VideoFrameRate.equals(propertyName) ) {
 					samplesPerFrame = TimeAndRateHelper.getSamplesPerFrame(Context.getAudioInput().getAudioInputInfo(), Context.getVideoOutputInfo());
 				}
 				if(PropertyName.VolumeControl.equals(propertyName)) {
 					FloatControl vol = Context.getVolumeControl();
 					if(vol != null) {
 						volume.setMinimum((int)vol.getMinimum());
 						volume.setMaximum((int)vol.getMaximum());
 						volume.setEnabled(true);
 						volume.setPaintTicks(true);
 						volume.setMajorTickSpacing(20);
 						setVolume(vol);
 					}
 				}
 			}
 		});
 		autoZoom.setSelected(false);
 		autoZoom.setEnabled(false);
 		autoZoom.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				timeLine.setAutoZoom(! autoZoom.isSelected());
 				timeLine.setInputOutputData();			
 				if( autoZoom.isSelected()) {
 					scrollToSelection();	
 				}
 			}
 		});
 
 		preRunFrames.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				timeLine.setPreRunFrames((Integer)preRunFrames.getValue());
 			}
 		});
 		
 		SpinnerNumberModel modelZoom = new SpinnerNumberModel(100, 10, 60000, 10);
 		final JSpinner framesToZoom = new JSpinner(modelZoom);
 		final JPanel fz = new JPanel();
 		fz.setLayout(new BorderLayout());
 		fz.add(framesToZoom, BorderLayout.WEST);
 		fz.add(new JLabel("samples"));
 		
 		JMenu waveSettings = new JMenu("wave");
 		JMenu set = new JMenu("set");
 		waveSettings.add(set);
 		JMenuItem zoom = new JMenuItem("zoom");
 		set.add(zoom);
 		zoom.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				final int ftzOld = (Integer)framesToZoom.getValue();
 				JOptionPane.showMessageDialog(null, fz, "wave zoom to number of samples", 
 						JOptionPane.PLAIN_MESSAGE);
 				int ftz = (Integer)framesToZoom.getValue(); 
 				if(ftz != ftzOld) {
 					timeLine.setAutoZoom(false);
 					autoZoom.setSelected(true);
 					timeLine.setSamplesToZoom(ftz);
 					timeLine.setInputOutputData();	
 					scrollToSelection();
 				}
 			}
 		});
 		
 		GridLayout gl = new GridLayout(1, 10, 5, 10);		
 		
 		JPanel commands = new JPanel();
 		commands.setBorder(new LineBorder(Color.WHITE, 2));
 		commands.setLayout(gl);
 		
 		commands.add(preRunFrames);
 		commands.add(new JLabel(" frames prerun"));
 		commands.add(stop);
 		commands.add(playPause);
 		commands.add(stateLabel);
 		commands.add(volume);
 		commands.add(autoZoom);
 		
 		JMenuBar waveBar = new JMenuBar();
 		waveBar.add(waveSettings);
 		commands.add(waveBar);
 
 		commands.add(frameCountlabel);
 		commands.add(timeLabel);
 		commands.add(jProgressBar);
 		
 		timeLine.setPreferredSize(new Dimension(600, 150));
 		timeLine.setGuiWidth(getWidth());
 		setLayout(new BorderLayout());
 		add(commands, BorderLayout.NORTH);
 		
 		add(timeLineScrollPane, BorderLayout.SOUTH);
 	}
 
 	@Override
 	public void newFrame(long frameCount, boolean sendPost) {
 		frameCountlabel.update(frameCount);
 		updateProgress();
 		if(progressPaintState == 1) {
 			timeLine.repaint();
 			progressPaintState = 2;
 		}
 		if(progressPaintState == 2) {
 			timeLine.paintProgress();
 		}
 		
 	}
 	@Override
 	public boolean nextSample(int[] amplitudes, byte[] rawData, long sampleCount) {
 		this.sampleCount = sampleCount;
 		if(sampleCount >= sampleLength) {
 			updateProgress();
 			playPause.reset();
 			timeLine.repaint();
 		}
 		return true; // always continue
 	}
 	@Override
 	public void start(AudioInputInfo audioInputInfo, VideoOutputInfo videoOutputInfo) {
 		sampleLength = audioInputInfo.getFrameLength();
 		sampleRate = audioInputInfo.getAudioFormat().getSampleRate();
 	}
 	private void updateProgress() {
 		if(sampleRate != 0) {
 			double d = (double)sampleCount / (double)sampleLength;
 			jProgressBar.setValue((int)(d * 100));
 			timeLabel.update(sampleCount, sampleRate);
 			timeLine.setSamplePosition(sampleCount);
 		}
 	}
 
 	private void scrollToSelection() {
 		int s = timeLine.getSelectPos();
 		int w = timeLine.getWidth();
 		Rectangle a = new Rectangle(s - getWidth() / 2, 0, w, 100);
 		timeLineScrollPane.getViewport().scrollRectToVisible(a);	
 	}
 	
 	public synchronized TimeLinePanel getTimeLine() {
 		return timeLine;
 	}
 
 	
 }
 
