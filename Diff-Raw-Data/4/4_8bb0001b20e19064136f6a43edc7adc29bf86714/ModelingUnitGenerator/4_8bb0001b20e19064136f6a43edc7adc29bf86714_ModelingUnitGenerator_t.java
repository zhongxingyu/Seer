 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.compiler.generator.modelgeneration;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.util.Monitor;
 import org.eclipse.mylyn.docs.intent.client.compiler.errors.CompilationErrorType;
 import org.eclipse.mylyn.docs.intent.client.compiler.errors.CompilationException;
 import org.eclipse.mylyn.docs.intent.client.compiler.errors.InvalidValueException;
 import org.eclipse.mylyn.docs.intent.client.compiler.generator.modellinking.ModelingUnitLinkResolver;
 import org.eclipse.mylyn.docs.intent.client.compiler.utils.IntentCompilerInformationHolder;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 import org.eclipse.mylyn.docs.intent.core.genericunit.LabelDeclaration;
 import org.eclipse.mylyn.docs.intent.core.genericunit.UnitInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.AnnotationDeclaration;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ContributionInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.IntentSectionReferenceinModelingUnit;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.NativeValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.NewObjectValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ReferenceValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ResourceDeclaration;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch;
 
 /**
  * Modeling Unit generator : generates, for the given Modeling Units, the elements described and register the
  * generated elements into an informationHolder.<br/>
  * This dispatcher calls the correct generator according to the elements types (typical use of EMF Switch).
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class ModelingUnitGenerator extends ModelingUnitSwitch<List<Object>> {
 
 	/**
 	 * The linkResolver to use for link resolving.
 	 */
 	private ModelingUnitLinkResolver linkResolver;
 
 	/**
 	 * List of the current imported packages (URIs).
 	 */
 	private List<String> currentImportedPackages;
 
 	/**
 	 * List of the Intent resources declared in the compiled modelingUnits.
 	 */
 	private List<ResourceDeclaration> resourceDeclarations;
 
 	/**
 	 * Indicates the mode of the generator : if true, the generator will only consider modeling Units defining
 	 * at least one EPackage ; if false, it will consider the other modeling units ONLY.
 	 */
 	private boolean generateOnlyEPackages;
 
 	/**
 	 * The information holder to use for register the generated elements.
 	 */
 	private IntentCompilerInformationHolder informationHolder;
 
 	/**
 	 * The progressMonitor to use for compilation ; if canceled, the compilation will stop immediately.
 	 */
 	private Monitor progressMonitor;
 
 	/**
 	 * ModelingUnitGenerator constructor.
 	 * 
 	 * @param linkResolver
 	 *            the linkResolver to use for resolving links
 	 * @param informationHolder
 	 *            the information holder to use for register the generated elements.
 	 * @param progressMonitor
 	 *            the progress Monitor to use
 	 */
 	public ModelingUnitGenerator(ModelingUnitLinkResolver linkResolver,
 			IntentCompilerInformationHolder informationHolder, Monitor progressMonitor) {
 		this.linkResolver = linkResolver;
 		this.resourceDeclarations = new ArrayList<ResourceDeclaration>();
 		this.generateOnlyEPackages = false;
 		this.informationHolder = informationHolder;
 		this.progressMonitor = progressMonitor;
 	}
 
 	/**
 	 * Generate all the elements described in the given modeling unit, and register those generated elements
 	 * into the informationHolder.
 	 * 
 	 * @param mu
 	 *            the modeling unit to compile
 	 */
 	public void generate(ModelingUnit mu) {
 		this.doSwitch(mu);
 	}
 
 	/**
 	 * Returns the list of the Intent resources declared in the compiled modelingUnits.
 	 * 
 	 * @return the resourceDeclarations the list of the Intent resources declared in the compiled
 	 *         modelingUnits
 	 */
 	public List<ResourceDeclaration> getResourceDeclarations() {
 		return resourceDeclarations;
 	}
 
 	/**
 	 * Remove from the status list of the given ReStrucutred models element all the status related to
 	 * compilation errors.
 	 * 
 	 * @param element
 	 *            the element containing the compilation status to remove
 	 */
 	public static void clearCompilationStatus(IntentGenericElement element) {
 		Iterator<CompilationStatus> iterator = element.getCompilationStatus().iterator();
 		while (iterator.hasNext()) {
 			CompilationStatus status = iterator.next();
 			iterator.remove();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseModelingUnit(org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit)
 	 */
 	@Override
 	public List<Object> caseModelingUnit(ModelingUnit modelingUnit) {
 
 		List<Object> createdObject = new ArrayList<Object>();
 		// If we have to consider this org.eclipse.mylyn.docs.intent.core.modelingunit
 		if (isModelingUnitToConsider(modelingUnit)) {
 			// We generate the elements (in other cases we simply do nothing)
 			currentImportedPackages = getImportedPackages(modelingUnit);
 			for (UnitInstruction instruction : modelingUnit.getInstructions()) {
 				if (!progressMonitor.isCanceled()) {
 					doSwitch(instruction);
 				}
 			}
 		}
 		return createdObject;
 	}
 
 	/**
 	 * Returns true if the given org.eclipse.mylyn.docs.intent.core.modelingunit must be considered, according
 	 * to the mode of the generator (generate only EPackages declarations mode or not).
 	 * 
 	 * @param modelingUnit
 	 *            the modeling unit to consider
 	 * @return true if the given org.eclipse.mylyn.docs.intent.core.modelingunit must be considered, false
 	 *         otherwise
 	 */
 	private boolean isModelingUnitToConsider(ModelingUnit modelingUnit) {
 		// If we are in generateOnlyEPackage mode, we consider this modelingunit
 		// only if it is describing at least one EPackage
 		// or is a contribution instruction
 		boolean isModelingUnitToConsider = isGenerateOnlyEPackages() && isDescribingEPackages(modelingUnit);
 		// Otherwise, it must define no EPackage at all
 		isModelingUnitToConsider = isModelingUnitToConsider
 				|| (!isGenerateOnlyEPackages() && !isDescribingEPackages(modelingUnit));
 		return isModelingUnitToConsider;
 	}
 
 	/**
 	 * Returns true if the given modeling Unit instantiate at least one EPackage.
 	 * 
 	 * @param modelingUnit
 	 *            the modeling unit to consider
 	 * @return true if the given modeling Unit instantiate at least one EPackage or contain a contribution
 	 *         instruction, false otherwise
 	 */
 	private boolean isDescribingEPackages(ModelingUnit modelingUnit) {
 		boolean isDescribingEPackages = false;
 
 		for (UnitInstruction instruction : modelingUnit.getInstructions()) {
 			if (!progressMonitor.isCanceled()) {
 				// At least one instruction must match an EPackage instanciation
 				if (instruction instanceof InstanciationInstruction) {
 					isDescribingEPackages = isDescribingEPackages
 							|| InstanciationInstructionGenerator
 									.isEPackageInstanciation((InstanciationInstruction)instruction);
 				}
 
 				// or a completion (we can't know by advance if it's completing an EPackage
 				if (instruction instanceof ContributionInstruction) {
 					ContributionInstruction contributionInstruction = (ContributionInstruction)instruction;
 					// this.getInformationHolder().addUnresolvedContribution(
 					// contribution.getReferencedElement().getHref(), contribution);
 					// ContributionInstructionGenerator.generate(contribution, this, linkResolver, null);
 					// isDescribingEPackages = true;
 					ContributionInstructionGenerator.generate(contributionInstruction, this, linkResolver);
 				}
 			}
 		}
 		return isDescribingEPackages;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseInstanciationInstruction(org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction)
 	 */
 	@Override
 	public List<Object> caseInstanciationInstruction(InstanciationInstruction instanciationInstruction) {
 		List<Object> createdObject = new ArrayList<Object>();
 		createdObject.add(InstanciationInstructionGenerator.generate(instanciationInstruction,
 				currentImportedPackages, linkResolver, this));
 		return createdObject;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseContributionInstruction(org.eclipse.mylyn.docs.intent.core.modelingunit.ContributionInstruction)
 	 */
 	@Override
 	public List<Object> caseContributionInstruction(ContributionInstruction contributionInstruction) {
 		List<Object> createdObject = new ArrayList<Object>();
		// We simply do nothing
 		return createdObject;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseResourceDeclaration(org.eclipse.mylyn.docs.intent.core.modelingunit.ResourceDeclaration)
 	 */
 	@Override
 	public List<Object> caseResourceDeclaration(ResourceDeclaration resourceDeclaration) {
		ModelingUnitGenerator.clearCompilationStatus(resourceDeclaration);
 		this.resourceDeclarations.add(resourceDeclaration);
 		return new ArrayList<Object>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseNativeValueForStructuralFeature(org.eclipse.mylyn.docs.intent.core.modelingunit.NativeValueForStructuralFeature)
 	 */
 	@Override
 	public List<Object> caseNativeValueForStructuralFeature(NativeValueForStructuralFeature value) {
 		List<Object> createdObject = new ArrayList<Object>();
 		try {
 			createdObject.add(NativeValueForStructuralFeatureGenerator.generate(value, linkResolver, this));
 		} catch (InvalidValueException e) {
 			this.getInformationHolder().registerCompilationExceptionAsCompilationStatus(
 					new CompilationException(e.getInvalidInstruction(),
 							CompilationErrorType.INVALID_VALUE_ERROR, "The value " + e.getMessage()));
 		}
 		return createdObject;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseReferenceValueForStructuralFeature(org.eclipse.mylyn.docs.intent.core.modelingunit.ReferenceValueForStructuralFeature)
 	 */
 	@Override
 	public List<Object> caseReferenceValueForStructuralFeature(
 			ReferenceValueForStructuralFeature referenceValue) {
 		List<Object> createdObject = new ArrayList<Object>();
 
 		createdObject.add(ReferenceValueForStructuralFeatureGenerator.generate(referenceValue, linkResolver,
 				this));
 		return createdObject;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseNewObjectValueForStructuralFeature(org.eclipse.mylyn.docs.intent.core.modelingunit.NewObjectValueForStructuralFeature)
 	 */
 	@Override
 	public List<Object> caseNewObjectValueForStructuralFeature(NewObjectValueForStructuralFeature value) {
 		List<Object> createdObjects = new ArrayList<Object>();
 		createdObjects.addAll(doSwitch(value.getValue()));
 		return createdObjects;
 	}
 
 	/**
 	 * Sample method for stubing package declaration.
 	 * 
 	 * @return the list of imported packages names
 	 */
 	@Deprecated
 	public List<String> getImportedPackages(ModelingUnit mu) {
 		return informationHolder.getCurrentImportedPackages();
 	}
 
 	/* IGNORED DECLARATIONS IN STEP ONE */
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseLabelDeclaration(org.eclipse.mylyn.docs.intent.core.genericunit.LabelDeclaration)
 	 */
 	@Override
 	public List<Object> caseLabelDeclaration(LabelDeclaration object) {
 		return new ArrayList<Object>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseAnnotationDeclaration(org.eclipse.mylyn.docs.intent.core.modelingunit.AnnotationDeclaration)
 	 */
 	@Override
 	public List<Object> caseAnnotationDeclaration(AnnotationDeclaration object) {
 		return new ArrayList<Object>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.core.modelingunit.util.ModelingUnitSwitch#caseIntentSectionReferenceinModelingUnit(org.eclipse.mylyn.docs.intent.core.modelingunit.IntentSectionReferenceinModelingUnit)
 	 */
 	@Override
 	public List<Object> caseIntentSectionReferenceinModelingUnit(IntentSectionReferenceinModelingUnit object) {
 		return new ArrayList<Object>();
 	}
 
 	/**
 	 * Sets the mode of this generator : if true, it will only consider ModelingUnits describing EPackages.
 	 * 
 	 * @param generateOnlyEPackages
 	 *            true if you want this generator to consider only ModelingUnits describing EPackages
 	 *            declarations, false if you want it to consider the other modeling units ONLY.
 	 */
 	public void setGenerateOnlyEPackages(boolean generateOnlyEPackages) {
 		this.generateOnlyEPackages = generateOnlyEPackages;
 	}
 
 	/**
 	 * Indicates if this generator will only consider ModelingUnits describing EPackages.
 	 * 
 	 * @return true if you want this generator considers only ModelingUnits describing EPackages declarations,
 	 *         false if it considers the other modeling units ONLY
 	 */
 	public boolean isGenerateOnlyEPackages() {
 		return generateOnlyEPackages;
 	}
 
 	/**
 	 * Returns the information holder to use for register the generated elements.
 	 * 
 	 * @return the information holder to use for register the generated elements
 	 */
 	public IntentCompilerInformationHolder getInformationHolder() {
 		return informationHolder;
 	}
 }
