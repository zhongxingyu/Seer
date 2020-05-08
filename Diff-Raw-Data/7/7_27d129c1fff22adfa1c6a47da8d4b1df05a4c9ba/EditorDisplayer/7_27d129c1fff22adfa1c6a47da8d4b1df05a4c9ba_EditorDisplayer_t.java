 
 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 
 package cytoscape.visual.ui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Frame;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 import cytoscape.Cytoscape;
 import cytoscape.util.CyColorChooser;
 import cytoscape.visual.ArrowShape;
 import cytoscape.visual.LabelPosition;
 import cytoscape.visual.LineStyle;
 import cytoscape.visual.NodeShape;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.ui.editors.continuous.C2CMappingEditor;
 import cytoscape.visual.ui.editors.continuous.C2DMappingEditor;
 import cytoscape.visual.ui.editors.continuous.GradientEditorPanel;
 
 /**
  * Managing value editors in for each data types.<br>
  * This enum contains both continuous and discrete editors.
  *
  * @version 0.5
  * @since Cytoscape 2.5
  * @author kono
  *
  */
 public enum EditorDisplayer {
 
 	DISCRETE_COLOR(CyColorChooser.class, "showDialog",
 	               new Class[] { Component.class, String.class, Color.class },
 	               new Object[] { Cytoscape.getDesktop(), "Select Color...", null }, Color.class),
 	DISCRETE_FONT(PopupFontChooser.class, "showDialog",
 	              new Class[] { Frame.class, Font.class },
 	              new Object[] { Cytoscape.getDesktop(), null }, Font.class), 
 	DISCRETE_NUMBER(JOptionPane.class, "showInputDialog",
 	                new Class[] { Component.class, Object.class },
 	                new Object[] { Cytoscape.getDesktop(), "Please enter new numeric value:" },
 	                Number.class), 
 	DISCRETE_STRING(JOptionPane.class, "showInputDialog",
 	                new Class[] { Component.class, Object.class },
 	                new Object[] { Cytoscape.getDesktop(), "Please enter new text value:" },
 	                String.class), 
 	DISCRETE_SHAPE(ValueSelectDialog.class, "showDialog",
 	               new Class[] { VisualPropertyType.class, JDialog.class },
 	               new Object[] { VisualPropertyType.NODE_SHAPE, null }, NodeShape.class), 
 	DISCRETE_ARROW_SHAPE(ValueSelectDialog.class, "showDialog",
 	                     new Class[] { VisualPropertyType.class, JDialog.class },
 	                     new Object[] { VisualPropertyType.EDGE_SRCARROW_SHAPE, null }, ArrowShape.class), 
 	DISCRETE_LINE_STYLE(ValueSelectDialog.class, "showDialog",
 	                   new Class[] { VisualPropertyType.class, JDialog.class },
 	                   new Object[] { VisualPropertyType.EDGE_LINE_STYLE, null }, LineStyle.class), 
 	DISCRETE_LABEL_POSITION(PopupLabelPositionChooser.class, "showDialog",
 	                        new Class[] { Frame.class, LabelPosition.class },
 	                        new Object[] { Cytoscape.getDesktop(), null }, LabelPosition.class), 
 	CONTINUOUS_COLOR(GradientEditorPanel.class, "showDialog",
 	                 new Class[] { int.class, int.class, String.class, VisualPropertyType.class },
 	                 new Object[] { 450, 180, "Gradient Editor", null }, Color.class), 
 	CONTINUOUS_CONTINUOUS(C2CMappingEditor.class, "showDialog",
 	                      new Class[] { int.class, int.class, String.class, VisualPropertyType.class },
 	                      new Object[] { 450, 250, "Continuous-Continuous Editor", null },
 	                      Number.class), 
 	CONTINUOUS_DISCRETE(C2DMappingEditor.class, "showDialog",
 	                    new Class[] { int.class, int.class, String.class, VisualPropertyType.class },
 	                    new Object[] { 450, 200, "Continuous-Discrete Editor", null }, Object.class);
 	private Class chooserClass;
 	private String command;
 	private Class[] paramTypes;
 	private Object[] parameters;
 	private Class compatibleClass;
 
 	/**
 	 * Defines editor type.
 	 */
 	public enum EditorType {
 		CONTINUOUS,
 		DISCRETE,
 		PASSTHROUGH;
 	}
 	private EditorDisplayer(Class chooserClass, String command, Class[] paramTypes,
 	                        Object[] parameters, Class compatibleClass) {
 		this.chooserClass = chooserClass;
 		this.command = command;
 		this.paramTypes = paramTypes;
 		this.parameters = parameters;
 		this.compatibleClass = compatibleClass;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public Class getActionClass() {
 		return chooserClass;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getCommand() {
 		return command;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public Class[] getParamTypes() {
 		return this.paramTypes;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public Object[] getParameters() {
 		return this.parameters;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public Class getCompatibleClass() {
 		return this.compatibleClass;
 	}
 
 	public void setParameters(Object[] param) {
 		this.parameters = param;
 	}
 
 	/**
 	 * Returns proper editor displayer object based on visual property type.
 	 *
 	 * @param type DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public static EditorDisplayer getEditor(final VisualPropertyType type, final EditorType editor) {
 		final Class dataType = type.getDataType();
 		for (EditorDisplayer command : values()) {
 			if ((dataType == command.getCompatibleClass())
 			    && (((editor == EditorType.CONTINUOUS)
 			        && command.toString().startsWith(EditorType.CONTINUOUS.name()))
 			       || ((editor == EditorType.DISCRETE)
 			          && command.toString().startsWith(EditorType.DISCRETE.name()))))
 				return command;
 		}
 
 		
 		/*
 		 * if not found in the loop above, this might be a C2DEditor.
 		 */
 		
 		return EditorDisplayer.CONTINUOUS_DISCRETE;
 	}
 }
