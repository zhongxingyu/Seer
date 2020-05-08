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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.set.IObservableSet;
 import org.eclipse.core.databinding.observable.set.WritableSet;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.riena.beans.common.WordNode;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.tests.collect.NonUITestCase;
 import org.eclipse.riena.ui.ridgets.IColumnFormatter;
 import org.eclipse.riena.ui.ridgets.swt.ColumnFormatter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Tests for the class {@link TableRidgetLabelProvider}.
  */
 @NonUITestCase
 public class TableRidgetLabelProviderTest extends TestCase {
 
 	private WordNode elementA;
 	private WordNode elementB;
 	private Color colorA;
 	private Color colorB;
 	private Font fontA;
 	private Font fontB;
 	private IObservableMap[] attrMaps;
 	private IColumnFormatter[] formatter;
 
 	@Override
 	protected void setUp() throws Exception {
 		Display display = Display.getDefault();
 		Realm realm = SWTObservables.getRealm(display);
 		ReflectionUtils.invokeHidden(realm, "setDefault", realm);
 		colorA = display.getSystemColor(SWT.COLOR_RED);
 		colorB = display.getSystemColor(SWT.COLOR_GREEN);
 		fontA = new Font(display, "Arial", 12, SWT.NORMAL);
 		fontB = new Font(display, "Courier", 12, SWT.NORMAL);
 
 		IObservableSet elements = createElements();
 		String[] columnProperties = { "word", "upperCase" };
 		attrMaps = BeansObservables.observeMaps(elements, WordNode.class, columnProperties);
 		formatter = new IColumnFormatter[] { null, new TestColumnFormatter() };
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		fontA.dispose();
 		fontB.dispose();
 	}
 
 	public void testGetText() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps);
 
 		assertEquals("Alpha", labelProvider.getText(elementA));
 		assertEquals("BRAVO", labelProvider.getText(elementB));
 	}
 
 	public void testGetColumnText() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps);
 
 		assertEquals("Alpha", labelProvider.getColumnText(elementA, 0));
 		assertEquals("BRAVO", labelProvider.getColumnText(elementB, 0));
 
 		assertEquals("false", labelProvider.getColumnText(elementA, 1));
 		assertEquals("true", labelProvider.getColumnText(elementB, 1));
 
 		assertEquals(null, labelProvider.getColumnText(elementA, 99));
 	}
 
 	public void testGetColumnTextWithFormatter() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps, formatter);
 
 		assertEquals("Alpha", labelProvider.getColumnText(elementA, 0));
 		assertEquals("BRAVO", labelProvider.getColumnText(elementB, 0));
 
 		assertEquals("no", labelProvider.getColumnText(elementA, 1));
 		assertEquals("yes", labelProvider.getColumnText(elementB, 1));
 
 		assertEquals(null, labelProvider.getColumnText(elementA, 99));
 	}
 
 	public void testSetFormatters() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps, formatter);
 
 		assertEquals("no", labelProvider.getColumnText(elementA, 1));
 		assertEquals("yes", labelProvider.getColumnText(elementB, 1));
 
		Object arg2 = new IColumnFormatter[] { null, null };
		ReflectionUtils.invokeHidden(labelProvider, "setFormatters", arg2);
 
 		assertEquals("false", labelProvider.getColumnText(elementA, 1));
 		assertEquals("true", labelProvider.getColumnText(elementB, 1));
 
 		try {
			Object arg3 = new IColumnFormatter[] { null, null, null };
			ReflectionUtils.invokeHidden(labelProvider, "setFormatters", arg3);
 			fail();
 		} catch (RuntimeException rex) {
 			// ok
 		}
 	}
 
 	public void testGetImage() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps);
 
 		assertNull(labelProvider.getImage(elementA));
 		assertNull(labelProvider.getImage(elementB));
 
 		IObservableSet elements = createElements();
 		String[] columnProperties = { "upperCase" };
 		IObservableMap[] attrMap = BeansObservables.observeMaps(elements, WordNode.class, columnProperties);
 		labelProvider = new TableRidgetLabelProvider(attrMap);
 
 		Image siUnchecked = Activator.getSharedImage(SharedImages.IMG_UNCHECKED);
 		assertNotNull(siUnchecked);
 		assertEquals(siUnchecked, labelProvider.getImage(elementA));
 
 		Image siChecked = Activator.getSharedImage(SharedImages.IMG_CHECKED);
 		assertNotNull(siChecked);
 		assertEquals(siChecked, labelProvider.getImage(elementB));
 
 		assertNotSame(siChecked, siUnchecked);
 	}
 
 	public void testGetColumnImage() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps);
 
 		assertNull(labelProvider.getColumnImage(elementA, 0));
 		assertNull(labelProvider.getColumnImage(elementB, 0));
 
 		Image siUnchecked = Activator.getSharedImage(SharedImages.IMG_UNCHECKED);
 		assertNotNull(siUnchecked);
 		assertEquals(siUnchecked, labelProvider.getColumnImage(elementA, 1));
 
 		Image siChecked = Activator.getSharedImage(SharedImages.IMG_CHECKED);
 		assertNotNull(siChecked);
 		assertEquals(siChecked, labelProvider.getColumnImage(elementB, 1));
 
 		assertNotSame(siChecked, siUnchecked);
 
 		assertEquals(null, labelProvider.getColumnImage(elementA, 99));
 	}
 
 	public void testGetColumnImageWithFormatter() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps, formatter);
 
 		assertNull(labelProvider.getColumnImage(elementA, 0));
 		assertNull(labelProvider.getColumnImage(elementB, 0));
 
 		Image siCollaped = Activator.getSharedImage(SharedImages.IMG_NODE_COLLAPSED);
 		assertNotNull(siCollaped);
 		assertEquals(siCollaped, labelProvider.getColumnImage(elementA, 1));
 
 		Image siExpanded = Activator.getSharedImage(SharedImages.IMG_NODE_EXPANDED);
 		assertNotNull(siExpanded);
 		assertEquals(siExpanded, labelProvider.getColumnImage(elementB, 1));
 
 		assertNotSame(siExpanded, siCollaped);
 
 		assertEquals(null, labelProvider.getColumnImage(elementA, 99));
 	}
 
 	public void testGetForeground() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps, formatter);
 
 		assertNull(labelProvider.getForeground(elementA, 0));
 		assertNull(labelProvider.getForeground(elementB, 0));
 
 		assertSame(colorA, labelProvider.getForeground(elementA, 1));
 		assertSame(colorB, labelProvider.getForeground(elementB, 1));
 
 		assertEquals(null, labelProvider.getForeground(elementA, 99));
 	}
 
 	public void testGetBackground() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps, formatter);
 
 		assertNull(labelProvider.getBackground(elementA, 0));
 		assertNull(labelProvider.getBackground(elementB, 0));
 
 		assertSame(colorA, labelProvider.getBackground(elementA, 1));
 		assertSame(colorB, labelProvider.getBackground(elementB, 1));
 
 		assertEquals(null, labelProvider.getBackground(elementA, 99));
 	}
 
 	public void testGetFont() {
 		TableRidgetLabelProvider labelProvider = new TableRidgetLabelProvider(attrMaps, formatter);
 
 		assertNull(labelProvider.getFont(elementA, 0));
 		assertNull(labelProvider.getFont(elementB, 0));
 
 		assertSame(fontA, labelProvider.getFont(elementA, 1));
 		assertSame(fontB, labelProvider.getFont(elementB, 1));
 
 		assertEquals(null, labelProvider.getFont(elementA, 99));
 	}
 
 	// helping methods
 	// ////////////////
 
 	private IObservableSet createElements() {
 		Collection<WordNode> collection = new ArrayList<WordNode>();
 		elementA = new WordNode("Alpha");
 		elementB = new WordNode("Bravo");
 		elementB.setUpperCase(true);
 		collection.add(elementA);
 		collection.add(elementB);
 		IObservableSet elements = new WritableSet(Realm.getDefault(), collection, WordNode.class);
 		return elements;
 	}
 
 	private final class TestColumnFormatter extends ColumnFormatter {
 
 		@Override
 		public String getText(Object element) {
 			WordNode wordNode = (WordNode) element;
 			return wordNode.isUpperCase() ? "yes" : "no";
 		}
 
 		@Override
 		public Image getImage(Object element) {
 			WordNode wordNode = (WordNode) element;
 			String key = "alpha".equalsIgnoreCase(wordNode.getWord()) ? SharedImages.IMG_NODE_COLLAPSED
 					: SharedImages.IMG_NODE_EXPANDED;
 			return Activator.getSharedImage(key);
 		}
 
 		@Override
 		public Color getForeground(Object element) {
 			WordNode wordNode = (WordNode) element;
 			return "alpha".equalsIgnoreCase(wordNode.getWord()) ? colorA : colorB;
 		}
 
 		@Override
 		public Color getBackground(Object element) {
 			WordNode wordNode = (WordNode) element;
 			return "alpha".equalsIgnoreCase(wordNode.getWord()) ? colorA : colorB;
 		}
 
 		@Override
 		public Font getFont(Object element) {
 			WordNode wordNode = (WordNode) element;
 			return "alpha".equalsIgnoreCase(wordNode.getWord()) ? fontA : fontB;
 		}
 	}
 
 }
