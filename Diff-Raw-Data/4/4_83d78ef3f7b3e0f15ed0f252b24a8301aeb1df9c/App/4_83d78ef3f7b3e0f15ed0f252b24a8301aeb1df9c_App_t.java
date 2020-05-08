 package org.mobiloc.lobgasp;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.hibernate.Session;
 import org.mobiloc.lobgasp.util.HibernateUtil;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Geometry;
 import java.util.Iterator;
 import org.mobiloc.lobgasp.osm.parser.OSMParser;
 import org.mobiloc.lobgasp.osm.parser.model.OSM;
 
 /**
  * Hello world!
  *
  */
 public class App {
 
     public static void main(String[] args) {
         System.out.println("Hello World!");
 
         Session s = HibernateUtil.getSessionFactory().getCurrentSession();
         s.beginTransaction();
         Iterator l = s.createSQLQuery("SELECT ST_AsText(ST_Transform(way,94326)), name FROM planet_osm_point"
                 + " WHERE amenity like 'pub' OR amenity like 'bar'"
                 + " ORDER BY osm_id").list().iterator();
         while (l.hasNext()) {
             Object[] row = (Object[]) l.next();
             String loc = (String) row[0];
             String name = (String) row[1];
             
             
             System.out.println(name + ": " + loc);
         }
         try {
            OSM osm = OSMParser.parse("src/test/map.osm");
             System.out.println(osm.getNodes().iterator().next().tags);
 
         } catch (Exception ex) {
             Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
