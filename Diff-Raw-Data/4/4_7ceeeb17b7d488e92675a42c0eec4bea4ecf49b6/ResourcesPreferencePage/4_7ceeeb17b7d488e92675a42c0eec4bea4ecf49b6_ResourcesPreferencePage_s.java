 package sk.stuba.fiit.perconik.ui.preferences;
 
 import java.io.IOException;
 import java.text.Collator;
 import java.util.Set;
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
 import sk.stuba.fiit.perconik.preferences.ResourcePreferences;
 import sk.stuba.fiit.perconik.preferences.persistence.ResourcePersistenceData;
 import sk.stuba.fiit.perconik.ui.utilities.Buttons;
 import sk.stuba.fiit.perconik.ui.utilities.Tables;
 import sk.stuba.fiit.perconik.ui.utilities.Widgets;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Sets;
 
 public final class ResourcesPreferencePage extends AbstractWorkbenchPreferencePage
 {
 	private ResourcePreferences preferences;
 	
 	private Set<ResourcePersistenceData> data;
 	
 	CheckboxTableViewer tableViewer;
 
 	Button addButton;
 	
 	Button removeButton;
 	
 	Button registerButton;
 	
 	Button unregisterButton;
 	
 	Button importButton;
 	
 	Button exportButton;
 
 	Button refreshButton;
 	
 	public ResourcesPreferencePage()
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
 
 		Tables.createColumn(table, tableLayout, "Name",          gc, 4);
 		Tables.createColumn(table, tableLayout, "Listener type", gc, 4);
 		Tables.createColumn(table, tableLayout, "Serializable",  gc, 1);
 
 		gc.dispose();
 
 		this.tableViewer = new CheckboxTableViewer(table);
 		
 		this.tableViewer.setLabelProvider(new ResourceLabelProvider());
 		this.tableViewer.setContentProvider(new ResourceContentProvider());
 		this.tableViewer.setComparator(new ResourceComparator());
 
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
 				updateData((ResourcePersistenceData) e.getElement(), e.getChecked());
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
 		
 		this.load(ResourcePreferences.getInstance());
 		
 		Dialog.applyDialogFont(parent);
 		
 		innerParent.layout();
 
 		return parent;
 	}
 
 	final Set<ResourcePersistenceData> checkedData()
 	{
 		return Sets.filter(this.data, new Predicate<ResourcePersistenceData>()
 		{
 			public final boolean apply(final ResourcePersistenceData data)
 			{
 				return data.hasRegistredMark();
 			}
 		});
 	}
 	
 	final void updateData(final ResourcePersistenceData data, final boolean status)
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
 			ResourcePersistenceData data = (ResourcePersistenceData) item;
 			
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
 				ResourcePersistenceData data = (ResourcePersistenceData) item;
 				
 				boolean registred = data.hasRegistredMark();
 				
 				registrable   = registrable || !registred;
 				unregistrable = unregistrable || registred;
 			}
 		}
 
 		this.removeButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
 
 		this.registerButton.setEnabled(registrable);
 		this.unregisterButton.setEnabled(unregistrable);
 
 		this.exportButton.setEnabled(selectionCount > 0);
 	}
 	
 	final void performAdd()
 	{
 		this.displayInformation("Add resource", "Operation not yet implemented.");
 	}
 
 	final void performRemove()
 	{
 		this.displayInformation("Remove resources", "Operation not yet implemented.");
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
 		this.displayInformation("Import resources", "Operation not yet implemented.");
 	}
 
 	final void performExport()
 	{
 		this.displayInformation("Export resources", "Operation not yet implemented.");
 	}
 	
 	final void performRefresh()
 	{
 		for (ResourcePersistenceData data: Sets.newHashSet(this.data))
 		{
 			this.updateData(data, data.isRegistred());
 		}
 	}
 	
 	private static final class ResourceLabelProvider extends LabelProvider implements ITableLabelProvider
 	{
 		ResourceLabelProvider()
 		{
 		}
 
 		public final Image getColumnImage(final Object element, final int column)
 		{
 			return null;
 		}
 
 		public final String getColumnText(final Object element, final int column)
 		{
 			ResourcePersistenceData data = (ResourcePersistenceData) element;
 
 			switch (column)
 			{
 				case 0:
 					return data.getResourceName();
 				case 1:
 					return data.getListenerType().getName();
 				case 2:
 					return data.hasSerializedResource() ? "yes" : "no";
 				default:
 					throw new IllegalStateException();
 			}
 		}
 	}
 
 	private static final class ResourceContentProvider implements IStructuredContentProvider
 	{
 		private Set<?> data;
 
 		ResourceContentProvider()
 		{
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
 
 	private static final class ResourceComparator extends ViewerComparator
 	{
 		ResourceComparator()
 		{
 		}
 	
 		@Override
 		public final int compare(final Viewer viewer, final Object a, final Object b)
 		{
 			if ((a instanceof ResourcePersistenceData) && (b instanceof ResourcePersistenceData))
 			{
 				ResourcePersistenceData data  = (ResourcePersistenceData) a;
 				ResourcePersistenceData other = (ResourcePersistenceData) b;
 				
 				int result = Collator.getInstance().compare(data.getResourceName(), other.getResourceName());
 				
 				if (result != 0)
 				{
 					return result;
 				}
 				
 				return Collator.getInstance().compare(data.getListenerType().getName(), other.getListenerType().getName());
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
 		this.load(ResourcePreferences.getInstance());
 		
 		return super.performCancel();
 	}
 
 	@Override
 	protected final void performDefaults()
 	{
 		this.load(ResourcePreferences.getDefault());
 		
 		this.preferences = ResourcePreferences.getInstance();
 		
 		super.performDefaults();
 	}
 
 	@Override
 	protected final void performApply()
 	{
 		super.performApply();
 	}
 
 	private final void apply()
 	{
		Set<ResourcePersistenceData> data = ResourcePersistenceData.applyRegisteredMark(this.data);
 		
 		this.preferences.setResourcePersistenceData(data);
 		
 		this.updateTable();
 		this.updateButtons();
 	}
 	
 	private final void load(final ResourcePreferences preferences)
 	{
 		this.setResourcePreferences(preferences);
 		
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
 			this.displayError("Resource Preferences", "Failed to save resource preferences.");
 		}
 	}
 
 	public final void setResourcePreferences(final ResourcePreferences preferences)
 	{
 		this.preferences = Preconditions.checkNotNull(preferences);
 		this.data        = this.preferences.getResourcePersistenceData();
 	}
 
 	public final ResourcePreferences getResourcePreferences()
 	{
 		return this.preferences;
 	}
 }
