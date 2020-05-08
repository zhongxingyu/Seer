 package com.uwusoft.timesheet;
 
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.commands.ParameterizedCommand;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tray;
 import org.eclipse.swt.widgets.TrayItem;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.application.WorkbenchWindowAdvisor;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.util.BusinessDayUtil;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.MessageBox;
 
 public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
 
 	private IWorkbenchWindow window;
 	private TrayItem trayItem;
 	private Image trayImage;
 	private ApplicationActionBarAdvisor actionBarAdvisor;
 	private IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 
 	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
 		super(configurer);
 	}
 
 	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
 		actionBarAdvisor = new ApplicationActionBarAdvisor(configurer);
 		return actionBarAdvisor;
 	}
 
 	public void preWindowOpen() {
 		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
 		configurer.setInitialSize(new Point(400, 300));
 		configurer.setShowCoolBar(false);
 		configurer.setShowProgressIndicator(true);
 		configurer.setTitle("Timesheet");
 	}
 	
 	public void postWindowOpen() {
 		super.postWindowOpen();
 		window = getWindowConfigurer().getWindow();
 		window.getShell().addShellListener(new ShellAdapter() {
 			public void shellIconified(ShellEvent e) { // If the window is minimized hide the window
 				preWindowShellClose();
 			}
 		});
 		trayItem = initTaskItem();
 		// Some OS might not support tray items
 		if (trayItem != null) {
 			window.getShell().setVisible(false);
 
 			StorageService storageService = new ExtensionManager<StorageService>(
 					StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
 			
 			Date startDate = TimesheetApp.startDate;
 			Date shutdownDate = startDate;
 			try {
 				String startTime = preferenceStore.getString(TimesheetApp.SYSTEM_START);
 				if (!StringUtils.isEmpty(startTime)) {
 					preferenceStore.setToDefault(TimesheetApp.SYSTEM_START);
 					startDate = StorageService.formatter.parse(startTime);				
 				}
 
 				String shutdownTime = preferenceStore.getString(TimesheetApp.SYSTEM_SHUTDOWN);
 				if (StringUtils.isEmpty(shutdownTime) || StorageService.formatter.parse(shutdownTime).before(storageService.getLastTaskEntryDate()))
 					shutdownDate = storageService.getLastTaskEntryDate();
 				else
 					shutdownDate = StorageService.formatter.parse(shutdownTime);				
 			} catch (ParseException e) {
 				MessageBox.setError("Shutdown date", e.getLocalizedMessage());
 			}
 			Calendar calDay = Calendar.getInstance();
 			Calendar calWeek = new GregorianCalendar();
 			int shutdownDay = 0;
 			int shutdownWeek = 0;
 			
 			calDay.setTime(startDate);
 			calWeek.setTime(startDate);
 			int startDay = calDay.get(Calendar.DAY_OF_YEAR);
 			int startWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
 
 			calDay.setTime(shutdownDate);
 			calWeek.setTime(shutdownDate);
 			shutdownDay = calDay.get(Calendar.DAY_OF_YEAR);
 			shutdownWeek = calWeek.get(Calendar.WEEK_OF_YEAR);
 
 			IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
 			ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
 				
 			if (startDay != shutdownDay && storageService.getLastTask() != null) { // don't automatically check in/out if program is restarted
 				try { // automatic check out
 					Map<String, String> parameters = new HashMap<String, String>();
 					parameters.put("Timesheet.commands.shutdownTime", StorageService.formatter.format(shutdownDate));
 					handlerService.executeCommand(ParameterizedCommand.generateCommand(commandService.getCommand("Timesheet.checkout"),	parameters), null);
 				} catch (Exception ex) {
 					MessageBox.setError("Timesheet.checkout command", ex.getLocalizedMessage());
 				}
 			}
 			if (storageService.getLastTask() == null) {					
 				try { // automatic check in
 					Date end = shutdownDate;
					do
 						end = BusinessDayUtil.getNextBusinessDay(end, true); // create missing holidays
					while (!end.after(startDate));
 					
 					Map<String, String> parameters = new HashMap<String, String>();
 					parameters.put("Timesheet.commands.startTime", StorageService.formatter.format(startDate));
 					parameters.put("Timesheet.commands.storeWeekTotal", Boolean.toString(startWeek != shutdownWeek));
 					handlerService.executeCommand(ParameterizedCommand.generateCommand(
 							commandService.getCommand("Timesheet.checkin"), parameters), null);
 					if (startWeek > shutdownWeek) {
 						parameters.clear();
 						parameters.put("Timesheet.commands.weekNum", Integer.toString(shutdownWeek));
 						handlerService.executeCommand(ParameterizedCommand.generateCommand(
 								commandService.getCommand("Timesheet.submit"), parameters), null);
 					}
 				} catch (Exception ex) {
 					MessageBox.setError("Timesheet.checkin command", ex.getLocalizedMessage());
 				}
 			}
 			hookPopupMenu();
 		}
 	}
 
 	public boolean preWindowShellClose() {
 		window.getShell().setVisible(false);
 		return false; // if window close button pressed only minimize to system tray (don't exit the program)
 	}
 	
 	private void hookPopupMenu() {
 		trayItem.addListener(SWT.MenuDetect, new Listener() {
 			public void handleEvent(Event event) {
 				MenuManager trayMenu = new MenuManager();
                 
 				StorageService storageService = new ExtensionManager<StorageService>(
 						StorageService.SERVICE_ID).getService(preferenceStore.getString(StorageService.PROPERTY));
 				if (storageService.getLastTask() == null) {					
                 	Map <String, String> parameters = new HashMap<String, String>();
                     parameters.put("Timesheet.commands.startTime", StorageService.formatter.format(new Date()));
                     CommandContributionItemParameter p = new CommandContributionItemParameter(window, null, "Timesheet.checkin", CommandContributionItem.STYLE_PUSH);
                     p.parameters = parameters;         
                     trayMenu.add(new CommandContributionItem(p));
                 }
                 else {
                     trayMenu.add(new CommandContributionItem(
             				new CommandContributionItemParameter(window, null, "Timesheet.changeTask", CommandContributionItem.STYLE_PUSH)));
                     
                     trayMenu.add(new CommandContributionItem(
                     		new CommandContributionItemParameter(window, null, "Timesheet.setBreak", CommandContributionItem.STYLE_PUSH)));
 
                     MenuManager wholeDayTask = new MenuManager("Set whole day task");
                     String wholeDayTaskCommandId = "Timesheet.commands.wholeDayTask";
                     
                     Map <String, String> parameters = new HashMap<String, String>();
                     parameters.put("Timesheet.commands.task", "task.holiday");
                     CommandContributionItemParameter p = new CommandContributionItemParameter(window, null, wholeDayTaskCommandId, CommandContributionItem.STYLE_PUSH);
                     p.label = "Holiday";
                     p.parameters = parameters;         
                     wholeDayTask.add(new CommandContributionItem(p));
 
                     parameters.put("Timesheet.commands.task", "task.vacation");
                     p = new CommandContributionItemParameter(window, null, wholeDayTaskCommandId, CommandContributionItem.STYLE_PUSH);
                     p.label = "Vacation";
                     p.parameters = parameters;         
                     wholeDayTask.add(new CommandContributionItem(p));
 
                     parameters.put("Timesheet.commands.task", "task.sick");
                     p = new CommandContributionItemParameter(window, null, wholeDayTaskCommandId, CommandContributionItem.STYLE_PUSH);
                     p.label = "Sick Leave";
                     p.parameters = parameters;         
                     wholeDayTask.add(new CommandContributionItem(p));
 
                     trayMenu.add(wholeDayTask);
 
                     parameters.clear();
                     parameters.put("Timesheet.commands.shutdownTime", StorageService.formatter.format(new Date()));
                     p = new CommandContributionItemParameter(window, null, "Timesheet.checkout", CommandContributionItem.STYLE_PUSH);
                     p.parameters = parameters;         
                     trayMenu.add(new CommandContributionItem(p));
                 }
                 trayMenu.add(new Separator());
                 
                 trayMenu.add(new CommandContributionItem(
         				new CommandContributionItemParameter(window, null, "Timesheet.importTasks", CommandContributionItem.STYLE_PUSH)));
 
                 Calendar cal = new GregorianCalendar();
             	cal.setTime(new Date());
             	//cal.setFirstDayOfWeek(Calendar.MONDAY);
 				Map<String, String> parameters = new HashMap<String, String>();
 				parameters.put("Timesheet.commands.weekNum", Integer.toString(cal.get(Calendar.WEEK_OF_YEAR) - 1));
 				CommandContributionItemParameter p = new CommandContributionItemParameter(window, null, "Timesheet.submit", CommandContributionItem.STYLE_PUSH);
                 p.parameters = parameters;         
                 trayMenu.add(new CommandContributionItem(p));
                 
                 trayMenu.add(new Separator());
 			    
                 Menu menu = trayMenu.createContextMenu(window.getShell());
 			    actionBarAdvisor.fillTrayItem(trayMenu);
 			    menu.setVisible(true);
 			}
 		});
 	}
 
 	private TrayItem initTaskItem() {
 		final Tray tray = window.getShell().getDisplay().getSystemTray();
 		final TrayItem trayItem = new TrayItem(tray, SWT.NONE);
 		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/clock.png");
 		if (descriptor != null) {
 			trayImage = descriptor.createImage();
 			trayItem.setImage(trayImage);
 		}
 		trayItem.setToolTipText("Timesheet");
 		trayItem.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				Shell shell = window.getShell();
 				shell.setVisible(true);
 				shell.setActive();
 				shell.setFocus();
 				shell.setMinimized(false);
 				//trayItem.dispose();
 			}
 		});
 		return trayItem;
 	}
 
 	// We need to clean-up after ourself
 	@Override
 	public void dispose() {
 		if (trayImage != null) {
 			trayImage.dispose();
 		}
 		if (trayItem != null) {
 			trayItem.dispose();
 		}
 	}
 }
