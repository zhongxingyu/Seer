 package Controller;
 
 import Model.*;
 import Persistence.HibernateUtil;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Package: Controller
  * User: Mathias
  * Date: 24-10-12
  * Time: 20:51
  */
 public class VerkoopController {
 
     private Session session;
 
 
     public VerkoopController() {
         session = HibernateUtil.getSessionFactory().getCurrentSession();
 
     }
     public List<Verkoop> getVerkopen(){
         Transaction tx = session.beginTransaction();
         Query query = session.createQuery("from Verkoop");
         List<Verkoop> verkopen = (List<Verkoop>) query.list();
         tx.commit();
         return verkopen;
     }
 
     public double voegVerkoopToe(Voorstelling v, Object k, int aantal, VerkoopKanaal vk, Zetel z, Zone zone){
         try{
             double totaalPrijs = 0;
             Transaction tx = session.beginTransaction();
             TicketController tc = new TicketController();
             Verkoop verkoop;
             if (k != null && k instanceof Klant){
                 verkoop = new Verkoop(new Date(), vk, (Klant) k);
             }
             else{
                 verkoop = new Verkoop(new Date(), vk, null);
             }
             Set s = new HashSet();
             if (!v.isZetelReservatie()){
                 z = null;
             }
            double prijs = berekenTicketPrijs(zone, v);
             for (int i = 0; i < aantal; i++){
                 totaalPrijs += prijs;
                 Ticket t = tc.voegTicketToe(v, z, prijs, verkoop);
                 s.add(t);
                 session.saveOrUpdate(t);
             }
             verkoop.setTickets(s);
             session.saveOrUpdate(verkoop);
             tx.commit();
             return totaalPrijs;
         } catch (Exception e){
             return -1;
         }
     }
 
     public double berekenTicketPrijs(Zone zone, Voorstelling v){   //TODO UITWERKEN
         double prijs = 0;
         Query query = session.createQuery("from Holding");
         Holding holding = (Holding) query.uniqueResult();
         if (v.getFilm().isFilm3D()){
             prijs += holding.getToelage3D();
         }
         if (zone != null){
             prijs += zone.getZonetype().getPrijs();
         }
         else{
             ZoneTypeController zc = new ZoneTypeController();
             prijs += zc.getNormaalZonePrijs();
         }
         return prijs;
     }
 }
