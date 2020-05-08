 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.roi;
 
 import java.io.Serializable;
 
 public class GridROI extends RectangularROI implements Serializable {
 
 	double xSpacing = 7;
 	double ySpacing = 7;
 	boolean midPointOn = true;
 	boolean gridLinesOn = false;
 	private GridPreferences gridPref = new GridPreferences();
 
 	@SuppressWarnings("unused")
 	private GridROI() {
 		super();
 	}
 
 	@SuppressWarnings("unused")
 	private GridROI(double width, double angle) {
 		super(width, angle);
 	}
 
 	@SuppressWarnings("unused")
 	private GridROI(double width, double height, double angle) {
 		super(width, height, angle);
 	}
 
 	
 	/**
 	 * If using this constructor, you should set x and y spacing and GridPreferences
 	 * at some point.
 	 * 
 	 * @param ptx
 	 * @param pty
 	 * @param width
 	 * @param height
 	 * @param angle
 	 */
 	public GridROI(double ptx, double pty, double width, double height, double angle) {
 		super(ptx, pty, width, height, angle);
 	}
 
 	@SuppressWarnings("unused")
 	private GridROI(double ptx, double pty, double width, double height, double angle, boolean clip) {
 		super(ptx, pty, width, height, angle, clip);
 	}
 
 	private GridROI(double ptx, double pty, double width, double height, double angle, double xSpacing, double ySpacing) {
 		super(ptx, pty, width, height, angle, false);
 		this.xSpacing = xSpacing;
 		this.ySpacing = ySpacing;
 	}
 
 	public GridROI(double ptx, double pty, double width, double height, double angle, double xSpacing, double ySpacing,
 			boolean gridLinesOn, boolean midPointOn, GridPreferences gridPref) {
 		this(ptx, pty, width, height, angle, xSpacing, ySpacing);
 		this.gridLinesOn = gridLinesOn;
 		this.midPointOn = midPointOn;
 		this.gridPref = gridPref;
 	}
 
 	public GridROI(GridPreferences gridPrefs) {
 		this.gridPref = gridPrefs;
 	}
 
 	@Override
 	public GridROI copy() {
 		GridROI croi = new GridROI(getPointX(), getPointY(), getLength(0), getLength(1), getAngle(), xSpacing,
 				ySpacing, gridLinesOn, midPointOn, gridPref);
 		croi.setPlot(plot);
 		return croi;
 	}
 
 	/**
 	 * @return Returns the spacing of X and Y grid
 	 */
 	public double[] getSpacing() {
 		return new double[] { xSpacing, ySpacing };
 	}
 
 	/**
 	 * @return integer lengths
 	 */
 	public int[] getIntSpacing() {
 		return new int[] { (int) xSpacing, (int) ySpacing };
 	}
 
 	/**
 	 * Work out where all the grid points (middle of grid box) are
 	 * 
 	 * @return grid points
 	 */
 	public double[][] getGridPoints() {
 		final double[] len = getLengths();
 		final double[] spt = getPointRef();
 		int xGrids = (int) ((len[0] / xSpacing) + 0.5);
 		int yGrids = (int) ((len[1] / ySpacing) + 0.5);
 		double[] xLocs = new double[xGrids];
 		double[] yLocs = new double[yGrids];
 
 		if (xGrids != 0 && yGrids != 0) {
 			xLocs[0] = spt[0] + xSpacing / 2.0;
 			yLocs[0] = spt[1] + ySpacing / 2.0;
 
 			for (int i = 1; i < xGrids; i++) {
 				xLocs[i] = xLocs[0] + xSpacing * i;
 			}
 			for (int i = 1; i < yGrids; i++) {
 				yLocs[i] = yLocs[0] + ySpacing * i;
 			}
 		}
 		double[][] gp = new double[2][];
 		gp[0] = xLocs;
 		gp[1] = yLocs;
 		return gp;
 	}
 
 	public double[][] getGridLines() {
 		double[][] gp = getGridPoints();
 		int xGrids = gp[0].length;
 		int yGrids = gp[1].length;
 		double[][] gl = new double[2][];
 
 		if (getSpacing()[0] * xGrids >= getLengths()[0]) {
 			xGrids--;
 			if (xGrids < 0) {
 				xGrids = 0;
 			}
 			gl[0] = new double[xGrids];
 		} else {
 			gl[0] = gp[0];
 		}
 
 		if (getSpacing()[1] * yGrids >= getLengths()[1]) {
 			yGrids--;
 			if (yGrids < 0) {
 				yGrids = 0;
 			}
 			gl[1] = new double[yGrids];
 		} else {
 			gl[1] = gp[1];
 		}
 
 		for (int i = 0; i < xGrids; i++) {
 			gl[0][i] = gp[0][i] + xSpacing / 2.0;
 		}
 		for (int i = 0; i < yGrids; i++) {
 			gl[1][i] = gp[1][i] + ySpacing / 2.0;
 		}
 		return gl;
 	}
 
 	/**
 	 * @return Returns the value for x-axis resolution
 	 */
 	public double getxSpacing() {
 		return xSpacing;
 	}
 
 	/**
 	 * @return Returns the value for y-axis resolution
 	 */
 	public double getySpacing() {
 		return ySpacing;
 	}
 
 	/**
 	 * @param xSpacing
 	 *            Sets grid resolution for x-axis
 	 * @param ySpacing
 	 *            Sets grid resolution for y-axis
 	 */
 	public void setxySpacing(double xSpacing, double ySpacing) {
 		this.xSpacing = xSpacing;
 		this.ySpacing = ySpacing;
 	}
 
 	/**
 	 * @return Returns true if midpoints are enabled, false otherwise
 	 */
 	public boolean isMidPointOn() {
 		return midPointOn;
 	}
 
 	/**
 	 * @param midPointOn
 	 *            Turns on display of midpoints
 	 */
 	public void setMidPointOn(boolean midPointOn) {
 		this.midPointOn = midPointOn;
 	}
 
 	/**
 	 * @return Returns true if gridpoints are enabled, false otherwise
 	 */
 	public boolean isGridLineOn() {
 		return gridLinesOn;
 	}
 
 	/**
 	 * @param gridLinesOn
 	 *            Turns on display of gridpoints
 	 */
 	public void setGridLineOn(boolean gridLinesOn) {
 		this.gridLinesOn = gridLinesOn;
 	}
 
 	public int getNumberOfPoints() {
 		int[] dimensions = getDimensions();
 		return dimensions[0] * dimensions[1];
 	}
 
 	public int[] getDimensions() {
 		double[][] gp = getGridPoints();
 		return new int[] { gp[0].length, gp[1].length };
 	}
 
 	/**
 	 * returns an array of (x, y) tuples that represent the physical amount the motors 
 	 * have to be driven relative to the current position (when taking the image) to drive 
 	 * every point on the grid into the beam.
 	 * 
 	 * @return the tuples
 	 */
 	public double[][] getPhysicalGridPoints() {
 		double[][] gp = getGridPoints();
 		double[][] xyTuples = new double[getNumberOfPoints()][2];
 		
 		double cang = Math.cos(getAngle());
 		double sang = Math.sin(getAngle());
 		
 		final double[] spt = getPointRef();
 		double[] beam = getBeamCentre();
 		double[] ppmm = new double[] { gridPref.getResolutionX(), gridPref.getResolutionY() };
 		
 		int i = 0;
 		for (double x : gp[0]) {
 			for (double y : gp[1]) {
 				xyTuples[i][0] = spt[0] + (x - spt[0]) * cang - (y - spt[1]) * sang;
 				xyTuples[i][0] -= beam[0];
 				xyTuples[i][0] /= ppmm[0];
 				xyTuples[i][1] = spt[1] + (y - spt[1]) * cang + (x - spt[0]) * sang;
 				xyTuples[i][1] -= beam[1];
 				xyTuples[i][1] /= ppmm[1];
 				i++;
 			}
 		}
 		return xyTuples;
 	}
 	
 	/**
 	 * beam centre on camera image
 	 * @return x and y pixels for beam centre
 	 */
 	public double[] getBeamCentre() {
 		return new double[] { gridPref.getBeamlinePosX(), gridPref.getBeamlinePosY() };
 	}
 	
 	/**
 	 * pixel size in x and y in m 
 	 * @return pixel size in x and y in m
 	 */
 	public double[] getPixelSizeM() {
 		return new double[] { 0.001/gridPref.getResolutionX(), 0.001/gridPref.getResolutionY() };
 	}
 
 	@Override
 	public String toString() {
 		String superString = super.toString();
 		return String.format("%s Spacing (%g, %g)", superString, xSpacing, ySpacing);
 	}
 
 	public GridPreferences getGridPreferences() {
 		return gridPref;
 	}
 
 	public void setGridPreferences(GridPreferences gridPref) {
 		this.gridPref = gridPref;
 	}
 }
