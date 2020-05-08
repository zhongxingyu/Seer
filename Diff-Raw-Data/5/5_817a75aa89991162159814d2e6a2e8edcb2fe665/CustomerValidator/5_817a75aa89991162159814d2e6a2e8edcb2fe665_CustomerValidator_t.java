 package cz.muni.fi.bapr.entity.validator;
 
 import cz.muni.fi.bapr.entity.Customer;
 import cz.muni.fi.bapr.service.CustomerService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 /**
  * @author Andrej Kuročenko <andrej@kurochenko.net>
  */
 @Component
 public class CustomerValidator implements Validator {
 
     @Autowired
     @Qualifier("springValidator")
     private Validator validator;
 
     @Autowired
     private CustomerService customerService;
 
 
     @Override
     public boolean supports(Class<?> clazz) {
         return Customer.class.equals(clazz);
     }
 
     @Override
     public void validate(Object target, Errors errors) {
 
         Customer customer = (Customer) target;
 
         if (customer.getId() == null) {
             if ((customerService.findByEmail(customer.getEmail()) != null)) {
                errors.rejectValue("email", "customer.validation.mail.exists");
             }
         } else {
             Customer customerDB = customerService.findByEmail(customer.getEmail());
             if ((customerDB != null) && !customerDB.getId().equals(customer.getId())) {
                errors.rejectValue("email", "customer.validation.mail.exists");
             }
         }
 
         validator.validate(target, errors);
     }
 }
