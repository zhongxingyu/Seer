 package net.mms_projects.copy_it.ui;
 
 import java.util.Date;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import net.mms_projects.copy_it.Activatable;
 import net.mms_projects.copy_it.ClipboardManager;
 import net.mms_projects.copy_it.Config;
 import net.mms_projects.copy_it.EnvironmentIntegration;
 import net.mms_projects.copy_it.FunctionalityManager;
 import net.mms_projects.copy_it.Messages;
 import net.mms_projects.copy_it.SyncListener;
 import net.mms_projects.copy_it.SyncManager;
 import net.mms_projects.copy_it.clipboard_backends.SwtBackend;
 import net.mms_projects.copy_it.ui.swt.forms.AboutDialog;
 import net.mms_projects.copy_it.ui.swt.forms.DataQueue;
 import net.mms_projects.copy_it.ui.swt.forms.PreferencesDialog;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SwtInterface implements UserInterfaceImplementation {
 
 	private SettingsUserInterface settingsUserInterface;
 	private AboutUserInterface aboutUserInterface;
 	private QueueUserInterface queueUserInterface;
 
 	protected Config config;
 	protected Display display;
 	protected Shell activityShell;
 
 	protected SyncManager syncManager;
 	protected ClipboardManager clipboardManager;
 
 	protected EnvironmentIntegration environmentIntegration;
 
 	private final Logger log = LoggerFactory.getLogger(this.getClass());
 	private FunctionalityManager<Activatable> functionality;
 
 	public SwtInterface(final Config config,
 			FunctionalityManager<Activatable> functionality,
 			EnvironmentIntegration environmentIntegration,
 			SyncManager syncManager, ClipboardManager clipboardManager) {
 		this.config = config;
 		this.syncManager = syncManager;
 		this.clipboardManager = clipboardManager;
 
 		this.functionality = functionality;
 
 		try {
 			this.display = Display.getDefault();
 		} catch (UnsatisfiedLinkError error) {
 			error.printStackTrace();
 
 			String message = Messages.getString("text.error.swt_loading",
 					error.getMessage());
 			JOptionPane.showMessageDialog(new JFrame(), message, "Dialog",
 					JOptionPane.ERROR_MESSAGE);
 			System.exit(1);
 		}
 		SwtBackend swtClipboard = new SwtBackend(this.clipboardManager);
 
 		this.clipboardManager.addCopyService(swtClipboard);
 		this.clipboardManager.addPasteService(swtClipboard);
 		this.clipboardManager.addPollingService(swtClipboard);
 
 		this.activityShell = new Shell(this.display);
 
 		this.setSettingsUserInterface(new PreferencesDialog(this.activityShell,
 				config, functionality, environmentIntegration));
 		this.setAboutUserInterface(new AboutDialog(this.activityShell, SWT.NONE));
 
 		DataQueue queueWindow = new DataQueue(this.activityShell,
 				SWT.DIALOG_TRIM, this.clipboardManager);
 		queueWindow.setup();
 		this.setQueueUserInterface(queueWindow);
 	}
 
 	@Override
 	public void open() {
 		if (this.config.get("run.firsttime") == null) {
 			MessageBox firstTimer = new MessageBox(this.activityShell);
 			firstTimer.setMessage(Messages.getString("text.firstrun"));
 			firstTimer.open();
 
 			this.getSettingsUserInterface().show();
 			this.config.set("run.firsttime", "nope");
 		}
 
 		// this.checkVersion();
 
 		syncManager.addListener(new SyncListener() {
 			@Override
 			public void onPushed(String content, Date date) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onPulled(final String content, Date date) {
 				if (!config.getBoolean("sync.queue.enabled")) {
 					clipboardManager.requestSet(content);
 				}
 			}
 		});
 
 		while (!this.activityShell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	@Override
 	public void close() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				activityShell.close();
			}
		});
 	}
 
 	@Override
 	public void setSettingsUserInterface(
 			SettingsUserInterface settingsUserInterface) {
 		this.settingsUserInterface = settingsUserInterface;
 	}
 
 	@Override
 	public SettingsUserInterface getSettingsUserInterface() {
 		return this.settingsUserInterface;
 	}
 
 	@Override
 	public void setAboutUserInterface(AboutUserInterface userInterface) {
 		this.aboutUserInterface = userInterface;
 	}
 
 	@Override
 	public AboutUserInterface getAboutUserInterface() {
 		return this.aboutUserInterface;
 	}
 
 	@Override
 	public void setQueueUserInterface(QueueUserInterface userInterface) {
 		this.queueUserInterface = userInterface;
 	}
 
 	@Override
 	public QueueUserInterface getQueueUserInterface() {
 		return this.queueUserInterface;
 	}
 
 }
