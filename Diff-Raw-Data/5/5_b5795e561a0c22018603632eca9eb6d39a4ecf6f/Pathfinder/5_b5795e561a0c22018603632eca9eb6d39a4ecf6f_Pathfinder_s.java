 package Pathfinding;
 
 import Crane.Crane;
 import Helpers.Vector3f;
 import Parkinglot.Parkinglot;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author EightOneGulf
  */
 public class Pathfinder {
     /**
      * 
      */
     public static Node[] Nodes; 
     /**
      * 
      */
     public static Path[] Paths;
 
     /**
      * 
      */
     public static void generateGrid(){
         int width = 10;
         int height = 10;
         int multiplier = -50;
         
         Nodes = new Node[width*height];
         Paths = new Path[351];
         
         int pathCounter = 0;
         for(int j = 0 ; j < height; j++){
             for(int i = 0 ; i < width; i++){
                 Nodes[i + j*width] = new Node(i*multiplier,j*multiplier);
                 
                 //Connect with previous nodes
                 if(i>0){
                     Paths[pathCounter]=new Path(Nodes[i + j*width], Nodes[i + j*width -1]);
                     pathCounter++;
                 }
                 if(j>0){
                     Paths[pathCounter]=new Path(Nodes[i + j*width], Nodes[i + (j-1)*width ]);
                     pathCounter++;
                 }
                 if(j>0 && i>0){
                     Paths[pathCounter]=new Path(Nodes[i + j*width], Nodes[i + (j-1)*width -1 ]);
                     pathCounter++;
                 }
                 if(j>0 && i<width){
                     Paths[pathCounter]=new Path(Nodes[i + j*width], Nodes[i + (j-1)*width +1 ]);
                     pathCounter++;
                 }
             }
         }
     }
     public static final float pathWidth = 3;
     public static final float gapBetweenRoads = 4;
     public static final float storageLenght = 1550;
     public static final float storageWidth = 600;  
     
     public static Crane[] Cranes = new Crane[10+8+4+20];
     public static Parkinglot[] parkinglots;
     
     public static void generateArea() throws Exception{
         Nodes = new Node[300];
         parkinglots = new Parkinglot[200];
         List<Path> pathList = new ArrayList<>();
         
         // helpers
         final float halfPathWidth = pathWidth/2;
         final float mainRoadWidth = 4*pathWidth;
         final float distanceToMainRoad = pathWidth+pathWidth*gapBetweenRoads;
         final float distanceToStorage= distanceToMainRoad+mainRoadWidth;
         final float totalLenght = distanceToStorage*2+storageLenght;
         final float totalWidth = distanceToStorage*2+storageWidth;
         
         // <editor-fold defaultstate="collapsed" desc="base squar  nodes(0-15)">
         // Nodes
         // n w corner
         Nodes[0] = new Node(distanceToMainRoad+0*pathWidth+halfPathWidth, distanceToStorage+storageWidth+3*pathWidth+halfPathWidth);
         Nodes[1] = new Node(distanceToMainRoad+1*pathWidth+halfPathWidth, distanceToStorage+storageWidth+2*pathWidth+halfPathWidth);
         Nodes[2] = new Node(distanceToMainRoad+2*pathWidth+halfPathWidth, distanceToStorage+storageWidth+1*pathWidth+halfPathWidth);
         Nodes[3] = new Node(distanceToMainRoad+3*pathWidth+halfPathWidth, distanceToStorage+storageWidth+0*pathWidth+halfPathWidth);
         // n o corner
         Nodes[4] = new Node(distanceToStorage+storageLenght+3*pathWidth+halfPathWidth, distanceToStorage+storageWidth+3*pathWidth+halfPathWidth);
         Nodes[5] = new Node(distanceToStorage+storageLenght+2*pathWidth+halfPathWidth, distanceToStorage+storageWidth+2*pathWidth+halfPathWidth);
         Nodes[6] = new Node(distanceToStorage+storageLenght+1*pathWidth+halfPathWidth, distanceToStorage+storageWidth+1*pathWidth+halfPathWidth);
         Nodes[7] = new Node(distanceToStorage+storageLenght+0*pathWidth+halfPathWidth, distanceToStorage+storageWidth+0*pathWidth+halfPathWidth);
         // z o corner
         Nodes[8] = new Node(distanceToStorage+storageLenght+3*pathWidth+halfPathWidth,distanceToMainRoad+0*pathWidth+halfPathWidth);
         Nodes[9] = new Node(distanceToStorage+storageLenght+2*pathWidth+halfPathWidth,distanceToMainRoad+1*pathWidth+halfPathWidth);
         Nodes[10] = new Node(distanceToStorage+storageLenght+1*pathWidth+halfPathWidth,distanceToMainRoad+2*pathWidth+halfPathWidth);
         Nodes[11] = new Node(distanceToStorage+storageLenght+0*pathWidth+halfPathWidth,distanceToMainRoad+3*pathWidth+halfPathWidth);
         // z w corner
         Nodes[12] = new Node(distanceToMainRoad+0*pathWidth+halfPathWidth, distanceToMainRoad+0*pathWidth+halfPathWidth);
         Nodes[13] = new Node(distanceToMainRoad+1*pathWidth+halfPathWidth, distanceToMainRoad+1*pathWidth+halfPathWidth);
         Nodes[14] = new Node(distanceToMainRoad+2*pathWidth+halfPathWidth, distanceToMainRoad+2*pathWidth+halfPathWidth);
         Nodes[15] = new Node(distanceToMainRoad+3*pathWidth+halfPathWidth, distanceToMainRoad+3*pathWidth+halfPathWidth);
         
         // Paths
         // n
         pathList.add(new Path(Nodes[4], Nodes[0]));
         pathList.add(new Path(Nodes[5], Nodes[1]));
         pathList.add(new Path(Nodes[2], Nodes[6]));
         pathList.add(new Path(Nodes[3], Nodes[7]));
         // o
         pathList.add(new Path(Nodes[8], Nodes[4]));
         pathList.add(new Path(Nodes[9], Nodes[5]));
         pathList.add(new Path(Nodes[6], Nodes[10]));
         pathList.add(new Path(Nodes[7], Nodes[11]));
         // z
         pathList.add(new Path(Nodes[12], Nodes[8]));
         pathList.add(new Path(Nodes[13], Nodes[9]));
         pathList.add(new Path(Nodes[10], Nodes[14]));
         pathList.add(new Path(Nodes[11], Nodes[15]));
         // w
         pathList.add(new Path(Nodes[0], Nodes[12]));
         pathList.add(new Path(Nodes[1], Nodes[13]));
         pathList.add(new Path(Nodes[14], Nodes[2]));
         pathList.add(new Path(Nodes[15], Nodes[3]));
         
         // </editor-fold>
         
         // <editor-fold defaultstate="collapsed" desc="seaship part   nodes(16-35), parkinglot(0, 1-10)">
         // Nodes
         Nodes[16] = new Node(distanceToMainRoad-halfPathWidth, distanceToStorage+storageWidth+mainRoadWidth-halfPathWidth); // n o corner
         Nodes[17] = new Node(halfPathWidth, distanceToStorage+storageWidth+mainRoadWidth-halfPathWidth); // n w corner
         Nodes[18] = new Node(halfPathWidth,distanceToMainRoad+halfPathWidth); // z w corner
         Nodes[19] = new Node(distanceToMainRoad-halfPathWidth,distanceToMainRoad+halfPathWidth); // z 0 corner
         Nodes[20] = new Node(distanceToMainRoad/2,distanceToStorage+storageWidth+mainRoadWidth+pathWidth+halfPathWidth); // parkinglot
         parkinglots[0] = new Parkinglot(10, Nodes[20]);
         Nodes[21] = new Node(distanceToMainRoad/2,distanceToStorage+storageWidth+mainRoadWidth-halfPathWidth); // parkinglot path node
         for (int i = 0; i < 10; i++) {
             Nodes[22+i] = new Node(halfPathWidth, distanceToStorage+(storageWidth/20)+storageWidth/10*i); // crane node
             parkinglots[1+i] = new Parkinglot(1, Nodes[22+i]);
         }
         
         // Paths
         for (int i = 0; i < 4; i++) {
             pathList.add(new Path(Nodes[i], Nodes[16])); // squar n w to start node
         }
         pathList.add(new Path(Nodes[16], Nodes[21])); // n o corner to parkinglot path node
         pathList.add(new Path(Nodes[20], Nodes[21])); // parkinglot to parkinglot path node
         pathList.add(new Path(Nodes[21], Nodes[17])); // parkinglot path node to n w corner
         for (int i = 0; i < 10; i++) {
             pathList.add(new Path(Nodes[17], Nodes[22+i])); // n w corner to crane
             pathList.add(new Path(Nodes[22+i], Nodes[18])); // crane to z w corner
         }
         pathList.add(new Path(Nodes[18], Nodes[19])); // z w corner to z o corner
         for (int i = 0; i < 4; i++) {
             pathList.add(new Path(Nodes[19], Nodes[12+i])); // z o corner to squar z w
         }
         // </editor-fold>
         
         // <editor-fold defaultstate="collapsed" desc="inlandship part   nodes(36-55), parkinglot(11, 12-19)">
         // Nodes
         Nodes[36] = new Node(distanceToStorage+storageLenght+mainRoadWidth-halfPathWidth,distanceToMainRoad-halfPathWidth); // n o corner
         Nodes[37] = new Node(distanceToMainRoad+halfPathWidth,distanceToMainRoad-halfPathWidth); // n w corner
         Nodes[38] = new Node(distanceToMainRoad+halfPathWidth, halfPathWidth); // z w corner
         Nodes[39] = new Node(distanceToStorage+storageLenght+mainRoadWidth-halfPathWidth, halfPathWidth); // z 0 corner
         Nodes[40] = new Node(distanceToMainRoad-(pathWidth+halfPathWidth), distanceToMainRoad/2); // parkinglot
         parkinglots[11] = new Parkinglot(1, Nodes[40]);
         Nodes[41] = new Node(distanceToMainRoad+halfPathWidth, distanceToMainRoad/2); // parkinglot path node
         for (int i = 0; i < 8; i++) {
             Nodes[42+i] = new Node((distanceToMainRoad+storageLenght/8/2)+(distanceToMainRoad+storageLenght/8*i), halfPathWidth); // crane node
             parkinglots[12+i] = new Parkinglot(10, Nodes[42+i]);
         }
         
         // Paths
         for (int i = 0; i < 4; i++) {
             pathList.add(new Path(Nodes[12+i], Nodes[37])); // squar z w to start node
         }
         pathList.add(new Path(Nodes[37], Nodes[41])); // n w corner to parkinglot path node
         pathList.add(new Path(Nodes[41], Nodes[40])); // parkinglot to parkinglot path node
         pathList.add(new Path(Nodes[41], Nodes[38])); // parkinglot path node to z w corner
         for (int i = 0; i < 8; i++) {
             pathList.add(new Path(Nodes[38], Nodes[42+i])); // n w corner to crane
             pathList.add(new Path(Nodes[42+i], Nodes[39])); // crane to z w corner
         }
         pathList.add(new Path(Nodes[39], Nodes[36])); // z w corner to z o corner
         for (int i = 0; i < 4; i++) {
             pathList.add(new Path(Nodes[36], Nodes[8+i])); // z o corner to squar z w
         }
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="truck part    nodes(56-85), parkinglot(20,21-40)">
         // Nodes
         Nodes[56] = new Node(distanceToStorage+storageLenght+mainRoadWidth+halfPathWidth, distanceToMainRoad+halfPathWidth); // z w corner
         Nodes[57] = new Node(totalLenght-halfPathWidth, distanceToMainRoad+halfPathWidth); // z o corner
         Nodes[58] = new Node(totalLenght-halfPathWidth, distanceToStorage+storageWidth+mainRoadWidth-halfPathWidth); // n o corner
         Nodes[59] = new Node(distanceToStorage+storageLenght+mainRoadWidth+halfPathWidth, distanceToStorage+storageWidth+mainRoadWidth-halfPathWidth); // n w corner
         Nodes[60] = new Node(distanceToStorage+storageLenght+mainRoadWidth+distanceToMainRoad/2, distanceToMainRoad-(pathWidth+halfPathWidth)); // parkinglot
         parkinglots[20] = new Parkinglot(10, Nodes[60]);
         Nodes[61] = new Node(distanceToStorage+storageLenght+mainRoadWidth+distanceToMainRoad/2, distanceToMainRoad+halfPathWidth); // parkinglot path node
         for (int i = 0; i < 20; i++) {
             Nodes[62+i] = new Node(distanceToStorage+storageLenght+distanceToStorage-halfPathWidth, (distanceToMainRoad+storageWidth/20/2)+(distanceToMainRoad+storageWidth/20*i)); // crane node
             parkinglots[21+i] = new Parkinglot(1, Nodes[62+i]);
         }
         
         // Paths
         for (int i = 0; i < 4; i++) {
             pathList.add(new Path(Nodes[8+i], Nodes[56])); // squar z o to start node
         }
         pathList.add(new Path(Nodes[56], Nodes[61])); // n w corner to parkinglot path node
         pathList.add(new Path(Nodes[60], Nodes[61])); // parkinglot to parkinglot path node
         pathList.add(new Path(Nodes[61], Nodes[57])); // parkinglot path node to z o corner
         for (int i = 0; i < 20; i++) {
             pathList.add(new Path(Nodes[57], Nodes[62+i])); // z o corner to crane
             pathList.add(new Path(Nodes[62+i], Nodes[58])); // crane to n o corner
         }
         pathList.add(new Path(Nodes[58], Nodes[59])); // n o corner to n w corner
         for (int i = 0; i < 4; i++) {
             pathList.add(new Path(Nodes[59], Nodes[4+i])); // n w corner to squar n o
         }
         // </editor-fold>
         
         // <editor-fold defaultstate="collapsed" desc="train part   nodes(86-100), parkinglot(41,41-45)">
         // Nodes
         Nodes[86] = new Node(distanceToStorage+storageLenght+mainRoadWidth-halfPathWidth, distanceToStorage+storageWidth+mainRoadWidth+halfPathWidth); // z o corner
         Nodes[87] = new Node(distanceToStorage+storageLenght+mainRoadWidth-halfPathWidth, totalWidth-halfPathWidth); // n o corner
         Nodes[88] = new Node(distanceToMainRoad+halfPathWidth, totalWidth-halfPathWidth); // n w corner
         Nodes[89] = new Node(distanceToMainRoad+halfPathWidth, distanceToStorage+storageWidth+mainRoadWidth+halfPathWidth); // z w corner
         Nodes[90] = new Node(distanceToStorage+storageLenght+mainRoadWidth-halfPathWidth, distanceToStorage+storageWidth+mainRoadWidth+distanceToMainRoad/2); // parkinglot
         parkinglots[41] = new Parkinglot(10, Nodes[90]);
         Nodes[91] = new Node(distanceToStorage+storageLenght+mainRoadWidth+pathWidth+halfPathWidth, distanceToStorage+storageWidth+mainRoadWidth+distanceToMainRoad/2); // parkinglot path node
         for (int i = 0; i < 4; i++) {
             Nodes[92+i] = new Node((distanceToMainRoad+storageLenght/4/2)+(distanceToMainRoad+storageLenght/4*i) ,distanceToStorage+storageWidth+distanceToStorage-halfPathWidth); // crane node
             parkinglots[42+i] = new Parkinglot(1, Nodes[92+i]);
         }
         
         // Paths
         for (int i = 0; i < 4; i++) {
             pathList.add(new Path(Nodes[4+i], Nodes[86])); // squar n o to start node
         }
         pathList.add(new Path(Nodes[86], Nodes[90])); // z o corner to parkinglot path node
         pathList.add(new Path(Nodes[91], Nodes[90])); // parkinglot to parkinglot path node
         pathList.add(new Path(Nodes[90], Nodes[87])); // parkinglot path node to n o corner
         for (int i = 0; i < 4; i++) {
             pathList.add(new Path(Nodes[87], Nodes[92+i])); // n o corner to crane
             pathList.add(new Path(Nodes[92+i], Nodes[88])); // crane to n w corner
         }
         pathList.add(new Path(Nodes[88], Nodes[89])); // n w corner to z w corner
         for (int i = 0; i < 4; i++) {
             pathList.add(new Path(Nodes[89], Nodes[i])); // z w corner to squar n w
         }
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="Seaship route  nodes(101-125), parkinglot(46)">
         float distanceToDockSeaship = 50; // todo edit this value
         float distandeBetweenNodesSeaship = 100;
         float curveSeaship = 1.5f;
         
         // nodes
         Nodes[101] = new Node(-distanceToDockSeaship, totalWidth/2); // dock
         parkinglots[46] = new Parkinglot(1,Nodes[101]);
         for (int i = 1; i <= 10; i++) {
             Nodes[101+i] = new Node(-distanceToDockSeaship-(int)Math.pow(i, curveSeaship+1), (totalWidth/2)+i*distandeBetweenNodesSeaship);
             Nodes[111+i] = new Node(-distanceToDockSeaship-(int)Math.pow(i, curveSeaship+1), (totalWidth/2)-i*distandeBetweenNodesSeaship);
         }
         
         //path
         pathList.add(new Path(Nodes[102], Nodes[101]));
         pathList.add(new Path(Nodes[101], Nodes[112]));
         for (int i = 0; i < 10-1; i++) {
             pathList.add(new Path(Nodes[111-i], Nodes[110-i]));
             pathList.add(new Path(Nodes[112+i], Nodes[113+i]));
         }
         // </editor-fold>
         
         // <editor-fold defaultstate="collapsed" desc="Inlandship route  nodes(130-175), parkinglot(47,48)">
         float distanceToDockInlandship = 50; // todo edit this value
         float distandeBetweenNodesInlandship = 100;
         float curveInlandship = 1.5f;
         
         // nodes
         Nodes[130] = new Node(distanceToStorage+storageLenght/4, -distanceToDockInlandship); // dock 1
         parkinglots[47] = new Parkinglot(1,Nodes[130]);
         Nodes[131] = new Node(distanceToStorage+storageLenght/4*3, -distanceToDockInlandship); // dock 2
         parkinglots[48] = new Parkinglot(1,Nodes[131]);
         
         for (int i = 1; i <= 10; i++) {
             Nodes[132+i] = new Node(((distanceToStorage+storageLenght/4)+i*distandeBetweenNodesInlandship), -distanceToDockSeaship-(int)Math.pow(i, curveInlandship+1));
             Nodes[142+i] = new Node(((distanceToStorage+storageLenght/4*3)+i*distandeBetweenNodesInlandship), -distanceToDockSeaship-(int)Math.pow(i, curveInlandship+1));
             Nodes[152+i] = new Node(((distanceToStorage+storageLenght/4)-i*distandeBetweenNodesInlandship), -distanceToDockSeaship-(int)Math.pow(i, curveInlandship+1));
             Nodes[162+i] = new Node(((distanceToStorage+storageLenght/4*3)-i*distandeBetweenNodesInlandship), -distanceToDockSeaship-(int)Math.pow(i, curveInlandship+1));
         }
         
         //path
         pathList.add(new Path(Nodes[153], Nodes[130]));
         pathList.add(new Path(Nodes[130], Nodes[133]));
         pathList.add(new Path(Nodes[163], Nodes[131]));
         pathList.add(new Path(Nodes[131], Nodes[143]));
         for (int i = 0; i < 10-1; i++) {
             pathList.add(new Path(Nodes[162-i], Nodes[161-i]));
             pathList.add(new Path(Nodes[133+i], Nodes[134+i]));
             pathList.add(new Path(Nodes[172-i], Nodes[171-i]));
             pathList.add(new Path(Nodes[143+i], Nodes[144+i]));
         }
         
         // </editor-fold>
         
         // <editor-fold defaultstate="collapsed" desc="truck route   nodes(176-205), parkinglot(49-68)">
         
         float distanceToDockTruck = 50; // todo edit this value
         float distandeBetweenNodesTruck = 200;
         
         // nodes
         for (int i = 0; i < 20; i++) {
             Nodes[176+i] = new Node(totalLenght+distanceToDockTruck, distanceToStorage+storageWidth/20/2+storageWidth/20*i);
             parkinglots[49+i] = new Parkinglot(1,Nodes[176+i]);
         }
         Nodes[196] = new Node(totalLenght+distanceToDockTruck, distanceToMainRoad);
         Nodes[197] = new Node(totalLenght+distanceToDockTruck, totalWidth-distanceToMainRoad);
         Nodes[198] = new Node(totalLenght+distanceToDockTruck+distandeBetweenNodesTruck, distanceToMainRoad);
         Nodes[199] = new Node(totalLenght+distanceToDockTruck+distandeBetweenNodesTruck, totalWidth-distanceToMainRoad);
         
         for (int i = 0; i < 20; i++) {
             pathList.add(new Path(Nodes[187], Nodes[176+i]));
             pathList.add(new Path(Nodes[176+i], Nodes[186]));
         }
         pathList.add(new Path(Nodes[199], Nodes[197]));
         pathList.add(new Path(Nodes[197], Nodes[195]));
         pathList.add(new Path(Nodes[176], Nodes[196]));
         pathList.add(new Path(Nodes[196], Nodes[198]));
         
         // </editor-fold>
         
         // <editor-fold defaultstate="collapsed" desc="train route   nodes(206-), parkinglot(69,70)">
         float distanceToDockTrain = 50; // todo edit this value
         float curveTrain = 1.8f;
         
         // nodes
         Nodes[206] = new Node(distanceToStorage+storageLenght/4, totalWidth+distanceToDockTrain); // dock 1
        parkinglots[68] = new Parkinglot(1,Nodes[206]);
         Nodes[207] = new Node(distanceToStorage+storageLenght/4*3, totalWidth+distanceToDockTrain); // dock 2
        parkinglots[69] = new Parkinglot(1,Nodes[207]);
         
         for (int i = 1; i <= 10; i++) {
             Nodes[207+i] = new Node(((distanceToStorage+storageLenght/2)+i*distanceToDockTrain), totalWidth+distanceToDockTrain+(int)Math.pow(i, curveTrain+1));
             Nodes[217+i] = new Node(((distanceToStorage+storageLenght)+i*distanceToDockTrain), totalWidth+distanceToDockTrain+(int)Math.pow(i, curveTrain+1));
         }
         Nodes[230] = new Node(distanceToStorage, totalWidth+distanceToDockTrain); // dock 2
         Nodes[231] = new Node(distanceToStorage+storageLenght/2, totalWidth+distanceToDockTrain); // dock 2
         
         //path
         pathList.add(new Path(Nodes[230], Nodes[206]));
         pathList.add(new Path(Nodes[206], Nodes[208]));
         pathList.add(new Path(Nodes[231], Nodes[207]));
         pathList.add(new Path(Nodes[207], Nodes[218]));
         for (int i = 0; i < 9; i++) {
             pathList.add(new Path(Nodes[208+i], Nodes[209+i]));
             pathList.add(new Path(Nodes[218+i], Nodes[219+i]));
         }
         // </editor-fold>
         
         // </editor-fold>
         Paths = new Path[pathList.size()];
         for (int i = 0; i < pathList.size(); i++) {
             Paths[i] = pathList.get(i);
         }
     }
     
     /**
      * 
      */
     public static void generatePaths(){
         Nodes = new Node[12];
         Paths = new Path[13];
         
         Nodes[0] = new Node(0, 0);
         
         Nodes[1] = new Node(2,2);
         Nodes[2] = new Node(2,3);
         Nodes[3] = new Node(4,3);
         Nodes[4] = new Node(3,3.5f);
         Nodes[5] = new Node(1,4);
         Nodes[6] = new Node(0,5);
         
         Nodes[7] = new Node(0,1);
         Nodes[8] = new Node(1,2);
         Nodes[9] = new Node(0,3);
         Nodes[10] = new Node(1,4);
         
         Nodes[11] = new Node(-4,3);
         
         
 
         Paths[0] = new Path(Nodes[0], Nodes[1]);
         Paths[1] = new Path(Nodes[1], Nodes[2]);
         Paths[2] = new Path(Nodes[2], Nodes[3]);
         Paths[3] = new Path(Nodes[3], Nodes[4]);
         Paths[4] = new Path(Nodes[4], Nodes[5]);
         Paths[5] = new Path(Nodes[5], Nodes[6]);
         
         Paths[6] = new Path(Nodes[0], Nodes[7]);
         Paths[7] = new Path(Nodes[7], Nodes[8]);
         Paths[8] = new Path(Nodes[8], Nodes[9]);
         Paths[9] = new Path(Nodes[9], Nodes[10]);
         Paths[10] = new Path(Nodes[10], Nodes[6]);
         
         Paths[11] = new Path(Nodes[0], Nodes[11]);
         Paths[12] = new Path(Nodes[11], Nodes[6]);
     }
 
     public static Node findClosestNode(Vector3f position){
         NodeScore[] nscore = new NodeScore[Nodes.length];
         for(int i = 0 ; i < Nodes.length ; i++){
             if(Nodes[i]!=null)
                 nscore[i] = new NodeScore(Nodes[i], Vector3f.distance(Nodes[i].getPosition(), position) , null);
         }
 
         float lowestScore = Float.MAX_VALUE;
         int index = 0;
         
         for(int i = 0 ; i < nscore.length ; i++){
             if(nscore[i]!= null && nscore[i].score<lowestScore){
                 lowestScore = nscore[i].score;
                 index = i;
             }
         }
 
         return nscore[index].node;
     }
 
     /**
      * 
      * @param startNode     The node at the current position
      * @param endNode       The node at the destination
      * @return              An array of all nodes on the route found.
      */
     public static Node[] findShortest(Node startNode, Node endNode) throws Exception{
         return findShortest(startNode, endNode, false);
     }
 
     /** 
      * 
      * @param startNode     The node at the current position
      * @param endNode       The node at the destination
      * @param cargoFilled   If true, the vehicle carries a container. When false, the vehicle is empty. Certain routes only allow full or empty.
      * @return              An array of all nodes on the route found.
      */
     public static Node[] findShortest(Node startNode, Node endNode, boolean cargoFilled) throws Exception{
         if(startNode==null||endNode==null)throw new Exception("Can't use empty nodes");
         if(!nodesContains(startNode))throw new Exception("Given startNode does not exist within pathfinder");
         if(!nodesContains(endNode))throw new Exception("Given endNode does not exist within pathfinder");
         
         
         ArrayList<Node> closedSet = new ArrayList();          //All nodes wich have already been processed. Don't process again
         ArrayList<NodeScore> openSet = new ArrayList();  //All nodes present for further process.
 
         openSet.add( new NodeScore(startNode,0, null) );            //Begin at startNode
         
         while(openSet.size() > 0){                  // Keep on processing till no more nodes are available
             Collections.sort(openSet);              //Sort on score, lowest score first
             
             NodeScore current = openSet.get(0);     //Fetch and remove first from openset..
             openSet.remove(current);
             closedSet.add(current.node);            //..and add to closed set, to prevent further processing
 
             if(current.node.equals(endNode)){      //Current node is endnode, route found 
                 //Found him                
                 
                 //////Build route
                 ArrayList<Node> route = new ArrayList();
                 route.add(current.node);
                 NodeScore parent = current.parent;
                 while(parent!=null){
                     route.add(parent.node);
                     parent = parent.parent;
                 }
                 
                 //////Convert ArrayList to array
                 Node[] finalroute = new Node[route.size()];
                 for(int i = 0 ; i<route.size() ; i++){
                     finalroute[i] = route.get(route.size()-1-i);
                 }
                 return finalroute;
             }
             
             
             for(NodeScore n : findNeighbourNodes(current, cargoFilled)){ //Loop trough all neighbours of current node
                 if(!closedSet.contains(n.node) && !nodeScoreContains(openSet, n.node)){    //if neighbour hasent been processed yet
                     n.score+=current.score; 
                     n.parent = current;     //Set parent for backtracking
                     openSet.add(n);         //Set neighbour for further processing
                 }
             }
         }
 
         return null;
     }
 
     private static boolean nodeScoreContains(ArrayList<NodeScore> openSet, Node node){
         for(NodeScore ns : openSet){
             if(ns.node.equals(node))return true;
         }
         return false;
     }
 
     private static ArrayList<NodeScore> findNeighbourNodes(NodeScore node, boolean cargoFilled){
         return findNeighbourNodes(node.node, cargoFilled);
     }
 
     private static ArrayList<NodeScore> findNeighbourNodes(Node node, boolean cargoFilled){  //Find all nodes connected to given node (via paths)
         ArrayList<Path> paths = findNeighbourPaths(node, cargoFilled);
         ArrayList<NodeScore> nodes = new ArrayList();
 
         for(Path p : paths){
             NodeScore n = new NodeScore( (p.getPointA().equals(node)?p.getPointB():p.getPointA()), p.getLength(), null);
             nodes.add(n);
         }
         return nodes;
     }
 
     private static ArrayList<Path> findNeighbourPaths(Node node, boolean cargoFilled){   //Find all paths connected to given node
         ArrayList<Path> neighbourPaths = new ArrayList();
         for(Path p : Paths){
             if(p.getPointA().equals(node) || p.getPointB().equals(node)){
                 if((cargoFilled && p.FilterCargo==2) || (!cargoFilled && p.FilterCargo==1)){
                     //Filter on cargo
                 }else
                 if(p.OneWay && p.getPointA().equals(node)){
                     //One way path, don't add
                 }else{
                     neighbourPaths.add(p);
                 }
             }
         }
         return neighbourPaths;
     }
  
     private static boolean nodesContains(Node i){
         for(Node n : Nodes){
             if(n.equals(i))return true;
         }
         return false;
     }
 }
