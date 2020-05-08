 package de.echox.hacklace.pix0lat0r.gui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 
 import de.echox.hacklace.pix0lat0r.data.Matrix;
 import de.echox.hacklace.pix0lat0r.data.MatrixImpl;
 
 public class Drawer extends Canvas {
 
 	 int width=5;
      int height=7;
      int bubblesize = 20;
      int spacing = 5;
      
      private Matrix matrix;
      
 	public Drawer(Composite parent, int style) {
 		super(parent, style);
 		
 		this.matrix = new MatrixImpl(width, height);
 		
 	     addPaintListener(new PaintListener() {
 	         public void paintControl(PaintEvent e) {
 	            Drawer.this.paintControl(e);
 	         }
 	     });
 	     
 		    this.addListener(SWT.MouseUp, new Listener() {
 		        @Override
 		        public void handleEvent(Event e) {
 		        	togglePixel(calcPos(e.x), calcPos(e.y));
 		        	Drawer.this.redraw();
 		        }           
 		    });
 	}
 	
 	int calcPos(int pos) {
 		return pos / (bubblesize + spacing);
 	}
 	
 	void paintControl(PaintEvent e) {
 	     int x=0;
 	     int y=0;
 	    
 			GC gc = e.gc;
 			
 			gc.setAntialias(SWT.DEFAULT);
 		    
 			Color colorActive = new Color(e.display, 5, 255, 5);
 			Color colorInActive = new Color(e.display, 10, 80, 10);
 		     
 		     for (int row = 0; row < height; row++) {
 		     for (int col = 0; col < width; col++) {
 		    	 
 		    	 if(getPixel(col, row)) {
 		    		 gc.setBackground(colorActive);
 		    	 } else {
 		    		 gc.setBackground(colorInActive);
 		    	 }
 		    	 
 			     gc.fillOval(x, y, bubblesize, bubblesize);
 			     x += bubblesize + spacing;
 			}
 		     	x=0;
 		     	y += bubblesize + spacing;
 			}
 	}
 
 	 public Point computeSize(int wHint, int hHint, boolean changed) {
 		 int width = (bubblesize + spacing) * this.width;
 		 int height = (bubblesize + spacing) * this.height;
 	     return new Point(width + 2, height + 2);    
 	  }
 	 
 	 public void setPixel(int x, int y) {
 		 matrix.setPixel(x, y);
 	 }
 	 public void unsetPixel(int x, int y) {
 		 matrix.unsetPixel(x, y);
 	 }
 	 
 	 public boolean getPixel(int x, int y) {
 		 return this.matrix.getPixel(x, y);
 	 }
 	 
 	 public void togglePixel(int x, int y) {
 		 this.matrix.togglePixel(x, y);
 	 }
 	 
	 public Matrix getMatrixData() {
 		 return this.matrix;
 	 }
 	 
	 public void setMatrixData(Matrix matrix) {
 		 this.matrix = matrix;
 	 }
 }
