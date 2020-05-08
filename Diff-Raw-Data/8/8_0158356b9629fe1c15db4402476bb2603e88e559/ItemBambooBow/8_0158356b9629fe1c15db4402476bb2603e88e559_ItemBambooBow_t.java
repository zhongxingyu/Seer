 package ruby.bamboo.item;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import ruby.bamboo.BambooCore;
 import ruby.bamboo.BambooInit;
 import ruby.bamboo.entity.EntityBambooSpear;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.ItemRenderer;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.client.renderer.texture.TextureManager;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemBow;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.world.World;
 import net.minecraftforge.client.IItemRenderer;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.entity.player.ArrowNockEvent;
 
 public class ItemBambooBow extends ItemBow implements IItemRenderer {
     private static final ResourceLocation ENCHANTED_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
     private final Icon icons[];
 
     public ItemBambooBow(int par1) {
         super(par1);
         this.maxStackSize = 1;
         this.setMaxDamage(300);
         icons = new Icon[3];
     }
 
     @Override
     public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, int par4) {
         int chargeFrame = this.getMaxItemUseDuration(par1ItemStack) - par4;
         boolean isNoResource = par3EntityPlayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, par1ItemStack) > 0;
 
         if (isNoResource || par3EntityPlayer.inventory.hasItem(BambooInit.bambooSpearIID)) {
             float power = chargeFrame / 10.0F;
             power = (power * power + power * 2.0F) / 3.0F;
 
             if (power < 0.1D) {
                 return;
             }
 
             if (power > 1.0F) {
                 power = 1.0F;
             }
 
             int slotNum = getInventorySlotContainItem(par3EntityPlayer, BambooInit.bambooSpearIID);
             int spearNum;
             int type = 0;
 
             if (slotNum > -1) {
                 type = par3EntityPlayer.inventory.mainInventory[slotNum].getItemDamage();
             }
 
             if (slotNum > -1 && !isNoResource) {
                 spearNum = par3EntityPlayer.inventory.mainInventory[slotNum].stackSize;
             } else {
                 spearNum = 64;
             }
 
             spearNum--;
             int limit = 5;
            int chargeTime = 8;
             EntityBambooSpear ebs;
             int attackCount;
             attackCount = chargeFrame / chargeTime > limit ? limit : chargeFrame / chargeTime;
             attackCount = attackCount < spearNum ? attackCount : spearNum;
 
             if (par3EntityPlayer.capabilities.isCreativeMode && par3EntityPlayer.capabilities.isFlying) {
                 attackCount = 12;
                 power = 1;
             }
 
             if (!par3EntityPlayer.isSneaking()) {
                 ebs = new EntityBambooSpear(par2World, par3EntityPlayer, power * 2.0F);
                 ebs.setDamage(1.5);
             } else {
                 ebs = new EntityBambooSpear(par2World, par3EntityPlayer, power * 4.0F);
                 ebs.setDamage(1 * (power > 0.8 ? power : 0.5) * 0.4);
             }
 
             if (attackCount > 0) {
                 ebs.setBarrage(attackCount);
             }
 
             if (power >= 1.0F) {
                 ebs.setIsCritical(true);
             }
 
             int enchantPower = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, par1ItemStack);
 
             if (enchantPower > 0) {
                 ebs.setDamage(ebs.getDamage() + enchantPower * 0.15D);
             }
 
             int enchantPunch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, par1ItemStack);
 
             if (enchantPunch > 0) {
                 ebs.setKnockbackStrength(enchantPunch);
             }
 
             if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, par1ItemStack) > 0) {
                 ebs.setFire(100);
             }
 
             par1ItemStack.damageItem(1, par3EntityPlayer);
             par2World.playSoundAtEntity(par3EntityPlayer, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + power * 0.5F);
 
             if (isNoResource) {
                 ebs.canBePickedUp = 2;
                 ebs.setMaxAge(60);
                 par1ItemStack.damageItem(attackCount * 2, par3EntityPlayer);
             } else {
                 par3EntityPlayer.inventory.mainInventory[slotNum].stackSize -= attackCount;
                 par3EntityPlayer.inventory.consumeInventoryItem(BambooInit.bambooSpearIID);
             }
 
             if (type == 1) {
                 ebs.setExplode();
             }
 
             if (!par2World.isRemote) {
                 par2World.spawnEntityInWorld(ebs);
             }
         }
     }
 
     private int getInventorySlotContainItem(EntityPlayer entity, int par1) {
         for (int var2 = 0; var2 < entity.inventory.mainInventory.length; ++var2) {
             if (entity.inventory.mainInventory[var2] != null && entity.inventory.mainInventory[var2].itemID == par1) {
                 return var2;
             }
         }
 
         return -1;
     }
 
     @Override
     public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
         ArrowNockEvent event = new ArrowNockEvent(par3EntityPlayer, par1ItemStack);
         MinecraftForge.EVENT_BUS.post(event);
         if (event.isCanceled()) {
             return event.result;
         }
         if (par3EntityPlayer.capabilities.isCreativeMode || par3EntityPlayer.inventory.hasItem(BambooInit.bambooSpearIID) || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, par1ItemStack) > 0) {
             par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
         }
 
         return par1ItemStack;
     }
 
     @Override
     public int getItemEnchantability() {
         return 1;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister) {
         this.itemIcon = par1IconRegister.registerIcon(BambooCore.resourceDomain + "bamboobow");
 
         for (int i = 0; i < icons.length; i++) {
             icons[i] = par1IconRegister.registerIcon(BambooCore.resourceDomain + "bamboobow_pull_" + i);
         }
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getItemIconForUseDuration(int par1) {
         return this.icons[par1];
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
         if (usingItem != null && usingItem.getItem().itemID == BambooInit.bambooBowIID) {
             int k = usingItem.getMaxItemUseDuration() - useRemaining;
 
            if (k >= 40) {
                 return getItemIconForUseDuration(2);
             }
 
            if (k > 25) {
                 return getItemIconForUseDuration(1);
             }
 
             if (k > 0) {
                 return getItemIconForUseDuration(0);
             }
         }
 
         return getIcon(stack, renderPass);
     }
 
     @Override
     public boolean handleRenderType(ItemStack item, ItemRenderType type) {
         if (type == ItemRenderType.EQUIPPED) {
             return true;
         }
         return false;
     }
 
     @Override
     public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
         return false;
     }
 
     @Override
     public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
         GL11.glPushMatrix();
         GL11.glTranslatef(0.6F, -0.6F, -0.25F);
         GL11.glRotatef(15.0F, 1.0F, 0.0F, 0.0F);
         GL11.glRotatef(-128.0F, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(-10.0F, 1.0F, 0.0F, 1.0F);
         render((EntityLivingBase) data[1], item, 0);
         GL11.glPopMatrix();
     }
 
     private void render(EntityLivingBase entityLivingBase, ItemStack itemStack, int renderPass) {
         GL11.glPushMatrix();
         TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
         Icon icon = entityLivingBase.getItemIcon(itemStack, renderPass);
 
         if (icon == null) {
             GL11.glPopMatrix();
             return;
         }
 
         texturemanager.bindTexture((texturemanager.getResourceLocation(itemStack.getItemSpriteNumber())));
         Tessellator tessellator = Tessellator.instance;
         float f = icon.getMinU();
         float f1 = icon.getMaxU();
         float f2 = icon.getMinV();
         float f3 = icon.getMaxV();
         float f4 = 0.0F;
         float f5 = 0.3F;
         GL11.glEnable(GL12.GL_RESCALE_NORMAL);
         GL11.glTranslatef(-f4, -f5, 0.0F);
         float f6 = 1.5F;
         GL11.glScalef(f6, f6, f6);
         GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
         GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
         ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
 
         if (itemStack.hasEffect(renderPass)) {
             GL11.glDepthFunc(GL11.GL_EQUAL);
             GL11.glDisable(GL11.GL_LIGHTING);
             texturemanager.bindTexture(ENCHANTED_ITEM_GLINT);
             GL11.glEnable(GL11.GL_BLEND);
             GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
             float f7 = 0.76F;
             GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
             GL11.glMatrixMode(GL11.GL_TEXTURE);
             GL11.glPushMatrix();
             float f8 = 0.125F;
             GL11.glScalef(f8, f8, f8);
             float f9 = Minecraft.getSystemTime() % 3000L / 3000.0F * 8.0F;
             GL11.glTranslatef(f9, 0.0F, 0.0F);
             GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
             ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
             GL11.glPopMatrix();
             GL11.glPushMatrix();
             GL11.glScalef(f8, f8, f8);
             f9 = Minecraft.getSystemTime() % 4873L / 4873.0F * 8.0F;
             GL11.glTranslatef(-f9, 0.0F, 0.0F);
             GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
             ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
             GL11.glPopMatrix();
             GL11.glMatrixMode(GL11.GL_MODELVIEW);
             GL11.glDisable(GL11.GL_BLEND);
             GL11.glEnable(GL11.GL_LIGHTING);
             GL11.glDepthFunc(GL11.GL_LEQUAL);
         }
 
         GL11.glDisable(GL12.GL_RESCALE_NORMAL);
         GL11.glPopMatrix();
     }
 }
