 /**
  * 
  */
 package net.frontlinesms.plugins.forms.data.repository.hibernate;
 
 import java.util.List;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Restrictions;
 
 import net.frontlinesms.data.events.DatabaseEntityNotification;
 import net.frontlinesms.data.events.EntityDeleteWarning;
 import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.events.EventObserver;
 import net.frontlinesms.events.FrontlineEventNotification;
 import net.frontlinesms.plugins.forms.data.domain.Form;
 import net.frontlinesms.plugins.forms.data.domain.FormField;
 import net.frontlinesms.plugins.forms.data.domain.FormFieldType;
 import net.frontlinesms.plugins.forms.data.domain.FormResponse;
 import net.frontlinesms.plugins.forms.data.domain.ResponseValue;
 import net.frontlinesms.plugins.forms.data.repository.FormResponseDao;
 
 /**
  * Hibernate implementation of {@link FormResponseDao}
  * @author Alex
  */
 public class HibernateFormResponseDao extends BaseHibernateDao<FormResponse> implements FormResponseDao, EventObserver {
 
 	/** Create new instance of this DAO */
 	public HibernateFormResponseDao() {
 		super(FormResponse.class);
 	}
 	
 	public void setEventBus(EventBus eventBus){
 		eventBus.registerObserver(this);
 		super.setEventBus(eventBus);
 	}
 	
 	/** @see FormResponseDao#getFormResponseCount(Form) */
 	public int getFormResponseCount(Form form) {
 		DetachedCriteria criteria = super.getCriterion();
 		criteria.add(Restrictions.eq(FormResponse.FIELD_FORM, form));
		return super.getCount(criteria);
 	}
 
 	/** @see FormResponseDao#getFormResponses(Form, int, int) */
 	public List<FormResponse> getFormResponses(Form form, int startIndex, int limit) {
 		// TODO please write a unit test to demonstrate the working code working and the non-working
 		// code NOT working.  This has proved difficult so far.  However, we (i.e. Morgan ;) worked
 		// out what is wrong with the non-working code - it's returning <limit> objects rather than
 		// <limit> FormResponses - it's counting the ResponseValue objects as well as the FormResponses.
 		// Why?  Not sure.  Does it behave this way in the unit tests?  Nope.  )¬;
 		
 		// THIS DOES NOT WORK
 //		DetachedCriteria criteria = super.getCriterion();
 //		criteria.add(Restrictions.eq(FormResponse.FIELD_FORM, form));
 //		return super.getList(criteria, startIndex, limit);
 		
 		// THIS WORKS:
 		String selectString = "SELECT fr FROM " + FormResponse.class.getName() + " fr " +
 				"WHERE " + FormResponse.FIELD_FORM + "=?";
 		return super.getList(selectString, startIndex, limit, form);
 	}
 
	/** @see FormResponseDao#getFormResponses(Form) */
	public List<FormResponse> getFormResponses(Form form) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq(FormResponse.FIELD_FORM, form));
		return super.getList(criteria);
	}

 	/** @see FormResponseDao#saveResponse(FormResponse) */
 	public void saveResponse(FormResponse formResponse) {
 		super.saveWithoutDuplicateHandling(formResponse);
 	}
 
 	public void notify(FrontlineEventNotification notification) {
 		if (notification instanceof DatabaseEntityNotification<?>) {
 			Object entity = ((DatabaseEntityNotification<?>) notification).getDatabaseEntity();
 			
 			if (entity instanceof Form && notification instanceof EntityDeleteWarning<?>) {
 				deleteResponsesOf((Form) entity);
 			}
 		}
 	}
 
 	/**
 	 * Deletes all responses related to a form
 	 * @param form
 	 */
 	private void deleteResponsesOf(Form form) {
 		DetachedCriteria criteria = super.getCriterion();
 		criteria.add(Restrictions.eq(FormResponse.FIELD_FORM, form));
 		
 		for (FormResponse formResponse : super.getList(criteria)) {
 			super.delete(formResponse);
 		}
 	}
 
 	public void delete(FormResponse formResponse) {
 		super.delete(formResponse);
 	}
 }
