 package com.broodsoft.venture.jpa.manager;
 
 import java.util.Collection;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import com.broodsoft.brew.db.jpa.JPAQueryAdapter;
 import com.broodsoft.brew.doc.CodeAuthor;
import com.broodsoft.venture.jpa.model.*;
 
 @CodeAuthor(first = "Drazzle", last = "Bay")
 public class PersonManager extends JPAQueryAdapter
 {
 	public PersonManager(EntityManager entityManager)
 	{
 		super(entityManager);
 	}
 
 	public Collection<Person> findPeopleByFirstName(final String firstName)
 	{
 		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
 		CriteriaQuery<Person> query = builder.createQuery(Person.class);
 		Root<Person> root = query.from(Person.class);
 
 		Predicate sameFirstName = builder.equal(root.get(Person_.name).get(Name_.firstName), firstName);
 
 		query = query.where(sameFirstName);
 		return getEntityManager().createQuery(query).getResultList();
 	}
 
 	public Collection<PhoneNumber> findPhoneNumber(int areaCode, int firstThree, int lastFour)
 	{
 		CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
 		CriteriaQuery<PhoneNumber> query = builder.createQuery(PhoneNumber.class);
 		Root<PhoneNumber> root = query.from(PhoneNumber.class);
 
 		Predicate sameAreaCode = builder.equal(root.get(PhoneNumber_.areaCode), areaCode);
 		Predicate sameFirstThree = builder.equal(root.get(PhoneNumber_.firstThree), firstThree);
 		Predicate sameLastFour = builder.equal(root.get(PhoneNumber_.lastFour), lastFour);
 		Predicate samePhoneInUS = builder.and(sameAreaCode, sameFirstThree, sameLastFour);
 
 		query = query.where(samePhoneInUS);
 		return getEntityManager().createQuery(query).getResultList();
 	}
 }
