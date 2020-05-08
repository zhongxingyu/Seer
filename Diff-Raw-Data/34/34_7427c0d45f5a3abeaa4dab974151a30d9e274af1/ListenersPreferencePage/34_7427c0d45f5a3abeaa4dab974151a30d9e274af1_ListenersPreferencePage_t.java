 package sk.stuba.fiit.perconik.ui.preferences;
 
 import java.io.IOException;
 import java.text.Collator;
 import java.util.Set;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.widgets.Table;
 import sk.stuba.fiit.perconik.core.ResourceNotRegistredException;
 import sk.stuba.fiit.perconik.core.persistence.Registrations;
 import sk.stuba.fiit.perconik.core.persistence.data.ListenerPersistenceData;
 import sk.stuba.fiit.perconik.preferences.ListenerPreferences;
 import sk.stuba.fiit.perconik.ui.utilities.Tables;
 
 /**
  * Listeners preference page.
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public final class ListenersPreferencePage extends AbstractRegistrationPreferencePage<ListenerPreferences, ListenerPersistenceData>
 {
 	public ListenersPreferencePage()
 	{
 	}
 
 	@Override
 	final Class<ListenerPersistenceData> type()
 	{
 		return ListenerPersistenceData.class;
 	}
 
 	@Override
 	protected final ListenerLabelProvider createContentProvider()
 	{
 		return new ListenerLabelProvider();
 	}
 
 	@Override
 	protected final ListenerViewerComparator createViewerComparator()
 	{
 		return new ListenerViewerComparator();
 	}
 
 	@Override
 	protected final void makeTableColumns(Table table, TableColumnLayout layout, GC gc)
 	{
 		Tables.createColumn(table, layout, "Listener implementation", gc, 4);
 		Tables.createColumn(table, layout, "Notes",                   gc, 1);
 	}
 
 	private static final class ListenerLabelProvider extends AbstractLabelProvider<ListenerPersistenceData>
 	{
 		ListenerLabelProvider()
 		{
 		}
 	
 		public final String getColumnText(final Object element, final int column)
 		{
 			ListenerPersistenceData data = (ListenerPersistenceData) element;
 	
 			switch (column)
 			{
 				case 0:
 					return data.getListenerClass().getName() + (data.isProvided() ? "" : " (unknown)");
 				case 1:
 					return this.getAnnotations(data);
 				default:
 					throw new IllegalStateException();
 			}
 		}
 	}
 
 	private static final class ListenerViewerComparator extends AbstractViewerComparator
 	{
 		ListenerViewerComparator()
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
 	}
 
 	@Override
 	final ListenerPreferences source()
 	{
 		return ListenerPreferences.getInstance();
 	}
 
 	@Override
 	final Set<ListenerPersistenceData> defaults()
 	{
 		return ListenerPersistenceData.defaults();
 	}
 
 	@Override
 	final void apply()
 	{
 		try
 		{
 			Set<ListenerPersistenceData> data = Registrations.applyRegisteredMark(this.registrations);
 			
 			this.getPreferences().setListenerPersistenceData(data);
 		}
 		catch (ResourceNotRegistredException e)
 		{
 			String message;
 			
 			message  = "Listener registration failed due to one or more unregistered but required resources. ";
			message += "Select only listeners with registered resources.\n\n";
			message += e.getMessage() + ".";
 			
 			this.displayError("Listener registration", message);
 			this.performRefresh();
 		}
 	}
 
 	@Override
 	final void load(final ListenerPreferences preferences)
 	{
 		this.setListenerPreferences(preferences);
 	}
 
 	@Override
 	final void save() throws IOException
 	{
 		this.getListenerPreferences().save();
 	}
 
 	public final void setListenerPreferences(final ListenerPreferences preferences)
 	{
 		this.setPreferences(preferences);
 		
 		this.registrations = preferences.getListenerPersistenceData();
 	}
 
 	public final ListenerPreferences getListenerPreferences()
 	{
 		return this.getPreferences();
 	}
 }
