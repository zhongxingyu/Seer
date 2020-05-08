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
 package cytoscape.visual;
 
 import cytoscape.visual.ui.icon.NodeIcon;
 import cytoscape.visual.ui.icon.VisualPropertyIcon;
 
 import ding.view.DGraphView;
 
 import giny.view.NodeView;
 
 import java.awt.Shape;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.Icon;
 
 /**
  *
  * @since Cytoscape 2.5
  * @version 0.7
  * @author kono
  *
  */
 public enum NodeShape {
 	RECT(NodeView.RECTANGLE, "Rectangle", true),
 	ROUND_RECT(NodeView.ROUNDED_RECTANGLE, "Round Rectangle", true),
 	RECT_3D(NodeView.RECTANGLE, "3D Rectabgle", false),
 	TRAPEZOID(NodeView.RECTANGLE, "Trapezoid", false),
 	TRAPEZOID_2(NodeView.RECTANGLE, "Trapezoid 2", false),
 	TRIANGLE(NodeView.TRIANGLE, "Triangle", true),
 	PARALLELOGRAM(NodeView.PARALELLOGRAM, "Parallelogram", true),
 	DIAMOND(NodeView.DIAMOND, "Diamond", true),
 	ELLIPSE(NodeView.ELLIPSE, "Ellipse", true),
 	HEXAGON(NodeView.HEXAGON, "Hexagon", true),
	OCTAGON(NodeView.OCTAGON, "Octagon", true);
 
 	private int ginyShape;
 	private String name;
 	private boolean isSupported;
 	private static Map<Integer, Shape> nodeShapes = DGraphView.getNodeShapes();
 
 	private NodeShape(int ginyShape, String name, boolean isSupported) {
 		this.ginyShape = ginyShape;
 		this.name = name;
 		this.isSupported = isSupported;
 	}
 
 	/**
 	 * If the shape is supported by rendering engine, return true.<br>
 	 * Otherwise, return false.
 	 * @return
 	 */
 	public boolean isSupported() {
 		return isSupported;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param text DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public static NodeShape parseNodeShapeText(String text) {
 		String trimed = text.trim();
 
 		for (NodeShape shape : values()) {
 			if (getNodeShapeText(shape).equalsIgnoreCase(trimed))
 				return shape;
 		}
 
 		// Unknown shape: return rectangle.
 		return NodeShape.RECT;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public static String[] valuesAsString() {
 		final int length = values().length;
 		final String[] nameArray = new String[length];
 
 		for (int i = 0; i < length; i++)
 			nameArray[i] = values()[i].getShapeName();
 
 		return nameArray;
 	}
 
 	/**
 	 * Get name of the shape.
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getShapeName() {
 		return name;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param type
 	 *            DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public static boolean isValidShape(NodeShape type) {
 		for (NodeShape curType : values()) {
 			if (type == curType)
 				return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param shape
 	 *            DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public static String getNodeShapeText(NodeShape shape) {
 		String nstext = shape.name();
 		nstext = nstext.replaceAll("_", "");
 
 		return nstext.toLowerCase();
 	}
 
 	/**
 	 * Get GINY shape as integer.
 	 *
 	 * @return Giny shape as integer.
 	 */
 	public int getGinyShape() {
 		return ginyShape;
 	}
 
 	/**
 	 * Convert from Giny shape to Cytoscape NodeShape enum.
 	 *
 	 * @param ginyShape
 	 * @return
 	 */
 	public static NodeShape getNodeShape(int ginyShape) {
 		for (NodeShape shape : values()) {
 			if (shape.ginyShape == ginyShape)
 				return shape;
 		}
 
 		// Unknown. Return rectangle as the def val.
 		return NodeShape.RECT;
 	}
 
 	/**
 	 * Returns a Shape object for the NodeShape in question.
 	 */
 	public Shape getShape() {
 		return nodeShapes.get(ginyShape);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public static Map<Object, Icon> getIconSet() {
 		Map<Object, Icon> nodeShapeIcons = new HashMap<Object, Icon>();
 
 		for (NodeShape shape : values()) {
			NodeIcon icon = new NodeIcon(nodeShapes.get(shape.getGinyShape()),
 			                             VisualPropertyIcon.DEFAULT_ICON_SIZE,
 			                             VisualPropertyIcon.DEFAULT_ICON_SIZE, shape.getShapeName());
 			nodeShapeIcons.put(shape, icon);
 		}
 
 		return nodeShapeIcons;
 	}
 }
