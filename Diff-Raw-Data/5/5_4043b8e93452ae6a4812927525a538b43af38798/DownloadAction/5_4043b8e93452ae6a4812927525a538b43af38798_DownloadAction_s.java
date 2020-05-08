 /*
  The contents of this file are subject to the Jbilling Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.jbilling.com/JPL/
 
  Software distributed under the License is distributed on an "AS IS"
  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  License for the specific language governing rights and limitations
  under the License.
 
  The Original Code is jbilling.
 
  The Initial Developer of the Original Code is Emiliano Conde.
  Portions created by Sapienter Billing Software Corp. are Copyright 
  (C) Sapienter Billing Software Corp. All Rights Reserved.
 
  Contributor(s): ______________________________________.
  */
 
 /*
  * Created on Aug 11, 2004
  *
  */
 package com.sapienter.jbilling.client.invoice;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.validator.DynaValidatorForm;
 
 import com.sapienter.jbilling.client.util.GenericMaintainAction;
 import com.sapienter.jbilling.common.Constants;
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.interfaces.InvoiceSession;
 import com.sapienter.jbilling.interfaces.InvoiceSessionHome;
 
 /**
  * @author Marius Munteanu
  * 
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Style - Code Templates
  */
 public final class DownloadAction extends Action {
 
 	Logger log;
     private Date dateFrom;
     private Date dateTo;
 
 	public DownloadAction() {
 		log = Logger.getLogger(DownloadAction.class);
 	}
 
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 
         DynaValidatorForm downloadForm = (DynaValidatorForm) form;
         
         ActionErrors errors = validate(mapping, request, downloadForm);
 
         Integer operationType = (Integer) downloadForm.get("operationType");
 		if (operationType != null && errors.isEmpty()) {
 			Map map = new HashMap();
 			map.put("operationType", operationType);
 			map.put("entityId", request.getSession().getAttribute(
                                             com.sapienter.jbilling.client.util.Constants.SESSION_ENTITY_ID_KEY));
 
 			if (operationType.equals(
 					Constants.OPERATION_TYPE_CUSTOMER)) {
 				map.put("customer", new Integer(
                         (String) downloadForm.get("customer")));
 			} else if (operationType.equals(
 					Constants.OPERATION_TYPE_RANGE)) {
 				map.put("from", new Integer(
                         (String) downloadForm.get("rangeStart")));
 				map.put("to", new Integer(
                         (String) downloadForm.get("rangeEnd")));
 			} else if (operationType.equals(
 					Constants.OPERATION_TYPE_PROCESS)) {
 				map.put("process", new Integer(
                         (String) downloadForm.get("process")));
             } else if (operationType.equals(
                     Constants.OPERATION_TYPE_DATE)) {
                 map.put("date_from", dateFrom);
                 map.put("date_to", dateTo);
             } else if (operationType.equals(
                     Constants.OPERATION_TYPE_NUMBER)) {
                 map.put("number_from", (String) downloadForm.get("number_from"));
                 map.put("number_to", (String) downloadForm.get("number_to"));
             }
 
 			try {
 				JNDILookup EJBFactory = JNDILookup.getFactory(false);
 				InvoiceSessionHome invoiceHome = (InvoiceSessionHome) EJBFactory
 						.lookUpHome(InvoiceSessionHome.class,
 								InvoiceSessionHome.JNDI_NAME);
 				InvoiceSession invoiceSession = invoiceHome.create();
 				String filename = invoiceSession.generatePDFFile(map,
                         getServlet().getServletContext().getRealPath("/_FILE_NAME_"));
 
 				if (filename == null) {
 					ActionMessages messages = new ActionMessages();
 					messages.add(ActionMessages.GLOBAL_MESSAGE,
 						new ActionMessage("invoice.download.msg.no.invoices"));
 					saveMessages(request, messages);
 					return mapping.findForward("form");
 				} else {
 					request.setAttribute("filename", filename);
 					return mapping.findForward("done");
 				}
 
 			} catch (Exception e) {
 				log.error("Exception ", e);
 			}
 			return mapping.findForward("error");
 
 		} else {
             saveErrors(request, errors);
 			return mapping.findForward("form");
 		}
         
         
 	}
     
     public ActionErrors validate(ActionMapping mapping,
             HttpServletRequest request, DynaValidatorForm form) {
 
         ActionErrors errors = new ActionErrors();
         Integer operationType = (Integer) form.get("operationType");
         String customer = (String) form.get("customer");
         String rangeStart = (String) form.get("rangeStart");
         String rangeEnd = (String) form.get("rangeEnd");
         String process = (String) form.get("process");
         String numberFrom = (String) form.get("number_from");
         String numberTo = (String) form.get("number_to");
         
         log.debug("validating operation " + operationType);
 
         if (operationType != null) {
             if (operationType.equals(Constants.OPERATION_TYPE_CUSTOMER)) {
                 if (isEmpty(customer)) {
                     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                             "invoice.download.err.customer.required"));
                 } else if (Long.parseLong(customer) <= 0) {
                     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                             "invoice.download.err.positive.value"));
                 }
             } else if (operationType.equals(Constants.OPERATION_TYPE_RANGE)) {
                 if (isEmpty(rangeStart) || isEmpty(rangeEnd)) {
                     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                             "invoice.download.err.both.ranges"));
                 } else if ((!isEmpty(rangeStart) && (Long.parseLong(rangeStart) <= 0))
                         || (!isEmpty(rangeEnd) && (Long.parseLong(rangeEnd) <= 0))) {
                     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                             "invoice.download.err.positive.value"));
                 } else if (Long.parseLong(rangeStart) > Long
                         .parseLong(rangeEnd)) {
                     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                             "invoice.download.err.process"));
                 }
             } else if (operationType.equals(Constants.OPERATION_TYPE_PROCESS)) {
                 if (isEmpty(process)) {
                     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                             "invoice.download.err.process.required"));
                 } else if ((Long.parseLong(process) <= 0)) {
                     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                             "invoice.download.err.positive.value"));
                 }
             } else if (operationType.equals(Constants.OPERATION_TYPE_DATE)) {
                 GenericMaintainAction helper = new GenericMaintainAction(mapping, form, 
                         request);
                 dateFrom = helper.parseDate("date_from", process);
                 dateTo = helper.parseDate("date_to", process);
                 errors = helper.getErrors();
                 
                 if (errors.isEmpty() && dateFrom.after(dateTo)) {
                     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                             "invoice.download.err.dates.range"));
                 }
             } else if (operationType.equals(Constants.OPERATION_TYPE_NUMBER)) {
                 if (isEmpty(numberFrom) || isEmpty(numberTo)) {
                     errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                         "invoice.download.err.number.required"));
                 }
             }
         }
 
         return errors;
     }
 
     private final boolean isEmpty(final String str) {
         if ((str == null) || (str.trim().length() == 0)) {
             return true;
         }
         return false;
     }
 
 }
