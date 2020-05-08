 /**
  * 
  */
 package org.eclipse.emf.modelmutator.intern;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.modelmutator.api.ModelMutatorConfiguration;
 import org.eclipse.emf.modelmutator.api.ModelMutatorUtil;
 
 /**
  * @author Eugen Neufeld
  * 
  */
 public abstract class AbstractModelMutator {
 
 	protected final ModelMutatorConfiguration configuration;
 
 	public AbstractModelMutator(ModelMutatorConfiguration config) {
 		this.configuration = config;
 	}
 
 	public abstract void preMutate();
 
 	public abstract void postMutate();
 
 	public void mutate() {
 		preMutate();
 		
 		setContaintments();
 
 		setReferences();
 		
 		postMutate();
 	}
 
 	private void setContaintments() {
 		Map<Integer, List<EObject>> depthToParentObjects = new LinkedHashMap<Integer, List<EObject>>();
 		List<EObject> parentsInThisDepth = new LinkedList<EObject>();
 		parentsInThisDepth.add(configuration.getRootEObject());
 		int currentDepth = 0;
 		depthToParentObjects.put(1, new LinkedList<EObject>());
 		if(configuration.isDoNotGenerateRoot()){
 			depthToParentObjects.put(2, new LinkedList<EObject>());
 			currentDepth++;
 			parentsInThisDepth = new LinkedList<EObject>(configuration.getRootEObject().eContents());
 		}
 		while (currentDepth < configuration.getDepth()) {
 			// for all parent EObjects in this depth
 
 			for (EObject nextParentEObject : parentsInThisDepth) {
 				//ModelMutatorUtil.setEObjectAttributes(nextParentEObject, configuration.getRandom(), configuration.getExceptionLog(), configuration.isIgnoreAndLog());
 				List<EObject> children = generateChildren(nextParentEObject,currentDepth==0&&configuration.isAllElementsOnRoot());
 				// will the just created EObjects have children?
 				depthToParentObjects.get(currentDepth + 1).addAll(children);
 			}
 
 			// proceed to the next level
 			currentDepth++;
 			parentsInThisDepth = depthToParentObjects.get(currentDepth);
 			depthToParentObjects.put((currentDepth + 1), new LinkedList<EObject>());
 		}
 	}
 
 	/**
 	 * Generates children for a certain parent EObject. Generation includes
 	 * setting containment references and attributes. All required references
 	 * are set first, thus the specified width might be exceeded.
 	 * 
 	 * @param parentEObject
 	 *            the EObject to generate children for
 	 * @return all generated children as a list
 	 * @see #generateContainments(EObject, EReference, int)
 	 */
 	private List<EObject> generateChildren(EObject parentEObject,boolean generateAllReferences) {
 		Map<EReference, List<EObject>> currentContainments = new HashMap<EReference, List<EObject>>();
 		List<EObject> result = new LinkedList<EObject>();
 		for (EObject curChild : parentEObject.eContents()) {
 			if (!currentContainments.containsKey(curChild.eContainmentFeature()))
 				currentContainments.put(curChild.eContainmentFeature(), new LinkedList<EObject>());
 			currentContainments.get(curChild.eContainmentFeature()).add(curChild);
 			ModelMutatorUtil.setEObjectAttributes(curChild, configuration.getRandom(), configuration.getExceptionLog(), configuration.isIgnoreAndLog());
 			result.add(curChild);
 		}
 
 		List<EReference> references = new LinkedList<EReference>();
 		for (EReference reference : parentEObject.eClass().getEAllContainments()) {
 			if (configuration.geteStructuralFeaturesToIgnore().contains(reference)
 					|| !ModelMutatorUtil.isValid(reference, parentEObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog())) {
 				continue;
 			}
 			references.add(reference);
 			int numCurrentContainments = 0;
 			if (currentContainments.containsKey(reference))
 				numCurrentContainments = currentContainments.get(reference).size();
 			
 			List<EObject> contain=null;
 			if(generateAllReferences)
 				contain = generateFullDifferentContainment(parentEObject, reference);
 			else
 				contain = generateMinContainments(parentEObject, reference, reference.getLowerBound() - numCurrentContainments);
 			
 			if (!currentContainments.containsKey(reference))
 				currentContainments.put(reference, new LinkedList<EObject>());
 			currentContainments.get(reference).addAll(contain);
 
 			result.addAll(contain);
 		}
 		if (references.size() != 0) {
 			for (int i = result.size(); i < configuration.getWidth() && references.size() != 0; i++) {
 				Collections.shuffle(references, configuration.getRandom());
 				EReference reference = references.get(0);
 				int upperBound=Integer.MAX_VALUE;
 				if(reference.getUpperBound()!=EReference.UNBOUNDED_MULTIPLICITY&&reference.getUpperBound()!=EReference.UNSPECIFIED_MULTIPLICITY)
 					upperBound=reference.getUpperBound();
 				if (currentContainments.get(reference).size() < upperBound) {
 					List<EObject> contain = generateMinContainments(parentEObject, reference, 1);
 					if (!currentContainments.containsKey(reference))
 						currentContainments.put(reference, new LinkedList<EObject>());
 					currentContainments.get(reference).addAll(contain);
 					result.addAll(contain);
 				} else {
 					references.remove(reference);
 					i--;
 				}
 			}
 		}
 		return result;
 	}
 
 	private List<EObject> generateFullDifferentContainment(EObject parentEObject, EReference reference) {
 		List<EClass> allEClasses = new LinkedList<EClass>();
 
 		allEClasses.addAll(ModelMutatorUtil.getAllEContainments(reference));
 
 		// only allow EClasses that appear in the specified EPackage
 		allEClasses.retainAll(ModelMutatorUtil.getAllEClasses(configuration.getModelPackage()));
 		// don't allow any EClass or sub class of all EClasses specified in
 		// ignoredClasses
 		for (EClass eClass : configuration.geteClassesToIgnore()) {
 			allEClasses.remove(eClass);
 			allEClasses.removeAll(ModelMutatorUtil.getAllSubEClasses(eClass));
 		}
 		List<EObject> result = new LinkedList<EObject>();
 		for(EClass eClass:allEClasses){
			
 			EObject newChild=generateElement(parentEObject,eClass,reference);
 			// was creating the child successful?
 			if (newChild != null) {
 				result.add(newChild);
 			}
 		}
		//Fill with random objects to get to the lowerBound
		int numToFillMin=reference.getLowerBound()-result.size();
		if(numToFillMin>0)
			result.addAll(generateMinContainments(parentEObject, reference, numToFillMin));
 		return result;
 	}
 
 	/**
 	 * Creates valid instances of children for <code>parentEObject</code> using
 	 * the information in the <code>reference</code>. They are set as a child
 	 * of <code>parentEObject</code> with AddCommand/SetCommand.
 	 * 
 	 * @param parentEObject
 	 *            the EObject that shall contain the new instances of
 	 *            children
 	 * @param reference
 	 *            the containment reference
 	 * @param width
 	 * 			  the amount of children to create
 	 * @return a list containing the instances of children or an empty list if 
 	 *  		  the operation failed
 	 * 
 	 * @see ModelGeneratorUtil#addPerCommand(EObject, EStructuralFeature,
 	 *      Object, Set, boolean)
 	 * @see ModelGeneratorUtil#setPerCommand(EObject, EStructuralFeature,
 	 *      Object, Set, boolean)
 	 */
 	private List<EObject> generateMinContainments(EObject parentEObject, EReference reference, int width) {
 		List<EObject> result = new LinkedList<EObject>();
 		for (int i = 0; i < width; i++) {
 			EClass eClass = getValidEClass(reference);
 
 			EObject newChild=generateElement(parentEObject,eClass,reference);
 			// was creating the child successful?
 			if (newChild != null) {
 				result.add(newChild);
 			}
 		}
 		return result;
 	}
 
 	private EObject generateElement(EObject parentEObject,EClass eClass,EReference reference) {
 		// create child and add it to parentEObject
 		// Old version which used another method:
 		//EObject newChild = setContainment(parentEObject, eClass, reference);
 		EObject newChild = null;
 		// create and set attributes
 		EObject newEObject = EcoreUtil.create(eClass);
 		ModelMutatorUtil.setEObjectAttributes(newEObject, configuration.getRandom(), configuration.getExceptionLog(), configuration.isIgnoreAndLog());
 		// reference created EObject to the parent
 		if (reference.isMany()) {
 			newChild = ModelMutatorUtil.addPerCommand(parentEObject, reference, newEObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog());
 		} else {
 			newChild = ModelMutatorUtil.setPerCommand(parentEObject, reference, newEObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog());
 		}
 		return newChild;
 	}
 
 	private EClass getValidEClass(EReference eReference) {
 		List<EClass> allEClasses = new LinkedList<EClass>();
 
 		allEClasses.addAll(ModelMutatorUtil.getAllEContainments(eReference));
 
 		// only allow EClasses that appear in the specified EPackage
 		allEClasses.retainAll(ModelMutatorUtil.getAllEClasses(configuration.getModelPackage()));
 		// don't allow any EClass or sub class of all EClasses specified in
 		// ignoredClasses
 		for (EClass eClass : configuration.geteClassesToIgnore()) {
 			allEClasses.remove(eClass);
 			allEClasses.removeAll(ModelMutatorUtil.getAllSubEClasses(eClass));
 		}
 		if (allEClasses.isEmpty()) {
 			// no valid EClass left
 			return null;
 		}
 		// random seed all the time
 		Collections.shuffle(allEClasses, configuration.getRandom());
 		return allEClasses.get(0);
 	}
 	
 	/**
 	 * Sets all references for every child (direct and indirect)
 	 * of <code>root</code>.
 	 * 
 	 * @see #changeEObjectAttributes(EObject)
 	 * @see #changeEObjectReferences(EObject, Map)
 	 */
 	private void setReferences() {
 		EObject rootObject = configuration.getRootEObject();
 		Map<EClass, List<EObject>> allObjectsByEClass = ModelMutatorUtil.getAllClassesAndObjects(rootObject);
 		for(EClass eClass : allObjectsByEClass.keySet()) {
 			for(EObject eObject : allObjectsByEClass.get(eClass)) {
 				generateReferences(eObject, allObjectsByEClass);
 			}
 		}
 	}
 	
 	/**
 	 * Generates references (no containment references) for an EObject.
 	 * All valid references are set with EObjects generated during the generation process.
 	 * 
 	 * @param eObject the EObject to set references for
 	 * @param allObjectsByEClass all possible EObjects that can be referenced, mapped to their EClass 
 	 * @see ModelGeneratorHelper#setReference(EObject, EClass, EReference, Map)
 	 */
 	private void generateReferences(EObject eObject, Map<EClass, List<EObject>> allObjectsByEClass) {
 		for(EReference reference : ModelMutatorUtil.getValidReferences(eObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog())) {
 			for(EClass nextReferenceClass : ModelMutatorUtil.getReferenceClasses(reference, allObjectsByEClass.keySet())) {
 				setEObjectReference(eObject, nextReferenceClass, reference, allObjectsByEClass);
 			}
 		}
 	}
 	
 	/**
 	 * Sets a reference, if the upper bound allows it,
 	 * using {@link ModelGeneratorUtil#setReference}.
 	 * 
 	 * @param eObject the EObject to set the reference for
 	 * @param referenceClass the EClass of EObjects that shall be referenced
 	 * @param reference the EReference that shall be set
 	 * @param allEObjects all possible EObjects that can be referenced
 	 * @see ModelGeneratorUtil#setReference(EObject, EClass, EReference, Random, Set, boolean, Map)
 	 */
 	private void setEObjectReference(EObject eObject, EClass referenceClass, EReference reference,
 		Map<EClass, List<EObject>> allEObjects) {
 
 		//Delete already set references (only applies when changing a model)
 		if(eObject.eIsSet(reference)) {
 			eObject.eUnset(reference);
 		}
 		// check if the upper bound is reached
 		if(!ModelMutatorUtil.isValid(reference, eObject, configuration.getExceptionLog(), configuration.isIgnoreAndLog()) ||
 				(!reference.isMany() && eObject.eIsSet(reference))) {
 			return;
 		}
 		ModelMutatorUtil.setReference(eObject, referenceClass, reference, configuration.getRandom(),
 			configuration.getExceptionLog(), configuration.isIgnoreAndLog(), allEObjects);
 	}
 }
