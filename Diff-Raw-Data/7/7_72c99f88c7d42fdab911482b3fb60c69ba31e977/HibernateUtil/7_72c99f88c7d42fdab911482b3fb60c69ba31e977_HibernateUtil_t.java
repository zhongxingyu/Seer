 package connectfour.persistence.hibernate;
 
 import connectfour.model.Computer;
 import connectfour.model.Human;
 import connectfour.model.Player;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.AnnotationConfiguration;
 
 
 public final class HibernateUtil {
     private static final SessionFactory sessionFactory;
 
     static {
         final AnnotationConfiguration cfg = new
                 AnnotationConfiguration();
         cfg.configure("/hibernate.cfg.xml");
         sessionFactory = cfg.buildSessionFactory();
     }
 
     private HibernateUtil() {
     }
 
     public static SessionFactory getInstance() {
         return sessionFactory;
     }
 
     public static PlayerHibernate convertToPlayerHibernate(Player player) {
        if (player == null) { return null;}
 
        String playerName = player.getName();
         return new PlayerHibernate(playerName, player instanceof Computer);
     }
 
     public static Player convertToStandardPlayer(PlayerHibernate playerHibernate) {
        if (playerHibernate == null) { return null; }

         Player player;
         if (playerHibernate.isComputer()) {
             player = new Computer(null, playerHibernate.getName());
         } else {
             player = new Human(playerHibernate.getName());
         }
         return player;
     }
 }
