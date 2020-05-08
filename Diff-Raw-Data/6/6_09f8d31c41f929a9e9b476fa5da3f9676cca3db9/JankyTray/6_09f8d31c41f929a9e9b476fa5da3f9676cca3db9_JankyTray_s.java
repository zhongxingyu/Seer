 package com.zombietank.jankytray;
 
 import static com.zombietank.support.InvokingListener.invokingListener;
 
 import javax.inject.Inject;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.TrayItem;
 import org.eclipse.swt.widgets.Widget;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.stereotype.Component;
 
 import com.zombietank.jenkins.model.Status;
 import com.zombietank.swt.EventListener;
 import com.zombietank.swt.ImageRegistryWrapper;
 import com.zombietank.swt.OnDisposeRemoveEventListener;
 import com.zombietank.swt.ProgramWrapper;
 
 /***
  * Tray icon with refresh timer which polls Jenkins server on specified
  * interval.
  * 
  * @author Tom Hermann
  */
 @Component
 public class JankyTray implements InitializingBean {
 	@Inject private JankyMenu jankyMenu;
 	@Inject private JankyOptions options;
 	@Inject private JankyWidgetContext context;
 	@Inject private ImageRegistryWrapper imageRegistry;
 	@Inject private ProgramWrapper programWrapper;
 	@Inject private ConfigurationDialog configurationDialog;
 	
 	public void afterPropertiesSet() throws Exception {
 		if(!options.hasJenkinsUrl()) {
 			configurationDialog.open();
 		}
		if(!options.hasJenkinsUrl()) {
 			MessageDialog.openError(context.getShell(), "Error.", "A valid Jenkins URL is required, please try again.");
 		}
		run();
 	}
 	
 	public void run() {
 		final Display display = context.getDisplay();
 		final TrayItem trayIcon = new TrayItem(context.getTray(), SWT.NORMAL);
 		trayIcon.setImage(imageRegistry.get(Status.UNKNOWN));
 		addListener(trayIcon, new EventListener(SWT.MenuDetect, invokingListener(jankyMenu, "display")));
 		addListener(trayIcon, new EventListener(SWT.DefaultSelection, invokingListener(jankyMenu, "refresh")));
 		addListener(trayIcon, new EventListener(SWT.DefaultSelection, invokingListener(programWrapper, "launch", options.getJenkinsUrl())));
 
 		Runnable runnable = new Runnable() {
 			public void run() {
 				Status refreshStatus = jankyMenu.refresh();
 				trayIcon.setImage(imageRegistry.get(refreshStatus));
 				trayIcon.setToolTipText(refreshStatus.getName());
 				display.timerExec(options.getPollingInterval() * 1000, this);
 			}
 		};
 		
 		display.asyncExec(runnable);
 		
 		while (!context.getShell().isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		
 		display.dispose();	
 	}
 	
 	private void addListener(Widget widget, EventListener eventListener) {
 		widget.addListener(eventListener.getEventType(), eventListener.getListener());
 		widget.addDisposeListener(new OnDisposeRemoveEventListener(eventListener));
 	}
 }
