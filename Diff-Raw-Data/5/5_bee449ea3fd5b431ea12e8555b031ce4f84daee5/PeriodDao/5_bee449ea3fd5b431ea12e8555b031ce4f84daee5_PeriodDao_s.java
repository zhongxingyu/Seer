 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 
 /**
  * @author Rhett Sutphin
  */
 public class PeriodDao extends StudyCalendarDao<Period> {
     public Class<Period> domainClass() {
         return Period.class;
     }
 
     public void save(Period period) {
         getHibernateTemplate().saveOrUpdate(period);
     }
 }
