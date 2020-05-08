 package com.bkahlert.devel.nebula.widgets.editor;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.BrowserFunction;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Widget;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import com.bkahlert.devel.nebula.utils.ExecutorUtil;
 import com.bkahlert.devel.nebula.widgets.browser.Anker;
 import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
 import com.bkahlert.devel.nebula.widgets.browser.IAnker;
 import com.bkahlert.devel.nebula.widgets.browser.IJavaScriptExceptionListener;
 import com.bkahlert.devel.nebula.widgets.browser.JavaScriptException;
 import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;
 
 /**
  * This is a wrapped CKEditor (ckeditor.com).
  * <p>
  * <b>Important developer warning</b>: Do not try to wrap a WYSIWYG editor based
  * on iframes like TinyMCE. Some internal browsers (verified with Webkit) do not
  * handle cut, copy and paste actions when the iframe is in focus. CKEditor can
  * operate in both modes - iframes and divs (and p tags).
  * 
  * @author bkahlert
  * 
  */
 public class Editor extends BrowserComposite {
 
 	private static final Logger LOGGER = Logger.getLogger(Editor.class);
 	private static final Pattern URL_PATTERN = Pattern
 			.compile("(.*?)(\\w+://[!#$&-;=?-\\[\\]_a-zA-Z~%]+)(.*?)");
 
 	private List<IAnkerLabelProvider> ankerLabelProviders = new ArrayList<IAnkerLabelProvider>();
 	private List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();
 	private String oldHtml = "";
 	private Timer delayChangeTimer = null;
 
 	public Editor(Composite parent, int style) {
 		this(parent, style, 0);
 	}
 
 	/**
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param delayChangeEventUpTo
 	 *            is the delay that must have been passed in order to fire a
 	 *            change event. If 0 no delay will be applied. The minimal delay
 	 *            is defined by the CKEditor's config.js.
 	 */
 	public Editor(Composite parent, int style, final long delayChangeEventUpTo) {
 		super(parent, style);
 		this.deactivateNativeMenu();
 
 		addJavaScriptExceptionListener(new IJavaScriptExceptionListener() {
 			@Override
 			public boolean thrown(JavaScriptException e) {
 				LOGGER.error("Internal " + Editor.class.getSimpleName()
 						+ " error", e);
 				return true;
 			}
 		});
 
 		fixShortcuts(delayChangeEventUpTo);
 		listenForModifications(delayChangeEventUpTo);
 	}
 
 	public void fixShortcuts(final long delayChangeEventUpTo) {
 		this.getBrowser().addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if ((e.stateMask & SWT.CTRL) != 0
 						|| (e.stateMask & SWT.COMMAND) != 0) {
 					if (e.keyCode == 97) {
 						// a - select all
 						selectAll();
 					}
 
 					/*
 					 * The CKEditor plugin "onchange" does not notify on cut and
 					 * paste operations. Therefore we need to handle them here.
 					 */
 					if (e.keyCode == 120) {
 						// x - cut
 						// wait for the ui thread to apply the operation
 						ExecutorUtil.asyncExec(new Runnable() {
 							public void run() {
 								modifiedCallback(getSource(),
 										delayChangeEventUpTo);
 							}
 						});
 					}
 					if (e.keyCode == 99) {
 						// c - copy
 						// do nothing
 					}
 					if (e.keyCode == 118) {
 						// v - paste
 						// wait for the ui thread to apply the operation
 						ExecutorUtil.asyncExec(new Runnable() {
 							public void run() {
 								modifiedCallback(getSource(),
 										delayChangeEventUpTo);
 							}
 						});
 					}
 				}
 
 			}
 		});
 	}
 
 	public void listenForModifications(final long delayChangeEventUpTo) {
 		new BrowserFunction(getBrowser(), "modified") {
 			@Override
 			public Object function(Object[] arguments) {
 				if (arguments.length >= 1) {
 					if (arguments[0] instanceof String) {
 						String newHtml = (String) arguments[0];
 						modifiedCallback(newHtml, delayChangeEventUpTo);
 					}
 				}
 				return null;
 			}
 		};
 
 		this.getBrowser().addDisposeListener(new DisposeListener() {
 			@Override
 			public void widgetDisposed(DisposeEvent e) {
 				modifiedCallback(getSource(), 0);
 			}
 		});
 	}
 
 	protected void modifiedCallback(String html, long delayChangeEventTo) {
 		String newHtml = (html == null || html.replace("&nbsp;", " ").trim()
 				.isEmpty()) ? "" : html.trim();
 		if (oldHtml.equals(newHtml))
 			return;
 
 		// When space entered but widget is not disposing, create links
 		if (delayChangeEventTo > 0) {
 			String prevCaretCharacter = getPrevCaretCharacter();
 			if (prevCaretCharacter != null
 					&& getPrevCaretCharacter().matches("[\\s| ]")) { // space is
 																		// non
 																		// breaking
 																		// space
 				String autoLinkedHtml = createLinks(newHtml);
 				if (!autoLinkedHtml.equals(newHtml)) {
 					this.setSource(autoLinkedHtml, true);
 					newHtml = autoLinkedHtml;
 				}
 			}
 		}
 
 		final String tmp = newHtml;
 		final Runnable fireRunnable = new Runnable() {
 			@Override
 			public void run() {
 				Event event = new Event();
 				event.display = Display.getCurrent();
 				event.time = (int) (new Date().getTime() & 0xFFFFFFFFL);
 				event.widget = (Widget) Editor.this;
 				event.text = tmp;
 				event.data = tmp;
 				ModifyEvent modifyEvent = new ModifyEvent(event);
 				for (ModifyListener modifyListener : modifyListeners) {
 					modifyListener.modifyText(modifyEvent);
 				}
 			}
 		};
 
 		oldHtml = tmp;
 
 		if (delayChangeTimer != null)
 			delayChangeTimer.cancel();
 		if (delayChangeEventTo > 0) {
 			delayChangeTimer = new Timer();
 			delayChangeTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					fireRunnable.run();
 				}
 			}, delayChangeEventTo);
 		} else {
 			delayChangeTimer = null;
 			fireRunnable.run();
 		}
 	}
 
 	private String createLinks(String html) {
 		boolean htmlChanged = false;
 		Document doc = Jsoup.parseBodyFragment(html);
 		for (Element e : doc.getAllElements()) {
 			if (e.tagName().equals("a")) {
 				IAnker anker = new Anker(e);
 				IAnker newAnker = createAnkerFromLabelProviders(anker);
 				if (newAnker == null
 						&& !anker.getHref().equals(anker.getContent())) {
 					newAnker = new Anker(anker.getContent(),
 							anker.getClasses(), anker.getContent());
 				}
 
 				if (newAnker != null) {
 					e.html(newAnker.toHtml());
 					htmlChanged = true;
 				}
 			} else {
 				String ownText = e.ownText();
 				Matcher matcher = URL_PATTERN.matcher(ownText);
 				if (matcher.matches()) {
 					String uri = matcher.group(2);
 
 					IAnker anker = new Anker(uri, new String[0], uri);
 					IAnker newAnker = createAnkerFromLabelProviders(anker);
 					if (newAnker == null) {
 						newAnker = anker;
 					}
 
 					String newHtml = e.html().replace(uri, newAnker.toHtml());
 					e.html(newHtml);
 					htmlChanged = true;
 				}
 			}
 		}
 		String newHtml = htmlChanged ? doc.body().children().toString() : html;
 		return newHtml;
 	}
 
 	public IAnker createAnkerFromLabelProviders(IAnker oldAnker) {
 		IAnker newAnker = null;
 		for (IAnkerLabelProvider ankerLabelProvider : ankerLabelProviders) {
 			if (ankerLabelProvider.isResponsible(oldAnker)) {
 				newAnker = new Anker(ankerLabelProvider.getHref(oldAnker),
 						ankerLabelProvider.getClasses(oldAnker),
 						ankerLabelProvider.getContent(oldAnker));
 				break;
 			}
 		}
 		return newAnker;
 	}
 
 	@Override
 	public String getStartUrl() {
 		try {
 			String editorUrlString = getFileUrl(Editor.class, "html/index.html");
 			return editorUrlString + "?internal=true";
 		} catch (IOException e) {
 			LOGGER.error("Could not open editor html", e);
 		}
 		return null;
 	}
 
 	/**
 	 * Checks whether the current editor contents present changes when compared
 	 * to the contents loaded into the editor at startup.
 	 * 
 	 * @return
 	 */
 	public Boolean isDirty() {
 		Boolean isDirty = (Boolean) this.getBrowser().evaluate(
 				"return com.bkahlert.devel.nebula.editor.isDirty();");
 		if (isDirty != null)
 			return isDirty;
 		else
 			return null;
 	}
 
 	public void selectAll() {
 		String js = "com.bkahlert.devel.nebula.editor.selectAll();";
 		this.enqueueJs(js);
 	}
 
 	public void setSource(String html) {
 		setSource(html, false);
 	}
 
 	public void setSource(String html, boolean restoreSelection) {
 		String js = "com.bkahlert.devel.nebula.editor.setSource("
 				+ TimelineJsonGenerator.enquote(html) + ", "
 				+ (restoreSelection ? "true" : "false") + ");";
 		this.enqueueJs(js);
 	}
 
 	public String getSource() {
 		if (!this.isLoadingCompleted())
 			return null;
 		String html = (String) this.getBrowser().evaluate(
 				"return com.bkahlert.devel.nebula.editor.getSource();");
		if (html != null)
			return StringEscapeUtils.unescapeHtml(html);
		else
			return null;
 	}
 
 	public void setMode(String mode) {
 		String js = "com.bkahlert.devel.nebula.editor.setMode(\"" + mode
 				+ "\");";
 		this.enqueueJs(js);
 	}
 
 	public void showSource() {
 		this.setMode("source");
 	}
 
 	public void hideSource() {
 		this.setMode("wysiwyg");
 	}
 
 	public String getPrevCaretCharacter() {
 		if (!this.isLoadingCompleted())
 			return null;
 		String html = (String) this
 				.getBrowser()
 				.evaluate(
 						"return com.bkahlert.devel.nebula.editor.getPrevCaretCharacter();");
 		return html;
 	}
 
 	public void saveSelection() {
 		this.enqueueJs("com.bkahlert.devel.nebula.editor.saveSelection();");
 	}
 
 	public void restoreSelection() {
 		this.enqueueJs("com.bkahlert.devel.nebula.editor.restoreSelection();");
 	}
 
 	@Override
 	public void setEnabled(boolean isEnabled) {
 		super.setEnabled(isEnabled);
 		String js = "com.bkahlert.devel.nebula.editor.setEnabled("
 				+ (isEnabled ? "true" : "false") + ");";
 		this.enqueueJs(js);
 	}
 
 	public void addAnkerLabelProvider(IAnkerLabelProvider ankerLabelProvider) {
 		this.ankerLabelProviders.add(ankerLabelProvider);
 	}
 
 	public void removeAnkerLabelProvider(IAnkerLabelProvider ankerLabelProvider) {
 		this.ankerLabelProviders.remove(ankerLabelProvider);
 	}
 
 	public void addModifyListener(ModifyListener modifyListener) {
 		this.modifyListeners.add(modifyListener);
 	}
 
 	public void removeModifyListener(ModifyListener modifyListener) {
 		this.modifyListeners.remove(modifyListener);
 	}
 
 }
