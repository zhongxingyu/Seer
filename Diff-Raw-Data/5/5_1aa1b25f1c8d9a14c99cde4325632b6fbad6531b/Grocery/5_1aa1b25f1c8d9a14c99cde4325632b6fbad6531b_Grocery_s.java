 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Grocery extends Controller {
 	
     public static void index(long roomId){
     	String username = Security.connected();
     	Room currentRoom = Room.find("byName","DefaultRoom").first();
     	List<GroceryItem> groceries=models.GroceryItem.find("roomId=? order by id desc",roomId).fetch();
     	List roomys=currentRoom.roomysList;
     	int allItemsCount = groceries.size();
     	int doneItem = 0;
     	for (GroceryItem i : groceries){
     		if (i.status)
     			doneItem++;
     	}
      	render(groceries,roomys,roomId, username,doneItem,allItemsCount);
     }
     
     public static void addGrocery(long roomId, String name,String assignment, String deadline, String userSelection){
     	Room currentRoom = Room.findById(roomId);
     	int dead=Integer.parseInt(deadline);
     	int select=Integer.parseInt(userSelection);
     	if(select==2)
     		dead=dead*24;
     	else if(select==3)
     		dead=dead*24*7;
     	Date today =new Date();
     	models.GroceryItem gro=new GroceryItem(name,false,false,"http://www.lidl.de/de/shop@Search?query="+name,assignment,dead,today,currentRoom.id);
     	currentRoom.groceryList.addItem(gro);
     	Grocery.index(roomId);
      }
     
     public static void deleteGrocery(long roomId, long itemId){
     	GroceryItem grocery = GroceryItem.findById(itemId);
     	Room currentRoom = Room.findById(roomId);
     	currentRoom.groceryList.deleteGroceryItem(grocery);
         Grocery.index(roomId); 
     }
     
     public static void updateGrocery(long roomId, long itemId, String name,String assignment, String deadline, String userSelection, String status, String important){
     	GroceryItem grocery= GroceryItem.findById(itemId);
     	grocery.name = (name!= null) ? name : grocery.name;
     	grocery.assignment= (assignment!=null) ? assignment : grocery.assignment;
     	grocery.deadline= (deadline!=null) ? Integer.parseInt(deadline): grocery.deadline;
    	   grocery.status= (status!=null) ? ( (status.equals("true")) ? true : false) : grocery.status;
   	   grocery.important= (important!=null) ? ( (important.equals("true")) ? true : false) : grocery.important;
   	   System.out.println(status);
   	   System.out.println(grocery.status);
   	   grocery.save();
   	   index(roomId);
      }
      
 
 
 }    
