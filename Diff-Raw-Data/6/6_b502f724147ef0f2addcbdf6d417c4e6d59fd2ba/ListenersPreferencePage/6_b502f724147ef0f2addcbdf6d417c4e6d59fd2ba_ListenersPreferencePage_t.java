 package sk.stuba.fiit.perconik.ui.preferences;
 
 import java.io.IOException;
 import java.text.Collator;
 import java.util.Collections;
 import java.util.Set;
 import javax.annotation.Nonnull;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.util.BidiUtils;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.ui.IWorkbench;
 import sk.stuba.fiit.perconik.eclipse.swt.widgets.WidgetListener;
 import sk.stuba.fiit.perconik.preferences.ListenerPreferences;
 import sk.stuba.fiit.perconik.preferences.persistence.ListenerPersistenceData;
 import sk.stuba.fiit.perconik.preferences.persistence.Registrations;
 import sk.stuba.fiit.perconik.ui.utilities.Buttons;
 import sk.stuba.fiit.perconik.ui.utilities.Tables;
 import sk.stuba.fiit.perconik.ui.utilities.Widgets;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Sets;
 
 public final class ListenersPreferencePage extends AbstractWorkbenchPreferencePage
 {
 	private ListenerPreferences preferences;
 	
 	private Set<ListenerPersistenceData> data;
 	
 	CheckboxTableViewer tableViewer;
 
 	Button addButton;
 	
 	Button removeButton;
 	
 	Button registerButton;
 	
 	Button unregisterButton;
 	
 	Button importButton;
 	
 	Button exportButton;
 
 	Button refreshButton;
 	
 	public ListenersPreferencePage()
 	{
 	}
 	
 	@Override
 	public final void init(final IWorkbench workbench)
 	{
 	}
 	
 	@Override
 	protected final Control createContents(final Composite ancestor)
 	{
 		Composite parent = new Composite(ancestor, SWT.NONE);
 		
 		GridLayout parentLayout = new GridLayout();
 		parentLayout.numColumns   = 2;
 		parentLayout.marginHeight = 0;
 		parentLayout.marginWidth  = 0;
 		parent.setLayout(parentLayout);
 
         Composite innerParent = new Composite(parent, SWT.NONE);
         
         GridLayout innerLayout = new GridLayout();
         innerLayout.numColumns   = 2;
         innerLayout.marginHeight = 0;
         innerLayout.marginWidth  = 0;
         innerParent.setLayout(innerLayout);
         
         GridData innerGrid = new GridData(GridData.FILL_BOTH);
         innerGrid.horizontalSpan = 2;
         innerParent.setLayoutData(innerGrid);
 
         Composite         tableComposite = new Composite(innerParent, SWT.NONE);
         TableColumnLayout tableLayout    = new TableColumnLayout();
         
         GridData tableGrid = new GridData(GridData.FILL_BOTH);
         tableGrid.widthHint  = 360;
         tableGrid.heightHint = this.convertHeightInCharsToPixels(10);
         tableComposite.setLayout(tableLayout);
         tableComposite.setLayoutData(tableGrid);
         
         Table table = Tables.create(tableComposite, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
 
 		GC gc = new GC(this.getShell());
 		gc.setFont(JFaceResources.getDialogFont());
 
 		Tables.createColumn(table, tableLayout, "Listener implementation", gc, 4);
 		Tables.createColumn(table, tableLayout, "Serializable",            gc, 1);
 
 		gc.dispose();
 
 		this.tableViewer = new CheckboxTableViewer(table);
 		
 		this.tableViewer.setLabelProvider(new ListenerLabelProvider());
 		this.tableViewer.setContentProvider(new ListenerContentProvider());
 		this.tableViewer.setComparator(new ListenerComparator());
 
 		this.tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
 		{
 			public final void selectionChanged(final SelectionChangedEvent e)
 			{
 				updateButtons();
 			}
 		});
 
 		this.tableViewer.addCheckStateListener(new ICheckStateListener()
 		{
 			public final void checkStateChanged(final CheckStateChangedEvent e)
 			{
 				updateData((ListenerPersistenceData) e.getElement(), e.getChecked());
 				updateButtons();
 			}
 		});
 		
 		BidiUtils.applyTextDirection(this.tableViewer.getControl(), BidiUtils.BTD_DEFAULT);
 
 		Composite buttons = new Composite(innerParent, SWT.NONE);
 		
 		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
 		parentLayout = new GridLayout();
 		parentLayout.marginHeight = 0;
 		parentLayout.marginWidth  = 0;
 		buttons.setLayout(parentLayout);
 
 		this.addButton = Buttons.create(buttons, "Add", new WidgetListener()
 		{
 			public final void handleEvent(final Event e)
 			{
 				performAdd();
 			}
 		});
 
 		this.removeButton = Buttons.create(buttons, "Remove", new WidgetListener()
 		{
 			public final void handleEvent(final Event e)
 			{
 				performRemove();
 			}
 		});
 
 		Widgets.createSeparator(buttons);
 
 		this.registerButton = Buttons.create(buttons, "Register", new WidgetListener()
 		{
 			public final void handleEvent(final Event e)
 			{
 				performRegister();
 			}
 		});
 
 		this.unregisterButton = Buttons.create(buttons, "Unregister", new WidgetListener()
 		{
 			public final void handleEvent(final Event e)
 			{
 				performUnregister();
 			}
 		});
 		
 		Widgets.createSeparator(buttons);
 		
 		this.importButton = Buttons.create(buttons, "Import", new WidgetListener()
 		{
 			public final void handleEvent(final Event e)
 			{
 				performImport();
 			}
 		});
 
 		this.exportButton = Buttons.create(buttons, "Export", new WidgetListener()
 		{
 			public final void handleEvent(final Event e)
 			{
 				performExport();
 			}
 		});
 		
 		Widgets.createSeparator(buttons);
 		
 		this.refreshButton = Buttons.create(buttons, "Refresh", new WidgetListener()
 		{
 			public final void handleEvent(final Event e)
 			{
 				performRefresh();
 			}
 		});
 		
 		this.load(ListenerPreferences.getInstance());
 		
 		Dialog.applyDialogFont(parent);
 		
 		innerParent.layout();
 
 		return parent;
 	}
 
 	final Set<ListenerPersistenceData> checkedData()
 	{
 		return Sets.filter(this.data, new Predicate<ListenerPersistenceData>()
 		{
 			public final boolean apply(@Nonnull final ListenerPersistenceData data)
 			{
 				return data.hasRegistredMark();
 			}
 		});
 	}
 	
 	final void updateData(final ListenerPersistenceData data, final boolean status)
 	{
 		this.data.remove(data);
 		this.data.add(data.markRegistered(status));
 		
 		this.tableViewer.setChecked(data, status);
 		this.tableViewer.refresh();
 	}
 
 	final void updateSelectedData(final boolean status)
 	{
 		IStructuredSelection selection = (IStructuredSelection) this.tableViewer.getSelection();
 
 		for (Object item: selection.toList())
 		{
 			ListenerPersistenceData data = (ListenerPersistenceData) item;
 			
 			this.updateData(data, status);
 		}
 
 		this.tableViewer.refresh();
 	}
 	
 	final void updateTable()
 	{
 		this.tableViewer.setInput(this.data);
 		this.tableViewer.refresh();
 		this.tableViewer.setAllChecked(false);
 		this.tableViewer.setCheckedElements(this.checkedData().toArray());
 	}
 	
 	final void updateButtons()
 	{
 		IStructuredSelection selection = (IStructuredSelection) this.tableViewer.getSelection();
 		
 		int selectionCount = selection.size();
 		int itemCount      = this.tableViewer.getTable().getItemCount();
 		
 		boolean registrable   = false;
 		boolean unregistrable = false;
 		
 		if (selectionCount > 0)
 		{
 			for (Object item: selection.toList())
 			{
 				ListenerPersistenceData data = (ListenerPersistenceData) item;
 				
 				boolean registred = data.hasRegistredMark();
 				
 				registrable   |= !registred;
 				unregistrable |= registred;
 			}
 		}
 
 		this.removeButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
 
 		this.registerButton.setEnabled(registrable);
 		this.unregisterButton.setEnabled(unregistrable);
 
 		this.exportButton.setEnabled(selectionCount > 0);
 	}
 	
 	final void performAdd()
 	{
 		this.displayInformation("Add listener", "Operation not yet implemented.");
 	}
 
 	final void performRemove()
 	{
 		this.displayInformation("Remove listeners", "Operation not yet implemented.");
 	}
 
 	final void performRegister()
 	{
 		this.updateSelectedData(true);
 		this.updateButtons();
 	}
 	
 	final void performUnregister()
 	{
 		this.updateSelectedData(false);
 		this.updateButtons();
 	}
 
 	final void performImport()
 	{
 		this.displayInformation("Import listeners", "Operation not yet implemented.");
 	}
 
 	final void performExport()
 	{
 		this.displayInformation("Export listeners", "Operation not yet implemented.");
 	}
 	
 	final void performRefresh()
 	{
 		for (ListenerPersistenceData data: Sets.newHashSet(this.data))
 		{
 			this.updateData(data, data.isRegistred());
 		}
 	}
 	
 	private static final class ListenerLabelProvider extends LabelProvider implements ITableLabelProvider
 	{
 		ListenerLabelProvider()
 		{
 		}
 
 		public final Image getColumnImage(final Object element, final int column)
 		{
 			return null;
 		}
 
 		public final String getColumnText(final Object element, final int column)
 		{
 			ListenerPersistenceData data = (ListenerPersistenceData) element;
 
 			switch (column)
 			{
 				case 0:
 					return data.getListenerClass().getName();
 				case 1:
 					return data.hasSerializedListener() ? "yes" : "no";
 				default:
 					throw new IllegalStateException();
 			}
 		}
 	}
 
 	private static final class ListenerContentProvider implements IStructuredContentProvider
 	{
 		private Set<?> data;
 
 		ListenerContentProvider()
 		{
 			this.data = Collections.emptySet();
 		}
 		
 		public final Object[] getElements(final Object input)
 		{
 			return this.data.toArray();
 		}
 
 		public final void inputChanged(final Viewer viewer, final Object from, final Object to)
 		{
 			this.data = (Set<?>) to;
 		}
 
 		public final void dispose()
 		{
 			this.data = null;
 		}
 	}
 
 	private static final class ListenerComparator extends ViewerComparator
 	{
 		ListenerComparator()
 		{
 		}
 	
 		@Override
 		public final int compare(final Viewer viewer, final Object a, final Object b)
 		{
 			if ((a instanceof ListenerPersistenceData) && (b instanceof ListenerPersistenceData))
 			{
 				ListenerPersistenceData data  = (ListenerPersistenceData) a;
 				ListenerPersistenceData other = (ListenerPersistenceData) b;
 				
 				return Collator.getInstance().compare(data.getListenerClass().getName(), other.getListenerClass().getName());
 			}
 			
 			return super.compare(viewer, a, b);
 		}
 	
 		@Override
 		public final boolean isSorterProperty(final Object element, final String property)
 		{
 			return true;
 		}
 	}
 
 	@Override
 	public final boolean performOk()
 	{
 		this.apply();
 		this.save();
 		
 		return super.performOk();
 	}
 
 	@Override
 	public final boolean performCancel()
 	{
 		this.load(ListenerPreferences.getInstance());
 		
 		return super.performCancel();
 	}
 
 	@Override
 	protected final void performDefaults()
 	{
 		this.load(ListenerPreferences.getDefault());
 		
 		this.preferences = ListenerPreferences.getInstance();
 		
 		super.performDefaults();
 	}
 
 	@Override
 	protected final void performApply()
 	{
 		super.performApply();
 	}
 
 	private final void apply()
 	{
 		Set<ListenerPersistenceData> data = Registrations.applyRegisteredMark(this.data);
 		
 		this.preferences.setListenerPersistenceData(data);
 		
 		this.updateTable();
 		this.updateButtons();
 	}
 	
 	private final void load(final ListenerPreferences preferences)
 	{
 		this.setListenerPreferences(preferences);
 		
 		this.updateTable();
 		this.updateButtons();
 	}
 	
 	private final void save()
 	{
 		try
 		{
 			this.preferences.save();
 		}
 		catch (IOException e)
 		{
 			this.displayError("Listener Preferences", "Failed to save listener preferences.");
 		}
 	}
 
 	public final void setListenerPreferences(final ListenerPreferences preferences)
 	{
 		this.preferences = Preconditions.checkNotNull(preferences);
 		this.data        = this.preferences.getListenerPersistenceData();
 	}
 
 	public final ListenerPreferences getListenerPreferences()
 	{
 		return this.preferences;
 	}
 }
