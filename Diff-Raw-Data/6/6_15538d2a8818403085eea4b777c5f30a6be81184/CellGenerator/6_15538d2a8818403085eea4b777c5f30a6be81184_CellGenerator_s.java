 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim;
 
import heartsim.cam.Nishiyama;
 import java.awt.Point;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import org.apache.batik.dom.svg.SVGOMPathElement;
 import org.apache.batik.gvt.GraphicsNode;
 import org.apache.batik.swing.JSVGCanvas;
 import org.apache.batik.util.SVGConstants;
 import org.w3c.dom.Element;
 
 /**
  *
  * @author Lee Boynton
  */
 public class CellGenerator implements Runnable
 {
     private List<CellGeneratorListener> listeners = Collections.synchronizedList(new ArrayList<CellGeneratorListener>());
     private JSVGCanvas canvas;
     private List<String> paths = new ArrayList<String>();
     private List<Element> elements = new ArrayList<Element>();
     private List<HeartTissue> tissues = new ArrayList<HeartTissue>();
     private String tissues2[][];
     private boolean cells[][];
     private boolean completed = false;
     private int progress = 0;
     private String tissueLoading = "None";
 
     public CellGenerator(JSVGCanvas canvas, String paths[])
     {
         this.canvas = canvas;
         this.paths.addAll(Arrays.asList(paths));
     }
 
     public int getProgress()
     {
         return progress;
     }
 
     public boolean isCompleted()
     {
         return completed;
     }
 
     public String getTissueLoading()
     {
         return tissueLoading;
     }
 
     public void addPath(String path)
     {
         paths.add(path);
     }
 
     public void removePath(String path)
     {
         if (paths.contains(path))
         {
             paths.remove(path);
         }
     }
 
     public String[] getPaths()
     {
         return (String[]) paths.toArray();
     }
 
     public void addGeneratorListener(CellGeneratorListener listener)
     {
         listeners.add(listener);
     }
 
     public void removeGeneratorListener(CellGeneratorListener listener)
     {
         if (listeners.contains(listener))
         {
             listeners.remove(listener);
         }
     }
 
     public void fireGenerationStarted()
     {
         for (CellGeneratorListener l : listeners)
         {
             l.cellGenerationStarted();
         }
     }
 
     public void fireGenerationCompleted()
     {
         for (CellGeneratorListener l : listeners)
         {
             l.cellGenerationCompleted();
         }
     }
 
     public boolean[][] getCells()
     {
         return cells;
     }
 
     public void run()
     {
         // notify listeners that cell generation has started
         fireGenerationStarted();
 
         //loadElements();
         loadTissues();
         //createDataArray();
         createDataArray2();
         completed = true;
 
         // notify listeners that cell generation has finished
         fireGenerationCompleted();
     }
 
     private void loadTissues()
     {
         for (String path : paths)
         {
             Element element = (Element) canvas.getSVGDocument().getElementById(path);
 
             if (element != null)
             {
                 HeartTissue tissue = new HeartTissue(path);
                 tissue.setElement(element);
                 tissues.add(tissue);
             }
         }
     }
 
     public List<HeartTissue> getTissues()
     {
         return tissues;
     }
 
     public String[][] getTissueNames()
     {
         return tissues2;
     }
 
     private void loadElements()
     {
         for (String path : paths)
         {
             Element element = (Element) canvas.getSVGDocument().getElementById(path);
 
             if (element != null)
             {
                 elements.add(element);
             }
         }
     }
 
     private void createDataArray2()
     {
         cells = new boolean[canvas.getPreferredSize().height][canvas.getPreferredSize().width];
         tissues2 = new String[canvas.getPreferredSize().height][canvas.getPreferredSize().width];
 
         for (HeartTissue tissue : tissues)
         {
             tissueLoading = tissue.getName();
 
             Application.getInstance().output("Generating cells for " + tissueLoading);
 

             GraphicsNode node = canvas.getUpdateManager().getBridgeContext().getGraphicsNode(tissue.getElement());
 
             if (node != null)
             {
                 AffineTransform elementsAt = node.getGlobalTransform();
                 Shape selectionHighlight = node.getOutline();
                 AffineTransform at = canvas.getRenderingTransform();
                 at.concatenate(elementsAt);
                 Shape s = at.createTransformedShape(selectionHighlight);
 
                 if (s == null)
                 {
                     break;
                 }
 
                 tissue.setShape(s);
 
                 for (int row = 0; row < canvas.getPreferredSize().height; row++)
                 {
                     for (int col = 0; col < canvas.getPreferredSize().width; col++)
                     {
                         if (s.contains(new Point(col, row)))
                         {
                             cells[row][col] = true;
                             tissues2[row][col] = tissueLoading;
                         }
                     }
 
                     // row completed
                     progress = (int) (((row + 1) / (double) canvas.getPreferredSize().height) * 100);
                 }
             }
         }
     }
 
     private void createDataArray()
     {
         cells = new boolean[canvas.getPreferredSize().height][canvas.getPreferredSize().width];
         tissues2 = new String[canvas.getPreferredSize().height][canvas.getPreferredSize().width];
 
         for (Element element : elements)
         {
             tissueLoading = ((SVGOMPathElement) element).getAttribute(SVGConstants.SVG_ID_ATTRIBUTE);
 
             Application.getInstance().output("Generating cells for " + tissueLoading);
 
             GraphicsNode node = canvas.getUpdateManager().getBridgeContext().getGraphicsNode(element);
 
             if (node != null)
             {
                 AffineTransform elementsAt = node.getGlobalTransform();
                 Shape selectionHighlight = node.getOutline();
                 AffineTransform at = canvas.getRenderingTransform();
                 at.concatenate(elementsAt);
                 Shape s = at.createTransformedShape(selectionHighlight);
 
                 if (s == null)
                 {
                     break;
                 }
 
                 for (int row = 0; row < canvas.getPreferredSize().height; row++)
                 {
                     for (int col = 0; col < canvas.getPreferredSize().width; col++)
                     {
                         if (s.contains(new Point(col, row)))
                         {
                             cells[row][col] = true;
                             tissues2[row][col] = tissueLoading;
                         }
                     }
 
                     // row completed
                     progress = (int) (((row + 1) / (double) canvas.getPreferredSize().height) * 100);
                 }
             }
         }
     }
 }
