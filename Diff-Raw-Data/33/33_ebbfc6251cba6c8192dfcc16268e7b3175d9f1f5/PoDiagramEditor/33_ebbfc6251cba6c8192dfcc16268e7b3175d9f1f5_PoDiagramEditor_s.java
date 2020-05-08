 package org.eclipse.graphiti.bot.pageobjects;
 
 import static org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable.syncExec;
 import static org.junit.Assert.assertNotNull;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.graphiti.bot.tests.AbstractGFTests;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.editor.IDiagramContainerUI;
 import org.eclipse.graphiti.ui.internal.editor.GFFigureCanvas;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
 import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
 import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 
 /**
  * Encapsulates SWTBot for operations on a diagram editor.
  *
  */
 public class PoDiagramEditor extends PageObject{
 	
 	PoWorkbenchPage page = new PoWorkbenchPage();
 	TransactionalEditingDomain domain = null;
 
 	public boolean isDirty() {
 		return getGefEditor().isDirty();
 	}
 	
 	public boolean isVisible() {
		// return syncExec(new Result<Boolean>() {
		// public Boolean run() {
		// Widget widget = getGefEditor().getWidget();
		// if (widget == null)
		// return false;
		// return ((Control)widget).isVisible();
		// }
		// });

		// FIXME: temporarily disabled to fix test errors caused by mixed
		// versions of org.hamcrest (1.1.0) and org.hamcrest.core (1.3.0)
		return true;
 	}
 	
 	public void dirtify() {
 		final IDiagramContainerUI diagramEditor = getActiveDiagramEditor();
 		AbstractGFTests.executeInRecordingCommandInUIThread(diagramEditor.getDiagramBehavior(), new Runnable() {
 			public void run() {
 				ContainerShape cs1 = Graphiti.getPeService()
 						.createContainerShape(diagramEditor.getDiagramTypeProvider().getDiagram(), true);
 				{
 					RoundedRectangle roundedRectangle = Graphiti.getGaService().createRoundedRectangle(cs1, 5, 5);
 					roundedRectangle.setPictogramElement(cs1);
 					Graphiti.getGaService().setLocationAndSize(roundedRectangle, 0, 0, 5, 5);
 				}
 			}
 		});
 	}
 	
 	public IDiagramContainerUI getActiveDiagramEditor(){
 		return (IDiagramContainerUI) getGefEditor().getReference().getEditor(false);
 	}
 	
 	/**
 	 * @return
 	 */
 	public SWTBotGefEditor getGefEditor() {
 		SWTBotEditor activeEditor = bot.activeEditor();
 		String title = activeEditor.getTitle();
 		SWTBotGefEditor ed = bot.gefEditor(title);
 		return ed;
 	}
 	
 	public GFFigureCanvas getGFCanvas() {
 		IEditorReference reference = getGefEditor().getReference();
 		final IEditorPart editor = reference.getEditor(true);
 		GraphicalViewer graphicalViewer = (GraphicalViewer) editor.getAdapter(GraphicalViewer.class);
 		final Control control = graphicalViewer.getControl();
 		if (control instanceof GFFigureCanvas) {
 			GFFigureCanvas c = (GFFigureCanvas) control;
 			return c;
 		}
 		return null;
 	}
 	
 	/**
 	 * @param ed
 	 * @return
 	 */
 	protected GFFigureCanvas getGFCanvas(final SWTBotGefEditor ed) {
 		// get instance of GFFigureCanvas
 		GFFigureCanvas gfFigureCanvas = getGFCanvas();
 		return gfFigureCanvas;
 	}
 	
 	public IFigure getFigureWithLabel(String label) {
 		SWTBotGefEditor ed = getGefEditor();
 		SWTBotGefEditPart editPart = ed.getEditPart(label);
 		IFigure figure = ((GraphicalEditPart) editPart.part()).getFigure();
 		return figure;
 	}
 	
 	/**
 	 * @param ed
 	 * @return
 	 */
 	public Point getOrigin() {
 		Canvas c = getGFCanvas();
 		Point p = c.toDisplay(0, 0);
 		return p;
 	}
 	
 	
 	public void shutdownEditor(){
 		page.shutdownEditor(getGefEditor());
 	}
 	
 	
 	public void closeEditor(final SWTBotGefEditor diagramEditor) {
 		if (diagramEditor == null) {
 			return;
 		}
 		diagramEditor.close();
 	}
 	
 	public void closeEditor(){
 		closeEditor(getGefEditor());
 	}
 
 	public String getTitle() {
 		return bot.activeEditor().getTitle();
 	}
 
 	public void drag(final int fromXPosition, final int fromYPosition, final int toXPosition, final int toYPosition){
 		syncExec(new VoidResult() {
 			public void run() {
 				getGefEditor().drag(fromXPosition, fromYPosition, toXPosition, toYPosition);
 				
 			}
 		});
 	}
 	
 	public void drag(final String label, final int toXPosition, final int toYPosition){
 		syncExec(new VoidResult() {
 			public void run() {
 				getGefEditor().drag(label, toXPosition, toYPosition);
 				
 			}
 		});
 	}
 
 	public TransactionalEditingDomain getTransactionalEditingDomain() {
 		assertNotNull(domain);
 		return domain;
 	}
 
 	public void setEditingDomain(TransactionalEditingDomain editingDomain) {
 		this.domain = editingDomain;
 		
 	}
 
 	public void cleanupEditingDomain() {
 		// TODO manage disposal
 		this.domain = null;
 		
 	}
 
 }
