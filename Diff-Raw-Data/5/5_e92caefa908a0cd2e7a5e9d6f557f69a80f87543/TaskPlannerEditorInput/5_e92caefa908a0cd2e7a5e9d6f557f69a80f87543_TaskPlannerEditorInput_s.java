 /*******************************************************************************
  * Copyright (c) 2004 - 2005 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylar.tasklist.internal.planner.ui;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.mylar.core.util.ErrorLogger;
 import org.eclipse.mylar.tasklist.ITask;
 import org.eclipse.mylar.tasklist.internal.TaskList;
 import org.eclipse.mylar.tasklist.internal.planner.CompletedTaskCollector;
 import org.eclipse.mylar.tasklist.internal.planner.ITaskCollector;
 import org.eclipse.mylar.tasklist.internal.planner.InProgressTaskCollector;
 import org.eclipse.mylar.tasklist.internal.planner.TaskReportGenerator;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IPersistableElement;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.progress.IProgressService;
 
 /**
  * @author Ken Sueda
  */
 public class TaskPlannerEditorInput implements IEditorInput {
 
 	private List<ITask> completedTasks = null;
 
 	private List<ITask> inProgressTasks = null;
   
 	private TaskReportGenerator taskReportGenerator = null;
 
 	private int prevDaysToReport = -1;
 
 	private long DAY = 24 * 3600 * 1000;
 
 	private Date reportStartDate = null;
 
 	public TaskPlannerEditorInput(int prevDays, TaskList tlist) {
 		prevDaysToReport = prevDays;
 		long today = new Date().getTime();
 		long lastDay = prevDaysToReport * DAY;
 
 		int offsetToday = Calendar.getInstance().get(Calendar.HOUR) * 60 * 60 * 1000
 			+ Calendar.getInstance().get(Calendar.MINUTE) * 60 * 1000
 			+ Calendar.getInstance().get(Calendar.SECOND) * 1000;
 		reportStartDate = new Date(today - offsetToday - lastDay);
 		
 		taskReportGenerator = new TaskReportGenerator(tlist);
 
 		ITaskCollector completedTaskCollector = new CompletedTaskCollector(reportStartDate);
 		taskReportGenerator.addCollector(completedTaskCollector);
 
 		ITaskCollector inProgressTaskCollector = new InProgressTaskCollector(reportStartDate);
 		taskReportGenerator.addCollector(inProgressTaskCollector);
 
 		try {
 			IProgressService service = PlatformUI.getWorkbench().getProgressService();
			service.run(true, true, taskReportGenerator);

			while (!taskReportGenerator.isFinished()) Thread.sleep(1000);
 		} catch (InvocationTargetException e) {
 			// operation was canceled
 		} catch (InterruptedException e) {
 			ErrorLogger.log(e, "Could not generate report");
 		}
 //		taskReportGenerator.collectTasks();
 
 		completedTasks = completedTaskCollector.getTasks();
 		inProgressTasks = inProgressTaskCollector.getTasks();
 	}
 
 	public boolean exists() {
 		return true;
 	}
 
 	public ImageDescriptor getImageDescriptor() {
 		return null;
 	}
 
 	public String getName() {
 		return "Mylar Task Planner";
 	}
 
 	public IPersistableElement getPersistable() {
 		return null;
 	}
 
 	public String getToolTipText() {
 		return "Task Planner";
 	}
 
 	public Object getAdapter(Class adapter) {
 		return null;
 	}
 
 	// Methods
 
 	public List<ITask> getCompletedTasks() {
 		return completedTasks;
 	}
 
 	public List<ITask> getInProgressTasks() {
 		return inProgressTasks;
 	}
 
 	public long getTotalTimeSpentOnCompletedTasks() {
 		long duration = 0;
 		for (ITask t : completedTasks) {
 			duration += t.getElapsedTime();
 		}
 		return duration;
 	}
 
 	public long getTotalTimeSpentOnInProgressTasks() {
 		long duration = 0;
 		for (ITask t : inProgressTasks) {
 			duration += t.getElapsedTime();
 		}
 		return duration;
 	}
 
 	public TaskReportGenerator getReportGenerator() {
 		return taskReportGenerator;
 	}
 
 	public boolean createdDuringReportPeriod(ITask task) {
 		Date creationDate = task.getCreationDate();
 		if (creationDate != null) {
 			return creationDate.compareTo(reportStartDate) > 0;
 		} else {
 			return false;
 		}
 	}
 
 	public Date getReportStartDate() {
 		return reportStartDate;
 	}
 
 	public int getTotalTimeEstimated() {
 		int duration = 0;
 		for (ITask task : inProgressTasks) {
 			duration += task.getEstimateTimeHours();
 		}
 		return duration;
 	}
 
 }
