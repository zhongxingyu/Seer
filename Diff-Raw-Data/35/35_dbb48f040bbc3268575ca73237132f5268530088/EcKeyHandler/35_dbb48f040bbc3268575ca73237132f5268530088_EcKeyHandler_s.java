 package ak.EnchantChanger.Client;
 
 import java.util.EnumSet;
 

import ak.EnchantChanger.EcItemSword;
import ak.EnchantChanger.EnchantChanger;

 import net.minecraft.client.settings.KeyBinding;
import net.minecraft.src.*;
import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
 import cpw.mods.fml.common.TickType;
 
 public class EcKeyHandler extends KeyHandler
 {
 	public static boolean MagicKeyDown = false;
 	public EcKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings)
 	{
 		super(keyBindings, repeatings);
 	}
 
 	@Override
 	public String getLabel()
 	{
 		return "Enchant Changer : " + this.getClass().getSimpleName();
 	}
 
 	@Override
 	public void keyDown(EnumSet<TickType> types, KeyBinding kb,boolean tickEnd, boolean isRepeat)
 	{
 		if (types.equals(EnumSet.of(TickType.CLIENT)))
 		{
 			if(kb == ClientProxy.MagicKey)
 			{
 				this.MagicKeyDown = true;
 			}
 		}
 	}
 	@Override
 	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd)
 	{
 		if (types.equals(EnumSet.of(TickType.CLIENT)))
 		{
 			if(kb == ClientProxy.MagicKey)
 			{
 				this.MagicKeyDown = false;
 			}
 		}
 	}
 
 	@Override
 	public EnumSet<TickType> ticks()
 	{
 		return EnumSet.of(TickType.CLIENT);
 	}
 
 }
