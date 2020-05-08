 package com.practo.action;
 
 import com.practo.TicketForm;
 import com.practo.bc.ZendeskApiBc;
 import com.practo.entity.TicketCriteria;
 import com.practo.entity.ZendeskTicket;
 import com.practo.enums.ReportType;
 import com.practo.enums.TicketStatus;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 
 /**
  * Company: AcStack
  * User: Shwetanka
  * Date: Apr 7, 2012
  * Time: 11:50:14 AM
  */
 public class IndexAction extends DispatchAction{
   private ZendeskApiBc zendeskApiBc;
 
   @Override
   protected ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
     return list(mapping, form, request, response);
   }
 
   public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
     TicketForm ticketForm = (TicketForm) form;
 
     TicketCriteria criteria  = getTicketCriteriaFromForm(ticketForm);
 
     List<ZendeskTicket> tickets = this.zendeskApiBc.getTicketsByCriteria(criteria);
     if(tickets!=null){
       request.setAttribute("tickets", tickets);
     }
     request.setAttribute("reportTypes", ReportType.values());
     request.setAttribute("statusTypes", TicketStatus.values());
     return mapping.findForward("list");
   }
 
   private TicketCriteria getTicketCriteriaFromForm(TicketForm form){
     TicketCriteria criteria = new TicketCriteria();
     TicketStatus status = TicketStatus.getStatusByValue(form.getStatusId());
     if(status!=null){
       criteria.setStatus(status);
    }else {
      criteria.setStatus(TicketStatus.OPEN);
     }
     ReportType type = ReportType.getReportTypeByValue(form.getStrType());
     if(type!=null){
       criteria.setType(type);
    }else {
      criteria.setType(ReportType.DAILY);
     }
     return criteria;
   }
 
   public ZendeskApiBc getZendeskApiBc() {
     return zendeskApiBc;
   }
 
   public void setZendeskApiBc(ZendeskApiBc zendeskApiBc) {
     this.zendeskApiBc = zendeskApiBc;
   }
 }
