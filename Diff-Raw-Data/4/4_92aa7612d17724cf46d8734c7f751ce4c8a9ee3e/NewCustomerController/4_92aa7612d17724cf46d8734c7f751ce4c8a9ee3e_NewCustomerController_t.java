 package dit126.group4.group4shop_app.controller;
 
 import dit126.group4.group4shop.core.Address;
 import dit126.group4.group4shop.core.Roles;
 import dit126.group4.group4shop.core.UserRoles;
 import dit126.group4.group4shop.core.Users;
 import dit126.group4.group4shop_app.model.Group4Shop;
 import dit126.group4.group4shop_app.view.NewCustomerBackingBean;
import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  *
  * @author Group4
  */
 @Named("newcustomerController")
@RequestScoped
 public class NewCustomerController {
     
     @Inject
     private NewCustomerBackingBean newCustomerBackingBean;
     
     @Inject
     private Group4Shop shop;
     
     
     public void register(){
         Users newCustomer = new Users(newCustomerBackingBean.getEmail(), newCustomerBackingBean.getFirstname(),
                newCustomerBackingBean.getLastname(), newCustomerBackingBean.getPassword());
         Address address = new Address(newCustomerBackingBean.getCountry(), newCustomerBackingBean.getCity(),
                 newCustomerBackingBean.getPostalcode(), newCustomerBackingBean.getStreet(), newCustomerBackingBean.getStreetnumber(),
                 newCustomerBackingBean.getEmail());
         
         shop.getUserRegister().add(newCustomer);
         shop.getAddressCatalogue().add(address);
         
         Roles role = shop.getRolesRegister().get("user");
         UserRoles newCustomerWithRole = new UserRoles(newCustomer, role);
         shop.getUserRoleRegister().add(newCustomerWithRole);
     }
     
 }
