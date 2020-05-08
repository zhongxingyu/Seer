 package com.uwusoft.timesheet.commands;
 
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.dialogs.ListDialog;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.dialog.TimeDialog;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.util.ExtensionManager;
 
 public class ChangeTaskHandler extends AbstractHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		StorageService storageService = new ExtensionManager<StorageService>(
 				StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
 		ListDialog listDialog = new ListDialog(HandlerUtil.getActiveShell(event));
 		listDialog.setTitle("Tasks");
 		listDialog.setMessage("Select next task");
 		listDialog.setContentProvider(ArrayContentProvider.getInstance());
 		listDialog.setLabelProvider(new LabelProvider());
 		listDialog.setWidthInChars(70);
		List<String> tasks = new ArrayList<String>(storageService.getTasks().get("Primavera")); // TODO
 		tasks.remove(preferenceStore.getString(TimesheetApp.LAST_TASK));
 		listDialog.setInput(tasks);
 		if (listDialog.open() == Dialog.OK) {
 		    String selectedTask = Arrays.toString(listDialog.getResult());
 		    selectedTask = selectedTask.substring(selectedTask.indexOf("[") + 1, selectedTask.indexOf("]"));
 			if (StringUtils.isEmpty(selectedTask)) return null;
 			TimeDialog timeDialog = new TimeDialog(Display.getDefault(), selectedTask, new Date());
 			if (timeDialog.open() == Dialog.OK) {
                 storageService.createTaskEntry(new Task(timeDialog.getTime(), preferenceStore.getString(TimesheetApp.LAST_TASK)));
 				preferenceStore.setValue(TimesheetApp.LAST_TASK, selectedTask);
 				preferenceStore.setValue(TimesheetApp.SYSTEM_SHUTDOWN, StorageService.formatter.format(timeDialog.getTime()));
 			}
 		}						
 		return null;
 	}
 
 }
