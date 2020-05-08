 package gui.view;
 
 import networking.ProxyLog;
 import networking.ProxyServer;
 import networking.HttpFilter;
 import networking.HttpResponseFilters;
 import networking.CustomHttpResponseFilter;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.custom.SashForm;
 import java.awt.Frame;
 
 import org.eclipse.swt.accessibility.Accessible;
 import org.eclipse.swt.awt.SWT_AWT;
 
 import java.awt.Color;
 import java.awt.Panel;
 import java.awt.BorderLayout;
 import java.awt.event.FocusEvent;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.GridData;
 import javax.swing.JTextArea;
 import javax.swing.border.Border;
 
 import org.eclipse.swt.widgets.List;
 import swing2swt.layout.FlowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ListViewer;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.ChannelGroupFuture;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 
 import storage.*;
 
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.swt.widgets.Text;
 
 public class ApplicationWindow{
 
 	//final static variables
 	final static String EXPORT = "Export";
 	final static String IMPORT = "Import";
 	
 	final static String CREATE = "Create";
 	final static String EDIT = "Edit";
 	
 	//Constructor variables
 	protected Shell shlButterfly;
 	protected Display display;
 	private ProxyServer server;
 	private Account account;
 	private Accounts accounts;
 	private JFrame frame;
 	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
 	
 	private  ListViewer filterInactiveListViewer;
 	private ListViewer  filterActiveListViewer;
 	private ListViewer AccountListViewer;
 	private Button btnCreate, btnDelete, btnEdit;
 	
 	// Status Menu Components
 	private JTextArea textAreaDialog;
 	private JTextArea textAreaConnectionList;
 	private Text textPort;
 	private Text textConnectionCount;
 	
 	/**
 	 * Launches Login window
 	 * @param shell
 	 * @return boolean
 	 */
 	private boolean authenticate(Shell shell) {
 		LoginShell login = new LoginShell(shell);
 		login.open(this);
 		return true;
 	}
 	
 	/**
 	 * Create the filter editing window. 
 	 * @param s type of window (create/edit)
 	 * @return boolean true upon close
 	 */
 	private boolean filterEdit(String s, Filter editFilter) {
 		Display display = Display.getDefault();
 		FilterShell filterEdit = new FilterShell(display, s, account, accounts);
 		filterEdit.setFilter(editFilter);
 		filterEdit.open();
 		return true;
 	}
 	
 	/**
 	 * Call the edit shell with account edit permissions
 	 * @param a
 	 * @return
 	 */
 	private boolean editUserAccount(Account a){
 		Display display = Display.getDefault();
 		EditShell eShell = new EditShell(display, a,account, accounts);
 		
 		//Disable the main window
 		shlButterfly.setEnabled(false);
 		
 		// open new window
 		eShell.open();
 		
 		//Re-Enable and make the window active
 		shlButterfly.setEnabled(true);
 		shlButterfly.setActive();
 		return true;
 	}
 	
 
 	
 	/**
 	 * Call the edit shell with edit user group permissions
 	 * @param g
 	 * @return
 	 */
 	private boolean editUserGroup(Group g){
 		Display display = Display.getDefault();
 		EditShell eShell = new EditShell(display, g, accounts);
 		
 		//Disable the main window
 		shlButterfly.setEnabled(false);
 		
 		// open new window
 		eShell.open();
 		
 		//Re-Enable and make the window active
 		if(account.getPermissions().contains(Permission.CREATEFILTER))
 			btnCreate.setEnabled(true);
 		else
 			btnCreate.setEnabled(false);
 		if(account.getPermissions().contains(Permission.EDITFILTER))
 			btnEdit.setEnabled(true);
 		else
 			btnEdit.setEnabled(false);
 		if(account.getPermissions().contains(Permission.DELETEFILTER))
 			btnDelete.setEnabled(true);
 		else
 			btnDelete.setEnabled(false);
 		shlButterfly.setEnabled(true);
 		shlButterfly.setActive();
 		return true;
 	}
 	
 	/**
 	 * Open filter import/export
 	 * @return
 	 */
 	private boolean impExpShell(Account a, String s){
 		Display display = Display.getDefault();
 		EditShell eShell = new EditShell(display, a, s, accounts);
 		
 		//Disable the main window
 		shlButterfly.setEnabled(false);
 		// open new window
 		eShell.open();
 		//Re-Enable and make the window active
 		shlButterfly.setEnabled(true);
 		shlButterfly.setActive();
 		accounts.loadAccounts();
 		List filterInactiveList = filterInactiveListViewer.getList();
 		filterInactiveList.removeAll();
 		java.util.List<Filter> fml = account.getInactiveFilters();
 		for(Filter fia: fml){
 			filterInactiveList.add(fia.toString());
 		}
 		List filteractiveList = filterActiveListViewer.getList();
 		filteractiveList.removeAll();
 		java.util.List<Filter> fma = account.getActiveFilters();
 		for(Filter fia: fma){
 			filteractiveList.add(fia.toString());
 		}	
 		return true;
 	}
 	
 	/**
 	 * Open the Account shell using the change password constructs. 
 	 * @param shell 
 	 * @param accName 
 	 * @param group
 	 * @return
 	 */
 	private boolean changePassword(Shell shell, String accName, Group group) {
 		AccountShell aShell = new AccountShell(shell, accName, group);
 	
 		//Disable the main window
 		shlButterfly.setEnabled(false);
 		
 		aShell.open(this);
 		
 		//Re-Enable and make the window active
 		shlButterfly.setEnabled(true);
 		shlButterfly.setActive();
 		return true;
 	}
 
 	/**
 	 * Launches Create Account window
 	 * @param shell
 	 * @return
 	 */
 	private boolean accountShell(Shell shell){
 		AccountShell aShell = new AccountShell(shell);
 		shlButterfly.setEnabled(false);
 		
 		aShell.open(this);
 		List AccountList = AccountListViewer.getList();
 		AccountList.removeAll();
 		AccountList.add("Administrator");
 		AccountList.add("Power");
 		AccountList.add("Standard");
 		accounts.loadAccounts();
 		for(Account ac: accounts){
 			
 			AccountList.add(ac.getName());
 		}
 		shlButterfly.setEnabled(true);
 		shlButterfly.setActive();
 		
 		return true;
 		
 	}
 	/**
 	 * 
 	 * @param a
 	 */
 	public void setAccount(Account a){
 		account = a;
 	}
 	
 
 	/**
 	 * Open the window.
 	 * @wbp.parser.entryPoint
 	 */
 	public void open() {
 		Display display = Display.getDefault();
 		final Shell shell = new Shell(display);
 		authenticate(shell);
 		createContents();
 		shlButterfly.open();
 		shlButterfly.layout();
 		while (!shlButterfly.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		if (!shlButterfly.isDisposed()) {
 			shlButterfly.dispose();
 		}
 		display.dispose();
 		System.exit(0);
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shlButterfly = new Shell(SWT.ON_TOP | SWT.CLOSE | SWT.TITLE | SWT.MIN);
 		shlButterfly.setSize(800, 600);
 		shlButterfly.setText("Butterfly - Logged in as "+ account.getName());
 		shlButterfly.setLayout(new FillLayout(SWT.HORIZONTAL));
 		
 		CTabFolder tabFolder = new CTabFolder(shlButterfly, SWT.BORDER);
 		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
 		
 		Border border;
 		border = BorderFactory.createLineBorder(Color.black);
 		
 		//-----------------------------------------------------------------
 		// Status Menu Item
 		//-----------------------------------------------------------------
 		CTabItem tbtmStatus = new CTabItem(tabFolder, SWT.NONE);
 		tbtmStatus.setText("Status");
 		
 		Composite statusComposite = new Composite(tabFolder, SWT.NONE);
 		tbtmStatus.setControl(statusComposite);
 		statusComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
 		
 		Composite statusCompositeLeft = new Composite(statusComposite, SWT.NONE);
 		formToolkit.adapt(statusCompositeLeft);
 		formToolkit.paintBordersFor(statusCompositeLeft);
 		statusCompositeLeft.setLayout(new GridLayout(1, false));
 		
 		Composite composite_1 = new Composite(statusCompositeLeft, SWT.BORDER);
 		composite_1.setLayout(new GridLayout(1, false));
 		GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
 		gd_composite_1.heightHint = 453;
 		gd_composite_1.widthHint = 469;
 		composite_1.setLayoutData(gd_composite_1);
 		formToolkit.adapt(composite_1);
 		formToolkit.paintBordersFor(composite_1);
 		
 		//Connection List label
 		Label lblConnectionList = new Label(composite_1, SWT.NONE);
 		lblConnectionList.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 		formToolkit.adapt(lblConnectionList, true, true);
 		lblConnectionList.setText("Connection List");
 		
 		//Tons of stuff for putting jtext areas in swt applications
 		Composite composite_4 = new Composite(composite_1, SWT.NONE);
 		composite_4.setLayout(new FillLayout(SWT.HORIZONTAL));
 		GridData gd_composite_4 = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
 		gd_composite_4.heightHint = 431;
 		gd_composite_4.widthHint = 386;
 		composite_4.setLayoutData(gd_composite_4);
 		formToolkit.adapt(composite_4);
 		formToolkit.paintBordersFor(composite_4);
 		
 		Composite composite_5 = new Composite(composite_4, SWT.EMBEDDED);
 		formToolkit.adapt(composite_5);
 		formToolkit.paintBordersFor(composite_5);
 		
 		Frame frame_2 = SWT_AWT.new_Frame(composite_5);
 		
 		Panel panel_1 = new Panel();
 		frame_2.add(panel_1);
 		panel_1.setLayout(new BorderLayout(0, 0));
 		
 		JRootPane rootPane_1 = new JRootPane();
 		panel_1.add(rootPane_1);
 		rootPane_1.getContentPane().setLayout(new java.awt.GridLayout(1, 0, 0, 0));
 		
 			// Initialize text area connection list
 			textAreaConnectionList = new JTextArea();
 			//rootPane_1.getContentPane().add(textAreaConnectionList);
 			textAreaConnectionList.setEditable(false);
 			textAreaConnectionList.setBorder(border);
 			JScrollPane sbConnectionList = new JScrollPane(textAreaConnectionList);
 			rootPane_1.getContentPane().add(sbConnectionList);
 		
 		Composite composite = new Composite(statusCompositeLeft, SWT.BORDER);
 		GridData gd_composite = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
 		gd_composite.widthHint = 385;
 		composite.setLayoutData(gd_composite);
 		composite.setLayout(new GridLayout(3, false));
 		//composite.setBorder(border);
 		
 		formToolkit.adapt(composite);
 		formToolkit.paintBordersFor(composite);
 		
 		//Port Label
 		Label lblPort = new Label(composite, SWT.NONE);
 		formToolkit.adapt(lblPort, true, true);
 		lblPort.setText("Port:");
 		
 			// Initialize Port Text Field
 			textPort = new Text(composite, SWT.BORDER);
 			textPort.setText(Integer.toString(accounts.getPortNumber()));
 			GridData gd_textPort = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
 			gd_textPort.widthHint = 262;
 			textPort.setLayoutData(gd_textPort);
 			formToolkit.adapt(textPort, true, true);
 			if (!account.getPermissions().contains(Permission.SETPORT)) {
 				textPort.setEditable(false);
 			}
 		
 			// Initialize Listen Button for the port
 			final Button btnListen = new Button(composite, SWT.NONE);
 			GridData gd_btnListen = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2);
 			gd_btnListen.heightHint = 50;
 			gd_btnListen.widthHint = 70;
 			btnListen.setLayoutData(gd_btnListen);
 			btnListen.setSelection(true);
 			formToolkit.adapt(btnListen, true, true);
 			btnListen.setText("Listen");
 			
 			//Listen Button listener
 			btnListen.addListener(SWT.Selection, new Listener(){
 				public void handleEvent(Event e){
 					switch(e.type){
 					case SWT.Selection:
 						if(server != null && server.isRunning()) {
 							server.stop();
 							btnListen.setText("Listen");
 						}
 						else {
 							accounts.setPortNumber(Integer.parseInt(textPort.getText()));
 							server = new ProxyServer(accounts.getPortNumber(), new HttpResponseFilters() {
 								public HttpFilter getFilter(String hostAndPort) {
 									return new CustomHttpResponseFilter(account.getActiveFilters());
 								}}, null);
 							server.start();
 							btnListen.setText("Stop");
 						}
 						accounts.saveAccounts();
 					}
 				}
 			});
 		
 		// Connection Count Label
 		Label lblConnections = new Label(composite, SWT.NONE);
 		lblConnections.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
 		formToolkit.adapt(lblConnections, true, true);
 		lblConnections.setText("Connection Count:");
 		
 			// Initialize connection count text field
			//TODO change to JTextArea not Text
 			textConnectionCount = new Text(composite, SWT.BORDER);
 			textConnectionCount.setText("0");
 			GridData gd_textConnectionCount = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
 			gd_textConnectionCount.widthHint = 266;
 			textConnectionCount.setLayoutData(gd_textConnectionCount);
 			formToolkit.adapt(textConnectionCount, true, true);
 			//disable the fields
 			textConnectionCount.setEnabled(false);
			//ProxyLog.setCountText(textConnectionCount);
 		
 		Composite statusCompositeRight = new Composite(statusComposite, SWT.NONE);
 		formToolkit.adapt(statusCompositeRight);
 		formToolkit.paintBordersFor(statusCompositeRight);
 		statusCompositeRight.setLayout(new GridLayout(1, false));
 		
 		Composite composite_2 = new Composite(statusCompositeRight, SWT.BORDER);
 		composite_2.setLayout(new GridLayout(1, false));
 		GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
 		gd_composite_2.heightHint = 529;
 		gd_composite_2.widthHint = 390;
 		composite_2.setLayoutData(gd_composite_2);
 		formToolkit.adapt(composite_2);
 		formToolkit.paintBordersFor(composite_2);
 		
 		//Dialog label
 		Label lblNewLabel_2 = new Label(composite_2, SWT.NONE);
 		lblNewLabel_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 		formToolkit.adapt(lblNewLabel_2, true, true);
 		lblNewLabel_2.setText("Dialog");
 		
 		//Tons of stuff for the dialog text area
 		Composite composite_3 = new Composite(composite_2, SWT.EMBEDDED);
 		composite_3.setLayout(new FillLayout(SWT.HORIZONTAL));
 		GridData gd_composite_3 = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
 		gd_composite_3.heightHint = 523;
 		gd_composite_3.widthHint = 407;
 		composite_3.setLayoutData(gd_composite_3);
 		formToolkit.adapt(composite_3);
 		formToolkit.paintBordersFor(composite_3);
 		
 		Frame frame_1 = SWT_AWT.new_Frame(composite_3);
 		
 		Panel panel = new Panel();
 		frame_1.add(panel);
 		panel.setLayout(new BorderLayout(0, 0));
 		
 		JRootPane rootPane = new JRootPane();
 		panel.add(rootPane);
 		rootPane.getContentPane().setLayout(new java.awt.GridLayout(1, 0, 0, 0));
 		
 			// Initialize Text Area for Dialog
 			textAreaDialog= new JTextArea();
 			textAreaDialog.setLineWrap(true);
 			//rootPane.getContentPane().add(textAreaDialog);
 			textAreaDialog.setBorder(border); //set border
 			textAreaDialog.setEditable(false); // meddling in my text area
 			JScrollPane sbDialog = new JScrollPane(textAreaDialog);
 			rootPane.getContentPane().add(sbDialog);
 		
 		//-----------------------------------------------------------------
 		// Filters menu item
 		//-----------------------------------------------------------------
 		CTabItem tbtmNewItem = new CTabItem(tabFolder, SWT.NONE);
 		tbtmNewItem.setText("Filters");
 		
 		Composite filterComposite = new Composite(tabFolder, SWT.NONE);
 		tbtmNewItem.setControl(filterComposite);
 		formToolkit.paintBordersFor(filterComposite);
 		filterComposite.setLayout(new GridLayout(1, false));
 		
 		Composite filterComposite_1 = new Composite(filterComposite, SWT.NONE);
 		GridData gd_filterComposite_1 = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
 		gd_filterComposite_1.widthHint = 768;
 		gd_filterComposite_1.heightHint = 474;
 		filterComposite_1.setLayoutData(gd_filterComposite_1);
 		formToolkit.adapt(filterComposite_1);
 		formToolkit.paintBordersFor(filterComposite_1);
 		filterComposite_1.setLayout(new GridLayout(3, false));
 		
 		Label lblActiveFilters = new Label(filterComposite_1, SWT.NONE);
 		lblActiveFilters.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 		formToolkit.adapt(lblActiveFilters, true, true);
 		lblActiveFilters.setText("Active Filters");
 		new Label(filterComposite_1, SWT.NONE);
 		
 		Label lblInactiveFilters = new Label(filterComposite_1, SWT.NONE);
 		lblInactiveFilters.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 		formToolkit.adapt(lblInactiveFilters, true, true);
 		lblInactiveFilters.setText("Inactive Filters");
 		
 		//Active Filter composite
 		Composite filterActiveComposite = new Composite(filterComposite_1, SWT.NONE);
 		GridData gd_filterActiveComposite = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
 		gd_filterActiveComposite.heightHint = 465;
 		gd_filterActiveComposite.widthHint = 333;
 		
 		filterActiveComposite.setLayoutData(gd_filterActiveComposite);
 		formToolkit.adapt(filterActiveComposite);
 		formToolkit.paintBordersFor(filterActiveComposite);
 		filterActiveComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
 			
 			
 				// Active filter list viewer
 				filterActiveListViewer = new ListViewer(filterActiveComposite, SWT.BORDER | SWT.V_SCROLL);
 				List filterActiveList = filterActiveListViewer.getList();
 			java.util.List<Filter> f = account.getActiveFilters();
 			for(Filter fil: f){
 				filterActiveList.add(fil.toString());
 			}
 			if(account.getGroup()!= Group.STANDARD){
 				f = account.getDefaultFilters();
 				for(Filter fil : f){
 					filterActiveList.add(fil.toString());
 				}
 			}
 			
 		//Filter middle button bar
 		Composite filterBtnComposite = new Composite(filterComposite_1, SWT.NONE);
 		filterBtnComposite.setLayout(new FillLayout(SWT.VERTICAL));
 		GridData gd_filterBtnComposite = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
 		gd_filterBtnComposite.heightHint = 465;
 		gd_filterBtnComposite.widthHint = 85;
 		filterBtnComposite.setLayoutData(gd_filterBtnComposite);
 		formToolkit.adapt(filterBtnComposite);
 		formToolkit.paintBordersFor(filterBtnComposite);
 		
 		Composite filterBtnComposite_NORTH = new Composite(filterBtnComposite, SWT.NONE);
 		formToolkit.adapt(filterBtnComposite_NORTH);
 		formToolkit.paintBordersFor(filterBtnComposite_NORTH);
 		
 		Composite filterBtnComposite_CENTER = new Composite(filterBtnComposite, SWT.NONE);
 		formToolkit.adapt(filterBtnComposite_CENTER);
 		formToolkit.paintBordersFor(filterBtnComposite_CENTER);
 		filterBtnComposite_CENTER.setLayout(new FillLayout(SWT.HORIZONTAL));
 		
 			//Add from inactive to active
 			Button btnAdd = new Button(filterBtnComposite_CENTER, SWT.NONE);
 			formToolkit.adapt(btnAdd, true, true);
 			btnAdd.setText("<");
 
 			//Remove from active to inactive
 			Button btnRemove = new Button(filterBtnComposite_CENTER, SWT.NONE);
 			formToolkit.adapt(btnRemove, true, true);
 			btnRemove.setText(">");
 		
 		
 		Composite filterBtnComposite_SOUTH = new Composite(filterBtnComposite, SWT.NONE);
 		formToolkit.adapt(filterBtnComposite_SOUTH);
 		formToolkit.paintBordersFor(filterBtnComposite_SOUTH);
 		
 		//Inactive filter composite
 		Composite filterInactiveComposite = new Composite(filterComposite_1, SWT.NONE);
 		GridData gd_filterInactiveComposite = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
 		gd_filterInactiveComposite.heightHint = 465;
 		gd_filterInactiveComposite.widthHint = 333;
 		filterInactiveComposite.setLayoutData(gd_filterInactiveComposite);
 		formToolkit.adapt(filterInactiveComposite);
 		formToolkit.paintBordersFor(filterInactiveComposite);
 		filterInactiveComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
 			
 				//Inactive List viewer
 				filterInactiveListViewer = new ListViewer(filterInactiveComposite, SWT.BORDER | SWT.V_SCROLL);
 				List filterInactiveList = filterInactiveListViewer.getList();
 			java.util.List<Filter> fml = account.getInactiveFilters();
 			for(Filter fil: fml){
 				filterInactiveList.add(fil.toString());
 			}
 		//Filter Button Bar
 		Composite filterBtnBarComposite = new Composite(filterComposite, SWT.NONE);
 		filterBtnBarComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
 		GridData gd_filterBtnBarComposite = new GridData(SWT.LEFT, SWT.BOTTOM, true, true, 1, 1);
 		gd_filterBtnBarComposite.widthHint = 771;
 		gd_filterBtnBarComposite.heightHint = 28;
 		filterBtnBarComposite.setLayoutData(gd_filterBtnBarComposite);
 		formToolkit.adapt(filterBtnBarComposite);
 		formToolkit.paintBordersFor(filterBtnBarComposite);
 			//Create filter
 			btnCreate = new Button(filterBtnBarComposite, SWT.NONE);
 			formToolkit.adapt(btnCreate, true, true);
 			btnCreate.setText(CREATE);
 			//Create filter button listener. Open blank text area.
 			btnCreate.addListener(SWT.Selection, new Listener(){
 				public void handleEvent(Event e){
 					switch (e.type){
 					case SWT.Selection:
 						btnCreateHandleEvent();
 					}
 				}
 			}
 			);
 			if(!account.getPermissions().contains(Permission.CREATEFILTER)){
 				btnCreate.setEnabled(false);
 			}
 			//Add Filter from inactive to active list
 			btnAdd.addListener(SWT.Selection, new Listener(){
 
 				@Override
 				public void handleEvent(Event event) {
 					switch(event.type){
 					case SWT.Selection:
 						btnAddHandleEvent();
 					}
 				}
 				
 			});
 			
 			btnRemove.addListener(SWT.Selection, new Listener(){
 
 				@Override
 				public void handleEvent(Event event) {
 					switch(event.type){
 					case SWT.Selection:
 						btnRemoveHandleEvent();
 					}
 				}
 				
 			});
 			
 		
 			
 			//Edit filter
 			btnEdit = new Button(filterBtnBarComposite, SWT.NONE);
 			formToolkit.adapt(btnEdit, true, true);
 			btnEdit.setText(EDIT);
 			
 			//Create filter button listener. Open text area with highlighted filters text.
 			btnEdit.addListener(SWT.Selection, new Listener(){
 				public void handleEvent(Event e){
 					switch (e.type){
 					case SWT.Selection:
 						btnEditHandleEvent();
 					}
 				}
 			}
 			);
 			if(!account.getPermissions().contains(Permission.EDITFILTER)){
 				btnEdit.setEnabled(false);
 			}
 				//Delete filter
 				btnDelete = new Button(filterBtnBarComposite, SWT.NONE);
 				formToolkit.adapt(btnDelete, true, true);
 				btnDelete.setText("Delete");
 			
 				btnDelete.addListener(SWT.Selection, new Listener(){
 					public void handleEvent(Event e){
 						switch(e.type){
 						case SWT.Selection:
 							btnDeleteHandleEvent();
 						
 						}
 					}
 				});
 				if(!account.getPermissions().contains(Permission.DELETEFILTER)){
 					btnDelete.setEnabled(false);
 				}
 		
 		//-----------------------------------------------------------------
 		//Administrator Tab
 		//-----------------------------------------------------------------
 		if(account.getGroup()==Group.ADMINISTRATOR){
 		CTabItem tbtmAdministrator = new CTabItem(tabFolder, SWT.NONE);
 		tbtmAdministrator.setText("Administrator");
 		
 		Composite admComposite = new Composite(tabFolder, SWT.NONE);
 		tbtmAdministrator.setControl(admComposite);
 		formToolkit.paintBordersFor(admComposite);
 		admComposite.setLayout(new GridLayout(3, false));
 			
 			Label lblAccounts = new Label(admComposite, SWT.NONE);
 			lblAccounts.setAlignment(SWT.CENTER);
 			GridData gd_lblAccounts = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
 			gd_lblAccounts.widthHint = 256;
 			lblAccounts.setLayoutData(gd_lblAccounts);
 			formToolkit.adapt(lblAccounts, true, true);
 			lblAccounts.setText("Accounts");
 			
 			//Active Filter label
 			Label lblNewLabel = new Label(admComposite, SWT.NONE);
 			lblNewLabel.setAlignment(SWT.CENTER);
 			GridData gd_lblNewLabel = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
 			gd_lblNewLabel.widthHint = 255;
 			lblNewLabel.setLayoutData(gd_lblNewLabel);
 			formToolkit.adapt(lblNewLabel, true, true);
 			lblNewLabel.setText("Active Filters");
 			
 			//Inactive filter label
 			Label lblNewLabel_1 = new Label(admComposite, SWT.NONE);
 			lblNewLabel_1.setAlignment(SWT.CENTER);
 			lblNewLabel_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 			formToolkit.adapt(lblNewLabel_1, true, true);
 			lblNewLabel_1.setText("Inactive Filters");
 		
 			Composite admTableTreeComposite = new Composite(admComposite, SWT.NONE);
 			GridData gd_admTableTreeComposite = new GridData(SWT.LEFT, SWT.TOP, true, true, 3, 1);
 			gd_admTableTreeComposite.heightHint = 484;
 			gd_admTableTreeComposite.widthHint = 778;
 			
 			admTableTreeComposite.setLayoutData(gd_admTableTreeComposite);
 			formToolkit.adapt(admTableTreeComposite);
 			formToolkit.paintBordersFor(admTableTreeComposite);
 			admTableTreeComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
 			
 			// Account viewer
 			AccountListViewer = new ListViewer(admTableTreeComposite, SWT.BORDER | SWT.V_SCROLL);
 			final List AccountList = AccountListViewer.getList();
 			
 			AccountList.add("Administrator");
 			AccountList.add("Power");
 			AccountList.add("Standard");
 			
 			//TODO set active list to selections current active filters
 			ListViewer activeViewer = new ListViewer(admTableTreeComposite, SWT.BORDER | SWT.V_SCROLL);
 			final List activeList = activeViewer.getList();
 			
 			//TODO set inactive list to selections current inactive filters
 			ListViewer inactiveViewer = new ListViewer(admTableTreeComposite, SWT.BORDER | SWT.V_SCROLL);
 			final List inactiveList = inactiveViewer.getList();
 							
 									// Administrator button bar
 									Composite admBtnBarComposite = formToolkit.createComposite(admComposite, SWT.NONE);
 									GridData gd_admBtnBarComposite = new GridData(SWT.LEFT, SWT.TOP, true, false, 3, 1);
 									gd_admBtnBarComposite.widthHint = 779;
 									gd_admBtnBarComposite.heightHint = 28;
 									admBtnBarComposite.setLayoutData(gd_admBtnBarComposite);
 									formToolkit.paintBordersFor(admBtnBarComposite);
 									admBtnBarComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
 									
 										//Create an Account
 										Button admBtnCreate = new Button(admBtnBarComposite, SWT.NONE);
 										formToolkit.adapt(admBtnCreate, true, true);
 										admBtnCreate.setText(CREATE);
 										
 										admBtnCreate.addListener(SWT.Selection, new Listener(){
 											public void handleEvent(Event e){
 												switch (e.type){
 												case SWT.Selection:
 													Shell accShell = new Shell(display);
 													accountShell(accShell);
 													
 												}
 											}
 										}
 										);
 										
 										//TODO implement user group edit/accounts
 										//Edit User Groups/Accounts
 										Button admBtnEdit = new Button(admBtnBarComposite, SWT.NONE);
 										formToolkit.adapt(admBtnEdit, true, true);
 										admBtnEdit.setText(EDIT);
 										
 											admBtnEdit.addListener(SWT.Selection, new Listener(){
 												public void handleEvent(Event e){
 													switch (e.type){
 													case SWT.Selection:
 														Accounts acc = new Accounts();
 														acc.loadAccounts();
 													try{
 														String selection = AccountList.getSelection()[0].trim();
 														Account a =acc.getAccount(selection);
 														if(a == null){
 															if(Group.valueOf(selection.toUpperCase())!= null){
 																editUserGroup(Group.valueOf(selection.toUpperCase()));
 															}
 														}
 														else{
 															//editShell(a,"Administrator");
 															
 															//TODO If an account is selected
 															editUserAccount(a);
 														}
 															//TODO If a user group is selected
 															//editUserGroup(group);
 													}catch(ArrayIndexOutOfBoundsException ex){
 														
 													}
 													catch(IllegalArgumentException ex){}
 													}
 												}
 											}
 											);
 											//Delete User Accounts
 											final Button admBtnDelete = new Button(admBtnBarComposite, SWT.NONE);
 											formToolkit.adapt(admBtnDelete, true, true);
 											admBtnDelete.setText("Delete");
 											admBtnDelete.addListener(SWT.Selection, new Listener(){
 												public void handleEvent(Event e){
 													switch(e.type){
 													case SWT.Selection:
 														Accounts acc = new Accounts();
 														acc.loadAccounts();
 														Account a =acc.getAccount(AccountList.getSelection()[0].trim());
 														if(a!=null){
 															if(!a.getName().equals(account.getName())){
 															acc.removeAccount(a);
 															acc.saveAccounts();
 															AccountList.removeAll();
 															AccountList.add("Administrator");
 															AccountList.add("Power");
 															AccountList.add("Standard");
 															for(Account ac: acc){
 																
 																AccountList.add(ac.getName());
 															}
 															}
 															
 														}
 														else
 															System.err.println("No account selected");
 													}
 												}
 											});
 											AccountListViewer.addSelectionChangedListener(new ISelectionChangedListener(){
 
 
 												@Override
 												public void selectionChanged(SelectionChangedEvent e) {
 													try{
 													String selection = AccountList.getSelection()[0].trim();
 													if(selection.equals("Administrator")||selection.equals("Power")||selection.equals("Standard")){
 														activeList.removeAll();
 														inactiveList.removeAll();
 														admBtnDelete.setEnabled(false);
 													}
 													else if(accounts.getAccount(selection)!=null){
 														Account acc = accounts.getAccount(selection);
 														activeList.removeAll();
 														inactiveList.removeAll();
 														for(Filter f: acc.getActiveFilters())
 															activeList.add(f.getName());
 														for(Filter f: acc.getInactiveFilters())
 															inactiveList.add(f.getName());
 														if(acc.getName().equals(account.getName()))
 															admBtnDelete.setEnabled(false);
 														else
 															admBtnDelete.setEnabled(true);
 													}
 													else
 														admBtnDelete.setEnabled(true);
 													
 													}catch(ArrayIndexOutOfBoundsException ex){
 														
 													}
 												}
 												
 											});
 		
 	
 				Accounts a = new Accounts();
 				a.loadAccounts(); 
 				for(Account acc: a){
 					if(acc.getGroup()==Group.ADMINISTRATOR)
 						AccountList.add("\t"+acc.getName());
 					
 				}
 				for(Account acc: a){
 					if(acc.getGroup()==Group.POWER)
 						AccountList.add("\t"+acc.getName());
 				}
 				for(Account acc: a){
 					if(acc.getGroup()==Group.STANDARD)
 							AccountList.add("\t"+acc.getName());
 						
 				}
 		
 		}
 		
 		//-----------------------------------------------------------------
 		//Main menu bar
 		//-----------------------------------------------------------------
 		Menu menu = new Menu(shlButterfly, SWT.BAR);
 		shlButterfly.setMenuBar(menu);
 		
 		// Menu Bar Main
 		MenuItem mntmMain = new MenuItem(menu, SWT.CASCADE);
 		mntmMain.setText("Main");
 		
 		Menu menu_main = new Menu(mntmMain);
 		mntmMain.setMenu(menu_main);
 		
 		/* commented this out because we moved it but I'm leaving this here for now
 		//Listen if the user is not a standard user
 		if (account.getGroup() != Group.STANDARD) {
 			final MenuItem mntmListen = new MenuItem(menu_main, SWT.CHECK);
 			//Set Listen to default on
 			mntmListen.setSelection(false);
 			mntmListen.setText("Listen");
 			mntmListen.addListener(SWT.Selection, new Listener(){
 				public void handleEvent(Event e){
 					if (mntmListen.getSelection()) {
 						//TODO turn on the proxy
 						System.out.println("Checked");
 					} else {
 						//TODO turn off the proxy
 						System.out.println("Uncheck");
 					}
 				}
 			});
 		}*/
 			//Import
 			MenuItem mntmImport = new MenuItem(menu_main, SWT.NONE);
 			mntmImport.setText(IMPORT);
 			mntmImport.addListener(SWT.Selection, new Listener(){
 				public void handleEvent(Event e){
 					switch (e.type){
 					case SWT.Selection:
 						impExpShell(account, IMPORT);
 					}
 				}
 			}
 			);
 			
 			//Export
 			MenuItem mntmExport = new MenuItem(menu_main, SWT.NONE);
 			mntmExport.setText(EXPORT);
 			
 			mntmExport.addListener(SWT.Selection, new Listener(){
 				public void handleEvent(Event e){
 					switch (e.type){
 					case SWT.Selection:
 						impExpShell(account, EXPORT);
 					}
 				}
 			}
 			);
 			
 			//Logout
 			MenuItem mntmNewItem = new MenuItem(menu_main, SWT.NONE);
 			mntmNewItem.setText("Logout");
 			
 			mntmNewItem.addListener(SWT.Selection, new Listener(){
 				public void handleEvent(Event e){
 					switch (e.type){
 					case SWT.Selection:
 						//TODO Logout, currently I (Zong) implemented this naive way of doing it. Please correct it if its wrong
 						if (server != null && server.isRunning()) {
 							server.stop();
 						}
 						if (!shlButterfly.isDisposed()) {
 							shlButterfly.dispose();
 						}
 						open();
 					}
 				}
 			}
 			);
 			//Quit
 			MenuItem mntmQuit = new MenuItem(menu_main, SWT.NONE);
 			mntmQuit.setText("Quit");
 			mntmQuit.addListener(SWT.Selection, new Listener(){
 				public void handleEvent(Event e){
 					switch (e.type){
 					case SWT.Selection:
 						if (!shlButterfly.isDisposed()) {
 							shlButterfly.dispose();
 						}
 					}
 				}
 			});
 			
 		// Menu Bar Settings	
 		MenuItem mntmSettings = new MenuItem(menu, SWT.CASCADE);
 		mntmSettings.setText("Settings");
 		
 		Menu menu_settings = new Menu(mntmSettings);
 		mntmSettings.setMenu(menu_settings);
 		
 				//Change Password
 				MenuItem mntmChangePassword = new MenuItem(menu_settings, SWT.NONE);
 				mntmChangePassword.setText("Change Password");
 				
 				mntmChangePassword.addListener(SWT.Selection, new Listener(){
 					public void handleEvent(Event e) {
 						Shell aShell = new Shell(display);
 						changePassword(aShell, account.getName(), account.getGroup());
 						
 					}
 				});
 
 				// Initialize proxy log items to settings previously set
 				ProxyLog.setLogEnabled(accounts.isLogEnabled());
 				if (accounts.isDialogEnabled()) {
 					ProxyLog.setDialogText(textAreaDialog);
 				}
 				if (accounts.isConnectionListEnabled()) {
 					ProxyLog.setConnectionText(textAreaConnectionList);
 				}
 				if (account.getGroup()==Group.ADMINISTRATOR || accounts.isDialogEnabled()) {
 					MenuItem mntmLogging = new MenuItem(menu, SWT.CASCADE);
 					mntmLogging.setText("Logging");
 					
 					Menu menu_logging = new Menu(mntmLogging);
 					mntmLogging.setMenu(menu_logging);
 
 					/*
 					 *  Menu Item for Clear Dialog
 					 */
 					final MenuItem mntmClearDialog = new MenuItem(menu_logging, SWT.NONE);
 					mntmClearDialog.setText("Clear Dialog");
 
 					// Listener for selection of Clear Dialog
 					mntmClearDialog.addListener(SWT.Selection, new Listener(){
 						public void handleEvent(Event e) {
 							ProxyLog.clearDialog();
 						}
 					});
 					if (account.getGroup()==Group.ADMINISTRATOR) {
 						
 						/*
 						 * MenuItem for Enable Log
 						 */
 						final MenuItem mntmEnableLogging = new MenuItem(menu_logging, SWT.CHECK);
 						mntmEnableLogging.setText("Enable Log");
 						
 						// Load default/previous setting
 						mntmEnableLogging.setSelection(accounts.isLogEnabled());
 						
 						// Listener for selection of Enable Log
 						mntmEnableLogging.addListener(SWT.Selection, new Listener(){
 							public void handleEvent(Event e) {
 								ProxyLog.setLogEnabled(mntmEnableLogging.getSelection());
 								accounts.setLogEnabled(mntmEnableLogging.getSelection());
 								accounts.saveAccounts();
 							}
 						});
 						
 						/*
 						 * MenuItem for Enable Dialog
 						 */
 						final MenuItem mntmNewCheckbox = new MenuItem(menu_logging, SWT.CHECK);
 						mntmNewCheckbox.setText("Enable Dialog");
 						
 						// Load default/previous setting
 						mntmNewCheckbox.setSelection(accounts.isDialogEnabled());
 						
 						// Listener for selection of Enable Dialog
 						mntmNewCheckbox.addListener(SWT.Selection, new Listener(){
 							public void handleEvent(Event e) {
 								if (mntmNewCheckbox.getSelection() == true) {
 									ProxyLog.setDialogText(textAreaDialog);
 									accounts.setDialogEnabled(true);
 								} else {
 									ProxyLog.setDialogText(null);
 									accounts.setDialogEnabled(false);
 								}
 								accounts.saveAccounts();
 							}
 						});
 						
 						/*
 						 *  Menu Item for Connection List
 						 */
 						final MenuItem mntmEnableConnectionList = new MenuItem(menu_logging, SWT.CHECK);
 						mntmEnableConnectionList.setText("Enable Connection List");
 						
 						// Load default/previous setting
 						mntmEnableConnectionList.setSelection(accounts.isConnectionListEnabled());
 						
 						// Listener for selection of Enable Connection List
 						mntmEnableConnectionList.addListener(SWT.Selection, new Listener(){
 							public void handleEvent(Event e) {
 								if (mntmEnableConnectionList.getSelection() == true) {
 									ProxyLog.setConnectionText(textAreaConnectionList);
 									accounts.setConnectionListEnabled(true);
 								} else {
 									ProxyLog.setConnectionText(null);
 									accounts.setConnectionListEnabled(false);
 								}
 								accounts.saveAccounts();
 							}
 						});
 					}
 				}
 				
 				
 	}
 	public void setAccounts(Accounts a){
 		accounts = a;
 	}
 	private void btnDeleteHandleEvent(){
 		try{
 			List activeFilters = filterActiveListViewer.getList();
 			List inactiveFilters = filterInactiveListViewer.getList();
 			String filter;
 			if(inactiveFilters.getSelection().length!=0)
 				filter = inactiveFilters.getSelection()[0];
 			else
 				filter = activeFilters.getSelection()[0];
 			String[] fil = filter.split(":");
 			String filterName = fil[0];
 			account.removeFilter(Integer.parseInt(filterName));
 			accounts.saveAccounts();
 			inactiveFilters.removeAll();
 			activeFilters.removeAll();
 			java.util.List<Filter> fml = account.getInactiveFilters();
 			for(Filter fia: fml){
 				inactiveFilters.add(fia.toString());
 			}
 			fml = account.getActiveFilters();
 			for(Filter fia: fml){
 				activeFilters.add(fia.toString());
 			}
 			
 		}catch(ArrayIndexOutOfBoundsException exc){
 			
 			System.err.println("Didn't select anything");
 		}
 	}
 	private void btnAddHandleEvent(){
 		List al = filterActiveListViewer.getList();
 		List il = filterInactiveListViewer.getList();
 		try{
 			String filter = il.getSelection()[0];
 			String[] fil = filter.split(":");
 			String filterName = fil[0];
 			Filter removedFilter = account.removeInactiveFilter(Integer.parseInt(filterName));
 			account.addFilter(removedFilter);
 			accounts.saveAccounts();
 			il.remove(filter);
 			al.add(filter);
 		}catch(ArrayIndexOutOfBoundsException e){
 			System.err.println("Didn't select anything");
 		}
 	}
 	private void btnRemoveHandleEvent(){
 		try{
 			List al = filterActiveListViewer.getList();
 			List il = filterInactiveListViewer.getList();
 			
 			String filter = al.getSelection()[0];
 			String[] fil = filter.split(":");
 			String filterName = fil[0];
 			Filter removedFilter = account.removeActiveFilter(Integer.parseInt(filterName));
 			account.addInactiveFilter(removedFilter);
 			accounts.saveAccounts();
 			al.remove(filter);
 			il.add(filter);
 		}catch(ArrayIndexOutOfBoundsException e){
 			System.err.println("Didn't select anything");
 		}
 	}
 	private void btnCreateHandleEvent(){
 		filterEdit(CREATE,null);
 		//Repopulate filter lists
 		List filterInactiveList = filterInactiveListViewer.getList();
 		filterInactiveList.removeAll();
 		java.util.List<Filter> fml = account.getInactiveFilters();
 		for(Filter fia: fml){
 			filterInactiveList.add(fia.toString());
 		}
 		List filteractiveList = filterActiveListViewer.getList();
 		filteractiveList.removeAll();
 		java.util.List<Filter> fma = account.getActiveFilters();
 		for(Filter fia: fma){
 			filteractiveList.add(fia.toString());
 		}
 	}
 	private void btnEditHandleEvent(){
 		List activeFilters = filterActiveListViewer.getList();
 		List inactiveFilters = filterInactiveListViewer.getList();
 		try{
 			String filter;
 			if(inactiveFilters.getSelection().length!=0)
 				filter = inactiveFilters.getSelection()[0];
 			else
 				filter = activeFilters.getSelection()[0];
 			String[] fil = filter.split(":");
 			String filterName = fil[0];
 			Filter editFilter = account.getFilter(Integer.parseInt(filterName));
 			filterEdit(EDIT,editFilter);
 			//Repopulate filter lists
 			List filterInactiveList = filterInactiveListViewer.getList();
 			filterInactiveList.removeAll();
 			java.util.List<Filter> fml = account.getInactiveFilters();
 			for(Filter fia: fml){
 				filterInactiveList.add(fia.toString());
 			}
 			List filteractiveList = filterActiveListViewer.getList();
 			filteractiveList.removeAll();
 			java.util.List<Filter> fma = account.getActiveFilters();
 			for(Filter fia: fma){
 				filteractiveList.add(fia.toString());
 			}
 		}catch(ArrayIndexOutOfBoundsException exc){
 			System.err.println("Didn't select anything");
 		}
 	}
 }
