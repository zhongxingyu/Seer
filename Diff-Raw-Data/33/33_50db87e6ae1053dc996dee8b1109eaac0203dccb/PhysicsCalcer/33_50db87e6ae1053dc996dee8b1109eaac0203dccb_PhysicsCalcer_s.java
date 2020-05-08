 package de.engine.physics;
 
 import de.engine.colldetect.CollisionData;
 import de.engine.colldetect.CollisionData.Contact;
 import de.engine.colldetect.ContactCreator;
 import de.engine.math.Util;
 import de.engine.math.Vector;
 import de.engine.objects.Circle;
 import de.engine.objects.ObjectProperties;
 import de.engine.objects.Polygon;
 
 public class PhysicsCalcer
 {
     
     public static void run(ObjectProperties obj1, ObjectProperties obj2, double collTime)
     {
         if (obj1 instanceof Circle && obj2 instanceof Circle)
         {
             PhysicsCalcer.calcCircles((Circle) obj1, (Circle) obj2, collTime);
         }
         else if (obj1 instanceof Circle && obj2 instanceof Polygon)
         {
             PhysicsCalcer.calcCirclePolygon((Circle) obj1, (Polygon) obj2, collTime);
         }
         else if (obj1 instanceof Polygon && obj2 instanceof Circle)
         {
             PhysicsCalcer.calcCirclePolygon((Circle) obj2, (Polygon) obj1, collTime);
         }
         else if (obj1 instanceof Polygon && obj2 instanceof Polygon)
         {
             PhysicsCalcer.calcPolygons((Polygon) obj2, (Polygon) obj1, collTime);
         }
     }
     
     private static void calcCircles(Circle c1, Circle c2, double collTime)
     {
 
         CollisionData cd = new CollisionData(c1, c2, collTime);
         ContactCreator.getCirclesContact(cd);
         
         if (cd.contacts.size() > 0)
         {
             resolveContact(c1, c2, collTime, cd.contacts.get(0));
         }
     }
     
     private static void calcCirclePolygon(Circle o1, Polygon o2, double collTime)
     {
         CollisionData cd = new CollisionData(o1, o2, collTime);
         ContactCreator.getCirclePolygonContact(cd);
         
         if (cd.contacts.size() > 0)
         {
             resolveContact(o1, o2, collTime, cd.contacts.get(0));
         }
     }
     
     private static void calcPolygons(Polygon o1, Polygon o2, double collTime)
     {
         CollisionData cd = new CollisionData(o1, o2, collTime);
         ContactCreator.getPolygonsContact(cd);
         
         if(cd.contacts.size() == 1)
         {
             resolveContact(o1, o2, collTime, cd.contacts.get(0));
         }
         else if(cd.contacts.size() == 2)
         {
             if(cd.contacts.get(0).normal.getX() == cd.contacts.get(1).normal.getX() && cd.contacts.get(0).normal.getY() == cd.contacts.get(1).normal.getY())
             {
                 Contact c = new Contact(Util.add(cd.contacts.get(0).point, cd.contacts.get(1).point).scale(0.5), cd.contacts.get(0).normal);
                 resolveContact(o1, o2, collTime, c);
             }
         }
     }
     
     //Quelle: http://www.myphysicslab.com/collision.html
     private static void resolveContact(ObjectProperties o1, ObjectProperties o2, double collTime, CollisionData.Contact contact)
     {
         Vector coll_point = contact.point;
         Vector coll_normal = contact.normal;
         
         Vector r_o1 = Util.minus(coll_point, o1.getPosition(collTime));
         Vector r_o2 = Util.minus(coll_point, o2.getPosition(collTime));
         Vector v_o1 = new Vector(o1.angular_velocity * r_o1.getX(), o1.angular_velocity * r_o1.getY()).add(o1.velocity);
         Vector v_o2 = new Vector(o2.angular_velocity * r_o2.getX(), o2.angular_velocity * r_o2.getY()).add(o2.velocity);
 
         double r_o1_cross_n = Util.crossProduct(r_o1, coll_normal);
         double r_o2_cross_n = Util.crossProduct(r_o2, coll_normal);
 
         Vector v_rel = Util.minus(v_o1, v_o2);
         double j_z = Util.scalarProduct(v_rel, coll_normal) *  -(1 + (o1.surface.elasticity() + o2.surface.elasticity()) / 2);
         double j_n;
         
         if(o1.isPinned)
             j_n = (1 / o2.getMass()) + (r_o2_cross_n * r_o2_cross_n) / o2.moment_of_inertia;
         else if(o2.isPinned)
             j_n = (1 / o1.getMass()) + (r_o1_cross_n * r_o1_cross_n) / o1.moment_of_inertia;
         else
             j_n = (1 / o1.getMass()) + (1 / o2.getMass()) + (r_o1_cross_n * r_o1_cross_n) / o1.moment_of_inertia + (r_o2_cross_n * r_o2_cross_n) / o2.moment_of_inertia;
         
         double j = j_z / j_n;
         
         Vector j_normal = Util.scale(coll_normal, j);
         
         if(!o1.isPinned) {
             Vector v_o1_n = Util.add(o1.velocity, Util.scale(j_normal, 1 / o1.getMass()));
             double ang_v_o1_n = o1.angular_velocity + Util.crossProduct(r_o1, j_normal) / o1.moment_of_inertia;
             
             o1.next_velocity = v_o1_n;
             o1.next_angular_velocity = ang_v_o1_n;
             o1.next_time = collTime;
             o1.update(collTime);
         }
         
         if(!o2.isPinned) {
             Vector v_o2_n = Util.minus(o2.velocity, Util.scale(j_normal, 1 / o2.getMass()));
             double ang_v_o2_n = o2.angular_velocity - Util.crossProduct(r_o2, j_normal) / o2.moment_of_inertia;
             
             o2.next_velocity = v_o2_n;
             o2.next_angular_velocity = ang_v_o2_n;
             o2.next_time = collTime;
             o2.update(collTime);
         }
     }
 }
