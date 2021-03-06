 /*
  *  Freeplane - mind map editor
  *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.freeplane.features.mindmapmode.attribute;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.JOptionPane;
 
 import org.freeplane.core.controller.Controller;
 import org.freeplane.core.ui.AFreeplaneAction;
 import org.freeplane.core.ui.AMultipleNodeAction;
 import org.freeplane.core.ui.EnabledAction;
 import org.freeplane.core.util.TextUtils;
 import org.freeplane.features.common.attribute.Attribute;
 import org.freeplane.features.common.attribute.NodeAttributeTableModel;
 import org.freeplane.features.common.map.NodeModel;
 import org.freeplane.features.common.styles.IStyle;
 import org.freeplane.features.common.styles.LogicalStyleController;
 import org.freeplane.features.common.styles.MapStyleModel;
 
 @EnabledAction(checkOnNodeChange=true)
 class CopyAttributes extends AFreeplaneAction {
 	private static Object[] attributes = null;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public static Object[] getAttributes() {
 		return attributes;
 	}
 
 	public CopyAttributes() {
 		super("CopyAttributes");
 	}
 
 	public void actionPerformed(final ActionEvent e) {
 		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
 		copyAttributes(node);
 	}
 
 	/**
 	 */
 	private void copyAttributes(final NodeModel node) {
 		final NodeAttributeTableModel model = NodeAttributeTableModel.getModel(node);
 		if(model == null){
 			attributes = null;
 			return;
 		}
 		final int attributeTableLength = model.getAttributeTableLength();
 		attributes = new Object[attributeTableLength * 2];
 		for(int i = 0; i < attributeTableLength; i++){
 			final Attribute attribute = model.getAttribute(i);
 			attributes[2 * i] = attribute.getName();
 			attributes[2 * i+1] = attribute.getValue();
 		}
 	}
 	@Override
     public void setEnabled() {
 		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
 		final NodeAttributeTableModel model = NodeAttributeTableModel.getModel(node);
 		setEnabled(model != null && model.getAttributeTableLength() > 0);
     }
 }
 
 @EnabledAction(checkOnPopup = true)
 class PasteAttributes extends AMultipleNodeAction {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public PasteAttributes() {
 		super("PasteAttributes");
 	}
 
 	@Override
 	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
 		pasteAttributes(node);
 	}
 
 	/**
 	 */
 	private void pasteAttributes(final NodeModel node) {
 		Object[] attributes = CopyAttributes.getAttributes();
 		if (attributes == null) {
 			JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(), TextUtils
 			    .getText("no_copy_attributes_before_paste_attributes"), "" /*=Title*/, JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		final MAttributeController controller = MAttributeController.getController();
 		for(int i = 0; i < attributes.length;){
 			final String name = attributes[i++].toString();
 			final Object value = attributes[i++];
 			controller.addAttribute(node, new Attribute(name, value));
 		}
 	}
 
 	@Override
     public void setEnabled() {
 		setEnabled(CopyAttributes.getAttributes() != null);
     }
 }
 
 @EnabledAction(checkOnPopup = true)
 class AddStyleAttributes extends AMultipleNodeAction {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public AddStyleAttributes() {
 		super("AddStyleAttributes");
 	}
 
 	@Override
 	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
 		pasteAttributes(node);
 	}
 
 	/**
 	 */
 	private void pasteAttributes(final NodeModel node) {
 		final NodeAttributeTableModel model = getAttributes(node);
 		if(model == null){
 			return;
 		}
 		final MAttributeController controller = MAttributeController.getController();
 		final int attributeTableLength = model.getAttributeTableLength();
 		for(int i = 0; i < attributeTableLength; i++){
 			final Attribute attribute = model.getAttribute(i);
 			controller.addAttribute(node, new Attribute(attribute.getName(), attribute.getValue()));
 		}
 	}
 
 	private NodeAttributeTableModel getAttributes(final NodeModel node) {
 		final IStyle style = LogicalStyleController.getController().getFirstStyle(node);
 		final MapStyleModel extension = MapStyleModel.getExtension(node.getMap());
 		final NodeModel styleNode = extension.getStyleNode(style);
 		final NodeAttributeTableModel model = NodeAttributeTableModel.getModel(styleNode);
 		if (model.getRowCount() > 0)
 			return model;
 		return null;
     }
 	
 	@Override
     public void setEnabled() {
 		for (final NodeModel selected : Controller.getCurrentModeController().getMapController().getSelectedNodes()) {
 			if(getAttributes(selected) != null){
 				setEnabled(true);
 				return;
 			}
 		}
 		setEnabled(false);
     }
 
 }
