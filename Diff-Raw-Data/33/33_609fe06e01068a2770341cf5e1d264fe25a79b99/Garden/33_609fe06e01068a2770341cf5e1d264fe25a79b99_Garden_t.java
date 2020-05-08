 package edu.berkeley.cs160.smartnature;
 
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.graphics.RectF;
 
 import java.util.ArrayList;
 
 import com.google.gson.annotations.Expose;
 
 public class Garden {
 
 	@Expose private String name;
 	@Expose private String city;
 	@Expose private String state;
 	private int previewId;
 	private ArrayList<Plot> plots = new ArrayList<Plot>();
	private RectF bounds = new RectF(0, 0, 800, 480);
 	
 	private static Rect padding = new Rect(30, 30, 30, 30);
 	private static Rect paddingLand = new Rect(20, 30, 20, 10);
 	private static Rect paddingPort = new Rect(30, 20, 10, 20);
 	
 	Garden() { this(R.drawable.preview, ""); }
 		
 	Garden(String gardenName) { this(R.drawable.preview, gardenName); }
 	
 	Garden(int resId, String gardenName) {
		bounds = new RectF(0, 0, 800, 480);
 		name = gardenName;
 		previewId = resId;
 	}
 	
 	public void addPlot(Plot plot) {
 		// do not immediately refresh bounds to preserve viewport
 		/*
 		RectF pBounds = plot.getRotateBounds();
 		if (plots.isEmpty()) {
 			bounds = new RectF(pBounds);
 		}
 		else {
 			bounds.left = Math.min(bounds.left, pBounds.left);
 			bounds.top = Math.min(bounds.top, pBounds.top);
 			bounds.right = Math.max(bounds.right, pBounds.right);
 			bounds.bottom = Math.max(bounds.bottom, pBounds.bottom);
 		}
 		*/
		//if (plots.isEmpty()) bounds = new RectF(plot.getRotateBounds());
 		
 		plot.setID(plots.size());
 		plots.add(plot);
 	}
 	
 	/** add a polygonal plot */
 	public void addPlot(String plotName, Rect plotBounds, float rotation, float[] points) {
 		addPlot(new Plot(plotName, plotBounds, rotation, points)); 
 	}
 	
 	/** add a rectangular or elliptical plot */
 	public void addPlot(String plotName, Rect plotBounds, float rotation, int shapeType) {
 		addPlot(new Plot(plotName, plotBounds, rotation, shapeType)); 
 	}
 	
 	/** Used for full screen mode */
 	public RectF getBounds() {
 		RectF padded = new RectF(bounds);
 		padded.left -= padding.left;
 		padded.top -= padding.top;
 		padded.right += padding.right;
 		padded.bottom += padding.bottom;
 		return padded;
 	}
 	
 	/** Used for portrait/landscape mode */
 	public RectF getBounds(boolean portraitMode) {
 		Rect offset = portraitMode ? paddingPort : paddingLand;
 		RectF padded = new RectF(bounds);
 		padded.left -= offset.left;
 		padded.top -= offset.top;
 		padded.right += offset.right;
 		padded.bottom += offset.bottom;
 		return padded;
 	}
 	
 	public void refreshBounds() {
 		refreshBounds(plots.size());
 	}
 	
 	public void refreshBounds(int count) {
 		if (plots.isEmpty())
 			bounds = new RectF();
 		else {
 			bounds = plots.get(0).getRotateBounds();
 			for (int i = 0; i < count; i++) {
 				Plot p = plots.get(i);
 				RectF pBounds = p.getRotateBounds();
 				bounds.left = Math.min(bounds.left, pBounds.left);
 				bounds.top = Math.min(bounds.top, pBounds.top);
 				bounds.right = Math.max(bounds.right, pBounds.right);
 				bounds.bottom = Math.max(bounds.bottom, pBounds.bottom);
 			}
 		}
 	}
 	
 	/** finds a plot which contains (x, y) after being transformed by the matrix */ 
 	public Plot plotAt(float x, float y, Matrix matrix) {
 		Matrix inverse = new Matrix();
 		matrix.invert(inverse);
 		for (Plot p : plots) {
 			float[] point = { x, y };
 			Matrix tmp = new Matrix(inverse);
 			tmp.postRotate(-p.getAngle(), p.getBounds().centerX(), p.getBounds().centerY());
 			tmp.mapPoints(point);
 			if (p.contains(point[0], point[1]))
 				return p;
 		}
 		return null;
 	}
 	
 	public String getName() { return name; }
 	
 	public void setName(String name) { this.name = name; }
 	
 	public String getCity() { return city; }
 	
 	public void setCity(String city) { this.city = city; }
 	
 	public String getState() { return state; }
 	
 	public void setState(String state) { this.state = state; }
 	
 	public int getPreviewId() { return previewId; }
 	
 	public void setPreviewId(int previewId) { this.previewId = previewId; }
 	
 	public ArrayList<Plot> getPlots() { return plots; }
 	
 	public RectF getRawBounds() { return bounds; }
 	
 	public void setRawBounds(RectF bounds) { this.bounds = bounds; }
 	
 	/** Helpful ArrayList-related methods */
 	
 	public Plot getPlot(int index) { return plots.get(index); }
 	
 	public int indexOf(Plot plot) { return plots.indexOf(plot); }
 	
 	public boolean isEmpty() { return plots.isEmpty(); }
 	
 	public void remove(Plot plot) { plots.remove(plot); }
 	
 	public int size() { return plots.size(); }
 	
 }
