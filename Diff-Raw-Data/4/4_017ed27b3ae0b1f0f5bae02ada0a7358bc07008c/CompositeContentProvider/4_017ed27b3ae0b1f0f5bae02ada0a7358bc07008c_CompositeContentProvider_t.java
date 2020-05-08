 package de.ptb.epics.eve.editor.views.scanmoduleview;
 
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 /**
  * Base class for content provider of table viewers used in
  * {@link ActionComposite}s. Clients using this class have to extend it and 
  * override {@link #getElements(Object)}.
  * <p>
  * Selects a newly added item of the table or the first one if one was removed.
  * 
  * @author Marcus Michalsky
  * @since 1.2
  */
 public abstract class CompositeContentProvider implements
 		IStructuredContentProvider, IModelUpdateListener {
 
 	private TableViewer currentViewer;
 	private int elementCount;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(ModelUpdateEvent modelUpdateEvent) {
 		this.currentViewer.refresh();
 		final int itemCount = this.currentViewer.getTable().getItemCount();
 		if (currentViewer.getTable().getItemCount() > elementCount) {
 			// element added -> select it
 			currentViewer.setSelection(
 					new StructuredSelection(currentViewer
 							.getElementAt(itemCount - 1)), true);
 		} else if (itemCount < elementCount && itemCount > 0) {
 			// element removed, but still >= 1 present -> select the first
		//	this.currentViewer.setSelection(new StructuredSelection(
		//			this.currentViewer.getElementAt(0)), true);
 		}
 		elementCount = currentViewer.getTable().getItemCount();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		this.currentViewer = (TableViewer) viewer;
 		if (oldInput != null) {
 			((ScanModule) oldInput).removeModelUpdateListener(this);
 		}
 		if (newInput != null) {
 			((ScanModule) newInput).addModelUpdateListener(this);
 			elementCount = this.currentViewer.getTable().getItemCount();
 		}
 	}
 }
