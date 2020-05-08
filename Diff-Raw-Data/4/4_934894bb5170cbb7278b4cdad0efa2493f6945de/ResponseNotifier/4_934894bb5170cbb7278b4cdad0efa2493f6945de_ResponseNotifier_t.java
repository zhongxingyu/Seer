 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server.robots;
 
 import org.accesointeligente.model.Notification;
 import org.accesointeligente.model.Response;
 import org.accesointeligente.model.User;
 import org.accesointeligente.server.ApplicationProperties;
 import org.accesointeligente.server.HibernateUtil;
 import org.accesointeligente.shared.AppPlace;
 
 import org.apache.log4j.Logger;
 import org.hibernate.*;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.Date;
 import java.util.List;
 
 public class ResponseNotifier {
 	private static final Logger logger = Logger.getLogger(ResponseNotifier.class);
 
 	public void notifyResponses() {
 		Session hibernate = null;
 		Long MILLISECONDS_PER_DAY = (long) (24 * 60 * 60 * 1000);
 
 		try {
 			hibernate = HibernateUtil.getSession();
 			hibernate.beginTransaction();
 			Criteria criteria = hibernate.createCriteria(Response.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("request", FetchMode.JOIN);
 			criteria.setFetchMode("request.user", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("notified", Boolean.FALSE));
 			List<Response> responses = criteria.list();
 			for (Response response : responses) {
 				Hibernate.initialize(response.getRequest());
				if (response.getRequest() != null) {
					Hibernate.initialize(response.getRequest().getUser());
				}
 			}
 			hibernate.getTransaction().commit();
 
 			for (Response response : responses) {
 				logger.info("responseId = " + response.getId());
 				createResponseNotification(response);
 			}
 		} catch (Exception ex) {
 			if (hibernate != null && hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			logger.error(ex.getMessage(), ex);
 		}
 
 		try {
 			hibernate = HibernateUtil.getSession();
 			hibernate.beginTransaction();
 			Criteria criteria = hibernate.createCriteria(Response.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.add(Restrictions.eq("notifiedSatisfaction", Boolean.FALSE));
 			criteria.add(Restrictions.or(Restrictions.eq("userSatisfaction", "NOANSWER"), Restrictions.isNull("userSatisfaction")));
 			List<Response> responses = criteria.list();
 			hibernate.getTransaction().commit();
 
 			for (Response response : responses) {
 				if ((((new Date()).getTime() - response.getDate().getTime())/ MILLISECONDS_PER_DAY) > 1) {
 					logger.info("responseId = " + response.getId());
 					createUserSatisfactionNotification(response);
 				}
 			}
 		} catch (Exception ex) {
 			if (hibernate != null && hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			logger.error(ex.getMessage(), ex);
 		}
 	}
 
 	public void createResponseNotification(Response response) {
 		Session hibernate = null;
 		try {
 			User user = response.getRequest().getUser();
 			Notification notification = new Notification();
 			notification.setEmail(user.getEmail());
 			notification.setSubject(ApplicationProperties.getProperty("email.response.arrived.subject"));
 			notification.setMessage(String.format(ApplicationProperties.getProperty("email.response.arrived.body"), user.getFirstName(), ApplicationProperties.getProperty("request.baseurl"), response.getRequest().getId(), response.getRequest().getTitle()) + ApplicationProperties.getProperty("email.signature"));
 			response.setNotified(true);
 
 			hibernate = HibernateUtil.getSession();
 			hibernate.beginTransaction();
 			hibernate.saveOrUpdate(response);
 			hibernate.saveOrUpdate(notification);
 			hibernate.getTransaction().commit();
 		} catch (Exception ex) {
 			if (hibernate != null && hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			logger.error("Couldn't create ResponseNotification", ex);
 		}
 	}
 
 	public void createUserSatisfactionNotification(Response response) {
 		Session hibernate = null;
 
 		try {
 			String responseSatisfactionLink = ApplicationProperties.getProperty("request.baseurl") + "#" + AppPlace.RESPONSEUSERSATISFACTION;
 			responseSatisfactionLink += ";responseId=" + response.getId().toString();
 			responseSatisfactionLink += ";responseKey=" + response.getResponseKey();
 			User user = response.getRequest().getUser();
 			Notification notification = new Notification();
 			notification.setEmail(user.getEmail());
 			notification.setSubject(ApplicationProperties.getProperty("email.response.satisfaction.subject"));
 			notification.setMessage(String.format(ApplicationProperties.getProperty("email.response.satisfaction.body"), user.getFirstName(), responseSatisfactionLink, response.getRequest().getTitle()) + ApplicationProperties.getProperty("email.signature"));
 			response.setNotifiedSatisfaction(true);
 
 			hibernate = HibernateUtil.getSession();
 			hibernate.beginTransaction();
 			hibernate.saveOrUpdate(response);
 			hibernate.saveOrUpdate(notification);
 			hibernate.getTransaction().commit();
 		} catch (Exception ex) {
 			if (hibernate != null && hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			logger.error("Couldn't create Response UserSatisfactionNotification", ex);
 		}
 	}
 }
