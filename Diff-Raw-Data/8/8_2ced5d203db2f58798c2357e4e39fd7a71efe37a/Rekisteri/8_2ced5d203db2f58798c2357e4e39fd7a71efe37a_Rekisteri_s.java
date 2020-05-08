 package ohtu.viitearto;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
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
         em.merge(uusi);
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
     
     public List<Viite> haeViiteTageilla(String viiteTyyppi, String... sanat) {
         em = getEntityManager();
         
         Query q = em.createQuery("SELECT t FROM Tag t");
         
         List<Tag> results = q.getResultList();  
         HashSet<Viite> refResults = new HashSet<Viite>();
         
         for (Tag t : results) { // loopataan kaikki tagit
             for (String hakusana : sanat) {
                 if (t.getNimi().contains(hakusana)) // jos tagin nimessä hakusana
                     refResults.addAll(t.getViitteet()); // lisätään hakutuloksiin viitteet, joilla on kys. tag
             }
         }
         
         if (viiteTyyppi != null && viiteTyyppi.length() > 0) {
             Iterator i = refResults.iterator();
 
             while (i.hasNext()) {
                 Viite viite = (Viite) i.next();
                 if (!viite.getType().equals(viiteTyyppi))
                     i.remove();
             }
         }
         
         return new ArrayList<Viite>(refResults);
     }
     
     public List<Viite> haeViiteHakuSanoilla(String viiteTyyppi, String kentta, String... sanat) {
         em = getEntityManager();
         
         Query q = null;
         
         if (viiteTyyppi != null && viiteTyyppi.length() > 0) {
             q = em.createQuery("SELECT v FROM Viite v WHERE v.type = :typeParam");
             q.setParameter("typeParam", viiteTyyppi);
         } else {
             q = em.createQuery("SELECT v FROM Viite v");
         }
         
        if (kentta.equals("all"))
            return haeKaikkienKenttienPerusteella(q, sanat);
        else
             return haeYhdenKentanPerusteella(sanat, q, kentta);
     }
     
     private List<Viite> haeKaikkienKenttienPerusteella(Query q, String... sanat) {    
         List<Viite> results = q.getResultList();    
         HashSet<Viite> finalResults = new HashSet<Viite>(); // talletetaan lopulliset hakutulokset
                
         for (Viite v : results) {    
             for (String field : v.getFields().values()) {
                 for (String hakusana : sanat) {
                     if (field == null || hakusana == null)
                         continue;
                     if (field.contains(hakusana)) {
                         finalResults.add(v);
                     }
                 }
             }
         }
         return new ArrayList<Viite>(finalResults);
     }
 
     private List<Viite> haeYhdenKentanPerusteella(String[] sanat, Query q, String kentta) {
         List<Viite> results = q.getResultList();
         HashSet<Viite> finalResults = new HashSet<Viite>(); // talletetaan lopulliset hakutulokset
 
         for (Viite v : results) {
             for (String hakusana : sanat) {
                 if (v.getField(kentta).contains(hakusana))
                     finalResults.add(v);
             }
             
         }
         return new ArrayList<Viite>(finalResults);
     }
 }
