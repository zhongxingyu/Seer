 /*
  * Last modification information:
 * $Revision: 1.1 $
 * $Date: 2004-09-02 16:27:28 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.ui;
 
 import java.awt.Color;
 import java.util.Vector;
 
 import org.concord.data.ui.DataColumnDescription;
 import org.concord.data.ui.DefaultTableCellColorModel;
 import org.concord.datagraph.engine.DataGraphable;
 import org.concord.framework.data.stream.DataStore;
 
 
 /**
  * DataGraphableTableCellColor
  * Class name and description
  *
  * Date created: Sep 1, 2004
  *
  * @author imoncada<p>
  *
  */
 public class DataGraphableTableCellColor extends DefaultTableCellColorModel
 {
 	Vector dataGraphables;	//List of DataGraphable objects 
 
 	/**
 	 * 
 	 */
 	public DataGraphableTableCellColor()
 	{
 		this(null);
 	}
 
 	/**
 	 * 
 	 */
 	public DataGraphableTableCellColor(Vector dataGraphableList)
 	{
 		setDataGraphableList(dataGraphableList);
 	}
 	
 	/**
 	 * @see org.concord.data.ui.TableCellColorModel#getBackgroundColor(int, int, boolean)
 	 */
 	public Color getBackgroundColor(int row, int col, boolean selected, boolean focus)
 	{
 		Color specificColor = super.getBackgroundColor(row, col, selected, focus);
 		if (specificColor != null) return specificColor;
 		if (dataGraphables == null) return null;
 		if (selected){
 			return null;//new Color(210,210,200);
 		}
 		return Color.white;
 	}
 
 	/**
 	 * @see org.concord.data.ui.TableCellColorModel#getForegroundColor(int, int, boolean)
 	 */
 	public Color getForegroundColor(int row, int col, boolean selected, boolean focus)
 	{
 		Color specificColor = super.getForegroundColor(row, col, selected, focus);
 		if (specificColor != null) return specificColor;
 		if (dataGraphables == null) return null;
 		DataColumnDescription dcol = (DataColumnDescription)dataGraphables.elementAt(col);
 		DataStore ds = dcol.getDataStore();
 		if (ds instanceof DataGraphable){
			return ((DataGraphable)ds).getLineColor();
 		}
 		return null;
 	}
 
 	/**
 	 * @see org.concord.data.ui.TableCellColorModel#getBorderColor(int, int, boolean)
 	 */
 	public Color getBorderColor(int row, int col, boolean selected, boolean focus)
 	{
 		Color specificColor = super.getBorderColor(row, col, selected, focus);
 		if (specificColor != null) return specificColor;
 		if (selected && focus){
 			return getForegroundColor(row, col, selected, focus);
 		}
 		else{
 			return null;
 		}
 	}
 
 	/**
 	 * @return Returns the dataGraphable.
 	 */
 	public Vector getDataGraphableList()
 	{
 		return dataGraphables;
 	}
 	
 	/**
 	 * @param dataGraphable The dataGraphable to set.
 	 */
 	public void setDataGraphableList(Vector dataGraphableList)
 	{
 		this.dataGraphables = dataGraphableList;
 	}
 }
