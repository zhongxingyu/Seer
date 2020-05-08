 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.patientview.logon;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.patientview.patientview.model.Unit;
 import org.patientview.utils.LegacySpringUtils;
import org.springframework.beans.support.PagedListHolder;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 
 public class UnitUserEditAction extends Action {
 
     public ActionForward execute(
         ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
             throws Exception {
         String username = BeanUtils.getProperty(form, "username");
         String password = BeanUtils.getProperty(form, "password");
         String name = BeanUtils.getProperty(form, "name");
         String email = BeanUtils.getProperty(form, "email");
         boolean emailverified = "true".equals(BeanUtils.getProperty(form, "emailverified"));
         String unitcode = BeanUtils.getProperty(form, "unitcode");
         String role = BeanUtils.getProperty(form, "role");
         boolean firstlogon = "true".equals(BeanUtils.getProperty(form, "firstlogon"));
         boolean isRecipient = "true".equals(BeanUtils.getProperty(form, "isrecipient"));
         boolean isClinician = "true".equals(BeanUtils.getProperty(form, "isclinician"));
 
         UnitAdmin unitAdmin = new UnitAdmin(username, password, name, email, emailverified, role, firstlogon);
         unitAdmin.setIsrecipient(isRecipient);
         unitAdmin.setIsclinician(isClinician);
 
         LegacySpringUtils.getUserManager().saveUserFromUnitAdmin(unitAdmin, unitcode);
 
         Unit unit = LegacySpringUtils.getUnitManager().get(unitcode);
         if (unit != null) {
             request.setAttribute("unit", unit);
         }
 
         List unitUsers = LegacySpringUtils.getUnitManager().getUnitUsers(unitcode);
        PagedListHolder pagedListHolder = new PagedListHolder(unitUsers);
        request.getSession().setAttribute("unitUsers", pagedListHolder);
 
         return LogonUtils.logonChecks(mapping, request);
     }
 
 }
