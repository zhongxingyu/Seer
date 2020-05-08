 package zh.usefulthings.handlers;
 
 import zh.usefulthings.UsefulThings;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.monster.EntityBlaze;
 import net.minecraft.entity.monster.EntityCaveSpider;
 import net.minecraft.entity.monster.EntityCreeper;
 import net.minecraft.entity.monster.EntityEnderman;
 import net.minecraft.entity.monster.EntityGhast;
 import net.minecraft.entity.monster.EntityMagmaCube;
 import net.minecraft.entity.monster.EntityPigZombie;
 import net.minecraft.entity.monster.EntitySkeleton;
 import net.minecraft.entity.monster.EntitySlime;
 import net.minecraft.entity.monster.EntitySpider;
 import net.minecraft.entity.monster.EntityZombie;
 import net.minecraft.entity.passive.EntityChicken;
 import net.minecraft.entity.passive.EntityCow;
 import net.minecraft.entity.passive.EntityPig;
 import net.minecraft.entity.passive.EntitySheep;
 import net.minecraft.entity.passive.EntitySquid;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingDropsEvent;
 
 public class ZHUsefulDropsEvent
 {
     @ForgeSubscribe
     public void onEntityDrop(LivingDropsEvent event)
     {
         if(event.entityLiving.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
         {
             if (event.entityLiving instanceof EntitySquid)
             {
                 if (UsefulThings.enableMoreFoodFromAnimals.getBoolean(true))
                 {
                     if (event.entityLiving.isBurning())
                         event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.fishCooked, 1)));
                     else
                         event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.fishRaw, 1)));
                 }
                 if (event.recentlyHit && UsefulThings.morePassiveDrops.getBoolean(true))
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.dyePowder, 1)));
             }
             else if (event.entityLiving instanceof EntitySheep)
             {
                 if (UsefulThings.enableMoreFoodFromAnimals.getBoolean(true))
                 {
                     int count = 1;
                     
                     for (int i = 0; i < 2 + (1 * event.lootingLevel); i++)
                     {
                         if (Math.random() < 0.5d)
                             count++;
                     }
                     
                     if (event.entityLiving.isBurning())
                         event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(UsefulThings.muttonCooked, count)));
                     else
                         event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(UsefulThings.muttonRaw, count)));
                 }
             }
             else if (event.entityLiving instanceof EntityPig)
             {
                 if (UsefulThings.enableMoreFoodFromAnimals.getBoolean(true) || UsefulThings.morePassiveDrops.getBoolean(true))
                 {
                     int count = 1;
                     
                     for (int i = 0; i < 3 + (1 * event.lootingLevel); i++)
                     {
                         if (Math.random() < 0.5d)
                             count++;
                     }
                     
                     if (event.entityLiving.isBurning())
                     {
                         if (UsefulThings.enableMoreFoodFromAnimals.getBoolean(true))
                             event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(UsefulThings.ribsCooked, count)));
                         if (event.recentlyHit && UsefulThings.morePassiveDrops.getBoolean(true))
                             event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.porkCooked, 1)));
                     }
                     else
                     {
                         if (UsefulThings.enableMoreFoodFromAnimals.getBoolean(true))
                             event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(UsefulThings.ribsRaw, count)));
                         if (event.recentlyHit && UsefulThings.morePassiveDrops.getBoolean(true))
                             event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.porkRaw, 1)));
                     }
                 }
             }
             else if (event.entityLiving instanceof EntityChicken)
             {
                 if (UsefulThings.enableMoreFoodFromAnimals.getBoolean(true))
                 {
                     if (event.entityLiving.isBurning())
                         event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(UsefulThings.drumstickCooked, 2)));
                     else
                         event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(UsefulThings.drumstickRaw, 2)));
                 }
                 
                 if (event.recentlyHit && UsefulThings.morePassiveDrops.getBoolean(true))
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.feather, 1)));
                 
             }
             else if (event.entityLiving instanceof EntityCow)
             {
                 if (event.recentlyHit && UsefulThings.morePassiveDrops.getBoolean(true))
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.leather, 1)));
             }
             
             // TODO: fix the dropping of mob heads...
             else if (event.entityLiving instanceof EntitySkeleton)
             {
                 if (event.recentlyHit && UsefulThings.moreEnemyDrops.getBoolean(true))
                 {
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.bone, 1)));
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.arrow, 1)));
                 }
                 
                 if (event.recentlyHit && UsefulThings.enableNewTools.getBoolean(true) && event.source.getEntity() instanceof EntityPlayer)
                 {
                     ItemStack temp = ((EntityPlayer) event.source.getEntity()).getHeldItem();
                     
                     if (temp != null)
                     {
                         //formatter:off
                         if (temp.itemID == UsefulThings.scytheWood.itemID || temp.itemID == UsefulThings.scytheStone.itemID || temp.itemID == UsefulThings.scytheGold.itemID || temp.itemID == UsefulThings.scytheIron.itemID || temp.itemID == UsefulThings.scytheDiamond.itemID 
                                 || (UsefulThings.enableOres.getBoolean(true) && temp.itemID == UsefulThings.scytheSapphire.itemID) 
                                 || (UsefulThings.cactusTools.getBoolean(true) && temp.itemID == UsefulThings.scytheCactus.itemID) 
                                 || (UsefulThings.flintTools.getBoolean(true) && temp.itemID == UsefulThings.scytheFlint.itemID)
                                 )
                         //formatter:on
                         {
                             if (event.specialDropValue > 5)
                             {
                                 if (Math.random() < 0.25d)
                                 {
                                     if (((EntitySkeleton) event.entityLiving).getSkeletonType() == 1)
                                         event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.skull, 1, 1)));
                                     else
                                         event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.skull, 1, 0)));
                                 }
                             }
                         }
                     }
                 }
             }
             else if (event.entityLiving instanceof EntityZombie)
             {
                 if (event.recentlyHit && UsefulThings.moreEnemyDrops.getBoolean(true))
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.rottenFlesh, 1)));
                 
                 if (event.recentlyHit && UsefulThings.enableNewTools.getBoolean(true) && event.source.getEntity() instanceof EntityPlayer)
                 {
                     ItemStack temp = ((EntityPlayer) event.source.getEntity()).getHeldItem();
                     
                     if (temp != null)
                     {
                         //formatter:off
                         if (temp.itemID == UsefulThings.scytheWood.itemID || temp.itemID == UsefulThings.scytheStone.itemID || temp.itemID == UsefulThings.scytheGold.itemID || temp.itemID == UsefulThings.scytheIron.itemID || temp.itemID == UsefulThings.scytheDiamond.itemID 
                                 || (UsefulThings.enableOres.getBoolean(true) && temp.itemID == UsefulThings.scytheSapphire.itemID) 
                                 || (UsefulThings.cactusTools.getBoolean(true) && temp.itemID == UsefulThings.scytheCactus.itemID) 
                                 || (UsefulThings.flintTools.getBoolean(true) && temp.itemID == UsefulThings.scytheFlint.itemID)
                                 )
                         //formatter:on
                         {
                             if (event.specialDropValue > 5)
                             {
                                 if (Math.random() < 0.25d)
                                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.skull, 1, 2)));
                             }
                         }
                     }
                 }
             }
             else if (event.entityLiving instanceof EntityCreeper)
             {
                 if (event.recentlyHit && UsefulThings.moreEnemyDrops.getBoolean(true))
                    event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.gunpowder, 1)));
                 
                 if (event.recentlyHit && UsefulThings.enableNewTools.getBoolean(true) && event.source.getEntity() instanceof EntityPlayer)
                 {
                     ItemStack temp = ((EntityPlayer) event.source.getEntity()).getHeldItem();
                     
                     if (temp != null)
                     {
                         //formatter:off
                         if (temp.itemID == UsefulThings.scytheWood.itemID || temp.itemID == UsefulThings.scytheStone.itemID || temp.itemID == UsefulThings.scytheGold.itemID || temp.itemID == UsefulThings.scytheIron.itemID || temp.itemID == UsefulThings.scytheDiamond.itemID 
                                 || (UsefulThings.enableOres.getBoolean(true) && temp.itemID == UsefulThings.scytheSapphire.itemID) 
                                 || (UsefulThings.cactusTools.getBoolean(true) && temp.itemID == UsefulThings.scytheCactus.itemID) 
                                 || (UsefulThings.flintTools.getBoolean(true) && temp.itemID == UsefulThings.scytheFlint.itemID)
                                 )
                         //formatter:on
                         {
                             if (event.specialDropValue > 5)
                             {
                                 if (Math.random() < 0.25d)
                                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.skull, 1, 4)));
                             }
                         }
                     }
                 }
             }
             // TODO: Add player head dropping to scythe...
             else if (event.recentlyHit && UsefulThings.moreEnemyDrops.getBoolean(true))
             {
                 if (event.entityLiving instanceof EntityEnderman)
                 {
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.enderPearl, 1)));
                 }
                 else if (event.entityLiving instanceof EntityPigZombie)
                 {
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.rottenFlesh, 1)));
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.goldNugget, 1)));
                 }
                 else if (event.entityLiving instanceof EntitySlime)
                 {
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.slimeBall, 1)));
                 }
                 else if (event.entityLiving instanceof EntitySpider || event.entityLiving instanceof EntityCaveSpider)
                 {
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.silk, 1)));
                     
                     int count = 1;
                     for (int i = 0; i < event.lootingLevel; i++)
                     {
                         if (Math.random() < 0.5d)
                             count++;
                     }
                     
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.spiderEye, count)));
                 }
                 else if (event.entityLiving instanceof EntityMagmaCube)
                 {
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.magmaCream, 1)));
                 }
                 else if (event.entityLiving instanceof EntityGhast)
                 {
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.ghastTear, 1)));
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.gunpowder, 1)));
                 }
                 else if (event.entityLiving instanceof EntityBlaze)
                 {
                     event.entityLiving.capturedDrops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, new ItemStack(Item.blazeRod, 1)));
                 }
                 // TODO: Witch always drops 1 beneficial and 1 splash potion on death
                 // else if (event.entityLiving instanceof EntityWitch)
                 // {
                 // rand = Math.random();
                 // if (rand < 25)
                 // {
                 // //Splash Potion of Poison
                 // event.entityLiving.dropItem(Item.potion, 13688);
                 // }
                 // else if (rand > 25 && rand < 50)
                 // {
                 // //Splash Potion of Weakness
                 // event.entityLiving.dropItem(Item.potion, 16392);
                 // }
                 // else if (rand > 50 && rand < 75)
                 // {
                 // //Splash Potion of Slowness
                 // event.entityLiving.dropItem(Item.potion, 16394, 1);
                 // }
                 // else
                 // {
                 // //Splash Potion of Harming
                 // event.entityLiving.dropItem(Item.potion, 16394);
                 // }
                 //
                 // rand = Math.random();
                 // if (rand < 33)
                 // {
                 // //Potion of Fire Resistance
                 // event.entityLiving.dropItem(Item.potion, 13688);
                 // }
                 // else
                 // {
                 // //Potion of Fire Resistance
                 // event.entityLiving.dropItem(Item.potion, 16392);
                 // }
                 //
                 // }
             }
         }
     }
 }
