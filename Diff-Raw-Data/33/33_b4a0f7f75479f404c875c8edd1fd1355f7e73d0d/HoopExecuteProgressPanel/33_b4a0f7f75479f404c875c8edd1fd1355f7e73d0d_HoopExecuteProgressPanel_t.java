 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package edu.cmu.cs.in.hoop.execute;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingConstants;
 import javax.swing.Timer;
 
 import edu.cmu.cs.in.base.HoopLink;
 import edu.cmu.cs.in.controls.HoopControlTools;
 import edu.cmu.cs.in.controls.base.HoopEmbeddedJPanel;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 import edu.cmu.cs.in.hoop.properties.HoopVisualProperties;
 import edu.cmu.cs.in.stats.HoopPerformanceMetrics;
 
 /** 
  *
  */
 public class HoopExecuteProgressPanel extends HoopEmbeddedJPanel implements HoopExecutionMonitor, ActionListener
 {	
 	private static final long serialVersionUID = -9132114294178560223L;
 	private JList executionTrace=null;	
 	private DefaultListModel<HoopBase> model=null;
 	
 	private JButton statusButton=null;
 	
     private JRadioButton showLinear = null;   
     private JRadioButton showStaggered = null;
     
     private JRadioButton showAverage = null;   
    private JRadioButton showLatest = null;
    
    private JComboBox traceChooser=null;
     
     private JLabel timeIndicator=null;    
     private Timer displayTimer=null;
     private Long timeCounter=(long) 0;
     
     private int executionResolution=1000;
     
     private Boolean calculating=false;
 	
 	/**
 	 * 
 	 */	
 	public HoopExecuteProgressPanel()
 	{
 		setClassName ("HoopExecuteProgressPanel");
 		debug ("HoopExecuteProgressPanel ()");
 		
 		Box mainBox = new Box (BoxLayout.Y_AXIS);
 		
 		// Create controls first
 		
 		Box controlBox = new Box (BoxLayout.X_AXIS);			
 		controlBox.setMinimumSize(new Dimension (100,22));
 		controlBox.setPreferredSize(new Dimension (100,22));
 		
 		statusButton = HoopControlTools.makeNavigationButton ("run","Run",HoopLink.getImageByName("run-running.png"));
 		statusButton.setEnabled(false);
 		
 	    showLinear = new JRadioButton();
 	    showLinear.setText("Show Linear");
 	    showLinear.setSelected(true);
 	    showLinear.setFont(new Font("Dialog", 1, 10));
 	    showLinear.addActionListener(this);
 	    
 	    showStaggered = new JRadioButton();
 	    showStaggered.setText("Show Staggered");
 	    showStaggered.setFont(new Font("Dialog", 1, 10));
 	    showStaggered.addActionListener(this);
 	    
 	    //Group the radio buttons.
 	    ButtonGroup group=new ButtonGroup();
 	    group.add (showLinear);
 	    group.add (showStaggered);
 	    
 	    controlBox.add(statusButton);
 	    
 	    JSeparator sep0=new JSeparator(SwingConstants.VERTICAL);
 	    sep0.setMaximumSize(new Dimension (5,22));
 	    
 	    controlBox.add(sep0);
 	    
 	    controlBox.add(showLinear);
 	    controlBox.add(showStaggered);	
 	    
 	    JSeparator sep1=new JSeparator(SwingConstants.VERTICAL);
 	    sep1.setMaximumSize(new Dimension (5,22));
 	    
 	    controlBox.add(sep1);
 	    
 	    showAverage = new JRadioButton();
 	    showAverage.setText("Show Average Time");
 	    //showAverage.setIcon(HoopLink.getImageByName("data.gif"));
 	    showAverage.setSelected(true);
 	    showAverage.setFont(new Font("Dialog", 1, 10));
 	    showAverage.addActionListener(this);
 	    
 	    showLatest = new JRadioButton();
 	    showLatest.setText("Show Latest Time");
 	    //showLatest.setIcon(HoopLink.getImageByName("delete.png"));
 	    showLatest.setFont(new Font("Dialog", 1, 10));
 	    showLatest.addActionListener(this);
 	    
 	    //Group the radio buttons.
 	    ButtonGroup group2=new ButtonGroup();
 	    group2.add (showAverage);
 	    group2.add (showLatest);	    
 		
 	    controlBox.add(showAverage);
 	    controlBox.add(showLatest);	
 	    
 	    JSeparator sep2=new JSeparator(SwingConstants.VERTICAL);
 	    sep2.setMaximumSize(new Dimension (5,22));
 	    
 	    controlBox.add(sep2);
 	    
 		timeIndicator=new JLabel ();
 		timeIndicator.setText("00:00:00");
 		timeIndicator.setFont(new Font("Dialog", 1, 10));
 		timeIndicator.setMinimumSize(new Dimension (100,23));
 		timeIndicator.setPreferredSize(new Dimension (100,23));
 		timeIndicator.setMaximumSize(new Dimension (100,23));
 		timeIndicator.setHorizontalTextPosition(JLabel.CENTER);
 	    
 		controlBox.add(timeIndicator);
 		
 	    JSeparator sep3=new JSeparator(SwingConstants.VERTICAL);
 	    sep3.setMaximumSize(new Dimension (5,22));
 	    
 	    controlBox.add(sep3);
	    
		JLabel traceLabel=new JLabel ();
		traceLabel.setText("Current Trace:");
		traceLabel.setFont(new Font("Dialog", 1, 10));
		traceLabel.setMinimumSize(new Dimension (100,23));
		traceLabel.setPreferredSize(new Dimension (100,23));
		traceLabel.setMaximumSize(new Dimension (100,23));
		traceLabel.setHorizontalTextPosition(JLabel.CENTER);
	    
		controlBox.add(traceLabel);
	    	    
		traceChooser = new JComboBox();
		traceChooser.setFont(new Font("Dialog",1,10));
		traceChooser.setPreferredSize(new Dimension (50,20));
		traceChooser.setMaximumSize(new Dimension (50,20));	    
		
		controlBox.add(traceChooser);
		
	    JSeparator sep4=new JSeparator(SwingConstants.VERTICAL);
	    sep3.setMaximumSize(new Dimension (5,22));
	    
	    controlBox.add(sep4);
 		
 		controlBox.add(Box.createHorizontalGlue());
 		
 		// Add the actual progress controls ...
 								
 		ListCellRenderer renderer = new HoopExecutionListRenderer ();
 		
 		model = new DefaultListModel<HoopBase>();
 		
 		executionTrace=new JList (model);
 		executionTrace.setOpaque(true);
 		executionTrace.setBackground(new Color (220,220,220));
 		executionTrace.setCellRenderer(renderer);
 		
 		JScrollPane traceContainer=new JScrollPane (executionTrace);
 		traceContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		traceContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		
 		// Wrap it all up ...
 		
 		mainBox.add(controlBox);
 		mainBox.add(traceContainer);
 		
 		//this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
 		
 		setContentPane (mainBox);
 		
 		HoopLink.executionMonitor=this;
 		
 		displayTimer = new Timer(executionResolution,this);
 		displayTimer.setInitialDelay(0);
 	}
 	/**
 	 * 
 	 */
 	@Override
 	public void reset() 
 	{
 		debug ("reset ()");
 	
 		model = new DefaultListModel();
 		
 		executionTrace.setModel(model);
 				
 		displayTimer.stop();
 		timeCounter=(long) 0;
 		timeIndicator.setText("00:00:00");
 	}
 	/**
 	 * 
 	 */
 	@Override
 	public void start() 
 	{
 		debug ("start ()");
 		
 		displayTimer.start();
 		
 		statusButton.setEnabled(true);
 	}
 	/**
 	 * 
 	 */
 	@Override
 	public void stop() 
 	{
 		debug ("stop ()");
 		
 		displayTimer.stop();
 		
 		statusButton.setEnabled(false);
 	}	
 	/**
 	 * 
 	 */
 	public void handleCloseEvent ()
 	{
 		debug ("handleCloseEvent ()");
 		
 		HoopLink.executionMonitor=null;
 	}
 	/**
 	 * 
 	 */
 	@Override
 	public void updateHoopStartExecution (HoopBase aHoop) 
 	{
 		debug ("updateHoopStartExecution ()");
 		
 		int elementIndex=model.indexOf(aHoop);
 		
 		if (elementIndex==-1)
 		{
 			int pos = executionTrace.getModel().getSize();
 			model.add (pos,aHoop);
 		}
 		else
 		{
 			executionTrace.repaint(executionTrace.getCellBounds(elementIndex, elementIndex));
 		}
 		
 		updateDependencyProgress ();
 	}
 	/**
 	 * 
 	 */
 	@Override
 	public void updateHoopEndExecution (HoopBase aHoop) 
 	{
 		debug ("updateHoopEndExecution ()");
 		
 		calcVisualStats ();
 		
 		int elementIndex=model.indexOf(aHoop);
 		
 		if (elementIndex==-1)
 		{
 			int pos = executionTrace.getModel().getSize();
 			model.add (pos,aHoop);
 		}
 		/*
 		else
 		{
 			executionTrace.repaint(executionTrace.getCellBounds(elementIndex, elementIndex));
 		}
 		*/
 		
 		executionTrace.repaint();
 		
 		updateDependencyProgress ();
 	}
 	/**
 	 * 
 	 */
 	private void calcVisualStats ()
 	{
 		debug ("calcVisualStats ()");
 		
 		if (HoopExecutionListRenderer.mode==HoopExecutionListRenderer.MODEDEFAULT)
 		{
 			calcVisualStatsLinear ();
 		}
 		
 		if (HoopExecutionListRenderer.mode==HoopExecutionListRenderer.MODESTAGGERED)
 		{
 			calcVisualStatsStaggered ();
 		}		
 	}
 	/**
 	 * 
 	 */
 	private void calcVisualStatsLinear ()
 	{
 		debug ("calcVisualStatsLinear ()");
 		
 		if (model==null)
 			return;
 		
 		// This ensures that this method isn't re-entrant
 		if (calculating==true)
 			return;
 		
 		calculating=true;
 		
 		HoopExecutionListRenderer.maxMs=(long) 1;
 
 		Long totalMeasure=(long) 0;
 
 		HoopExecutionListRenderer.totalCount=model.size();
 
 		for (int t=0;t<model.size();t++)
 		{
 			HoopBase aHoop=(HoopBase) model.get(t);
 			
 			if (aHoop.getExecutionCount()>0)
 			{
 				HoopPerformanceMetrics metrics=aHoop.getPerformanceMetrics();
 			
 				Long aMeasure=metrics.getYValue();
 			
 				if (HoopExecutionListRenderer.modeTime==HoopExecutionListRenderer.TIMEAVERAGE)
 				{
 					aMeasure=(long) Math.round(metrics.getAverage());
 				}
 			
 				if (aMeasure==0)
 					aMeasure=(long) 1;
 			
 				HoopVisualProperties vizProps=aHoop.getVisualProperties();
 				
 				vizProps.duration=aMeasure;
 			
 				totalMeasure+=aMeasure;
 			
 				if (aMeasure>HoopExecutionListRenderer.maxMs)
 					HoopExecutionListRenderer.maxMs=aMeasure;
 			}	
 		}
 				
 		// Calculate transforms ...
 		
 		if (HoopExecutionListRenderer.totalWidth>0)
 		{
 			float divver=totalMeasure/(HoopExecutionListRenderer.totalWidth-4); // account for a small amount of padding
 				
 			int offset=0;
 		
 			for (int i=0;i<model.size();i++)
 			{
 				HoopBase aHoop=(HoopBase) model.get(i);
 			
 				if ((aHoop.getExecutionCount()>0) && (aHoop.getExecutionState().equals("STOPPED")==true))
 				{							
 					HoopVisualProperties vizProps=aHoop.getVisualProperties();
 											
 					vizProps.durationOffset=offset;
 
 					vizProps.durationWidth=(int) (vizProps.duration/divver);
 						
 					offset+=vizProps.durationWidth;
 				}	
 			}
 		}
 		
 		calculating=false;
 	}	
 	/**
 	 * 
 	 */
 	private void calcVisualStatsStaggered ()
 	{
 		debug ("calcVisualStatsStaggered ()");
 		
 		if (model==null)
 			return;
 		
 		// This ensures that this method isn't re-entrant
 		if (calculating==true)
 			return;
 		
 		calculating=true;
 		
 		Long totalMeasure=(long) 0;
 
 		HoopExecutionListRenderer.totalCount=model.size();
 
 		for (int t=0;t<model.size();t++)
 		{
 			HoopBase aHoop=(HoopBase) model.get(t);
 			
 			if (aHoop.getExecutionCount()>0)
 			{
 				HoopPerformanceMetrics metrics=aHoop.getPerformanceMetrics();
 			
 				Long aMeasure=metrics.getYValue();
 			
 				if (HoopExecutionListRenderer.modeTime==HoopExecutionListRenderer.TIMEAVERAGE)
 				{
 					aMeasure=(long) Math.round(metrics.getAverage());
 				}
 			
 				if (aMeasure==0)
 					aMeasure=(long) 1;
 			
 				HoopVisualProperties vizProps=aHoop.getVisualProperties();
 				
 				vizProps.duration=aMeasure;
 			
 				totalMeasure+=aMeasure;			
 			}	
 		}
 				
 		// Calculate transforms ...
 		
 		if (HoopExecutionListRenderer.totalWidth>0)
 		{
 			float divver=totalMeasure/(HoopExecutionListRenderer.totalWidth-4); // account for a small amount of padding
 				
 			int offset=0;
 		
 			for (int i=0;i<model.size();i++)
 			{
 				HoopBase aHoop=(HoopBase) model.get(i);
 			
 				if ((aHoop.getExecutionCount()>0) && (aHoop.getExecutionState().equals("STOPPED")==true))
 				{							
 					HoopVisualProperties vizProps=aHoop.getVisualProperties();
 											
 					vizProps.durationOffset=offset;
 
 					vizProps.durationWidth=(int) (vizProps.duration/divver);
 						
 					offset+=vizProps.durationWidth;
 				}	
 			}
 		}
 		
 		calculating=false;
 	}		
 	/**
 	 * 
 	 */
 	private void updateDependencyProgress ()
 	{
 		debug ("updateDependencyProgress ()");	
 	}
 	/**
 	 * 
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) 
 	{
 		//debug ("actionPerformed ()");
 	
 		if (e.getSource()==showLinear)
 		{
 			debug ("showLinear");
 			
 			HoopExecutionListRenderer.mode=HoopExecutionListRenderer.MODEDEFAULT;
 			
 			calcVisualStats ();
 			
 			executionTrace.repaint();
 			
 			return;			
 		}
 		
 		if (e.getSource()==showStaggered)
 		{
 			debug ("showStaggered");
 			
 			HoopExecutionListRenderer.mode=HoopExecutionListRenderer.MODESTAGGERED;
 			
 			calcVisualStats ();
 			
 			executionTrace.repaint();
 			
 			return;			
 		}
 	    
 		if (e.getSource()==showAverage)
 		{
 			debug ("showAverage");
 			
 			HoopExecutionListRenderer.modeTime=HoopExecutionListRenderer.TIMEAVERAGE;
 			
 			calcVisualStats ();
 			
 			executionTrace.repaint();
 			
 			return;			
 		}
 		
 		if (e.getSource()==showLatest)
 		{
 			debug ("showLatest");
 			
 			HoopExecutionListRenderer.modeTime=HoopExecutionListRenderer.TIMEDEFAULT;
 			
 			calcVisualStats ();
 			
 			return;
 		}
 		
 		if (e.getSource()==displayTimer)
 		{
 			timeCounter+=executionResolution;
 			
 			long hours=TimeUnit.MILLISECONDS.toHours(timeCounter);
 			long minutes=TimeUnit.MILLISECONDS.toMinutes(timeCounter);
 			long seconds=TimeUnit.MILLISECONDS.toSeconds(timeCounter);
 						
 			timeIndicator.setText(String.format("%02d:%02d:%02d", 
 												hours,
 												Math.abs((hours*60)-minutes),
 												Math.abs((minutes*60)-seconds)));
 			
 			calcVisualStats ();
 			
 			executionTrace.repaint();
 		}
 	}
 }
