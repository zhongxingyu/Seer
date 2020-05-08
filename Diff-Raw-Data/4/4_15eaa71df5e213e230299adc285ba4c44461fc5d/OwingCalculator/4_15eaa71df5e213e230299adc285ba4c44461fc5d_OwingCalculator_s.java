 package com.github.notyy.reafactoring.owingPrinter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class OwingCalculator {
 
     private String name;
     private List<OrderItem> orderItems;
 
     public OwingCalculator(String name, List<OrderItem> orderItems){
         this.name = name;
         this.orderItems = orderItems;
     }
 
     public void printOwing() {

         //print logo
         System.out.println("********************");
         System.out.println("***customer owing***");
         System.out.println("********************");
 
         //calculate total
        double total = 0.0;
         for(OrderItem orderItem: orderItems){
             total += orderItem.getAmount();
         }
 
         //print details
         System.out.println("name:" + name);
         System.out.println("amount:" + total);
     }
 
     public static void main(String[] args){
         List<OrderItem> orderItems = new ArrayList<OrderItem>();
         orderItems.add(new OrderItem(25.0));
         orderItems.add(new OrderItem(35.0));
         orderItems.add(new OrderItem(40.0));
 
         new OwingCalculator("notyy", orderItems).printOwing();
     }
 }
