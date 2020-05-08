 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.core.database.nodes;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.text.Format;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.SplashRelationshipTypes;
 import org.neo4j.api.core.Node;
 
 /**
  * Wrapper of Spreadsheet Column
  * 
  * @author Lagutko_N
  */
 
 public class SplashFormatNode extends AbstractNode {
 
 	/** String CELL_FORMAT_TYPE field */
     private static final String CELL_FORMAT_TYPE = "format";
     /*
 	 * Type of this Node
 	 */
 	private static final String SPLASH_FORMAT_NODE_TYPE = "splash_format";
 	/*
 	 * Background Color property. Blue component.
 	 */    
 	private static final String CELL_BG_COLOR_B = "bg_color_b";
 
 	/*
 	 * Background Color property. Green component.
 	 */
 	private static final String CELL_BG_COLOR_G = "bg_color_g";
 
 	/*
 	 * Background Color property. Red component.
 	 */
 	private static final String CELL_BG_COLOR_R = "bg_color_r";
 
 	/*
 	 * Font Color property. Blue component.
 	 */
 	private static final String CELL_FONT_COLOR_B = "font_color_b";
 
 	/*
 	 * Font Color property. Green component.
 	 */
 	private static final String CELL_FONT_COLOR_G = "font_color_g";
 
 	/*
 	 * Font Color property. Red component.
 	 */
 	private static final String CELL_FONT_COLOR_R = "font_color_r";
 
 	/*
 	 * Horizontal Alignment property.
 	 */
 	private static final String CELL_HORIZONTAL_ALIGNMENT = "horizontal_alignment";
 
 	/*
 	 * Vertical Alignment property.
 	 */
 	private static final String CELL_VERTICAL_ALIGNMENT = "vertical_alignment";
 
 	/*
 	 * Font Style property.
 	 */
 	private static final String CELL_FONT_STYLE = "font_style";
 
 	/*
 	 * Font Size property.
 	 */
 	private static final String CELL_FONT_SIZE = "font_size";
 
 	/*
 	 * Font Name property.
 	 */
 	private static final String CELL_FONT_NAME = "font_name";
 	
 	/**
      * Constructor for wrapping existing column nodes. To reduce API confusion,
      * this constructor is private, and users should use the factory method instead.
      * @param node
      */
     public SplashFormatNode(Node node) {
         super(node);
         setParameter(INeoConstants.PROPERTY_TYPE_NAME, SPLASH_FORMAT_NODE_TYPE);
     }
     
     /**
      * Use factory method to ensure clear API different to normal constructor.
      *
      * @param node representing an existing column project
      * @return SplashFormatNode from existing Node
      */
     public static SplashFormatNode fromNode(Node node) {
         return new SplashFormatNode(node);
     }
 
 	
 	/**
 	 * Returns Font Name of Cell
 	 *
 	 * @return cell's font name
 	 */
 	public String getFontName() {
 		return (String)getParameter(CELL_FONT_NAME);
 	}
 
 	/**
 	 * Sets Font Name for Cell
 	 *
 	 * @param fontName cell's font name
 	 */
 	public void setFontName(String fontName) {
 		setParameter(CELL_FONT_NAME, fontName);
 	}
 
 	/**
 	 * Returns Font Size of Cell
 	 *
 	 * @return cell's font size
 	 */
 	public Integer getFontSize() {
 		return (Integer)getParameter(CELL_FONT_SIZE);
 	}
 
 	/**
 	 * Sets Font Size for Cell 
 	 *
 	 * @param fontSize cell's font size
 	 */    
 	public void setFontSize(Integer fontSize) {
 		setParameter(CELL_FONT_SIZE, fontSize);
 	}
 
 	/**
 	 * Returns Font Style of Cell
 	 *
 	 * @return cell's font style
 	 */
 	public Integer getFontStyle() {
 		return (Integer)getParameter(CELL_FONT_STYLE);
 	}
 
 	/**
 	 * Sets Font Style for Cell
 	 *
 	 * @param fontStyle cell's font style
 	 */
 	public void setFontStyle(Integer fontStyle) {
 		setParameter(CELL_FONT_STYLE, fontStyle);
 	}
 
 	/**
 	 * Returns Vertical Alignment of Cell
 	 *
 	 * @return cell's vertical alignment
 	 */
 	public Integer getVerticalAlignment() {
 		return (Integer)getParameter(CELL_VERTICAL_ALIGNMENT);
 	}
 
 	/**
 	 * Sets Vertical Alignment for Cell
 	 *
 	 * @param verticalAlignment cell's vertical alignment
 	 */
 	public void setVerticalAlignment(Integer verticalAlignment) {
 		setParameter(CELL_VERTICAL_ALIGNMENT, verticalAlignment);
 	}
 
 	/**
 	 * Returns Horizontal Alignment of Cell
 	 *
 	 * @return cell's horizontal alignment
 	 */
 	public Integer getHorizontalAlignment() {
 		return (Integer)getParameter(CELL_HORIZONTAL_ALIGNMENT);
 	}
 
 	/**
 	 * Sets Horizontal Alignment for Cell
 	 *
 	 * @param horizontalAlignment cell's horizontal alignment
 	 */
 	public void setHorizontalAlignment(Integer horizontalAlignment) {
 		setParameter(CELL_HORIZONTAL_ALIGNMENT, horizontalAlignment);
 	}
 
 	/**
 	 * Returns Red Component of Cell's Font Color
 	 *
 	 * @return red component of Font Color
 	 */
 	public Integer getFontColorR() {
 		return (Integer)getParameter(CELL_FONT_COLOR_R);
 	}
 
 	/**
 	 * Sets Red Component for Cell's Font Color
 	 *
 	 * @param fontColorR red component of Font Color
 	 */
 	public void setFontColorR(Integer fontColorR) {
 		setParameter(CELL_FONT_COLOR_R, fontColorR);
 	}
 
 	/**
 	 * Returns Green Component of Cell's Font Color
 	 *
 	 * @return green component of Font Color
 	 */
 	public Integer getFontColorG() {
 		return (Integer)getParameter(CELL_FONT_COLOR_G);
 	}
 
 	/**
 	 * Sets Green Component for Cell's Font Color
 	 *
 	 * @param fontColorG green component of Font Color
 	 */
 	public void setFontColorG(Integer fontColorG) {
 		setParameter(CELL_FONT_COLOR_G, fontColorG);
 	}
 
 	/**
 	 * Returns Blue Component of Cell's Font Color
 	 *
 	 * @return Blue component of Font Color
 	 */
 	public Integer getFontColorB() {
 		return (Integer)getParameter(CELL_FONT_COLOR_B);
 	}
 
 	/**
 	 * Sets Blue Component for Cell's Font Color
 	 *
 	 * @param fontColorB blue component of Font Color
 	 */
 	public void setFontColorB(Integer fontColorB) {
 		setParameter(CELL_FONT_COLOR_B, fontColorB);
 	}
 
 	/**
 	 * Returns Red Component of Cell's Background Color
 	 *
 	 * @return red component of Background Color
 	 */
 	public Integer getBackgroundColorR() {
 		return (Integer)getParameter(CELL_BG_COLOR_R);
 	}
 
 	/**
 	 * Sets Red Component for Cell's Background Color
 	 *
 	 * @param backgroundColorR red component of Background Color
 	 */
 	public void setBackgroundColorR(Integer backgroundColorR) {
 		setParameter(CELL_BG_COLOR_R, backgroundColorR);
 	}
 
 	/**
 	 * Returns Red Component of Cell's Background Color
 	 *
 	 * @return red component of Background Color
 	 */
 	public Integer getBackgroundColorG() {
 		return (Integer)getParameter(CELL_BG_COLOR_G);
 	}
 
 	/**
 	 * Sets Green Component for Cell's Background Color
 	 *
 	 * @param backgroundColorG green component of Background Color
 	 */
 	public void setBackgroundColorG(Integer backgroundColorG) {
 		setParameter(CELL_BG_COLOR_G, backgroundColorG);
 	}
 
 	/**
 	 * Returns Blue Component of Cell's Background Color
 	 *
 	 * @return blue component of Background Color
 	 */
 	public Integer getBackgroundColorB() {
 		return (Integer)getParameter(CELL_BG_COLOR_B);
 	}
 
 	/**
 	 * Sets Blue Component for Cell's Background Color
 	 *
 	 * @param backgroundColorB blue component of Background Color
 	 */
 	public void setBackgroundColorB(Integer backgroundColorB) {
 		setParameter(CELL_BG_COLOR_B, backgroundColorB);
 	}
 	
 	/**
 	 * Adds a Cell to Column
 	 * 
 	 * @param cell
 	 *            Cell
 	 */
 	public void addCell(CellNode cell) {
 		addRelationship(SplashRelationshipTypes.SPLASH_FORMAT, cell.getUnderlyingNode());
 	}
 	
 	/**
 	 * Sets a format of Cell
 	 *
 	 * @param format format
 	 */
 	public void setFormat(Format format) {
 	    try {
 	        setParameter(CELL_FORMAT_TYPE, serializeFormat(format));
 	    }
 	    catch (Exception e) {
 	        NeoCorePlugin.error(null, e);
 	    }
 	}
 	
 	/**
 	 * Returns a format of Cell
 	 *
 	 * @return format of Cell
 	 */
 	public Format getFormat() {
 	    try {
 	        return deserializeFormat((byte[])getParameter(CELL_FORMAT_TYPE));
 	    }
 	    catch (Exception e) {
 	        NeoCorePlugin.error(null, e);
 	        return null;
 	    }
 	}
 	
 	/**
 	 * Utility method that serializes Format object to byte array
 	 *
 	 * @param format format of Cell 
 	 * @return byte array of serialized format
 	 * @throws IOException
 	 */
 	private byte[] serializeFormat(Format format) throws IOException {
 	    ByteArrayOutputStream s = new ByteArrayOutputStream();
 	    ObjectOutputStream o = new ObjectOutputStream(s);
 	    o.writeObject(format);
 	    o.close();
 	    s.close();	    
 	    
 	    return s.toByteArray();	    
 	}
 	
 	/**
 	 * Utility method that deserializes Format object from byte array
 	 *
 	 * @param format byte array of serialized Format
 	 * @return Format object
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
	private Format deserializeFormat(byte[] format) throws IOException, ClassNotFoundException, NullPointerException {
	    if (format == null) {
	        return null;
	    }
 	    ByteArrayInputStream s = new ByteArrayInputStream(format);
 	    ObjectInputStream o = new ObjectInputStream(s);
 	    
 	    o.close();
 	    s.close();
 	    
 	    return (Format)o.readObject();
 	}
 	
 }
