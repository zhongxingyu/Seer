 package ch.opentrainingcenter.db.internal;
 
 import org.hibernate.HibernateException;
 import org.hibernate.MappingException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 
 import ch.opentrainingcenter.core.assertions.Assertions;
 import ch.opentrainingcenter.core.db.DatabaseConnectionConfiguration;
 import ch.opentrainingcenter.db.USAGE;
 
 public class Dao implements IDao {
 
     private Session session;
     private SessionFactory sessionFactory;
     private final USAGE usage;
     private final DatabaseConnectionConfiguration config;
 
     public Dao(final USAGE usage, final DatabaseConnectionConfiguration config) {
         Assertions.notNull(config, "Datenbankkonfiguration darf nicht null sein");
         this.usage = usage;
         this.config = config;
         final org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
         configuration.setProperties(config.getProperties());
         configuration.setProperty("current_session_context_class", "thread");
         configuration.setProperty("cache.provider_class", "org.hibernate.cache.NoCacheProvider");
         configuration.setProperty("show_sql", String.valueOf(usage.isShowSql()));
         configuration.setProperty("format_sql", String.valueOf(usage.isFormatSql()));
         try {
             configuration.addResource("Athlete.hbm.xml");//$NON-NLS-1$
             configuration.addResource("Health.hbm.xml"); //$NON-NLS-1$
             configuration.addResource("Weather.hbm.xml"); //$NON-NLS-1$
             configuration.addResource("Training.hbm.xml"); //$NON-NLS-1$
             configuration.addResource("Trainingtype.hbm.xml"); //$NON-NLS-1$
             configuration.addResource("Tracktrainingproperty.hbm.xml"); //$NON-NLS-1$
             configuration.addResource("Streckenpunkte.hbm.xml"); //$NON-NLS-1$
             configuration.addResource("Planungwoche.hbm.xml"); //$NON-NLS-1$
             configuration.addResource("Route.hbm.xml"); //$NON-NLS-1$
             sessionFactory = configuration.buildSessionFactory();
         } catch (final MappingException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public Transaction begin() {
         return getSession().beginTransaction();
     }
 
     @Override
     public void close() {
         getSession().close();
     }
 
     @Override
     public void commit() {
         getSession().getTransaction().commit();
     }
 
     @Override
     public Session getSession() {
        if (session == null) {
            try {
                session = sessionFactory.getCurrentSession();
            } catch (final org.hibernate.HibernateException he) {
                session = sessionFactory.openSession();
            }
            // session = sessionFactory.getCurrentSession();// openSession();
         }
         return session;
     }
 
     @Override
     public USAGE getUsage() {
         return usage;
     }
 
     @Override
     public void rollback() {
         try {
             getSession().getTransaction().rollback();
         } catch (final HibernateException e) {
             System.out.println(e.getMessage());
         }
         try {
             getSession().close();
         } catch (final HibernateException e) {
             System.out.println(e.getMessage());
         }
     }
 
     @Override
     public DatabaseConnectionConfiguration getConfig() {
         return config;
     }
 }
