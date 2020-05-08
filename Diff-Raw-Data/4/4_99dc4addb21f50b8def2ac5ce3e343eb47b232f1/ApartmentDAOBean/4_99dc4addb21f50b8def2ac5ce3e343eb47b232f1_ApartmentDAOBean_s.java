 package com.hansson.rento.dao;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang3.text.WordUtils;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Projection;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.transform.Transformers;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.hansson.rento.entities.Apartment;
 import com.hansson.rento.utils.HtmlUtil;
 
 public class ApartmentDAOBean implements ApartmentDAO {
 
 	private SessionFactory mSessionFactory;
 
 	@Override
 	@Transactional
 	public Apartment create(Apartment apartment) {
 		Session session = mSessionFactory.getCurrentSession();
 		return (Apartment) session.merge(apartment);
 	}
 
 	@Override
 	@Transactional
 	public void delete(Apartment apartment) {
 		Session session = mSessionFactory.getCurrentSession();
 		session.delete(apartment);
 	}
 
 	@Override
 	@Transactional
 	public Apartment update(Apartment apartment) {
 		Session session = mSessionFactory.getCurrentSession();
 		return (Apartment) session.merge(apartment);
 	}
 
 	@Override
 	@Transactional
 	public Apartment find(int id) {
 		Session session = mSessionFactory.getCurrentSession();
 		Query query = session.createQuery("from Apartment a where a.id = " + id);
 		return (Apartment) query.uniqueResult();
 	}
 
 	@Override
 	@Transactional
 	public Apartment find(String landlord, String identifier) {
 		Session session = mSessionFactory.getCurrentSession();
 		Query query = session.createQuery("from Apartment a where 'a.mIdentifier' = '" + identifier + "' and 'a.mLandlord' = '" + landlord + "'");
 		return (Apartment) query.uniqueResult();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	@Transactional
 	public List<Apartment> findAll() {
 		Session session = mSessionFactory.getCurrentSession();
 		Query query = session.createQuery("from Apartment a");
 		return (List<Apartment>) query.list();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	@Transactional
 	public List<Apartment> findAllByCity(String city) {
 		Session session = mSessionFactory.getCurrentSession();
 		city = WordUtils.capitalize(city);
 		Query query = session.createQuery("from Apartment a where mCity = '" + city + "'");
 		return (List<Apartment>) query.list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	@Transactional
 	public List<Apartment> findAllByLandlord(String landlord) {
 		Session session = mSessionFactory.getCurrentSession();
 		Query query = session.createQuery("from Apartment a where mLandlord = '" + landlord + "'");
 		return (List<Apartment>) query.list();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	@Transactional
 	public List<String> findAllCities() {
 		Session session = mSessionFactory.getCurrentSession();
		List<Apartment> list = session.createCriteria(Apartment.class).setProjection(Projections.distinct(Projections.projectionList().add(Projections.property("mCity"), "city"))).setResultTransformer(Transformers.aliasToBean(Apartment.class))
 				.list();
 		List<String> returnList = new LinkedList<String>();
 		for (Apartment apartment : list) {
 				returnList.add(HtmlUtil.htmlToText(apartment.getCity()));
 		}
 		return returnList;
 	}
 
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.mSessionFactory = sessionFactory;
 
 	}
 
 }
