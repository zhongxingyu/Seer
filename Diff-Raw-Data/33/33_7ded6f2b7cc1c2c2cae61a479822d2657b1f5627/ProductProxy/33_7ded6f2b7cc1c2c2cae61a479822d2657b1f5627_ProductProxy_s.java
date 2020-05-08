 package dit126.group4.group4shop_app.client;
 
 import dit126.group4.group4shop.core.Product;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author Group4
  */
 
 @XmlRootElement(name="product")
 @XmlAccessorType(XmlAccessType.PROPERTY)
 public class ProductProxy {
     
     private Product product;
     
     
     protected ProductProxy(){
     }
     public ProductProxy(Product product) {
         this.product = product;
     }
     
     @XmlElement
     public String getName() {
         return product.getName();
     }
     
     @XmlElement
     public Long getId() {
         return product.getId();
     }
     
     @XmlElement
     public double getPrice() {
         return product.getPrice();
     }
     
    /*@XmlElement
     public String getCategory(){
         return product.getCategory();
    }*/
     
     @XmlElement
     public String getDescription() {
         return product.getDescription();
     }
 }
