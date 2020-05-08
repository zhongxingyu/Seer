 package com.worldline.gmf.propertysections.core;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.transaction.DemultiplexingListener;
 import org.eclipse.emf.transaction.NotificationFilter;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.util.TransactionUtil;
 import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
 
 /**
  * This class defines a generic AbstractSection. A AbstractSection is an object that will
  * directly added to the Properties View background
  * 
  * @author mvanbesien
  * @version $Revision: 1.2 $
  * @since $Date: 2008/02/05 16:19:01 $
  * 
  */
 public abstract class AbstractSection extends AbstractPropertySection {
 
 	/**
 	 * Map containing the Zones to display in this section
 	 */
 	private Map<String, AbstractZone> zones;
 
 	/**
 	 * Instance of Widget Factory
 	 */
 	protected TabbedPropertySheetWidgetFactory widgetFactory;
 
 	/**
 	 * Composite standing for the background of this section
 	 */
 	private Composite backGround = null;
 
 	/**
 	 * FormData, used to define the Zones position among others.
 	 */
 	protected FormData fData;
 
 	/**
 	 * TransactionalEditingDomain retrieved from current Opened Editor
 	 */
 	private TransactionalEditingDomain editingDomain;
 
 	/**
 	 * Selected EObject
 	 */
 	private EObject eObject;
 
 	/**
 	 * Selected Graphical Edit Part
 	 */
 	private AbstractGraphicalEditPart editPart;
 
 	/**
 	 * Listener used to keep bijection between the properties view and the
 	 * diagram
 	 */
 	protected DemultiplexingListener eventListener = new DemultiplexingListener(getFilter()) {
 
 		protected void handleNotification(TransactionalEditingDomain domain,
 				Notification notification) {
 			if (notification.getFeature() != null)
 				update();
 		}
 	};
 
 	/**
 	 * Method used to update all the zoned included in this section
 	 */
 	private final void update() {
 		Iterator<String> i = zones.keySet().iterator();
 		while (i.hasNext()) {
 			zones.get(i.next()).refreshZoneAndDiagram();
 		}
 	}
 
 	/**
 	 * @return AbstractFilter used for the Listener in charge to keep bijection
 	 */
 	public NotificationFilter getFilter() {
 		return NotificationFilter.createEventTypeFilter(Notification.SET).or(
 				NotificationFilter.createEventTypeFilter(Notification.UNSET).and(
 						NotificationFilter.createNotifierTypeFilter(EObject.class)));
 	}
 
 	/**
 	 * This method is called when a AbstractSection instance is about to be drawn in the
 	 * Properties View. This method is not intented to be used directly by the
 	 * user.
 	 */
 	@Override
 	public final void aboutToBeShown() {
 		super.aboutToBeShown();
 		editingDomain.addResourceSetListener(eventListener);
 	}
 
 	/**
 	 * This method is called when a AbstractSection instance is about to be removed in
 	 * the Properties View. This method is not intented to be used directly by
 	 * the user.
 	 */
 	@Override
 	public final void aboutToBeHidden() {
 		super.aboutToBeHidden();
 		editingDomain.removeResourceSetListener(eventListener);
 	}
 
 	/**
 	 * This method is called when a AbstractSection is being drawn. This method contains
 	 * the creation od the section. This method is not intented to be used
 	 * directly by the user
 	 */
 	@Override
 	public final void createControls(Composite parent,
 			TabbedPropertySheetPage aTabbedPropertySheetPage) {
 		super.createControls(parent, aTabbedPropertySheetPage);
 
 		this.zones = new HashMap<String, AbstractZone>();
 
 		this.widgetFactory = super.getWidgetFactory();
 
 		backGround = getWidgetFactory().createFlatFormComposite(parent);
 		FormLayout layout = new FormLayout();
 		backGround.setLayout(layout);
 
 		initParts();
 		addPartsToSection();
 		abstractAddLayoutsToParts();
 		addListenersToParts();
 	}
 
 	/**
 	 * Added an AbstractItem, or a AbstractZone to this section.
 	 */
 	protected final void addPartsToSection() {
 		Iterator<String> ite = zones.keySet().iterator();
 		while (ite.hasNext()) {
 			zones.get(ite.next()).addItemsToZone();
 		}
 	}
 
 	/**
 	 * Method which purpose is to add Layouts to Items or Zones in this section
 	 */
 	protected final void abstractAddLayoutsToParts() {
 		Iterator<String> ite = zones.keySet().iterator();
 		while (ite.hasNext()) {
 			zones.get(ite.next()).addLayoutsToItems();
 		}
 		addLayoutsToParts();
 	}
 
 	/**
 	 * Method which purpose is to add Listeners to Items or Zones in this
 	 * section
 	 */
 
 	protected final void addListenersToParts() {
 		Iterator<String> ite = zones.keySet().iterator();
 		while (ite.hasNext()) {
 			zones.get(ite.next()).addListenersToItems();
 		}
 	}
 
 	/**
 	 * Method which purpose is to create and initialize the graphical elements
 	 * to be displayed in this AbstractSection
 	 */
 	protected abstract void initParts();
 
 	/**
 	 * Method which purpose is to create and add Layouts to the graphical
 	 * elements to be displayed in this AbstractSection
 	 */
 	protected abstract void addLayoutsToParts();
 
 	/**
 	 * This method sets the input of the AbstractSection. The input is the filtered
 	 * object from the diagram, which properties have to be displayed in this
 	 * Properties View. This method is not intended to be called directly by the
 	 * user
 	 */
 	@Override
 	public final void setInput(IWorkbenchPart part, ISelection selection) {
 		super.setInput(part, selection);
 		if (part instanceof DiagramDocumentEditor) {
			editingDomain = ((DiagramDocumentEditor) part).getEditingDomain();
 		}
 
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection treeSelection = (IStructuredSelection) selection;
 			eObject = null;
 			editPart = null;
 			if (treeSelection.getFirstElement() instanceof EObject)
 				eObject = (EObject) treeSelection.getFirstElement();
 			else if (treeSelection.getFirstElement() instanceof AbstractGraphicalEditPart) {
 				AbstractGraphicalEditPart myGEP = (AbstractGraphicalEditPart) treeSelection
 						.getFirstElement();
 
 				editPart = myGEP;
 
 				Object model = myGEP.getModel();
 				if (model instanceof View) {
 					eObject = ((View) model).getElement();
 				}
 			}
 		}
 		
 		if (editingDomain == null && eObject != null)
 			editingDomain = TransactionUtil.getEditingDomain(eObject);
 		
 		updatePartsValues();
 	}
 
 	/**
 	 * Method which purpose is to update values of the graphical elements
 	 * contained by this AbstractSection
 	 * 
 	 */
 	protected final void updatePartsValues() {
 		Iterator<String> ite = zones.keySet().iterator();
 		while (ite.hasNext()) {
 			AbstractZone g = zones.get(ite.next());
 			if (g != null) {
 				g.init(getEObject(), getEditPart(), getEditingDomain());
 				g.updateItemsValues();
 			}
 		}
 
 	}
 
 	/**
 	 * @return Transactional Editing Domain from the current editor
 	 */
 	protected final TransactionalEditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	/**
 	 * @return Selected Abstract Graphical Edit Part
 	 */
 	protected final AbstractGraphicalEditPart getEditPart() {
 		return editPart;
 	}
 
 	/**
 	 * @return Selected EObject
 	 */
 	protected final EObject getEObject() {
 		return eObject;
 	}
 
 	/**
 	 * @return the backGround of this AbstractSection
 	 */
 	public final Composite getBackGround() {
 		return backGround;
 	}
 
 	/**
 	 * @return Map containing Zones (with their ID) from this section
 	 */
 	public final Map<String, AbstractZone> getZones() {
 		return zones;
 	}
 
 	/**
 	 * Retrieves the zone contained by this AbstractSection from its ID.
 	 * 
 	 * @param key :
 	 *            ID of the AbstractZone
 	 * @return : the AbstractZone
 	 */
 	public final Composite getZone(String key) {
 		return getZones().get(key).getZone();
 	}
 
 }
