 package mtudiscountstrategy;
 
 /**
  *
  * @author Mark Urbanski
  */
 public class LineItem {
     //Pass in product and quantity from Receipt.
     //Receipt will get product from FakeDatabase code in CodeSamples.txt
     private int quantity;
     private Product product;
 
     public LineItem(Product product, int quantity) {
         this.product = product;
         this.quantity = quantity;
     }
 
     public int getQuantity() {
         return quantity;
     }
 
     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }
 
     public Product getProduct() {
         return product;
     }
 
     public void setProduct(Product product) {
         this.product = product;
     }
     
     public double getExtendedPrice() {
         double extendedPrice = quantity * product.getProductCost();
                 
                 
         return extendedPrice;
     }
     
     public String getLineItemString() {
         //This will output a string that will have ID, Name, Unit Price, 
         //quantity, amount, and amount saved
         String outLine = product.getProductId() + "\t" +
                 product.getProductName() + "\t" +
                 product.getProductCost() + "\t" +
                 quantity + "\t" +
                 getExtendedPrice() + "\t" + 
                product.getDiscount(quantity, product.getProductCost())  ;
         return outLine;
     }
     
     public static void main(String[] args) {
         //This is where I will do my testing.
        DiscountStrategy ds = new PercentOffDiscount();
        ds.setDiscountRate(.1);
        Product prod = new Product("71", "Towel", 30.0, ds);
        LineItem line = new LineItem(prod, 12);
        System.out.println("Discount rate = .1, 12 items, 30/item");
        System.out.println(line.getLineItemString());
     }
 }
