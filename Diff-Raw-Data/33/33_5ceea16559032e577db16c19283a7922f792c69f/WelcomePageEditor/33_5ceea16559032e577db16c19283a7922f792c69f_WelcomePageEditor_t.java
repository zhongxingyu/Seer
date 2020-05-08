 package org.zend.php.common.welcome;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.CloseWindowListener;
 import org.eclipse.swt.browser.OpenWindowListener;
 import org.eclipse.swt.browser.StatusTextEvent;
 import org.eclipse.swt.browser.StatusTextListener;
 import org.eclipse.swt.browser.VisibilityWindowListener;
 import org.eclipse.swt.browser.WindowEvent;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.WorkbenchPage;
 import org.eclipse.ui.internal.browser.WebBrowserEditor;
 import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
 import org.osgi.service.prefs.BackingStoreException;
 import org.zend.php.common.Activator;
 import org.zend.php.common.ZendCatalogViewer;
 
 public class WelcomePageEditor extends WebBrowserEditor {
 
 	private static final String OUTLINE_VIEW = "org.eclipse.ui.views.ContentOutline";
 	
 	private static final String VIEWS_TO_REOPEN = "viewsToReopen";
 
 	public static final String EDITOR_ID = "org.zend.customization.welcome.welcomePageEditor"; //$NON-NLS-1$
 
 	private ZendCatalogViewer viewer;
 
 	public WelcomePageEditor() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		setSite(site);
 		setInput(input);
 	}
 
 	public boolean isDirty() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void createPartControl(Composite parent) {
 		FillLayout fl = (FillLayout) parent.getLayout();
 		fl.marginHeight = 0;
 		fl.marginWidth = 0;
 		fl.spacing = 0;
 		parent.setLayout(fl);
 		final Composite editor = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(3, true);
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		editor.setLayout(layout);
 		editor.setBackgroundMode(SWT.INHERIT_FORCE);
 		Color c = new Color(Display.getDefault(), 95, 166, 48);
 		editor.setBackground(c);
 		createWelcomePageContent(editor);
 		createFeatureManager(editor);
 
 		if (getEditorInput() instanceof WelcomePageEditorInput) {
 			WelcomePageEditorInput welcomeInput = (WelcomePageEditorInput) getEditorInput();
 			welcomeInput.initFeaturesViewer(viewer);
 		}
 
 		Display.getCurrent().asyncExec(new Runnable() {
 			public void run() {
 				if (!editor.isDisposed()) {
 					viewer.updateCatalog();
 				}
 			}
 		});
 		// PlatformUI.getWorkbench().getHelpSystem()
 		// .setHelp(parent, IStudioHelpContextIds.WELCOME_PAGE);
 	}
 
 	private void createWelcomePageContent(Composite parent) {
 		Composite composite = new Composite(parent, SWT.TRANSPARENT);
 		GridLayout layout = new GridLayout(1, true);
 		composite.setLayout(layout);
 		GridData td = new GridData(SWT.FILL, SWT.FILL, true, true);
 		td.horizontalSpan = 2;
 		composite.setLayoutData(td);
 		composite.setBackgroundMode(SWT.INHERIT_FORCE);
 
 		try {
 			Browser br = new Browser(composite, SWT.NONE);
 			br.setLayoutData(new GridData(GridData.FILL_BOTH));
 			br.setUrl(((WebBrowserEditorInput) getEditorInput()).getURL()
 					.toString());
 
 			final WelcomeLinkListenerManager m = new WelcomeLinkListenerManager();
 			br.addStatusTextListener(new StatusTextListener() {
 
 				public void changed(StatusTextEvent event) {
 					if (event.text != null) {
 						final Runnable runnable = m.getRunnable(event.text);
 
 						if (runnable != null) {
 							Display.getDefault().asyncExec(runnable);
 						}
 					}
 				}
 			});
 			initialize(br.getDisplay(), br);
 		} catch (Throwable ex) {
 			composite.dispose();
 
 			// recreate composite, because it may contain some leftover after
 			// browser
 			composite = new Composite(parent, SWT.TRANSPARENT);
 			layout = new GridLayout(1, true);
 			composite.setLayout(layout);
 			td = new GridData(SWT.FILL, SWT.FILL, true, true);
 			td.horizontalSpan = 2;
 			composite.setLayoutData(td);
 			composite.setBackgroundMode(SWT.INHERIT_FORCE);
 			showStaticWelcomeImage(composite);
 			Activator.log(ex);
 		}
 		
 		Composite editorComp = parent;
 		while(!(editorComp instanceof CTabFolder)&&editorComp!=null){
 			editorComp = editorComp.getParent();
 		}
 		if (editorComp instanceof CTabFolder) {
 			if (editorComp.getSize().x<800) {
 				hideRightSideViews();
 			}
		}else if(editorComp==null){
			if (parent.getParent().getParent().getParent().getSize().x<800) {//FIXME This is hard coded
				hideRightSideViews();
			}
 		}
 	}
 
 	/* register WindowEvent listeners */
 	static void initialize(final Display display, Browser browser) {
 		browser.addOpenWindowListener(new OpenWindowListener() {
 			public void open(WindowEvent event) {
 				if (!event.required) return;	/* only do it if necessary */
 				Shell shell = new Shell(display);
 				shell.setText("Welcome");
 				shell.setLayout(new FillLayout());
 				Browser browser = new Browser(shell, SWT.NONE);
 				initialize(display, browser);
 				event.browser = browser;
 			}
 		});
 		browser.addVisibilityWindowListener(new VisibilityWindowListener() {
 			public void hide(WindowEvent event) {
 				Browser browser = (Browser)event.widget;
 				Shell shell = browser.getShell();
 				shell.setVisible(false);
 			}
 			public void show(WindowEvent event) {
 				Browser browser = (Browser)event.widget;
 				final Shell shell = browser.getShell();
 				if (event.location != null) shell.setLocation(event.location);
 				if (event.size != null) {
 					Point size = event.size;
 					shell.setSize(shell.computeSize(size.x, size.y));
 				}
 				shell.open();
 			}
 		});
 		browser.addCloseWindowListener(new CloseWindowListener() {
 			public void close(WindowEvent event) {
 				Browser browser = (Browser)event.widget;
 				Shell shell = browser.getShell();
 				shell.close();
 			}
 		});
 	}
 	
 	private void showStaticWelcomeImage(Composite composite) {
 		Image img = Activator.getDefault().getImageRegistry()
 				.get(Activator.PDT_STATIC_WELCOME);
 
 		ScrolledComposite sc = new ScrolledComposite(composite, SWT.NONE);
 		sc.setLayoutData(new GridData(GridData.FILL_BOTH));
 		sc.setLayout(new GridLayout());
 
 		final Label label = new Label(sc, SWT.NONE);
 		label.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true));
 		label.setImage(img);
 		label.setSize(img.getImageData().width, img.getImageData().height);
 
 		final Cursor cursor = composite.getDisplay().getSystemCursor(
 				SWT.CURSOR_HAND);
 		label.setCursor(cursor);
 
 		label.addListener(SWT.MouseUp, new Listener() {
 			public void handleEvent(Event e) {
 				Program.launch("http://www.zend.com/en/products/studio/features");
 			}
 		});
 	}
 
 	private void createFeatureManager(Composite parent) {
 		IRunnableContext context = new ProgressMonitorDialog(parent.getShell());
 		IShellProvider provider = new IShellProvider() {
 			public Shell getShell() {
 				return Activator.getDefault().getWorkbench()
 						.getActiveWorkbenchWindow().getShell();
 			}
 		};
 		final Composite viewerComposite = new Composite(parent, SWT.TRANSPARENT);
 		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gd.horizontalSpan = 1;
 		viewerComposite.setLayoutData(gd);
 		GridLayout gdL = new GridLayout(1, true);
 		gdL.verticalSpacing = 0;
 		gdL.horizontalSpacing = 0;
 		viewerComposite.setLayout(gdL);
 		viewer = new ZendCatalogViewer(provider, context);
 		viewer.createControl(viewerComposite);
 		// PlatformUI
 		// .getWorkbench()
 		// .getHelpSystem()
 		// .setHelp(
 		// viewerComposite,
 		// IStudioHelpContextIds.CUSTOMIZING_ZEND_STUDIO_USING_THE_WELCOME_PAGE);
 	}
 
 	public void setFocus() {
 
 	}
 
 	public void dispose() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				reopenRightSideViews();
			}
		});
