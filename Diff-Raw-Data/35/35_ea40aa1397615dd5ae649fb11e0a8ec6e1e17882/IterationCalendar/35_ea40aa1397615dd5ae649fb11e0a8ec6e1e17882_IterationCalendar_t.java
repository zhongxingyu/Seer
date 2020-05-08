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
 import java.util.Calendar;
 import java.util.Date;
import java.util.List;
 
 import org.jdesktop.swingx.JXMonthView;
 import org.jdesktop.swingx.calendar.DateSelectionModel.SelectionMode;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 
 public class IterationCalendar extends JXMonthView implements ActionListener {
 	
 	private Date startDate = null;
 	private Date endDate = null;
 	private IterationPanel parent;
 	/**
 	 * Constructor for the iteration calendar.
 	 */
 	public IterationCalendar(IterationPanel parent)
 	{
 		this.parent = parent;
 		this.setPreferredColumnCount(4);
 		this.setPreferredRowCount(3);
 		this.setFlaggedDayForeground(Color.GREEN);
 		this.setSelectionBackground(Color.GREEN);
 		this.setAlignmentX(CENTER_ALIGNMENT);
 		this.setSelectionMode(SelectionMode.SINGLE_INTERVAL_SELECTION);
 		this.addActionListener(this);
 	}
 	
 	@Override
 	public boolean isUnselectableDate(Date date) {
		List<Iteration> forDate = IterationModel.getInstance().getIterationForDate(date);
 		
		boolean unselectable = forDate.size() > 0;
 		
		if(unselectable && forDate.size() == 1)
 		{
			unselectable &= !(forDate.get(0).getStart().getDate().equals(date));
			unselectable &= !(forDate.get(0).getEnd().getDate().equals(date));
 		}
 		
 		return unselectable || super.isUnselectableDate(date);
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
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
