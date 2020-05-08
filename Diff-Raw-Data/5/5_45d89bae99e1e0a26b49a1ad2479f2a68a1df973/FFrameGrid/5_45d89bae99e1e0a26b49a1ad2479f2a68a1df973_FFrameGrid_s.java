 package com.iver.cit.gvsig.project.documents.layout.fframes;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.font.FontRenderContext;
 import java.awt.font.TextLayout;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 
 import javax.print.attribute.PrintRequestAttributeSet;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
 import com.iver.cit.gvsig.project.documents.layout.FLayoutUtilities;
 import com.iver.cit.gvsig.project.documents.layout.fframes.gui.dialogs.FFrameGridDialog;
 import com.iver.cit.gvsig.project.documents.layout.fframes.gui.dialogs.IFFrameDialog;
 import com.iver.cit.gvsig.project.documents.layout.gui.Layout;
 import com.iver.utiles.StringUtilities;
 import com.iver.utiles.XMLEntity;
 
 
 
 /**
  * FFrame para introducir una cuadrcula sobre una vista en el Layout.
  *
  * @author Vicente Caballero Navarro
  */
 public class FFrameGrid extends FFrame implements IFFrameViewDependence{
 
 	private FFrameView fframeview;
 	private double interval;
 	private double lineWidth;
 	private Color textColor=Color.black;
 	private Color lineColor=Color.black;
 	private boolean isLine;
 	private int sizeFont=8;
 	private int dependenceIndex;
 
	public void draw(Graphics2D g, AffineTransform at, Rectangle2D rv, BufferedImage imgBase) throws ReadDriverException {
 		FontRenderContext frc = g.getFontRenderContext();
 		double myScale = at.getScaleX() * 0.0234; //FLayoutUtilities.fromSheetDistance(folio.getAncho(),at)/rv.getWidth();
         int scaledFontSize = (int) (myScale * sizeFont);
 		Font font=new Font(g.getFont().getFamily(),g.getFont().getStyle(),scaledFontSize);
 		Rectangle2D.Double r = getBoundingBox(at);
 		Rectangle2D rView=fframeview.getBoundingBox(at);
 		g.rotate(Math.toRadians(getRotation()), r.x + (r.width / 2),
             r.y + (r.height / 2));
         AffineTransform atView=fframeview.getATMap();
 
 
         Rectangle2D extent=fframeview.getMapContext().getViewPort().getAdjustedExtent();
         double extentX=extent.getMinX();
         double extentY=extent.getMinY();
 
         double restX=(extentX/interval) % 1;
         double distX=restX*interval;
         //double distPixelsX=FLayoutUtilities.fromSheetDistance(distX,atView);
         double restY=(extentY/interval) % 1;
         double distY=restY*interval;
         //double distPixelsY=FLayoutUtilities.fromSheetDistance(distY,atView);
 
         double x=extentX-distX;
         //double pixelsX = rView.getMinX()-distPixelsX;
         double y=extentY-distY;
         //double pixelsY = rView.getMinY()-distPixelsY;
 
         //fframeview.getMapContext().getViewPort().fromMapPoint(extentX,extentY);
         //double pixelsInterval=FLayoutUtilities.fromSheetDistance(interval,atView);
         g.setStroke(new BasicStroke((int)lineWidth));
         g.setColor(Color.black);
 
 
         // Dibuja los mrgenes
         double valueIntervalX=((extentX/interval)-restX) * interval-interval;
         while(x<extent.getMaxX()){
         	if (x>extentX) {
         		Point2D p2=fframeview.getMapContext().getViewPort().fromMapPoint(x,extentY);
         		Point2D p1=fframeview.getMapContext().getViewPort().fromMapPoint(x,extent.getMaxY());
         		g.setColor(lineColor);
         		g.drawLine((int)p1.getX(),(int)p1.getY()-5,(int)p1.getX(),(int)p1.getY());
         		g.drawLine((int)p2.getX(),(int)p2.getY(),(int)p2.getX(),(int)p2.getY()+5);
         		TextLayout textaux = new TextLayout(String.valueOf(valueIntervalX),
                         font, frc);
 
         		double w=textaux.getBounds().getWidth();
         		double h=textaux.getBounds().getHeight();
         		g.setColor(textColor);
         		textaux.draw(g,(int)(p1.getX()-w/2),(int)(p1.getY()-h)-5);
         		textaux.draw(g,(int)(p2.getX()-w/2),(int)(p2.getY()+h*2)+5);
         	}
         	valueIntervalX=valueIntervalX+interval;
         	x=x+interval;
         }
         double valueIntervalY=((extentY/interval)-restY) * interval-interval;
         while(y<extent.getMaxY()){
         	if (y>extentY) {
         		Point2D p1=fframeview.getMapContext().getViewPort().fromMapPoint(extentX,y);
         		Point2D p2=fframeview.getMapContext().getViewPort().fromMapPoint(extent.getMaxX(),y);
         		g.setColor(lineColor);
         		g.drawLine((int)p1.getX()-5,(int)p1.getY(),(int)p1.getX(),(int)p1.getY());
         		g.drawLine((int)p2.getX(),(int)p2.getY(),(int)p2.getX()+5,(int)p2.getY());
         		TextLayout textaux = new TextLayout(String.valueOf(valueIntervalY),
                         font, frc);
         		double w=textaux.getBounds().getWidth();
         		double h=textaux.getBounds().getHeight();
         		g.setColor(textColor);
         		textaux.draw(g,(int)(p1.getX()-w-10),(int)(p1.getY()+h/2));
         		textaux.draw(g,(int)p2.getX()+10,(int)(p2.getY()+h/2));
         	}
         	valueIntervalY=valueIntervalY+interval;
         	y=y+interval;
         }
         g.setColor(lineColor);
 
         g.draw(rView);
 
         x = extentX-distX;
         y = extentY-distY;
 
         if (isLine) { // Dibuja las lneas.
 //        	while(x<extent.getMaxX()){
 //	        	if (x>extentX) {
 //	        		g.drawLine((int)pixelsX,(int)rView.getMinY(),(int)pixelsX,(int)rView.getMaxY());
 //	        	}
 //	        	pixelsX=pixelsX+pixelsInterval;
 //	        }
 //	        while(pixelsY<rView.getMaxY()){
 //	        	if (pixelsY>rView.getMinY())
 //	        		g.drawLine((int)rView.getMinX(),(int)pixelsY,(int)rView.getMaxX(),(int)pixelsY);
 //	        	pixelsY=pixelsY+pixelsInterval;
 //	        }
         	 while(x<extent.getMaxX()){
         		 Point2D antPoint=fframeview.getMapContext().getViewPort().fromMapPoint(x,extentY);
  	        	if (x>extentX) {
  	                while(y<=extent.getMaxY()){
  	    	        	if (y>=extentY) {
  	    	        		Point2D p=fframeview.getMapContext().getViewPort().fromMapPoint(x,y);
  	    	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
  	    	        		antPoint=(Point2D)p.clone();
  	    	        	}
 
  	    	        	y=y+interval;
  	    	    	}
  	                Point2D p=fframeview.getMapContext().getViewPort().fromMapPoint(x,extent.getMaxY());
 	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
 	        		antPoint=(Point2D)p.clone();
  	    	        y=extentY-distY;
 
  	        	}
 
 
  	        	x=x+interval;
  	        }
         	 while(y<=extent.getMaxY()){
         		 Point2D antPoint=fframeview.getMapContext().getViewPort().fromMapPoint(extentX,y);
  	        	if (y>extentY) {
  	                while(x<=extent.getMaxX()){
  	    	        	if (x>=extentX) {
  	    	        		Point2D p=fframeview.getMapContext().getViewPort().fromMapPoint(x,y);
  	    	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
  	    	        		antPoint=p;
  	    	        	}
  	    	        	x=x+interval;
  	    	    	}
  	                Point2D p=fframeview.getMapContext().getViewPort().fromMapPoint(extent.getMaxX(),y);
 	        		g.drawLine((int)antPoint.getX(),(int)antPoint.getY(),(int)p.getX(),(int)p.getY());
 	        		antPoint=(Point2D)p.clone();
  	    	        x=extentX-distX;
  	        	}
  	        	y=y+interval;
  	        }
         } else { //Dibuja los puntos
 	        while(x<=extent.getMaxX()){
 	        	if (x>extentX) {
 	                while(y<=extent.getMaxY()){
 	    	        	if (y>=extentY) {
 	    	        		Point2D p=fframeview.getMapContext().getViewPort().fromMapPoint(x,y);
 	    	        		g.drawLine((int)p.getX()-10,(int)p.getY(),(int)p.getX()+10,(int)p.getY());
 	    	        		g.drawLine((int)p.getX(),(int)p.getY()-10,(int)p.getX(),(int)p.getY()+10);
 	    	        	}
 	    	        	y=y+interval;
 	    	    	}
 	    	        y=extentY-distY;
 	        	}
 	        	x=x+interval;
 	        }
 
         }
 
         g.rotate(Math.toRadians(-getRotation()), r.x + (r.width / 2),
                 r.y + (r.height / 2));
 	}
 
	public void print(Graphics2D g, AffineTransform at, FShape shape,PrintRequestAttributeSet properties) throws ReadDriverException {
 		draw(g,at,null,null);
 	}
 
 	public XMLEntity getXMLEntity() throws SaveException {
 		 XMLEntity xml = super.getXMLEntity();
 		 try {
            xml.putProperty("interval", interval);
            xml.putProperty("isLine", isLine);
            xml.putProperty("lineColor", StringUtilities.color2String(lineColor));
            xml.putProperty("lineWidth", lineWidth);
            xml.putProperty("sizeFont", sizeFont);
            xml.putProperty("textColor", StringUtilities.color2String(textColor));
 
            if (fframeview != null) {
                Layout layout = fframeview.getLayout();
                IFFrame[] fframes = layout.getLayoutContext().getAllFFrames();
 
                for (int i = 0; i < fframes.length; i++) {
                    if (fframeview.equals(fframes[i])) {
                        xml.putProperty("index", i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new SaveException(e, this.getClass().getName());
        }
 		 return xml;
 	}
 	public void setXMLEntity(XMLEntity xml) {
 		if (xml.getIntProperty("m_Selected") != 0) {
 			this.setSelected(true);
 		} else {
 			this.setSelected(false);
 		}
 
 		this.interval = xml.getDoubleProperty("interval");
 		this.isLine = xml.getBooleanProperty("isLine");
 		this.lineColor = StringUtilities.string2Color(xml
 				.getStringProperty("lineColor"));
 		this.lineWidth = xml.getDoubleProperty("lineWidth");
 		this.sizeFont = xml.getIntProperty("sizeFont");
 		this.textColor = StringUtilities.string2Color(xml
 				.getStringProperty("textColor"));
 
 		setRotation(xml.getDoubleProperty("m_rotation"));
 
 		if (xml.contains("index")) {
 			dependenceIndex = xml.getIntProperty("index");
 		}
 	}
 
 	public void setXMLEntity03(XMLEntity xml, Layout l) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public String getNameFFrame() {
 		return PluginServices.getText(this, "cuadricula")+ num;
 	}
 
 	public void cloneActions(IFFrame frame) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setFFrameDependence(IFFrame f) {
 		fframeview=(FFrameView)f;
 		fframeview.refresh();
 		setBoundBox();
 	}
 
 	public IFFrame[] getFFrameDependence() {
 		return new IFFrame[] {fframeview};
 	}
 
 	 /**
      * Actualiza las dependencias que tenga este FFrame con el resto.
      *
      * @param fframes Resto de FFrames.
      */
     public void initDependence(IFFrame[] fframes) {
         if ((dependenceIndex != -1) &&
                 fframes[dependenceIndex] instanceof FFrameView) {
             fframeview = (FFrameView) fframes[dependenceIndex];
         }
     }
 
 	public void setInterval(double d) {
 		interval=d;
 	}
 	public double getInterval() {
 		return interval;
 	}
 
 	public void setLineWidth(double d) {
 		lineWidth=d;
 	}
 
 	public void setTextColor(Color textcolor) {
 		textColor=textcolor;
 	}
 
 	public void setLineColor(Color linecolor) {
 		lineColor=linecolor;
 	}
 
 	public void setIsLine(boolean b) {
 		isLine=b;
 	}
 	public boolean isLine() {
 		return isLine;
 	}
 
 	public IFFrameDialog getPropertyDialog() {
 		return new FFrameGridDialog(getLayout(),this);
 	}
 
 	public double getLineWidth() {
 		return lineWidth;
 	}
 
 	public Color getFontColor() {
 		return textColor;
 	}
 
 	public Color getLineColor() {
 		return lineColor;
 	}
 
 	public int getSizeFont() {
 		return sizeFont;
 	}
 
 	public void setSizeFont(int sizeFont) {
 		this.sizeFont = sizeFont;
 	}
 
 	public void setBoundBox() {
 		Rectangle2D r=fframeview.getBoundBox();
 		Rectangle2D extent=fframeview.getMapContext().getViewPort().getAdjustedExtent();
 	    double extentX=extent.getMaxX();
 	    double extentY=extent.getMaxY();
 	    int lengthX=String.valueOf((long)extentX).length();
 	    double myScale = getLayout().getLayoutControl().getAT().getScaleX() * 0.0234; //FLayoutUtilities.fromSheetDistance(folio.getAncho(),at)/rv.getWidth();
         int scaledFontSize = (int) (myScale * sizeFont);
 	    int pixelsX=(int)(lengthX*scaledFontSize*0.7);
 	    int lengthY=String.valueOf((long)extentY).length();
 	    int pixelsY=(lengthY*scaledFontSize);
 	    double dX=FLayoutUtilities.toSheetDistance(pixelsX,getLayout().getLayoutControl().getAT());
 	    double dY=FLayoutUtilities.toSheetDistance(pixelsY,getLayout().getLayoutControl().getAT());
 	    Rectangle2D rBound=new Rectangle2D.Double(r.getMinX()-dY,r.getMinY()-dX,r.getWidth()+dY*2,r.getHeight()+dX*2);
 	    super.setBoundBox(rBound);
 	}
 	 public Rectangle2D getMovieRect(int difx, int dify) {
 		 return this.getBoundingBox(null);
 	 }
 
 	public void refreshDependence(IFFrame fant, IFFrame fnew) {
 		if (fframeview.equals(fant)) {
 			fframeview=(FFrameView)fnew;
 			fframeview.refresh();
 			setBoundBox();
 		}
 
 	}
 }
