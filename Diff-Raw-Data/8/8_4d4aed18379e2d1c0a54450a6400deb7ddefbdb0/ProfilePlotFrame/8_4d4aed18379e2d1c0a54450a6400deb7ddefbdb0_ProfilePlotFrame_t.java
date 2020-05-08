 /**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis.ui;
 
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.data.xy.XYSeries;
 import org.nees.rpi.vis.AppSettings;
 import org.nees.rpi.vis.ui.widgets.VisSaveJChartPlotToImageButton;
 
 import javax.swing.*;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 
 /**
  *
  */
 public class ProfilePlotFrame extends VisFrame
 {
 	/** the main pane of the frame which will include all layout and all child components */
 	private JPanel contentPane;
 
 	/** a slider to keep track of the user selected time */
 	private JSlider timeSlider;
 
 	/** a label to indicate the current time selected from the slider */
 	private JLabel timeLabel;
 
 	/** a button looping through the time values in sequence to simulate a movie effect for the user */
 	private VisLinkButton playButton;
 
 	/** a button to stop the current playback of the profile "movie" */
 	private VisLinkButton pauseButton;
 
 	/** a button allowing control over the slider moving the time forward one increment */
 	private VisLinkButton nextButton;
 
 	/** a button allowing control over the slider moving the time back one increment */
 	private VisLinkButton previousButton;
 
 	/** the charting panel used for the plots */
 	private ChartPanel chartPanel;
 
 	/** the xy dataset the chart object uses to plot the data */
 	private XYSeriesCollection xyDataset;
 
 	/** the time series for the displayed plot */
 	private float[] time;
 
 	/** the timer object to play the sequence of profiles as a movie */
 	private Timer playbackTimer;
 
 	/** the time in seconds the playback takes place for for the "movie" */
 	private int playbackTime = 10; // 10 seconds
 
 	/** the number of plot changes during a second; note this may be dynamically set lower if there are not enough frames */
 	private int playbackFrameRate = 10; // per second
 
 	/** the number of time increments by which the "movie" moves forward during each step */
 	private int playbackIncrement = 0;
 
 	/** the range for the axis bounds, set by the added series values as they are added */
 	private float rangeMin, rangeMax;
 
 	/** the series information stored to display the plots */
 	private ArrayList<ProfileSeries> series = new ArrayList<ProfileSeries>();
 
 	public ProfilePlotFrame()
 	{
 		super("profileplotwindow");
 
 		initFrame();
 		initChartArea();
 		initSliderArea();
 	}
 
 	private void initFrame()
 	{
 		setTitle("Profile Plot");
 		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 
 		contentPane = new JPanel();
 		contentPane.setOpaque(true);
 		contentPane.setBackground(Color.WHITE);
 		contentPane.setLayout(new BorderLayout());
 		setContentPane(contentPane);
 	}
 
 	private void initChartArea()
 	{
 		JFreeChart chart;
 
 		xyDataset = new XYSeriesCollection();
 		chart = ChartFactory.createXYLineChart(
 			null,
 			"Sensor Reading",
 			"Sensor Depth",
 			xyDataset,
 			PlotOrientation.VERTICAL,
 			true,
 			true,
 			false
 		);
 		chart.setBackgroundPaint(Color.WHITE);
 		XYPlot plot = (XYPlot) chart.getPlot();
 		plot.getDomainAxis().setAutoRange(false);
 		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
 		renderer.setShapesVisible(true);
 		renderer.setShapesFilled(true);
 		chartPanel = new ChartPanel(chart);
 		chartPanel.setFillZoomRectangle(true);
 		chartPanel.setHorizontalAxisTrace(true);
 		chartPanel.setVerticalAxisTrace(true);
 		chartPanel.setOpaque(false);
 
 		VisSaveJChartPlotToImageButton saveButton = new VisSaveJChartPlotToImageButton(this, chart);
 
 		JPanel buttonArea = new JPanel();
 		buttonArea.setOpaque(false);
 		buttonArea.setLayout(new FlowLayout(FlowLayout.LEFT));
 		buttonArea.setBorder(BorderFactory.createEmptyBorder(3,3,0,3));
 		buttonArea.add(saveButton);
 
 		JPanel chartArea = new JPanel();
 		chartArea.setOpaque(false);
 		chartArea.setLayout(new BorderLayout());
 		chartArea.add(chartPanel, BorderLayout.CENTER);
 		chartArea.add(buttonArea, BorderLayout.NORTH);
 
 		contentPane.add(chartArea, BorderLayout.CENTER);
 	}
 
 	private void initSliderArea()
 	{
 		JPanel sliderArea = new JPanel();
 		sliderArea.setOpaque(false);
 		sliderArea.setLayout(new BorderLayout());
 		sliderArea.setBorder(BorderFactory.createTitledBorder("Time"));
 		contentPane.add(sliderArea, BorderLayout.SOUTH);
 
 		timeSlider = new JSlider();
 		timeSlider.setOpaque(false);
 		timeSlider.addChangeListener(new ProfileSliderChangeListener());
 		timeSlider.addMouseListener(new ProfileSliderMouseListener());
 
 		timeLabel = new JLabel();
 		styleTimeLabel();
 
 		JPanel buttonArea = new JPanel();
 		buttonArea.setOpaque(false);
 		BoxLayout buttonAreaLayout = new BoxLayout(buttonArea, BoxLayout.Y_AXIS);
 		buttonArea.setLayout(buttonAreaLayout);
 
 		playButton = new VisLinkButton("Play", getClass().getResource("/images/profile-plot/plotplay.png"));
 		playButton.setToolTipText("Display a timed incremental sequence of the profiles");
 		playButton.addActionListener(new PlayProfilesMovie());
 
 		pauseButton = new VisLinkButton("Pause", getClass().getResource("/images/profile-plot/plotpause.png"));
 		pauseButton.setToolTipText("Pause the playback of the profiles sequence");
 		pauseButton.addActionListener(new PauseProfilesMovie());
 		pauseButton.setVisible(false);
 
 		nextButton = new VisLinkButton("Forward", getClass().getResource("/images/profile-plot/plotnext.png"));
 		nextButton.setToolTipText("Plot the profile for the next time increment");
 		nextButton.addActionListener(new MoveToNextTimeIncrement());
 
 		previousButton = new VisLinkButton("Back", getClass().getResource("/images/profile-plot/plotprevious.png"));
 		previousButton.setToolTipText("Plot the profile for the previous time increment");
 		previousButton.addActionListener(new MoveToPreviousTimeIncrement());
 
 		buttonArea.add(playButton);
 		buttonArea.add(pauseButton);
 		buttonArea.add(nextButton);
 		buttonArea.add(previousButton);
 
 		sliderArea.add(timeSlider, BorderLayout.CENTER);
 		sliderArea.add(timeLabel, BorderLayout.SOUTH);
 		sliderArea.add(buttonArea, BorderLayout.WEST);
 	}
 
 	private void initSliderForNewTime()
 	{
 		Hashtable labels = new Hashtable();

 		for (int i=0; i<6; i++)
 		{
			int index = time.length / 5 * i;
			if (index == time.length)
				index = time.length - 1;
			labels.put(index, new JLabel(String.valueOf(time[index])));
 		}
 		timeSlider.setMajorTickSpacing(time.length / 5);
 		timeSlider.setLabelTable(labels);
 		timeSlider.setPaintTicks(true);
 		timeSlider.setPaintLabels(true);
 		timeSlider.setMinimum(0);
 		timeSlider.setMaximum(time.length - 1);
 		timeSlider.setValue(0);
 	}
 
 	private void styleTimeLabel()
 	{
 		timeLabel.setFont(AppSettings.getInstance().getDefaultFont());
 		timeLabel.setHorizontalAlignment(JLabel.CENTER);
 		timeLabel.setOpaque(true);
 		timeLabel.setBackground(Color.DARK_GRAY);
 		timeLabel.setForeground(Color.WHITE);
 	}
 
 	private void initPlaybackParamsForNewTime()
 	{
 		playbackIncrement = time.length / playbackTime / playbackFrameRate;
 	}
 
 	public void setTime(float[] timedata)
 	{
 		time = timedata;
 		initSliderForNewTime();
 		initPlaybackParamsForNewTime();
 	}
 
 	public void addSeries(String name, float[] data, float depth)
 	{
 		ProfileSeries newseries = new ProfileSeries(name, data, depth);
 
 		// figure out the min/max range for the axis
 		if (series.isEmpty())
 		{
 			rangeMin = newseries.min;
 			rangeMax = newseries.max;
 		}
 		else
 		{
 			rangeMin = newseries.min < rangeMin ? newseries.min : rangeMin;
 			rangeMax = newseries.max > rangeMax ? newseries.max : rangeMax;
 		}
 
 		series.add(newseries);
 	}
 
 	public void plot()
 	{
 		Collections.sort(series);
 
 		XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
 		NumberAxis axis = (NumberAxis) plot.getDomainAxis();
 		axis.setRange(rangeMin, rangeMax);
 
 		int timeIndex = 0;
 		plotTime(timeIndex);
 	}
 
 	public void plotTime(int timeIndex)
 	{
 		// NOTE:
 		// 	turning off auto-sort is important otherwise it autosorts by x value
 		//	which is not what we want
 		boolean do_not_autosort_series = false;
 		XYSeries xy = new XYSeries("Profile", do_not_autosort_series);
 		for (ProfileSeries entry : series)
 		{
 			xy.add(entry.data[timeIndex], entry.depth);
 		}
 
 		xyDataset.removeAllSeries();
 		xyDataset.addSeries(xy);
 
 		timeLabel.setText("Time: " + time[timeIndex]);
 	}
 
 	public void reset()
 	{
 		time = null;
 		series.clear();
 		xyDataset.removeAllSeries();
 		chartPanel.restoreAutoBounds();
 	}
 
 	private void stopPlaybackIfRunning()
 	{
 		if (playbackTimer != null && playbackTimer.isRunning())
 		{
 			playbackTimer.stop();
 			playButton.setVisible(true);
 			pauseButton.setVisible(false);
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		new ProfilePlotFrame().setVisible(true);
 	}
 
 	/**
 	 * Convenience structure to store the relevant information
 	 * for a series used in the profile plot
 	 */
 	class ProfileSeries implements Comparable
 	{
 		String name;
 		float[] data;
 		float depth;
 		float max;
 		float min;
 
 		public ProfileSeries(String name, float[] data, float depth)
 		{
 			this.name = name;
 			this.data = data;
 			this.depth = depth;
 
 			max = min = data[0];
 			for (float i : data)
 			{
 				min = i < min ? i : min;
 				max = i > max ? i : max;
 			}
 		}
 
 		public int compareTo(Object o)
 		{
 			ProfileSeries comparor = (ProfileSeries) o;
 
 			if (depth == comparor.depth)
 				return 0;
 			else if (depth < comparor.depth)
 				return 1;
 			else
 				return -1;
 		}
 	}
 
 	class ProfileSliderChangeListener implements ChangeListener
 	{
 		public void stateChanged(ChangeEvent changeEvent)
 		{
 			plotTime(timeSlider.getValue());
 			if (timeSlider.getValue() == 0)
 				previousButton.setEnabled(false);
 			else if (timeSlider.getValue() == time.length - 1)
 				nextButton.setEnabled(false);
 			else
 			{
 				if (!previousButton.isEnabled())
 					previousButton.setEnabled(true);
 				if (!nextButton.isEnabled())
 					nextButton.setEnabled(true);
 			}
 		}
 	}
 
 	class ProfileSliderMouseListener extends MouseAdapter
 	{
 		public void mouseClicked(MouseEvent e)
 		{
 			stopPlaybackIfRunning();
 		}
 	}
 
 	class PlayProfilesMovie extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			if (playbackTimer != null && playbackTimer.isRunning())
 					return;
 
 			//reset the slider to the start
 			if ((timeSlider.getValue() + (playbackFrameRate * playbackIncrement)) >= time.length)
 				timeSlider.setValue(0);
 
 			playbackTimer = new Timer(1000 / playbackFrameRate, new PlaybackTimerActionListener());
 			playbackTimer.start();
 
 			pauseButton.setVisible(true);
 			playButton.setVisible(false);
 		}
 	}
 
 	class PlaybackTimerActionListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent actionEvent)
 		{
 			int timeTick = timeSlider.getValue() + playbackIncrement;
 			if (timeTick >= time.length)
 				stopPlaybackIfRunning();
 			else
 				timeSlider.setValue(timeTick);
 		}
 	}
 
 	class PauseProfilesMovie extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			stopPlaybackIfRunning();
 		}
 	}
 
 	class MoveToNextTimeIncrement extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			stopPlaybackIfRunning();
 			timeSlider.setValue(timeSlider.getValue() + 1);
 		}
 	}
 
 	class MoveToPreviousTimeIncrement extends AbstractAction
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			stopPlaybackIfRunning();
 			timeSlider.setValue(timeSlider.getValue() - 1);
 		}
 	}
 }
