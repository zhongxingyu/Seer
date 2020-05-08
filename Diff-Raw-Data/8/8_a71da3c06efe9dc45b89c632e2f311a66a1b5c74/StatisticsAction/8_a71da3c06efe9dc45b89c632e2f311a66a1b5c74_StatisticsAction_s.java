 package com.ai.action;
 
 import com.ai.model.Branch;
 import com.ai.model.Invoice;
 import com.ai.service.BranchService;
 import com.ai.service.InvoiceService;
 import com.ai.service.PermissionService;
 import com.ai.service.impl.BranchServiceImpl;
 import com.ai.service.impl.InvoiceServiceImpl;
 import com.ai.service.impl.PermissionServiceImpl;
 import com.ai.util.*;
 import com.ai.validator.Validator;
 import com.ai.validator.impl.ValidatorImpl;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import org.apache.struts2.interceptor.SessionAware;
 import org.hibernate.mapping.Array;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ishara
  * Date: 5/13/13
  * Time: 12:16 PM
  * To change this template use File | Settings | File Templates.
  */
 public class StatisticsAction extends ActionSupport implements SessionAware {
     private Map session = ActionContext.getContext().getSession();
     private ActiveUser activeUser;
     private Logger logger = LoggerFactory.getLogger(ReportListAction.class);
     ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
     PermissionService permissionService = (PermissionServiceImpl) applicationContext.getBean("permissionService");
     Validator validator = (ValidatorImpl) applicationContext.getBean("validator");
     BranchService branchService = (BranchServiceImpl) applicationContext.getBean("branchService");
     InvoiceService invoiceService = (InvoiceServiceImpl) applicationContext.getBean("invoiceService");
 
     public Map<Long, String> branchList = new HashMap<Long, String>();
     private List<Long> branchId = new ArrayList<Long>();
     private String period;
     private List<SalesHistory> totalSales = new ArrayList<SalesHistory>();
 
 
     private void init() {
         for (Branch b : branchService.findAllByTenant(activeUser.getTenant())) {
             branchList.put(b.getBranchId(), b.getBranchName());
         }
     }
 
     private String handelValidator(Return r){
         if( r == Return.VALIDATION_FAIL){
             addActionError("Invalid request");
             return Return.INVALID_REQUEST.getReturnCode();
         } else if( r == Return.AUTHENTICATION_FAIL){
             addActionError("Please login to the system");
             return Return.INVALID_SESSION.getReturnCode();
         } else {
             addActionError("User not authorised");
             return Return.INVALID_SESSION.getReturnCode();
         }
     }
 
     public String execute(){
         logger.info("Statistic action execute");
         try{
             activeUser = (ActiveUser)session.get("activeUser");
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.STATISTICS.getPermission()), hasActionErrors(), hasFieldErrors());
             if( r == Return.VALIDATOR_OK){
                 init();
                 return Return.SUCCESS.getReturnCode();
             } else {
                 return handelValidator(r);
             }
         } catch (Exception e){
             e.printStackTrace();
             return Return.INTERNAL_ERROR.getReturnCode();
         }
     }
     
     public String search(){
         logger.info("Search by branch:[{}]..p:[{}]", branchId, period);
         try{
             activeUser = (ActiveUser)session.get("activeUser");
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.STATISTICS.getPermission()), hasActionErrors(), hasFieldErrors());
             if( r == Return.VALIDATOR_OK){
                 if(period != null && branchId != null){
                     totalSales = searchHistory(branchId, period);
                 } else {
                     addActionError("please select branch/s and period");
                 }
                 init();
                 return Return.SUCCESS.getReturnCode();
             } else {
                 return handelValidator(r);
             }
         } catch (Exception e){
             e.printStackTrace();
             return Return.INTERNAL_ERROR.getReturnCode();
         }    
     }
 
 
     private List<SalesHistory> searchHistory(List<Long> branchIds, String period){
         Calendar currentDate = Calendar.getInstance();
         List<SalesHistory> sh = new ArrayList<SalesHistory>();
         List<Invoice> invoices = new ArrayList<Invoice>();
 
         if(period.equalsIgnoreCase(Period.MONTHLY.getPeriod())){
             for (Long id : branchIds) {
                 SalesHistory salesHistory = new SalesHistory();
                 salesHistory.setBranchName(branchService.findBranchById(id).getBranchName());
 
                 int to = currentDate.getMaximum(Calendar.DAY_OF_MONTH);
                 for(int from=1; from<to; from=from+6 ){
                     Calendar fromDate = Calendar.getInstance();
                     Calendar toDate = Calendar.getInstance();
                     double weeklyAmount = 0;
                     fromDate.set(Calendar.DAY_OF_MONTH, from);
                     toDate.set(Calendar.DAY_OF_MONTH, from+6);
                     invoices = invoiceService.searchByDateAndBranch(fromDate.getTime(), toDate.getTime(), id.toString());
                     for (Invoice invoice : invoices) {
                         weeklyAmount = weeklyAmount + invoice.getTotalAmount();
                     }
                     salesHistory.getDailySales().add(weeklyAmount);
                 }
                 sh.add(salesHistory);
             }
         } else if(period.equalsIgnoreCase(Period.ANNUALLY.getPeriod())){
             for (Long id : branchIds) {
                 SalesHistory salesHistory = new SalesHistory();
                 salesHistory.setBranchName(branchService.findBranchById(id).getBranchName());
 
                 int to = currentDate.getMaximum(Calendar.DAY_OF_YEAR);
                 for(int from=1; from<to; from=from+30){
                     Calendar fromDate = Calendar.getInstance();
                     Calendar toDate = Calendar.getInstance();
                     double monthlyAmount = 0;
                     fromDate.set(Calendar.DAY_OF_YEAR, from);
                     toDate.set(Calendar.DAY_OF_YEAR, from+30);
                     invoices = invoiceService.searchByDateAndBranch(fromDate.getTime(), toDate.getTime(), id.toString());
                     for (Invoice invoice : invoices) {
                         monthlyAmount = monthlyAmount + invoice.getTotalAmount();
                     }
                     salesHistory.getDailySales().add(monthlyAmount);
                 }
                 sh.add(salesHistory);
             }
         } else {
             for (Long id : branchIds) {
                 SalesHistory salesHistory = new SalesHistory();
                 salesHistory.setBranchName(branchService.findBranchById(id).getBranchName());
 
                 int from1 = currentDate.get(Calendar.DAY_OF_MONTH) - currentDate.get(Calendar.DAY_OF_WEEK);
 
                int to = from1+6;
                logger.info("---------from:[{}] todate:[{}]",from1,to);

                 for(int from=from1; from<to; from++){
                     Calendar fromDate = Calendar.getInstance();
                     Calendar toDate = Calendar.getInstance();
                     double dailyAmount = 0;
                     fromDate.set(Calendar.DAY_OF_MONTH, from);
                     fromDate.set(Calendar.HOUR_OF_DAY, 0);
                     fromDate.set(Calendar.MINUTE, 0);
                     toDate.set(Calendar.DAY_OF_MONTH, from);
                     toDate.set(Calendar.HOUR_OF_DAY, 24);
                     toDate.set(Calendar.MINUTE, 59);
                    logger.info("---------search from:[{}] to:[{}]",fromDate.getTime(), toDate.getTime());
                     invoices = invoiceService.searchByDateAndBranch(fromDate.getTime(), toDate.getTime(), id.toString());
                    logger.info("--------found :[{}] records", invoices.size());
                     for (Invoice invoice : invoices) {
                         dailyAmount = dailyAmount + invoice.getTotalAmount();
                     }
                     salesHistory.getDailySales().add(dailyAmount);
                 }
                 sh.add(salesHistory);
             }
         }
         return sh;
     }
 
 
     @Override
     public void setSession(Map session1) {
         this.session = session1;
     }
 
     public List<Long> getBranchId() {
         return branchId;
     }
 
     public void setBranchId(List<Long> branchId) {
         this.branchId = branchId;
     }
 
     public String getPeriod() {
         return period;
     }
 
     public void setPeriod(String period) {
         this.period = period;
     }
 
     public List<SalesHistory> getTotalSales() {
         return totalSales;
     }
 
     public void setTotalSales(List<SalesHistory> totalSales) {
         this.totalSales = totalSales;
     }
 }
