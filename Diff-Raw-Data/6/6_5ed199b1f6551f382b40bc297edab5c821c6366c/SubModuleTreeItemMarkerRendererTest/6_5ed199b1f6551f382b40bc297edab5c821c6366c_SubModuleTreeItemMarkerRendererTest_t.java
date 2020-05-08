 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.lnf.renderer;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.runtime.AssertionFailedException;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.IIconizableMarker;
 import org.eclipse.riena.ui.core.marker.NegativeMarker;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * Tests of the class {@link SubModuleTreeItemMarkerRenderer}.
  */
 public class SubModuleTreeItemMarkerRendererTest extends TestCase {
 
 	private Shell shell;
 	private GC gc;
 	private TreeItem item;
 
 	/**
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		shell = new Shell();
 		Tree tree = new Tree(shell, SWT.NONE);
 		item = new TreeItem(tree, SWT.NONE);
 		gc = new GC(tree);
 		LnfManager.setLnf(new MyLnf());
 		LnfManager.getLnf().initialize();
 	}
 
 	/**
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		gc.dispose();
 		gc = null;
 		SwtUtilities.disposeWidget(shell);
 	}
 
 	/**
 	 * Tests the method {@code paint}.
 	 */
 	public void testPaint() {
 
 		MockRenderer renderer = new MockRenderer();
 		renderer.setBounds(0, 0, 100, 100);
 
 		try {
 			renderer.paint(gc, null);
 			fail("AssertionFailedException expected");
 		} catch (AssertionFailedException e) {
 			// ok, expected
 		}
 
 		try {
 			renderer.paint(gc, shell);
 			fail("AssertionFailedException expected");
 		} catch (AssertionFailedException e) {
 			// ok, expected
 		}
 
 		try {
 			renderer.paint(null, item);
 			fail("AssertionFailedException expected");
 		} catch (AssertionFailedException e) {
 			// ok, expected
 		}
 
 		renderer.resetPaintMarkersCalled();
 		renderer.paint(gc, item);
 		assertFalse(renderer.isPaintMarkersCalled());
 
 		SubModuleNode node = new SubModuleNode();
		renderer.setMarkers(node.getMarkers());
 		renderer.resetPaintMarkersCalled();
 		renderer.paint(gc, item);
 		assertFalse(renderer.isPaintMarkersCalled());
 
 		node.addMarker(new ErrorMarker());
		renderer.setMarkers(node.getMarkers());
 		renderer.resetPaintMarkersCalled();
 		renderer.paint(gc, item);
 		assertTrue(renderer.isPaintMarkersCalled());
 
 		node.removeAllMarkers();
 		node.addMarker(new NegativeMarker());
		renderer.setMarkers(node.getMarkers());
 		renderer.resetPaintMarkersCalled();
 		renderer.paint(gc, item);
 		assertFalse(renderer.isPaintMarkersCalled());
 
 		renderer.dispose();
 
 	}
 
 	/**
 	 * Tests the method {@code paintMarkers}.
 	 * 
 	 * @throws Exception
 	 */
 	public void testPaintMarkers() throws Exception {
 
 		item.setImage(createItemImage());
 
 		SubModuleTreeItemMarkerRenderer renderer = new SubModuleTreeItemMarkerRenderer();
 		renderer.setBounds(0, 0, 100, 25);
 
 		// create image without markers
 		Collection<IIconizableMarker> markers = new ArrayList<IIconizableMarker>();
 		Image noMarkersImage = new Image(shell.getDisplay(), new Rectangle(0, 0, 10, 10));
 		GC noMarkersGC = new GC(noMarkersImage);
 		// renderer.paintMarkers(noMarkersGC, markers, item);
 		ReflectionUtils.invokeHidden(renderer, "paintMarkers", noMarkersGC, markers, item);
 		byte[] noMarkersBytes = noMarkersImage.getImageData().data;
 		noMarkersGC.dispose();
 		SwtUtilities.disposeResource(noMarkersImage);
 
 		// no marker -> no marker image is drawn
 		Image paintImage = new Image(shell.getDisplay(), new Rectangle(0, 0, 10, 10));
 		GC paintGC = new GC(paintImage);
 		ReflectionUtils.invokeHidden(renderer, "paintMarkers", paintGC, markers, item);
 		byte[] paintBytes = paintImage.getImageData().data;
 		paintGC.dispose();
 		SwtUtilities.disposeResource(paintImage);
 		assertTrue(Arrays.equals(noMarkersBytes, paintBytes));
 
 		// one error marker -> error marker image is drawn
 		markers.add(new ErrorMarker());
 		paintImage = new Image(shell.getDisplay(), new Rectangle(0, 0, 10, 10));
 		paintGC = new GC(paintImage);
 		ReflectionUtils.invokeHidden(renderer, "paintMarkers", paintGC, markers, item);
 		paintBytes = paintImage.getImageData().data;
 		paintGC.dispose();
 		SwtUtilities.disposeResource(paintImage);
 		assertFalse(Arrays.equals(noMarkersBytes, paintBytes));
 
 		// one error marker, but item has no image -> no marker image is drawn
 		item.setImage((Image) null);
 		paintImage = new Image(shell.getDisplay(), new Rectangle(0, 0, 10, 10));
 		paintGC = new GC(paintImage);
 		ReflectionUtils.invokeHidden(renderer, "paintMarkers", paintGC, markers, item);
 		paintBytes = paintImage.getImageData().data;
 		paintGC.dispose();
 		SwtUtilities.disposeResource(paintImage);
 		assertTrue(Arrays.equals(noMarkersBytes, paintBytes));
 
 		renderer.dispose();
 
 	}
 
 	/**
 	 * Tests the method {@code calcMarkerCoordinates}.
 	 */
 	public void testCalcMarkerCoordinates() {
 
 		SubModuleTreeItemMarkerRenderer renderer = new SubModuleTreeItemMarkerRenderer();
 		renderer.setBounds(2, 3, 100, 25);
 
 		Image itemImage = createItemImage();
 		Image markerImage = createMarkerImage();
 
 		Point pos = ReflectionUtils.invokeHidden(renderer, "calcMarkerCoordinates", itemImage, markerImage,
 				IIconizableMarker.MarkerPosition.TOP_LEFT);
 		assertEquals(2, pos.x);
 		assertEquals(3, pos.y);
 
 		pos = ReflectionUtils.invokeHidden(renderer, "calcMarkerCoordinates", itemImage, markerImage,
 				IIconizableMarker.MarkerPosition.TOP_RIGHT);
 		assertEquals(2 + 5, pos.x);
 		assertEquals(3, pos.y);
 
 		pos = ReflectionUtils.invokeHidden(renderer, "calcMarkerCoordinates", itemImage, markerImage,
 				IIconizableMarker.MarkerPosition.BOTTOM_LEFT);
 		assertEquals(2, pos.x);
 		assertEquals(3 + 5, pos.y);
 
 		pos = ReflectionUtils.invokeHidden(renderer, "calcMarkerCoordinates", itemImage, markerImage,
 				IIconizableMarker.MarkerPosition.BOTTOM_RIGHT);
 		assertEquals(2 + 5, pos.x);
 		assertEquals(3 + 5, pos.y);
 
 		SwtUtilities.disposeResource(itemImage);
 		SwtUtilities.disposeResource(markerImage);
 
 	}
 
 	/**
 	 * Creates a image with a small green rectangle.
 	 * 
 	 * @return image
 	 */
 	private Image createItemImage() {
 		Image image = new Image(shell.getDisplay(), new Rectangle(0, 0, 10, 10));
 		GC gc = new GC(image);
 		gc.setForeground(LnfManager.getLnf().getColor("green"));
 		gc.setBackground(LnfManager.getLnf().getColor("green"));
 		gc.fillRectangle(0, 0, 5, 5);
 		gc.dispose();
 		return image;
 	}
 
 	/**
 	 * Creates a image with a very small red rectangle.
 	 * 
 	 * @return image
 	 */
 	private Image createMarkerImage() {
 		Image image = new Image(shell.getDisplay(), new Rectangle(0, 0, 5, 5));
 		GC gc = new GC(image);
 		gc.setForeground(LnfManager.getLnf().getColor("red"));
 		gc.setBackground(LnfManager.getLnf().getColor("red"));
 		gc.fillRectangle(0, 0, 2, 2);
 		gc.dispose();
 		return image;
 	}
 
 	/**
 	 * This Look and Feel returns always the same image.
 	 */
 	private class MyLnf extends RienaDefaultLnf {
 
 		private Image image;
 
 		public MyLnf() {
 			super();
 			image = createMarkerImage();
 		}
 
 		@Override
 		public Image getImage(String key) {
 			return image;
 		}
 
 	}
 
 	private class MockRenderer extends SubModuleTreeItemMarkerRenderer {
 
 		private boolean paintMarkersCalled;
 
 		public MockRenderer() {
 			resetPaintMarkersCalled();
 		}
 
 		public boolean isPaintMarkersCalled() {
 			return paintMarkersCalled;
 		}
 
 		public void resetPaintMarkersCalled() {
 			this.paintMarkersCalled = false;
 		}
 
 		/**
 		 * @see org.eclipse.riena.navigation.ui.swt.lnf.renderer.SubModuleTreeItemMarkerRenderer#paintMarkers(org.eclipse.swt.graphics.GC,
 		 *      java.util.Collection, org.eclipse.swt.widgets.TreeItem)
 		 */
 		@Override
 		protected void paintMarkers(GC gc, Collection<IIconizableMarker> markers, TreeItem item) {
 			super.paintMarkers(gc, markers, item);
 			this.paintMarkersCalled = true;
 		}
 
 	}
 
 }
