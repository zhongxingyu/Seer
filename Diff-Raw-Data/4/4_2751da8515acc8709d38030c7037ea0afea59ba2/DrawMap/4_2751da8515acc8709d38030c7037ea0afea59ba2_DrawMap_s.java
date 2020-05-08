 package tracking;
 
 import utils.Pair;
 import utils.Utils;
 
 import java.applet.Applet;
 import java.awt.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class DrawMap extends Applet implements Runnable {
 
 
     public static final int floorWidth = 1000;
     public static final int floorHeight = 5000;
     public static final String SKEL_DIR = "/home/nogai/An4/Licenta/ActivityRecognitionModule/skel/";
 
     private RoomInfo map;
     private List<Snapshot> snapshotList = new ArrayList<Snapshot>();
 
     private static final int widthParts = 80, heightParts = 50;
     private int count = 0, maxCount;
 
     private Image bi;
     private Graphics2D big;
     private Thread animatie;
 
 
     @Override
     public void init() {
         super.init();
         map = new RoomInfo(getSize().width, getSize().height, widthParts, heightParts);
 
 
         List<String> fileNames = Utils.getFileNamesFromDirectory(new File(SKEL_DIR).listFiles());
 
 
         for (String fileName : fileNames) {
 
             try {
                 //if(fileName.startsWith("skeleton_"))
                 snapshotList.add(new Snapshot(fileName, widthParts, heightParts, floorWidth, floorHeight));
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
 
         maxCount = snapshotList.size();
 
 
         //double buffering
         bi = createImage((int) map.getWidth(), (int) map.getHeight());
         big = (Graphics2D) bi.getGraphics();
 
         //initializare thread
         animatie = new Thread(this);
         //pornire thread
         animatie.start();
     }
 
 
     public void paint(Graphics g) {
 
         Graphics2D gg;
         gg = (Graphics2D) g;
         int x, y;
         Pair<Integer, Integer> position;
 
        position = snapshotList.get(count).getUserOnFloorPosition();
         System.out.println("Count: " + (count + 5) + " Highlight: " + position);
 
 
         big.setColor(Color.white);
 
         big.clearRect(0, 0, (int) map.getWidth(), (int) map.getHeight());
 
         big.setBackground(Color.black);
 
 
         for (int line = 0; line < heightParts; line++) {
             for (int column = 0; column < widthParts; column++) {
 
                 x = column * (int) map.getWidthChunk();
                 y = line * (int) map.getHeightChunk();
 
 
                 if (line == position.getFirst() && column == position.getSecond()) {
                     big.setColor(Color.green);
                     big.fillRect(x, y, (int) map.getWidthChunk(), (int) map.getHeightChunk());
                 } else {
                     big.setColor(Color.blue);
                     big.drawRect(x, y, (int) map.getWidthChunk(), (int) map.getHeightChunk());
 
                 }
 
             }
         }
 
 
         gg.drawImage(bi, 0, 0, this);
 
     }
 
 
     @Override
     public void run() {
         while (true) {
             try {
                 Thread.sleep(1000);
 
 
                 if (count < maxCount - 1)
                     count++;
 
                 repaint();
             } catch (Exception e) {
             }
         }
 
 
     }
 
 
 }
