 package net.mms_projects.copy_it.ui.swt.forms;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import net.mms_projects.copy_it.Activatable;
 import net.mms_projects.copy_it.ClipboardManager;
 import net.mms_projects.copy_it.SettingsListener;
 import net.mms_projects.copy_it.SyncListener;
 import net.mms_projects.copy_it.listeners.EnabledListener;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 public class DataQueue extends Dialog implements SyncListener,
 		SettingsListener, Activatable {
 
 	protected Object result;
 	protected Shell shell;
 	private Table table;
 	private TableItem tableItem;
 	private boolean enabled;
 	private ClipboardManager clipboardManager;
 	private List<EnabledListener> enabledListeners = new ArrayList<EnabledListener>();
 
 	/**
 	 * Create the dialog.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	public DataQueue(Shell parent, int style, ClipboardManager clipboardManager) {
 		super(parent, style);
 
 		this.clipboardManager = clipboardManager;
 
 		setText("SWT Dialog");
 	}
 
 	public void setup() {
 		createContents();
 
 		this.shell.addListener(SWT.Close, new Listener() {
 			public void handleEvent(Event e) {
 				e.doit = false;
 				shell.setVisible(false);
 			}
 		});
 	}
 
 	/**
 	 * Open the dialog.
 	 * 
 	 * @return the result
 	 */
 	public Object open() {
 		shell.open();
 		shell.layout();
 		Display display = getParent().getDisplay();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 */
 	private void createContents() {
 		shell = new Shell(getParent(), getStyle());
 		shell.setSize(450, 300);
 		shell.setText(getText());
 		shell.setLayout(new FormLayout());
 
 		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
 		FormData fd_table = new FormData();
 		fd_table.top = new FormAttachment(0, 10);
 		fd_table.left = new FormAttachment(0, 10);
 		fd_table.bottom = new FormAttachment(100, -10);
 		fd_table.right = new FormAttachment(100, -10);
 		table.setLayoutData(fd_table);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		TableColumn tblclmnData = new TableColumn(table, SWT.NONE);
 		tblclmnData.setWidth(336);
 		tblclmnData.setText("Data");
 
 		TableColumn tblclmnDate = new TableColumn(table, SWT.NONE);
 		tblclmnDate.setWidth(100);
 		tblclmnDate.setText("Date");
 
 		Menu menu = new Menu(table);
 		MenuItem itemPaste = new MenuItem(menu, SWT.PUSH);
 		itemPaste.setText("Paste");
 		itemPaste.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				TableItem tableItem = table.getSelection()[0];
 				String data = tableItem.getText(0);
 				clipboardManager.requestSet(data);
 			}
 		});
 		table.setMenu(menu);
 	}
 
 	@Override
 	public void onPushed(String content, Date date) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onPulled(final String content, final Date date) {
 		Display.getDefault().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				if (enabled) {
 					shell.setVisible(true);
 				}
 				tableItem = new TableItem(table, SWT.NONE);
 				tableItem.setText(new String[] { content, date.toString() });
 			}
 		});
 
 	}
 
 	@Override
 	public void onChange(String key, String value) {
 		if ("sync.queue.enabled".equals(key)) {
 			this.setEnabled(Boolean.parseBoolean(value));
 		}
 	}
 
 	@Override
 	public void enable() {
		this.enabled = true;
 
 		for (EnabledListener listener : this.enabledListeners) {
 			listener.onEnabled();
 		}
 	}
 
 	@Override
 	public void disable() {
 		if (this.shell.isVisible()) {
 			this.shell.setVisible(false);
 		}
 
		this.enabled = false;
 
 		for (EnabledListener listener : this.enabledListeners) {
 			listener.onDisabled();
 		}
 	}
 
 	public void setEnabled(boolean state) {
 		if (state) {
 			this.enable();
 		} else {
 			this.disable();
 		}
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return this.enabled;
 	}
 
 	@Override
 	public void addEnabledListener(EnabledListener listener) {
 		this.enabledListeners.add(listener);
 	}
 
 }
