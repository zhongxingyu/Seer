 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.iterations;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.jdesktop.swingx.JXMonthView;
 import org.jdesktop.swingx.calendar.DateSelectionModel.SelectionMode;
 import org.jdesktop.swingx.event.DateSelectionEvent;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.ViewEventController;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.ViewMode;
 
 /**
  * @author justinhess
  * @version $Revision: 1.0 $
  */
 public class IterationCalendar extends JXMonthView implements ActionListener, KeyListener {
 	
	public static final Color START_END_DAY = new Color(47, 150, 9);
	public static final Color SELECTION = new Color(236,252,144);
 	public static final Color UNSELECTABLE = Color.red;
 	
 	private ViewMode viewMode;
 	private Date startDate = null;
 	private Date endDate = null;
 	private IterationPanel parent;
 	private Iteration displayIteration;
 	private IterationOverviewPanel overviewParent;
 	private boolean isOverview;
 	/**
 	 * Constructor for the iteration calendar.
 	 * @param parent IterationPanel
 	 * @param vm ViewMode
 	 * @param displayIteration Iteration
 	 */
 	public IterationCalendar(IterationPanel parent, ViewMode vm, Iteration displayIteration)
 	{
 		this.isOverview = false;
 		this.viewMode = vm;
 		this.parent = parent;
 		this.displayIteration = displayIteration;
 		buildLayout();
 	}
 	
 	/**
 	 * Constructor for iteration calendar.
 	 * @param parent IterationOverviewPanel
 	 */
 	public IterationCalendar(IterationOverviewPanel parent) {
 		this.overviewParent = parent;
 		this.isOverview = true;
 		buildLayout();
 	}
 	
 	/**
 	 * Builds the layout
 	 */
 	private void buildLayout()
 	{
 		this.setPreferredColumnCount(4);
 		this.setPreferredRowCount(3);
 		this.setSelectionBackground(SELECTION);
 		this.setFlaggedDayForeground(START_END_DAY);
 		this.setSelectionMode(SelectionMode.SINGLE_INTERVAL_SELECTION);
 		this.setAlignmentX(CENTER_ALIGNMENT);
 		this.addActionListener(this);
 		this.addMouseMotionListener(new MouseMotionListener()
 		{
 			@Override
 			public void mouseDragged(MouseEvent e) {				
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent e) {
 				int x = e.getX();
 				int y = e.getY();
 				
 				Date forLocation = IterationCalendar.this.getDayAtLocation(x, y);
 				
 				if(forLocation != null)
 				{
 					List<Iteration> atDate = IterationModel.getInstance().getIterationForDate(forLocation);
 					
 					if(atDate.size() > 0)
 					{
 						String toolTipText = "<html>";
 						
 						for(Iteration it : atDate)
 						{
 							toolTipText += it.getName() + "<br>";
 							toolTipText += "&nbsp &nbsp Estimate: " + it.getEstimate() + "<br>";
 						}
 						toolTipText += "</html>";
 						IterationCalendar.this.setToolTipText(toolTipText);
 					}
 					else
 					{
 						IterationCalendar.this.setToolTipText(null);
 					}
 				}
 			}		
 		});
 		
 		this.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent e)
 			{
 				if(e.getClickCount() == 2)
 				{
 					int x = e.getX();
 					int y = e.getY();
 					Date forClick = IterationCalendar.this.getDayAtLocation(x, y);
 					
 					List<Iteration> forDate = IterationModel.getInstance().getIterationForDate(forClick);
 									
 					for(Iteration it : forDate)
 					{
 						ViewEventController.getInstance().editIteration(it);
 					}
 					
 				}	
 			}
 		});
 		
 		this.addKeyListener(this);
 
 	}
 
 	/**
 	 * Method isFlaggedDate.
 	 * @param date Date
 	
 	 * @return boolean */
 	@Override
 	public boolean isFlaggedDate(Date date)
 	{
 		boolean isFlaggedDate = false;
 		
 		if(isOverview)
 		{
 			List<Iteration> forDate = IterationModel.getInstance().getIterationForDate(date);
 						
 			isFlaggedDate = forDate.size() > 0;
 		}
 		else
 		{
 		
 			List<Iteration> forDate = IterationModel.getInstance().getIterationForDate(date);
 			if(forDate.contains(displayIteration)) return false;
 				
 		
 			for(Iteration iter : forDate)
 			{
 				isFlaggedDate |= date.equals(iter.getStart().getDate()) || date.equals(iter.getEnd().getDate());
 			}
 		}
 		
 		return isFlaggedDate || super.isFlaggedDate(date);
 	}
 	
 	/**
 	 * Method isUnselectableDate.
 	 * @param date Date
 	
 	 * @return boolean */
 	@Override
 	public boolean isUnselectableDate(Date date) {
 		boolean unselectable = false;
 		if(!isOverview)
 		{
 			List<Iteration> forDate = IterationModel.getInstance().getIterationForDate(date);
 			
 			if(forDate.contains(displayIteration)) return false;
 			
 			unselectable = forDate.size() == 1 && !isFlaggedDate(date);
 		}
 
 		return unselectable || super.isUnselectableDate(date);
 	}
 	
 	/**
 	 * Method highlightIteration.
 	 * @param it Iteration
 	 */
 	public void highlightIteration(Iteration it)
 	{
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(this.getFirstDisplayedDay());
 		Date firstSeen = cal.getTime();
 		cal.add(Calendar.DAY_OF_YEAR, 364);
 		Date lastSeen = cal.getTime();
 		
 		if(it == IterationModel.getInstance().getBacklog())
 		{
 			startDate = null;
 			endDate = null;
 			clearSelection();
 		}
 		else
 		{
 			this.startDate = it.getStart().getDate();
 			this.endDate = it.getEnd().getDate();
 			
 			if(startDate.after(lastSeen) || startDate.before(firstSeen))
 			{
 				this.setFirstDisplayedDay(startDate);
 			}
 			
 			setSelectionInterval(startDate, endDate);
 		}
 	}
 
 	private void handleSelectionForOverview()
 	{
 		if(isOverview)
 		{
 			Date tempStart = this.getSelectionModel().getFirstSelectionDate();
 
 			List<Iteration> forDate = IterationModel.getInstance().getIterationForDate(tempStart);
 			
 			if(forDate.size() > 0)
 			{
 				this.overviewParent.highlight(forDate.get(0));
 				ViewEventController.getInstance().getOverviewTree().selectIteration(forDate.get(0));
 			}
 			else
 			{
 				this.clearSelection();
 				if(startDate != null && endDate != null)
 				{
 					this.setSelectionInterval(startDate, endDate);
 				}
 			}
 			return;
 		}
 	}
 	
 	private void handleSelectionForPanel()
 	{
 		if(!isOverview)
 		{
 			//check that the selected dates are valid dates.
 			boolean validSelection = true;
 
 			Date secondDate = this.getSelectionModel().getLastSelectionDate();
 			Calendar firstDate = Calendar.getInstance();
 			firstDate.setTime(this.getSelectionModel().getFirstSelectionDate());
 			
 			if(isUnselectableDate(firstDate.getTime()) || isUnselectableDate(secondDate)) validSelection = false;
 			
 			//if any date in the interval is unselectable, its invalid.
 			while(firstDate.getTime().before(secondDate))
 			{
 				if(isUnselectableDate(firstDate.getTime()))
 				{
 					validSelection = false;
 					break;
 				}
 				
 				firstDate.add(Calendar.DAY_OF_YEAR, 1);
 			}
 		
 			if(validSelection)
 			{
 				//if is valid date, update it as date in parent.
 				startDate = this.getSelectionModel().getFirstSelectionDate();
 				endDate = this.getSelectionModel().getLastSelectionDate();
 				
 				parent.setDates(startDate, endDate);
 			}
 			else
 			{
 				//otherwise, return back to previous selection.
 				this.getSelectionModel().clearSelection();
 				if(startDate != null && endDate != null)
 				{
 					this.getSelectionModel().setSelectionInterval(startDate, endDate);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method actionPerformed.
 	 * @param e ActionEvent
 	
 	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent) */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		handleSelectionForOverview();
 		handleSelectionForPanel();
 	}
 
 	/**
 	 * Method keyTyped.
 	 * @param e KeyEvent
 	 * @see java.awt.event.KeyListener#keyTyped(KeyEvent)
 	 */
 	@Override
 	public void keyTyped(KeyEvent e) {
 		handleSelectionForOverview();
 		handleSelectionForPanel();		
 	}
 
 	/**
 	 * Method keyPressed.
 	 * @param e KeyEvent
 	 * @see java.awt.event.KeyListener#keyPressed(KeyEvent)
 	 */
 	@Override
 	public void keyPressed(KeyEvent e) {
 		handleSelectionForOverview();
 		handleSelectionForPanel();		
 	}
 
 	/**
 	 * Method keyReleased.
 	 * @param e KeyEvent
 	 * @see java.awt.event.KeyListener#keyReleased(KeyEvent)
 	 */
 	@Override
 	public void keyReleased(KeyEvent e) {
 		handleSelectionForOverview();
 		handleSelectionForPanel();		
 	}
 }
