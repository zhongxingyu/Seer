 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.compare.match;
 
 import org.eclipse.emf.compare.utils.DiffUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.compare.debug.DebugUtils;
 import org.eclipse.mylyn.docs.intent.compare.match.EditionDistance.CountingDiffEngine;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionBloc;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionUnit;
 import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
 import org.eclipse.mylyn.docs.intent.markup.markup.MarkupPackage;
 import org.eclipse.mylyn.docs.intent.markup.markup.Text;
 import org.eclipse.mylyn.docs.intent.markup.serializer.WikiTextSerializer;
 import org.eclipse.mylyn.docs.intent.serializer.IntentSerializer;
 
 /**
  * An implementation of a diff engine which count and measure the detected changes into Intent documents.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentCountingDiffEngine extends CountingDiffEngine {
 
 	/**
 	 * We set a default distance here because if maxDistance is 0 results are invalid.
 	 */
	private static final int DEFAULT_DISTANCE = 200;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param editionDistance
 	 *            for instanciation
 	 * @param maxDistance
 	 *            the max distance
 	 */
 	public IntentCountingDiffEngine(
 			org.eclipse.mylyn.docs.intent.compare.match.EditionDistance editionDistance, int maxDistance) {
 		editionDistance.super(maxDistance);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.compare.match.EditionDistance.CountingDiffEngine#measureDifferences(org.eclipse.emf.ecore.EObject,
 	 *      org.eclipse.emf.ecore.EObject)
 	 */
 	@Override
 	public int measureDifferences(EObject a, EObject b) {
		// TODO remove debug instructions when ready

 		int serializationDistance = getSerializationDistance(a, b);
 		int uriDistance = getStringDistance(helper.getURI(a).fragment(), helper.getURI(b).fragment());
 
		int distance = (int)((uriDistance + serializationDistance) / 2);

		// DEBUG
 		String aString = DebugUtils.elementToReadableString(a);
 		String bString = DebugUtils.elementToReadableString(b);
 		if (aString != null && bString != null) {
 			if (a instanceof Text) {
 				System.out.println(distance + "\t" + aString + " <=> " + bString);
 			}
 		}
 		return distance;
 	}
 
 	/**
 	 * Returns the distance between document elements by comparing their serialization.
 	 * 
 	 * @param eObjA
 	 *            the first element
 	 * @param eObjB
 	 *            the second element
 	 * @return the distance between two strings
 	 */
 	private static int getSerializationDistance(EObject eObjA, EObject eObjB) {
		int res = DEFAULT_DISTANCE;
 		if (eObjA instanceof ModelingUnit || eObjA instanceof DescriptionUnit
 				|| eObjA instanceof IntentStructuredElement) {
 			String serializedA = new IntentSerializer().serialize(eObjA);
 			String serializedB = new IntentSerializer().serialize(eObjB);
 			res = getStringDistance(serializedA, serializedB);
 		} else if (eObjA.eClass().getEPackage().equals(MarkupPackage.eINSTANCE)) {
 			String serializedA = new WikiTextSerializer().serialize(eObjA);
 			String serializedB = new WikiTextSerializer().serialize(eObjB);
 			res = getStringDistance(serializedA, serializedB);
 		} else if (eObjA instanceof DescriptionBloc) {
 			res = getSerializationDistance(((DescriptionBloc)eObjA).getDescriptionBloc(),
 					((DescriptionBloc)eObjB).getDescriptionBloc());
 		} else {
 			System.out.println("UNABLE TO SERIALIZE " + eObjA.eClass().getName());
 		}
 		return res;
 	}
 
 	/**
 	 * Returns the distance between two strings.
 	 * 
 	 * @param a
 	 *            the first string
 	 * @param b
 	 *            the second string
 	 * @return the distance between two strings
 	 */
 	private static int getStringDistance(String a, String b) {
		int res = DEFAULT_DISTANCE;
 		if (a != null && b != null) {
 			double sizeCoeff = 1 - (2d * Math.abs(a.length() - b.length())) / (a.length() + b.length());
 			double diceCoefficient = DiffUtil.diceCoefficient(a, b);
			double average = (diceCoefficient + sizeCoeff) / 2;
			res = (int)((1 - average) * DEFAULT_DISTANCE);
 		} else {
 			if (a == null && b == null) {
 				res = 0;
 			}
 		}
 		return res;
 	}
 }
