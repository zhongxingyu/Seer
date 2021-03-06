 /*******************************************************************************
  * Copyright (c) 2007 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.seam.core.test;
 
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.preference.IPersistentPreferenceStore;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.jboss.tools.seam.core.ISeamProject;
 import org.jboss.tools.seam.core.SeamCorePlugin;
 import org.jboss.tools.seam.core.SeamPreferences;
 import org.jboss.tools.seam.internal.core.SeamProject;
 import org.jboss.tools.test.util.JUnitUtils;
 import org.jboss.tools.test.util.xpl.EditorTestHelper;
 
 public class SeamValidatorsTest extends TestCase {
 	IProject project = null;
 
 	public SeamValidatorsTest() {}
 
 	protected void setUp() throws Exception {
 		project = ResourcesPlugin.getWorkspace().getRoot().getProject("SeamWebWarTestProject");
 		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
 		EditorTestHelper.joinJobs(5000, 20000, 1000);
 	}
 
 	private ISeamProject getSeamProject(IProject project) {
 		refreshProject(project);
 		
 		ISeamProject seamProject = null;
 		try {
 			seamProject = (ISeamProject)project.getNature(SeamProject.NATURE_ID);
 		} catch (Exception e) {
 			JUnitUtils.fail("Cannot get seam nature.",e);
 		}
 		assertNotNull("Seam project is null", seamProject);
 		return seamProject;
 	}
 
 	public void testComponentsValidator() {
 		ISeamProject seamProject = getSeamProject(project);
 		
 		IFile bbcComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.java");
 		IFile statefulComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java");
 		IFile componentsFile = project.getFile("WebContent/WEB-INF/components.xml");
 		
 		int number = getMarkersNumber(bbcComponentFile);
 		assertTrue("Problem marker was found in BbcComponent.java file", number == 0);
 
 		number = getMarkersNumber(statefulComponentFile);
 		assertTrue("Problem marker was found in StatefulComponent.java file", number == 0);
 
 		number = getMarkersNumber(componentsFile);
 		assertTrue("Problem marker was found in components.xml file", number == 0);
 
 		// Duplicate component name
 		System.out.println("Test - Duplicate component name");
 		
 		IFile bbcComponentFile2 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.2");
 		try{
 			bbcComponentFile.setContents(bbcComponentFile2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'BbcComponent.java' content to " +
 					"'BbcComponent.2'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(bbcComponentFile);
 			assertFalse("Problem marker 'Duplicate component name' not found", number == 0);
 		
 		String[] messages = getMarkersMessage(bbcComponentFile);
 		
 		assertTrue("Problem marker 'Duplicate component name' not found","Duplicate component name: abcComponent".equals(messages[0]));
 		
 		int[] lineNumbers = getMarkersNumbersOfLine(bbcComponentFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 7);
 		
 		// Stateful component does not contain @Remove method
 		System.out.println("Test - Stateful component does not contain @Remove method");
 		
 		IFile statefulComponentFile2 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.2");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.2'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Stateful component does not contain @Remove method' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Stateful component does not contain @Remove method' not found", "Stateful component \"statefulComponent\" must have a method marked @Remove".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 16);
 		
 		// Stateful component does not contain @Destroy method
 		System.out.println("Test - Stateful component does not contain @Destroy method");
 		
 		IFile statefulComponentFile3 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.3");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile3.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.3'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Stateful component does not contain @Destroy method' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Stateful component does not contain @Destroy method' not found", "Stateful component \"statefulComponent\" must have a method marked @Destroy".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 16);
 		
 		// Stateful component has wrong scope
 		System.out.println("Test - Stateful component has wrong scope");
 		
 		IFile statefulComponentFile4 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.4");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile4.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.4'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Stateful component has wrong scope' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Stateful component has wrong scope' not found", "Stateful component \"statefulComponent\" should not have org.jboss.seam.ScopeType.PAGE, nor org.jboss.seam.ScopeType.STATELESS".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 16);
 		
 		// Component class name cannot be resolved to a type
 		System.out.println("Test - Component class name cannot be resolved to a type");
 		
 		IFile componentsFile2 = project.getFile("WebContent/WEB-INF/components.2");
 		
 		try{
 			componentsFile.setContents(componentsFile2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'components.xml' content to " +
 					"'components.2'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(componentsFile);
 		assertFalse("Problem marker 'Component class name cannot be resolved to a type' was not found", number == 0);
 		
 		messages = getMarkersMessage(componentsFile);
 		assertTrue("Problem marker 'Component class name cannot be resolved to a type' was not found", "\"org.domain.SeamWebTestProject.session.StateComponent\" cannot be resolved to a type".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(componentsFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 15);
 
 		// Component class does not contain setter for property
 		System.out.println("Test - Component class does not contain setter for property");
 		
 		IFile componentsFile3 = project.getFile("WebContent/WEB-INF/components.3");
 		
 		try{
 			componentsFile.setContents(componentsFile3.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'components.xml' content to " +
 					"'components.3'", ex);
 		}
 		
 		IFile statefulComponentFile5 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.5");
 
 		try{
 			statefulComponentFile.setContents(statefulComponentFile5.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.5'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(componentsFile);
 		assertFalse("Problem marker 'Component class does not contain setter for property' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(componentsFile);
 		assertTrue("Problem marker 'Component class does not contain setter for property' not found", "Class \"StatefulComponent\" of component \"statefulComponent\" does not contain setter for property \"abc\"".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(componentsFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 16);
 		
 		// resolve error in BbcComponent.java
 		IFile bbcComponentFile3 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/BbcComponent.3");
 		try{
 			bbcComponentFile.setContents(bbcComponentFile3.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'BbcComponent.java' content to " +
 					"'BbcComponent.3'", ex);
 		}
 	}
 	
 	public void testEntitiesValidator() {
 		ISeamProject seamProject = getSeamProject(project);
 		
 		IFile abcEntityFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/entity/abcEntity.java");
 		
 		int number = getMarkersNumber(abcEntityFile);
 		assertTrue("Problem marker was found in abcEntity.java", number == 0);
 		
 		// Entity component has wrong scope
 		System.out.println("Test - Entity component has wrong scope");
 		
 		IFile abcEntityFile2 = project.getFile("src/action/org/domain/SeamWebWarTestProject/entity/abcEntity.2");
 		try{
 			abcEntityFile.setContents(abcEntityFile2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'abcEntity.java' content to " +
 					"'abcEntity.2'", ex);
 		}
 
 		refreshProject(project);
 
 		number = getMarkersNumber(abcEntityFile);
 		assertFalse("Problem marker 'Entity component has wrong scope' not found' not found' not found", number == 0);
 
 		String[] messages = getMarkersMessage(abcEntityFile);
 		assertTrue("Problem marker 'Entity component has wrong scope' not found", "Entity component \"abcEntity\" should not have org.jboss.seam.ScopeType.STATELESS".equals(messages[0]));
 
 		int[] lineNumbers = getMarkersNumbersOfLine(abcEntityFile);
 
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 15);
 	}
 
 	public void testComponentLifeCycleMethodsValidator() {
 		ISeamProject seamProject = getSeamProject(project);
 		IFile componentsFile = project.getFile("WebContent/WEB-INF/components.xml");
 
 		IFile statefulComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.java");
 
 		// Duplicate @Destroy method
 		System.out.println("Test - Duplicate @Destroy method");
 
 		IFile statefulComponentFile6 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.6");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile6.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.6'", ex);
 		}
 		
 		refreshProject(project);
 
 		int number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Duplicate @Destroy method' was not found", number == 0);
 		
 		String[] messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Duplicate @Destroy method' was not found", messages[0].startsWith("Duplicate @Destroy method \"destroyMethod"));
 
 		int[] lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Wrong number of problem markers", lineNumbers.length == messages.length && messages.length == 2);
 		
		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 34);
		assertTrue("Problem marker has wrong line number", lineNumbers[1] == 39);
 
 		
 		// Duplicate @Create method
 		System.out.println("Test - Duplicate @Create method");
 		
 		IFile statefulComponentFile7 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.7");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile7.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.7'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Duplicate @Create method' was not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Duplicate @Create method' was not found", messages[0].startsWith("Duplicate @Create method \"createMethod"));
 		
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Wrong number of problem markers", lineNumbers.length == messages.length && messages.length == 2);
 		
 		if(messages[1].indexOf("createMethod2") >= 0){
 			assertTrue("Problem marker has wrong line number", lineNumbers[0] == 33);
 			assertTrue("Problem marker has wrong line number", lineNumbers[1] == 40);
 		}else{
 			assertTrue("Problem marker has wrong line number", lineNumbers[0] == 40);
 			assertTrue("Problem marker has wrong line number", lineNumbers[1] == 33);
 			
 		}
 		
 		// Duplicate @Unwrap method
 		System.out.println("Test - Duplicate @Unwrap method");
 		
 		IFile statefulComponentFile8 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.8");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile8.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.8'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Duplicate @Unwrap method' was not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Duplicate @Unwrap method' was not found", messages[0].startsWith("Duplicate @Unwrap method \"unwrapMethod"));
 
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Wrong number of problem markers", lineNumbers.length == messages.length && messages.length == 2);
 		
 		if(messages[1].indexOf("unwrapMethod2") >= 0){
 			assertTrue("Problem marker has wrong line number", lineNumbers[0] == 39);
 			assertTrue("Problem marker has wrong line number", lineNumbers[1] == 44);
 		}else{
 			assertTrue("Problem marker has wrong line number", lineNumbers[0] == 44);
 			assertTrue("Problem marker has wrong line number", lineNumbers[1] == 39);
 			
 		}
 		
 		// Only component class can have @Destroy method
 		System.out.println("Test - Only component class can have @Destroy method");
 		
 		IFile componentsFile4 = project.getFile("WebContent/WEB-INF/components.4");
 		
 		try{
 			componentsFile.setContents(componentsFile4.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'components.xml' content to " +
 					"'components.4'", ex);
 		}
 		IFile statefulComponentFile9 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.9");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile9.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.9'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Only component class can have @Destroy method' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Only component class can have @Destroy method' not found", "Only component class can have @Destroy method \"destroyMethod\"".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 23);
 		
 		// Only component class can have @Create method
 		System.out.println("Test - Only component class can have @Create method");
 		
 		IFile statefulComponentFile10 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.10");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile10.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.10'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Only component class can have @Create method' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Only component class can have @Create method' not found", "Only component class can have @Create method \"createMethod\"".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 23);
 		
 		// Only component class can have @Unwrap method
 		System.out.println("Test - Only component class can have @Unwrap method");
 		
 		IFile statefulComponentFile11 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.11");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile11.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.11'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Only component class can have @Unwrap method' not found' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Only component class can have @Unwrap method' not found", "Only component class can have @Unwrap method \"unwrapMethod\"".equals(messages[0]));
 
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 23);
 		
 		// Only component class can have @Observer method
 		System.out.println("Test - Only component class can have @Observer method");
 		
 		IFile statefulComponentFile12 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.12");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile12.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.12'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Only component class can have @Observer method' not found' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Only component class can have @Observer method' not found", "Only component class can have @Observer method \"observerMethod\"".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 23);
 
 		// Duplicate @Remove method
 
 		IFile statefulComponentFile1 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/StatefulComponent.1");
 		try{
 			statefulComponentFile.setContents(statefulComponentFile1.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'StatefulComponent.java' content to " +
 					"'StatefulComponent.1'", ex);
 		}
 
 		refreshProject(project);
 
 		number = getMarkersNumber(statefulComponentFile);
 		assertFalse("Problem marker 'Duplicate @Remove method' not found", number == 0);
 
 		messages = getMarkersMessage(statefulComponentFile);
 		assertTrue("Problem marker 'Duplicate @Remove method' not found", messages[0].startsWith("Duplicate @Remove method \"removeMethod"));
 
 		lineNumbers = getMarkersNumbersOfLine(statefulComponentFile);
 
 		assertTrue("Wrong number of problem markers", lineNumbers.length == messages.length && messages.length == 2);
 
 		if(messages[1].indexOf("removeMethod2") >= 0){
 			assertTrue("Problem marker has wrong line number", lineNumbers[0] == 17);
 			assertTrue("Problem marker has wrong line number", lineNumbers[1] == 21);
 		}else{
 			assertTrue("Problem marker has wrong line number", lineNumbers[0] == 21);
 			assertTrue("Problem marker has wrong line number", lineNumbers[1] == 17);
 		}
 	}
 
 	/**
 	 * The validator should check duplicate @Remove methods only in stateful session bean component
 	 * This method tests usual component (not stateful sessian bean) with two @Remove methods. It must not have error markers.  
 	 */
 	public void testDuplicateRemoveMethodInComponent() {
 		getSeamProject(project);
 		IFile componentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/UsualComponent.java");
 		int number = getMarkersNumber(componentFile);
 		assertTrue("Problem marker was found in UsualComponent.java file", number == 0);
 	}
 
 	public void testFactoriesValidator() {
 		ISeamProject seamProject = getSeamProject(project);
 		
 		IFile Component12File = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/Component12.java");
 		
 		refreshProject(project);
 		
 		int number = getMarkersNumber(Component12File);
 		assertTrue("Problem marker was found in Component12.java", number == 0);
 
 		// Unknown factory name
 		System.out.println("Test - Unknown factory name");
 		
 		IFile Component12File2 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/Component12.2");
 		try{
 			Component12File.setContents(Component12File2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'Component12File2.java' content to " +
 					"'Component12File2.2'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(Component12File);
 		assertFalse("Problem marker 'Unknown factory name' not found' not found' not found' not found", number == 0);
 		
 		String[] messages = getMarkersMessage(Component12File);
 
 		assertTrue("Problem marker 'Unknown factory name' not found", "Factory method \"messageList2\" with a void return type must have an associated @Out/Databinder".equals(messages[0]));
 		
 		int[] lineNumbers = getMarkersNumbersOfLine(Component12File);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 24);
 	}
 	
 	public void testBijectionsValidator() {
 		ISeamProject seamProject = getSeamProject(project);
 		
 		IFile selectionTestFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionTest.java");
 		IFile selectionIndexTestFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionIndexTest.java");
 		
 		refreshProject(project);
 		
 		int number = getMarkersNumber(selectionTestFile);
 		assertTrue("Problem marker was found in SelectionIndexTest.java", number == 0);
 		
 		number = getMarkersNumber(selectionIndexTestFile);
 		assertTrue("Problem marker was found in SelectionIndexTest.java", number == 0);
 
 		// Multiple data binder
 		System.out.println("Test - Multiple data binder");
 		
 		IFile selectionTestFile2 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionTest.2");
 		try{
 			selectionTestFile.setContents(selectionTestFile2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'SelectionTest.java' content to " +
 					"'SelectionTest.2'", ex);
 		}
 
 		IFile selectionIndexTestFile2 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionIndexTest.2");
 		try{
 			selectionIndexTestFile.setContents(selectionIndexTestFile2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'SelectionIndexTest.java' content to " +
 					"'SelectionIndexTest.2'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(selectionTestFile);
 		assertFalse("Problem marker 'Multiple data binder' not found' not found' not found' not found", number == 0);
 
 		String[] messages = getMarkersMessage(selectionTestFile);
 		assertTrue("Problem marker 'Multiple data binder", messages[0].startsWith("@DataModelSelection and @DataModelSelectionIndex without name of the DataModel requires the only one @DataModel in the component"));
 
 		int[] lineNumbers = getMarkersNumbersOfLine(selectionTestFile);
 		
 		assertTrue("Wrong number of problem markers", lineNumbers.length == messages.length && messages.length == 2);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 21 || lineNumbers[0] == 24);
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 21 || lineNumbers[0] == 24);
 
 		number = getMarkersNumber(selectionIndexTestFile);
 		assertFalse("Problem marker 'Multiple data binder' not found' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(selectionIndexTestFile);
 		assertTrue("Problem marker 'Multiple data binder", messages[0].startsWith("@DataModelSelection and @DataModelSelectionIndex without name of the DataModel requires the only one @DataModel in the component"));
 
 		lineNumbers = getMarkersNumbersOfLine(selectionIndexTestFile);
 		
 		assertTrue("Wrong number of problem markers", lineNumbers.length == messages.length && messages.length == 2);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 21 || lineNumbers[0] == 24);
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 21 || lineNumbers[0] == 24);
 		
 		// Unknown @DataModel/@Out name
 		System.out.println("Test - Unknown @DataModel/@Out name");
 		
 		IFile selectionTestFile3 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionTest.3");
 		try{
 			selectionTestFile.setContents(selectionTestFile3.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'SelectionTest.java' content to " +
 					"'SelectionTest.3'", ex);
 		}
 
 		IFile selectionIndexTestFile3 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/SelectionIndexTest.3");
 		try{
 			selectionIndexTestFile.setContents(selectionIndexTestFile3.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'SelectionIndexTest.java' content to " +
 					"'SelectionIndexTest.3'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(selectionTestFile);
 		assertFalse("Problem marker 'Unknown @DataModel/@Out name' not found' not found' not found' not found", number == 0);
 
 		messages = getMarkersMessage(selectionTestFile);
 		assertTrue("Problem marker 'Unknown @DataModel/@Out name", messages[0].startsWith("Unknown @DataModel/@Out name: messageList2"));
 
 		lineNumbers = getMarkersNumbersOfLine(selectionTestFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 27);
 		
 		number = getMarkersNumber(selectionIndexTestFile);
 		assertFalse("Problem marker 'Unknown @DataModel/@Out name' not found' not found' not found' not found", number == 0);
 
 		messages = getMarkersMessage(selectionIndexTestFile);
 		assertTrue("Problem marker 'Unknown @DataModel/@Out name", messages[0].startsWith("Unknown @DataModel/@Out name: messageList2"));
 		
 		lineNumbers = getMarkersNumbersOfLine(selectionIndexTestFile);
 
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 27);
 	}
 
 	public void testContextVariablesValidator() {
 		modifyPreferences();
 		IPreferenceStore store = SeamCorePlugin.getDefault().getPreferenceStore();
 		System.out.println("UNKNOWN_EL_VARIABLE_NAME value- "+store.getString(SeamPreferences.UNKNOWN_EL_VARIABLE_NAME));
 
 		ISeamProject seamProject = getSeamProject(project);
 		
 		IFile contextVariableTestFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.java");
 		
 		refreshProject(project);
 		
 		int number = getMarkersNumber(contextVariableTestFile);
 		assertTrue("Problem marker was found in contextVariableTestFile.java", number == 0);
 		
 		// Duplicate variable name
 		System.out.println("Test - Duplicate variable name");
 		
 		IFile contextVariableTestFile2 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.2");
 		try{
 			contextVariableTestFile.setContents(contextVariableTestFile2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'ContextVariableTest.java' content to " +
 					"'ContextVariableTest.2'", ex);
 		}
 		
 		refreshProject(project);
 		
 		String[] messages = getMarkersMessage(contextVariableTestFile);
 		
 		assertTrue("Not all problem markers 'Duplicate variable name' was found", messages.length == 4);
 		
 		for(int i=0;i<4;i++)
 			assertTrue("Problem marker 'Duplicate variable name' not found", "Duplicate variable name: messageList".equals(messages[i]));
 		
 		int[] lineNumbers = getMarkersNumbersOfLine(contextVariableTestFile);
 		
 		for(int i=0;i<4;i++)
 			assertTrue("Problem marker has wrong line number", (lineNumbers[i] == 16)||(lineNumbers[i] == 17)||(lineNumbers[i] == 36)||(lineNumbers[i] == 41));
 		
 		// Unknown variable name
 		System.out.println("Test - Unknown variable name");
 		
 		IFile contextVariableTestFile3 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/ContextVariableTest.3");
 		try{
 			contextVariableTestFile.setContents(contextVariableTestFile3.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'ContextVariableTest.java' content to " +
 					"'ContextVariableTest.3'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(contextVariableTestFile);
 		assertFalse("Problem marker 'Unknown variable name' not found' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(contextVariableTestFile);
 		
 		assertTrue("Problem marker 'Unknown variable name' not found", "Unknown context variable name: messageList5".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(contextVariableTestFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 22);
 
 	}
 
 	public void testExpressionLanguageValidator() {
 		modifyPreferences();
 		IPreferenceStore store = SeamCorePlugin.getDefault().getPreferenceStore();
 		System.out.println("UNKNOWN_EL_VARIABLE_PROPERTY_NAME value- "+store.getString(SeamPreferences.UNKNOWN_EL_VARIABLE_PROPERTY_NAME));
 		System.out.println("UNKNOWN_VARIABLE_NAME value- "+store.getString(SeamPreferences.UNKNOWN_VARIABLE_NAME));
 		System.out.println("UNPAIRED_GETTER_OR_SETTER value- "+store.getString(SeamPreferences.UNPAIRED_GETTER_OR_SETTER));
 
 		ISeamProject seamProject = getSeamProject(project);
 		
 		IFile abcComponentXHTMLFile = project.getFile("WebContent/abcComponent.xhtml");
 		IFile abcComponentFile = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/AbcComponent.java");
 		
 		refreshProject(project);
 		
 		int number = getMarkersNumber(abcComponentXHTMLFile);
 		assertTrue("Problem marker was found in abcComponent.xhtml", number == 0);
 		
 		number = getMarkersNumber(abcComponentFile);
 		assertTrue("Problem marker was found in AbcComponent.java", number == 0);
 
 		// Context variable cannot be resolved
 		System.out.println("Test - Context variable cannot be resolved");
 
 		IFile abcComponentXHTMLFile2 = project.getFile("WebContent/abcComponent.2");
 		try{
 			abcComponentXHTMLFile.setContents(abcComponentXHTMLFile2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'abcComponent.xhtml' content to " +
 					"'abcComponent.2'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(abcComponentXHTMLFile);
 		assertFalse("Problem marker 'Context variable cannot be resolved' not found' not found' not found' not found", number == 0);
 		
 		String[] messages = getMarkersMessage(abcComponentXHTMLFile);
 		
 		assertTrue("Problem marker 'Context variable cannot be resolved' not found", "bcComponent cannot be resolved".equals(messages[0]));
 		
 		int[] lineNumbers = getMarkersNumbersOfLine(abcComponentXHTMLFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 22);
 		
 		// Property cannot be resolved
 		System.out.println("Test - Property cannot be resolved");
 
 		IFile abcComponentXHTMLFile3 = project.getFile("WebContent/abcComponent.3");
 		try{
 			abcComponentXHTMLFile.setContents(abcComponentXHTMLFile3.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'abcComponent.xhtml' content to " +
 					"'abcComponent.3'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(abcComponentXHTMLFile);
 		assertFalse("Problem marker 'Property cannot be resolved' not found' not found' not found' not found", number == 0);
 		
 		messages = getMarkersMessage(abcComponentXHTMLFile);
 		
 		assertTrue("Problem marker 'Property cannot be resolved' not found", "actionType2 cannot be resolved".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(abcComponentXHTMLFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 22);
 		
 		// Unpaired Getter/Setter
 		System.out.println("Test - Unpaired Getter/Setter");
 		
 		IFile abcComponentXHTMLFile4 = project.getFile("WebContent/abcComponent.4");
 		try{
 			abcComponentXHTMLFile.setContents(abcComponentXHTMLFile4.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'abcComponent.xhtml' content to " +
 					"'abcComponent.4'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(abcComponentXHTMLFile);
 		assertTrue("Problem marker was found in abcComponent.xhtml", number == 0);
 
 		IFile abcComponentFile2 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/AbcComponent.2");
 		try{
 			abcComponentFile.setContents(abcComponentFile2.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'abcComponent.java' content to " +
 					"'abcComponent.2'", ex);
 		}
 		
 		refreshProject(project);
 		
 		number = getMarkersNumber(abcComponentXHTMLFile);
 		assertFalse("Problem marker 'Unpaired Getter/Setter' not found' not found' not found' not found", number == 0);
 
 		messages = getMarkersMessage(abcComponentXHTMLFile);
 
 		assertTrue("Problem marker 'Unpaired Getter/Setter' not found", "Property \"actionType\" has only Setter. Getter is missing.".equals(messages[0]));
 		
 		lineNumbers = getMarkersNumbersOfLine(abcComponentXHTMLFile);
 		
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 22);
 
 		IFile abcComponentFile3 = project.getFile("src/action/org/domain/SeamWebWarTestProject/session/AbcComponent.3");
 		try{
 			abcComponentFile.setContents(abcComponentFile3.getContents(), true, false, new NullProgressMonitor());
 		}catch(Exception ex){
 			JUnitUtils.fail("Error in changing 'abcComponent.java' content to " +
 					"'abcComponent.3'", ex);
 		}
 
 		refreshProject(project);
 
 		number = getMarkersNumber(abcComponentXHTMLFile);
 		assertFalse("Problem marker 'Unpaired Getter/Setter' not found' not found' not found' not found", number == 0);
 
 		messages = getMarkersMessage(abcComponentXHTMLFile);
 
 		assertTrue("Problem marker 'Unpaired Getter/Setter' not found", "Property \"actionType\" has only Getter. Setter is missing.".equals(messages[0]));
 
 		lineNumbers = getMarkersNumbersOfLine(abcComponentXHTMLFile);
 
 		assertTrue("Problem marker has wrong line number", lineNumbers[0] == 22);
 
 		// Test for http://jira.jboss.com/jira/browse/JBIDE-1631
 		IFile jbide1631XHTMLFile = project.getFile("WebContent/JBIDE-1631.xhtml");
 		lineNumbers = getMarkersNumbersOfLine(jbide1631XHTMLFile);
 		String errorMessage = "Seam tools doesn't validate string with a few EL properly. There should be two markers in string '#{authenticator.foo1} #{authenticator.foo2}'.";
 		assertTrue(errorMessage, lineNumbers.length>1);
 		assertTrue(errorMessage, lineNumbers[0] == 16);
 		assertTrue(errorMessage, lineNumbers[1] == 16);
 	}
 
 	private void modifyPreferences(){
 		IPreferenceStore store = SeamCorePlugin.getDefault().getPreferenceStore();
 		store.putValue(SeamPreferences.UNKNOWN_EL_VARIABLE_NAME, SeamPreferences.ERROR);
 		store.putValue(SeamPreferences.UNKNOWN_EL_VARIABLE_PROPERTY_NAME, SeamPreferences.ERROR);
 		store.putValue(SeamPreferences.UNKNOWN_VARIABLE_NAME, SeamPreferences.ERROR);
 		store.putValue(SeamPreferences.UNPAIRED_GETTER_OR_SETTER, SeamPreferences.ERROR);
 
 		if(store instanceof IPersistentPreferenceStore) {
 			try {
 				((IPersistentPreferenceStore)store).save();
 			} catch (IOException e) {
 				SeamCorePlugin.getPluginLog().logError(e);
 			}
 		}
 	}
 	
 	private int getMarkersNumber(IFile file){
 		try{
 			IMarker[] markers = file.findMarkers(null, true, IResource.DEPTH_INFINITE);
 			for(int i=0;i<markers.length;i++){
 				System.out.println("Marker - "+markers[i].getAttribute(IMarker.MESSAGE, ""));
 			}
 			return markers.length;
 			
 		}catch(CoreException ex){
 			JUnitUtils.fail("Error in getting problem markers", ex);
 		}
 		return -1;
 	}
 
 	private String[] getMarkersMessage(IFile file){
 		String[] messages = new String[1];
 		messages[0]="";
 		try{
 			IMarker[] markers = file.findMarkers(null, true, IResource.DEPTH_INFINITE);
 			messages = new String[markers.length];
 			
 			for(int i=0;i<markers.length;i++){
 				System.out.println("Marker - "+markers[i].getAttribute(IMarker.MESSAGE, ""));
 				messages[i] = markers[i].getAttribute(IMarker.MESSAGE, "");
 			}
 		}catch(CoreException ex){
 			JUnitUtils.fail("Error in getting problem markers", ex);
 		}
 		return messages;
 	}
 
 	private int[] getMarkersNumbersOfLine(IFile file){
 		int[] numbers = new int[1];
 		numbers[0]=0;
 		try{
 			IMarker[] markers = file.findMarkers(null, true, IResource.DEPTH_INFINITE);
 			numbers = new int[markers.length];
 			
 			for(int i=0;i<markers.length;i++){
 				System.out.println("Marker line number - "+markers[i].getAttribute(IMarker.LINE_NUMBER, 0));
 				numbers[i] = markers[i].getAttribute(IMarker.LINE_NUMBER, 0);
 			}
 		}catch(CoreException ex){
 			JUnitUtils.fail("Error in getting problem markers", ex);
 		}
 		return numbers;
 	}
 
 	private void refreshProject(IProject project){
 		waitForJob();
 	}
 
 	public static void waitForJob() {
 		EditorTestHelper.joinJobs(1000,10000,500);
 	}
 }
