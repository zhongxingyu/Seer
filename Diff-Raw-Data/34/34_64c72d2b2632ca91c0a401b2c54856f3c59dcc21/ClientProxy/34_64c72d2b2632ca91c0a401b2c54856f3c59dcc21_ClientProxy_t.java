 package net.medsouz.omg.proxy;
 
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 import net.medsouz.omg.OpenModularGuns;
 import net.medsouz.omg.api.Gun;
 import net.medsouz.omg.gun.GunM16;
 import net.medsouz.omg.item.ItemGun;
 import net.medsouz.omg.item.ItemRenderGun;
 import net.medsouz.omg.util.Vector3;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.model.ModelBiped;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraftforge.client.IItemRenderer;
 import net.minecraftforge.client.MinecraftForgeClient;
 
 public class ClientProxy extends CommonProxy{
 	
 	public void load(){
 		
 	}
 	
 	public void registerRenderers() {
 		MinecraftForgeClient.registerItemRenderer(OpenModularGuns.m16.itemID, (IItemRenderer)new ItemRenderGun());
 	}
 	
	
	private static boolean fixArm = false;//arms need to be "fixed"
	
 	public static void bipedRotationHook(ModelBiped modelPlayer, Entity entity, float var3){
 		if(entity instanceof EntityPlayer){
 			EntityPlayer ep = (EntityPlayer)entity;
 			if(ep.getHeldItem() != null && ep.getHeldItem().getItem() instanceof ItemGun){
 				boolean inUse = (ep.getItemInUse() == ep.getHeldItem());
 				if(!(ep.equals(Minecraft.getMinecraft().renderViewEntity) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)){//TODO: Make weapon follow head's x and y rotations
 					ItemGun ig = (ItemGun)ep.getHeldItem().getItem();
 					Gun g = ig.gun;
 					Vector3 right = g.rightHandRot(new Vector3( modelPlayer.bipedRightArm.rotateAngleX, modelPlayer.bipedRightArm.rotateAngleY, modelPlayer.bipedRightArm.rotateAngleZ), inUse);
 					Vector3 rightOff = g.rightHandPos(new Vector3( modelPlayer.bipedRightArm.offsetX, modelPlayer.bipedRightArm.offsetY, modelPlayer.bipedRightArm.offsetZ), inUse);
 					modelPlayer.bipedRightArm.rotateAngleX = right.x;
 					modelPlayer.bipedRightArm.rotateAngleY = right.y;
 					modelPlayer.bipedRightArm.rotateAngleZ = right.z;
 					modelPlayer.bipedRightArm.offsetX = rightOff.x;
 					modelPlayer.bipedRightArm.offsetY = rightOff.y;
 					modelPlayer.bipedRightArm.offsetZ = rightOff.z;
 					if(g.isTwoHanded()){
 						Vector3 left = g.leftHandRot(new Vector3( modelPlayer.bipedLeftArm.rotateAngleX, modelPlayer.bipedLeftArm.rotateAngleY, modelPlayer.bipedLeftArm.rotateAngleZ), inUse);
 						Vector3 leftOff = g.leftHandPos(new Vector3( modelPlayer.bipedLeftArm.offsetX, modelPlayer.bipedLeftArm.offsetY, modelPlayer.bipedLeftArm.offsetZ), inUse);
 						modelPlayer.bipedLeftArm.rotateAngleX = left.x;
 						modelPlayer.bipedLeftArm.rotateAngleY = left.y;
 						modelPlayer.bipedLeftArm.rotateAngleZ = left.z;
 						modelPlayer.bipedLeftArm.offsetX = leftOff.x;
 						modelPlayer.bipedLeftArm.offsetY = leftOff.y;
 						modelPlayer.bipedLeftArm.offsetZ = leftOff.z;
 					}
					fixArm = true;
 				}
			}
			if(fixArm){
				modelPlayer.bipedRightArm.offsetX = 0F;
				modelPlayer.bipedRightArm.offsetY = 0F;
 				modelPlayer.bipedRightArm.offsetZ = 0F;
 				modelPlayer.bipedLeftArm.offsetX = 0F;
				modelPlayer.bipedLeftArm.offsetY = 0F;
				modelPlayer.bipedLeftArm.offsetZ = 0F;
				fixArm = false;
 			}
 		}
 		
 	}
 }
