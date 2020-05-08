 package com.laboki.eclipse.plugin.fastopen.ui;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ILazyContentProvider;
 import org.eclipse.jface.viewers.StyledCellLabelProvider;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.TextStyle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PartInitException;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Lists;
 import com.google.common.eventbus.AllowConcurrentEvents;
 import com.google.common.eventbus.Subscribe;
 import com.laboki.eclipse.plugin.fastopen.context.FileUtil;
 import com.laboki.eclipse.plugin.fastopen.events.FilterFilesEvent;
 import com.laboki.eclipse.plugin.fastopen.events.FilteredFilesResultEvent;
 import com.laboki.eclipse.plugin.fastopen.events.RankedFilesEvent;
 import com.laboki.eclipse.plugin.fastopen.events.ShowFastOpenDialogEvent;
 import com.laboki.eclipse.plugin.fastopen.instance.EventBusInstance;
 import com.laboki.eclipse.plugin.fastopen.instance.Instance;
 import com.laboki.eclipse.plugin.fastopen.main.EditorContext;
 import com.laboki.eclipse.plugin.fastopen.main.EventBus;
 import com.laboki.eclipse.plugin.fastopen.task.AsyncTask;
 import com.laboki.eclipse.plugin.fastopen.task.TaskMutexRule;
 
 public final class Dialog extends EventBusInstance {
 
 	private static final TaskMutexRule RULE = new TaskMutexRule();
 	private static final int HEIGHT = 480;
 	private static final int WIDTH = Dialog.HEIGHT * 2;
 	private static final int SPACING_SIZE_IN_PIXELS = 10;
 	private static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE
 		| Pattern.CANON_EQ
 		| Pattern.UNICODE_CASE;
 	private static final Pattern TEXT_PATTERN = Pattern
 		.compile("\\p{Punct}*|\\w*| *", Dialog.PATTERN_FLAGS);
 	protected static final Shell SHELL = new Shell(
 		EditorContext.getShell(),
 		SWT.RESIZE | SWT.APPLICATION_MODAL);
 	protected static final Text TEXT = new Text(Dialog.SHELL, SWT.SEARCH
 		| SWT.ICON_CANCEL
 		| SWT.ICON_SEARCH
 		| SWT.NO_FOCUS);
 	protected static final TableViewer VIEWER = new TableViewer(
 		Dialog.SHELL,
 		SWT.VIRTUAL | SWT.BORDER | SWT.MULTI);
 	protected static final Table TABLE = Dialog.VIEWER.getTable();
 	protected final static Logger LOGGER = Logger.getLogger(Dialog.class
 		.getName());
 
 	public Dialog() {
 		Dialog.arrangeWidgets();
 		Dialog.setupDialog();
 		Dialog.setupText();
 		this.setupViewer();
 		this.addListeners();
 	}
 
 	private static void
 	arrangeWidgets() {
 		Dialog.setDialogLayout();
 		Dialog.setTextLayout();
 		Dialog.setViewerLayout();
 		Dialog.SHELL.pack();
 		Dialog.TABLE.pack();
 	}
 
 	private static void
 	setDialogLayout() {
 		final GridLayout layout = new GridLayout(1, true);
 		Dialog.spaceDialogLayout(layout);
 		Dialog.SHELL.setLayout(layout);
 		Dialog.SHELL.setLayoutData(Dialog.createFillGridData());
 	}
 
 	private static void
 	spaceDialogLayout(final GridLayout layout) {
 		layout.marginLeft = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.marginTop = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.marginRight = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.marginBottom = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.horizontalSpacing = Dialog.SPACING_SIZE_IN_PIXELS;
 		layout.verticalSpacing = Dialog.SPACING_SIZE_IN_PIXELS;
 	}
 
 	private static GridData
 	createFillGridData() {
 		return new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1);
 	}
 
 	private static void
 	setTextLayout() {
 		final GridData textGridData = new GridData();
 		textGridData.horizontalAlignment = GridData.FILL;
 		textGridData.grabExcessHorizontalSpace = true;
 		Dialog.TEXT.setLayoutData(textGridData);
 	}
 
 	private static void
 	setViewerLayout() {
 		Dialog.VIEWER.getTable().setLayoutData(Dialog.createFillGridData());
 	}
 
 	private static void
 	setupDialog() {
 		Dialog.SHELL.setTabList(Lists
 			.newArrayList(Dialog.VIEWER.getControl())
 			.toArray(new Control[1]));
 		Dialog.SHELL.setSize(Dialog.WIDTH, Dialog.HEIGHT);
 	}
 
 	private static void
 	setupText() {
 		Dialog.TEXT.setMessage("start typing to filter files...");
 	}
 
 	private void
 	setupViewer() {
 		this.setupTable();
 		Dialog.VIEWER.setContentProvider(this.new ContentProvider());
 		Dialog.VIEWER.setUseHashlookup(true);
 	}
 
 	private final class ContentProvider implements ILazyContentProvider {
 
 		private IFile[] files;
 
 		public ContentProvider() {}
 
 		@Override
 		public void
 		dispose() {}
 
 		@Override
 		public void
 		inputChanged(	final Viewer arg0,
 									final Object oldInput,
 									final Object newInput) {
 			this.files = (IFile[]) newInput;
 		}
 
 		@Override
 		public void
 		updateElement(final int index) {
 			try {
 				Dialog.VIEWER.replace(this.files[index], index);
 			}
 			catch (final Exception e) {
 				Dialog.LOGGER.log(Level.FINE, e.getMessage(), e);
 			}
 		}
 	}
 
 	private void
 	setupTable() {
 		Dialog.TABLE.setLinesVisible(true);
 		Dialog.TABLE.setHeaderVisible(false);
 		Dialog.TABLE.setSize(Dialog.TABLE.getClientArea().width, Dialog.TABLE
 			.getClientArea().height);
 		this.createTableColumn();
 	}
 
 	private void
 	createTableColumn() {
 		final TableViewerColumn col =
 			new TableViewerColumn(Dialog.VIEWER, SWT.RIGHT | SWT.LEFT | SWT.CENTER);
 		col.getColumn().setWidth(Dialog.TABLE.getClientArea().width);
 		col.getColumn().setResizable(true);
 		col.setLabelProvider(new LabelProvider());
 		if (!EditorContext.isWindows()) col.getColumn().pack();
 	}
 
 	private final class LabelProvider extends StyledCellLabelProvider {
 
 		private final String separator = this.getSeparator();
 		private final StyledString.Styler filenameStyler = this
 			.styler(FONT.LARGE_BOLD_FONT, null);
 		private final StyledString.Styler folderStyler = this
 			.styler(FONT.NORMAL_FONT, this.color(SWT.COLOR_DARK_GRAY));
 		private final StyledString.Styler inStyler = this
 			.styler(FONT.ITALIC_FONT, this.color(SWT.COLOR_GRAY));
 		private final StyledString.Styler modifiedStyler = this
 			.styler(FONT.SMALL_ITALIC_FONT, this.color(SWT.COLOR_GRAY));
 		private final StyledString.Styler timeStyler = this
 			.styler(FONT.SMALL_BOLD_FONT, this.color(SWT.COLOR_DARK_RED));
 		private final StyledString.Styler typeStyler = this
 			.styler(FONT.SMALL_BOLD_FONT, this.color(SWT.COLOR_DARK_BLUE));
 
 		public LabelProvider() {
 			this.setOwnerDrawEnabled(true);
 		}
 
 		@Override
 		protected StyleRange
 		prepareStyleRange(final StyleRange styleRange, final boolean applyColors) {
 			return super.prepareStyleRange(styleRange, applyColors);
 		}
 
 		@Override
 		protected void
 		paint(final Event event, final Object element) {
 			super.paint(event, element);
 		}
 
 		@Override
 		protected void
 		measure(final Event event, final Object element) {
 			super.measure(event, element);
 		}
 
 		@Override
 		public void
 		update(final ViewerCell cell) {
 			this.updateCellProperties(cell, (IFile) cell.getElement(), this
 				.createStyledText((IFile) cell.getElement()));
 			super.update(cell);
 		}
 
 		private void
 		updateCellProperties(	final ViewerCell cell,
 													final IFile file,
 													final StyledString text) {
 			final Image image =
 				FileUtil.getContentTypeImage(Optional.fromNullable(file)).get();
 			cell.setText(text.toString());
 			cell.setImage(image);
 			cell.setStyleRanges(text.getStyleRanges());
 		}
 
 		private StyledString
 		createStyledText(final IFile file) {
 			final Optional<IFile> _file = Optional.fromNullable(file);
 			final StyledString text = new StyledString();
 			text.append(file.getName() + this.separator, this.filenameStyler);
 			text.append("in  ", this.inStyler);
 			text.append(this.getFolder(_file) + this.separator, this.folderStyler);
 			text.append("modified  ", this.modifiedStyler);
 			text.append(this.getTime(_file) + "  ", this.timeStyler);
 			text.append(this.getContentType(_file), this.typeStyler);
 			return text;
 		}
 
 		private String
 		getContentType(final Optional<IFile> optional) {
 			return FileUtil.getContentTypeName(optional).get();
 		}
 
 		private String
 		getTime(final Optional<IFile> optional) {
 			return FileUtil.getModificationTime(optional).get();
 		}
 
 		private String
 		getFolder(final Optional<IFile> optional) {
 			return FileUtil.getFolder(optional).get();
 		}
 
 		private Color
 		color(final int color) {
 			return Display.getCurrent().getSystemColor(color);
 		}
 
 		private StyledString.Styler
 		styler(final Font font, final Color color) {
 			return new StyledString.Styler() {
 
 				@Override
 				public void
 				applyStyles(final TextStyle textStyle) {
 					textStyle.font = font;
 					textStyle.foreground = color;
 				}
 			};
 		}
 
 		private String
 		getSeparator() {
 			if (EditorContext.isWindows()) return "  ";
 			return System.getProperty("line.separator");
 		}
 	}
 
 	private enum FONT {
 		FONT;
 
 		private static final FontData[] FONT_DATAS = Dialog.TABLE
 			.getFont()
 			.getFontData();
 		private static final String DEFAULT_FONT_NAME = FONT.FONT_DATAS[0]
 			.getName();
 		private static final int DEFAULT_FONT_HEIGHT = FONT.FONT_DATAS[0]
 			.getHeight();
 		public static final Font ITALIC_FONT = FONT.makeItalicizedFont();
 		public static final Font LARGE_BOLD_FONT = FONT.makeLargeBoldFont();
 		public static final Font SMALL_BOLD_FONT = FONT.makeSmallBoldFont();
 		public static final Font SMALL_ITALIC_FONT = FONT
 			.makeSmallItalicizedFont();
 		public static final Font NORMAL_FONT = FONT.makeNormalFont();
 
 		private static Font
 		makeItalicizedFont() {
 			return new Font(
 				EditorContext.DISPLAY,
 				FONT.DEFAULT_FONT_NAME,
 				FONT.DEFAULT_FONT_HEIGHT,
 				SWT.ITALIC);
 		}
 
 		private static Font
 		makeLargeBoldFont() {
 			return new Font(
 				EditorContext.DISPLAY,
 				FONT.DEFAULT_FONT_NAME,
 				FONT.DEFAULT_FONT_HEIGHT + 2,
 				SWT.BOLD);
 		}
 
 		private static Font
 		makeSmallBoldFont() {
 			return new Font(
 				EditorContext.DISPLAY,
 				FONT.DEFAULT_FONT_NAME,
 				FONT.DEFAULT_FONT_HEIGHT - 2,
 				SWT.BOLD);
 		}
 
 		private static Font
 		makeSmallItalicizedFont() {
 			return new Font(
 				EditorContext.DISPLAY,
 				FONT.DEFAULT_FONT_NAME,
 				FONT.DEFAULT_FONT_HEIGHT - 2,
 				SWT.ITALIC);
 		}
 
 		private static Font
 		makeNormalFont() {
 			return new Font(
 				EditorContext.DISPLAY,
 				FONT.DEFAULT_FONT_NAME,
 				FONT.DEFAULT_FONT_HEIGHT,
 				SWT.NORMAL);
 		}
 	}
 
 	private void
 	addListeners() {
 		Dialog.SHELL.addShellListener(new DialogShellListener());
 		Dialog.TABLE.addFocusListener(new ViewerFocusListener());
 		Dialog.TEXT.addFocusListener(new TextFocusListener());
 		Dialog.SHELL.addFocusListener(new TextFocusListener());
 		Dialog.TABLE.addKeyListener(new ViewerKeyListener());
 		Dialog.TEXT.addModifyListener(new TextModifyListener());
 		Dialog.VIEWER.addDoubleClickListener(new ViewerDoubleClickListener());
 		Dialog.listenForTextSelection();
 	}
 
 	private final class DialogShellListener implements ShellListener {
 
 		public DialogShellListener() {}
 
 		@Override
 		public void
 		shellActivated(final ShellEvent arg0) {
 			new AsyncTask() {
 
 				@Override
 				public void
 				execute() {
 					Dialog.focusViewer();
 				}
 			}.start();
 		}
 
 		@Override
 		public void
 		shellClosed(final ShellEvent event) {
 			event.doit = false;
 			Dialog.SHELL.setVisible(false);
 			Dialog.reset();
 		}
 
 		@Override
 		public void
 		shellDeactivated(final ShellEvent arg0) {
 			new AsyncTask() {
 
 				@Override
 				public void
 				execute() {
 					Dialog.refresh();
 				}
 			}.start();
 		}
 
 		@Override
 		public void
 		shellDeiconified(final ShellEvent arg0) {}
 
 		@Override
 		public void
 		shellIconified(final ShellEvent arg0) {}
 	}
 
 	private final class ViewerFocusListener implements FocusListener {
 
 		public ViewerFocusListener() {}
 
 		@Override
 		public void
 		focusGained(final FocusEvent arg0) {}
 
 		@Override
 		public void
 		focusLost(final FocusEvent arg0) {
 			new AsyncTask() {
 
 				@Override
 				public void
 				execute() {
 					Dialog.refocusViewer();
 				}
 			}.start();
 		}
 	}
 
 	private final class TextFocusListener implements FocusListener {
 
 		public TextFocusListener() {}
 
 		@Override
 		public void
 		focusGained(final FocusEvent arg0) {
 			Dialog.refocusViewer();
 		}
 
 		@Override
 		public void
 		focusLost(final FocusEvent arg0) {}
 	}
 
 	private final class ViewerKeyListener implements KeyListener {
 
 		protected final TaskMutexRule rule = new TaskMutexRule();
 
 		public ViewerKeyListener() {}
 
 		@Override
 		public void
 		keyPressed(final KeyEvent event) {
 			if (Dialog.isValidCharacter(String.valueOf(event.character))) {
 				event.doit = false;
 				new AsyncTask() {
 
 					@Override
 					public void
 					execute() {
 						Dialog.updateText(event.character);
 					}
 				}.setRule(this.rule).start();
 			} else if (event.keyCode == SWT.BS) {
 				event.doit = false;
 				new AsyncTask() {
 
 					@Override
 					public void
 					execute() {
 						Dialog.backspace();
 					}
 				}.setRule(this.rule).start();
 			} else if ((event.keyCode == SWT.CR) || (event.keyCode == SWT.KEYPAD_CR)) {
 				event.doit = false;
 				new AsyncTask() {
 
 					@Override
 					public void
 					execute() {
 						Dialog.SHELL.close();
 						Dialog.openFiles();
 					}
 				}.setRule(this.rule).start();
 			} else if ((event.keyCode == SWT.DEL)) {
 				event.doit = false;
 				new AsyncTask() {
 
 					@Override
 					public void
 					execute() {
 						Dialog.SHELL.close();
 						Dialog.closeFiles();
 					}
 				}.setRule(this.rule).start();
 			}
 		}
 
 		@Override
 		public void
 		keyReleased(final KeyEvent arg0) {}
 	}
 
 	private final class TextModifyListener implements ModifyListener {
 
 		private final TaskMutexRule rule = new TaskMutexRule();
 
 		public TextModifyListener() {}
 
 		@Override
 		public void
 		modifyText(final ModifyEvent arg0) {
 			new AsyncTask() {
 
 				@Override
 				public void
 				execute() {
 					Dialog.filterViewer();
 				}
 			}.setRule(this.rule).start();
 		}
 	}
 
 	private final class ViewerDoubleClickListener
 		implements
 			IDoubleClickListener {
 
 		private final TaskMutexRule rule = new TaskMutexRule();
 
 		public ViewerDoubleClickListener() {}
 
 		@Override
 		public void
 		doubleClick(final DoubleClickEvent arg0) {
 			new AsyncTask() {
 
 				@Override
 				public void
 				execute() {
 					Dialog.SHELL.close();
 					Dialog.openFiles();
 				}
 			}.setRule(this.rule).start();
 		}
 	}
 
 	private static void
 	listenForTextSelection() {
 		Dialog.SHELL.getDisplay().addFilter(SWT.KeyDown, new Listener() {
 
 			@Override
 			public void
 			handleEvent(final Event event) {
 				if (this.isCtrlL(event)) new AsyncTask() {
 
 					@Override
 					public void
 					execute() {
 						selectText();
 					}
 				}.start();
 			}
 
 			private boolean
 			isCtrlL(final Event event) {
 				return (Character.toUpperCase((char) event.keyCode) == 'L')
 					&& ((event.stateMask & SWT.CTRL) == SWT.CTRL);
 			}
 
 			protected void
 			selectText() {
 				Dialog.TEXT.selectAll();
 			}
 		});
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public static void
 	eventHandler(final RankedFilesEvent event) {
 		new AsyncTask() {
 
 			@Override
 			public void
 			execute() {
 				Dialog.updateViewer(event.getFiles());
 			}
 		}.setRule(Dialog.RULE).start();
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public static void
 	eventHandler(final FilteredFilesResultEvent event) {
 		new AsyncTask() {
 
 			@Override
 			public void
 			execute() {
 				Dialog.updateViewer(event.getFiles());
 			}
 		}.setRule(Dialog.RULE).start();
 	}
 
 	protected static void
 	updateViewer(final List<IFile> Files) {
 		try {
 			Dialog._updateViewer(Files);
 		}
 		catch (final Exception e) {
 			Dialog.LOGGER.log(Level.WARNING, e.getMessage(), e);
 		}
 	}
 
 	private static void
 	_updateViewer(final List<IFile> files) {
 		Dialog.VIEWER.getControl().setRedraw(false);
 		Dialog.VIEWER.setInput(files.toArray(new IFile[files.size()]));
 		Dialog.VIEWER.setItemCount(files.size());
 		Dialog.VIEWER.getControl().setRedraw(true);
 		Dialog.refresh();
 		Dialog.focusViewer();
 	}
 
 	@Subscribe
 	@AllowConcurrentEvents
 	public static void
 	eventHandler(final ShowFastOpenDialogEvent event) {
 		new AsyncTask() {
 
 			@Override
 			public void
 			execute() {
 				Dialog.refresh();
 				Dialog.SHELL.open();
 				Dialog.focusViewer();
 			}
 		}.start();
 	}
 
 	protected static void
 	focusViewer() {
 		Dialog._focusViewer();
 		Dialog.TABLE.setSelection(Dialog.TABLE.getTopIndex());
 	}
 
 	public static void
 	reset() {
 		Dialog.TEXT.setText("");
 	}
 
 	protected static void
 	refresh() {
 		Dialog.VIEWER.refresh(true, true);
 	}
 
 	protected static boolean
 	isValidCharacter(final String character) {
 		return Dialog.TEXT_PATTERN.matcher(character).matches();
 	}
 
 	protected static void
 	updateText(final char character) {
 		Dialog.TEXT.insert(String.valueOf(character));
 	}
 
 	protected static void
 	backspace() {
 		final int end = Dialog.TEXT.getCaretPosition();
 		if (end < 1) return;
 		Dialog.delete(end);
 	}
 
 	private static void
 	delete(final int end) {
 		final int start =
 			end
 				- (Dialog.TEXT.getSelectionText().length() > 0 ? Dialog.TEXT
 					.getSelectionText()
 					.length() : 1);
 		Dialog.TEXT.setSelection(start, end);
 		Dialog.TEXT.cut();
 		Dialog.TEXT.setSelection(start, start);
 	}
 
 	protected static void
 	openFiles() {
 		for (final int index : Dialog.TABLE.getSelectionIndices())
 			new AsyncTask() {
 
 				@Override
 				public void
 				execute() {
 					Dialog.openFile(((IFile) Dialog.VIEWER.getElementAt(index)));
 				}
 			}.start();
 	}
 
 	protected static void
 	openFile(final IFile file) {
 		try {
 			EditorContext.openEditor(file);
 		}
 		catch (final PartInitException e) {
 			Dialog.openLink(file);
 		}
 	}
 
 	private static void
 	openLink(final IFile file) {
 		try {
 			EditorContext.openLink(file);
 		}
 		catch (final CoreException e) {
 			Dialog.LOGGER.log(Level.SEVERE, e.getMessage(), e);
 		}
 	}
 
 	protected static void
 	closeFiles() {
 		for (final int index : Dialog.TABLE.getSelectionIndices())
 			new AsyncTask() {
 
 				@Override
 				public void
 				execute() {
 					EditorContext
 						.closeEditor(((IFile) Dialog.VIEWER.getElementAt(index)));
 				}
 			}.start();
 	}
 
 	protected static void
 	filterViewer() {
 		EventBus.post(new FilterFilesEvent(Dialog.TEXT.getText().trim()));
 	}
 
 	protected static void
 	refocusViewer() {
 		Dialog._focusViewer();
 		Dialog.TABLE.setSelection(Dialog.TABLE.getSelectionIndex());
 	}
 
 	private static void
 	_focusViewer() {
 		Dialog.TABLE.setFocus();
 		Dialog.TABLE.forceFocus();
 	}
 
 	@Override
 	public Instance
 	stop() {
 		Dialog.SHELL.dispose();
 		return super.stop();
 	}
 }
