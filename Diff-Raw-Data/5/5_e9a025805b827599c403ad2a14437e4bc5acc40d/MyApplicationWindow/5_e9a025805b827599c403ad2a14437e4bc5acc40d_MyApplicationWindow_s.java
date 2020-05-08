 package at.photoselector.ui;
 
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Shell;
 
 import at.photoselector.Settings;
 
 public abstract class MyApplicationWindow extends ApplicationWindow implements
 		Runnable {
 
 	protected String control = null;
 
 	public MyApplicationWindow(Shell parentShell) {
 		super(null);
 	}
 
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 
 		// restore window size and location
 		control = this.getClass().getSimpleName().replaceAll("s?Dialog$", "")
 				.toLowerCase();
 
 		Rectangle tmp = Settings.rememberWindowPosition(control);
 		shell.setLocation(tmp.x, tmp.y);
 		shell.setSize(tmp.width, tmp.height);
 
 		shell.addDisposeListener(new DisposeListener() {
 
 			@Override
 			public void widgetDisposed(DisposeEvent e) {
 				// memorize window location
 				/*
 				 * FIXME dirty fix of swt but. shell.getLocation is not updated
 				 * unless we hide it and show it again...
 				 */
 				getShell().setVisible(false);
 				getShell().setVisible(true);
 				Settings.memorizeWindowPosition(control, getShell().getBounds());
 			}
 		});
 	}
 
 	public void run() {
 		// Don't return from open() until window closes
 		setBlockOnOpen(true);
 
 		// Open the main window
 		open();
 	}
 
 	public abstract void update();
 }
