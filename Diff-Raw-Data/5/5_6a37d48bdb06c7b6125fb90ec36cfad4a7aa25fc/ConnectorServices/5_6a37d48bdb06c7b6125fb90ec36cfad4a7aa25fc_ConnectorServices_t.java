 /**
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  * 
  */
 package org.obeonetwork.dsl.smartdesigner.design.services;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.obeonetwork.dsl.smartdesigner.Diagram;
 import org.obeonetwork.dsl.smartdesigner.GraphicalElement;
 import org.obeonetwork.dsl.smartdesigner.design.util.BasicDiagramUtil;
 
 /**
  * Services for the edges.
  * 
  * @author Stephane Drapeau - Obeo
  * 
  */
 public class ConnectorServices {
 
 	/**
 	 * Returns the label for the edge that links the GraphicalElement
 	 * <code>source</code> with the graphicalElement <code>target</code>.
 	 * 
 	 * @param source
 	 *            The GraphicalElement that is the source of the edge.
 	 * @param target
 	 *            The graphicalElement that is the target of the edge.
 	 * @return the label for the edge that links the GraphicalElement
 	 *         <code>source</code> with the graphicalElement <code>target</code>
 	 *         .
 	 */
 	public String getLabel(GraphicalElement source, GraphicalElement target) {
 		String result = "";
 		boolean first = true;
 		for (EReference ref : source.getSemanticElement().eClass()
 				.getEAllReferences()) {
 			if (source.getSemanticElement().eIsSet(ref)) {
 				if (source.getSemanticElement().eGet(ref) instanceof List) {
 					if (((List) source.getSemanticElement().eGet(ref))
 							.contains(target.getSemanticElement())) {
 						if (first) {
 							first = false;
 						} else {
 							result = result + "\n ";
 						}
 						result = result
 								+ BasicDiagramUtil
 										.splitCamelCase(ref.getName());
 					}
 				} else {
 					if (target.getSemanticElement().equals(
 							source.getSemanticElement().eGet(ref))) {
 						if (first) {
 							first = false;
 						} else {
 							result = result + "\n ";
 						}
 						result = result
 								+ BasicDiagramUtil
 										.splitCamelCase(ref.getName());
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Check if the connection between <code>source</code> and
 	 * <code>target</code> is bidirectional. The connection is bidirectional if
 	 * it exists a reference from the semantic element of <code>source</code> to
 	 * the semantic element of <code>target</code> and a reference from the
 	 * semantic element of <code>target</code> to the semantic element of
 	 * <code>source</code>.
 	 * 
 	 * @param source
 	 *            First GraphicalElement to consider.
 	 * @param target
 	 *            Second GraphicalElement to consider.
 	 * @return True if it exists a reference from the semantic element of
 	 *         <code>source</code> to the semantic element of
 	 *         <code>target</code> and a reference from the semantic element of
 	 *         <code>target</code> to the semantic element of
 	 *         <code>source</code>. False otherwise.
 	 */
 	public boolean isBidirectionalConnector(GraphicalElement source,
 			GraphicalElement target) {
 		boolean result = existRelation(source.getSemanticElement(),
 				target.getSemanticElement());
 		if (result) {
 			result = existRelation(target.getSemanticElement(),
 					source.getSemanticElement());
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the list of GraphicalElement in relation with
 	 * <code>graphicalElement</code>. <code>graphicalElement</code> is in
 	 * relation with an other GraphicalElement B if :
 	 * <ul>
 	 * <li>a reference exists from <code>graphicalElement</code> to B and
 	 * compare(<code>graphicalElement</code>
 	 * .getSemanticElement(),B.getSemanticElement()>0).</li>
 	 * <li>no reference exists from <code>graphicalElement</code> to B and
 	 * compare(<code>graphicalElement</code>
 	 * .getSemanticElement(),B.getSemanticElement()>0) and a reference exists
 	 * from B to <code>graphicalElement</code>.</li>
 	 * </ul>
 	 * 
 	 * @param graphicalElement
 	 *            The GraphicalElement for which we are looking the list of
 	 *            GraphicalElement in relation with.
 	 * @return the list of GraphicalElement in relation with
 	 *         <code>graphicalElement</code>.
 	 */
 	public List<GraphicalElement> getRelatedElements(
 			GraphicalElement graphicalElement) {
 		Diagram diagram = BasicDiagramUtil.getDiagram(graphicalElement);
 		if (diagram == null) {
 			return new ArrayList<GraphicalElement>();
 		}
 
 		Set<GraphicalElement> resultSet = new HashSet<GraphicalElement>();
 		Iterator<GraphicalElement> it = diagram.getElements().iterator();
 		while (it.hasNext()) {
 			GraphicalElement ge = it.next();
 			if (ge != graphicalElement) {
 				EObject sourceObject = graphicalElement.getSemanticElement();
 				EObject targetObject = ge.getSemanticElement();
 				if (compare(sourceObject, targetObject) > 0 && //
 						(//
 						(//
 						existRelation(sourceObject, targetObject)//
 						) //
 						|| //
 						(//
 						!existRelation(sourceObject, targetObject) && //
 						existRelation(targetObject, sourceObject)//
 						)//
 						)//
 				) {
 					resultSet.add(ge);
 				}
 			}
 		}
 		return new ArrayList<GraphicalElement>(resultSet);
 	}
 
 	/**
 	 * Checks if a reference exists between <code>sourceObject</code> and
 	 * <code>targetObject</code>.
 	 * 
 	 * @param sourceObject
 	 *            EObject for which we are looking for a reference to
 	 *            <code>targetObject</code>.
 	 * @param targetObject
 	 *            EObject for which we are looking for a reference from
 	 *            <code>sourceObject</code>.
 	 * @return True if a reference exists between <code>sourceObject</code> and
 	 *         <code>targetObject</code>, false otherwise.
 	 */
 	public boolean existRelation(EObject sourceObject, EObject targetObject) {
 		boolean found = false;
 		Iterator<EReference> refs = sourceObject.eClass().getEAllReferences()
 				.iterator();
 		while (!found && refs.hasNext()) {
 			EReference ref = refs.next();
 			if (sourceObject.eGet(ref) instanceof List) {
 				if (((List) sourceObject.eGet(ref)).contains(targetObject)) {
 					found = true;
 				}
 			} else if (targetObject.equals(sourceObject.eGet(ref))) {
 				found = true;
 			}
 		}
 		return found;
 	}
 
 	/**
 	 * A comparison function used to compare <code>sourceObject</code> and
 	 * <code>targetObject</code>.
 	 * 
 	 * @param sourceObject
 	 *            First object to compare.
 	 * @param targetObject
 	 *            Second object to compare.
 	 * @return an int, result of the comparison of <code>sourceObject</code> and
 	 *         <code>targetObject</code>.
 	 */
 	private int compare(EObject sourceObject, EObject targetObject) {
		int result = ((CDOObject) sourceObject).cdoID().compareTo(
				((CDOObject) targetObject).cdoID());
 		return result;
 	}
 
 }
