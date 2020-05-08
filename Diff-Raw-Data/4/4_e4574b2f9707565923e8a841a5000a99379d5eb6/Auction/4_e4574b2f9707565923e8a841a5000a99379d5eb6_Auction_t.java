 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mycompany.longcatarmy;
 
 import java.util.List;
 import java.util.Date;
 import java.util.HashMap;
 
 /**
  *
  * @author emesven
  */
 public class Auction {
     public String title;
     public String info;
     public Double price;
     public Date expireDate; 
     public Customer creator;
     public HashMap<Customer, Double> bidderMap; //för att kunna presentera listan på smidigt sätt
     public List<HashMap> bidderList;
     public List<Customer> flagList;
     public Long id;
     
     public Auction(Customer creator, String title, String info, Double price, Date expireDate){
         this.creator = creator;
         this.title = title;
         this.info = info;
         this.price = price;
         this.expireDate = expireDate;
     }
     
     public void addFlag(Customer c){
         //kolla sen när vi vet hur admin och customer ser ut
     }
     
     public void setBid(Customer bidder, Double price){
         if(this.price < price) {
             //eventuellt semaforer för att förhindra att två budar samtidigt?? kolla med handledare!
             bidderMap.put(bidder, price);
             bidderList.add(bidderMap);
             this.price += price;
         }
         else {
             //nåt felmeddelande, bud kan ej vara under aktuellt bud
         }
     }
     public String getTitle(){
         return title;
     }
     public String getInfo(){
         return info;
     }
     public Double getPrice(){
         return price;
     }
     public Date getExpire() {
         return expireDate;
     }
     public Customer getCreator(){
         return creator;
     }
    public List<HashMap> getBidder(){
        return bidderList;
     }
     public List<Customer> getFlagList(){
         return flagList;
     }
     public Long getId(){
         return id;
     }
 }
