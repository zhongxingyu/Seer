 package org.eclipse.uml2.diagram.common.editpolicies;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
 import org.eclipse.gmf.runtime.emf.type.core.commands.SetValueCommand;
 import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
 import org.eclipse.gmf.runtime.notation.LineStyle;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.uml2.diagram.common.preferences.UMLPreferencesConstants;
 import org.eclipse.uml2.uml.Port;
 import org.eclipse.uml2.uml.UMLPackage;
 
 public class PortVisualEffectEditPolicy extends AbstractVisualEffectEditPolicy {
 
 	@Override
 	protected boolean shouldHandleNotificationEvent(Notification event) {
 		return UMLPackage.eINSTANCE.getTypedElement_Type() == event.getFeature();
 	}
 
 	@Override
 	protected void installVisualEffect() {
 		ensureHasStyle(NotationPackage.eINSTANCE.getLineStyle());
 	}
 
 	@Override
 	protected void refreshVisualEffect() {
 		EObject semanticHost = getSemanticHost();
 		if (false == semanticHost instanceof Port) {
 			return;
 		}
 		Port port = (Port) semanticHost;
 		IGraphicalEditPart editPart = getHostImpl();
 		View view = editPart.getNotationView();
 		LineStyle lineStyle = (LineStyle) view.getStyle(NotationPackage.eINSTANCE.getLineStyle());
 
 		boolean hasType = port.getType() != null;
 		IPreferenceStore store = (IPreferenceStore) editPart.getDiagramPreferencesHint().getPreferenceStore();
 		int highlightColor = getColor(store, UMLPreferencesConstants.HIGHLIGHT_COLOR);
 		boolean isMarkedInvalid = (lineStyle.getLineColor() == highlightColor);
 
 		if (hasType && isMarkedInvalid) {
 			int usualColor = getColor(store, IPreferenceConstants.PREF_LINE_COLOR);
 			setLineColor(editPart, lineStyle, usualColor);
 		} else if (!hasType && !isMarkedInvalid) {
 			setLineColor(editPart, lineStyle, highlightColor);
 		}
 	}
 
 	private int getColor(IPreferenceStore store, String name) {
 		RGB rgb = PreferenceConverter.getColor(store, name);
		return FigureUtilities.RGBToInteger(rgb).intValue();
 	}
 
 	private void setLineColor(IGraphicalEditPart editPart, LineStyle lineStyle, int color) {
 		SetRequest request = new SetRequest(editPart.getEditingDomain(), lineStyle,//
 				NotationPackage.eINSTANCE.getLineStyle_LineColor(), color);
 		executeCommand(new ICommandProxy(new SetValueCommand(request)));
 	}
 }
