 package ml.boxes.item;
 
 import ml.boxes.block.MetaType;
 import net.minecraft.util.Icon;
 
 public enum ItemType {
 	Cardboard(),
 	Label();
 	
 	public String iconRef;
 	public Icon ico;
 	
 	private ItemType() {
 		iconRef = "Boxes:" + this.name().toLowerCase();
 	}
 	
 	public static ItemType fromMeta(int meta){
 		if (meta >-1 && meta<ItemType.values().length){
 			return ItemType.values()[meta];
 		}
 		return null;
 	}
 }
