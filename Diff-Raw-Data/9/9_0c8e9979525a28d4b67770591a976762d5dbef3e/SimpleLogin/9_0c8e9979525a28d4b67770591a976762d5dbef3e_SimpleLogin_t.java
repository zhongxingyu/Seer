 
 import modelbeans.CartModelBean;
 import modelbeans.CustomerRegistryBean;
 import core.Customer;
 import java.io.Serializable;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 @Named
 @SessionScoped
 public class SimpleLogin implements Serializable {
     @Inject
     CartModelBean cartModelBean;
     @Inject
     CustomerRegistryBean customerRegistryBean;
     private String loginname;
     private String password;
     private Customer customer;
     
     private static final Logger log = Logger.getLogger(SimpleLogin.class.getName());
     
     public SimpleLogin() {
     }
 
     public String getLoginname() {
         return loginname;
     }
 
     public void setLoginname(String loginname) {
         this.loginname = loginname;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String CheckValidUser() {
 
         List<Customer> results = customerRegistryBean.validateLogin(loginname, password);
         if (!results.isEmpty()) {
             customer = results.get(0);
         } else {
             return "login?faces-redirect=true";
         }
         customer.setCart(cartModelBean.getCart());
         return "shopProducts?faces-redirect=true";
     }
 
     
     public String LoginFromCheckout() {
 
         List<Customer> results = customerRegistryBean.validateLogin(loginname, password);
         if (!results.isEmpty()) {
             customer = results.get(0);
         }
         else {
             return "purchase?faces-redirect=true";
         }
         customer.setCart(cartModelBean.getCart());
         return "purchase?faces-redirect=true";
     }
 
         public String logout() {
         String result = "/jsf/index?faces-redirect=true";
 
         FacesContext context = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
 
         try {
             request.logout();
         } catch (ServletException e) {
             log.log(Level.SEVERE, "Failed to logout user!", e);
             result = "/loginError?faces-redirect=true";
         }
 
         return result;
     }
    
    public boolean isLoggedIn(String name)
    {
        if (name.equals("")){
        return false;    
        }
        else
            return true;
    }
 
 
     public Customer getCurrentUser() {
 
         return customer;
 
     }
 
     public Customer getCustomer() {
         return customer;
     }
 }
