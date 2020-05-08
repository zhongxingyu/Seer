 /*******************************************************************************
  * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet)
  * and others. All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.ecommons.ui.mpbv;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler2;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.action.ContributionItem;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.CloseWindowListener;
 import org.eclipse.swt.browser.LocationEvent;
 import org.eclipse.swt.browser.LocationListener;
 import org.eclipse.swt.browser.OpenWindowListener;
 import org.eclipse.swt.browser.ProgressEvent;
 import org.eclipse.swt.browser.ProgressListener;
 import org.eclipse.swt.browser.StatusTextEvent;
 import org.eclipse.swt.browser.StatusTextListener;
 import org.eclipse.swt.browser.TitleEvent;
 import org.eclipse.swt.browser.TitleListener;
 import org.eclipse.swt.browser.WindowEvent;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.IWorkbenchCommandConstants;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 import org.eclipse.ui.part.Page;
 import org.eclipse.ui.services.IServiceLocator;
 
 import de.walware.ecommons.ui.SharedMessages;
 import de.walware.ecommons.ui.SharedUIResources;
 import de.walware.ecommons.ui.actions.HandlerCollection;
 import de.walware.ecommons.ui.actions.HandlerContributionItem;
 import de.walware.ecommons.ui.actions.SearchContributionItem;
 import de.walware.ecommons.ui.actions.SimpleContributionItem;
 import de.walware.ecommons.ui.util.LayoutUtil;
 import de.walware.ecommons.ui.util.UIAccess;
 
 
 public class PageBookBrowserPage extends Page implements ProgressListener,
 		LocationListener, TitleListener, StatusTextListener, OpenWindowListener, CloseWindowListener {
 	
 	
 	protected static void appendEscapedJavascriptString(final StringBuilder sb, final String s) {
 		for (int i = 0; i < s.length(); i++) {
 			final char c = s.charAt(i);
 			switch (c) {
 			case '\\':
 			case '\"':
 			case '\'':
 				sb.append('\\');
 				sb.append(c);
 				continue;
 			default:
 				sb.append(c);
 				continue;
 			}
 		}
 	}
 	
 	
 	protected class SearchBar implements DisposeListener {
 		
 		
 		private ToolBarManager fSearchBarManager;
 		private ToolBar fSearchBar;
 		private SearchContributionItem fSearchTextItem;
 		
 		private boolean fSearchCaseSensitive;
 		
 		
 		public SearchBar(final Composite parent) {
 			create(parent);
 		}
 		
 		
 		private void create(final Composite parent) {
 			fSearchBarManager = new ToolBarManager(SWT.FLAT);
 			fSearchBar = fSearchBarManager.createControl(parent);
 			fSearchBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 			fSearchBar.addDisposeListener(this);
 			
 			fSearchBarManager.add(new ContributionItem() {
 				@Override
 				public void fill(final ToolBar parent, final int index) {
 					final ToolItem item = new ToolItem(parent, SWT.PUSH);
 					item.setImage(SharedUIResources.getImages().get(SharedUIResources.LOCTOOL_CLOSETRAY_IMAGE_ID));
 					item.setHotImage(SharedUIResources.getImages().get(SharedUIResources.LOCTOOL_CLOSETRAY_H_IMAGE_ID));
 					item.setToolTipText("Close Search");
 					item.addSelectionListener(new SelectionAdapter() {
 						@Override
 						public void widgetSelected(final SelectionEvent e) {
 							hide();
 						}
 					});
 				}
 			});
 			
 			fSearchBarManager.add(new Separator());
 			
 			fSearchTextItem = new SearchContributionItem("search.text", false) { //$NON-NLS-1$
 				@Override
 				public void fill(final ToolBar parent, final int index) {
 					super.fill(parent, index);
 					getSearchText().getTextControl().addKeyListener(new KeyAdapter() {
 						@Override
 						public void keyPressed(final KeyEvent e) {
 							if (e.keyCode == SWT.ESC && e.doit) {
 								hide();
 								e.doit = false;
 								return;
 							}
 						}
 					});
 				}
 				@Override
 				protected void search() {
 					PageBookBrowserPage.this.search(true);
 				}
 			};
 			fSearchTextItem.setToolTip("Find Text");
 			fSearchTextItem.setSizeControl(parent);
 			fSearchBarManager.add(fSearchTextItem);
 			
 			final ImageRegistry ecommonsImages = SharedUIResources.getImages();
 			final HandlerCollection pageHandlers = fPageHandlers;
 			fSearchBarManager.add(new HandlerContributionItem(new CommandContributionItemParameter(
 					getSite(), "search.next", SharedUIResources.FIND_NEXT_COMMAND_ID, null, //$NON-NLS-1$
 					ecommonsImages.getDescriptor(SharedUIResources.LOCTOOL_DOWN_IMAGE_ID), null, ecommonsImages.getDescriptor(SharedUIResources.LOCTOOL_DOWN_H_IMAGE_ID),
 					SharedMessages.FindNext_tooltip, null, null, SWT.PUSH, null, false), pageHandlers.get(SharedUIResources.FIND_NEXT_COMMAND_ID)));
 			fSearchBarManager.add(new HandlerContributionItem(new CommandContributionItemParameter(
 					getSite(), "search.previous", SharedUIResources.FIND_PREVIOUS_COMMAND_ID, null, //$NON-NLS-1$
 					ecommonsImages.getDescriptor(SharedUIResources.LOCTOOL_UP_IMAGE_ID), null, ecommonsImages.getDescriptor(SharedUIResources.LOCTOOL_UP_H_IMAGE_ID),
 					SharedMessages.FindPrevious_tooltip, null, null, SWT.PUSH, null, false), pageHandlers.get(SharedUIResources.FIND_PREVIOUS_COMMAND_ID)));
 			
 			fSearchBarManager.add(new Separator());
 			
 			final SimpleContributionItem caseItem = new SimpleContributionItem(new CommandContributionItemParameter(null, null, null, null,
 					ecommonsImages.getDescriptor(SharedUIResources.LOCTOOL_CASESENSITIVE_IMAGE_ID), null, null,
 					null, null, "Case Sensitive", SimpleContributionItem.STYLE_CHECK, null, false)) {
 				@Override
 				protected void execute() throws ExecutionException {
 					fSearchCaseSensitive = !fSearchCaseSensitive;
 					setChecked(fSearchCaseSensitive);
 				}
 			};
 			caseItem.setChecked(fSearchCaseSensitive);
 			fSearchBarManager.add(caseItem);
 			
 			fSearchBarManager.update(true);
 			fSearchTextItem.resize();
 		}
 		
 		public void widgetDisposed(final DisposeEvent e) {
 			if (fSearchBar != null) {
 				fSearchBarManager.dispose();
 				fSearchBarManager = null;
 				
 				fSearchBar = null;
 			}
 		}
 		
 		
 		public void show() {
 			final GridData gd = (GridData) fSearchBar.getLayoutData();
 			gd.exclude = false;
 			fSearchBar.getParent().layout(true, true);
 			fSearchTextItem.getSearchText().setFocus();
 		}
 		
 		public void hide() {
 			setFocusToBrowser();
 			final GridData gd = (GridData) fSearchBar.getLayoutData();
 			gd.exclude = true;
 			fSearchBar.getParent().layout(new Control[] { fSearchBar });
 		}
 		
 		public String getText() {
 			return fSearchTextItem.getText();
 		}
 		
 		public boolean isCaseSensitiveEnabled() {
 			return fSearchCaseSensitive;
 		}
 		
 	}
 	
 	
 	private final PageBookBrowserView fView;
 	
 	private final BrowserSession fSession;
 	
 	private Composite fComposite;
 	
 	private Browser fBrowser;
 	
 	private SearchBar fSearchBar;
 	
 	private final HandlerCollection fPageHandlers = new HandlerCollection();
 	
 	private String fBrowserStatusText;
 	
 	private int fProgressTotal;
 	private int fProgressWorked;
 	
 	
 	public PageBookBrowserPage(final PageBookBrowserView view, final BrowserSession session) {
 		fView = view;
 		fSession = session;
 	}
 	
 	
 	@Override
 	public void createControl(final Composite parent) {
 		fComposite = new Composite(parent, SWT.NONE);
 		fComposite.setLayout(LayoutUtil.applySashDefaults(new GridLayout(), 1));
 		
 		{	final Control control = createAddressBar(fComposite);
 			if (control != null) {
 				control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 			}
 		}
 		final Control browser = createBrowser(fComposite);
 		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		initActions(getSite(), fPageHandlers);
 		
		// check required for open in new window/page
		if (fSession.fUrl != null && fSession.fUrl.length() > 0) {
			setUrl(fSession.fUrl);
		}
 		setFocus();
 	}
 	
 	private Control createBrowser(final Composite parent) {
 		fBrowser = new Browser(parent, SWT.NONE);
 		
 		fBrowser.addProgressListener(this);
 		fBrowser.addLocationListener(this);
 		fBrowser.addTitleListener(this);
 		fBrowser.addStatusTextListener(this);
 		fBrowser.addOpenWindowListener(this);
 		fBrowser.addCloseWindowListener(this);
 		
 		return fBrowser;
 	}
 	
 	protected Control createAddressBar(final Composite parent) {
 		return null;
 	}
 	
 	protected void initActions(final IServiceLocator serviceLocator, final HandlerCollection handlers) {
 		final IHandlerService handlerService = (IHandlerService) serviceLocator.getService(IHandlerService.class);
 		
 		final String browserType = fBrowser.getBrowserType();
 		if (browserType.equals("mozilla") || browserType.equals("webkit")) { //$NON-NLS-1$ //$NON-NLS-2$
 			{	final IHandler2 handler = new AbstractHandler() {
 					public Object execute(final ExecutionEvent event) throws ExecutionException {
 						if (!UIAccess.isOkToUse(fBrowser)) {
 							return null;
 						}
 						if (fSearchBar == null) {
 							fSearchBar = new SearchBar(fComposite);
 						}
 						fSearchBar.show();
 						return null;
 					}
 				};
 				handlers.add(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE, handler);
 				handlerService.activateHandler(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE, handler);
 			}
 			{	final IHandler2 handler = new AbstractHandler() {
 					public Object execute(final ExecutionEvent arg0) {
 						PageBookBrowserPage.this.search(true);
 						return null;
 					}
 				};
 				handlers.add(SharedUIResources.FIND_NEXT_COMMAND_ID, handler);
 				handlerService.activateHandler("org.eclipse.ui.navigate.next", handler); //$NON-NLS-1$
 			}
 			{	final IHandler2 handler = new AbstractHandler() {
 					public Object execute(final ExecutionEvent arg0) {
 						PageBookBrowserPage.this.search(false);
 						return null;
 					}
 				};
 				handlers.add(SharedUIResources.FIND_PREVIOUS_COMMAND_ID, handler);
 				handlerService.activateHandler("org.eclipse.ui.navigate.previous", handler); //$NON-NLS-1$
 			}
 		}
 	}
 	
 	private void search(final boolean forward) {
 		if (fSearchBar == null || !UIAccess.isOkToUse(fBrowser)) {
 			return;
 		}
 		final String text = fSearchBar.getText();
 		if (text == null || text.length() == 0) {
 			return;
 		}
 		final boolean caseSensitive = fSearchBar.isCaseSensitiveEnabled();
 		
 		final String message;
 		if (doSearch(text, forward, caseSensitive, false)) {
 			message = null;
 		}
 		else if (doSearch(text, forward, caseSensitive, true)) {
 			message = forward ? "Search continued from top" : "Search continued from bottom";
 		}
 		else {
 			Display.getCurrent().beep();
 			message = "Search text not found";
 		}
 		
 		if (fView.getCurrentBrowserPage() == this) {
 			fView.setTemporaryStatus(IStatus.INFO, message);
 		}
 	}
 	
 	private boolean doSearch(final String text, final boolean forward, final boolean caseSensitive, final boolean wrap) {
 		final StringBuilder script = new StringBuilder(50);
 		script.append("return window.find(\""); //$NON-NLS-1$
 		appendEscapedJavascriptString(script, text);
 		script.append("\","); //$NON-NLS-1$
 		script.append(caseSensitive);
 		script.append(',');
 		script.append(!forward); // upward
 		script.append(',');
 		script.append(wrap); // wrap
 		script.append(",false,true)"); // wholeWord, inFrames //$NON-NLS-1$
 						// inFrames fixes wrap in some situations
 		final Object found = fBrowser.evaluate(script.toString());
 		return Boolean.TRUE.equals(found);
 	}
 	
 	
 	protected Browser getBrowser() {
 		return fBrowser;
 	}
 	
 	BrowserSession getSession() {
 		return fSession;
 	}
 	
 	@Override
 	public Control getControl() {
 		return fComposite;
 	}
 	
 	@Override
 	public void setFocus() {
 		fBrowser.setFocus();
 	}
 	
 	public boolean isBrowserFocusControl() {
 		return (UIAccess.isOkToUse(fBrowser) && fBrowser.isFocusControl());
 	}
 	
 	public void setFocusToBrowser() {
 		fBrowser.setFocus();
 	}
 	
 	
 	public void setUrl(String url) {
 		if (fBrowser == null) {
 			return;
 		}
 		if (url == null || url.length() == 0) {
 			url = "about:blank"; //$NON-NLS-1$
 		}
 		if (url.startsWith("html:///")) { //$NON-NLS-1$
 			final int id = fSession.putStatic(url.substring(8));
 			url = "estatic:///" + id; //$NON-NLS-1$
 		}
 		fBrowser.setUrl(url);
 	}
 	
 	public String getCurrentTitle() {
 		final String title = fSession.fTitle;
 		return (title != null) ? title : ""; //$NON-NLS-1$
 	}
 	
 	public String getCurrentUrl() {
 		return fSession.fUrl;
 	}
 	
 	public String getCurrentStatusText() {
 		return fBrowserStatusText;
 	}
 	
 	int getCurrentProgressTotal() {
 		return fProgressTotal;
 	}
 	
 	int getCurrentProgressWorked() {
 		return fProgressWorked;
 	}
 	
 	
 	public void changed(final ProgressEvent event) {
 		if (event.total == 0) {
 			fProgressTotal = 0;
 			fProgressWorked = 0;
 		}
 		else {
 			fProgressTotal = event.total;
 			fProgressWorked = event.current;
 		}
 	}
 	
 	public void changing(final LocationEvent event) {
 		if (event.top) {
 			fSession.fImageDescriptor = null;
 		}
 		if (event.location.startsWith("estatic:///")) { //$NON-NLS-1$
 			event.doit = false;
 			try {
 				final String html = fSession.getStatic(Integer.parseInt(event.location.substring(11)));
 				if (html != null) {
 					fBrowser.setText(html);
 				}
 			}
 			catch (final Exception e) {
 			}
 			return;
 		}
 		if (event.location.startsWith("esystem://")) { //$NON-NLS-1$
 			final String file = event.location.substring(10);
 			event.doit = false;
 			if (file.length() > 0) {
 				UIAccess.getDisplay().asyncExec(new Runnable() {
 					public void run() {
 						Program.launch(file);
 					}
 				});
 			}
 			return;
 		}
 		if (event.location.startsWith("about:")) { //$NON-NLS-1$
 			if (!event.location.equals("about:blank")) { //$NON-NLS-1$
 				event.doit = false;
 			}
 			return;
 		}
 		if (event.location.startsWith("res:")) { //$NON-NLS-1$
 			event.doit = false;
 			return;
 		}
 	}
 	
 	public void changed(final LocationEvent event) {
 		if (!event.top) {
 			return;
 		}
 		String location = event.location;
 		if ("about:blank".equals(location)) { //$NON-NLS-1$
 			location = ""; //$NON-NLS-1$
 		}
 		fSession.fUrl = location;
 	}
 	
 	public void completed(final ProgressEvent event) {
 		fProgressTotal = 0;
 		fProgressWorked = 0;
 	}
 	
 	public void changed(final TitleEvent event) {
 		String title = event.title;
 		if (title == null) {
 			title = ""; //$NON-NLS-1$
 		}
 		else if (title.startsWith("http://")) { //$NON-NLS-1$
 			final int idx = title.lastIndexOf('/');
 			if (idx >= 0) {
 				title = title.substring(idx+1);
 			}
 		}
 		fSession.fTitle = title;
 	}
 	
 	public void changed(final StatusTextEvent event) {
 		fBrowserStatusText = event.text;
 	}
 	
 	protected void setIcon(final ImageDescriptor imageDescriptor) {
 		fSession.fImageDescriptor = imageDescriptor;
 	}
 	
 	
 	public void open(final WindowEvent event) {
 		final PageBookBrowserPage page = (PageBookBrowserPage) fView.newPage(new BrowserSession(), true);
 		if (page != null) {
 			event.browser = page.fBrowser;
 		}
 	}
 	
 	public void close(final WindowEvent event) {
 		fView.closePage(fSession);
 	}
 	
 	public String getSelection() {
 		final Object value = getBrowser().evaluate(
 				"if (window.getSelection) {" + //$NON-NLS-1$
 					"var sel = window.getSelection();" + //$NON-NLS-1$
 					"if (sel.getRangeAt) {" + //$NON-NLS-1$
 						"return sel.getRangeAt(0).toString();" + //$NON-NLS-1$
 					"}" + //$NON-NLS-1$
 					"return sel;" + //$NON-NLS-1$
 				"}" + //$NON-NLS-1$
 				"else if (document.getSelection) {" + //$NON-NLS-1$
 					"return document.getSelection();" + //$NON-NLS-1$
 				"}" + //$NON-NLS-1$
 				"else if (document.selection) {" + //$NON-NLS-1$
 					"return document.selection.createRange().text;" + //$NON-NLS-1$
 				"}" + //$NON-NLS-1$
 				"else {" + //$NON-NLS-1$
 					"return '';" + //$NON-NLS-1$
 				"}"); //$NON-NLS-1$
 		if (value instanceof String) {
 			return (String) value;
 		}
 		return null;
 	}
 	
 }
