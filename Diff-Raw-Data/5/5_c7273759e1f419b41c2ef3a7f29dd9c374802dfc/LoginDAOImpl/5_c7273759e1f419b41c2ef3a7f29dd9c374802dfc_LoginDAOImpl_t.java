 package com.project.dao;
 
 import java.util.List;
 
 import org.apache.maven.artifact.versioning.Restriction;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.exception.ConstraintViolationException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.TransactionException;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.project.data.GrupyZajeciowe;
 import com.project.data.Kursy;
 import com.project.data.Prowadzacy;
 import com.project.data.Studenci;
 
 @Repository
 public class LoginDAOImpl implements LoginDAO {
 
 	@Autowired
 	private SessionFactory sessionFactory;
 
 	@Transactional
 	public void addProwadzacy(Prowadzacy prowadzacy) {
 		
 		Session session = sessionFactory.openSession();
 		Transaction tx = session.beginTransaction();
 		sessionFactory.getCurrentSession().save(prowadzacy);
 		tx.commit();
 		session.close();
 	}
 
 	@Transactional
 	public List<Kursy> listKursy() {
 
 		return sessionFactory.getCurrentSession().createQuery("from Kursy")
 				.list();
 
 	}
 
 	@Transactional
 	public List<GrupyZajeciowe> listGrupyZajeciowe() {
 		return sessionFactory.getCurrentSession()
 				.createQuery("from GrupyZajeciowe").list();
 
 	}
 
 	@Transactional
 	public List<Studenci> listStudenci() {
 		return sessionFactory.getCurrentSession().createQuery("from Studenci")
 				.list();
 
 	}
 
 	@Transactional
 	public List<Prowadzacy> listProwadzacy() {
 
 		return sessionFactory.getCurrentSession()
 				.createQuery("from Prowadzacy").list();
 	}
 
 	@Transactional
 	public List<Prowadzacy> validateLogin(String login, String haslo) {
 
 		return sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"from Prowadzacy where login=:login and haslo=:haslo")
 				.setString("login", login).setString("haslo", haslo).list();
 
 	}
 
 	@Transactional
 	public List<Prowadzacy> validatePass(Integer userid, String haslo) {
 
 		return sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"from Prowadzacy where idProwadzacy=:userid and haslo=:haslo")
 				.setInteger("userid", userid).setString("haslo", haslo).list();
 
 	}
 
 	@Transactional
 	public List<Prowadzacy> logout(Integer userid) {
 
 		return sessionFactory.getCurrentSession()
 				.createQuery("from Prowadzacy where idProwadzacy=:userid")
 				.setInteger("userid", userid).list();
 
 	}
 
 	@Transactional
 	public List<Kursy> validateKursy(String kodKursu, String nazwaKursu) {
 
 		return sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"from Kursy where kodKursu=:kodKursu and nazwaKursu=:nazwaKursu")
 				.setString("kodKursu", kodKursu)
 				.setString("nazwaKursu", nazwaKursu).list();
 
 	}
 
 	@Transactional
 	public List<Prowadzacy> validatePname(String imiona, String nazwisko) {
 
 		// sessionFactory.getCurrentSession().createCriteria(Prowadzacy.class)
 		// .add(Restrictions.and(Restrictions.eq("imiona", imiona),
 		// Restrictions.eq("nazwisko", nazwisko))).list();
 		return sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"from Prowadzacy where imiona=:imiona and nazwisko=:nazwisko")
 				.setString("imiona", imiona).setString("nazwisko", nazwisko)
 				.list();
 
 	}
 
 	@Transactional
 	public List<GrupyZajeciowe> validateGrupyza(String kod_grupy, String termin) {
 
 		return sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"from GrupyZajeciowe where kodGrupy=:kod_grupy and termin=:termin")
 				.setString("kod_grupy", kod_grupy).setString("termin", termin)
 				.list();
 
 	}
 
 	@Transactional
 	public List<Studenci> validateSname(String nr_indeksu) {
 
 		return sessionFactory.getCurrentSession()
 				.createQuery("from Studenci where nrIndeksu=:nr_indeksu")
 				.setString("nr_indeksu", nr_indeksu).list();
 
 	}
 
 	@Transactional
 	public void aktywacja(String Login, boolean Aktywowany) {
 
 		Session session = sessionFactory.openSession();
 		Transaction tx = session.beginTransaction();
 		sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"UPDATE Prowadzacy SET Aktywowany=:Aktywowany WHERE Login=:Login")
 				.setString("Login", Login).setBoolean("Aktywowany", Aktywowany)
 				.executeUpdate();
 		tx.commit();
 		session.close();
 	}
 
 	@Transactional
 	public void zmienPass(Integer iduser, String haslo) {
 
 		Session session = sessionFactory.openSession();
 		Transaction tx = session.beginTransaction();
 		sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"UPDATE Prowadzacy SET haslo=:haslo WHERE idProwadzacy=:iduser")
 				.setInteger("iduser", iduser).setString("haslo", haslo)
 				.executeUpdate();
 		tx.commit();
 		session.close();
 	}
 
 	@Transactional
 	public List<Prowadzacy> validateRegister(String imiona, String nazwisko,
 			String email, String login) {
 		return sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"from Prowadzacy where imiona=:imiona and nazwisko=:nazwisko and email=:email and login=:login")
 				.setString("imiona", imiona).setString("nazwisko", nazwisko)
 				.setString("email", email).setString("login", login).list();
 	}
 
 	@Transactional
 	public void addKursy(String kodkursu, String nazwakursu) {
 
 		Session session = sessionFactory.openSession();
 		Transaction tx = session.beginTransaction();
 
		String query = "insert ignore into Kursy(Kod_kursu,Nazwa_kursu) values(:kodkursu,:nazwakursu)";
 
		sessionFactory.getCurrentSession().createSQLQuery(query)
 				.setParameter("kodkursu", kodkursu)
 				.setParameter("nazwakursu", nazwakursu).executeUpdate();
 
 		tx.commit();
 		session.close();
 
 		/*
 		 * Kursy kursy = new Kursy(); kursy.setKodKursu(kodkursu);
 		 * kursy.setNazwaKursu(nazwakursu); try {
 		 * sessionFactory.getCurrentSession().saveOrUpdate(kursy); } catch
 		 * (HibernateException e) { }
 		 */
 	}
 
 	@Transactional
 	public void addGrupyZajeciowe(String kodGrupy, String infoEdu,
 			Integer idProwadzacego, Integer idKursu, String nazwa,
 			String termin, String komentarz) {
 
 		// String query =
 		// "insert ignore into GrupyZajeciowe(kodGrupy,infoEdu,idProwadzacego, idKursu, nazwa, termin, komentarz) values(:kodGrupy,:infoEdu,:idProwadzacego,:idKursu,:nazwa,:termin,:komentarz)";
 		Session session = sessionFactory.openSession();
 		Transaction tx = session.beginTransaction();
 
 		String query = "insert ignore into grupy_zajeciowe("
 				+ "Kod_grupy, "
 				+ "Info_Edukacja, "
 				+ "id_Prowadzacego, "
 				+ "id_Kursu, "
 				+ "Nazwa, "
 				+ "Termin, "
 				+ "Komentarz)"
 				+ " values(:kodGrupy,:infoEdu,:idProwadzacego,:idKursu,:nazwa,:termin,:komentarz)";
 
 		sessionFactory.getCurrentSession().createSQLQuery(query)
 				.setString("kodGrupy", kodGrupy).setString("infoEdu", infoEdu)
 				.setInteger("idProwadzacego", idProwadzacego)
 				.setInteger("idKursu", idKursu).setString("nazwa", nazwa)
 				.setString("termin", termin).setString("komentarz", komentarz)
 				.executeUpdate();
 		tx.commit();
 		session.close();
 	}
 
 	@Transactional
 	public void addStudenciDoGrupZajeciowych(Integer idStudenta,
 			Integer idGrupyOryginalnej, Integer idGrupyChodzacej) {
 		Session session = sessionFactory.openSession();
 		Transaction tx = session.beginTransaction();
 		// String query =
 		// "insert ignore into kursy(idStudenta,idGrupyOryginalnej,idGrupyChodzacej) values(:idStudenta,:idGrupyOryginalnej,:idGrupyChodzacej)";
 		String query = "insert into studenci_do_grup_zajeciowych(id_studenta,id_grupy_oryginalnej,id_grupy_chodzacej) values(:idStudenta,:idGrupyOryginalnej,:idGrupyChodzacej)";
 
 		sessionFactory.getCurrentSession().createSQLQuery(query)
 				.setParameter("idStudenta", idStudenta)
 				.setParameter("idGrupyOryginalnej", idGrupyOryginalnej)
 				.setParameter("idGrupyChodzacej", idGrupyChodzacej)
 				.executeUpdate();
 		tx.commit();
 		session.close();
 	}
 
 	@Transactional
 	public void addStudenci(String imie, String nazwisko, String nrIndeksu,
 			String email, Integer rok, Integer semestr, String przedmiot,
 			String login, String haslo) {
 
 		Session session = sessionFactory.openSession();
 		Transaction tx = session.beginTransaction();
 		String query = "insert into studenci(imiona,nazwisko,nr_indeksu, email, rok, semestr, przedmiot_krztalcenia, login, haslo) values(:imie,:nazwisko,:nrIndeksu,:email,:rok,:semestr,:przedmiot,:login,:haslo)";
 
 		sessionFactory.getCurrentSession().createSQLQuery(query)
 				.setParameter("imie", imie).setParameter("nazwisko", nazwisko)
 				.setParameter("nrIndeksu", nrIndeksu)
 				.setParameter("email", email).setParameter("rok", rok)
 				.setParameter("semestr", semestr)
 				.setParameter("przedmiot", przedmiot)
 				.setParameter("login", login).setParameter("haslo", haslo)
 				.executeUpdate();
 		tx.commit();
 		session.close();
 	}
 }
