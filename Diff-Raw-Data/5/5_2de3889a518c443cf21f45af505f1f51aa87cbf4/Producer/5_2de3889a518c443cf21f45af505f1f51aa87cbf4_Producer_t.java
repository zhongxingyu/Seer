 package scape;
 
 import scape.Message.Content;
 
 public class Producer extends Agent {
 
     private int stock;
     private int sellPrice;
     // Sale Variables
     private int production = 10;
     private int saleQuantity = 20;
     private int upperSL = 75;
     private int lowerSL = 25;
 
     // The Producer Constructor
     public Producer(Scape controller, String food) {
         super(controller, "producer");
         this.setProduct(food);
         stock = 100;
         sellPrice = 50;
     }
 
     // The Producer's act function, called once per step, handling all the Producer's behavior.
     public void act() {
         increaseStocks();
         updateSellPrice();
         if (messageWaiting) {
             handleMessages();
         }
     }
 
     // Increasing the Producer's stocks, called once per step, to simulate production.
     private void increaseStocks() {
         if (stock > (100 - production)) {
             stock = 100;
         } else {
             stock = stock + production;
         }
     }
 
     // Evaluating the Producer's sellprice, called once per step, adjusting it based on the current stock.
     // "upperS(tock)L(imit)" is set to 75, lowerS(tock)L(imit)" to 25.
     private void updateSellPrice() {
         if (stock > upperSL && sellPrice > 1) {
             sellPrice--;
         }
         if (stock < lowerSL && sellPrice < 100) {
             sellPrice++;
         }
     }
 
     // Handling all messages received this step, then emptying the message Vector.
     private void handleMessages() {
         for (Message message : messages) {
             Content content = message.content();
             switch (content) {
                 case WHAT_IS_PRICE:
                     if (getProduct().equals(message.what())) {
                         message.sender().deliverMessage(new Message(this, Message.Content.PRICE_IS, getProduct(), sellPrice));
                     }
                     break;
                 case ACCEPT_PRICE:
                     sell();
                     break;
                 default:
                     System.exit(1);
             }
         }
        messages.clear();
        messageWaiting = false;
     }
 
     // Handling a sale, by decreasing stock and increasing the sellPrice.
     private void sell() {
         stock = stock - saleQuantity;
         sellPrice++;
     }
 
     // Returns the Producers' stock.
     public int getStock() {
         return stock;
     }
 
     // Returns the Producer's sellPrice.
     public int getSellPrice() {
         return sellPrice;
     }
 }
