 package web.dao.impl;
 
 import java.util.List;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.transform.Transformers;
 import org.joda.time.LocalDateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import web.dao.SearchDao;
 import web.model.SearchResult;
 
 /**
  * @author Romain <ro.foncier@gmail.com>
  */
 
 @Repository
 public class SearchDaoImpl implements SearchDao {
 
     @Autowired
     private SessionFactory sessionFactory;
     
     public Session getCurrentSession() {
         return sessionFactory.getCurrentSession();
     }
     
     @Override
     public List<SearchResult> searchForSpecificCountry(String country, LocalDateTime checkin, LocalDateTime checkout, Integer guests) {
         String hql = "SELECT p.property_id, p.title, p.short_desc, p.price, p.city, p.country, " +
                                      "p.type, p.note, p.rooms*2 AS places, p.latitude, p.longitude,  " +
                                      "po.parking, po.swimming_pool AS swim, po.wifi, po.laundry " +
                                      "FROM property AS p " +
                                      "JOIN property_options AS po ON p.property_id=po.target_property " +
                                      "WHERE p.country=:country " +
                                      "AND p.rent_period_start<=:checkin " +
                                      "AND p.rent_period_stop>=:checkout " +
                                      "AND NOT EXISTS (SELECT SUM(r.hosts) AS guests_number FROM reservation AS r " +
                                      "WHERE p.property_id=r.target_property "+
                                      "AND r.date_rent_start<=:checkin " +
                                      "AND :checkout<=r.date_rent_stop HAVING places < guests_number+:guests)";
         Query query = sessionFactory.getCurrentSession().createSQLQuery(hql);
         query.setParameter("country", country);
         query.setParameter("checkin", checkin.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
         query.setParameter("checkout", checkin.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
         query.setParameter("guests", guests);
         query.setResultTransformer(Transformers.aliasToBean(SearchResult.class));
         return query.list();
     }
     
     @Override
    public List<SearchResult> complexSearch(Double lat, Double lng, LocalDateTime checkin, LocalDateTime checkout, Integer guests, Integer min, Integer max, String[] property_types, String[] property_options) {
         String hql = "SELECT p.property_id, p.title, p.short_desc, p.price, p.city, p.country, " +
                                      "p.type, p.note, p.rooms*2 AS places, p.latitude, p.longitude,  " +
                                      "po.parking, po.swimming_pool AS swim, po.wifi, po.laundry, " +
                                      "( 3959 * acos( cos( radians(:lat) ) * cos( radians( p.latitude ) ) * cos( radians( p.longitude ) " +
                                      " - radians(:lng) ) + sin( radians(:lat) ) * sin( radians( p.latitude ) ) ) ) AS distance " +
                                      "FROM property AS p " +
                                      "JOIN property_options AS po ON p.property_id=po.target_property " +
                                      "AND p.rent_period_start<=:checkin " +
                                      "AND p.rent_period_stop>=:checkout ";
         
         // Check for price range
         if (min != null) {
             hql += "AND p.price BETWEEN :min AND :max ";
         }
         
         // Add property type selection if required
         if (property_types != null) {
             String thql = "(";
             for (int i = 0; i < property_types.length; i++) {
                 thql += "p.type=:pType_" + i;
                 thql += (i < property_types.length -1) ? " OR " : "";
             }
             thql += ")";
             hql += "AND " + thql + " ";
         }
         
         // Add property options selection if required
         if (property_options != null) {
             String ohql = "(";
             for (int j = 0; j < property_options.length; j++) {
                 ohql += "po." + property_options[j] + "=1";
                 ohql += (j < property_options.length -1) ? " AND " : "";
             }
             ohql += ")";
             hql += "AND " + ohql + " ";
         }
         
         hql += "AND NOT EXISTS (SELECT SUM(r.hosts) AS guests_number FROM reservation AS r " +
                                      "WHERE p.property_id=r.target_property "+
                                      "AND r.date_rent_start<=:checkin " +
                                      "AND :checkout<=r.date_rent_stop HAVING places < guests_number+:guests) " +
                                      "HAVING distance < 25";
         Query query = sessionFactory.getCurrentSession().createSQLQuery(hql);
         query.setParameter("lat", lat);
         query.setParameter("lng", lng);
         query.setParameter("checkin", checkin.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
         query.setParameter("checkout", checkin.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
         query.setParameter("guests", guests);
         // Add parameters for price range
         if (min != null) {
             query.setParameter("min", min);
             query.setParameter("max", max);
         }
         
         // Add parameters for property_types
         if (property_types != null) {
             for (int i = 0; i < property_types.length; i++) {
                 query.setParameter("pType_" + i, property_types[i]);
             }
         }
         query.setResultTransformer(Transformers.aliasToBean(SearchResult.class));
         return query.list();
     }
 }
