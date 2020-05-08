 package ak.EnchantChanger;
 
 import java.io.DataOutputStream;
 
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemSword;
 import net.minecraft.world.World;
 import net.minecraftforge.client.IItemRenderer;
 import ak.EnchantChanger.Client.EcKeyHandler;
 import ak.EnchantChanger.Client.EcModelCloudSword2;
 import ak.EnchantChanger.Client.EcModelCloudSwordCore2;
 import ak.EnchantChanger.Client.EcModelSephirothSword;
 import ak.EnchantChanger.Client.EcModelUltimateWeapon;
 import ak.EnchantChanger.Client.EcModelZackSword;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 public class EcItemSword extends ItemSword implements IItemRenderer
 {
 	private boolean toggle = false;
 	public EcItemSword(int par1 , EnumToolMaterial toolMaterial)
 	{
 		super(par1, toolMaterial);
 		this.setMaxDamage(-1);
 		this.setTextureFile(EnchantChanger.EcSprites);
 	}
 	@Override
 	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
 		if(par3Entity instanceof EntityPlayer && par5)
 		{
 			if(par2World.isRemote)
 			{
				this.toggle = EcKeyHandler.MagicKeyDown && !this.toggle;
 				PacketDispatcher.sendPacketToServer(Packet_EnchantChanger.getPacketSword(this));
 			}
 			if(toggle)
 			{
 				doMagic(par1ItemStack, par2World, (EntityPlayer) par3Entity);
 			}
 		}
 	}
 	public static void doMagic(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
 	{
 		if(EnchantmentHelper.getEnchantmentLevel(EnchantChanger.EnchantmentMeteoId, par1ItemStack) > 0)
 		{
 			EcItemMateria.Meteo(par2World, par3EntityPlayer);
 		}
 		if(EnchantmentHelper.getEnchantmentLevel(EnchantChanger.EndhantmentHolyId, par1ItemStack) > 0)
 		{
 			EcItemMateria.Holy(par2World, par3EntityPlayer);
 		}
 		if(EnchantmentHelper.getEnchantmentLevel(EnchantChanger.EnchantmentTelepoId, par1ItemStack) > 0)
 		{
 			EcItemMateria.Teleport(par1ItemStack, par2World, par3EntityPlayer);
 		}
 		if(EnchantmentHelper.getEnchantmentLevel(EnchantChanger.EnchantmentThunderId, par1ItemStack) > 0)
 		{
 			EcItemMateria.Thunder(par2World, par3EntityPlayer);
 		}
 	}
 	public static boolean hasFloat(ItemStack itemstack)
 	{
 		if(EnchantmentHelper.getEnchantmentLevel(EnchantChanger.EnchantmentFloatId, itemstack) > 0)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	public Item setNoRepair()
 	{
 		canRepair = false;
 		return this;
 	}
 	@Override
 	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
 		return type == ItemRenderType.EQUIPPED;
 	}
 	@Override
 	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
 		return false;
 	}
 	@Override
 	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
 		EcModelUltimateWeapon UModel = new EcModelUltimateWeapon();
 		EcModelCloudSwordCore2 CCModel = new EcModelCloudSwordCore2();
 		EcModelCloudSword2 CModel = new EcModelCloudSword2();
 		EcModelSephirothSword SModel = new EcModelSephirothSword();
 		EcModelZackSword ZModel = new EcModelZackSword();
 		if(item.getItem() instanceof EcItemZackSword)
 			ZModel.renderItem(item, (EntityLiving) data[1]);
 		else if(item.getItem() instanceof EcItemCloudSword)
 			CModel.renderItem(item, (EntityLiving) data[1]);
 		else if(item.getItem() instanceof EcItemCloudSwordCore)
 			CCModel.renderItem(item, (EntityLiving) data[1], ((EcItemCloudSwordCore)item.getItem()).ActiveMode);
 		else if(item.getItem() instanceof EcItemSephirothSword || item.getItem() instanceof EcItemSephirothSwordImit)
 			SModel.renderItem(item, (EntityLiving) data[1]);
 		else if(item.getItem() instanceof EcItemUltimateWeapon)
 			UModel.renderItem(item, (EntityLiving) data[1]);
 	}
 	// パケットの読み込み(パケットの受け取りはPacketHandlerで行う)
 	public void readPacketData(ByteArrayDataInput data)
 	{
 		try
 		{
 			this.toggle = data.readBoolean();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	// パケットの書き込み(パケットの生成自体はPacketHandlerで行う)
 	public void writePacketData(DataOutputStream dos)
 	{
 		try
 		{
 			dos.writeBoolean(toggle);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
