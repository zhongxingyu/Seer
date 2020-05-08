 package de.ptb.epics.eve.viewer.views.messagesview;
 
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 
 import de.ptb.epics.eve.viewer.Activator;
 import de.ptb.epics.eve.viewer.messages.TypeViewerComparator;
 
 /**
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public final class MessagesView extends ViewPart {
 
 	// the only element in this view is a table viewer for the messages
 	private TableViewer tableViewer;
 	
 	private boolean sort;
 	private TypeViewerComparator typeViewerComparator;
 	private Image sortImage;
 	
 	private IMemento memento;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		this.memento = memento;
 		super.init(site, memento);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 
 		this.tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | 
 				SWT.V_SCROLL);
 
 		// the first column contains the time
 		TableViewerColumn timeColumn = new TableViewerColumn(this.tableViewer,
 				SWT.LEFT);
 		timeColumn.getColumn().setText("Time");
 		timeColumn.getColumn().setWidth(180);
 
 		// second column: source (the sender of the message)
 		TableViewerColumn sourceColumn = new TableViewerColumn(this.tableViewer, 
 				SWT.LEFT);
 		sourceColumn.getColumn().setText("Source");
 		sourceColumn.getColumn().setWidth(80);
 
 		// third column: the type (e.g. I for Info or E for Error)
 		TableViewerColumn typeColumn = new TableViewerColumn(this.tableViewer, 
 				SWT.LEFT);
 		typeColumn.getColumn().setText("");
 		typeColumn.getColumn().setWidth(25);
 		typeColumn.getColumn().addSelectionListener(
 				new TypeColumnSelectionListener());
 		
 		// fourth column: the text (contents) of the message
 		TableViewerColumn messageColumn = new TableViewerColumn(
 				this.tableViewer, SWT.LEFT);
 		messageColumn.getColumn().setText("Message");
 		messageColumn.getColumn().setWidth(300);
 
 		this.tableViewer.getTable().setHeaderVisible(true);
 		this.tableViewer.getTable().setLinesVisible(true);
 
 		// set provider classes which fill the tables labels and content
 		ContentProvider cp = new ContentProvider();
 		this.tableViewer.setContentProvider(cp);
 		this.tableViewer.setLabelProvider(new LabelProvider());
 		
 		// if a new scan arrives, old messages are removed
 		Activator.getDefault().getXMLFileDispatcher()
 				.addObserver(cp);
 
 		// the MessageContainer is the input object of the table viewer
 		this.tableViewer.setInput(Activator.getDefault().
 				getMessagesContainer());
 		
 		this.sortImage = Activator.getDefault().getImageRegistry()
 				.get("SORTARROW");
 		this.sort = false;
 		this.typeViewerComparator = new TypeViewerComparator();
 		
 		this.restoreState();
 	}
 
 	/*
 	 * 
 	 */
 	private void restoreState() {
		if (this.memento == null) {
			return;
		}
 		this.sort = this.memento.getBoolean("sort") != null 
 					? this.memento.getBoolean("sort")
 					: false;


 		if (this.sort) {
 			tableViewer.getTable().getColumn(2).setImage(sortImage);
 			tableViewer.setComparator(typeViewerComparator);
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void saveState(IMemento memento) {
 		memento.putBoolean("sort", this.sort);
 		super.saveState(memento);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 		this.tableViewer.getTable().setFocus();
 	}
 	
 	/* ******************************************************************** */
 	/* *************************** Listeners ****************************** */
 	/* ******************************************************************** */
 
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of 
 	 * typeColumn.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.4
 	 */
 	private class TypeColumnSelectionListener implements SelectionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			if (sort) {
 				tableViewer.getTable().getColumn(2).setImage(null);
 				tableViewer.setComparator(null);
 				sort = false;
 			} else {
 				tableViewer.getTable().getColumn(2).setImage(sortImage);
 				tableViewer.setComparator(typeViewerComparator);
 				tableViewer.getTable().showItem(
 						tableViewer.getTable().getItem(0));
 				sort = true;
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 	}
 }
