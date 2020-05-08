 package edu.mit.wi.haploview;
 import java.awt.*;
 //import java.awt.geom.*;
 import java.awt.image.*;
 import java.io.*;
 import javax.swing.*;
 import java.text.*;
 import java.util.*;
 
 public class HaplotypeDisplay extends JComponent {
 
     Haplotype[][] orderedHaplos;
     Haplotype[][] filteredHaplos;
     HaploData theData;
 
     double multidprimeArray[];
     int missingLimit = 5;
     boolean useThickness = true;
     int displayThresh = 1;
     int thinThresh = 1;
     int thickThresh = 10;
     private boolean forExport = false;
     public int alleleDisp;
     private Color dullRed = new Color(204,51,51);
     private Color dullBlue = new Color(51,51,204);
 
 
     public HaplotypeDisplay(HaploData h) throws HaploViewException {
 
         theData=h;
         if (blackNumImages == null) loadFontImages();
         getHaps();
 
         //    int[][] bk = parent.blockController.blocks;
         //Vector blockVector = new Vector();
         //for (int i = 0; i < bk.length; i++) {
         //  blockVector.add(bk[i]);
         // }
     }
 
     public Image export(Image i){
         forExport = true;
         paintComponent(i.getGraphics());
         forExport = false;
         return i;
     }
 
     public void getHaps() throws HaploViewException{
         if (theData.blocks == null) {return;}
 
         Haplotype[][] haplos = theData.generateHaplotypes(theData.blocks, 0);
 
         orderedHaplos = new Haplotype[haplos.length][];
         for (int i = 0; i < haplos.length; i++) {
             Vector orderedHaps = new Vector();
             //step through each haplotype in this block
             for (int hapCount = 0; hapCount < haplos[i].length; hapCount++) {
                 if (orderedHaps.size() == 0) {
                     orderedHaps.add(haplos[i][hapCount]);
                 } else {
                     for (int j = 0; j < orderedHaps.size(); j++) {
                         if (((Haplotype)(orderedHaps.elementAt(j))).getPercentage() <
                                 haplos[i][hapCount].getPercentage()) {
                             orderedHaps.add(j, haplos[i][hapCount]);
                             break;
                         }
                         if ((j+1) == orderedHaps.size()) {
                             orderedHaps.add(haplos[i][hapCount]);
                             break;
                         }
                     }
                 }
             }
             orderedHaplos[i] = new Haplotype[orderedHaps.size()];
             orderedHaps.copyInto(orderedHaplos[i]);
         }
         adjustDisplay(displayThresh);
     }
 
 
     static Image charImages[];
     static Image blackNumImages[];
     static Image grayNumImages[];
     static Image markerNumImages[];
 
     static final int ROW_HEIGHT = 20;
     static final int CHAR_WIDTH = 10;
     static final int CHAR_HEIGHT = 16;
     static final int MARKER_CHAR_WIDTH = 6;
     static final int MARKER_CHAR_HEIGHT = 9;
 
     // amount of room for lines between things
     final int LINE_LEFT = 3;
     final int LINE_RIGHT = 34;
     final int LINE_SPAN = 37;
 
     // border around entire image
     static final int BORDER = 10;
 
     // tag diamond
     static Image tagImage; // li'l pointy arrow
     static final int TAG_SPAN = 10;
 
     // load fake font as images, since java font stuff is so lousy
     protected void loadFontImages() {
         // read images from file
         Toolkit tk = Toolkit.getDefaultToolkit();
         byte data[] = null;
 
         try {
             InputStream is = getClass().getResourceAsStream("orator.raw");
             BufferedInputStream bis = new BufferedInputStream(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream();
 
             int c = bis.read();
             while (c != -1) {
                 out.write(c);
                 c = bis.read();
             }
             //return out.toByteArray();
             data = out.toByteArray();
 
         } catch (IOException e) {
             System.err.println("error loading font data from orator.raw");
             e.printStackTrace();
             System.exit(1);
         }
 
         //byte data[] = loadBytes("orator1675.raw");
         int pixels[] = new int[data.length];
         for (int i = 0; i < data.length; i++) {
             // image values are the alpha of the image
             // image itself would be black
             pixels[i] = ((255 - (data[i] & 0xff)) << 24) | 0x000000;
         }
         MemoryImageSource mis;
 
         // top data is 110x48 image
         // lines are 16 pixels high, each char 10 pixels wide
         // the fifth 'char' is actually the little down triangle for tags
         // the eighth char is the 'unknown', which is an 8 in the data struct
         // (the acgt letters are 1234)
         // 6 and 7 are blank, but who cares, since it makes the code simpler
         // ACGT^  x
         // 012345679. (black)
         // 012345679. (gray)
 
         charImages = new Image[8];
         for (int i = 0; i < 8; i++) {
             mis = new MemoryImageSource(10, 16, pixels, i*CHAR_WIDTH, 110);
             charImages[i] = tk.createImage(mis);
         }
         tagImage = charImages[4];
 
         blackNumImages = new Image[11];
         grayNumImages = new Image[11];
         for (int i = 0; i < 11; i++) {
             mis = new MemoryImageSource(CHAR_WIDTH, CHAR_HEIGHT, pixels,
                     110*CHAR_HEIGHT + i*CHAR_WIDTH, 110);
             blackNumImages[i] = tk.createImage(mis);
 
             mis = new MemoryImageSource(CHAR_WIDTH, CHAR_HEIGHT, pixels,
                     110*CHAR_HEIGHT*2 + i*CHAR_WIDTH, 110);
             grayNumImages[i] = tk.createImage(mis);
         }
 
         // tag nums are rotated in the file (as they are used in the vis)
         // so they start at 9 and go down to 0
         markerNumImages = new Image[10];
         int offset = 110 * CHAR_HEIGHT * 3;
         for (int i = 9; i >= 0; --i) {
             mis = new MemoryImageSource(MARKER_CHAR_HEIGHT,
                     MARKER_CHAR_WIDTH, pixels,
                     offset + MARKER_CHAR_WIDTH*110*(9-i), 110);
             markerNumImages[i] = tk.createImage(mis);
         }
     }
 
     public void adjustDisplay(int dt){
         //this is called when the controller wants to change the haps
         //displayed, instead of directly repainting so that none of this math
         //is done when the screen repaints for other reasons (resizing, focus change, etc)
 
 
         //first filter haps on displaythresh
         Haplotype[][] filts;
         filts = new Haplotype[orderedHaplos.length][];
         int numhaps = 0;
         int printable = 0;
         for (int i = 0; i < orderedHaplos.length; i++){
             Vector tempVector = new Vector();
             for (int j = 0; j < orderedHaplos[i].length; j++){
                 if (orderedHaplos[i][j].getPercentage()*100 > dt){
                     tempVector.add(orderedHaplos[i][j]);
                     numhaps++;
                 }
             }
             if (numhaps > 1){
                 printable++; numhaps=0;
             }
             filts[i] = new Haplotype[tempVector.size()];
             tempVector.copyInto(filts[i]);
         }
 
         // if user sets display thresh higher than most common hap in any given block
         if (!(printable == filts.length)){
             JOptionPane.showMessageDialog(this.getParent(),
                     "Error: At least one block has too few haplotypes of frequency > " + dt,
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         displayThresh = dt;
         filteredHaplos = filts;
         //then re-tag
         try{
             filteredHaplos = theData.generateCrossovers(filteredHaplos);
         }catch (HaploViewException e){
                     JOptionPane.showMessageDialog(this.getParent(),
                     e.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         multidprimeArray = theData.getMultiDprime();
 
         repaint();
     }
 
 
     public Dimension getPreferredSize() {
         int wide = BORDER*2;
         int high = BORDER*2;
         if (filteredHaplos == null) {return new Dimension(wide, high);}
 
         // height for the marker digits
         int markerDigits =
                 (int) (0.0000001 + Math.log(Chromosome.getSize()) / Math.log(10)) + 1;
         high += MARKER_CHAR_WIDTH * markerDigits;
 
         // space for the diamond
         high += TAG_SPAN;
 
         int maxh = 0;
         for (int i = 0; i < filteredHaplos.length; i++){
             maxh = Math.max(filteredHaplos[i].length, maxh);
             // size of genotypes for this column
             // +4 for the percentage reading
             wide += (filteredHaplos[i][0].getGeno().length + 4) * CHAR_WIDTH;
             // percentage plus the line size
             if (i != 0) wide += LINE_SPAN;
         }
         // +1 because of extra row for multi between the columns
         high += (maxh + 1) * ROW_HEIGHT;
 
         return new Dimension(wide, high);
     }
 
 
     public void paintComponent(Graphics graphics) {
 
         if (filteredHaplos == null){
             super.paintComponent(graphics);
             return;
         }
 
         Graphics2D g = (Graphics2D) graphics;
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
 
         //System.out.println(getSize());
         Dimension size = getSize();
         Dimension pref = getPreferredSize();
         g.setColor(this.getBackground());
         g.fillRect(0,0,pref.width, pref.height);
 
         if (!forExport){
             g.translate((size.width - pref.width) / 2,
                     (size.height - pref.height) / 2);
         }
 
         //g.drawRect(0, 0, pref.width, pref.height);
 
         final BasicStroke thinStroke = new BasicStroke(0.5f);
         final BasicStroke thickStroke = new BasicStroke(2.0f);
 
         // width of one letter of the haplotype block
         //int letterWidth = haploMetrics.charWidth('G');
         //int percentWidth = pctMetrics.stringWidth(".000");
 
         //final int verticalOffset = 43; // room for tags and diamonds
         int left = BORDER;
         int top = BORDER; //verticalOffset;
         //int totalWidth = 0;
 
         // percentages for each haplotype
         NumberFormat nf = NumberFormat.getInstance(Locale.US);
         nf.setMinimumFractionDigits(3);
         nf.setMaximumFractionDigits(3);
         nf.setMinimumIntegerDigits(0);
         nf.setMaximumIntegerDigits(0);
 
         // multi reading, between the columns
         NumberFormat nfMulti = NumberFormat.getInstance(Locale.US);
         nfMulti.setMinimumFractionDigits(2);
         nfMulti.setMaximumFractionDigits(2);
         nfMulti.setMinimumIntegerDigits(0);
         nfMulti.setMaximumIntegerDigits(0);
 
         int[][] lookupPos = new int[filteredHaplos.length][];
         for (int p = 0; p < lookupPos.length; p++) {
             lookupPos[p] = new int[filteredHaplos[p].length];
             for (int q = 0; q < lookupPos[p].length; q++){
                 lookupPos[p][filteredHaplos[p][q].getListOrder()] = q;
             }
         }
 
         // set number formatter to pad with appropriate number of zeroes
         NumberFormat nfMarker = NumberFormat.getInstance(Locale.US);
         int markerCount = Chromosome.getSize();
         // the +0.0000001 is because there is
         // some suckage where log(1000) / log(10) isn't actually 3
         int markerDigits =
                 (int) (0.0000001 + Math.log(markerCount) / Math.log(10)) + 1;
         nfMarker.setMinimumIntegerDigits(markerDigits);
         nfMarker.setMaximumIntegerDigits(markerDigits);
 
         //int tagShapeX[] = new int[3];
         //int tagShapeY[] = new int[3];
         //Polygon tagShape;
         int textRight = 0; // gets updated for scooting over
 
         // i = 0 to number of columns - 1
         for (int i = 0; i < filteredHaplos.length; i++) {
             int[] markerNums = filteredHaplos[i][0].getMarkers();
             boolean[] tags = filteredHaplos[i][0].getTags();
             //int headerX = x;
 
             for (int z = 0; z < markerNums.length; z++) {
                 //int tagMiddle = tagMetrics.getAscent() / 2;
                 //int tagLeft = x + z*letterWidth + tagMiddle;
                 //g.translate(tagLeft, 20);
 
                 // if tag snp, draw little triangle pooper
                 if (tags[z]) {
                     g.drawImage(tagImage, left + z*CHAR_WIDTH,
                             top + markerDigits*MARKER_CHAR_WIDTH +
                             -(CHAR_HEIGHT - TAG_SPAN), null);
                 }
 
                 //g.rotate(-Math.PI / 2.0);
                 //g.drawLine(0, 0, 0, 0);
 
                 //g.setColor(Color.black);
                 //g.drawString(nfMarker.format(markerNums[z]), 0, tagMiddle);
                 char markerChars[] = nfMarker.format(Chromosome.realIndex[markerNums[z]]+1).toCharArray();
                 for (int m = 0; m < markerDigits; m++) {
                     g.drawImage(markerNumImages[markerChars[m] - '0'],
                             left + z*CHAR_WIDTH +
                             (1 + CHAR_WIDTH - MARKER_CHAR_HEIGHT)/2,
                             top + (markerDigits-m-1)*MARKER_CHAR_WIDTH, null);
                 }
 
                 // undo the transform.. no push/pop.. arrgh
                 //g.rotate(Math.PI / 2.0);
                 //g.translate(-tagLeft, -20);
             }
 
             // y position of the first image for the haplotype letter
             // top + the size of the marker digits + the size of the tag +
             // the character height centered in the row's height
             int above = top + markerDigits*MARKER_CHAR_WIDTH + TAG_SPAN +
                     (ROW_HEIGHT - CHAR_HEIGHT) / 2;
 
             //figure out which allele is the major allele
             double[][] alleleCounts = new double[filteredHaplos[i][0].getGeno().length][9];
             //zero this out
             for (int j = 0; j < alleleCounts.length; j++){
                 for (int k = 0; k < alleleCounts[j].length; k++){
                     alleleCounts[j][k] = 0;
                 }
             }
             for (int j = 0; j < filteredHaplos[i].length; j++){
                 int curHapNum = lookupPos[i][j];
                 int[] theGeno = filteredHaplos[i][curHapNum].getGeno();
                 double theFreq = filteredHaplos[i][curHapNum].getPercentage();
                 for (int k = 0; k < theGeno.length; k++){
                     alleleCounts[k][theGeno[k]] += theFreq;
                 }
             }
             int[] majorAllele = new int[filteredHaplos[i][0].getGeno().length];
             for (int k = 0; k < majorAllele.length; k++){
                 double maj = 0;
                 for (int z = 0; z < alleleCounts[k].length; z++){
                     if (alleleCounts[k][z] > maj){
                         majorAllele[k] = z;
                         maj = alleleCounts[k][z];
                     }
                 }
             }
 
             for (int j = 0; j < filteredHaplos[i].length; j++){
                 int curHapNum = lookupPos[i][j];
                 //String theHap = new String();
                 //String thePercentage = new String();
                 int[] theGeno = filteredHaplos[i][curHapNum].getGeno(); //getGeno();
 
                 // j is the row of haplotype
                 for (int k = 0; k < theGeno.length; k++) {
                     // theGeno[k] will be 1,2,3,4 (acgt) or 8 (for bad)
                     if (alleleDisp == 0){
                         g.drawImage(charImages[theGeno[k] - 1],
                                 left + k*CHAR_WIDTH, above + j*ROW_HEIGHT, null);
                     }else if (alleleDisp == 1){
                         g.drawImage(blackNumImages[theGeno[k]],
                                 left + k*CHAR_WIDTH, above + j*ROW_HEIGHT, null);
                     }else{
                         if (theGeno[k] == majorAllele[k]){
                             g.setColor(dullBlue);
                        }else{
                            g.setColor(dullRed);
                         }
                         g.fillRect(left + k*CHAR_WIDTH,
                                 above + j*ROW_HEIGHT + (ROW_HEIGHT - CHAR_WIDTH)/2,
                                 CHAR_WIDTH, CHAR_WIDTH);
                     }
                 }
 
 
                 //draw the percentage value in non mono font
                 double percent = filteredHaplos[i][curHapNum].getPercentage();
                 //thePercentage = " " + nf.format(percent);
                 char percentChars[] = nf.format(percent).toCharArray();
 
                 // perhaps need an exceptional case for 1.0 being the percent
                 for (int m = 0; m < percentChars.length; m++) {
                     g.drawImage(grayNumImages[(m == 0) ? 10 : percentChars[m]-'0'],
                             left + theGeno.length*CHAR_WIDTH + m*CHAR_WIDTH,
                             above + j*ROW_HEIGHT, null);
                 }
 
                 // 4 is the number of chars in .999 for the percent
                 textRight = left + theGeno.length*CHAR_WIDTH + 4*CHAR_WIDTH;
 
 
                 g.setColor(Color.black);
                 if (i < filteredHaplos.length - 1) {  //draw crossovers
                     for (int crossCount = 0;
                          crossCount < filteredHaplos[i+1].length;
                          crossCount++) {
                         double crossVal =
                                 filteredHaplos[i][curHapNum].getCrossover(crossCount);
 
                         //draw thin and thick lines
                         int crossValue = (int) (crossVal*100);
                         if (crossValue > thinThresh) {
                             g.setStroke(crossValue > thickThresh ? thickStroke : thinStroke);
                             int connectTo = filteredHaplos[i+1][crossCount].getListOrder();
                             g.drawLine(textRight + LINE_LEFT,
                                     above + j*ROW_HEIGHT + ROW_HEIGHT/2,
                                     textRight + LINE_RIGHT,
                                     above + connectTo*ROW_HEIGHT + ROW_HEIGHT/2);
                         }
                     }
                 }
             }
             left = textRight;
 
             // add the multilocus d prime if appropriate
             if (i < filteredHaplos.length - 1) {
 
                 //put the numbers in the right place vertically
                 int depth;
                 if (filteredHaplos[i].length > filteredHaplos[i+1].length){
                     depth = filteredHaplos[i].length;
                 }else{
                     depth = filteredHaplos[i+1].length;
                 }
 
                 char multiChars[] =
                         nfMulti.format(multidprimeArray[i]).toCharArray();
                 if (multidprimeArray[i] > 0.99){
                     //draw 1.0 vals specially
                     g.drawImage(blackNumImages[1],
                             left + (LINE_SPAN - 7*CHAR_WIDTH/2)/2 + CHAR_WIDTH,
                             above + (depth * ROW_HEIGHT), null);
                     g.drawImage(blackNumImages[10],
                             left + (LINE_SPAN - 9*CHAR_WIDTH/2)/2 + 2*CHAR_WIDTH,
                             above + (depth * ROW_HEIGHT), null);
                     g.drawImage(blackNumImages[0],
                             left + (LINE_SPAN - 9*CHAR_WIDTH/2)/2 + 3*CHAR_WIDTH,
                             above + (depth * ROW_HEIGHT), null);
                 }else{
                     for (int m = 0; m < 3; m++) {
                         g.drawImage(blackNumImages[(m == 0) ? 10 : multiChars[m]-'0'],
                                 left + (LINE_SPAN - 7*CHAR_WIDTH/2)/2 + m*CHAR_WIDTH,
                                 above + (depth * ROW_HEIGHT), null);
                     }
                 }
             }
             left += LINE_SPAN;
         }
     }
 }
