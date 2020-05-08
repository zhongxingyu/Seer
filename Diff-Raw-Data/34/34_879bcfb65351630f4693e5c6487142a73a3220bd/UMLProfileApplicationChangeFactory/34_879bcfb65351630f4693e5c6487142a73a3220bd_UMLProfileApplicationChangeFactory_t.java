 /*******************************************************************************
  * Copyright (c) 2011, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.uml2.internal.postprocessor.extension.profile;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.uml2.internal.ProfileApplicationChange;
 import org.eclipse.emf.compare.uml2.internal.StereotypeApplicationChange;
 import org.eclipse.emf.compare.uml2.internal.UMLCompareFactory;
 import org.eclipse.emf.compare.uml2.internal.UMLDiff;
 import org.eclipse.emf.compare.uml2.internal.postprocessor.extension.AbstractDiffExtensionFactory;
 import org.eclipse.emf.compare.utils.MatchUtil;
 import org.eclipse.emf.compare.utils.ReferenceUtil;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.uml2.uml.Element;
 import org.eclipse.uml2.uml.ProfileApplication;
 import org.eclipse.uml2.uml.Stereotype;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.internal.impl.ProfileApplicationImpl;
 
 /**
  * Factory for UMLProfileApplicationAddition.
  * 
  * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
  */
 public class UMLProfileApplicationChangeFactory extends AbstractDiffExtensionFactory {
 
 	private static final String UML_VERSIONS = "http://www.eclipse.org/uml2/\\d.0.0/UML";
 
 	public Class<? extends UMLDiff> getExtensionKind() {
 		return ProfileApplicationChange.class;
 	}
 
 	@Override
 	protected UMLDiff createExtension() {
 		return UMLCompareFactory.eINSTANCE.createProfileApplicationChange();
 	}
 
 	@Override
 	protected EObject getDiscriminantFromDiff(Diff input) {
 		EObject result = null;
 		final DifferenceKind kind = getRelatedExtensionKind(input);
 		if (kind == DifferenceKind.ADD || kind == DifferenceKind.DELETE) {
 			return ((ReferenceChange)input).getValue();
 		} else if (kind == DifferenceKind.CHANGE) {
 			final EObject container = MatchUtil.getContainer(input.getMatch().getComparison(), input);
 			return getDiscriminantForChanges(container);
 		}
 		return result;
 	}
 
 	private ProfileApplication getDiscriminantForChanges(EObject container) {
 		// FIXME Have to manage a list of ProfileApplication from a Profile and choose the correct one
 		if (container instanceof ProfileApplication) {
 			return (ProfileApplication)container;
 		} else if (container instanceof EAnnotation && container.eContainer() instanceof ProfileApplication) {
 			return (ProfileApplication)container.eContainer();
 		}
 		// } else if (container instanceof Profile) {
 		// final Setting setting = getInverseReferences(container,
 		// new Predicate<EStructuralFeature.Setting>() {
 		// public boolean apply(EStructuralFeature.Setting currentSetting) {
 		// return currentSetting.getEStructuralFeature() ==
 		// UMLPackage.Literals.PROFILE_APPLICATION__APPLIED_PROFILE;
 		// }
 		// });
 		// return (ProfileApplication)setting.getEObject();
 		// } else if (container instanceof EPackage) {
 		// final Setting setting = getInverseReferences(container,
 		// new Predicate<EStructuralFeature.Setting>() {
 		// public boolean apply(EStructuralFeature.Setting currentSetting) {
 		// return currentSetting.getEStructuralFeature() == EcorePackage.Literals.EANNOTATION__REFERENCES
 		// && currentSetting.getEObject().eContainer() instanceof ProfileApplication;
 		// }
 		// });
 		// return (ProfileApplication)((EAnnotation)setting.getEObject()).eContainer();
 		// }
 		return null;
 	}
 
 	@Override
 	protected List<EObject> getPotentialChangedValuesFromDiscriminant(EObject discriminant) {
 		List<EObject> result = new ArrayList<EObject>();
 		if (discriminant instanceof ProfileApplicationImpl) {
 			result.add(((ProfileApplicationImpl)discriminant).basicGetAppliedProfile());
 			for (EAnnotation annotation : (List<? extends EAnnotation>)ReferenceUtil.getAsList(discriminant,
 					EcorePackage.Literals.EMODEL_ELEMENT__EANNOTATIONS)) {
 				result.addAll((List<? extends EObject>)ReferenceUtil.getAsList(annotation,
 						EcorePackage.Literals.EANNOTATION__REFERENCES));
 			}
 		}
 		return result;
 	}
 
 	@Override
 	protected boolean isRelatedToAnExtensionAdd(ReferenceChange input) {
 		return input.getReference().isContainment() && input.getKind().equals(DifferenceKind.ADD)
 				&& input.getValue() instanceof ProfileApplication;
 	}
 
 	@Override
 	protected boolean isRelatedToAnExtensionDelete(ReferenceChange input) {
 		return input.getReference().isContainment() && input.getKind().equals(DifferenceKind.DELETE)
 				&& input.getValue() instanceof ProfileApplication;
 	}
 
 	@Override
 	protected boolean isRelatedToAnExtensionChange(ReferenceChange input) {
		final boolean ret;
		EReference reference = input.getReference();
		if (reference == UMLPackage.Literals.PROFILE_APPLICATION__APPLIED_PROFILE) {
			ret = true;
		} else if (reference == EcorePackage.Literals.EANNOTATION__REFERENCES) {
			Comparison comparison = input.getMatch().getComparison();
			EAnnotation container = (EAnnotation)MatchUtil.getContainer(comparison, input);
			ret = isSourceMatchesUMLVersion(container);
		} else if (reference == EcorePackage.Literals.EMODEL_ELEMENT__EANNOTATIONS) {
			EAnnotation eAnnotation = (EAnnotation)input.getValue();
			ret = isSourceMatchesUMLVersion(eAnnotation);
		} else {
			ret = false;
		}
		return ret;
	}

	private static boolean isSourceMatchesUMLVersion(EAnnotation container) {
		final boolean ret;
		String source = container.getSource();
		if (source != null) {
			ret = source.matches(UML_VERSIONS);
		} else {
			ret = false;
		}
		return ret;
 	}
 
 	@Override
 	public void fillRequiredDifferences(Comparison comparison, UMLDiff extension) {
 		if (extension instanceof ProfileApplicationChange) {
 			final ProfileApplicationChange profileApplicationChange = (ProfileApplicationChange)extension;
 			if (profileApplicationChange.getKind() == DifferenceKind.DELETE) {
 				final Iterator<EObject> stereotypeApplications = getStereotypeApplications(
 						(ProfileApplication)profileApplicationChange.getDiscriminant()).iterator();
 				while (stereotypeApplications.hasNext()) {
 					final EObject eObject = stereotypeApplications.next();
 					for (Diff diff : comparison.getDifferences(eObject)) {
 						if (diff instanceof StereotypeApplicationChange
 								&& diff.getKind() == DifferenceKind.DELETE) {
 							profileApplicationChange.getRequires().add(diff);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private List<EObject> getStereotypeApplications(ProfileApplication profileApplication) {
 		final List<EObject> result = new ArrayList<EObject>();
 		final org.eclipse.uml2.uml.Package p = profileApplication.getApplyingPackage();
 		final Iterator<EObject> it = p.eAllContents();
 		while (it.hasNext()) {
 			final EObject elt = it.next();
 			if (elt instanceof Element) {
 				for (Stereotype stereotype : ((Element)elt).getAppliedStereotypes()) {
 					if (stereotype.getProfile().equals(profileApplication.getAppliedProfile())) {
 						final EObject stereotypeApplication = ((Element)elt)
 								.getStereotypeApplication(stereotype);
 						result.add(stereotypeApplication);
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public Match getParentMatch(Diff input) {
 		final EObject discriminant = getDiscriminantFromDiff(input);
 		if (discriminant instanceof ProfileApplication) {
 			final org.eclipse.uml2.uml.Package p = ((ProfileApplication)discriminant).getApplyingPackage();
 			final Match match = input.getMatch().getComparison().getMatch(p);
 			if (match != null) {
 				return match;
 			}
 		}
 		return super.getParentMatch(input);
 	}
 
 }
