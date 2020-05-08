 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.client.test.integration.forward;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EPackage.Registry;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.emfstore.client.model.Configuration;
 import org.eclipse.emf.emfstore.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.common.model.util.SerializationException;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
import org.eclipse.emf.emfstore.server.model.versioning.operations.CreateDeleteOperation;
 
 /**
  * Helper class for testing.
  * 
  * @author Hodaie
  */
 public final class IntegrationTestHelper {
 
 	private static final Logger LOGGER = Logger
 		.getLogger("org.unicase.workspace.test.integration.forward.IntegrationTestHelper");
 
 	/**
 	 * Numbers of different integration tests. Each integration test case must have a corresponding method in this class
 	 * which actually implements the test and will be called from test case. Knowing the number of available methods is
 	 * required in composite test.
 	 */
 	public static final int NUM_OF_TESTS = 16;
 
 	private static EditingDomain domain;
 	private static final String TEMP_PATH = Configuration.getWorkspaceDirectory() + "tmp";
 	private Random random;
 	private Set<EObject> allMEsInProject;
 	private Project testProject;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param randomSeed random seed
 	 * @param testProject test project
 	 */
 	public IntegrationTestHelper(long randomSeed, Project testProject) {
 		this.random = new Random(randomSeed);
 		this.testProject = testProject;
 	}
 
 	/**
 	 * Creates an empty project space.
 	 * 
 	 * @param name project space name
 	 * @return an empty project space
 	 */
 	public static ProjectSpace createEmptyProjectSpace(String name) {
 
 		ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 		ProjectId projectId = org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createProjectId();
 		projectId.setId(name);
 		projectSpace.setIdentifier(name);
 		projectSpace.setProjectId(projectId);
 		projectSpace.setProjectName(name);
 		projectSpace.setProjectDescription("description");
 		PrimaryVersionSpec versionSpec = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 		versionSpec.setIdentifier(0);
 		projectSpace.setBaseVersion(versionSpec);
 		projectSpace.setLastUpdated(new Date());
 		projectSpace.setUsersession(null);
 		// projectSpace.setProject(ModelFactory.eINSTANCE.createProject());
 		projectSpace.setResourceCount(0);
 
 		File file = new File(TEMP_PATH);
 		if (!file.exists()) {
 
 			new File(TEMP_PATH).mkdir();
 		}
 
 		return projectSpace;
 
 	}
 
 	/**
 	 * Creates a change package from given operations.
 	 * 
 	 * @param operations list of operations. This can be obtained by calling ProjectSpace.getOperations() method.
 	 * @param cannonize if operations will be cannonized.
 	 * @param clearOperations if operations must be cleared from project space. This is used when running more than one
 	 *            change package test after each other and we don't want operation from one change package test to be
 	 *            accumulated in operations of change package test run after that.
 	 * @return a change package created from given operations
 	 */
 	public static ChangePackage getChangePackage(final List<AbstractOperation> operations, final boolean cannonize,
 		final boolean clearOperations) {
 
 		final ChangePackage changePackage = VersioningFactory.eINSTANCE.createChangePackage();
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				for (AbstractOperation op : operations) {
 					changePackage.getOperations().add(EcoreUtil.copy(op));
 
 				}
 				if (clearOperations) {
 					operations.clear();
 				}
 
 				if (cannonize) {
 					changePackage.cannonize();
 				}
 			}
 
 		}.run(false);
 		return changePackage;
 	}
 
 	/**
 	 * This method compares two projects using {@link ModelUtil#areEqual(EObject, EObject)}. You can also use
 	 * linearCompare(project, project) to identify the position in which the project differ.
 	 * 
 	 * @param testSpace test project space
 	 * @param compareSpace compare project space
 	 * @return if objects are equal
 	 */
 	public static boolean compare(ProjectSpace testSpace, ProjectSpace compareSpace) {
 
 		// extract changes from testSpace and apply them to compareSpace
 		prepareCompare(testSpace, compareSpace, false);
 
 		System.out.println("comparing...");
 		return ModelUtil.areEqual(testSpace.getProject(), compareSpace.getProject());
 	}
 
 	/**
 	 * Extracts changes form test project and applies them to compare project. Extracted ChangePackage is also written
 	 * to disk (to TMP_PATH/changePackage.txt file) for further investigations.
 	 * 
 	 * @param testSpace project space to extract operations from
 	 * @param compareSpace project space to apply extracted operations to
 	 * @param accumulative if operations must remain in test project space after extracting them
 	 */
 	private static void prepareCompare(final ProjectSpace testSpace, final ProjectSpace compareSpace,
 		final boolean accumulative) {
 		System.out.println("extracting operations from test project...");
 
 		List<AbstractOperation> operations = testSpace.getOperations();
 		System.out.println(operations.size() + " operatoins");
 		final ChangePackage changePackage = getChangePackage(operations, true, false);
 
 		// Save change package for later reference to disk.
 		// The saved change package will be overwritten every time a test
 		// succeeds.
 		EObject copyChangePackage = EcoreUtil.copy(changePackage);
 		ResourceSet reseourceSet = new ResourceSetImpl();
 		Resource resource = reseourceSet.createResource(URI.createFileURI(TEMP_PATH + File.separator
 			+ "changePackage.txt"));
 		resource.getContents().add(copyChangePackage);
 		try {
 			resource.save(null);
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}
 
 		// apply changes to compare project
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				System.out.println("applying changes to compareSpace...");
 				((ProjectSpaceImpl) compareSpace).stopChangeRecording();
 				changePackage.apply(compareSpace.getProject());
 				if (!accumulative) {
 					testSpace.getOperations().clear();
 				}
 			}
 		}.run(false);
 	}
 
 	/**
 	 * Uses linearCompare(projectA, projectB) method to compare projects contained in given project spaces. The result
 	 * is an array of integers with following elements: index 0 (ARE_EQUAL) 0 if files are not equal and 1 otherwise;
 	 * index 1 (DIFFRENCE_POSITION) index of differing character; index 2 (CHARACTER) the differing character; index 3
 	 * (LINE_NUM) line number of first differing character; index 4 (COL_NUM) column number of first differing
 	 * character. If any exceptions is thrown, all indices in result array will be -1.
 	 * 
 	 * @param testSpace test project space
 	 * @param compareSpace compare project space
 	 * @return if projects inside these project spaces are equal or not, and if they are not equal the location of
 	 *         differing character in their serialized string
 	 */
 	public static int[] linearCompare(ProjectSpace testSpace, ProjectSpace compareSpace) {
 
 		prepareCompare(testSpace, compareSpace, false);
 		System.out.println("linear comparing...");
 		return linearCompare(testSpace.getProject(), compareSpace.getProject());
 
 	}
 
 	/**
 	 * Returns editing domain.
 	 * 
 	 * @return editing domain
 	 */
 	public static EditingDomain getDomain() {
 
 		if (domain == null) {
 			domain = Configuration.getEditingDomain();
 
 		}
 		return domain;
 	}
 
 	/**
 	 * Returns a serialized string for this object. It can also save serialized string to disk (in TMP_PATH) under given
 	 * name.
 	 * 
 	 * @param object object
 	 * @param name name to save string representation of object
 	 * @param writeToDisk if serialized string must be saved to disk
 	 * @return serialized string representation of this object
 	 * @throws SerializationException SerializationException
 	 */
 	public static String eObjectToString(EObject object, String name, boolean writeToDisk)
 		throws SerializationException {
 		if (object == null) {
 			return null;
 		}
 		Resource res = (new ResourceSetImpl()).createResource(URI.createURI("virtualUnicaseUri"));
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		res.getContents().add(EcoreUtil.copy(object));
 		try {
 			res.save(out, null);
 		} catch (IOException e) {
 			throw new SerializationException(e);
 		}
 
 		if (writeToDisk) {
 			writeToDisk(name, out);
 		}
 
 		return out.toString();
 	}
 
 	/**
 	 * Save the given OutputStream to disk under the given name.
 	 * 
 	 * @param name
 	 * @param out
 	 */
 	private static void writeToDisk(String name, ByteArrayOutputStream out) {
 		File file = new File(TEMP_PATH + File.separator + name + ".txt");
 		try {
 			if (!file.exists()) {
 
 				new File(TEMP_PATH).mkdir();
 			}
 
 			FileOutputStream fos = new FileOutputStream(file);
 			fos.write(out.toByteArray());
 			fos.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This method compare two projects with each other. It first converts projects in string and finds location of
 	 * first character in which these strings differ. The result is an array of integers with following elements: index
 	 * 0 (ARE_EQUAL) 0 if files are not equal and 1 otherwise; index 1 (DIFFRENCE_POSITION) index of differing
 	 * character; index 2 (CHARACTER) the differing character; index 3 (LINE_NUM) line number of first differing
 	 * character; index 4 (COL_NUM) column number of first differing character. If any exceptions is thrown, all indices
 	 * in result array will be -1.
 	 * 
 	 * @param projectA project A
 	 * @param projectB project B
 	 * @return if projects are equal or not, and if they are not equal the location of differing character in their
 	 *         serialized string
 	 */
 	public static int[] linearCompare(Project projectA, Project projectB) {
 		int[] result = new int[5];
 		int areEqual = 0;
 		int differencePosition = 1;
 		int character = 2;
 		int lineNum = 3;
 		int columnNum = 4;
 		result[areEqual] = 1;
 		String stringA;
 		String stringB;
 
 		try {
 
 			stringA = eObjectToString(projectA, "testProj", false);
 			stringB = eObjectToString(projectB, "compareProj", false);
 
 		} catch (SerializationException e) {
 			for (int i = 0; i < 5; i++) {
 				result[i] = -1;
 			}
 			return result;
 		}
 
 		int length = Math.min(stringA.length(), stringB.length());
 		for (int index = 0; index < length; index++) {
 			if (stringA.charAt(index) != stringB.charAt(index)) {
 				result[areEqual] = 0;
 				result[differencePosition] = index;
 				result[character] = stringA.charAt(index);
 				int lineNumber = getLineNum(stringA, index);
 				result[lineNum] = lineNumber;
 				result[columnNum] = getColNum(stringA, index);
 				break;
 			}
 
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * Finds column number of given index inside a multi-line string.
 	 * 
 	 * @param stringA string
 	 * @param index index
 	 * @return
 	 */
 	private static int getColNum(String stringA, int index) {
 		int lineNum = 1;
 		int pos = index;
 		int j = 0;
 		for (int i = 0; i < index; i++) {
 			j++;
 			if (stringA.charAt(i) == '\n') {
 				lineNum++;
 				pos -= j;
 				j = 0;
 			}
 		}
 		return pos;
 
 	}
 
 	/**
 	 * Finds line number of a given index inside a multi-line string.
 	 * 
 	 * @param stringA
 	 * @param index
 	 * @return
 	 */
 	private static int getLineNum(String stringA, int index) {
 		int lineNum = 1;
 		for (int i = 0; i < index; i++) {
 			if (stringA.charAt(i) == '\n') {
 				lineNum++;
 			}
 		}
 		return lineNum;
 	}
 
 	/**
 	 * Returns a list of randomly selected MEs.
 	 * 
 	 * @param project project from which to select MEs
 	 * @param num how many MEs to select
 	 * @param unique if they must be unique
 	 * @return a random list of MEs
 	 */
 	public List<EObject> getRandomMEs(Project project, int num, boolean unique) {
 
 		List<EObject> result = new ArrayList<EObject>();
 		if (allMEsInProject == null) {
 			System.out.println("getting list of all model elements in project...");
 			allMEsInProject = project.getAllModelElements();
 			System.out.println(allMEsInProject.size() + " MEs in project...");
 		}
 
 		int numOfMEs = allMEsInProject.size();
 		if (num > numOfMEs) {
 			throw new IllegalArgumentException(
 				"Number of random MEs to return is greater than total number of MEs in project.");
 		}
 
 		if (unique) {
 			do {
 				final EObject me = (EObject) allMEsInProject.toArray()[getRandom().nextInt(numOfMEs - 1)];
 				if (!result.contains(me)) {
 					result.add(me);
 				}
 
 			} while (result.size() < num);
 
 		} else {
 			for (int i = 0; i < num; i++) {
 				final EObject me = (EObject) allMEsInProject.toArray()[getRandom().nextInt(numOfMEs - 1)];
 				result.add(me);
 			}
 
 		}
 		return result;
 	}
 
 	/**
 	 * Returns a randomly selected ME form this project.
 	 * 
 	 * @param project project
 	 * @return randomly selected ME
 	 */
 	public EObject getRandomME(Project project) {
 		List<EObject> modelElements = getRandomMEs(project, 1, false);
 		return modelElements.get(0);
 	}
 
 	/**
 	 * Returns a randomly selected ME of given type from this project. If there is no ME of this type in project null is
 	 * returned.
 	 * 
 	 * @param project project
 	 * @param type model element type
 	 * @return ME or null if there is no ME of this type in project
 	 */
 	public EObject getRandomMEofType(Project project, EClass type) {
 
 		List<EObject> refTypeMEs = project.getAllModelElementsbyClass(type, new BasicEList<EObject>());
 
 		int size = refTypeMEs.size();
 		if (size == 0) {
 			return null;
 			// throw new IllegalStateException("There is no ME of this type in Project: " + type.getName());
 		}
 
 		EObject me = refTypeMEs.get(getRandomPosition(size));
 		return me;
 	}
 
 	/**
 	 * Creates an instance of model element with a randomly selected model element type.
 	 * 
 	 * @return ME
 	 */
 	public EObject createRandomME() {
 		Set<EClass> eClazzSet = getSubclasses(EcoreFactory.eINSTANCE.getEcorePackage().getEObject());
 		ArrayList<EClass> eClazz = new ArrayList<EClass>(eClazzSet);
 		EClass eClass = eClazz.get(getRandom().nextInt(eClazz.size() - 1));
 		EObject me = eClass.getEPackage().getEFactoryInstance().create(eClass);
 
 		return me;
 	}
 
 	/**
 	 * Returns all non-abstract, non-interface subclasses of the given input class in the given package. In other words
 	 * returns all classes that have direct instances.
 	 * 
 	 * @param clazz the eClass, must be a subtype of ModelElement
 	 * @return a set of EClasses IMPORTANT: Will throw an IllegalArgumentException if given EClass is not a subtype of
 	 *         ModelElement
 	 */
 	public static Set<EClass> getSubclasses(EClass clazz) {
 		return getSubclasses(clazz, false);
 	}
 
 	/**
 	 * Returns all subclasses of the given input class in the given package.
 	 * 
 	 * @param clazz the eClass, must be a subtype of ModelElement
 	 * @param includeAbstractClassesAndInterfaces true if interfaces and abstract classes should be included in the
 	 *            result
 	 * @return a set of EClasses IMPORTANT: Will throw an IllegalArgumentException if given EClass is not a subtype of
 	 *         ModelElement
 	 */
 	public static Set<EClass> getSubclasses(EClass clazz, boolean includeAbstractClassesAndInterfaces) {
 		// sanity checks
 		// EClass modelELementEClass = MetamodelPackage.eINSTANCE.getModelElement();
 		// if (!modelELementEClass.isSuperTypeOf(clazz)) {
 		// throw new IllegalStateException("Given EClass \"" + clazz.getName()
 		// + "\" is not a subtype of EClass ModelElement");
 		// }
 
 		Set<EClass> ret = new HashSet<EClass>();
 		for (EPackage ePackage : getAllModelPackages()) {
 			getSubclasses(clazz, ret, ePackage, includeAbstractClassesAndInterfaces);
 		}
 		return ret;
 	}
 
 	private static void getSubclasses(EClass clazz, Set<EClass> ret, EPackage ePackage,
 		boolean includeAbstractClassesAndInterfaces) {
 
 		for (EClassifier classifier : ePackage.getEClassifiers()) {
 			if (EcorePackage.eINSTANCE.getEClass().isInstance(classifier)) {
 				EClass subClass = (EClass) classifier;
 				if ((clazz.isSuperTypeOf(subClass) || clazz == EcorePackage.eINSTANCE.getEObject())
 					&& (includeAbstractClassesAndInterfaces || canHaveInstances(subClass))) {
 					ret.add(subClass);
 				}
 			}
 		}
 		for (EPackage subPackage : ePackage.getESubpackages()) {
 			getSubclasses(clazz, ret, subPackage, includeAbstractClassesAndInterfaces);
 		}
 	}
 
 	private static boolean canHaveInstances(EClass eClass) {
 		return !(eClass.isAbstract() || eClass.isInterface());
 	}
 
 	/**
 	 * Retrieve all EPackages that are model packages for unicase starting with the unicase model prefix as defined in
 	 * {@link MetamodelPackage}.
 	 * 
 	 * @return a set of EPackages
 	 */
 	public static Set<EPackage> getAllModelPackages() {
 		Set<EPackage> result = new HashSet<EPackage>();
 		Registry registry = EPackage.Registry.INSTANCE;
 
 		for (Entry<String, Object> entry : registry.entrySet()) {
 			// if (entry.getKey().startsWith(ModelPackage..MODEL_URL_PREFIX)) {
 			// try {
 			EPackage model = EPackage.Registry.INSTANCE.getEPackage(entry.getKey());
 			result.add(model);
 			// }
 			// BEGIN SUPRESS CATCH EXCEPTION
 			// catch (RuntimeException exception) {
 			// END SUPRESS CATCH EXCEPTION
 			// logException("Failed to load model package " + entry.getKey(), exception);
 			// }
 			// }
 		}
 		return result;
 	}
 
 	/**
 	 * This creates a mew model element of given type. If type is abstract or interface a randomly selected sub-type of
 	 * it will be instantiated.
 	 * 
 	 * @param refType EReference type
 	 * @return a new model element instance of refType
 	 */
 	public EObject createInstance(EClass refType) {
 
 		EObject ret = null;
 
 		if (refType.isAbstract() || refType.isInterface()) {
 			List<EClass> eClazz = new ArrayList<EClass>(getSubclasses(refType));
 			int index = getRandomPosition(eClazz.size());
 			refType = eClazz.get(index);
 		}
 
 		ret = refType.getEPackage().getEFactoryInstance().create(refType);
 
 		return ret;
 
 	}
 
 	/**
 	 * This changes the given attribute on given ME. If attribute isMany then a new entry of attribute type will be
 	 * added to a random position of its list. Also if attribute is an Enum, then a new value from this enumeration will
 	 * be set for it. Note that new values are all selected randomly (except for strings).
 	 * 
 	 * @param me model element
 	 * @param attribute attribute to change
 	 */
 	@SuppressWarnings("unchecked")
 	public void changeAttribute(EObject me, EAttribute attribute) {
 
 		if (attribute.getEType().getInstanceClass().equals(String.class)) {
 			if (attribute.isMany()) {
 				Object object = me.eGet(attribute);
 				EList<String> eList = (EList<String>) object;
 				int position = getRandomPosition(eList.size());
 				eList.add(position, "new entry for" + attribute.getName());
 
 			} else {
 				String oldValue = (String) me.eGet(attribute);
 				String newValue = "changed-" + oldValue;
 				me.eSet(attribute, newValue);
 			}
 
 		} else if (attribute.getEType().getInstanceClass().equals(boolean.class)) {
 			if (attribute.isMany()) {
 				Object object = me.eGet(attribute);
 				EList<Boolean> eList = (EList<Boolean>) object;
 				int position = getRandomPosition(eList.size());
 				eList.add(position, getRandom().nextBoolean());
 			} else {
 				me.eSet(attribute, !((Boolean) me.eGet(attribute)));
 			}
 
 		} else if (attribute.getEType().getInstanceClass().equals(int.class)) {
 			if (attribute.isMany()) {
 				Object object = me.eGet(attribute);
 				EList<Integer> eList = (EList<Integer>) object;
 				int position = getRandomPosition(eList.size());
 				eList.add(position, getRandom().nextInt());
 			} else {
 				me.eSet(attribute, getRandom().nextInt());
 			}
 
 		} else if (attribute.getEType().getInstanceClass().equals(Date.class)) {
 			if (attribute.isMany()) {
 				Object object = me.eGet(attribute);
 				EList<Date> eList = (EList<Date>) object;
 				int position = getRandomPosition(eList.size());
 				eList.add(position, getRandomDate());
 			} else {
 				me.eSet(attribute, getRandomDate());
 			}
 
 		}
 		if (attribute.getEType() instanceof EEnum) {
 			EEnum en = (EEnum) attribute.getEType();
 			int numOfLiterals = en.getELiterals().size();
 			int index = getRandomPosition(numOfLiterals);
 			EEnumLiteral value = en.getELiterals().get(index);
 			me.eSet(attribute, value.getInstance());
 		}
 
 	}
 
 	/**
 	 * Returns a random position in a list. If list size is 0, 0 is returned.
 	 * 
 	 * @param listSize list size
 	 * @return random index
 	 */
 	public int getRandomPosition(int listSize) {
 
 		int position;
 		if (listSize == 0) {
 			position = 0;
 		} else {
 			position = getRandom().nextInt(listSize);
 		}
 		return position;
 	}
 
 	/**
 	 * Returns random generator of this TestHelper.
 	 * 
 	 * @return random
 	 */
 	public Random getRandom() {
 		return random;
 	}
 
 	/**
 	 * get a random date.
 	 * 
 	 * @return ranodm date
 	 */
 	private static Date getRandomDate() {
 		return new Date();
 	}
 
 	/**
 	 * Returns randomly one of attributes of this ME. The returned attribute is changeable, is not
 	 * MODEL_ELEMENT_IDENTIFIER, and is not transient. Returns null if this ME does not have any attributes.
 	 * 
 	 * @param me ME
 	 * @return a random selected attribute
 	 */
 	public EAttribute getRandomAttribute(EObject me) {
 		EAttribute attribute = null;
 		List<EAttribute> attributes = new ArrayList<EAttribute>();
 		for (EAttribute tmpAttr : me.eClass().getEAllAttributes()) {
 			if (tmpAttr.isChangeable() && !tmpAttr.isTransient()) {
 				attributes.add(tmpAttr);
 			}
 		}
 
 		int size = attributes.size();
 		if (size != 0) {
 			attribute = attributes.get(getRandomPosition(size));
 
 		}
 
 		return attribute;
 
 	}
 
 	/**
 	 * returns a random reference of this ME. Returns null if this ME does not have any references.
 	 * 
 	 * @param me model element
 	 * @return random reference of this ME
 	 */
 	public EReference getRandomReference(EObject me) {
 		int size = me.eClass().getEAllReferences().size();
 		int position = getRandomPosition(size);
 		EReference ref = null;
 		if (size != 0) {
 			ref = me.eClass().getEAllReferences().get(position);
 		}
 
 		return ref;
 	}
 
 	/**
 	 * Returns a randomly selected non-containment reference of this ME. It is changeable, and not transient. Returns
 	 * null if this ME does not have any matching references.
 	 * 
 	 * @param me ME
 	 * @return randomly selected changeable non-transient non-containment reference of this ME
 	 */
 	public EReference getRandomNonContainmentRef(EObject me) {
 		EReference nonContainmentRef = null;
 		List<EReference> nonContainmentRefs = new ArrayList<EReference>();
 		for (EReference ref : me.eClass().getEAllReferences()) {
 			if (!ref.isContainment() && !ref.isContainer() && ref.isChangeable() && !ref.isTransient()) {
 				nonContainmentRefs.add(ref);
 			}
 		}
 
 		int size = nonContainmentRefs.size();
 		if (size != 0) {
 			nonContainmentRef = nonContainmentRefs.get(getRandomPosition(size));
 		}
 
 		return nonContainmentRef;
 
 	}
 
 	/**
 	 * Returns a randomly selected containment reference of this ME. It is changeable and not transient. Returns null if
 	 * this ME does not have any matching references.
 	 * 
 	 * @param me ME
 	 * @return a randomly selected containment reference of this ME. It is changeable and not transient
 	 */
 	public EReference getRandomContainmentRef(EObject me) {
 		EReference containmentRef = null;
 		List<EReference> containments = new ArrayList<EReference>();
 		for (EReference ref : me.eClass().getEAllContainments()) {
 			if (ref.isChangeable() && !ref.isTransient()) {
 				containments.add(ref);
 			}
 		}
 		int size = containments.size();
 		if (size != 0) {
 			containmentRef = containments.get(getRandomPosition(size));
 		}
 
 		return containmentRef;
 	}
 
 	/**
 	 * This method takes a model element and a non-containment reference. It gathers all model elements in project which
 	 * have the type of that reference. Takes one of these in random, and adds it to reference list of input model
 	 * element (in a random position if ref is many).
 	 * 
 	 * @param me the input model element
 	 * @param ref the non-containment reference to change
 	 * @param project the project
 	 * @return model element which is added to this reference
 	 */
 	@SuppressWarnings("unchecked")
 	public EObject changeNonContainementRef(EObject me, EReference ref, Project project) {
 
 		EClass refType = ref.getEReferenceType();
 		List<EObject> refTypeMEs = project.getAllModelElementsbyClass(refType, new BasicEList<EObject>());
 
 		if (refTypeMEs.contains(me)) {
 			refTypeMEs.remove(me);
 		}
 
 		EObject toBeReferencedME = refTypeMEs.get(getRandomPosition(refTypeMEs.size()));
 
 		Object object = me.eGet(ref);
 		if (ref.isMany()) {
 			EList<EObject> eList = (EList<EObject>) object;
 			if (eList == null) {
 				throw new IllegalStateException("Null list return for feature " + ref.getName() + " on "
 					+ ModelUtil.getProject(me).getModelElementId(me).getId());
 			} else {
 				int position = getRandomPosition(eList.size());
 				eList.add(position, toBeReferencedME);
 			}
 		} else {
 			me.eSet(ref, toBeReferencedME);
 		}
 
 		return toBeReferencedME;
 
 	}
 
 	/**
 	 * Adds the given model element to this non-containment reference of given ME. If reference is not many, the given
 	 * model element replaces the previous value of this reference.
 	 * 
 	 * @param me me model element to change its reference
 	 * @param ref non-containment reference
 	 * @param toBeReferencedME model element to reference
 	 */
 	@SuppressWarnings("unchecked")
 	public void changeReference(EObject me, EReference ref, EObject toBeReferencedME) {
 		Object object = me.eGet(ref);
 		if (ref.isMany()) {
 			EList<EObject> eList = (EList<EObject>) object;
 			if (eList == null) {
 				throw new IllegalStateException("Null list return for feature " + ref.getName() + " on "
 					+ ModelUtil.getProject(me).getModelElementId(me).getId());
 			} else {
 				int position = getRandomPosition(eList.size());
 				eList.add(position, toBeReferencedME);
 			}
 		} else {
 			me.eSet(ref, toBeReferencedME);
 		}
 
 	}
 
 	/**
 	 * Changes the index of a value in a multi-valued EAtrribute of this ME.
 	 * 
 	 * @param me ME model element to change
 	 * @param attribute an attribute with multiple values (isMany = true)
 	 */
 	public void moveMultiAttributeValue(EObject me, EAttribute attribute) {
 		if (!attribute.isMany()) {
 			throw new IllegalArgumentException("Given attribute must be multiple valued (isMany = true)");
 		}
 
 		Object object = me.eGet(attribute);
 		EList<?> eList = (EList<?>) object;
 		int position1 = getRandomPosition(eList.size());
 		int position2 = getRandomPosition(eList.size());
 		if (position1 == position2) {
 			return;
 		}
 		Collections.swap(eList, position1, position2);
 
 	}
 
 	/**
 	 * Changes index of a reference in a many EReference of given ME.
 	 * 
 	 * @param me ME
 	 * @param ref EReference to change
 	 */
 	@SuppressWarnings("unchecked")
 	public void moveMultiReferenceValue(EObject me, EReference ref) {
 		if (!ref.isMany()) {
 			throw new IllegalArgumentException("Given reference must be multiple valued (isMany = true)");
 		}
 		Object object = me.eGet(ref);
 		EList<EObject> eList = (EList<EObject>) object;
 		if (eList == null) {
 			throw new IllegalStateException("Null list return for feature " + ref.getName() + " on "
 				+ ModelUtil.getProject(me).getModelElementId(me).getId());
 		} else {
 			int position1 = getRandomPosition(eList.size());
 			int position2 = getRandomPosition(eList.size());
 			if (position1 == position2) {
 				return;
 			}
 			Collections.swap(eList, position1, position2);
 		}
 
 	}
 
 	/**
 	 * Compares test project and compare project using their serialized strings.
 	 * 
 	 * @param testProject test project
 	 * @param compareProject compare project
 	 * @param testName test name
 	 * @return if serialized string from projects are equal
 	 * @throws SerializationException SerializationException
 	 */
 	public static boolean areEqual(Project testProject, Project compareProject, String testName)
 		throws SerializationException {
 		LOGGER.log(Level.INFO, "examining the equlity of test project and the checked out compare project...");
 		String strTestProj = eObjectToString(testProject, testName + "-test", true);
 		String strCompareProj = eObjectToString(compareProject, testName + "-compare", true);
 		return strTestProj.equals(strCompareProj);
 	}
 
 	/**
 	 * @return the testProject
 	 */
 	public Project getTestProject() {
 		return testProject;
 	}
 
 	/**
 	 * Takes a random ME (meA). Takes randomly one of its containment references. Creates a new ME matching containment
 	 * reference type (meB). Adds created meB to meA's containment reference.
 	 */
 	@SuppressWarnings("unchecked")
 	public void doContainemntReferenceAddNew() {
 		// get a random ME and one of its containment references
 		EObject me = getRandomME(testProject);
 		EReference refToChange = getRandomContainmentRef(me);
 
 		// Maybe this ME does not have any containment references. Then choose another one.
 		while (refToChange == null) {
 			me = getRandomME(testProject);
 			refToChange = getRandomContainmentRef(me);
 		}
 
 		EClass refType = refToChange.getEReferenceType();
 
 		// create a new instance of reference type
 		EObject newInstance = createInstance(refType);
 
 		if (newInstance == null) {
 			throw new IllegalStateException("could not create a model element of specified type.");
 		}
 
 		Object object = me.eGet(refToChange);
 		if (refToChange.isMany()) {
 			EList<EObject> eList = (EList<EObject>) object;
 
 			if (eList == null) {
 				throw new IllegalStateException("Null list return for feature " + refToChange.getName() + " on "
 					+ ModelUtil.getProject(me).getModelElementId(me).getId());
 			} else {
 				int position = getRandomPosition(eList.size());
 				eList.add(position, newInstance);
 			}
 
 		} else {
 			me.eSet(refToChange, newInstance);
 		}
 
 	}
 
 	/**
 	 * 1. Get a random model element form test project; 2. get randomly one of its attributes. 3. change the attribute
 	 */
 	public void doChangeAttribute() {
 		EObject me = getRandomME(getTestProject());
 		EAttribute attributeToChange = getRandomAttribute(me);
 		changeAttribute(me, attributeToChange);
 
 	}
 
 	/**
 	 * Select a random ME (meA). Select one of its non-containment references. Find an ME matching reference type (meB).
 	 * Add meB to meA.
 	 */
 	public void doNonContainmentReferenceAdd() {
 
 		EObject meToReference = null;
 		EObject me = null;
 		EReference refToChange = null;
 
 		while (meToReference == null) {
 			me = getRandomME(getTestProject());
 			refToChange = getRandomNonContainmentRef(me);
 
 			while (refToChange == null) {
 				me = createRandomME();
 				refToChange = getRandomNonContainmentRef(me);
 			}
 
 			meToReference = getRandomMEofType(getTestProject(), refToChange.getEReferenceType());
 
 		}
 
 		changeReference(me, refToChange, meToReference);
 
 	}
 
 	/**
 	 * Change the same attribute on a randomly selected ME twice.
 	 */
 	public void doAttributeTransitiveChange() {
 		EObject me = getRandomME(getTestProject());
 		EAttribute attributeToChange = getRandomAttribute(me);
 
 		// from unset or a to b
 		changeAttribute(me, attributeToChange);
 
 		// from b to c
 		changeAttribute(me, attributeToChange);
 
 	}
 
 	/**
 	 * This move an element in a many reference list to another position.
 	 */
 	public void doMultiReferenceMove() {
 		EObject me = getRandomME(getTestProject());
 		EReference refToChange = getRandomReference(me);
 
 		while (refToChange == null || !refToChange.isMany()) {
 			me = getRandomME(getTestProject());
 			refToChange = getRandomReference(me);
 		}
 
 		moveMultiReferenceValue(me, refToChange);
 
 	}
 
 	/**
 	 * Delete a random ME.
 	 */
 	public void doDelete() {
 
 		EObject me = getRandomME(getTestProject());
 		ModelUtil.getProject(me).deleteModelElement(me);
 
 	}
 
 	/**
 	 * 
 	 */
 	public void doContainmentReferenceMove() {
 
 		EObject meToMove = null;
 		EObject me = null;
 		EReference refToChange = null;
 
 		while (meToMove == null) {
 			// get a random ME and one of its containment references (refToChange)
 			me = getRandomME(testProject);
 			refToChange = getRandomContainmentRef(me);
 
 			// Maybe this ME does not have any containment references. Then choose another one.
 			while (refToChange == null) {
 				me = getRandomME(testProject);
 				refToChange = getRandomContainmentRef(me);
 			}
 			// get a random ME matching refToChange. Also take care that meToMove is not the same as or an ancestor of
 			// ME
 			meToMove = getRandomMEofType(getTestProject(), refToChange.getEReferenceType());
 			if (meToMove != null && (meToMove.equals(me) || EcoreUtil.isAncestor(meToMove, me))) {
 				meToMove = null;
 			}
 		}
 
 		changeReference(me, refToChange, meToMove);
 
 	}
 
 	/**
 	 * This takes a random model element (meA). Takes one of its containments (meToMove). Takes containing reference of
 	 * meToMove. Finds another ME of type meA (meB). Moves meToMove to meB. Finds yet another ME of type meA (meC) .
 	 * Moves meToMove to meC.
 	 */
 	public void doContainmentRefTransitiveChange() {
 		EObject meA = null;
 		EObject meB = null;
 		EObject meC = null;
 		EObject meToMove = null;
 		EReference refToChange = null;
 
 		while (refToChange == null || meB == null || meC == null) {
 			while (meToMove == null) {
 				// get a random ME and one of its containment references (refToChange)
 				meA = getRandomME(testProject);
 				int contentsSize = meA.eContents().size();
 				if (contentsSize != 0) {
 					meToMove = meA.eContents().get(getRandomPosition(contentsSize));
 				}
 
 			}
 			refToChange = meToMove.eContainmentFeature();
 			meB = getRandomMEofType(testProject, meA.eClass());
 			meC = getRandomMEofType(testProject, meA.eClass());
 			if (meA.equals(meB) || meA.equals(meC) || meB.equals(meC)) {
 				refToChange = null;
 			}
 			if (!sanityCheckContainmentRefTransitiveChange(meB, meC, meToMove, refToChange)) {
 				refToChange = null;
 			}
 
 		}
 
 		changeReference(meB, refToChange, meToMove);
 		changeReference(meC, refToChange, meToMove);
 	}
 
 	/**
 	 * @param meA
 	 * @param meB
 	 * @param meC
 	 * @param meToMove
 	 * @param refToChange
 	 * @return
 	 */
 	private boolean sanityCheckContainmentRefTransitiveChange(EObject meB, EObject meC, EObject meToMove,
 		EReference refToChange) {
 
 		if (meToMove == null) {
 			return false;
 		}
 
 		if (meToMove.equals(meB) || EcoreUtil.isAncestor(meToMove, meB)) {
 			return false;
 		}
 
 		if (meToMove.equals(meC) || EcoreUtil.isAncestor(meToMove, meC)) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * create a random ME and change one of its attributes.
 	 */
 	public void doCreateAndChangeAttribute() {
 		EObject me = createRandomME();
 		EAttribute attributeToChange = getRandomAttribute(me);
 
 		while (attributeToChange == null) {
 			me = createRandomME();
 			attributeToChange = getRandomAttribute(me);
 		}
 
 		testProject.getModelElements().add(me);
 		changeAttribute(me, attributeToChange);
 
 	}
 
 	/**
 	 * Create a random ME and change one of its non-containment references.
 	 */
 	public void doCreateAndChangeRef() {
 
 		EObject meToReference = null;
 		EObject me = null;
 		EReference refToChange = null;
 
 		while (meToReference == null) {
 			me = createRandomME();
 			refToChange = getRandomNonContainmentRef(me);
 
 			while (refToChange == null) {
 				me = createRandomME();
 				refToChange = getRandomNonContainmentRef(me);
 			}
 
 			meToReference = getRandomMEofType(getTestProject(), refToChange.getEReferenceType());
 		}
 
 		testProject.getModelElements().add(me);
 		changeReference(me, refToChange, meToReference);
 
 	}
 
 	/**
 	 * Create a random ME and change one of its attributes, then changes one of its references, then changes one of its
 	 * attributes, and again changes one of its references.
 	 */
 	public void doCreateAndMultipleChange() {
 		EObject me = null;
 		EAttribute attr1 = null;
 		EAttribute attr2 = null;
 		EReference ref1 = null;
 		EReference ref2 = null;
 		EObject meToRef1 = null;
 		EObject meToRef2 = null;
 
 		while (!sanityCheckCreateAndMultipleChange(me, ref1, meToRef1, ref2, meToRef2)) {
 			meToRef1 = null;
 			meToRef2 = null;
 			me = createRandomME();
 			ref1 = getRandomReference(me);
 			ref2 = getRandomReference(me);
 			attr1 = getRandomAttribute(me);
 			attr2 = getRandomAttribute(me);
 
 			if (ref1 == null || ref2 == null || attr1 == null || attr2 == null) {
 				continue;
 			}
 			meToRef1 = getRandomMEofType(getTestProject(), ref1.getEReferenceType());
 			meToRef2 = getRandomMEofType(getTestProject(), ref2.getEReferenceType());
 
 		}
 
 		getTestProject().getModelElements().add(me);
 		changeAttribute(me, attr1);
 		changeReference(me, ref1, meToRef1);
 		changeAttribute(me, attr2);
 		changeReference(me, ref2, meToRef2);
 
 	}
 
 	/**
 	 * @param me
 	 * @param ref1
 	 * @param meToRef1
 	 * @param ref2
 	 * @param meToRef2
 	 * @return
 	 */
 	private boolean sanityCheckCreateAndMultipleChange(EObject me, EReference ref1, EObject meToRef1, EReference ref2,
 		EObject meToRef2) {
 
 		if (ref1 == null || ref2 == null) {
 			return false;
 		}
 
 		if (meToRef1 == null || meToRef2 == null) {
 			return false;
 		}
 
 		if (ref1.isContainment() && (meToRef1.equals(me) || EcoreUtil.isAncestor(meToRef1, me))) {
 			return false;
 		}
 
 		if (ref2.isContainment() && (meToRef2.equals(me) || EcoreUtil.isAncestor(meToRef2, me))) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Create a random ME. Change one of its non-containment references. Delete ME.
 	 */
 	public void doCreateChangeRefDelete() {
 
 		EObject meToReference = null;
 		EObject me = null;
 		EReference refToChange = null;
 
 		while (meToReference == null) {
 			me = createRandomME();
 			refToChange = getRandomNonContainmentRef(me);
 			while (refToChange == null) {
 				me = createRandomME();
 				refToChange = getRandomNonContainmentRef(me);
 			}
 			meToReference = getRandomMEofType(getTestProject(), refToChange.getEReferenceType());
 		}
 
 		getTestProject().getModelElements().add(me);
 		changeReference(me, refToChange, meToReference);
 		ModelUtil.getProject(me).deleteModelElement(me);
 	}
 
 	/**
 	 * Create a random ME. Delete ME.
 	 */
 	public void doCreateDelete() {
 
 		EObject me = createRandomME();
 		getTestProject().getModelElements().add(me);
 		getTestProject().deleteModelElement(me);
 
 	}
 
 	/**
 	 * Delete a random ME. Revert delete.
 	 */
 	public void doDeleteAndRevert() {
 
 		EObject modelElement = getRandomME(getTestProject());
 		getTestProject().deleteModelElement(modelElement);
 		List<AbstractOperation> operations = WorkspaceManager.getProjectSpace(testProject).getOperations();
		CreateDeleteOperation operation = (CreateDeleteOperation) operations.get(operations.size() - 1);
		CreateDeleteOperation reverse = (CreateDeleteOperation) operation.reverse();
 		reverse.apply(getTestProject());
 	}
 
 	/**
 	 * Removes a referenced model element form a non-containment reference of a randomly selected ME.
 	 */
 	@SuppressWarnings("unchecked")
 	public void doNonContainmentReferenceRemove() {
 
 		EObject me = getRandomME(getTestProject());
 
 		while (me.eCrossReferences().size() < 1) {
 			me = getRandomME(getTestProject());
 		}
 
 		int indexToRemove = getRandomPosition(me.eCrossReferences().size());
 		EObject meToRemove = me.eCrossReferences().get(indexToRemove);
 
 		EReference refToChange = findReference(me, meToRemove);
 
 		Object object = me.eGet(refToChange);
 		if (refToChange.isMany()) {
 			EList<EObject> eList = (EList<EObject>) object;
 			eList.remove(meToRemove);
 		} else {
 			me.eSet(refToChange, null);
 		}
 
 	}
 
 	/**
 	 * This finds an EReference on modelElement that matches referencedME.
 	 * 
 	 * @param modelElement
 	 * @param referencedME
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private EReference findReference(EObject modelElement, EObject referencedME) {
 
 		List<EReference> refsMatchingReferencedME = new ArrayList<EReference>();
 		for (EReference ref : modelElement.eClass().getEAllReferences()) {
 			if (!(ref.isContainer() || ref.isContainment())
 				&& (ref.getEReferenceType().equals(referencedME.eClass()) || ref.getEReferenceType().isSuperTypeOf(
 					referencedME.eClass())) || ref.getEReferenceType().equals(EcorePackage.eINSTANCE.getEObject())) {
 				refsMatchingReferencedME.add(ref);
 			}
 		}
 
 		if (refsMatchingReferencedME.size() == 1) {
 			return refsMatchingReferencedME.get(0);
 		}
 
 		for (EReference ref : refsMatchingReferencedME) {
 			Object object = modelElement.eGet(ref);
 			if (object == null) {
 				continue;
 			}
 			if (ref.isMany()) {
 				EList<EObject> eList = (EList<EObject>) object;
 				if (eList.contains(referencedME)) {
 					return ref;
 				}
 			} else {
 				if (modelElement.eGet(ref).equals(referencedME)) {
 					return ref;
 				}
 			}
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Finds an attribute with isMany = true and moves elements inside this attribute.
 	 */
 	public void doMultiAttributeMove() {
 
 		EObject me = getRandomME(getTestProject());
 		EAttribute attributeToChange = getRandomAttribute(me);
 		int tries = 0;
 		// since isMany() attributes are very rare, we try for limited times to find one.
 		while (attributeToChange == null || !attributeToChange.isMany()) {
 			me = createRandomME();
 			attributeToChange = getRandomAttribute(me);
 
 			tries++;
 			if (tries > 2000) {
 				return;
 			}
 		}
 
 		moveMultiAttributeValue(me, attributeToChange);
 
 	}
 }
