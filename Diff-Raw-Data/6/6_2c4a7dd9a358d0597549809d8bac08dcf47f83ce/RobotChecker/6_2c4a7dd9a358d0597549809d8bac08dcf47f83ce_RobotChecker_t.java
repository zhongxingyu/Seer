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
 
 import org.accesointeligente.model.Institution;
 import org.accesointeligente.model.Notification;
 import org.accesointeligente.server.ApplicationProperties;
 import org.accesointeligente.server.HibernateUtil;
 import org.accesointeligente.server.RobotContext;
import org.accesointeligente.shared.NotificationType;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 
import java.util.Date;
 import java.util.List;
 
 public class RobotChecker {
 	private static final Logger logger = Logger.getLogger(RobotChecker.class);
 
 	public void checkRobots() {
 		Session hibernate = null;
 
 		try {
 			hibernate = HibernateUtil.getSession();
 			hibernate.beginTransaction();
 			Criteria criteria = hibernate.createCriteria(Institution.class);
 			criteria.addOrder(Order.asc("id"));
 			List<Institution> institutions = criteria.list();
 			hibernate.getTransaction().commit();
 
 			Boolean enabled = false;
 
 			for (Institution institution : institutions) {
 				Robot robot = RobotContext.getRobot(institution.getInstitutionClass());
 				logger.info("institutionId: " + institution.getId());
 
 				enabled = institution.getEnabled();
 
 				if (robot != null) {
 					try {
 						robot.login();
 						institution.setCanLogin(true);
 					} catch (Exception ex) {
 						institution.setCanLogin(false);
 					}
 
 					if (institution.getCanLogin()) {
 						try {
 							institution.setCanMakeRequest(robot.checkInstitutionId());
 						} catch (Exception ex) {
 							institution.setCanMakeRequest(false);
 						}
 					} else {
 						institution.setCanMakeRequest(false);
 					}
 				} else {
 					institution.setCanLogin(false);
 					institution.setCanMakeRequest(false);
 				}
 
 				institution.setEnabled(institution.getCanLogin() && institution.getCanMakeRequest());
 				hibernate = HibernateUtil.getSession();
 				hibernate.beginTransaction();
 				hibernate.update(institution);
 				hibernate.getTransaction().commit();
 
 				if (institution.getEnabled() != enabled) {
 					if (institution.getEnabled() == true) {
 						saveNotification(institution.getName(), ApplicationProperties.getProperty("robot.status.enabled"));
 					} else {
 						saveNotification(institution.getName(), ApplicationProperties.getProperty("robot.status.disabled"));
 					}
 				}
 			}
 		} catch (Exception ex) {
 			if (hibernate != null && hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 				logger.error("Failure", ex);
 			}
 		}
 	}
 
 	public void saveNotification(String institutionName, String status) {
 		Session hibernate = null;
 		try {
 			hibernate = HibernateUtil.getSession();
 			hibernate.beginTransaction();
 			Notification notification = new Notification();
 			notification.setEmail(ApplicationProperties.getProperty("email.admin"));
 			notification.setSubject(ApplicationProperties.getProperty("email.admin.robot.subject"));
 			notification.setMessage(String.format(ApplicationProperties.getProperty("email.admin.robot.body"), institutionName, status) + ApplicationProperties.getProperty("email.signature"));
			notification.setType(NotificationType.ROBOTCHECK);
			notification.setDate(new Date());
 			hibernate.save(notification);
 			hibernate.getTransaction().commit();
 		} catch (Exception ex) {
 			if (hibernate != null && hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 				logger.error("Failure", ex);
 			}
 		}
 	}
 }
