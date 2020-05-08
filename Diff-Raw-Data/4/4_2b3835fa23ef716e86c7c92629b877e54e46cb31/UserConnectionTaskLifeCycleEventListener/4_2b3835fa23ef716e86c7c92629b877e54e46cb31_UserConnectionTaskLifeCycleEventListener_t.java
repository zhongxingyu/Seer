 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.myconnections.workflow.jbpm;
 
 import org.apache.commons.lang.StringUtils;
 import org.jahia.registries.ServicesRegistry;
 import org.jahia.services.usermanager.JahiaPrincipal;
 import org.jahia.services.usermanager.JahiaUser;
 import org.jahia.services.workflow.jbpm.JBPMTaskLifeCycleEventListener;
 import org.jbpm.runtime.manager.impl.task.SynchronizedTaskService;
 import org.jbpm.services.task.events.AfterTaskAddedEvent;
 import org.jbpm.services.task.impl.model.GroupImpl;
 import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl;
 import org.jbpm.services.task.impl.model.UserImpl;
 import org.kie.api.task.model.OrganizationalEntity;
 import org.kie.api.task.model.PeopleAssignments;
 import org.kie.api.task.model.Task;
 
 import javax.enterprise.event.Observes;
 import javax.enterprise.event.Reception;
 import javax.jcr.RepositoryException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Assignment handler for user connection task.
  *
  * @author Serge Huber
  */
 public class UserConnectionTaskLifeCycleEventListener extends JBPMTaskLifeCycleEventListener {
 
     private static final long serialVersionUID = 3356236148908996978L;
 
     /**
      * sets the actorId and candidates for the given task.
      */
 
     @Override
     public void afterTaskAddedEvent(@Observes(notifyObserver = Reception.IF_EXISTS) @AfterTaskAddedEvent Task task) {
 
         Map<String, Object> taskInputParameters = getTaskInputParameters(task);
         Map<String, Object> taskOutputParameters = getTaskOutputParameters(task, taskInputParameters);
 
         PeopleAssignments peopleAssignments = new PeopleAssignmentsImpl();
         List<OrganizationalEntity> potentialOwners = new ArrayList<OrganizationalEntity>();
         String to = (String) taskInputParameters.get("to");
         JahiaUser jahiaUser = null;
         if (StringUtils.isNotEmpty(to)) {
             jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(to);
             potentialOwners.add(new UserImpl((jahiaUser).getUserKey()));
         }
 
         List<OrganizationalEntity> administrators = new ArrayList<OrganizationalEntity>();
         administrators.add(new GroupImpl(ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdministratorGroup(null).getGroupKey()));
//        peopleAssignments.getBusinessAdministrators().addAll(administrators);
//        peopleAssignments.getPotentialOwners().addAll(potentialOwners);
 
         if (jahiaUser != null) {
             List<JahiaPrincipal> jahiaPrincipals = new ArrayList<JahiaPrincipal>();
             jahiaPrincipals.add(jahiaUser);
             try {
                 createTask(task, taskInputParameters, taskOutputParameters, jahiaPrincipals);
                 ((SynchronizedTaskService) taskService).addContent(task.getId(), taskOutputParameters);
 
             } catch (RepositoryException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
 }
