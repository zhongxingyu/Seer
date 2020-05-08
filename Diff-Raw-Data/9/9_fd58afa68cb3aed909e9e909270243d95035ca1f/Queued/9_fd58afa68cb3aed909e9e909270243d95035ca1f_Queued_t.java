 package nl.giantit.minecraft.GiantShop.API.GSW;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.core.Items.ItemID;
 
 /**
  *
  * @author Giant
  */
 public class Queued {
 	
 	private int id;
 	private int type;
	private Integer itemType;
 	private int amount;
 	private String transactionID;
 	
 	public Queued(int id, int type, int amount, String transactionID) {
 		this.id = id;
 		this.type = type;
		this.itemType = (type <= 0) ? null : type;
 		this.amount = amount;
 		this.transactionID = transactionID;
 	}
 	
 	public int getItemID() {
 		return this.id;
 	}
 	
 	public int getItemType() {
 		return this.type;
 	}
 	
 	public ItemID getItemIDOBJ() {
		return GiantShop.getPlugin().getItemHandler().getItemIDByName(GiantShop.getPlugin().getItemHandler().getItemNameByID(this.id, this.itemType));
 	}
 	
 	public String getItemName() {
		return GiantShop.getPlugin().getItemHandler().getItemNameByID(this.id, this.itemType);
 	}
 	
 	public int getAmount() {
 		return this.amount;
 	}
 	
 	public String getTransactionID() {
 		return this.transactionID;
 	}
 	
 	@Override
 	public String toString() {
 		return "{transaction: " + this.transactionID + ", itemID: " + this.id + ", itemType: " + this.type + ", amount: " + this.amount + "}";
 	}
 	
 }
