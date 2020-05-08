 /**
  * Copyright (C) 2012  Severin Heiniger <severinheiniger@gmail.com>
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package valable.outline;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.viewers.TreeNode;
 import org.gnome.vala.Class;
 import org.junit.Test;
 
 import valable.AbstractTest;
 import valable.model.ValaSource;
 
 /**
  * Tests {@link ValaContentProvider}.
  */
 public class ValaContentProviderTest extends AbstractTest {
 
 	@Test
 	public void testParse() throws CoreException, IOException {
 		ValaSource source = parseTestSource("simple.vala");
 
 		Class sampleClass = source.getClass("Simple");
 		Class fooClass = source.getClass("Foo");
 		TreeNode sampleTreeNode = new TreeNode(sampleClass);
 		sampleTreeNode.setChildren(new TreeNode[] {
 				new TreeNode(sampleClass.getField("age")),
 				new TreeNode(sampleClass.getField("name")),
 				new TreeNode(sampleClass.getField("count")),
 				new TreeNode(sampleClass.getMethod("main")),
 				new TreeNode(sampleClass.getMethod("doThing")),
				new TreeNode(sampleClass.getMethod("getParent")),
				new TreeNode(sampleClass.getMethod(".new")) });
 		TreeNode fooTreeNode = new TreeNode(fooClass);
 		fooTreeNode.setChildren(new TreeNode[] {
 				new TreeNode(fooClass.getMethod("getParent")),
				new TreeNode(fooClass.getMethod("removeParent")),
				new TreeNode(fooClass.getMethod(".new")) });
 
 		TreeNode[] expectedTreeNodes = { sampleTreeNode, fooTreeNode };
 
 		ValaContentProvider contentProvider = new ValaContentProvider();
 		TreeNode[] treeNodes = contentProvider.getElements(source);
 
 		assertTreeNodes(expectedTreeNodes, treeNodes);
 	}
 
 	@Test
 	public void testProperties() {
 		ValaSource source = parseTestSource("properties.vala");
 
 		Class nonPrivAccessClass = source.getClass("NonPrivAccess");
 		Class sampleClass = source.getClass("Sample");
 		TreeNode nonPrivAccessClassNode = new TreeNode(nonPrivAccessClass);
 		nonPrivAccessClassNode.setChildren(new TreeNode[] { new TreeNode(
 				nonPrivAccessClass.getMethod(".new")) });
 		TreeNode sampleClassNode = new TreeNode(sampleClass);
 		sampleClassNode.setChildren(new TreeNode[] {
 				new TreeNode(sampleClass.getField("_name")),
 				new TreeNode(sampleClass.getField("_read_only")),
 				new TreeNode(sampleClass.getMethod(".new")),
 				new TreeNode(sampleClass.getMethod("run")),
 				new TreeNode(sampleClass.getMethod("main")) });
 
 		TreeNode[] expectedTreeNodes = { nonPrivAccessClassNode,
 				sampleClassNode };
 		ValaContentProvider contentProvider = new ValaContentProvider();
 		TreeNode[] treeNodes = contentProvider.getElements(source);
 
 		assertTreeNodes(expectedTreeNodes, treeNodes);
 	}
 
 	/**
 	 * Asserts that two tree node arrays are equal, including their children.
 	 * 
 	 * @param expectedTreeNodes
 	 *            the expected array of tree nodes
 	 * @param treeNodes
 	 *            the actual array of tree nodes
 	 */
 	public void assertTreeNodes(TreeNode[] expectedTreeNodes,
 			TreeNode[] treeNodes) {
 		if (expectedTreeNodes == null && treeNodes == null) {
 			return;
 		}
 		assertEquals(expectedTreeNodes.length, treeNodes.length);
 
 		for (int index = 0; index < treeNodes.length; index++) {
 			TreeNode expectedTreeNode = expectedTreeNodes[index];
 			TreeNode treeNode = treeNodes[index];
 			assertEquals(expectedTreeNode.getValue(), treeNode.getValue());
 			assertTreeNodes(expectedTreeNode.getChildren(),
 					treeNode.getChildren());
 		}
 	}
 
 }
