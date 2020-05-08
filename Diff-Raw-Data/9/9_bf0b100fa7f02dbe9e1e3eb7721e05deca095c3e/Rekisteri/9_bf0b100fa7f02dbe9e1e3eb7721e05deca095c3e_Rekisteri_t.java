 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.viitearto;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 /**
  *
  * @author Keni
  */
 public class Rekisteri {
 
     private static Rekisteri instance;
     private EntityManager em;
     
     private EntityManagerFactory emf = null;
 
     public static Rekisteri getInstance() {
         if (instance == null) {
             instance = new Rekisteri();
         }
         
         return instance;
     }
     
     private Rekisteri() {
         // käytetään "ViiteArtoPU"-konfiguraatiota    
         emf = Persistence.createEntityManagerFactory("ViiteArtoPU");
     }
     
     public EntityManager getEntityManager() {
         return emf.createEntityManager();
     }
     
     public void lisaaViite(Viite viite) {
         em = getEntityManager();
         
         em.getTransaction().begin();
         em.merge(viite);
         em.getTransaction().commit();
     }
     
     public void lisaaTagi(Tag uusi) {
         em = getEntityManager();
         em.getTransaction().begin();
         em.persist(uusi);
         em.getTransaction().commit();
     }
     
     public List<Viite> getViitteet() {
         em = getEntityManager();
         return em.createQuery("SELECT v FROM Viite v").getResultList();
     }
 
     public void poistaViite(long id) {
         em = getEntityManager();
         em.getTransaction().begin();
 
         Viite poistettava = em.find(Viite.class, id);
         List<Tag> tagit = poistettava.getTagit();
 
         if (tagit != null) {
             for (int i = 0; i < tagit.size(); ++i) {
                 tagit.get(i).getViitteet().remove(poistettava); // tuhotaan tageilta tiedot poistettavaan
             }                                                   // viitteeseen
 
             poistettava.setTagit(null); // poistetaan tiedot tageihin poistettavalta viitteeltä
         }
         em.remove(poistettava);
         em.getTransaction().commit();
     }
 
     public Viite haeViite(long id) {
         em = getEntityManager();
         
         return em.find(Viite.class, id);
     }
     
     public Tag haeTag(String tunniste) {
         em = getEntityManager();
         
         return em.find(Tag.class, tunniste);
     }
 
     public List<Viite> haeViiteYhdellaHakuSanalla(String haku, String viiteTyyppi, String kentta) {
         em = getEntityManager();
         
         Query q = null;
         
         if (viiteTyyppi != null) {
             q = em.createQuery("SELECT v FROM Viite v WHERE v."+kentta+" LIKE :fieldParam and v.type = :typeParam");
             q.setParameter("fieldParam", haku + "%").setParameter("typeParam", viiteTyyppi);
         } else {
             q = em.createQuery("SELECT v FROM Viite v WHERE v."+kentta+" LIKE :fieldParam");
             q.setParameter("fieldParam", haku + "%");
         }
         return q.getResultList();
     }
     
     public List<Viite> haeViiteKahdellaHakuSanalla(String ekaHaku, String tokaHaku, String viiteTyyppi, String ekaKentta, String tokaKentta, String operand) {
         em = getEntityManager();
         
         Query q = null;
         
         if (viiteTyyppi != null) {
            q = em.createQuery("SELECT v FROM Viite v WHERE v."+ekaKentta+" LIKE :firstParam "+operand+" v."+tokaKentta+" LIKE :secondParam and v.type = :typeParam");
             q.setParameter("firstParam", ekaHaku + "%").setParameter("secondParam", tokaHaku+"%").setParameter("typeParam", viiteTyyppi);
         } else {
             q = em.createQuery("SELECT v FROM Viite v WHERE v."+ekaKentta+" LIKE :firstParam "+operand+" v."+tokaKentta+" LIKE :secondParam");
             q.setParameter("firstParam", ekaHaku + "%").setParameter("secondParam", tokaHaku+"%");
         }
         return q.getResultList();
     }
 
     public List<Viite> haeViiteTageilla(String haku, String viiteTyyppi) {
         List<Viite> haettavat = haeTag(haku).getViitteet();
         List<Viite> kopio = new ArrayList<Viite>();
         
         for (Viite viite : haettavat) {
             kopio.add(viite);
         }  
         
         if (viiteTyyppi != null) {
             for (int i=0; i < kopio.size(); ++i) {
                 if (!kopio.get(i).getType().equals(viiteTyyppi)) {
                     kopio.remove(kopio.get(i));
                 }
             }
         }
         
         return kopio;
     }
     
     public List<Viite> haeViiteTagJaHakusana(String ekaHaku, String tokaHaku, String viiteTyyppi, String ekaKentta, String tokaKentta, String operand) {
 
         if (ekaKentta.equals("tag")) {
 
             if (haeTag(ekaHaku) == null || haeTag(ekaHaku).getViitteet() == null) {
             } else {
 
                 List<Viite> haettavat = haeTag(ekaHaku).getViitteet(); // tagien perusteella haetut
 
                 List<Viite> kopio = new ArrayList<Viite>();
 
                 for (Viite viite : haettavat) {
                     kopio.add(viite);
                 }
 
                 if (viiteTyyppi != null) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getType().equals(viiteTyyppi)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 }
 
                 if (tokaKentta.equals("author")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getAuthor().contains(tokaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (tokaKentta.equals("address")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getAddress().contains(tokaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (tokaKentta.equals("title")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getTitle().contains(tokaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (tokaKentta.equals("booktitle")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getBooktitle().contains(tokaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (tokaKentta.equals("journal")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getJournal().contains(tokaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (tokaKentta.equals("publisher")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getPublisher().contains(tokaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 }
 
                 return kopio;
             }
         } else if (tokaKentta.equals("tag")) {
 
             if (haeTag(tokaHaku) == null || haeTag(tokaHaku).getViitteet() == null) {
             } else {
 
                 List<Viite> haettavat = haeTag(tokaHaku).getViitteet(); // tagien perusteella haetut
 
                 List<Viite> kopio = new ArrayList<Viite>();
 
                 for (Viite viite : haettavat) {
                     kopio.add(viite);
                 }
 
                 if (viiteTyyppi != null) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getType().equals(viiteTyyppi)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 }
 
                 if (ekaKentta.equals("author")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getAuthor().contains(ekaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (ekaKentta.equals("address")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getAddress().contains(ekaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (ekaKentta.equals("title")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getTitle().contains(ekaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (ekaKentta.equals("booktitle")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getBooktitle().contains(ekaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (ekaKentta.equals("journal")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getJournal().contains(ekaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 } else if (ekaKentta.equals("publisher")) {
                     for (int i = 0; i < kopio.size(); ++i) {
                         if (!kopio.get(i).getPublisher().contains(ekaHaku)) {
                             kopio.remove(kopio.get(i));
                         }
                     }
                 }
 
                 return kopio;
             }
         }
 
         if (ekaKentta.equals("tag")) {
             em = getEntityManager();
             Query q;
 
             if (viiteTyyppi != null) {
                 q = em.createQuery("SELECT v FROM Viite v WHERE v." + tokaKentta + " LIKE :firstParam " + operand + " v.type = :typeParam");
                 q.setParameter("firstParam", tokaHaku + "%").setParameter("typeParam", viiteTyyppi);
             } else {
                 q = em.createQuery("SELECT v FROM Viite v WHERE v." + tokaKentta + " LIKE :firstParam");
                 q.setParameter("firstParam", tokaHaku + "%");
             }
 
             return q.getResultList();
         } else if (tokaKentta.equals("tag")) {
             em = getEntityManager();
             Query q;
 
             if (viiteTyyppi != null) {
                 q = em.createQuery("SELECT v FROM Viite v WHERE v." + ekaKentta + " LIKE :firstParam " + operand + " v.type = :typeParam");
                 q.setParameter("firstParam", ekaHaku + "%").setParameter("typeParam", viiteTyyppi);
             } else {
                 q = em.createQuery("SELECT v FROM Viite v WHERE v." + ekaKentta + " LIKE :firstParam");
                 q.setParameter("firstParam", ekaHaku + "%");
             }
 
             return q.getResultList();
         }
 
         return null;
     }
 }
