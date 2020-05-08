 package hylinn.minecraft.ElementalWands.item;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 
 public enum EnumWandMaterial {
 	WOOD   (10, 10, Item.stick),
 	BONE   (20, 15, Item.bone),
	IRON   (30, 12, Item.ingotGold),
	QUARTZ (60, 12, Item.ingotIron),
	GOLD   (50, 20, Item.field_94583_ca);
 	
 	private final int enchantability;
 	private final int maxCharges;
 	private final Object material;
 	
 	private EnumWandMaterial(int maxCharges, int enchantability, Item material) {
 		this.enchantability = enchantability;
 		this.maxCharges = maxCharges;
 		this.material = material;
 	}
 	
 	private EnumWandMaterial(int maxCharges, int enchantability, Block material) {
 		this.enchantability = enchantability;
 		this.maxCharges = maxCharges;
 		this.material = material;
 	}
 	
 	public int getMaxCharges()
     {
         return this.maxCharges;
     }
 	
 	public int getEnchantability()
     {
         return this.enchantability;
     }
 	
 	public Object getMaterial() {
 		return this.material;
 	}
 	
 	public int getMaterialID() {
 		return material instanceof Item ? ((Item) material).itemID : ((Block) material).blockID;
 	}
 }
