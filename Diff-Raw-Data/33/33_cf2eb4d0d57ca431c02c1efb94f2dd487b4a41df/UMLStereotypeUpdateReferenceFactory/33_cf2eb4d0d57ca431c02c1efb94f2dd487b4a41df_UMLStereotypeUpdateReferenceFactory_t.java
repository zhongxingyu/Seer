 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.uml2.diff.internal.extension.profile;
 
 import org.eclipse.emf.compare.diff.metamodel.AbstractDiffExtension;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.UpdateReference;
 import org.eclipse.emf.compare.uml2.diff.UML2DiffEngine;
 import org.eclipse.emf.compare.uml2.diff.internal.extension.AbstractDiffExtensionFactory;
 import org.eclipse.emf.compare.uml2diff.UML2DiffFactory;
 import org.eclipse.emf.compare.uml2diff.UMLStereotypeUpdateReference;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.uml2.uml.util.UMLUtil;
 
 /**
  * Factory for UMLStereotypeAttributeChangeLeftTarget.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class UMLStereotypeUpdateReferenceFactory extends AbstractDiffExtensionFactory {
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param engine
 	 *            The UML2 difference engine.
 	 */
 	public UMLStereotypeUpdateReferenceFactory(UML2DiffEngine engine) {
 		super(engine);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.uml2.diff.internal.extension.IDiffExtensionFactory#handles(org.eclipse.emf.compare.diff.metamodel.DiffElement)
 	 */
 	public boolean handles(DiffElement input) {
 		if (input instanceof UpdateReference) {
 			final EObject left = ((UpdateReference)input).getLeftElement();
 			final EObject right = ((UpdateReference)input).getRightElement();
 			final EObject leftBase = UMLUtil.getBaseElement(left);
 			final EObject rightBase = UMLUtil.getBaseElement(right);
 			return leftBase != null && rightBase != null;
 		}
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.uml2.diff.internal.extension.IDiffExtensionFactory#create(org.eclipse.emf.compare.diff.metamodel.DiffElement,
 	 *      org.eclipse.emf.ecore.util.EcoreUtil.CrossReferencer)
 	 */
 	public AbstractDiffExtension create(DiffElement input, EcoreUtil.CrossReferencer crossReferencer) {
 		final UpdateReference updateReference = (UpdateReference)input;
 		final EObject leftElement = updateReference.getLeftElement();
 		final EObject rightElement = updateReference.getRightElement();
 		final EObject leftBase = UMLUtil.getBaseElement(leftElement);
 		final EObject rightBase = UMLUtil.getBaseElement(rightElement);
 
 		final UMLStereotypeUpdateReference ret = UML2DiffFactory.eINSTANCE
 				.createUMLStereotypeUpdateReference();
 
 		ret.setStereotype(UMLUtil.getStereotype(rightElement));
 		ret.setRemote(input.isRemote());
 
 		ret.setReference(updateReference.getReference());
 		ret.setLeftElement(leftBase);
 		ret.setRightElement(rightBase);

		setTargets(updateReference, ret);
 
 		ret.getHideElements().add(input);
 		ret.getRequires().add(input);
 
 		return ret;
 	}
 
 	@Override
 	public DiffElement getParentDiff(DiffElement input, EcoreUtil.CrossReferencer crossReferencer) {
 		final EObject right = ((UpdateReference)input).getRightElement();
 		final EObject rightBase = UMLUtil.getBaseElement(right);
 
 		final DiffModel rootDiffGroup = (DiffModel)EcoreUtil.getRootContainer(input);
 
 		return findOrCreateDiffGroup(rootDiffGroup, rightBase, crossReferencer);
 	}

	/**
	 * Sets the target of the given reference (The generic diff engine finds a target in the model, but
	 * stereotype values and features should apply to the "base" element of a diff).
	 * 
	 * @param updateReference
	 *            {@link UpdateReference} from DiffModel.
	 * @param ret
	 *            {@link UpdateReference} which will be modified and return.
	 * @see UMLUtil#getBaseElement(EObject)
	 */
	protected void setTargets(final UpdateReference updateReference, final UpdateReference ret) {
		final EStructuralFeature reference = updateReference.getReference();
		final EObject diffRightTarget = updateReference.getRightTarget();

		final EObject rightTargetBase = UMLUtil.getBaseElement(diffRightTarget);
		if (rightTargetBase != null && rightTargetBase.eGet(reference) != null) {
			ret.setRightTarget(rightTargetBase);
		}

		final EObject diffLeftTarget = updateReference.getLeftTarget();
		final EObject leftTargetBase = UMLUtil.getBaseElement(diffLeftTarget);
		if (leftTargetBase != null && leftTargetBase.eGet(reference) != null) {
			ret.setLeftTarget(leftTargetBase);
		}
	}
 }
