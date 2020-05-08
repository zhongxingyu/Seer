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
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.agilemore.agilegrid.AgileGrid;
 import org.agilemore.agilegrid.SWTResourceManager;
 import org.agilemore.agilegrid.renderers.TextCellRenderer;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.rmf.pror.reqif10.editor.presentation.Reqif10EditorPlugin;
 import org.eclipse.rmf.pror.reqif10.util.ConfigurationUtil;
 import org.eclipse.rmf.reqif10.ReqIf;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.SpecRelation;
 import org.eclipse.rmf.reqif10.util.Reqif10Util;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 
 public class ProrLinkCellRenderer extends TextCellRenderer {
 
 	private Set<SpecRelation> incoming;
 	private Set<SpecRelation> outgoing;
 	
 	private final Image specRelationConnectorIcon;
 
 
 	public ProrLinkCellRenderer(AgileGrid agileGrid) {
 		super(agileGrid);
 		specRelationConnectorIcon = ExtendedImageRegistry
 				.getInstance()
 				.getImage(
 						Reqif10EditorPlugin.INSTANCE
 								.getImage("full/obj16/SpecRelationConnector.png"));
 	}
 
 	@Override
 	protected void doDrawCellContent(GC gc, Rectangle rect, int row, int col) {
 		Object content = agileGrid.getContentAt(row, col);
 		if (content instanceof SpecRelation) {
 			SpecObject target = ((SpecRelation) content).getTarget();
 			if (target != null) {
 				String label = ConfigurationUtil.getSpecElementLabel(target);
 				drawTextImage(gc, label, alignment, null, alignment,
 						rect.x + 3, rect.y + 2, rect.width - 6, rect.height - 4);
 			}
 		} else
 		if (content instanceof SpecElementWithAttributes) {
 			updateIncomingAndOutgoing((SpecElementWithAttributes) content);
 			if (incoming.size() == 0 && outgoing.size() == 0)
 				return;
 			String in = incoming.size() + " ";
 			String out = " " + outgoing.size();
 			int alignment = getAlignment();
 
 			drawTextImage(gc, in, alignment, null, alignment,
 					rect.x + 3, rect.y + 2, rect.width - 6, rect.height - 4);
 			int offset = gc.textExtent(in).x;
 
 			drawTextImage(gc, out, alignment, specRelationConnectorIcon,
 					alignment, rect.x + 3 + offset, rect.y + 2,
 					rect.width - 6, rect.height - 4);
 
 		}
 	}
 
 	/**
 	 * This method updates the two Sets {@link #incoming} and {@link #outgoing}.
 	 */
 	private void updateIncomingAndOutgoing(
 			SpecElementWithAttributes specElement) {
 		ReqIf rif = Reqif10Util.getReqIf(specElement);
 		incoming = new HashSet<SpecRelation>();
 		outgoing = new HashSet<SpecRelation>();
 		if (specElement == null)
 			return;
 		EList<SpecRelation> relations = rif.getCoreContent()
 				.getSpecRelations();
 		for (SpecRelation relation : relations) {
 			if (relation.getTarget() != null
 					&& relation.getTarget().equals(specElement)) {
 				incoming.add(relation);
 			}
 			if (relation.getSource() != null
 					&& relation.getSource().equals(specElement)) {
 				outgoing.add(relation);
 			}
 		}
 	}
 	
 	// Workaround: Upon closing a UIEditor and reopening a new one, the color got
 	// disposed. No idea why. This is a workaround.
 	protected void initialColor(int row, int col) {
 		if (agileGrid.isCellSelected(row, col)) {
 			background = SWTResourceManager.getColor(223, 227, 237);
 		}
 	}
 
 }
