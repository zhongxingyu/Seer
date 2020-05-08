 package dk.itu.grp11.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Test;
 
 import dk.itu.grp11.data.Parser;
 import dk.itu.grp11.data.Point;
 import dk.itu.grp11.data.Road;
 import dk.itu.grp11.enums.MapBound;
 import dk.itu.grp11.enums.RoadType;
 import dk.itu.grp11.util.Interval;
 import dk.itu.grp11.util.Interval2D;
 
 public class ParserTest {
   
   //Needs the default files for this test
   Parser ps = Parser.getTestParser(null, null, null);
   
   //Testing to see whether (some) points get the right id and values assigned or not
   @Test
   public void assignedIdTest() throws IOException {
     Map<Integer, Point> points = Parser.getParser().points();
     
     //The first point
     assertEquals(1, points.get(1).getID());
     assertEquals(595527.51786, points.get(1).getX(), 0);
     assertEquals(6402050.98297, points.get(1).getY(), 0);
     
     //The last point
     assertEquals(675902, points.get(points.size()).getID());
     assertEquals(692067.66450, points.get(points.size()).getX(), 0);
     assertEquals(6049914.43018, points.get(points.size()).getY(), 0);
   }
 
   //Testing if all roads, with zoom level 1, are included at zoom level 1
   @Test
   public void allRoadsTest() throws IOException {
     // Road types to count
     Set<RoadType> roadTypes = new HashSet<>();
     roadTypes.add(RoadType.MOTORVEJ);
     roadTypes.add(RoadType.MOTORTRAFIKVEJ);
     roadTypes.add(RoadType.PROJ_MOTORVEJ);
     roadTypes.add(RoadType.PROJ_MOTORTRAFIKVEJ);
     roadTypes.add(RoadType.MOTORVEJSTUNNEL);
     roadTypes.add(RoadType.MOTORTRAFIKVEJSTUNNEL);
     roadTypes.add(RoadType.PRIMAERRUTE_OVER_6M);
     roadTypes.add(RoadType.FAERGEFORBINDELSE);
     
     int roadCount = 0;
     
     // Count number of roads for every road type
     for(RoadType rt : roadTypes) {
       Interval<Double, RoadType> intervalX = new Interval<Double, RoadType>(Parser.getParser().mapBound(MapBound.MINX), Parser.getParser().mapBound(MapBound.MAXX), rt);
       Interval<Double, RoadType> intervalY = new Interval<Double, RoadType>(Parser.getParser().mapBound(MapBound.MINY), Parser.getParser().mapBound(MapBound.MAXY), rt);
       Interval2D<Double, RoadType> rect = new Interval2D<Double, RoadType>(intervalX, intervalY);
       Set<Road> roadsInViewbox = ps.roads().query2D(rect);
       roadCount += roadsInViewbox.size();
     }
     
     // Expecting all roads with zoom level 1 to be included
     assertEquals(countRoads(roadTypes), roadCount);
   }
   
   //Counts number of roads directly from file
   public int countRoads(Set<RoadType> roadTypes) throws IOException {
    // Reading all points
    HashMap<Integer, Point> points = ps.points();
    
    // Counting roads
     File f = new File("src\\dk\\itu\\grp11\\files\\kdv_unload.txt");
     BufferedReader input = new BufferedReader(new FileReader(f));
     String line = input.readLine(); line = input.readLine();
     int count = 0;
     while(line != null) {
       String[] r = line.split(",");
       if(roadTypes.contains(RoadType.getById(Integer.parseInt(r[5])))) count++;
      // Subtracting roads where x and y coordinates are equal
      if(roadTypes.contains(RoadType.getById(Integer.parseInt(r[5]))) &&
         points.get(Integer.parseInt(r[0])).getX() == points.get(Integer.parseInt(r[1])).getX() &&
         points.get(Integer.parseInt(r[0])).getY() == points.get(Integer.parseInt(r[1])).getY()) count--;
       line = input.readLine();
     }
     return count;
   }
   
   /*
    * Testing the speed of the roadname prefix finder
    */
   @Test
   public void prefixSpeedTest() {
     System.out.println("Prefix time test");
 
     Set<String> p = new HashSet<String>();
     for (String s : ps.roadNames().keySet()) {
       p.add(s.substring(0, 2));
     }
 
     long max = 0;
     long accumulated = 0;
     int zeroOne = 0;
     for (String s : p) {
       long start = System.currentTimeMillis();
       ps.roadsWithPrefix(s);
       long diff = System.currentTimeMillis() - start;
 
       // System.out.println(s+": " + diff + "ns");
       if (diff > max)
         max = diff;
       if (diff <= 1)
         zeroOne++;
       accumulated += diff;
     }
     System.out.println("Tests: " + p.size());
     System.out.println("Zero or one: " + zeroOne);
     System.out.println("Maximum: " + max + "ns");
     System.out.println("Accumulated: " + accumulated + "ns");
     System.out.println("Average: " + accumulated / p.size() + "ns");
   }
 }
