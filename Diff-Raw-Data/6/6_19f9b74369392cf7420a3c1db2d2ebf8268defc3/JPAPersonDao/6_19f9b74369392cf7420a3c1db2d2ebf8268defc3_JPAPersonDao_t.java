 package com.mpower.dao.jpa;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.validator.GenericValidator;
 import org.springframework.stereotype.Repository;
 
 import com.mpower.dao.PersonDao;
 import com.mpower.dao.util.QueryUtil;
 import com.mpower.domain.Person;
 import com.mpower.domain.Phone;
 import com.mpower.domain.Site;
 import com.mpower.service.impl.SessionServiceImpl;
 import com.mpower.util.EntityUtility;
 
 @Repository("personDao")
 public class JPAPersonDao implements PersonDao {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @PersistenceContext
     private EntityManager em;
 
     @Override
     public Person savePerson(Person person) {
         // Sanity check
         if (!person.getSite().getName().equals(SessionServiceImpl.lookupUserSiteName())) throw new RuntimeException("Person object does not belong to current site.");
 
         for (Iterator<Phone> iter = person.getPhones().iterator(); iter.hasNext();) {
             Phone phone = iter.next();
             if (phone == null || StringUtils.stripToNull(phone.getNumber()) == null) {
                 iter.remove();
             }
         }
         if (person.getId() == null) {
             em.persist(person);
             return person;
         }
         return em.merge(person);
     }
 
     @Override
     public Person readPerson(Long id) {
         Person person = em.find(Person.class, id);
         // Sanity check
         if (person!=null && !person.getSite().getName().equals(SessionServiceImpl.lookupUserSiteName())) throw new RuntimeException("Person object does not belong to current site.");
         return person;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public Person readPersonByLoginId(String loginId, String siteName) {
         Query query = em.createNamedQuery("READ_PERSON_BY_LOGIN_ID");
         query.setParameter("loginId", loginId);
         query.setParameter("siteId", siteName);
         List<Person> persons = query.getResultList();
         if (persons.size() == 0) return null;
         return persons.get(0);
     }
 
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Person> readPersons(String siteName, List<Long> ids) {
     	
     	if (ids == null || ids.size() == 0) {
             return new ArrayList<Person>();
         }
     	
         StringBuilder queryString = new StringBuilder();
         for (Long id : ids) {
         	if (queryString.length() > 0) {
                 queryString.append(',');
             }
         	queryString.append(id);
         }
         queryString.insert(0, "SELECT person FROM com.mpower.domain.Person person WHERE person.site.name = :siteName AND person.id IN (");
         queryString.append(")");
         
         Query query = em.createQuery(queryString.toString());
         query.setParameter("siteName", siteName);
         return query.getResultList();
         	
     }
 
     public List<Person> readPersons(String siteName, Map<String, Object> params) {
         return readPersons(siteName, params, null);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Person> readPersons(String siteName, Map<String, Object> params, List<Long> ignoreIds) {
         boolean whereUsed = true;
         StringBuilder queryString = new StringBuilder("SELECT person FROM com.mpower.domain.Person person WHERE person.site.name = :siteName");
         boolean hasIgnoreIds = false;
         if (ignoreIds != null && !ignoreIds.isEmpty()) {
             queryString.append(" AND person.id NOT IN (:ignoreIds)");
             hasIgnoreIds = true;
         }
         Map<String, Object> addressParams = new HashMap<String, Object>();
         Map<String, Object> phoneParams = new HashMap<String, Object>();
         Map<String, Object> emailParams = new HashMap<String, Object>();
         Map<String, String> customParams = new HashMap<String, String>();
         LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<String, Object>();
         if (params != null) {
             for (Map.Entry<String, Object> pair : params.entrySet()) {
                 String key = pair.getKey();
                 Object value = pair.getValue();

                // HACK: will implement a permanent fix at web layer for final
                if(key.equals("addressMap[home].stateProvince") && ((String)value).equalsIgnoreCase("none")) {
                    continue;
                }

                 boolean isString = true;
                 if (value instanceof String) {
                     if (GenericValidator.isBlankOrNull((String) value)) {
                         continue;
                     }
                 } 
                 else {
                     if (value == null) {
                         continue;
                     }
                     isString = false;
                 }
                 if (key.startsWith("addressMap[")) {
                     addressParams.put(key.substring(key.indexOf('.') + 1), value);
                 } 
                 else if (key.startsWith("phoneMap[")) {
                     phoneParams.put(key.substring(key.indexOf('.') + 1), value);
                 } 
                 else if (key.startsWith("emailMap[")) {
                     emailParams.put(key.substring(key.indexOf('.') + 1), value);
                 } 
                 else if (key.startsWith("customFieldMap[")) {
                     customParams.put(key.substring(key.indexOf('[') + 1, key.indexOf(']')), (String) value);
                 } 
                 else {
                     whereUsed = EntityUtility.addWhereOrAnd(whereUsed, queryString);
                     queryString.append(" person.");
                     queryString.append(key);
                     String paramName = key.replace(".", "_");
                     if (isString) {
                         queryString.append(" LIKE :");
                         queryString.append(paramName);
                         parameterMap.put(paramName, "%" + value + "%");
                     } else {
                         queryString.append(" = :");
                         queryString.append(paramName);
                         parameterMap.put(paramName, value);
                     }
                 }
             }
         }
         queryString.append(getStringForType(addressParams, parameterMap, "address"));
         queryString.append(getStringForType(phoneParams, parameterMap, "phone"));
         queryString.append(getStringForType(emailParams, parameterMap, "email"));
         queryString.append(QueryUtil.getCustomString(customParams, parameterMap));
 
         Query query = em.createQuery(queryString.toString());
         query.setParameter("siteName", siteName);
         if (hasIgnoreIds) {
             query.setParameter("ignoreIds", ignoreIds);
         }
         for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
             query.setParameter(entry.getKey(), entry.getValue());
         }
         return query.getResultList();
     }
 
     private StringBuilder getStringForType(Map<String, Object> params, LinkedHashMap<String, Object> parameterMap, String type) {
         StringBuilder str = new StringBuilder();
         if (params != null && !params.isEmpty()) {
             str.append(" AND EXISTS ( SELECT ").append(type).append(" FROM com.mpower.domain.").append(StringUtils.capitalize(type)).append(" ").append(type).append(" WHERE ").append(type).append(".person.id = person.id ");
             for (Map.Entry<String, Object> pair : params.entrySet()) {
                 String key = pair.getKey();
                 Object value = pair.getValue();
                 str.append(" AND ").append(type).append(".");
                 str.append(key);
                 str.append(" LIKE :");
                 String paramName = key.replace(".", "_");
                 str.append(paramName);
                 if (value instanceof String) {
                     parameterMap.put(paramName, "%" + value + "%");
                 } else {
                     parameterMap.put(paramName, value);
                 }
             }
             str.append(")");
         }
         return str;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Person> analyzeLapsedDonor(Date beginDate, Date currentDate) {
         Query query = em.createNamedQuery("ANALYZE_FOR_LAPSED_DONOR");
         query.setParameter("beginDate", beginDate);
         query.setParameter("currentDate", currentDate);
         return query.getResultList();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Person> readAllPeople() {
         Query query = em.createNamedQuery("READ_ALL_PEOPLE");
         return query.getResultList();
     }
 
     @Override
     public void setLapsedDonor(Long personId) {
         Query query = em.createNamedQuery("SET_LAPSED_DONOR");
         query.setParameter("personId", personId);
         query.executeUpdate();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Person> readAllPeopleBySite(Site site) {
         Query query = em.createNamedQuery("READ_ALL_PEOPLE_BY_SITE");
         query.setParameter("site", site);
         return query.getResultList();
     }
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Person> readAllPeopleBySiteName(String siteName) {
         Query query = em.createNamedQuery("READ_ALL_PEOPLE_BY_SITE_ID");
         query.setParameter("siteId", siteName);
         return query.getResultList();
 	}
 }