//		Display.getDefault().asyncExec(new Runnable() {//change syncExec to asyncExec,or there is NPE
//				public void run() {
//					reopenRightSideViews();
//				}
//			});
 		super.dispose();
 	}
 
 	private void hideRightSideViews() {
 		IWorkbench wb = null;
 		try {
 			wb = PlatformUI.getWorkbench();
 		} catch (IllegalArgumentException ex) {
 			return;
 		}
 
 		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
 		if (window == null) {
 			return;
 		}
 		IWorkbenchPage page = window.getActivePage();
 		if (page == null) {
 			return;
 		}
 
 		
 		IViewReference outlineView = page
 				.findViewReference(OUTLINE_VIEW);

		List<String> viewIds = new ArrayList<String>();
 		if (outlineView != null) {
 			page.hideView(outlineView);
			viewIds.add(OUTLINE_VIEW);
		}
		if (viewIds.size() > 0) {
			saveState(viewIds);
 		}
 	}
 
 	protected void reopenOutlineView() {
 		IWorkbenchWindow window = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow();
 		if (window == null) {
 			return;
 		}
 		IWorkbenchPage page = window.getActivePage();
 		if (page == null) {
 			return;
 		}

 		try {
 			page.showView(OUTLINE_VIEW);
 		} catch (PartInitException e) {
 			Activator.log(e);
 		}
 	}
 	protected void reopenRightSideViews() {
 		IWorkbenchWindow window = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow();
 		if (window == null) {
 			return;
 		}
 		IWorkbenchPage page = window.getActivePage();
 		if (page == null) {
 			return;
 		}
 
 		List<String> viewIds = restoreState();
 		for (String viewId : viewIds) {
 			try {
 				page.showView(viewId, null, viewId.equals(OUTLINE_VIEW) ? IWorkbenchPage.VIEW_VISIBLE : IWorkbenchPage.VIEW_CREATE);
 			} catch (Exception e) {
 				// ignore
 			}
 		}
 
 	}
 
 	public void saveState(List<String> viewIds) {
 		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
 		
 		StringBuilder sb = new StringBuilder();
 		for (String viewId : viewIds) {
 			sb.append(viewId).append(",");
 		}
 		
 		preferences.put(VIEWS_TO_REOPEN, sb.toString());
 		try {
 			preferences.flush();
 		} catch (BackingStoreException e) {
 			// ignore
 		}
 	}
 
 	public List<String> restoreState() {
 		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
 		
 		List<String> viewIds = new ArrayList<String>();
 		String viewsToReopen = preferences.get(VIEWS_TO_REOPEN, "");
 		if ((viewsToReopen != null) && (viewsToReopen.length() > 0)) {
 			StringTokenizer st = new StringTokenizer(viewsToReopen, ",");
 			while (st.hasMoreTokens()) {
 				String viewName = st.nextToken();
 				if (viewName.length() > 0) {
 					viewIds.add(viewName);
 				}
 			}
 		}
 		
 		return viewIds;
 	}
 
 }
