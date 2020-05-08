 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.editor.agilegrid;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.SWTResourceManager;
 import org.agilemore.agilegrid.renderers.TextCellRenderer;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.rmf.pror.reqif10.util.ProrUtil;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.EnumValue;
 import org.eclipse.rmf.reqif10.datatypes.XhtmlContent;
 import org.eclipse.rmf.reqif10.util.Reqif10Util;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Rectangle;
 
 /**
  * @author Lukas Ladenberger
  * 
  */
 public class AbstractProrCellRenderer extends TextCellRenderer {
 
 	private final AdapterFactory adapterFactory;
 
 	/**
 	 * @param agileGrid
 	 */
 	public AbstractProrCellRenderer(AgileGrid agileGrid, AdapterFactory adapterFactory) {
 		super(agileGrid);
 		this.adapterFactory = adapterFactory;
 	}
 
 	protected int doDrawCellContentDefault(GC gc, Rectangle rect, Object value) {
 		String stringValue;
 		if (value instanceof AttributeValue) {
 			Object v = Reqif10Util.getTheValue((AttributeValue) value);
 			if (v instanceof XMLGregorianCalendar) {
 				XMLGregorianCalendar cal = (XMLGregorianCalendar) v;
 				Date date = cal.toGregorianCalendar().getTime();
 				stringValue = DateFormat.getDateInstance().format(date);
 			} else if (v instanceof XhtmlContent) {
 				stringValue = "XHTML NOT YET SUPPORTED";
 			} else if (v instanceof List<?>) {
 				stringValue = convertListToString((List<?>) v);
 			} else {
 				stringValue = v == null ? "" : v.toString();
 			}
 		} else {
 			stringValue = value != null ? value.toString() : "";
 		}
 
 		int alignment = getAlignment();
 		String wrappedText = wrapText(gc, stringValue, rect.width);
 		drawTextImage(gc, wrappedText, alignment, null, alignment, rect.x + 3,
 				rect.y + 2, rect.width - 6, rect.height - 4);
 		return gc.textExtent(wrappedText).y;
 	}
 
 	private String convertListToString(List<?> list) {
 		String stringValue;
 		StringBuffer sb = new StringBuffer();
 		for (Object object : list) {
 			if (object instanceof EnumValue) {
 				ItemProviderAdapter itemProvider = ProrUtil.getItemProvider(adapterFactory, object);
 				sb.append(itemProvider.getText(object));
 
 			} else {
 				sb.append(object.toString());
 			}
 			sb.append("\n");
 		}
 		if (sb.length() > 0)
 			sb.delete(sb.length() - 1, sb.length());
 		stringValue = sb.toString();
 		return stringValue;
 	}
 
 	// Workaround: Upon closing a UIEditor and reopening a new one, the color got
 	// disposed. No idea why. This is a workaround.
	@Override
 	protected void initialColor(int row, int col) {
 		if (agileGrid.isCellSelected(row, col)) {
 			background = SWTResourceManager.getColor(223, 227, 237);
 		}
 	}
 
 }
