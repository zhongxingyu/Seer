 package edu.mit.wi.haploview;
 
 import java.awt.*;
 import java.awt.geom.GeneralPath;
 import java.awt.image.BufferedImage;
 import java.awt.event.*;
 import java.util.Vector;
 import javax.swing.*;
 import javax.swing.border.CompoundBorder;
 
 class DPrimeDisplay extends JComponent{
     private static final int H_BORDER = 15;
     private static final int V_BORDER = 15;
     private static final int TEXT_NUMBER_GAP = 3;
 
     private static final int DEFAULT_BOX_SIZE = 50;
     private static final int DEFAULT_BOX_RADIUS = 24;
     private static final int TICK_HEIGHT = 8;
     private static final int TICK_BOTTOM = 50;
 
     private int widestMarkerName = 80; //default size
     private int infoHeight = 0;
     private int boxSize = DEFAULT_BOX_SIZE;
     private int boxRadius = DEFAULT_BOX_RADIUS;
     private int lowX, highX, lowY, highY;
     private int left, top, clickXShift, clickYShift;
 
     private Font boxFont = new Font("SansSerif", Font.PLAIN, 12);
     private Font markerNumFont = new Font("SansSerif", Font.BOLD, 12);
     private Font markerNameFont = new Font("Default", Font.PLAIN, 12);
 
     private boolean markersLoaded;
     private boolean printDetails = true;
     private boolean noImage = true;
 
     private Vector blocks;
     private Rectangle ir = new Rectangle();
     private Rectangle worldmapRect = new Rectangle(0,0,-1,-1);
     private BufferedImage worldmap;
     PairwiseLinkage dPrimeTable[][];
     private Dimension chartSize;
 
     DPrimeDisplay(PairwiseLinkage[][] t, boolean b, Vector v){
         markersLoaded = b;
         dPrimeTable = t;
         blocks = v;
         this.setDoubleBuffered(true);
         addMouseListener(new PopMouseListener(this));
     }
 
     public void loadMarkers(){
         noImage = true;
         markersLoaded = true;
         repaint();
     }
 
     public void paintComponent(Graphics g){
         Graphics2D g2 = (Graphics2D) g;
         Dimension size = getSize();
         Dimension pref = getPreferredSize();
         Rectangle visRect = getVisibleRect();
         /*
         boxSize = ((clipRect.width-2*H_BORDER)/dPrimeTable.length-1);
         if (boxSize < 12){boxSize=12;}
         if (boxSize < 25){
         printDetails = false;
         boxRadius = boxSize/2;
         }else{
         boxRadius = boxSize/2 - 1;
         }
         */
 
         //okay so this dumb if block is to prevent the ugly repainting
         //bug when loading markers after the data are already being displayed,
         //results in a little off-centering for small datasets, but not too bad.
         //clickxshift and clickyshift are used later to translate from x,y coords
         //to the pair of markers comparison at those coords
         if (!(markersLoaded)){
             g2.translate((size.width - pref.width) / 2,
                     (size.height - pref.height) / 2);
             clickXShift = left + (size.width-pref.width)/2;
             clickYShift = top + (size.height - pref.height)/2;
         } else {
             g2.translate((size.width - pref.width) / 2,
                     0);
             clickXShift = left + (size.width-pref.width)/2;
             clickYShift = top;
         }
 
         FontMetrics boxFontMetrics = g.getFontMetrics(boxFont);
 
         int diamondX[] = new int[4];
         int diamondY[] = new int[4];
         Polygon diamond;
 
         left = H_BORDER;
         top = V_BORDER;
 
         FontMetrics metrics;
         int ascent;
 
         g2.setColor(this.getBackground());
         g2.fillRect(0,0,pref.width,pref.height);
         g2.setColor(Color.BLACK);
 
         BasicStroke thickerStroke = new BasicStroke(1);
         BasicStroke thinnerStroke = new BasicStroke(0.25f);
         BasicStroke fatStroke = new BasicStroke(3.0f);
 
         if (markersLoaded) {
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);
 
             //// draw the marker locations
 
             int wide = (dPrimeTable.length-1) * boxSize;
             //TODO: talk to kirby about locusview scaling gizmo
             int lineLeft = wide/20;
             int lineSpan = (wide/10)*9;
            long minpos = Chromosome.getFilteredMarker(0).getPosition();
            long maxpos = Chromosome.getFilteredMarker(Chromosome.getSize()-1).getPosition();
             double spanpos = maxpos - minpos;
             g2.setStroke(thinnerStroke);
             g2.setColor(Color.white);
             g2.fillRect(left + lineLeft, 5, lineSpan, TICK_HEIGHT);
             g2.setColor(Color.black);
             g2.drawRect(left + lineLeft, 5, lineSpan, TICK_HEIGHT);
 
            for (int i = 0; i < Chromosome.getSize(); i++) {
                 double pos = (Chromosome.getFilteredMarker(i).getPosition() - minpos) / spanpos;
                 int xx = (int) (left + lineLeft + lineSpan*pos);
                 g2.setStroke(thickerStroke);
                 g.drawLine(xx, 5, xx, 5 + TICK_HEIGHT);
                 g2.setStroke(thinnerStroke);
                 g.drawLine(xx, 5 + TICK_HEIGHT,
                         left + i*boxSize, TICK_BOTTOM);
             }
             top += TICK_BOTTOM;
             //// draw the marker names
             if (printDetails){
                 g.setFont(markerNameFont);
                 metrics = g.getFontMetrics();
                 ascent = metrics.getAscent();
 
                 widestMarkerName = metrics.stringWidth(Chromosome.getFilteredMarker(0).getName());
                 for (int x = 1; x < dPrimeTable.length; x++) {
                     int thiswide = metrics.stringWidth(Chromosome.getFilteredMarker(x).getName());
                     if (thiswide > widestMarkerName) widestMarkerName = thiswide;
                 }
 
                 g2.translate(left, top + widestMarkerName);
                 g2.rotate(-Math.PI / 2.0);
                 for (int x = 0; x < dPrimeTable.length; x++) {
                     g2.drawString(Chromosome.getFilteredMarker(x).getName(),TEXT_NUMBER_GAP, x*boxSize + ascent/3);
                 }
 
                 g2.rotate(Math.PI / 2.0);
                 g2.translate(-left, -(top + widestMarkerName));
 
                 // move everybody down
                 top += widestMarkerName + TEXT_NUMBER_GAP;
             }
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_OFF);
         }
 
         //// draw the marker numbers
         if (printDetails){
             g.setFont(markerNumFont);
             metrics = g.getFontMetrics();
             ascent = metrics.getAscent();
 
             for (int x = 0; x < dPrimeTable.length; x++) {
                 String mark = String.valueOf(Chromosome.realIndex[x] + 1);
                 g.drawString(mark,
                         left + x*boxSize - metrics.stringWidth(mark)/2,
                         top + ascent);
             }
             top += boxRadius/2; // give a little space between numbers and boxes
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
                 g.setColor(boxColor);
                 g.fillPolygon(diamond);
                 if (boxColor == Color.white) {
                     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
                     g.setColor(Color.lightGray);
                     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_OFF);
                 }
 
                 if(printDetails){
                     g.setFont(boxFont);
                     ascent = boxFontMetrics.getAscent();
                     int val = (int) (d * 100);
                     g.setColor((val < 50) ? Color.gray : Color.black);
                     if (val != 100) {
                         String valu = String.valueOf(val);
                         int widf = boxFontMetrics.stringWidth(valu);
                         g.drawString(valu, xx - widf/2, yy + ascent/2);
                     }
                 }
             }
         }
 
         //highlight blocks
         boolean even = true;
         //g.setColor(new Color(153,255,153));
         g.setColor(new Color(51,153,51));
         g2.setStroke(fatStroke);
         for (int i = 0; i < blocks.size(); i++){
             int[] theBlock = (int[])blocks.elementAt(i);
             int first = theBlock[0];
             int last = theBlock[theBlock.length-1];
             g.drawLine(left + (2*first + 1) * boxSize/2 - boxRadius,
                     top + boxSize/2,
                     left + (first + last) * boxSize/2,
                     top + (last - first) * boxSize/2 + boxRadius);
             g.drawLine(left + (first + last) * boxSize/2,
                     top + (last - first) * boxSize/2 + boxRadius,
                     left + (2*last - 1) * boxSize/2+boxRadius,
                     top + boxSize/2);
             for (int j = 0; j < theBlock.length; j++){
                 g.drawLine(left + (2*theBlock[j]+1) * boxSize/2 - boxRadius,
                         top + boxSize/2,
                         left + (2*theBlock[j]+1) * boxSize/2,
                         top + boxSize/2 - boxRadius);
                 g.drawLine (left + (2*theBlock[j]) * boxSize/2 - boxRadius,
                         top + boxSize/2 - boxRadius,
                         left + (2*theBlock[j]) * boxSize/2,
                         top + boxSize/2);
             }
         }
         g2.setStroke(thickerStroke);
 
         if (pref.getWidth() > (2*visRect.width)){
             //dataset is big enough to require worldmap
             if (noImage){
                 //first time through draw a worldmap if dataset is big:
                 final int WM_MAX_WIDTH = visRect.width/3;
                 double scalefactor;
                 scalefactor = (double)(chartSize.width)/WM_MAX_WIDTH;
 
                 CompoundBorder wmBorder = new CompoundBorder(BorderFactory.createRaisedBevelBorder(),
                         BorderFactory.createLoweredBevelBorder());
                 worldmap = new BufferedImage((int)(chartSize.width/scalefactor)+wmBorder.getBorderInsets(this).left*2,
                         (int)(chartSize.height/scalefactor)+wmBorder.getBorderInsets(this).top*2,
                         BufferedImage.TYPE_3BYTE_BGR);
 
                 Graphics gw = worldmap.getGraphics();
                 Graphics2D gw2 = (Graphics2D)(gw);
                 gw2.setColor(this.getBackground());
                 gw2.fillRect(1,1,worldmap.getWidth()-2,worldmap.getHeight()-2);
                 //make a pretty border
                 gw2.setColor(Color.BLACK);
 
                 wmBorder.paintBorder(this,gw2,0,0,worldmap.getWidth()-1,worldmap.getHeight()-1);
                 ir = wmBorder.getInteriorRectangle(this,0,0,worldmap.getWidth()-1, worldmap.getHeight()-1);
 
                 double prefBoxSize = boxSize/scalefactor;
                 float[] smallDiamondX = new float[4];
                 float[] smallDiamondY = new float[4];
                 GeneralPath gp;
                 for (int x = 0; x < dPrimeTable.length-1; x++){
                     for (int y = x+1; y < dPrimeTable.length; y++){
                         if (dPrimeTable[x][y] == null){
                             continue;
                         }
                         double xx = (x + y)*prefBoxSize/2+wmBorder.getBorderInsets(this).left;
                         double yy = (y - x)*prefBoxSize/2+wmBorder.getBorderInsets(this).top;
 
 
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
                 noImage = false;
             }
             g.drawImage(worldmap,visRect.x,
                     visRect.y + visRect.height - worldmap.getHeight(),
                     this);
             worldmapRect = new Rectangle(visRect.x,
                     visRect.y+visRect.height-worldmap.getHeight(),
                     worldmap.getWidth(),
                     worldmap.getHeight());
 
             //draw the outline of the viewport
             g.setColor(Color.BLACK);
             double hRatio = ir.getWidth()/pref.getWidth();
             double vRatio = ir.getHeight()/pref.getHeight();
             int hBump = worldmap.getWidth()-ir.width;
             int vBump = worldmap.getHeight()-ir.height;
             //bump a few pixels to avoid drawing on the border
             g.drawRect((int)(visRect.x*hRatio)+hBump/2+visRect.x,
                     (int)(visRect.y*vRatio)+vBump/2+(visRect.y + visRect.height - worldmap.getHeight()),
                     (int)(visRect.width*hRatio),
                     (int)(visRect.height*vRatio));
         }
     }
 
 
     public Dimension getPreferredSize() {
         //loop through table to find deepest non-null comparison
         int count = 0;
         for (int x = 0; x < dPrimeTable.length-1; x++){
            for (int y = x+1; y < dPrimeTable.length; y++){
                if (dPrimeTable[x][y] != null){
                  if (count < y-x){
                      count = y-x;
                  }
                }
            }
         }
         //add one so we don't clip bottom box
         count ++;
 
         int high = 2*V_BORDER + count*boxSize/2;
         chartSize = new Dimension(2*H_BORDER + boxSize*(dPrimeTable.length-1),high);
         //this dimension is just the area taken up by the dprime chart
         //it is used in drawing the worldmap
 
         if (markersLoaded){
             infoHeight = TICK_BOTTOM + widestMarkerName + TEXT_NUMBER_GAP;
             high += infoHeight;
         }else{
             infoHeight=0;
         }
         return new Dimension(2*H_BORDER + boxSize*(dPrimeTable.length-1), high);
     }
 
 
     int[] centerString(String s, FontMetrics fm) {
         int[] returnArray = new int[2];
         returnArray[0] = (30-fm.stringWidth(s))/2;
         returnArray[1] = 10+(30-fm.getAscent())/2;
         return returnArray;
     }
 
     public void refreshBlocks(Vector v) {
         //recolor the worldmap and change the blocklist
         noImage=true;
         blocks = v;
     }
 
 
     class PopMouseListener implements MouseListener{
         JComponent caller;
         public PopMouseListener(DPrimeDisplay d){
               caller = d;
         }
 
         public void mouseClicked(MouseEvent e) {
             if ((e.getModifiers() & InputEvent.BUTTON1_MASK) ==
                     InputEvent.BUTTON1_MASK) {
                 int clickX = e.getX();
                 int clickY = e.getY();
                 if (worldmapRect.contains(clickX,clickY)){
                     //convert a click on the worldmap to a point on the big picture
                     int bigClickX = (((clickX - caller.getVisibleRect().x) * chartSize.width) /
                             worldmap.getWidth())-caller.getVisibleRect().width/2;
                     int bigClickY = (((clickY - caller.getVisibleRect().y -
                             (caller.getVisibleRect().height-worldmap.getHeight())) *
                             chartSize.height) / worldmap.getHeight()) -
                             caller.getVisibleRect().height/2 + infoHeight;
 
                     //if the clicks are near the edges, correct values
                     if (bigClickX > chartSize.width - caller.getVisibleRect().width){
                         bigClickX = chartSize.width - caller.getVisibleRect().width;
                     }
                     if (bigClickX < 0){
                         bigClickX = 0;
                     }
                     if (bigClickY > chartSize.height - caller.getVisibleRect().height + infoHeight){
                         bigClickY = chartSize.height - caller.getVisibleRect().height + infoHeight;
                     }
                     if (bigClickY < 0){
                         bigClickY = 0;
                     }
 
                     ((JViewport)caller.getParent()).setViewPosition(new Point(bigClickX,bigClickY));
                 }
             }
         }
 
         public void mousePressed (MouseEvent e) {
             if ((e.getModifiers() & InputEvent.BUTTON3_MASK) ==
                     InputEvent.BUTTON3_MASK){
 
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
                         !(worldmapRect.contains(clickX,clickY))){
                     if (dPrimeTable[boxX][boxY] != null){
                         final SwingWorker worker = new SwingWorker(){
                             public Object construct(){
                                 final int leftMargin = 12;
                                 String[] displayStrings = new String[5];
                                 if (markersLoaded){
                                     displayStrings[0] = new String ("(" +Chromosome.getFilteredMarker(boxX).getName() +
                                             ", " + Chromosome.getFilteredMarker(boxY).getName() + ")");
                                 }else{
                                     displayStrings[0] = new String("(" + (Chromosome.realIndex[boxX]+1) + ", " +
                                             (Chromosome.realIndex[boxY]+1) + ")");
                                 }
                                 displayStrings[1] = new String ("D': " + dPrimeTable[boxX][boxY].getDPrime());
                                 displayStrings[2] = new String ("LOD: " + dPrimeTable[boxX][boxY].getLOD());
                                 displayStrings[3] = new String ("r^2: " + dPrimeTable[boxX][boxY].getRSquared());
                                 displayStrings[4] = new String ("D' conf. bounds: " +
                                         dPrimeTable[boxX][boxY].getConfidenceLow() + "-" +
                                         dPrimeTable[boxX][boxY].getConfidenceHigh());
                                 Graphics g = caller.getGraphics();
                                 g.setFont(boxFont);
                                 FontMetrics metrics = g.getFontMetrics();
                                 int strlen = 0;
                                 for (int x = 0; x < 5; x++){
                                     if (strlen < metrics.stringWidth(displayStrings[x])){
                                         strlen = metrics.stringWidth(displayStrings[x]);
                                     }
                                 }
 
                                 //edge shifts prevent window from popping up partially offscreen
                                 int visRightBound = (int)(caller.getVisibleRect().getWidth() + caller.getVisibleRect().getX());
                                 int visBotBound = (int)(caller.getVisibleRect().getHeight() + caller.getVisibleRect().getY());
                                 int rightEdgeShift = 0;
                                 if (clickX + strlen + leftMargin +5 > visRightBound){
                                     rightEdgeShift = clickX + strlen + leftMargin + 10 - visRightBound;
                                 }
                                 int botEdgeShift = 0;
                                 if (clickY + 5*metrics.getHeight()+10 > visBotBound){
                                     botEdgeShift = clickY + 5*metrics.getHeight()+15 - visBotBound;
                                 }
 
                                 g.setColor(Color.WHITE);
                                 g.fillRect(clickX+1-rightEdgeShift,
                                         clickY+1-botEdgeShift,
                                         strlen+leftMargin+4,
                                         5*metrics.getHeight()+9);
                                 g.setColor(Color.BLACK);
                                 g.drawRect(clickX-rightEdgeShift,
                                         clickY-botEdgeShift,
                                         strlen+leftMargin+5,
                                         5*metrics.getHeight()+10);
 
                                 for (int x = 0; x < 5; x++){
                                     g.drawString(displayStrings[x],clickX + leftMargin - rightEdgeShift,
                                             clickY+5+((x+1)*metrics.getHeight())-botEdgeShift);
                                 }
                                 return "";
                             }
                         };
                         worker.start();
                     }
                 }
             }
         }
 
         public void mouseReleased(MouseEvent e) {
             if ((e.getModifiers() & InputEvent.BUTTON3_MASK) ==
                     InputEvent.BUTTON3_MASK){
                 caller.repaint();
             }
         }
 
         public void mouseEntered(MouseEvent e) {
         }
 
         public void mouseExited(MouseEvent e) {
         }
     }
 }
 
