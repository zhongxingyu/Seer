 /**
  * Copyright (c) 2013 itemis AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 * 
  * Contributors:
  *   itemis AG - initial API and implementation
  */
 package org.eclipse.rmf.tests.serialization.save;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.StringReader;
 
 import javax.xml.namespace.QName;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.rmf.tests.serialization.model.nodes.Node;
 import org.eclipse.rmf.tests.serialization.model.nodes.NodesFactory;
 import org.eclipse.rmf.tests.serialization.model.nodes.NodesPackage;
 import org.eclipse.rmf.tests.serialization.model.nodes.SubNode;
 import org.junit.Test;
 import org.xml.sax.InputSource;
 
 @SuppressWarnings("nls")
 public class BasicSaveTests extends AbstractSaveTestCase {
 
 	@Test
 	public void testEReference_Contained0000Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0000Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained0000Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("leafNode11",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode12",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode21",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode22",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode31",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[5]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode32",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[6]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode41",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[7]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode42",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[8]/@name", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0001Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0001Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained0001Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate("/nodes:NODE/nodes:NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode11",
 					xpath.evaluate("/nodes:NODE/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode12",
 					xpath.evaluate("/nodes:NODE/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 
 			assertEquals("intermediateNode2", xpath.evaluate("/nodes:NODE/nodes:NODE[2]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode21",
 					xpath.evaluate("/nodes:NODE/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode22",
 					xpath.evaluate("/nodes:NODE/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode3", xpath.evaluate("/nodes:NODE/nodes:SUB-NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode31",
 					xpath.evaluate("/nodes:NODE/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode32",
 					xpath.evaluate("/nodes:NODE/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode4", xpath.evaluate("/nodes:NODE/nodes:SUB-NODE[2]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode41",
 					xpath.evaluate("/nodes:NODE/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode42",
 					xpath.evaluate("/nodes:NODE/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0010Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0010Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained0010Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("leafNode11",
 					xpath.evaluate("/nodes:NODE/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode12",
 					xpath.evaluate("/nodes:NODE/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode21",
 					xpath.evaluate("/nodes:NODE/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode22",
 					xpath.evaluate("/nodes:NODE/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode31", xpath.evaluate("/nodes:NODE/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("leafNode32", xpath.evaluate("/nodes:NODE/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("leafNode41", xpath.evaluate("/nodes:NODE/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("leafNode42", xpath.evaluate("/nodes:NODE/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0011Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0011Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained0011Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate("/nodes:NODE/nodes:NODES[1]/nodes:NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode11", xpath.evaluate(
 					"/nodes:NODE/nodes:NODES[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode12", xpath.evaluate(
 					"/nodes:NODE/nodes:NODES[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 
 			assertEquals("intermediateNode2", xpath.evaluate("/nodes:NODE/nodes:NODES[1]/nodes:NODE[2]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode21", xpath.evaluate(
 					"/nodes:NODE/nodes:NODES[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode22", xpath.evaluate(
 					"/nodes:NODE/nodes:NODES[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode3",
 					xpath.evaluate("/nodes:NODE/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode31", xpath.evaluate(
 					"/nodes:NODE/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("leafNode32", xpath.evaluate(
 					"/nodes:NODE/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root,
 					XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode4",
 					xpath.evaluate("/nodes:NODE/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode41", xpath.evaluate(
 					"/nodes:NODE/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("leafNode42", xpath.evaluate(
 					"/nodes:NODE/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0100Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0100Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode11", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("leafNode12", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root,
 					XPathConstants.STRING));
 
 			assertEquals("intermediateNode2",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode21", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("leafNode22", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root,
 					XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode3",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name", root, XPathConstants.STRING));
 			assertEquals("nodes:SUB-NODE",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@xsi:type", root, XPathConstants.STRING));
 			assertEquals("leafNode31", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("leafNode32", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root,
 					XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode4",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name", root, XPathConstants.STRING));
 			assertEquals("nodes:SUB-NODE",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@xsi:type", root, XPathConstants.STRING));
 			assertEquals("leafNode41", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("leafNode42", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0101Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0101Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained0101Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[1]/nodes:NODE/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode11", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[1]/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 					root, XPathConstants.STRING));
 			assertEquals("leafNode12", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[1]/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 					root, XPathConstants.STRING));
 
 			assertEquals("intermediateNode2",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[2]/nodes:NODE/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode21", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[2]/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 					root, XPathConstants.STRING));
 			assertEquals("leafNode22", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[2]/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 					root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode3",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[3]/nodes:SUB-NODE/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode31", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[3]/nodes:SUB-NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 					root, XPathConstants.STRING));
 			assertEquals("leafNode32", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[3]/nodes:SUB-NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 					root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode4",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[4]/nodes:SUB-NODE/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode41", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[4]/nodes:SUB-NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 					root, XPathConstants.STRING));
 			assertEquals("leafNode42", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0101-MULTI[4]/nodes:SUB-NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 					root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0110Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0110Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained0110Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("leafNode11", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0110-MULTI[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 					root, XPathConstants.STRING));
 			assertEquals("leafNode12", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0110-MULTI[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 					root, XPathConstants.STRING));
 			assertEquals("leafNode21", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0110-MULTI[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name",
 					root, XPathConstants.STRING));
 			assertEquals("leafNode22", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0110-MULTI[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name",
 					root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode31",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0110-MULTI[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode32",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0110-MULTI[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode41",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0110-MULTI[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode42",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0110-MULTI[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0111Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0111Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained0111Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:NODES[1]/nodes:NODE[1]/@name", root, XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode11",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:NODES[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode12",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:NODES[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 			assertEquals("intermediateNode2", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:NODES[1]/nodes:NODE[2]/@name", root, XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode21",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:NODES[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode22",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:NODES[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode3", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/@name", root,
 					XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode31",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode32",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 			assertEquals("intermediateSubNode4", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/@name", root,
 					XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode41",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode42",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1001Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1001Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained1001Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode11", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 					root, XPathConstants.STRING));
 			assertEquals("leafNode12", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 					root, XPathConstants.STRING));
 
 			assertEquals("intermediateNode2",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:NODE[2]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode21", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 					root, XPathConstants.STRING));
 			assertEquals("leafNode22", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 					root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode3", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:SUB-NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode31",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode32",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode4", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:SUB-NODE[2]/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode41",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode42",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1001-MULTIS[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1010Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1010Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained1010Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals(
 					"leafNode11",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1010-MULTIS[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode12",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1010-MULTIS[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode21",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1010-MULTIS[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode22",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1010-MULTIS[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode31",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1010-MULTIS[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode32",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1010-MULTIS[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode41",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1010-MULTIS[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode42",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1010-MULTIS[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1011Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1011Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained1011Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:NODES[1]/nodes:NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode11",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:NODES[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode12",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:NODES[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals("intermediateNode2", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:NODES[1]/nodes:NODE[2]/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode21",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:NODES[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode22",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:NODES[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode3", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals(
 					"leafNode31",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode32",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode4", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/@name", root,
 					XPathConstants.STRING));
 			assertEquals(
 					"leafNode41",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode42",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1011-MULTIS[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1100Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1100Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained1100Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals(
 					"leafNode11",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode12",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals("intermediateNode2", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[2]/@name", root,
 					XPathConstants.STRING));
 			assertEquals(
 					"leafNode21",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode22",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode3", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[3]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("nodes:SUB-NODE", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[3]/@xsi:type", root,
 					XPathConstants.STRING));
 			assertEquals(
 					"leafNode31",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[3]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode32",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[3]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals("intermediateSubNode4", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[4]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("nodes:SUB-NODE", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[4]/@xsi:type", root,
 					XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode41",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[4]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode42",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1100-MULTI[4]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1101Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1101Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained1101Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[1]/nodes:NODE/@name",
 					root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode11",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[1]/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode12",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[1]/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals("intermediateNode2", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[2]/nodes:NODE/@name",
 					root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode21",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[2]/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode22",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[2]/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals(
 					"intermediateSubNode3",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[3]/nodes:SUB-NODE/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode31",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[3]/nodes:SUB-NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode32",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[3]/nodes:SUB-NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals(
 					"intermediateSubNode4",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[4]/nodes:SUB-NODE/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode41",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[4]/nodes:SUB-NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode42",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1101-MULTI[4]/nodes:SUB-NODE/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1110Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1110Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained1110Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals(
 					"leafNode11",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTI[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode12",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTI[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode21",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTI[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode22",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTI[1]/nodes:NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode31",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTI[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode32",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTI[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode41",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTI[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[3]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode42",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1110-MULTI[1]/nodes:SUB-NODES[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[4]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1111Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1111Many.xml";
 			Node rootNode = createNodeModel_ContainedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Contained1111Many(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals(
 					"intermediateNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:NODES[1]/nodes:NODE[1]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode11",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:NODES[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode12",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:NODES[1]/nodes:NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"intermediateNode2",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:NODES[1]/nodes:NODE[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode21",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:NODES[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode22",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:NODES[1]/nodes:NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals(
 					"intermediateSubNode3",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode31",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode32",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"intermediateSubNode4",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/@name",
 							root, XPathConstants.STRING));
 
 			assertEquals(
 					"leafNode41",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode42",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTIS[1]/nodes:FEATURE-WITH-SERIALIZATION-1111-MULTI[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[2]/nodes:FEATURE-WITH-SERIALIZATION-0100-MULTI[2]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0000Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0000Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained0000Single(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0101Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("leafNode1",
 					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-CONTAINED-0101-SINGLE[1]/nodes:SUB-NODE/@name", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0001Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0001Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained0001Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate("/nodes:NODE/nodes:NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode1", xpath.evaluate("/nodes:NODE/nodes:NODE[1]/nodes:SUB-NODE[1]/@name", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0010Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0010Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained0010Single(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0101Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate("/nodes:NODE/nodes:NODES[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode1", xpath.evaluate("/nodes:NODE/nodes:NODES[1]//nodes:EREFERENCE-CONTAINED-0101-SINGLE[1]/nodes:SUB-NODE[1]/@name",
 					root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0011Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0011Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained0011Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate("/nodes:NODE/nodes:NODES[1]/nodes:NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode1", xpath.evaluate("/nodes:NODE/nodes:NODES[1]/nodes:NODE[1]/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/@name", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0100Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0100Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained0100Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1",
 					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-CONTAINED-0100-SINGLE[1]/@name", root, XPathConstants.STRING));
 			assertEquals("leafNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:EREFERENCE-CONTAINED-0100-SINGLE[1]/nodes:EREFERENCE-CONTAINED-0100-SINGLE[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals("nodes:SUB-NODE", xpath.evaluate(
 					"/nodes:NODE/nodes:EREFERENCE-CONTAINED-0100-SINGLE[1]/nodes:EREFERENCE-CONTAINED-0100-SINGLE[1]/@xsi:type", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0101Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0101Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained0101Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1",
 					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-CONTAINED-0101-SINGLE[1]/nodes:NODE/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-0101-SINGLE[1]/nodes:NODE/nodes:EREFERENCE-CONTAINED-0101-SINGLE[1]/nodes:SUB-NODE/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-0101-SINGLE[1]/nodes:NODE/nodes:EREFERENCE-CONTAINED-0101-SINGLE[1]/nodes:NODE/@xsi:type",
 							root, XPathConstants.STRING));
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0110Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0110Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained0110Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1",
 					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-CONTAINED-0110-SINGLE[1]/nodes:NODES/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-0110-SINGLE[1]/nodes:NODES/nodes:EREFERENCE-CONTAINED-0110-SINGLE[1]/nodes:SUB-NODES/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-0110-SINGLE[1]/nodes:NODES/nodes:EREFERENCE-CONTAINED-0110-SINGLE[1]/nodes:SUB-NODES/@xsi:type",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained0111Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained0111Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained0111Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1",
 					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-CONTAINED-0111-SINGLE[1]/nodes:NODES/nodes:NODE/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-0111-SINGLE[1]/nodes:NODES/nodes:NODE/nodes:EREFERENCE-CONTAINED-0111-SINGLE[1]/nodes:SUB-NODES/nodes:SUB-NODE/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-0111-SINGLE[1]/nodes:NODES/nodes:NODE/nodes:EREFERENCE-CONTAINED-0111-SINGLE[1]/nodes:SUB-NODES/nodes:SUB-NODE/@xsi:type",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1000Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1000Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained1000Single(),
 					NodesPackage.eINSTANCE.getNode_EReference_Contained0101Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("leafNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1000-SINGLES/nodes:EREFERENCE-CONTAINED-0101-SINGLE[1]/nodes:SUB-NODE/@name", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1001Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1001Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained1001Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1",
 					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-CONTAINED-1001-SINGLES/nodes:NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1001-SINGLES/nodes:NODE[1]/nodes:EREFERENCE-CONTAINED-1001-SINGLES/nodes:SUB-NODE[1]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1010Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1010Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained1010Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1",
 					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-CONTAINED-1010-SINGLES/nodes:NODES[1]/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1010-SINGLES/nodes:NODES[1]/nodes:EREFERENCE-CONTAINED-1010-SINGLES[1]/nodes:SUB-NODES[1]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1011Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1011Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained1011Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1011-SINGLES/nodes:NODES[1]/nodes:NODE[1]/@name", root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1011-SINGLES/nodes:NODES[1]/nodes:NODE[1]/nodes:EREFERENCE-CONTAINED-1011-SINGLES/nodes:SUB-NODES[1]/nodes:SUB-NODE[1]/@name",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1100Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1100Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained1100Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1100-SINGLES/nodes:EREFERENCE-CONTAINED-1100-SINGLE[1]/@name", root,
 					XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1100-SINGLES/nodes:EREFERENCE-CONTAINED-1100-SINGLE[1]/nodes:EREFERENCE-CONTAINED-1100-SINGLES/nodes:EREFERENCE-CONTAINED-1100-SINGLE[1]/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"nodes:SUB-NODE",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1100-SINGLES/nodes:EREFERENCE-CONTAINED-1100-SINGLE[1]/nodes:EREFERENCE-CONTAINED-1100-SINGLES/nodes:EREFERENCE-CONTAINED-1100-SINGLE[1]/@xsi:type",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1101Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1101Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained1101Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1101-SINGLES/nodes:EREFERENCE-CONTAINED-1101-SINGLE[1]/nodes:NODE/@name", root,
 					XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1101-SINGLES/nodes:EREFERENCE-CONTAINED-1101-SINGLE[1]/nodes:NODE/nodes:EREFERENCE-CONTAINED-1101-SINGLES/nodes:EREFERENCE-CONTAINED-1101-SINGLE[1]/nodes:SUB-NODE/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1101-SINGLES/nodes:EREFERENCE-CONTAINED-1101-SINGLE[1]/nodes:NODE/nodes:EREFERENCE-CONTAINED-1101-SINGLES/nodes:EREFERENCE-CONTAINED-1101-SINGLE[1]/nodes:SUB-NODE/@xsi:type",
 							root, XPathConstants.STRING));
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1110Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1110Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained1110Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1110-SINGLES/nodes:EREFERENCE-CONTAINED-1110-SINGLE[1]/nodes:NODES/@name", root,
 					XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1110-SINGLES/nodes:EREFERENCE-CONTAINED-1110-SINGLE[1]/nodes:NODES/nodes:EREFERENCE-CONTAINED-1110-SINGLES/nodes:EREFERENCE-CONTAINED-1110-SINGLE[1]/nodes:SUB-NODES/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1110-SINGLES/nodes:EREFERENCE-CONTAINED-1110-SINGLE[1]/nodes:NODES/nodes:EREFERENCE-CONTAINED-1110-SINGLES/nodes:EREFERENCE-CONTAINED-1110-SINGLE[1]/nodes:SUB-NODES/@xsi:type",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Contained1111Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Contained1111Single.xml";
 			Node rootNode = createNodeModel_ContainedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Contained1111Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("intermediateNode1", xpath.evaluate(
 					"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1111-SINGLES/nodes:EREFERENCE-CONTAINED-1111-SINGLE[1]/nodes:NODES/nodes:NODE/@name",
 					root, XPathConstants.STRING));
 			assertEquals(
 					"leafNode1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1111-SINGLES/nodes:EREFERENCE-CONTAINED-1111-SINGLE[1]/nodes:NODES/nodes:NODE/nodes:EREFERENCE-CONTAINED-1111-SINGLES/nodes:EREFERENCE-CONTAINED-1111-SINGLE[1]/nodes:SUB-NODES/nodes:SUB-NODE/@name",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EREFERENCE-CONTAINED-1111-SINGLES/nodes:EREFERENCE-CONTAINED-1111-SINGLE[1]/nodes:NODES/nodes:NODE/nodes:EREFERENCE-CONTAINED-1111-SINGLES/nodes:EREFERENCE-CONTAINED-1111-SINGLE[1]/nodes:SUB-NODES/nodes:SUB-NODE/@xsi:type",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Referenced0100Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Referenced0100Single.xml";
 			Node rootNode = createNodeModel_ReferencedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Referenced0100Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("root", xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-0100-SINGLE-REF", root, XPathConstants.STRING));
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Referenced0101Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Referenced0101Single.xml";
 			Node rootNode = createNodeModel_ReferencedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Referenced0101Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("root", xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-0101-SINGLE-REF/nodes:NODE", root, XPathConstants.STRING));
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Referenced1001Single() {
 		try {
 			String fileName = BASEDIR + "EReference_Referenced1001Single.xml";
 			Node rootNode = createNodeModel_ReferencedxxxxSingle(NodesPackage.eINSTANCE.getNode_EReference_Referenced1001Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("root", xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-1001-SINGLE-REFS/nodes:NODE", root, XPathConstants.STRING));
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Referenced0100Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Referenced0100Many.xml";
 			Node rootNode = createNodeModel_ReferencedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Referenced0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("node", xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-0100-MANY-REF[1]", root, XPathConstants.STRING));
 			assertEquals("", xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-0100-MANY-REF[1]/@xsi:type", root, XPathConstants.STRING));
 			assertEquals("subNode", xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-0100-MANY-REF[2]", root, XPathConstants.STRING));
			assertEquals("nodes:SUB-NODE",
					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-0100-MANY-REF[2]/@xsi:type", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Referenced0101Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Referenced0101Many.xml";
 			Node rootNode = createNodeModel_ReferencedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Referenced0101Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("node", xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-0101-MANY-REF/nodes:NODE[1]", root, XPathConstants.STRING));
 			assertEquals("subNode",
 					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-0101-MANY-REF/nodes:SUB-NODE[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEReference_Referenced1001Many() {
 		try {
 			String fileName = BASEDIR + "EReference_Referenced1001Many.xml";
 			Node rootNode = createNodeModel_ReferencedxxxxMany(NodesPackage.eINSTANCE.getNode_EReference_Referenced1001Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("node", xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-1001-MANY-REFS/nodes:NODE[1]", root, XPathConstants.STRING));
 			assertEquals("subNode",
 					xpath.evaluate("/nodes:NODE/nodes:EREFERENCE-REFERENCED-1001-MANY-REFS/nodes:SUB-NODE[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testReqIF_EReference_Referenced0101Single() {
 		try {
 			String fileName = BASEDIR + "ReqIF_EReference_Referenced0101Single.xml";
 			Node rootNode = createNodeModel_ReferencedxxxxSingle(NodesPackage.eINSTANCE.getNode_Reqif_eReference_Referenced0101Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("root",
 					xpath.evaluate("/nodes:NODE/nodes:REQIF-EREFERENCE-REFERENCED-0101-SINGLE/nodes:NODE-REF", root, XPathConstants.STRING));
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testReqIF_EReference_Referenced1001Many() {
 		try {
 			String fileName = BASEDIR + "ReqIF_EReference_Referenced1001Many.xml";
 			Node rootNode = createNodeModel_ReferencedxxxxMany(NodesPackage.eINSTANCE.getNode_Reqif_eReference_Referenced1001Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("node",
 					xpath.evaluate("/nodes:NODE/nodes:REQIF-EREFERENCE-REFERENCED-1001-MANIES/nodes:NODE-REF[1]", root, XPathConstants.STRING));
 			assertEquals("subNode",
 					xpath.evaluate("/nodes:NODE/nodes:REQIF-EREFERENCE-REFERENCED-1001-MANIES/nodes:SUB-NODE-REF[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0001Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0001Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0001Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/ecore:EString[1]", root, XPathConstants.STRING));
 			assertEquals("value2", xpath.evaluate("/nodes:NODE/ecore:EString[2]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0010Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0010Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0010Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1 value2", xpath.evaluate("/nodes:NODE/ecore:EStrings[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0011Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0011Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0011Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/ecore:EStrings[1]/ecore:EString[1]", root, XPathConstants.STRING));
 			assertEquals("value2", xpath.evaluate("/nodes:NODE/ecore:EStrings[1]/ecore:EString[2]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0100Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0100Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0100-MANY[1]", root, XPathConstants.STRING));
 			assertEquals("value2", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0100-MANY[2]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0101Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0101Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0101Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0101-MANY[1]/ecore:EString[1]", root, XPathConstants.STRING));
 			assertEquals("value2",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0101-MANY[2]/ecore:EString[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0110Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0110Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0110Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1 value2",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0110-MANY[1]/ecore:EStrings[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0111Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0111Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0111Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0111-MANY[1]/ecore:EStrings[1]/ecore:EString[1]", root,
 					XPathConstants.STRING));
 			assertEquals("value2", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0111-MANY[1]/ecore:EStrings[1]/ecore:EString[2]", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1000Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1000Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1000Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1 value2", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1000-MANIES[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	public void testEAttribute_Attribute1001Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1001Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1001Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1001-MANIES[1]/ecore:EString[1]", root, XPathConstants.STRING));
 			assertEquals("value2",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1001-MANIES[1]/ecore:EString[2]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1010Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1010Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1010Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1 value2",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1010-MANIES[1]/ecore:EStrings[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1011Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1011Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1011Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1011-MANIES[1]/ecore:EStrings[1]/ecore:EString[1]", root,
 					XPathConstants.STRING));
 			assertEquals("value2", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1011-MANIES[1]/ecore:EStrings[1]/ecore:EString[2]", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1100Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1100Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1100-MANIES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1100-MANY[1]",
 					root, XPathConstants.STRING));
 			assertEquals("value2", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1100-MANIES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1100-MANY[2]",
 					root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1101Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1101Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1101Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate(
 					"/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1101-MANIES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1101-MANY[1]/ecore:EString[1]", root,
 					XPathConstants.STRING));
 			assertEquals("value2", xpath.evaluate(
 					"/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1101-MANIES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1101-MANY[2]/ecore:EString[1]", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1110Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1110Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1110Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1 value2", xpath.evaluate(
 					"/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1110-MANIES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1110-MANY[1]/ecore:EStrings[1]", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1111Many() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1111Many.xml";
 			Node rootNode = createNodeModel_AttributexxxxMany(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1111Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals(
 					"value1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1111-MANIES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1111-MANY[1]/ecore:EStrings[1]/ecore:EString[1]",
 							root, XPathConstants.STRING));
 			assertEquals(
 					"value2",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1111-MANIES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1111-MANY[1]/ecore:EStrings[1]/ecore:EString[2]",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0001Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0001Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0001Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/ecore:EString[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0010Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0010Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0010Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/ecore:EStrings[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0011Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0011Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0011Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/ecore:EStrings[1]/ecore:EString[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0100Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0100Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0100Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0100-SINGLE[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0101Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0101Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0101Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0101-SINGLE[1]/ecore:EString[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0110Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0110Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0110Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0110-SINGLE[1]/ecore:EStrings[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute0111Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute0111Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute0111Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-0111-SINGLE[1]/ecore:EStrings[1]/ecore:EString[1]", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1000Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1000Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1000Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1000-SINGLES[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	public void testEAttribute_Attribute1001Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1001Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1001Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1001-SINGLES[1]/ecore:EString[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1010Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1010Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1010Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1",
 					xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1010-SINGLES[1]/ecore:EStrings[1]", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1011Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1011Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1011Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1011-SINGLES[1]/ecore:EStrings[1]/ecore:EString[1]", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1100Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1100Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1100Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate("/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1100-SINGLES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1100-SINGLE[1]",
 					root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1101Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1101Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1101Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate(
 					"/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1101-SINGLES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1101-SINGLE[1]/ecore:EString[1]", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1110Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1110Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1110Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("value1", xpath.evaluate(
 					"/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1110-SINGLES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1110-SINGLE[1]/ecore:EStrings[1]", root,
 					XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testEAttribute_Attribute1111Single() {
 		try {
 			String fileName = BASEDIR + "EAttribute_Attribute1111Single.xml";
 			Node rootNode = createNodeModel_AttributexxxxSingle(NodesPackage.eINSTANCE.getNode_EAttribute_Attribute1111Single());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals(
 					"value1",
 					xpath.evaluate(
 							"/nodes:NODE/nodes:EATTRIBUTE-ATTRIBUTE-1111-SINGLES[1]/nodes:EATTRIBUTE-ATTRIBUTE-1111-SINGLE[1]/ecore:EStrings[1]/ecore:EString[1]",
 							root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@Test
 	public void testFeatureContainmentReferenceWithTypeEObjectAndSerialization0100Many() {
 		try {
 			String fileName = BASEDIR + "FeatureContainmentReferenceWithTypeEObjectAndSerialization0100ManyMany.xml";
 			Node rootNode = createNodeModelWithForeignSubmodel_ContainedxxxxMany(NodesPackage.eINSTANCE
 					.getNode_EReference_WithTypeEObject_Contained0100Many());
 			org.w3c.dom.Node root = getXMLRootNode(fileName, rootNode);
 
 			assertEquals("EPackage1",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-TYPE-EOBJECT-AND-SERIALIZATION-0100-MULTI[1]/@name", root, XPathConstants.STRING));
 			assertEquals("EClass11", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-TYPE-EOBJECT-AND-SERIALIZATION-0100-MULTI[1]/eClassifiers[1]/@name", root, XPathConstants.STRING));
 			assertEquals("EClass12", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-TYPE-EOBJECT-AND-SERIALIZATION-0100-MULTI[1]/eClassifiers[2]/@name", root, XPathConstants.STRING));
 
 			assertEquals("EPackage2",
 					xpath.evaluate("/nodes:NODE/nodes:FEATURE-WITH-TYPE-EOBJECT-AND-SERIALIZATION-0100-MULTI[2]/@name", root, XPathConstants.STRING));
 			assertEquals("EClass21", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-TYPE-EOBJECT-AND-SERIALIZATION-0100-MULTI[2]/eClassifiers[1]/@name", root, XPathConstants.STRING));
 			assertEquals("EClass22", xpath.evaluate(
 					"/nodes:NODE/nodes:FEATURE-WITH-TYPE-EOBJECT-AND-SERIALIZATION-0100-MULTI[2]/eClassifiers[2]/@name", root, XPathConstants.STRING));
 
 		} catch (Exception ex) {
 			assertTrue(ex.getMessage(), false);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Node createNodeModel_ContainedxxxxMany(EStructuralFeature topLevelfeature, EStructuralFeature subfeature) {
 		assert null != subfeature;
 		assert subfeature.isMany();
 		Node rootNode = NodesFactory.eINSTANCE.createNode();
 		rootNode.setName("root");
 
 		Node intermediateNode1 = NodesFactory.eINSTANCE.createNode();
 		intermediateNode1.setName("intermediateNode1");
 
 		Node intermediateNode2 = NodesFactory.eINSTANCE.createNode();
 		intermediateNode2.setName("intermediateNode2");
 
 		Node intermediateSubNode3 = NodesFactory.eINSTANCE.createSubNode();
 		intermediateSubNode3.setName("intermediateSubNode3");
 
 		Node intermediateSubNode4 = NodesFactory.eINSTANCE.createSubNode();
 		intermediateSubNode4.setName("intermediateSubNode4");
 
 		Node leafNode11 = NodesFactory.eINSTANCE.createNode();
 		leafNode11.setName("leafNode11");
 
 		Node leafNode12 = NodesFactory.eINSTANCE.createNode();
 		leafNode12.setName("leafNode12");
 
 		Node leafNode21 = NodesFactory.eINSTANCE.createNode();
 		leafNode21.setName("leafNode21");
 
 		Node leafNode22 = NodesFactory.eINSTANCE.createNode();
 		leafNode22.setName("leafNode22");
 
 		Node leafNode31 = NodesFactory.eINSTANCE.createNode();
 		leafNode31.setName("leafNode31");
 
 		Node leafNode32 = NodesFactory.eINSTANCE.createNode();
 		leafNode32.setName("leafNode32");
 
 		Node leafNode41 = NodesFactory.eINSTANCE.createNode();
 		leafNode41.setName("leafNode41");
 
 		Node leafNode42 = NodesFactory.eINSTANCE.createNode();
 		leafNode42.setName("leafNode42");
 
 		((EList<Object>) intermediateNode1.eGet(subfeature)).add(leafNode11);
 		((EList<Object>) intermediateNode1.eGet(subfeature)).add(leafNode12);
 		((EList<Object>) intermediateNode2.eGet(subfeature)).add(leafNode21);
 		((EList<Object>) intermediateNode2.eGet(subfeature)).add(leafNode22);
 		((EList<Object>) intermediateSubNode3.eGet(subfeature)).add(leafNode31);
 		((EList<Object>) intermediateSubNode3.eGet(subfeature)).add(leafNode32);
 		((EList<Object>) intermediateSubNode4.eGet(subfeature)).add(leafNode41);
 		((EList<Object>) intermediateSubNode4.eGet(subfeature)).add(leafNode42);
 
 		((EList<Object>) rootNode.eGet(topLevelfeature)).add(intermediateNode1);
 		((EList<Object>) rootNode.eGet(topLevelfeature)).add(intermediateNode2);
 		((EList<Object>) rootNode.eGet(topLevelfeature)).add(intermediateSubNode3);
 		((EList<Object>) rootNode.eGet(topLevelfeature)).add(intermediateSubNode4);
 
 		return rootNode;
 	}
 
 	protected Node createNodeModel_ContainedxxxxSingle(EStructuralFeature feature) {
 		return createNodeModel_ContainedxxxxSingle(feature, feature);
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Node createNodeModel_ContainedxxxxSingle(EStructuralFeature topLevelfeature, EStructuralFeature subFeature) {
 		assert !topLevelfeature.isMany();
 		Node rootNode = NodesFactory.eINSTANCE.createNode();
 		rootNode.setName("root");
 
 		Node intermediateNode1 = NodesFactory.eINSTANCE.createNode();
 		intermediateNode1.setName("intermediateNode1");
 
 		SubNode leafNode1 = NodesFactory.eINSTANCE.createSubNode();
 		leafNode1.setName("leafNode1");
 
 		intermediateNode1.eSet(subFeature, leafNode1);
 		rootNode.eSet(topLevelfeature, intermediateNode1);
 
 		return rootNode;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Node createNodeModel_ReferencedxxxxSingle(EReference reference) {
 		assert !reference.isContainment();
 		assert !reference.isMany();
 		Node rootNode = NodesFactory.eINSTANCE.createNode();
 		rootNode.setName("root");
 
 		rootNode.eSet(reference, rootNode);
 
 		return rootNode;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Node createNodeModel_ReferencedxxxxMany(EReference reference) {
 		assert !reference.isContainment();
 		assert reference.isMany();
 		Node rootNode = NodesFactory.eINSTANCE.createNode();
 		rootNode.setName("root");
 
 		Node node = NodesFactory.eINSTANCE.createNode();
 		node.setName("node");
 
 		SubNode subNode = NodesFactory.eINSTANCE.createSubNode();
 		subNode.setName("subNode");
 
 		EList<EObject> containedValues = (EList<EObject>) rootNode.eGet(NodesPackage.eINSTANCE.getNode_EReference_Contained0100Many());
 		containedValues.add(node);
 		containedValues.add(subNode);
 
 		EList<EObject> referencedNodes = (EList<EObject>) rootNode.eGet(reference);
 		referencedNodes.add(node);
 		referencedNodes.add(subNode);
 
 		return rootNode;
 	}
 
 	protected Node createNodeModel(EStructuralFeature feature) {
 		return createNodeModel_ContainedxxxxMany(feature, feature);
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Node createNodeModelWithForeignSubmodel_ContainedxxxxMany(EStructuralFeature feature) {
 		assert null != feature;
 		assert feature.isMany();
 		Node rootNode = NodesFactory.eINSTANCE.createNode();
 
 		EPackage ePackage1 = EcoreFactory.eINSTANCE.createEPackage();
 		ePackage1.setName("EPackage1");
 
 		EPackage ePackage2 = EcoreFactory.eINSTANCE.createEPackage();
 		ePackage2.setName("EPackage2");
 
 		EClass eClass11 = EcoreFactory.eINSTANCE.createEClass();
 		eClass11.setName("EClass11");
 
 		EClass eClass12 = EcoreFactory.eINSTANCE.createEClass();
 		eClass12.setName("EClass12");
 
 		EClass eClass21 = EcoreFactory.eINSTANCE.createEClass();
 		eClass21.setName("EClass21");
 
 		EClass eClass22 = EcoreFactory.eINSTANCE.createEClass();
 		eClass22.setName("EClass22");
 
 		ePackage1.getEClassifiers().add(eClass11);
 		ePackage1.getEClassifiers().add(eClass12);
 		ePackage2.getEClassifiers().add(eClass21);
 		ePackage2.getEClassifiers().add(eClass22);
 
 		((EList<Object>) rootNode.eGet(feature)).add(ePackage1);
 		((EList<Object>) rootNode.eGet(feature)).add(ePackage2);
 
 		return rootNode;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Node createNodeModel_AttributexxxxMany(EAttribute feature) {
 		assert null != feature;
 		assert feature.isMany();
 		Node rootNode = NodesFactory.eINSTANCE.createNode();
 		EList<Object> values = (EList<Object>) rootNode.eGet(feature);
 		values.add("value1");
 		values.add("value2");
 
 		return rootNode;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Node createNodeModel_AttributexxxxSingle(EAttribute feature) {
 		assert null != feature;
 		assert !feature.isMany();
 		Node rootNode = NodesFactory.eINSTANCE.createNode();
 		rootNode.eSet(feature, "value1");
 		return rootNode;
 	}
 
 	protected void validateOutput(String xpathExpression, String output, String expectedResult, QName resultType) throws XPathExpressionException {
 		Object result = xpath.evaluate(xpathExpression, new InputSource(new StringReader(output)), resultType);
 		assertEquals(expectedResult, result);
 
 	}
 
 }
