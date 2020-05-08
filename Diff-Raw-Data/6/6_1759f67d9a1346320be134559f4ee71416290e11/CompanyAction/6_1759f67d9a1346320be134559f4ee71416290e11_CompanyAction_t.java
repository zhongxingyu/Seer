 package nl.bhit.webapp.action;
 
 import com.opensymphony.xwork2.Preparable;
 import nl.bhit.service.GenericManager;
 import nl.bhit.dao.SearchException;
 import nl.bhit.model.Company;
 import nl.bhit.webapp.action.BaseAction;
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
 import java.util.List;
 
 public class CompanyAction extends BaseAction implements Preparable {
     private GenericManager<Company, Long> companyManager;
     private List companies;
     private Company company;
     private Long id;
     private String query;
 
     public void setCompanyManager(GenericManager<Company, Long> companyManager) {
         this.companyManager = companyManager;
     }
 
     public List getCompanies() {
         return companies;
     }
 
     /**
      * Grab the entity from the database before populating with request parameters
      */
     public void prepare() {
         if (getRequest().getMethod().equalsIgnoreCase("post")) {
             // prevent failures on new
             String companyId = getRequest().getParameter("company.id");
             if (companyId != null && !companyId.equals("")) {
                 company = companyManager.get(new Long(companyId));
             }
         }
     }
 
     public void setQ(String q) {
         this.query = q;
     }
 
     public String list() {
         try {
             companies = companyManager.search(query, Company.class);
            Collection companiesNew = new LinkedHashSet(companies);
            companies = new ArrayList(companiesNew);
         } catch (SearchException se) {
             addActionError(se.getMessage());
             companies = companyManager.getAll();
         }
         return SUCCESS;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Company getCompany() {
         return company;
     }
 
     public void setCompany(Company company) {
         this.company = company;
     }
 
     public String delete() {
         companyManager.remove(company.getId());
         saveMessage(getText("company.deleted"));
 
         return SUCCESS;
     }
 
     public String edit() {
         if (id != null) {
             company = companyManager.get(id);
         } else {
             company = new Company();
         }
 
         return SUCCESS;
     }
 
     public String save() throws Exception {
         if (cancel != null) {
             return "cancel";
         }
 
         if (delete != null) {
             return delete();
         }
 
         boolean isNew = (company.getId() == null);
 
         companyManager.save(company);
 
         String key = (isNew) ? "company.added" : "company.updated";
         saveMessage(getText(key));
 
         if (!isNew) {
             return INPUT;
         } else {
             return SUCCESS;
         }
     }
 }
