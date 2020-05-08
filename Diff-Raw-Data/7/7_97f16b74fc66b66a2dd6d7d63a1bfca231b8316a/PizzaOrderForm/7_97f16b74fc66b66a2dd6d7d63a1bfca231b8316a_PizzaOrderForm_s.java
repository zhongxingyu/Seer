 package org.jdto.demo.dtos;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.validation.constraints.Size;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.jdto.annotation.DTOTransient;
 import org.jdto.annotation.Source;
 
 /**
  *
  * @author juancavallotti
  */
 public class PizzaOrderForm implements Serializable {
     
     @NotEmpty //validation annotation
     @Source("whoOrderedName")
     private String customerName;
     
     @NotEmpty //validation
     @Source("contactNumber")
     private String customerPhone;
     
     @NotEmpty //validation
     private String customerAddress;
     
     @Size(min=1) //validation
     @DTOTransient
     private List<PizzaDTO> pizzas;
     
     public String getCustomerAddress() {
         return customerAddress;
     }
 
     public void setCustomerAddress(String customerAddress) {
         this.customerAddress = customerAddress;
     }
 
     public String getCustomerName() {
         return customerName;
     }
 
     public void setCustomerName(String customerName) {
         this.customerName = customerName;
     }
 
     public String getCustomerPhone() {
         return customerPhone;
     }
 
     public void setCustomerPhone(String customerPhone) {
         this.customerPhone = customerPhone;
     }
 
     public List<PizzaDTO> getPizzas() {
         return pizzas;
     }
 
     public void setPizzas(List<PizzaDTO> pizzas) {
         this.pizzas = pizzas;
     }
     
 }
