 package sse.client.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import org.apache.log4j.Logger;
 import sse.ejb.dao.CustomerDAO;
 import sse.model.Customer;
 
 @ManagedBean(name="customerCtrl")
 @SessionScoped
 public class CustomerController {
 
     private static final Logger log = Logger.getLogger(CustomerController.class);
 
 	@EJB
 	private CustomerDAO dao;
 	private String filterCustTxt;
 
 	public CustomerController() {
 		customers = new ArrayList<Customer>();
 	}
 
 	private List<Customer> customers;
 	private Customer selectedCustomer = new Customer();
 
 	public List<Customer> getCustomers() {
 		return customers;
 	}
 
 	public void setCustomers(List<Customer> customers) {
 		this.customers = customers;
 	}
 
 	public Customer getSelectedCustomer() {
 		return selectedCustomer;
 	}
 
 	public void setSelectedCustomer(Customer selectedCustomer) {
 		this.selectedCustomer = selectedCustomer;
 	}
 
 	public void create() {
 		this.selectedCustomer = new Customer();
 	}
 
 	public void save() {
 		dao.save(selectedCustomer);
 		load();
 	}
 
 	public void delete() {
 		dao.delete(selectedCustomer);
 		this.selectedCustomer = new Customer();
 		load();
 	}
 	
 	public List<Customer> filterCust() {
         if (!filterCustTxt.equals("")) {
            dao.findByNamedQuery("filterCustomers", filterCustTxt);
         } else {
			dao.findAll();
 		}
 
         return customers;
 	}
 	
 	public String load() {
         log.info("load");
         customers = dao.findAll();
 
         return "customer.jsf";
     }
 
 	public void setFilterCustTxt(String filterCustTxt) {
 		this.filterCustTxt = filterCustTxt;
 	}
 
 	public String getFilterCustTxt() {
 		return filterCustTxt;
 	}
 
 }
