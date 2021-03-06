 /**
  * 
  */
 package v9t9.emulator.clients.builtin.swt.debugger;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeSet;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.resource.FontDescriptor;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.IFontDecorator;
 import org.eclipse.jface.viewers.IFontProvider;
 import org.eclipse.jface.viewers.ILazyContentProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MenuDetectEvent;
 import org.eclipse.swt.events.MenuDetectListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 import v9t9.emulator.hardware.V9t9;
 import v9t9.engine.memory.Memory;
 import v9t9.engine.memory.MemoryDomain;
 import v9t9.engine.memory.MemoryEntry;
 import v9t9.engine.memory.MemoryListener;
 import v9t9.utils.Utils;
 
 /**
  * @author Ed
  *
  */
 public class MemoryViewer extends Composite {
 
 	private TableViewer byteTableViewer;
 	private ComboViewer entryViewer;
 	private final Memory memory;
 	private TimerTask refreshTask;
 	protected MemoryRange currentRange;
 	private Button refreshButton;
 	protected boolean autoRefresh;
 	private Button pinButton;
 	private boolean pinMemory;
 	private Button filterButton;
 	private boolean filterMemory;
 	protected Set<Integer> changedMemory;
 	private Timer timer;
 
 	public MemoryViewer(Composite parent, int style, Memory memory, final Timer timer) {
 		super(parent, style);
 		this.memory = memory;
 		changedMemory = new TreeSet<Integer>();
 
 		setLayout(new GridLayout(2, false));
 		
 		createByteTable();
 		
 		memory.addListener(new MemoryListener() {
 
 			public void physicalMemoryMapChanged(MemoryEntry entry) {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						refreshEntryCombo();
 						
 					}
 				});
 			}
 			public void logicalMemoryMapChanged(MemoryEntry entry) {
 				
 			}
 			
 		});
 		
 		this.timer = timer;
 		refreshTask = new TimerTask() {
 
 			@Override
 			public void run() {
 				if (timer != null && autoRefresh && !isDisposed())
 					getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							refreshViewer();
 						}
 					});
 			}
 			
 		};
 		timer.schedule(refreshTask, 0, 250);
 		
 	}
 
 	@Override
 	public void dispose() {
 		refreshTask.cancel();
		timer.cancel();
		timer = null;
 		super.dispose();
 	}
 	
 	protected void scrollToActiveRegion(int lowRange, int hiRange) {
 		int row = getMemoryRowIndex(lowRange);
 		if (byteTableViewer.getContentProvider() instanceof ILazyContentProvider) {
 			try {
 				((ILazyContentProvider)byteTableViewer.getContentProvider()).updateElement(row);
 			} catch (Exception e) {
 				// can throw if it's not gonna be visible
 			}
 		}
 		Object elementAt = byteTableViewer.getElementAt(row);
 		if (elementAt != null) {
 			byteTableViewer.reveal(elementAt);
 		}
 		int visibleRows = byteTableViewer.getTable().getSize().y 
 			/ byteTableViewer.getTable().getItemHeight();
 		int endRow = getMemoryRowIndex(hiRange);
 		if (visibleRows >= endRow - row) {
 			if (byteTableViewer.getContentProvider() instanceof ILazyContentProvider) {
 				try {
 					((ILazyContentProvider)byteTableViewer.getContentProvider()).updateElement(endRow);
 				} catch (Exception e) {
 					// can throw if it's not gonna be visible	
 				}
 			}
 			elementAt = byteTableViewer.getElementAt(endRow);
 			if (elementAt != null) {
 				byteTableViewer.reveal(elementAt);
 			}
 		}
 	}
 
 	protected final int getMemoryRowIndex(int addr) {
 		return (addr - currentRange.addr) / 16;
 	}
 	protected final int getMemoryColumnIndex(int addr) {
 		return (addr - currentRange.addr) % 16;
 	}
 
 	static class MemoryEntryLabelProvider extends LabelProvider {
 		@Override
 		public String getText(Object element) {
 			MemoryEntry entry = (MemoryEntry) element;
 			return entry.getName() + " (" 
 				+ entry.getDomain().getName() + " >" + Utils.toHex4(entry.addr + entry.addrOffset) + ")";
 		}
 	}
 	protected void createByteTable() {
 		entryViewer = new ComboViewer(this, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.NO_FOCUS);
 		entryViewer.setContentProvider(new ArrayContentProvider());
 		entryViewer.setLabelProvider(new MemoryEntryLabelProvider());
 		/*entryViewer.setSorter(new ViewerSorter() {
 			@Override
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				if (e1 instanceof MemoryEntry && e2 instanceof MemoryEntry) {
 					MemoryEntry me1 = (MemoryEntry) e1;
 					MemoryEntry me2 = (MemoryEntry) e2;
 					if (me1.hasWriteAccess()) {
 						if (me2.hasWriteAccess())
 							return me1.addr - me2.addr;
 						else
 							return -1;
 					}
 					if (me1.hasReadAccess()) {
 						if (me2.hasWriteAccess())
 							return 1;
 						else if (me2.hasReadAccess())
 							return me1.addr - me2.addr;
 						else
 							return 0;
 					}
 					return -1;
 				}
 				return super.compare(viewer, e1, e2);
 			}
 		});*/
 		
 		entryViewer.setFilters(new ViewerFilter[] {
 			new ViewerFilter() {
 
 				@Override
 				public boolean select(Viewer viewer, Object parentElement,
 						Object element) {
 					MemoryEntry entry = (MemoryEntry) element;
 					if (!entry.hasReadAccess())
 						return false;
 					if (filterMemory && !entry.hasWriteAccess())
 						return false;
 					return true;
 				}
 				
 			}
 		});
 		entryViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			public void selectionChanged(SelectionChangedEvent event) {
 				Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
 				MemoryRange range;
 				if (element instanceof MemoryEntry) {
 					range = new MemoryRange((MemoryEntry) element);
 				} else if (element instanceof MemoryRange) {
 					range = (MemoryRange) element;
 				} else {
 					return;
 				}
 				changeCurrentRange(range);
 			}
 			
 		});
 		
 		Composite buttonBar = new Composite(this, SWT.NO_FOCUS);
 		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(buttonBar);
 		buttonBar.setLayout(new RowLayout(SWT.HORIZONTAL));
 		
 		filterButton = new Button(buttonBar, SWT.TOGGLE);
 		filterButton.setImage(new Image(getDisplay(), V9t9.getDataFile("icons/filter.png").getAbsolutePath()));
 		filterButton.setSize(24, 24);
 		filterMemory = true;
 		filterButton.setSelection(true);
 		filterButton.setToolTipText("View only writeable RAM entries");
 		filterButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				filterMemory = filterButton.getSelection();
 				entryViewer.refresh();
 			}
 		});
 
 		
 		refreshButton = new Button(buttonBar, SWT.TOGGLE);
 		refreshButton.setImage(new Image(getDisplay(), V9t9.getDataFile("icons/refresh.png").getAbsolutePath()));
 		refreshButton.setSize(24, 24);
 		autoRefresh = true;
 		refreshButton.setSelection(true);
 		refreshButton.setToolTipText("Automatically refresh memory view if set");
 		refreshButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				autoRefresh = refreshButton.getSelection();
 			}
 		});
 		
 		pinButton = new Button(buttonBar, SWT.TOGGLE);
 		pinButton.setImage(new Image(getDisplay(), V9t9.getDataFile("icons/pin.png").getAbsolutePath()));
 		pinButton.setSize(24, 24);
 		pinMemory = false;
 		pinButton.setSelection(false);
 		pinButton.setToolTipText("Pin view to scroll position if set, else, scroll to active memory");
 		pinButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				pinMemory = pinButton.getSelection();
 			}
 		});
 
 		
 		byteTableViewer = new TableViewer(this, SWT.V_SCROLL + SWT.BORDER + SWT.VIRTUAL + SWT.NO_FOCUS);
 		byteTableViewer.setContentProvider(new MemoryContentProvider());
 		byteTableViewer.setLabelProvider(new ByteMemoryLabelProvider(
 				getDisplay().getSystemColor(SWT.COLOR_RED)
 				));
 		
 		refreshEntryCombo();
 				
 		final Table table = byteTableViewer.getTable();
 		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(table);
 		
 		String[] props = new String[1 + 16 + 1];
 		props[0] = "Addr";
 		new TableColumn(table, SWT.CENTER).setText(props[0]);
 		for (int i = 0; i < 16; i++) {
 			String id = Integer.toHexString(i).toUpperCase();
 			props[i + 1] = id;
 			new TableColumn(table, SWT.CENTER | SWT.NO_FOCUS).setText(id);
 		}
 		props[17] = "0123456789ABCDEF";
 		new TableColumn(table, SWT.NO_FOCUS | SWT.CENTER).setText(props[17]);
 		
 		// hmmm... FontRegister.createFont() is busted
 		Font textFont = JFaceResources.getTextFont();
 		FontData[] fontData = textFont.getFontData();
 		int len = 0;
 		while (len < fontData.length && fontData[len] != null) 
 			len++;
 		FontData[] fontData2 = new FontData[len];
 		System.arraycopy(fontData, 0, fontData2, 0, len);
 		///
 		
 		FontDescriptor fontDescriptor = FontDescriptor.createFrom(fontData2);
 		fontDescriptor = fontDescriptor.increaseHeight(-2);
 		table.setFont(fontDescriptor.createFont(getDisplay()));
 		
 		for (int i = 0; i < 18; i++) {
 			table.getColumn(i).pack();
 		}
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		CellEditor[] editors = new CellEditor[1+16+16];
 		for (int i = 1; i < 17; i++) {
 			editors[i] = new TextCellEditor(table);
 		}
 		
 		byteTableViewer.setColumnProperties(props);
 		byteTableViewer.setCellModifier(new ByteMemoryCellModifier(byteTableViewer));
 		byteTableViewer.setCellEditors(editors);
 		
 		addTableContextMenu(table);
 	}
 
 	private void addTableContextMenu(final Table table) {
 		Menu menu = new Menu(table);
 		MenuItem item;
 		item = new MenuItem(menu, SWT.NONE);
 		item.setText("Set start range");
 		item.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (currentRange == null)
 					return;
 				int addr = table.getSelectionIndex() * 16;
 				restrictRange(currentRange.getAddress() + addr, currentRange.getAddress() + currentRange.getSize());
 			}
 		});
 		item = new MenuItem(menu, SWT.NONE);
 		item.setText("Set end range");
 		item.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (currentRange == null)
 					return;
 				int endAddr = (table.getSelectionIndex() + 1) * 16;
 				restrictRange(currentRange.getAddress(), currentRange.getAddress() + endAddr);
 			}
 		});
 		
 		item = new MenuItem(menu, SWT.NONE);
 		item.setText("Clear range");
 		item.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (currentRange == null)
 					return;
 				changeCurrentRange(new MemoryRange(currentRange.getEntry()));
 			}
 		});
 		table.setMenu(menu);
 	}
 
 	protected void changeCurrentRange(MemoryRange range) {
 		byteTableViewer.getTable().setLayoutDeferred(true);
 		byteTableViewer.setInput(range);
 		currentRange = range;
 		for (TableColumn column : byteTableViewer.getTable().getColumns())
 			column.pack();
 		byteTableViewer.getTable().setLayoutDeferred(false);
 		//MemoryViewer.this.getShell().layout(true, true);
 		MemoryViewer.this.getShell().pack();
 		
 	}
 
 	protected void restrictRange(int addr, int endAddr) {
 		MemoryRange range = new MemoryRange(
 				currentRange.getEntry(),
 				addr,
 				endAddr - addr);
 		changeCurrentRange(range);
 	}
 
 	private void refreshEntryCombo() {
 		if (!entryViewer.getControl().isDisposed()) {
 			List<MemoryEntry> allEntries = new ArrayList<MemoryEntry>();
 			for (MemoryDomain domain : memory.getDomains()) {
 				allEntries.addAll(Arrays.asList(domain.getFlattenedMemoryEntries()));
 			}
 			entryViewer.setInput(allEntries.toArray());
 		}
 	}
 
 	protected void refreshViewer() {
 		if (!isDisposed() && isVisible() && currentRange != null) {
 			int lowRange, hiRange;
 			synchronized (currentRange) {
 				lowRange = currentRange.getLowTouchRange();
 				hiRange = currentRange.getHiTouchRange();
 				if (lowRange < hiRange) {
 					if (!byteTableViewer.getTable().isDisposed()) {
 						if (pinMemory) {
 							byteTableViewer.setSelection(null);
 						}
 						/*
 						for (int addr = lowRange; addr < hiRange; addr += 16) {
 							int row = (lowRange - currentEntry.addr) / 16;
 							Object elementAt = byteTableViewer.getElementAt(row);
 							if (elementAt != null) {
 								byteTableViewer.refresh(elementAt);
 								if (addr == lowRange && !pinMemory) {
 									byteTableViewer.reveal(elementAt);
 								}
 							}
 						}*/
 						
 						if (!pinMemory) {
 							scrollToActiveRegion(lowRange, hiRange);
 						}
 						byteTableViewer.refresh();
 						currentRange.clearTouchRange();
 					}
 				}
 			}
 		}
 	}
 	
 }
