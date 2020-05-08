 package com.uwusoft.timesheet.view;
 
 import java.beans.PropertyChangeEvent;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.DialogCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.dialog.AllDayTaskDateDialog;
 import com.uwusoft.timesheet.dialog.EndAllDayTaskDateDialog;
 import com.uwusoft.timesheet.dialog.ExternalAllDayTaskListDialog;
 import com.uwusoft.timesheet.dialog.InternalAllDayTaskListDialog;
 import com.uwusoft.timesheet.dialog.StartAllDayTaskDateDialog;
 import com.uwusoft.timesheet.dialog.TaskListDialog;
 import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
 import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.model.AllDayTaskEntry;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.util.BusinessDayUtil;
 
 public class AllDayTasksView extends AbstractTasksView {
 	public static final String ID = "com.uwusoft.timesheet.view.alldaytasksview";
 	
 	protected boolean addTaskEntries() {
 		if (viewer == null) return false;
 		viewer.setInput(LocalStorageService.getInstance().getAllDayTaskEntries());
 		return true;
 	}
 
 	protected void createColumns(final Composite parent, final TableViewer viewer) {
 		final Map<String, Integer> allDayTaskIndex = new HashMap<String, Integer>();
     	int i = 0;
     	final List<String> allDayTasks = new ArrayList<String>(storageService.getAllDayTasks());
 		for(String allDayTask : allDayTasks)
     		allDayTaskIndex.put(allDayTask, i++);
 
 		String[] titles = { "From", "To", "Requested", "Task", "Project", "Issue Key" };
 		int[] bounds = { 80, 80, 70, 150, 150, 70 };
         
 		int colNum = 0;
 		// First column is for the from date
 		TableViewerColumn col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 		    protected boolean canEdit(Object element) {
 		        return true;
 		    }
 
 		    protected CellEditor getCellEditor(Object element) {
 		        return new DateDialogCellEditor(viewer.getTable(), (AllDayTaskEntry) element) {
 
 					@Override
 					protected AllDayTaskDateDialog getDateDialog(Control cellEditorWindow, AllDayTaskEntry entry) {
 						return new StartAllDayTaskDateDialog(cellEditorWindow.getDisplay(), "Set start date", entry.getFrom(), entry.getTo(), entry.getId());
 					}		        	
 
 					@Override
 					protected Date getDate(AllDayTaskEntry entry) {
 						return entry.getFrom();
 					}
 
 					@Override
 					protected void setDate(AllDayTaskEntry entry, Timestamp date) {
 						entry.setFrom(date);
 					}
 		        };
 		    }
 
 		    protected Object getValue(Object element) {
 		        return DateFormat.getDateInstance(DateFormat.SHORT).format(((AllDayTaskEntry) element).getFrom());
 		    }
 
 		    protected void setValue(Object element, Object value) {
 		    }
 		});
 		col.setLabelProvider(new AlternatingColumnProvider() {			
 			public String getText(Object element) {
 				Timestamp date = ((AllDayTaskEntry) element).getFrom();
 	    		return DateFormat.getDateInstance(DateFormat.SHORT).format(date);
 			}
 			public Image getImage(Object obj) {
 	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/date.png").createImage();
 			}
 		});
 		// Second column is for the to date
 		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 		    protected boolean canEdit(Object element) {
 		        return true;
 		    }
 
 		    protected CellEditor getCellEditor(Object element) {
 		        return new DateDialogCellEditor(viewer.getTable(), (AllDayTaskEntry) element) {
 
 					@Override
 					protected AllDayTaskDateDialog getDateDialog(Control cellEditorWindow, AllDayTaskEntry entry) {
 						return new EndAllDayTaskDateDialog(cellEditorWindow.getDisplay(), "Set end date", entry.getFrom(), entry.getTo(), entry.getId());
 					}		        	
 
 					@Override
 					protected Date getDate(AllDayTaskEntry entry) {
 						return entry.getTo();
 					}
 
 					@Override
 					protected void setDate(AllDayTaskEntry entry, Timestamp date) {
 						entry.setTo(date);
 					}		        	
 		        };
 		    }
 
 		    protected Object getValue(Object element) {
 		        return DateFormat.getDateInstance(DateFormat.SHORT).format(((AllDayTaskEntry) element).getTo());
 		    }
 
 		    protected void setValue(Object element, Object value) {
 		    }
 		});
 		col.setLabelProvider(new AlternatingColumnProvider() {			
 			public String getText(Object element) {
 				Timestamp date = ((AllDayTaskEntry) element).getTo();
 	    		return DateFormat.getDateInstance(DateFormat.SHORT).format(date);
 			}
 			public Image getImage(Object obj) {
 	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/date.png").createImage();
 			}
 		});
 		// Third column is for the requested
 		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 		    protected boolean canEdit(Object element) {
 		        return false;
 		    }
 
 		    protected CellEditor getCellEditor(Object element) {
 		        return null;
 		    }
 
 		    protected Object getValue(Object element) {
 				return getRequestedDays(element);
 		    }
 
 		    protected void setValue(Object element, Object value) {
 		    }
 		});
 		col.setLabelProvider(new AlternatingColumnProvider() {
 			public String getText(Object element) {
 				Integer requestedDays = getRequestedDays(element);
 				if (requestedDays != null) return requestedDays.toString();
 		        return null;
 			}
 			public Image getImage(Object obj) {
 	    		return null;
 			}
 		});
 		// Fourth column is for the task
 		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 		    protected boolean canEdit(Object element) {
 		        return true;
 		    }
 
 		    protected CellEditor getCellEditor(Object element) {
 		        return new AllDayTaskListDialogCellEditor(viewer.getTable(), (AllDayTaskEntry) element);
 		    }
 
 		    protected Object getValue(Object element) {
 		        return ((AllDayTaskEntry) element).getTask().getName();
 		    }
 
 		    protected void setValue(Object element, Object value) {
 		    }
 		});
 		col.setLabelProvider(new AlternatingColumnProvider() {
 			public String getText(Object element) {
 				return ((AllDayTaskEntry) element).getTask().getName();
 			}
 			public Image getImage(Object obj) {
	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
 			}
 		});
 		// Fifth column is for the project
 		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 		    protected boolean canEdit(Object element) {
 		        return false;
 		    }
 
 		    protected CellEditor getCellEditor(Object element) {
 		        return null;
 		    }
 
 		    protected Object getValue(Object element) {
 		        return ((AllDayTaskEntry) element).getTask().getProject().getName();
 		    }
 
 		    protected void setValue(Object element, Object value) {
 		    }
 		});
 		col.setLabelProvider(new AlternatingColumnProvider() {
 			public String getText(Object element) {
 				return ((AllDayTaskEntry) element).getTask().getProject().getName();
 			}
 			public Image getImage(Object obj) {
 	    		return AbstractUIPlugin.imageDescriptorFromPlugin("com.uwusoft.timesheet", "/icons/task_16.png").createImage();
 			}
 		});
 		// Sixth column is for the issue key
 		col = createTableViewerColumn(titles[colNum], bounds[colNum], colNum++);
 		col.setEditingSupport(new EditingSupport(viewer) {
 
 		    protected boolean canEdit(Object element) {
 		        return !StringUtils.isEmpty(((AllDayTaskEntry) element).getExternalId());
 		    }
 
 		    protected CellEditor getCellEditor(Object element) {
 		        return new ExternalTaskDialogCellEditor(viewer.getTable(), (AllDayTaskEntry) element);
 		    }
 
 		    protected Object getValue(Object element) {
 		        return ((AllDayTaskEntry) element).getExternalId();
 		    }
 
 		    protected void setValue(Object element, Object value) {
 		    }
 		});
 		col.setLabelProvider(new AlternatingColumnProvider() {			
 			public String getText(Object element) {
 				return ((AllDayTaskEntry) element).getExternalId();
 			}
 			public Image getImage(Object obj) {
 	    		return null;
 			}
 			@Override
 			public Color getBackground(Object element) {
 				if (storageService.isDue(((AllDayTaskEntry)element)) != null)
 					return viewer.getTable().getDisplay().getSystemColor(SWT.COLOR_RED);
 				return super.getBackground(element);
 			}
 			@Override
 			public String getToolTipText(Object element) {
 				return storageService.isDue(((AllDayTaskEntry)element));
 			}
 		});
 	}
 
 	abstract class DateDialogCellEditor extends DialogCellEditor {
 
 		private AllDayTaskEntry entry;
 		
 		/**
 		 * @param parent
 		 */
 		public DateDialogCellEditor(Composite parent, AllDayTaskEntry entry) {
 			super(parent);
 			this.entry = entry;
 		}
 
 		@Override
 		protected Object openDialogBox(Control cellEditorWindow) {
 			AllDayTaskDateDialog dateDialog = getDateDialog(cellEditorWindow, entry);
 			if (dateDialog.open() == Dialog.OK) {
     			Date date = dateDialog.getDate();
     			if (getDate(entry).after(date) || getDate(entry).before(date)) {
     				setDate(entry, new Timestamp(date.getTime()));
     				entry.setSyncStatus(false);
     				storageService.updateAllDayTaskEntry(entry);
     				storageService.synchronizeAllDayTaskEntries();
     				if (entry.isSyncStatus()) viewer.refresh(entry);
     			}
 				return DateFormat.getDateInstance(DateFormat.SHORT).format(dateDialog.getDate());
 			}
 			return null;
 		}
 		
 		protected abstract AllDayTaskDateDialog getDateDialog(Control cellEditorWindow, AllDayTaskEntry entry);
 		
 		protected abstract Date getDate(AllDayTaskEntry entry);
 		
 		protected abstract void setDate(AllDayTaskEntry entry, Timestamp date);
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (StorageService.PROPERTY_ALLDAYTASK.equals(evt.getPropertyName()) && evt.getNewValue() != null) {
 			if (addTaskEntries()) viewer.refresh();
 		}
 	}
 
 	@Override
 	protected void createPartBeforeViewer(Composite parent) {
 	}
 
 	@Override
 	protected boolean getConditionForDarkGrey(Object element) {
 		return false;
 	}
 
 	protected Integer getRequestedDays(Object element) {
 		Task vacationPlanningTask = TimesheetApp.createTask(AllDayTaskService.PREFIX + AllDayTaskService.VACATION_PLANNING_TASK);
 		Task vacationTask = TimesheetApp.createTask(AllDayTaskService.PREFIX + AllDayTaskService.VACATION_TASK);
 		AllDayTaskEntry entry = (AllDayTaskEntry) element;
 		if (vacationPlanningTask.getName().equals(entry.getTask().getName())
 				|| vacationTask.getName().equals(entry.getTask().getName()))
 			return BusinessDayUtil.getRequestedDays(((AllDayTaskEntry) element).getFrom(), ((AllDayTaskEntry) element).getTo());
 		return null;
 	}
 
 	class AllDayTaskListDialogCellEditor extends DialogCellEditor {
 
 		private AllDayTaskEntry entry;
 		
 		/**
 		 * @param parent
 		 */
 		public AllDayTaskListDialogCellEditor(Composite parent, AllDayTaskEntry entry) {
 			super(parent);
 			this.entry = entry;
 		}
 
 		@Override
 		protected Object openDialogBox(Control cellEditorWindow) {
 			TaskListDialog listDialog;
 			String system = TimesheetApp.getDescriptiveName(Activator.getDefault().getPreferenceStore().getString(AllDayTaskService.PROPERTY),
 					AllDayTaskService.SERVICE_NAME);
 			if (system.equals(entry.getTask().getProject().getSystem()))
 				listDialog = new ExternalAllDayTaskListDialog(cellEditorWindow.getShell(), entry.getTask());
 			else
 				listDialog = new InternalAllDayTaskListDialog(cellEditorWindow.getShell(), entry.getTask());
 			if (listDialog.open() == Dialog.OK) {
 			    String selectedTask = Arrays.toString(listDialog.getResult());
 			    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
 				if (StringUtils.isEmpty(selectedTask)) return null;
 				
 		    	if (!selectedTask.equals(entry.getTask().getName())) {
 		    		entry.setTask(storageService.findTaskByNameProjectAndSystem(selectedTask, listDialog.getProject(), listDialog.getSystem()));
     				entry.setSyncStatus(false);
     				storageService.updateAllDayTaskEntry(entry);
     				storageService.synchronizeAllDayTaskEntries();
     				if (entry.isSyncStatus()) viewer.refresh(entry);
 					return selectedTask;
 		    	}				
 			}
 			return null;
 		}		
 	}
 	
 	class ExternalTaskDialogCellEditor extends DialogCellEditor {
 		private AllDayTaskEntry entry;
 
 		public ExternalTaskDialogCellEditor(Composite parent, AllDayTaskEntry entry) {
 			super(parent);
 			this.entry = entry;
 		}
 		
 		@Override
 		protected Object openDialogBox(Control cellEditorWindow) {
 			LocalStorageService.getAllDayTaskService().openUrl(entry);
 			return entry.getExternalId();
 		}
 
 	}
 }
