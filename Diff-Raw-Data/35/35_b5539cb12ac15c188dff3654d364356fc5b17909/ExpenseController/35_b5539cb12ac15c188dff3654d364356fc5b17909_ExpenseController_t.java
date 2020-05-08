 package cz.muni.fi.pv243.et.controller;
 
 import cz.muni.fi.pv243.et.model.*;
 
 import cz.muni.fi.pv243.et.service.ExpenseReportService;
 import cz.muni.fi.pv243.et.util.CurrentPerson;
 import org.jboss.solder.logging.Logger;
 
 
 import javax.enterprise.inject.Model;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.*;
 
 
 @Model
 public class ExpenseController {
 
     @Inject
     private ExpenseModel model;
 
     @Inject
     private ExpenseReportService service;
 
     @Inject
     @CurrentPerson
     private PersonWrapper currentPerson;
 
     @Inject
     Logger logger;
 
     private Map<Long, Boolean> checked = new HashMap<Long, Boolean>();
 
     public Map<Long, Boolean> getChecked() {
         return checked;
     }
 
     public void setChecked(Map<Long, Boolean> checked) {
         this.checked = checked;
     }
 
     @Produces
     @Named("expenseReports")
     public Collection<ExpenseReport> getAllReports() {
         return service.findAll();
     }
 
     public Collection<ExpenseReport> allOpenForVerifier() {
         Collection<ExpenseReport> list = new ArrayList<ExpenseReport>();
         list.addAll(service.findForVerifierWithStatus(currentPerson.getPerson(), ReportStatus.SUBMITTED) );
         list.addAll(service.findForVerifierWithStatus(currentPerson.getPerson(), ReportStatus.APPROVED));
         return list;
     }
 
 
 
     public Collection<ExpenseReport> getAllForSubmitter() {
         return service.findForSubmitter(currentPerson.getPerson());
     }
 
     public Collection<ExpenseReport> getAllWithNoVerifierAssigned() {
         List<ExpenseReport> reports = (List<ExpenseReport>) service.findWithNoVerifierAssigned();

         return reports;
     }
 
     public String showSingleReport(Long id, String fromUrl) {
         model.setBackUrl(fromUrl);
         model.setReport(service.get(id));
 
         return "/secured/report";
     }
 
     public String editReport(Long id) {
         model.setReport(service.get(id));
 
         return "/secured/editReport";
     }
 
     public String createReport() {
         model.setReport(new ExpenseReport());
 
         return "/secured/createReport";
     }
 
     public String saveReport() {
         ExpenseReport r = model.getReport();
         boolean isNew = r.getId() == null;
 
         r.setLastChangeDate(new Date());
         if (isNew) {
             r.setStatus(ReportStatus.OPEN);
             r.setSubmitter(currentPerson.getPerson());
             r.setMoneyTransfers(new ArrayList<MoneyTransfer>());
             r.setPayments(new ArrayList<Payment>());
             r.setLastSubmittedDate(new Date());
         }
         service.save(r);
 
         model.setReport(r);
 
         return "/secured/report?faces-redirect=true";
     }
 
     public String claimReports() {
         System.out.println("claimReports()" + checked.keySet() + checked.values());
         for (Long k : checked.keySet()) {
             if (checked.get(k)) {
                ExpenseReport er = service.get(k);
                if (er != null) {
                    logger.debug("claimReports()" + er.getId() + er.getName());
                    service.claim(er, currentPerson.getPerson() );
                } else {
                    logger.debug("unknown report with id: " + k);
                 }
             }
         }
 
         return "/secured/reports";
     }
 
     public String submitReport() {
         service.submit(model.getReport());
         return "/secured/reports";
     }
 
     public boolean isSubmittable() {
         ReportStatus status = model.getReport().getStatus();
         boolean submittableByStatus = !(status == ReportStatus.SUBMITTED || status == ReportStatus.APPROVED || status == ReportStatus.SETTLED);
         boolean submittableByPayment = !(model.getReport().getPayments().isEmpty());
 
         return (submittableByPayment && submittableByStatus);
     }
 
     public boolean isOpen() {
         ReportStatus status = model.getReport().getStatus();
         return (status == ReportStatus.OPEN);
     }
 
     public boolean isSubmitted() {
         ReportStatus status = model.getReport().getStatus();
         return (status == ReportStatus.SUBMITTED);
     }
 
     public boolean isSettled() {
         ReportStatus status = model.getReport().getStatus();
         return (status == ReportStatus.SETTLED);
     }
 
     public boolean isApproved() {
         ReportStatus status = model.getReport().getStatus();
         return (status == ReportStatus.APPROVED);
     }
 
     public boolean isRejected() {
         ReportStatus status = model.getReport().getStatus();
         return (status == ReportStatus.REJECTED);
     }
 
     public boolean hasVerifier() {
         return (model.getReport().getVerifier() != null);
     }
 
     public String rejectReport() {
         service.setStatus(model.getReport(), ReportStatus.REJECTED);
         return "/secured/report";
     }
 
     public String approveReport() {
         service.approve(model.getReport());
         return "/secured/report";
     }
 
     public String sendMoney() {
         //TODO -- maybe create automatic money-transfer?
         service.setStatus(model.getReport(), ReportStatus.SETTLED);
         return "/secured/report";
     }
 
     public String cancel() {
         return model.getBackUrl();
     }
 
 
 
 }
