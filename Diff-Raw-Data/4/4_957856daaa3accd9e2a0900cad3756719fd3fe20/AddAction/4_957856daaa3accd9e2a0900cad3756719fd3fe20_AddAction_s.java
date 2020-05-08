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
 package org.jpos.ee.pm.struts.actions;
 
 import org.hibernate.exception.ConstraintViolationException;
 import org.jpos.core.ConfigurationException;
 import org.jpos.ee.pm.core.EntityInstanceWrapper;
 import org.jpos.ee.pm.core.Field;
 import org.jpos.ee.pm.core.PMContext;
 import org.jpos.ee.pm.core.PMException;
 import org.jpos.ee.pm.core.PMLogger;
 import org.jpos.ee.pm.core.PMMessage;
 import org.jpos.ee.pm.struts.PMStrutsException;
 
 public class AddAction extends RowActionSupport {
 	
 	public boolean testSelectedExist() { return false;	}
 
 	protected boolean prepare(PMContext ctx) throws PMException {
 		super.prepare(ctx);
 		if(ctx.getParameter(FINISH)==null){
 			//Creates bean and put it in session
 			Object obj;
 			try {
 				obj = getPMService().getFactory().newInstance (ctx.getEntity().getClazz());
 				ctx.getEntityContainer().setSelected(new EntityInstanceWrapper(obj));
 				ctx.getEntityContainer().setSelectedNew(true);
				return false;
 			} catch (ConfigurationException e) {
 				PMLogger.error(e);
 				ctx.getErrors().add(new PMMessage(ENTITY,e.getMessage()));
 				throw new PMException();
 			}
 		}
 		if(ctx.getSelected() == null){
 			ctx.getErrors().add(new PMMessage(ENTITY, "pm.instance.not.found"));
 			throw new PMException();
 		}
 		for (Field f : ctx.getEntity().getFields()) {
         	proccessField(ctx, f, ctx.getSelected());
         }
         if(!ctx.getErrors().isEmpty()) 
         	throw new PMException();
         
         return true;
 	}
 
 	protected void doExecute(PMContext ctx) throws PMException {
 		if(ctx.isWeak()){
 			getModifiedOwnerCollection(ctx, ctx.getEntity().getOwner().getEntity_property()).add(ctx.getSelected().getInstance());
 			String p = ctx.getEntity().getOwner().getLocal_property();
 			if(p != null){
 				ctx.getEntitySupport().set(ctx.getSelected().getInstance(), p, ctx.getOwner().getSelected());
 			}
 		}else{
 			try {
 				if(ctx.getEntity().isPersistent())
 					ctx.getEntity().getDataAccess().add(ctx, ctx.getSelected().getInstance());
 			} catch (ConstraintViolationException e) {
 				throw new PMStrutsException("constraint.violation.exception");
 			}
 		}
 	}
 	
 	protected boolean openTransaction() {
 		return true;
 	}
 }
