 package edu.northwestern.bioinformatics.studycalendar.dao;
 
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 
 /**
  * @author Rhett Sutphin
  */
@Transactional(readOnly = true)
 public class PeriodDao extends StudyCalendarDao<Period> {
     public Class<Period> domainClass() {
         return Period.class;
     }
 
    @Transactional(readOnly = false)
     public void save(Period period) {
         getHibernateTemplate().saveOrUpdate(period);
     }
 }
