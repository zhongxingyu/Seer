 package sk.stuba.fiit.perconik.ui.preferences;
 
 import java.io.IOException;
 import java.lang.annotation.Annotation;
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
 import sk.stuba.fiit.perconik.core.persistence.AnnotableRegistration;
 import sk.stuba.fiit.perconik.core.persistence.MarkableRegistration;
 import sk.stuba.fiit.perconik.core.persistence.RegistrationMarker;
 import sk.stuba.fiit.perconik.eclipse.swt.widgets.WidgetListener;
 import sk.stuba.fiit.perconik.ui.utilities.Buttons;
 import sk.stuba.fiit.perconik.ui.utilities.Tables;
 import sk.stuba.fiit.perconik.ui.utilities.Widgets;
 import com.google.common.base.CaseFormat;
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Sets;
 
 abstract class AbstractRegistrationPreferencePage<P, R extends AnnotableRegistration & MarkableRegistration & RegistrationMarker<R>> extends AbstractWorkbenchPreferencePage
 {
 	private P preferences;
 	
 	Set<R> registrations;
 	
 	CheckboxTableViewer tableViewer;
 
 	Button addButton;
 	
 	Button removeButton;
 	
 	Button registerButton;
 	
 	Button unregisterButton;
 	
 	Button importButton;
 	
 	Button exportButton;
 
 	Button refreshButton;
 	
 	AbstractRegistrationPreferencePage()
 	{
 	}
 	
 	abstract Class<R> type();
 	
 	final R cast(final Object o)
 	{
 		return this.type().cast(o);
 	}
 	
 	@Override
 	protected final Control createContents(final Composite parent)
 	{
 		Composite composite = new Composite(parent, SWT.NONE);
 		
 		GridLayout parentLayout = new GridLayout();
 		parentLayout.numColumns   = 2;
 		parentLayout.marginHeight = 0;
 		parentLayout.marginWidth  = 0;
 		composite.setLayout(parentLayout);
 
         Composite innerParent = new Composite(composite, SWT.NONE);
         
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
 
 		this.makeTableColumns(table, tableLayout, gc);
 
 		gc.dispose();
 
 		this.tableViewer = new CheckboxTableViewer(table);
 		
 		this.tableViewer.setContentProvider(new StandardContentProvider());
 		this.tableViewer.setLabelProvider(this.createContentProvider());
 		this.tableViewer.setComparator(this.createViewerComparator());
 
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
 				R data = (R) e.getElement();
 				
 				if (data.isProvided())
 				{
 					updateData(data, e.getChecked());
 					updateButtons();
 				}
 				else
 				{
 					e.getCheckable().setChecked(data, data.hasRegistredMark());
 				}
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
 		
 		this.loadInternal(this.source());
 
 		Dialog.applyDialogFont(composite);
 		
 		innerParent.layout();
 		
 		return composite;
 	}
 	
 	protected abstract AbstractLabelProvider<R> createContentProvider();
 	
 	protected abstract AbstractViewerComparator createViewerComparator();
 	
 	protected abstract void makeTableColumns(final Table table, final TableColumnLayout layout, final GC gc);
 	
 	final Set<R> checkedData()
 	{
 		return Sets.filter(this.registrations, new Predicate<R>()
 		{
 			public final boolean apply(@Nonnull final R registration)
 			{
 				return registration.hasRegistredMark();
 			}
 		});
 	}
 	
 	final Set<R> unknownData()
 	{
 		return Sets.filter(this.registrations, new Predicate<R>()
 		{
 			public final boolean apply(@Nonnull final R registration)
 			{
 				return !registration.isProvided();
 			}
 		});
 	}
 
 	final void updateData(final R registration, final boolean status)
 	{
 		this.registrations.remove(registration);
 		this.registrations.add(registration.markRegistered(status));
 		
 		this.tableViewer.setChecked(registration, status);
 		this.tableViewer.refresh();
 	}
 
 	final void updateSelectedData(final boolean status)
 	{
 		IStructuredSelection selection = (IStructuredSelection) this.tableViewer.getSelection();
 
 		for (Object item: selection.toList())
 		{
 			R registration = this.cast(item);
 			
 			if (registration.isProvided())
 			{
 				this.updateData(registration, status);
 			}
 		}
 
 		this.tableViewer.refresh();
 	}
 	
 	final void updateTable()
 	{
 		this.tableViewer.setInput(this.registrations);
 		this.tableViewer.refresh();
 		this.tableViewer.setAllChecked(false);
 		this.tableViewer.setCheckedElements(this.checkedData().toArray());
 		this.tableViewer.setGrayedElements(this.unknownData().toArray());
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
 				R registration = this.cast(item);
 				
 				if (registration.isProvided())
 				{
 					boolean registred = registration.hasRegistredMark();
 					
 					registrable   |= !registred;
 					unregistrable |= registred;
 				}
 			}
 		}
 
 		this.removeButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
 
 		this.registerButton.setEnabled(registrable);
 		this.unregisterButton.setEnabled(unregistrable);
 
 		this.exportButton.setEnabled(selectionCount > 0);
 	}
 	
 	private static final class StandardContentProvider implements IStructuredContentProvider
 	{
 		private Set<?> data;
 	
 		StandardContentProvider()
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
 
 	static abstract class AbstractLabelProvider<R extends AnnotableRegistration & MarkableRegistration & RegistrationMarker<R>> extends LabelProvider implements ITableLabelProvider
 	{
 		AbstractLabelProvider()
 		{
 		}
 
 		public final String getAnnotations(final R registration)
 		{
 			Set<String> flags = Sets.newTreeSet();
 			
 			for (Annotation annotation: registration.getAnnotations())
 			{
 				flags.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, annotation.annotationType().getSimpleName()));
 			}
 			
 			return Joiner.on(", ").join(flags);
 		}
 
 		public Image getColumnImage(Object element, int column)
 		{
 			return null;
 		}
 	}
 
 	static abstract class AbstractViewerComparator extends ViewerComparator
 	{
 		AbstractViewerComparator()
 		{
 		}
 	
 		@Override
 		public boolean isSorterProperty(Object element, String property)
 		{
 			return true;
 		}
 	}
 
 	void performAdd()
 	{
 		this.displayNotice("Add", "Operation not yet implemented.");
 	}
 
 	void performRemove()
 	{
 		this.displayNotice("Remove", "Operation not yet implemented.");
 	}
 
 	void performRegister()
 	{
 		this.updateSelectedData(true);
 		this.updateButtons();
 	}
 	
 	void performUnregister()
 	{
 		this.updateSelectedData(false);
 		this.updateButtons();
 	}
 
 	void performImport()
 	{
 		this.displayNotice("Import", "Operation not yet implemented.");
 	}
 
 	void performExport()
 	{
 		this.displayNotice("Export", "Operation not yet implemented.");
 	}
 	
 	void performRefresh()
 	{
 		for (R registration: Sets.newHashSet(this.registrations))
 		{
 			this.updateData(registration, registration.isRegistered());
 		}
 	}
 	
 	abstract P source();
 
 	abstract Set<R> defaults();
 	
 	@Override
 	public final boolean performOk()
 	{
 		this.applyInternal();
 		this.saveInternal();
 		
 		return super.performOk();
 	}
 
 	@Override
 	public final boolean performCancel()
 	{
 		this.loadInternal(this.source());
 		
 		return super.performCancel();
 	}
 
 	@Override
 	protected final void performDefaults()
 	{
 		this.registrations = this.defaults();
 		
 		this.updateTable();
 		this.updateButtons();
 		
 		super.performDefaults();
 	}
 
 	@Override
 	protected final void performApply()
 	{
 		super.performApply();
 	}
 	
 	abstract void apply();
 
 	abstract void load(P preferences);
 	
 	abstract void save() throws IOException;
 
 	private final void applyInternal()
 	{
 		this.apply();
 		
 		this.updateTable();
 		this.updateButtons();
 	}
 
 	private final void loadInternal(final P preferences)
 	{
 		this.load(preferences);
 		
 		this.updateTable();
 		this.updateButtons();
 	}
 	
 	private final void saveInternal()
 	{
 		try
 		{
 			this.save();
 		}
 		catch (IOException e)
 		{
 			this.displayError("Preferences", "Failed to save preferences.");
 		}
 	}
 	
 	final void setPreferences(final P preferences)
 	{
 		this.preferences = Preconditions.checkNotNull(preferences);
 	}
 
 	final P getPreferences()
 	{
 		return this.preferences;
 	}
 }
