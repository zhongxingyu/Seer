 package de.engine.colldetect;
 
 import java.util.ArrayList;
 
 import de.engine.colldetect.CollisionData.Contact;
 import de.engine.math.Util;
 import de.engine.math.Vector;
 import de.engine.objects.Polygon;
 
 public class ContactCreator
 {
     public static void getCirclesContact(CollisionData cd)
     {
         Vector pos_o2 = cd.o2.getPosition(cd.time);
         Vector pos_o1 = cd.o1.getPosition(cd.time);
         Vector dist = Util.minus(pos_o1, pos_o2);
         Vector normal = dist.getUnitVector();
         Vector coll_point = Util.add(pos_o2, dist.scale(cd.o2.getRadius() / dist.getLength()));
         cd.contacts.add(new Contact(coll_point, normal));
         
     }
     
     public static void getCirclePolygonContact(CollisionData cd)
     {
         Vector circle_pos = cd.o1.getPosition(cd.time);
         for (int i = 0; i < ((Polygon)cd.o2).points.length; i++)
         {
             int j = (i == (((Polygon)cd.o2).points.length - 1)) ? 0 : i + 1;
             Vector point_pos = ((Polygon)cd.o2).getWorldPointPos(i, cd.time);
             Vector edge_ray = Util.minus(((Polygon)cd.o2).getWorldPointPos(j, cd.time), point_pos);
             Vector pos_ray = edge_ray.getNormalVector().getUnitVector();
             
             Vector coll_point = Util.crossEdges(circle_pos, Util.scale(pos_ray, -1 * cd.o1.getRadius()), point_pos, edge_ray);
             if (coll_point != null)
             {
                 cd.contacts.add(new Contact(coll_point, pos_ray));
                 return;
             } else {
                 Vector point_col = Util.minus(point_pos, circle_pos);
                 if(point_col.getLength() <= cd.o1.getRadius()){
                     cd.contacts.add(new Contact(point_pos, point_col.getUnitVector()));
                     return;
                 }
             }
         }
     }
     
     public static void getPolygonsContact(CollisionData cd)
     {
         ArrayList<Contact> contacts = searchContact((Polygon)cd.o1, (Polygon)cd.o2, cd.time);
         for (Contact contact : contacts)
         {
             cd.contacts.add(contact);
         }
        contacts = searchContact((Polygon)cd.o2, (Polygon)cd.o1, cd.time);
         for (Contact contact : contacts)
         {
             cd.contacts.add(contact);
         }
     }
     
     private static ArrayList<Contact> searchContact(Polygon o1, Polygon o2, double time)
     {
         ArrayList<Contact> contacts = new ArrayList<>();
         for (int i = 0; i < o1.points.length; i++)
         {
            Vector corner_o1 = o1.getWorldPointPos(i, time);
             for (int k = 0; k < o2.points.length; k++)
             {
                 int l = (k == (o2.points.length - 1)) ? 0 : k + 1;
                 Vector corner_o2 = o2.getWorldPointPos(k, time);
                 //Vector dist = Util.minus(corner_o1, corner_o2);
                 Vector edge = o2.getWorldPointPos(l, time).minus(corner_o2);
                 Vector edge_normal = edge.getNormalVector().getUnitVector();
                 //double distance = Util.scalarProduct(dist, ray_normal);
                 Vector cross_point = Util.crossEdges(corner_o1, edge_normal, corner_o2, edge);
                if(cross_point != null)// && Util.scalarProduct(edge_normal, Util.minus(o1.getPosition(time), corner_o1)) > 0)
                 {
                     contacts.add(new Contact(cross_point, edge_normal));
                 }
             }
         }
         return contacts;
     }
 }
