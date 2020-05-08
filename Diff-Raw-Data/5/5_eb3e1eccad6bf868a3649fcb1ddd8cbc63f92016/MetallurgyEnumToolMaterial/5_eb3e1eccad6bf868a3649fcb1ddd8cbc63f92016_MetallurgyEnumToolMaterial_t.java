 // Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
 // Jad home page: http://www.kpdus.com/jad.html
 // Decompiler options: packimports(3) braces deadcode fieldsfirst 
 
 package shadow.mods.metallurgy;
 
 import net.minecraft.src.EnumToolMaterial;
 
 public enum MetallurgyEnumToolMaterial
 {
 	//Name, ID?, HarvestLevel, Durability, Strength, Damage, Enchantability
 	//Base
     //Wood("Wood", 0, 0, 60, 2F, 0, 15),
     //Stone("Stone", 0, 1, 132, 4F, 1, 5),
 	Copper("Copper", 0, 1, 180, 5F, 1, 5),
 	Bronze("Bronze", 0, 1, 250, 6F, 1, 9),
 	Hepatizon("Hepatizon", 0, 1, 300, 8F, 1, 22),
     //Iron("Iron", 0, 2, 251, 6F, 2, 14),
    DamascusSteel("DamascusSteel", 0, 1, 500, 6F, 2, 18),
     Angmallen("Angmallen", 0, 2, 300, 8F, 2, 30),
     Steel("Steel", 0, 2, 750, 8F, 3, 14),
     //Diamond("Diamond", 0, 3, 1562, 8F, 3, 10),
     //Nether
     Ignatius("Ignatius", 0, 3, 200, 4F, 2, 15),
     ShadowIron("ShadowIron", 0, 3, 300, 5F, 2, 2),
     ShadowSteel("ShadowSteel", 0, 3, 400, 6F, 2, 5),
     Midasium("Midasium", 0, 3, 100, 10F, 2, 35),
     Vyroxeres("Vyroxeres", 0, 3, 300, 7F, 2, 16),
     Ceruclase("Ceruclase", 0, 3, 500, 7F, 2, 18),
     Inolashite("Inolashite", 0, 3, 900, 8F, 3, 25),
     Kalendrite("Kalendrite", 0, 3, 1000, 8F, 3, 20),
     Amordrine("Amordrine", 0, 3, 500, 14F, 2, 50),
     Vulcanite("Vulcanite", 0, 3, 1500, 10F, 3, 20),
     Sanguinite("Sanguinite", 0, 3, 1750, 12F, 4, 25),
     //Fantasy
     Prometheum("Prometheum", 0, 3, 200, 4F, 1, 16),
     DeepIron("DeepIron", 0, 3, 250, 6F, 2, 14),
 	BlackSteel("BlackSteel", 0, 3, 500, 8F, 2, 17),
     Oureclase("Oureclase", 0, 3, 750, 8F, 2, 18),
     Aredrite("Aredrite", 0, 3, 750, 8F, 2, 18),
     AstralSilver("AstralSilver", 0, 3, 35, 12F, 1, 30),
     Carmot("Carmot", 0, 3, 50, 12F, 1, 40),
     Mithril("Mithril", 0, 3, 1000, 9F, 3, 18),
 	Quicksilver("Quicksilver", 0, 3, 1100, 14F, 3, 20),
     Haderoth("Haderoth", 0, 3, 1250, 12F, 3, 19),
     Orichalcum("Orichalcum", 0, 3, 1350, 9F, 3, 20),
 	Celenegil("Celenegil", 0, 3, 1600, 14F, 3, 50),
     Adamantine("Adamantine", 0, 3, 1550, 10F, 4, 22),
     Atlarus("Atlarus", 0, 3, 1750, 10F, 4, 22),
     Tartarite("Tartarite", 0, 3, 3000, 14F, 5, 25),
     //Precious
     Brass("Brass", 0, 3, 15, 18F, 1, 18),
     Silver("Silver", 0, 3, 25, 12F, 1, 20),
     //Gold("Gold", 0, 0, 75, 50F, 1, 22),
     Electrum("Electrum", 0, 3, 50, 14F, 1, 30),
     Platinum("Platinum", 0, 3, 100, 16F, 1, 50);
 
     static 
     {
         allToolMaterials = (new MetallurgyEnumToolMaterial[] {
            Copper, Bronze, Hepatizon, DamascusSteel, Angmallen, Steel, Ignatius, ShadowIron, ShadowSteel, Midasium, Vyroxeres, Ceruclase, Inolashite, Kalendrite, Amordrine, Vulcanite, Sanguinite, Mithril, Orichalcum, Adamantine, Brass, Silver, Platinum
         });
     }
   /*
     public static final EnumToolMaterial WOOD;
     public static final EnumToolMaterial STONE;
     public static final EnumToolMaterial IRON;
     public static final EnumToolMaterial EMERALD;
     public static final EnumToolMaterial GOLD;
 */
     private final int harvestLevel;
     private final int maxUses;
     private final float efficiencyOnProperMaterial;
     private final int damageVsEntity;
     private final int enchantability;
     private static final MetallurgyEnumToolMaterial allToolMaterials[]; /* synthetic field */
 /*
     public static final EnumToolMaterial[] values()
     {
         return (EnumToolMaterial[])allToolMaterials.clone();
     }
 
     public static EnumToolMaterial valueOf(String s)
     {
         return (EnumToolMaterial)Enum.valueOf(net.minecraft.src.EnumToolMaterial.class, s);
     }
 */
     private MetallurgyEnumToolMaterial(String s, int i, int j, int k, float f, int l, int i1)
     {
 //        super(s, i);
         harvestLevel = j;
         maxUses = k;
         efficiencyOnProperMaterial = f;
         damageVsEntity = l;
         enchantability = i1;
     }
 
     public int getMaxUses()
     {
         return maxUses;
     }
 
     public float getEfficiencyOnProperMaterial()
     {
         return efficiencyOnProperMaterial;
     }
 
     public int getDamageVsEntity()
     {
         return damageVsEntity;
     }
 
     public int getHarvestLevel()
     {
         return harvestLevel;
     }
 
     public int getEnchantability()
     {
         return enchantability;
     }
 
 
 }
