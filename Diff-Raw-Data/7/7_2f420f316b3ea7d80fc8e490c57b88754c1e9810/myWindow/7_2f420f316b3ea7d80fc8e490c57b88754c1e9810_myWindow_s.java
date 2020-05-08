 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.StatusLineManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import weito.Main;
 import debug.Printer;
 import debug.TextViewerPrinter;
 
 
 public class myWindow extends ApplicationWindow {
 	private Action newRun;
 	private StyledText styledText;
 
 	/**
 	 * Create the application window.
 	 */
 	public myWindow() {
 		super(null);
 		createActions();
 		addToolBar(SWT.FLAT | SWT.WRAP);
 		addMenuBar();
 		addStatusLine();
 	}
 
 	/**
 	 * Create contents of the application window.
 	 * @param parent
 	 */
 	@Override
 	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.V_SCROLL);
 		container.setLayout(new FillLayout(SWT.HORIZONTAL));
 		{
			TextViewer textViewer = new TextViewer(container, SWT.BORDER);
 			textViewer.setEditable(false);
 			styledText = textViewer.getTextWidget();
 		}
 
 		return container;
 	}
 
 	/**
 	 * Create the actions.
 	 */
 	private void createActions() {
 		// Create the actions
 		{
 			newRun = new Action("Create new Run") {
 				@Override
 				public void run() {
 					Printer.setInstance(new TextViewerPrinter(styledText));
 					Main.main(null);
 					super.run();
 				}
 			};
 		}
 	}
 
 	/**
 	 * Create the menu manager.
 	 * @return the menu manager
 	 */
 	@Override
 	protected MenuManager createMenuManager() {
 		MenuManager menuManager = new MenuManager("menu");
 		{
 			MenuManager menuManager_1 = new MenuManager("File");
 			menuManager.add(menuManager_1);
 			menuManager_1.add(newRun);
 		}
 		return menuManager;
 	}
 
 	/**
 	 * Create the toolbar manager.
 	 * @return the toolbar manager
 	 */
 	@Override
 	protected ToolBarManager createToolBarManager(int style) {
 		ToolBarManager toolBarManager = new ToolBarManager(style);
 		toolBarManager.add(newRun);
 		return toolBarManager;
 	}
 
 	/**
 	 * Create the status line manager.
 	 * @return the status line manager
 	 */
 	@Override
 	protected StatusLineManager createStatusLineManager() {
 		StatusLineManager statusLineManager = new StatusLineManager();
 		return statusLineManager;
 	}
 
 	/**
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String args[]) {
 		try {
 			myWindow window = new myWindow();
 			window.setBlockOnOpen(true);
 			window.open();
 			Display.getCurrent().dispose();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Configure the shell.
 	 * @param newShell
 	 */
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText("WeitoGUI");
 	}
 
 	/**
 	 * Return the initial size of the window.
 	 */
 	@Override
 	protected Point getInitialSize() {
 		return new Point(450, 300);
 	}
 
 }
