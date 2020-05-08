 package org.dawb.common.ui.plot.axis;
 
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 /**
  * Interface used to define an axis on the graph (multiple x and y are allowed).
  * 
  * You can get an IAxis by using IPlottingSystem.createAxis(...) then you can configure the
  * axis as desired, for instance setLog10(...)
  * 
  * @author fcp94556
  *
  */
 public interface IAxis {
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getTitle();
 	
 	/**
 	 * 
 	 * @param title
 	 */
 	public void setTitle(String title);
 	
 	/**
 	 * 
 	 * @param titleFont
 	 */
 	public void setTitleFont(final Font titleFont);
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Font getTitleFont();
 	
 	/**
 	 * 
 	 * @return true if axis is a primary one that cannot be deleted.
 	 */
	public boolean isPrimaryAxis();
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isLog10();
 	
 	/**
 	 * 
 	 * @param isLog10
 	 */
 	public void setLog10(final boolean isLog10);
 	
 	/**
 	 * 
 	 * @param color
 	 */
 	public void setForegroundColor(final Color color);
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Color getForegroundColor();
 	
 	/**
 	 * 
 	 * @param color
 	 */
 	public void setBackgroundColor(final Color color);
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Color getBackgroundColor();
 
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isShowMajorGrid();
 
 	/**
 	 * 
 	 * @param showMajorGrid
 	 */
 	public void setShowMajorGrid(final boolean showMajorGrid);
 
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isShowMinorGrid();
 
 	/**
 	 * 
 	 * @param showMinorGrid
 	 */
 	public void setShowMinorGrid(final boolean showMinorGrid);
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Color getMajorGridColor();
 
 	/**
 	 * 
 	 * @param majorGridColor
 	 */
 	public void setMajorGridColor(final Color majorGridColor);
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Color getMinorGridColor();
 
 	/**
 	 * 
 	 * @param minorGridColor
 	 */
 	public void setMinorGridColor(final Color minorGridColor);
 	
 	/**
 	 * NumberFormat pattern for axis
 	 * @return
 	 */
 	public String getFormatPattern();
 	
 	/**
 	 * NumberFormat pattern for axis
 	 * @param formatPattern
 	 */
 	public void setFormatPattern(String formatPattern);
 
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isYAxis();
 	
 	/**
 	 * 
 	 * @param isYAxis
 	 */
 	public void setYAxis(final boolean isYAxis);
 
 	/**
 	 * 
 	 * @param b
 	 */
 	public void setVisible(boolean b);
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isVisible();
 	
 	/**
 	 * returns the upper bound in the units of the axis (not pixels)
 	 */
 	public double getUpper();
 	
 	/**
 	 * returns the lower bound in the units of the axis (not pixels)
 	 */
 	public double getLower();
 
 	/**
 	 * Sets the range of the axis, start maybe > end for reversed axes.
 	 * @param start
 	 * @param end
 	 */
 	public void setRange(double start, double end);
 	
 	/**
 	 * Add a listener notified when the axis changes.
 	 * @param listener
 	 */
 	public void addAxisListener(IAxisListener listener);
 	
 	/**
 	 * Add a listener notified when the axis changes.
 	 * @param listener
 	 */
 	public void removeAxisListener(IAxisListener listener);
 
 	/**
 	 * Sets if the date format should be formatting this axis.
 	 * @param dateEnabled
 	 */
 	public void setDateFormatEnabled(boolean dateEnabled);
 
 	/**
 	 * @return true if in date/time mode, false normally.
 	 */
 	public boolean isDateFormatEnabled();
 	
 	/**
 	 * The position in pixels of a given value.
 	 * @param value
 	 * @return
 	 */
 	public int getValuePosition(double value);
 	
 	/**
 	 * The value for a position in pixels.
 	 * @param value
 	 * @return
 	 */
 	public double getPositionValue(int position);
 
 	
 	/**
 	 * Sets an alternative dataset to use for the labels and the title
 	 * @param labels
 	 */
 	public void setLabelDataAndTitle(AbstractDataset labels);
 	
 	/**
 	 * Call to set a maximum range for the axis 
 	 */
 	public void setMaximumRange(double lower, double upper);
 
 	/**
 	 * Set axis scale so that there are ticks on the start and end
 	 * @param ticksAtEnds if true then place ticks on scale ends
 	 */
 	public void setTicksAtEnds(boolean ticksAtEnds);
 }
