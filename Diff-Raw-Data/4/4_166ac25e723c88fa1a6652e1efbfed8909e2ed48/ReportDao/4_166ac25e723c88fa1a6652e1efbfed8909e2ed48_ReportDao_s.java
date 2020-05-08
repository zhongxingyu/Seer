 package edu.northwestern.bioinformatics.studycalendar.dao.reporting;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.springframework.orm.hibernate3.HibernateCallback;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author John Dzak
  */
 public abstract class ReportDao<F extends ReportFilters, R extends DomainObject> extends StudyCalendarDao<R> {
     @SuppressWarnings("unchecked")
     public List<R> search(final F filters) {
         return getHibernateTemplate().executeFind(new HibernateCallback() {
             @SuppressWarnings("unchecked")
             public Object doInHibernate(Session session) throws HibernateException {
                 if (filters.isEmpty()) {
                     logger.debug("No filters selected, skipping search: " + filters);
                     return Collections.emptyList();
                 } else {
                    Criteria criteria = session.createCriteria(domainClass());
                     filters.apply(session);
                     return (List<R>) criteria.list();
 
                 }
             }
         });
     }
 }
