 package edu.mit.wi.haploview;
 
 import java.awt.*;
 import java.awt.geom.GeneralPath;
 import java.awt.image.BufferedImage;
 import java.awt.event.*;
 import java.util.Vector;
 import javax.swing.*;
 import javax.swing.border.CompoundBorder;
 
 class DPrimeDisplay extends JComponent implements MouseListener, MouseMotionListener, Constants{
     private static final int H_BORDER = 30;
     private static final int V_BORDER = 15;
     private static final int TEXT_GAP = 3;
 
     private static final int STD_SCHEME = 1;
     private static final int SFS_SCHEME = 2;
     private static final int GAM_SCHEME = 3;
 
     private static final int BOX_SIZES[] = {50, 24, 12};
     private static final int BOX_RADII[] = {24, 11, 6};
     private static final int TICK_HEIGHT = 8;
     private static final int TICK_BOTTOM = 50;
 
     private int widestMarkerName = 80; //default size
     private int infoHeight = 0, blockDispHeight = 0;
     private int boxSize = BOX_SIZES[0];
     private int boxRadius = BOX_RADII[0];
     private int lowX, highX, lowY, highY;
     private int left = H_BORDER;
     private int top = V_BORDER;
     private int clickXShift, clickYShift;
     private String[] displayStrings;
     private final int popupLeftMargin = 12;
 
    private final Color BG_GREY = new Color(212,208,200);

 
     BasicStroke thickerStroke = new BasicStroke(1);
     BasicStroke thinnerStroke = new BasicStroke(0.35f);
     BasicStroke fatStroke = new BasicStroke(2.5f);
     float dash1[] = {5.0f};
     BasicStroke dashedFatStroke = new BasicStroke(2.5f,
             BasicStroke.CAP_BUTT,
             BasicStroke.JOIN_MITER,
             5.0f, dash1, 0.0f);
     BasicStroke dashedThinStroke = new BasicStroke(0.35f,
             BasicStroke.CAP_BUTT,
             BasicStroke.JOIN_MITER,
             5.0f, dash1, 0.0f);
     private Font boxFont = new Font("SansSerif", Font.PLAIN, 12);
     private Font markerNumFont = new Font("SansSerif", Font.BOLD, 12);
     private Font markerNameFont = new Font("Default", Font.PLAIN, 12);
     private Font boldMarkerNameFont = new Font("Default", Font.BOLD, 12);
 
     private boolean printDetails = true;
     private boolean forExport = false;
     private int exportStart, exportStop;
     private boolean showWM = false;
     private int zoomLevel = 0;
     private boolean noImage = true;
     private boolean popupExists, resizeRectExists, blockRectExists = false;
 
     private Rectangle wmInteriorRect = new Rectangle();
     private Rectangle wmResizeCorner = new Rectangle(0,0,-1,-1);
     private Rectangle resizeWMRect = new Rectangle(0,0,-1,-1);
     private Rectangle popupDrawRect = new Rectangle(0,0,-1,-1);
     private BufferedImage worldmap;
     private HaploData theData;
     private HaploView theHV;
     private Dimension chartSize=null;
     private int wmMaxWidth=0;
     private Rectangle blockRect = new Rectangle(0,0,-1,-1);
     private int blockStartX = 0;
 
 
     DPrimeDisplay(HaploView h){
         theData=h.theData;
         theHV = h;
         this.colorDPrime(STD_SCHEME);
         this.setDoubleBuffered(true);
         addMouseListener(this);
         addMouseMotionListener(this);
     }
 
     public void refresh(){
         noImage = true;
         repaint();
     }
 
     public void colorDPrime(int scheme){
 
         PairwiseLinkage dPrime[][] = theData.filteredDPrimeTable;
         if (scheme == STD_SCHEME){
             // set coloring based on LOD and D'
             for (int i = 0; i < dPrime.length; i++){
                 for (int j = i+1; j < dPrime[i].length; j++){
                     PairwiseLinkage thisPair = dPrime[i][j];
                     if (thisPair == null){
                         continue;
                     }
 
                     double d = thisPair.getDPrime();
                     double l = thisPair.getLOD();
                     Color boxColor = null;
                     if (l > 2) {
                         if (d < 0.5) {
                             //high LOD, low D'
                             boxColor = new Color(255, 224, 224);
                         } else {
                             //high LOD, high D' shades of red
                             double blgr = (255-32)*2*(1-d);
                             boxColor = new Color(255, (int) blgr, (int) blgr);
                             //boxColor = new Color(224, (int) blgr, (int) blgr);
                         }
                     } else if (d > 0.99) {
                         //high D', low LOD blueish color
                         boxColor = new Color(192, 192, 240);
                     } else {
                         //no LD
                         boxColor = Color.white;
                     }
                     thisPair.setColor(boxColor);
                 }
             }
         }else if (scheme == SFS_SCHEME){
             for (int x = 0; x < dPrime.length-1; x++){
                 for (int y = x+1; y < dPrime.length; y++){
                     PairwiseLinkage thisPair = dPrime[x][y];
                     if (thisPair == null){
                         continue;
                     }
                     //get the right bits
                     double lowCI = thisPair.getConfidenceLow();
                     double highCI = thisPair.getConfidenceHigh();
 
                     //color in squares
                     if (lowCI >= FindBlocks.cutLowCI && highCI >= FindBlocks.cutHighCI) {
                         thisPair.setColor(Color.darkGray);  //strong LD
                     }else if (highCI >= FindBlocks.recHighCI) {
                         thisPair.setColor(Color.lightGray); //uninformative
                     } else {
                         thisPair.setColor(Color.white); //recomb
                     }
                 }
             }
         }else if (scheme == GAM_SCHEME){
             for (int x = 0; x < dPrime.length-1; x++){
                 for (int y = x+1; y < dPrime.length; y++){
                     PairwiseLinkage thisPair = dPrime[x][y];
                     if (thisPair == null) {
                         continue;
                     }
 
                     double[] freqs = thisPair.getFreqs();
                     int numGam = 0;
                     for (int i = 0; i < freqs.length; i++){
                         //add a little bump for EM probs which should be zero but are really like 10^-10
                         if (freqs[i] > FindBlocks.fourGameteCutoff + 1E-8) numGam++;
                     }
 
                     //color in squares
                     if(numGam > 3){
                         thisPair.setColor(Color.white);
                     }else{
                         thisPair.setColor(Color.darkGray);
                     }
                 }
             }
         }
     }
 
     public BufferedImage export(int start, int stop){
         forExport = true;
         exportStart = start;
         if (exportStart < 0){
             exportStart = 0;
         }
         exportStop = stop;
         if (exportStop > theData.filteredDPrimeTable.length){
             exportStop = theData.filteredDPrimeTable.length;
         }
         Dimension pref = getPreferredSize();
         BufferedImage i = new BufferedImage(pref.width, pref.height,
                 BufferedImage.TYPE_3BYTE_BGR);
         paintComponent(i.getGraphics());
         forExport = false;
         return i;
     }
 
     public void zoom(int type){
         int diff = type - zoomLevel;
 
         zoomLevel = type;
         int x=0, y=0;
         int oldX = getVisibleRect().x;
         int oldY = getVisibleRect().y;
         int oldWidth = getVisibleRect().width;
         int oldHeight = getVisibleRect().height;
 
         if (diff > 0){
             //we're zooming out
             x = oldX /(2*diff) - oldWidth/4*diff;
             y = oldY /(2*diff) - oldHeight/4*diff;
         } else if (diff < 0 ) {
             //we're zooming in
             diff = -diff;
             x = oldX*2*diff + oldWidth/2*diff;
             y = oldY*2*diff + oldHeight/2*diff;
         }else{
             //we didn't change the zoom so don't waste cycles
             return;
         }
         if (x < 0){
             x = 0;
         }
         if (y < 0){
             y = 0;
         }
         boxSize = BOX_SIZES[zoomLevel];
         boxRadius = BOX_RADII[zoomLevel];
         ((JViewport)getParent()).setViewSize(getPreferredSize());
         //System.out.println(oldX + " " + x + " " + oldY + " " + y);
         ((JViewport)getParent()).setViewPosition(new Point(x,y));
     }
 
     public void paintComponent(Graphics g){
         PairwiseLinkage[][] dPrimeTable = theData.filteredDPrimeTable;
         Vector blocks = theData.blocks;
         Rectangle visRect = getVisibleRect();
 
         //deal with zooming
         if (chartSize.getWidth() > (3*visRect.width)){
             showWM = true;
         }else{
             showWM = false;
         }
         if (zoomLevel == 0){
             printDetails = true;
         } else{
             printDetails = false;
         }
 
         Graphics2D g2 = (Graphics2D) g;
         Dimension size = getSize();
         Dimension pref = getPreferredSize();
         if (size.height < pref.height){
             setSize(pref);
         }
 
         //okay so this dumb if block is to prevent the ugly repainting
         //bug when loading markers after the data are already being displayed,
         //results in a little off-centering for small datasets, but not too bad.
         if (!forExport){
             if (!theData.infoKnown){
                 g2.translate((size.width - pref.width) / 2,
                         (size.height - pref.height) / 2);
             } else {
                 g2.translate((size.width - pref.width) / 2,
                         0);
             }
         }
 
         FontMetrics boxFontMetrics = g2.getFontMetrics(boxFont);
 
         int diamondX[] = new int[4];
         int diamondY[] = new int[4];
         Polygon diamond;
 
         left = H_BORDER;
         top = V_BORDER;
         if (forExport){
             left -= exportStart * boxSize;
         }
 
         FontMetrics metrics;
         int ascent;
        g2.setColor(BG_GREY);
         g2.fillRect(0,0,pref.width,pref.height);
         g2.setColor(Color.black);
 
 
 
         g2.setFont(boldMarkerNameFont);
         metrics = g2.getFontMetrics();
         ascent = metrics.getAscent();
 
         //TODO: finish implementing scaling gizmo
         /*//deal with adding some space to better display data with large gaps
         int cumulativeGap[] = new int[Chromosome.getFilteredSize()];
         for (int i = 0; i < cumulativeGap.length; i++){
             cumulativeGap[i] = 0;
         }
         if (theData.infoKnown){
             double mean = (((SNP)Chromosome.markers[Chromosome.markers.length-1]).getPosition() -
                     ((SNP)Chromosome.markers[0]).getPosition())/Chromosome.markers.length-1;
             for (int i = 1; i < cumulativeGap.length; i++){
                 double sep = Chromosome.getMarker(i).getPosition() - Chromosome.getMarker(i-1).getPosition();
                 if (sep > mean*10){
                     cumulativeGap[i] = cumulativeGap[i-1] + (int)(sep/mean)*4;
                 }else{
                     cumulativeGap[i] = cumulativeGap[i-1];
                 }
             }
         } */
 
         if (theData.infoKnown) {
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);
 
             //// draw the marker locations
 
             int lineSpan = (dPrimeTable.length-1) * boxSize;
 
             long minpos = Chromosome.getMarker(0).getPosition();
             long maxpos = Chromosome.getMarker(Chromosome.getSize()-1).getPosition();
             double spanpos = maxpos - minpos;
             g2.setStroke(thinnerStroke);
             g2.setColor(Color.white);
             g2.fillRect(left+1, top+1, lineSpan-1, TICK_HEIGHT-1);
             g2.setColor(Color.black);
             g2.drawRect(left, top, lineSpan, TICK_HEIGHT);
 
             for (int i = 0; i < Chromosome.getFilteredSize(); i++) {
                 double pos = (Chromosome.getFilteredMarker(i).getPosition() - minpos) / spanpos;
                 int xx = (int) (left + lineSpan*pos);
                 g2.setStroke(thickerStroke);
                 g2.drawLine(xx, top, xx, top + TICK_HEIGHT);
                 g2.setStroke(thinnerStroke);
                 g2.drawLine(xx, top + TICK_HEIGHT,
                         left + i*boxSize, top+TICK_BOTTOM);
             }
             top += TICK_BOTTOM + TICK_HEIGHT;
 
             //// draw the marker names
             if (printDetails){
                 widestMarkerName = metrics.stringWidth(Chromosome.getFilteredMarker(0).getName());
                 for (int x = 1; x < dPrimeTable.length; x++) {
                     int thiswide = metrics.stringWidth(Chromosome.getFilteredMarker(x).getName());
                     if (thiswide > widestMarkerName) widestMarkerName = thiswide;
                 }
 
                 g2.translate(left, top + widestMarkerName);
                 g2.rotate(-Math.PI / 2.0);
                 for (int x = 0; x < dPrimeTable.length; x++) {
                     if (theData.isInBlock[x]){
                         g2.setFont(boldMarkerNameFont);
                     }else{
                         g2.setFont(markerNameFont);
                     }
                     g2.drawString(Chromosome.getFilteredMarker(x).getName(),TEXT_GAP, x*boxSize + ascent/3);
                 }
 
                 g2.rotate(Math.PI / 2.0);
                 g2.translate(-left, -(top + widestMarkerName));
 
                 // move everybody down
                 top += widestMarkerName + TEXT_GAP;
             }
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_OFF);
         }
         top  += blockDispHeight;
 
 
         //// draw the marker numbers
         if (printDetails){
             g2.setFont(markerNumFont);
             metrics = g2.getFontMetrics();
             ascent = metrics.getAscent();
 
             for (int x = 0; x < dPrimeTable.length; x++) {
                 String mark = String.valueOf(Chromosome.realIndex[x] + 1);
                 g2.drawString(mark,
                         left + x*boxSize - metrics.stringWidth(mark)/2,
                         top + ascent);
             }
             top += boxRadius/2; // give a little space between numbers and boxes
         }
 
         //clickxshift and clickyshift are used later to translate from x,y coords
         //to the pair of markers comparison at those coords
         if (!(theData.infoKnown)){
             clickXShift = left + (size.width-pref.width)/2;
             clickYShift = top + (size.height - pref.height)/2;
         } else {
             clickXShift = left + (size.width-pref.width)/2;
             clickYShift = top;
         }
 
         //the following values are the bounds on the boxes we want to
         //display given that the current window is 'visRect'
         lowX = (visRect.x-clickXShift-(visRect.y +
                 visRect.height-clickYShift))/boxSize;
         if (lowX < 0) {
             lowX = 0;
         }
         highX = ((visRect.x + visRect.width)/boxSize)+1;
         if (highX > dPrimeTable.length-1){
             highX = dPrimeTable.length-1;
         }
         lowY = ((visRect.x-clickXShift)+(visRect.y-clickYShift))/boxSize;
         if (lowY < lowX+1){
             lowY = lowX+1;
         }
         highY = (((visRect.x-clickXShift+visRect.width) +
                 (visRect.y-clickYShift+visRect.height))/boxSize)+1;
         if (highY > dPrimeTable.length){
             highY = dPrimeTable.length;
         }
 
         if (forExport){
             lowX = exportStart;
             lowY = exportStart;
             highX = exportStop;
             highY = exportStop;
         }
 
         // draw table column by column
         for (int x = lowX; x < highX; x++) {
 
             //always draw the fewest possible boxes
             if (lowY < x+1){
                 lowY = x+1;
             }
 
             for (int y = lowY; y < highY; y++) {
                 if (dPrimeTable[x][y] == null){
                     continue;
                 }
                 double d = dPrimeTable[x][y].getDPrime();
                 //double l = dPrimeTable[x][y].getLOD();
                 Color boxColor = dPrimeTable[x][y].getColor();
 
                 // draw markers above
                 int xx = left + (x + y) * boxSize / 2;
                 int yy = top + (y - x) * boxSize / 2;
 
                 diamondX[0] = xx; diamondY[0] = yy - boxRadius;
                 diamondX[1] = xx + boxRadius; diamondY[1] = yy;
                 diamondX[2] = xx; diamondY[2] = yy + boxRadius;
                 diamondX[3] = xx - boxRadius; diamondY[3] = yy;
 
                 diamond = new Polygon(diamondX, diamondY, 4);
                 g2.setColor(boxColor);
                 g2.fillPolygon(diamond);
 
                 if(printDetails){
                     g2.setFont(boxFont);
                     ascent = boxFontMetrics.getAscent();
                     int val = (int) (d * 100);
                     g2.setColor((val < 50) ? Color.gray : Color.black);
                     if (boxColor == Color.darkGray){
                         g2.setColor(Color.white);
                     }
                     if (val != 100) {
                         String valu = String.valueOf(val);
                         int widf = boxFontMetrics.stringWidth(valu);
                         g.drawString(valu, xx - widf/2, yy + ascent/2);
                     }
                 }
             }
         }
 
         boolean even = true;
         //highlight blocks
         g2.setFont(markerNameFont);
         ascent = g2.getFontMetrics().getAscent();
         //g.setColor(new Color(153,255,153));
         g2.setColor(Color.black);
         //g.setColor(new Color(51,153,51));
         for (int i = 0; i < blocks.size(); i++){
             int[] theBlock = (int[])blocks.elementAt(i);
             int first = theBlock[0];
             int last = theBlock[theBlock.length-1];
 
             //big vee around whole thing
             g2.setStroke(fatStroke);
             g2.drawLine(left + (2*first) * boxSize/2 - boxRadius,
                     top,
                     left + (first + last) * boxSize/2,
                     top + (last - first) * boxSize/2 + boxRadius);
             g2.drawLine(left + (first + last) * boxSize/2,
                     top + (last - first) * boxSize/2 + boxRadius,
                     left + (2*last) * boxSize/2+boxRadius,
                     top);
 
             for (int j = first; j <= last; j++){
                 if (theData.isInBlock[j]){
                     g2.setStroke(fatStroke);
                 }else{
                     g2.setStroke(dashedFatStroke);
                 }
                 g2.drawLine(left+j*boxSize-boxSize/2,
                         top-blockDispHeight,
                         left+j*boxSize+boxSize/2,
                         top-blockDispHeight);
             }
 
             //lines to connect to block display
             g2.setStroke(fatStroke);
             g2.drawLine(left + first*boxSize-boxSize/2,
                     top-1,
                     left+first*boxSize-boxSize/2,
                     top-blockDispHeight);
             g2.drawLine(left+last*boxSize+boxSize/2,
                     top-1,
                     left+last*boxSize+boxSize/2,
                     top-blockDispHeight);
             if (printDetails){
                 String labelString = new String ("Block " + (i+1));
                 if (theData.infoKnown){
                     long blockSize = Chromosome.getMarker(last).getPosition() -
                             Chromosome.getMarker(first).getPosition();
                     labelString += " (" + blockSize/1000 + " kb)";
                 }
                 g2.drawString(labelString, left+first*boxSize-boxSize/2+TEXT_GAP, top-boxSize/3);
             }
         }
         g2.setStroke(thickerStroke);
 
 
         //see if the user has right-clicked to popup some marker info
         if(popupExists){
             int smallDatasetSlopH = 0;
             int smallDatasetSlopV = 0;
             if (pref.getWidth() < visRect.width){
                 //dumb bug where little datasets popup the box in the wrong place
                 smallDatasetSlopH = (int)(visRect.width - pref.getWidth())/2;
                 smallDatasetSlopV = (int)(visRect.height - pref.getHeight())/2;
             }
             g2.setColor(Color.white);
             g2.fillRect(popupDrawRect.x+1-smallDatasetSlopH,
                     popupDrawRect.y+1-smallDatasetSlopV,
                     popupDrawRect.width-1,
                     popupDrawRect.height-1);
             g2.setColor(Color.black);
             g2.drawRect(popupDrawRect.x-smallDatasetSlopH,
                     popupDrawRect.y-smallDatasetSlopV,
                     popupDrawRect.width,
                     popupDrawRect.height);
 
             for (int x = 0; x < displayStrings.length; x++){
                 g.drawString(displayStrings[x],popupDrawRect.x + popupLeftMargin-smallDatasetSlopH,
                         popupDrawRect.y+((x+1)*metrics.getHeight())-smallDatasetSlopV);
             }
         }
 
         if (showWM && !forExport){
             //dataset is big enough to require worldmap
             if (noImage){
                 //first time through draw a worldmap if dataset is big:
                 final int WM_BD_GAP = 1;
                 final int WM_BD_HEIGHT = 2;
                 final int WM_BD_TOTAL = WM_BD_HEIGHT + 2*WM_BD_GAP;
                 if (wmMaxWidth == 0){
                     wmMaxWidth = visRect.width/3;
                 }
                 double scalefactor;
                 scalefactor = (double)(chartSize.width)/wmMaxWidth;
 
                 CompoundBorder wmBorder = new CompoundBorder(BorderFactory.createRaisedBevelBorder(),
                         BorderFactory.createLoweredBevelBorder());
                 worldmap = new BufferedImage((int)(chartSize.width/scalefactor)+wmBorder.getBorderInsets(this).left*2,
                         (int)(chartSize.height/scalefactor)+wmBorder.getBorderInsets(this).top*2+WM_BD_TOTAL,
                         BufferedImage.TYPE_3BYTE_BGR);
 
                 Graphics gw = worldmap.getGraphics();
                 Graphics2D gw2 = (Graphics2D)(gw);
                 gw2.setColor(this.getBackground());
                 gw2.fillRect(1,1,worldmap.getWidth()-1,worldmap.getHeight()-1);
                 //make a pretty border
                 gw2.setColor(Color.black);
 
                 wmBorder.paintBorder(this,gw2,0,0,worldmap.getWidth(),worldmap.getHeight());
                 wmInteriorRect = wmBorder.getInteriorRectangle(this,0,0,worldmap.getWidth(), worldmap.getHeight());
 
                 double prefBoxSize = boxSize/(scalefactor*((double)wmMaxWidth/(double)(wmMaxWidth-WM_BD_TOTAL)));
 
                 float[] smallDiamondX = new float[4];
                 float[] smallDiamondY = new float[4];
                 GeneralPath gp;
                 for (int x = 0; x < dPrimeTable.length-1; x++){
                     for (int y = x+1; y < dPrimeTable.length; y++){
                         if (dPrimeTable[x][y] == null){
                             continue;
                         }
                         double xx = (x + y)*prefBoxSize/2+wmBorder.getBorderInsets(this).left;
                         double yy = (y - x)*prefBoxSize/2+wmBorder.getBorderInsets(this).top + WM_BD_TOTAL;
 
 
                         smallDiamondX[0] = (float)xx; smallDiamondY[0] = (float)(yy - prefBoxSize/2);
                         smallDiamondX[1] = (float)(xx + prefBoxSize/2); smallDiamondY[1] = (float)yy;
                         smallDiamondX[2] = (float)xx; smallDiamondY[2] = (float)(yy + prefBoxSize/2);
                         smallDiamondX[3] = (float)(xx - prefBoxSize/2); smallDiamondY[3] = (float)yy;
 
                         gp =  new GeneralPath(GeneralPath.WIND_EVEN_ODD,  smallDiamondX.length);
                         gp.moveTo(smallDiamondX[0],smallDiamondY[0]);
                         for (int i = 1; i < smallDiamondX.length; i++){
                             gp.lineTo(smallDiamondX[i], smallDiamondY[i]);
                         }
                         gp.closePath();
 
                         gw2.setColor(dPrimeTable[x][y].getColor());
                         gw2.fill(gp);
 
                     }
                 }
                 //draw block display in worldmap
                 gw2.setColor(this.getBackground());
                 gw2.fillRect(wmBorder.getBorderInsets(this).left,
                         wmBorder.getBorderInsets(this).top+WM_BD_GAP,
                         wmInteriorRect.width,
                         WM_BD_HEIGHT);
                 gw2.setColor(Color.black);
                 even = true;
                 for (int i = 0; i < blocks.size(); i++){
                     int first = ((int[])blocks.elementAt(i))[0];
                     int last = ((int[])blocks.elementAt(i))[((int[])blocks.elementAt(i)).length-1];
                     int voffset;
                     if (even){
                         voffset = 0;
                     }else{
                         voffset = WM_BD_HEIGHT/2;
                     }
                     gw2.fillRect(wmBorder.getBorderInsets(this).left+(int)(prefBoxSize*first),
                             wmBorder.getBorderInsets(this).top+voffset+WM_BD_GAP,
                             (int)((last-first+1)*prefBoxSize),
                             WM_BD_HEIGHT/2);
                     even = !even;
                 }
                 noImage = false;
             }
             wmResizeCorner = new Rectangle(visRect.x + worldmap.getWidth() - (worldmap.getWidth()-wmInteriorRect.width)/2,
                     visRect.y + visRect.height - worldmap.getHeight(),
                     (worldmap.getWidth()-wmInteriorRect.width)/2,
                     (worldmap.getHeight() -wmInteriorRect.height)/2);
 
             g2.drawImage(worldmap,visRect.x,
                     visRect.y + visRect.height - worldmap.getHeight(),
                     this);
             wmInteriorRect.x = visRect.x + (worldmap.getWidth() - wmInteriorRect.width);
             wmInteriorRect.y = visRect.y+visRect.height-worldmap.getHeight() +
                     (worldmap.getHeight() - wmInteriorRect.height);
 
             //draw the outline of the viewport
             g2.setColor(Color.black);
             double hRatio = wmInteriorRect.getWidth()/pref.getWidth();
             double vRatio = wmInteriorRect.getHeight()/pref.getHeight();
             int hBump = worldmap.getWidth()-wmInteriorRect.width;
             int vBump = worldmap.getHeight()-wmInteriorRect.height;
             //bump a few pixels to avoid drawing on the border
             g2.drawRect((int)(visRect.x*hRatio)+hBump/2+visRect.x,
                     (int)(visRect.y*vRatio)+vBump/2+(visRect.y + visRect.height - worldmap.getHeight()),
                     (int)(visRect.width*hRatio),
                     (int)(visRect.height*vRatio));
         }
 
         //see if we're drawing a worldmap resize rect
         if (resizeRectExists){
             g2.setColor(Color.black);
             g2.drawRect(resizeWMRect.x,
                     resizeWMRect.y,
                     resizeWMRect.width,
                     resizeWMRect.height);
         }
 
         //see if we're drawing a block selector rect
         if (blockRectExists){
             g2.setColor(Color.black);
             g2.setStroke(dashedThinStroke);
             g2.drawRect(blockRect.x, blockRect.y,
                     blockRect.width, blockRect.height);
         }
     }
 
     public Dimension getPreferredSize() {
         //loop through table to find deepest non-null comparison
         PairwiseLinkage[][] dPrimeTable = theData.filteredDPrimeTable;
         int upLim, loLim;
         if (forExport){
             loLim = exportStart;
             upLim = exportStop;
         }else{
             loLim = 0;
             upLim = dPrimeTable.length;
         }
         int count = 0;
         for (int x = loLim; x < upLim-1; x++){
             for (int y = x+1; y < upLim; y++){
                 if (dPrimeTable[x][y] != null){
                     if (count < y-x){
                         count = y-x;
                     }
                 }
             }
         }
         //add one so we don't clip bottom box
         count ++;
 
         Graphics g = this.getGraphics();
         g.setFont(markerNameFont);
         FontMetrics fm = g.getFontMetrics();
         if (printDetails){
             blockDispHeight = boxSize/3 + fm.getAscent();
         }else{
             blockDispHeight = boxSize/3;
         }
 
         int high = 2*V_BORDER + count*boxSize/2 + blockDispHeight;
         chartSize = new Dimension(2*H_BORDER + boxSize*(upLim-1),high);
         //this dimension is just the area taken up by the dprime chart
         //it is used in drawing the worldmap
 
         if (theData.infoKnown){
             infoHeight = TICK_HEIGHT + TICK_BOTTOM + widestMarkerName + TEXT_GAP;
             high += infoHeight;
         }else{
             infoHeight=0;
         }
         int wide = 2*H_BORDER + boxSize*(upLim-loLim-1);
         Rectangle visRect = getVisibleRect();
         //big datasets often scroll way offscreen in zoom-out mode
         //but aren't the full height of the viewport
         if (high < visRect.height && showWM){
             high = visRect.height;
         }
         return new Dimension(wide, high);
     }
 
     public void mouseClicked(MouseEvent e) {
         if ((e.getModifiers() & InputEvent.BUTTON1_MASK) ==
                 InputEvent.BUTTON1_MASK) {
             int clickX = e.getX();
             int clickY = e.getY();
             if (showWM && wmInteriorRect.contains(clickX,clickY)){
                 //convert a click on the worldmap to a point on the big picture
                 int bigClickX = (((clickX - getVisibleRect().x - (worldmap.getWidth()-wmInteriorRect.width)/2)
                         * chartSize.width) /
                         wmInteriorRect.width)-getVisibleRect().width/2;
                 int bigClickY = (((clickY - getVisibleRect().y -
                         (worldmap.getHeight() - wmInteriorRect.height)/2 -
                         (getVisibleRect().height-worldmap.getHeight())) *
                         chartSize.height) / wmInteriorRect.height) -
                         getVisibleRect().height/2 + infoHeight;
 
                 //System.out.println(chartSize.height);
                 //if the clicks are near the edges, correct values
                 if (bigClickX > chartSize.width - getVisibleRect().width){
                     bigClickX = chartSize.width - getVisibleRect().width;
                 }
                 if (bigClickX < 0){
                     bigClickX = 0;
                 }
                 if (bigClickY > chartSize.height - getVisibleRect().height + infoHeight){
                     bigClickY = chartSize.height - getVisibleRect().height + infoHeight;
                 }
                 if (bigClickY < 0){
                     bigClickY = 0;
                 }
 
                 ((JViewport)getParent()).setViewPosition(new Point(bigClickX,bigClickY));
             }else{
                 theHV.blockMenuItems[BLOX_CUSTOM].setSelected(true);
                 Rectangle blockselector = new Rectangle(clickXShift-boxRadius,clickYShift - boxRadius,
                         (Chromosome.getFilteredSize()*boxSize), boxSize);
                 if(blockselector.contains(clickX,clickY)){
                     int whichMarker = (int)(0.5 + (double)((clickX - clickXShift))/boxSize);
                     if (theData.isInBlock[whichMarker]){
                         theData.removeFromBlock(whichMarker);
                         refresh();
                     } else if (whichMarker > 0 && whichMarker < Chromosome.realIndex.length){
                         theData.addMarkerIntoSurroundingBlock(whichMarker);
                     }
                 }
             }
         }
     }
 
     public void mousePressed (MouseEvent e) {
         Rectangle blockselector = new Rectangle(clickXShift-boxRadius,clickYShift - boxRadius,
                 (Chromosome.getFilteredSize()*boxSize), boxSize);
 
         //if users right clicks & holds, pop up the info
         if ((e.getModifiers() & InputEvent.BUTTON3_MASK) ==
                 InputEvent.BUTTON3_MASK){
             Graphics g = getGraphics();
             g.setFont(boxFont);
             FontMetrics metrics = g.getFontMetrics();
             PairwiseLinkage[][] dPrimeTable = theData.filteredDPrimeTable;
             final int clickX = e.getX();
             final int clickY = e.getY();
             double dboxX = (double)(clickX - clickXShift - (clickY-clickYShift))/boxSize;
             double dboxY = (double)(clickX - clickXShift + (clickY-clickYShift))/boxSize;
             final int boxX, boxY;
             if (dboxX < 0){
                 boxX = (int)(dboxX - 0.5);
             } else{
                 boxX = (int)(dboxX + 0.5);
             }
             if (dboxY < 0){
                 boxY = (int)(dboxY - 0.5);
             }else{
                 boxY = (int)(dboxY + 0.5);
             }
             if ((boxX >= lowX && boxX <= highX) &&
                     (boxY > boxX && boxY < highY) &&
                     !(wmInteriorRect.contains(clickX,clickY))){
                 if (dPrimeTable[boxX][boxY] != null){
                     if (theData.infoKnown){
                         displayStrings = new String[6];
                         displayStrings[0] = new String ("(" +Chromosome.getFilteredMarker(boxX).getName() +
                                 ", " + Chromosome.getFilteredMarker(boxY).getName() + ")");
                         int sep = (int)((Chromosome.getFilteredMarker(boxY).getPosition() -
                                 Chromosome.getFilteredMarker(boxX).getPosition())/1000);
                         displayStrings[5] = new Long(sep).toString() + " kb";
                     }else{
                         displayStrings = new String[5];
                         displayStrings[0] = new String("(" + (Chromosome.realIndex[boxX]+1) + ", " +
                                 (Chromosome.realIndex[boxY]+1) + ")");
                     }
                     displayStrings[1] = new String ("D': " + dPrimeTable[boxX][boxY].getDPrime());
                     displayStrings[2] = new String ("LOD: " + dPrimeTable[boxX][boxY].getLOD());
                     displayStrings[3] = new String ("r^2: " + dPrimeTable[boxX][boxY].getRSquared());
                     displayStrings[4] = new String ("D' conf. bounds: " +
                             dPrimeTable[boxX][boxY].getConfidenceLow() + "-" +
                             dPrimeTable[boxX][boxY].getConfidenceHigh());
                     popupExists = true;
                 }
             } else if (blockselector.contains(clickX, clickY)){
                 int marker = (int)(0.5 + (double)((clickX - clickXShift))/boxSize);
                 displayStrings = new String[2];
                 if (theData.infoKnown){
                     displayStrings[0] = new String (Chromosome.getFilteredMarker(marker).getName());
                 }else{
                     displayStrings[0] = new String("Marker " + (Chromosome.realIndex[marker]+1));
                 }
                 displayStrings[1] = new String ("MAF: " + Chromosome.getFilteredMarker(marker).getMAF());
                 popupExists = true;
             }
             if (popupExists){
                 int strlen = 0;
                 for (int x = 0; x < displayStrings.length; x++){
                     if (strlen < metrics.stringWidth(displayStrings[x])){
                         strlen = metrics.stringWidth(displayStrings[x]);
                     }
                 }
                 //edge shifts prevent window from popping up partially offscreen
                 int visRightBound = (int)(getVisibleRect().getWidth() + getVisibleRect().getX());
                 int visBotBound = (int)(getVisibleRect().getHeight() + getVisibleRect().getY());
                 int rightEdgeShift = 0;
                 if (clickX + strlen + popupLeftMargin +5 > visRightBound){
                     rightEdgeShift = clickX + strlen + popupLeftMargin + 10 - visRightBound;
                 }
                 int botEdgeShift = 0;
                 if (clickY + 5*metrics.getHeight()+10 > visBotBound){
                     botEdgeShift = clickY + 5*metrics.getHeight()+15 - visBotBound;
                 }
                 int smallDataVertSlop = 0;
                 if (getPreferredSize().getWidth() < getVisibleRect().width && theData.infoKnown){
                     smallDataVertSlop = (int)(getVisibleRect().height - getPreferredSize().getHeight())/2;
                 }
                 popupDrawRect = new Rectangle(clickX-rightEdgeShift,
                         clickY-botEdgeShift+smallDataVertSlop,
                         strlen+popupLeftMargin+5,
                         displayStrings.length*metrics.getHeight()+10);
                 repaint();
             }
         }else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) ==
                 InputEvent.BUTTON1_MASK){
             int x = e.getX();
             int y = e.getY();
             if (blockselector.contains(x,y)){
                 setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                 blockStartX = x;
             }
         }
     }
 
     public void mouseReleased(MouseEvent e) {
         //remove popped up window
         if ((e.getModifiers() & InputEvent.BUTTON3_MASK) ==
                 InputEvent.BUTTON3_MASK){
             popupExists = false;
             repaint();
         //resize window once user has ceased dragging
         } else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) ==
                 InputEvent.BUTTON1_MASK){
             if (getCursor() == Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)){
                 resizeRectExists = false;
                 noImage = true;
                 if (resizeWMRect.width > 20){
                     wmMaxWidth = resizeWMRect.width;
                 }
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 repaint();
             }
             if (getCursor() == Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)){
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 blockRectExists = false;
                 int firstMarker = (int)(0.5 + (double)((blockStartX - clickXShift))/boxSize);
                 int lastMarker = (int)(0.5 + (double)((e.getX() - clickXShift))/boxSize);
                 if (firstMarker > lastMarker){
                     int temp = firstMarker;
                     firstMarker = lastMarker;
                     lastMarker = temp;
                 }
                 theData.addBlock(firstMarker, lastMarker);
                 refresh();
             }
         }
     }
 
     public void mouseDragged(MouseEvent e) {
         if ((e.getModifiers() & InputEvent.BUTTON1_MASK) ==
                 InputEvent.BUTTON1_MASK) {
             //conveniently, we can tell what do do with the drag event
             //based on what the cursor is
             if (getCursor() == Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)){
                 int width = e.getX() - wmInteriorRect.x;
                 double ratio = (double)width/(double)worldmap.getWidth();
                 int height = (int)(ratio*worldmap.getHeight());
 
                 resizeWMRect = new Rectangle(wmInteriorRect.x+1,
                         wmInteriorRect.y + wmInteriorRect.height - height,
                         width,
                         height-1);
                 resizeRectExists = true;
                 repaint();
             }else if (getCursor() == Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)){
                 theHV.blockMenuItems[BLOX_CUSTOM].setSelected(true);
                 int xcorner,width;
                 if (e.getX() < blockStartX){
                     //we're dragging right to left, so flip it.
                     xcorner = e.getX() - clickXShift + left;
                     width =  blockStartX - e.getX();
                 }else{
                     xcorner = blockStartX - clickXShift + left;
                     width = e.getX() - blockStartX;
                 }
                 blockRect = new Rectangle(xcorner, top - boxRadius/2 - TEXT_GAP,
                         width,boxRadius);
                 blockRectExists=true;
                 repaint();
             }
         }
     }
 
     public void mouseMoved(MouseEvent e){
         //when the user mouses over the corner of the worldmap, change the cursor
         //to the resize cursor
         if (getCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)){
             if (wmResizeCorner.contains(e.getPoint())){
                 setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
             }
         } else if (getCursor() == Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)){
             if (!(wmResizeCorner.contains(e.getPoint()))){
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             }
         }
     }
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseExited(MouseEvent e) {
     }
 }
 
