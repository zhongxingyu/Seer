 /*
  * Created on Feb 5, 2008
  *
  * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
  * (See licensing and redistribution disclosures at end of this file.)
  * 
  */
 package org.jasig.portlets.FeedbackPortlet.dao.hibernate;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Order;
 import org.jasig.portlets.FeedbackPortlet.FeedbackItem;
 import org.jasig.portlets.FeedbackPortlet.FeedbackQueryParameters;
 import org.jasig.portlets.FeedbackPortlet.OverallFeedbackStats;
 import org.jasig.portlets.FeedbackPortlet.dao.FeedbackStore;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 /**
  * HibernateFeedbackStore is a hibernate implementation of the FeedbackStore interface.
  * 
  * @author Jen Bourey
  */
 public class HibernateFeedbackStore extends HibernateDaoSupport implements FeedbackStore {
 
 	private Log log = LogFactory.getLog(getClass());
	
 	@Override
 	public void storeFeedback(FeedbackItem feedback) {
 		try {
 			final Session session = this.getSession(false);
 
 			// If the FeedbackItem is new it must be saved first
 			if (feedback.getId() == -1) {
 				session.save(feedback);
 			}
 
 			session.update(feedback);
 			session.flush();
 		} catch (HibernateException ex) {
 			throw convertHibernateAccessException(ex);
 		}
 	}
 
     @Override
 	public List<FeedbackItem> getFeedback() {
 		List<FeedbackItem> results;
 		try {
 			final Session session = this.getSession(false);
 			Criteria crit = session.createCriteria(FeedbackItem.class);
 			crit.addOrder(Order.desc("submissiontime"));
 			results = crit.list();
 
 		} catch (HibernateException ex) {
 			throw convertHibernateAccessException(ex);
 		}
 		return results;
 	}
 	
     /** 
 	 * Searches the feedback and pulls the results between (inclusively) the dates listed.   One does not actually have to be less than the other
 	 * @param Date1
 	 * @param Date2
 	 * @return list of items
 	 */
     @Override
     @SuppressWarnings("unchecked")
 	public List<FeedbackItem> getFeedback(FeedbackQueryParameters params) {
         List<FeedbackItem> results;
         
         int start = params.getInt(params.START_DISPLAY_COUNT);
         int items = params.getInt(params.ITEMS_DISPLAYED);
         String role = params.getString(params.USER_ROLE);
         String feedbacktype = params.getString(params.FEEDBACK_TYPE);
         boolean comments = params.getBoolean(params.COMMENTS_ONLY_DISPLAYED);
         Date startDate = params.getDate(params.START_DISPLAY_DATE);
         Date endDate = params.getEndDate(params.END_DISPLAY_DATE);
         try {
             final Session session = this.getSession(false);
             Criteria crit = session.createCriteria(FeedbackItem.class);
             crit.addOrder(Order.desc("submissiontime"));
             crit.setFirstResult(start);
             if (role != null && !role.isEmpty()) {
                  crit.add(Expression.eq("userrole", role));
             }
             if (feedbacktype != null && !feedbacktype.isEmpty()) {
                  crit.add(Expression.eq("feedbacktype", feedbacktype));
             }
             if (comments == true) {
                crit.add(Expression.or(Expression.ne("feedback", ""), Expression.ne("feedback", " ")));
                 crit.add(Expression.isNotNull("feedback"));
             }
             // Dates are on by default and throws an error if not entered, so they should never be null 
             crit.add(Expression.between("submissiontime", startDate, endDate));
             
             crit.setMaxResults(items);
              
             results = crit.list();
        } catch (HibernateException ex) {
            throw convertHibernateAccessException(ex);
        }
        return results;
 	}
 	
     @Override
     public long getFeedbackTotal(FeedbackQueryParameters params) {
         String role = params.getString(params.USER_ROLE);
         String feedbacktype = params.getString(params.FEEDBACK_TYPE);
         boolean comments = params.getBoolean(params.COMMENTS_ONLY_DISPLAYED);
         Date startDate = params.getDate(params.START_DISPLAY_DATE);
         Date endDate = params.getEndDate(params.END_DISPLAY_DATE);
         try {
             final Session session = this.getSession(false);
             String sql = "select count(item.id) from FeedbackItem item";
             if (role != null && !role.isEmpty()) {
                 sql = sql.concat(!sql.contains(" where ") ? " where " : " and ");
                 sql = sql.concat("userrole = :userrole");
             }
             if (feedbacktype != null && !feedbacktype.isEmpty()) {
                 sql = sql.concat(!sql.contains(" where ") ? " where " : " and ");
                 sql = sql.concat("feedbacktype = :feedbacktype");
             }
             if (comments != false) {
                 sql = sql.concat(!sql.contains(" where ") ? " where " : " and ");
                 sql = sql.concat("LENGTH(feedback) > 0");
             }
             if (startDate != null && endDate != null) {
                 sql = sql.concat(!sql.contains(" where ") ? " where " : " and ");
                 sql = sql.concat("submissiontime BETWEEN :startDate AND :endDate");
             }
             Query query = session.createQuery(sql);
             if (role != null && !role.isEmpty()) {
                 query.setString("userrole", role);
             }
             if (feedbacktype != null && !feedbacktype.isEmpty()) {
                 query.setString("feedbacktype", feedbacktype);
             }
             if (startDate != null && endDate != null) {
                 query.setDate("startDate", startDate);
                 query.setDate("endDate", endDate);
             }
             return (Long) query.uniqueResult();
         } catch (HibernateException ex) {
             throw convertHibernateAccessException(ex);
         }
     }
 
     @Override
 	public OverallFeedbackStats getStats() {
 		OverallFeedbackStats stats = new OverallFeedbackStats();
 		try {
 			final Session session = this.getSession(false);
 
 			stats
 					.setUniqueUsers((Long) session
 							.createQuery(
 									"select count(distinct item.userid) " +
 									"from FeedbackItem item")
 							.uniqueResult());
 
 			Iterator i = session
 					.createQuery(
 							"select item.feedbacktype, count(item) from " +
 							"FeedbackItem item group by item.feedbacktype")
 					.list().iterator();
 			while (i.hasNext()) {
 				Object[] row = (Object[]) i.next();
 				String feedbacktype = (String) row[0];
 				Long count = (Long) row[1];
 				if (feedbacktype.equals(FeedbackItem.YES))
 					stats.setPositiveResponses(count);
 				else if (feedbacktype.equals(FeedbackItem.NO))
 					stats.setNegativeResponses(count);
 				else
 					stats.setUndecidedResponses(count);
 			}
 
 		} catch (HibernateException ex) {
 			throw convertHibernateAccessException(ex);
 		}
 		return stats;
 	}
 
     @Override
 	public Map<String, OverallFeedbackStats> getStatsByRole() {
 		OverallFeedbackStats stats = new OverallFeedbackStats();
 		Map<String, OverallFeedbackStats> statsMap = new HashMap<String, OverallFeedbackStats>();
 		try {
 			final Session session = this.getSession(false);
 
 			Iterator i = session
 					.createQuery(
 							"select item.userrole, count(distinct item.userid) " +
 							"from FeedbackItem item group by item.userrole")
 					.list().iterator();
 			while (i.hasNext()) {
 				Object[] row = (Object[]) i.next();
 				String userrole = (String) row[0];
 				Long count = (Long) row[1];
 				stats = new OverallFeedbackStats();
 				stats.setUniqueUsers(count);
 				statsMap.put(userrole, stats);
 			}
 
 			i = session
 					.createQuery(
 							"select item.userrole, item.feedbacktype, count(item) " +
 							"from FeedbackItem item group by item.userrole, " +
 							"item.feedbacktype")
 					.list().iterator();
 			while (i.hasNext()) {
 				Object[] row = (Object[]) i.next();
 				String userrole = (String) row[0];
 				String feedbacktype = (String) row[1];
 				Long count = (Long) row[2];
 
 				stats = (OverallFeedbackStats) statsMap.get(userrole);
 				if (feedbacktype.equals(FeedbackItem.YES))
 					stats.setPositiveResponses(count);
 				else if (feedbacktype.equals(FeedbackItem.NO))
 					stats.setNegativeResponses(count);
 				else
 					stats.setUndecidedResponses(count);
 			}
 
 		} catch (HibernateException ex) {
 			throw convertHibernateAccessException(ex);
 		}
 		return statsMap;
 	}
 
 }
 
 /*
  * HibernateFeedbackStore.java
  * 
  * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
  * 
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
  * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * Redistribution and use of this software in source or binary forms, with or
  * without modification, are permitted, provided that the following conditions
  * are met.
  * 
  * 1. Any redistribution must include the above copyright notice and disclaimer
  * and this list of conditions in any related documentation and, if feasible, in
  * the redistributed software.
  * 
  * 2. Any redistribution must include the acknowledgment, "This product includes
  * software developed by Yale University," in any related documentation and, if
  * feasible, in the redistributed software.
  * 
  * 3. The names "Yale" and "Yale University" must not be used to endorse or
  * promote products derived from this software.
  */
