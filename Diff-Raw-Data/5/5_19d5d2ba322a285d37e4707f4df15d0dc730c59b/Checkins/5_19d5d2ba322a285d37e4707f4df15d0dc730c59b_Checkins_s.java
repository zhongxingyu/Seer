 package controllers;
 
 import java.lang.reflect.Constructor;
 import java.util.Calendar;
 import java.util.List;
 
 import models.BorrowItem;
 import models.Item;
 import models.ItemStatus;
 
 import play.db.Model;
 import play.exceptions.TemplateNotFoundException;
 import utils.Globals;
 import controllers.CRUD.ObjectType;
 import flexjson.JSONSerializer;
 
 public class Checkins extends CRUD{
 	public static void blank() throws Exception {
             render("Checkins/scanItem.html");
 	}
 	
 	public static void checkin(String[] itemBarcodeScanned) throws Exception {
 		
 	}
 	
 	public static void scanItem(String barcode) throws Exception {
 		// 
 		//TODO: Lookup the barcode from the borrowed material.
 		List<BorrowItem> items = BorrowItem.find("item.barcode = ?", barcode).fetch();
 		
 		int numberFound = items.size();
 		if (numberFound==1) { /* found the exact item */
 			BorrowItem borrowItem = items.get(0);
 			//Save the returned date
 			borrowItem.returnedDate = Calendar.getInstance().getTime();
 			borrowItem.save();
 			
 			Item item = borrowItem.item;
 			String info = toJson(item);
 			//Change the status of the Item
 			ItemStatus shelfStatus = ItemStatus.find("code = ?", Globals.ItemStatus_Shelf).first();
 			item.itemStatus = shelfStatus;
 			item.save();
 			
 			renderJSON(info);
 		} else if (numberFound==0) { /* found nothing */
			response.status =  700;
 		} else { /* more than one items found */
            response.status = 710;			
 		}
 	}
 	
 	private static String toJson(Item item) {
 		JSONSerializer borrowedItemSerializer = new JSONSerializer().include("barcode", "name").exclude("*");
 		String json = borrowedItemSerializer.serialize(item);
 		return json;
 	}
 }
