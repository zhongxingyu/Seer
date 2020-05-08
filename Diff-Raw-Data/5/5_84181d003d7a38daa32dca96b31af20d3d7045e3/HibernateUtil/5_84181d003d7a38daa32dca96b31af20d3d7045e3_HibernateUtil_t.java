 package br.com.caelum.fixtureFactory.infrastructure.persistence.hibernate;
 
 import java.net.URL;
 
 import org.apache.log4j.Logger;
 import org.hibernate.FlushMode;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.service.ServiceRegistryBuilder;
 
 import br.com.caelum.fixtureFactory.domain.model.Address;
 import br.com.caelum.fixtureFactory.domain.model.City;
 import br.com.caelum.fixtureFactory.domain.model.State;
 import br.com.caelum.fixtureFactory.domain.model.User;
 
 public class HibernateUtil {
 	private static Logger logger = Logger.getLogger(HibernateUtil.class);
 	private static final SessionFactory sessionFactory;
 	public static final ThreadLocal<Session> session = new ThreadLocal<Session>();
 	public static final ThreadLocal<Transaction> transaction = new ThreadLocal<Transaction>();
 
 	/**
 	 * Inicializa as configuracoes do hibernate. Mapeia as entidades utilizadas
 	 * atraves da chave <b>hibernate.mapped.packages</b> no
 	 * <b>hibernate.properties</b>
 	 */
 	static {
 		try {
 			HibernateUtil.logger.info("Iniciando configuracao do Hibernate");
 			Configuration cfg = new Configuration()
 					.configure(getHibernateCfgLocation());
 			HibernateUtil.logger.info("Configurando entidades..");
 
 			cfg.addAnnotatedClass(User.class);
 			cfg.addAnnotatedClass(Address.class);
 			cfg.addAnnotatedClass(State.class);
 			cfg.addAnnotatedClass(City.class);
 
 			sessionFactory = cfg
 					.buildSessionFactory(new ServiceRegistryBuilder()
 							.applySettings(cfg.getProperties())
 							.buildServiceRegistry());
 
 			HibernateUtil.logger.info("Fim da configuracao do Hibernate");
 		} catch (Throwable ex) {
 			HibernateUtil.logger.error("Erro ao configurar Hibernate", ex);
 			throw new ExceptionInInitializerError(ex);
 		}
 	}
 
 	private static URL getHibernateCfgLocation() {
 		return HibernateUtil.class.getResource(getHibernateCfgName());
 	}
 
 	private static String getHibernateCfgName() {
 		return "/hibernate.cfg.xml";
 	}
 
 	/**
 	 * Retorna o {@link Session} atual para a Thread ou abre um caso nao exista
 	 * 
 	 * @return {@link Session} correspondente a Thread
 	 */
 	public static Session currentSession() {
 		Session s = HibernateUtil.session.get();
 		if (s == null) {
 			s = HibernateUtil.sessionFactory.openSession();
 			s.setFlushMode(FlushMode.ALWAYS);
 			HibernateUtil.session.set(s);
 		}
 		return s;
 	}
 
 	/**
 	 * Fecha a sessao atual pertencente a Thread
 	 */
 	public static void closeSession() {
 		Session s = HibernateUtil.session.get();
 		if (s != null) {
 			s.close();
 		}
 		HibernateUtil.session.set(null);
 	}
 
 	/**
 	 * Inicia uma transacao para o {@link Session} pertencente a Thread
 	 * 
 	 * @throws IllegalStateException
 	 *             caso a {@link Session} seja NULL ou esteja fechada
 	 */
 	public static void beginTransaction() {
 		Session s = HibernateUtil.session.get();
 		if (s != null && s.isOpen()) {
 			HibernateUtil.transaction.set(HibernateUtil.currentSession()
 					.beginTransaction());
 		} else {
 			HibernateUtil.logger.error("Erro ao abrir transacao no Hibernate");
 			throw new IllegalStateException("Erro ao abrir transacao");
 		}
 	}
 
 	/**
 	 * Comita uma transacao aberta para a Thread
 	 * 
 	 * @throws IllegalStateException
 	 *             caso o {@link Session} seja NULL ou esteja fechado ou caso a
 	 *             transacao ja tenha sido comitada ou feito o rollback
 	 */
 	public static void commitTransaction() {
 		Session s = HibernateUtil.session.get();
 		Transaction t = HibernateUtil.transaction.get();
 		if (s != null && s.isOpen() && t != null && !t.wasCommitted()
 				&& !t.wasRolledBack()) {
 			t.commit();
 		} else {
 			HibernateUtil.logger
 					.error("Erro ao comitar transacao do Hibernate");
 			throw new IllegalStateException("Erro ao comitar transacao");
 		}
 	}
 
 	/**
 	 * Executa o rollback em uma transacao aberta para a Thread
 	 * 
 	 * @throws IllegalStateException
 	 *             caso o {@link Session} seja NULL ou esteja fechado ou caso a
 	 *             transacao ja tenha sido comitada ou feito o rollback
 	 */
 	public static void rollbackTransaction() {
 		Session s = HibernateUtil.session.get();
 		Transaction t = HibernateUtil.transaction.get();
 		if (s != null && s.isOpen() && t != null && !t.wasCommitted()
 				&& !t.wasRolledBack()) {
 			t.rollback();
 		} else {
 			HibernateUtil.logger
 					.error("Erro no rollback da transacao do Hibernate");
 			throw new IllegalStateException("Erro no rollback da transacao");
 		}
 	}
 
 }
