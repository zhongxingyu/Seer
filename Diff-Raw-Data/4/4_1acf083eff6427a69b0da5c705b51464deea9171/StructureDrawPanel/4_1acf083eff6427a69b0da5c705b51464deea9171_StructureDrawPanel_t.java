 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package structurevis.ui;
 
 import com.kitfox.svg.SVGCache;
 import com.kitfox.svg.SVGDiagram;
 import com.kitfox.svg.SVGException;
 import com.kitfox.svg.SVGUniverse;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.*;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Arc2D;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.QuadCurve2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URI;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import net.hanjava.svg.SVG2EMF;
 import structurevis.structures.Structure;
 import structurevis.ui.MainApp.NucleotideCompositionType;
 
 /**
  *
  * @author Michael Golden
  */
 public class StructureDrawPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener {
 
     public static final int SHOW = 0;
     public static final int HIDE = 1;
     public int oneDimensionalData = SHOW;
     public boolean show2DData = true;
     public static final int NASP_SHOW = 0;
     public static final int NASP_HIDE = 1;
     public int naspType = NASP_HIDE;
     //public int maxDistance = -1;    
     public Graphics2D g = null;
     public boolean repaint = true;
     public int currentStructure = -1;
     int numStructures;
     MainApp mainapp = null;
     static ArrayList<String> sequences = new ArrayList<String>();
     static ArrayList<String> sequenceNames = new ArrayList<String>();
     static double[] weights;
     Font f1 = new Font("Arial", Font.PLAIN, 100);
     Font f2 = new Font("Arial", Font.PLAIN, 12);
     ArrayList<Interaction> covariationInteractions = new ArrayList<Interaction>();
 //    AnnotatedStructure structure = null;
     final static float dash1[] = {7.0f};
     final static BasicStroke dashedStroke = new BasicStroke(2.0f,
             BasicStroke.CAP_BUTT,
             BasicStroke.JOIN_MITER,
             7.0f, dash1, 0.0f);
     final static BasicStroke normalStroke = new BasicStroke(6f);
     //File naspStructuresFile = new File("C:/project/hepacivirus/10seq_aligned_d0.fasta.out");
     //File naspAlignmentFile = new File("C:/project/hepacivirus/10seq_aligned_d0.fasta");
     boolean saveStructures = false;
     Structure structure = null;
     DistanceMatrix structureDistanceMatrix = null;
     Point2D.Double[] nucleotidePositions;
     int nucleotideDiameter = 40;
     double xoffset = 150;
     double zoomScale = 1;
     ArrayList<Point2D.Double> np = null;
     double minx = Double.MAX_VALUE;
     double miny = Double.MAX_VALUE;
     double maxx = Double.MIN_VALUE;
     double maxy = Double.MIN_VALUE;
     JPopupMenu popupMenu = new JPopupMenu();
     JMenu zoomWindowMenu = new JMenu("Zoom level");
     private static final int[] zoomLevels = {25, 50, 75, 100, 125, 150, 175, 200, 250, 300, 400};
     ButtonGroup slidingWindowGroup = new ButtonGroup();
     JRadioButtonMenuItem[] zoomMenuItems = new JRadioButtonMenuItem[zoomLevels.length];
     int currentZoomIndex = 3;
     JMenu dnaRnaMenu = new JMenu("Nucleic acid");
     JRadioButtonMenuItem dnaMenuItem = new JRadioButtonMenuItem("DNA");
     JRadioButtonMenuItem rnaMenuItem = new JRadioButtonMenuItem("RNA");
     ButtonGroup dnaRnaGroup = new ButtonGroup();
     JMenuItem saveAsPNGItem = new JMenuItem("Save as PNG (at current zoom level)");
     JMenuItem saveAsSVGItem = new JMenuItem("Save as SVG");
     JMenuItem saveAsEMFItem = new JMenuItem("Save as EMF");
     JCheckBoxMenuItem showBondsItem = new JCheckBoxMenuItem("Show bonds");
     JMenu numberingMenu = new JMenu("Numbering");
     ButtonGroup numberingGroup = new ButtonGroup();
     JRadioButtonMenuItem numberNone = new JRadioButtonMenuItem("None");
     JRadioButtonMenuItem number1 = new JRadioButtonMenuItem("1");
     JRadioButtonMenuItem number5 = new JRadioButtonMenuItem("5");
     JRadioButtonMenuItem number10 = new JRadioButtonMenuItem("10");
     public static final int DRAW_SVG_GRAPHIC = 1;
     public static final int DRAW_NATIVE_GRAPHIC = 2;
     boolean useNativeDrawType = true;
     int currentDrawType = DRAW_SVG_GRAPHIC;
     DecimalFormat decimalFormat = new DecimalFormat("0.00");
 
     Color bondColor = Color.lightGray;
     int bondThickness = 4;
     
     //public boolean drawUsingSVG = false; // if true draw graphic using SVG, otherwise use native java graphics
     public StructureDrawPanel() {
         addMouseListener(this);
         addMouseMotionListener(this);
         addMouseWheelListener(this);
         addMouseListener(this);
 
         dnaMenuItem.addActionListener(this);
         dnaRnaMenu.add(dnaMenuItem);
         dnaRnaGroup.add(dnaMenuItem);
         rnaMenuItem.addActionListener(this);
         dnaRnaMenu.add(rnaMenuItem);
         dnaRnaGroup.add(rnaMenuItem);
         popupMenu.add(dnaRnaMenu);
         dnaMenuItem.setSelected(true);
 
         for (int i = 0; i < zoomLevels.length; i++) {
             zoomMenuItems[i] = new JRadioButtonMenuItem(zoomLevels[i] + "%");
             zoomMenuItems[i].addActionListener(this);
             slidingWindowGroup.add(zoomMenuItems[i]);
             zoomWindowMenu.add(zoomMenuItems[i]);
         }
         zoomMenuItems[currentZoomIndex].setSelected(true);
         popupMenu.add(zoomWindowMenu);
 
         saveAsPNGItem.addActionListener(this);
         popupMenu.add(saveAsPNGItem);
         saveAsSVGItem.addActionListener(this);
         popupMenu.add(saveAsSVGItem);
         saveAsEMFItem.addActionListener(this);
         popupMenu.add(saveAsEMFItem);
 
         showBondsItem.setSelected(true);
         showBondsItem.addActionListener(this);
         popupMenu.add(showBondsItem);
 
         numberNone.addActionListener(this);
         number1.addActionListener(this);
         number5.addActionListener(this);
         number5.setSelected(true);
         number10.addActionListener(this);
         numberingMenu.add(numberNone);
         numberingGroup.add(numberNone);
         numberingMenu.add(number1);
         numberingGroup.add(number1);
         numberingMenu.add(number5);
         numberingGroup.add(number5);
         numberingMenu.add(number10);
         numberingGroup.add(number10);
         popupMenu.add(numberingMenu);
     }
 
     public void initialise(MainApp mainapp) {
         this.mainapp = mainapp;
         numStructures = 1;
         nucleotidePositions = null;
         nextStructure();
         // required! otherwise structure panel doesn't get added correctly to mainapp
         setPreferredSize(new Dimension(800, 600));
         revalidate();
     }
 
     public Point2D getPointAlongArc(double x, double y, double width, double height, double startAngle, double offsetAngle) {
         return new Arc2D.Double(x, y, width, height, startAngle, offsetAngle, Arc2D.OPEN).getEndPoint();
     }
 
     public Ellipse2D getCircleCenteredAt(double x, double y, double diameter) {
         return new Ellipse2D.Double(x - (diameter / 2), y - (diameter / 2), diameter, diameter);
     }
 
     public void previousStructure() {
         currentStructure = (currentStructure - 1);
         structure = mainapp.structureCollection.structures.get(currentStructure);
         if (currentStructure < 0) {
             currentStructure += numStructures;
         }
         openStructure(structure);
     }
 
     public void nextStructure() {
         currentStructure = (currentStructure + 1) % numStructures;
         if (mainapp.structureCollection != null && mainapp.structureCollection.structures != null) {
             structure = mainapp.structureCollection.structures.get(currentStructure);
         }
         openStructure(structure);
     }
 
     public void openStructure(Structure s) {
         structure = s;
         if (structure.length < 500) {
             structureDistanceMatrix = new DistanceMatrix(structure.pairedSites);
         } else {
             structureDistanceMatrix = null;
         }
 
 //        mainapp.genomeLegend1.setSelectedRegion(s.startPosition - 1, s.getEndPosition() - 1);
         if (mainapp != null) {
             if (mainapp.showDNA) {
                 dnaMenuItem.setSelected(true);
             } else {
                 rnaMenuItem.setSelected(true);
             }
         }
         computeAndDraw();
 
         if (saveStructures) {
             try {
                 Thread.sleep(200);
             } catch (InterruptedException ex) {
                 Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
             }
             mainapp.callNext();
         }
     }
 
     /*
      * public void gotoStructure(int i) { currentStructure = (i + 1) %
      * numStructures; structure =
      * mainapp.structureCollection.structures.get(currentStructure);
      * //complexStructure = ComplexStructure.getComplexStructure(structure,
      * mainapp.fs.naspStructuresFile, mainapp.fs.naspAlignmentFile); if (mainapp
      * != null) { mainapp.setInfoLabel("id = " + structure.name + ", len = " +
      * structure.length); } computeAndDraw(); }
      */
     public void computeAndDraw() {
         computeStructureToBeDrawn(structure);
         repaint = true;
         repaint();
     }
 
     public void redraw() {
         repaint = true;
         repaint();
     }
     
     
     double horizontalScale = 2.6;
     double verticalScale = 2.6;
     public void computeStructureToBeDrawn(Structure structure) {
         if (structure == null) {
             return;
         }
 
         //np = mainapp.getStructureCoordinates(structure.getDotBracketString());
         np = NAView.naview_xy_coordinates(RNAFoldingTools.getPairedSitesFromDotBracketString(structure.getDotBracketString()));
 
         minx = Double.MAX_VALUE;
         miny = Double.MAX_VALUE;
         maxx = Double.MIN_VALUE;
         maxy = Double.MIN_VALUE;
 
         for (int i = 0; i < np.size(); i++) {
             Point2D.Double pos = np.get(i);
             minx = Math.min(minx, pos.x);
             miny = Math.min(miny, pos.y);
             maxx = Math.max(maxx, pos.x);
             maxy = Math.max(maxy, pos.y);
         }
         nucleotidePositions = new Point2D.Double[np.size()];
         for (int i = 0; i < nucleotidePositions.length; i++) {
             nucleotidePositions[i] = new Point2D.Double();
             nucleotidePositions[i].x = xoffset + (np.get(i).x - minx) * horizontalScale;
             nucleotidePositions[i].y = 50 + (np.get(i).y - miny) * verticalScale;
         }
 
     }
     //SVGIcon icon;
     URI uri;
     LinkedList extraElements = new LinkedList();
     SVGUniverse svgUniverse = SVGCache.getSVGUniverse();
     SVGDiagram diagram = null;
     SVGDiagram diagramWithLogo = null;
 
     public void createStructureSVG() {
         StringReader reader = new StringReader(drawStructure(true));
         SVGCache.getSVGUniverse().clear();
 
         uri = SVGCache.getSVGUniverse().loadSVG(reader, "myImage");
         diagram = SVGCache.getSVGUniverse().getDiagram(uri);
 
         /*
          * SVGElement b = diagram.getElement("logo_1_0"); if(b != null) {
          * System.out.println("here1"); if(b instanceof Text) {
          * System.out.println("here2"); Text text = (Text) b;
          * //text.appendText("A"); // text.appendText("C");
          * text.appendText("G"); //text.appendText("T"); try { text.build(); }
          * catch (SVGException ex) { ex.printStackTrace(); }
          * //tspan.setText("DDDDDDD"+tspan.getText()+"DDDDDDD"); //
          * diagram.setElement("base", tspan); try { diagram.updateTime(0); }
          * catch (SVGException ex) { ex.printStackTrace(); } } }
          */
     }
 
     public static String getHexString(Color color) {
         return Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
     }
 
     public String drawStructure(boolean drawSequenceLogo) {
 
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
 
         if (structure == null || nucleotidePositions == null) {
             return null;
         }
 
         int panelWidth = (int) ((maxx - minx) * horizontalScale + xoffset * 2);
         int panelHeight = (int) ((maxy - miny) * verticalScale + 100);
 
         // initialise svg
         pw.println("<?xml version=\"1.0\" standalone=\"no\"?>");
         pw.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
         pw.println("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + panelWidth + "\" height=\"" + panelHeight + "\" style=\"fill:none;stroke-width:16\">");
 
 
         if (mainapp.showBonds) {
             pw.println("<g>");
             for (int i = 0; i < nucleotidePositions.length; i++) {
                 int a = structure.pairedSites[0][i] - structure.pairedSites[0][0];
                 int b = structure.pairedSites[1][i] - structure.pairedSites[0][0];
                 if (structure.pairedSites[0][i] < structure.pairedSites[1][i]) {
                     /*
                      * Line2D bond = new Line2D.Double(nucleotidePositions[a],
                      * nucleotidePositions[b]); g.setStroke(new BasicStroke(2));
                      * g.setColor(Color.gray); g.draw(bond);
                      */
                     pw.println("    <line id=\"bond_" + i + "\" x1=\"" + nucleotidePositions[a].x + "\" y1=\"" + nucleotidePositions[a].y + "\"  x2=\"" + nucleotidePositions[b].x + "\" y2=\"" + nucleotidePositions[b].y + "\" style=\"stroke-width:\"" +bondThickness+"\";stroke:#" + getHexString(bondColor) + "\"/>");
                 }
             }
             pw.println("</g>");
         }
 
         // draw two-dimensional data
         pw.println("<g>");
         int length = structure.length;
         covariationInteractions.clear();
         if (show2DData && mainapp.data2D != null) {
             for (int i = structure.getStartPosition(); i <= structure.getEndPosition(); i++) {
                 for (int j = structure.getStartPosition(); j <= structure.getEndPosition(); j++) {
                     int k = i - structure.getStartPosition();
                     int l = j - structure.getStartPosition();
 
                     if (mainapp.maxDistance == -1 || (structureDistanceMatrix != null && structureDistanceMatrix.getDistance(k, l) <= mainapp.maxDistance) || (structureDistanceMatrix == null && mainapp.distanceMatrix.getDistance(i - 1, j - 1) <= mainapp.maxDistance)) {
                         Color c = null;
 
                         double value = mainapp.data2D.matrix.get(i - 1, j - 1);
                         if (value == mainapp.data2D.matrix.emptyValue) {
                             c = null;
                         } else if (((!mainapp.useLowerThreshold2D || value >= mainapp.thresholdMin2D) && (!mainapp.useUpperThreshold2D || value <= mainapp.thresholdMax2D))) {
                             //  Sy
                             if (mainapp.data2D != null) {
                                 //System.out.println(p);
                                 c = mainapp.data2D.colorGradientSecondary.getColor((float) mainapp.data2D.dataTransform.transform(value));
                             }
                         }
 
                         if (c != null && nucleotidePositions.length == structure.length) { // TODO strange error where the array length is different from the structure length, probably a race condition
 
                             double x1 = nucleotidePositions[k].getX();
                             double y1 = nucleotidePositions[k].getY();
                             double x2 = nucleotidePositions[l].getX();
                             double y2 = nucleotidePositions[l].getY();
                             /*
                              * double x1 = 0; double y1 = 0; double x2 = 0;
                              * double y2 = 0; try { x1 =
                              * nucleotidePositions[k].getX(); y1 =
                              * nucleotidePositions[k].getY(); x2 =
                              * nucleotidePositions[l].getX(); y2 =
                              * nucleotidePositions[l].getY(); } catch (Exception
                              * ex) { System.out.println(k + "\t" + l + "\t" +
                              * nucleotidePositions.length + "\t" +
                              * structure.getStartPosition() + "\t" +
                              * structure.getEndPosition() + "\t" +
                              * structure.getDotBracketString().length() + "\t" +
                              * structure.getDotBracketString());
                              * ex.printStackTrace(); }
                              */
 
                             Shape shape = null;
                             int structureMidpoint = structure.getStartPosition() + (structure.length / 2);
 
                             /*
                              * if (i >= structure.gapStartA && i <=
                              * structure.gapEndA && j > structureMidpoint && j <
                              * structure.gapStartB) { shape = new
                              * QuadCurve2D.Double(x1, y1, (x1 + x2) / 2, y2, x2,
                              * y2); } else if (i > structure.gapEndA && i <=
                              * structureMidpoint && j > structure.gapStartB && j
                              * < structure.gapEndB) { shape = new
                              * QuadCurve2D.Double(x1, y1, (x1 + x2) / 2, y1, x2,
                              * y2); } else if (i >= structure.gapEndA && i <=
                              * structure.gapStartB && j >= structure.gapEndA &&
                              * j <= structure.gapStartB) { shape = new
                              * Line2D.Double(nucleotidePositions[k],
                              * nucleotidePositions[l]); } else
                              */ if (i <= structureMidpoint && j <= structureMidpoint) { // both on left side
                                 double x1p = Math.max(x1 - Math.abs((y1 - y2) / 2), 0);
                                 shape = new QuadCurve2D.Double(x1, y1, x1p, (y1 + y2) / 2, x2, y2);
                             } else if (i > structureMidpoint && j > structureMidpoint) { // both on right side
                                 double x2p = Math.min(x2 + Math.abs((y1 - y2) / 2), panelWidth);
                                 shape = new QuadCurve2D.Double(x1, y1, x2p, (y1 + y2) / 2, x2, y2);
                             } else {
                                 shape = new Line2D.Double(nucleotidePositions[k], nucleotidePositions[l]);
                             }
                             covariationInteractions.add(new Interaction(shape, i, j));
                             pw.println("    <polyline id=\"2d_data_" + i + "_" + j + "\" points=\"" + x1 + " " + y1 + " " + x2 + " " + y2 + "\" style=\"stroke-width:5;stroke:#" + getHexString(c) + "\"/>");
                             //  pw.println("    <metadata><value>" + value + "</value></metadata>");
                         }
                     }
                 }
             }
         }
         pw.println("</g>");
 
 
         // draw the nucleotides
         pw.println("<g>");
         for (int i = 0; i < nucleotidePositions.length; i++) {
             int pos = (structure.startPosition + i - 1) % mainapp.structureCollection.genomeLength;
             Color nucleotideBackgroundColor = mainapp.missingDataColor;
             if (oneDimensionalData == SHOW && mainapp.data1D != null && mainapp.data1D.used[pos]) {
                 double p = mainapp.data1D.data[pos];
                 if (mainapp.data1D.used[pos] && ((!mainapp.useLowerThreshold1D || p >= mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D || p <= mainapp.thresholdMax1D))) {
                     nucleotideBackgroundColor = mainapp.data1D.colorGradientSecondary.getColor(mainapp.data1D.dataTransform.transform((float) p));
                 } else if (!((!mainapp.useLowerThreshold1D || p >= mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D || p <= mainapp.thresholdMax1D))) {
                     nucleotideBackgroundColor = mainapp.filteredDataColor;
                 }
             }
 
             // pw.println("<g transform=\"scale(1,0.5)\">");
             pw.println("    <circle id=\"nucleotide_" + (structure.startPosition + i) + "\" cx=\"" + nucleotidePositions[i].getX() + "\" cy=\"" + nucleotidePositions[i].getY() + "\" r=\"" + nucleotideDiameter / 2 + "\" style=\"stroke-width:2;stroke:black;fill:#" + getHexString(nucleotideBackgroundColor) + "\"/>");
             //pw.println("</g>");
 
             // draw the information
             if (drawSequenceLogo) {
                 Color bestColor = ColorTools.selectBestForegroundColor(nucleotideBackgroundColor, Color.white, Color.black);
                 if (mainapp.nucleotideComposition != null) {
                     if (mainapp.nucleotideCompositionType == NucleotideCompositionType.SHANNON) {
                         // fa = structure.shannonFrequencies[i];
                         double[] fa = Arrays.copyOf(mainapp.nucleotideComposition.mappedShannonComposition[(structure.startPosition + i - 1) % mainapp.structureCollection.genomeLength], 5);
                         for (int k = 0; k < 4; k++) {
                             fa[k] = fa[k] / 2;
                         }
                         pw.print(drawSequenceLogoSVG("logo_" + (structure.startPosition + i), nucleotidePositions[i].getX(), nucleotidePositions[i].getY() - (nucleotideDiameter / 2) + 4, nucleotideDiameter, nucleotideDiameter - 8, fa, bestColor));
                     } else if (mainapp.nucleotideCompositionType == NucleotideCompositionType.FREQUENCY) {
                         double[] fa = mainapp.nucleotideComposition.mappedFrequencyComposition[(structure.startPosition + i - 1) % mainapp.structureCollection.genomeLength];
                         pw.print(drawSequenceLogoSVG("logo_" + (structure.startPosition + i), nucleotidePositions[i].getX(), nucleotidePositions[i].getY() - (nucleotideDiameter / 2) + 4, nucleotideDiameter, nucleotideDiameter - 8, fa, bestColor));
                     }
                 }
             }
         }
         pw.println("</g>");
 
         // draw position lines
         pw.println("<g>");
         for (int i = 0; i < nucleotidePositions.length; i++) {
             int offsetx = 0;
             double side = 1;
             String textanchor = "start";
             if (i < length / 2) {
                 offsetx = -(int) (nucleotideDiameter / 2) - 5;
                 side = -1;
                 textanchor = "end";
             } else {
                 offsetx = (int) (nucleotideDiameter / 2) + 5;
             }
 
             if (nucleotidePositions[i] != null) {
                 int pos = (structure.getStartPosition() + i - 1) % mainapp.structureCollection.genomeLength + 1;
                 double fontSize = 11;
                 if(mainapp.numbering != 0 && pos % mainapp.numbering == 0)
                 {
                     pw.println("    <text id=\"nucleotide_position_" + pos + "\" x=\"" + (offsetx + nucleotidePositions[i].getX()) + "\" y=\"" + (nucleotidePositions[i].getY() + (fontSize / 2)) + "\" style=\"font-size:" + fontSize + "px;stroke:none;fill:black\" text-anchor=\"" + textanchor + "\" >");
                     pw.println("        <tspan>" + pos + "</tspan>");
                     pw.println("    </text>");
 
                     double x1 = nucleotidePositions[i].getX() + (side * nucleotideDiameter / 2) - 2.5;
                     double y1 = nucleotidePositions[i].getY();
                     double x2 = nucleotidePositions[i].getX() + (side * nucleotideDiameter / 2) + 2.5;
                     double y2 = nucleotidePositions[i].getY();
                     pw.println("    <polyline points=\"" + x1 + " " + y1 + " " + x2 + " " + y2 + "\" style=\"stroke-width:1;stroke:black\"/>");
 
                 }
             }
         }
         pw.println("</g>");
 
         pw.println("</svg>");
         pw.close();
         //System.out.println(sw.toString());
         return sw.toString();
     }
 
     public String drawSequenceLogoSVG(String id, double x, double y, double width, double height, double[] h, Color textColor) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
 
         double fontHeight = 100;
         double scale = (height / fontHeight);
         double base = y;
         for (int i = 0; i < h.length; i++) {
             //h[i] = 0.25;
             double fontHeightScale = (h[i]);
             //Font tallerFont = f1.deriveFont(AffineTransform.getScaleInstance(scale, fontHeightScale * scale));
 
             String a = "";
             switch (i) {
                 case 0:
                     a = "A";
                     break;
                 case 1:
                     a = "C";
                     break;
                 case 2:
                     a = "G";
                     break;
                 case 3:
                     if (mainapp.showDNA) {
                         a = "T";
                     } else {
                         a = "U";
                     }
                     break;
             }
             String b = a.length() > 0 ? a : "X";
             //g.setFont(tallerFont);
             //base += g.getFontMetrics(tallerFont).getStringBounds(b, g).getHeight() - g.getFontMetrics().getDescent();
             //g.drawString(a, (float) (x + (-g.getFontMetrics().getStringBounds(a, g).getWidth() / 2)), (float) (base));
             base += fontHeightScale * scale * fontHeight;
             if (!b.equals("X") && h[i] > 0) {
                 float xf = (float) x;
                 float yf = (float) (base);
                 xf /= scale;
                 yf /= fontHeightScale * scale;
                 pw.println("    <g transform=\"scale(" + scale + "," + fontHeightScale * scale + ")\">");
                 pw.print("        <text x=\"" + xf + "\" y=\"" + yf + "\" id=\"" + id + "_" + i + "\"  style=\"font-size:" + (int) fontHeight + "px;stroke:none;fill:#" + getHexString(textColor) + ";text-anchor:middle;\">");
                 pw.println("            <tspan id=\"base\">" + b + "</tspan>");
                 pw.println("</text>");
                 pw.println("    </g>");
             }
             //base += fontHeightScale*scale*100;
         }
 
         return sw.toString();
     }
     public BufferedImage bufferedImage = null;
 
     public void drawStructureNative(/*
              * Graphics graphics
              */) {
         if (structure == null || nucleotidePositions == null) {
             return;
         }
         int panelWidth = (int) ((maxx - minx) * horizontalScale + xoffset * 2);
         int panelHeight = (int) ((maxy - miny) * verticalScale + 100);
         Dimension d = new Dimension((int) (panelWidth * zoomScale), (int) (panelHeight * zoomScale));
 
         try {
             if (d.width > 0 && d.height > 0) {
                 if ((bufferedImage == null || d.width != bufferedImage.getWidth() || d.height != bufferedImage.getHeight())) {
                     bufferedImage = (BufferedImage) (this.createImage(d.width, d.height));
                 }
             }
         } catch (Exception ex) {
             //System.out.println(ex);
             JOptionPane.showMessageDialog(this.mainapp,
                     ex.getMessage(),
                     "Exception",
                     JOptionPane.ERROR_MESSAGE);
             return;
         } catch (Error e) {
             JOptionPane.showMessageDialog(this.mainapp,
                     e.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         if (bufferedImage == null) {
             return;
         }
 
         g = (Graphics2D) bufferedImage.getGraphics();
         //g = (Graphics2D) graphics;
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.scale(zoomScale, zoomScale);
 
         g.setColor(Color.white);
         g.fillRect(0, 0, panelWidth, panelHeight);
 
 
         // draw the base pair interactions
         if (mainapp.showBonds) {
             for (int i = 0; i < nucleotidePositions.length; i++) {
                 int a = structure.pairedSites[0][i] - structure.pairedSites[0][0];
                 int b = structure.pairedSites[1][i] - structure.pairedSites[0][0];
                 if (structure.pairedSites[0][i] < structure.pairedSites[1][i]) {
                     Line2D bond = new Line2D.Double(nucleotidePositions[a], nucleotidePositions[b]);
                     g.setStroke(new BasicStroke(bondThickness));
                     g.setColor(bondColor);
                     g.draw(bond);
                 }
             }
         }
 
 
         g.setColor(Color.black);
         int length = structure.length;
         covariationInteractions.clear();
         if (show2DData && mainapp.data2D != null) {
             for (int i = structure.getStartPosition(); i <= structure.getEndPosition(); i++) {
                 for (int j = structure.getStartPosition(); j <= structure.getEndPosition(); j++) {
                     int k = i - structure.getStartPosition();
                     int l = j - structure.getStartPosition();
                     if (mainapp.maxDistance == -1 || (structureDistanceMatrix != null && structureDistanceMatrix.getDistance(k, l) <= mainapp.maxDistance) || (structureDistanceMatrix == null && mainapp.distanceMatrix.getDistance(i - 1, j - 1) <= mainapp.maxDistance)) {
                         Color c = null;
                         double p = mainapp.data2D.matrix.get(i - 1, j - 1);
                         if (p == mainapp.data2D.matrix.emptyValue) {
                             c = null;
                         } else if (((!mainapp.useLowerThreshold2D || p >= mainapp.thresholdMin2D) && (!mainapp.useUpperThreshold2D || p <= mainapp.thresholdMax2D))) {
                             //  Sy
                             if (mainapp.data2D != null) {
                                 //System.out.println(p);
                                 c = mainapp.data2D.colorGradientSecondary.getColor((float) mainapp.data2D.dataTransform.transform(p));
                             }
                         }
 
                         if (c != null) {
                             double x1 = nucleotidePositions[k].getX();
                             double y1 = nucleotidePositions[k].getY();
                             double x2 = nucleotidePositions[l].getX();
                             double y2 = nucleotidePositions[l].getY();
 
                             Shape shape = null;
                             int structureMidpoint = structure.getStartPosition() + (structure.length / 2);
 
                             /*
                              * if (i >= structure.gapStartA && i <=
                              * structure.gapEndA && j > structureMidpoint && j <
                              * structure.gapStartB) { shape = new
                              * QuadCurve2D.Double(x1, y1, (x1 + x2) / 2, y2, x2,
                              * y2); } else if (i > structure.gapEndA && i <=
                              * structureMidpoint && j > structure.gapStartB && j
                              * < structure.gapEndB) { shape = new
                              * QuadCurve2D.Double(x1, y1, (x1 + x2) / 2, y1, x2,
                              * y2); } else if (i >= structure.gapEndA && i <=
                              * structure.gapStartB && j >= structure.gapEndA &&
                              * j <= structure.gapStartB) { shape = new
                              * Line2D.Double(nucleotidePositions[k],
                              * nucleotidePositions[l]); } else
                              */ if (i <= structureMidpoint && j <= structureMidpoint) { // both on left side
                                 double x1p = Math.max(x1 - Math.abs((y1 - y2) / 2), 0);
                                 shape = new QuadCurve2D.Double(x1, y1, x1p, (y1 + y2) / 2, x2, y2);
                             } else if (i > structureMidpoint && j > structureMidpoint) { // both on right side
                                 double x2p = Math.min(x2 + Math.abs((y1 - y2) / 2), panelWidth);
                                 shape = new QuadCurve2D.Double(x1, y1, x2p, (y1 + y2) / 2, x2, y2);
                             } else {
                                 shape = new Line2D.Double(nucleotidePositions[k], nucleotidePositions[l]);
                             }
                             covariationInteractions.add(new Interaction(shape, i, j));
 
                             g.setColor(c);
                             g.setStroke(normalStroke);
                             g.draw(shape);
 
                             g.setColor(Color.black);
                             g.setStroke(new BasicStroke());
                         }
                     }
                 }
             }
         }
 
         int[] tetraloop = new int[nucleotidePositions.length];
         /*
          * for (int i = 0; i < nucleotidePositions.length; i++) { if
          * (consensusSequence.substring(i).matches("^G[ACGT][GA]A.*")) {
          * tetraloop[i] = 1; tetraloop[i + 1] = 1; tetraloop[i + 2] = 1;
          * tetraloop[i + 3] = 1; //System.out.println("MATCH GNRA " + i); } if
          * (consensusSequence.substring(i).matches("^A[ACGT]CG.*")) {
          * tetraloop[i] = 2; tetraloop[i + 1] = 2; tetraloop[i + 2] = 2;
          * tetraloop[i + 3] = 2; //System.out.println("MATCH ANCG " + i); } if
          * (consensusSequence.substring(i).matches("^CAAG.*")) { tetraloop[i] =
          * 3; tetraloop[i + 1] = 3; tetraloop[i + 2] = 3; tetraloop[i + 3] = 3;
          * //System.out.println("MATCH CAAG " + i); } }
          */
 
         // draw the nucleotides
         for (int i = 0; i < nucleotidePositions.length; i++) {
             int pos = (structure.startPosition + i - 1) % mainapp.structureCollection.genomeLength;
             Ellipse2D stemNucleotide = getCircleCenteredAt(nucleotidePositions[i].getX(), nucleotidePositions[i].getY(), nucleotideDiameter);
             g.setColor(Color.white);
             Color nucleotideBackgroundColor = mainapp.missingDataColor;
             if (oneDimensionalData == SHOW && mainapp.data1D != null && mainapp.data1D.used[pos]) {
                 double p = mainapp.data1D.data[pos];
                 if (mainapp.data1D.used[pos] && ((!mainapp.useLowerThreshold1D || p >= mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D || p <= mainapp.thresholdMax1D))) {
                     nucleotideBackgroundColor = mainapp.data1D.colorGradientSecondary.getColor(mainapp.data1D.dataTransform.transform((float) p));
                 } else if (!((!mainapp.useLowerThreshold1D || p >= mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D || p <= mainapp.thresholdMax1D))) {
                     nucleotideBackgroundColor = mainapp.filteredDataColor;
                 }
                 g.setColor(nucleotideBackgroundColor);
                 g.fill(stemNucleotide);
                 g.setColor(Color.black);
                 // drawStringCentred(g, nucleotidePositions[i].getX(), nucleotidePositions[i].getY()+10, val.toString());
             } else {
                 g.setColor(nucleotideBackgroundColor);
                 g.fill(stemNucleotide);
             }
 
             g.setColor(Color.black);
             g.draw(stemNucleotide);
             g.setStroke(new BasicStroke());
 
 
             /*
              *
              * if (tetraloop[i] > 0) { g.setColor(Color.magenta);
              * g.setStroke(new BasicStroke((float) 3)); g.draw(stemNucleotide);
              * } else { g.setColor(Color.black); g.draw(stemNucleotide); }
              * g.setStroke(new BasicStroke());
              */
 
             // draw the information
             g.setColor(ColorTools.selectBestForegroundColor(nucleotideBackgroundColor, Color.white, Color.black));
             if (mainapp.nucleotideComposition != null) {
                 if (mainapp.nucleotideCompositionType == NucleotideCompositionType.SHANNON) {
                     if (pos < mainapp.nucleotideComposition.mappedShannonComposition.length) {
                         double[] fa = new double[4];
                         for (int k = 0; k < fa.length; k++) // java Arrays.copy causes fatal error
                         {
                             fa[k] = mainapp.nucleotideComposition.mappedShannonComposition[pos][k];
                             if (Double.isNaN(fa[k])) {
                                 fa[k] = 0; // java crashes fatally if this is not done
                             }
                         }
                         for (int k = 0; k < 4; k++) {
                             fa[k] = fa[k] / 2;
                         }
                         drawSequenceLogo(g, nucleotidePositions[i].getX(), nucleotidePositions[i].getY() - (nucleotideDiameter / 2) + 0, nucleotideDiameter, nucleotideDiameter - 5, fa);
                         g.setFont(f2);
                     }
                 } else if (mainapp.nucleotideCompositionType == NucleotideCompositionType.FREQUENCY) {
                     int index = (structure.startPosition + i - 1) % mainapp.structureCollection.genomeLength;
                     if (index < mainapp.nucleotideComposition.mappedFrequencyComposition.length) {
                         double[] fa = mainapp.nucleotideComposition.mappedFrequencyComposition[index];
                         drawSequenceLogo(g, nucleotidePositions[i].getX(), nucleotidePositions[i].getY() - (nucleotideDiameter / 2) + 0, nucleotideDiameter, nucleotideDiameter - 5, fa);
                         g.setFont(f2);
                     }
                 }
             }
         }
 
         // draw position lines
         for (int i = 0; i < nucleotidePositions.length; i++) {
             int offsetx = 0;
             double side = 1;
             if (i < length / 2) {
                 offsetx = -(int) (nucleotideDiameter - 3);
 
                 side = -1;
             } else {
                 offsetx = (int) (nucleotideDiameter - 3);
             }
 
             if (nucleotidePositions[i] != null) {
                 g.setColor(Color.black);
                 g.setFont(f2);
                 int pos = (structure.getStartPosition() + i - 1) % mainapp.structureCollection.genomeLength + 1;
                 if(mainapp.numbering != 0 && pos % mainapp.numbering == 0)
                 {
                     drawStringCentred(g, offsetx + nucleotidePositions[i].getX(), nucleotidePositions[i].getY() - 2, "" + pos);
                     g.setColor(Color.black);
                     g.draw(new Line2D.Double(nucleotidePositions[i].getX() + (side * nucleotideDiameter / 2) - 2, nucleotidePositions[i].getY(), nucleotidePositions[i].getX() + (side * nucleotideDiameter / 2) + 2, nucleotidePositions[i].getY()));
                 }
             }
         }
     }
 
     /*
      * public void drawStructureGraphicSVG() throws TransformerException { if
      * (structure == null || nucleotidePositions == null) { return; } int
      * panelWidth = (int) ((maxx - minx) * 4 + xoffset * 2); int panelHeight =
      * (int) ((maxy - miny) * 4 + 100); Dimension d = new Dimension((int)
      * (panelWidth), (int) (panelHeight));
      *
      *
      * DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
      * String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI; SVGDocument doc =
      * (SVGDocument) impl.createDocument(svgNS, "svg", null);
      *
      * SVGGraphics2D g = new SVGGraphics2D(doc); g.setSVGCanvasSize(new
      * Dimension(d.width, d.height)); //
      * g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      * RenderingHints.VALUE_ANTIALIAS_ON);
      *
      * // g.scale(zoomScale, zoomScale);
      *
      * g.setColor(Color.white); g.fillRect(0, 0, d.width, d.height);
      *
      * g.setColor(Color.black);
      *
      * int length = structure.length; covariationInteractions.clear(); if
      * (show2DData && mainapp.data2D != null) { for (int i =
      * structure.getStartPosition(); i <= structure.getEndPosition(); i++) { for
      * (int j = structure.getStartPosition(); j <= structure.getEndPosition();
      * j++) { if (mainapp.maxDistance == -1 ||
      * mainapp.distanceMatrix.getDistance(i - 1, j - 1) <= mainapp.maxDistance)
      * { Color c = null; int k = i - structure.getStartPosition(); int l = j -
      * structure.getStartPosition();
      *
      * double p = mainapp.data2D.matrix.get(i - 1, j - 1); if (p ==
      * mainapp.data2D.matrix.emptyValue) { c = null; } else if
      * (((!mainapp.useLowerThreshold2D || p >= mainapp.thresholdMin2D) &&
      * (!mainapp.useUpperThreshold2D || p <= mainapp.thresholdMax2D))) { // Sy
      * if (mainapp.data2D != null) { //System.out.println(p); c =
      * mainapp.data2D.colorGradientSecondary.getColor((float)
      * mainapp.data2D.dataTransform.transform(p)); } }
      *
      * if (c != null) { double x1 = nucleotidePositions[k].getX(); double y1 =
      * nucleotidePositions[k].getY(); double x2 = nucleotidePositions[l].getX();
      * double y2 = nucleotidePositions[l].getY();
      *
      * Shape shape = null; int structureMidpoint = structure.getStartPosition()
      * + (structure.length / 2);
      *
      * if (i <= structureMidpoint && j <= structureMidpoint) { // both on left
      * side double x1p = Math.max(x1 - Math.abs((y1 - y2) / 2), 0); shape = new
      * QuadCurve2D.Double(x1, y1, x1p, (y1 + y2) / 2, x2, y2); } else if (i >
      * structureMidpoint && j > structureMidpoint) { // both on right side
      * double x2p = Math.min(x2 + Math.abs((y1 - y2) / 2), panelWidth); shape =
      * new QuadCurve2D.Double(x1, y1, x2p, (y1 + y2) / 2, x2, y2); } else {
      * shape = new Line2D.Double(nucleotidePositions[k],
      * nucleotidePositions[l]); } covariationInteractions.add(new
      * Interaction(shape, i, j));
      *
      * g.setColor(c); g.setStroke(normalStroke); g.draw(shape);
      *
      * g.setColor(Color.black); g.setStroke(new BasicStroke()); } } } } }
      *
      * int[] tetraloop = new int[nucleotidePositions.length];
      *
      *
      * // draw the nucleotides for (int i = 0; i < nucleotidePositions.length;
      * i++) { int pos = (structure.startPosition + i - 1) %
      * mainapp.structureCollection.genomeLength; Ellipse2D stemNucleotide =
      * getCircleCenteredAt(nucleotidePositions[i].getX(),
      * nucleotidePositions[i].getY(), nucleotideDiameter);
      * g.setColor(Color.white); Color nucleotideBackgroundColor =
      * mainapp.missingDataColor; if (oneDimensionalData == SHOW &&
      * mainapp.data1D != null && mainapp.data1D.used[pos]) { double p =
      * mainapp.data1D.data[pos]; if (mainapp.data1D.used[pos] &&
      * ((!mainapp.useLowerThreshold1D || p >= mainapp.thresholdMin1D) &&
      * (!mainapp.useUpperThreshold1D || p <= mainapp.thresholdMax1D))) {
      * nucleotideBackgroundColor =
      * mainapp.data1D.colorGradientSecondary.getColor(mainapp.data1D.dataTransform.transform((float)
      * p)); } else if (!((!mainapp.useLowerThreshold1D || p >=
      * mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D || p <=
      * mainapp.thresholdMax1D))) { nucleotideBackgroundColor =
      * mainapp.filteredDataColor; } g.setColor(nucleotideBackgroundColor);
      * g.fill(stemNucleotide); g.setColor(Color.black); // drawStringCentred(g,
      * nucleotidePositions[i].getX(), nucleotidePositions[i].getY()+10,
      * val.toString()); } else { g.setColor(nucleotideBackgroundColor);
      * g.fill(stemNucleotide); }
      *
      * if (tetraloop[i] > 0) { g.setColor(Color.magenta); g.setStroke(new
      * BasicStroke((float) 3)); g.draw(stemNucleotide); } else {
      * g.setColor(Color.black); g.draw(stemNucleotide); } g.setStroke(new
      * BasicStroke());
      *
      * // draw the information
      * g.setColor(ColorTools.selectBestForegroundColor(nucleotideBackgroundColor,
      * Color.white, Color.black)); if (mainapp.nucleotideComposition != null) {
      * if (mainapp.nucleotideCompositionType ==
      * NucleotideCompositionType.SHANNON) { // fa =
      * structure.shannonFrequencies[i]; double[] fa =
      * Arrays.copyOf(mainapp.nucleotideComposition.mappedShannonComposition[(structure.startPosition
      * + i - 1) % mainapp.structureCollection.genomeLength], 5); for (int k = 0;
      * k < 4; k++) { fa[k] = fa[k] / 2; } drawSequenceLogo(g,
      * nucleotidePositions[i].getX(), nucleotidePositions[i].getY() -
      * (nucleotideDiameter / 2) + 3, nucleotideDiameter, nucleotideDiameter - 5,
      * fa); g.setFont(f2); } else if (mainapp.nucleotideCompositionType ==
      * NucleotideCompositionType.FREQUENCY) { double[] fa =
      * mainapp.nucleotideComposition.mappedFrequencyComposition[(structure.startPosition
      * + i - 1) % mainapp.structureCollection.genomeLength]; drawSequenceLogo(g,
      * nucleotidePositions[i].getX(), nucleotidePositions[i].getY() -
      * (nucleotideDiameter / 2) + 3, nucleotideDiameter, nucleotideDiameter - 5,
      * fa); g.setFont(f2); } } }
      *
      * // draw position lines for (int i = 0; i < nucleotidePositions.length;
      * i++) { int offsetx = 0; double side = 1; if (i < length / 2) { offsetx =
      * -(int) (nucleotideDiameter - 3);
      *
      * side = -1; } else { offsetx = (int) (nucleotideDiameter - 3); }
      *
      * if (nucleotidePositions[i] != null) { g.setColor(Color.black); int pos =
      * (structure.getStartPosition() + i - 1) %
      * mainapp.structureCollection.genomeLength + 1; drawStringCentred(g,
      * offsetx + nucleotidePositions[i].getX(), nucleotidePositions[i].getY() -
      * 2, "" + pos); g.setColor(Color.black); g.draw(new
      * Line2D.Double(nucleotidePositions[i].getX() + (side * nucleotideDiameter
      * / 2) - 2, nucleotidePositions[i].getY(), nucleotidePositions[i].getX() +
      * (side * nucleotideDiameter / 2) + 2, nucleotidePositions[i].getY())); } }
      *
      * Element root = doc.getDocumentElement(); g.getRoot(root);
      *
      * // Prepare the output file Transformer transformer; try { transformer =
      * TransformerFactory.newInstance().newTransformer();
      * transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      *
      * //initialize StreamResult with File object to save to file StreamResult
      * result = new StreamResult(new StringWriter()); DOMSource source = new
      * DOMSource(doc); transformer.transform(source, result);
      *
      * String xmlString = result.getWriter().toString();
      * System.out.println(xmlString);
      *
      * } catch (TransformerConfigurationException ex) { ex.printStackTrace(); }
      * }
      */
     public int drawComplexStructure() {
         if (structure == null || nucleotidePositions == null) {
             return 0;
         }
 
         if (useNativeDrawType) {
             drawStructureNative();
             return DRAW_NATIVE_GRAPHIC;
         }
 
         if (structure.length <= 200) {
             createStructureSVG();
             return DRAW_SVG_GRAPHIC;
         } else {
             drawStructureNative();
             return DRAW_NATIVE_GRAPHIC;
         }
     }
 
     public void saveAsSVG(File file) throws IOException {
         BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
         buffer.write(drawStructure(true));
         buffer.close();
     }
 
     public void saveAsEMF(File file) throws IOException {
         File tempFile = new File("temp.svg");
         saveAsSVG(tempFile);
         String svgUrl = "file:///" + tempFile.getAbsolutePath();
         SVG2EMF.convert(svgUrl, file);
     }
 
     public void saveAsPNG(File file) {
        int panelWidth = (int) ((maxx - minx) * horizontalScale + xoffset * 2);
        int panelHeight = (int) ((maxy - miny) * verticalScale + 100);
         Dimension d = new Dimension((int) Math.ceil(panelWidth * zoomScale), (int) Math.ceil(panelHeight * zoomScale));
 
         BufferedImage tempInage = null;
         try {
             tempInage = (BufferedImage) (this.createImage((int) (d.width), (int) (d.height)));
             g = (Graphics2D) tempInage.getGraphics();
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
             //g.scale(zoomScale, zoomScale);
             g.setColor(Color.white);
             g.fillRect(0, 0, (int) (panelWidth * zoomScale), (int) (panelHeight * zoomScale));
 
             if (useNativeDrawType) {
                 g.drawImage(this.bufferedImage, 0, 0, this);
             } else if (diagram != null) {
                 diagram.render(g);
             } else {
                 throw new Exception("Diagram could not be saved. Unknown reason.");
             }
 
             if (tempInage != null) {
                 ImageIO.write(tempInage, "png", file);
             }
 
         } catch (SVGException ex) {
             JOptionPane.showMessageDialog(this.mainapp, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             //    Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
             return;
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(this.mainapp, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
     }
 
     public void drawSequenceLogo(Graphics2D g, double x, double y, double width, double height, double[] h) {
         double fontHeight = g.getFontMetrics(f1).getAscent();
         double base = y;
         double scale = (height / (fontHeight));
 
         double sum = 0;
         for (int i = 0; i < 4; i++) {
             sum += h[i];
         }
         Font subtractFont = f1.deriveFont(AffineTransform.getScaleInstance(scale * 0.8, (1 - sum) * scale));
         base += g.getFontMetrics(subtractFont).getAscent() / 2;
 
         //Font[] fonts = new Font[4];
         for (int i = 0; i < h.length; i++) {
             Font scaledFont = f1.deriveFont(AffineTransform.getScaleInstance(scale * 0.8, h[i] * scale));
             String a = "";
             switch (i) {
                 case 0:
                     a = "A";
                     break;
                 case 1:
                     a = "C";
                     break;
                 case 2:
                     a = "G";
                     break;
                 case 3:
                     if (mainapp.showDNA) {
                         a = "T";
                     } else {
                         a = "U";
                     }
                     break;
             }
             double fh = g.getFontMetrics(scaledFont).getAscent();
             base += fh;
             g.setFont(scaledFont);
             g.drawString(a, (float) (x - (g.getFontMetrics().getStringBounds(a, g).getWidth() / 2)), (float) base);
         }
 
 
         for (int i = 0; i < h.length; i++) {
             //g.setFont(scaledFont);
             ///System.out.println(i + " - " + (base - y));
             //base += g.getFontMetrics(scaledFont).getStringBounds(b, g).getHeight() - (g.getFontMetrics().getHeight()-g.getFontMetrics().getAscent());
             //base += g.getFontMetrics().getHeight() - g.getFontMetrics().getAscent();
             //g.drawString(a, (float) (x - (g.getFontMetrics().getStringBounds(a, g).getWidth() / 2)), (float) base);
         }
     }
 
     /*
      * public void drawSequenceLogo(Graphics2D g, double x, double y, double
      * width, double height, double[] h) { double fontHeight =
      * g.getFontMetrics(f1).getHeight();
      *
      * double scale = (height / (fontHeight)); Font [] fonts = new Font[4];
      * for(int i = 0 ; i < fonts.length ; i++) { double fontHeightScale =
      * (h[i]); fonts[i] = f1.deriveFont(AffineTransform.getScaleInstance(scale,
      * fontHeightScale * scale)); String a = ""; switch (i) { case 0: a = "A";
      * break; case 1: a = "C"; break; case 2: a = "G"; break; case 3: if
      * (dnasequence) { a = "T"; } else { a = "U"; } break; } }
      *
      *
      * double base = y; for (int i = 0; i < h.length; i++) { //h[i] = 0.25;
      *
      * Font scaledFont =
      *
      * String a = ""; switch (i) { case 0: a = "A"; break; case 1: a = "C";
      * break; case 2: a = "G"; break; case 3: if (dnasequence) { a = "T"; } else
      * { a = "U"; } break; } g.setFont(scaledFont); System.out.println(i + " - "
      * + (base - y)); //base += g.getFontMetrics(scaledFont).getStringBounds(b,
      * g).getHeight() -
      * (g.getFontMetrics().getHeight()-g.getFontMetrics().getAscent());
      *
      * base += g.getFontMetrics().getHeight()-g.getFontMetrics().getAscent();
      * g.drawString(a, (float) (x - (g.getFontMetrics().getStringBounds(a,
      * g).getWidth() / 2)), (float) base); } System.out.println(); }
      */
 
     /*
      * public static double transformPval(double pval, double maxPval, double
      * minPval) { double q = Math.log(1 / 255.0); double min =
      * (Math.log10(minPval) - Math.log10(maxPval)); double scale = q / min / 2;
      * double f = Math.exp((Math.log10(pval) - Math.log10(maxPval)) * scale);
      * return f; }
      */
 
     /*
      * public static Color getColorForDsValue(double ds) { float x =
      * (float)Math.max(Math.min(ds/2, 1), 0); return
      * mainapp.data1D.colorGradient.getColor(x); }
      */
     @Override
     public void paintComponent(Graphics graphics) {
         super.paintComponent(graphics);
         Graphics2D g = (Graphics2D) graphics;
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
         int panelWidth = (int) ((maxx - minx) * horizontalScale + xoffset * 2);
         int panelHeight = (int) ((maxy - miny) * verticalScale + 100);
 
         if (repaint) {
             repaint = false;
             currentDrawType = drawComplexStructure();
         }
 
         if (currentDrawType == DRAW_SVG_GRAPHIC) {
             g.scale(zoomScale, zoomScale);
             g.setColor(Color.white);
             g.fillRect(0, 0, panelWidth, panelHeight);
 
             if (diagram != null) {
                 try {
                     diagram.render(g);
 
                     /*
                      * for (int i = 0; i < nucleotidePositions.length; i++) {
                      * int pos = (structure.startPosition + i - 1) %
                      * mainapp.structureCollection.genomeLength;
                      *
                      * Color nucleotideBackgroundColor =
                      * mainapp.missingDataColor; if (oneDimensionalData == SHOW
                      * && mainapp.data1D != null && mainapp.data1D.used[pos]) {
                      * double p = mainapp.data1D.data[pos]; if
                      * (mainapp.data1D.used[pos] &&
                      * ((!mainapp.useLowerThreshold1D || p >=
                      * mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D
                      * || p <= mainapp.thresholdMax1D))) {
                      * nucleotideBackgroundColor =
                      * mainapp.data1D.colorGradientSecondary.getColor(mainapp.data1D.dataTransform.transform((float)
                      * p)); } else if (!((!mainapp.useLowerThreshold1D || p >=
                      * mainapp.thresholdMin1D) && (!mainapp.useUpperThreshold1D
                      * || p <= mainapp.thresholdMax1D))) {
                      * nucleotideBackgroundColor = mainapp.filteredDataColor; }
                      * }
                      *
                      * // draw the information
                      * g.setColor(ColorTools.selectBestForegroundColor(nucleotideBackgroundColor,
                      * Color.white, Color.black)); if
                      * (mainapp.nucleotideComposition != null) { if
                      * (mainapp.nucleotideCompositionType ==
                      * NucleotideCompositionType.SHANNON) { // fa =
                      * structure.shannonFrequencies[i]; double[] fa =
                      * Arrays.copyOf(mainapp.nucleotideComposition.mappedShannonComposition[(structure.startPosition
                      * + i - 1) % mainapp.structureCollection.genomeLength], 5);
                      * for (int k = 0; k < 4; k++) { fa[k] = fa[k] / 2; }
                      * drawSequenceLogo(g, nucleotidePositions[i].getX(),
                      * nucleotidePositions[i].getY() - (nucleotideDiameter / 2)
                      * + 3, nucleotideDiameter, nucleotideDiameter - 5, fa);
                      * g.setFont(f2); } else if
                      * (mainapp.nucleotideCompositionType ==
                      * NucleotideCompositionType.FREQUENCY) { double[] fa =
                      * mainapp.nucleotideComposition.mappedFrequencyComposition[(structure.startPosition
                      * + i - 1) % mainapp.structureCollection.genomeLength];
                      * drawSequenceLogo(g, nucleotidePositions[i].getX(),
                      * nucleotidePositions[i].getY() - (nucleotideDiameter / 2)
                      * + 3, nucleotideDiameter, nucleotideDiameter - 5, fa);
                      * g.setFont(f2); } } }
                      */
                     //  icon.paintIcon(this, g, 0, 0);
                 } catch (SVGException ex) {
                     Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         } else if (currentDrawType == DRAW_NATIVE_GRAPHIC) {
             //g.setColor(Color.lightGray);
             //g.fillRect(0, 0, getWidth(), getHeight());
             g.setColor(Color.white);
             g.fillRect(0, 0, (int) (panelWidth * zoomScale), (int) (panelHeight * zoomScale));
             //drawStructureNative();
             g.drawImage(bufferedImage, 0, 0, this);
         }
 
 
         setPreferredSize(new Dimension((int) Math.ceil(panelWidth * zoomScale), (int) Math.ceil(panelHeight * zoomScale)));
         revalidate();
         g.scale(zoomScale, zoomScale);
         if (selectedNucleotide != -1) {
             g.setColor(Color.black);
             g.drawOval((int) posx - (nucleotideDiameter / 2), (int) posy - (nucleotideDiameter / 2), (int) (nucleotideDiameter), (int) (nucleotideDiameter));
             nucleotidePositions[selectedNucleotide] = new Point2D.Double(posx, posy);
         }
 
         if (selectedNucleotideX != -1 || selectedNucleotideY != -1) {
             g.setColor(Color.blue);
             g.setStroke(new BasicStroke((float) 4));
 
             int nucX = selectedNucleotideX - structure.startPosition + 1;
             if (nucX >= 0 && nucX < nucleotidePositions.length) {
                 g.drawOval((int) nucleotidePositions[nucX].x - (nucleotideDiameter / 2), (int) nucleotidePositions[nucX].y - (nucleotideDiameter / 2), (int) (nucleotideDiameter), (int) (nucleotideDiameter));
             }
 
             int nucY = selectedNucleotideY - structure.startPosition + 1;
             if (nucY >= 0 && nucY < nucleotidePositions.length) {
                 g.drawOval((int) nucleotidePositions[nucY].x - (nucleotideDiameter / 2), (int) nucleotidePositions[nucY].y - (nucleotideDiameter / 2), (int) (nucleotideDiameter), (int) (nucleotideDiameter));
             }
 
             g.setColor(Color.black);
             g.setStroke(new BasicStroke());
         }
 
         // highlight nucleotides
         for (int i = startHighlightPosition; i < endHighlightPosition; i++) {
             g.setColor(Color.orange);
             g.setStroke(new BasicStroke((float) 3));
 
             int nucX = i - structure.startPosition;
             if (nucX >= 0 && nucX < nucleotidePositions.length) {
                 g.drawOval((int) nucleotidePositions[nucX].x - (nucleotideDiameter / 2), (int) nucleotidePositions[nucX].y - (nucleotideDiameter / 2), (int) (nucleotideDiameter), (int) (nucleotideDiameter));
             }
         }
     }
     int startHighlightPosition = -1;
     int endHighlightPosition = -1;
 
     public void setHighlightPosition(int startPosition, int endPosition) {
         this.startHighlightPosition = startPosition;
         this.endHighlightPosition = endPosition;
         repaint();
     }
 
     public static void drawStringCentred(Graphics2D g, double x, double y, String s) {
         FontMetrics fm = g.getFontMetrics();
         java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, g);
 
         int textHeight = (int) (rect.getHeight());
         int textWidth = (int) (rect.getWidth());
         double x1 = x + (-textWidth / 2);
         double y1 = y + (-textHeight / 2 + fm.getAscent());
 
         g.drawString(s, (float) x1, (float) y1);  // Draw the string.
     }
     double posx = -1;
     double posy = -1;
 
     public void mouseDragged(MouseEvent e) {
         // if (SwingUtilities.isLeftMouseButton(e)) {
         posx = e.getX() / zoomScale;
         posy = e.getY() / zoomScale;
         repaint();
         //}
     }
     int selectedNucleotideX = -1;
     int selectedNucleotideY = -1;
 
     public void mouseMoved(MouseEvent e) {
         String interactionText = "";
 
         double x = e.getPoint().x / zoomScale;
         double y = e.getPoint().y / zoomScale;
 
         if (nucleotidePositions != null) {
             // 1D interactions
             int minIndex = -1;
             double minDistance = Double.MAX_VALUE;
             for (int i = 0; i < nucleotidePositions.length; i++) {
                 Point2D.Double scaledPoint = new Point2D.Double(x, y);
                 double distance = nucleotidePositions[i].distance(scaledPoint);
                 if (distance < minDistance) {
                     minDistance = distance;
                     minIndex = i;
                 }
             }
             int nucleotide = -1;
             if (minDistance <= nucleotideDiameter / 2) {
                 nucleotide = minIndex;
             }
 
             int pos = (structure.getStartPosition() + nucleotide - 1) % mainapp.structureCollection.genomeLength;
             if (mainapp.nucleotideComposition != null && nucleotide != -1) {
                 interactionText += "Composition (";
                 for (int i = 0; i < 4; i++) {
                     String a = "";
                     switch (i) {
                         case 0:
                             a = "A";
                             break;
                         case 1:
                             a = "C";
                             break;
                         case 2:
                             a = "G";
                             break;
                         case 3:
                             if (mainapp.showDNA) {
                                 a = "T";
                             } else {
                                 a = "U";
                             }
                             break;
                     }
                     double perc = mainapp.nucleotideComposition.mappedFrequencyComposition[pos][i];
                     interactionText += a + " " + decimalFormat.format(perc * 100) + "%,  ";
                 }
                 interactionText += "[" + mainapp.nucleotideComposition.mappedNonGapCount[pos] + "])     ";
             } else {
                 interactionText += "Composition (none)     ";
             }
 
             if (mainapp.data1D != null && nucleotide != -1) {
                 double p = mainapp.data1D.data[pos];
                 interactionText += "1D data (" + (pos + 1) + ", " + DataLegend.formatValue(p, mainapp.data1D.dataTransform, 6) + ")     ";
 
             } else {
                 interactionText += "1D data (none)     ";
             }
         }
 
 
         // 2D interactions
         if (mainapp != null) {
             mainapp.data2DLabel.setText("");
             int interaction2D = -1;
             for (int i = 0; i < covariationInteractions.size(); i++) {
                 //  if (covariationInteractions.get(i).shape instanceof QuadCurve2) {
                 int c = 0;
                 boolean[] count = new boolean[4];
                 count[0] = covariationInteractions.get(i).shape.intersects(x - 2, y - 2, 4, 4);
                 count[1] = covariationInteractions.get(i).shape.intersects(x + 2, y - 2, 4, 4);
                 count[2] = covariationInteractions.get(i).shape.intersects(x - 2, y + 2, 4, 4);
                 count[3] = covariationInteractions.get(i).shape.intersects(x + 2, y + 2, 4, 4);
                 for (int k = 0; k < count.length; k++) {
                     if (count[k]) {
                         c++;
                     }
                 }
                 if (c > 0) {
                     //System.out.println(c);
                 }
                 if (c >= 1 && c <= 3) {// mouse over information
                     interaction2D = i;
                 }
             }
 
             int oldSelectedNucleotideX = selectedNucleotideX;
             int oldSelectedNucleotideY = selectedNucleotideY;
             selectedNucleotideX = -1;
             selectedNucleotideY = -1;
             if (interaction2D != -1) {
                 Interaction interaction = covariationInteractions.get(interaction2D);
                 double p = mainapp.data2D.matrix.get(interaction.nucleotidei - 1, interaction.nucleotidej - 1);
                 //mainapp.data2DLabel.setText(interaction.nucleotidei + " <-> " + interaction.nucleotidej + "  =  " + mainapp.data2D.matrix.get(interaction.nucleotidei - 1, interaction.nucleotidej - 1));
                 //System.out.println("INTERACTION " + covariationInteractions.get(i));
                 interactionText += "2D data (" + interaction.nucleotidei + " <-> " + interaction.nucleotidej + ", " + DataLegend.formatValue(p, mainapp.data2D.dataTransform, 6) + ")";
                 this.selectedNucleotideX = interaction.nucleotidei - 1;
                 this.selectedNucleotideY = interaction.nucleotidej - 1;
             } else {
                 interactionText += "2D data (none)";
             }
 
             mainapp.data2DLabel.setText(interactionText);
 
             if (oldSelectedNucleotideX != selectedNucleotideX || oldSelectedNucleotideY != selectedNucleotideY) {
                 repaint();
             }
         }
 
     }
     int selectedNucleotide = -1;
 
     public void mouseClicked(MouseEvent e) {
 
         /*
          * for(int i = 0 ; ; i++) { saveAsPNG(new
          * File("c:/project/hepacivirus/images/hcv-"+(i+1)+".png"));
          * mainapp.selected = (mainapp.selected + 1) %
          * mainapp.directoryStructureFiles.size();
          * mainapp.openStructure(mainapp.selected); }
          */
     }
 
     public void mousePressed(MouseEvent e) {
         if (SwingUtilities.isLeftMouseButton(e)) {
             // select nucleotide to be moved
             int minIndex = -1;
             double minDistance = Double.MAX_VALUE;
             for (int i = 0; i < nucleotidePositions.length; i++) {
                 Point2D.Double scaledPoint = new Point2D.Double(e.getPoint().x / zoomScale, e.getPoint().y / zoomScale);
                 double distance = nucleotidePositions[i].distance(scaledPoint);
                 if (distance < minDistance) {
                     minDistance = distance;
                     minIndex = i;
                 }
             }
             if (minDistance <= nucleotideDiameter / 2) {
                 selectedNucleotide = minIndex;
             }
         }
     }
 
     public void mouseReleased(MouseEvent e) {
         if (SwingUtilities.isRightMouseButton(e)) {
             this.popupMenu.show(this, e.getX(), e.getY());
         } else if (SwingUtilities.isLeftMouseButton(e)) {
             selectedNucleotide = -1;
             redraw();
         }
     }
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseExited(MouseEvent e) {
     }
 
     public void actionPerformed(ActionEvent e) {
         if (e.getSource().equals(saveAsPNGItem)) {
             String name = "structure";
             MainApp.fileChooserSave.setDialogTitle("Save as PNG");
             MainApp.fileChooserSave.setSelectedFile(new File(MainApp.fileChooserSave.getCurrentDirectory().getPath() + "/" + name + ".png"));
             int returnVal = MainApp.fileChooserSave.showSaveDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 saveAsPNG(MainApp.fileChooserSave.getSelectedFile());
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             }
             MainApp.fileChooserSave.setDialogTitle("Open");
         } else if (e.getSource().equals(saveAsSVGItem)) {
             MainApp.fileChooserSave.setDialogTitle("Save as SVG");
             String name = "structure";
             MainApp.fileChooserSave.setSelectedFile(new File(MainApp.fileChooserSave.getCurrentDirectory().getPath() + "/" + name + ".svg"));
             int returnVal = MainApp.fileChooserSave.showSaveDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 try {
                     saveAsSVG(MainApp.fileChooserSave.getSelectedFile());
                 } catch (IOException ex) {
                     Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             }
             MainApp.fileChooserSave.setDialogTitle("Open");
         } else if (e.getSource().equals(saveAsEMFItem)) {
             MainApp.fileChooserSave.setDialogTitle("Save as EMF");
             String name = "structure";
             MainApp.fileChooserSave.setSelectedFile(new File(MainApp.fileChooserSave.getCurrentDirectory().getPath() + "/" + name + ".emf"));
             int returnVal = MainApp.fileChooserSave.showSaveDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 try {
                     saveAsEMF(MainApp.fileChooserSave.getSelectedFile());
                 } catch (IOException ex) {
                     Logger.getLogger(StructureDrawPanel.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             }
             MainApp.fileChooserSave.setDialogTitle("Open");
         } else if (e.getSource().equals(dnaMenuItem)) {
             mainapp.showDNA = true;
             redraw();
         } else if (e.getSource().equals(rnaMenuItem)) {
             mainapp.showDNA = false;
             redraw();
 
         } else if (e.getSource().equals(this.showBondsItem)) {
             mainapp.showBonds = showBondsItem.isSelected();
             redraw();
         } else if (e.getSource().equals(numberNone) || e.getSource().equals(number1) || e.getSource().equals(number5) || e.getSource().equals(number10)) {
             if (e.getSource().equals(numberNone)) {
                 mainapp.numbering = 0;
             }
             if (e.getSource().equals(number1)) {
                 mainapp.numbering = 1;
             }
             if (e.getSource().equals(number5)) {
                 mainapp.numbering = 5;
             }
             if (e.getSource().equals(number10)) {
                 mainapp.numbering = 10;
             }
             redraw();
         } else {
             // else zoom levels
             for (int i = 0; i < zoomLevels.length; i++) {
                 if (e.getSource().equals(zoomMenuItems[i])) {
                     currentZoomIndex = i;
                     this.zoomScale = (double) zoomLevels[currentZoomIndex] / 100.0;
                     drawComplexStructure();
                     //createStructureSVG();
                     break;
                 }
             }
         }
     }
 
     @Override
     public void mouseWheelMoved(MouseWheelEvent e) {
         int clicks = e.getWheelRotation();
         if(clicks < 0)
         {
             zoomScale = Math.min(4, zoomScale + 0.10*(-Math.min(clicks,4)));
         }
         else
         {
             zoomScale = Math.max(0.1, zoomScale - 0.10*Math.min(clicks,4));
         }
         drawComplexStructure();
         redraw();
     }
 
     class Interaction {
 
         Shape shape;
         int nucleotidei;
         int nucleotidej;
 
         public Interaction(Shape shape, int nucleotidei, int nucleotidej) {
             this.shape = shape;
             this.nucleotidei = nucleotidei;
             this.nucleotidej = nucleotidej;
         }
 
         public String toString() {
             return nucleotidei + " <-> " + nucleotidej;
         }
     }
 }
