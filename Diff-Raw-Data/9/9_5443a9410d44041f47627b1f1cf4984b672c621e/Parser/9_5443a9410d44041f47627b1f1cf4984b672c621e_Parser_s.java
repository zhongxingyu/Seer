 package com.mustangexchange.polymeal;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 /**
  * Created by Blake on 8/6/13.
  */
 public class Parser
 {
     private Item item;
     private ItemSet items;
     private ArrayList<ItemSet> listItems;
 
     //counter for getting each h2 element since they are not a part of the the html table
     private int counter;
 
     private boolean parseDesc;
     private boolean price;
     private boolean name;
     private boolean soupAndSalad = false;
 
     private Document doc;
 
     public Parser(ArrayList<ItemSet> listItems,Document doc)
     {
         this.listItems = listItems;
         this.doc = doc;
     }
 
     //receives doc to parse and a boolean that determines whether the breakfast table is valid or not.
     public void parse(boolean breakfast)
     {
         Elements h2Eles = doc.getElementsByTag("h2");
         Elements tables = doc.select("table");
             //parses html with tag hierarchy starting with each table the moving to each table row, then table data and then each strong tag
         for(Element table : tables)
         {
             items = new ItemSet();
             String h2 = h2Eles.get(counter).text();
             items.setTitle(h2);
             for(Element tr : table.select("tr"))
             {
                 String itemName = null;
                 BigDecimal itemPrice = null;
                 String itemDesc = null;
 
                 String tempName = "";
 
                 //for storing each part of soup and salad
                 String one = null;
                 String two = null;
                 BigDecimal priceOne = null;
                 BigDecimal priceTwo = null;
 
                 for(Element td : tr.select("td"))
                 {
                     String strongName = td.select("strong").text();
                     String description = tr.select("td").text();
                     //handle special cases and remove unnecessary part of string for looks.
                     if(!strongName.equals("")&&!h2.equals("Breakfast"))
                     {
                         if(!strongName.contains("$"))
                         {
                             tempName = strongName;
                             name = true;
                             parseDesc = false;
                             if(strongName.contains("Combos - "))
                             {
                                 itemName = strongName.replace("Combos - ","");
                             }
                             else if(strongName.contains("Just Burgers - "))
                             {
                                 itemName = strongName.replace("Just Burgers - ","");
                             }
                             else if(strongName.contains("Just Sandwiches - "))
                             {
                                 itemName = strongName.replace("Just Sandwiches - ","");
                             }
                             else if(strongName.contains("Just Sandwiches - "))
                             {
                                 itemName = strongName.replace("Just Sandwiches- ","");
                             }
                             else if(strongName.contains("Soup and Salad"))
                             {
                                 soupAndSalad = true;
                                 one = strongName.substring(0,5);
                                 two = strongName.substring(9,14);
                             }
                             else
                             {
                                 itemName = strongName;
                             }
 
                         }
                         else if(strongName.contains("$"))
                         {
                             price = true;
                             parseDesc = true;
                             strongName = strongName.replace(",",".");
 
                             //automatically calculates tax if any.
                             if(strongName.contains("plus tax"))
                             {
                                 strongName = strongName.replace(" plus tax","");
                                 strongName = strongName.replace("$","");
                                 //set.getPrices().add(df.format(new Double(strongName)+new Double(strongName)*.08)+"");
                                itemPrice = (new BigDecimal(strongName).add(new BigDecimal(strongName))).multiply(new BigDecimal(".08"));
                             }
                             //gets proper values for anything per oz items by substringing them out.
                             else if(strongName.contains("per oz"))
                             {
                                 priceOne = new BigDecimal(strongName.substring(7,11));
                                 priceTwo = new BigDecimal(strongName.substring(26,30));
                             }
                             else
                             {
                                 strongName = strongName.replace("$","");
                                 itemPrice = new BigDecimal(strongName);
                             }
                         }
                     }
                     else if(h2.equals("Breakfast")&&breakfast)
                     {
                         if(strongName.contains("$"))
                         {
                             parseDesc = true;
                             itemPrice = new BigDecimal(strongName.replace("$",""));
                         }
                         else
                         {
                             parseDesc = false;
                             itemName = strongName;
                         }
                     }
                     if(parseDesc)
                     {
                        itemDesc = descParse(tempName,strongName,description);
                     }
                 }
                 if(itemName!=null)
                 {
                     if(soupAndSalad)
                     {
                         Item itemOne = new Item(one,priceOne,itemDesc,true);
                         Item itemTwo = new Item(two,priceTwo,itemDesc,true);
                         itemOne.setOunces(true);
                         itemTwo.setOunces(true);
                         items.add(itemOne);
                         items.add(itemTwo);
                         soupAndSalad = false;
                     }
                     else if(price&&name)
                     {
                         items.add(new Item(itemName,itemPrice,itemDesc,true));
                         price = false;
                         name = false;
                     }
                     else
                     {
                         items.add(new Item(itemName,itemPrice,itemDesc,false));
                         price = false;
                         name = false;
                     }
                 }
             }
                 //adds each table to itemset arraylist then adds one to counter to allow h2 tag selection(workaround for Cal Poly table formatting)
                 listItems.add(items);
                 counter++;
         }
     }
     private String descParse(String tempName,String tempPrice,String description)
     {
         //if it is a price value remove both the name and price from the description string.
         description = description.replace(tempName,"");
         description = description.replace(tempPrice,"");
         description = description.replace("$","");
         if(description.equals(" "))
         {
             return "No Description Available!";
         }
         else
         {
             return description.replaceFirst(" ", "");
         }
     }
 }
