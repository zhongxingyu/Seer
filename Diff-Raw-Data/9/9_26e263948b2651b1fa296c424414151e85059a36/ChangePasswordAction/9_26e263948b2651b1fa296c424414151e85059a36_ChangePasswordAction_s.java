 /*
  * jPOS Presentation Manager [http://jpospm.blogspot.com]
  * Copyright (C) 2010 Jeronimo Paoletti [jeronimo.paoletti@gmail.com]
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.security.ui.actions;
 
 import org.jpos.ee.pm.core.PMException;
 import org.jpos.ee.pm.security.core.operations.ChangePassword;
 import org.jpos.ee.pm.struts.PMForwardException;
 import org.jpos.ee.pm.struts.PMStrutsContext;
 import org.jpos.ee.pm.struts.actions.ActionSupport;
 
 public class ChangePasswordAction extends ActionSupport {
 
     @Override
     protected void doExecute(PMStrutsContext ctx) throws PMException {
 
         final ChangePassword op = new ChangePassword("changepassword");
        if (!op.excecute(ctx)) {
             throw new PMForwardException(CONTINUE);
         }
     }
 }
